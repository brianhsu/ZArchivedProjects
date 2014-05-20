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

import android.app.Activity
import android.app.AlertDialog

import android.content.Intent
import android.content.DialogInterface

import android.os.Bundle
import android.util.Log
import android.net.Uri

import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ContextMenu

import android.widget.AdapterView._
import android.widget.ListView
import android.widget.ImageView
import android.widget.AdapterView
import android.widget.TextView
import android.widget.ImageView
import android.widget.ImageSwitcher

import android.widget.RelativeLayout
import android.util.Log

import android.R.drawable
import MaidroidReminder._

class ActivityList extends Activity with FindView
{
    /*=================================================================
     * MenuID List
     *===============================================================*/
    private object MenuID
    {
        val View   = 0
        val Add    = 1
        val Edit   = 2
        val Delete = 3
        val Prefs  = 4
        val Clear  = 5
        val Sync   = 6
        val About  = 7
    }

    /*=================================================================
     * Private Fields
     *===============================================================*/

    private lazy val listView:   ListView   = findView (R.id.list_list)
    private lazy val maidImage:  ImageView  = findView (R.id.list_maid)
    private lazy val messageBox: ViewMessageBox = findView (R.id.list_message)
    private lazy val action  = getIntent.getAction

    private def adapter = listView.getAdapter
    private def maid    = Maid.getMaid (this)
    private var skipReload = false

    /*=================================================================
     * Private Methods
     *===============================================================*/

    private def getReminderItem (menuInfo: ContextMenu.ContextMenuInfo) =
    {
        val pos = menuInfo.asInstanceOf[AdapterContextMenuInfo].position
        adapter.getItem (pos).asInstanceOf[ReminderItem]
    }

    private def addMenuItem[T] (menu: Menu, menuID: Int, 
                                title: T, 
                                iconResID: Option[Int],
                                intent: Option[Intent]): MenuItem =
    {
        val menuItem = title match {
            case resID: Int    => menu.add (Menu.NONE, menuID, menuID, resID)
            case title: String => menu.add (Menu.NONE, menuID, menuID, title)
            case _             => throw new Exception ("Menu title error")
        }

        iconResID.foreach ( menuItem.setIcon(_) )
        intent.foreach    ( menuItem.setIntent(_) )

        menuItem
    }

    private def addMenuItem[T] (menu: Menu, menuID: Int, 
                                title: T, 
                                iconResID: Option[Int]): MenuItem =
    {
        addMenuItem (menu, menuID, title, iconResID, None)
    }


    private def addMenuItem [T] (menu: Menu, menuID: Int, 
                                 title: T): MenuItem =
    {
        addMenuItem (menu, menuID, title, None, None)
    }

    private def updateList ()
    {   
        def actionToBoundary (): (Option[TriggerTime], Option[TriggerTime]) = 
        {
            val now = new TriggerTime (System.currentTimeMillis)

            action match {
                case ACTION.LIST_WEEK    => (Some(now), Some(now.nextWeekend))
                case ACTION.LIST_MONTH   => (Some(now), Some(now.nextMonth))
                case ACTION.LIST_ALL     => (Some(now), None)
                case ACTION.LIST_OUTDATE => (None, Some(now))
                case _ => (None, None)
            }
        }

        def filter (item: ReminderItem): Boolean = 
        {
            val (lower, upper) = actionToBoundary ()

            val lowerOK = (lower == None || 
                           item.triggerTime >= lower.get.timeInMillis)

            val upperOK = (upper == None ||
                           item.triggerTime <= upper.get.timeInMillis)

            return (lowerOK && upperOK)
        }

        val adapter = ReminderItem.getListAdapter (this, filter)

        listView.setAdapter (adapter)
        listView.setOnItemSelectedListener (onItemSelected)
    }

    private lazy val onItemSelected = new AdapterView.OnItemSelectedListener()
    {
        override def onItemSelected (parent: AdapterView[_], view: View, 
                                     position: Int, id: Long) 
        {
            val item    = adapter.getItem (position).asInstanceOf[ReminderItem]
            val message = maid.itemSelectedMessage (item)

            messageBox.setMessageList (message)
        }

        override def onNothingSelected (parent: AdapterView[_]) {}
    }


    /*=================================================================
     * Callbacks
     *===============================================================*/

