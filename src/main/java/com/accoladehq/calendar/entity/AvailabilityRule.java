package com.accoladehq.calendar.entity;

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
import lombok.Data;

/**
 * Entity representing an availability rule for a {@link CalendarOwner}.
 * <p>
 * Defines a daily availability window during which the owner can accept
 * appointments. The rule specifies:
 * </p>
 * <ul>
 *   <li>Start time of availability</li>
 *   <li>End time of availability</li>
 * </ul>
 * <p>
 * All appointments must fall within the defined time range.
 * </p>
 */
@Data
@Entity
@Table(name = "availability_rule")
public class AvailabilityRule {

    /** Primary key ID for the availability rule. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The calendar owner to whom this rule applies. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private CalendarOwner owner;

    /** Start time of the daily availability window (inclusive). */
    @Column(nullable = false)
    private LocalTime startTime;

    /** End time of the daily availability window (exclusive). */
    @Column(nullable = false)
    private LocalTime endTime;
}
