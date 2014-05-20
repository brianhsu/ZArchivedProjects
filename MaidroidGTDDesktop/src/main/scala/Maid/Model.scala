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

import org.eclipse.swt.graphics._
import org.eclipse.swt.widgets.{List => SWTList, _}

import org.eclipse.swt.SWT

import org.maidroid.gtd.model._

import I18N.TR.{tr => _t}
import Controller.gtd.Implicit._
import java.text.SimpleDateFormat

case class Message(text: String, image: ExtendedImage)

trait MaidBase
{
    val display = Display.getDefault

    lazy val smile = new ExtendedImage("/maid/maid01.png")
    lazy val happy = new ExtendedImage("/maid/maid02.png")
    lazy val panic = new ExtendedImage("/maid/maid03.png")
    lazy val shy   = new ExtendedImage("/maid/maid04.png")
    lazy val angry = new ExtendedImage("/maid/maid05.png")
}

object Maid extends MaidBase
{
    val dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm")

    object TabMessage {
        def overview = {
            Message(_t("Here's the overview the master's work and some tips about GTD."), Maid.happy) ::
            Message(_t("If master feels hard to make your mind about something, you could switch back to this page and think about it again."), Maid.smile) :: 
            Nil
        }

        def inbox(stuffs: List[Stuff]) = {
            Message(_t("There are about %d items that master have not decide how to handle it.").format(stuffs.length), Maid.shy) ::
            Message(_t("I think it is great if master could find sometime and make descision about these item."), Maid.smile) ::
            Message(_t("If there is anything that need to be noted, please click the plus button on the tool bar."), Maid.happy) :: 
            Nil
        }

        def action = {
            Message(_t("Master, here is the stuff that is actionable or already done."), Maid.smile) ::
            Message(_t("Working hard is good, but please take care of yourself, my master."), Maid.shy) ::
            Message(_t("I will be sad if master is sick beacuse of hard working."), Maid.shy) ::
            Nil
        }

        def maybe = {
            Message(_t("Here is things that master does not make up your mind yet."), Maid.smile) ::
            Message(_t("It is really hard to make a decision sometimes, but I think master should review these thing if you have time."), Maid.shy) ::
            Message(_t("So...sorry! I should not criticize master..."), Maid.panic) ::
            Nil
        }

        def reference = {
            Message(_t("Does master want to get some reference file?"), Maid.panic) ::
            Message(_t("Master could tell me what you need to find, I will search it for master."), Maid.shy) ::
            Message(_t("Because I'm more familiar with how reference is filed than master."), Maid.happy) ::
            Nil
        }

        def project = {
            Message(_t("Here is the project master currently has..."), Maid.shy) ::
            Message(_t("Just for your information, master could drag and drop the actions to rearrange the project of actions."), Maid.smile) ::
            Nil
        }
    }

    object MainWindowMessage {
        def opened = Message(_t("Yes, may I help you?"), Maid.panic) :: Nil
    }

    def startMessage = {
        Message(_t("りなさいませ、ご主人さま。Master could click the dialog to continue."), Maid.happy) :: 
        Message(_t("If master wish me to do something, please click on me."), Maid.smile) :: 
        Message(_t("Yes, may I help you?"), Maid.shy) :: 
        Nil
    }

    def mailErrorMessage(error: Throwable) = {
        Message(_t("Master, I could not retrieve the mail from server..."), Maid.panic) ::
        Message(_t("Here is the problem:\n") + error.getMessage, Maid.shy) ::
        Nil
    }

    object ActionDialogMessage {
        def opened = Message(_t("Does master want to edit this action? Please feel free to tell me."), Maid.happy) :: Nil
        def cancel = Message(_t("Cancel it? OK, I got it."), Maid.smile) :: Nil
        def updated = Message(_t("OK, it is updated!"), Maid.smile) :: Nil
    }

    object MaybeDialogMessage {

