package com.zhqchen.dragdismiss;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * 仿QQ消息的红点拖拽消失的效果
 * Created by zhqchen on 2016-01-16.
 */
class DragDismissView extends FrameLayout {

    private float FARTHEST_DISTANCE_DEFAULT = 250;//拖动的最远距离,可自己配置
    private float POINT_RADIUS_DEFAULT = 20;//默认圆点半径大小，即为被拖动view的大小，默认为20
    private float MIN_RADIUS = 8;//圆点的最小半径，为被拖动view大小的40%，默认是8

    private Paint mPaint;
    private Path mPath;
    private PointF startPoint;//拖动起始点
    private PointF currentPoint;//当前移动点

    private ImageView ivStartView;//被拖动View的镜像
    private ImageView ivExplore;//爆炸动画ImageView

    private int[] startLocation = new int[2];//拖动起始View的中心点
    private int dragViewWidth;//拖动起始View的宽度
    private int dragViewHeight;//拖动起始View高度
//    private int mStatusBarHeight;//手机状态栏高度. 因RedPointViewHelper中已设置为全屏的模式，点击的位置判断，不再需要减去状态栏的高度

    private boolean isNeedDrawBezierCurve = true;//是否已经被拖出指定区域外
    private float mRadius = POINT_RADIUS_DEFAULT;
    private float currentDistance;//当前拖动的距离
    private int exploreAnimationDuration;

    private AnimatorSet backAnimator;

    private CurrentStateListener currentStateListener;

    /**
     * 不定义为public，使其仅由同包内的RedPointViewHelper使用
     */
    interface CurrentStateListener {

        void onStart();//拖动开始，即点击按下

        void onInnerUp();//拖动View在范围内松手

        void onOutUp();//拖动View在范围外松手
    }

    public DragDismissView(Context context) {
        super(context);
        initViews();
    }

    public DragDismissView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public DragDismissView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        startPoint = new PointF();
        currentPoint = new PointF();

