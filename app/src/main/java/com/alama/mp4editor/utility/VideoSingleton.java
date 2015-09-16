package com.alama.mp4editor.utility;

import com.squareup.otto.Bus;

/**
 * We are  making several calls to the EventBus object. Each call is experience.
 * And we all need one instance of the Bus t work accross our application.
 * Therefore we create a singleton for it.
 *
 * @author Alama Tounkara
 */
public class VideoSingleton {

    private static VideoSingleton mVideoSingleton = null;
    private final Bus mBus;

    private VideoSingleton() {
        mBus = new Bus();
    }

    public static VideoSingleton getInstance() {
        if (mVideoSingleton == null) {
            mVideoSingleton = new VideoSingleton();
        }
        return mVideoSingleton;
    }

    public Bus getBus() {
        return mBus;
    }

    public void registerMyBus(Object obj) {
        mBus.register(obj);
    }

    public void unRegisterMyBus(Object obj) {
        mBus.unregister(obj);
    }

    public void postMsg(AbstractEvent event) {
        mBus.post(event);
    }
}
