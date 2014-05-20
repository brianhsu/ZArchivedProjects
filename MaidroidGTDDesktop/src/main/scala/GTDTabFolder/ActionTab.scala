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

import scala.sys.process._

import org.eclipse.swt.events._
import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.graphics._
import org.eclipse.swt.layout._

import org.eclipse.swt.SWT
import org.eclipse.swt.custom._
import org.eclipse.swt.program.Program

import java.text.SimpleDateFormat
import java.util.Date

import org.maidroid.gtd.model._
import Controller.gtd.Implicit._

import I18N.TR.{tr => _t}

class ActionTab(tabFolder: TabFolder) extends GTDTabItem(tabFolder, _t("Action"), "/icons/action.png") with SimpleSWT
{
    def createAllTab (tabFolder: TabFolder)
    {
        val tabItem = new TabItem(tabFolder, SWT.NONE)
        tabItem.setText(_t("All"))
        tabItem.addComposite (composite(new FillLayout)(
            table (
                column(_t("Done"), style = SWT.CENTER)_ :: column("")_ :: column(_t("Create Date"))_ :: 
                column(_t("Topic"))_ :: column(_t("Project"))_ :: column(_t("Context"))_ :: column(_t("Title"))_ :: 
                column(_t("Delegated To"))_ :: column(_t("Scheduled Date"))_ :: Nil,
                style = SWT.FULL_SELECTION | SWT.CHECK,
                id = 'AllActionTable
            )
        ))
    }

    def createDoASAPTab (tabFolder: TabFolder)
    {
        val tabItem = new TabItem(tabFolder, SWT.NONE)
        tabItem.setText(_t("Do ASAP"))
        tabItem.addComposite (composite(new FillLayout)(
            table (
                column(_t("Done"))_ :: column("")_ :: column(_t("Create Date"))_ :: 
                column(_t("Topic"))_ :: column(_t("Project"))_ :: column(_t("Context"))_ :: 
                column(_t("Title"))_ :: Nil,
                style = SWT.FULL_SELECTION | SWT.CHECK,
                id = 'DoASAPTable
            )
        ))
    }

    def createDelegatedTab (tabFolder: TabFolder)
    {
        val tabItem = new TabItem(tabFolder, SWT.NONE)
        tabItem.setText(_t("Delegated"))
        tabItem.addComposite (composite(new FillLayout)(
            table (
                column(_t("Done"))_ :: column("")_ :: column(_t("Create Date"))_ :: 
                column(_t("Topic"))_ :: column(_t("Project"))_ :: column(_t("Context"))_ :: 
                column(_t("Title"))_ :: column(_t("Delegated To"))_ :: column(_t("Follow up"))_ :: Nil,
                style = SWT.FULL_SELECTION | SWT.CHECK,
                id = 'DelegatedTable
            )
        ))
    }

    def createScheduledTab (tabFolder: TabFolder)
    {
        val tabItem = new TabItem(tabFolder, SWT.NONE)
        tabItem.setText(_t("Scheduled"))
        tabItem.addComposite (composite(new FillLayout)(
            table (
                column(_t("Done"))_ :: column("")_ :: column(_t("Create Date"))_ :: 
                column(_t("Topic"))_ :: column(_t("Project"))_ :: column(_t("Context"))_ ::
                column(_t("Title"))_ :: column(_t("Scheduled Date"))_ :: Nil,
                style = SWT.FULL_SELECTION | SWT.CHECK,
                id = 'ScheduledTable
            )
        ))
    }

    def createDoneTab (tabFolder: TabFolder)
    {
        val tabItem = new TabItem(tabFolder, SWT.NONE)
        tabItem.setText(_t("Done"))
        tabItem.addComposite (composite(new FillLayout)(
            table (
                column(_t("Done"), style = SWT.CENTER)_ :: column("")_ :: column(_t("Create Date"))_ :: 
                column(_t("Topic"))_ :: column(_t("Project"))_ :: column(_t("Context"))_ ::
                column(_t("Title"))_ :: Nil,
                style = SWT.FULL_SELECTION | SWT.CHECK,
                id = 'DoneTable
            )
        ))

    }