        def enableTickle = {
            Message(_t("OK, when should I put this item back into Inbox, My master?"), Maid.smile) :: 
            Nil
        }

        def disableTickle = Message(_t("There is no need to remind master about this item? OK, I got it."), Maid.panic) :: Nil
        def opened = Message(_t("Does master want to edit this item? I got it."), Maid.happy) :: Nil
        def cancel = Message(_t("Master want to remain this item? OK, as your wish."), Maid.happy) :: Nil
        def updated = {
            Message(_t("It is already updated..."), Maid.shy) ::
            Message(_t("Master could check it again."), Maid.smile) ::
            Nil
        }
    }

    object ProcessDialogMessage {
        def actionableOrNot(title: String) = {
            Message(_t("My master does '%s' could be achieved by some real action?") format(title), Maid.smile) ::
            Message(_t("If it is actionable, could be delegated to someone else or is already scheduled, please select 'It is Actionable'"), Maid.happy) ::
            Message(_t("If this item could not take a action, please select 'It is not actionable'."), Maid.smile) ::
            Nil
        }

        def whatActionType = {
            Message(_t("Excuse me, what is the expected outcome of this item? And what to do next, my master?"), Maid.shy) ::
            Message(_t("Just for your information, you could also delegeted this item to someone else."), Maid.smile) ::
            Message(_t("If this is a appointment, please selected 'Scheduled'"), Maid.smile) ::
            Nil
        }

        def notActionable = {
            Message(_t("This item is not actionable? OK, I got it."), Maid.shy) ::
            Message(_t("Please tell me should I delete it or keep it as reference."), Maid.smile) ::
            Message(_t("If this item could become an action later, please select 'Someday/Maybe'."), Maid.happy) ::
            Nil
        }

        def maybe = {
            Message(_t("If master want me to remind you about this item later, please free to tell me."), Maid.happy) ::
            Message(_t("Just check the 'Tickle me' box, and set the date, I will put this item back into Inbox when the date is arrived."), Maid.smile) ::
            Nil
        }

        def doASAP = {
            Message(_t("Master, if this action could be done in two minutes, it is better to do it right now."), Maid.happy) ::
            Message(_t("If this action could not be done in two minutes, you could defer it."), Maid.smile) ::
            Nil
        }

        def delegated = {
            Message(_t("Please tell me who to delegate this action."), Maid.happy) ::
            Message(_t("Master, please rember that delegated action need to follow up."), Maid.smile) ::
            Nil
        }

        def scheduled = {
            Message(_t("This item has been scheduled?"), Maid.panic) ::
            Message(_t("Please tell me specific date time, I would remind you when the time is arrived, my master."), Maid.smile) ::
            Nil
        }
    }

    object ReferenceDialogMessage {
        def opened = {
            Message(_t("Need to update this reference?"), Maid.happy) ::
            Message(_t("Please feel free to tell me."), Maid.smile) ::
            Nil
        }

        def updated = Message(_t("OK, I've updated content of this reference."), Maid.happy) :: Nil
        def cancel = Message(_t("Master want to keep the origin version? I got it."), Maid.shy) :: Nil
    }

    object ProjectDialogMessage {
        def vision = {
            Message(_t("Vision means the final goal of this project."), Maid.smile) ::
            Message(_t("Because there is a vision, so we could know what to looking after!"), Maid.happy) ::
            Nil
        }

        def cancel = Message(_t("Does master wish to cancel it? OK, I will do that for you."), Maid.shy) :: Nil
        def addOpened = {
            Message(_t("Ahh... Please tell me what project master want to add."), Maid.happy) ::
            Message(_t("Project is used to keep track a group of action."), Maid.smile) ::
            Message(_t("You could assign actions to project, and track the status of actions under it."), Maid.smile) ::
            Nil
        }

        def updateOpened = {
            Message(_t("Is this project need to be updated?"), Maid.shy) ::
            Message(_t("If master have any request, please feel free to tell me."), Maid.smile) ::
            Nil
        }

