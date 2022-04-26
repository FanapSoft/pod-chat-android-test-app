package ir.fanap.chattestapp.application.ui.test

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import ir.fanap.chattestapp.R
import kotlinx.android.synthetic.main.demo_spinner.view.*

class NumbersSpinnerAdapter(ctx: Context?,
                            moods: List<Int>) :
    ArrayAdapter<Int>(ctx!!, 0, moods) {

    override fun getView(position: Int, recycledView: View?, parent: ViewGroup): View {
        return this.createView(position, recycledView, parent)
    }

    override fun getDropDownView(position: Int, recycledView: View?, parent: ViewGroup): View {
        return this.createView(position, recycledView, parent)
    }

    private fun createView(position: Int, recycledView: View?, parent: ViewGroup): View {

        val mood = getItem(position)

        val view = recycledView ?: LayoutInflater.from(context).inflate(
            R.layout.demo_spinner,
            parent,
            false
        )

        view.moodText.text = mood.toString()

        return view
    }
}