
package com.normanou.bluegallery.util;

import android.util.Log;

public class LongLogHelper {

    public static void logLong(String TAG, String sb) {
        if (sb.length() > 4000) {
            Log.v(TAG, "sb.length = " + sb.length());
            int chunkCount = sb.length() / 4000; // integer division
            for (int i = 0; i <= chunkCount; i++) {
                int max = 4000 * (i + 1);
                if (max >= sb.length()) {
                    Log.v(TAG, "chunk " + i + " of " + chunkCount + ":" + sb.substring(4000 * i));
                } else {
                    Log.v(TAG,
                            "chunk " + i + " of " + chunkCount + ":" + sb.substring(4000 * i, max));
                }
            }
        }
    }
}
