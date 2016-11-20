
package com.normanou.bluegallery.util;

import android.util.Log;

public class BLog {

    public static void d(String tag, String msg) {
        if (GlobalDebugControl.isDebug) {
            Log.d(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (GlobalDebugControl.isDebug) {
            Log.d(tag, msg);
        }
    }

    public static void dLong(String tag, String msg) {
        if (GlobalDebugControl.isDebug) {
            LongLogHelper.logLong(tag, msg);
        }
    }
}
