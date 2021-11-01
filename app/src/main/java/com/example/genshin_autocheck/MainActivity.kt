package com.example.genshin_autocheck

import android.app.AlarmManager
import android.app.AlarmManager.INTERVAL_DAY
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import com.example.genshin_autocheck.Constant.Companion.NOTIFICATION_ID
import com.example.genshin_autocheck.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.os.Build
import android.view.WindowInsets
import android.view.WindowManager


class MainActivity : AppCompatActivity() {
    // 뷰바인딩
    private var mBinding: ActivityMainBinding? = null
    private val binding get() = mBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 뷰바인딩
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 상태바 없애기
        var actionBar : androidx.appcompat.app.ActionBar?
        actionBar = supportActionBar
        actionBar?.hide()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, MyReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, NOTIFICATION_ID, intent,
            PendingIntent.FLAG_UPDATE_CURRENT)

        val pref = this?.getSharedPreferences("uidtoken", MODE_PRIVATE)
        if(pref.getString("qiqi", "off") == "on") {
            binding.toggleButton.setBackgroundResource(R.drawable.qiqi_on)
            binding.txtAct.setTextColor(getColor(R.color.green))
            binding.txtAct.setText("자동출첵 on \n종료하려면 치치를 누르세요")

        } else{
            binding.toggleButton.setBackgroundResource(R.drawable.qiqi_off)
            binding.txtAct.setTextColor(getColor(R.color.red))
            binding.txtAct.setText("자동출첵 off \n시작하려면 치치를 누르세요")
        }


        if(pref.getString("ltuid", "null") != "null") {
            var ltuid = pref.getString("ltuid", "null")
            binding.editLtuid.setText(ltuid)
        }
        if(pref.getString("ltoken", "null") != "null") {
            var ltoken = pref.getString("ltoken", "null")
            binding.editLtoken.setText(ltoken)
        }


        binding.toggleButton.setOnClickListener{
            // 토글 on
            if(binding.toggleButton.isChecked == true) {
                senddata(this)
                saveIdata("on")
                // 매일 누른시간에 출석체크
                val triggerTime = (SystemClock.elapsedRealtime()) // 기기가 부팅된 후 경과한 시간 사용
                alarmManager.setInexactRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerTime + INTERVAL_DAY, INTERVAL_DAY,
                    pendingIntent // 반복할 작업(onReceive)
                ) // setInexactRepeating : 반복성 알림,
                Toast.makeText(this, "자동출석이 시작되었습니다", Toast.LENGTH_SHORT).show()
                binding.toggleButton.setBackgroundResource(R.drawable.qiqi_on)
                binding.txtAct.setTextColor(getColor(R.color.green))
                binding.txtAct.setText("자동출첵 on \n종료하려면 치치를 누르세요")
            }
            // 토글 off
            else {
                val pref = this?.getSharedPreferences("uidtoken", MODE_PRIVATE)
                val editor = pref.edit()
                editor.remove("ltuid")
                    .remove("ltoken")
                    .remove("qiqi")
                    .commit()
                alarmManager.cancel(pendingIntent)
                Toast.makeText(this, "자동출석이 종료되었습니다", Toast.LENGTH_SHORT).show()
                binding.toggleButton.setBackgroundResource(R.drawable.qiqi_off)
                binding.txtAct.setTextColor(getColor(R.color.red))
                binding.txtAct.setText("자동출첵 off \n시작하려면 치치를 누르세요")
            }
        }
    }

    // 데이터 저장
    private fun saveIdata(qiqi : String) {
        val pref = this?.getSharedPreferences("uidtoken", MODE_PRIVATE)
        val editor = pref.edit()
        val ltuid = binding.editLtuid.text.toString()
        val ltoken = binding.editLtoken.text.toString()

        editor.putString("ltuid", ltuid)
            .putString("ltoken", ltoken)
            .putString("qiqi", qiqi)
            .commit()
    }

    // 데이터 보내기
    private fun senddata(context: Context) {
        val pref = this.getSharedPreferences("uidtoken", AppCompatActivity.MODE_PRIVATE)
        var ltuid = pref.getString("ltuid"," ")!!
        var ltoken = pref.getString("ltoken"," ")!!

        val BASE_URL_Mihoyo = "https://hk4e-api-os.mihoyo.com/"
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL_Mihoyo)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(CheckAPI::class.java)

        val cookies = "ltuid=${ltuid};ltoken=${ltoken};"

        val PostCookie = api.putCookie(cookies)
        PostCookie.enqueue(object : Callback<Message> {
            override fun onResponse(call: Call<Message>, response: Response<Message>) {
                Log.d(ContentValues.TAG, "성공 : ${response.raw()}")
            }
            override fun onFailure(call: Call<Message>, t: Throwable) {
                Log.d(ContentValues.TAG, "실패 : $t")
            }
        })
    }

    override fun onDestroy() {
        mBinding = null
        super.onDestroy()
    }
}