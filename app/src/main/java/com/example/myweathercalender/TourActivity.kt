package com.example.myweathercalender

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_tour.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser
import org.w3c.dom.NodeList
import java.io.IOException
import java.lang.ref.WeakReference
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList

class TourActivity : AppCompatActivity() {

    val servicekey = "4kHoDJy4e42REWeE36BlB0rP2ihiE5RvFYWxwPpzpsFWyr63rDeXwnXy4DdYTdRqROzJQdffxKqtGM64WcgaCg%3D%3D"
    var locationManger :LocationManager?=null
    lateinit var webUrl:String
    private val REQUEST_CODE_LOCATION:Int = 2
    var currentLocation : String = ""
    var latitude :Double?=null
    var longitude :Double?=null
    var tourdata = ArrayList<TourData>()
    lateinit var adapter:TourAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tour)
        getCurrentLoc()
        Log.i("RRRR",currentLocation)

        Log.i("RRRR",longitude.toString())
        init()
    }

    private fun init() {
        webUrl ="http://api.visitkorea.or.kr/openapi/service/rest/KorService/locationBasedList?serviceKey=$servicekey&numOfRows=20&pageNo=1&MobileOS=ETC&MobileApp=AppTest&arrange=B&contentTypeId=12&mapX=126.981611&mapY=37.568477&radius=1000&listYN=Y&modifiedtime=&"
        tourView.layoutManager =LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        adapter = TourAdapter(tourdata)
        adapter.ItemClickListener=object:TourAdapter.OnItemClickListener{
            override fun OnItemClick(holder: TourAdapter.MyViewHolder, view: View, data: String) {
                showWeatherPopup(data)
                val i = Intent()
                i.putExtra("data",data)
                setResult(Activity.RESULT_OK,i)
            }

        }
        startXMLTask()
        tourView.adapter = adapter
    }

    fun startXMLTask(){
        val task = MyAsyncTaskTour(this)
        task.execute(URL(webUrl))
    }

    class MyAsyncTaskTour(context: TourActivity):AsyncTask<URL,Unit,Unit>(){
        val activityreferences = WeakReference(context)
        override fun doInBackground(vararg p0: URL?) {
            val activity = activityreferences.get()
            val doc = Jsoup.connect(p0[0].toString()).parser(Parser.xmlParser()).get()
            doc.normalise()
//            val str = doc.select("item")
            for(i in 0 until 10){
                val t1 = doc.select("title")[i].text()
                Log.i("FFFF",t1)
                val t2 = doc.select("dist")[i].text()
                val t3 = doc.select("firstimage")[i].text()
                Log.i("FFFF",t3)
                val t4 = doc.select("firstimage2")[i].text()
                val ttt = TourData(t1,t2,t3,t4)
                activity?.tourdata?.add(ttt)

            }
        }

        override fun onPostExecute(result: Unit?) {
            super.onPostExecute(result)
            val activity = activityreferences.get()
            if(activity == null || activity.isFinishing){
                return
            }
            activity.adapter.notifyDataSetChanged()
        }

    }

    private fun showWeatherPopup(title:String) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE)as LayoutInflater
        val view = inflater.inflate(R.layout.weather_popup, null)
        val txt: TextView = view.findViewById(R.id.textweatherview)
        txt.text = title+"을 선택 하시겠습니까?"

        val alertDialog = AlertDialog.Builder(this)
            .setPositiveButton("예", null)
            .setTitle("여행지 선정")
            .setNegativeButton("취소", null)
            .create()

        alertDialog.setView(view)
        alertDialog.show()
    }

    private fun getCurrentLoc() {
        locationManger = getSystemService(Context.LOCATION_SERVICE)as LocationManager?
        var userLocation:Location = getLatLng()
        if(userLocation != null)
        {
            latitude = userLocation.latitude
            longitude = userLocation.longitude
            Log.d("CheckCurrentLocation", "현재 내 위치 $latitude, $longitude")
            var mGeocoder = Geocoder(applicationContext, Locale.KOREAN)
            var mResultList:List<Address>? = null
            try{
                mResultList = mGeocoder.getFromLocation(
                    latitude!!, longitude!!, 1
                )
            } catch (e:IOException){
                e.printStackTrace()
            }
            if(mResultList != null){
                currentLocation = mResultList[0].getAddressLine(0)
                //currentLocation = currentLocation.substring(11)
            }
        }
    }

    private fun getLatLng(): Location {
        var currentLatLng:Location? = null
        if(ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(applicationContext,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), this.REQUEST_CODE_LOCATION)
            getLatLng()
        } else{
            val locationProvider = LocationManager.GPS_PROVIDER
            currentLatLng = locationManger?.getLastKnownLocation(locationProvider)
        }
        return currentLatLng!!
    }


}