        def projectAdded(title: String) = {
            Message(_t("Master, I've already add '%s' to the project list.") format(title), Maid.panic) ::
            Message(_t("You may assign the corresponding project when adding "), Maid.smile) ::
            Nil
        }

        def projectUpdated = {
            Message(_t("I've already updated this project for you. Master could check it out in the list."), Maid.smile) ::
            Nil
        }
    }

    object StuffDialogMessage {
        def titleFocus = {
            Message(_t("My master, this field is required. So we could know what it is."), Maid.happy) ::
            Message(_t("If there is any request, please feel free to tell me."), Maid.smile) ::
            Nil
        }

        def topicFocus = {
            Message(_t("Master, what topic does this item belong?"), Maid.shy) ::
            Message(_t("If there is no apporitate topic in the list, you could enter new topic directly."), Maid.smile) ::
            Nil
        }

        def descriptionFocus = {
            Message(_t("If there is something need to be noted, please tell me using this field."), Maid.smile) :: Nil
        }

        def added = {
            Message(_t("OK, I've remembered this item, my master."), Maid.happy) ::
            Message(_t("Please remember to determine what to do about this item if you have time, my master."), Maid.smile) ::
            Nil
        }

        def cancel = {
            Message(_t("OK, I got it."), Maid.shy) ::
            Message(_t("If master have any other request, please feel free to tell me."), Maid.smile) ::
            Nil
        }

        def updated = Message(_t("I've updated the status of this item, my master."), Maid.smile) :: Nil
    }

    object ActionTabMessage {

        def doASAPDetail(action: Action) = {

            val description = action.description match {
                case None       => _t("It seems there is no notes...")
                case Some(text) => text
            }

            Message(_t("Master, '%s' should be done as soon as possible.") format(action.title), Maid.smile) ::
            Message(_t("Here is the description:\n\n") + description, Maid.shy) ::
            Nil
        }

        def mailClientError = {
            Message(_t("Excuse me, my master"), Maid.panic) ::
            Message(_t("I could not start mail client, is the setting correct?"), Maid.shy) ::
            Nil
        }

        def noMailClient = {
            Message(_t("It seems master does not set EMail client yet..."), Maid.shy) ::
            Message(_t("Master could set it in Preference window."), Maid.smile) ::
            Nil
        }

        def delegatedDetail(action: Action) = {
            val delegatedInfo = action.delegatedInfo.get

            Message(_t("My master, action '%s' has been delegated to %s.") format(action.title, delegatedInfo.name), Maid.smile) ::
            Message(_t("Delegated:%s\nE-Mail:%s\nDescription:\n\n%s") format(delegatedInfo.name, delegatedInfo.email.getOrElse(""), action.description.getOrElse("")), Maid.shy) ::
            Nil
        }
        
        def scheduledDetail(action: Action) = {
            val scheduledInfo = action.scheduledInfo.get
            val startTime = dateFormatter.format(scheduledInfo.startTime)
            val location = scheduledInfo.location.getOrElse("")
            val description = action.description.getOrElse("")

            Message(_t("Master, action '%s' has been scheduled at %s, please don't forget.") format(action.title, startTime), Maid.smile) ::
            Message(_t("Location:%s\nDescription:\n%s") format(location, description), Maid.shy) ::
            Nil
        }

        def markAsDone = {
            Message(_t("This action has been done? OK, I will mark it as done."), Maid.happy) ::
            Message(_t("Master could be the 'Done' tab to check it out."), Maid.smile) ::
            Nil
        }

        def deleted = {
            Message(_t("Does master want to delete this action?"), Maid.panic) ::
            Message(_t("OK, it has been deleted."), Maid.smile) ::
            Nil
        }

