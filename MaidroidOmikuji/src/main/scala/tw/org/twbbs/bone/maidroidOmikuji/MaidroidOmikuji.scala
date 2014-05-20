package org.maidroid.omikuji;

import android.app.PendingIntent;

import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetManager;

import android.content.Context;
import android.content.Intent;

import android.net.Uri

import android.widget.RemoteViews;
import android.widget.Toast;

object MaidroidOmikuji
{
    def updateWidget (context : Context, widgetID : Int, 
                      resType : String,  value : String,
                      appWidgetManager: AppWidgetManager)
    {
        def getResID (name : String) : Int = {
            context.getResources().getIdentifier (name, "drawable", 
                                                  Util.packageName)
    
        }

        val views = new RemoteViews (context.getPackageName(), 
                                     R.layout.main)

         // Set image for widgets.
        resType match {
            case "Drawable" =>  val resID = getResID (value)
                               views.setImageViewResource (R.id.maid, resID)
            case "Uri"     =>  val uri = Uri.parse (value)
                               views.setImageViewUri (R.id.maid, uri)
        }

        // Connect singal to the ImageView
        val intent  = new Intent (context, classOf[OmikujiService])
        val pIntent = PendingIntent.getService (
                          context, 0, 
                          intent,PendingIntent.FLAG_UPDATE_CURRENT
                      )
        views.setOnClickPendingIntent (R.id.maid, pIntent)
        appWidgetManager.updateAppWidget (widgetID, views)
       
    }

}

class MaidroidOmikuji extends AppWidgetProvider 
{
    override def onUpdate (context : Context, 
                           appWidgetManager : AppWidgetManager, 
                           appWidgetIds : Array[Int]) 
    {
        for (widgetID <- appWidgetIds) {
            val (resType, value) = OmikujiConfig.loadWidgetConfig (context,
                                                                   widgetID)
            MaidroidOmikuji.updateWidget (context, widgetID, 
                                          resType, value,
                                          appWidgetManager)
        }

    }



}
