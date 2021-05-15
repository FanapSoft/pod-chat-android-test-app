package ir.fanap.chattestapp.application.ui

import android.app.Activity
import android.app.Application
import android.app.NotificationManager
import android.arch.lifecycle.AndroidViewModel
import android.content.Context
import android.net.Uri
import android.support.annotation.Nullable
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.widget.Toast
import com.fanap.podchat.ProgressHandler
import com.fanap.podchat.chat.Chat
import com.fanap.podchat.chat.ChatListener
import com.fanap.podchat.chat.assistant.model.AssistantVo
import com.fanap.podchat.chat.assistant.request_model.DeActiveAssistantRequest
import com.fanap.podchat.chat.assistant.request_model.GetAssistantRequest
import com.fanap.podchat.chat.assistant.request_model.RegisterAssistantRequest
import com.fanap.podchat.chat.bot.request_model.CreateBotRequest
import com.fanap.podchat.chat.bot.request_model.DefineBotCommandRequest
import com.fanap.podchat.chat.bot.request_model.StartAndStopBotRequest
import com.fanap.podchat.chat.bot.result_model.CreateBotResult
import com.fanap.podchat.chat.bot.result_model.DefineBotCommandResult
import com.fanap.podchat.chat.bot.result_model.GetUserBotsResult
import com.fanap.podchat.chat.bot.result_model.StartStopBotResult
import com.fanap.podchat.chat.mention.model.RequestGetMentionList
import com.fanap.podchat.chat.pin.pin_message.model.RequestPinMessage
import com.fanap.podchat.chat.pin.pin_message.model.ResultPinMessage
import com.fanap.podchat.chat.pin.pin_thread.model.RequestPinThread
import com.fanap.podchat.chat.pin.pin_thread.model.ResultPinThread
import com.fanap.podchat.chat.thread.public_thread.RequestCheckIsNameAvailable
import com.fanap.podchat.chat.thread.public_thread.RequestCreatePublicThread
import com.fanap.podchat.chat.thread.public_thread.ResultIsNameAvailable
import com.fanap.podchat.chat.thread.request.CloseThreadRequest
import com.fanap.podchat.chat.thread.request.SafeLeaveRequest
import com.fanap.podchat.chat.thread.respone.CloseThreadResult
import com.fanap.podchat.chat.user.profile.RequestUpdateProfile
import com.fanap.podchat.chat.user.profile.ResultUpdateProfile
import com.fanap.podchat.chat.user.user_roles.model.ResultCurrentUserRoles
import com.fanap.podchat.mainmodel.*
import com.fanap.podchat.model.*
import com.fanap.podchat.notification.CustomNotificationConfig
import com.fanap.podchat.requestobject.*
import com.fanap.podchat.util.ChatStateType
import com.fanap.podchat.util.NetworkUtils.NetworkPingSender
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import ir.fanap.chattestapp.R
import ir.fanap.chattestapp.application.ui.log.refactorLog
import ir.fanap.chattestapp.application.ui.util.SmartArrayList
import ir.fanap.chattestapp.bussines.model.LogClass
import ir.fanap.chattestapp.bussines.token.TokenHandler
import rx.exceptions.MissingBackpressureException
import rx.exceptions.OnErrorNotImplementedException
import rx.subjects.PublishSubject
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val listOfLogs: SmartArrayList<LogClass> = SmartArrayList()

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    private var chat: Chat = Chat.init(application)
    private lateinit var testListener: TestListener
    var observable: PublishSubject<String> = PublishSubject.create()

    var observableLog: PublishSubject<String> = PublishSubject.create()

    var tokenHandler: TokenHandler? = null

    private var chatState: String? = ""

    private var savedContact: Contact? = null
    private var savedThread: Thread? = null

    init {

        val networkStateConfig = NetworkPingSender.NetworkStateConfig()
            .setHostName("msg.pod.ir")
            .setPort(443)
            .setDisConnectionThreshold(2)
            .setInterval(5000)
            .setConnectTimeout(3500)

            .build()

        chat.setNetworkStateConfig(networkStateConfig)

        chat.isLoggable(true)

        chat.isSentryLogActive(true)

        chat.isSentryResponseLogActive(true)

        chat.addListener(object : ChatListener {

            override fun onChatState(state: String?) {
                super.onChatState(state)

                try {
                    observable.onNext(state)
                    chatState = state
                } catch (e: Exception) {
                    observable.onError(e)
                } catch (be: MissingBackpressureException) {
                    observable.onError(be)
                } catch (il: IllegalStateException) {
                    observable.onError(il)
                } catch (oe: OnErrorNotImplementedException) {
                    observable.onError(oe)
                }


            }


            override fun onUserInfo(content: String?, response: ChatResponse<ResultUserInfo>?) {
                super.onUserInfo(content, response)

                testListener.onGetUserInfo(response)

            }

            override fun onBotCreated(response: ChatResponse<CreateBotResult>?) {
                super.onBotCreated(response)

                testListener.onBotCreated(response)
            }

            override fun onThreadClosed(response: ChatResponse<CloseThreadResult>?) {
                super.onThreadClosed(response)

                testListener.onThreadClosed(response)
            }

            override fun onBotCommandsDefined(response: ChatResponse<DefineBotCommandResult>?) {
                super.onBotCommandsDefined(response)

                testListener.onBotCommandsDefined(response)

            }

            override fun onBotStarted(response: ChatResponse<StartStopBotResult>?) {
                super.onBotStarted(response)

                testListener.onBotStarted(response)

            }

            override fun onBotStopped(response: ChatResponse<StartStopBotResult>?) {
                super.onBotStopped(response)

                testListener.onBotStopped(response)
            }

            override fun onRegisterAssistant(response: ChatResponse<MutableList<AssistantVo>>?) {
                super.onRegisterAssistant(response)

                testListener.onRegisterAssistant(response)
            }

            override fun onDeActiveAssistant(response: ChatResponse<MutableList<AssistantVo>>?) {
                super.onDeActiveAssistant(response)
                testListener.onDeActiveAssistant(response)
            }

            override fun onGetAssistants(response: ChatResponse<MutableList<AssistantVo>>?) {
                super.onGetAssistants(response)
                testListener.onGetAssistants(response)
            }

            override fun onChatProfileUpdated(response: ChatResponse<ResultUpdateProfile>?) {
                super.onChatProfileUpdated(response)

                testListener.onChatProfileUpdated(response)


            }
//


            override fun onRemoveRoleFromUser(outputSetRoleToUser: ChatResponse<ResultSetAdmin>?) {
                super.onRemoveRoleFromUser(outputSetRoleToUser)
                testListener.onRemoveRoleFromUser(outputSetRoleToUser)
            }

            override fun onGetCurrentUserRoles(response: ChatResponse<ResultCurrentUserRoles>?) {
                super.onGetCurrentUserRoles(response)

                testListener.onGetCurrentUserRoles(response)


            }

            override fun onGetMentionList(response: ChatResponse<ResultHistory>?) {
                super.onGetMentionList(response)

                testListener.onGetMentionList(response)

            }

            override fun onUnPinMessage(response: ChatResponse<ResultPinMessage>?) {
                super.onUnPinMessage(response)

                testListener.onMessageUnPinned(response)
            }

            override fun onPinMessage(response: ChatResponse<ResultPinMessage>?) {
                super.onPinMessage(response)

                testListener.onMessagePinned(response)
            }

            override fun onUnPinThread(response: ChatResponse<ResultPinThread>?) {
                super.onUnPinThread(response)

                testListener.onUnPinThread(response)
            }

            override fun onPinThread(response: ChatResponse<ResultPinThread>?) {
                super.onPinThread(response)

                testListener.onPinThread(response)
            }

            //todo implement not seen duration
            override fun OnNotSeenDuration(resultNotSeen: OutPutNotSeenDurations?) {
                super.OnNotSeenDuration(resultNotSeen)
            }

            override fun onGetThreadParticipant(
                content: String?,
                response: ChatResponse<ResultParticipant>?
            ) {
                super.onGetThreadParticipant(content, response)

                testListener.onGetThreadParticipant(response)
            }

            override fun onNewMessage(content: String?, response: ChatResponse<ResultNewMessage>?) {
                super.onNewMessage(content, response)

                testListener.onNewMessage(response)

            }

            override fun onError(content: String?, OutPutError: ErrorOutPut?) {
                super.onError(content, OutPutError)

                testListener.onError(OutPutError)

                if (OutPutError!!.errorCode == 21L) {

                    tokenHandler!!.refreshToken()

                }


            }

            override fun onUniqueNameIsAvailable(response: ChatResponse<ResultIsNameAvailable>?) {
                super.onUniqueNameIsAvailable(response)
                testListener.onCheckIsNameAvailable(response)
            }

            override fun onCreateThread(content: String?, response: ChatResponse<ResultThread>?) {
                super.onCreateThread(content, response)
                testListener.onCreateThread(response)
            }

            override fun onContactAdded(
                content: String?,
                response: ChatResponse<ResultAddContact>?
            ) {
                super.onContactAdded(content, response)
                testListener.onAddContact(response)
            }

            override fun onUserBots(response: ChatResponse<GetUserBotsResult>?) {
                super.onUserBots(response)
            }
            override fun onRemoveContact(
                content: String?,
                response: ChatResponse<ResultRemoveContact>?
            ) {
                super.onRemoveContact(content, response)
                testListener.onRemoveContact(response)
            }

            override fun onBlock(content: String?, response: ChatResponse<ResultBlock>?) {
                super.onBlock(content, response)
                testListener.onBlock(response)
            }

            override fun onGetContacts(content: String?, response: ChatResponse<ResultContact>?) {
                super.onGetContacts(content, response)
                testListener.onGetContact(response)
            }

            override fun onGetBlockList(
                content: String?,
                response: ChatResponse<ResultBlockList>?
            ) {
                super.onGetBlockList(content, response)
                testListener.onBlockList(response)
            }


            override fun onLogEvent(logName: String?, json: String?) {


                saveLogs(logName, json)

                testListener.onLogEventWithName(logName!!, json!!)
            }

            override fun onLogEvent(log: String?) {
                super.onLogEvent(log)
                testListener.onLogEvent(log!!)
            }

            override fun onUpdateContact(
                content: String?,
                response: ChatResponse<ResultUpdateContact>?
            ) {
                super.onUpdateContact(content, response)
                testListener.onUpdateContact(response)
            }

            override fun onUnBlock(content: String?, response: ChatResponse<ResultBlock>?) {
                super.onUnBlock(content, response)
                testListener.onUnBlock(response)
            }

            override fun onSent(content: String?, response: ChatResponse<ResultMessage>?) {
                super.onSent(content, response)
                testListener.onSent(response)
            }

            override fun onGetThread(content: String?, thread: ChatResponse<ResultThreads>?) {
                super.onGetThread(content, thread)
                testListener.onGetThread(thread)
            }

            override fun onSeen(content: String?, response: ChatResponse<ResultMessage>?) {
                super.onSeen(content, response)
                testListener.onSeen(response)
            }

            override fun onDeliver(content: String?, response: ChatResponse<ResultMessage>?) {
                super.onDeliver(content, response)
                testListener.onDeliver(response)
            }

            override fun onThreadRemoveParticipant(
                content: String?,
                response: ChatResponse<ResultParticipant>?
            ) {
                super.onThreadRemoveParticipant(content, response)
                testListener.onThreadRemoveParticipant(response)
            }

            override fun onThreadAddParticipant(
                content: String?,
                response: ChatResponse<ResultAddParticipant>?
            ) {
                super.onThreadAddParticipant(content, response)
                testListener.onThreadAddParticipant(response)
            }

            override fun onThreadLeaveParticipant(
                content: String?,
                response: ChatResponse<ResultLeaveThread>?
            ) {
                super.onThreadLeaveParticipant(content, response)
                testListener.onLeaveThread(response)
            }

            override fun onMuteThread(content: String?, response: ChatResponse<ResultMute>?) {
                super.onMuteThread(content, response)
                testListener.onMuteThread(response)
            }

            override fun onUnmuteThread(content: String?, response: ChatResponse<ResultMute>?) {
                super.onUnmuteThread(content, response)
                testListener.onUnmuteThread(response)
            }

            override fun onDeleteMessage(
                content: String?,
                response: ChatResponse<ResultDeleteMessage>?
            ) {
                super.onDeleteMessage(content, response)
                testListener.onDeleteMessage(response)
            }

            override fun onEditedMessage(
                content: String?,
                response: ChatResponse<ResultNewMessage>?
            ) {
                super.onEditedMessage(content, response)
                testListener.onEditedMessage(response)
            }

            override fun onGetHistory(content: String?, response: ChatResponse<ResultHistory>?) {
                super.onGetHistory(content, response)
                testListener.onGetHistory(response)
            }

            override fun OnClearHistory(
                content: String?,
                chatResponse: ChatResponse<ResultClearHistory>?
            ) {
                super.OnClearHistory(content, chatResponse)
                testListener.onClearHistory(chatResponse)
            }


            override fun onGetThreadAdmin(
                content: String?,
                chatResponse: ChatResponse<ResultParticipant>?
            ) {
                super.onGetThreadAdmin(content, chatResponse)

                testListener.onGetAdminList(chatResponse)

            }

            override fun OnSetRule(outputSetRoleToUser: ChatResponse<ResultSetAdmin>?) {
                super.OnSetRule(outputSetRoleToUser)

                testListener.onSetRole(outputSetRoleToUser)

            }

            override fun OnSeenMessageList(
                content: String?,
                response: ChatResponse<ResultParticipant>?
            ) {
                super.OnSeenMessageList(content, response)

                testListener.onGetSeenMessageList(response)
            }

            override fun OnDeliveredMessageList(
                content: String?,
                response: ChatResponse<ResultParticipant>?
            ) {
                super.OnDeliveredMessageList(content, response)

                testListener.onGetDeliverMessageList(response)

            }


            override fun onSearchContact(content: String?, response: ChatResponse<ResultContact>?) {
                super.onSearchContact(content, response)

                testListener.onGetSearchContactResult(response)
            }


            override fun OnStaticMap(response: ChatResponse<ResultStaticMapImage>?) {
                super.OnStaticMap(response)

                testListener.onGetStaticMap(response)
            }

            override fun onUploadImageFile(
                content: String?,
                response: ChatResponse<ResultImageFile>?
            ) {
                super.onUploadImageFile(content, response)

                testListener.onUploadImageFile(content, response)
            }

            override fun onUploadFile(content: String?, response: ChatResponse<ResultFile>?) {
                super.onUploadFile(content, response)

                testListener.onUploadFile(response)
            }
        })


        if (tokenHandler == null) {

            tokenHandler = TokenHandler(application)

            tokenHandler!!.addListener(object : TokenHandler.ITokenHandler {
                override fun onGetToken(token: String?) {

                    handleTokenReceived(token)

                }

                override fun onTokenRefreshed(token: String?) {

                    handleTokenReceived(token)

                }

                override fun onError(message: String?) {

                    try {
                        Toast.makeText(application.applicationContext, message, Toast.LENGTH_LONG)
                            .show()
                    } catch (e: Exception) {
                    }
                }
            })

        }


    }

    private fun handleTokenReceived(token: String?) {

        !token.isNullOrBlank().apply {

            if (chatState!! == ChatStateType.ChatSateConstant.ASYNC_READY) {

                chat.setToken(token)

            } else {

                testListener.onConnectWithOTP(token)

            }

        }

    }


    fun setupNotification(activity: Activity) {

        val notificationConfig =
            CustomNotificationConfig.Builder(activity)
                .setChannelName("MY TEST APP")
                .setChannelId("TEST APP")
                .setChannelDescription("Fanap soft test app notification channel")
                .setIcon(R.drawable.ic_message)
                .setNotificationImportance(NotificationManager.IMPORTANCE_DEFAULT)
                .build()


        chat.setupNotification(notificationConfig)

    }
