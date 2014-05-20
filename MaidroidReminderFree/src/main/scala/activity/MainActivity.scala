package org.maidroid.reminder2

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import android.view.View
import android.content.Intent

class MainActivity extends TypedActivity with MaidDialogUI {

    // Google Ads
    lazy val adLayout = findView(TR.main_ads)
    lazy val adView = new AdView(this, AdSize.BANNER, AD_PUBLISHER_ID)

    lazy val maid = Maid.getMaid(this, 'Maro)

    lazy val context = this
    lazy val messageBox = findView(TR.message)
    lazy val maidImage = findView(TR.maid)
    lazy val scrollView = findView(TR.scrollView)
    lazy val maidDialog = new MaidDialog(this)
    lazy val addButton = findView(TR.main_add_button)
    lazy val browseButton = findView(TR.main_browse_button)
    lazy val settingButton = findView(TR.main_setting_button)
    lazy val exitButton = findView(TR.main_exit_button)

    private def initWelcomeMessageHandler ()
    {
        addButton.setOnFocusListener { 
            maidDialog.setMessages(maid.addButtonMessage)
        }

        browseButton.setOnFocusListener { 
            maidDialog.setMessages(maid.browseButtonMessage)
        }

        settingButton.setOnFocusListener { 
            maidDialog.setMessages(maid.settingButtonMessage)
        }

        exitButton.setOnFocusListener { 
            maidDialog.setMessages(maid.exitButtonMessage)
        }
    }

    private def initListener()
    {
        addButton.setOnClickListener{ view: View =>
            startActivity(new Intent(MainActivity.this, classOf[AddActivity]))
        }

        settingButton.setOnClickListener{ view: View =>
            startActivity(new Intent(MainActivity.this, classOf[SettingActivity]))
        }

        exitButton.setOnClickListener { view: View =>
            this.finish()
        }

        browseButton.setOnClickListener{ view: View =>
            startActivity(new Intent(MainActivity.this, classOf[BrowseActivity]))
        }


    }

    private def refreshAdMobs()
    {
        val adRequest = new AdRequest()
        //adRequest.addTestDevice(AD_TEST_DEVICE_ID_PHONE)
        adRequest.addTestDevice(AD_TEST_DEVICE_ID_PAD);
        adView.loadAd(adRequest)
    }

    private def initAdMobs ()
    {
        adLayout.addView(adView)
    }

    override def onResume ()
    {
        super.onResume()
        refreshAdMobs()
    }

    override def onDestroy()
    {
        adView.destroy()
        super.onDestroy()
    }

    override def onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        maidDialog.setMessages(maid.welcomeMessages)
        initWelcomeMessageHandler()
        initListener()
        initAdMobs()
    }
}
