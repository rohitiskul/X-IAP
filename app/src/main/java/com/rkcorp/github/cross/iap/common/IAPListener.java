package com.rkcorp.github.cross.iap.common;


import com.rkcorp.github.cross.iap.common.models.RestoreSku;
import com.rkcorp.github.cross.iap.common.models.SkuData;

import java.util.ArrayList;


/**
 * To listen to purchase callbacks
 * Created by Rohit.Kulkarni on 3/5/15.
 */
public interface IAPListener {

    void onFetchInventory(final ArrayList<SkuData> availableSkus, final ArrayList<SkuData> unavailableSkus);

    void onPrePurchase();

    void onPurchaseSuccess(String sku, String signedData, String signature);

    void onPurchaseFailed(String sku, Reason reason);

    void onConsumeSuccess(String sku);

    void onConsumeFailed();

    void onRestorePurchases(final ArrayList<RestoreSku> restoreSkus);
}
