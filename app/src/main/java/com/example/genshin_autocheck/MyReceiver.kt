package com.example.genshin_autocheck

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.example.genshin_autocheck.Constant.Companion.CHANNEL_ID
import com.example.genshin_autocheck.Constant.Companion.NOTIFICATION_ID
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Constant {
    companion object {
        // 아이디 선언
        const val NOTIFICATION_ID = 0
        const val CHANNEL_ID = "notification_channel"

        // 알림 시간 설정
        const val ALARM_TIMER = 5
    }
}

class MyReceiver : BroadcastReceiver() {

    lateinit var notificationManager: NotificationManager

    override fun onReceive(context: Context, intent: Intent) {
        notificationManager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager


        val appContext = context?.applicationContext ?: return
        senddata(appContext)

        createNotificationChannel()
        deliverNotification(context)
    }

    // 데이터 보내기
    private fun senddata(context: Context) {
        val pref=context.getSharedPreferences("uidtoken", AppCompatActivity.MODE_PRIVATE)
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

    // Notification 을 띄우기 위한 Channel 등록
    fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID, // 채널의 아이디
                "원신 출석체크", // 채널의 이름
                NotificationManager.IMPORTANCE_LOW
                /*
                1. IMPORTANCE_HIGH = 알림음이 울리고 헤드업 알림으로 표시
                2. IMPORTANCE_DEFAULT = 알림음 울림
                3. IMPORTANCE_LOW = 알림음 없음
                4. IMPORTANCE_MIN = 알림음 없고 상태줄 표시 X
                 */
            )
            notificationChannel.enableLights(true) // 불빛
            notificationChannel.lightColor = Color.RED // 색상
            notificationChannel.enableVibration(false) // 진동 여부
            notificationChannel.description = "완료" // 채널 정보
            notificationManager.createNotificationChannel(
                notificationChannel)
        }
    }

    // Notification 등록
    private fun deliverNotification(context: Context){
        val contentIntent = Intent(context, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID, // requestCode
            contentIntent, // 알림 클릭 시 이동할 인텐트
            PendingIntent.FLAG_UPDATE_CURRENT
            /*
            1. FLAG_UPDATE_CURRENT : 현재 PendingIntent를 유지하고, 대신 인텐트의 extra data는 새로 전달된 Intent로 교체
            2. FLAG_CANCEL_CURRENT : 현재 인텐트가 이미 등록되어있다면 삭제, 다시 등록
            3. FLAG_NO_CREATE : 이미 등록된 인텐트가 있다면, null
            4. FLAG_ONE_SHOT : 한번 사용되면, 그 다음에 다시 사용하지 않음
             */
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.gs_icon) // 아이콘
            .setContentTitle("미호요 출첵") // 제목
            .setContentText("완료") // 내용
            .setContentIntent(contentPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

}