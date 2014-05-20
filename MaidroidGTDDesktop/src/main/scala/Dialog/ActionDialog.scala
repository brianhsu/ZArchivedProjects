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

import org.eclipse.swt.custom._

import org.eclipse.swt.events._
import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.SWT

import org.maidroid.gtd.model._

import Controller.gtd.Implicit._

import java.util.Calendar

import I18N.TR.{tr => _t}

class EditActionDialog(action: Action) extends SimpleSWT
{
    val shell = new Shell(MainWindow.shell, SWT.DIALOG_TRIM|SWT.SHEET)

    shell.setText(_t("Edit Action"))

    lazy val titleText = controls('TitleText).asInstanceOf[Text]
    lazy val descriptionText = controls('DescriptionText).asInstanceOf[Text]
    lazy val outcomeText = controls('OutcomeText).asInstanceOf[Text]

    lazy val delegatedText = controls('DelegatedTo).asInstanceOf[Text]
    lazy val delegatedEMail = controls('DelegatedEMail).asInstanceOf[Text]
    lazy val followUpButton = controls('FollowUp).asInstanceOf[Button]
    lazy val followDate = controls('FollowDate).asInstanceOf[DateTime]
    lazy val followTime = controls('FollowTime).asInstanceOf[DateTime]

    lazy val editButton = controls('EditButton).asInstanceOf[Button]
    lazy val cancelButton = controls('CancelButton).asInstanceOf[Button]

    lazy val topicCombo = new TopicCombo(controls('TopicCombo).asInstanceOf[Combo])
    lazy val contextCombo = new ContextCombo(controls('ContextCombo).asInstanceOf[Combo])
    lazy val projectCombo = new ProjectCombo(controls('ProjectCombo).asInstanceOf[Combo])

    lazy val scheduledDate = controls('ScheduledDate).asInstanceOf[DateTime]
    lazy val scheduledTime = controls('ScheduledTime).asInstanceOf[DateTime]
    lazy val duration = controls('DurationSpinner).asInstanceOf[Spinner]
    lazy val location = controls('LocationText).asInstanceOf[Text]

    lazy val actionTypeCombo = controls('ActionTypeCombo).asInstanceOf[Combo]
    lazy val stackLayout = new StackLayout
    lazy val stackComposite = controls('StackComposite).asInstanceOf[Composite]

    def setupUILayout ()
    {
        val gridLayout = new GridLayout(2, false)
        val rowLayout = new RowLayout
        rowLayout.justify = true
        rowLayout.marginRight = 10
        rowLayout.pack = false

        val fillWidth = new GridData(SWT.FILL, SWT.NONE, true, false)
        val fillBoth  = new GridData(SWT.FILL, SWT.FILL, true, true)
        val rowLayoutData = new GridData(SWT.END, SWT.NONE, false, false, 2, 1)

        val stackFillWidth = new GridData(SWT.FILL, SWT.NONE, true, false)
        val stackFillWidth2 = new GridData(SWT.FILL, SWT.NONE, true, false, 2, 1)

        shell.addComposite(composite(gridLayout) (
            label(_t("Ttile")), text(_t("Required"), layoutData = fillWidth, id = 'TitleText),
            label(_t("Topic")), combo(layoutData = fillWidth, id = 'TopicCombo),
            label(_t("Context")), combo(layoutData = fillWidth, id = 'ContextCombo),

            label(_t("Project")), combo(layoutData = fillWidth, id = 'ProjectCombo),

            label(_t("Description")), 
            text(
                _t("Description"), 
                style = SWT.MULTI|SWT.BORDER|SWT.V_SCROLL|SWT.WRAP,
                layoutData = fillBoth, 
                id = 'DescriptionText
            ),
            label(_t("Type")), combo(style = SWT.DROP_DOWN|SWT.READ_ONLY, id = 'ActionTypeCombo),
            label(_t("Successful Outcome"), layoutData = new GridData(SWT.FILL, SWT.NONE, true, false, 2, 1)),

            text("", layoutData = new GridData(SWT.FILL, SWT.NONE, true, false, 2, 1), id = 'OutcomeText),

            composite(layout = stackLayout, id = 'StackComposite,
                      layoutData = new GridData(SWT.FILL, SWT.NONE, true, false, 2, 1))
            (
                composite(layout = new GridLayout(2, false), id = 'DoASAPInfo)(
                ),

                composite(layout = new GridLayout(3, false), id = 'DelegatedInfo)(
                    label(_t("Deleaged To")),
                    text(_t("Required"), id = 'DelegatedTo, layoutData = stackFillWidth2),
                    label(_t("EMail")),
                    text("", layoutData = stackFillWidth2, id = 'DelegatedEMail),
                    button(_t("Follow Up"), style = SWT.CHECK, id = 'FollowUp),
                    dateTime(style = SWT.DATE|SWT.DROP_DOWN, id = 'FollowDate, layoutData = stackFillWidth, init = {_.setEnabled(false)}),
                    dateTime(style = SWT.TIME|SWT.SHORT, id = 'FollowTime, layoutData = stackFillWidth, init = {_.setEnabled(false)})
                ),
                composite(layout = new GridLayout(3, false), id = 'ScheduledInfo)(
                    label(_t("Date/Time")), 
                    dateTime(
                        style = SWT.DATE|SWT.DROP_DOWN,
                        id = 'ScheduledDate, 
                        layoutData = stackFillWidth
                    ), 
                    dateTime(
                        style = SWT.TIME|SWT.SHORT, 
                        id = 'ScheduledTime,
                        layoutData = stackFillWidth
                    ),
                    label(_t("Duration")),
                    spinner(
                        id = 'DurationSpinner, 
                        layoutData = new GridData(SWT.FILL, SWT.NONE, false, false)
                    ),
                    label(_t("minutes")),
                    label(_t("Location")), 
                    text("",   
                        id = 'LocationText,
                        layoutData = new GridData(SWT.FILL, SWT.NONE, true, true, 2, 1)
                    )
                )
            ),

            composite(rowLayout, rowLayoutData) (
                button(_t("Cancel"), icon = "/icons/cancel.png", id = 'CancelButton), 
                button(_t("Edit"), icon = "/icons/finish.png", id = 'EditButton, init = {_.setEnabled(false)})
            )
        ))

        topicCombo.init()
        projectCombo.init()
        contextCombo.init()

        actionTypeCombo.add (_t("Do ASAP"))
        actionTypeCombo.add (_t("Delegated"))
        actionTypeCombo.add (_t("Scheduled"))
    }

    def setupUIListener()
    {
        titleText.addModifyListener {e: ModifyEvent => setEditButtonState() }
        delegatedText.addModifyListener {e: ModifyEvent => setEditButtonState() }

        followUpButton.addSelectionListener{e: SelectionEvent =>
            followDate.setEnabled(followUpButton.getSelection)
            followTime.setEnabled(followUpButton.getSelection)
        }

        cancelButton.addSelectionListener { e: SelectionEvent =>
            shell.dispose()
            DialogWindow.setMessages(Maid.ActionDialogMessage.cancel)
        }

        actionTypeCombo.addSelectionListener {e: SelectionEvent =>
            actionTypeCombo.getSelectionIndex match {
                case 0 => stackLayout.topControl = controls('DoASAPInfo).asInstanceOf[Composite]
                case 1 => stackLayout.topControl = controls('DelegatedInfo).asInstanceOf[Composite]
                case 2 => stackLayout.topControl = controls('ScheduledInfo).asInstanceOf[Composite]
            }

            stackComposite.layout()
            setEditButtonState()
        }

        editButton.addSelectionListener { e: SelectionEvent =>

            val newAction = actionTypeCombo.getSelectionIndex match {
                case 0 => action.convertToDoASAP()
                case 1 =>
                    val followTimeStamp = followUpButton.getSelection match {
                        case false => None
                        case true =>
                            val calendar = Calendar.getInstance
                            calendar.clear()
                            calendar.set(
                                followDate.getYear, followDate.getMonth, followDate.getDay,
                                followTime.getHours, followTime.getMinutes
                            )
                            Some(timestampFrom(calendar.getTime))
                    }

                    action.convertToDelegated(
                        delegatedText.getText, 
                        delegatedEMail.getOptionText,
                        followTimeStamp
                    )

                case 2 =>
                    val calendar = Calendar.getInstance
                    calendar.clear()
                    calendar.set(
                        scheduledDate.getYear, scheduledDate.getMonth, scheduledDate.getDay,
                        scheduledTime.getHours, scheduledTime.getMinutes
                    )
                    val duration = this.duration.getSelection match {
                        case 0       => None
                        case minutes => Some(minutes)
                    }
                    action.convertToScheduled(
                        timestampFrom(calendar.getTime), duration, location.getOptionText
                    )

            }

            val updatedAction = newAction.copy (
                title = titleText.getText,
                description = descriptionText.getOptionText,
                successfulOutcome = outcomeText.getOptionText
            )

            newAction.update(updatedAction)
            newAction.topics.foreach { action.removeTopic }
            newAction.contexts.foreach { action.removeContext }
            newAction.projects.foreach { action.removeProject }

            topicCombo.getTopic.foreach { updatedAction.addToTopic }
            contextCombo.getContext.foreach { updatedAction.addToContext }
            projectCombo.getProject.foreach { updatedAction.addToProject }

            MainWindow.actionTab.updateRow(action, updatedAction)
            MainWindow.projectTab.updateAction(updatedAction)

            shell.dispose()

            DialogWindow.setMessages(Maid.ActionDialogMessage.updated)
        }
    }

    def setEditButtonState() = {
        val shouldEnable = actionTypeCombo.getSelectionIndex match {
            case 1 => titleText.getText.length > 0 && delegatedText.getText.length > 0
            case _ => titleText.getText.length > 0
        }

        editButton.setEnabled(shouldEnable)
    }

    def init () {
        val comboID = action.actionType match {
            case ActionType.DoASAP => 0
            case ActionType.Delegated => 1
            case ActionType.Scheduled => 2
            case _ => 0
        }

        actionTypeCombo.select(comboID)
        titleText.setText (action.title)

        action.topics.foreach { topicCombo.setTopic }
        action.contexts.foreach { contextCombo.setContext }
        action.projects.foreach { projectCombo.setProject }

        action.description.foreach { descriptionText.setText }
        action.successfulOutcome.foreach { outcomeText.setText }

        action.delegatedInfo.foreach { info =>
            delegatedText.setText(info.name)
            info.email.foreach { delegatedEMail.setText }
            info.follow.foreach { timestamp =>
                followUpButton.setSelection(true)
                followDate.setEnabled(true)
                followTime.setEnabled(true)

                val calendar = Calendar.getInstance
                calendar.clear
                calendar.setTime(timestamp)
    
                followDate.setYear(calendar.get(Calendar.YEAR))
                followDate.setMonth(calendar.get(Calendar.MONTH))
                followDate.setDay(calendar.get(Calendar.DATE))
                followTime.setHours(calendar.get(Calendar.HOUR_OF_DAY))
                followTime.setMinutes(calendar.get(Calendar.MINUTE))

            }
        }

        action.scheduledInfo.foreach { info =>
            val scheduledDateTime = info.startTime
            val calendar = Calendar.getInstance
            calendar.clear
            calendar.setTime(scheduledDateTime)

            scheduledDate.setYear(calendar.get(Calendar.YEAR))
            scheduledDate.setMonth(calendar.get(Calendar.MONTH))
            scheduledDate.setDay(calendar.get(Calendar.DATE))
            scheduledTime.setHours(calendar.get(Calendar.HOUR_OF_DAY))
            scheduledTime.setMinutes(calendar.get(Calendar.MINUTE))

            info.duration.foreach { duration.setSelection }
            info.location.foreach { location.setText }

        }

        action.actionType match {
            case ActionType.DoASAP =>
                stackLayout.topControl = controls('DoASAPInfo).asInstanceOf[Composite]

            case ActionType.Delegated =>
                stackLayout.topControl = controls('DelegatedInfo).asInstanceOf[Composite]

            case ActionType.Scheduled =>
                stackLayout.topControl = controls('ScheduledInfo).asInstanceOf[Composite]

            case _ =>
        }

        stackComposite.layout()
        setEditButtonState()

        DialogWindow.setMessages(Maid.ActionDialogMessage.opened)
    }

    def run () {

        setupUILayout()
        setupUIListener()

        shell.setSize(400, 500)
        shell.open()

        init ()

        val display = Display.getDefault

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    run()
}
