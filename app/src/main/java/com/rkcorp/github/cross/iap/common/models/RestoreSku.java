package com.rkcorp.github.cross.iap.common.models;

import java.util.Date;

/**
 * Restore sku is model related to restoring purchases for skus.
 * Created by Rohit.Kulkarni on 3/5/15.
 */
public class RestoreSku {
    public SkuData sku;
    public String receipt;
    public Date purchaseDate;
    public Date cancelDate;

    @Override
    public String toString() {
        return "sku=" + sku +
                ",receipt=" + receipt +
                ",purchaseDate=" + purchaseDate +
                ",cancelDate=" + cancelDate;
    }
}
