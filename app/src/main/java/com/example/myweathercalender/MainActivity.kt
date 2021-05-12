package com.example.myweathercalender

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.AsyncTask
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import java.io.PrintStream
import java.lang.Exception
import java.lang.ref.WeakReference
import java.net.URL
import java.util.*
import java.util.jar.Manifest
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    lateinit var webUrl:String
    lateinit var webUrl2:String
    lateinit var scheduleRecyclerViewAdapter: CalendarAdapter
    val weather = ArrayList<CalData>()
    var caldata = ArrayList<CalData>()
    lateinit var weatherviewtxt:String
    lateinit var today:String
    lateinit var cal:Calendar


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu1, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.weatherOption->{
                showWeatherPopup()
            }
            R.id.tourOption->{
                val i = Intent(this, TourActivity::class.java)
                startActivityForResult(i ,100)
            }
        }

        return super.onOptionsItemSelected(item)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()

    }

    private fun init() {
        val file = PrintStream(openFileOutput("calendar.txt", Context.MODE_APPEND))
        file.close()
        cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.KOREAN)
        lateinit var thistime:String
        if(cal.get(Calendar.HOUR_OF_DAY) < 6){
            thistime = "1800"
            cal.set(Calendar.DATE,cal.get(Calendar.DATE) - 1)
        }else if(cal.get(Calendar.HOUR_OF_DAY) in 6..17){
            thistime = "0600"
        }
        else{
            thistime = "1800"
        }
        today = sdf.format(cal.time).toString()
        webUrl = "http://apis.data.go.kr/1360000/MidFcstInfoService/getMidLandFcst?serviceKey=4kHoDJy4e42REWeE36BlB0rP2ihiE5RvFYWxwPpzpsFWyr63rDeXwnXy4DdYTdRqROzJQdffxKqtGM64WcgaCg%3D%3D&numOfRows=10&pageNo=1&regId=11B00000&tmFc=$today$thistime"
        webUrl2 = "http://apis.data.go.kr/1360000/MidFcstInfoService/getMidFcst?serviceKey=4kHoDJy4e42REWeE36BlB0rP2ihiE5RvFYWxwPpzpsFWyr63rDeXwnXy4DdYTdRqROzJQdffxKqtGM64WcgaCg%3D%3D&pageNo=1&numOfRows=10&dataType=XML&stnId=108&tmFc=$today$thistime"
        recyclerView.layoutManager = GridLayoutManager(this, BaseCalendar.DAYS_OF_WEEK)
        scheduleRecyclerViewAdapter = CalendarAdapter(this, weather, caldata)
        scheduleRecyclerViewAdapter.ItemClickListener=object:CalendarAdapter.OnItemClickListener{
            override fun OnItemClick(
                holder: CalendarAdapter.MyViewHolder,
                view: View,
                date:String
            ) {
                calPopup(date)
            }

        }
        tv_prev_month.setOnClickListener{
            scheduleRecyclerViewAdapter.changeToPrevMonth()
        }

        tv_next_month.setOnClickListener{
            scheduleRecyclerViewAdapter.changeToNextMonth()
        }
        startXMLTask()
        recyclerView.adapter = scheduleRecyclerViewAdapter

        startXMLTask2()
        readdata()
    }
    private fun calPopup(datein:String){
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE)as LayoutInflater
        val view = inflater.inflate(R.layout.cal_popup,null)
        val txt:EditText = view.findViewById(R.id.editText)
        val date:TextView = view.findViewById(R.id.textDate)
        date.text = datein
        val alertDialog = AlertDialog.Builder(this)
            .setPositiveButton("저장"){
                dialogInterface, i ->
                writedata(datein,txt.text.toString())
                readdata()
            }
            .setTitle("기록하기")
            .create()

        alertDialog.setView(view)
        alertDialog.show()
    }

    private fun showWeatherPopup() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE)as LayoutInflater
        val view = inflater.inflate(R.layout.weather_popup, null)
        val txt:TextView = view.findViewById(R.id.textweatherview)
        txt.text = weatherviewtxt

        val alertDialog = AlertDialog.Builder(this)
            .setPositiveButton("확인",null)
            .setTitle("기상 전망")
            .create()

        alertDialog.setView(view)
        alertDialog.show()
    }

    fun startXMLTask(){
        val task = MyAsyncTask(this)
        textInform.text = "날씨 데이터 로딩중"
        task.execute(URL(webUrl))
    }
    fun startXMLTask2(){
        val task = MyAsyncTask2(this)
        task.execute(URL(webUrl2))
    }

    fun refreshCurrentMonth(calendar:Calendar){
        val sdf = SimpleDateFormat("yyyy MM", Locale.KOREAN)
        tv_current_month.text = sdf.format(calendar.time)
    }

    class MyAsyncTask2(context: MainActivity):AsyncTask<URL,Unit,Unit>(){
        val activityreferences = WeakReference(context)
        override fun doInBackground(vararg p0: URL?) {
            val activity = activityreferences.get()
            val doc = Jsoup.connect(p0[0].toString()).parser(Parser.xmlParser()).get()
            val str = doc.select("wfSv")
            activity?.weatherviewtxt = str.text()
        }

    }
    class MyAsyncTask(context:MainActivity):AsyncTask<URL,Unit,Unit>(){
        val activityreferences = WeakReference(context)
        override fun doInBackground(vararg p0: URL?) {

            val dateweather = arrayOf("wf3Am","wf3Pm","wf4Am","wf4Pm","wf5Am","wf5Pm","wf6Am","wf6Pm","wf7Am","wf7Pm")
            val dateweather2 = arrayOf("wf8","wf9","wf10")
            val activity = activityreferences.get()
            try {
                val doc = Jsoup.connect(p0[0].toString()).parser(Parser.xmlParser()).get()
                val weat = doc.select("item")
                val sdf = SimpleDateFormat("yyyyMMdd", Locale.KOREAN)
                val cale = activity?.cal?.clone() as Calendar
                cale.set(Calendar.DATE, cale.get(Calendar.DATE) + 3)
                var idx = "Am"
                for (date in dateweather) {
                    var cdate = sdf.format(cale.time).toString()
                    if(idx == "Pm") {
                        cale.set(Calendar.DATE, cale.get(Calendar.DATE) + 1)
                        cdate+="Pm"
                        idx = "Am"
                    }
                    else {
                        cdate+="Am"
                        idx = "Pm"
                    }
                    val content = weat.select(date).text()
                    val caldata = CalData(cdate,content)
                    activity?.weather?.add(caldata)
                }
                for(date in dateweather2){
                    var cdate = sdf.format(cale.time).toString()+"Da"
                    val content = weat.select(date).text()
                    val caldata = CalData(cdate,content)
                    activity?.weather?.add(caldata)
                    cale.set(Calendar.DATE, cale.get(Calendar.DATE) + 1)
                }
            }catch (e:Exception){

            }
        }

        override fun onPostExecute(result: Unit?) {
            super.onPostExecute(result)
            val activity = activityreferences.get()
            activity?.textInform?.text = ""
            activity?.textInform?.visibility = View.GONE
            if(activity == null || activity.isFinishing){
                return
            }
            activity.scheduleRecyclerViewAdapter.notifyDataSetChanged()
        }
    }

    fun readdata(){
        val scan = Scanner(openFileInput("calendar.txt"))
        caldata.clear()
        while(scan.hasNextLine()){
            val caldate = scan.nextLine()
            val content = scan.nextLine()
            val cd = CalData(caldate, content)
            caldata.add(cd)
        }
        scan.close()
        scheduleRecyclerViewAdapter.notifyDataSetChanged()
    }

    fun writedata(date:String, cont:String){
        val file = PrintStream(openFileOutput("calendar.txt", Context.MODE_APPEND))
        file.println(date)
        file.println(cont)
        file.close()

    }

}