        mPath = new Path();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.RED);//默认为红色
        mPaint.setStyle(Paint.Style.FILL);

        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        ivExplore = new ImageView(getContext());
        ivExplore.setLayoutParams(params);
        ivExplore.setImageResource(R.drawable.anim_list_explore);
        ivExplore.setVisibility(View.GONE);

        ivStartView = new ImageView(getContext());
        ivStartView.setLayoutParams(params);

        addView(ivStartView);
        addView(ivExplore);
    }

    /**
     * 设置贝塞尔曲线的画笔颜色
     * @param color int
     */
    public void setPaintColor(int color) {
        mPaint.setColor(color);
    }

    /**
     * 设置最远拖动的距离
     * @param distance float
     */
    public void setFarthestDistance(float distance) {
        this.FARTHEST_DISTANCE_DEFAULT = distance;
    }

    /**
     * 上层设置起始View的位置信息
     * @param originView
     * @param drawingCache
     */
    public void setStartCenterPoint(View originView, Bitmap drawingCache) {
        int width = originView.getWidth();
        int height = originView.getHeight();
        originView.getLocationOnScreen(startLocation);
        startLocation[0] += width / 2;
        startLocation[1] += height / 2;
        startPoint.set(startLocation[0], startLocation[1]);//以原View位置为起始点

        this.dragViewWidth = drawingCache.getWidth();
        this.dragViewHeight = drawingCache.getHeight();
        this.POINT_RADIUS_DEFAULT = this.mRadius = width < height ? width / 2 : height / 2;//以原始View的大小，设置起始圆半径和当前半径
        this.MIN_RADIUS = this.POINT_RADIUS_DEFAULT * 0.4f;

        ivStartView.setImageBitmap(drawingCache);
        ivStartView.setX(startPoint.x - dragViewWidth / 2);
        ivStartView.setY(startPoint.y - dragViewHeight / 2);//设置startView的初始位置
//        invalidate();
    }

    public void setCurrentStateListener(CurrentStateListener listener) {
        this.currentStateListener = listener;
    }

    /**
     * 这个方法计算了两圆点之间的贝赛尔曲线的生成过程
     */
    private void calculatePath() {
        float startPointX = startPoint.x;
        float startPointY = startPoint.y;
        float currentPointX = currentPoint.x;
        float currentPointY = currentPoint.y;

        float dx = currentPointX - startPointX;
        float dy = currentPointY - startPointY;

        currentDistance = getDragDistance(currentPoint.x, currentPoint.y, startPoint.x, startPoint.y);//拖动时，起始点和终点间的距离;
        mRadius = POINT_RADIUS_DEFAULT - currentDistance / 20;
//        mRadius -= mRadius * (farthestDistance - currentDistance) / farthestDistance;//按比例计算起始圆点的半径
        if(mRadius < MIN_RADIUS) {
            mRadius = MIN_RADIUS;
        }

        double angle = Math.atan(dy / dx);//arctan求角度
        float offsetX = (float) (mRadius * Math.sin(angle));
        float offsetY = (float) (mRadius * Math.cos(angle));

        float x1 = startPointX + offsetX;
        float y1 = startPointY - offsetY;

        float x3 = currentPointX + offsetX;
        float y3 = currentPointY - offsetY;

        float x4 = currentPointX - offsetX;
        float y4 = currentPointY + offsetY;

        float x2 = startPointX - offsetX;
        float y2 = startPointY + offsetY;

        float centerX = (startPointX + currentPointX) / 2;
        float centerY = (startPointY + currentPointY) / 2;

        //(x1, y1)-->(x3, y3)-->(x4, y4)-->(x2, y2)-->(x1, y1)闭环
        mPath.reset();//先重置Path路径
        mPath.moveTo(x1, y1);
        mPath.quadTo(centerX, centerY, x3, y3);

        mPath.lineTo(x4, y4);
        mPath.quadTo(centerX, centerY, x2, y2);
        mPath.lineTo(x1, y1);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        if(isNeedDrawBezierCurve) {//在actionMove被拖出指定区域后就不再画贝塞尔曲线
            calculatePath();
            canvas.drawPath(mPath, mPaint);//中间连线
            canvas.drawCircle(startPoint.x, startPoint.y, mRadius, mPaint);//起始点圆
            canvas.drawCircle(currentPoint.x, currentPoint.y, mRadius, mPaint);//结束点圆
        }
        canvas.restore();
        super.dispatchDraw(canvas);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //true 和false都会使dispatchTouchEvent不再分发事件，点击此view，onInterceptTouchEvent和onTouchEvent都会失效
        return true;//防止View的误点击
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        currentPoint.set(event.getRawX(), event.getRawY());//getX是event点相对于父布局的位置，getRawX是相对于屏幕的位置
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                isNeedDrawBezierCurve = true;//在按下时也绘制贝塞尔曲线
                if(currentStateListener != null) {
                    currentStateListener.onStart();
                }
                ivStartView.setX(currentPoint.x - dragViewWidth / 2);
                ivStartView.setY(currentPoint.y - dragViewHeight / 2);//设置startView的当前点击位置
                break;
            case MotionEvent.ACTION_MOVE:
                if(isNeedDrawBezierCurve) {
                    //将textview的中心放在当前手指位置, 在dispatchDraw时画出贝塞尔曲线
                    ivStartView.setX(currentPoint.x - dragViewWidth / 2);
                    ivStartView.setY(currentPoint.y - dragViewHeight / 2);
                    currentDistance = getDragDistance(currentPoint.x, currentPoint.y, startPoint.x, startPoint.y);
                    if(currentDistance > FARTHEST_DISTANCE_DEFAULT) {
                        isNeedDrawBezierCurve = false;//被拖出了限制区域外, 不再画曲线
                    }
                } else {//被拖出限制区域外之后，再移动，仅绘制拖动view
                    ivStartView.setX(currentPoint.x - dragViewWidth / 2);
                    ivStartView.setY(currentPoint.y - dragViewHeight / 2);
                }
                break;
            case MotionEvent.ACTION_UP:
                isNeedDrawBezierCurve = false;
                currentDistance = getDragDistance(currentPoint.x, currentPoint.y, startPoint.x, startPoint.y);
                if(currentDistance > FARTHEST_DISTANCE_DEFAULT) {
                    animateExploreView();//抬起时,拖动已超过最大拖动距离, 消除红点
                } else {
                    back2StartPosition();//回弹到起始位置
                }
                break;
        }
        invalidate();
        return true;
    }

    /**
     * 获取两点之间的间距
     */
    private float getDragDistance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));//拖动时，起始点和终点间的距离;
    }

    /**
     * 红点消除的动画，并隐藏未读数TextView
     */
    private void animateExploreView() {
        ivExplore.setX(currentPoint.x - ivStartView.getWidth() / 2);
        ivExplore.setY(currentPoint.y - ivStartView.getHeight() / 2);
        ivExplore.setVisibility(View.VISIBLE);
        final AnimationDrawable animationDrawable = (AnimationDrawable) ivExplore.getDrawable();
        exploreAnimationDuration = getExploreAnimationDuration(animationDrawable);
        animationDrawable.start();
        ivExplore.postDelayed(new Runnable() {
            @Override
            public void run() {
                animationDrawable.stop();
                ivExplore.clearAnimation();
                if(currentStateListener != null) {
                    currentStateListener.onOutUp();
                }
            }
        }, exploreAnimationDuration);//动画执行完成再回调上层
        ivStartView.setVisibility(View.GONE);
    }

    private int getExploreAnimationDuration(AnimationDrawable animationDrawable) {
        if(exploreAnimationDuration == 0) {
            for(int i = 0; i < animationDrawable.getNumberOfFrames(); i ++) {
                exploreAnimationDuration += animationDrawable.getDuration(i);
            }
        }
        return exploreAnimationDuration;
    }

    /**
     * 回弹的动画
     */
    private void back2StartPosition() {
        if(backAnimator != null) {
            backAnimator.start();
            return;
        }
        backAnimator = new AnimatorSet();
        backAnimator.playTogether(
                ObjectAnimator.ofFloat(ivStartView, "translationX", currentPoint.x -  ivStartView.getWidth() / 2, startPoint.x - ivStartView.getWidth() / 2),
                ObjectAnimator.ofFloat(ivStartView, "translationY", currentPoint.y -  ivStartView.getHeight() / 2, startPoint.y - ivStartView.getHeight() / 2)
        );
        backAnimator.setDuration(200);
        backAnimator.setInterpolator(new OvershootInterpolator(2));
        backAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if(currentStateListener != null) {
                    currentStateListener.onInnerUp();//动画执行完成再回调上层
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        backAnimator.start();
    }
}
