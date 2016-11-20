
package com.normanou.bluegallery.network;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.normanou.bluegallery.util.BLog;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


public class HtmlRequest<T> extends Request<T> {

    private static final String TAG = HtmlRequest.class.getSimpleName();

    private final Listener<T> mListener;

    private HtmlDecoderBase<T> mHtmlDecoder;

    public HtmlRequest(int method, String url, Listener<T> listener, ErrorListener errorListener,
                       HtmlDecoderBase<T> htmlDecoder) {
        super(method, url, errorListener);

        mListener = listener;
        // RetryPolicy retryPolicy = new DefaultRetryPolicy(5000,
        // 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        // setRetryPolicy(retryPolicy);

        mHtmlDecoder = htmlDecoder;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.98 Safari/537.36");
        return headers;
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        BLog.dLong(TAG, "response.statusCode is " + response.statusCode);
        String htmlString = "";
        try {
            htmlString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            T result = mHtmlDecoder.decode(htmlString);
            return Response.success(result, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (HtmlDecoderBase.HtmlException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(T response) {
        mListener.onResponse(response);
    }

}
