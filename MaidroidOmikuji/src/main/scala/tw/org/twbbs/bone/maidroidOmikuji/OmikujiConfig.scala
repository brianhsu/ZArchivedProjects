package org.maidroid.omikuji

import scala.collection.mutable.ArrayBuffer

import android.app.Activity
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences;

import android.net.Uri
import android.os.Bundle

import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Gallery
import android.widget.RemoteViews;

object OmikujiConfig
{
    private val REQUEST_ADD = 1;

    private val PREFS_NAME = Util.packageName
    private val PREFS_RES_TYPE = "resType_"
    private val PREFS_VALUE    = "value_"

    def saveWidgetConfig (context : Context, widgetID : Int,
                          resType : String,  value : String)
    {
        val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit

        prefs.putString (PREFS_RES_TYPE + widgetID, resType)
        prefs.putString (PREFS_VALUE + widgetID, value)

        prefs.commit ()

    }

    def loadWidgetConfig (context : Context, 
                          widgetID : Int) : (String, String) =
    {
        val prefs = context.getSharedPreferences(PREFS_NAME, 0)
        val resType = prefs.getString(PREFS_RES_TYPE + widgetID, "Drawable");
        val value = prefs.getString(PREFS_VALUE + widgetID, "miko01");

        (resType, value)
    }

}

class OmikujiConfig extends Activity
{
    private val imageFiles = List ("miko01", "miko02", 
                                   "miko03", "miko04", "miko05")

    private val drawableList = for (file <- imageFiles) yield ('Drawable, file)
    private lazy val imageAdapter = new ImageAdapter (this, drawableList)
    private lazy val widgetID   = getWidgetID
    private lazy val gallery    = findViewById (R.id.characters).
                                  asInstanceOf[Gallery]

    override def onCreate (savedInstanceState : Bundle)
    {
        super.onCreate (savedInstanceState)

        setTitle (R.string.title)
        setResult (Activity.RESULT_CANCELED)

        setContentView (R.layout.omikuji_config)

        connectSignals
        gallery.setAdapter (imageAdapter)
    }

    override def onActivityResult (requestCode : Int, resultCode : Int, 
                                   data : Intent)
    {
        if (resultCode != Activity.RESULT_OK)
            return

        requestCode match {
            case OmikujiConfig.REQUEST_ADD =>
                val uri = data.getData
                val pos = imageAdapter.add (('Uri, uri.toString))

                imageAdapter.notifyDataSetChanged()
                gallery.setSelection (pos)

        }
    }

    private def getResID (name : String) : Int = {
        getResources().getIdentifier (name, "drawable", 
                                      Util.packageName)

    }

    private def connectSignals ()
    {
        val okButton     = findViewById (R.id.okButton).asInstanceOf[Button]
        val addButton    = findViewById (R.id.addButton).asInstanceOf[Button]
        val cancelButton = findViewById (R.id.cancelButton).asInstanceOf[Button]

        okButton.setOnClickListener (onOKClicked)
        addButton.setOnClickListener (onAddClicked)
        cancelButton.setOnClickListener (onCancelClicked)
    }

    private def getWidgetID : Int =
    {
        val extras = getIntent.getExtras

        // Return the widgetID
        if (extras != null) 
            extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, 
                          AppWidgetManager.INVALID_APPWIDGET_ID);
        else
            AppWidgetManager.INVALID_APPWIDGET_ID
    }

    private def configureWidget (image : (Symbol, String))
    {
        val appWidgetManager = AppWidgetManager.getInstance(this);
        val (resType, value) = image

        // Save the configuration
        OmikujiConfig.saveWidgetConfig (this, widgetID, resType.name,
                                        value)

        // Update the widget
        MaidroidOmikuji.updateWidget (this, widgetID, resType.name,
                                      value, appWidgetManager)
    }

    val onOKClicked = new View.OnClickListener () 
    {
        def onClick (view : View) 
        {
            val position = gallery.getSelectedItemPosition
            val image    = imageAdapter.getTuple(position)

            configureWidget (image)

            val resultIntent = new Intent ()
            resultIntent.putExtra (AppWidgetManager.EXTRA_APPWIDGET_ID, 
                                   widgetID)
            setResult (Activity.RESULT_OK, resultIntent)
            finish ()
        }
    }

    val onAddClicked = new View.OnClickListener () 
    {
        def onClick (view : View) 
        {
            val intent = new Intent (Intent.ACTION_GET_CONTENT)
            intent.setType ("image/*")
            startActivityForResult (intent, OmikujiConfig.REQUEST_ADD)
        }

    }

    val onCancelClicked = new View.OnClickListener () 
    {
        def onClick (view : View) 
        {
            setResult (Activity.RESULT_CANCELED)
            finish ()
        }
    }

}


