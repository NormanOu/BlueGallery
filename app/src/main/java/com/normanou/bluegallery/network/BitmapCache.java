/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.normanou.bluegallery.network;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Process;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.LruCache;

import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;


/**
 * This class holds our bitmap caches (memory and disk).
 */
public class BitmapCache implements ImageLoader.ImageCache {

    private static final boolean DEBUG = VolleyLog.DEBUG;

    private static final String TAG = BitmapCache.class.getName();

    // Default memory cache size as a percent of device memory class
    private static final float DEFAULT_MEM_CACHE_PERCENT = 0.1f;

    private LruCache<String, Bitmap> mMemoryCache;

    private static volatile BitmapCache INSTANCE = null;

    /**
     * Don't instantiate this class directly, use
     *
     * @param memCacheSize Memory cache size in KB.
     */
    private BitmapCache(int memCacheSize) {
        init(memCacheSize);
        VolleyLog.v("====== BitmapCache memCacheSize " + memCacheSize + " ==========");
    }

    public static BitmapCache getInstance() {
        if (INSTANCE == null) {
            synchronized (BitmapCache.class) {
                if (INSTANCE == null) {
                    INSTANCE = new BitmapCache(calculateMemCacheSize(DEFAULT_MEM_CACHE_PERCENT));
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Initialize the cache.
     */
    private void init(int memCacheSize) {
        // Set up memory cache
        mMemoryCache = new LruCache<String, Bitmap>(memCacheSize) {
            /**
             * Measure item size in kilobytes rather than units which is more
             * practical for a bitmap cache
             */
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                final int bitmapSize = getBitmapSize(bitmap) / 1024;
                return bitmapSize == 0 ? 1 : bitmapSize;
            }

            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue,
                                        Bitmap newValue) {
                /**
                 * Recycle the removed bitmap
                 */
                if (oldValue != null && !oldValue.isRecycled()) {
                    oldValue.recycle();
                }
            }
        };
    }

    /**
     * Adds a bitmap to both memory and disk cache.
     *
     * @param data   Unique identifier for the bitmap to store
     * @param bitmap The bitmap to store
     */
    public void addBitmapToCache(String data, Bitmap bitmap) {
        if (data == null || bitmap == null) {
            return;
        }

        synchronized (mMemoryCache) {
            // Add to memory cache
            if (mMemoryCache.get(data) == null || mMemoryCache.get(data).isRecycled()) {
                mMemoryCache.put(data, bitmap);
                // notify the listener
                if (mOnPutBitmapListener != null) {
                    mOnPutBitmapListener.onPutBitmap();
                }
            }
        }
    }

    /**
     * Get from memory cache.
     *
     * @param data Unique identifier for which item to get
     * @return The bitmap if found in cache, null otherwise
     */
    public Bitmap getBitmapFromMemCache(String data) {
        if (data != null) {
            synchronized (mMemoryCache) {
                final Bitmap memBitmap = mMemoryCache.get(data);
                if (memBitmap != null) {
                    return memBitmap;
                }
            }
        }
        return null;
    }

    /**
     * Trims the memory cache to target size.
     */
    public void trimToSize(int targetSize) {
        if (mMemoryCache != null) {
            mMemoryCache.trimToSize(targetSize);
        }
    }

    /**
     * Clears the memory cache.
     */
    public void clearCache() {
        if (mMemoryCache != null) {
            mMemoryCache.evictAll();
        }
    }

    /**
     * Returns the number of entries in the cache.
     */
    public int cachSize() {
        if (mMemoryCache != null) {
            return mMemoryCache.size();
        }

        return 0;
    }

    /**
     * Returns the maximum number of entries in the cache.
     */
    public int cacheMaxSize() {
        if (mMemoryCache != null) {
            return mMemoryCache.maxSize();
        }

        return 0;
    }

    /**
     * Sets the memory cache size based on a percentage of the max available VM
     * memory. Eg. setting percent to 0.2 would set the memory cache to one
     * fifth of the available memory. Throws {@link IllegalArgumentException} if
     * percent is < 0.05 or > .8. memCacheSize is stored in kilobytes instead of
     * bytes as this will eventually be passed to construct a LruCache which
     * takes an int in its constructor. This value should be chosen carefully
     * based on a number of factors Refer to the corresponding Android Training
     * class for more discussion:
     * http://developer.android.com/training/displaying-bitmaps/
     *
     * @param percent Percent of memory class to use to size memory cache
     * @return Memory cache size in KB
     */
    public static int calculateMemCacheSize(float percent) {
        if (percent < 0.05f || percent > 0.8f) {
            throw new IllegalArgumentException("setMemCacheSizePercent - percent must be "
                    + "between 0.05 and 0.8 (inclusive)");
        }
        return Math.max(Math.round(percent * Runtime.getRuntime().maxMemory() / 1024), 1024 * 10);
    }

    /**
     * Get the size in bytes of a bitmap.
     */
    public static int getBitmapSize(Bitmap bitmap) {
        return bitmap.getByteCount();
    }

    /**
     * Locate an existing instance of this Fragment or if not found, create and
     * add it using FragmentManager.
     *
     * @param fm The FragmentManager manager to use.
     * @return The existing instance of the Fragment or the new instance if just
     * created.
     */
    public static RetainFragment maintainInRetainFragment(FragmentManager fm) {
        // Check to see if we have retained the worker fragment.
        RetainFragment mRetainFragment = (RetainFragment) fm.findFragmentByTag(TAG);

        // If not retained (or first time running), we need to create and add
        // it.
        if (mRetainFragment == null) {
            mRetainFragment = new RetainFragment();
            mRetainFragment.setBitmapCache(BitmapCache.getInstance());
            fm.beginTransaction().add(mRetainFragment, TAG).commitAllowingStateLoss();
            fm.executePendingTransactions();
        }

        return mRetainFragment;
    }

    @Override
    public Bitmap getBitmap(String key) {
        return getBitmapFromMemCache(key);
    }

    @Override
    public void putBitmap(String key, Bitmap bitmap) {
        addBitmapToCache(key, bitmap);
    }

    /**
     * A simple non-UI Fragment that stores a single Object and is retained over
     * configuration changes. It will be used to retain the BitmapCache object.
     */
    public static class RetainFragment extends Fragment {
        private BitmapCache mBitmapCache;

        /**
         * Empty constructor as per the Fragment documentation
         */
        public RetainFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Obtain the memory info service
            mAm = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);

            // Make sure this Fragment is retained over a configuration change
            setRetainInstance(true);

            // Start up the worker thread.
            mThread.start();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mQuiting = true;
            mThread.interrupt();
            if (null != mBitmapCache) {
                mBitmapCache.clearCache();
            }
        }

        /**
         * Store a single object in this Fragment.
         *
         * @param cache The object to store
         */
        public void setBitmapCache(BitmapCache cache) {
            mBitmapCache = cache;
            mBitmapCache.setOnPutBitmapListener(new OnPutBitmapListener() {

                @Override
                public void onPutBitmap() {
                    mThread.interrupt();
                    detectMemory();
                }
            });
        }

        /**
         * Get the stored object.
         *
         * @return The stored object
         */
        public BitmapCache getBitmapCache() {
            return mBitmapCache;
        }

        ActivityManager mAm;

        public void detectMemory() {
            if (mBitmapCache == null) {
                return;
            }

            if (DEBUG) {
                VolleyLog.v("vz-detectMemory Check memory...");
            }
            if (isDeviceMemoryLow() || isHeapMemoryLow()) {
                final int cacheMaxSize = mBitmapCache.cacheMaxSize();
                mBitmapCache.trimToSize(cacheMaxSize / 2);
                if (DEBUG) {
                    VolleyLog.v("vz-detectMemory Trim bitmap cache to half size.");
                }
            }
        }

        public boolean isDeviceMemoryLow() {
            if (mAm != null) {
                MemoryInfo outInfo = new MemoryInfo();
                mAm.getMemoryInfo(outInfo);
                if (DEBUG) {
                    VolleyLog.v("Device memory:" + outInfo.availMem + " / " + outInfo.threshold);
                }
                if (outInfo.availMem <= outInfo.threshold) {
                    return true;
                }
            }
            return false;
        }

        public boolean isHeapMemoryLow() {
            final long maxMemory = Runtime.getRuntime().maxMemory();
            final long totalMemory = Runtime.getRuntime().totalMemory();
            final long freeMemory = Runtime.getRuntime().freeMemory();

            long availMem = maxMemory - (totalMemory - freeMemory);
            long threshold = (long) (maxMemory * 0.2f);
            if (DEBUG) {
                VolleyLog.v("Heap memory:" + availMem + " / " + threshold);
            }
            if (availMem <= threshold) {
                return true;
            }
            return false;
        }

        public void interruptToDetectMem() {
            if (mThread != null) {
                mInterruptToDeleteMem = true;
                mThread.interrupt();
            }
        }

        volatile boolean mQuiting = false;

        volatile boolean mInterruptToDeleteMem = false;

        final static int PERIOD = 5 * 60 * 1000; // 5 minutes.

        final Thread mThread = new Thread() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                // This thread runs almost forever.
                while (true) {

                    if (mQuiting) {
                        return;
                    }

                    try {
                        sleep(PERIOD);
                        detectMemory();
                    } catch (InterruptedException e) {
                        if (mQuiting) {
                            return;
                        }
                        if (mInterruptToDeleteMem) {
                            detectMemory();
                            mInterruptToDeleteMem = false;
                        }
                        continue;
                    }

                }
            }
        };
    }

    private OnPutBitmapListener mOnPutBitmapListener;

    public void setOnPutBitmapListener(OnPutBitmapListener listener) {
        mOnPutBitmapListener = listener;
    }

    public interface OnPutBitmapListener {
        public void onPutBitmap();
    }

}
