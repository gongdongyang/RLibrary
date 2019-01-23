package com.angcyo.uiview.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.support.v7.widget.AppCompatEditText
import android.text.InputFilter
import android.text.method.DigitsKeyListener
import android.text.method.KeyListener
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ContextMenu
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import com.angcyo.uiview.R
import com.angcyo.uiview.kotlin.*
import com.angcyo.uiview.skin.SkinHelper

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/07/04 16:52
 * 修改人员：Robi
 * 修改时间：2017/07/04 16:52
 * 修改备注：
 * Version: 1.0.0
 */
class PasswordInputEditText(context: Context, attributeSet: AttributeSet? = null)
    : AppCompatEditText(context, attributeSet) {

    /**需要输入密码的数量*/
    var passwordCount = 4

    /**密码与密码之间的间隙*/
    var passwordSpace: Float = 10 * density

    /**密码提示框的大小, 当空隙为0时, 自动层叠边框, 小于等于0时, 自动平分View的宽度*/
    var passwordSize: Float = 30 * density

    /**绘制密码的大小比例*/
    var passwordDrawSizeScale: Float = 0.6f

    /**拆分成宽度和高度*/
    var passwordWidth: Float = 30 * density
    var passwordHeight: Float = 30 * density

    var strokeWidth = 2 * density

    var passwordHighlightColor = Color.RED
    /*背景颜色*/
    var passwordBgColor = Color.TRANSPARENT
    /*密码的颜色*/
    var passwordColor = Color.GRAY

    /**边框的颜色*/
    var passwordBorderColor = Color.parseColor("#E0E0E0")

    /**密码不可见时的提示样式*/
    var passwordTipType = TIP_TYPE_CIRCLE

    /**是否需要显示高亮框*/
    var showHighlight = true

    private var oldKeyListener: KeyListener? = null

    /**是否激活密码输入*/
    var enablePasswordInput = true
        set(value) {
            field = value
            if (value) {
                isEnabled = true
                oldKeyListener?.let {
                    this.keyListener = it
                }
            } else {
                isEnabled = false
                oldKeyListener = keyListener
                keyListener = null
            }
        }

    companion object {
        /**画圆*/
        const val TIP_TYPE_CIRCLE = 1
        /**画真实数字*/
        const val TIP_TYPE_RAW = 2
        /**画矩形*/
        const val TIP_TYPE_RECT = 3
    }

    private val paint: Paint by lazy {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.strokeWidth = strokeWidth
        p
    }

    private val textRawPaint: Paint by lazy {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p
    }

    private val rect: Rect by lazy {
        Rect()
    }

    private val highlightRect: Rect by lazy {
        Rect()
    }

    init {
        val array = context.obtainStyledAttributes(attributeSet, R.styleable.PasswordInputEditText)
        passwordCount = array.getInt(R.styleable.PasswordInputEditText_r_password_count, passwordCount)
        passwordDrawSizeScale = array.getFloat(R.styleable.PasswordInputEditText_r_password_draw_size_scale, passwordDrawSizeScale)
        passwordSpace = array.getDimensionPixelOffset(R.styleable.PasswordInputEditText_r_password_space, passwordSpace.toInt()).toFloat()
        passwordSize = array.getDimensionPixelOffset(R.styleable.PasswordInputEditText_r_password_size, passwordSize.toInt()).toFloat()

        passwordHeight = array.getDimensionPixelOffset(R.styleable.PasswordInputEditText_r_password_height, passwordSize.toInt()).toFloat()
        passwordWidth = array.getDimensionPixelOffset(R.styleable.PasswordInputEditText_r_password_width, passwordSize.toInt()).toFloat()

        strokeWidth = array.getDimensionPixelOffset(R.styleable.PasswordInputEditText_r_border_width, strokeWidth.toInt()).toFloat()

        if (!isInEditMode) {
            passwordHighlightColor = SkinHelper.getSkin().themeSubColor
        }

        passwordHighlightColor = array.getColor(R.styleable.PasswordInputEditText_r_password_highlight_color, passwordHighlightColor)
        passwordBgColor = array.getColor(R.styleable.PasswordInputEditText_r_password_bg_color, passwordBgColor)
        passwordColor = array.getColor(R.styleable.PasswordInputEditText_r_password_color, passwordColor)
        passwordBorderColor = array.getColor(R.styleable.PasswordInputEditText_r_password_border_color, passwordBorderColor)

        showHighlight = array.getBoolean(R.styleable.PasswordInputEditText_r_show_highlight, showHighlight)
        passwordTipType = array.getInt(R.styleable.PasswordInputEditText_r_password_tip_type, passwordTipType)

        val enableInput = array.getBoolean(R.styleable.PasswordInputEditText_r_enable_password_input, enablePasswordInput)

        array.recycle()

        if (passwordTipType == TIP_TYPE_RAW) {
            textRawPaint.set(getPaint())
        }

        setBackgroundColor(Color.TRANSPARENT)
        setTextColor(Color.TRANSPARENT)
        isCursorVisible = false
        setTextSize(TypedValue.COMPLEX_UNIT_PX, 0f)
        setTextIsSelectable(false)
        inputType = EditorInfo.TYPE_CLASS_NUMBER
        filters = arrayOf(InputFilter.LengthFilter(passwordCount))
        imeOptions = imeOptions or EditorInfo.IME_FLAG_NO_EXTRACT_UI
        keyListener = DigitsKeyListener.getInstance("1234567890")

        if (!enableInput) {
            enablePasswordInput = false
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        var heightSize = View.MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)

        if (passwordSize <= 0) {
            passwordWidth = ((widthSize - paddingLeft - paddingRight) / passwordCount).toFloat()
        }

        if (widthMode != View.MeasureSpec.EXACTLY) {
            if (passwordSpace == 0f) {
                widthSize = (passwordCount * passwordWidth + strokeWidth +
                        paddingLeft + paddingRight).toInt()
            } else {
                widthSize = (passwordCount * (passwordWidth + strokeWidth) +
                        Math.max(passwordCount - 1, 0) * passwordSpace +
                        paddingLeft + paddingRight).toInt()
            }
        }

        if (heightMode != View.MeasureSpec.EXACTLY) {
            heightSize = (passwordHeight + strokeWidth + paddingTop + paddingBottom).toInt()
        }

        setMeasuredDimension(widthSize, heightSize)
    }

    override fun createContextMenu(menu: ContextMenu?) {
        //super.createContextMenu(menu)
    }

    override fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)

        if (text?.length == passwordCount && passwordCount != 0) {
            //T_.show("本次密码:$text")
            onPasswordInputListener?.onPassword(text?.toString())
        }
    }

    override fun isFocused(): Boolean {
        if (isInEditMode) {
            return true
        }
        return super.isFocused()
    }

    override fun onDraw(canvas: Canvas) {
        //canvas.drawColor(Color.parseColor("#40000000"))
        paint.strokeWidth = strokeWidth
        highlightRect.setEmpty()

        var left: Float
        for (i in 0 until passwordCount) {
            if (passwordSpace == 0f) {
                left = paddingLeft + passwordWidth * i + strokeWidth / 2
            } else {
                left = paddingLeft + strokeWidth / 2 + (passwordWidth + strokeWidth) * i + Math.max(i, 0) * passwordSpace
            }

            rect.set(left.toInt(), (paddingTop + strokeWidth / 2).toInt(),
                    (left + passwordWidth).toInt(), (paddingTop + strokeWidth / 2 + passwordHeight).toInt())

            //填充密码框
            paint.style = Paint.Style.FILL_AND_STROKE
            paint.color = passwordBgColor
            canvas.drawRect(rect, paint)

            //绘制外框
            paint.style = Paint.Style.STROKE
            if (isFocused && text?.length ?: 0 == i) {
                //高亮颜色
                if (showHighlight) {
                    paint.color = passwordHighlightColor
                } else {
                    paint.color = passwordBorderColor
                }
                highlightRect.set(rect)
            } else {
                paint.color = passwordBorderColor
            }
            canvas.drawRect(rect, paint)

            //绘制内框
            if (text?.length ?: 0 > i) {
                paint.style = Paint.Style.FILL_AND_STROKE
                paint.color = passwordColor

                when (passwordTipType) {
                    TIP_TYPE_CIRCLE -> {
                        canvas.drawCircle(rect.centerX().toFloat(), rect.centerY().toFloat(),
                                passwordWidth.maxValue(passwordHeight) * passwordDrawSizeScale / 2, paint)
                    }
                    TIP_TYPE_RECT -> {
                        //val offset = 4 * density
                        val widthOffset = rect.width() * (1 - passwordDrawSizeScale) / 2
                        val heightOffset = rect.height() * (1 - passwordDrawSizeScale) / 2
                        canvas.drawRect(rect.left + widthOffset, rect.top + heightOffset,
                                rect.right - widthOffset, rect.bottom - heightOffset, paint)
                    }
                    TIP_TYPE_RAW -> {
                        val s = text.toString()[i].toString()
                        textRawPaint.color = passwordColor
                        canvas.drawText(text.toString()[i].toString(),
                                rect.centerX() - textWidth(textRawPaint, s) / 2,
                                rect.centerY() + textHeight(textRawPaint) / 2 - textRawPaint.descent(), textRawPaint)
                    }
                }
            }
        }

        //高亮处理
        if (passwordSpace == 0f && showHighlight) {
            //间隙等于0时, 高亮的矩形会被左右的矩形覆盖, 所以需要重新绘制
            paint.color = passwordHighlightColor
            canvas.drawRect(highlightRect, paint)
        }
    }

    /**是否输入有误*/
    fun isInputError(): Boolean {
        if (text.isNullOrEmpty()) {
            return true
        }
        if (text?.length ?: 0 != passwordCount) {
            return true
        }

        return false
    }

    fun string(): String {
        val rawText = text.toString().trim()
        return rawText
    }

    fun delInput() {
        if (enablePasswordInput) {
            del()
        } else {
            val newText = string()
            setInputText(newText.subSequence(0, 0.minValue(newText.length - 1)).toString())
        }
    }

    fun insertInput(input: String) {
        val oldLength = string().length
        if (oldLength >= passwordCount) {
            //已经输入满了密码
            return
        }
        val newText = string() + input
        setInputText(newText.subSequence(0, passwordCount.maxValue(newText.length)).toString())
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!isEnabled) {
            return false
        }
        return super.onTouchEvent(event)
    }

    var onPasswordInputListener: OnPasswordInputListener? = null

    open interface OnPasswordInputListener {
        fun onPassword(password: String)
    }
}