package src.Controller;

import src.Model.Booking;
import src.Model.Room;
import src.Model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BookingHandler
{
    private static BookingHandler instance;

    private static final DataHandler dataHandler = DataHandler.getDataHandler();

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private BookingHandler()
    {
        //
    }

    //  ==== CRUD ====

    // == CREATE ==
    public static String handleBooking(String[] parts)
    {
        try
        {
            // Find date and credential positions
            var lastIndex = parts.length - 1;

            var password = parts[lastIndex];

            var username = parts[lastIndex - 1];

            var checkOutTime = LocalDateTime.parse(parts[lastIndex - 2], formatter);

            var checkInTime = LocalDateTime.parse(parts[lastIndex - 3], formatter);

            // Validate user
            var user = UserHandler.findUser(username);

            if (user == null || !user.validatePassword(password))
            {
                return "403 ❌ Invalid credentials";
            }

            // Generate transaction ID
            var transactionId = generateTransactionId();

            // Process room IDs
            var bookingResults = new StringBuilder("200 ✅ Booking results:\n");

            var anyFail = false;

            var bookings = new ArrayList<Booking>();


            for (var i = 1; i < parts.length - 4; i++)
            {
                var roomId = Integer.parseInt(parts[i]);

                // Verify room exists
                var room = RoomHandler.findRoomById(roomId);

                if(!room.getLock().tryLock(1000, TimeUnit.MILLISECONDS))
                {
                    anyFail = true;
                }
            }

            if (anyFail)
            {
                return "400 ❌ No rooms were successfully booked";
            }

            // Start from index 1 (after "BOOK") and stop before the dates and credentials
            for (var i = 1; i < parts.length - 4; i++)
            {
                try {
                    var roomId = Integer.parseInt(parts[i]);

                    // Verify room exists
                    var room = RoomHandler.findRoomById(roomId);

                    if (room == null) {
                        rollBackBookings(bookings);

                        bookingResults.append("  Room ID ").append(roomId).append(": Room not found\n");

                        break;
                    }

                    // Book the room
                    var bookingId = bookRoomWithTransaction(roomId, user.getId(), checkInTime, checkOutTime, transactionId);

                    if (bookingId == -1) {
                        anyFail = true;

                        rollBackBookings(bookings);

                        bookingResults.append("  Room ").append(room.getRoomNumber()).append(": Not available for selected dates\n");

                        break;
                    } else {
                        bookingResults.append("  Room ").append(room.getRoomNumber())
                                .append(": Successfully booked (Booking ID: ").append(bookingId).append(")\n");
                    }
                } catch (NumberFormatException e) {
                    anyFail = true;

                    rollBackBookings(bookings);

                    bookingResults.append("  Invalid room ID format: ").append(parts[i]).append("\n");
                }
            }

            for (var i = 1; i < parts.length - 4; i++)
            {
                var roomId = Integer.parseInt(parts[i]);

                // Verify room exists
                var room = RoomHandler.findRoomById(roomId);

                room.getLock().unlock();
            }

            if (anyFail)
            {
                return "400 ❌ No rooms were successfully booked";
            }

            String res = bookingResults.toString();

            bookingResults.setLength(0);

            return res;
        }
        catch (Exception e)
        {
            return "500 ❌ Booking error: " + e.getMessage();
        }
    }

    // == READ ==
    public static String listBookings(User usr)
    {
        var bookings = getBookings(usr);

        if (usr == null)
        {
            return "404 📅 No USER found";
        }

        if (bookings.isEmpty())
        {
            return "404 📅 No bookings found";
        }

        var response = new StringBuilder("200 📅 All Bookings:\n");


        for (Booking booking : bookings)
        {
            response.append("Transaction #").append(booking.getTransactionId())
                    .append("\n");

            String roomType = "UNKNOWN", roomNumber = "UNKNOWN", hotelName = "UNKNOWN";
            int roomId = -1;

            var room = dataHandler.getRooms().get(booking.getRoomId());

            var hotel = dataHandler.getHotels().get(room.getHotel());

            roomType = room.getType().toString();

            roomNumber = room.getRoomNumber();

            roomId = room.getId();

            hotelName = hotel.getName();

            User bookingUser = null;
            for(User u: dataHandler.getUsers().values())
            {
                if(u.getId() == booking.getUserId())
                {
                    bookingUser = u;
                    break;
                }
            }

            response
                    .append("  Booking #").append(booking.getId()).append("\n")
                    .append("    - Room: ").append(roomNumber).append(" (RoomId: ").append(roomId).append(")\n")
                    .append("    - User: ").append(bookingUser.getUsername()).append(" (UserId: ").append(bookingUser.getId()).append(")\n")
                    .append("    - Room Type: ").append(roomType).append("\n")
                    .append("    - Hotel: ").append(hotelName).append("\n")
                    .append("    - Check-in: ").append(booking.getCheckInTime().format(displayFormatter)).append("\n")
                    .append("    - Check-out: ").append(booking.getCheckOutTime().format(displayFormatter)).append("\n");
        }


        response.append("\n");

        String res = response.toString();

        response.setLength(0);

        return res;
    }

    public static List<Booking> getBookings(User user)
    {
        if (!user.isAdmin())
        {
            return dataHandler.getBookings().stream().filter((b) ->
            {
                return b.getUserId() == user.getId();

            }).collect(Collectors.toList());
        }

        return dataHandler.getBookings();
    }

    public static String handleCheck(String[] parts)
    {
        try
        {
            // New CHECK format: CHECK <CHECKINTIME> <CHECKOUTTIME> <USERNAME> <USERPASS>
            var checkInTime = LocalDateTime.parse(parts[1], formatter);

            var checkOutTime = LocalDateTime.parse(parts[2], formatter);

            var username = parts[3];

            var password = parts[4];

            // Validate user
            var user = UserHandler.findUser(username);

            if (user == null || !user.validatePassword(password))
            {
                return "403 ❌ Invalid credentials";
            }

            // Find all available rooms
            var availableRooms = new ArrayList<Room>();

            for (var room : dataHandler.getRooms().values())
            {
                if (isRoomAvailable(room.getId(), checkInTime, checkOutTime))
                {
                    availableRooms.add(room);
                }
            }

            if (availableRooms.isEmpty())
            {
                return "404 ❌ No rooms available for the selected dates";
            }

            var response = new StringBuilder("200 ✅ Available rooms for the selected dates:\n");

            for (var room : availableRooms)
            {
                var hotelName = "Unknown";

                for (var hotel : dataHandler.getHotels().values())
                {
                    if (hotel.getId() == room.getHotel())
                    {
                        hotelName = hotel.getName();

                        break;
                    }
                }

                response.append("  Room ID: ").append(room.getId())
                        .append(" - Number: ").append(room.getRoomNumber())
                        .append(" - Type: ").append(room.getType())
                        .append(" - Price: $").append(room.getPrice())
                        .append(" - Hotel: ").append(hotelName)
                        .append("\n");
            }

            String res = response.toString();

            response.setLength(0);

            return res;
        }
        catch (DateTimeParseException e)
        {
            return "500 ❌ Invalid date format";
        }
        catch (Exception e)
        {
            return "500 ❌ Error checking availability: " + e.getMessage();
        }
    }

    // == DELETE ==
    public static String removeBooking(String bookingIdStr)
    {
        try
        {
            var bookingId = Integer.parseInt(bookingIdStr);

            for (var booking : dataHandler.getBookings())
            {
                if (booking.getId() == bookingId)
                {
                    dataHandler.getBookings().remove(booking);

                    return "200 ✅ Booking removed successfully";
                }
            }

            return "404 ❌ Booking not found";
        }
        catch (Exception e)
        {
            return "500 ❌ Error removing booking: " + e.getMessage();
        }
    }

    // ==== HELPER METHODS ====

    public static boolean isRoomAvailable(int roomId, LocalDateTime checkInTime, LocalDateTime checkOutTime)
    {
        for (Booking booking : dataHandler.getBookings())
        {
            if (booking.getRoomId() == roomId)
            {
                // Check if there's an overlap in booking periods
                var overlapExists =
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

    private static int bookRoomWithTransaction(int roomId, int userId, LocalDateTime checkInTime, LocalDateTime checkOutTime, int transactionId)
    {
        if (!isRoomAvailable(roomId, checkInTime, checkOutTime))
        {
            return -1; // Room not available
        }

        var booking = new Booking(roomId, userId, checkInTime, checkOutTime, transactionId);

        dataHandler.getBookings().add(booking);

        return booking.getId();
    }

    private static void rollBackBookings(ArrayList<Booking> bookings)
    {
        for (Booking booking : bookings)
        {
            dataHandler.getBookings().remove(booking);
        }
    }

    private static int generateTransactionId()
    {
        var maxTransactionId = dataHandler.getBookings().stream()
                .mapToInt(Booking::getTransactionId)
                .max()
                .orElse(0);

        return maxTransactionId + 1;
    }

}
