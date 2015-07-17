package com.rkcorp.github.cross.iap.common;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Build;

import com.rkcorp.github.cross.iap.amazon.AmazonIAP;
import com.rkcorp.github.cross.iap.android.GoogleIAP;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.WeakHashMap;


/**
 * Factory class for in-app purchases management
 * Created by Rohit.Kulkarni on 3/9/15.
 */
public final class XIAP {

    private static XIAP singleton;
    private String[] mConsumables;
    private String[] mNonConsumables;
    private PLATFORM mPlatform;
    private WeakHashMap<Activity, AbstractIAPManager> mIAPMap;
    private AbstractIAPManager mIAPManagerInUse;

    private XIAP() {
    }

    XIAP(Application application, String[] consumables, String[] nonConsumables, String googleKey) {
        if (application == null) {
            throw new NullPointerException("Application instance must not be null");
        }
        mIAPMap = new WeakHashMap<>();
        mConsumables = consumables;
        mNonConsumables = nonConsumables;
        mPlatform = Build.MANUFACTURER.contains("Amazon") ? PLATFORM.AMAZON : PLATFORM.GOOGLE;
        switch (mPlatform) {
            case AMAZON:
                //Do nothing
                break;
            case GOOGLE:
                GoogleIAP.initApp(application.getApplicationContext(), googleKey, mConsumables, mNonConsumables);
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
        singleton = new XIAP(xiapBuilder.mApplication, xiapBuilder.mConsumables, xiapBuilder.mNonConsumables, xiapBuilder.mGoogleKey);
    }

    /**
     * Must be called in activity onCreate() or fragments onActivityCreated()
     *
     * @param activity activity
     * @param listener if you want to listen to purchasing event
     */
    public void onCreate(Activity activity, IAPListener listener) {
        if (mIAPMap == null) {
            throw new NullPointerException("You must initialize XIAP from XIAP.create()");
        }
        switch (mPlatform) {
            case AMAZON:
                final AmazonIAP amazonIAP = new AmazonIAP(activity, mConsumables, mNonConsumables);
                amazonIAP.setListener(listener);
                amazonIAP.onCreate(activity);
                mIAPMap.put(activity, amazonIAP);

                mIAPManagerInUse = amazonIAP;
                break;
            case GOOGLE:
                final GoogleIAP googleIAP = new GoogleIAP(activity, mNonConsumables);
                googleIAP.setListener(listener);
                googleIAP.onCreate(activity);
                mIAPMap.put(activity, googleIAP);

                mIAPManagerInUse = googleIAP;
                break;
        }
    }

    /**
     * Activity result method of activity. Should be called in activities onActivityResult()
     *
     * @param requestCode requestCode
     * @param resultCode  resultCode
     * @param data        data
     */
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (mIAPManagerInUse == null)
            return;
        mIAPManagerInUse.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Initiate purchase
     *
     * @param sku Name of the product on market
     */
    public void purchase(final String sku) {
        if (mIAPManagerInUse == null)
            return;
        mIAPManagerInUse.purchase(sku);
    }

    /**
     * Refresh inventory. You should listen to {@link IAPListener}.onFetchInventory(availableSku, unavailableSku) method
     */
    public void fetchInventory() {
        if (mIAPManagerInUse == null)
            return;
        final ArrayList<String> products = new ArrayList<>();
        products.addAll(Arrays.asList(mConsumables));
        products.addAll(Arrays.asList(mNonConsumables));
        mIAPManagerInUse.fetchInventory(products);
    }

    /**
     * Restore purchases. Listen to
     */
    public void restorePurchases() {
        if (mIAPManagerInUse == null)
            return;
        mIAPManagerInUse.restorePurchases();
    }

    /**
     * Pause XIAP manager instance when activity is paused
     *
     * @param activity activity instance
     */
    public void onPause(final Activity activity) {
        if (mIAPMap == null)
            return;
        mIAPMap.put(activity, mIAPManagerInUse);
    }

    /**
     * Resumes XIAP manager instance when activity is resumed
     *
     * @param activity
     */
    public void onResume(final Activity activity) {
        if (mIAPMap == null)
            return;
        final AbstractIAPManager iapManager = mIAPMap.get(activity);
        if (iapManager == null)
            return;
        mIAPManagerInUse = iapManager;
    }

    /**
     * Destroy initialized managers
     */
    public void onDestroy(final Activity activity) {
        if (mIAPMap == null)
            return;
        final AbstractIAPManager iapManager = mIAPMap.get(activity);
        if (iapManager == null)
            return;
        iapManager.setListener(null);
        iapManager.onDestroy();
        mIAPMap.remove(activity);
    }

    /**
     * Completely kill XIAP. No use after this is called.
     */
    public void kill() {
        if (mIAPMap == null)
            return;
        mIAPManagerInUse = null;
        mIAPMap.clear();
        mIAPMap = null;
    }

    enum PLATFORM {
        AMAZON, GOOGLE
    }

    public static final class Builder {

        private Application mApplication;
        private String[] mConsumables;
        private String[] mNonConsumables;
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
        public Builder setConsumables(String... products) {
            mConsumables = products;
            return this;
        }

        public Builder setNonConsumables(String... nonConsumables) {
            mNonConsumables = nonConsumables;
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
