package com.normanou.bluegallery.protocol;

import com.android.volley.Request;
import com.android.volley.Response;
import com.normanou.bluegallery.entity.TagEntity;
import com.normanou.bluegallery.network.HtmlDecoderBase;
import com.normanou.bluegallery.network.HtmlRequest;
import com.normanou.bluegallery.network.RequestManager;
import com.normanou.bluegallery.util.BLog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by bluewinter on 20/11/2016.
 */
public class TagProtocol {
    private static final String TAG = "TagProtocol";

    public static void getTags(String url, Response.Listener<List<TagEntity>> listener, Response.ErrorListener errorListener, Object tag) {
//        RequestManager.addRequest(new HtmlRequest<>(Request.Method.GET, "http://www.lofter.com/wall", listener, errorListener,
//                new TagEntityDecoderFake()), tag);
        RequestManager.addRequest(new HtmlRequest<>(Request.Method.GET, url, listener, errorListener,
                new TagEntityDecoder()), tag);
    }

//    public static class TagEntityDecoderFake extends HtmlDecoderBase<List<TagEntity>> {
//        @Override
//        public List<TagEntity> decode(String html) throws HtmlException {
//            List<WallEntity> wallEntities = new WallProtocol.WallEntityDecoder().decode(html);
//
//            if (wallEntities == null) {
//                return null;
//            }
//
//            List<TagEntity> tagEntities = new ArrayList<>(wallEntities.size());
//            for (WallEntity wall : wallEntities) {
//                TagEntity tag = new TagEntity();
//                tag.imgUrl = wall.imgUrl;
//                tag.msg = wall.name;
//
//                tagEntities.add(tag);
//            }
//
//            return tagEntities;
//        }
//    }

    private static final String URL_PATTEN = "https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,4}\\b([-a-zA-Z0-9@:;%_\\+.~#?&//=]*)";
    private static final String URL_PREFIX = "firstImageUrl\":\"\\[\\\\\"";
    private static final String URL_POSTFIX = "";

    public static class TagEntityDecoder extends HtmlDecoderBase<List<TagEntity>> {
        @Override
        public List<TagEntity> decode(String html) throws HtmlException {
            List<TagEntity> entities = new ArrayList<>();
            Document document = Jsoup.parse(html);

            Elements itemElements = document.select("textarea[name=js]");

            if (itemElements.size() > 0) {
                String content = itemElements.get(0).html();

                try {
                    content = URLDecoder.decode(content, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                BLog.d("BLUE", content);
                Pattern p = Pattern.compile(URL_PREFIX + "(" + URL_PATTEN + ")" + URL_POSTFIX);
                Matcher m = p.matcher(content);

                while (m.find()) {
                    TagEntity entity = new TagEntity();
                    entity.imgUrl = m.group(1);
                    BLog.d("BLUE", "url is " + m.group(1));

                    entities.add(entity);
                }
            } else {
                throw new HtmlException("unexpected html structure");
            }

            if (entities.size() == 0) {
                throw new HtmlException("unexpected html structure");
            }
            return entities;
        }
    }
}
