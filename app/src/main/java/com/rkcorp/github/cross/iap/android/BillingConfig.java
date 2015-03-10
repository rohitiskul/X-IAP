package com.rkcorp.github.cross.iap.android;

import org.solovyev.android.checkout.Billing;

/**
 * Billing config for android play store.
 * Created by Rohit.Kulkarni on 3/9/15.
 */
public class BillingConfig extends Billing.DefaultConfiguration {

    private String mPublicKey;

    public BillingConfig(String key) {
        mPublicKey = key;
    }

    @Override
    public String getPublicKey() {
        return mPublicKey;
    }


    @Override
    public boolean isAutoConnect() {
        return false;
    }
}
