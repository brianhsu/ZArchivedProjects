package org.bone

import scala.io.Source
import java.io.File

import org.eclipse.swt.layout._
import org.eclipse.swt.widgets._
import org.eclipse.swt._
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.graphics.GC
import org.eclipse.swt.graphics.Color

/**
 *  Linux ACPI Thermal 資訊物件
 *
 *  此物件會從 sysfs 檔案系統中讀取系統的溫度相關資訊
 *
 *  @param  sysFSDirPath    要讀取的 ACPI Thermal Zone 目錄
 */
class ACPIThermal(sysFSDirPath: String)
{
    private val sysFSDir = new File(if (sysFSDirPath.last == '/') sysFSDirPath.init else sysFSDirPath)
    private val temperatureFile = new File(sysFSDir + "/temp")

    // 檢查使用者指定的路徑是不是 Thermal Zone
    // 檢查項目：
    //  1. 該路徑是目錄
    //  2. 該目錄下有 temp 這個檔案
    require (sysFSDir.exists & sysFSDir.isDirectory, "%s is not directory" format(sysFSDir))
    require (temperatureFile.exists & temperatureFile.isFile, "%s is not exist" format(temperatureFile))

    // 根據 sysfs 的定義，溫度是以攝氏 1/1000 度做單位
    def temperature = Source.fromFile(temperatureFile).getLines().mkString.toInt / 1000
}

/**
 *  主程式
 *
 *  使用 SWT 來達成在 System Tray 下顯示系統溫度
 *
 */
object SThermal
{
    // 產生相關的 SWT 元件
    val display = new Display ()
    val shell = new Shell(display)
    val image = new Image(display, 16, 16)

    // 如果使用者所使用的桌面環境有 System Tray，才建立顯示溫度的元件
    //
    // Scala 裡的 Option[T] 是個神奇的東西，可以把他想像成一個盒子，
    // 有兩種狀態：
    //
    //  - 裡面沒東西（None）
    //  - 裡面有一個類型是 T 的東西（Some(T)）
    //
    val trayItem: Option[TrayItem] =  display.getSystemTray match {
        case null => None
        case systemTray => Some(new TrayItem(systemTray, SWT.NONE))
    }

    /**
     *  更新溫度顯示用的 Thread
     *
     *  @param  thermal         ACPI Thermal 資訊
     *  @param  updateInterval  多久更新一次，單位為 ms
     */
    class UpdateThread(thermal: ACPIThermal, updateInterval: Int) extends Thread 
    {
        def updateTrayImage() 
        {
            // 如果要在其他 Thread 來存取 SWT UI 元件，需要用這種方式
            display.syncExec (new Runnable() {

                // 很不幸的，目前 TrayItem 只能顯示 Image，我們要自己畫
                val image = new Image(display, 16, 16)

                def run() {

                    // 在 Image 上畫上溫度
                    val gc = new GC(image)
                    gc.setForeground(display.getSystemColor(SWT.COLOR_BLUE))
                    gc.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND))
                    gc.drawText (thermal.temperature.toString, 0, 0)

                    // 如果 trayItem 裡面有東西，就做 {} 裡面的事，將
                    // trayItem 所使用的圖型設成上面的 image
                    trayItem.foreach { _.setImage(image) }
                }
            })
        }

        // 沒什麼特別的，每隔 updateInterval ms 後就更新
        override def run() {
            while (true) {
                updateTrayImage()
                Thread.sleep(updateInterval)
            }
        }
    }

    def main (args: Array[String]) 
    {
        // 檢查使用者所提供的參數是否足夠
        if (args.length != 2) {
            println ("Usage: java -jar [JARFile] [Thermal ACPI sysfs dir] [Update Interval]")
            exit(0)
        }

        // 設定參數
        val thermalPath = args(0)
        val updateInterval = args(1).toInt

        val thermal = new ACPIThermal(args(0))
        val updateThread = new UpdateThread(thermal, updateInterval)

        updateThread.start()

        // 標準的 SWT 的寫法
        while (!shell.isDisposed ()) {
            if (!display.readAndDispatch ()) display.sleep ();
        }

        display.dispose ();
    }
}
