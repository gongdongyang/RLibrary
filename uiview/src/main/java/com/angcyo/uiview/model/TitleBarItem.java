package com.angcyo.uiview.model;

import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.angcyo.uiview.RApplication;

public class TitleBarItem {
    public String text;
    @DrawableRes
    public int res = -1;
    public Drawable icoDrawable;//为null 表示文本按钮, 否则 ImageView
    public View.OnClickListener listener;
    public int visibility = View.VISIBLE;
    @ColorInt
    public int textColor = -1;
    public float textSize = -1;//px

    public int leftMargin = 0;
    public int topMargin = 0;
    public int rightMargin = 0;
    public int bottomMargin = 0;
    /**
     * 文本id的左图标资源
     */
    public int textLeftRes = -1;
    public int textRightRes = -1;

    /**
     * 0表示不透明, 1表示全透明
     */
    public float alpha = 0f;

    /**
     * View 的id
     */
    @IdRes
    public int id = -1;

    public OnItemInitListener mOnItemInitListener;

    TitleBarItem() {

    }

    public TitleBarItem(String text, View.OnClickListener listener) {
        this.text = text;
        this.listener = listener;
    }

    public TitleBarItem(@DrawableRes int res, View.OnClickListener listener) {
        this.listener = listener;
        setRes(res);
    }

    public TitleBarItem(String text, int textLeftRes, View.OnClickListener listener) {
        this.text = text;
        this.listener = listener;
        this.textLeftRes = textLeftRes;
    }

    public static TitleBarItem build(String text, View.OnClickListener listener) {
        return new TitleBarItem(text, listener);
    }

    public static TitleBarItem build(String text, int textLeftRes, View.OnClickListener listener) {
        return new TitleBarItem(text, listener).setTextLeftRes(textLeftRes);
    }

    public static TitleBarItem build(String text, int textLeftRes, int textRightRes, View.OnClickListener listener) {
        return new TitleBarItem(text, listener).setTextLeftRes(textLeftRes).setTextRightRes(textRightRes);
    }

    public static TitleBarItem build(@DrawableRes int icoRes, View.OnClickListener listener) {
        return new TitleBarItem(icoRes, listener);
    }

    @Deprecated
    public static TitleBarItem build() {
        return new TitleBarItem();
    }

    public TitleBarItem setText(String text) {
        this.text = text;
        return this;
    }

    public TitleBarItem setRes(int res) {
        this.res = res;
        if (res == -1) {
            setIcoDrawable(null);
        } else {
            setIcoDrawable(ContextCompat.getDrawable(RApplication.getApp(), res));
        }
        return this;
    }

    public TitleBarItem setId(int id) {
        this.id = id;
        return this;
    }

    public TitleBarItem setIcoDrawable(Drawable icoDrawable) {
        this.icoDrawable = icoDrawable;
        return this;
    }

    public TitleBarItem setVisibility(int visibility) {
        this.visibility = visibility;
        return this;
    }

    public TitleBarItem setListener(View.OnClickListener listener) {
        this.listener = listener;
        return this;
    }

    public TitleBarItem setTextColor(@ColorInt int textColor) {
        this.textColor = textColor;
        return this;
    }

    public TitleBarItem setTextSize(float textSize) {
        this.textSize = textSize;
        return this;
    }

    public TitleBarItem setLeftMargin(int leftMargin) {
        this.leftMargin = leftMargin;
        return this;
    }

    public TitleBarItem setTopMargin(int topMargin) {
        this.topMargin = topMargin;
        return this;
    }

    public TitleBarItem setRightMargin(int rightMargin) {
        this.rightMargin = rightMargin;
        return this;
    }

    public TitleBarItem setBottomMargin(int bottomMargin) {
        this.bottomMargin = bottomMargin;
        return this;
    }

    public TitleBarItem setOnItemInitListener(OnItemInitListener onItemInitListener) {
        mOnItemInitListener = onItemInitListener;
        return this;
    }

    public TitleBarItem setAlpha(float alpha) {
        this.alpha = alpha;
        return this;
    }

    public TitleBarItem setTextLeftRes(int textLeftRes) {
        this.textLeftRes = textLeftRes;
        return this;
    }

    public TitleBarItem setTextRightRes(int textRightRes) {
        this.textRightRes = textRightRes;
        return this;
    }

    public interface OnItemInitListener {
        void onItemInit(View itemView, TitleBarItem item);
    }
}
