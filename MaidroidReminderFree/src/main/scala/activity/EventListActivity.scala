package org.maidroid.reminder2

import android.app.Activity
import android.app.Dialog

import android.content.Intent
import android.net.Uri

import android.os.Bundle

import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.Button

import android.view.View
import android.view.ViewGroup
import android.view.Menu
import android.view.MenuItem

import org.maidroid.reminder2.AndroidDBEvent._
import org.maidroid.calendar.model._

import java.util.{Calendar => JCalendar, Date}

class OutdatedEventsActivity extends EventListActivity
{
    val MENU_CLEAR_ITEM = 1

    override protected def welcomeMessages: List[Message] = maid.tabOutdate
    override protected def eventContent: Array[Event] = {
        val currentTime = new Date()
        def inTimeRange(event: Event) = event.startTime.getTime < currentTime.getTime &&
                                        event.recurrence == None
        
        calendar.getEvents.filter(inTimeRange).toArray
    }

    override def onCreateOptionsMenu(menu: Menu): Boolean = {
        super.onCreateOptionsMenu(menu)

        menu.add(0, MENU_CLEAR_ITEM, 1, getString(R.string.clear)).
             setIcon(android.R.drawable.ic_menu_close_clear_cancel)

        true
    }

    override def onOptionsItemSelected(item: MenuItem) = item.getItemId match {
        case MENU_CLEAR_ITEM => 
            println("Hello World")
            eventContent.foreach(event =>  calendar.deleteEvent(event))
            eventListView.setAdapter(EventAdapter)
            maidDialog.setMessages(maid.tabEventsCleared)
            true

        case _ => super.onOptionsItemSelected(item)
    }
}

class AllEventsActivity extends EventListActivity
{
    override protected def welcomeMessages: List[Message] = maid.tabAll
    override protected def eventContent: Array[Event] = {
        val currentTime = new Date
        def inTimeRange(event: Event) = event.startTime.getTime >= currentTime.getTime &&
                                        event.recurrence == None

        calendar.getEvents.filter(inTimeRange).toArray
    }
}

class MonthEventsActivity extends EventListActivity
{
    override protected def welcomeMessages: List[Message] = maid.tabMonth
    override protected def eventContent: Array[Event] = {
        val currentTime = new Date
        val nextMonth = {
            val calendar = JCalendar.getInstance
            calendar.setTime(currentTime)
            calendar.add (JCalendar.MONTH, 1)
            calendar.getTime
        }

        def inTimeRange(event: Event) = event.startTime.getTime >= currentTime.getTime && 
                                        event.startTime.getTime <= nextMonth.getTime &&
                                        event.recurrence == None
        
        calendar.getEvents.filter(inTimeRange).toArray
    }

}


class WeekEventsActivity extends EventListActivity
{
    override protected def welcomeMessages: List[Message] = maid.tabWeek
    override protected def eventContent: Array[Event] = {
        val currentTime = new Date
        val nextWeek = {
            val calendar = JCalendar.getInstance
            calendar.setTime(currentTime)
            calendar.add (JCalendar.WEEK_OF_MONTH, 1)
            calendar.getTime
        }

        def inTimeRange(event: Event) = event.startTime.getTime >= currentTime.getTime && 
                                        event.startTime.getTime <= nextWeek.getTime &&
                                        event.recurrence == None

        calendar.getEvents.filter(inTimeRange).toArray
    }

}

class RepeatEventsActivity extends EventListActivity
{
    override protected def welcomeMessages: List[Message] = maid.tabRepeat
    override protected def eventContent: Array[Event] = {
        val currentTime = new Date
        def inTimeRange(event: Event) = event.recurrence != None
        calendar.getEvents.filter(inTimeRange).toArray
    }

}

abstract class EventListActivity extends TypedActivity with MaidDialogUI with MaidToast
{
    protected def eventContent: Array[Event];
    protected def welcomeMessages: List[Message];

