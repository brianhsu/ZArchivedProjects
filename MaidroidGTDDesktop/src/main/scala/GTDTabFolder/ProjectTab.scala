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

import org.eclipse.swt.dnd._
import org.eclipse.swt.events._
import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.graphics._
import org.eclipse.swt.layout._

import org.eclipse.swt.SWT
import org.eclipse.swt.custom._

import java.text.SimpleDateFormat

import org.maidroid.gtd.model._
import Controller.gtd.Implicit._

import I18N.TR.{tr => _t}

class ProjectTab(tabFolder: TabFolder) extends GTDTabItem(tabFolder, _t("Project"), "/icons/projectTab.png") with SimpleSWT
{
    val display = Display.getDefault
    lazy val projectTree = controls('ProjectTree).asInstanceOf[Tree]

    lazy val projectIcon = new Image(display, getClass.getResourceAsStream("/icons/projectIcon.png"))
    lazy val doASAPIcon = new Image(Display.getDefault, getClass.getResourceAsStream("/icons/doASAP.png"))
    lazy val delegatedIcon = new Image(Display.getDefault, getClass.getResourceAsStream("/icons/delegated.png"))
    lazy val scheduledIcon = new Image(Display.getDefault, getClass.getResourceAsStream("/icons/scheduled.png"))

    lazy val reprocessButton = controls('ReprocessButton).asInstanceOf[ToolItem]
    lazy val addButton = controls('AddButton).asInstanceOf[ToolItem]
    lazy val removeButton = controls('RemoveButton).asInstanceOf[ToolItem]

    lazy val searchText = controls('SearchText).asInstanceOf[Text]

    def setupUILayout ()
    {
        val fillGrid = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1)
        val fillWidth = new GridData(SWT.FILL, SWT.NONE, false, false)
        val searchLayoutData = new GridData(SWT.RIGHT, SWT.CENTER, false, false)
        searchLayoutData.widthHint = 200

        tabItem.addComposite(composite(new GridLayout(2, false)) (
            toolbar(
                toolItem(icon = "/icons/add.png", toolTip = _t("Add"), id = 'AddButton)_ ::
                toolItem(icon = "/icons/remove.png", toolTip = _t("Remove"), id = 'RemoveButton) _ :: 
                toolItem(icon = "/icons/stuff.png", toolTip = _t("Reprocess"), id = 'ReprocessButton) _ ::
                Nil,
                layoutData = fillWidth
            ),
            
            text(
                _t("Search"), style = SWT.SINGLE|SWT.ICON_SEARCH|SWT.SEARCH|SWT.ICON_CANCEL,
                layoutData = searchLayoutData,
                id = 'SearchText
            ),

            tree(
                treeColumn(_t("Title"))_ :: treeColumn(_t("Topic"))_ :: treeColumn(_t("Vision"))_ :: Nil,
                style = SWT.CHECK, 
                layoutData = fillGrid,
                id = 'ProjectTree
            )
        ))

        projectTree.getColumns.foreach {_.setWidth(200)}
        projectTree.setHeaderVisible(true)
    }

    def setupDND()
    {
        val types = Array[org.eclipse.swt.dnd.Transfer](TextTransfer.getInstance())
        val operations = DND.DROP_MOVE;
        val source = new DragSource(projectTree, operations);
        var dragSourceItem:TreeItem = null

        source.setTransfer(types)
        source.addDragListener(new DragSourceListener() {
            def dragStart(event: DragSourceEvent) {
                val selection = projectTree.getSelection();
                if (selection.length > 0 && selection(0).getData("type") == "action") {
                    event.doit = true;
                    dragSourceItem = selection(0);
                } else {
                    event.doit = false;
                }
            }

            def dragSetData(event: DragSourceEvent) {
                event.data = dragSourceItem.getData.toString
            }

            def dragFinished(event: DragSourceEvent) {
                if (event.detail == DND.DROP_MOVE)
                    dragSourceItem.dispose();

                dragSourceItem = null;
            }
        })

        val target = new DropTarget(projectTree, operations)
        target.setTransfer(types);
        target.addDropListener(new DropTargetAdapter() {
            override def dragOver(event: DropTargetEvent) {
                event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
                if (event.item != null) {
                    val item = event.item.asInstanceOf[TreeItem];
                    val pt = display.map(null, projectTree, event.x, event.y);
                    val bounds = item.getBounds();
                    val isInRange = pt.y > bounds.y && pt.y < bounds.y + bounds.height
                    val isProject = item.getData("type") == "project"
                    val isParent = dragSourceItem.getParentItem == item

                    if (isInRange && isProject && !isParent) {
                        event.feedback |= DND.FEEDBACK_SELECT;
                    }
                }
            }

            override def drop(event: DropTargetEvent) {
                val item = event.item.asInstanceOf[TreeItem]
                val isParent = dragSourceItem.getParentItem == item

                if (item.getData("type") != "project" || isParent) {
                    event.detail = DND.DROP_NONE
                    return
                }

                val actionID = event.data.asInstanceOf[String].toInt
                val projectID = item.getData.asInstanceOf[Int]
                val action = Controller.gtd.Action.get(actionID)
                val project = Controller.gtd.Project.get(projectID)

                action.projects.foreach {action.removeProject}
                action.addToProject(project)

                addAction(action)

                action.isDone match {
                    case false =>
                        MainWindow.actionTab.updateAllActionTable(action)

                        action.actionType match {
                            case ActionType.DoASAP => MainWindow.actionTab.updateDoASAP(action)
                            case ActionType.Delegated => MainWindow.actionTab.updateDelegated(action)
                            case ActionType.Scheduled => MainWindow.actionTab.updateScheduled(action)
                        }

                    case true => 
                        MainWindow.actionTab.updateDone(action)
                }

                DialogWindow.setMessages(Maid.ProjectTabMessage.setActionToProject(project, action))
            }
        })
    }

    def popluateProject ()
    {
        def addAction(parent: TreeItem, action: Action)
        {
            val actionItem = new TreeItem(parent, SWT.NONE)
            val actionIcon = action.actionType match {
                case ActionType.DoASAP => doASAPIcon
                case ActionType.Delegated => delegatedIcon
                case ActionType.Scheduled => scheduledIcon
                case _ => doASAPIcon
            }

            actionItem.setText(action.title)
            actionItem.setImage(actionIcon)
            actionItem.setChecked(action.isDone)
            actionItem.setData("type", "action")
            actionItem.setData(action.actionID)
        }

        val projects = Controller.gtd.Project.list

        projects foreach { project =>

            val projectItem = addProject(project)

            project.actions.foreach {addAction(projectItem, _)}

        }
    }

    def updateAction(action: Action)
    {
        val actionItems = projectTree.getItems.flatMap(_.getItems).filter(_.getData == action.actionID)

        if (actionItems.length == 0) {
            addAction(action)
            return
        }

        actionItems.foreach { item =>
            
            val projectID = item.getParent.getData.asInstanceOf[Int]
            val isSameProject = action.projects.map(_.projectID) contains (projectID)

            isSameProject match {
                case true  =>
                    item.setText(action.title)
                    item.setChecked(action.isDone)
                case false =>
                    removeAction(action)
                    addAction(action)
            }
        }
    }

    def addProject(project: Project) = 
    {
        val projectItem = new TreeItem(projectTree, SWT.NONE)
        val title = project.title
        val topics = project.topics.map(_.name).mkString(",")
        val vision = project.vision.getOrElse("")

        projectItem.setText(Array(title, topics, vision))
        projectItem.setImage(projectIcon)
        projectItem.setData("type", "project")
        projectItem.setData(project.projectID)
        projectItem.setChecked(project.isDone)
        projectItem
    }

    def removeAction(action: Action)
    {
        projectTree.getItems.foreach { projectItem =>
            projectItem.getItems.filter(_.getData == action.actionID).foreach {_.dispose()}
        }
    }

    def setActionIsDone(action: Action, isDone: Boolean)
    {
        projectTree.getItems.flatMap(_.getItems).filter(_.getData == action.actionID).foreach {_.setChecked(isDone)}
    }

    def addAction(action: Action)
    {
        action.projects.foreach {addActionToProject(_, action)}
    }

    def updateProject(project: Project)
    {
        val title = project.title
        val topics = project.topics.map(_.name).mkString(",")
        val vision = project.vision.getOrElse("")

        projectTree.getItems.filter(_.getData == project.projectID).foreach { item =>
            item.setText(Array(title, topics, vision))
            item.setChecked(project.isDone)
        }
    }

    def addActionToProject(project: Project, action: Action)
    {
        projectTree.getItems.filter(_.getData == project.projectID).foreach { parent =>
            val actionItem = new TreeItem(parent, SWT.NONE)
            val actionIcon = action.actionType match {
                case ActionType.DoASAP => doASAPIcon
                case ActionType.Delegated => delegatedIcon
                case ActionType.Scheduled => scheduledIcon
                case _ => doASAPIcon
            }

            actionItem.setText(action.title)
            actionItem.setImage(actionIcon)
            actionItem.setChecked(action.isDone)
            actionItem.setData("type", "action")
            actionItem.setData(action.actionID)
            setProjectDone(parent, false)
        }
    }

    def setProjectDone(item: TreeItem, isDone: Boolean)
    {
        val project = Controller.gtd.Project.get(item.getData.asInstanceOf[Int])
        val updatedProject = project.copy(isDone = isDone)
        project.update(updatedProject)

        item.setChecked(isDone)
    }

    def getAllItems() = 
    {
        var allItems: List[TreeItem] = Nil

        for (projectItem <- projectTree.getItems) {
            allItems ::= projectItem
            projectItem.getItems.foreach { allItems ::= _ }
        }

        allItems.reverse
    }

    def findNext(target: String) = 
    {
        val allItems = getAllItems
        val currentIndex = projectTree.getSelection match {
            case Array() => -1
            case Array(item, _*) => allItems.indexOf(item)
        }

        val columnCount = projectTree.getColumnCount
        val (before, after) = allItems.splitAt(currentIndex + 1)
        
        val result = (after ++ before).find { item =>
            val allText = for (i <- 0 until columnCount) yield item.getText(i)
            allText.exists(x => x.contains(target))
        }

        result.foreach {projectTree.setSelection}
        !result.isEmpty
    }

    var afterCheck = false

    def setupUIListener ()
    {
        projectTree.addSelectionListener {event: SelectionEvent =>

            def checkProject(item: TreeItem)
            {
                val actionsState = item.getItems.map(_.getChecked)
                val couldMarkAsDone = !actionsState.contains(false)

                couldMarkAsDone match {
                    case true => 
                        setProjectDone(item, item.getChecked)

                        val messages = item.getChecked match {
                            case true  => Maid.ProjectTabMessage.markProjectAsDone
                            case false => Maid.ProjectTabMessage.unmarkProjectAsDone
                        }

                        DialogWindow.setMessages(messages)

                    case false =>
                        projectTree.setRedraw(false)
                        item.setChecked(!item.getChecked)
                        projectTree.setRedraw(true)
                        DialogWindow.setMessages(Maid.ProjectTabMessage.couldNotMarkProjectDone)
                }
            }

            def checkAction(item: TreeItem)
            {
                val action = Controller.gtd.Action.get(item.getData.asInstanceOf[Int])

                action.isDone match {
                    case false => 
                        val doneAction = action.markAsDone()
                        MainWindow.actionTab.removeAction(action)
                        MainWindow.actionTab.addToDone(doneAction)

                        DialogWindow.setMessages(Maid.ProjectTabMessage.markActionDone)

                    case true  =>
                        val updatedAction = action.copy(isDone = false, doneTimestamp = None)
                        action.update(updatedAction)
                        setProjectDone(item.getParentItem, false)
                        MainWindow.actionTab.removeAction(action)
                        MainWindow.actionTab.addAction(updatedAction)

                        DialogWindow.setMessages(Maid.ProjectTabMessage.unmarkActionDone)
                }
            }

            val treeItem = event.item.asInstanceOf[TreeItem]
            treeItem.getData ("type") match {
                case "project" if event.detail == SWT.CHECK => checkProject(treeItem) ; afterCheck = true
                case "action"  if event.detail == SWT.CHECK => checkAction(treeItem) ; afterCheck = true
                case "action"  if afterCheck => afterCheck = false
                case "project" if afterCheck => afterCheck = false
                case "action" => 
                    val actionID = treeItem.getData.asInstanceOf[Int]
                    val action = Controller.gtd.Action.get(actionID)
                    DialogWindow.setMessages(Maid.ProjectTabMessage.actionDetails(action))

                case "project" =>
                    val projectID = treeItem.getData.asInstanceOf[Int]
                    val project = Controller.gtd.Project.get(projectID)
                    DialogWindow.setMessages(Maid.ProjectTabMessage.projectDetails(project))
            }
        }

        addButton.addSelectionListener {e: SelectionEvent =>
            val dialog = new AddProjectDialog()
        }

        removeButton.addSelectionListener {event: SelectionEvent =>
            projectTree.getSelection.foreach { item =>
                val id = item.getData.asInstanceOf[Int]
                item.getData("type") match {
                    case "project" =>
                        val actions = Controller.gtd.Project.get(id).actions
                        Controller.gtd.Project.get(id).delete()

                        actions.foreach { action =>
                            MainWindow.actionTab.updateAllActionTable(action)
                            MainWindow.actionTab.updateDoASAP(action)
                            MainWindow.actionTab.updateDelegated(action)
                            MainWindow.actionTab.updateScheduled(action)
                        }

                    case "action" =>
                        val action = Controller.gtd.Action.get(id)
                        action.delete()
                        MainWindow.actionTab.removeAction(action)
                }

                DialogWindow.setMessages(Maid.ProjectTabMessage.deleted)
                item.dispose()
            }
        }

        reprocessButton.addSelectionListener {event: SelectionEvent =>
            val actionItems = projectTree.getSelection.filter(_.getData("type") == "action")
            actionItems.foreach { item =>
                val action = Controller.gtd.Action.get(item.getData.asInstanceOf[Int])
                val stuff = action.reprocess()
                removeAction(action)
                MainWindow.actionTab.removeAction(action)
                MainWindow.inboxTab.addToStuffTable(stuff)

                DialogWindow.setMessages(Maid.ProjectTabMessage.reprocess(stuff))
            }
        }

        projectTree.addMouseListener(new MouseAdapter() {
            override def mouseDoubleClick(e: MouseEvent) {
                projectTree.getSelection.foreach { item =>
                    item.getData("type") match {
                        case "project" =>
                            val project = Controller.gtd.Project.get(item.getData.asInstanceOf[Int])
                            val dialog = new EditProjectDialog(project)
                        case "action"  =>
                            val action = Controller.gtd.Action.get(item.getData.asInstanceOf[Int])
                            val dialog = new EditActionDialog(action)
                    }
                }
            }
        })

        searchText.addSelectionListener (new SelectionAdapter() {
            override def widgetDefaultSelected(e: SelectionEvent)
            {
                val target = searchText.getText

                if (e.detail != SWT.CANCEL && target != "") {
                    val isFound = findNext(target)
                    DialogWindow.setMessages(Maid.ProjectTabMessage.search(isFound))
                }
            }
        })

    }

    setupUILayout()
    setupUIListener()
    popluateProject()
    setupDND()
}

