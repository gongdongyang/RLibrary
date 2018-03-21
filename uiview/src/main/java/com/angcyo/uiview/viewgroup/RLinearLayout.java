package com.angcyo.uiview.viewgroup;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import com.angcyo.uiview.R;
import com.angcyo.uiview.kotlin.ViewExKt;
import com.angcyo.uiview.utils.ScreenUtil;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：在不影响原有的背景上, 再添加一层背景颜色
 * 创建人员：Robi
 * 创建时间：2017/04/27 16:00
 * 修改人员：Robi
 * 修改时间：2017/04/27 16:00
 * 修改备注：
 * Version: 1.0.0
 */
public class RLinearLayout extends LinearLayout {

    private Drawable mBackgroundDrawable;

    /**
     * 允许的最大高度, 如果为-2px,那么就是屏幕高度的一半, 如果是-3px,那么就是屏幕高度的三分之, 以此内推, 0不处理
     */
    private int maxHeight = -1;

    /**
     * 当有2个TextView时, 横向排列, 如果第二个TextView不够排列时, 换行排列
     */
    private boolean autoFixNewLine = false;

    /**
     * 反向布局(目前只支持横向)
     */
    private boolean reverseLayout = false;

    /**
     * 是否在聊天布局中, 会自动控制child(LinearLayout) 的 gravity
     */
    private boolean isInChatLayout = false;

    private String widthHeightRatio;

    private int drawLine = 0;//不绘制线
    private int drawLineColor = 0;
    private int drawLineOffsetLeft = 0;//左偏移
    private int drawLineOffsetRight = 0;//右偏移
    private float drawLineWidth = 1 * ScreenUtil.density();
    private Paint linePaint;

    public RLinearLayout(Context context) {
        this(context, null);
    }

