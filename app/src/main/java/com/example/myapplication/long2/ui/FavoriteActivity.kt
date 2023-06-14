package com.example.myapplication.long2.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.Bus_Route_Details
import com.example.myapplication.YangMainActivity
import com.example.myapplication.long2.ActivityJumper
import com.example.myapplication.databinding.ActivityFavoriteBinding
import com.example.myapplication.long2.ui.adapter.FavoriteContainerItemRecycleViewAdapter
import com.example.myapplication.long2.ui.view_model.FavoriteViewModel

const val KEY_FAVORITE_USER = "com.example.myapplication.ui.favoriteActivity.user"
class FavoriteActivity : AppCompatActivity() , ActivityJumper {
    private var binding : ActivityFavoriteBinding? = null
    private val viewModel : FavoriteViewModel by viewModels()
    private var adapter: FavoriteContainerItemRecycleViewAdapter? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteBinding.inflate(layoutInflater)
        adapter = FavoriteContainerItemRecycleViewAdapter(viewModel,this)

        val progressDialogFragment = ProgressDialogFragment()

        with(binding!!){
            setContentView(root)
            with(recycleViewInFavorite){
                layoutManager = LinearLayoutManager(this@FavoriteActivity)
                adapter = this@FavoriteActivity.adapter!!
            }
        }

        viewModel.favorite.observe(this){data->
            adapter!!.setDataSet(
                data.first,
                data.second.first,
                data.second.second,
                data.second.third,
                data.third
            )
            progressDialogFragment.dismiss()
        }

        intent?.let {
            val user = it.getStringExtra(KEY_FAVORITE_USER)
            viewModel.user = user
        }
        progressDialogFragment.show(supportFragmentManager,"data loading")
    }

    override fun jumpToBusStopDetail(city_str: String, city_name: String, station: String) {
        val intent = Intent(this, YangMainActivity::class.java)
        intent.putExtra("city_str", city_str)
        intent.putExtra("city_name", city_name)
        intent.putExtra("station", station)
        Toast.makeText(this, "jump to busStop detail", Toast.LENGTH_SHORT).show()
        startActivity(intent)
    }

    override fun jumpToBusLineDetail(city_str: String, city_name: String, line: String,nowStopName:String) {
        val intent = Intent(this, Bus_Route_Details::class.java)
        intent.putExtra("city_str", city_str)
        intent.putExtra("city_name", city_name)
        intent.putExtra("line", line)
        intent.putExtra("xing_Current_site",nowStopName)
        intent.putExtra("xing_user",viewModel.user!!)
        startActivity(intent)
        Toast.makeText(this, "jump to busLine detail", Toast.LENGTH_SHORT).show()
    }



//    override fun onStop() {
//        viewModel.postFavorite()
//        super.onStop()
//    }
}