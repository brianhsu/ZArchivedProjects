package org.maidroid.reminder

import MaidroidReminder._

abstract class Mia extends Maid
{
    private val resPrefix = "android.resource://" + 
                            MaidroidReminder.PACKAGE + "/raw/"

    def name:String       = R.string.mia_name
    val room              = R.drawable.room
    val notificationIcon  = R.drawable.mia_head
    val notificationSound = resPrefix + "mia_02"

    val faceNormal        = R.drawable.mia_normal
    val faceShy           = R.drawable.mia_shy
    val faceSmile         = R.drawable.mia_smile
    val facePanic         = R.drawable.mia_panic

    val maidWhole         = R.drawable.mia

    override def remindMessage (item: ReminderItem) = List (
        Message (faceNormal, R.string.mia_remind_1),
        Message (faceSmile,  R.string.mia_remind_2),
        Message (faceNormal, R.string.mia_remind_3.
                             format(item.title,item.description))
    )
    
    def addItemMessage = List (
        Message (faceSmile,  R.string.mia_add_item_1),
        Message (faceNormal, R.string.mia_add_item_2),
        Message (faceNormal, R.string.mia_add_item_3)
    )

    def editItemMessage = List (
        Message (facePanic, R.string.mia_edit_item_1),
        Message (facePanic, R.string.mia_edit_item_2)
    )

    def syncMessage = List (
        Message (faceNormal, R.string.mia_sync_1),
        Message (faceSmile,  R.string.mia_sync_2)
    )

    def itemListMessage (action: String) = action match {

        case ACTION.LIST_WEEK => List ( 
            Message (faceSmile,  R.string.mia_list_week_1, resPrefix + "mia_01"),
            Message (faceNormal, R.string.mia_list_week_2),
            Message (faceSmile,  R.string.mia_list_week_3)
        )

        case ACTION.LIST_MONTH   => List ( 
            Message (faceSmile,  R.string.mia_list_month_1),
            Message (faceNormal, R.string.mia_list_month_2)
        )
           
        case ACTION.LIST_ALL     => List (
            Message (facePanic, R.string.mia_list_all_1),
            Message (faceShy,   R.string.mia_list_all_2)
        )

        case ACTION.LIST_OUTDATE => List (
            Message (faceNormal, R.string.mia_list_outdate_1),
            Message (faceShy,    R.string.mia_list_outdate_2)
        )

        case _ => Nil
    }

    def aboutMessage () = List (
        Message (faceShy,   R.string.mia_about_1),
        Message (faceSmile, R.string.mia_about_2),
        Message (faceSmile, R.string.mia_about_3)
    )

    def itemSelectedMessage (item: ReminderItem) = 
    {
        val triggerTime = new TriggerTime (item.triggerTime)
        val message     = item.description match {
            case "" => List (
                Message (facePanic, R.string.mia_item_select_no_desc.format (triggerTime))
            )

            case _  => List (
                Message (faceNormal, R.string.mia_item_select_desc_1.format (triggerTime, item.description)), 
                Message (faceSmile,  R.string.mia_item_select_desc_2)
            )
        }

        Message (faceShy, R.string.mia_item_select_first) :: message
    }

    def delTitle   (item: ReminderItem) = R.string.mia_delete_title.format (item.title)
    def delMessage (item: ReminderItem) = Message (facePanic, R.string.mia_delete_message)

    def toastOK          = Message (maidWhole, R.string.mia_toastOK)
    def toastNoDisturb   = Message (maidWhole, R.string.mia_toastNoDisturb)
    def addItemNoTitle   = Message (maidWhole, R.string.mia_addItemNoTitle)
    def addItemTimeError = Message (maidWhole, R.string.mia_addItemTimeError)
    def excuseMe         = Message (faceSmile, R.string.mia_excuseMe)
    def noUsername       = Message (faceSmile, R.string.mia_no_username)
    def noPassword       = Message (facePanic, R.string.mia_no_password)
    def syncError(reason: String) = Message (facePanic, reason)
 
    def notificationTitle   (item: ReminderItem) = item.title
    def notificationContent (item: ReminderItem) = item.description
}