        def reprocess(stuff: Stuff) = {
            Message(_t("Does master want to reprocess '%s'?") format(stuff.title), Maid.panic) ::
            Message(_t("As your wish, I will put it into inbox, master could process it later."), Maid.happy) ::
            Nil
        }

        def unmarkDone = {
            Message(_t("This action need to mark as not done again?"), Maid.panic) ::
            Message(_t("OK, I got it. If there is any request, please feel free to tell me."), Maid.shy) ::
            Nil
        }

        def search(isFound: Boolean) = isFound match {
            case true =>
                Message(_t("Are you searching for this? If not, please hit enter to show next item that is matched."), Maid.smile) :: Nil

            case false =>
                Message(_t("Master, excuse me, it seems there is no matched item..."), Maid.shy) :: Nil
        }

        def mailDelegate = Message(_t("Master want mail to the delegated person? Please wait a minute..."), Maid.smile) :: Nil
        def noDelegatedMail = Message(_t("Exceuse me, my master...It seems there is no e-mail address."), Maid.shy) :: Nil
        def followUp = {
            Message(_t("OK, I've updated the follow-up time."), Maid.happy) ::
            Message(_t("Please remember keep track of this item until it is done."), Maid.smile) ::
            Nil
        }
        def notDelegated = {
            Message(_t("Excuse me, my master."), Maid.shy) ::
            Message(_t("You could only set follow-up time for delegated item."), Maid.shy) ::
            Nil
        }
    }

    object InboxTabMessage {

        def deleted(stuff: Stuff) = {
            Message(_t("I've deleted '%s', my master.") format(stuff.title), Maid.smile) :: Nil
        }

        def detail(stuff: Stuff) = {
            val description = stuff.description match {
                case Some(text) => _t("Master said:\n\n") + text
                case None       => _t("It seems there is no notes...")
            }

            Message(_t("About '%s'? Please wait a minute...").format(stuff.title), Maid.smile) ::
            Message(description, Maid.shy) ::
            Nil
        }

        def search(isFound: Boolean) = isFound match {
            case true =>
                Message(_t("Are you searching for this? If not, please hit enter to show next item that is matched."), Maid.smile) :: Nil

            case false =>
                Message(_t("Master, excuse me, it seems there is no matched item..."), Maid.shy) :: Nil
        }

        def checkMail = {
                Message(_t("Master want to check mail box? OK, I got it."), Maid.smile) ::
                Message(_t("I will put the mail into inbox when it is ready, master could do something else when I process these mails."), Maid.happy) ::
                Nil
        }
    }

    object MaybeTabMessage {
        def details(maybe: Maybe) = {

            val description = maybe.description match {
                case Some(text) => _t("Master said:\n\n") + text
                case None       => _t("It seems there is no notes...")
            }

            Message(_t("About '%s'? Let me see...") format(maybe.title), Maid.smile) ::
            Message(description, Maid.shy) ::
            Nil
        }

        def deleted(maybe: Maybe) = {
            Message(_t("OK, I've deleted '%s' for you, my master.") format(maybe.title), Maid.smile) :: Nil
        }

        def reprocess(maybe: Maybe) = {
            Message(_t("Does master want to reprocess '%s'?") format(maybe.title), Maid.panic) ::
            Message(_t("As your wish, I will put it into inbox, master could process it later."), Maid.happy) ::
            Nil
        }

        def search(isFound: Boolean) = isFound match {
            case true =>
                Message(_t("Are you searching for this? If not, please hit enter to show next item that is matched."), Maid.smile) :: Nil

            case false =>
                Message(_t("Master, excuse me, it seems there is no matched item..."), Maid.shy) :: Nil
        }
    }

    object OverviewTabMessage {
        def scheduledAction(action: Action) = {
            val scheduledInfo = action.scheduledInfo.get
            val startTime = dateFormatter.format(scheduledInfo.startTime)

            Message(_t("Master, action '%s' has been scheduled at %s, please don't forget.") format(action.title, startTime), Maid.smile) ::
            Message(_t("Location:%s\nDescription:\n%s") format(scheduledInfo.location.getOrElse(""), action.description.getOrElse("")), Maid.shy) ::
            Nil
        }

    }

