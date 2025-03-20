// src/Model/Booking.java
package src.Model;

import java.time.LocalDateTime;

public class Booking
{
    private static int bookingCounter = 0;

    private static int transactionCounter = 0;

    private final int id;

    private final int roomId;

    private final int userId;

    private final int transactionId;

    private final LocalDateTime checkInTime;

    private final LocalDateTime checkOutTime;

    private final LocalDateTime bookedTime;

    // Constructor with specified transaction ID
    public Booking(int roomId, int userId, LocalDateTime checkInTime, LocalDateTime checkOutTime, int transactionId)
    {
        this.id = ++bookingCounter;

        this.roomId = roomId;

        this.userId = userId;

        this.transactionId = transactionId;

        this.checkInTime = checkInTime;

        this.checkOutTime = checkOutTime;

        this.bookedTime = LocalDateTime.now();

        // Update the transaction counter if needed
        if (transactionId > transactionCounter)
        {
            transactionCounter = transactionId;
        }
    }

    // Getters
    public int getId()
    {
        return id;
    }

    public int getRoomId()
    {
        return roomId;
    }

    public int getUserId()
    {
        return userId;
    }

    public LocalDateTime getCheckInTime()
    {
        return checkInTime;
    }

    public LocalDateTime getCheckOutTime()
    {
        return checkOutTime;
    }

    public LocalDateTime getBookedTime()
    {
        return bookedTime;
    }

    public int getTransactionId()
    {
        return transactionId;
    }
}
