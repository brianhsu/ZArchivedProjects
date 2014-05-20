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
import java.util.Calendar

import org.maidroid.gtd.model._
import Controller.gtd.Implicit._

import I18N.TR.{tr => _t}

class InboxTab(tabFolder: TabFolder) extends GTDTabItem(tabFolder, _t("Inbox"), "/icons/inbox.png") with SimpleSWT
{
    val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")

    lazy val stuffTable = controls('StuffTable).asInstanceOf[Table]
    lazy val addButton = controls('AddButton).asInstanceOf[ToolItem]
    lazy val removeButton = controls('RemoveButton).asInstanceOf[ToolItem]
    lazy val processButton = controls('ProcessButton).asInstanceOf[ToolItem]
    lazy val mailButton = controls('MailButton).asInstanceOf[ToolItem]

    lazy val searchText = controls('SearchText).asInstanceOf[Text]

    lazy val stuffIcon = new Image(Display.getDefault, getClass.getResourceAsStream("/icons/stuff.png"))

    def setupUILayout ()
    {
        val fillGrid = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1)
        val fillWidth = new GridData(SWT.FILL, SWT.NONE, false, false)
        val searchLayoutData = new GridData(SWT.RIGHT, SWT.CENTER, false, false)
        searchLayoutData.widthHint = 200

        val stuffTable = table (
            column("")_ :: column(_t("Topic"))_ :: column(_t("Title"))_ :: Nil,
            layoutData = fillGrid, id = 'StuffTable
        )_

        tabItem.addComposite(composite(new GridLayout(2, false)) (
            toolbar(
                toolItem(icon = "/icons/add.png", toolTip = _t("Add"), id = 'AddButton)_ :: 
                toolItem(icon = "/icons/remove.png", toolTip = _t("Remove"), id = 'RemoveButton) _ :: 
                toolItem(icon = "/icons/process.png", toolTip = _t("Process"), id = 'ProcessButton) _ ::
                toolItem(icon = "/icons/receiveMail.png", toolTip = _t("Receive Mail"), id = 'MailButton)_ ::
                Nil,
                layoutData = fillWidth
            ),

            text(
                _t("Search"), style = SWT.SINGLE|SWT.ICON_SEARCH|SWT.SEARCH|SWT.ICON_CANCEL,
                layoutData = searchLayoutData,
                id = 'SearchText
            ),

            stuffTable
        ))

        Controller.gtd.Stuff.list.foreach (addToStuffTable)
        adjustColumnWidth()
    }

    def startProcess ()
    {
        val stuffs = stuffTable.getItems.map { item =>
            val stuffID = item.getData.asInstanceOf[Int]
            Controller.gtd.Stuff.get(stuffID)
        }

        val processDialog = new ProcessDialog(stuffs.toList, stuffTable)
    }

    def adjustColumnWidth ()
    {
        stuffTable.getColumns.foreach { column =>
            column.pack() 
            column.setWidth(column.getWidth + 5)
        }

        stuffTable.getColumns()(0).setResizable(false)
        stuffTable.getColumns()(0).setWidth(30)
    }

    def setupUIListener ()
    {
        stuffTable.addMouseListener(new MouseAdapter() {
            override def mouseDoubleClick(e: MouseEvent)
            {
                val tableItem = stuffTable.getSelection.head
                val stuff = Controller.gtd.Stuff.get(tableItem.getData.asInstanceOf[Int])
                val editDialog = new EditStuffDialog(stuff)
            }
        })

        addButton.addSelectionListener { e: SelectionEvent =>
            val addDialog = new AddStuffDialog
        }

        removeButton.addSelectionListener { e: SelectionEvent =>
            stuffTable.getSelectionIndices.foreach { index =>
                val tableItem = stuffTable.getItem(index)
                val stuffID = tableItem.getData.asInstanceOf[Int]
                val stuff = Controller.gtd.Stuff.get(stuffID)
                stuff.delete()
                stuffTable.remove(index)
                DialogWindow.setMessages(Maid.InboxTabMessage.deleted(stuff))
            }
        }

        processButton.addSelectionListener { e: SelectionEvent =>
            startProcess()            
        }

        stuffTable.addSelectionListener {e: SelectionEvent =>

            val stuffID = stuffTable.getSelection.head.getData.asInstanceOf[Int]
            val stuff = Controller.gtd.Stuff.get(stuffID)
               
            DialogWindow.setMessages(Maid.InboxTabMessage.detail(stuff))
        }

        searchText.addSelectionListener (new SelectionAdapter() {
            override def widgetDefaultSelected(e: SelectionEvent)
            {
                val target = searchText.getText

                if (e.detail != SWT.CANCEL && target != "") {

                    val isFound = stuffTable.findNext(target)
                    DialogWindow.setMessages(Maid.InboxTabMessage.search(isFound))
                }
            }
        })

        mailButton.addSelectionListener{e: SelectionEvent =>
            DialogWindow.setMessages(Maid.InboxTabMessage.checkMail)
            POP3Receiver.checkMailAtInboxTab()
        }

    }

    def addToStuffTable (stuff: Stuff) {
        val tableItem = new TableItem(stuffTable, SWT.NONE)
        val topics = stuff.topics.map(_.name).mkString(",")
        
        tableItem.setText(Array("", topics, stuff.title))
        tableItem.setImage(0, stuffIcon)
        tableItem.setData(stuff.stuffID)
    }

    def updateRow(stuff: Stuff)
    {
        val topics = stuff.topics.map(_.name).mkString(",")
        val tableItem = stuffTable.getItems.filter(_.getData == stuff.stuffID)

        tableItem.foreach { item =>
            item.setText(Array("", topics, stuff.title))
            item.setData(stuff.stuffID)
        }
    }

    def reprocessTickled()
    {
        val calendar = Calendar.getInstance
        calendar.set(Calendar.HOUR, 23)
        calendar.set(Calendar.MINUTE, 59)

        def shouldBeTickled(maybe: Maybe) =
        {
            maybe.tickleDate.isDefined &&
            maybe.tickleDate.get.before(calendar.getTime)
        }

        val tickled = Controller.gtd.Maybe.list.filter(shouldBeTickled)
        tickled.foreach { maybe =>
            val title = _t("[Tickle %s] %s") format(dateFormatter.format(calendar.getTime), maybe.title)
            val stuff = maybe.reprocess()
            val updatedStuff = stuff.copy(title = title)

            stuff.update(updatedStuff)
            addToStuffTable(updatedStuff)

            MainWindow.maybeTab.maybeTable.removeByData(maybe.maybeID)
        }
    }

    setupUILayout()
    setupUIListener()
    reprocessTickled()
}

