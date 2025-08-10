package com.accoladehq.calendar.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

/**
 * Entity representing a booked appointment.
 * <p>
 * This table stores details about a specific appointment booked for a
 * {@link CalendarOwner}. Each appointment has:
 * </p>
 * <ul>
 *   <li>A date and a 60-minute time slot (start and end time)</li>
 *   <li>The invitee's name and optional email</li>
 *   <li>A unique constraint ensuring no double-booking for the same owner, date, and start time</li>
 * </ul>
 *
 * <p><b>Unique Constraint:</b></p>
 * <ul>
 *   <li>{@code owner_id + date + start_time} must be unique to prevent booking conflicts.</li>
 * </ul>
 */
@Data
@Entity
@Table(
    name = "appointment",
    uniqueConstraints = @UniqueConstraint(columnNames = {"owner_id", "date", "start_time"})
)
public class Appointment {

    /** Primary key ID for the appointment. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The calendar owner for whom this appointment is booked. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private CalendarOwner owner;

    /** The date of the appointment. */
    @Column(nullable = false)
    private LocalDate date;

    /** Start time of the 60-minute slot. */
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    /** End time of the 60-minute slot. */
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    /** Name of the person booking the appointment. */
    @Column(nullable = false)
    private String inviteeName;

    /** Optional email of the invitee. */
    private String inviteeEmail;
}
