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

import org.scalaquery.session.Database
import org.maidroid.gtd.controller._
import org.maidroid.gtd.model._

import java.io.File
import java.util.Calendar

import Controller.gtd.Implicit._

import I18N.TR.{tr => _t}

trait WizardPage {
    def wizardPage: Composite
    def initPage(): Unit
    def setMessage(): Unit
}

class ProcessDialog(stuffs: List[Stuff], stuffTable: Table) extends SimpleSWT
{
    var continue = true

    def createDialog(stuff:Stuff) = {
        val shell = new Shell(MainWindow.shell, SWT.SHEET)

        lazy val titleText = controls('TitleText).asInstanceOf[Text]
        lazy val descriptionText = controls('DescriptionText).asInstanceOf[Text]
        lazy val cancelButton = controls('CancelButton).asInstanceOf[Button]
        lazy val topicCombo = new TopicCombo(controls('TopicCombo).asInstanceOf[Combo])
        lazy val outcomeText = controls('OutcomeText).asInstanceOf[Text]

        val fillWidth = new GridData(SWT.FILL, SWT.NONE, true, false)
        val fillBoth  = new GridData(SWT.FILL, SWT.FILL, true, true)

        val stackLayout = new StackLayout

        val rowLayoutData = new GridData(SWT.END, SWT.NONE, true, false, 1, 1)
        val rowLayout = new RowLayout
        rowLayout.justify = true
        rowLayout.marginRight = 10
        rowLayout.pack = false

        object WizardPage0 extends WizardPage
        {
            def wizardPage = controls('WPage0).asInstanceOf[Composite]

            def actionableButton = controls('ActionableRadio).asInstanceOf[Button]
            def notActionableButton = controls('NotActionableRadio).asInstanceOf[Button]

            def prevButton = controls('PrevButton0).asInstanceOf[Button]
            def nextButton = controls('NextButton0).asInstanceOf[Button]
            def finishButton = controls('FinishButton0).asInstanceOf[Button]
            def cancelButton = controls('CancelButton0).asInstanceOf[Button]

            def initPage () {
                prevButton.setEnabled(false)
                finishButton.setEnabled(false)
            }

            def setMessage()
            {
                val title = titleText.getText
                DialogWindow.setMessages(Maid.ProcessDialogMessage.actionableOrNot(title))
            }

            def ui = composite(layout = new GridLayout, layoutData = fillBoth, id = 'WPage0)(
                composite(new GridLayout, layoutData = fillBoth)(
                    button(_t("Yes, it is actionable"), style = SWT.RADIO, id = 'ActionableRadio, init = {_.setSelection(true)}),
                    button(_t("No, it is not actionable"), style = SWT.RADIO, id = 'NotActionableRadio)
                ),

                composite(new RowLayout, rowLayoutData) (
                    button(_t("Prev"), icon = "/icons/prev.png", id = 'PrevButton0),
                    button(_t("Next"), icon = "/icons/next.png", id = 'NextButton0),
                    button(_t("Finish"), icon = "/icons/finish.png", id = 'FinishButton0),
                    button(_t("Cancel"), icon = "/icons/cancel.png", id = 'CancelButton0)
                )
            )_

            def initListener () {
                cancelButton.addSelectionListener { e: SelectionEvent =>
                    continue = false
                    shell.close()
                }

                nextButton.addSelectionListener {e: SelectionEvent =>
                    actionableButton.getSelection match {
                        case true  => setWizardPage(WizardPage1)
                        case false => setWizardPage(WizardPage2)
                    }
                }
            }

        }

        object WizardPage1 extends WizardPage
        {
            def wizardPage = controls('WPage1).asInstanceOf[Composite]

            def doASAPRadio = controls('DoASAPRadio).asInstanceOf[Button]
            def delegatedRadio = controls('DelegatedRadio).asInstanceOf[Button]
            def scheduledRadio = controls('ScheduledRadio).asInstanceOf[Button]

            def prevButton = controls('PrevButton1).asInstanceOf[Button]
            def nextButton = controls('NextButton1).asInstanceOf[Button]
            def finishButton = controls('FinishButton1).asInstanceOf[Button]
            def cancelButton = controls('CancelButton1).asInstanceOf[Button]

            def initPage () {
                finishButton.setEnabled(false)
            }

            def setMessage() {
                DialogWindow.setMessages(Maid.ProcessDialogMessage.whatActionType)
            }

            def ui = composite(layout = new GridLayout, layoutData = fillBoth, id = 'WPage1)(
                composite(new GridLayout, layoutData = fillBoth)(
                    label(_t("Expected Successful outcome:")), 
                    text("", layoutData = new GridData(SWT.FILL, SWT.NONE, true, false), id = 'OutcomeText),
                    button(_t("Do ASAP"), style = SWT.RADIO, id = 'DoASAPRadio, init = {_.setSelection(true)}),
                    button(_t("Delegated"), style = SWT.RADIO, id = 'DelegatedRadio),
                    button(_t("Scheduled"), style = SWT.RADIO, id = 'ScheduledRadio)
                ),

                composite(new RowLayout, rowLayoutData) (
                    button(_t("Prev"), icon = "/icons/prev.png", id = 'PrevButton1),
                    button(_t("Next"), icon = "/icons/next.png", id = 'NextButton1),
                    button(_t("Finish"), icon = "/icons/finish.png", id = 'FinishButton1),
                    button(_t("Cancel"), icon = "/icons/cancel.png", id = 'CancelButton1)
                )
            )_

            def initListener () {
                cancelButton.addSelectionListener { e: SelectionEvent =>
                    continue = false
                    shell.close()
                }

                prevButton.addSelectionListener {e: SelectionEvent => setWizardPage(WizardPage0) }
                nextButton.addSelectionListener {e: SelectionEvent =>

                    if (doASAPRadio.getSelection) {
                        setWizardPage(WizardPage4)
                    }

                    if (delegatedRadio.getSelection) {
                        setWizardPage(WizardPage5)
                    }

                    if (scheduledRadio.getSelection) {
                        setWizardPage(WizardPage6)
                    }

                }
            }

        }

        object WizardPage2 extends WizardPage
        {
            def wizardPage = controls('WPage2).asInstanceOf[Composite]

            def maybeRadio = controls('MaybeRadio).asInstanceOf[Button]
            def deleteRadio = controls('DeleteRadio).asInstanceOf[Button]
            def referenceRadio = controls('ReferenceRadio).asInstanceOf[Button]

            def prevButton = controls('PrevButton2).asInstanceOf[Button]
            def nextButton = controls('NextButton2).asInstanceOf[Button]
            def finishButton = controls('FinishButton2).asInstanceOf[Button]
            def cancelButton = controls('CancelButton2).asInstanceOf[Button]

            def isLastStep = !maybeRadio.getSelection

            def initPage () {
                finishButton.setEnabled(isLastStep && titleText.getText.length > 0)
                nextButton.setEnabled(!isLastStep)
            }

            def setMessage()
            {
                DialogWindow.setMessages(Maid.ProcessDialogMessage.notActionable)
            }

            def ui = composite(layout = new GridLayout, layoutData = fillBoth, id = 'WPage2)(
                composite(new RowLayout(SWT.VERTICAL), layoutData = fillBoth)(
                    button(
                        _t("Delete"), 
                        style = SWT.RADIO, 
                        id = 'DeleteRadio, 
                        init = {_.setSelection(true)}
                    ),
                    button(_t("Someday / Maybe"), style = SWT.RADIO, id = 'MaybeRadio),
                    button(_t("Reference"), style = SWT.RADIO, id = 'ReferenceRadio)
                ),

                composite(new RowLayout, rowLayoutData) (
                    button(_t("Prev"), icon = "/icons/prev.png", id = 'PrevButton2),
                    button(_t("Next"), icon = "/icons/next.png", id = 'NextButton2),
                    button(_t("Finish"), icon = "/icons/finish.png", id = 'FinishButton2),
                    button(_t("Cancel"), icon = "/icons/cancel.png", id = 'CancelButton2)
                )
            )_

            def initListener () {
                titleText.addModifyListener{e : ModifyEvent =>
                    initPage ()
                }

                cancelButton.addSelectionListener { e: SelectionEvent =>
                    continue = false
                    shell.close()
                }

                maybeRadio.addSelectionListener {e: SelectionEvent =>
                    initPage()
                }

                prevButton.addSelectionListener {e: SelectionEvent => setWizardPage(WizardPage0) }
                nextButton.addSelectionListener {e: SelectionEvent => setWizardPage(WizardPage3) }

                finishButton.addSelectionListener {e: SelectionEvent =>
                    if (deleteRadio.getSelection) {
                        stuff.delete()
                    }

                    if (referenceRadio.getSelection) {
                        val description = descriptionText.getOptionText
                        val reference = stuff.convertToReference(
                            titleText.getText, 
                            description, 
                            topicCombo.getTopic
                        )
                        MainWindow.referenceTab.addToReferenceTable(reference)
                        MainWindow.referenceTab.adjustColumnWidth ()
                    }

                    stuffTable.removeByData(stuff.stuffID)
                    shell.close()
                }
            }

        }

        object WizardPage3 extends WizardPage
        {
            def wizardPage = controls('WPage3).asInstanceOf[Composite]

            def tickleCheck = controls('TickeCheck).asInstanceOf[Button]
            def tickleDate = controls('TickleDate).asInstanceOf[DateTime]

            def prevButton = controls('PrevButton3).asInstanceOf[Button]
            def nextButton = controls('NextButton3).asInstanceOf[Button]
            def finishButton = controls('FinishButton3).asInstanceOf[Button]
            def cancelButton = controls('CancelButton3).asInstanceOf[Button]

            def initPage () {
                finishButton.setEnabled(titleText.getText.length > 0)
                nextButton.setEnabled(false)
            }

            def setMessage ()
            {
                DialogWindow.setMessages(Maid.ProcessDialogMessage.maybe)
            }

            def ui = composite(layout = new GridLayout, layoutData = fillBoth, id = 'WPage3)(
                composite(new RowLayout, layoutData = fillBoth)(
                    button(
                        _t("Tickle me at"), 
                        style = SWT.CHECK, 
                        id = 'TickeCheck, 
                        init = {_.setSelection(false)}
                    ),
                    dateTime(
                        style = SWT.DATE|SWT.DROP_DOWN, 
                        id = 'TickleDate, 
                        init = {_.setEnabled(false)}
                    )
                ),

                composite(new RowLayout, rowLayoutData) (
                    button(_t("Prev"), icon = "/icons/prev.png", id = 'PrevButton3),
                    button(_t("Next"), icon = "/icons/next.png", id = 'NextButton3),
                    button(_t("Finish"), icon = "/icons/finish.png", id = 'FinishButton3),
                    button(_t("Cancel"), icon = "/icons/cancel.png", id = 'CancelButton3)
                )
            )_

            def initListener () {
                tickleCheck.addSelectionListener{e: SelectionEvent => 
                    tickleDate.setEnabled(tickleCheck.getSelection)
                }
                titleText.addModifyListener{e : ModifyEvent => initPage () }

                cancelButton.addSelectionListener { e: SelectionEvent =>
                    continue = false
                    shell.close()
                }

                prevButton.addSelectionListener {e: SelectionEvent => setWizardPage(WizardPage2) }
                finishButton.addSelectionListener {e: SelectionEvent =>
                    val description = descriptionText.getOptionText

                    val tickleDateOption = tickleCheck.getSelection match {
                        case false => None
                        case true  => 
                            val calendar = Calendar.getInstance
                            calendar.clear()
                            calendar.set(tickleDate.getYear, tickleDate.getMonth, tickleDate.getDay)
                            Some(calendar.getTime)
                    }

                    val maybe = stuff.convertToMaybe(
                        titleText.getText, description, 
                        tickleDateOption, topicCombo.getTopic
                    )

                    MainWindow.maybeTab.addToMaybeTable(maybe)
                    MainWindow.maybeTab.adjustColumnWidth ()

                    stuffTable.removeByData(stuff.stuffID)
                    shell.close()
                }
            }

        }

        // DoASAP
        object WizardPage4 extends WizardPage
        {
            def wizardPage = controls('WPage4).asInstanceOf[Composite]

            def prevButton = controls('PrevButton4).asInstanceOf[Button]
            def nextButton = controls('NextButton4).asInstanceOf[Button]
            def finishButton = controls('FinishButton4).asInstanceOf[Button]
            def cancelButton = controls('CancelButton4).asInstanceOf[Button]

            def contextCombo = new ContextCombo(controls('ContextCombo4).asInstanceOf[Combo])
            def projectCombo = new ProjectCombo(controls('ProjectCombo4).asInstanceOf[Combo])
            def isDoneButton = controls('IsDoneButton4).asInstanceOf[Button]

            def initPage () {
                finishButton.setEnabled(titleText.getText.length > 0)
                nextButton.setEnabled(false)
            }

            def setMessage ()
            {
                DialogWindow.setMessages(Maid.ProcessDialogMessage.doASAP)
            }

            def ui = composite(layout = new GridLayout, layoutData = fillBoth, id = 'WPage4)(
                composite(new GridLayout(2, false), layoutData = fillBoth)(
                    label(_t("Context:")), combo(layoutData = fillWidth, id = 'ContextCombo4),
                    label(_t("Project:")), combo(layoutData = fillWidth, id = 'ProjectCombo4),
                    button(_t("Done"), style = SWT.CHECK, id = 'IsDoneButton4)
                ),

                composite(new RowLayout, rowLayoutData) (
                    button(_t("Prev"), icon = "/icons/prev.png", id = 'PrevButton4),
                    button(_t("Next"), icon = "/icons/next.png", id = 'NextButton4),
                    button(_t("Finish"), icon = "/icons/finish.png", id = 'FinishButton4),
                    button(_t("Cancel"), icon = "/icons/cancel.png", id = 'CancelButton4)
                )
            )_

            def initListener () {

                contextCombo.init()
                projectCombo.init()

                titleText.addModifyListener{e : ModifyEvent =>
                    initPage ()
                }

                prevButton.addSelectionListener {e: SelectionEvent =>
                    setWizardPage(WizardPage1)
                }

                cancelButton.addSelectionListener { e: SelectionEvent =>
                    continue = false
                    shell.close()
                }

                finishButton.addSelectionListener {e: SelectionEvent =>
                    val description = descriptionText.getOptionText
                    val outcome = outcomeText.getOptionText
                    val isDone = isDoneButton.getSelection
                    val topic = topicCombo.getTopic
                    val context = contextCombo.getContext
                    val project = projectCombo.getProject

                    val action = stuff.convertToActionDoASAP(
                        titleText.getText, description, 
                        outcome, topic
                    )
                    context.foreach { action.addToContext }
                    project.foreach { action.addToProject }

                    if (isDone) {
                        action.markAsDone()
                    }

                    MainWindow.actionTab.addAction(action)
                    MainWindow.projectTab.addAction(action)
                    MainWindow.actionTab.adjustColumnWidth()

                    stuffTable.removeByData(stuff.stuffID)
                    shell.close()
                }

            }

        }

        // Delegated
        object WizardPage5 extends WizardPage
        {
            def wizardPage = controls('WPage5).asInstanceOf[Composite]

            def prevButton = controls('PrevButton5).asInstanceOf[Button]
            def nextButton = controls('NextButton5).asInstanceOf[Button]
            def finishButton = controls('FinishButton5).asInstanceOf[Button]
            def cancelButton = controls('CancelButton5).asInstanceOf[Button]

            def delegatedText = controls('DelegatedTo).asInstanceOf[Text]
            def delegatedEMailText = controls('DelegatedEMail).asInstanceOf[Text]

            def contextCombo = new ContextCombo(controls('ContextCombo5).asInstanceOf[Combo])
            def projectCombo = new ProjectCombo(controls('ProjectCombo5).asInstanceOf[Combo])
            def isDoneButton = controls('IsDoneButton5).asInstanceOf[Button]

            def initPage () {
                finishButton.setEnabled(titleText.getText.length > 0 && delegatedText.getText.length > 0)
                nextButton.setEnabled(false)
            }

            def setMessage ()
            {
                DialogWindow.setMessages(Maid.ProcessDialogMessage.delegated)
            }

            def ui = composite(layout = new GridLayout, layoutData = fillBoth, id = 'WPage5)(
                composite(new GridLayout(2, false), layoutData = fillBoth)(
                    label(_t("Delegated:")), text(_t("Required"), layoutData = fillWidth, id = 'DelegatedTo),
                    label(_t("Email:")), text("", layoutData = fillWidth, id = 'DelegatedEMail),
                    label(_t("Context:")), combo(layoutData = fillWidth, id = 'ContextCombo5),
                    label(_t("Project:")), combo(layoutData = fillWidth, id = 'ProjectCombo5),
                    button(_t("Done"), style = SWT.CHECK, id = 'IsDoneButton5)
                   
                ),

                composite(new RowLayout, rowLayoutData) (
                    button(_t("Prev"), icon = "/icons/prev.png", id = 'PrevButton5),
                    button(_t("Next"), icon = "/icons/next.png", id = 'NextButton5),
                    button(_t("Finish"), icon = "/icons/finish.png", id = 'FinishButton5),
                    button(_t("Cancel"), icon = "/icons/cancel.png", id = 'CancelButton5)
                )
            )_

            def initListener () {
                
                contextCombo.init()
                projectCombo.init()

                titleText.addModifyListener{e : ModifyEvent =>
                    initPage ()
                }

                delegatedText.addModifyListener{e : ModifyEvent =>
                    initPage ()
                }

                prevButton.addSelectionListener {e: SelectionEvent =>
                    setWizardPage(WizardPage1)
                }

                cancelButton.addSelectionListener { e: SelectionEvent =>
                    continue = false
                    shell.close()
                }

                finishButton.addSelectionListener {e: SelectionEvent =>
                    val description = descriptionText.getOptionText
                    val outcome = outcomeText.getOptionText
                    val isDone = isDoneButton.getSelection
                    val topic = topicCombo.getTopic
                    val context = contextCombo.getContext
                    val project = projectCombo.getProject
                    val delegatedTo = delegatedText.getText
                    val delegatedEMail = delegatedEMailText.getOptionText

                    val action = stuff.convertToActionDelegated (
                        titleText.getText, description, outcome, topic)(delegatedTo, delegatedEMail)

                    context.foreach { action.addToContext }
                    project.foreach { action.addToProject }

                    if (isDone) {
                        action.markAsDone()
                    }

                    MainWindow.actionTab.addAction(action)
                    MainWindow.projectTab.addAction(action)
                    MainWindow.actionTab.adjustColumnWidth()

                    stuffTable.removeByData(stuff.stuffID)
                    shell.close()
                }

            }
        }

        // Scheduled
        object WizardPage6 extends WizardPage
        {
            def wizardPage = controls('WPage6).asInstanceOf[Composite]

            def prevButton = controls('PrevButton6).asInstanceOf[Button]
            def nextButton = controls('NextButton6).asInstanceOf[Button]
            def finishButton = controls('FinishButton6).asInstanceOf[Button]
            def cancelButton = controls('CancelButton6).asInstanceOf[Button]

            def contextCombo = new ContextCombo(controls('ContextCombo6).asInstanceOf[Combo])
            def projectCombo = new ProjectCombo(controls('ProjectCombo6).asInstanceOf[Combo])
            def isDoneButton = controls('IsDoneButton5).asInstanceOf[Button]
            def locationText = controls('LocationText).asInstanceOf[Text]
            def scheduledDate = controls('ScheduledDate).asInstanceOf[DateTime]
            def scheduledTime = controls('ScheduledTime).asInstanceOf[DateTime]
            def durationSpinner = controls('DurationSpinner).asInstanceOf[Spinner]

            def initPage () {
                finishButton.setEnabled(titleText.getText.length > 0)
                nextButton.setEnabled(false)
            }

            def setMessage ()
            {
                DialogWindow.setMessages(Maid.ProcessDialogMessage.scheduled)
            }

            val fillWidthSpan = new GridData(SWT.FILL, SWT.NONE, true, false, 2, 1)

            def ui = composite(layout = new GridLayout, layoutData = fillBoth, id = 'WPage6)(
                composite(new GridLayout(3, false), layoutData = fillBoth)(
                    label(_t("Context:")), combo(layoutData = fillWidthSpan, id = 'ContextCombo6),
                    label(_t("Project:")), combo(layoutData = fillWidthSpan, id = 'ProjectCombo6),
                    label(_t("Date:")), dateTime(style = SWT.DATE|SWT.DROP_DOWN, layoutData = fillWidth, id = 'ScheduledDate),
                    dateTime(style = SWT.TIME|SWT.SHORT, layoutData = fillWidth, id = 'ScheduledTime),
                    label(_t("Duration:")), spinner(layoutData = new GridData(SWT.FILL, SWT.FILL, false, false), id = 'DurationSpinner), label ("minutes"),
                    label(_t("Location:")), text("", layoutData = fillWidthSpan, id = 'LocationText),

                    button(_t("Done"), style = SWT.CHECK, id = 'IsDoneButton6)
                   
                ),

                composite(new RowLayout, rowLayoutData) (
                    button(_t("Prev"), icon = "/icons/prev.png", id = 'PrevButton6),
                    button(_t("Next"), icon = "/icons/next.png", id = 'NextButton6),
                    button(_t("Finish"), icon = "/icons/finish.png", id = 'FinishButton6),
                    button(_t("Cancel"), icon = "/icons/cancel.png", id = 'CancelButton6)
                )
            )_

            def initListener () {
                
                contextCombo.init()
                projectCombo.init()

                titleText.addModifyListener{e : ModifyEvent =>
                    initPage ()
                }

                prevButton.addSelectionListener {e: SelectionEvent =>
                    setWizardPage(WizardPage1)
                }

                cancelButton.addSelectionListener { e: SelectionEvent =>
                    continue = false
                    shell.close()
                }

                finishButton.addSelectionListener { e: SelectionEvent =>
                    val description = descriptionText.getOptionText
                    val outcome = outcomeText.getOptionText
                    val isDone = isDoneButton.getSelection
                    val topic = topicCombo.getTopic
                    val context = contextCombo.getContext
                    val project = projectCombo.getProject
                    val location = locationText.getOptionText
                    val duration = durationSpinner.getSelection match {
                        case 0 => None
                        case _ => Some(durationSpinner.getSelection)
                    }

                    val calendar = Calendar.getInstance

                    calendar.clear()
                    calendar.set(
                        scheduledDate.getYear, scheduledDate.getMonth, 
                        scheduledDate.getDay, scheduledTime.getHours,
                        scheduledTime.getMinutes
                    )


                    import java.sql.Timestamp

                    val action = stuff.convertToActionScheduled(
                        titleText.getText, description, 
                        outcome, topicCombo.getTopic)(new Timestamp(calendar.getTime.getTime), duration, location)

                    context.foreach { action.addToContext }
                    project.foreach { action.addToProject }

                    if (isDone) {
                        action.markAsDone()
                    }

                    MainWindow.actionTab.addAction(action)
                    MainWindow.projectTab.addAction(action)
                    MainWindow.actionTab.adjustColumnWidth()

                    stuffTable.removeByData(stuff.stuffID)
                    shell.close()

                }
            }
        }

        def setWizardPage(page: WizardPage) {
            page.initPage()
            page.setMessage()
            stackLayout.topControl = page.wizardPage
            controls('WizardPage).asInstanceOf[Composite].layout()
            shell.setData(page)
        }

        shell.setText(_t("Process Stuff"))
        shell.addComposite(composite(new GridLayout)(
            group(_t("Info"), layout = new GridLayout(2, false), layoutData = fillBoth)(
                label(_t("Title")), text(_t("Required"), layoutData = fillWidth, id = 'TitleText, init = {_.setText(stuff.title)}),
                label(_t("Topic")), combo(layoutData = fillWidth, id = 'TopicCombo),
                label(_t("Description")), 
                text(
                    _t("Stuff Description"), 
                    style = SWT.MULTI|SWT.BORDER|SWT.V_SCROLL|SWT.WRAP,
                    layoutData = fillBoth, 
                    id = 'DescriptionText, 
                    init = {_.setText(stuff.description getOrElse "")}
                )
            ),

            composite(stackLayout, layoutData = fillBoth, id = 'WizardPage)(
                WizardPage0.ui, WizardPage1.ui, WizardPage2.ui, WizardPage3.ui,
                WizardPage4.ui, WizardPage5.ui, WizardPage6.ui
            )
        ))

        topicCombo.init()
        stuff.topics.foreach { topicCombo.setTopic }

        setWizardPage(WizardPage0)

        WizardPage0.initListener()
        WizardPage1.initListener()
        WizardPage2.initListener()
        WizardPage3.initListener()
        WizardPage4.initListener()
        WizardPage5.initListener()
        WizardPage6.initListener()

        shell.setSize(400, 350)
        shell.pack()
        shell.open()
        shell
    }

    stuffs.foreach { stuff =>
        if (continue) {
            val dialogShell = createDialog(stuff)
            val display = Display.getDefault

            while (!dialogShell.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }
        }
    }
}

