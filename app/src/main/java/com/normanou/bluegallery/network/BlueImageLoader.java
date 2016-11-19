package com.normanou.bluegallery.network;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.android.volley.Cache;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;

import java.io.File;

/**
 * Created by bluewinter on 19/11/2016.
 */
public class BlueImageLoader extends ImageLoader {

    private static final String CACHE_DIR = "images";

    private Resources mResources;

    int mMaxImageHeight = 0;

    int mMaxImageWidth = 0;

    public BlueImageLoader(Context context, String cacheDir) {
        super(newRequestQueue(context, cacheDir), BitmapCache.getInstance());
        mResources = context.getResources();
        DisplayMetrics metrics = mResources.getDisplayMetrics();
        mMaxImageWidth = metrics.widthPixels;
        mMaxImageHeight = metrics.heightPixels;
    }

    private static RequestQueue newRequestQueue(Context context, String cacheDir) {
        RequestQueue requestQueue = new RequestQueue(openCache(context, cacheDir),
                new BasicNetwork(new HurlStack()));
        requestQueue.start();
        return requestQueue;
    }

    private static Cache openCache(Context context, String cacheDir) {
        return new DiskBasedCache(
                getDiskCacheDir(context, cacheDir),
                10 * 1024 * 1024);
    }

    public static File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath = getCacheDir(context);

        return new File(cachePath + File.separator + CACHE_DIR + File.separator + uniqueName);
    }

    private static String getCacheDir(Context context) {
        File file = context.getExternalCacheDir();
        if (file == null) {
            file = context.getCacheDir();
        }
        return file.getPath();
    }
}
