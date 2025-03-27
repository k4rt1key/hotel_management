package src.Server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import src.Controllers.*;


public class Worker implements Runnable
{
    private final Socket client;

    private final BufferedReader clientReader;

    private final PrintWriter clientWriter;

    // Constructor
    Worker(Socket client) throws Exception
    {
        Runtime.getRuntime().addShutdownHook(new Thread(this::cleanupWorkerResources));

        this.client = client;

        this.clientReader = new BufferedReader(new InputStreamReader(client.getInputStream()));

        this.clientWriter = new PrintWriter(client.getOutputStream(), true);

    }

    @Override
    public void run()
    {
        try
        {
            var request = clientReader.readLine();

            var response = processRequest(request);

            clientWriter.println(response);

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

            return switch (command)
            {
                case "LOGIN" -> UserHandler.handleLogin(parts[1], parts[2]);
                case "CREATE" ->
                {
                    switch (parts[1])
                    {
                        case "USER" ->
                        {
                            yield UserHandler.handleCreateUser(parts[2], parts[3]);
                        }

                        case "ROOM" ->
                        {
                            yield RoomHandler.handleCreateRoom(parts);
                        }

                        case "HOTEL" -> {
                            yield HotelHandler.handleCreateHotel(parts);
                        }
                    }

                    yield "500 ❌ Unknown CREATE command";
                }

                case "CHECK" -> BookingHandler.handleCheck(parts);

                case "BOOK" -> BookingHandler.handleBooking(parts);

                case "REMOVE" -> handleRemove(parts);

                case "LIST" -> handleList(parts);

                case "UPDATE" ->
                {
                    if (parts[1].equals("ROOM"))
                    {
                        yield RoomHandler.handleUpdateRoom(parts);
                    }
                    else if (parts[1].equals("HOTEL"))
                    {
                        yield HotelHandler.handleUpdateHotel(parts);
                    }

                    yield "500 ❌ Unknown UPDATE command";
                }
                default -> "500 ❌ Unknown command";
            };
        }
        catch (Exception e)
        {
            System.out.println("Worker error = " + e.getMessage());

            return "500 ❌ Internal Server error";
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

            if (user == null || user.validatePassword(password))
            {
                return "403 ❌ Unauthorized access";
            }

            return switch (type) {
                case "ROOMS" -> RoomHandler.listRooms();
                case "HOTELS" -> HotelHandler.listHotels();
                case "USERS" -> UserHandler.listUsers();
                case "BOOKINGS" -> BookingHandler.listBookings(user);
                default -> "500 ❌ Unknown list type";
            };
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

            if (admin == null || !admin.isAdmin() || admin.validatePassword(adminPassword))
            {
                return "403 ❌ Unauthorized access";
            }

            return switch (type) {
                case "USER" -> UserHandler.removeUser(parts[2]);
                case "BOOKING" -> BookingHandler.removeBooking(parts[2]);
                case "ROOM" -> RoomHandler.removeRoom(parts[2]);
                case "HOTEL" -> HotelHandler.removeHotel(parts[2]);
                default -> "500 ❌ Unknown remove type";
            };
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
            System.out.println("Worker shutdown hook triggered...");

            if (client != null && !client.isClosed())
            {
                client.close();

                System.out.println("Client socket closed in shutdown hook.");
            }

            clientReader.close();

            clientWriter.close();

            System.out.println("Worker shutdown complete.");

        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

}