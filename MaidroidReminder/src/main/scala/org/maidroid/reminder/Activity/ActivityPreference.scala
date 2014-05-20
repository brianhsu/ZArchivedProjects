/**
 *  Maidroid Reminder
 *  
 *  -------------------------------------------------------------------------
 *  @license
 *
 *  This file is part of Maidroid Reminder
 *
 *  Maidroid Reminder is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Maidroid Reminder is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Maidroid Reminder.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  --------------------------------------------------------------------------
 *  @author Brian Hsu (brianhsu.hsu [at] gmail.com)
 *  
 */

package org.maidroid.reminder

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener

import android.preference.ListPreference
import android.preference.CheckBoxPreference

import android.preference.Preference
import android.preference.PreferenceActivity
import android.preference.PreferenceManager
import android.preference.PreferenceScreen

import android.content.Intent
import android.os.Bundle

import android.util.Log

class ActivityPreference extends PreferenceActivity
{
    /*=================================================================
     * Private Fields
     *===============================================================*/

    private lazy val maid = findPreference ("maid").asInstanceOf[ListPreference]
    private lazy val ignoreSilentPreference = findPreference ("ignoreSilent").
                                              asInstanceOf[CheckBoxPreference]

    private lazy val sharedPreference = PreferenceManager.
                                        getDefaultSharedPreferences(this)

    /*=================================================================
     * Private Methods
     *===============================================================*/

    private def updateMaidSummary ()
    {
        val maidTitle = maid.getEntry
        val message   = getResources.getString(R.string.prefs_current_maid).
                        format (maidTitle)

        maid.setSummary (message)
    }

    private def setMaidPreference ()
    {
        val maidMap = Maid.maidMap (this)

        val maidTitle = maidMap.map {case (id, maid) => maid.name}.
                                toList.toArray[CharSequence]

        val maidID    = maidMap.keySet.toArray[CharSequence]

        maid.setEntries     (maidTitle)
        maid.setEntryValues (maidID)

        if (maid.getEntry == null) {
            val value = new MaidroidPreference(this).maid
            val index = maid.findIndexOfValue (value)

            maid.setValueIndex (if (index >= 0) index else 0)
        }

        updateMaidSummary ()
    }

    /*=================================================================
     * Callbacks
     *===============================================================*/

    private def updateIgnoreSilentMode ()
    {
        val maidroidPreference = new MaidroidPreference (ActivityPreference.this)

        if (maidroidPreference.voiceOn == false) {
            ignoreSilentPreference.setChecked (false)
        }
    }

    private lazy val changeListener = new OnSharedPreferenceChangeListener ()
    {
        def onSharedPreferenceChanged (preference: SharedPreferences,
                                       key: String) 
        {
            updateMaidSummary ()
            updateIgnoreSilentMode ()
        }
    }


    /*=================================================================
     * Android Framework
     *===============================================================*/

    override def onCreate(savedInstanceState : Bundle)
    {
        super.onCreate (savedInstanceState)

        addPreferencesFromResource (R.xml.prefs)
        setMaidPreference ()

        sharedPreference.registerOnSharedPreferenceChangeListener (
            changeListener
        )

    }
}

