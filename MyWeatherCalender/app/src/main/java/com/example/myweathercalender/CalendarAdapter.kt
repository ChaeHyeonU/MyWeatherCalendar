package com.example.myweathercalender

import android.app.ActionBar
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import android.util.Xml
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_schedule.view.*
import java.util.*
import java.util.logging.Handler
import kotlin.coroutines.*
import kotlin.collections.ArrayList

class CalendarAdapter(val mainActivity: MainActivity, val weather:ArrayList<CalData>, val contentdata:ArrayList<CalData>)
    :RecyclerView.Adapter<CalendarAdapter.MyViewHolder>() {

    interface OnItemClickListener{
        fun OnItemClick(holder:MyViewHolder, view:View, date:String)
    }

    var ItemClickListener:OnItemClickListener?=null
    val baseCalendar = BaseCalendar()
    val thismonth = Calendar.getInstance().get(Calendar.MONTH)
    var arrweatdate = mutableMapOf<String,String>()
    var todayindex:Int

    init{
        if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 6){
            todayindex = baseCalendar.prevMonthTailOffset + Calendar.getInstance().get(Calendar.DATE) - 1
        }else{
            todayindex = baseCalendar.prevMonthTailOffset + Calendar.getInstance().get(Calendar.DATE)
        }
        baseCalendar.initBaseCalendar {
            refreshView(it)
        }

    }

    inner class MyViewHolder(override val containerView: View):RecyclerView.ViewHolder(containerView), LayoutContainer{
        var datetext: TextView = itemView.findViewById(R.id.tv_date)
        var weatherlay: LinearLayout = itemView.findViewById(R.id.weatLay)
        var weatheramimg: ImageView = itemView.findViewById(R.id.tv_weather_am)
        var weatherpmimg: ImageView = itemView.findViewById(R.id.tv_weather_pm)
        var slashimg: ImageView = itemView.findViewById(R.id.tv_slash)
        var cont:TextView = itemView.findViewById(R.id.textLine)
        init{
            containerView.setOnClickListener{
                ItemClickListener?.OnItemClick(this,it,baseCalendar.datedata[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_schedule,parent,false)
        if(weather.size>12){
            arrweatdate.clear()
            for(i in 0 until weather.size){
                arrweatdate.put(weather[i].todate,weather[i].content)
            }
        }
        return MyViewHolder(v)
    }

    override fun getItemCount(): Int {

        return BaseCalendar.LOW_OF_CALENDAR * BaseCalendar.DAYS_OF_WEEK
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        if(position % BaseCalendar.DAYS_OF_WEEK == 0)
            holder.datetext.setTextColor(Color.parseColor("#ff1200"))
        else if(position % BaseCalendar.DAYS_OF_WEEK == 6)
            holder.datetext.setTextColor(Color.parseColor("#3792E1"))
        else holder.datetext.setTextColor(Color.parseColor("#676d6e"))

        if(position < baseCalendar.prevMonthTailOffset
            || position >= baseCalendar.prevMonthTailOffset + baseCalendar.currentMonthMaxDate){
            holder.datetext.alpha = 0.5f
            holder.weatheramimg.alpha = 0.5f
            holder.weatherpmimg.alpha = 0.5f
            holder.slashimg.alpha = 0.5f
        }else{
            holder.datetext.alpha = 1f
            holder.weatheramimg.alpha = 1f
            holder.weatherpmimg.alpha = 1f
            holder.slashimg.alpha = 1f
        }
        if(position == todayindex && baseCalendar.calendar.get(Calendar.MONTH) == thismonth)
            holder.containerView.backlayout.setBackgroundResource(R.color.colorToday)
        //
        if(weather.size > 12){
            holder.weatheramimg.visibility = View.VISIBLE
            holder.weatherpmimg.visibility = View.VISIBLE
            holder.slashimg.visibility = View.VISIBLE
            holder.weatherlay.visibility = View.VISIBLE
            if(arrweatdate[baseCalendar.datedata[position]+"Am"] != null){
                holder.weatheramimg.setImageResource(chooseimg(arrweatdate[baseCalendar.datedata[position]+"Am"]!!))
            }
            if(arrweatdate[baseCalendar.datedata[position]+"Pm"] != null){
                holder.weatherpmimg.setImageResource(chooseimg(arrweatdate[baseCalendar.datedata[position]+"Pm"]!!))
            }
            if(arrweatdate[baseCalendar.datedata[position]+"Da"] != null){
                holder.weatherpmimg.visibility = View.GONE
                holder.slashimg.visibility = View.GONE
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,0.8f)
                holder.weatheramimg.layoutParams = params
                holder.weatheramimg.setImageResource(chooseimg(arrweatdate[baseCalendar.datedata[position]+"Da"]!!))
            }
            if(arrweatdate[baseCalendar.datedata[position]+"Da"] == null && arrweatdate[baseCalendar.datedata[position]+"Pm"] == null && arrweatdate[baseCalendar.datedata[position]+"Am"] == null){
                holder.weatheramimg.visibility = View.GONE
                holder.weatherpmimg.visibility = View.GONE
                holder.slashimg.visibility = View.GONE
                holder.weatherlay.visibility = View.INVISIBLE
            }
        }


        var slicedate = baseCalendar.datedata[position].slice(6..7)
        if(slicedate[0] == '0'){
            holder.datetext.text = slicedate[1].toString()
        }else
            holder.datetext.text = slicedate

        val contarr = getcont(baseCalendar.datedata[position])
        if(contarr.size != 0){
            var contxt = ""
            for(i in 0 until contarr.size){
                contxt+=contarr[i]+"\n"
            }
            holder.cont.setBackgroundResource(R.color.colorContent)
            holder.cont.text = contxt
        }

    }

    fun getcont(date:String):ArrayList<String>{
        val arr = ArrayList<String>()
        for(i in 0 until contentdata.size){
            if(date == contentdata[i].todate){
                arr.add(contentdata[i].content)
            }
        }
        return arr
    }

    fun changeToPrevMonth(){
        baseCalendar.changeToPrevMonth {
            refreshView(it)
        }
    }

    fun changeToNextMonth(){
        baseCalendar.changeToNextMonth {
            refreshView(it)
        }
    }

    private fun refreshView(calendar: Calendar) {

        notifyDataSetChanged()
        mainActivity.refreshCurrentMonth(calendar)
    }

    private fun chooseimg(str:String):Int{
        when(str){
            "맑음"->return R.drawable.sun
            "구름많음"->return R.drawable.cloud
            "구름많고 비"->return R.drawable.cloud_rain
            "구름많고 눈"->return R.drawable.snow
            "구름많고 비/눈"->return R.drawable.cloud_rain
            "구름많고 눈/비"->return R.drawable.snow
            "흐림"->return R.drawable.clouds_sun
            "흐리고 비"->return R.drawable.cloud_rain_sun
            "흐리고 눈"->return R.drawable.snow
            "흐리고 비/눈"->return R.drawable.cloud_rain_sun
            "흐리고 눈/비"->return R.drawable.snow
        }
        return 0
    }

}