package com.android.waterdelivery;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class Utils {
    public static String fixNumber(Double number) {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);
        return df.format(number);
    }
}
