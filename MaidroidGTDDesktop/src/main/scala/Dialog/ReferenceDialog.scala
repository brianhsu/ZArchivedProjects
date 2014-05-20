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
import org.eclipse.swt.SWT

import org.maidroid.gtd.model._

import Controller.gtd.Implicit._

import I18N.TR.{tr => _t}

class EditReferenceDialog(reference: Reference) extends SimpleSWT
{
    val shell = new Shell(MainWindow.shell, SWT.DIALOG_TRIM|SWT.SHEET)

    shell.setText(_t("Edit Reference"))

    lazy val titleText = controls('TitleText).asInstanceOf[Text]
    lazy val descriptionText = controls('DescriptionText).asInstanceOf[Text]
    lazy val editButton = controls('EditButton).asInstanceOf[Button]
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
            label(_t("Ttile")), text(_t("Stuff Title"), layoutData = fillWidth, id = 'TitleText),
            label(_t("Topic")), combo(layoutData = fillWidth, id = 'TopicCombo),
            label(_t("Description")), 
            text(
                _t("Description"), 
                style = SWT.MULTI|SWT.BORDER|SWT.V_SCROLL|SWT.WRAP,
                layoutData = fillBoth, 
                id = 'DescriptionText
            ),

            composite(rowLayout, rowLayoutData) (
                button(_t("Cancel"), icon = "/icons/cancel.png", id = 'CancelButton), 
                button(_t("Edit"), icon = "/icons/finish.png", id = 'EditButton, init = {_.setEnabled(false)})
            )
        ))

        topicCombo.init()
    }

    def setupUIListener()
    {
        editButton.addSelectionListener { event: SelectionEvent =>
            
            val newReference = reference.copy (
                title = titleText.getText,
                description = descriptionText.getOptionText
            )

            reference.update(newReference)
            reference.topics.foreach { reference.removeTopic }
            topicCombo.getTopic.foreach { newReference.addToTopic }


            MainWindow.referenceTab.updateRow(newReference)
            MainWindow.referenceTab.adjustColumnWidth()

            shell.dispose()

            DialogWindow.setMessages(Maid.ReferenceDialogMessage.updated)
        }

        cancelButton.addSelectionListener { event: SelectionEvent =>
            shell.dispose()
            DialogWindow.setMessages(Maid.ReferenceDialogMessage.cancel)
        }

        titleText.addModifyListener{e : ModifyEvent =>
            titleText.getText.length match {
                case 0 => editButton.setEnabled(false)
                case _ => editButton.setEnabled(true)
            }
        }
    }

    def init () {
        titleText.setText(reference.title)
        reference.topics.foreach { topicCombo.setTopic }
        reference.description.foreach { descriptionText.setText }
        DialogWindow.setMessages(Maid.ReferenceDialogMessage.opened)
    }

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
