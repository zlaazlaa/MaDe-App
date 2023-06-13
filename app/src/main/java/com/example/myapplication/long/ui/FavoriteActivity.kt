package com.example.myapplication.long.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.long.ActivityJumper
import com.example.myapplication.databinding.ActivityFavoriteBinding
import com.example.myapplication.long.ui.adapter.FavoriteContainerItemRecycleViewAdapter
import com.example.myapplication.long.ui.view_model.FavoriteViewModel

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

    override fun jumpToBusStopDetail() {
        Toast.makeText(this,"jump to bus stop Detail",Toast.LENGTH_SHORT).show()
    }

    override fun jumpToBusLineDetail() {
        Toast.makeText(this,"jump to bus line Detail",Toast.LENGTH_SHORT).show()
    }

//    override fun onStop() {
//        viewModel.postFavorite()
//        super.onStop()
//    }
}