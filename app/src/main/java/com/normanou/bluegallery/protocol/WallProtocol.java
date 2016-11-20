package com.normanou.bluegallery.protocol;

import com.android.volley.Request;
import com.android.volley.Response;
import com.normanou.bluegallery.entity.WallEntity;
import com.normanou.bluegallery.network.HtmlDecoderBase;
import com.normanou.bluegallery.network.HtmlRequest;
import com.normanou.bluegallery.network.RequestManager;
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
public class WallProtocol {

    private static final String HOST = "http://www.lofter.com/";
    private static final String WALL_URL = HOST + "wall";

    public static void getWall(Response.Listener<List<WallEntity>> listener, Response.ErrorListener errorListener, Object tag) {
        RequestManager.addRequest(new HtmlRequest<>(Request.Method.GET, WALL_URL, listener, errorListener,
                new WallEntityDecoder()), tag);
    }

    public static class WallEntityDecoder extends HtmlDecoderBase<List<WallEntity>> {
        @Override
        public List<WallEntity> decode(String html) throws HtmlException {
            List<WallEntity> wallEntities = new ArrayList<>();

            Document document = Jsoup.parse(html);

            Elements itemElements = document.select("ul#tagColList div.itm a.ztag");
            if (itemElements.isEmpty()) {
                throw new HtmlException("unexpected html structure");
            }

            for (int i = 0; i < itemElements.size(); i++) {
                WallEntity thisEntity = new WallEntity();

                Element thisElement = itemElements.get(i);
                thisEntity.detailUrl = HOST + thisElement.attr("href");
                thisEntity.imgUrl = thisElement.select("img.ztag").first().attr("src");
                thisEntity.name = thisElement.select("span.tag.ztag").first().text();

                if (thisEntity.isValid()) {
                    wallEntities.add(thisEntity);
                }
            }

            if (itemElements.size() != wallEntities.size()) {
                BLog.e("BLUE", "html structure might have changed");
            }

            if (wallEntities.size() == 0) {
                throw new HtmlException("unexpected html structure");
            }

            return wallEntities;
        }
    }
}
