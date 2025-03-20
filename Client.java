package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import src.Util.Validator;

public class Client
{
    private final String HOST = "localhost";

    private final int PORT = 8080;

    private String username;

    private String password;

    private boolean isAdmin = false;

    private final Scanner sc;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    // Define valid room types for validation
    private static final List<String> VALID_ROOM_TYPES = Arrays.asList(
            "SINGLE_ROOM", "DOUBLE_ROOM", "DELUX_ROOM", "SUITE");

    Client()
    {
        this.sc = new Scanner(System.in);

        displayWelcome();

        // Initial login
        var loggedIn = false;

        while (!loggedIn)
        {
            System.out.println("\nPlease login or create a new account:");

            System.out.println("1. Login (LOGIN <username> <password>)");

            System.out.println("2. Create Account (CREATE USER <username> <password>)");

            System.out.println("3. Exit");

            System.out.print(">");

            String input = sc.nextLine().trim();

            if (input.equalsIgnoreCase("3"))
            {
                System.out.println("Exiting application. Goodbye!");

                return;
            }

            if (input.toUpperCase().startsWith("LOGIN "))
            {
                if (Validator.validateLoginCommand(input))
                {
                    loggedIn = processLoginCommand(input);
                }
            }
            else if (input.toUpperCase().startsWith("CREATE USER "))
            {
                if (Validator.validateCreateUserCommand(input))
                {
                    processCreateUserCommand(input);
                }
            }
            else
            {
                System.out.println("Invalid command. Please try again.");
            }
        }

        // After login, show appropriate interface
        if (isAdmin)
        {
            showAdminInterface();
        }
        else
        {
            showUserInterface();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() ->
        {
            System.out.println("Worker shutdown hook triggered");

            cleanupWorkerResources();

            System.out.println("Worker shutdown complete.");
        }));
    }

    private void cleanupWorkerResources()
    {
        // Cleanup specific to this Worker instance
        try
        {
            sc.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void displayWelcome()
    {
        System.out.println("===============================================");

        System.out.println("         WELCOME TO HOTEL BOOKING SYSTEM       ");

        System.out.println("===============================================");
    }

    private boolean processLoginCommand(String command)
    {
        String[] parts = command.split(" ");

        this.username = parts[1];

        this.password = parts[2];

        try (
                var client = new Socket(HOST, PORT);

                var serverWriter = new PrintWriter(client.getOutputStream(), true);

                var serverReader = new BufferedReader(new InputStreamReader(client.getInputStream()))
        )
        {
            serverWriter.println(command);

            var response = serverReader.readLine();

            System.out.println(response);

            if (response.startsWith("200"))
            {
                if (username.equals("admin"))
                {
                    this.isAdmin = true;
                }

                return true;
            }

            return false;
        }
        catch (IOException e)
        {
            System.out.println("Error connecting to server: " + e.getMessage());

            return false;
        }
    }

    private void processCreateUserCommand(String command)
    {
        try (
                var client = new Socket(HOST, PORT);

                var serverWriter = new PrintWriter(client.getOutputStream(), true);

                var serverReader = new BufferedReader(new InputStreamReader(client.getInputStream()))
        )
        {
            serverWriter.println(command);

            var response = serverReader.readLine();

            System.out.println(response);
        }
        catch (IOException e)
        {
            System.out.println("Error connecting to server: " + e.getMessage());
        }
    }

    private void showUserInterface()
    {
        System.out.println("\n===============================================");

        System.out.println("          HOTEL BOOKING SYSTEM - USER          ");

        System.out.println("===============================================");

        while (true)
        {
            System.out.println("\nAvailable Commands:");

            System.out.println("- CHECK <CHECKINTIME> <CHECKOUTTIME>");

            System.out.println("- BOOK <ROOMID1> [<ROOMID2> ...] <CHECKINTIME> <CHECKOUTTIME>");

            System.out.println("- LIST BOOKINGS");

            System.out.println("- HELP");

            System.out.println("- EXIT");

            System.out.print(">");

            var command = sc.nextLine().trim();

            if(!command.startsWith("HELP") && !command.startsWith("EXIT"))
            {
                command += " " + username + " " + password;
            }

            if (command.equalsIgnoreCase("EXIT"))
            {
                System.out.println("Thank you for using the Hotel Booking System. Goodbye!");

                break;
            }
            else if (command.equalsIgnoreCase("HELP"))
            {
                displayHelp(false);
            }
            else
            {
                if (Validator.validateCommand(command))
                {
                    processCommand(command);
                }
            }
        }
    }

    private void showAdminInterface()
    {
        System.out.println("\n===============================================");

        System.out.println("          HOTEL BOOKING SYSTEM - ADMIN         ");

        System.out.println("===============================================");

        while (true)
        {
            System.out.println("\nAvailable Commands:");

            System.out.println("--- User Management ---");

            System.out.println("- CREATE USER <USERNAME> <USERPASS>");

            System.out.println("- REMOVE USER <USERID>");

            System.out.println("- LIST USERS");

            System.out.println("--- Hotel Management ---");

            System.out.println("- CREATE HOTEL <HOTELNAME>");

            System.out.println("- UPDATE HOTEL <HOTELID> <HOTELNAME>");

            System.out.println("- REMOVE HOTEL <HOTELID>");

            System.out.println("- LIST HOTELS ");

            System.out.println("--- Room Management ---");

            System.out.println("- CREATE ROOM <HOTELID> <ROOMNUMBER> <ROOMTYPE> <PRICE>");

            System.out.println("- UPDATE ROOM <ROOMID> <HOTELID> <ROOMNUMBER> <ROOMTYPE> <PRICE>");

            System.out.println("- REMOVE ROOM <ROOMID>");

            System.out.println("- LIST ROOMS ");

            System.out.println("--- Booking Management ---");

            System.out.println("- LIST BOOKINGS ");

            System.out.println("- REMOVE BOOKING <BOOKINGID>");

            System.out.println("--- Other ---");

            System.out.println("- HELP");

            System.out.println("- EXIT");

            System.out.print(">");

            var command = sc.nextLine().trim();

            if(!command.startsWith("CREATE USER") && !command.startsWith("HELP") && !command.startsWith("EXIT"))
            {
                command += " " + username + " " + password;
            }

            if (command.equalsIgnoreCase("EXIT"))
            {
                System.out.println("Thank you for using the Hotel Booking System. Goodbye!");

                break;
            }
            else if (command.equalsIgnoreCase("HELP"))
            {
                displayHelp(true);
            }
            else
            {
                if (Validator.validateCommand(command))
                {
                    processCommand(command);
                }
            }
        }
    }

    private void displayHelp(boolean isAdmin)
    {
        System.out.println("\n===============================================");

        System.out.println("                 COMMAND HELP                  ");

        System.out.println("===============================================");

        if (isAdmin)
        {
            System.out.println("\n--- User Management ---");

            System.out.println("CREATE USER <USERNAME> <USERPASS>");

            System.out.println("  - Creates a new user account");

            System.out.println("REMOVE USER <USERNAME>");

            System.out.println("  - Removes an existing user account");

            System.out.println("LIST USERS");

            System.out.println("  - Lists all user accounts");

            System.out.println("\n--- Hotel Management ---");

            System.out.println("CREATE HOTEL <HOTELNAME>");

            System.out.println("  - Creates a new hotel");

            System.out.println("UPDATE HOTEL <HOTELID> <HOTELNAME>");

            System.out.println("  - Updates an existing hotel");

            System.out.println("REMOVE HOTEL <HOTELID>");

            System.out.println("  - Removes an existing hotel");

            System.out.println("LIST HOTELS ");

            System.out.println("  - Lists all hotels");

            System.out.println("\n--- Room Management ---");

            System.out.println("CREATE ROOM <HOTELID> <ROOMNUMBER> <ROOMTYPE> <PRICE>");

            System.out.println("  - Creates a new room in a hotel");

            System.out.println("  - Valid room types: SINGLE_ROOM, DOUBLE_ROOM, DELUX_ROOM, SUITE");

            System.out.println("UPDATE ROOM <ROOMID> <HOTELID> <ROOMNUMBER> <ROOMTYPE> <PRICE>");

            System.out.println("  - Updates an existing room");

            System.out.println("REMOVE ROOM <ROOMID>");

            System.out.println("  - Removes an existing room");

            System.out.println("LIST ROOMS ");

            System.out.println("  - Lists all rooms");

            System.out.println("\n--- Booking Management ---");

            System.out.println("LIST BOOKINGS ");

            System.out.println("  - Lists all bookings");

            System.out.println("REMOVE BOOKING <BOOKINGID>");

            System.out.println("  - Removes an existing booking");
        }
        else
        {
            System.out.println("\n--- User Commands ---");

            System.out.println("CHECK <CHECKINTIME> <CHECKOUTTIME> <USERNAME> <USERPASS>");

            System.out.println("  - Checks availability for rooms in the specified time period");

            System.out.println("  - Date format: yyyy-MM-ddTHH:mm (e.g., 2025-03-19T14:30)");

            System.out.println("BOOK <ROOMID1> [<ROOMID2> ...] <CHECKINTIME> <CHECKOUTTIME> <USERNAME> <USERPASS>");

            System.out.println("  - Books one or more rooms");

            System.out.println("  - Date format: yyyy-MM-ddTHH:mm (e.g., 2025-03-19T14:30)");

            System.out.println("LIST BOOKINGS <USERNAME> <USERPASS>");

            System.out.println("  - Lists all your bookings");
        }

        System.out.println("\n--- Other Commands ---");

        System.out.println("HELP - Displays this help information");

        System.out.println("EXIT - Exits the application");
    }

    private void processCommand(String command)
    {
        var uppercaseCommand = command.toUpperCase();

        // Format dates if needed
        if (uppercaseCommand.startsWith("CHECK ") || uppercaseCommand.startsWith("BOOK "))
        {
            command = formatDateTimeInCommand(command);
        }

        try (
                var client = new Socket(HOST, PORT);

                var serverWriter = new PrintWriter(client.getOutputStream(), true);

                var serverReader = new BufferedReader(new InputStreamReader(client.getInputStream()))
        )
        {
            serverWriter.println(command);

            // Read all lines of the response
            var response = new StringBuilder();

            var line = "";

            while ((line = serverReader.readLine()) != null)
            {
                response.append(line).append("\n");

                if (line.isEmpty())
                {
                    break;
                }
            }

            System.out.println(response.toString());
        }
        catch (IOException e)
        {
            System.out.println("Error communicating with server: " + e.getMessage());
        }
    }

    private String formatDateTimeInCommand(String command)
    {
        var parts = command.split(" ");

        // Format dates in CHECK command
        if (command.toUpperCase().startsWith("CHECK "))
        {
            if (parts.length >= 3)
            {
                try
                {
                    parts[1] = LocalDateTime.parse(parts[1], inputFormatter).format(formatter);

                    parts[2] = LocalDateTime.parse(parts[2], inputFormatter).format(formatter);
                }
                catch (DateTimeParseException e)
                {
                    // Already validated, should not happen
                }
            }
        }
        // Format dates in BOOK command
        else if (command.toUpperCase().startsWith("BOOK "))
        {
            if (parts.length >= 5)
            {
                var lastIndex = parts.length - 1;

                try
                {
                    parts[lastIndex - 3] = LocalDateTime.parse(parts[lastIndex - 3], inputFormatter).format(formatter);

                    parts[lastIndex - 2] = LocalDateTime.parse(parts[lastIndex - 2], inputFormatter).format(formatter);
                }
                catch (DateTimeParseException e)
                {
                    // Already validated, should not happen
                }
            }
        }

        return String.join(" ", parts);
    }

    public static void main(String[] args)
    {
        var client = new Client();
    }
}