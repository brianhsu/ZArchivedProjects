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

import org.eclipse.swt.events._
import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.graphics._
import org.eclipse.swt.layout._

import org.eclipse.swt.SWT
import org.eclipse.swt.custom._

import java.text.SimpleDateFormat

import org.maidroid.gtd.model._
import Controller.gtd.Implicit._

import I18N.TR.{tr => _t}

class MaybeTab(tabFolder: TabFolder) extends GTDTabItem(tabFolder, _t("Maybe"), "/icons/maybeBig.png") with SimpleSWT
{
    lazy val maybeTable = controls('ReferenceTable).asInstanceOf[Table]
    lazy val removeButton = controls('RemoveButton).asInstanceOf[ToolItem]
    lazy val reprocessButton = controls('ReprocessButton).asInstanceOf[ToolItem]
    lazy val searchText = controls('SearchText).asInstanceOf[Text]

    lazy val maybeIcon = new Image(Display.getDefault, getClass.getResourceAsStream("/icons/maybeSmall.png"))
    val dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm")
    val tickleDateFormatter = new SimpleDateFormat("yyyy-MM-dd")

    def setupUILayout ()
    {
        val fillGrid = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1)
        val fillWidth = new GridData(SWT.FILL, SWT.NONE, false, false)
        val searchLayoutData = new GridData(SWT.RIGHT, SWT.CENTER, false, false)
        searchLayoutData.widthHint = 200

        val maybeTable = table (
            column("")_ :: column(_t("Create Date"))_ :: column(_t("Topic"))_ :: column(_t("Title"))_ :: 
            column(_t("Tickle Date"), style = SWT.RIGHT)_ :: Nil,
            layoutData = fillGrid, id = 'ReferenceTable
        )_

        tabItem.addComposite(composite(new GridLayout(2, false)) (
            toolbar(
                toolItem(icon = "/icons/remove.png", toolTip = _t("Remove"), id = 'RemoveButton) _ :: 
                toolItem(icon = "/icons/stuff.png", toolTip = _t("Reprocess"), id = 'ReprocessButton) _ ::
                Nil,
                layoutData = fillWidth
            ),

            text(_t("Search"), style = SWT.SINGLE|SWT.ICON_SEARCH|SWT.SEARCH|SWT.ICON_CANCEL,
                 layoutData = searchLayoutData,
                 id = 'SearchText),

            maybeTable
        ))

        Controller.gtd.Maybe.list.foreach (addToMaybeTable)
        adjustColumnWidth ()
    }

    def addToMaybeTable (maybe: Maybe)
    {
        val tableItem = new TableItem(maybeTable, SWT.NONE)
        val topics = maybe.topics.map(_.name).mkString(",")
        val createdDate = dateFormatter.format(maybe.timestamp)
        val tickleDate = maybe.tickleDate.map(tickleDateFormatter.format).getOrElse(_t("None"))

        tableItem.setText(Array("", createdDate, topics, maybe.title, tickleDate))
        tableItem.setImage(0, maybeIcon)
        tableItem.setData(maybe.maybeID)
    }

    def updateRow(maybe: Maybe)
    {
        val topics = maybe.topics.map(_.name).mkString(",")
        val tableItem = maybeTable.getItems.filter(_.getData == maybe.maybeID)

        tableItem.foreach { item =>
            val createdDate = dateFormatter.format(maybe.timestamp)
            val tickleDate = maybe.tickleDate.map(tickleDateFormatter.format).getOrElse(_t("None"))

            item.setText(Array("", createdDate, topics, maybe.title, tickleDate))
        }
    }

    def adjustColumnWidth ()
    {
        maybeTable.getColumns.foreach { column =>
            column.pack() 
            column.setWidth(column.getWidth + 5)
        }

        maybeTable.getColumns()(0).setResizable(false)
        maybeTable.getColumns()(0).setWidth(30)
    }

    def setupListener ()
    {
        maybeTable.addMouseListener(new MouseAdapter() {
            override def mouseDoubleClick(e: MouseEvent)
            {
                val tableItem = maybeTable.getSelection.head
                val maybe = Controller.gtd.Maybe.get(tableItem.getData.asInstanceOf[Int])
                val dialog = new EditMaybeDialog(maybe)
            }
        })

        maybeTable.addSelectionListener{e: SelectionEvent =>
            val maybeID = maybeTable.getSelection.head.getData.asInstanceOf[Int]
            val maybe = Controller.gtd.Maybe.get(maybeID)

            DialogWindow.setMessages(Maid.MaybeTabMessage.details(maybe))
        }

        maybeTable.getColumns.zipWithIndex.drop(1).foreach {case(column, index) =>
            column.setSorting(index)
        }

        removeButton.addSelectionListener { e: SelectionEvent =>

            maybeTable.getSelection.foreach { item =>
                val maybeID = item.getData.asInstanceOf[Int]
                val maybe = Controller.gtd.Maybe.get(maybeID)
                maybe.delete()
                maybeTable.removeByData(maybeID)
                DialogWindow.setMessages(Maid.MaybeTabMessage.deleted(maybe))
            }
        }

        reprocessButton.addSelectionListener { e: SelectionEvent =>
            maybeTable.getSelection.foreach { item =>
                val maybeID = item.getData.asInstanceOf[Int]
                val maybe = Controller.gtd.Maybe.get(maybeID)
                val stuff = maybe.reprocess()
                
                maybeTable.removeByData(maybeID)
                MainWindow.inboxTab.addToStuffTable(stuff)
                MainWindow.inboxTab.adjustColumnWidth()

                DialogWindow.setMessages(Maid.MaybeTabMessage.reprocess(maybe))
            }
        }

        searchText.addSelectionListener (new SelectionAdapter() {
            override def widgetDefaultSelected(e: SelectionEvent)
            {
                val target = searchText.getText

                if (e.detail != SWT.CANCEL && target != "") {
                    val isFound = maybeTable.findNext(target)
                    DialogWindow.setMessages(Maid.MaybeTabMessage.search(isFound))
                }
            }
        })
    }

    setupUILayout ()
    setupListener ()
}

