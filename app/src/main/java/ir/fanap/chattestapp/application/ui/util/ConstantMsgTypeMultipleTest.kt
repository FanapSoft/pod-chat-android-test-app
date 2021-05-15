package ir.fanap.chattestapp.application.ui.util

import android.support.annotation.StringDef

class ConstantMsgTypeMultipleTest {

    companion object {
        const val CREATE_THREAD = "CREATE_THREAD"
        const val SEND_MESSAGE = "SEND_MESSAGE"
        const val GET_THREAD = "GET_THREAD"
        const val GET_HISTORY = "GET_HISTORY"
        const val GET_CONTACT = "GET_CONTACT"
        const val GET_PARTICIPANT = "GET_PARTICIPANT"
    }


    @StringDef(
        CREATE_THREAD,
        SEND_MESSAGE,
        GET_THREAD,
        GET_HISTORY,
        GET_CONTACT,
        GET_PARTICIPANT
    )

    @Retention(AnnotationRetention.SOURCE)
    annotation class CONSTANT
}