    public RLinearLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RLinearLayout);
        mBackgroundDrawable = typedArray.getDrawable(R.styleable.RLinearLayout_r_background);
        maxHeight = typedArray.getDimensionPixelOffset(R.styleable.RLinearLayout_r_max_height, -1);
        autoFixNewLine = typedArray.getBoolean(R.styleable.RLinearLayout_r_auto_fix_new_line, autoFixNewLine);
        reverseLayout = typedArray.getBoolean(R.styleable.RLinearLayout_r_reverse_layout, reverseLayout);
        isInChatLayout = typedArray.getBoolean(R.styleable.RLinearLayout_r_is_in_chat_layout, isInChatLayout);
        widthHeightRatio = typedArray.getString(R.styleable.RLinearLayout_r_width_height_ratio);

        drawLine = typedArray.getInt(R.styleable.RLinearLayout_r_draw_line, drawLine);
        drawLineOffsetLeft = typedArray.getDimensionPixelOffset(R.styleable.RLinearLayout_r_draw_line_offset_left, drawLineOffsetLeft);
        drawLineOffsetRight = typedArray.getDimensionPixelOffset(R.styleable.RLinearLayout_r_draw_line_offset_right, drawLineOffsetRight);
        drawLineColor = typedArray.getColor(R.styleable.RLinearLayout_r_draw_line_color, ContextCompat.getColor(getContext(), R.color.base_chat_bg_color));
        drawLineWidth = typedArray.getDimensionPixelOffset(R.styleable.RLinearLayout_r_draw_line_width, (int) drawLineWidth);

        typedArray.recycle();
        resetMaxHeight();
        initLayout();
    }

    private void initLayout() {
        setWillNotDraw(false);
    }

    private void resetMaxHeight() {
        if (maxHeight < -1) {
            int num = Math.abs(maxHeight);
            maxHeight = ScreenUtil.screenHeight / num;
        }
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
        resetMaxHeight();
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int statusBarHeight = getResources().getDimensionPixelSize(R.dimen.status_bar_height);

        if (maxHeight > 0) {
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST));
            int measuredHeight = getMeasuredHeight();
            if (measuredHeight > maxHeight) {
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(maxHeight, heightMode));
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

        if (needNewLine()) {
            getChildAt(1).measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.AT_MOST),
                    MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.AT_MOST));
            setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight() + getChildAt(1).getMeasuredHeight());
        }

        int[] widthHeightRatio = ViewExKt.calcWidthHeightRatio(this, this.widthHeightRatio);
        if (widthHeightRatio != null) {
            super.onMeasure(ViewExKt.exactlyMeasure(this, widthHeightRatio[0]),
                    ViewExKt.exactlyMeasure(this, widthHeightRatio[1]));
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (isInChatLayout) {
            if (reverseLayout && getOrientation() == HORIZONTAL) {
                //目前只支持横向 反向布局
                int rightOffset = getPaddingRight();
                int topOffset = getPaddingTop();
                for (int i = 0; i < getChildCount(); i++) {
                    View childAt = getChildAt(i);
                    if (childAt instanceof LinearLayout) {
                        ((LinearLayout) childAt).setGravity(Gravity.END);
                    }
                    final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) childAt.getLayoutParams();
                    int top = topOffset + lp.topMargin;
                    int right = getMeasuredWidth() - rightOffset;
                    int viewWidth = childAt.getMeasuredWidth();
                    int viewHeight = childAt.getMeasuredHeight();
                    int left = right - viewWidth;
                    childAt.layout(left, top, right, top + viewHeight);
                    rightOffset += viewWidth + lp.leftMargin;
                }
            } else {
                for (int i = 0; i < getChildCount(); i++) {
                    View childAt = getChildAt(i);
                    if (childAt instanceof LinearLayout) {
                        ((LinearLayout) childAt).setGravity(Gravity.START);
                    }
                }
                super.onLayout(changed, l, t, r, b);
            }
        } else {
            super.onLayout(changed, l, t, r, b);
            if (needNewLine()) {
                getChildAt(1).layout(getPaddingLeft(),
                        getChildAt(0).getBottom(), getPaddingLeft() + getChildAt(1).getMeasuredWidth(),
                        getChildAt(0).getBottom() + getChildAt(1).getMeasuredHeight());
            }
        }
    }

    private boolean needNewLine() {
        return autoFixNewLine &&
                getChildCount() == 2 &&
                (getChildAt(0).getMeasuredWidth() + getChildAt(1).getMeasuredWidth()) >= (getMeasuredWidth() - getPaddingLeft() - getPaddingRight());
    }

    @Override
    public void draw(Canvas canvas) {
        if (mBackgroundDrawable != null) {
            mBackgroundDrawable.setBounds(canvas.getClipBounds());
            mBackgroundDrawable.draw(canvas);
        }
        if (drawLine > 0) {
            if (linePaint == null) {
                linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                linePaint.setStyle(Paint.Style.FILL_AND_STROKE);
            }
            linePaint.setStrokeWidth(drawLineWidth);
            linePaint.setColor(drawLineColor);

            switch (drawLine) {
                case 1://L
                    //暂不支持
                    break;
                case 2://T
                    canvas.drawLine(drawLineOffsetLeft, drawLineWidth / 2,
                            getMeasuredWidth() - drawLineOffsetRight, drawLineWidth / 2,
                            linePaint);
                    break;
                case 3://R
                    //暂不支持
                    break;
                case 4://B
                    canvas.drawLine(drawLineOffsetLeft, getMeasuredHeight() - drawLineWidth / 2,
                            getMeasuredWidth() - drawLineOffsetRight, getMeasuredHeight() - drawLineWidth / 2,
                            linePaint);
                    break;
            }
        }
        super.draw(canvas);
    }

    public void setRBackgroundDrawable(@ColorInt int color) {
        setRBackgroundDrawable(new ColorDrawable(color));
    }

    public void setRBackgroundDrawable(Drawable drawable) {
        mBackgroundDrawable = drawable;
        postInvalidate();
    }

    public boolean isReverseLayout() {
        return reverseLayout;
    }

    public void setReverseLayout(boolean reverseLayout) {
        boolean old = this.reverseLayout;
        this.reverseLayout = reverseLayout;
        if (old != reverseLayout) {
            requestLayout();
        }
    }

    public boolean isInChatLayout() {
        return isInChatLayout;
    }

    public void setInChatLayout(boolean inChatLayout) {
        boolean old = this.isInChatLayout;
        isInChatLayout = inChatLayout;
        if (old != isInChatLayout) {
            requestLayout();
        }
    }
}
