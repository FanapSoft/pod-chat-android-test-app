package ir.fanap.chattestapp.application.ui.test

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModelProvider
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fanap.podchat.mainmodel.ChatMessage
import com.fanap.podchat.model.*
import com.fanap.podchat.requestobject.RequestGetContact
import com.fanap.podchat.requestobject.RequestMessage
import com.fanap.podchat.requestobject.RequestThread
import com.fanap.podchat.requestobject.RequestThreadParticipant
import com.fanap.podchat.util.ChatMessageType
import com.fanap.podchat.util.TextMessageType
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import ir.fanap.chattestapp.R
import ir.fanap.chattestapp.application.ui.MainViewModel
import ir.fanap.chattestapp.application.ui.TestListener
import ir.fanap.chattestapp.application.ui.log.LogAdapter
import ir.fanap.chattestapp.application.ui.log.refactorLog
import ir.fanap.chattestapp.application.ui.util.SmartHashMap
import ir.fanap.chattestapp.bussines.model.LogClass
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.*


@SuppressLint("SetTextI18n")
class TestFragment : Fragment(), TestListener, LogAdapter.ViewHolderListener {
    var itrators = listOf(2, 5, 10, 30, 50, 80, 120)
    var methods = listOf("GET_THREAD", "GET_CONTACT", "SEND_MESSAGE", "GET_PARTICIPANT")
    var selectedItrator: Int = 0
    var selectedSenario: String = ""
    private var chatReady: Boolean = false
    private lateinit var mainViewModel: MainViewModel
    private var fucCallback: SmartHashMap<String, String> = SmartHashMap()
    private lateinit var itratorSpinner: Spinner
    private lateinit var senarioSpinner: Spinner
    private lateinit var btn_execute: Button
    private lateinit var txt_response_count: TextView
    private lateinit var txt_send_count: TextView
    private lateinit var contextFrag: Context
    val targetThreadId = 8946L
    var result: StringBuilder? = null
    private lateinit var logAdapter: LogAdapter
    lateinit var recyclerView: RecyclerView;
    private var logs: MutableList<LogClass> = mutableListOf()
    var reqstate: Int = 0;
    var resstate: Int = 0;

    companion object {
        fun newInstance(): TestFragment {
            return TestFragment()
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        contextFrag = context!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_test, container, false)
        initViews(view)
        setListeners()
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel =
            ViewModelProvider.AndroidViewModelFactory.getInstance(activity!!.application)
                .create(MainViewModel::class.java)
        mainViewModel.observable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { Log.d("CHAT_TEST_UI", "UI ERROR") }
            .subscribe {
                if (it == "CHAT_READY") {
                    chatReady = true
                }
            }

        mainViewModel.setTestListener(this)
        fucCallback.onInsertObserver.subscribe { pair ->
            Log.d("QTAG", "INSERTED: $pair")
        }
    }

    private fun initViews(view: View) {
        itratorSpinner = view.findViewById(R.id.spinner)
        senarioSpinner = view.findViewById(R.id.spinnersenario)
        btn_execute = view.findViewById(R.id.btn_execute)
        txt_response_count = view.findViewById(R.id.txt_response_count)
        txt_send_count = view.findViewById(R.id.txt_send_count)
        recyclerView = view.findViewById(R.id.recyclerV_funcLog)
        logAdapter = context?.let { LogAdapter(logs, this, context = it) }!!
        recyclerView.adapter = logAdapter
        val linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager

        if (itratorSpinner != null) {
            itratorSpinner.adapter = NumbersSpinnerAdapter(
                context,
                itrators
            )
        }
        if (senarioSpinner != null) {
            senarioSpinner.adapter = MethodsSpinnerAdapter(
                context,
                methods
            )
        }
    }

    private fun setListeners() {
        itratorSpinner.adapter = NumbersSpinnerAdapter(
            context,
            itrators
        )
        senarioSpinner.adapter = MethodsSpinnerAdapter(
            context,
            methods
        )
        itratorSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                if (!InProcess())
                    selectedItrator = itrators[position];
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        senarioSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                if (!InProcess())
                    selectedSenario = methods[position];
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        btn_execute.setOnClickListener(this::runSenario)
    }

    fun InProcess(): Boolean {
        if (reqstate == 0 || reqstate == selectedItrator) {
            reqstate = 0;
            resstate = 0;
            txt_send_count.setText("")
            txt_response_count.setText("")
            return false
        }
        else showToast("In process")
        return true
    }

