package com.android.hq.livewallpaper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.SystemClock;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by heqiang on 17-5-27.
 */

public class SnowUtils {

    private Context mContext;


    private int SNOW_FLAKE_MAX_COUNT = 100;

    private float[] mAlphas = new float[]{0.5f,0.6f,0.8f,0.9f,1f};
    private float[] mSpeedFactors = new float[]{0.5f,0.7f,0.8f,0.9f,1f};
    private float[] mScaleFactors = new float[]{0.1f,0.2f,0.3f,0.4f,0.5f};

    private Random mRandom = new Random();
    private Paint mPaint = new Paint();
    private int mProduceNumPerTime = 2;

    private Bitmap mSnowBitmap;
    private ArrayList<SnowFlake> mSnowFlakeList;
    private Bitmap[] mBitmaps;
    private int mMaxSpeed = 30,mMinSpeed = 7;
    private int mHeight;
    private int mWidth;


    public SnowUtils(Context context){
        mContext = context;
    }

    public void init(int width, int height){
        mHeight = height;
        mWidth = width;
        initSnowFlakes();
    }

    private void initSnowFlakes(){
        mSnowBitmap = ((BitmapDrawable)(mContext.getResources().getDrawable(R.drawable.snow, mContext.getTheme()))).getBitmap();
        mBitmaps = new Bitmap[]{resizeBitmap(mScaleFactors[0]),resizeBitmap(mScaleFactors[1]),
                resizeBitmap(mScaleFactors[2]),resizeBitmap(mScaleFactors[3]),mSnowBitmap};
        mSnowFlakeList = new ArrayList<>(SNOW_FLAKE_MAX_COUNT);
        for(int i = 0; i < SNOW_FLAKE_MAX_COUNT; i++){
            SnowFlake snow = new SnowFlake();
            mSnowFlakeList.add(snow);
        }
    }

    private Bitmap resizeBitmap(float scale){
        Matrix m = new Matrix();
        m.setScale(scale, scale);
        Bitmap resizeBitmap = Bitmap.createBitmap(mSnowBitmap,0,0,mSnowBitmap.getWidth(),mSnowBitmap.getHeight(),m,true);
        return resizeBitmap;
    }

    private void updateSnowFlake(){
        for(int i = 0; i < mSnowFlakeList.size(); i++){
            SnowFlake snow = mSnowFlakeList.get(i);
            if(snow.isLive){
                long currentTime = SystemClock.uptimeMillis();
                int offsetY = (int)(((float)(currentTime - snow.startTimeVertical))/100 * snow.speedVertical);
                snow.y = snow.startY + offsetY;

                if(snow.y > mHeight){
                    snow.isLive = false;
                    if(snow.isInstant)
                        mSnowFlakeList.remove(i);
                }
            }
        }
    }

    public void draw(Canvas canvas){
        for(int i = 0; i < mSnowFlakeList.size(); i++) {
            SnowFlake snow = mSnowFlakeList.get(i);
            if(snow.isLive){
                int save = canvas.save();
                mPaint.setAlpha(snow.alpha);
                canvas.drawBitmap(mBitmaps[snow.index], snow.x, snow.y, mPaint);
                canvas.restoreToCount(save);
            }
        }
        updateSnowFlake();
    }

    public void produceSnowFlake(){
        int produceCount = 0;
        for(int i = 0; i < mSnowFlakeList.size(); i++){
            SnowFlake snow = mSnowFlakeList.get(i);
            if(!snow.isLive){
                int index = mRandom.nextInt(4);
                snow.isLive = true;
                snow.x = mRandom.nextInt(mWidth);
                snow.y = -100;
                snow.startX = snow.x;
                snow.startY = snow.y;
                snow.alpha = (int)(mAlphas[index]*255);//mRandom.nextInt(155)+100;
                snow.speedVertical = (int)((mRandom.nextInt(mMaxSpeed - mMinSpeed)+mMinSpeed) * mSpeedFactors[index]);

                snow.index = index;

                long currentTime = SystemClock.uptimeMillis();
                snow.startTimeHorizontal = snow.startTimeVertical = currentTime;

                produceCount++;
                if(produceCount >= mProduceNumPerTime){
                    break;
                }
            }
        }
    }

    public void produceInstantFlake(int x, int y){
        SnowFlake snow = new SnowFlake();
        snow.isInstant = true;
        snow.isLive = true;
        snow.index = 3;
        snow.x = x;
        snow.y = y;
        snow.startX = snow.x;
        snow.startY = snow.y;
        snow.alpha = 255;
        snow.speedVertical = 7;
        long currentTime = SystemClock.uptimeMillis();
        snow.startTimeHorizontal = snow.startTimeVertical = currentTime;
        mSnowFlakeList.add(snow);
    }

    public static class SnowFlake {
        public int startX;
        public int startY;
        public int x;
        public int y;
        public long startTimeVertical;
        public long startTimeHorizontal;
        public boolean isLive;
        public int alpha;
        public int speedVertical;
        public float scale;
        public int index;
        public boolean isInstant;

        public SnowFlake(){
            this.startX = 0;
            this.startY = 0;
            this.x = 0;
            this.y = 0;
            this.startTimeVertical = 0;
            this.alpha = 255;
            this.speedVertical = 0;
            this.isLive = false;
            this.scale = 1;
            this.isInstant = false;
        }
    }
}
