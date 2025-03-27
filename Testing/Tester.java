package src.Testing;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Tester
{
    private static final String SERVER_ADDRESS = "localhost";

    private static final int SERVER_PORT = 8081; // Update this to match your server port

    private static final int ROOMS = 18; // Number of rooms

    private static final int REQUESTS_PER_ROOM = 700; // Requests per room

    private static final int TOTAL_THREADS = ROOMS * REQUESTS_PER_ROOM; // 1000 total threads

    private static final AtomicInteger successfulBookings = new AtomicInteger(0);

    private static final AtomicInteger failedBookings = new AtomicInteger(0);

    private static final AtomicInteger[] roomSuccessCount = new AtomicInteger[ROOMS + 1];

    static
    {
        // Initialize counters for each room
        for (int i = 1; i <= ROOMS; i++)
        {
            roomSuccessCount[i] = new AtomicInteger(0);
        }
    }

    public static void main(String[] args)
    {

        var executor = Executors.newFixedThreadPool(TOTAL_THREADS);

        var latch = new CountDownLatch(TOTAL_THREADS);

        var startTime = System.currentTimeMillis();

        // Launch threads for each room
        for (var roomNum = 1; roomNum <= ROOMS; roomNum++)
        {
            final var room = roomNum;

            // Launch multiple requests for the same room
            for (var i = 0; i < REQUESTS_PER_ROOM; i++)
            {
                final var requestNum = i;

                executor.submit(() ->
                {
                    try
                    {
                        var bookingCommand = "BOOK " + room + " 2025-05-12T10:10 2025-05-21T10:10 user user";

                        var bookingResponse = sendCommand(bookingCommand);

                        System.out.println("***Room " + room + ", Request " + requestNum +
                                " response: " + bookingResponse);

                        if (bookingResponse.startsWith("200"))
                        {
                            successfulBookings.incrementAndGet();

                            roomSuccessCount[room].incrementAndGet();
                        } 
                        else
                        {
                            failedBookings.incrementAndGet();
                        }
                    }
                    catch (Exception e)
                    {
                        System.err.println("Room " + room + ", Request " + requestNum +
                                " error: " + e.getMessage());

                        failedBookings.incrementAndGet();
                    }
                    finally
                    {
                        latch.countDown();
                    }
                });
            }
        }

        try
        {
            latch.await(); // Wait for all threads to complete

            var endTime = System.currentTimeMillis();

            System.out.println("***\n=== Test Results ===");

            System.out.println("***Total threads: " + TOTAL_THREADS);

            System.out.println("***Successful bookings: " + successfulBookings.get());

            System.out.println("***Failed bookings: " + failedBookings.get());

            // Print success rate per room
            System.out.println("***\n=== Room Success Rates ===");

            for (var i = 1; i <= ROOMS; i++)
            {
                System.out.println("***Room " + i + ": " + roomSuccessCount[i].get() +
                        "/" + REQUESTS_PER_ROOM + " successful (" +
                        (roomSuccessCount[i].get() * 100.0 / REQUESTS_PER_ROOM) + "%)");
            }

            System.out.println("***\nTotal time: " + (endTime - startTime) + "ms");

            System.out.println("***Average response time per request: " +
                   ((endTime - startTime) / (float)TOTAL_THREADS) + "ms");

            System.out.println("***Requests per second: " +
                   (1000.0 * TOTAL_THREADS / (endTime - startTime)));

        }
        catch (InterruptedException e)
        {
            System.err.println("Main thread interrupted: " + e.getMessage());
        }
        finally
        {
            executor.shutdown();
        }
    }

    private static String sendCommand(String command) throws IOException
    {
        try (var socket = new Socket(SERVER_ADDRESS, SERVER_PORT))
        {
            // Set a timeout to prevent hanging connections
            socket.setSoTimeout(10000); // 10 seconds timeout

            // Send command
            var out = new PrintWriter(socket.getOutputStream(), true);

            out.println(command);

            // Read response
            var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            var response = new StringBuilder();

            var line = "";

            while ((line = in.readLine()) != null)
            {
                response.append(line).append("\n");
            }

            in.close();

            String res = response.toString().trim();

            response.setLength(0);

            return res;
        }
    }
}