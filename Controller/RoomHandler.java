package src.Controller;

import src.Model.Room;

import java.time.LocalDateTime;

public class RoomHandler
{
    private static DataHandler dataHandler = DataHandler.getDataHandler();

    public static Room findRoomById(int roomId)
    {
        return dataHandler.getRooms().get(roomId);
    }

    // ==== CRUD ====

    // == CREATE ==
    public static String handleCreateRoom(String[] parts)
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
            var admin = UserHandler.findUser(adminUsername);

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

    // == READ ==
    public static String listRooms()
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

        String res = response.toString();

        response.setLength(0);

        return res;
    }

    // == UPDATE ==
    public static String handleUpdateRoom(String[] parts)
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
            var admin = UserHandler.findUser(adminUsername);

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

    // == DELETE ==
    public static String removeRoom(String roomIdStr)
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

}
