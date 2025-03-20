package src.Controller;

import src.Model.Booking;
import java.time.LocalDateTime;

public class BookingHandler
{
    private static BookingHandler instance;

    private final DataHandler dataHandler;

    private BookingHandler()
    {
        this.dataHandler = DataHandler.getDataHandler();
    }

    public static BookingHandler getBookingHandler()
    {
        if (instance == null)
        {
            instance = new BookingHandler();
        }

        return instance;
    }

    public boolean isRoomAvailable(int roomId, LocalDateTime checkInTime, LocalDateTime checkOutTime)
    {
        for (Booking booking : dataHandler.getBookings())
        {
            if (booking.getRoomId() == roomId)
            {
                // Check if there's an overlap in booking periods
                boolean overlapExists =
                        (checkInTime.isBefore(booking.getCheckOutTime()) || checkInTime.isEqual(booking.getCheckOutTime())) &&
                                (checkOutTime.isAfter(booking.getCheckInTime()) || checkOutTime.isEqual(booking.getCheckInTime()));

                if (overlapExists)
                {
                    return false;
                }
            }
        }

        return true;
    }

    // Book a single room with transaction ID
    public int bookRoomWithTransaction(int roomId, int userId, LocalDateTime checkInTime, LocalDateTime checkOutTime, int transactionId)
    {
        if (!isRoomAvailable(roomId, checkInTime, checkOutTime))
        {
            return -1; // Room not available
        }

        Booking booking = new Booking(roomId, userId, checkInTime, checkOutTime, transactionId);

        dataHandler.getBookings().add(booking);

        return booking.getId();
    }
}