    object ProjectTabMessage {
        def setActionToProject(project: Project, action: Action) = {
            Message(_t("OK, I've add action '%s' into project '%s'.") format(action.title, project.title), Maid.happy) ::
            Message(_t("Is there any request?"), Maid.smile) ::
            Nil
        }

        def markProjectAsDone = {
            Message(_t("OK, I've mark this project as done. My master."), Maid.happy) ::
            Message(_t("Just for your information, if master add action not has been done into this project, this project will automatically mark as not done again."), Maid.smile) ::
            Nil
        }
        def unmarkProjectAsDone = Message(_t("OK, this project has been mark as not done again."), Maid.shy) :: Nil
        def couldNotMarkProjectDone = {
            Message(_t("Master, you need finish all actions in the project first."), Maid.shy) ::
            Message(_t("If actions in the project is not mark as done, this check box will not work."), Maid.smile) ::
            Nil
        }
        def markActionDone = Message(_t("This action has been done? OK, I will mark it as done."), Maid.happy) :: Nil
        def unmarkActionDone = {
            Message(_t("This action need to mark as not done again?"), Maid.panic) ::
            Message(_t("OK, I got it. If there is any request, please feel free to tell me."), Maid.shy) ::
            Nil
        }

        def actionDetails(action: Action) = {

            val description = action.description match {
                case Some(text) => _t("Master said:\n\n") + text
                case None       => _t("It seems there is no notes...")
            }

            Message(_t("About '%s'? Let me see...") format(action.title), Maid.smile) ::
            Message(description, Maid.shy) ::
            Nil
        }

        def projectDetails(project: Project) = {
            val vision = project.vision.getOrElse("")
            val description = project.description.getOrElse("")
            
            Message(_t("About project '%s'? Let me see...") format(project.title), Maid.smile) ::
            Message(_t("Vision:%s\nDescription:\n%s") format(vision, description), Maid.shy) ::
            Nil
        }

        def deleted = Message(_t("OK, I've deleted it!"), Maid.smile) :: Nil
        def reprocess(stuff: Stuff) = {
            Message(_t("Does master want to reprocess '%s'?") format(stuff.title), Maid.panic) ::
            Message(_t("As your wish, I will put it into inbox, master could process it later."), Maid.happy) ::
            Nil
        }

        def search(isFound: Boolean) = isFound match {
            case true =>
                Message(_t("Are you searching for this? If not, please hit enter to show next item that is matched."), Maid.smile) :: Nil

            case false =>
                Message(_t("Master, excuse me, it seems there is no matched item..."), Maid.shy) :: Nil
        }

    }

    object ReferenceTabMessage {
        def referenceDetails(reference: Reference) = {

            val description = reference.description match {
                case Some(text) => _t("Master said:\n\n") + text
                case None       => _t("It seems there is no notes...")
            }

            Message(_t("About '%s'? Let me see...") format(reference.title), Maid.smile) ::
            Message(description, Maid.shy) ::
            Nil
        }

        def deleted(reference: Reference) = {
            Message(_t("OK, I've delete '%s'.") format(reference.title), Maid.smile) :: 
            Nil
        }

        def reprocess(stuff: Stuff) = {
            Message(_t("Does master want to reprocess '%s'?") format(stuff.title), Maid.panic) ::
            Message(_t("As your wish, I will put it into inbox, master could process it later."), Maid.happy) ::
            Nil
        }

        def search(isFound: Boolean) = isFound match {
            case true =>
                Message(_t("Are you searching for this? If not, please hit enter to show next item that is matched."), Maid.smile) :: Nil

            case false =>
                Message(_t("Master, excuse me, it seems there is no matched item..."), Maid.shy) :: Nil
        }

    }
}

