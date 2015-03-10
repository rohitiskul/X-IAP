package com.rkcorp.github.cross.iap.common;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Class will handle shared preferences related to in-app purchase.
 * Created by Rohit.Kulkarni on 3/5/15.
 */
public final class ExtraPrefs {

    public static final String VERSION = "version";
    public static final String USER = "user";
    public static final String MARKET_PLACE = "marketplace";
    private static final String IAP_PREFS = "com.cross.iap.IAP_PREFS";
    private SharedPreferences mSharedPreferences;

    public ExtraPrefs(Context context) {
        mSharedPreferences = context.getSharedPreferences(IAP_PREFS, Context.MODE_PRIVATE);
    }

    /**
     * Support for upgrading shared preferences
     *
     * @param newVersion version
     */
    public void upgrade(int newVersion) {
        int version = mSharedPreferences.getInt(VERSION, 0);
        if (newVersion > version) {
            mSharedPreferences.edit().putInt(VERSION, newVersion).apply();
        }
    }

    public ExtraPrefs setUser(final String user) {
        mSharedPreferences.edit().putString(USER, user).apply();
        return this;
    }

    public ExtraPrefs setMarketPlace(final String marketPlace) {
        mSharedPreferences.edit().putString(MARKET_PLACE, marketPlace).apply();
        return this;
    }
}