    // For Maid Dialog
    lazy val context = this
    lazy val messageBox = findView(TR.list_message)
    lazy val maidImage = findView(TR.list_maid)
    lazy val scrollView = findView(TR.list_scrollView)
    lazy val maidDialog = new MaidDialog(this)
    lazy val maid = Maid.getMaid(this, 'Maro)

    lazy val eventListView = findView(TR.event_list)
    lazy val calendarDAO = Calendar.getCalendarDAO(this)
    lazy val calendar = calendarDAO.getCalendars(0)

    val REQUEST_EDIT = 0
    val CONTEXT_MENU_DIALOG = 1
    val CONTEXT_DELETE_DIALOG = 2

    object EventAdapter extends BaseAdapter {
        override def getCount() = eventContent.size
        override def getItem(position: Int) = eventContent(position)
        override def getItemId(position: Int) = eventContent(position).eventID
        override def getView(position: Int, convertView: View, parent: ViewGroup): View = {
            val textView = new TextView(EventListActivity.this)

            textView.setText (eventContent(position).title)
            textView.setTextSize(25)
            textView.setPadding(10, 10, 10, 10)
            textView.setSingleLine(true)
            textView.setEllipsize(android.text.TextUtils.TruncateAt.MARQUEE)

            textView
        }
    }

    override def onCreateOptionsMenu(menu: Menu): Boolean = 
    {
        val add = menu.add(0, 0, 0, getString(R.string.add))
        add.setIcon(android.R.drawable.ic_menu_add)
        add.setIntent(new Intent(this, classOf[AddActivity]))
        true
    }

    override def onResume ()
    {
        super.onResume()
        maidDialog.setMessages (welcomeMessages)
        EventAdapter.notifyDataSetChanged()
    }

    override def onCreate(savedInstanceState: Bundle) 
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list)
        eventListView.setAdapter(EventAdapter)
        initListener()
    }

    def onEventLongClicked (position: Int) = {
        val bundle = new Bundle()
        bundle.putInt ("index", position)
        showDialog(CONTEXT_MENU_DIALOG, bundle)
        true
    }

    def prepareDeleteDialog(dialog: EventDeleteDialog, args: Bundle) =
    {
        val position = args.getInt("index", -1)
        val event = eventContent(position)

        def onCancelClickListener (view: View): Unit = {
            dialog.dismiss()
            this.dismissDialog(CONTEXT_MENU_DIALOG)
        }

        def onConfirmClickListener (view: View): Unit ={ 
            dismissDialog(CONTEXT_MENU_DIALOG)
            dialog.dismiss() 
            event.unregisterAlarm(this)
            calendar.deleteEvent(event)
            EventAdapter.notifyDataSetChanged()
        }

        dialog.setTitle(maid.deleteConfirm.text)
        dialog.maidDialog.setMessages(maid.contextDeleteMessage(event))

        val cancelButton = dialog.findViewById(R.id.context_delete_cancel).asInstanceOf[Button]
        val confirmButton = dialog.findViewById(R.id.context_delete_ok).asInstanceOf[Button]

        cancelButton.setOnClickListener(onCancelClickListener _)
        confirmButton.setOnClickListener(onConfirmClickListener _)
    }

    override def onActivityResult (requestCode: Int, resultCode: Int, data: Intent) = {
        requestCode match {
            case REQUEST_EDIT => EventAdapter.notifyDataSetChanged()
            case _ =>
        }
    }

    def prepareContextMenuDialog(dialog: EventContextDialog, args: Bundle) =
    {
        val position = args.getInt("index", -1)
        val event = eventContent(position)

        def onEditClickListener (view: View) = {
            dialog.dismiss()
            val intent = new Intent(Intent.ACTION_EDIT, Uri.parse(event.androidURIString))
            startActivityForResult(intent, REQUEST_EDIT)
        }
        def onDeleteClickListener (view: View) { showDialog(CONTEXT_DELETE_DIALOG, args) }

        dialog.setTitle(maid.whatShouldIDo.text)
        dialog.maidDialog.setMessages(maid.contextMenuMessage(event))

        val editButton = dialog.findViewById(R.id.context_menu_edit).asInstanceOf[Button]
        val deleteButton = dialog.findViewById(R.id.context_menu_delete).asInstanceOf[Button]
        editButton.setOnClickListener(onEditClickListener _)
        deleteButton.setOnClickListener(onDeleteClickListener _)
    }


    override def onCreateDialog (id: Int, args: Bundle) = 
    {
        id match {
            case CONTEXT_MENU_DIALOG => new EventContextDialog(this)
            case CONTEXT_DELETE_DIALOG => new EventDeleteDialog(this)
            case _ => super.onCreateDialog(id, args)
        }
    }

    override def onPrepareDialog (id: Int, dialog: Dialog, args: Bundle) = id match {
        case CONTEXT_MENU_DIALOG => 
            prepareContextMenuDialog (dialog.asInstanceOf[EventContextDialog], args)
        case CONTEXT_DELETE_DIALOG => 
            prepareDeleteDialog(dialog.asInstanceOf[EventDeleteDialog], args)

        case _ => super.onPrepareDialog(id, dialog, args)
    }

    private def onEventSelected(position: Int)
    {
         val event = eventContent(position)
         maidDialog.setMessages (maid.eventDetailMessage(event))
    }

    private def initListener() 
    {
        eventListView.setOnItemSelectedListener(onEventSelected _)
        eventListView.setOnItemClickListener(onEventSelected _)
        eventListView.setOnItemLongClickListener(onEventLongClicked _)
    }
}
