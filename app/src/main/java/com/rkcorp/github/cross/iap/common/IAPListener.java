package com.rkcorp.github.cross.iap.common;


import com.rkcorp.github.cross.iap.common.models.RestoreSku;
import com.rkcorp.github.cross.iap.common.models.SkuData;

import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;


/**
 * To listen to purchase callbacks
 * Created by Rohit.Kulkarni on 3/5/15.
 */
public interface IAPListener {

    public void onFetchInventory(final ArrayList<SkuData> availableSkus, final ArrayList<SkuData> unavailableSkus);

    public void onPrePurchase();

    public void onPurchaseSuccess(String sku, BasicNameValuePair... extraData);

    public void onPurchaseFailed(String sku, Reason reason);

    public void onConsumeSuccess(String sku);

    public void onConsumeFailed();

    public void onRestorePurchases(final ArrayList<RestoreSku> restoreSkus);
}
