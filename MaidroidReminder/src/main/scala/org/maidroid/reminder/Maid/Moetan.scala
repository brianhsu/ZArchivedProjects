package org.maidroid.reminder

import MaidroidReminder._

abstract class Moetan extends Maid
{
    private val resPrefix = "android.resource://" + 
                            MaidroidReminder.PACKAGE + "/raw/"

    def name:String       = R.string.moetan_name
    val room              = R.drawable.room
    val notificationIcon  = R.drawable.moetan_head
    val notificationSound = resPrefix + "moetan_02"

    val faceSmile         = R.drawable.moetan_smile
    val maidWhole         = R.drawable.moetan

    override def remindMessage (item: ReminderItem) = List (
        Message (faceSmile, R.string.moetan_remind_1),
        Message (faceSmile, R.string.moetan_remind_2),
        Message (faceSmile, R.string.moetan_remind_3.
                             format(item.title,item.description))
    )
    
    def addItemMessage = List (
        Message (faceSmile, R.string.moetan_add_item_1),
        Message (faceSmile, R.string.moetan_add_item_2)
    )

    def editItemMessage = List (
        Message (faceSmile, R.string.moetan_edit_item_1),
        Message (faceSmile, R.string.moetan_edit_item_2)
    )

    def syncMessage = List (
        Message (faceSmile, R.string.moetan_sync_1),
        Message (faceSmile, R.string.moetan_sync_2)
    )

    def itemListMessage (action: String) = action match {
        case ACTION.LIST_WEEK => List ( 
            Message (faceSmile, R.string.moetan_list_week_1, resPrefix + "moetan_01"),
            Message (faceSmile, R.string.moetan_list_week_2)
        )

        case ACTION.LIST_MONTH   => List ( 
            Message (faceSmile, R.string.moetan_list_month_1),
            Message (faceSmile, R.string.moetan_list_month_2)
        )
           
        case ACTION.LIST_ALL     => List (
            Message (faceSmile, R.string.moetan_list_all_1),
            Message (faceSmile, R.string.moetan_list_all_2)
        )

        case ACTION.LIST_OUTDATE => List (
            Message (faceSmile, R.string.moetan_list_outdate_1),
            Message (faceSmile, R.string.moetan_list_outdate_2)
        )

        case _ => Nil

    }

    def aboutMessage () = List (
        Message (faceSmile, R.string.moetan_about_1),
        Message (faceSmile, R.string.moetan_about_2),
        Message (faceSmile, R.string.moetan_about_3)

    )

    def itemSelectedMessage (item: ReminderItem) = 
    {
        val triggerTime = new TriggerTime (item.triggerTime)
        val message     = item.description match {
            case "" => List (
                Message (faceSmile, R.string.moetan_item_select_no_desc.format (triggerTime))
            )

            case _  => List (
                Message (faceSmile, R.string.moetan_item_select_desc_1.format (triggerTime, item.description)), 
                Message (faceSmile,  R.string.moetan_item_select_desc_2)
            )
        }

        Message (faceSmile, R.string.moetan_item_select_first) :: message
    }

    def delTitle   (item: ReminderItem) = R.string.moetan_delete_title.format (item.title)
    def delMessage (item: ReminderItem) = Message (faceSmile, R.string.moetan_delete_message)

    def toastOK          = Message (maidWhole, R.string.moetan_toastOK)
    def toastNoDisturb   = Message (maidWhole, R.string.moetan_toastNoDisturb)
    def addItemNoTitle   = Message (maidWhole, R.string.moetan_addItemNoTitle)
    def addItemTimeError = Message (maidWhole, R.string.moetan_addItemTimeError)
    def excuseMe         = Message (faceSmile, R.string.moetan_excuseMe)
    def noUsername       = Message (faceSmile, R.string.moetan_no_username)
    def noPassword       = Message (faceSmile, R.string.moetan_no_password)
    def syncError(reason: String) = Message (faceSmile, reason)

    def notificationTitle   (item: ReminderItem) = item.title
    def notificationContent (item: ReminderItem) = item.description
}
