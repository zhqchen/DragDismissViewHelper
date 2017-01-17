package com.zhqchen.dragdismiss;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.WindowManager;

/** 核心思想：点击原始View时，创建自定义拖动View，加入到WindowManager中，将原始View的ouTouch事件与自定义View联动
 * Created by zhqchen on 2016-01-16.
 */
public class DragDismissViewHelper implements View.OnTouchListener {

    private Context context;
    private View originView;
    private DragDismissView dismissView;

    private WindowManager windowManager;
    private WindowManager.LayoutParams mParams;

    private DragStateListener dragStateListener;

    private int paintColor = -1;//连接线的颜色
    private float farthestDistance = -1;//最远拖动距离

    public interface DragStateListener {

        void onOutFingerUp(View view);//拖动View在范围外松手，消除红点

        void onInnerFingerUp(View view);//拖动View在范围内松手，红点回归原位
    }

    public DragDismissViewHelper(Context context, View originView) {
        this.context = context;
        this.originView = originView;
        this.originView.setOnTouchListener(this);
        mParams = new WindowManager.LayoutParams();
        mParams.format = PixelFormat.TRANSLUCENT;
        //这样设置type和flags，才会使拖动时的红点延伸至状态栏，且使状态栏不改变
        mParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;//TYPE_SYSTEM_ALERT为系统提示窗，但无法覆盖住状态栏
        mParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;//窗口占满屏幕(会忽略状态栏), 而FLAG_FULLSCREEN会隐藏状态栏
    }

    public void setDragStateListener(DragStateListener listener) {
        this.dragStateListener = listener;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        if(action == MotionEvent.ACTION_DOWN) {
            ViewParent parent = v.getParent();
            if(parent == null) {
                return false;
            }
            parent.requestDisallowInterceptTouchEvent(true);//禁用上层View的滑动
            originView.setVisibility(View.INVISIBLE);
            dismissView = new DragDismissView(context);
            calculateAndInitDismissView();

            if(windowManager == null) {
                windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            }
            windowManager.addView(dismissView, mParams);//加入自定义的view
        }
        return dismissView.onTouchEvent(event);
    }

    private void calculateAndInitDismissView() {
        originView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(originView.getDrawingCache());
        originView.setDrawingCacheEnabled(false);
        //y需减去手机状态栏的高度
        if(paintColor != -1) {
            dismissView.setPaintColor(paintColor);
        }
        if(farthestDistance != -1) {
            dismissView.setFarthestDistance(farthestDistance);
        }
        dismissView.setStartCenterPoint(originView, bitmap);
        dismissView.setCurrentStateListener(new DragDismissView.CurrentStateListener() {
            @Override
            public void onStart() {
                originView.post(new Runnable() {
                    @Override
                    public void run() {
                        originView.setVisibility(View.INVISIBLE);//点击拖动开始，隐藏原始view
                    }
                });
            }

            @Override
            public void onInnerUp() {
                removeViews();
                if (dragStateListener != null) {
                    dragStateListener.onInnerFingerUp(originView);
                }
            }

            @Override
            public void onOutUp() {
                removeViews();
                if (dragStateListener != null) {
                    dragStateListener.onOutFingerUp(originView);
                }
            }
        });
    }

    //清空
    private void removeViews() {
        if(windowManager != null && dismissView.getParent() != null) {
            windowManager.removeView(dismissView);
        }
    }

    /**
     * 设置贝塞尔曲线的画笔颜色
     * @param color int
     */
    public void setPaintColor(int color) {
        this.paintColor = color;
    }

    /**
     * 设置最远拖动的距离
     * @param distance float
     */
    public void setFarthestDistance(float distance) {
        this.farthestDistance = distance;
    }
}
