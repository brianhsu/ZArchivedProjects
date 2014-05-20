/**
 *  Maidroid Reminder
 *  
 *  -------------------------------------------------------------------------
 *  @license
 *
 *  This file is part of Maidroid Reminder
 *
 *  Maidroid Reminder is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Maidroid Reminder is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Maidroid Reminder.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  --------------------------------------------------------------------------
 *  @author Brian Hsu (brianhsu.hsu [at] gmail.com)
 *  
 */

package org.maidroid.reminder

import android.app.TabActivity
import android.os.Bundle
import android.content.Intent
import android.content.IntentFilter

import android.util.Log

class ActivityMain extends TabActivity with FindView
{
    private lazy val tabHost = getTabHost

    private def addTab (tag: String, title: String, iconResID: Int, 
                        action: String) =
    {
        val drawable = getResources().getDrawable(iconResID)
        val intent   = new Intent (action)
        
        tabHost.addTab (
            tabHost.newTabSpec   (tag).
                    setIndicator (title, drawable).
                    setContent   (intent)
        )
    }

    override def onCreate(savedInstanceState : Bundle)
    {
        super.onCreate(savedInstanceState)

        val week    = getResources.getString (R.string.main_week)
        val month   = getResources.getString (R.string.main_month)
        val all     = getResources.getString (R.string.main_all)
        val outdate = getResources.getString (R.string.main_outdate)

        addTab (week, week, android.R.drawable.ic_menu_week,
                MaidroidReminder.ACTION.LIST_WEEK)

        addTab (month, month, android.R.drawable.ic_menu_month,
                MaidroidReminder.ACTION.LIST_MONTH)

        addTab (all, all, android.R.drawable.ic_menu_my_calendar,
                MaidroidReminder.ACTION.LIST_ALL)

        addTab (outdate, outdate, android.R.drawable.ic_menu_more,
                MaidroidReminder.ACTION.LIST_OUTDATE)
    }
}

