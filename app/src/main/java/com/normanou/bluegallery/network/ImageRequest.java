package com.normanou.bluegallery.network;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;

/**
 * Created by bluewinter on 21/11/2016.
 */
public class ImageRequest extends com.android.volley.toolbox.ImageRequest{
    public ImageRequest(String url, Response.Listener<Bitmap> listener, int maxWidth, int maxHeight, ImageView.ScaleType scaleType, Bitmap.Config decodeConfig, Response.ErrorListener errorListener) {
        super(url, listener, maxWidth, maxHeight, scaleType, decodeConfig, errorListener);
    }

    @Override
    protected Response<Bitmap> parseNetworkResponse(NetworkResponse response) {
        Response res = super.parseNetworkResponse(response);

        if (res.isSuccess() && res.cacheEntry.ttl == 0) {
            res.cacheEntry.softTtl = System.currentTimeMillis() + 30l * 24 * 60 * 60 * 1000; // cache for a month
            res.cacheEntry.ttl = res.cacheEntry.softTtl;
        }
        return res;
    }
}
