package com.tencent.tcr.xr;

import android.graphics.SurfaceTexture;

import android.util.Log;
import android.view.Surface;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * TODO:
 * 1.增加一个CustomVideoDecoder， 从WebRTC获取原始数据，解码到一个SurfaceTexture上面, 验证视频帧和Sei同步的情况下是否会抖, 会抖的话，继续下列动作。
 * 2.增加一个CustomVideoDecoder, 从WebRTC获取原始数据，解码以后丢到native层进行渲染, 依然会抖之后继续下列动作。
 * 3.把我们的逻辑搬到ALXR中。
 */
public class OpenXrTextureEglRenderer implements SurfaceTexture.OnFrameAvailableListener { 
    private static final String TAG = "OpenXrEglRenderer";
    private final SurfaceTexture mSurfaceTexture;
    private final Surface mSurface;
    // <captureTimeNs,displayTime>
    private final Map<Long, Long> videoFrameTime2DisplayTime = new ConcurrentHashMap<>();

    public OpenXrTextureEglRenderer(int textureId) {
        Log.i(TAG, "OpenXrTextureEglRenderer textureId:" + textureId);
        mSurfaceTexture = new SurfaceTexture(textureId);
        mSurfaceTexture.setOnFrameAvailableListener(this);
        mSurface = new Surface(mSurfaceTexture);
    }

    public Surface getSurface() {
        return mSurface;
    }

    public void setDefaultBufferSize(int width, int height) {
        mSurfaceTexture.setDefaultBufferSize(width, height);
    }

    // 记录上一次纹理更新的displayTime
    // 如果native层某次纹理更新没有正常更新，那么说明纹理还是上一次纹理，应该使用上一次的displayTime
    private long lastTextureDisplayTime;
    // 记录第一帧updateTexture()图像的displayTime。为了方便在日志里直观看到每帧的时间间隔。
    private long mStartDisplayTime;

    /**
     * 更新纹理，并且返回当前当前纹理视频帧对应时间的displayTime.
     */
    public long updateTexture() {
        if (mFrameAvailable) {
            mFrameAvailable = false;
            mSurfaceTexture.updateTexImage();
            return mSurfaceTexture.getTimestamp();
            // long pts = mSurfaceTexture.getTimestamp() / 1000;
            // Long displayTime = videoFrameTime2DisplayTime.remove(pts);
            // if (displayTime == null) {
            //     Log.e(TAG + "_handley", "updateTexture() draw pts=" + pts + " displayTime=" + displayTime);
            // } else {
            //     if (mStartDisplayTime == 0L) {
            //         mStartDisplayTime = displayTime;
            //         Log.i(TAG + "_handley", "updateTexture() mStartDisplayTime=" + mStartDisplayTime);
            //     }
            //     displayTime -= mStartDisplayTime;
            //     lastTextureDisplayTime = displayTime;
            //     Log.i(TAG + "_handley", "updateTexture() draw pts=" + pts + " displayTime=" + displayTime);
            // }
        }
        // return lastTextureDisplayTime + mStartDisplayTime;
        return 0;
    }

    // 标识mSurfaceTexture的Surface有没有更新
    private volatile boolean mFrameAvailable = true;
    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mFrameAvailable = true;
        Log.v(TAG, "onFrameAvailable()");
    }

    /**
     * 接收到SEI帧里面的displayTime, 这一帧对应的画面时间是videoFrameTimeMs.
     */
    public void onReceiveDisplayTime(long videoFrameTimeMs, long displayTime) {
        long pts = videoFrameTimeMs;
        if (mStartPts == 0L) {
            mStartPts = pts;
            Log.v(TAG + "_handley", "onReceiveDisplayTime() mStartPts=" + mStartPts);
        }
        pts -= mStartPts;
        //displayTime -= mStartDisplayTime;
        videoFrameTime2DisplayTime.put(pts, displayTime);
        Log.v(TAG + "_handley", "onReceiveDisplayTime() sei pts=" + pts + " displayTime=" + displayTime);
    }

    // 记录第一帧SEI的pts值。为了方便在日志里直观看到每帧的时间间隔。
    private long mStartPts;
}