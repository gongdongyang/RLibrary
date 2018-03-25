package com.angcyo.uiview.recycler.adapter

import com.angcyo.uiview.base.UIExItemUIView
import com.angcyo.uiview.container.ILayout
import com.angcyo.uiview.container.UIParam
import com.angcyo.uiview.recycler.RBaseViewHolder
import com.angcyo.uiview.view.IView

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：Item处理类
 * 创建人员：Robi
 * 创建时间：2018/03/16 16:53
 * 修改人员：Robi
 * 修改时间：2018/03/16 16:53
 * 修改备注：
 * Version: 1.0.0
 */
abstract class RExItemHolder<DataType> {
    var iLayout: ILayout? = null
    var exItemUIView: UIExItemUIView<*, DataType>? = null
    var exItemAdapter: RExItemAdapter<*, DataType>? = null

    open fun startIView(iView: IView) {
        iLayout?.startIView(iView)
    }

    open fun startIView(iView: IView, param: UIParam) {
        iLayout?.startIView(iView, param)
    }

    abstract fun onBindItemDataView(holder: RBaseViewHolder, posInData: Int, dataBean: DataType)
}