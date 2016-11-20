package com.normanou.bluegallery.entity;

import android.text.TextUtils;

import com.normanou.bluegallery.network.HtmlDecoderBase;
import com.normanou.bluegallery.util.BLog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bluewinter on 19/11/2016.
 */
public class WallEntity {

    public String name;

    public String imgUrl;

    public String detailUrl;

    public boolean isValid() {
        return !TextUtils.isEmpty(name) && !TextUtils.isEmpty(imgUrl) && !TextUtils.isEmpty(detailUrl);
    }


}
