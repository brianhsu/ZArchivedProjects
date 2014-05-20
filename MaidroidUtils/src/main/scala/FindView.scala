package org.maidroid.utils

import android.app.Activity

trait FindView
{
    this: Activity =>

    def findView [WidgetType] (id: Int) : WidgetType = {
        return findViewById (id).asInstanceOf[WidgetType]
    }
}
