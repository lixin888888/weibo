/*
 * Copyright (C) 2012 http://lytsing.org
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

package org.lytsing.android.weibo;

import android.app.Application;
import android.app.ActivityManager;
import android.content.Context;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.BitmapAjaxCallback;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;

import org.lytsing.android.weibo.core.Session;
import org.lytsing.android.weibo.toolbox.BitmapLruCache;
import org.lytsing.android.weibo.toolbox.OkHttpStack;

/**
 * WeiboApplication.
 * @author Liqing Huang
 */
public class WeiboApplication extends Application {

    /**
     * Log or request TAG.
     */
    public static final String TAG = "Weibo";

    private static WeiboApplication sWeiboApplication;

    private Oauth2AccessToken mOauth2AccessToken;

    @Override
    public void onCreate() {
        super.onCreate();

        Logger.addLogAdapter(new AndroidLogAdapter());
        setWeiboApplication(this);

        mOauth2AccessToken = Session.restore(sWeiboApplication);

        int memClass = ((ActivityManager) sWeiboApplication.getSystemService(
                Context.ACTIVITY_SERVICE)).getMemoryClass();
        // Use 1/8th of the available memory for this memory cache.
        int cacheSize = 1024 * 1024 * memClass / 8;
        mImageLoader = new ImageLoader(getRequestQueue(), new BitmapLruCache(cacheSize));

        // set the max number of concurrent network connections, default is 4
        AjaxCallback.setNetworkLimit(8);

        // set the max number of icons (image width <= 50) to be cached in
        // memory, default is 20
        BitmapAjaxCallback.setIconCacheLimit(40);

        // set the max number of images (image width > 50) to be cached in
        // memory, default is 20
        BitmapAjaxCallback.setCacheLimit(40);

        // set the max size of an image to be cached in memory, default is 1600
        // pixels (ie. 400x400)
        BitmapAjaxCallback.setPixelLimit(400 * 400);

        // set the max size of the memory cache, default is 1M pixels (4MB)
        BitmapAjaxCallback.setMaxPixelLimit(2000000);
    }

    @Override
    public void onLowMemory() {

        // clear all memory cached images when system is in low memory
        // note that you can configure the max image cache count, see
        // CONFIGURATION
        super.onLowMemory();
        BitmapAjaxCallback.clearCache();
    }

    public static synchronized WeiboApplication getWeiboApplication() {
        return sWeiboApplication;
    }

    static void setWeiboApplication(WeiboApplication application) {
        sWeiboApplication = application;
    }

    public Oauth2AccessToken getOauth2AccessToken() {
        return mOauth2AccessToken;
    }

    public void setOauth2AccessToken(Oauth2AccessToken token) {
        this.mOauth2AccessToken = token;
    }

    /**
     * Global request queue for Volley.
     */
    private RequestQueue mRequestQueue;

    /**
     * @return The Volley Request queue, the queue will be created if it is null
     */
    public RequestQueue getRequestQueue() {
        // lazy initialize the request queue, the queue instance will be
        // created when it is accessed for the first time
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext(), new OkHttpStack());
        }

        return mRequestQueue;
    }

    private ImageLoader mImageLoader;

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    /**
     * Adds the specified request to the global queue, if tag is specified
     * then it is used else Default TAG is used.
     *
     * @param req request
     * @param tag log tag
     */
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);

        VolleyLog.d("Adding request to queue: %s", req.getUrl());

        getRequestQueue().add(req);
    }

    /**
     * Adds the specified request to the global queue using the Default TAG.
     *
     * @param req request
     */
    public <T> void addToRequestQueue(Request<T> req) {
        // set the default tag if tag is empty
        req.setTag(TAG);

        getRequestQueue().add(req);
    }

    /**
     * Cancels all pending requests by the specified TAG, it is important
     * to specify a TAG so that the pending/ongoing requests can be cancelled.
     *
     * @param tag tag of request
     */
    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
}

