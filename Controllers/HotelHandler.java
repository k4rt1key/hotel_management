package src.Controllers;

import src.Models.Hotel;
import src.Server.Database;

public class HotelHandler
{

    public static Hotel findHotelById(int hotelId)
    {
        return Database.hotels.get(hotelId);
    }

    public static String handleCreateHotel(String[] parts)
    {
        try
        {
            var hotelName = parts[2];

            var adminUsername = parts[3];

            var adminPassword = parts[4];

            // Validate admin
            var admin = UserHandler.findUser(adminUsername);

            if (admin == null || !admin.isAdmin() || admin.validatePassword(adminPassword))
            {
                return "403 ❌ Unauthorized access";
            }

            // Create and add hotel
            var hotel = new Hotel(hotelName);

            Database.hotels.put(hotel.getId(), hotel);

            return "200 ✅ Hotel added successfully - ID: " + hotel.getId() + " - Name: " + hotel.getName();
        }
        catch (Exception e)
        {
            return "500 ❌ Error creating hotel: " + e.getMessage();
        }
    }

    public static String listHotels()
    {
        var hotels = Database.hotels;

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

            for (var room : Database.rooms.values())
            {
                if (room.getHotel() == hotel.getId())
                {
                    response.append("          Room ID: ").append(room.getId())
                            .append(" - Number: ").append(room.getRoomNumber())
                            .append(" - Type: ").append(room.getType())
                            .append(" - Price: $").append(room.getPrice())
                            .append("\n");
                }
            }
        }


        String res = response.toString();

        response.setLength(0);

        return res;
    }

    public static String handleUpdateHotel(String[] parts)
    {
        try
        {
            var hotelId = Integer.parseInt(parts[2]);

            var hotelName = parts[3];

            var adminUsername = parts[4];

            var adminPassword = parts[5];

            // Validate admin
            var admin = UserHandler.findUser(adminUsername);

            if (admin == null || !admin.isAdmin() || admin.validatePassword(adminPassword))
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

    public static String removeHotel(String hotelIdStr)
    {
        try
        {
            var hotelId = Integer.parseInt(hotelIdStr);

            // Check if there are any rooms in this hotel
            for (var room : Database.rooms.values())
            {
                if (room.getHotel() == hotelId)
                {
                    return "400 ❌ Cannot remove hotel with existing rooms";
                }
            }


            if(Database.hotels.get(hotelId) == null)
            {
                return "404 ❌ Hotel not found";
            }

            Database.hotels.remove(hotelId);

            return "200 ✅ Hotel removed successfully";

        }
        catch (Exception e)
        {
            return "500 ❌ Error removing hotel: " + e.getMessage();
        }
    }

}
