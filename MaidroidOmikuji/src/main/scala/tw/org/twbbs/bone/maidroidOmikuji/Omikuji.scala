package org.maidroid.omikuji

import scala.util.Random

import android.content.Context
import android.net.Uri

class Omikuji (context : Context)
{
    val number = (new Random).nextInt (101)
    val url    = String.format (
                    "http://bone.twbbs.org.tw/omikuji/omikuji%03d.jpg",
                    number.asInstanceOf[Object]
                 );

    def uri = Uri.parse (url)

}
