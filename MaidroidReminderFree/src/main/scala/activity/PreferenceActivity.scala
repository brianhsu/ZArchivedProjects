package org.maidroid.reminder2

import android.app.Activity
import android.preference.PreferenceActivity
import android.os.Bundle
import android.widget.TextView
import android.view.View
import android.content.Intent

class SettingActivity extends PreferenceActivity with TypedActivity with MaidDialogUI {

    lazy val context = this
    lazy val messageBox = findView(TR.setting_message)
    lazy val maidImage = findView(TR.setting_maid)
    lazy val scrollView = findView(TR.setting_scrollView)
    lazy val maidDialog = new MaidDialog(this)
    lazy val maid = Maid.getMaid(this, 'Maro)

    private val VOICE_POS = 1
    private val SOUND_POS = 3
    private val VIBRATION_POS = 4

    def initListener ()
    {
        this.getListView.setOnItemSelectedListener { position: Int => 
            position match {
                case VOICE_POS => maidDialog.setMessages(maid.preferenceVoice)
                case SOUND_POS => maidDialog.setMessages(maid.preferenceSound)
                case VIBRATION_POS => maidDialog.setMessages(maid.preferenceVibration)
                case _ => 
            }
        }
    }

    override def onCreate(savedInstanceState: Bundle) 
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.preference)
        addPreferencesFromResource(R.xml.settings)
        maidDialog.setMessages(maid.preferenceWelcome)
        initListener()
    }
}
