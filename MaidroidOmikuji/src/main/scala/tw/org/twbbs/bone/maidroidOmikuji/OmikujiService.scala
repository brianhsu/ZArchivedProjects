package org.maidroid.omikuji

import android.app.Service
import android.os.IBinder
import android.content.Intent
import android.content.Context
import android.widget.Toast
import android.net.Uri

class OmikujiService extends Service
{
    override def onStart (intent : Intent, startID : Int)
    {
        val omikuji = new Omikuji (this)
        val intent  = new Intent (Intent.ACTION_VIEW, omikuji.uri).
                      setFlags (Intent.FLAG_ACTIVITY_NEW_TASK)

        startActivity (intent)
    }

    override def onBind (intent : Intent) : IBinder = {
        // We don't need to bind to this service
        return null
    }

}

