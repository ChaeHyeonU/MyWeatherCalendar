package com.example.myweathercalender

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class BaseCalendar {
    companion object{
        const val DAYS_OF_WEEK = 7
        const val LOW_OF_CALENDAR = 6
    }

    val calendar = Calendar.getInstance()

    var prevMonthTailOffset = 0
    var nextMonthHeadOffset = 0
    var currentMonthMaxDate = 0

    var datedata = arrayListOf<String>()

    init{
        calendar.time = Date()
    }

    fun initBaseCalendar(refreshCallback:(Calendar)->Unit){
        makeMonthDate(refreshCallback)
    }

    fun changeToPrevMonth(refreshCallback: (Calendar) -> Unit){
        if(calendar.get(Calendar.MONTH) == 0){
            calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 1)
            calendar.set(Calendar.MONTH, Calendar.DECEMBER)
        }else{
            calendar.set(Calendar.MONTH,calendar.get(Calendar.MONTH) - 1)
        }
        makeMonthDate(refreshCallback)
    }

    fun changeToNextMonth(refreshCallback: (Calendar) -> Unit){
        if(calendar.get(Calendar.MONTH) == Calendar.DECEMBER){
            calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1)
            calendar.set(Calendar.MONTH, 0)
        }else{
            calendar.set(Calendar.MONTH,calendar.get(Calendar.MONTH) + 1)
        }
        makeMonthDate(refreshCallback)
    }

    private fun makeMonthDate(refreshCallback: (Calendar) -> Unit) {
        datedata.clear()

        calendar.set(Calendar.DATE, 1)
        currentMonthMaxDate = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        prevMonthTailOffset = calendar.get(Calendar.DAY_OF_WEEK) - 1

        makePrevMonthTail(calendar.clone() as Calendar)
        makeCurrentMonth(calendar.clone() as Calendar)

        nextMonthHeadOffset = LOW_OF_CALENDAR * DAYS_OF_WEEK - (prevMonthTailOffset + currentMonthMaxDate)
        makeNextMonthHead(calendar.clone() as Calendar)

        refreshCallback(calendar)
    }

    private fun makePrevMonthTail(calendar: Calendar) {
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1)
        val maxDate = calendar.getActualMaximum(Calendar.DATE)
        var maxOffsetDate = maxDate - prevMonthTailOffset

        for (i in 1..prevMonthTailOffset) {
            val sdf = SimpleDateFormat("yyyyMM", Locale.KOREAN)
            var today = sdf.format(calendar.time) + (++maxOffsetDate).toString()
            datedata.add(today)
        }
    }

    private fun makeCurrentMonth(calendar: Calendar) {
        for (i in 1..calendar.getActualMaximum(Calendar.DATE)){
            val sdf = SimpleDateFormat("yyyyMMdd", Locale.KOREAN)
            var today = sdf.format(calendar.time)
            calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + 1)
            datedata.add(today)
        }
    }

    private fun makeNextMonthHead(calendar: Calendar) {
        if(calendar.get(Calendar.MONTH) == Calendar.DECEMBER){
            calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1)
            calendar.set(Calendar.MONTH, 0)
        }else{
            calendar.set(Calendar.MONTH,calendar.get(Calendar.MONTH) + 1)
        }
        for (i in 1..nextMonthHeadOffset) {
            val sdf = SimpleDateFormat("yyyyMMdd", Locale.KOREAN)
            var today = sdf.format(calendar.time)
            calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + 1)
            datedata.add(today)
        }
    }

}