//        val notificationConfig = CustomNotificationConfig.Builder(
//            NOTIFICATION_APPLICATION_ID,
//            activity
//        )
//            .setChannelName("PODCHAT_TEST_CHANNEL")
//            .setChannelId("PODCHATTEST")
//            .setChannelDescription("Fanap soft podchat notification channel")
//            .setNotificationImportance(NotificationManager.IMPORTANCE_DEFAULT)
//            .build()
//
//
//        chat.enableNotification(
//            notificationConfig, object : INotification {
//                override fun onUserIdUpdated(userId: String?) {
//
//                    Log.d("MTAG", "new User name $userId")
//                }
//
//                override fun onPushMessageReceived(message: String?) {
//                    super.onPushMessageReceived(message)
//
//                    Log.d("MTAG", "new Push message $message")
//
//                    ShowNotificationHelper.showNotification(
//                        "from Test App",
//                        message,
//                        getApplication(),
//                        MainActivity::class.java,
//                        null,
//                        R.drawable.fanap_logo
//                    )
//                }
//            })
//
//
//
//    }

    private fun saveLogs(logName: String?, json: String?) {


        listOfLogs.addAll(refactorLog(logName = logName!!, log = json!!, gson = gson))


    }

    private fun addToLogsAndLogNames(uniqueId: String, logName: String?, log: String?) {


        listOfLogs.add(LogClass(uniqueId = uniqueId, logName = logName!!, log = log!!))


    }

    fun setTestListener(testListener: TestListener) {
        this.testListener = testListener
    }

    fun connect(
        socketAddress: String,
        appId: String,
        severName: String,
        token: String,
        ssoHost: String,
        platformHost: String,
        fileServer: String,
        podSpaceUrl: String,
        typeCode: String?
    ) {

        val rb: RequestConnect = RequestConnect.Builder(
            socketAddress,
            appId,
            severName,
            token,
            ssoHost,
            platformHost,
            fileServer,
            podSpaceUrl
        ).build()

        chat.connect(rb)

    }

    fun uploadFile(requestUploadFile: RequestUploadFile): String {
        return chat.uploadFile(requestUploadFile)
    }

    var isCachaeble = false;

    fun isCachable(boolean: Boolean) {

        isCachaeble = boolean
        chat.isCacheables(boolean)


    }

    fun uploadFileProgress(
        requestUploadFile: RequestUploadFile,
        progress: ProgressHandler.onProgressFile
    ): String {
        return chat.uploadFileProgress(requestUploadFile, progress)
    }

    fun uploadImage(activity: FragmentActivity?, uri: Uri): String {

        val req = RequestUploadImage.Builder(activity, uri)
            .build()

        return chat.uploadImage(req)
    }

    fun uploadImageProgress(
        context: Context,
        activity: FragmentActivity?,
        uri: Uri?,
        progress: ProgressHandler.onProgress
    ): String {

        val req = RequestUploadImage.Builder(activity, uri)
            .build()

        return chat.uploadImageProgress(req, progress)
    }

    fun uploadImageProgress(
        requestUploadImage: RequestUploadImage,
        progress: ProgressHandler.onProgress
    ): String {
        return chat.uploadImageProgress(requestUploadImage, progress)
    }

    fun sendFileMessage(
        requestFileMessage: RequestFileMessage,
        objects: ProgressHandler.sendFileMessage
    ): String {
        return chat.sendFileMessage(requestFileMessage, objects)
    }

    fun replyWithFile(
        requestReplyMessage: RequestReplyFileMessage,
        objects: ProgressHandler.sendFileMessage
    ): String {
        return chat.replyFileMessage(requestReplyMessage, objects)
    }

    fun getHistory(requestGetHistory: RequestGetHistory): String {
        return chat.getHistory(requestGetHistory, null)
    }

    fun editMessage(requestEditMessage: RequestEditMessage): String {
        return chat.editMessage(requestEditMessage, null)
    }

    fun muteThread(requestMuteThread: RequestMuteThread): String {
        return chat.muteThread(requestMuteThread, null)
    }

    fun closeThread(requestCloseThread: CloseThreadRequest): String {
        return chat.closeThread(requestCloseThread)
    }

    fun safeLeaveThread(requestSafeLeave: SafeLeaveRequest): String {

        return chat.safeLeaveThread(requestSafeLeave)
    }

    fun unMuteThread(requestMuteThread: RequestMuteThread): String {
        return chat.unMuteThread(requestMuteThread, null)
    }

    fun createThread(
        threadType: Int,
        invitee: Array<Invitee>,
        threadTitle: String,
        description: String,
        image: String,
        metadata: String
    ): String {
        return chat.createThread(
            threadType,
            invitee,
            threadTitle,
            description,
            image,
            metadata,
            null
        )
    }


    fun createThreadWithMessage(requestCreateThread: RequestCreateThread): ArrayList<String>? {

        return chat.createThreadWithMessage(requestCreateThread)
    }

    fun registerAssistant(request: RegisterAssistantRequest): String? {
        return chat.registerAssistant(request)
    }

    fun getAssistants(request: GetAssistantRequest): String? {
        return chat.getAssistants(request)
    }

    fun deactiveAssistant(request: DeActiveAssistantRequest): String? {
        return chat.deactiveAssistant(request)
    }


    fun updateContact(requestUpdateContact: RequestUpdateContact): String {
        return chat.updateContact(requestUpdateContact)
    }


    fun getContact(requestGetContact: RequestGetContact): String {

        return chat.getContacts(requestGetContact, null)
    }

    fun addContacts(requestAddContact: RequestAddContact): String {

        return chat.addContact(requestAddContact)
    }

    fun removeContact(requestRemoveContact: RequestRemoveContact): String {
        return chat.removeContact(requestRemoveContact)
    }


    fun blockContact(requestBlock: RequestBlock): String {
        return chat.block(requestBlock, null)
    }

    fun unBlock(requestUnBlock: RequestUnBlock): String {
        return chat.unblock(requestUnBlock, null)
    }

    fun deleteMessage(requestDeleteMessage: RequestDeleteMessage): String {
        return chat.deleteMessage(requestDeleteMessage, null)
    }

    fun createBot(request: CreateBotRequest): String {
        return chat.createBot(request)
    }

    fun defineBotCommand(request: DefineBotCommandRequest): String {
        return chat.addBotCommand(request)
    }

    fun startBot(request: StartAndStopBotRequest): String {
        return chat.startBot(request)
    }

    fun getThreadBotList(request: StartAndStopBotRequest): String {
//           return chat.getBlockList(request)
        return ""
    }

    fun stopBot(request: StartAndStopBotRequest): String {
        return chat.stopBot(request)
    }

    fun getThreads(requestThread: RequestThread): String {

        return chat.getThreads(requestThread, null)
    }

    fun getBlockList(requestBlockList: RequestBlockList): String {

        return chat.getBlockList(requestBlockList, null)
    }

    fun sendTextMsg(requestMessage: RequestMessage): String {
        return chat.sendTextMessage(requestMessage, null)
    }

    fun forwardMessage(requestForwardMessage: RequestForwardMessage): List<String> {
        return chat.forwardMessage(requestForwardMessage)
    }

    fun replyMessage(replyMessage: RequestReplyMessage): String {
        return chat.replyMessage(replyMessage, null)
    }

    fun addParticipant(requestAddParticipants: RequestAddParticipants): String {
        return chat.addParticipants(requestAddParticipants, null)
    }

    fun removeParticipant(requestRemoveParticipants: RemoveParticipantRequest): String {
        return chat.removeParticipants(requestRemoveParticipants, null)
    }

    fun leaveThread(requestLeaveThread: RequestLeaveThread): String {
        return chat.leaveThread(requestLeaveThread, null)
    }

    fun getParticipant(requestThreadParticipant: RequestThreadParticipant): String {

        return chat.getThreadParticipants(requestThreadParticipant, null)
    }

    fun clearHistory(requestClearHistory: RequestClearHistory): String {

        return chat.clearHistory(requestClearHistory)
    }

    fun getAdminList(requestGetAdmin: RequestGetAdmin): String {

        return chat.getAdminList(requestGetAdmin)
    }

    fun addAdmin(addAdmin: RequestSetAdmin?): String {

        return chat.addAdmin(addAdmin)
    }

    fun removeAdminRoles(addAdmin: RequestSetAdmin?): String {

        return chat.removeAdmin(addAdmin)
    }

    fun getDeliverMessageList(request: RequestDeliveredMessageList): String {

        return chat.getMessageDeliveredList(request)

    }

    fun getSeenMessageList(request: RequestSeenMessageList): String {

        return chat.getMessageSeenList(request)
    }

    fun searchContact(searchContact: RequestSearchContact): String {
        return chat.searchContact(searchContact)
    }

    fun sendLocationMessage(
        requestLocationMessage: RequestLocationMessage,
        param: ProgressHandler.sendFileMessage
    ): String {

        return chat.sendLocationMessage(requestLocationMessage, param)


    }

    fun deleteMultipleMessage(requestDeleteMessage: RequestDeleteMessage): ArrayList<String> {

        return ArrayList(chat.deleteMultipleMessage(requestDeleteMessage, null))

    }

    fun spamThread(requestSpam: RequestSpam): String {

        return chat.spam(requestSpam)
    }

    fun setToken(sandToken: String) {

        chat.setToken(sandToken)

    }

    fun pinThread(request: RequestPinThread): String {

        return chat.pinThread(request)

    }

    fun unpinThread(request: RequestPinThread): String {

        return chat.unPinThread(request)
    }

    fun pinMessage(request: RequestPinMessage): String {

        return chat.pinMessage(request)

    }

    fun unpinMessage(request: RequestPinMessage?): String {

        return chat.unPinMessage(request)
    }

    fun getMentionList(request: RequestGetMentionList?): String {

        return chat.getMentionList(request)

    }

    fun getCurrentUserRoles(request: RequestGetUserRoles?): String {

        return chat.getCurrentUserRoles(request)
    }

    fun updateChatProfile(request: RequestUpdateProfile): String {

        return chat.updateChatProfile(request)
    }

    fun getUserInfo(): String {

        return chat.getUserInfo(null)

    }

    fun verifyOTPCode(entry: String) {

        tokenHandler!!.verifyNumber(entry)
    }

    fun checkNumber(entry: String) {

        tokenHandler!!.handshake(entry)
    }

    fun closeChat() {

        Log.i("OTP", "CLOSING CHAT...")
        chat.closeChat()


    }

    fun checkIsNameAvailable(request: RequestCheckIsNameAvailable?): String {

        return chat.isNameAvailable(request)
    }

    fun createThread(request: RequestCreateThread?): String {

        return chat.createThread(request)

    }

    fun createPublicThread(request: RequestCreatePublicThread?): String {

        return chat.createThread(request)
    }

    fun setDownloadDire(cacheDir: File?) {

        chat.setDownloadDirectory(cacheDir)

    }

    fun createThreadWithFile(
        request: RequestCreateThreadWithFile,
        progress: ProgressHandler.sendFileMessage
    ): ArrayList<String> {

        return chat.createThreadWithFile(request, progress)

    }

    fun keepThread(thread: Thread?) {
        savedThread = thread
    }

    fun keepContact(contact: Contact?) {
        savedContact = contact
    }

    @Nullable
    fun getSavedThread() = savedThread

    @Nullable
    fun getSavedContact() = savedContact

    fun clearSavedThread() {
        savedThread = null
    }

    fun clearSavedContact() {
        savedContact = null
    }

}