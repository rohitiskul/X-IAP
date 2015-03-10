package com.rkcorp.github.cross.iap.common;

import android.content.Context;
import android.content.Intent;

/**
 * Abstract manager which will act as a Parent for android/kindle iap managers.
 * Created by Rohit.Kulkarni on 3/5/15.
 */
public abstract class AbstractIAPManager {

    private Context mContext;

    private IAPListener mIapListener;

    private String mSkuInProgress;

    private boolean mIsConsumable;

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

    protected boolean skuIsConsumable() {
        return mIsConsumable;
    }


    public void purchase(final String sku, final boolean isConsumable) {
        mSkuInProgress = sku;
        mIsConsumable = isConsumable;
        if (mIapListener != null)
            mIapListener.onPrePurchase();
    }

    public abstract void fetchInventory(final String... skuList);

    public abstract void onCreate(Context context);

    public abstract void onDestroy();

    public abstract void onActivityResult(int requestCode, int resultCode, Intent data);

}
