package org.maidroid.reminder2

import android.app.Dialog
import android.os.Bundle
import android.content.Context

import android.widget.ScrollView
import android.widget.TextView
import android.widget.ImageView
import android.view.Window

class EventDeleteDialog(val context: Context) extends Dialog(context) with MaidDialogUI
{
    // For Maid Dialog
    lazy val messageBox = findViewById(R.id.event_delete_message).asInstanceOf[TextView]
    lazy val maidImage = findViewById(R.id.event_delete_maid).asInstanceOf[ImageView]
    lazy val scrollView = findViewById(R.id.event_delete_scrollView).asInstanceOf[ScrollView]
    lazy val maidDialog = new MaidDialog(this)

    override def onCreate(savedInstance: Bundle)
    {
        this.setContentView(R.layout.event_delete_dialog)
    }
}

class EventContextDialog(val context: Context) extends Dialog(context) with MaidDialogUI
{
    // For Maid Dialog
    lazy val messageBox = findViewById(R.id.event_context_message).asInstanceOf[TextView]
    lazy val maidImage = findViewById(R.id.event_context_maid).asInstanceOf[ImageView]
    lazy val scrollView = findViewById(R.id.event_context_scrollView).asInstanceOf[ScrollView]
    lazy val maidDialog = new MaidDialog(this)

    override def onCreate(savedInstance: Bundle)
    {
        this.setContentView(R.layout.event_context_dialog)
    }
}
