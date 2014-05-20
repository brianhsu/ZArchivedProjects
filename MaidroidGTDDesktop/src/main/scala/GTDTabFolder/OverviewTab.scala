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
import java.util.Date

import org.maidroid.gtd.controller._
import org.maidroid.gtd.model._
import Controller.gtd.Implicit._

import I18N.TR.{tr => _t}

class OverviewTab(tabFolder: TabFolder) extends GTDTabItem(tabFolder, _t("Overview"), "/icons/overview.png") with SimpleSWT
{
    val fillBoth = new GridData(SWT.FILL, SWT.FILL, true, true)
    val fillWidth = new GridData(SWT.FILL, SWT.NONE, true, false)
    val display = Display.getDefault
    
    val inboxIcon = new Image(display, getClass.getResourceAsStream("/icons/inbox.png"))
    val processIcon = new Image(display, getClass.getResourceAsStream("/icons/processIcon.png"))
    val actionIcon = new Image(display, getClass.getResourceAsStream("/icons/action.png"))

    val maybeIcon = new Image(display, getClass.getResourceAsStream("/icons/maybeBig.png"))
    val referenceIcon = new Image(display, getClass.getResourceAsStream("/icons/referenceIcon.png"))
    val projectIcon = new Image(display, getClass.getResourceAsStream("/icons/projectTab.png"))

    val doASAPIcon = new Image(display, getClass.getResourceAsStream("/icons/doASAPBig.png"))
    val delegatedIcon = new Image(display, getClass.getResourceAsStream("/icons/delegatedBig.png"))
    val scheduledIcon = new Image(display, getClass.getResourceAsStream("/icons/scheduledBig.png"))
    val scheduledIcon22 = new Image(display, getClass.getResourceAsStream("/icons/scheduled.png"))

