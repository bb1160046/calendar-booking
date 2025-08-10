package com.accoladehq.calendar.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Entity representing a calendar owner.
 * <p>
 * A calendar owner is the person who defines availability rules and
 * receives appointment bookings from invitees.
 * </p>
 * 
 * <ul>
 *   <li><strong>username</strong> – unique identifier (e.g., email or system username).</li>
 *   <li><strong>displayName</strong> – optional, more user-friendly name.</li>
 * </ul>
 */
@Data
@Entity
@Table(name = "calendar_owner")
public class CalendarOwner {

    /** Primary key for the calendar owner. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique identifier for the owner (e.g., email or username). */
    @Column(nullable = false, unique = true)
    private String username;

    /** Display name for the owner (optional, for UI purposes). */
    private String displayName;
}