    override def onOptionsItemSelected (menuItem: MenuItem) : Boolean = 
    {
        def clearAll ()
        {
            val allItems = for (pos <- 0 until adapter.getCount) yield 
                               adapter.getItem (pos).asInstanceOf[ReminderItem]

            allItems.foreach (_.delete)
            updateList ()
        }

        def showAbout ()
        {
            messageBox.setMessageList (maid.aboutMessage)
        }

        menuItem.getItemId match {
            case MenuID.Clear => clearAll ()
            case MenuID.About => showAbout ()
            case _ =>
        }

        return false
    }

    override def onContextItemSelected (menuItem: MenuItem) = 
    {
        val item = getReminderItem (menuItem.getMenuInfo)

        def editItem ()
        {
            startActivity (new Intent (Intent.ACTION_EDIT, item.uri))
            skipReload = true
        }

        def deleteItem ()
        {
            val deleteListener = new DialogInterface.OnClickListener () {
                def onClick (dialog: DialogInterface, which: Int) {
                    item.delete ()
                    updateList  ()
                }
            }

            val messageView: View = {
                val message      = maid.delMessage (item)
                val layout       = View.inflate (this, R.layout.dialog, null)
                val messageView  = layout.findViewById (R.id.dialogMessage).
                                   asInstanceOf[TextView]
                val maidIcon     = layout.findViewById (R.id.dialogMaidIcon).
                                   asInstanceOf[ImageView]

                messageView.setText (message.message)
                maidIcon.setImageResource (message.faceResID)

                layout
            }

            val confirm = new AlertDialog.Builder (this).
                              setTitle   (maid.delTitle(item)).
                              setView    (messageView).
                              setPositiveButton (R.string.context_delete_ok, 
                                                 deleteListener).
                              setNegativeButton (R.string.context_delete_cancel,
                                                 null)
            confirm.show
        }

        def viewItem ()
        {
            val message = maid.itemSelectedMessage (item)
            messageBox.setMessageList (message)
        }

        menuItem.getItemId match {
            case MenuID.View   => viewItem ()
            case MenuID.Edit   => editItem ()
            case MenuID.Delete => deleteItem ()
            case _ =>
        }

        false
    }

    /*=================================================================
     * Android Framework
     *===============================================================*/

    override def onCreate(savedInstanceState : Bundle)
    {
        super.onCreate(savedInstanceState)
        setContentView (R.layout.list)

        listView.setOnCreateContextMenuListener (this)
        messageBox.setMaidView (maidImage)
    }

    override def onRestart ()
    {
        super.onRestart ()
    }

    override def onResume ()
    {
        super.onResume ()

        if (!skipReload) {
            messageBox.setMessageList (maid.itemListMessage(action))
        }

        skipReload = false
        updateList ()
    }

    override def onMenuItemSelected (menuID: Int, item: MenuItem) = 
    {
        if (item.getItemId == MenuID.Add) {
            skipReload = true
        }

        super.onMenuItemSelected (menuID, item)
    }

    override def onCreateContextMenu (menu: ContextMenu, view: View,
                                      menuInfo: ContextMenu.ContextMenuInfo)
    {
        action match {
            case ACTION.LIST_OUTDATE =>
                addMenuItem (menu, MenuID.View,   R.string.context_view)
                addMenuItem (menu, MenuID.Delete, R.string.context_delete)

            case _ =>
                addMenuItem (menu, MenuID.View,   R.string.context_view)
                addMenuItem (menu, MenuID.Edit,   R.string.context_edit)
                addMenuItem (menu, MenuID.Delete, R.string.context_delete)
        }

        val title      = getReminderItem(menuInfo).title

        menu.setHeaderTitle (title)
    }

    override def onCreateOptionsMenu (menu: Menu) : Boolean = 
    {
        val intentAdd   = new Intent(ACTION.ADD_REMIND_ITEM)
        val intentPrefs = new Intent(ACTION.PREFERENCE)
        val intentSync  = new Intent(ACTION.SYNC)

        addMenuItem (menu, MenuID.Add, R.string.menu_add,
                     Some(drawable.ic_menu_add), Some(intentAdd))

        addMenuItem (menu, MenuID.Prefs, R.string.menu_preferences,
                     Some(drawable.ic_menu_preferences), 
                     Some(intentPrefs))

        addMenuItem (menu, MenuID.Sync, "Sync",
                     Some(drawable.ic_menu_agenda), Some(intentSync))

        addMenuItem (menu, MenuID.About, R.string.menu_about,
                     Some(drawable.ic_menu_info_details), 
                     None)

        if (action == ACTION.LIST_OUTDATE) {
            addMenuItem (menu, MenuID.Clear, R.string.menu_clear,
                         Some(drawable.ic_menu_close_clear_cancel), 
                         None)
        }

        super.onCreateOptionsMenu (menu)
    }
}

