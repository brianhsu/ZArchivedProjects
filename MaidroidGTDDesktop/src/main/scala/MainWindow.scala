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

import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import java.util.Locale;

object I18N
{
    val TR = I18nFactory.getI18n(
        getClass, Locale.getDefault(), 
        I18nFactory.FALLBACK|I18nFactory.READ_PROPERTIES
    );
}

import I18N.TR.{tr => _t}


class GTDTabItem(tabFolder: TabFolder, title: String, iconPath: String)
{
    val tabItem = new TabItem(tabFolder, SWT.NONE)
    val image = new Image(Display.getDefault, getClass.getResourceAsStream(iconPath))

    tabItem.setText(title)
    tabItem.setImage(image)
}

object MainWindow extends SimpleSWT
{
    val display = Display.getDefault
    val shell = new Shell(SWT.SHELL_TRIM)

    lazy val tabFolder = controls('TabFolder).asInstanceOf[TabFolder]
    lazy val overviewTab  = controls('OverviewTab).asInstanceOf[OverviewTab]
    lazy val inboxTab  = controls('InboxTab).asInstanceOf[InboxTab]
    lazy val actionTab = controls('ActionTab).asInstanceOf[ActionTab]
    lazy val maybeTab = controls('MaybeTab).asInstanceOf[MaybeTab]
    lazy val referenceTab = controls('ReferenceTab).asInstanceOf[ReferenceTab]
    lazy val projectTab = controls('ProjectTab).asInstanceOf[ProjectTab]

    def messageForTab(index: Int) = index match {
        case 0 => Maid.TabMessage.overview
        case 1 => Maid.TabMessage.inbox(Controller.gtd.Stuff.list)
        case 2 => Maid.TabMessage.action
        case 3 => Maid.TabMessage.maybe
        case 4 => Maid.TabMessage.reference
        case 5 => Maid.TabMessage.project
    }

    def setupUILayout ()
    {
        val gridLayout = new GridLayout(2, false)

        def createOverviewTab(parent: TabFolder) = controls.put ('OverviewTab, new OverviewTab(parent))
        def createInboxTab(parent: TabFolder) = controls.put ('InboxTab, new InboxTab(parent))
        def createReferenceTab(parent: TabFolder) = controls.put ('ReferenceTab, new ReferenceTab(parent))
        def createMaybeTab(parent: TabFolder) = controls.put ('MaybeTab, new MaybeTab(parent))
        def createActionTab(parent: TabFolder) = controls.put ('ActionTab, new ActionTab(parent))
        def createProjectTab(parent: TabFolder) = controls.put ('ProjectTab, new ProjectTab(parent))

        shell.setText(_t("GTDDesktop"))

        shell.addComposite(composite(gridLayout)(
            tabFolder(
                createOverviewTab _ :: createInboxTab _ :: 
                createActionTab _ :: createMaybeTab _ ::
                createReferenceTab _ :: createProjectTab _ :: Nil,
                layoutData = new GridData(SWT.FILL, SWT.FILL, true, true), id = 'TabFolder
            )
        ))

        shell.addListener (SWT.Close, new Listener() {
            def handleEvent (event: Event) {
                event.doit = false
                shell.setVisible(false)
            }
        })

        tabFolder.addSelectionListener(new SelectionListener() {
            override def widgetDefaultSelected(e: SelectionEvent) {
            }

            override def widgetSelected(e: SelectionEvent) {
                println("The button has been pressed")
            }
        })


        tabFolder.addSelectionListener {e: SelectionEvent =>
            DialogWindow.setMessages(messageForTab(tabFolder.getSelectionIndex))

            tabFolder.getSelectionIndex match {
                case 0 => 
                    MainWindow.overviewTab.populateScheduledEvent()
                    MainWindow.overviewTab.updateStatus()

                case 1 =>
                    MainWindow.inboxTab.reprocessTickled()

                case _ =>
            }
        }

        shell.setImage(new Image(display, getClass.getResourceAsStream("/icons/maid.png")))
        shell.pack()
    }

    setupUILayout()

    def isOpen = shell.isVisible
    def show () {
        shell.open()
        DialogWindow.setMessages(
            Maid.MainWindowMessage.opened ++
            messageForTab(tabFolder.getSelectionIndex)
        )
    }

    def hide () {
        shell.setVisible(false)
    }
}

object Splash extends SimpleSWT
{
    val shell = new Shell(SWT.NO_TRIM)

    lazy val progressBar = controls('LoadingProgress).asInstanceOf[ProgressBar]

    shell.addComposite(composite(new GridLayout)(
        label(_t("Maidroid Loading...")),
        progressBar(
            style = SWT.SMOOTH,
            id = 'LoadingProgress, 
            layoutData = new GridData(SWT.FILL, SWT.NONE, true, false)
        )
    ))

    def setLocation()
    {
        val shellSize = shell.getBounds
        val displaySize = Display.getDefault.getPrimaryMonitor.getBounds
        val x = (displaySize.width - shellSize.width) / 2
        val y = (displaySize.height - shellSize.height) / 2

        shell.setLocation(x, y)
    }

    def showSplash ()
    {
        progressBar.setMaximum(6)
        shell.pack()
        setLocation()
        shell.open()

        Display.getDefault.syncExec(new Runnable() {
            override def run ()
            {
                MainWindow
                progressBar.setSelection(1)

                Maid.smile.region
                progressBar.setSelection(1)

                Maid.happy.region
                progressBar.setSelection(3)

                Maid.panic.region
                progressBar.setSelection(4)

                Maid.shy.region
                progressBar.setSelection(5)

                Maid.angry.region
                progressBar.setSelection(6)

                shell.close()
            }
        })

    }

}

object GTDDesktop extends SimpleSWT
{
    def setupTrayItem () =
    {
        val display = Display.getDefault
        val systemTray = Option(display.getSystemTray)

        def addMenuItem(menu: Menu, title: String)(action: => Any)
        {
            val menuItem = new MenuItem(menu, SWT.PUSH)
            menuItem.setText(title)
            menuItem.addSelectionListener {e: SelectionEvent =>
                action
            }
        }

        systemTray.foreach { tray =>
            val item = new TrayItem(tray, SWT.NONE)
            val icon = new Image(display, getClass.getResourceAsStream("/icons/maid.png"))
            val menu = new Menu(MaidWindow.shell, SWT.POP_UP)

            addMenuItem(menu, _t("Show / Hide")){ 
                MaidWindow.toogleVisible() 
                DialogWindow.toogleVisible()
            }

            addMenuItem(menu, _t("Preference")) {
                val preferenceWindow = new PreferenceWindow
                preferenceWindow.show()
            }

            addMenuItem(menu, _t("Exit")){ MaidWindow.shell.dispose() }

            item.setImage(icon)
            item.setToolTipText(_t("MaidroidGTD"))
            item.addSelectionListener { e: SelectionEvent =>
                MaidWindow.toogleVisible()
                DialogWindow.toogleVisible()
            }

            item.addMenuDetectListener(new MenuDetectListener() {
                override def menuDetected(e: MenuDetectEvent) {
                    menu.setVisible(true)
                }
            })

        }

        !systemTray.isEmpty
    }

    def main (args: Array[String])
    {
        Display.setAppName("MaidroidGTD")

        println("==> Loading..")
        Splash.showSplash()
        println("==> Loading tray..")

        setupTrayItem() match {
            case true  =>
            case false =>
                MaidWindow.show()
                DialogWindow.show()
        }

        POP3Receiver.checkMailAtStart()
        POP3Receiver.hookEMailCheck()

        MaidWindow.doEventLoop()
    }
}

