package org.maidroid.omikuji

import scala.collection.mutable.ArrayBuffer

import android.content.Context
import android.net.Uri

import android.view.View
import android.view.ViewGroup

import android.widget.BaseAdapter
import android.widget.ImageView

object Util
{
    val packageName = "org.maidroid.omikuji"

    private def getResID (context: Context, name : String) : Int = 
    {
        context.getResources().getIdentifier (name, "drawable", 
                                              Util.packageName)
    }

    def getImageView (context : Context, 
                      image : (Symbol, String)) : ImageView = 
    {
        val (resType, value) = image
        getImageView (context, resType.name, value)
    }

    def getImageView (context : Context, resType: String, 
                      value : String) : ImageView =
    {
        val imageView = new ImageView (context)

        resType match {
            case "Drawable" => val resID = getResID (context, value)
                              imageView.setImageResource (resID)

            case "Uri"     => imageView.setImageURI (Uri.parse(value))
        }

        imageView
    }
}

class ImageAdapter (context : Context, list : List[(Symbol, String)]) extends 
      BaseAdapter
{
    private val images = new ArrayBuffer[(Symbol, String)]
    images ++= list

    override def getCount  = images.length
    override def getItemId (position: Int) : Long   = position
    override def getItem   (position: Int) : Object = images(position).
                                                      asInstanceOf[Object]

    override def getView (position : Int, convertView : View, 
                          parent : ViewGroup) : View =
    {
        Util.getImageView (context, images(position))
    }

    def add (image : (Symbol, String)) : Int = 
    {
        images += image 
        images.length - 1 // The index of last image
    }

    def getTuple (position : Int) : (Symbol, String) =
    {
        images (position)
    }
}
