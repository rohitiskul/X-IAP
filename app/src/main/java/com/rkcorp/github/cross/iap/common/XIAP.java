package com.rkcorp.github.cross.iap.common;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.rkcorp.github.cross.iap.amazon.AmazonIAP;
import com.rkcorp.github.cross.iap.android.GoogleIAP;


/**
 * Factory class for in-app purchases management
 * Created by Rohit.Kulkarni on 3/9/15.
 */
public class XIAP {


    public static final String XTRA_SIGNED_DATA = "signed_data";
    public static final String XTRA_SIGNATURE = "signature";

    private static XIAP singleton;
    private String[] mProducts;
    private AbstractIAPManager mIAPManager;

    private XIAP() {
    }

    XIAP(Application application, String[] products, String googleKey) {
        if (application == null) {
            throw new NullPointerException("Application instance must not be null");
        }
        mProducts = products;
        final PLATFORM platform = Build.MANUFACTURER.contains("Amazon") ? PLATFORM.AMAZON : PLATFORM.GOOGLE;
        switch (platform) {
            case AMAZON:
                mIAPManager = new AmazonIAP(application.getApplicationContext());
                break;
            case GOOGLE:
                mIAPManager = new GoogleIAP(application.getApplicationContext(), googleKey, mProducts);
                break;
        }
    }

    /**
     * @return singleton of {@link XIAP}
     */
    public static XIAP instance() {
        if (singleton == null) {
            synchronized (XIAP.class) {
                if (singleton == null) {
                    throw new NullPointerException("Singleton should be created using com.cross.iap.common.XIAP.Builder object");
                }
            }
        }
        return singleton;
    }

    /**
     * To create singleton using builder
     *
     * @param xiapBuilder Instance of Builder class
     */
    public static void create(Builder xiapBuilder) {
        singleton = new XIAP(xiapBuilder.mApplication, xiapBuilder.mProducts, xiapBuilder.mGoogleKey);
    }

    /**
     * Must be called in activity onCreate() or fragments onActivityCreated()
     *
     * @param context  activity context
     * @param listener if you want to listen to purchasing event
     */
    public void onCreate(Context context, IAPListener listener) {
        if (!(context instanceof Activity)) {
            throw new IllegalArgumentException("Context must be instance of activity when calling onCreate(..) ");
        }
        mIAPManager.setListener(listener);
        mIAPManager.onCreate(context);
    }

    /**
     * Activity result method of activity. Should be called in activities onActivityResult()
     *
     * @param requestCode requestCode
     * @param resultCode  resultCode
     * @param data        data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mIAPManager.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Initiate purchase
     *
     * @param sku          Name of the product on market
     * @param isConsumable Is this in-app product consumable
     */
    public void purchase(final String sku, final boolean isConsumable) {
        mIAPManager.purchase(sku, isConsumable);
    }

    /**
     * Refresh inventory. You should listen to {@link IAPListener}.onFetchInventory(availableSku, unavailableSku) method
     */
    public void fetchInventory() {
        mIAPManager.fetchInventory(mProducts);
    }

    /**
     * Destroy initialized purchasing manager
     */
    public void destroy() {
        mIAPManager.onDestroy();
    }


    enum PLATFORM {
        AMAZON, GOOGLE
    }

    public static final class Builder {

        private Application mApplication;
        private String[] mProducts;
        private String mGoogleKey;

        /**
         * Set application instance, this should be called in Application onCreate
         *
         * @param application Application instance
         * @return instance of {@link XIAP.Builder}
         */
        public Builder setApplication(Application application) {
            mApplication = application;
            return this;
        }

        /**
         * Set products ie. Sku list
         *
         * @param products Sku list params
         * @return instance of {@link XIAP.Builder}
         */
        public Builder setProducts(String... products) {
            mProducts = products;
            return this;
        }

        /**
         * Set google play store public key. Only applicable if you are using play store purchases.
         *
         * @param publicKey Google play public key.
         * @return instance of {@link XIAP.Builder}
         */
        public Builder setGooglePublicKey(String publicKey) {
            mGoogleKey = publicKey;
            return this;
        }

    }
}
