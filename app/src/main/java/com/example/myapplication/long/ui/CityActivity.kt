package com.example.myapplication.long.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.Bus_Route_Details
import com.example.myapplication.YangMainActivity
import com.example.myapplication.long.ActivityJumper
import com.example.myapplication.databinding.ActivityCityLongBinding
import com.example.myapplication.databinding.BottomSheetSearchBinding
import com.example.myapplication.long.model.City
import com.example.myapplication.long.model.StopOrLine
import com.example.myapplication.long.ui.adapter.BusStopItemRecyclerViewAdapter
import com.example.myapplication.long.ui.adapter.SearchItemAdapter
import com.example.myapplication.long.ui.view_model.CityViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.util.*
import kotlin.collections.ArrayList


const val KEY_CITY_NAME = "com.example.myapplication.ui.cityActivity.cityName"
const val KEY_CITY_STR = "com.example.myapplication.ui.cityActivity.cityStr"
const val KEY_CITY_USER = "com.example.myapplication.ui.cityActivity.user"

class CityActivity : AppCompatActivity(), ActivityJumper {
    private var binding: ActivityCityLongBinding? = null

    private val viewModel: CityViewModel by viewModels()
    private var adapter: BusStopItemRecyclerViewAdapter? = null

    private var canSearch = false

    //bottom sheet var
    private var bottomSheetAdapter: SearchItemAdapter? = null
    private var bottomSheetBehavior: BottomSheetBehavior<RelativeLayout>? = null

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCityLongBinding.inflate(layoutInflater)
        adapter = BusStopItemRecyclerViewAdapter(viewModel, this)
        val progressDialogFragment = ProgressDialogFragment()
        binding?.run {
            setContentView(root)
            searchInputButton.setOnClickListener {
                if (canSearch)
                    setSearchExpanded()
            }
            with(recycleViewInCityActivity) {
                layoutManager = LinearLayoutManager(this@CityActivity)
                adapter = this@CityActivity.adapter
            }

            searchInputButton.setOnClickListener {
                if (canSearch) {
                    setSearchExpanded()
                }
            }
            initBottomSheet(bottomSheetSearch)
        }
        with(viewModel) {
            data.observe(this@CityActivity) { data ->
                canSearch = true
                this@CityActivity.adapter!!.busStopData = data
            }
            favorites.observe(this@CityActivity) { data ->
                this@CityActivity.adapter!!.setFavorites(data.first, data.second, data.third)
                progressDialogFragment.dismiss()
            }
        }

        intent?.let {
            val cityName = it.getStringExtra(KEY_CITY_NAME)
            val cityStr = it.getStringExtra(KEY_CITY_STR)
            val user = it.getStringExtra(KEY_CITY_USER)
            viewModel.setUserAndCity(
                user!!,
                City(cityName = cityName!!, cityStr = cityStr!!),
            )
        }
        progressDialogFragment.show(supportFragmentManager, "data loading")
    }

    private fun initBottomSheet(bottomSheetBinding: BottomSheetSearchBinding) {
        bottomSheetAdapter = SearchItemAdapter(this)
        with(bottomSheetBinding) {
            backButton.setOnClickListener {
                bottomSheetBehavior?.let {
                    if (it.state == BottomSheetBehavior.STATE_EXPANDED)
                        setSearchCollapse()
                }
            }

            with(recycleViewInSearchFragment) {
                layoutManager = LinearLayoutManager(context)
                adapter = this@CityActivity.bottomSheetAdapter
            }
            searchInput.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    //do nothing
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    //do nothing
                }

                override fun afterTextChanged(s: Editable?) {
                    if (s != null) {
                        if (s.isNotEmpty())
                            showSearchResult(viewModel.search(s.toString()))
                        else showSearchResult()
                    }
                }

            })
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
            bottomSheetBehavior?.peekHeight = 0
        }
    }

    private fun setSearchCollapse() {
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun setSearchExpanded() {
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }


    fun showSearchResult(searchResult: List<StopOrLine> = ArrayList()) {
        bottomSheetAdapter?.dataSet = searchResult
    }

//    override fun onStop() {
//        viewModel.postFavorite()
//        super.onStop()
//    }

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
}