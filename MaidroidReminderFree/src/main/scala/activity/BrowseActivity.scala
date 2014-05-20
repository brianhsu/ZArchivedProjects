package org.maidroid.reminder2

import android.app.TabActivity
import android.os.Bundle
import android.content.Context
import android.content.Intent

class BrowseActivity extends TabActivity with TypedActivity
{
    private lazy val tabHost = getTabHost

    private def addHost(tabID: String, titleResID: Int, activity: Intent) {
        val tabSpec = tabHost.newTabSpec(tabID).
                              setIndicator(getString(titleResID)).
                              setContent(activity)

        tabHost.addTab (tabSpec)
    }

    override def onCreate(savedInstanceState: Bundle) {

        super.onCreate(savedInstanceState)

        setContentView (R.layout.tab)

        addHost("week", R.string.week, new Intent(this, classOf[WeekEventsActivity]))
        addHost("month",R.string.month, new Intent(this, classOf[MonthEventsActivity]))
        addHost("all", R.string.all, new Intent(this, classOf[AllEventsActivity]))
        addHost("repeat", R.string.repeat, new Intent(this, classOf[RepeatEventsActivity]))
        addHost("outdate", R.string.outdated, new Intent(this, classOf[OutdatedEventsActivity]))
    }
}
