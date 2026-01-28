package com.mobile.habittrackernew.data.database

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalTime

class Converters {

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }

    @TypeConverter
    fun toLocalDate(epochDay: Long?): LocalDate? {
        return epochDay?.let { LocalDate.ofEpochDay(it) }
    }

    @TypeConverter
    fun fromLocalTime(time: LocalTime?): String? {
        return time?.toString()
    }

    @TypeConverter
    fun toLocalTime(timeString: String?): LocalTime? {
        return timeString?.let { LocalTime.parse(it) }
    }

    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        return list?.joinToString(",")
    }

    @TypeConverter
    fun toStringList(data: String?): List<String>? {
        return if (data.isNullOrBlank()) emptyList() else data.split(",").filter { it.isNotEmpty() }
    }

    @TypeConverter
    fun fromIntList(list: List<Int>?): String? {
        return list?.joinToString(",")
    }

    @TypeConverter
    fun toIntList(data: String?): List<Int>? {
        return if (data.isNullOrBlank()) emptyList() else data.split(",").filter { it.isNotEmpty() }.map { it.toInt() }
    }
}