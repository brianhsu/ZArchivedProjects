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

class ReferenceTab(tabFolder: TabFolder) extends GTDTabItem(tabFolder, _t("Reference"), "/icons/reference.png") with SimpleSWT
{
    lazy val referenceTable = controls('ReferenceTable).asInstanceOf[Table]
    lazy val removeButton = controls('RemoveButton).asInstanceOf[ToolItem]
    lazy val reprocessButton = controls('ReprocessButton).asInstanceOf[ToolItem]
    lazy val searchText = controls('SearchText).asInstanceOf[Text]

    lazy val referenceIcon = new Image(Display.getDefault, getClass.getResourceAsStream("/icons/referenceIcon.png"))
    val dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm")


    def setupUILayout ()
    {
        val fillGrid = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1)
        val fillWidth = new GridData(SWT.FILL, SWT.NONE, false, false)
        val searchLayoutData = new GridData(SWT.RIGHT, SWT.CENTER, false, false)

        searchLayoutData.widthHint = 200

        val referenceTable = table (
            column("")_ :: column(_t("Date"))_ :: column(_t("Topic"))_ :: column(_t("Title"))_ :: Nil,
            layoutData = fillGrid, id = 'ReferenceTable
        )_

        tabItem.addComposite(composite(new GridLayout(2, false)) (
            toolbar(
                toolItem(icon = "/icons/remove.png", toolTip = _t("Remove"), id = 'RemoveButton) _ :: 
                toolItem(icon = "/icons/stuff.png", toolTip = _t("Reprocess"), id = 'ReprocessButton) _ ::
                Nil,
                layoutData = fillWidth
            ),

            text(_t("Search"), style = SWT.SEARCH|SWT.ICON_SEARCH|SWT.ICON_CANCEL, 
                 layoutData = searchLayoutData, id = 'SearchText),

            referenceTable
        ))

        Controller.gtd.Reference.list.foreach (addToReferenceTable)
        adjustColumnWidth ()
    }

    def setupListener ()
    {
        referenceTable.addMouseListener(new MouseAdapter() {
            override def mouseDoubleClick(e: MouseEvent)
            {
                val tableItem = referenceTable.getSelection.head
                val reference = Controller.gtd.Reference.get(tableItem.getData.asInstanceOf[Int])
                val editDialog = new EditReferenceDialog(reference)
            }
        })

        referenceTable.getColumns.zipWithIndex.drop(1).foreach {case(column, index) =>
            column.setSorting(index)
        }

        referenceTable.addSelectionListener{e: SelectionEvent =>
            val referenceID = referenceTable.getSelection.head.getData.asInstanceOf[Int]
            val reference = Controller.gtd.Reference.get(referenceID)

            DialogWindow.setMessages(Maid.ReferenceTabMessage.referenceDetails(reference))
        }

        removeButton.addSelectionListener { e: SelectionEvent =>

            referenceTable.getSelection.foreach { item =>
                val referenceID = item.getData.asInstanceOf[Int]
                val reference = Controller.gtd.Reference.get(referenceID)

                reference.delete()
                referenceTable.removeByData(referenceID)

                DialogWindow.setMessages(Maid.ReferenceTabMessage.deleted(reference))
            }
        }

        reprocessButton.addSelectionListener { e: SelectionEvent =>
            referenceTable.getSelection.foreach { item =>
                val referenceID = item.getData.asInstanceOf[Int]
                val stuff = Controller.gtd.Reference.get(referenceID).reprocess()
                
                referenceTable.removeByData(referenceID)
                MainWindow.inboxTab.addToStuffTable(stuff)
                MainWindow.inboxTab.adjustColumnWidth()

                DialogWindow.setMessages(Maid.ReferenceTabMessage.reprocess(stuff))
            }
        }

        searchText.addSelectionListener (new SelectionAdapter() {
            override def widgetDefaultSelected(e: SelectionEvent)
            {
                val target = searchText.getText

                if (e.detail != SWT.CANCEL && target != "") {
                    val isFound = referenceTable.findNext(target)
                    DialogWindow.setMessages(Maid.ReferenceTabMessage.search(isFound))
                }
            }
        })

    }
    
    def addToReferenceTable (reference: Reference)
    {
        val tableItem = new TableItem(referenceTable, SWT.NONE)
        val topics = reference.topics.map(_.name).mkString(",")

        tableItem.setText(Array("", dateFormatter.format(reference.timestamp), topics, reference.title))
        tableItem.setImage(0, referenceIcon)
        tableItem.setData(reference.referenceID)

    }

    def updateRow(reference: Reference)
    {
        val topics = reference.topics.map(_.name).mkString(",")
        val tableItem = referenceTable.getItems.filter(_.getData == reference.referenceID)

        tableItem.foreach { item =>
            item.setText(Array("", dateFormatter.format(reference.timestamp), topics, reference.title))
            item.setData(reference.referenceID)
        }
    }

    def adjustColumnWidth ()
    {
        referenceTable.getColumns.foreach { column =>
            column.pack() 
            column.setWidth(column.getWidth + 5)
        }

        referenceTable.getColumns()(0).setResizable(false)
        referenceTable.getColumns()(0).setWidth(30)
    }

    setupUILayout()
    setupListener()
}

