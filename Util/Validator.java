// src/Util/Validator.java
package src.Util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

public class Validator
{

    private static final DateTimeFormatter inputDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    // Define valid room types for validation
    private static final List<String> VALID_ROOM_TYPES = Arrays.asList
            (
                    "SINGLE_ROOM", "DOUBLE_ROOM", "DELUX_ROOM", "SUITE"
            );

    public static boolean validateCommand(String command)
    {
        var parts = command.split(" ");

        if (parts.length == 0)
        {
            System.out.println("Error: Empty command");

            return false;
        }

        var commandType = parts[0].toUpperCase();

        // Validate based on command type
        switch (commandType)
        {
            case "CHECK":
                return validateCheckCommand(parts);

            case "BOOK":
                return validateBookCommand(parts);

            case "CREATE":
                if (parts.length < 2)
                {
                    System.out.println("Error: Invalid CREATE command");

                    return false;
                }

                var createType = parts[1].toUpperCase();

                switch (createType)
                {
                    case "USER":
                        return validateCreateUserCommand(command);

                    case "HOTEL":
                        return validateCreateHotelCommand(parts);

                    case "ROOM":
                        return validateCreateRoomCommand(parts);

                    default:
                        System.out.println("Error: Unknown CREATE type");

                        return false;
                }

            case "REMOVE":
                if (parts.length < 2)
                {
                    System.out.println("Error: Invalid REMOVE command");

                    return false;
                }

                var removeType = parts[1].toUpperCase();

                switch (removeType)
                {
                    case "USER":
                        return validateRemoveUserCommand(parts);

                    case "BOOKING":
                        return validateRemoveBookingCommand(parts);

                    case "ROOM":
                        return validateRemoveRoomCommand(parts);

                    case "HOTEL":
                        return validateRemoveHotelCommand(parts);

                    default:
                        System.out.println("Error: Unknown REMOVE type");

                        return false;
                }

            case "LIST":
                if (parts.length < 2)
                {
                    System.out.println("Error: Invalid LIST command");

                    return false;
                }

                var listType = parts[1].toUpperCase();

                switch (listType)
                {
                    case "ROOMS":
                        return validateListCommand(parts, "ROOMS");

                    case "HOTELS":
                        return validateListCommand(parts, "HOTELS");

                    case "USERS":
                        return validateListCommand(parts, "USERS");

                    case "BOOKINGS":
                        return validateListCommand(parts, "BOOKINGS");

                    default:
                        System.out.println("Error: Unknown LIST type");

                        return false;
                }

            case "UPDATE":
                if (parts.length < 2)
                {
                    System.out.println("Error: Invalid UPDATE command");

                    return false;
                }

                var updateType = parts[1].toUpperCase();

                switch (updateType)
                {
                    case "ROOM":
                        return validateUpdateRoomCommand(parts);

                    case "HOTEL":
                        return validateUpdateHotelCommand(parts);

                    default:
                        System.out.println("Error: Unknown UPDATE type");

                        return false;
                }

            default:
                System.out.println("Error: Unknown command");

                return false;
        }
    }

    public static boolean validateLoginCommand(String command)
    {
        var parts = command.split(" ");

        if (parts.length != 3)
        {
            System.out.println("Syntax Error: LOGIN <USERNAME> <USERPASS>");

            return false;
        }

        if (!Validator.checkNull(parts[1], "Username") || !Validator.checkNull(parts[2], "Password"))
        {
            return false;
        }

        return true;
    }

    public static boolean validateCreateUserCommand(String command)
    {
       var parts = command.split(" ");

        if (parts.length != 4)
        {
            System.out.println("Syntax Error: CREATE USER <USERNAME> <USERPASS>");

            return false;
        }

        if (!parts[1].equals("USER"))
        {
            System.out.println("Syntax Error: CREATE USER <USERNAME> <USERPASS>");

            return false;
        }

        if (!checkNull(parts[2], "Username") || !checkNull(parts[3], "Password"))
        {
            return false;
        }

        return true;
    }

    private static boolean validateCheckCommand(String[] parts)
    {
        if (parts.length != 5)
        {
            System.out.println("Syntax Error: CHECK <CHECKINTIME> <CHECKOUTTIME> <USERNAME> <USERPASS>");

            return false;
        }

        if (!checkDate(parts[1], "Check-in time") || !checkDate(parts[2], "Check-out time"))
        {
            return false;
        }

        if (!checkInOutTime(parts[1], parts[2]))
        {
            return false;
        }

        if (!checkNull(parts[3], "Username") || !checkNull(parts[4], "Password"))
        {
            return false;
        }

        return true;
    }

