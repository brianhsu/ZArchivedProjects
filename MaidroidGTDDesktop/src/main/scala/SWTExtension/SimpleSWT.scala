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

class ExtendedImage(val filename: String)
{
    lazy val image = new Image(Display.getDefault, getClass.getResourceAsStream(filename))
    lazy val imageData = image.getImageData

    lazy val regionFile = getClass.getResourceAsStream(filename + ".region")
    lazy val region = calculateRegion()

    private def calculateRegion() = {
        print("Cal region ")
        val t1 = System.nanoTime();
        val region = new Region
        val pixel = new Rectangle(0, 0, 1, 1)

        for (x <- 0 until imageData.width; y <- 0 until imageData.height) {
            if (imageData.getAlpha(x, y) >= 200) {
                pixel.x = imageData.x + x
                pixel.y = imageData.y + y
                region.add(pixel)
            }
        }

        val t2 = System.nanoTime();

        println("done.." + (t2-t1)/1000.0/1000.0)
        region
    }
}

trait SimpleSWT
{
    import scala.collection.mutable.HashMap
    type ControlBuilder = (Composite) => Any

    val controls = HashMap[Symbol, Any]()

    implicit def toImage(extImage: ExtendedImage) = extImage.image
    implicit def toAdvancedText(text: Text) = new AdvancedText(text)
    implicit def toAdvancedTableColumn(column: TableColumn) = new AdvancedTableColumn(column)
    implicit def toAdvancedTable(table: Table) = new AdvancedTable(table)
    implicit def toAdvancedTabItem(tabItem: TabItem) = new AdvancedTabItem(tabItem)
    implicit def toAdvancedComposite(composite: Composite) = new AdvancedComposite(composite)
    implicit def toSelectionAdapter(action: SelectionEvent => Any) = new SelectionAdapter() {
        override def widgetSelected (event: SelectionEvent) {
            action(event)
        }
    }
    implicit def toModifiyListener(action: ModifyEvent => Any) = new ModifyListener () {
        override def modifyText(event: ModifyEvent) {
            action(event)
        }
    }

    implicit def toFocusListener(action: FocusEvent => Any) =
    {
        new FocusAdapter() {
            override def focusGained(e: FocusEvent) = action(e)
        }
    }

    class AdvancedTableColumn(tableColumn: TableColumn)
    {
        type Sort = (Table, TableColumn, Int) => Any

        def sortByText(index: Int, table: Table, column: TableColumn, direction: Int)
        {
            val sortedItem = table.getItems.sortWith { 
                direction match {
                    case SWT.DOWN => _.getText(index) < _.getText(index)
                    case SWT.UP => _.getText(index) > _.getText(index)
                }
            }

            sortedItem.foreach { x =>
                val item = new TableItem(table, SWT.NONE)
                for (i <- 0 until table.getColumnCount) {
                    item.setText(i, x.getText(i))
                    item.setImage(i, x.getImage(i))
                    item.setData(x.getData)
                    item.setChecked(x.getChecked)
                }
            }

            sortedItem.foreach {_.dispose()}
        }

        def setSorting(columnIndex: Int)
        {
            setSorting(sortByText(columnIndex, _, _, _))
        }

        def setSorting(action: Sort)
        {
            val table = tableColumn.getParent
            tableColumn.addSelectionListener {e: SelectionEvent =>
                table.setSortColumn(tableColumn)
                val direction = table.getSortDirection match {
                    case SWT.NONE => SWT.DOWN
                    case SWT.DOWN => SWT.UP
                    case SWT.UP   => SWT.DOWN
                }
                table.setSortDirection(direction)
                action(table, tableColumn, direction)
            }

        }
    }

    class AdvancedText (text: Text)
    {
        def getOptionText() = text.getText.length match {
            case 0 => None
            case _ => Some(text.getText)
        }
    }

    class AdvancedTable (table: Table)
    {
        def removeByData (data: Any)
        {
            val indices = table.getItems.zipWithIndex.filter(_._1.getData == data).map(_._2)

            indices.foreach {table.remove(_)}
        }

        def findNext(target: String) = 
        {
            val currentSelection = table.getSelectionIndex
            val columnCount = table.getColumnCount
            val (before, after) = table.getItems.splitAt(currentSelection+1)
    
            val result = (after ++ before).find { item =>
                val allText = for (i <- 0 until columnCount) yield item.getText(i)
                allText.exists(x => x.contains(target))
            }
    
            result.foreach {table.setSelection}
            !result.isEmpty
        }

    }

    class AdvancedTabItem (tabItem: TabItem)
    {
        def addComposite (layout: Composite => Any) = {
            val composite = new Composite(tabItem.getParent, SWT.NONE)
            composite.setLayout(new FillLayout)
            layout(composite)
            tabItem.setControl(composite)
        }
    }

    class AdvancedComposite(composite: Composite)
    {
        def addComposite (layout: Composite => Any) = {
            composite.setLayout(new FillLayout)
            layout(composite)
        }
    }

    def setupWidget[T <% Widget] (control: T, id: Symbol, init: T => Any)
    {
        if (id != null) controls.put(id, control)
        if (init != null) init(control)
    }

    def setupControl[T <% Control] (control: T, layoutData: AnyRef, id: Symbol, init: T => Any)
    {
        setupWidget(control, id, init)
        if (layoutData != null)  control.setLayoutData(layoutData)
    }

    def label(title: String, style: Int = SWT.NONE, layoutData: AnyRef = null, id: Symbol = null, 
              init: Label => Any = null)(parent: Composite)
    {
        val label = new Label(parent, style)
        label.setText(title)
        setupControl (label, layoutData, id, init)
    }

