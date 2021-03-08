package com.example.futanalyser

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.futanalyser.Constants.Companion.baseUrl
import com.example.futanalyser.Constants.Companion.userAgent
import com.example.futanalyser.model.Competition
import com.example.futanalyser.model.Match
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.time.LocalTime
import java.util.*

class MainActivity : AppCompatActivity() {

    private val myCompetitions = mutableListOf<Competition>()
    private val myMatches = mutableListOf<Match>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getMatches()
    }

    private fun getMatches() {
        Thread() {
            val doc: Document = Jsoup.connect(baseUrl).userAgent(userAgent).maxBodySize(Int.MAX_VALUE).get()

            val groupElements = doc.select("div.css-mj57cz-Group")
            groupElements.forEach { groupElement ->
                val nameCompetition = groupElement.select("a.css-1g2mffk-GroupTitleLink").text()
                val isMyCompetition = separeteName(nameCompetition)

                if (isMyCompetition) {
                    val matchElements = groupElement.select("a.css-wdtn5d-MatchWrapper-positioning")
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

        Handler().postDelayed({ alarmMgr.cancel(alarmIntent) }, 1000 * 60 * 5)
    }

    private fun separeteName(name: String?): Boolean {
        return when (name) {
            "ENGLAND - PREMIER LEAGUE" -> true
            "SPAIN - LALIGA" -> true
            "GERMANY - 1. BUNDESLIGA" -> true
            "ITALY - SERIE A" -> true
            "FRANCE - LIGUE 1" -> true
            else -> false
        }
    }

    private fun setupMatchs(matchElements: Elements): MutableList<Match> {
        val matches = mutableListOf<Match>()

        matchElements.forEach { matchElement ->
            val link = matchElement.attr("href")
            val startAt = matchElement.select("span.css-1v1cnzp-time").text()
            val homeVsAway = matchElement.text()
            matches.add(Match(link, startAt, homeVsAway))
        }
        return matches
    }
}