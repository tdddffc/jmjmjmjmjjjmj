package com.weatherfocus.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import com.weatherfocus.app.MainActivity
import com.weatherfocus.app.R
import com.weatherfocus.app.data.alert.CustomWeatherAlertEngine
import com.weatherfocus.app.data.model.AppSettings
import com.weatherfocus.app.data.model.ConditionGroup
import com.weatherfocus.app.data.model.CustomAlertMatch
import com.weatherfocus.app.data.model.DayForecast
import com.weatherfocus.app.data.model.WidgetStyle
import com.weatherfocus.app.data.model.WidgetTile
import com.weatherfocus.app.data.repository.WeatherBundle
import com.weatherfocus.app.ui.WeatherFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Builds the RemoteViews for the two home-screen widgets (the full weather widget and the alerts-only
 * widget) from the latest weather bundle and the user's widget settings. The weather widget adapts to
 * its size: tiny shows the header only, bigger adds alerts, the hourly strip, info tiles and the
 * multi-day list, and a full-screen size adds the complete alert list.
 */
object WidgetRenderer {
    const val ACTION_REFRESH = "com.weatherfocus.app.WIDGET_REFRESH"

    fun renderAll(context: Context, settings: AppSettings, bundle: WeatherBundle) {
        val mgr = AppWidgetManager.getInstance(context)
        val alerts = CustomWeatherAlertEngine.evaluate(bundle.hourlyForAlerts, settings.customAlertRules)
        mgr.getAppWidgetIds(ComponentName(context, WeatherWidgetProvider::class.java)).forEach { id ->
            val (w, h) = sizeOf(mgr, id)
            mgr.updateAppWidget(id, weatherViews(context, settings, bundle, alerts, w, h))
        }
        mgr.getAppWidgetIds(ComponentName(context, AlertsWidgetProvider::class.java)).forEach { id ->
            val (w, h) = sizeOf(mgr, id)
            mgr.updateAppWidget(id, alertsViews(context, settings, alerts, w, h))
        }
    }

