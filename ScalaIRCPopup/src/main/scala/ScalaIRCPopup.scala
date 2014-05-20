import org.eclipse.swt.SWT
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.widgets.{List => _, _}
import org.jibble.pircbot._

object ScalaIRCPopup
{
    type IRCCallback = (String, String, String, String, String) => Any
    class IRCBot (callback: IRCCallback) extends PircBot 
    {
        setName ("brianhsu_bot")
        
        override def onMessage (channel: String, sender: String,
                                login: String, hostname: String,
                                message: String)
        {
            callback (channel, sender, login, hostname, message)
        }
    }

    implicit def runnableFromFun (func: () => Any) = new Runnable {
        override def run (){func()}
    }

    val display = new Display()
    val shell   = new Shell(display)

    val tooltip = {
        val tray    = display.getSystemTray()
        val item    = new TrayItem (tray, SWT.NONE);
        val image   = new Image    (display, "icon.jpg");
        val tooltip = new ToolTip  (shell, SWT.BALLOON);

        item.setImage(image);
        item.setToolTip(tooltip);

        tooltip
    }

    def setupBot (channels: List[String])
    {
        channels.foreach {parm =>
            val List(server, channel) = parm.split("\\|").toList

            val bot = new IRCBot ((channel, sender, 
                                   login, hostname, message) => {
                val popup = "[%s]\n%s:%s".format (channel, sender, message)
                setTooltip (popup)
            })

            bot.setVerbose (true)
            bot.connect (server)
            bot.changeNick ("BrianHsu_BOT")

            bot.joinChannel("#" + channel)
        }
    }

    def setTooltip (message: String)
    {
        display.syncExec { () =>
            tooltip.setVisible (false)
            tooltip.setText(message);
            tooltip.setVisible (true)
        }
    }

    def main (args: Array[String]) 
    {
        setupBot (args.toList)

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep()
            }
        }
    
        display.dispose()
    }
}
