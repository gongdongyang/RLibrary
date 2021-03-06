package com.angcyo.uiview.viewgroup

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.angcyo.uiview.R
import com.angcyo.uiview.kotlin.abs
import com.angcyo.uiview.kotlin.calcLayoutWidthHeight
import com.angcyo.uiview.kotlin.exactlyMeasure
import com.angcyo.uiview.rsen.RefreshLayout
import com.angcyo.uiview.utils.UI

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：下拉返回, 上拉全屏
 * 创建人员：Robi
 * 创建时间：2017/10/16 17:06
 * 修改人员：Robi
 * 修改时间：2017/10/16 17:06
 * 修改备注：
 * Version: 1.0.0
 */
open class TouchBackLayout(context: Context, attributeSet: AttributeSet? = null) : TouchLayout(context, attributeSet) {
    /*所有child的高度和*/
    //private var viewMaxHeight: Int = 0

    /**是否激活下拉返回*/
    var enableTouchBack = false

    /**顶部留出多少空间, 用来实现半屏效果*/
    var offsetScrollTop = 0
        set(value) {
            field = value
            if (field != 0) {
                scrollTo(0, -offsetScrollTop)
            }
        }

    private var defaultOffsetScrollTop = 0
    private var isFling = false

    private var rLayoutWidth: String? = null
    private var rLayoutHeight: String? = null

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.TouchBackLayout)
        enableTouchBack = typedArray.getBoolean(R.styleable.TouchBackLayout_r_enable_touch_back, enableTouchBack)
        defaultOffsetScrollTop = typedArray.getDimensionPixelOffset(R.styleable.TouchBackLayout_r_touch_offset_scroll_top, defaultOffsetScrollTop)
        rLayoutWidth = typedArray.getString(R.styleable.TouchBackLayout_r_layout_width)
        rLayoutHeight = typedArray.getString(R.styleable.TouchBackLayout_r_layout_height)
        typedArray.recycle()

        offsetScrollTop = defaultOffsetScrollTop
    }

//    /**布局方式, 采用的是垂直方向的线性布局方式*/
//    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
//        //super.onLayout(changed, left, top, right, bottom)
//        var top = 0
//        for (i in 0 until childCount) {
//            val childAt = getChildAt(i)
//            childAt.layout(0, top, measuredWidth, top + childAt.measuredHeight)
//            top += childAt.measuredHeight
//        }
//        viewMaxHeight = top
//    }

    private var downY: Float = 0f

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val touchEvent = super.onInterceptTouchEvent(ev)
        if (enableTouchBack) {

            if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
                downY = ev.y
            } else if (ev.actionMasked == MotionEvent.ACTION_MOVE &&
                    !isNestedAccepted &&
                    scrollY != 0 &&
                    (ev.y - downY).abs() > 10) {
                return true
            }

            return touchEvent
        } else {
            return false
        }
    }

