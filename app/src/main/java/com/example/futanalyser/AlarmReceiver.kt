package com.example.futanalyser

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.futanalyser.Constants.Companion.baseUrl
import com.example.futanalyser.Constants.Companion.userAgent
import com.example.futanalyser.model.Statistic
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val link = intent?.getStringExtra("link")?.replace("matchfacts", "stats")
        getStatistics(link)
    }

    private fun getStatistics(link: String?) {
        Thread() {
            val doc: Document = Jsoup.connect(baseUrl + link).userAgent(userAgent).maxBodySize(Int.MAX_VALUE).get()

            val score = doc.selectFirst("span.css-4rm8lc-topRow").text()

            val statistics = doc.select("section.css-v9dyli-WithPadding div")
            val myStats = setupStats(statistics)

            checkStats(myStats)
        }.start()
    }

    private fun checkStats(myStats: MutableList<Statistic>) {
        val xG = myStats[0]
        val totalShots = myStats[1]
        val onTarget = myStats[2]
        val insideBox = myStats[3]

        xG.home
        xG.away
        totalShots.home
        totalShots.away
        onTarget.home
        onTarget.away
        insideBox.home
        insideBox.away
    }

    private fun setupStats(statistics: Elements): MutableList<Statistic> {
        val myStats = mutableListOf<Statistic>()

        statistics.forEach { statistic ->
            if (statistic.childrenSize() > 1) {
                val type = statistic.child(1).text()
                val home = statistic.child(0).text()
                val away = statistic.child(2).text()
                when (type) {
                    "Expected goals (xG)" -> myStats.add(Statistic(type, home.toFloat(), away.toFloat()))
                    "Total shots" -> myStats.add(Statistic(type, home.toFloat(), away.toFloat()))
                    "Shots on target" -> myStats.add(Statistic(type, home.toFloat(), away.toFloat()))
                    "Shots inside box" -> myStats.add(Statistic(type, home.toFloat(), away.toFloat()))
                }
            }
        }
        return myStats
    }
}
