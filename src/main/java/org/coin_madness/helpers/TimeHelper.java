package org.coin_madness.helpers;

import java.util.Date;

public class TimeHelper {
    public static long getNowInMillis() {
        return new Date().getTime();
    }
}
