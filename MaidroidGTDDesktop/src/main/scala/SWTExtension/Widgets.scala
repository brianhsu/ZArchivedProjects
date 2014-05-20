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

import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.SWT

import org.maidroid.gtd.controller._
import org.maidroid.gtd.model._

class TopicCombo(val combo: Combo)
{
    lazy val topicList = Controller.gtd.Topic.list

    def init () {
        Controller.gtd.Topic.list.foreach { topic => combo.add(topic.name) }
    }

    def createTopic (name: String) = topicList.filter(_.name == combo.getText) match {
        case Nil => Controller.gtd.Topic.add(name) :: Nil
        case xs  => xs
    }

    def setTopic (topic: Topic) = combo.setText(topic.name)

    def getTopic() = combo.getText.length match {
        case 0 => Nil
        case _ => createTopic(combo.getText)
    }
}

class ProjectCombo(val combo: Combo)
{
    lazy val projectList = Controller.gtd.Project.list

    def init () {
        Controller.gtd.Project.list.foreach { project => combo.add(project.title) }
    }

    def createProject (title: String) = projectList.filter(_.title == combo.getText) match {
        case Nil => 
            val project = Controller.gtd.Project.add(title) 
            MainWindow.projectTab.addProject(project)
            project :: Nil
        case xs  => xs
    }

    def setProject (project: Project) = combo.setText(project.title)

    def getProject() = combo.getText.length match {
        case 0 => Nil
        case _ => createProject(combo.getText)
    }
}

class ContextCombo(val combo: Combo)
{
    lazy val contextList = Controller.gtd.Context.list

    def init () {
        Controller.gtd.Context.list.foreach { context => combo.add(context.name) }
    }

    def createContext (name: String) = contextList.filter(_.name == combo.getText) match {
        case Nil => Controller.gtd.Context.add(name) :: Nil
        case xs  => xs
    }

    def setContext (context: Context) = combo.setText(context.name)

    def getContext() = combo.getText.length match {
        case 0 => Nil
        case _ => createContext(combo.getText)
    }

}

