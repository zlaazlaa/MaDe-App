package com.example.myapplication

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.widget.Button
import android.widget.EdgeEffect
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.amap.api.maps.MapsInitializer


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val button = findViewById<Button>(R.id.myButton)
        button.setOnClickListener {
            val intent = Intent(this, BuslineActivity::class.java)
            intent.putExtra("bus", findViewById<EditText>(R.id.bus).text.toString())
            intent.putExtra("city", findViewById<EditText>(R.id.city).text.toString())
            startActivity(intent)
        }
        privacyCompliance()
    }
    private fun privacyCompliance() {
        MapsInitializer.updatePrivacyShow(this@MainActivity, true, true)
        val spannable =
            SpannableStringBuilder("\"亲，感谢您对XXX一直以来的信任！我们依据最新的监管要求更新了XXX《隐私权政策》，特向您说明如下\n1.为向您提供交易相关基本功能，我们会收集、使用必要的信息；\n2.基于您的明示授权，我们可能会获取您的位置（为您提供附近的商品、店铺及优惠资讯等）等信息，您有权拒绝或取消授权；\n3.我们会采取业界先进的安全措施保护您的信息安全；\n4.未经您同意，我们不会从第三方处获取、共享或向提供您的信息；\n")
        spannable.setSpan(
            ForegroundColorSpan(Color.BLUE),
            35,
            42,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        AlertDialog.Builder(this)
            .setTitle("温馨提示(隐私合规示例)")
            .setMessage(spannable)
            .setPositiveButton(
                "同意"
            ) { dialogInterface, i -> MapsInitializer.updatePrivacyAgree(this@MainActivity, true) }
            .setNegativeButton(
                "不同意"
            ) { dialogInterface, i -> MapsInitializer.updatePrivacyAgree(this@MainActivity, false) }
            .show()
    }
}