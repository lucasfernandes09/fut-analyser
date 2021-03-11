package com.example.futanalyser

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.futanalyser.Constants.Companion.baseUrl
import com.example.futanalyser.Constants.Companion.idChannel
import com.example.futanalyser.Constants.Companion.userAgent
import com.example.futanalyser.application.FutAnalyserApp
import com.example.futanalyser.model.Statistic
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

class AlarmReceiver() : BroadcastReceiver() {

    private var score: String? = null
    private var name: String? = null
    private var idMatch: Int? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        name = intent?.getStringExtra("name")
        val link = intent?.getStringExtra("link")?.replace("matchfacts", "stats")
        idMatch = link?.replace("/livescores/", "")?.substring(0, 7)?.toInt()

        getStatistics(link)
    }

    private fun getStatistics(link: String?) {
        Thread() {
            val doc: Document = Jsoup.connect(baseUrl + link).userAgent(userAgent).maxBodySize(Int.MAX_VALUE).get()

            score = doc.selectFirst("span.css-jkaqxa").text()

            val statistics = doc.select("section.css-v9dyli-WithPadding div")
            val myStats = setupStats(statistics)

            checkStats(myStats)
        }.start()
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

    private fun checkStats(myStats: MutableList<Statistic>) {
        val xG = myStats[0]; val xg1 = xG.home; val xg2 = xG.away
        val totalShots = myStats[1]; val t1 = totalShots.home; val t2 = totalShots.away
        val onTarget = myStats[2]; val o1 = onTarget.home; val o2 = onTarget.away
        val insideBox = myStats[3]; val i1 = insideBox.home; val i2 = insideBox.away

        val tx: Float; val ox: Float; val ix: Float
        val ty: Float; val oy: Float; val iy: Float

        when {
            t1 > t2 -> {
                tx = t1; ox = o1; ix = i1
                ty = t2; oy = o2; iy = i2
            }
            t2 > t1 -> {
                tx = t2; ox = o2; ix = i2
                ty = t1; oy = o1; iy = i1
            }
            else -> return
        }

        if (ox > oy && ix > iy &&
            tx / (tx + ty) >= 0.7 &&
            tx >= 5 && ix / tx >= 0.6
        ) {
            sendNotification(
                "$xg1   xG   $xg2 \n" +
                    "$t1   total   $t2 \n" +
                    "$o1   onTarget   $o2 \n" +
                    "$i1   inside   $i2 \n"
            )
        }
    }

    private fun sendNotification(content: String) {
        FutAnalyserApp.context?.let { context ->

            val builder = NotificationCompat.Builder(context, idChannel)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(name)
                .setContentText(score)
                .setStyle(NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(NotificationCompat.PRIORITY_HIGH)

            with(NotificationManagerCompat.from(context)) {
                val id = System.currentTimeMillis()
                notify(idMatch ?: id.toInt(), builder.build())
            }
        }
    }
}
