package com.rkcorp.github.cross.iap.common;

import com.rkcorp.github.cross.iap.common.models.RestoreSku;
import com.rkcorp.github.cross.iap.common.models.SkuData;

import java.util.ArrayList;

/**
 * To listen to purchase callbacks. Only needed callbacks.
 * Created by Rohit.Kulkarni on 3/10/15.
 */
public class IAPListenerAdapter implements IAPListener {

    @Override
    public void onFetchInventory(ArrayList<SkuData> availableSkus, ArrayList<SkuData> unavailableSkus) {

    }

    @Override
    public void onPrePurchase() {

    }

    @Override
    public void onPurchaseSuccess(String sku, String signedData, String signature) {

    }


    @Override
    public void onPurchaseFailed(String sku, Reason reason) {

    }

    @Override
    public void onConsumeSuccess(String sku) {

    }

    @Override
    public void onConsumeFailed() {

    }

    @Override
    public void onRestorePurchases(ArrayList<RestoreSku> restoreSkus) {

    }
}
