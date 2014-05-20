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
import org.eclipse.swt.layout._
import org.eclipse.swt.graphics._
import org.eclipse.swt.SWT

import org.maidroid.gtd.model._

import Controller.gtd.Implicit._

import I18N.TR.{tr => _t}


trait ProjectDialog extends SimpleSWT
{
    def action (): Unit

    val shell = new Shell(MainWindow.shell, SWT.DIALOG_TRIM|SWT.SHEET)

    shell.setText(_t("Add stuff"))

    lazy val titleText = controls('TitleText).asInstanceOf[Text]
    lazy val visionText = controls('VisionText).asInstanceOf[Text]
    lazy val descriptionText = controls('DescriptionText).asInstanceOf[Text]
    lazy val addButton = controls('AddButton).asInstanceOf[Button]
    lazy val cancelButton = controls('CancelButton).asInstanceOf[Button]

    lazy val topicCombo = new TopicCombo(controls('TopicCombo).asInstanceOf[Combo])

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

        shell.addComposite(composite(gridLayout) (
            label(_t("Ttile")), text(_t("Required"), layoutData = fillWidth, id = 'TitleText),
            label(_t("Topic")), combo(layoutData = fillWidth, id = 'TopicCombo),
            label(_t("Vision")), text("", layoutData = fillWidth, id = 'VisionText),
            label(_t("Description")), 
            text(
                "", 
                style = SWT.MULTI|SWT.BORDER|SWT.V_SCROLL|SWT.WRAP,
                layoutData = fillBoth, 
                id = 'DescriptionText
            ),

            composite(rowLayout, rowLayoutData) (
                button(_t("Cancel"), icon = "/icons/cancel.png", id = 'CancelButton), 
                button(_t("Add"), icon = "/icons/add.png", id = 'AddButton, init = {_.setEnabled(false)})
            )
        ))

        topicCombo.init()
    }

    def setupUIListener()
    {
        visionText.addFocusListener {e: FocusEvent =>
            DialogWindow.setMessages(Maid.ProjectDialogMessage.vision)
        }

        addButton.addSelectionListener { event: SelectionEvent =>
            action()
        }

        cancelButton.addSelectionListener { event: SelectionEvent =>
            shell.dispose()
            DialogWindow.setMessages(Maid.ProjectDialogMessage.cancel)
        }

        titleText.addModifyListener{e : ModifyEvent =>
            titleText.getText.length match {
                case 0 => addButton.setEnabled(false)
                case _ => addButton.setEnabled(true)
            }
        }
    }

    def init () {}
    def run () {

        setupUILayout()
        setupUIListener()

        shell.setSize(400, 300)
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

class AddProjectDialog extends ProjectDialog
{
    override def init ()
    {
        DialogWindow.setMessages(Maid.ProjectDialogMessage.addOpened)
    }

    override def action ()
    {
        val title = titleText.getText
        val description = descriptionText.getOptionText
        val topic = topicCombo.getTopic
        val vision = visionText.getOptionText

        val project = Controller.gtd.Project.add(title, description, vision)
        topic.foreach {project.addToTopic(_)}

        MainWindow.projectTab.addProject(project)
        shell.dispose()

        DialogWindow.setMessages(Maid.ProjectDialogMessage.projectAdded(project.title))
    }
}

class EditProjectDialog(project: Project) extends ProjectDialog
{

    override def init ()
    {
        val editIcon = new Image(Display.getDefault, getClass.getResourceAsStream("/icons/finish.png"))

        shell.setText (_t("Edit Project"))

        addButton.setText(_t("Edit"))
        addButton.setImage(editIcon)

        titleText.setText(project.title)
        project.topics.foreach { topicCombo.setTopic }
        project.vision.foreach { visionText.setText }
        project.description.foreach { descriptionText.setText }

        DialogWindow.setMessages(Maid.ProjectDialogMessage.updateOpened)
    }

    override def action ()
    {
        val title = titleText.getText
        val description = descriptionText.getOptionText
        val topic = topicCombo.getTopic
        val vision = visionText.getOptionText

        val newProject = project.copy(title = title, description = description, vision = vision)
        project.update(newProject)
        project.topics.foreach {project.removeTopic}
        topic.foreach {newProject.addToTopic}

        MainWindow.projectTab.updateProject(newProject)

        shell.dispose()

        DialogWindow.setMessages(Maid.ProjectDialogMessage.projectUpdated)
    }
}


