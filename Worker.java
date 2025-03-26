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
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import src.Controller.*;
import src.Model.Booking;
import src.Model.Hotel;
import src.Model.Room;
import src.Model.User;

public class Worker implements Runnable
{
    // Static variables
    private static final DataHandler dataHandler = DataHandler.getDataHandler();

    // Instance variables
    private Socket client;

    private BufferedReader clientReader;

    private PrintWriter clientWriter;

    // Constructor
    Worker(Socket client) throws Exception
    {
        this.client = client;

        this.clientReader = new BufferedReader(new InputStreamReader(client.getInputStream()));

        this.clientWriter = new PrintWriter(client.getOutputStream(), true);

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
                    return UserHandler.handleLogin(parts[1], parts[2]);

                case "CREATE":
                    if (parts[1].equals("USER"))
                    {
                        return UserHandler.handleCreateUser(parts[2], parts[3]);
                    }
                    else if (parts[1].equals("ROOM"))
                    {
                        return RoomHandler.handleCreateRoom(parts);
                    }
                    else if (parts[1].equals("HOTEL"))
                    {
                        return HostelHandler.handleCreateHotel(parts);
                    }

                    return "500 ❌ Unknown CREATE command";

                case "CHECK":
                    return BookingHandler.handleCheck(parts);

                case "BOOK":
                    return BookingHandler.handleBooking(parts);

                case "REMOVE":
                    return handleRemove(parts);

                case "LIST":
                    return handleList(parts);

                case "UPDATE":
                    if (parts[1].equals("ROOM"))
                    {
                        return RoomHandler.handleUpdateRoom(parts);
                    }
                    else if (parts[1].equals("HOTEL"))
                    {
                        return HostelHandler.handleUpdateHotel(parts);
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

    private String handleList(String[] parts)
    {
        try
        {
            var type = parts[1];

            var username = parts[2];

            var password = parts[3];

            // Validate admin
            var user = UserHandler.findUser(username);

            if (user == null || !user.validatePassword(password))
            {
                return "403 ❌ Unauthorized access";
            }

            switch (type)
            {
                case "ROOMS":
                    return RoomHandler.listRooms();

                case "HOTELS":
                    return HostelHandler.listHotels();

                case "USERS":
                    return UserHandler.listUsers();

                case "BOOKINGS":
                    return BookingHandler.listBookings(user);

                default:
                    return "500 ❌ Unknown list type";
            }
        }
        catch (Exception e)
        {
            return "500 ❌ Error listing resources: " + e.getMessage();
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
            var admin = UserHandler.findUser(adminUsername);

            if (admin == null || !admin.isAdmin() || !admin.validatePassword(adminPassword))
            {
                return "403 ❌ Unauthorized access";
            }

            switch (type)
            {
                case "USER":
                    return UserHandler.removeUser(parts[2]);

                case "BOOKING":
                    return BookingHandler.removeBooking(parts[2]);

                case "ROOM":
                    return RoomHandler.removeRoom(parts[2]);

                case "HOTEL":
                    return HostelHandler.removeHotel(parts[2]);

                default:
                    return "500 ❌ Unknown remove type";
            }
        }
        catch (Exception e)
        {
            return "500 ❌ Error removing resource: " + e.getMessage();
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

}