package ir.fanap.chattestapp.application.ui.log

import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.core.content.ContextCompat.getSystemService
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ir.fanap.chattestapp.R
import ir.fanap.chattestapp.bussines.model.LogClass


class LogAdapter2(val logs: ArrayList<LogClass>) : RecyclerView.Adapter<LogAdapter2.ViewHolder>() {

    var filteredLogs: ArrayList<LogClass> = logs



    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {




        val logText = logs[position].log

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            viewHolder.textViewLog.text = Html.fromHtml(logText,Html.FROM_HTML_MODE_LEGACY)

            viewHolder.tvLogName.text = Html.fromHtml("..:: ${logs[position].logName} ::.." ,Html.FROM_HTML_MODE_LEGACY)




        }else{

            viewHolder.textViewLog.text = Html.fromHtml(logText)

            viewHolder.tvLogName.text = Html.fromHtml("..:: ${logs[position].logName} ::..")

        }

        viewHolder.logNum.text = "#${(position+1)}"

        viewHolder.btnCopy.setOnClickListener {

            setClipboard(context = viewHolder.itemView.context , text =  viewHolder.textViewLog.text.toString() )

        }

    }



    fun clearLog(){

        logs.clear()
        filteredLogs.clear()
        notifyDataSetChanged()
        //changed

    }
//    override fun getFilter(): Filter {
//
//        return object : Filter(){
//            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
//
//                filteredLogs = results?.values as MutableList<String>
//                notifyDataSetChanged()
//            }
//
//            override fun performFiltering(constraint: CharSequence?): FilterResults {
//                var charString:String = constraint.toString()
//                if (charString.isEmpty()) {
//                    filteredLogs = logs
//                }else{
//                var filteredLogsLst: MutableList<String> = mutableListOf()
//                    for (row in logs) {
//                        if (row.toLowerCase().contains(charString.toLowerCase())) {
//                            filteredLogsLst.add(row)
//                        }
//                    }
//
//                    filteredLogs = filteredLogsLst
//                }
//
//                var filterResults: FilterResults? = null
//                filterResults?.values = filteredLogs
//                return filterResults!!
//            }
//
//        }
//    }
    override fun getItemCount(): Int {
        return logs.size
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_log2, viewGroup, false)
        return ViewHolder(view)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textViewLog: TextView = itemView.findViewById(R.id.textView_log)
        var tvLogName: TextView = itemView.findViewById(R.id.tvLogName)

        var logNum: TextView = itemView.findViewById(R.id.tvLogNum)
        val btnCopy : FloatingActionButton = itemView.findViewById(R.id.btnCopy)
    }


    //Added
    private fun setClipboard(context: Context, text: String) {

            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = android.content.ClipData.newPlainText("Copied Text", text)
            clipboard.setPrimaryClip(clip)

        Toast.makeText(context,"Text Copied to clipboard",Toast.LENGTH_LONG)
            .show()


    }


}