    private fun sizeOf(mgr: AppWidgetManager, id: Int): Pair<Int, Int> {
        val o = mgr.getAppWidgetOptions(id)
        val w = o.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 250)
        val h = o.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, o.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 300))
        return w to h
    }

    private data class Palette(val text: Int, val bgPanel: Int, val bgTile: Int, val alertBg: Int, val alertText: Int, val alertSub: Int)

    private fun paletteFor(style: WidgetStyle) = when (style) {
        WidgetStyle.LIGHT -> Palette(
            Color.parseColor("#1D2A4D"), R.drawable.widget_panel_light, R.drawable.widget_tile_light,
            R.drawable.widget_alert_light, Color.parseColor("#7A1F1F"), Color.parseColor("#A14A4A")
        )
        WidgetStyle.DARK -> Palette(
            Color.parseColor("#EEF0F4"), R.drawable.widget_panel_dark, R.drawable.widget_tile_dark,
            R.drawable.widget_alert_dark, Color.parseColor("#FFE3E3"), Color.parseColor("#FFB4B4")
        )
        WidgetStyle.WEATHER -> Palette(
            Color.WHITE, R.drawable.widget_panel_weather, R.drawable.widget_tile_weather,
            R.drawable.widget_alert_light, Color.parseColor("#7A1F1F"), Color.parseColor("#A14A4A")
        )
    }

    private fun openAppIntent(context: Context): PendingIntent =
        PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun refreshIntent(context: Context, provider: Class<*>, requestCode: Int): PendingIntent =
        PendingIntent.getBroadcast(
            context, requestCode,
            Intent(context, provider).setAction(ACTION_REFRESH),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    /** Background drawable: fixed for dark/light, otherwise the worst weather in the look-ahead window. */
    private fun backgroundFor(settings: AppSettings, bundle: WeatherBundle, slots: List<HourSlot>, now: Long): Int {
        when (settings.widget.style) {
            WidgetStyle.DARK -> return R.drawable.widget_bg_dark
            WidgetStyle.LIGHT -> return R.drawable.widget_bg_light
            WidgetStyle.WEATHER -> Unit
        }
        val cur = bundle.current
        var worst = cur.conditionGroup
        val hours = settings.widget.lookaheadHours
        if (hours > 0) {
            val end = now + hours * 3_600_000L
            slots.filter { it.millis in now..end }.forEach {
                val g = WidgetData.groupOfCode(it.code)
                if (WidgetData.severity(g) > WidgetData.severity(worst)) worst = g
            }
        }
        val night = WidgetData.isNight(now, cur.sunrise, cur.sunset)
        return when (worst) {
            ConditionGroup.CLEAR -> if (night) R.drawable.widget_bg_night else R.drawable.widget_bg_clear
            ConditionGroup.CLOUDY, ConditionGroup.UNKNOWN -> R.drawable.widget_bg_cloudy
            ConditionGroup.RAIN -> R.drawable.widget_bg_rain
            ConditionGroup.THUNDER -> R.drawable.widget_bg_thunder
            ConditionGroup.SNOW -> R.drawable.widget_bg_snow
            ConditionGroup.FOG -> R.drawable.widget_bg_fog
        }
    }

    private fun fmt1(v: Double): String = String.format(Locale.US, "%.1f", v)

    private fun tileValue(tile: WidgetTile, bundle: WeatherBundle, upcoming: List<HourSlot>, all: List<HourSlot>, now: Long, useF: Boolean): String {
        val cur = bundle.current
        val next12 = upcoming.take(12)
        val todayKey = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date(now))
        val today = all.filter { SimpleDateFormat("yyyyMMdd", Locale.US).format(Date(it.millis)) == todayKey }
        return when (tile) {
            WidgetTile.WIND -> cur.windSpeedKmh?.let { "${it.roundToInt()} km/h" } ?: "--"
            WidgetTile.GUST -> next12.mapNotNull { it.gust }.maxOrNull()?.let { "${it.roundToInt()} km/h" } ?: "--"
            WidgetTile.HUMIDITY -> cur.humidity?.let { "$it%" } ?: "--"
            WidgetTile.UV -> upcoming.firstOrNull()?.uv?.let { fmt1(it) } ?: "--"
            WidgetTile.SNOW -> today.mapNotNull { it.snowCm }.takeIf { it.isNotEmpty() }?.sum()?.let { "${fmt1(it)} cm" } ?: "--"
            WidgetTile.PRECIP -> today.mapNotNull { it.rainMm }.takeIf { it.isNotEmpty() }?.sum()?.let { "${fmt1(it)} mm" } ?: "--"
            WidgetTile.RAIN_CHANCE -> next12.mapNotNull { it.rainChance }.maxOrNull()?.let { "$it%" } ?: "--"
            WidgetTile.PRESSURE -> cur.pressure?.let { "$it hPa" } ?: "--"
            WidgetTile.FEELS -> WeatherFormat.tempNoUnit(cur.feelsLike, useF)
            WidgetTile.VISIBILITY -> upcoming.firstOrNull()?.visibilityM?.let {
                if (it >= 1000) "${fmt1(it / 1000)} km" else "${it.roundToInt()} m"
            } ?: "--"
        }
    }

    private fun weatherViews(
        context: Context, settings: AppSettings, bundle: WeatherBundle,
        alerts: List<CustomAlertMatch>, widthDp: Int, heightDp: Int
    ): RemoteViews {
        val ws = settings.widget
        val pal = paletteFor(ws.style)
        val rv = RemoteViews(context.packageName, R.layout.widget_weather)
        val now = System.currentTimeMillis()
        val useF = settings.useFahrenheit
        val cur = bundle.current
        val all = WidgetData.slots(bundle.hourlyForAlerts)
        val upcoming = WidgetData.upcoming(all, now)

        rv.setInt(R.id.root, "setBackgroundResource", backgroundFor(settings, bundle, all, now))
        WidgetIds.weatherTexts.forEach { rv.setTextColor(it, pal.text) }
        rv.setTextColor(R.id.refresh, pal.text)

        // ----- header + main -----
        val night = WidgetData.isNight(now, cur.sunrise, cur.sunset)
        rv.setTextViewText(R.id.loc, "📍 ${settings.cityName}, ${settings.countryLabel}")
        rv.setTextViewText(R.id.updated, "Updated ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(now))} · tap to open")
        val emoji = upcoming.firstOrNull()?.code?.let { WidgetData.emojiForCode(it, night) }
            ?: WidgetData.emojiForGroup(cur.conditionGroup, night)
        rv.setTextViewText(R.id.emoji, emoji)
        rv.setTextViewText(R.id.temp, WeatherFormat.tempNoUnit(cur.temp, useF))
        rv.setTextViewText(R.id.cond, cur.description ?: "")
        rv.setTextViewText(R.id.feels, "Feels ${WeatherFormat.tempNoUnit(cur.feelsLike, useF)}")
        rv.setTextViewText(R.id.hilo, "H ${WeatherFormat.tempNoUnit(cur.tempMax, useF)} · L ${WeatherFormat.tempNoUnit(cur.tempMin, useF)}")

        // ----- alert banner -----
        val showBanner = ws.showAlert && alerts.isNotEmpty() && heightDp >= 130
        rv.setViewVisibility(R.id.alert_box, if (showBanner) android.view.View.VISIBLE else android.view.View.GONE)
        if (showBanner) {
            val first = alerts.first()
            val (e, title) = WidgetData.splitEmoji(first.label)
            rv.setInt(R.id.alert_box, "setBackgroundResource", pal.alertBg)
            rv.setTextViewText(R.id.alert_e, e)
            rv.setTextViewText(R.id.alert_t, title + if (alerts.size > 1) "  (+${alerts.size - 1} more)" else "")
            rv.setTextViewText(R.id.alert_s, first.detail)
            rv.setTextColor(R.id.alert_t, pal.alertText)
            rv.setTextColor(R.id.alert_s, pal.alertSub)
        }

        // ----- hourly strip -----
        val showHours = ws.showHours && heightDp >= 210
        rv.setViewVisibility(R.id.hours_box, if (showHours) android.view.View.VISIBLE else android.view.View.GONE)
        if (showHours) {
            rv.setInt(R.id.hours_box, "setBackgroundResource", pal.bgPanel)
            val n = if (widthDp < 300) minOf(ws.hoursCount, 4) else ws.hoursCount.coerceIn(3, 8)
            val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
            for (i in 0 until 8) {
                val slot = upcoming.getOrNull(i)
                val visible = i < n && slot != null
                rv.setViewVisibility(WidgetIds.hourBox[i], if (visible) android.view.View.VISIBLE else android.view.View.GONE)
                if (visible && slot != null) {
                    rv.setTextViewText(WidgetIds.hourT[i], if (i == 0) "Now" else timeFmt.format(Date(slot.millis)))
                    rv.setTextViewText(WidgetIds.hourE[i], WidgetData.emojiForCode(slot.code, WidgetData.isNight(slot.millis, cur.sunrise, cur.sunset)))
                    rv.setTextViewText(WidgetIds.hourV[i], WeatherFormat.tempNoUnit(slot.temp, useF))
                    rv.setTextViewText(WidgetIds.hourP[i], slot.rainChance?.let { "💧 $it%" } ?: "")
                }
            }
        }

        // ----- info tiles -----
        val showTiles = ws.showTiles && ws.tiles.isNotEmpty() && heightDp >= 300
        rv.setViewVisibility(R.id.tiles_box, if (showTiles) android.view.View.VISIBLE else android.view.View.GONE)
        if (showTiles) {
            val maxTiles = if (heightDp >= 420) 8 else 4
            val chosen = ws.tiles.take(maxTiles)
            rv.setViewVisibility(R.id.tiles_row2, if (chosen.size > 4) android.view.View.VISIBLE else android.view.View.GONE)
            for (i in 0 until 8) {
                val tile = chosen.getOrNull(i)
                rv.setViewVisibility(WidgetIds.tileBox[i], if (tile != null) android.view.View.VISIBLE else android.view.View.INVISIBLE)
                if (tile != null) {
                    rv.setInt(WidgetIds.tileBox[i], "setBackgroundResource", pal.bgTile)
                    rv.setTextViewText(WidgetIds.tileE[i], tile.emoji)
                    rv.setTextViewText(WidgetIds.tileV[i], tileValue(tile, bundle, upcoming, all, now, useF))
                    rv.setTextViewText(WidgetIds.tileL[i], tile.label)
                }
            }
        }

        // ----- multi-day list -----
        val realDays: List<DayForecast> = bundle.monthDays.filter { !it.isOutlookEstimate }.drop(1).ifEmpty { bundle.next3Days }
        val fitDays = when {
            heightDp >= 520 -> 7
            heightDp >= 440 -> 5
            heightDp >= 360 -> 3
            else -> 0
        }
        val dayCount = minOf(ws.daysCount, fitDays, realDays.size)
        val showDays = ws.showDays && dayCount > 0
        rv.setViewVisibility(R.id.days_box, if (showDays) android.view.View.VISIBLE else android.view.View.GONE)
        if (showDays) {
            rv.setInt(R.id.days_box, "setBackgroundResource", pal.bgPanel)
            for (i in 0 until 7) {
                val d = realDays.getOrNull(i)
                val visible = i < dayCount && d != null
                rv.setViewVisibility(WidgetIds.dayRow[i], if (visible) android.view.View.VISIBLE else android.view.View.GONE)
                if (visible && d != null) {
                    rv.setTextViewText(WidgetIds.dayD[i], d.dayLabel)
                    rv.setTextViewText(WidgetIds.dayE[i], WidgetData.emojiForGroup(d.conditionGroup, false))
                    rv.setTextViewText(WidgetIds.dayT[i], "${WeatherFormat.tempNoUnit(d.maxTemp, useF)} / ${WeatherFormat.tempNoUnit(d.minTemp, useF)}")
                    rv.setTextViewText(WidgetIds.dayR[i], d.rainChancePercent?.let { "💧 $it%" } ?: "")
                }
            }
        }

        // ----- full alert list (only on very tall widgets) -----
        val showAllAlerts = ws.showAlert && alerts.size > 1 && heightDp >= 560
        rv.setViewVisibility(R.id.xalerts_box, if (showAllAlerts) android.view.View.VISIBLE else android.view.View.GONE)
        if (showAllAlerts) {
            rv.setInt(R.id.xalerts_box, "setBackgroundResource", pal.bgPanel)
            for (i in 0 until 4) {
                val a = alerts.getOrNull(i)
                rv.setViewVisibility(WidgetIds.xAlert[i], if (a != null) android.view.View.VISIBLE else android.view.View.GONE)
                if (a != null) rv.setTextViewText(WidgetIds.xAlert[i], "${a.label} — ${a.detail}")
            }
        }

        // ----- sunrise / sunset -----
        val showSun = ws.showSun && heightDp >= 250
        rv.setViewVisibility(R.id.foot_box, if (showSun) android.view.View.VISIBLE else android.view.View.GONE)
        if (showSun) {
            rv.setTextViewText(R.id.sunrise, "${settings.sunIconStyle.sunrise} ${cur.sunrise ?: "--"}")
            rv.setTextViewText(R.id.sunset, "${settings.sunIconStyle.sunset} ${cur.sunset ?: "--"}")
            rv.setTextViewText(R.id.daylight, WidgetData.daylightText(cur.sunrise, cur.sunset)?.let { "☀️ $it daylight" } ?: "")
        }

        rv.setOnClickPendingIntent(R.id.root, openAppIntent(context))
        rv.setOnClickPendingIntent(R.id.refresh, refreshIntent(context, WeatherWidgetProvider::class.java, 1))
        return rv
    }

    private fun alertsViews(context: Context, settings: AppSettings, alerts: List<CustomAlertMatch>, widthDp: Int, heightDp: Int): RemoteViews {
        val ws = settings.widget
        val pal = paletteFor(if (ws.style == WidgetStyle.WEATHER) WidgetStyle.DARK else ws.style)
        val rv = RemoteViews(context.packageName, R.layout.widget_alerts)
        val now = System.currentTimeMillis()

        rv.setInt(R.id.root, "setBackgroundResource", if (ws.style == WidgetStyle.LIGHT) R.drawable.widget_bg_light else R.drawable.widget_bg_dark)
        WidgetIds.alertTexts.forEach { rv.setTextColor(it, pal.text) }
        rv.setTextColor(R.id.refresh, pal.text)
        rv.setTextViewText(R.id.a_sub, "${settings.cityName} · updated ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(now))}")

        val rules = settings.customAlertRules
        val maxRows = when {
            heightDp >= 300 -> 5
            heightDp >= 220 -> 4
            heightDp >= 160 -> 3
            heightDp >= 110 -> 2
            else -> 1
        }
        if (alerts.isEmpty()) {
            rv.setViewVisibility(R.id.a_empty, android.view.View.VISIBLE)
            rv.setTextViewText(
                R.id.a_empty,
                if (rules.enabled) "✅ No alerts in the next ${rules.horizonHours} hours. Enjoy!"
                else "Custom alerts are off. Turn them on in the app: Settings → Custom Weather Alerts."
            )
        } else {
            rv.setViewVisibility(R.id.a_empty, android.view.View.GONE)
        }
        for (i in 0 until 5) {
            val a = alerts.getOrNull(i)
            val visible = a != null && i < maxRows
            rv.setViewVisibility(WidgetIds.alRow[i], if (visible) android.view.View.VISIBLE else android.view.View.GONE)
            if (visible && a != null) {
                val (e, title) = WidgetData.splitEmoji(a.label)
                rv.setInt(WidgetIds.alRow[i], "setBackgroundResource", pal.bgPanel)
                rv.setTextViewText(WidgetIds.alE[i], e)
                rv.setTextViewText(WidgetIds.alT[i], title)
                rv.setTextViewText(WidgetIds.alS[i], a.detail)
            }
        }
        rv.setOnClickPendingIntent(R.id.root, openAppIntent(context))
        rv.setOnClickPendingIntent(R.id.refresh, refreshIntent(context, AlertsWidgetProvider::class.java, 2))
        return rv
    }
}
