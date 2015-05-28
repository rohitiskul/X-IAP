package com.rkcorp.github.cross.iap.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;

/**
 * Abstract manager which will act as a Parent for android/kindle iap managers.
 * Created by Rohit.Kulkarni on 3/5/15.
 */
public abstract class AbstractIAPManager {

    private Context mContext;

    private IAPListener mIapListener;

    private String mSkuInProgress;

    protected AbstractIAPManager(final Context context) {
        mContext = context;
    }

    /**
     * @return instance of application context passed in constructor of the class
     */
    protected Context context() {
        return mContext;
    }

    /**
     * Set listener to listen to IAP Managers
     *
     * @param listener IAP Listener common to any in-app model
     */
    public void setListener(final IAPListener listener) {
        mIapListener = listener;
    }

    protected IAPListener listener() {
        return mIapListener;
    }

    protected String skuInProgress() {
        return mSkuInProgress;
    }


    public void purchase(final String sku) {
        mSkuInProgress = sku;
        if (mIapListener != null)
            mIapListener.onPrePurchase();
    }

    public abstract void fetchInventory(final ArrayList<String> skuList);

    public abstract void onCreate(Activity activity);

    public abstract void onDestroy();

    public abstract void onActivityResult(int requestCode, int resultCode, Intent data);

    public abstract void restorePurchases();
}
