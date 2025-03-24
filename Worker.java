package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import src.Controller.BookingHandler;
import src.Controller.DataHandler;
import src.Model.Booking;
import src.Model.Hotel;
import src.Model.Room;
import src.Model.User;

public class Worker implements Runnable
{
    // Static variables
    private static final DataHandler dataHandler = DataHandler.getDataHandler();

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Instance variables
    private Socket client;

    private BufferedReader clientReader;

    private PrintWriter clientWriter;

    private BookingHandler bookingHandler;

    // Constructor
    Worker(Socket client) throws Exception
    {
        this.client = client;

        this.clientReader = new BufferedReader(new InputStreamReader(client.getInputStream()));

        this.clientWriter = new PrintWriter(client.getOutputStream(), true);

        this.bookingHandler = BookingHandler.getBookingHandler();

        Runtime.getRuntime().addShutdownHook(new Thread(() ->
        {
            System.out.println("Worker shutdown hook triggered for client: " + client.getInetAddress());

            cleanupWorkerResources();

            System.out.println("Worker shutdown complete.");
        }));
    }

    @Override
    public void run()
    {
        try
        {
            var request = clientReader.readLine();

            var response = processRequest(request);

            clientWriter.println(response);
            clientWriter.flush();

        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        finally {
            try
            {
                client.close();
                clientReader.close();
                clientWriter.close();
            }
            catch (IOException e)
            {
                System.out.println("Error closing socket");
            }
        }
    }

    private void cleanupWorkerResources()
    {
        // Cleanup specific to this Worker instance
        try
        {
            if (client != null && !client.isClosed())
            {
                client.close();

                System.out.println("Client socket closed in shutdown hook.");
            }

            clientReader.close();

            clientWriter.close();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

    private String processRequest(String request)
    {
        try
        {
            var parts = request.split(" ");

            var command = parts[0];

            // Process commands without excessive validation (moved to client)
            switch (command)
            {
                case "LOGIN":
                    return handleLogin(parts[1], parts[2]);

                case "CREATE":
                    if (parts[1].equals("USER"))
                    {
                        return handleCreateUser(parts[2], parts[3]);
                    }
                    else if (parts[1].equals("ROOM"))
                    {
                        return handleCreateRoom(parts);
                    }
                    else if (parts[1].equals("HOTEL"))
                    {
                        return handleCreateHotel(parts);
                    }

                    return "500 ❌ Unknown CREATE command";

                case "CHECK":
                    return handleCheck(parts);

                case "BOOK":
                    return handleBooking(parts);

                case "REMOVE":
                    return handleRemove(parts);

                case "LIST":
                    return handleList(parts);

                case "UPDATE":
                    if (parts[1].equals("ROOM"))
                    {
                        return handleUpdateRoom(parts);
                    }
                    else if (parts[1].equals("HOTEL"))
                    {
                        return handleUpdateHotel(parts);
                    }

                    return "500 ❌ Unknown UPDATE command";

                default:
                    return "500 ❌ Unknown command";
            }
        }
        catch (Exception e)
        {
            return "500 ❌ Syntax Error: " + e.getMessage();
        }
    }

    private String handleLogin(String username, String password)
    {
        User user = dataHandler.getUsers().get(username);

        if (user == null)
        {
            return "404 ❌ User not found";
        }

        if (!user.validatePassword(password))
        {
            return "403 ❌ Invalid password";
        }

        return "200 ✅ Login successful. User: " + username + " (ID: " + user.getId() + "), Admin: " + (user.isAdmin() ? "Yes" : "No");
    }

    private String handleCreateUser(String username, String password)
    {
        var existingUser = findUser(username);

        if (existingUser != null)
        {
            return "409 ❌ User already exists";
        }

        var newUser = new User(username, password, false);

        dataHandler.getUsers().put(newUser.getUsername(), newUser);

        return "200 ✅ User created successfully: " + username + " (ID: " + newUser.getId() + ")";
    }

    private String handleCheck(String[] parts)
    {
        try
        {
            // New CHECK format: CHECK <CHECKINTIME> <CHECKOUTTIME> <USERNAME> <USERPASS>
            var checkInTime = LocalDateTime.parse(parts[1], formatter);

            var checkOutTime = LocalDateTime.parse(parts[2], formatter);

            var username = parts[3];

            var password = parts[4];

            // Validate user
            var user = findUser(username);

            if (user == null || !user.validatePassword(password))
            {
                return "403 ❌ Invalid credentials";
            }

            // Find all available rooms
            var availableRooms = new ArrayList<Room>();

            for (var room : dataHandler.getRooms().values())
            {
                if (bookingHandler.isRoomAvailable(room.getId(), checkInTime, checkOutTime))
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

            return response.toString();
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

    private String handleBooking(String[] parts)
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
            var user = findUser(username);

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
                var room = findRoomById(roomId);

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
                        var room = findRoomById(roomId);

                        if (room == null) {
                            rollBackBookings(bookings);

                            bookingResults.append("  Room ID ").append(roomId).append(": Room not found\n");

                            break;
                        }

                        // Book the room
                        var bookingId = bookingHandler.bookRoomWithTransaction(roomId, user.getId(), checkInTime, checkOutTime, transactionId);

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
                    var room = findRoomById(roomId);

                    room.getLock().unlock();
                }

            if (anyFail)
            {
                return "400 ❌ No rooms were successfully booked";
            }

            return bookingResults.toString();
        }
        catch (Exception e)
        {
            return "500 ❌ Booking error: " + e.getMessage();
        }
    }

    private void rollBackBookings(ArrayList<Booking> bookings)
    {
        for (Booking booking : bookings)
        {
            dataHandler.getBookings().remove(booking);
        }
    }

    private String handleCreateRoom(String[] parts)
    {
        try
        {
            var hotelId = Integer.parseInt(parts[2]);

            var roomNumber = parts[3];

            var roomTypeStr = parts[4].toUpperCase();

            var price = Integer.parseInt(parts[5]);

            var adminUsername = parts[6];

            var adminPassword = parts[7];

            // Validate admin
            var admin = findUser(adminUsername);

            if (admin == null || !admin.isAdmin() || !admin.validatePassword(adminPassword))
            {
                return "403 ❌ Unauthorized access";
            }

            // Find hotel
            var hotelExists = dataHandler.getHotels().get(hotelId) != null;

            if (!hotelExists)
            {
                return "404 ❌ Hotel not found";
            }

            // Validate room type
            Room.RoomType roomType;

            try
            {
                roomType = Room.RoomType.valueOf(roomTypeStr);
            }
            catch (IllegalArgumentException e)
            {
                return "400 ❌ Invalid room type. Valid types: SINGLE_ROOM, DOUBLE_ROOM, DELUX_ROOM, SUITE";
            }

            // Create and add room
            var room = new Room(roomNumber, price, roomType, hotelId);

            dataHandler.getRooms().put(room.getId(), room);

            return "200 ✅ Room added successfully - ID: " + room.getId();
        }
        catch (Exception e)
        {
            return "500 ❌ Error creating room: " + e.getMessage();
        }
    }

    private String handleCreateHotel(String[] parts)
    {
        try
        {
            var hotelName = parts[2];

            var adminUsername = parts[3];

            var adminPassword = parts[4];

            // Validate admin
            var admin = findUser(adminUsername);

            if (admin == null || !admin.isAdmin() || !admin.validatePassword(adminPassword))
            {
                return "403 ❌ Unauthorized access";
            }

            // Create and add hotel
            var hotel = new Hotel(hotelName);

            dataHandler.getHotels().put(hotel.getId(), hotel);

            return "200 ✅ Hotel added successfully - ID: " + hotel.getId() + " - Name: " + hotel.getName();
        }
        catch (Exception e)
        {
            return "500 ❌ Error creating hotel: " + e.getMessage();
        }
    }

    private String handleRemove(String[] parts)
    {
        try
        {
            var type = parts[1];

            var adminUsername = parts[parts.length - 2];

            var adminPassword = parts[parts.length - 1];

            // Validate admin
            var admin = findUser(adminUsername);

            if (admin == null || !admin.isAdmin() || !admin.validatePassword(adminPassword))
            {
                return "403 ❌ Unauthorized access";
            }

            switch (type)
            {
                case "USER":
                    return removeUser(parts[2]);

                case "BOOKING":
                    return removeBooking(parts[2]);

                case "ROOM":
                    return removeRoom(parts[2]);

                case "HOTEL":
                    return removeHotel(parts[2]);

                default:
                    return "500 ❌ Unknown remove type";
            }
        }
        catch (Exception e)
        {
            return "500 ❌ Error removing resource: " + e.getMessage();
        }
    }

    private String handleList(String[] parts)
    {
        try
        {
            var type = parts[1];

            var username = parts[2];

            var password = parts[3];

            // Validate admin
            var user = findUser(username);

            if (user == null || !user.validatePassword(password))
            {
                return "403 ❌ Unauthorized access";
            }

            switch (type)
            {
                case "ROOMS":
                    return listRooms();

                case "HOTELS":
                    return listHotels();

                case "USERS":
                    return listUsers();

                case "BOOKINGS":
                    return listBookings(user);

                default:
                    return "500 ❌ Unknown list type";
            }
        }
        catch (Exception e)
        {
            return "500 ❌ Error listing resources: " + e.getMessage();
        }
    }

    private String handleUpdateRoom(String[] parts)
    {
        try
        {
            var roomId = Integer.parseInt(parts[2]);

            var hotelId = Integer.parseInt(parts[3]);

            var roomNumber = parts[4];

            var roomTypeStr = parts[5].toUpperCase();

            var price = Integer.parseInt(parts[6]);

            var adminUsername = parts[7];

            var adminPassword = parts[8];

            // Validate admin
            var admin = findUser(adminUsername);

            if (admin == null || !admin.isAdmin() || !admin.validatePassword(adminPassword))
            {
                return "403 ❌ Unauthorized access";
            }

            // Find room
            var targetRoom = findRoomById(roomId);

            if (targetRoom == null)
            {
                return "404 ❌ Room not found";
            }

            // Validate hotel exists
            var hotelExists = dataHandler.getHotels().get(hotelId) != null;

            if (!hotelExists)
            {
                return "404 ❌ Hotel not found";
            }

            // Validate room type
            Room.RoomType roomType;

            try
            {
                roomType = Room.RoomType.valueOf(roomTypeStr);
            }
            catch (IllegalArgumentException e)
            {
                return "400 ❌ Invalid room type. Valid types: SINGLE_ROOM, DOUBLE_ROOM, DELUX_ROOM, SUITE";
            }

            // Update room
            targetRoom.setRoomNumber(roomNumber);

            targetRoom.setHotelId(hotelId);

            targetRoom.setType(roomType);

            targetRoom.setPrice(price);

            return "200 ✅ Room updated successfully";
        }
        catch (Exception e)
        {
            return "500 ❌ Error updating room: " + e.getMessage();
        }
    }

    private String handleUpdateHotel(String[] parts)
    {
        try
        {
            var hotelId = Integer.parseInt(parts[2]);

            var hotelName = parts[3];

            var adminUsername = parts[4];

            var adminPassword = parts[5];

            // Validate admin
            var admin = findUser(adminUsername);

            if (admin == null || !admin.isAdmin() || !admin.validatePassword(adminPassword))
            {
                return "403 ❌ Unauthorized access";
            }

            // Find hotel
            var targetHotel = findHotelById(hotelId);

            if (targetHotel == null)
            {
                return "404 ❌ Hotel not found";
            }

            // Update hotel
            targetHotel.setName(hotelName);

            return "200 ✅ Hotel updated successfully";
        }
        catch (Exception e)
        {
            return "500 ❌ Error updating hotel: " + e.getMessage();
        }
    }

    // Utility methods for finding model objects
    private User findUser(String username)
    {
        return dataHandler.getUsers().get(username);
    }

    private Room findRoomById(int roomId)
    {
        return dataHandler.getRooms().get(roomId);
    }

    private Hotel findHotelById(int hotelId)
    {
        return dataHandler.getHotels().get(hotelId);
    }

    // Transaction ID generator
    private int generateTransactionId()
    {
        var maxTransactionId = dataHandler.getBookings().stream()
                .mapToInt(Booking::getTransactionId)
                .max()
                .orElse(0);

        return maxTransactionId + 1;
    }

    // Resource removal methods
    private String removeBooking(String bookingIdStr)
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

    private String removeUser(String username)
    {
        try
        {
            if (username.equals("admin"))
            {
                return "403 ❌ Cannot remove admin user";
            }

            var targetUser = findUser(username);

            if (targetUser == null)
            {
                return "404 ❌ User not found";
            }

            // Check for future bookings
            var hasFutureBookings = dataHandler.getBookings().stream()
                    .anyMatch(booking -> booking.getUserId() == targetUser.getId()
                            && booking.getCheckOutTime().isAfter(LocalDateTime.now()));

            if (hasFutureBookings)
            {
                return "400 ❌ Cannot remove user with future bookings";
            }

            dataHandler.getUsers().remove(targetUser.getUsername());

            return "200 ✅ User removed successfully";
        }
        catch (Exception e)
        {
            return "500 ❌ Error removing user: " + e.getMessage();
        }
    }

    private String removeRoom(String roomIdStr)
    {
        try
        {
            var roomId = Integer.parseInt(roomIdStr);

            // Check for future bookings
            var hasFutureBookings = dataHandler.getBookings().stream()
                    .anyMatch(booking -> booking.getRoomId() == roomId
                            && booking.getCheckOutTime().isAfter(LocalDateTime.now()));

            if (hasFutureBookings)
            {
                return "400 ❌ Cannot remove room with future bookings";
            }

            if(dataHandler.getRooms().get(roomId) == null)
            {
                return "404 ❌ Room not found";
            }

            dataHandler.getRooms().remove(roomId);


            return "200 ✅ Room removed successfully";
        
        }
        catch (Exception e)
        {
            return "500 ❌ Error removing room: " + e.getMessage();
        }
    }

    private String removeHotel(String hotelIdStr)
    {
        try
        {
            var hotelId = Integer.parseInt(hotelIdStr);

            // Check if there are any rooms in this hotel
            for (var room : dataHandler.getRooms().values())
            {
                if (room.getHotel() == hotelId)
                {
                    return "400 ❌ Cannot remove hotel with existing rooms";
                }
            }


            if(dataHandler.getHotels().get(hotelId) == null)
            {
                return "404 ❌ Hotel not found";
            }

            dataHandler.getHotels().remove(hotelId);

            return "200 ✅ Hotel removed successfully";
 
        }
        catch (Exception e)
        {
            return "500 ❌ Error removing hotel: " + e.getMessage();
        }
    }

    // Listing methods
    private String listRooms()
    {
        var rooms = dataHandler.getRooms();

        if (rooms.isEmpty())
        {
            return "404 🏨 No rooms found";
        }

        var response = new StringBuilder("200 🏨 Rooms:\n");

        for (var room : rooms.values())
        {
            response.append("  Room ID: ").append(room.getId())
                    .append(" - Number: ").append(room.getRoomNumber())
                    .append(" - Type: ").append(room.getType())
                    .append(" - Price: $").append(room.getPrice());

            // Add hotel name
            for (var hotel : dataHandler.getHotels().values())
            {
                if (hotel.getId() == room.getHotel())
                {
                    response.append(" - Hotel: ").append(hotel.getName());

                    break;
                }
            }

            response.append("\n");
        }

        return response.toString();
    }

    private String listHotels()
    {
        var hotels = dataHandler.getHotels();

        if (hotels.isEmpty())
        {
            return "404 🏨 No hotels found";
        }

        var response = new StringBuilder("200 🏨 Hotels:\n");

        for (var hotel : hotels.values())
        {
            response.append("  Hotel ID: ").append(hotel.getId())
                    .append(" - Name: ").append(hotel.getName())
                    .append("\n");
        }

        return response.toString();
    }

    private String listUsers()
    {
        var users = dataHandler.getUsers();

        if (users.isEmpty())
        {
            return "404 👤 No users found";
        }

        var response = new StringBuilder("200 👤 Users:\n");

        for (var user : users.values())
        {
            response.append("  User ID: ").append(user.getId())
                    .append(" - Username: ").append(user.getUsername())
                    .append(" - Admin: ").append(user.isAdmin() ? "Yes" : "No")
                    .append("\n");
        }

        return response.toString();
    }

    private String listBookings(User usr)
    {
        var bookings = dataHandler.getBookings(usr);

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

              response.append("  Booking #").append(booking.getId())
                    .append(" - Room: ").append(roomNumber)
                    .append(" With RoomId ").append(roomId)
                    .append(" - Username: ").append(bookingUser.getUsername())
                      .append(" With UserId: ").append(bookingUser.getId())
                    .append(" (").append(roomType).append(")")
                    .append(" - Hotel: ").append(hotelName)
                    .append(" - Check-in: ").append(booking.getCheckInTime().format(displayFormatter))
                    .append(" - Check-out: ").append(booking.getCheckOutTime().format(displayFormatter))
                    .append("\n");
        }

        response.append("\n");

        return response.toString();
    }
}