//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        orientationGestureDetector.onTouchEvent(event)
//        return super.onTouchEvent(event)
//    }

    /*touch up时的, 恢复操作*/
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (!enableTouchBack) {
            return super.dispatchTouchEvent(ev)
        }

        val dispatchTouchEvent = super.dispatchTouchEvent(ev)
        val actionMasked = ev.actionMasked
        when (actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isFling = false
            }
            MotionEvent.ACTION_UP -> {
                if (offsetScrollTop.abs() > scrollY.abs()) {
                    offsetScrollTop = 0
                    scrollToDefault()
                } else {
                    if (!isFling) {
                        resetScroll()
                    }
                }
                isNestedAccepted = false
            }
        }
        return dispatchTouchEvent
    }

    /*外面的滚动处理*/
    override fun onScrollChange(orientation: ORIENTATION, distance: Float) {
        super.onScrollChange(orientation, distance)
        if (enableTouchBack && isVertical(orientation) && !isNestedAccepted) {
            scrollBy(0, calcConsumedDy(distance.toInt()))
        }
    }

    override fun onFlingChange(orientation: ORIENTATION, velocity: Float) {
        super.onFlingChange(orientation, velocity)
        if (isVertical(orientation) && !isNestedAccepted) {
            if (velocity > 1000) {
                isFling = true
                scrollToBack()
            } else if (velocity < -1000) {
                isFling = true
                scrollToDefault()
            }
        }
    }

    /*接收内嵌滚动*/
    override fun onStartNestedScroll(child: View?, target: View?, nestedScrollAxes: Int): Boolean {
        return if (enableTouchBack) {
            if (target?.parent is RefreshLayout) {
                (target.parent as RefreshLayout).isEnabled = false
            }
            true
        } else {
            false
        }
    }

    private var isNestedAccepted = false

    override fun onNestedScrollAccepted(child: View?, target: View?, axes: Int) {
        super.onNestedScrollAccepted(child, target, axes)
        isNestedAccepted = true
    }

    /*内嵌滚动开始, 处理需要消耗的滚动距离*/
    override fun onNestedPreScroll(target: View?, dx: Int, dy: Int, consumed: IntArray) {
        super.onNestedPreScroll(target, dx, dy, consumed)
        if (dy < 0) {
            //手指向下滑动
            if (!UI.canChildScrollUp(target)) {
                scrollBy(0, dy)
                consumed[1] = dy
            }
        } else if (dy > 0) {
            if (scrollY < 0) {
                val calcConsumedDy = calcConsumedDy(dy)
                scrollBy(0, calcConsumedDy)
                consumed[1] = calcConsumedDy
            }
        }
    }

    /*滚动的边界处理*/
    private fun calcConsumedDy(dy: Int): Int {
        var y = dy
        if (dy < 0) {

        } else {
            if (scrollY + dy > 0) {
                y = -scrollY
            }
        }
        return y
    }

    /**滚动到底部, 并返回*/
    fun scrollToBack(anim: Boolean = true) {
        if (anim) {
            startScrollY(-measuredHeight - scrollY)
            postInvalidate()
        } else {
            scrollTo(0, -measuredHeight)
        }
    }

    /**滚动到初始位置 (顶部)*/
    fun scrollToDefault(anim: Boolean = true) {
        if (anim) {
            startScrollY(-offsetScrollTop - scrollY)
            postInvalidate()
        } else {
            scrollTo(0, 0)
        }
    }

    /**滚动到最开始偏移位置, 如果没有偏移位置, 那么就是顶部*/
    fun scrollToDefaultOffset(anim: Boolean = true) {
        if (anim) {
            startScrollY(-scrollY - defaultOffsetScrollTop)
            postInvalidate()
        } else {
            scrollTo(0, -defaultOffsetScrollTop)
        }
    }

    /**重置位置*/
    fun resetScroll(anim: Boolean = true) {
        val scrollY = scrollY
        if (scrollY.abs() - offsetScrollTop >= (measuredHeight - offsetScrollTop) / 4) {
            //需要下滑返回
            scrollToBack(anim)
        } else {
            //恢复到初始化位置
            scrollToDefault(anim)
        }
    }

    override fun scrollTo(x: Int, y: Int) {
        if (enableTouchBack) {
            val oldY = scrollY
            super.scrollTo(x, y)
            onTouchBackListener?.onTouchBackListener(this, oldY.abs(), y.abs(), measuredHeight)
        }
    }

    var onTouchBackListener: OnTouchBackListener? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val layoutWidthHeight = calcLayoutWidthHeight(rLayoutWidth, rLayoutHeight, 0, 0)
        val width = layoutWidthHeight[0]
        val height = layoutWidthHeight[1]
        if (width == -1 && height == -1) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        } else if (width > 0 && height > 0) {
            super.onMeasure(exactlyMeasure(width), exactlyMeasure(height))
        } else {
            if (width == -1) {
                super.onMeasure(widthMeasureSpec, exactlyMeasure(height))
            } else {
                super.onMeasure(exactlyMeasure(width), heightMeasureSpec)
            }
        }
    }

    interface OnTouchBackListener {
        fun onTouchBackListener(layout: TouchBackLayout,
                                oldScrollY: Int /*已经做了abs处理, 确保是正数*/,
                                scrollY: Int /*已经做了abs处理, 确保是正数*/,
                                maxScrollY: Int /*允许滚动的最大距离, 当达到最大距离, 视为back*/)
    }
}