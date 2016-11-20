
package com.normanou.bluegallery.network;

import java.util.IllegalFormatException;

public abstract class HtmlDecoderBase<T> {

    public abstract T decode(String html) throws HtmlException;

    public static class HtmlException extends Exception {
        public HtmlException(String s) {
            super(s);
        }
    }
}
