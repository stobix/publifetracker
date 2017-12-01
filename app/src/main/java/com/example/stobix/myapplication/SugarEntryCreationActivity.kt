package com.example.stobix.myapplication

import android.annotation.SuppressLint
import android.app.DialogFragment
import android.os.Bundle
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.stobix.myapplication.NumberPickerDialog.OnNumberSetListener

/**
 * A dialog for creating an entry to the blood sugar database
 * Created by stobix on 11/19/17.
 */

open class SugarEntryCreationActivity
@SuppressLint("ValidFragment") constructor
(
) : DialogFragment( )
        ,DatePickerFragment.DatePickerHandler
        ,TimePickerFragment.TimePickerHandler
        ,SendResultAble
        ,OnNumberSetListener
{

    private var uid: Int=0
    private var date: DateHandler = DateHandler()
    private var sugarLevel: Float? = null
    private var dateView: TextView? = null
    private var timeView: TextView? = null
    private var sugarView: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        d("SugarEntry create","weeeee")
        super.onCreate(savedInstanceState)
        uid = arguments.getInt("uid")
        date.timestamp = arguments.getLong("timestamp")
    }

    override fun receiveResult(type: String, vararg results: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        d("SugarEntry view","weeeee")
        val v = inflater?.inflate(R.layout.activity_sugar_entry_creation, container, false)

        v ?:throw Error("could not create view")

        dateView= v.findViewById(R.id.entryCreatorDate)
        timeView= v.findViewById(R.id.entryCreatorTime)
        sugarView= v.findViewById(R.id.entryCreatorSugar)

        val dateText="%d-%02d-%02d".format(date.year,date.month+1,date.day)
        val timeText=""+date.hour+":"+date.minute // +s":"+date.second
        dateView!!.text=dateText
        timeView!!.text=timeText

        dateView!!.setOnClickListener { (activity as MainActivity).showDatePicker() }
        timeView!!.setOnClickListener { (activity as MainActivity).showTimePicker() }
        sugarView!!.setOnClickListener{ (activity as MainActivity).showNumberPicker() }
        val sugarV = v.findViewById<TextView>(R.id.entryCreatorSugar)
        val extraV = v.findViewById<TextView>(R.id.entryCreatorExtra)

        v.findViewById<Button>(R.id.entryAdd).setOnClickListener {onSubmit(sugarV,extraV)}
        v.findViewById<Button>(R.id.entryClose).setOnClickListener { onClose() }
        v.findViewById<Button>(R.id.entryAddClose).setOnClickListener { onSubmitAndClose(sugarV,extraV) }

        return v

    }

    // Sending a full SugarEntry since I'm not sure what fields it will contain in the future.
    interface OnSugarEntryEnteredHandler {
        fun onSugarEntryEntered(s: SugarEntry)
    }

    private fun onSubmit(sugarV: TextView, extraV: TextView) {
        handleSubmission(sugarV, extraV)
        uid++
    }

    private fun onSubmitAndClose(sugarV: TextView, extraV: TextView) {
        handleSubmission(sugarV, extraV)
        this.dismiss()
    }

    private fun handleSubmission(sugarV: TextView, extraV: TextView){
        val entry= SugarEntry(uid,date.timestamp)
        entry.sugarLevel = sugarLevel?.times(10)?.toInt() ?: -1
        entry.extra = extraV.text?.toString()?:"N/A"
        d("SugarEntry submit", ""+entry.uid+" "+entry.epochTimestamp+" "+entry.sugarLevel+" "+entry.extra)
        (activity as OnSugarEntryEnteredHandler).onSugarEntryEntered(entry)
    }

    private fun onClose(){
        this.dismiss()
    }

    override fun handleDate(year: Int, month: Int, day: Int) {
        d("SugarEntry got date", "$year-$month-$day")
        date.setDate(year,month,day)
        // Calendars use a 0-indexed gregorian/julian month for some reason!
        val dateText="%d-%02d-%02d".format(year,month+1,day)
        dateView!!.setText(dateText, TextView.BufferType.NORMAL)
    }

    override fun handleTime(hour: Int, minute: Int) {
        d("SugarEntry got time", "$hour:$minute")
        date.setTime(hour,minute)
        val timeText= "%02d:%02d".format(hour,minute)
        timeView!!.setText(timeText, TextView.BufferType.NORMAL)
    }

    override fun onNumberSet(view: NumberPickerDialog, number: Float) {
        d("number set!","$number")
        sugarLevel=number
        sugarView?.setText(number.toString(),TextView.BufferType.NORMAL)
    }

    override fun onNumberClear(view: NumberPickerDialog) {
        sugarLevel=null
        sugarView?.setText("",TextView.BufferType.NORMAL)
    }

    companion object{
        fun newInstance(uid: Int) = newInstance(uid,DateHandler().timestamp)
        fun newInstance(uid: Int, timestamp: Long ) : SugarEntryCreationActivity{
            val s = SugarEntryCreationActivity()
            val args = Bundle()
            d("EntryCreation","Called with uid:"+uid+" timestamp:"+timestamp)
            args.putInt("uid",uid)
            args.putLong("timestamp",timestamp)
            s.arguments=args
            return s
        }
    }
}