    def text(message: String, style: Int = SWT.BORDER, layoutData: AnyRef = null, id: Symbol = null,
             init: Text => Any = null)(parent: Composite)
    {
        val text = new Text(parent, style)
        text.setMessage(message)
        setupControl (text, layoutData, id, init)
    }

    def progressBar(style: Int = SWT.SMOOTH, layoutData: AnyRef = null, id: Symbol = null, init: ProgressBar => Any = null)(parent: Composite)
    {
        val progressBar = new ProgressBar(parent, style)

        setupControl (progressBar, layoutData, id, init)

    }

    def combo(style: Int = SWT.DROP_DOWN, layoutData: AnyRef = null, id: Symbol = null, init: Combo => Any = null)
             (parent: Composite)
    {
        val combo = new Combo(parent, style)

        setupControl (combo, layoutData, id, init)
    }

    def spinner(layoutData: AnyRef = null, id: Symbol = null, init: Spinner => Any = null)(parent: Composite)
    {
        val spinner = new Spinner(parent, SWT.NONE)
        spinner.setMaximum(1500)
        setupControl (spinner, layoutData, id, init)
    }

    def button(text: String, style: Int = SWT.PUSH, icon: String = null, layoutData: AnyRef = null, id: Symbol = null,
               init: Button => Any = null)(parent: Composite)
    {
        val button = new Button(parent, style)
        button.setText(text)

        if (icon != null) button.setImage(new Image(Display.getDefault, getClass.getResourceAsStream(icon)))
        setupControl (button, layoutData, id, init)
    }

    def dateTime(style: Int = SWT.DATE, layoutData: AnyRef = null, id: Symbol = null,
               init: DateTime => Any = null)(parent: Composite)
    {
        val dateTime = new DateTime(parent, style)

        setupControl (dateTime, layoutData, id, init)
    }

    def toolItem(text: String = null, style: Int = SWT.PUSH, icon: String = null, toolTip: String = null, id: Symbol = null, init: ToolItem => Any = null)(parent: ToolBar) {
        val toolItem = new ToolItem(parent, style)

        if (text != null) toolItem.setText(text)
        if (icon != null) toolItem.setImage(new Image(Display.getDefault, getClass.getResourceAsStream(icon)))
        if (toolTip != null) toolItem.setToolTipText(toolTip)

        setupWidget (toolItem, id, init)
    }

    def toolbar(items: List[ToolBar => Any], layoutData: AnyRef = null, id: Symbol = null)(parent: Composite) {
        val toolbar = new ToolBar(parent, SWT.NONE)

        items.foreach { builder => builder(toolbar) }

        setupControl (toolbar, layoutData, id, null)
    }

    def treeColumn(title: String, style: Int = SWT.LEFT, id: Symbol = null, init: TreeColumn => Any = null)(parent: Tree) {
        val column = new TreeColumn(parent, style)
        column.setText(title)
        column.setResizable(true)
        setupWidget(column, id, init)
    }

    def column(title: String, style: Int = SWT.LEFT, id: Symbol = null, init: TableColumn => Any = null)(parent: Table) {
        val column = new TableColumn(parent, style)
        column.setText(title)
        column.setResizable(true)
        setupWidget(column, id, init)
    }

    def tree(columns: List[Tree => Any] = Nil, style: Int = SWT.FULL_SELECTION, layoutData: AnyRef = null, 
             id: Symbol = null, init: Tree => Any = null)(parent: Composite)
    {
        val tree = new Tree(parent, style)
        columns.foreach { builder => builder(tree) }
        setupControl(tree, layoutData, id, init)
    }

    def table(columns: List[Table => Any], style: Int = SWT.FULL_SELECTION, 
              isHeaderVisible: Boolean = true, isLinesVisible: Boolean = true, 
              layoutData: AnyRef = null, id: Symbol = null, init: Table => Any = null)(parent: Composite) 
    {
        val table = new Table(parent, style)
        
        table.setHeaderVisible(isHeaderVisible)
        table.setLinesVisible(isLinesVisible)

        columns.foreach { builder => builder(table) }
        table.getColumns.foreach {_.pack()}
        setupControl (table, layoutData, id, init)
    }

    def tabFolder(tabItems: List[TabFolder => Any], style: Int = SWT.TOP, layoutData: AnyRef = null, id: Symbol = null, 
                  init: TabFolder => Any = null)(parent: Composite)
    {
        val tabFolder = new TabFolder(parent, style)

        tabItems.foreach { builder => builder(tabFolder) }
        setupControl (tabFolder, layoutData, id, init)
    }

    def sashForm (style: Int = SWT.VERTICAL, layoutData: AnyRef = null, id: Symbol = null, 
                  init: SashForm => Any = null)(controlBuilders: ControlBuilder*)(parent: Composite)
    {
        val sashForm = new SashForm(parent, style)

        controlBuilders.foreach { builder => builder(sashForm) }

        setupControl(sashForm, layoutData, id, init);
    }


    def group(text: String, style: Int = SWT.SHADOW_ETCHED_IN, layout: Layout = new FillLayout, layoutData: AnyRef = null, id: Symbol = null, init: Group => Any = null)(controlBuilders: ControlBuilder*)(parent: Composite)
    {
        val group = new Group(parent, style)

        group.setText(text)
        group.setLayout(layout)

        controlBuilders.foreach { builder => builder(group) }
        setupControl(group, layoutData, id, init);
    }

    def composite(layout: Layout, layoutData: AnyRef = null, id: Symbol = null)
                 (controlBuilders: ControlBuilder*)(parent: Composite) =
    {
        val composite = new Composite(parent, SWT.NONE)
        composite.setLayout(layout)

        controlBuilders.foreach { builder => builder(composite) }

        if (layoutData != null) {
            composite.setLayoutData(layoutData)
        }

        if (id != null) controls.put(id, composite)
    }

}

