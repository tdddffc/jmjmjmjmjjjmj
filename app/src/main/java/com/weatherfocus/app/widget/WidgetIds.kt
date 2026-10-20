package com.weatherfocus.app.widget

import com.weatherfocus.app.R

/** View ids of the widget layouts, grouped so the renderer can loop over slots. Generated from the layout XML. */
object WidgetIds {
    val hourBox = intArrayOf(R.id.h1_box, R.id.h2_box, R.id.h3_box, R.id.h4_box, R.id.h5_box, R.id.h6_box, R.id.h7_box, R.id.h8_box)
    val hourT = intArrayOf(R.id.h1_t, R.id.h2_t, R.id.h3_t, R.id.h4_t, R.id.h5_t, R.id.h6_t, R.id.h7_t, R.id.h8_t)
    val hourE = intArrayOf(R.id.h1_e, R.id.h2_e, R.id.h3_e, R.id.h4_e, R.id.h5_e, R.id.h6_e, R.id.h7_e, R.id.h8_e)
    val hourV = intArrayOf(R.id.h1_v, R.id.h2_v, R.id.h3_v, R.id.h4_v, R.id.h5_v, R.id.h6_v, R.id.h7_v, R.id.h8_v)
    val hourP = intArrayOf(R.id.h1_p, R.id.h2_p, R.id.h3_p, R.id.h4_p, R.id.h5_p, R.id.h6_p, R.id.h7_p, R.id.h8_p)
    val tileBox = intArrayOf(R.id.t1_box, R.id.t2_box, R.id.t3_box, R.id.t4_box, R.id.t5_box, R.id.t6_box, R.id.t7_box, R.id.t8_box)
    val tileE = intArrayOf(R.id.t1_e, R.id.t2_e, R.id.t3_e, R.id.t4_e, R.id.t5_e, R.id.t6_e, R.id.t7_e, R.id.t8_e)
    val tileV = intArrayOf(R.id.t1_v, R.id.t2_v, R.id.t3_v, R.id.t4_v, R.id.t5_v, R.id.t6_v, R.id.t7_v, R.id.t8_v)
    val tileL = intArrayOf(R.id.t1_l, R.id.t2_l, R.id.t3_l, R.id.t4_l, R.id.t5_l, R.id.t6_l, R.id.t7_l, R.id.t8_l)
    val dayRow = intArrayOf(R.id.d1_row, R.id.d2_row, R.id.d3_row, R.id.d4_row, R.id.d5_row, R.id.d6_row, R.id.d7_row)
    val dayD = intArrayOf(R.id.d1_d, R.id.d2_d, R.id.d3_d, R.id.d4_d, R.id.d5_d, R.id.d6_d, R.id.d7_d)
    val dayE = intArrayOf(R.id.d1_e, R.id.d2_e, R.id.d3_e, R.id.d4_e, R.id.d5_e, R.id.d6_e, R.id.d7_e)
    val dayT = intArrayOf(R.id.d1_t, R.id.d2_t, R.id.d3_t, R.id.d4_t, R.id.d5_t, R.id.d6_t, R.id.d7_t)
    val dayR = intArrayOf(R.id.d1_r, R.id.d2_r, R.id.d3_r, R.id.d4_r, R.id.d5_r, R.id.d6_r, R.id.d7_r)
    val xAlert = intArrayOf(R.id.xa1, R.id.xa2, R.id.xa3, R.id.xa4)
    val alRow = intArrayOf(R.id.ar1_box, R.id.ar2_box, R.id.ar3_box, R.id.ar4_box, R.id.ar5_box)
    val alE = intArrayOf(R.id.ar1_e, R.id.ar2_e, R.id.ar3_e, R.id.ar4_e, R.id.ar5_e)
    val alT = intArrayOf(R.id.ar1_t, R.id.ar2_t, R.id.ar3_t, R.id.ar4_t, R.id.ar5_t)
    val alS = intArrayOf(R.id.ar1_s, R.id.ar2_s, R.id.ar3_s, R.id.ar4_s, R.id.ar5_s)
    /** Every TextView in the weather widget that takes the style's text colour. */
    val weatherTexts = intArrayOf(R.id.loc, R.id.updated, R.id.temp, R.id.cond, R.id.feels, R.id.hilo, R.id.h1_t, R.id.h1_v, R.id.h1_p, R.id.h2_t, R.id.h2_v, R.id.h2_p, R.id.h3_t, R.id.h3_v, R.id.h3_p, R.id.h4_t, R.id.h4_v, R.id.h4_p, R.id.h5_t, R.id.h5_v, R.id.h5_p, R.id.h6_t, R.id.h6_v, R.id.h6_p, R.id.h7_t, R.id.h7_v, R.id.h7_p, R.id.h8_t, R.id.h8_v, R.id.h8_p, R.id.t1_v, R.id.t1_l, R.id.t2_v, R.id.t2_l, R.id.t3_v, R.id.t3_l, R.id.t4_v, R.id.t4_l, R.id.t5_v, R.id.t5_l, R.id.t6_v, R.id.t6_l, R.id.t7_v, R.id.t7_l, R.id.t8_v, R.id.t8_l, R.id.d1_d, R.id.d1_t, R.id.d1_r, R.id.d2_d, R.id.d2_t, R.id.d2_r, R.id.d3_d, R.id.d3_t, R.id.d3_r, R.id.d4_d, R.id.d4_t, R.id.d4_r, R.id.d5_d, R.id.d5_t, R.id.d5_r, R.id.d6_d, R.id.d6_t, R.id.d6_r, R.id.d7_d, R.id.d7_t, R.id.d7_r, R.id.xalerts_title, R.id.xa1, R.id.xa2, R.id.xa3, R.id.xa4, R.id.sunrise, R.id.daylight, R.id.sunset)
    val alertTexts = intArrayOf(R.id.a_title, R.id.a_sub, R.id.a_empty, R.id.ar1_t, R.id.ar1_s, R.id.ar2_t, R.id.ar2_s, R.id.ar3_t, R.id.ar3_s, R.id.ar4_t, R.id.ar4_s, R.id.ar5_t, R.id.ar5_s)
    /** Hour/tile/day slot TextViews are coloured through these lists too. */
}