    private static boolean validateBookCommand(String[] parts)
    {
        // BOOK <ROOMID1> [<ROOMID2> ...] <CHECKINTIME> <CHECKOUTTIME> <USERNAME> <USERPASS>
        if (parts.length < 6)
        {
            System.out.println("Syntax Error: BOOK <ROOMID1> [<ROOMID2> ...] <CHECKINTIME> <CHECKOUTTIME> <USERNAME> <USERPASS>");

            return false;
        }

        // Check if there's at least one room ID
        if (!checkInt(parts[1], "Room ID"))
        {
            return false;
        }

        // The last four parameters are: check-in time, check-out time, username, password
        var lastIndex = parts.length - 1;

        var password = parts[lastIndex];

        var username = parts[lastIndex - 1];

        var checkOutTime = parts[lastIndex - 2];

        var checkInTime = parts[lastIndex - 3];

        if (!checkDate(checkInTime, "Check-in time") || !checkDate(checkOutTime, "Check-out time"))
        {
            return false;
        }

        if (!checkInOutTime(checkInTime, checkOutTime))
        {
            return false;
        }

        if (!checkNull(username, "Username") || !checkNull(password, "Password"))
        {
            return false;
        }

        // Check if all room IDs are valid integers
        for (int i = 1; i < parts.length - 4; i++)
        {
            if (!checkInt(parts[i], "Room ID"))
            {
                return false;
            }
        }

        return true;
    }

    private static boolean validateCreateHotelCommand(String[] parts)
    {
        // CREATE HOTEL <HOTELNAME>
        if (parts.length != 5)
        {
            System.out.println("Syntax Error: CREATE HOTEL <HOTELNAME>");

            return false;
        }

        if (!checkNull(parts[2], "Hotel name"))
        {
            return false;
        }

        if (!checkNull(parts[3], "Admin username") || !checkNull(parts[4], "Admin password"))
        {
            return false;
        }

        return true;
    }

    private static boolean validateCreateRoomCommand(String[] parts)
    {
        // CREATE ROOM <HOTELID> <ROOMNUMBER> <ROOMTYPE> <PRICE>
        if (parts.length != 8)
        {
            System.out.println("Syntax Error: CREATE ROOM <HOTELID> <ROOMNUMBER> <ROOMTYPE> <PRICE>");

            return false;
        }

        if (!checkInt(parts[2], "Hotel ID"))
        {
            return false;
        }

        if (!checkNull(parts[3], "Room number"))
        {
            return false;
        }

        if (!checkRoomType(parts[4]))
        {
            return false;
        }

        if (!checkInt(parts[5], "Price"))
        {
            return false;
        }

        if (!checkNull(parts[6], "Admin username") || !checkNull(parts[7], "Admin password"))
        {
            return false;
        }

        return true;
    }

    private static boolean validateRemoveUserCommand(String[] parts)
    {
        // REMOVE USER <USERID>
        if (parts.length != 5)
        {
            System.out.println("Syntax Error: REMOVE USER <USERID>");

            return false;
        }

        if (!checkNull(parts[2], "User ID"))
        {
            return false;
        }

        if (!checkNull(parts[3], "Admin username") || !checkNull(parts[4], "Admin password"))
        {
            return false;
        }

        return true;
    }

    private static boolean validateRemoveBookingCommand(String[] parts)
    {
        // REMOVE BOOKING <BOOKINGID>
        if (parts.length != 5)
        {
            System.out.println("Syntax Error: REMOVE BOOKING <BOOKINGID>");

            return false;
        }

        if (!checkInt(parts[2], "Booking ID"))
        {
            return false;
        }

        if (!checkNull(parts[3], "Admin username") || !checkNull(parts[4], "Admin password"))
        {
            return false;
        }

        return true;
    }

    private static boolean validateRemoveRoomCommand(String[] parts)
    {
        // REMOVE ROOM <ROOMID>
        if (parts.length != 5)
        {
            System.out.println("Syntax Error: REMOVE ROOM <ROOMID>");

            return false;
        }

        if (!checkInt(parts[2], "Room ID"))
        {
            return false;
        }

        if (!checkNull(parts[3], "Admin username") || !checkNull(parts[4], "Admin password"))
        {
            return false;
        }

        return true;
    }

