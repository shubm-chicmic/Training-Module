package com.chicmic.trainingModule.Util;

public class RatingUtil {
    public static Float roundOff_Rating(double rating) {
        int dat = (int) (rating * 10);
        float val = 0f;
        if ((dat % 10) == 0) val = dat / 10;
        else {
            if ((dat % 10) < 5) val = dat / 10;
            else {
                val = (float) (dat / 10) + 0.5f;
            }
        }
        return val;
    }
}
