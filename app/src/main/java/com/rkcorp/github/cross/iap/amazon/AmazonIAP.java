package com.rkcorp.github.cross.iap.amazon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.amazon.device.iap.PurchasingListener;
import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.model.FulfillmentResult;
import com.amazon.device.iap.model.Product;
import com.amazon.device.iap.model.ProductDataResponse;
import com.amazon.device.iap.model.ProductType;
import com.amazon.device.iap.model.PurchaseResponse;
import com.amazon.device.iap.model.PurchaseUpdatesResponse;
import com.amazon.device.iap.model.Receipt;
import com.amazon.device.iap.model.UserDataResponse;
import com.rkcorp.github.cross.iap.common.AbstractIAPManager;
import com.rkcorp.github.cross.iap.common.ExtraPrefs;
import com.rkcorp.github.cross.iap.common.Reason;
import com.rkcorp.github.cross.iap.common.models.RestoreSku;
import com.rkcorp.github.cross.iap.common.models.SkuData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Amazon in-app purchase manager to manage kindle purchases.
 * Created by Rohit.Kulkarni on 3/5/15.
 */
public class AmazonIAP extends AbstractIAPManager implements PurchasingListener {

    public static final String TAG = "###AmazonIAP###";

    public AmazonIAP(Context context) {
        super(context);
    }

    @Override
    public void onCreate(Activity activity) {
        PurchasingService.registerListener(activity, this);
    }

    @Override
    public void purchase(String sku) {
        if (listener() != null)
            listener().onPrePurchase();
        super.purchase(sku);
        // Make purchase
        PurchasingService.purchase(sku);
    }

    @Override
    public void fetchInventory(ArrayList<String> sku) {
        Set<String> skuList = new HashSet<>();
        skuList.addAll(sku);
        PurchasingService.getProductData(skuList);
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    @Override
    public void restorePurchases() {
        //TODO
    }

    /**
     * Callback gives information about the user of device. User id and amazon marketplace.
     *
     * @param userDataResponse Will have information about user id and marketplace.
     */
    @Override
    public void onUserDataResponse(UserDataResponse userDataResponse) {
        Log.d(TAG, "userData = " + userDataResponse.toString());
        ExtraPrefs prefs = new ExtraPrefs(context().getApplicationContext());
        prefs.setUser(userDataResponse.getUserData().getUserId())
                .setMarketPlace(userDataResponse.getUserData().getMarketplace());
    }

    /**
     * Callback gives information about SKU (Price, Description, Title, Type)
     *
     * @param productDataResponse Information related to each product available on market
     */
    @Override
    public void onProductDataResponse(ProductDataResponse productDataResponse) {
        if (productDataResponse.getRequestStatus() == ProductDataResponse.RequestStatus.SUCCESSFUL) {
            if (listener() != null && productDataResponse.getProductData() != null) {
                final Map<String, Product> productMap = productDataResponse.getProductData();
                final ArrayList<SkuData> skuDataArrayList = new ArrayList<>();
                final ArrayList<SkuData> unavailableSkuDataList = new ArrayList<>();
                for (Map.Entry<String, Product> entry : productMap.entrySet()) {
                    System.out.println(entry.getKey() + " = " + entry.getValue());
                    SkuData sku = new SkuData();
                    sku.id = entry.getKey();
                    sku.title = entry.getValue().getTitle();
                    sku.price = entry.getValue().getPrice();
                    skuDataArrayList.add(sku);
                }
                if (productDataResponse.getUnavailableSkus() != null)
                    for (String skuId : productDataResponse.getUnavailableSkus()) {
                        SkuData sku = new SkuData();
                        sku.id = skuId;
                        unavailableSkuDataList.add(sku);
                    }
                listener().onFetchInventory(skuDataArrayList, unavailableSkuDataList);
            }
        }
    }

    /**
     * When user tries to purchase in-app item. Callback gives information on what happened with purchase.
     *
     * @param purchaseResponse If purchase is successful it will give info on receipts, purchase date time etc.
     */
    @Override
    public void onPurchaseResponse(PurchaseResponse purchaseResponse) {
        switch (purchaseResponse.getRequestStatus()) {
            case SUCCESSFUL:
                PurchasingService.notifyFulfillment(purchaseResponse.getReceipt().getReceiptId(), FulfillmentResult.FULFILLED);
                if (listener() != null) {
                    //Here in amazon purchase and consume is same
                    listener().onPurchaseSuccess(purchaseResponse.getReceipt().getSku(),
                            purchaseResponse.getReceipt().getReceiptId(), purchaseResponse.getRequestId().toString());
                    //If product is not an "Entitlement" then send callback for consume
                    if (purchaseResponse.getReceipt().getProductType() != ProductType.ENTITLED)
                        listener().onConsumeSuccess(purchaseResponse.getReceipt().getSku());
                }
                break;
            case FAILED:
                if (listener() != null)
                    listener().onPurchaseFailed(skuInProgress(), Reason.ERROR);
                break;
            case INVALID_SKU:
                if (listener() != null)
                    listener().onPurchaseFailed(skuInProgress(), Reason.ERROR);
                break;
            case ALREADY_PURCHASED:
                if (listener() != null)
                    listener().onPurchaseFailed(skuInProgress(), Reason.ERROR);
                break;
            case NOT_SUPPORTED:
                if (listener() != null)
                    listener().onPurchaseFailed(skuInProgress(), Reason.ERROR);
                break;
        }
    }

    /**
     * Callback used for inventory refresh or restore purchases etc.
     *
     * @param purchaseUpdatesResponse Purchase updates about sku
     */
    @Override
    public void onPurchaseUpdatesResponse(PurchaseUpdatesResponse purchaseUpdatesResponse) {
        if (purchaseUpdatesResponse.hasMore()) {
            PurchasingService.getPurchaseUpdates(false);
            Log.d(TAG, "onPurchaseUpdate more receipt to come sku consume to user account: " + purchaseUpdatesResponse.toString());
        } else {
            Log.d(TAG, "onPurchaseUpdate, can't consume sku to user account: " + purchaseUpdatesResponse.toString());
            final ArrayList<RestoreSku> restoreSkuList = new ArrayList<>();
            for (Receipt receipt : purchaseUpdatesResponse.getReceipts()) {
                if (receipt.getCancelDate() == null) {
                    final SkuData data = new SkuData();
                    data.id = receipt.getSku();
                    final RestoreSku restoreSku = new RestoreSku();
                    restoreSku.cancelDate = receipt.getCancelDate();
                    restoreSku.purchaseDate = receipt.getPurchaseDate();
                    restoreSku.receipt = receipt.getReceiptId();
                    restoreSku.sku = data;
                    restoreSkuList.add(restoreSku);
                }
            }
            if (listener() != null)
                listener().onRestorePurchases(restoreSkuList);
        }
    }
}