    private static boolean validateRemoveHotelCommand(String[] parts)
    {
        // REMOVE HOTEL <HOTELID>
        if (parts.length != 5)
        {
            System.out.println("Syntax Error: REMOVE HOTEL <HOTELID>");

            return false;
        }

        if (!checkInt(parts[2], "Hotel ID"))
        {
            return false;
        }

        if (!checkNull(parts[3], "Admin username") || !checkNull(parts[4], "Admin password"))
        {
            return false;
        }

        return true;
    }

    private static boolean validateListCommand(String[] parts, String listType)
    {
        // LIST <TYPE>
        if (parts.length != 4)
        {
            System.out.println("Syntax Error: LIST " + listType + " ");

            return false;
        }

        if (!checkNull(parts[2], "Admin username") || !checkNull(parts[3], "Admin password"))
        {
            return false;
        }

        return true;
    }

    private static boolean validateUpdateRoomCommand(String[] parts)
    {
        // UPDATE ROOM <ROOMID> <HOTELID> <ROOMNUMBER> <ROOMTYPE> <PRICE>
        if (parts.length != 9)
        {
            System.out.println("Syntax Error: UPDATE ROOM <ROOMID> <HOTELID> <ROOMNUMBER> <ROOMTYPE> <PRICE>");

            return false;
        }

        if (!checkInt(parts[2], "Room ID"))
        {
            return false;
        }

        if (!checkInt(parts[3], "Hotel ID"))
        {
            return false;
        }

        if (!checkNull(parts[4], "Room number"))
        {
            return false;
        }

        if (!checkRoomType(parts[5]))
        {
            return false;
        }

        if (!checkInt(parts[6], "Price"))
        {
            return false;
        }

        if (!checkNull(parts[7], "Admin username") || !checkNull(parts[8], "Admin password"))
        {
            return false;
        }

        return true;
    }

    private static boolean validateUpdateHotelCommand(String[] parts)
    {
        // UPDATE HOTEL <HOTELID> <HOTELNAME>
        if (parts.length != 6)
        {
            System.out.println("Syntax Error: UPDATE HOTEL <HOTELID> <HOTELNAME>");

            return false;
        }

        if (!checkInt(parts[2], "Hotel ID"))
        {
            return false;
        }

        if (!checkNull(parts[3], "Hotel name"))
        {
            return false;
        }

        if (!checkNull(parts[4], "Admin username") || !checkNull(parts[5], "Admin password"))
        {
            return false;
        }

        return true;
    }

    // ================= Validation helper methods =====================

    private static boolean checkNull(String value, String fieldName)
    {
        if (value == null || value.trim().isEmpty())
        {
            System.out.println("Error: " + fieldName + " cannot be empty");

            return false;
        }

        return true;
    }

    private static boolean checkInt(String value, String fieldName)
    {
        try
        {
            Integer.parseInt(value);

            return true;
        }
        catch (NumberFormatException e)
        {
            System.out.println("Error: " + fieldName + " must be a valid integer");

            return false;
        }
    }

    private static boolean checkDate(String dateStr, String fieldName)
    {
        try
        {
            LocalDateTime.parse(dateStr, inputDateFormatter);

            return true;
        }
        catch (DateTimeParseException e)
        {
            System.out.println("Error: " + fieldName + " must be in the format yyyy-MM-ddTHH:mm (e.g., 2025-03-19T14:30)");

            return false;
        }
    }

    private static boolean checkInOutTime(String checkInStr, String checkOutStr)
    {
        try
        {
            var checkIn = LocalDateTime.parse(checkInStr, inputDateFormatter);
            var checkOut = LocalDateTime.parse(checkOutStr, inputDateFormatter);

            if (checkIn.isAfter(checkOut) || checkIn.isEqual(checkOut))
            {
                System.out.println("Error: Check-out time must be after check-in time");

                return false;
            }

            // Check if check-in time is in the future
            if (checkIn.isBefore(LocalDateTime.now()))
            {
                System.out.println("Error: Check-in time must be in the future");

                return false;
            }

            return true;
        }
        catch (DateTimeParseException e)
        {
            // This should not happen since we already checked the date format
            return false;
        }
    }

    private static boolean checkRoomType(String roomType)
    {
        if (!VALID_ROOM_TYPES.contains(roomType.toUpperCase()))
        {
            System.out.println("Error: Invalid room type. Valid types: " + String.join(", ", VALID_ROOM_TYPES));

            return false;
        }

        return true;
    }
}