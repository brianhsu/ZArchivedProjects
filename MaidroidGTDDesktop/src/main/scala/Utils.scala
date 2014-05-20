/*
  MaidroidGTD Desktop

  Copyright (C) 2011  BrianHsu <brianhsu.hsu@gmail.com>
  
  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation; either version 2
  of the License, or (at your option) any later version.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

*/

package org.maidroid.gtd.desktop

import org.scalaquery.session.Database
import org.maidroid.gtd.controller._
import org.maidroid.gtd.model._

import java.io.File
import java.io.FileWriter

import java.util.Properties
import java.io.FileInputStream

object Preference extends Properties
{
    val prefsFile = new File(System.getProperty("user.home") + "/.maidroidGTD/config")
    if (!prefsFile.getParentFile.exists) prefsFile.getParentFile.mkdirs()
    if (!prefsFile.exists) prefsFile.createNewFile()

    this.load(new FileInputStream(prefsFile))

    def get(key: String): Option[String] = getProperty(key) match {
        case null  => None
        case value => Some(value)
    }

    def save()
    {
        store(new FileWriter(prefsFile), "")
    }

    def hasPOP3Config =
    {
        Preference.get("EMailServer").isDefined &&
        Preference.get("EMailServerPort").isDefined &&
        Preference.get("EMailUserName").isDefined &&
        Preference.get("EMailPassword").isDefined
    }

}

object Controller
{
    lazy val defaultDB = new File(System.getProperty("user.home") + "/.maidroidGTD/gtd.db")
    lazy val connection = Database.forURL(url = "jdbc:sqlite:%s".format(defaultDB), 
                                          driver = "org.sqlite.JDBC")

    lazy val gtd = new GTDController(connection)

    if (!defaultDB.getParentFile.exists) defaultDB.getParentFile.mkdirs()
    if (!defaultDB.exists) gtd.initDB()
}

