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

import Controller.gtd.Implicit._

import I18N.TR.{tr => _t}

class PreferenceWindow extends SimpleSWT
{
    val display = Display.getDefault
    val shell = new Shell(display, SWT.SHELL_TRIM)

    def createEMailTab(tabFolder: TabFolder) {
        val tabItem = new TabItem(tabFolder, SWT.NONE)
        val fillWidth = new GridData(SWT.FILL, SWT.NONE, true, false, 2, 1)

        val serverComposite = composite(new GridLayout(3, false))(
            label(_t("Username:")), text("", layoutData = fillWidth, id = 'EMailUserName),
            label(_t("Password:")), text("", style = SWT.PASSWORD|SWT.BORDER, layoutData = fillWidth, id = 'EMailPassword),
            label(_t("POP3 Server:")), text("", layoutData = fillWidth, id = 'EMailPOP3Server),
            label(_t("Port:")), text("", layoutData = fillWidth, id = 'EMailServerPort),
            button(_t("Use SSL Connection"), style = SWT.CHECK, id = 'EMailSSL, layoutData = new GridData(SWT.NONE, SWT.NONE, false, false, 3, 1)),
            button(_t("Check mail at every "), style = SWT.CHECK, id = 'EMailCheckEvery), 
            text("    ", id = 'EMailCheckInterval), label(_t("minutes (Need restart)")),
            button(_t("Check mail at start"), style = SWT.CHECK, id = 'EMailCheckStart)
        )_

        val clientComposite = composite(new GridLayout(2, false))(
            label(_t("Command:")), 
            text("%s will replace with receiver's mail address", layoutData = new GridData(SWT.FILL, SWT.NONE, true, false), id = 'EMailClient)
        )_

        tabItem.setText("EMail")
        tabItem.addComposite(composite(new GridLayout)(
            group(_t("Mail Server"), layout = new FillLayout, layoutData = new GridData(SWT.FILL, SWT.NONE, true, false))(
                serverComposite
            ),

            group(_t("Mail Client"), layout = new FillLayout, layoutData = new GridData(SWT.FILL, SWT.NONE, true, false))(
                clientComposite
            )
        ))
    }

    def createContextTab(tabFolder: TabFolder) {
        val tabItem = new TabItem(tabFolder, SWT.NONE)
        val fillWidth = new GridData(SWT.FILL, SWT.NONE, true, false)
        tabItem.setText(_t("Context"))
        tabItem.addComposite(composite(new GridLayout(2, false))(
            table(column(_t("Title"))_ :: Nil, layoutData = new GridData(SWT.FILL, SWT.FILL, true, true), id = 'ContextTable),
            composite(new RowLayout(SWT.VERTICAL), layoutData = new GridData(SWT.CENTER, SWT.TOP, false, true))(
                button(_t("Remove"), icon = "/icons/remove.png", id = 'ContextRemoveButton)
            )
        ))
    }

    def createTopicTab(tabFolder: TabFolder) {
        val tabItem = new TabItem(tabFolder, SWT.NONE)
        val fillWidth = new GridData(SWT.FILL, SWT.NONE, true, false)
        tabItem.setText(_t("Topic"))
        tabItem.addComposite(composite(new GridLayout(2, false))(
            table(column(_t("Title"))_ :: Nil, layoutData = new GridData(SWT.FILL, SWT.FILL, true, true), id = 'TopicTable),
            composite(new RowLayout(SWT.VERTICAL), layoutData = new GridData(SWT.CENTER, SWT.TOP, false, true))(
                button(_t("Remove"), icon = "/icons/remove.png", id = 'TopicRemoveButton)
            )
        ))
    }

    lazy val applyButton = controls('ApplyButton).asInstanceOf[Button]
    lazy val closeButton = controls('CloseButton).asInstanceOf[Button]

    object EMailTabUI {
        lazy val username = controls('EMailUserName).asInstanceOf[Text]
        lazy val password = controls('EMailPassword).asInstanceOf[Text]
        lazy val server = controls('EMailPOP3Server).asInstanceOf[Text]
        lazy val port = controls('EMailServerPort).asInstanceOf[Text]
        lazy val useSSL = controls('EMailSSL).asInstanceOf[Button]
        lazy val checkInterval = controls('EMailCheckInterval).asInstanceOf[Text]
        lazy val checkEvery = controls('EMailCheckEvery).asInstanceOf[Button]
        lazy val checkStart = controls('EMailCheckStart).asInstanceOf[Button]
        lazy val emailClient = controls('EMailClient).asInstanceOf[Text]
    }

    object ContextTabUI {
        lazy val table = controls('ContextTable).asInstanceOf[Table]
        lazy val removeButton = controls('ContextRemoveButton).asInstanceOf[Button]
    }

    object TopicTabUI {
        lazy val table = controls('TopicTable).asInstanceOf[Table]
        lazy val removeButton = controls('TopicRemoveButton).asInstanceOf[Button]
    }

    shell.setText(_t("Preference"))
    shell.addComposite(composite(new GridLayout)(
        composite(new FillLayout, layoutData = new GridData(SWT.FILL, SWT.FILL, true, true))(
            tabFolder(createEMailTab _ :: createTopicTab _ :: createContextTab _ :: Nil)
        ),
        composite(new RowLayout, layoutData = new GridData(SWT.END, SWT.NONE, true, false))(
            button(_t("OK"), icon = "/icons/finish.png", id = 'ApplyButton),
            button(_t("Close"), icon = "/icons/close.png", id = 'CloseButton)
        )
    ))

    def initTopicTab ()
    {
        Controller.gtd.Topic.list.sortBy(_.name).foreach { topic =>
            val tableItem = new TableItem(TopicTabUI.table, SWT.NONE)
            tableItem.setText(topic.name)
            tableItem.setData(topic.topicID)
        }
    }

    def initContextTab ()
    {
        Controller.gtd.Context.list.sortBy(_.name).foreach { context =>
            val tableItem = new TableItem(ContextTabUI.table, SWT.NONE)
            tableItem.setText(context.name)
            tableItem.setData(context.contextID)
        }
    }

    def initEMailTab ()
    {
        Preference.get("EMailUserName").foreach { EMailTabUI.username.setText }
        Preference.get("EMailPassword").foreach { EMailTabUI.password.setText }
        Preference.get("EMailServer").foreach { EMailTabUI.server.setText }
        Preference.get("EMailServerPort").foreach { EMailTabUI.port.setText }
        Preference.get("EMailCheckInterval").foreach { EMailTabUI.checkInterval.setText }
        Preference.get("EMailClient").foreach { EMailTabUI.emailClient.setText }

        Preference.get("EMailSSL") match {
            case Some("true") => EMailTabUI.useSSL.setSelection(true)
            case _ =>
        }

        Preference.get("EMailCheckStart") match {
            case Some("true") => EMailTabUI.checkStart.setSelection(true)
            case _ =>
        }

        Preference.get("EMailCheckEvery") match {
            case Some("true") => 
                EMailTabUI.checkEvery.setSelection(true)
                EMailTabUI.checkInterval.setEnabled(true)

            case _ =>
                EMailTabUI.checkEvery.setSelection(false)
                EMailTabUI.checkInterval.setEnabled(false)

        }

    }

    def setupListener()
    {
        EMailTabUI.checkEvery.addSelectionListener {e: SelectionEvent =>
            EMailTabUI.checkInterval.setEnabled(EMailTabUI.checkEvery.getSelection)
        }

        EMailTabUI.port.addVerifyListener(new VerifyListener() {
            override def verifyText(e: VerifyEvent) {
                e.doit = EMailTabUI.port.getText match {
                    case null => false
                    case value => value.forall(_.isDigit) && e.text.forall(_.isDigit)
                }
            }
        })

        EMailTabUI.checkInterval.addVerifyListener(new VerifyListener() {
            override def verifyText(e: VerifyEvent) {
                e.doit = EMailTabUI.port.getText match {
                    case null => false
                    case value => value.forall(_.isDigit) && e.text.forall(_.isDigit)
                }
            }

        })

        ContextTabUI.removeButton.addSelectionListener{e: SelectionEvent =>
            
            ContextTabUI.table.getSelection.foreach { item =>
                val contextID = item.getData.asInstanceOf[Int]
                val context = Controller.gtd.Context.get(contextID)
                val actions = context.actions

                context.delete()
                ContextTabUI.table.removeByData(contextID)

                actions.foreach { action =>
                    MainWindow.actionTab.updateRow(action, action)
                }
            }
        }

        TopicTabUI.removeButton.addSelectionListener{e: SelectionEvent =>
            
            TopicTabUI.table.getSelection.foreach { item =>
                val topicID = item.getData.asInstanceOf[Int]
                val topic = Controller.gtd.Topic.get(topicID)
                val stuffs = topic.stuffs
                val actions = topic.actions
                val projects = topic.projects
                val references = topic.references
                val maybes = topic.maybes

                topic.delete()
                TopicTabUI.table.removeByData(topicID)

                stuffs.foreach { MainWindow.inboxTab.updateRow }
                projects.foreach { MainWindow.projectTab.updateProject }
                references.foreach { MainWindow.referenceTab.updateRow }
                maybes.foreach { MainWindow.maybeTab.updateRow }

                actions.foreach { action =>
                    MainWindow.actionTab.updateRow(action, action)
                    MainWindow.projectTab.updateAction(action)
                }

            }

        }

        closeButton.addSelectionListener{e: SelectionEvent =>
            shell.dispose()
        }

        applyButton.addSelectionListener{e: SelectionEvent =>

            Preference.clear()

            EMailTabUI.username.getOptionText.foreach {
                Preference.setProperty("EMailUserName", _)
            }

            EMailTabUI.password.getOptionText.foreach {
                Preference.setProperty("EMailPassword", _)
            }

            EMailTabUI.server.getOptionText.foreach {
                Preference.setProperty("EMailServer", _)
            }

            EMailTabUI.port.getOptionText.foreach {
                Preference.setProperty("EMailServerPort", _)
            }

            EMailTabUI.checkInterval.getOptionText.foreach {
                Preference.setProperty("EMailCheckInterval", _)
            }

            EMailTabUI.emailClient.getOptionText.foreach {
                Preference.setProperty("EMailClient", _)
            }

            EMailTabUI.useSSL.getSelection match {
                case true => Preference.setProperty("EMailSSL", "true")
                case false => Preference.setProperty("EMailSSL", "false")
            }

            EMailTabUI.checkStart.getSelection match {
                case true  => Preference.setProperty("EMailCheckStart", "true")
                case false => 
            }

            EMailTabUI.checkEvery.getSelection match {
                case true  => Preference.setProperty("EMailCheckEvery", "true")
                case false => 
            }

            Preference.save()
            shell.dispose()
        }
    }

    def show()
    {
        shell.open()
    }

    initEMailTab()
    initContextTab()
    initTopicTab()
    setupListener()
}

