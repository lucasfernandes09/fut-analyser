package com.example.futanalyser

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.futanalyser.Constants.Companion.baseUrl
import com.example.futanalyser.Constants.Companion.date
import com.example.futanalyser.Constants.Companion.idChannel
import com.example.futanalyser.Constants.Companion.minToCancel
import com.example.futanalyser.Constants.Companion.userAgent
import com.example.futanalyser.model.Competition
import com.example.futanalyser.model.Match
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

class MainActivity : AppCompatActivity() {

    private val myCompetitions = mutableListOf<Competition>()
    private val myMatches = mutableListOf<Match>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCenter.start(
            application, "6fc8a07e-1e08-4384-9617-368c6d092ca9",
            Analytics::class.java, Crashes::class.java
        )

        createNotificationChannel()
        getMatches()
    }

    private fun getMatches() {
        Thread() {
            val day = LocalDate.now().toString().replace("-", "")
            val doc: Document = Jsoup.connect("$baseUrl$date$day").userAgent(userAgent).maxBodySize(Int.MAX_VALUE).get()

            val groupElements = doc.select("div.css-niuknl-Group")
            groupElements.forEach { groupElement ->
                val nameCompetition = groupElement.select("a.css-1g2mffk-GroupTitleLink").text()
                val isMyCompetition = separeteName(nameCompetition)

                if (isMyCompetition) {
                    val matchElements = groupElement.select("a.css-1r2we3n-MatchWrapper")
                    val matches = setupMatchs(matchElements)
                    val competition = Competition(nameCompetition, matches)
                    myCompetitions.add(competition)
                }
            }

            myCompetitions.forEach {
                myMatches.add(Match(null, null, it.name))
                it.matches?.let { matches -> myMatches.addAll(matches) }
            }

            runOnUiThread {
                val listRecyclerView: RecyclerView = findViewById(R.id.ListRecyclerView)
                listRecyclerView.apply {
                    setHasFixedSize(true)
                    adapter = ListAdapter(myMatches) { isChecked, match ->
                        if (isChecked) setupAlarm(match)
                    }
                }
            }
        }.start()
    }

    private fun setupAlarm(match: Match) {

        val alarmMgr = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val id = System.currentTimeMillis()
        val alarmIntent = Intent(this, AlarmReceiver::class.java).let { intent ->
            intent.putExtra("link", match.link)
            intent.putExtra("name", match.homeVsAway)
            PendingIntent.getBroadcast(this, id.toInt(), intent, 0)
        }

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            val startAt = match.startAt!!
            val hour = if (startAt.isEmpty()) LocalTime.now().hour else startAt.substring(0, 2).toInt()
            val minute = if (startAt.isEmpty()) LocalTime.now().minute else startAt.substring(3, 5).toInt()
            set(Calendar.HOUR_OF_DAY, (hour - 4))
            set(Calendar.MINUTE, minute)
        }

        alarmMgr.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            1000 * 60,
            alarmIntent
        )

        Handler().postDelayed({ alarmMgr.cancel(alarmIntent) }, 1000 * 60 * minToCancel)
    }

    private fun separeteName(name: String?): Boolean {
        return when (name) {
            "ENGLAND - PREMIER LEAGUE" -> true
            "SPAIN - LALIGA" -> true
            "GERMANY - 1. BUNDESLIGA" -> true
            "ITALY - SERIE A" -> true
            "FRANCE - LIGUE 1" -> true
            "CHAMPIONS LEAGUE FINAL STAGE" -> true
            "EUROPA LEAGUE FINAL STAGE" -> true
            else -> false
        }
    }

    private fun setupMatchs(matchElements: Elements): MutableList<Match> {
        val matches = mutableListOf<Match>()

        matchElements.forEach { matchElement ->
            val link = matchElement.attr("href")
            val startAt = matchElement.select("span.css-8o8lqm").text()
            val homeVsAway = matchElement.text()
            matches.add(Match(link, startAt, homeVsAway))
        }
        return matches
    }

    private fun createNotificationChannel() {
        val name = "futChannel"
        val descriptionText = "My channel of notification"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(idChannel, name, importance).apply {
            description = descriptionText
        }

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
