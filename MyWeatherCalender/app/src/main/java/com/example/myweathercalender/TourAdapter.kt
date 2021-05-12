package com.example.myweathercalender

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.lang.System.load
import java.net.URL

class TourAdapter( val tour:ArrayList<TourData>)
    :RecyclerView.Adapter<TourAdapter.MyViewHolder>(){

    interface OnItemClickListener{
        fun OnItemClick(holder:MyViewHolder, view: View, data:String)
    }
    var ItemClickListener:OnItemClickListener?=null

    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var title: TextView = itemView.findViewById(R.id.titleText)
        var distance: TextView = itemView.findViewById(R.id.distText)
        var img1: ImageView = itemView.findViewById(R.id.imageView1)
        var img2: ImageView = itemView.findViewById(R.id.imageView2)

        init{
            itemView.setOnClickListener{
                ItemClickListener?.OnItemClick(this, it, tour[adapterPosition].title)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder  {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_tour, parent, false)
        return MyViewHolder(v)
    }

    override fun getItemCount(): Int {
        return tour.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Log.i("TTTT", tour[position].title)
        holder.title.text =tour[position].title
        holder.distance.text = "거리 : "+tour[position].distance +"m"

        Glide.with(holder.itemView).load(tour[position].img1).into(holder.img1)
        Glide.with(holder.itemView).load(tour[position].img2).into(holder.img2)
    }

    class URLtoBitmapTask(): AsyncTask<Unit, Unit, Bitmap>() {
        lateinit var url:URL
        override fun doInBackground(vararg p0: Unit?): Bitmap {
            val bitmap = BitmapFactory.decodeStream(url.openStream())
            return bitmap
        }

        override fun onPreExecute() {
            super.onPreExecute()

        }

        override fun onPostExecute(result: Bitmap) {
            super.onPostExecute(result)
        }
    }
}