    fun showToast(message: String) {
        Toast.makeText(
            activity, message, Toast.LENGTH_LONG
        ).show()
    }

    private fun runSenario(view: View) {
        if (InProcess()) return;


        logs.clear()
        result = StringBuilder()
        Thread(Runnable {
            for (i in 1..selectedItrator) {
                if (!chatReady) {
                    logMsg("break")
                    break
                }
                logMsg("run")
                runFunction();
            }
        }).start()
    }

    private fun runFunction() {
        Thread.sleep(500)
        var uniq: String = ""
        when (selectedSenario) {
            "GET_THREAD" -> {
                uniq = getThread(10)
            }
            "GET_CONTACT" -> {
                uniq = getContact(50);
            }
            "SEND_MESSAGE" -> {
                uniq = sendTextMessage();
            }
            "GET_PARTICIPANT" -> {
                uniq = getThreadParticipant(50);
            }
        }
        fucCallback[uniq] = uniq
        logMsg(selectedSenario + "runSenario: " + reqstate + "------> " + uniq)
        result?.append(selectedSenario + "runSenario: " + reqstate + "------> " + uniq + "\n \n")
        reqstate++;

        activity?.runOnUiThread(Runnable {
            txt_send_count.setText("requestcount: " + reqstate.toString())
        })
    }

    override fun onGetThread(chatResponse: ChatResponse<ResultThreads>?) {
        super.onGetThread(chatResponse)
        addResult("Thread response: " + resstate + "  " + chatResponse?.isCache + "------> " + chatResponse?.uniqueId)
    }

    override fun onGetContact(response: ChatResponse<ResultContact>?) {
        super.onGetContact(response)
        addResult("Contact response: " + resstate + "  " + response?.isCache + "------> " + response?.uniqueId)
    }

    override fun onGetThreadParticipant(response: ChatResponse<ResultParticipant>?) {
        super.onGetThreadParticipant(response)
        addResult("Participant response: " + resstate + "  " + response?.isCache + "------> " + response?.uniqueId)
    }

    override fun onSent(response: ChatResponse<ResultMessage>?) {
        super.onSent(response)
        addResult(" Sent response: " + resstate + "  " + response?.isCache + "------> " + response?.uniqueId)
    }

    private fun addResult(resultMsg: String) {
        logMsg(resultMsg)
        result?.append(resultMsg)
        resstate++
        activity?.runOnUiThread(Runnable {
            txt_response_count.setText("ResultCount: " + resstate.toString())
            logAdapter.notifyDataSetChanged()
        })
    }

    private fun getThread(offset: Long): String {
        val requestGetThread = RequestThread.Builder()
            .count(25)
            .offset(offset)
            .build()
        return mainViewModel.getThreads(requestGetThread)
    }

    private fun getContact(offset: Long): String {
        val requestGetContact = RequestGetContact.Builder().build()
        return mainViewModel.getContact(requestGetContact)
    }

    private fun getThreadParticipant(offset: Long): String {
        val request = RequestThreadParticipant.Builder()
            .threadId(targetThreadId)
            .build()
        return mainViewModel.getParticipant(request)
    }

    private fun sendTextMessage(): String {
        val requestMessage = RequestMessage
            .Builder("test text message ${Date()}", targetThreadId)
            .build()
        requestMessage.messageType = TextMessageType.Constants.TEXT;
        return mainViewModel.sendTextMsg(requestMessage)
    }


    override fun onLogEventWithName(logName: String, json: String) {
        super.onLogEventWithName(logName, json)

        if (logName.isEmpty()) return

        val gson: Gson = GsonBuilder().setPrettyPrinting().create()

        val chatMessage: ChatMessage = gson.fromJson(json, ChatMessage::class.java)

        if (chatMessage.type == ChatMessageType.Constants.PING) return

        var logs: ArrayList<LogClass> = refactorLog(logName = logName!!, log = json!!, gson = gson)
        if (chatMessage.uniqueId == fucCallback[chatMessage.uniqueId]) {
            this.logs.add(logs.get(0))
        }

    }

    private fun logMsg(message: String) {
        Log.e("SenarioTest", message)
    }

    override fun onItemShowPairedLog(pos: Int, lastSelected: Int, log: LogClass) {

    }
}

