package org.maidroid.reminder

import android.app.Dialog

import android.content.DialogInterface
import android.content.Context

import android.preference.DialogPreference

import android.view.View
import android.view.ViewGroup

import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener

import android.util.AttributeSet

class VolumePreference(context: Context, attrs: AttributeSet, defStyle: Int) extends DialogPreference(context, attrs, 0) with SeekBar.OnSeekBarChangeListener
{
    private var      mValue   = 0
    private lazy val seekBar = getDialog.findViewById (R.id.volumeSeekBar).asInstanceOf[SeekBar]

    def this (context: Context, attrs: AttributeSet) = this (context, attrs, 0)
    def value = getPersistedInt (100)

    override def onBindDialogView (view: View)
    {
        super.onBindDialogView (view)
        val seekbar = view.findViewById (R.id.volumeSeekBar).asInstanceOf[SeekBar]

        seekbar.setOnSeekBarChangeListener (this)
        seekbar.setProgress (getPersistedInt (100))
    }


    override def onClick (dialog: DialogInterface, which: Int) 
    {

        if (which == DialogInterface.BUTTON_POSITIVE) {
            persistInt (mValue)
        }

    }

    override def onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) 
    {
        mValue = progress
    }

    override def onStartTrackingTouch (seekBar: SeekBar) {}
    override def onStopTrackingTouch  (seekBar: SeekBar) {}
}
