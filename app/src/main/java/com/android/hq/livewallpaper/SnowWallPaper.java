package com.android.hq.livewallpaper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.SurfaceHolder;


/**
 * Created by heqiang on 17-5-15.
 */

public class SnowWallPaper extends WallpaperService {
    private final static String TAG = "WallpaperService";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public Engine onCreateEngine() {
        return new SnowEngine();
    }

    public class SnowEngine extends Engine {
        private SnowUtils mSnowUtils;
        private Handler mHandler = new MyHandler();
        Bitmap mBackground;

        public static final int MSG_DRAW_FRAME = 1;
        public static final int MSG_PRODUCE_SNOW = 2;

        public SnowEngine() {
            super();
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            setTouchEventsEnabled(true);
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                mSnowUtils.produceInstantFlake((int)event.getX(), (int)event.getY());
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if (visible) {
                mHandler.obtainMessage(MSG_DRAW_FRAME).sendToTarget();
                mHandler.obtainMessage(MSG_PRODUCE_SNOW).sendToTarget();
            }
            else {
                mHandler.removeCallbacksAndMessages(null);
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            drawFrame();
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
            super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
            drawFrame();
        }

        private void drawFrame(){

            SurfaceHolder sh = getSurfaceHolder();
            final Rect frame = sh.getSurfaceFrame();
            final int dw = frame.width();
            final int dh = frame.height();

            if(mSnowUtils == null){
                mSnowUtils = new SnowUtils(getApplicationContext());
                mSnowUtils.init(dw, dh);
            }

            if (mBackground == null) {
                mBackground = BitmapFactory.decodeResource(getResources(),R.drawable.snow_bg);
            }

            Canvas c = sh.lockCanvas();
            if(c != null){
                try {
                    drawBackground(c,dw,dh);
                    mSnowUtils.draw(c);
                }finally {
                    sh.unlockCanvasAndPost(c);
                }
            }
        }

        private void drawBackground(Canvas c,int w, int h){
            if (mBackground != null) {
                RectF dest = new RectF(0, 0, w, h);
                c.drawBitmap(mBackground, null, dest, null);
            }
        }

        public class MyHandler extends Handler{
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case MSG_DRAW_FRAME:
                        drawFrame();
                        removeMessages(MSG_DRAW_FRAME);
                        sendMessageDelayed(obtainMessage(MSG_DRAW_FRAME), 50);
                        break;
                    case MSG_PRODUCE_SNOW:
                        mSnowUtils.produceSnowFlake();
                        removeMessages(MSG_PRODUCE_SNOW);
                        sendMessageDelayed(obtainMessage(MSG_PRODUCE_SNOW), 1000);
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