    def setupUILayout ()
    {
        val fillGrid = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1)
        val fillWidth = new GridData(SWT.FILL, SWT.NONE, false, false)
        val searchLayoutData = new GridData(SWT.RIGHT, SWT.CENTER, false, false)
        searchLayoutData.widthHint = 200

        tabItem.addComposite(composite(new GridLayout(2, false)) (
            toolbar(
                toolItem(icon = "/icons/remove.png", toolTip = _t("Remove"), id = 'RemoveButton) _ :: 
                toolItem(icon = "/icons/stuff.png", toolTip = _t("Reprocess"), id = 'ReprocessButton) _ ::
                toolItem(icon = "/icons/mail.png", toolTip = _t("Mail"), id = 'MailTo) _ ::
                toolItem(icon = "/icons/followup.png", toolTip = _t("Follow Up"), id = 'FollowUp)_ ::
                Nil
            ),

            text(_t("Search"), style = SWT.SINGLE|SWT.ICON_SEARCH|SWT.SEARCH|SWT.ICON_CANCEL,
                 layoutData = searchLayoutData,
                 id = 'SearchText),

            tabFolder (
                createAllTab _ :: createDoASAPTab _ :: createDelegatedTab _ ::
                createScheduledTab _ :: createDoneTab _ :: Nil, 
                layoutData = fillGrid,
                id = 'ActionTabFolder
            )

        ))
    }

    lazy val actionTabFolder = controls('ActionTabFolder).asInstanceOf[TabFolder]

    lazy val allActionTable = controls('AllActionTable).asInstanceOf[Table]
    lazy val doASAPTable = controls('DoASAPTable).asInstanceOf[Table]
    lazy val delegatedTable = controls('DelegatedTable).asInstanceOf[Table]
    lazy val scheduledTable = controls('ScheduledTable).asInstanceOf[Table]
    lazy val doneTable = controls('DoneTable).asInstanceOf[Table]
    lazy val searchText = controls('SearchText).asInstanceOf[Text]

    lazy val removeButton = controls('RemoveButton).asInstanceOf[ToolItem]
    lazy val reprocessButton = controls('ReprocessButton).asInstanceOf[ToolItem]

    lazy val mailToButton = controls('MailTo).asInstanceOf[ToolItem]
    lazy val followUpButton = controls('FollowUp).asInstanceOf[ToolItem]

    lazy val doASAPIcon = new Image(Display.getDefault, getClass.getResourceAsStream("/icons/doASAP.png"))
    lazy val delegatedIcon = new Image(Display.getDefault, getClass.getResourceAsStream("/icons/delegated.png"))
    lazy val scheduledIcon = new Image(Display.getDefault, getClass.getResourceAsStream("/icons/scheduled.png"))

    val dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm")

    def tabToTable(tabIndex: Int) = tabIndex match {
        case 0 => allActionTable
        case 1 => doASAPTable
        case 2 => delegatedTable
        case 3 => scheduledTable
        case 4 => doneTable
    }

    def tabFromAction (action: Action) = action.actionType match {
        case ActionType.DoASAP => doASAPTable
        case ActionType.Delegated => delegatedTable
        case ActionType.Scheduled => scheduledTable
    }

    def updateDone(action: Action)
    {
        val createDate = dateFormatter.format(action.createTimestamp)
        val topic = action.topics.map(_.name).mkString(",")
        val project = action.projects.map(_.title).mkString(",")
        val context = action.contexts.map(_.name).mkString(",")

        val item = doneTable.getItems.filter (_.getData == action.actionID)
        item.foreach { row =>
            row.setText(Array("", "", createDate, topic, project, context, action.title))
        }
    }

    def updateRow (oldAction: Action, newAction: Action)
    {
        updateAllActionTable(newAction)

        if (oldAction.isDone && newAction.isDone) {
            updateDone(newAction)
            return
        }

        oldAction.actionType != newAction.actionType match {
            case true  =>
                tabFromAction(oldAction).removeByData(oldAction.actionID)
                addAction (newAction, false)

            case false =>
                newAction.actionType match {
                    case ActionType.DoASAP => updateDoASAP(newAction)
                    case ActionType.Delegated => updateDelegated(newAction)
                    case ActionType.Scheduled => updateScheduled(newAction)
                }
        }
    }


    object TableSorting
    {
        def sortByType(table: Table, column: TableColumn, direction: Int)
        {
            val sortedItem = table.getItems.sortWith { (item1, item2) =>
                val action1 = Controller.gtd.Action.get(item1.getData.asInstanceOf[Int])
                val action2 = Controller.gtd.Action.get(item2.getData.asInstanceOf[Int])

                def order(actionType: ActionType) = actionType match {
                    case ActionType.DoASAP => 1
                    case ActionType.Delegated => 2
                    case ActionType.Scheduled => 3
                }
                
                direction match {
                    case SWT.DOWN => order(action1.actionType) < order(action2.actionType)
                    case SWT.UP   => order(action1.actionType) > order(action2.actionType)
                }
            }

            sortedItem.foreach { x =>
                val item = new TableItem(table, SWT.NONE)
                for (i <- 0 until table.getColumnCount) {
                    item.setText(i, x.getText(i))
                    item.setImage(i, x.getImage(i))
                    item.setData(x.getData)
                    item.setChecked(x.getChecked)
                }
            }

            sortedItem.foreach {_.dispose()}
        }

    }

    def setAllActionSortListener()
    {
        allActionTable.getColumn(1).setSorting(TableSorting.sortByType _)

        for (columnIndex <- 2 to 8) {
            allActionTable.getColumn(columnIndex).setSorting(columnIndex)
        }
    }

    def setDoASAPSortListener()
    {
        for (columnIndex <- 2 to 6) {
            doASAPTable.getColumn(columnIndex).setSorting(columnIndex)
        }
    }

    def setDelegatedSortListener()
    {
        for (columnIndex <- 2 to 8) {
            delegatedTable.getColumn(columnIndex).setSorting(columnIndex)
        }
    }

    def setScheduledSortListener()
    {
        for (columnIndex <- 2 to 7) {
            scheduledTable.getColumn(columnIndex).setSorting(columnIndex)
        }
    }

    def setDoneSortListener()
    {
        doneTable.getColumn(1).setSorting(TableSorting.sortByType _)

        for (columnIndex <- 2 to 6) {
            doneTable.getColumn(columnIndex).setSorting(columnIndex)
        }

    }

    def setListener ()
    {
        def actionFromItem(item: TableItem) = {
            val actionID = item.getData.asInstanceOf[Int]
            Controller.gtd.Action.get(actionID)
        }

        def setDetailMessage(table: Table)
        {
            table.addSelectionListener{e: SelectionEvent =>
                table.getSelection.foreach {tableItem =>

                    val action = actionFromItem(tableItem)
                    val message = action.actionType match {
                        case ActionType.DoASAP    => Maid.ActionTabMessage.doASAPDetail(action)
                        case ActionType.Delegated => Maid.ActionTabMessage.delegatedDetail(action)
                        case ActionType.Scheduled => Maid.ActionTabMessage.scheduledDetail(action)
                        case _ => Nil
                    }

                    DialogWindow.setMessages(message)
                }
            }
        }

        def setDoubleClick(table: Table)
        {
            table.addMouseListener(new MouseAdapter() {
                override def mouseDoubleClick(e: MouseEvent)
                {
                    table.getSelection.foreach { tableItem =>
                        val action = actionFromItem(tableItem)
                        val dialog = new EditActionDialog(action)
                    }
                }
            })
        }

        def setCheckDone (table: Table)
        {
            table.addSelectionListener {e: SelectionEvent =>
                val isCheckedEvent = e.detail == SWT.CHECK
            
                if (isCheckedEvent) {
                    val actionID = table.getSelection.head.getData.asInstanceOf[Int]
                    val action = Controller.gtd.Action.get(actionID)
                    val doneAction = action.markAsDone()
    
                    allActionTable.removeByData(actionID)
    
                    action.actionType match {
                        case ActionType.DoASAP => doASAPTable.removeByData(actionID)
                        case ActionType.Delegated => delegatedTable.removeByData(actionID)
                        case ActionType.Scheduled => scheduledTable.removeByData(actionID)
                    }
    
                    addToDone(doneAction)
                    MainWindow.projectTab.setActionIsDone(doneAction, true)
                    DialogWindow.setMessages(Maid.ActionTabMessage.markAsDone)
                }
            }
        }

        setDoubleClick (allActionTable)
        setDoubleClick (doASAPTable)
        setDoubleClick (delegatedTable)
        setDoubleClick (scheduledTable)
        setDoubleClick (doneTable)

        setCheckDone (allActionTable)
        setCheckDone (doASAPTable)
        setCheckDone (delegatedTable)
        setCheckDone (scheduledTable)

        setDetailMessage (allActionTable)
        setDetailMessage (doASAPTable)
        setDetailMessage (delegatedTable)
        setDetailMessage (scheduledTable)
        setDetailMessage (doneTable)

        setAllActionSortListener()
        setDoASAPSortListener()
        setDelegatedSortListener()
        setScheduledSortListener()
        setDoneSortListener()

        removeButton.addSelectionListener { e: SelectionEvent =>
            
            val table = tabToTable(actionTabFolder.getSelectionIndex)

            table.getSelection.foreach { item =>
                val action = actionFromItem(item)
                removeAction(action)
                action.delete()

                DialogWindow.setMessages(Maid.ActionTabMessage.deleted)
            }
            
        }

        reprocessButton.addSelectionListener {e: SelectionEvent =>
            val table = tabToTable(actionTabFolder.getSelectionIndex)

            table.getSelection.foreach { item =>
                val action = actionFromItem(item)
                val stuff = action.reprocess()

                removeAction(action)

                MainWindow.inboxTab.addToStuffTable(stuff)
                MainWindow.inboxTab.adjustColumnWidth()
                MainWindow.projectTab.removeAction(action)

                DialogWindow.setMessages(Maid.ActionTabMessage.reprocess(stuff))
            }
        }

        doneTable.addSelectionListener { e: SelectionEvent =>
            if (e.detail == SWT.CHECK) {
                val item = doneTable.getSelection.head
                val actionID = item.getData.asInstanceOf[Int]
                val action = Controller.gtd.Action.get(actionID)
                val updatedAction = action.copy(isDone = false, doneTimestamp = None)

                action.update(updatedAction)

                addAction(updatedAction)
                MainWindow.projectTab.updateAction(updatedAction)
                item.dispose()

                DialogWindow.setMessages(Maid.ActionTabMessage.unmarkDone)
            }
        }

        searchText.addSelectionListener (new SelectionAdapter() {
            override def widgetDefaultSelected(e: SelectionEvent)
            {
                val target = searchText.getText
                val table = tabToTable(actionTabFolder.getSelectionIndex)

                if (e.detail != SWT.CANCEL && target != "") {
                    val found = table.findNext(target)
                    DialogWindow.setMessages(Maid.ActionTabMessage.search(found))
                }
            }
        })

        mailToButton.addSelectionListener{e: SelectionEvent =>
            def isMailableAction(action: Action) = {
                action.actionType == ActionType.Delegated &&
                action.delegatedInfo.get.email != None
            }

            val table   = tabToTable(actionTabFolder.getSelectionIndex)
            val actions = table.getSelection.map(actionFromItem).filter(isMailableAction)

            actions.foreach { action =>
                val delegatedInfo = action.delegatedInfo.get
                DialogWindow.setMessages(Maid.ActionTabMessage.mailDelegate)

                Preference.get("EMailClient") match {
                    case None => 
                        DialogWindow.setMessages(Maid.ActionTabMessage.noMailClient)
                    case Some(command) =>
                        try {
                            command.format(delegatedInfo.email.get).run()
                        } catch {
                            case _ => DialogWindow.setMessages(Maid.ActionTabMessage.mailClientError)
                        }
                }

            }

            if (actions.length == 0) {
                DialogWindow.setMessages(Maid.ActionTabMessage.noDelegatedMail)
            }
        }


        followUpButton.addSelectionListener{e: SelectionEvent =>
            val table   = tabToTable(actionTabFolder.getSelectionIndex)
            val actions = table.getSelection.map(actionFromItem).
                                filter(_.actionType == ActionType.Delegated)

            actions.foreach { action =>
                val delegatedInfo = action.delegatedInfo.get
                val newFollowUp = timestampFrom(new Date)

                val newAction = action.convertToDelegated(
                    delegatedInfo.name, delegatedInfo.email, Option(newFollowUp)
                )

                updateRow(action, newAction)
                DialogWindow.setMessages(Maid.ActionTabMessage.followUp)
            }

            if (actions.length == 0) {
                DialogWindow.setMessages(Maid.ActionTabMessage.notDelegated)
            }
        }
    }

    def removeAction (action: Action)
    {
        action.isDone match {
            case true => doneTable.removeByData (action.actionID)
            case false =>
                allActionTable.removeByData (action.actionID)
                action.actionType match {
                    case ActionType.DoASAP => doASAPTable.removeByData(action.actionID)
                    case ActionType.Delegated => delegatedTable.removeByData(action.actionID)
                    case ActionType.Scheduled => scheduledTable.removeByData(action.actionID)
                    case _ => 
                }
        }
    }

    def updateDoASAP (action: Action)
    {
        val createDate = dateFormatter.format(action.createTimestamp)
        val topic = action.topics.map(_.name).mkString(",")
        val project = action.projects.map(_.title).mkString(",")
        val context = action.contexts.map(_.name).mkString(",")

        val item = doASAPTable.getItems.filter (_.getData == action.actionID)
        item.foreach { row =>
            row.setText(Array("", "", createDate, topic, project, context, action.title))
        }
    }

    def addToDoASAP (action: Action)
    {
        val createDate = dateFormatter.format(action.createTimestamp)
        val topic = action.topics.map(_.name).mkString(",")
        val project = action.projects.map(_.title).mkString(",")
        val context = action.contexts.map(_.name).mkString(",")

        val item = new TableItem(doASAPTable, SWT.NONE)

        item.setData (action.actionID)
        item.setImage(1, doASAPIcon)
        item.setText(Array("", "", createDate, topic, project, context, action.title))
    }

    def updateDelegated (action: Action)
    {
        val createDate = dateFormatter.format(action.createTimestamp)
        val topic = action.topics.map(_.name).mkString(",")
        val project = action.projects.map(_.title).mkString(",")
        val delegatedTo = action.delegatedInfo.map(_.name).getOrElse("")
        val context = action.contexts.map(_.name).mkString(",")
        val followUp = action.delegatedInfo.get.follow match {
            case None => "Not Yet"
            case Some(date) => dateFormatter.format(date)
        }

        val item = delegatedTable.getItems.filter (_.getData == action.actionID)
        item.foreach { row =>
            row.setText(Array("", "", createDate, topic, project, context, action.title, delegatedTo, followUp))
        }
    }

    def addToDelegated (action: Action)
    {
        val createDate = dateFormatter.format(action.createTimestamp)
        val topic = action.topics.map(_.name).mkString(",")
        val project = action.projects.map(_.title).mkString(",")
        val delegatedTo = action.delegatedInfo.map(_.name).getOrElse("")
        val context = action.contexts.map(_.name).mkString(",")
        val followUp = action.delegatedInfo.get.follow match {
            case None => "Not Yet"
            case Some(date) => dateFormatter.format(date)
        }

        val item = new TableItem(delegatedTable, SWT.NONE)

        item.setData (action.actionID)
        item.setImage(1, delegatedIcon)
        item.setText(Array("", "", createDate, topic, project, context, action.title, delegatedTo, followUp))
    }

    def updateScheduled (action: Action)
    {
        val createDate = dateFormatter.format(action.createTimestamp)
        val topic = action.topics.map(_.name).mkString(",")
        val project = action.projects.map(_.title).mkString(",")
        val scheduledDate = action.scheduledInfo.map(info => dateFormatter.format(info.startTime)).getOrElse("")
        val context = action.contexts.map(_.name).mkString(",")

        val item = scheduledTable.getItems.filter (_.getData == action.actionID)
        item.foreach { row =>
            row.setText(
                Array("", "", createDate, topic, project, context, action.title, scheduledDate)
            )
        }
    }

    def addToScheduled (action: Action)
    {
        val createDate = dateFormatter.format(action.createTimestamp)
        val topic = action.topics.map(_.name).mkString(",")
        val project = action.projects.map(_.title).mkString(",")
        val scheduledDate = action.scheduledInfo.map(info => dateFormatter.format(info.startTime)).getOrElse("")
        val context = action.contexts.map(_.name).mkString(",")

        val item = new TableItem(scheduledTable, SWT.NONE)

        item.setData (action.actionID)
        item.setImage(1, scheduledIcon)
        item.setText(Array("", "", createDate, topic, project, context, action.title, scheduledDate))

    }

    def updateAllActionTable (action: Action)
    {
        val createDate = dateFormatter.format(action.createTimestamp)
        val topic = action.topics.map(_.name).mkString(",")
        val project = action.projects.map(_.title).mkString(",")
        val context = action.contexts.map(_.name).mkString(",")
        val delegatedTo = action.delegatedInfo.map(_.name).getOrElse("")
        val scheduledDate = action.scheduledInfo.map(info => dateFormatter.format(info.startTime)).getOrElse("")

        val actionIcon = action.actionType match {
            case ActionType.DoASAP => doASAPIcon
            case ActionType.Delegated => delegatedIcon
            case ActionType.Scheduled => scheduledIcon
            case _ => doASAPIcon
        }

        val item = allActionTable.getItems.filter (_.getData == action.actionID)
        item.foreach { row =>
            row.setImage(1, actionIcon)
            row.setText(
                Array("", "", createDate, topic, project, context, 
                      action.title, delegatedTo, scheduledDate)
            )

        }
    }

    def addToAll (action: Action)
    {
        val createDate = dateFormatter.format(action.createTimestamp)
        val topic = action.topics.map(_.name).mkString(",")
        val project = action.projects.map(_.title).mkString(",")
        val context = action.contexts.map(_.name).mkString(",")
        val delegatedTo = action.delegatedInfo.map(_.name).getOrElse("")
        val scheduledDate = action.scheduledInfo.map(info => dateFormatter.format(info.startTime)).getOrElse("")

        val actionIcon = action.actionType match {
            case ActionType.DoASAP => doASAPIcon
            case ActionType.Delegated => delegatedIcon
            case ActionType.Scheduled => scheduledIcon
            case _ => doASAPIcon
        }
        val item = new TableItem(allActionTable, SWT.NONE)

        item.setData (action.actionID)
        item.setImage(1, actionIcon)
        item.setText(Array("", "", createDate, topic, project, context, action.title, delegatedTo, scheduledDate))
    }

    def addToDone (action: Action)
    {
        val createDate = dateFormatter.format(action.createTimestamp)
        val topic = action.topics.map(_.name).mkString(",")
        val project = action.projects.map(_.title).mkString(",")
        val context = action.contexts.map(_.name).mkString(",")
        val delegatedTo = action.delegatedInfo.map(_.name).getOrElse("")
        val scheduledDate = action.scheduledInfo.map(info => dateFormatter.format(info.startTime)).getOrElse("")

        val actionIcon = action.actionType match {
            case ActionType.DoASAP => doASAPIcon
            case ActionType.Delegated => delegatedIcon
            case ActionType.Scheduled => scheduledIcon
            case _ => doASAPIcon
        }
        val item = new TableItem(doneTable, SWT.NONE)

        item.setData (action.actionID)
        item.setImage(1, actionIcon)
        item.setText(Array("", "", createDate, topic, project, context, action.title, delegatedTo, scheduledDate))
        item.setChecked(action.isDone)
    }

    def addAction (action: Action, addToAllActionTab: Boolean = true)
    {
        action.isDone match {
            case false =>
                if (addToAllActionTab) { addToAll (action) }
                action.actionType match {
                    case ActionType.DoASAP => addToDoASAP(action)
                    case ActionType.Delegated => addToDelegated(action)
                    case ActionType.Scheduled => addToScheduled(action)
                    case _ => 
                }
            case true =>
                addToDone (action)
        }
    }

    def adjustColumnWidth ()
    {
        def adjustTable(table: Table) {
            table.getColumns.foreach { column =>
                column.pack() 
                column.setWidth(column.getWidth + 5)
            }

            table.getColumns()(1).setResizable(false)
            table.getColumns()(1).setWidth(30)
        }

        adjustTable(allActionTable)
        adjustTable(doASAPTable)
        adjustTable(delegatedTable)
        adjustTable(scheduledTable)
        adjustTable(doneTable)
    }

    setupUILayout ()
    setListener ()

    Controller.gtd.Action.list.foreach (addAction(_, true))
    adjustColumnWidth ()

}

