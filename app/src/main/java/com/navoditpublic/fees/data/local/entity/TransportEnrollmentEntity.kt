package com.navoditpublic.fees.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity to track student transport enrollment history.
 * Tracks when a student started and stopped using transport.
 * Transport fees apply for the entire month even if started/stopped mid-month.
 * 
 * Multiple records allow tracking:
 * - Student started transport in April, stopped in November
 * - Same student restarted in January with a different route
 */
@Entity(
    tableName = "transport_enrollments",
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["student_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TransportRouteEntity::class,
            parentColumns = ["id"],
            childColumns = ["route_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["student_id"]),
        Index(value = ["route_id"]),
        Index(value = ["student_id", "start_date"])
    ]
)
data class TransportEnrollmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "student_id")
    val studentId: Long,
    
    @ColumnInfo(name = "route_id")
    val routeId: Long,
    
    @ColumnInfo(name = "start_date")
    val startDate: Long, // When transport started (epoch millis)
    
    @ColumnInfo(name = "end_date")
    val endDate: Long? = null, // When transport ended (null = currently active)
    
    @ColumnInfo(name = "monthly_fee_at_enrollment")
    val monthlyFeeAtEnrollment: Double, // Fee locked at the time of enrollment
    
    @ColumnInfo(name = "remarks")
    val remarks: String = "", // Optional notes
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

