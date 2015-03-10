package com.rkcorp.github.cross.iap.common.models;

/**
 * Sku Data will have info on Sku, everything about a Sku
 * Created by Rohit.Kulkarni on 3/5/15.
 */
public class SkuData {
    public String id;
    public String title;
    public String price;

    @Override
    public String toString() {
        return "id=" + id + ",title=" + title + ",price=" + price;
    }
}