    lazy val inboxButton = controls('InboxButton).asInstanceOf[Button]
    lazy val processButton = controls('ProcessButton).asInstanceOf[Button]
    lazy val actionButton = controls('ActionButton).asInstanceOf[Button]
    lazy val maybeButton = controls('MaybeButton).asInstanceOf[Button]
    lazy val referenceButton = controls('ReferenceButton).asInstanceOf[Button]
    lazy val projectButton = controls('ProjectButton).asInstanceOf[Button]
    lazy val doASAPButton = controls('DoASAPButton).asInstanceOf[Button]
    lazy val delegatedButton = controls('DelegatedButton).asInstanceOf[Button]
    lazy val scheduledButton = controls('ScheduledButton).asInstanceOf[Button]

    lazy val actionProgress = controls('ActionProgress).asInstanceOf[ProgressBar]
    lazy val projectProgress = controls('ProjectProgress).asInstanceOf[ProgressBar]
    lazy val stuffSummary = controls('StuffSummary).asInstanceOf[Label]
    lazy val actionSummary = controls('ActionSummary).asInstanceOf[Label]
    lazy val projectSummary = controls('ProjectSummary).asInstanceOf[Label]

    lazy val scheduledTable = controls('ScheduledTable).asInstanceOf[Table]
    lazy val rangeCombo = controls('RangeCombo).asInstanceOf[Combo]

    val dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm")

    val collectComposite = composite(new GridLayout, layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false))(
        button(
            _t("Collect all stuff into inbox"), 
            init = {_.setImage(inboxIcon)}, 
            layoutData = new GridData(SWT.FILL, SWT.FILL, true, true),
            id = 'InboxButton
        )
    )_

    val processComposite = composite(new GridLayout(6, false), layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false))(
        button(
            _t("Action"), 
            layoutData = new GridData(SWT.NONE, SWT.FILL, false, true), 
            id = 'ActionButton,
            init = {_.setImage(actionIcon)}
        ),
        label ("<---"),
        button(
            _t("What is it? Is it actionable?"),
            layoutData = new GridData(SWT.FILL, SWT.FILL, true, true),
            id = 'ProcessButton,
            init = {_.setImage(processIcon)}
        ),
        label ("--->"),
        button(
            _t("Maybe"), 
            layoutData = new GridData(SWT.NONE, SWT.FILL, false, true), 
            id = 'MaybeButton,
            init = {_.setImage(maybeIcon)}
        ),
        button(
            _t("Reference"), 
            layoutData = new GridData(SWT.NONE, SWT.FILL, false, true),
            id = 'ReferenceButton,
            init = {_.setImage(referenceIcon)}
        ) 
    )_

    val reviewComposite = composite(new GridLayout(4, false), layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false))(
        button(
            _t("Project"), layoutData = fillBoth, 
            id = 'ProjectButton, init = {_.setImage(projectIcon)}
        ),
        button(
            _t("Do ASAP"), layoutData = fillBoth, 
            id = 'DoASAPButton, init = {_.setImage(doASAPIcon)}
        ),
        button(
            _t("Delegated"), layoutData = fillBoth, 
            id = 'DelegatedButton, init = {_.setImage(delegatedIcon)}
        ),
        button(
            _t("Scheduled"), layoutData = fillBoth, 
            id = 'ScheduledButton, init = {_.setImage(scheduledIcon)}
        )
    )_


    def setupUILayout ()
    {
        val fillOverviewGroup = new GridData(SWT.FILL, SWT.NONE, true, false)

        tabItem.addComposite(composite(new GridLayout)(
            group(_t("GTD Process"), layout = new GridLayout(2, false), layoutData = fillWidth)(
                label(_t("1. Collect"), SWT.CENTER), collectComposite,
                label(_t("2. Process"), SWT.CENTER), processComposite,
                label(_t("3. Review"), SWT.CENTER), reviewComposite
            ),

            composite(layout = new GridLayout(2, true), layoutData = fillBoth)(
                group(_t("Overview"), layout = new GridLayout(3,false), layoutData = fillBoth)(
                    label(_t("Stuff:")), 
                    label("", id = 'StuffSummary, layoutData = new GridData(SWT.NONE, SWT.NONE, false, false, 2, 1)),
                    label(_t("Actions:")), 
                    progressBar(id = 'ActionProgress, layoutData = fillOverviewGroup), 
                    label("", id = 'ActionSummary),
                    label(_t("Projects:")), 
                    progressBar(id = 'ProjectProgress, layoutData = fillOverviewGroup), 
                    label("", id = 'ProjectSummary)
                ),
                group(_t("Recent Date"), layout = new GridLayout, layoutData = fillBoth)(
                
                    combo(
                        style = SWT.READ_ONLY, 
                        layoutData = new GridData(SWT.FILL, SWT.NONE, true, false),
                        id = 'RangeCombo
                    ),
                    table(
                        column("")_ :: column(_t("Title"))_ :: column(_t("Scheduled"))_ :: column(_t("Location"))_ :: Nil,
                        style = SWT.FULL_SELECTION,
                        layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1),
                        id = 'ScheduledTable
                    )
                )

            )
        ))

        adjustColumnWidth()

    }

    def adjustColumnWidth()
    {
        val columns = scheduledTable.getColumns

        columns.foreach { column =>
            column.pack() 
            column.setWidth(column.getWidth + 5)
        }

        columns(0).setResizable(false)
        columns(0).setWidth(30)

    }

    def setupListener()
    {
        inboxButton.addSelectionListener{e: SelectionEvent =>
            MainWindow.tabFolder.setSelection(1)
            MainWindow.inboxTab.reprocessTickled()
        }

        processButton.addSelectionListener{e: SelectionEvent =>
            MainWindow.tabFolder.setSelection(1)
            MainWindow.inboxTab.startProcess()
        }

        actionButton.addSelectionListener{e: SelectionEvent =>
            MainWindow.tabFolder.setSelection(2)
        }

        maybeButton.addSelectionListener{e: SelectionEvent =>
            MainWindow.tabFolder.setSelection(3)
        }

        referenceButton.addSelectionListener{e: SelectionEvent =>
            MainWindow.tabFolder.setSelection(4)
        }

        projectButton.addSelectionListener{e: SelectionEvent =>
            MainWindow.tabFolder.setSelection(5)
        }

        doASAPButton.addSelectionListener{e: SelectionEvent =>
            MainWindow.tabFolder.setSelection(2)
            MainWindow.actionTab.actionTabFolder.setSelection(1)
        }

        delegatedButton.addSelectionListener{e: SelectionEvent =>
            MainWindow.tabFolder.setSelection(2)
            MainWindow.actionTab.actionTabFolder.setSelection(2)
        }

        scheduledButton.addSelectionListener{e: SelectionEvent =>
            MainWindow.tabFolder.setSelection(2)
            MainWindow.actionTab.actionTabFolder.setSelection(3)
        }

        rangeCombo.addSelectionListener{e: SelectionEvent =>
            populateScheduledEvent()
        }

        scheduledTable.addSelectionListener {e: SelectionEvent =>
            scheduledTable.getSelection.foreach { tableItem =>
                val actionID = tableItem.getData.asInstanceOf[Int]
                val action = Controller.gtd.Action.get(actionID)

                DialogWindow.setMessages(Maid.OverviewTabMessage.scheduledAction(action))
            }
        }

    }

    def updateStatus()
    {
        val totalStuff = Controller.gtd.Stuff.list.length
        val totalAction = Controller.gtd.Action.list.length
        val totalProject = Controller.gtd.Project.list.length

        val doneAction = Controller.gtd.Action.list.filter(_.isDone == true).length
        val doneProject = Controller.gtd.Project.list.filter(_.isDone == true).length

        stuffSummary.setText(totalStuff.toString)
        actionSummary.setText("%s/%s" format(doneAction, totalAction))
        projectSummary.setText("%s/%s" format(doneProject, totalProject))

        actionProgress.setMaximum(totalAction)
        actionProgress.setSelection(doneAction)

        projectProgress.setMaximum(totalProject)
        projectProgress.setSelection(doneProject)
    }

    def addScheduledAction(action: Action)
    {
        val tableItem = new TableItem(scheduledTable, SWT.NONE)
        val startTime = dateFormatter.format(action.scheduledInfo.get.startTime)
        val location = action.scheduledInfo.get.location.getOrElse("")

        tableItem.setText(Array("", action.title, startTime, location))
        tableItem.setImage(scheduledIcon22)
        tableItem.setData(action.actionID)
    }

    def populateScheduledEvent()
    {
        def isInRange(action: Action, minTimestamp: Date, maxTimestamp: Date) = {
            action.actionType == ActionType.Scheduled &&
            action.scheduledInfo.get.startTime.after(minTimestamp) &&
            action.scheduledInfo.get.startTime.before(maxTimestamp)
        }

        val calendar = Calendar.getInstance
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        val currentTime = calendar.getTime

        rangeCombo.getSelectionIndex match {
            case 0 => calendar.add(Calendar.DAY_OF_MONTH, 7)
            case 1 => calendar.add(Calendar.DAY_OF_MONTH, 14)
            case 2 => calendar.add(Calendar.DAY_OF_MONTH, 21)
            case 3 => calendar.add(Calendar.DAY_OF_MONTH, 31)
        }

        val maxTime = calendar.getTime
        val actions = Controller.gtd.Action.list.filter(isInRange(_, currentTime, maxTime))

        scheduledTable.getItems.foreach{_.dispose()}
        actions.foreach {addScheduledAction}

        adjustColumnWidth()

    }

    def init ()
    {
        rangeCombo.add(_t("1 week"))
        rangeCombo.add(_t("2 weeks"))
        rangeCombo.add(_t("3 weeks"))
        rangeCombo.add(_t("1 month"))
        rangeCombo.select(0)

        populateScheduledEvent()
        updateStatus()
    }

    setupUILayout()
    setupListener()
    init ()
}
