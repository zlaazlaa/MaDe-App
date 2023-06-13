package com.example.myapplication.long

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.myapplication.databinding.ActivityMainLongBinding
import com.example.myapplication.long.ui.*

class LongMainActivity : AppCompatActivity() {
    private var binding :ActivityMainLongBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainLongBinding.inflate(layoutInflater)
        binding?.run {
            setContentView(root)
            button.setOnClickListener{
                val intent = Intent(this@LongMainActivity, CityActivity::class.java)
                intent.putExtra(KEY_CITY_NAME,"上海")
                intent.putExtra(KEY_CITY_STR,"shanghai")
                intent.putExtra(KEY_CITY_USER,"cwl")
                startActivity(intent)
            }
            button2.setOnClickListener{
                val intent = Intent(this@LongMainActivity, FavoriteActivity::class.java)
                intent.putExtra(KEY_FAVORITE_USER,"cwl")
                startActivity(intent)
            }
        }
    }
}