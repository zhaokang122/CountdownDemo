package com.peter.countdownview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

public class CountdownView extends View {
    //最大倒计时数
    private int maxCount = 30 * 60;
    //当前倒计时数
    private int currentCount = maxCount;
    //区域宽度和高度
    private float mWidth, mHeight;
    private Context mContext;
    //画图
    private Paint mPaint;
    private Paint mCirclePaint;
    private Paint mTextPaint;
    private Paint mLinePaint;

    private float mRadius;
    private float circleWidth;
    private RectF bgRect;
    private int circleBackgroundColor;
    private int circleColor;
    private int textColor;


    public CountdownView(Context context) {
        this(context, null);
    }

    public CountdownView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CountdownView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;

        initSource(attrs);
        initPaint();
    }

    private void initSource(AttributeSet attrs) {
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.CountdownView);

        circleBackgroundColor = typedArray.
                getColor(R.styleable.CountdownView_cv_circle_bg_color, ContextCompat.getColor(mContext, R.color.defaultCircleBackgroundColor));
        circleColor = typedArray.
                getColor(R.styleable.CountdownView_cv_circle_color, ContextCompat.getColor(mContext, R.color.defaultCircleColor));
        textColor = typedArray.
                getColor(R.styleable.CountdownView_cv_text_color, ContextCompat.getColor(mContext, R.color.defaultTextColor));

        typedArray.recycle();
    }

    private void initPaint() {
        mPaint = new Paint();
        mCirclePaint = new Paint();
        mLinePaint = new Paint();
        mTextPaint = new Paint();

        mPaint.setAntiAlias(true);
        mPaint.setColor(circleBackgroundColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setColor(circleColor);

        mLinePaint.setAntiAlias(true);
        mLinePaint.setColor(circleBackgroundColor);

        Typeface font = Typeface.createFromAsset(mContext.getAssets(), "digital_7.ttf");

        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(textColor);
        mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTypeface(font);
        mTextPaint.setTextSize(20);

        bgRect = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        bgRect.set(mWidth / 2 - mRadius,
                mHeight / 2 - mRadius,
                mWidth / 2 + mRadius,
                mHeight / 2 + mRadius);

        float section = (currentCount * 1.0f) / maxCount;
        if (section > 1) section = 1;
        if (section < 0) section = 0;
        //画底部圆
        mPaint.setStrokeWidth(circleWidth);
        canvas.drawArc(bgRect, 270, 360, false, mPaint);

        //画进度圆
        mPaint.setColor(circleColor);
        canvas.drawArc(bgRect, 270, section * 360, false, mPaint);
        mPaint.setColor(circleBackgroundColor);

        //画进度点圆
        canvas.drawCircle(getCX(section - 0.25f, mRadius), getCY(section - 0.25f, mRadius), circleWidth * 2, mCirclePaint);

        //画齿轮
        mLinePaint.setStrokeWidth(circleWidth / 2);
        float gearRadius = (mRadius - circleWidth * 2);
        int gearCount = (int) (gearRadius / 4);
        float gearWidth = circleWidth * 3;
        for (float i = 0; i < gearCount; i++) {
            float n = i / gearCount;
            canvas.drawLine(getCX(n, gearRadius), getCY(n, gearRadius),
                    getCX(n, gearRadius - gearWidth), getCY(n, gearRadius - gearWidth), mLinePaint);
        }

        //写上字
        String time = getTime(currentCount);
        time = time.substring(time.indexOf(":") + 1);
        adjustTvTextSize(time, (gearRadius - gearWidth * 2) * 2);
        canvas.drawText(time, mWidth / 2, mHeight / 2 + getFontHeight() / 2, mTextPaint);
    }

    private float getFontHeight() {
        Paint.FontMetrics fm = mTextPaint.getFontMetrics();
        return (float) Math.ceil(fm.descent - fm.ascent);
    }

    //动态修改字体大小
    private void adjustTvTextSize(String text, float avaiWidth) {
        if (avaiWidth <= 0)
            return;
        float trySize = 200;
        mTextPaint.setTextSize(trySize);
        while (mTextPaint.measureText(text) > avaiWidth && trySize > 20) {
            trySize--;
            mTextPaint.setTextSize(trySize);
        }
    }

    /**
     * 获得X轴角度值
     *
     * @param n 位置值
     * @return 角度值
     */
    private float getCX(float n, float radius) {
        n = n > 1 ? n - 1 : n;
        double v = (2 * Math.PI * n);
        double cos = Math.cos(v);
        return mWidth / 2 + (float) (radius * cos);
    }

    /**
     * 获得Y轴角度值
     *
     * @param n 位置值
     * @return 角度值
     */
    private float getCY(float n, float radius) {
        n = n > 1 ? n - 1 : n;
        double v = (2 * Math.PI * n);
        double sin = Math.sin(v);
        return mHeight / 2 + (float) (radius * sin);
    }

    /***
     * 设置倒计时时间
     *
     * @param second 秒数
     */
    public void setCountdownSecond(int second) {
        maxCount = currentCount = second;
        startCountdown();
        postInvalidate();
    }

    private TimerTask mTimerTask;
    private Timer mTimer;

    private void initTimer() {
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (currentCount > 0) {
                    currentCount -= 1;
                    postInvalidate();
                } else {
                    currentCount = 0;
                    stopCountdown();
                }
            }
        };
        mTimer = new Timer(true);
    }

    /**
     * 开始倒计时
     */
    private void startCountdown() {
        stopCountdown();
        initTimer();
        mTimer.schedule(mTimerTask, 0, 1000);
    }


    /**
     * 停止倒计时
     */
    public void stopCountdown() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        mRadius = ((mWidth * 3 / 4) > mHeight ? mHeight : (mWidth * 3 / 4)) / 2;
        circleWidth = mRadius / 40;
    }

    /**
     * 根据秒数转化为时分秒00:00:00
     *
     * @param second 秒数
     * @return 返回格式化的字符串
     */
    private static String getTime(int second) {
        if (second < 10) {
            return "00:" + "00:0" + second;
        }
        if (second < 60) {
            return "00:" + "00:" + second;
        }
        if (second < 3600) {
            int minute = second / 60;
            second = second - minute * 60;
            if (minute < 10) {
                if (second < 10) {
                    return "00:" + "0" + minute + ":0" + second;
                }
                return "00:" + "0" + minute + ":" + second;
            }
            if (second < 10) {
                return "00:" + minute + ":0" + second;
            }
            return "00:" + minute + ":" + second;
        }
        int hour = second / 3600;
        int minute = (second - hour * 3600) / 60;
        second = second - hour * 3600 - minute * 60;
        if (hour < 10) {
            if (minute < 10) {
                if (second < 10) {
                    return "0" + hour + ":0" + minute + ":0" + second;
                }
                return "0" + hour + ":0" + minute + ":" + second;
            }
            if (second < 10) {
                return "0" + hour + ":" + minute + ":0" + second;
            }
            return "0" + hour + ":" + minute + ":" + second;
        }
        if (minute < 10) {
            if (second < 10) {
                return hour + ":0" + minute + ":0" + second;
            }
            return hour + ":0" + minute + ":" + second;
        }
        if (second < 10) {
            return hour + ":" + minute + ":0" + second;
        }
        return hour + ":" + minute + ":" + second;
    }
}

