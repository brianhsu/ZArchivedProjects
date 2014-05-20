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

import org.maidroid.gtd.model._
import java.text.SimpleDateFormat

import I18N.TR.{tr => _t}
import Controller.gtd.Implicit._


object DialogWindow extends SimpleSWT
{
    val display = Display.getDefault
    val shell = new Shell(display, SWT.NO_TRIM|SWT.ON_TOP)

    lazy val image = new ExtendedImage("/dialog.png")
    lazy val label = new Label(shell, SWT.LEFT|SWT.WRAP)
    lazy val continueMark = new Label(shell, SWT.CENTER)

    var messages: List[Message] = Nil

    def toogleVisible()
    {
        shell.isVisible match {
            case false => show()
            case true  => hide()
        }
    }

    def setupUILayout ()
    {
        val layout = new GridLayout

        layout.marginTop = 10;
        layout.marginLeft = 10;
        layout.marginRight = 25;
        layout.marginBottom = 20;

        shell.setBackgroundMode(SWT.INHERIT_DEFAULT)
        shell.setRegion(image.region)
        shell.setBackgroundImage(image)
        shell.setSize(350, 150)
        shell.setLayout(layout)
        shell.setLocation(calculateLocation)
        label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true))

        continueMark.setLayoutData(new GridData(SWT.FILL, SWT.NONE, false, false))

        /*
        val fontData = label.getFont().getFontData()
        fontData(0).setHeight(16)
        label.setFont(new Font(display, fontData(0)))
        */
    }

    def calculateLocation() = {
        val maidPosition = MaidWindow.shell.getLocation
        val size = shell.getSize
        val x = maidPosition.x - size.x - 10
        val y = maidPosition.y + MaidWindow.shell.getSize.y - size.y
        new Point(x, y)
    }

    def setupListener () {
        label.addMouseListener(new MouseAdapter() {
            override def mouseDown (e: MouseEvent) {
                if (e.button == 1 && e.count == 1) {
                    showNextMessage()
                }
            }
        })
    }

    def setMessages(messages: List[Message]) = messages match {
        case Nil =>
        case _   => this.messages = messages
                    showNextMessage()
    }

    def showNextMessage () = messages match {
        case Nil => 
        case msg :: Nil => 
            MaidWindow.setImage(msg.image)
            label.setText(msg.text)
            continueMark.setText("■")
            messages = messages.tail

        case msg :: xs =>
            MaidWindow.setImage(msg.image)
            label.setText(msg.text)
            continueMark.setText("▼")
            messages = messages.tail
    }

    def hide() = {
        shell.setVisible(false)
    }

    def show() = {
        shell.open()
    }

    setupUILayout ()
    setupListener ()
    setMessages(Maid.startMessage)
}

object MaidWindow extends SimpleSWT
{
    val display = Display.getDefault
    val shell = new Shell(display, SWT.NO_TRIM|SWT.ON_TOP)
    var image = Maid.smile

    def toogleVisible()
    {
        shell.isVisible match {
            case false => show()
            case true  => hide()
        }
    }

    def setupListener ()
    {
        shell.addMouseListener(new MouseAdapter{
            override def mouseUp(e: MouseEvent) {
                if (e.button == 1 && e.count == 1) {
                    MainWindow.isOpen match {
                        case true  => MainWindow.hide()
                        case false => MainWindow.show()
                    }
                }
            }
        })
    }

    def setImage(image: ExtendedImage)
    {
        this.image = image
        shell.setRegion(image.region)
        shell.setBackgroundImage(image.image)
    }

    def setupPopMenu ()
    {
        val menu = new Menu (shell, SWT.POP_UP);
        val hideItem = new MenuItem (menu, SWT.PUSH);
        val exitItem = new MenuItem (menu, SWT.PUSH);

        hideItem.setText(_t("Show/Hide"))
        hideItem.addSelectionListener { e: SelectionEvent =>
            this.toogleVisible()
            DialogWindow.toogleVisible()
        }

        exitItem.setText("Exit")
        exitItem.addSelectionListener { e: SelectionEvent =>
            shell.close()
        }

        shell.setMenu(menu)
    }

    def setupUILayout ()
    {
        shell.setText("MaidroidGTD")
        shell.setRegion(image.region)
        shell.setSize(image.imageData.width, image.imageData.height)
        shell.setImage(new Image(display, getClass.getResourceAsStream("/icons/maid.png")))
    }

    def calculateLocation() = {
        val clientArea = display.getPrimaryMonitor.getBounds
        val size = shell.getSize
        val x = clientArea.width - size.x
        val y = clientArea.height - size.y

        new Point(x, y)
    }

    def hide ()
    {
        shell.setVisible(false)
    }

    def show ()
    {
        shell.setLocation (calculateLocation())
        shell.open ()
    }

    def doEventLoop ()
    {
        while (!shell.isDisposed) {
            if (!display.readAndDispatch ()) display.sleep ();
        }

        display.dispose ()
    }

    setupUILayout()
    setupListener()
    setupPopMenu()
}
