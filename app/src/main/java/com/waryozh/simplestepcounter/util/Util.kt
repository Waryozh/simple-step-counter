package com.waryozh.simplestepcounter.util

import java.text.SimpleDateFormat
import java.util.*

fun calculateDistance(steps: Int, length: Int) = (steps * (length / 100F)).toInt()

fun getCurrentDate(): String = SimpleDateFormat("dd.MM.yy", Locale.getDefault()).format(Date())
