package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.AlertType
import com.example.data.model.EventType
import com.example.data.model.MediaType
import com.example.data.model.ReportCategory
import com.example.data.model.ReportCondition
import com.example.data.model.ReportSeverity
import com.example.data.model.ReportStatus

class Converters {
    @TypeConverter
    fun fromCategory(value: ReportCategory): String = value.name

    @TypeConverter
    fun toCategory(value: String): ReportCategory = try {
        ReportCategory.valueOf(value)
    } catch (e: Exception) {
        ReportCategory.OTHER
    }

    @TypeConverter
    fun fromSeverity(value: ReportSeverity): String = value.name

    @TypeConverter
    fun toSeverity(value: String): ReportSeverity = try {
        ReportSeverity.valueOf(value)
    } catch (e: Exception) {
        ReportSeverity.MEDIUM
    }

    @TypeConverter
    fun fromStatus(value: ReportStatus): String = value.name

    @TypeConverter
    fun toStatus(value: String): ReportStatus = try {
        ReportStatus.valueOf(value)
    } catch (e: Exception) {
        ReportStatus.ACTIVE
    }

    @TypeConverter
    fun fromCondition(value: ReportCondition): String = value.name

    @TypeConverter
    fun toCondition(value: String): ReportCondition = try {
        ReportCondition.valueOf(value)
    } catch (e: Exception) {
        ReportCondition.HAPPENING_NOW
    }

    @TypeConverter
    fun fromEventType(value: EventType): String = value.name

    @TypeConverter
    fun toEventType(value: String): EventType = try {
        EventType.valueOf(value)
    } catch (e: Exception) {
        EventType.REPORT_CONFIRMED
    }

    @TypeConverter
    fun fromMediaType(value: MediaType): String = value.name

    @TypeConverter
    fun toMediaType(value: String): MediaType = try {
        MediaType.valueOf(value)
    } catch (e: Exception) {
        MediaType.PHOTO
    }

    @TypeConverter
    fun fromAlertType(value: AlertType): String = value.name

    @TypeConverter
    fun toAlertType(value: String): AlertType = try {
        AlertType.valueOf(value)
    } catch (e: Exception) {
        AlertType.NEARBY
    }
}
