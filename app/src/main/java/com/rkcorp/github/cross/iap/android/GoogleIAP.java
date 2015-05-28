package com.rkcorp.github.cross.iap.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.rkcorp.github.cross.iap.common.AbstractIAPManager;
import com.rkcorp.github.cross.iap.common.Reason;
import com.rkcorp.github.cross.iap.common.models.RestoreSku;
import com.rkcorp.github.cross.iap.common.models.SkuData;

import org.solovyev.android.checkout.ActivityCheckout;
import org.solovyev.android.checkout.Billing;
import org.solovyev.android.checkout.BillingRequests;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.Inventory;
import org.solovyev.android.checkout.ProductTypes;
import org.solovyev.android.checkout.Products;
import org.solovyev.android.checkout.Purchase;
import org.solovyev.android.checkout.RequestListener;
import org.solovyev.android.checkout.ResponseCodes;
import org.solovyev.android.checkout.Sku;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Google play store purchasing manager
 * Created by Rohit.Kulkarni on 3/5/15.
 */
public class GoogleIAP extends AbstractIAPManager implements Inventory.Listener {

    public static final String TAG = "###GoogleIAP###";
    private static Checkout applicationCheckout;
    private ActivityCheckout activityCheckout;
    private Inventory inventory;
    private List<String> nonConsumables;

    public GoogleIAP(Context context, String[] nonConsumables) {
        super(context);
        this.nonConsumables = Arrays.asList(nonConsumables);
    }

    public static void initApp(final Context context, final String googleKey, final String[] consumables, final String[] nonConsumables) {
        final ArrayList<String> products = new ArrayList<>();
        products.addAll(Arrays.asList(consumables));
        products.addAll(Arrays.asList(nonConsumables));
        final Billing billing = new Billing(context, new BillingConfig(googleKey));
        applicationCheckout = Checkout.forApplication(billing, Products.create().add(ProductTypes.IN_APP, products));
    }

    @Override
    public void purchase(final String sku) {
        if (listener() != null)
            listener().onPrePurchase();
        super.purchase(sku);
        activityCheckout.whenReady(new Checkout.ListenerAdapter() {
            @Override
            public void onReady(BillingRequests requests) {
                requests.purchase(ProductTypes.IN_APP, sku, null, activityCheckout.getPurchaseFlow());
            }
        });
    }

    @Override
    public void fetchInventory(ArrayList<String> skuList) {
        inventory = activityCheckout.loadInventory();
        inventory.whenLoaded(this);
    }

    private void consume(final String token, final RequestListener<Object> onConsumed) {
        activityCheckout.whenReady(new Checkout.ListenerAdapter() {
            @Override
            public void onReady(BillingRequests requests) {
                requests.consume(token, onConsumed);
            }
        });
    }

    @Override
    public void onCreate(Activity activity) {
        activityCheckout = Checkout.forActivity(activity, applicationCheckout);
        activityCheckout.start();
        activityCheckout.createPurchaseFlow(new PurchaseListener());
        inventory = activityCheckout.loadInventory();
        inventory.whenLoaded(this);
    }

    @Override
    public void onDestroy() {
        activityCheckout.destroyPurchaseFlow();
        activityCheckout.stop();
        activityCheckout = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        activityCheckout.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void restorePurchases() {
        inventory.load().whenLoaded(GoogleIAP.this);
    }

    @Override
    public void onLoaded(Inventory.Products products) {
        final Inventory.Product product = products.get(ProductTypes.IN_APP);
        final ArrayList<SkuData> availableSku = new ArrayList<>();
        for (Sku sku : product.getSkus()) {
            SkuData skuData = new SkuData();
            skuData.id = sku.id;
            skuData.price = sku.price;
            skuData.title = sku.title;
            availableSku.add(skuData);
        }
        if (listener() != null)
            listener().onFetchInventory(availableSku, null);
        final ArrayList<RestoreSku> restoreSkuList = new ArrayList<>();
        for (Purchase purchase : product.getPurchases()) {
            final SkuData data = new SkuData();
            data.id = purchase.sku;
            final RestoreSku restoreSku = new RestoreSku();
            restoreSku.purchaseDate = new Date(purchase.time);
            restoreSku.receipt = purchase.toJson(true);
            restoreSku.sku = data;
            restoreSkuList.add(restoreSku);
        }
        if (listener() != null)
            listener().onRestorePurchases(restoreSkuList);
        if (product.supported) {
            for (Sku sku : product.getSkus()) {
                final Purchase purchase = product.getPurchaseInState(sku, Purchase.State.PURCHASED);
                if (purchase == null)
                    continue;
                if (!nonConsumables.contains(purchase.sku))
                    consume(purchase.token, new ConsumeListener(sku.id));
            }
        }
    }

    private class PurchaseListener implements RequestListener<Purchase> {

        @Override
        public void onSuccess(Purchase purchase) {
            // let's update purchase information in local inventory
            if (!nonConsumables.contains(purchase.sku))
                consume(purchase.token, new ConsumeListener(purchase.sku));
            if (listener() != null) {
                listener().onPurchaseSuccess(purchase.sku, purchase.toJson(), purchase.signature);
            }
        }

        @Override
        public void onError(int response, Exception e) {
            // it is possible that our data is not synchronized with data on Google Play => need to handle some errors
            if (response == ResponseCodes.ITEM_ALREADY_OWNED) {
                inventory.load().whenLoaded(GoogleIAP.this);
            }
            if (listener() != null)
                listener().onPurchaseFailed(skuInProgress(), Reason.ERROR);
        }
    }

    private class ConsumeListener implements RequestListener<Object> {

        String prodId;

        public ConsumeListener(String productId) {
            this.prodId = productId;
        }

        @Override
        public void onSuccess(Object result) {
            if (listener() != null)
                listener().onConsumeSuccess(prodId);
        }

        @Override
        public void onError(int response, Exception e) {
            // it is possible that our data is not synchronized with data on Google Play => need to handle some errors
            if (response == ResponseCodes.ITEM_NOT_OWNED) {
                inventory.load().whenLoaded(GoogleIAP.this);
            } else {
                if (listener() != null)
                    listener().onConsumeFailed();
            }
        }
    }
}
