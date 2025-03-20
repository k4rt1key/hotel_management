package src;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import src.Controller.DataHandler;

public class Server
{
    // Using a constant PORT as this is not expected to change
    private static final int PORT = 8080;

    private static ServerSocket server;

    private static ExecutorService threadPool;

    private static Server instance;

    private static boolean running = true;

    public static Server getInstance() throws IOException
    {
        if(instance == null)
        {
            instance = new Server();
        }

        return instance;
    }

    private Server() throws IOException
    {
        this.server = new ServerSocket(PORT);

        // Enable address reuse for quicker server restart
        server.setReuseAddress(true);

        // Add shutdown hook to gracefully close server
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

        // Using a fixed thread pool with 8 threads to handle concurrent client connections
        // This number can be adjusted based on expected load and server capacity
        this.threadPool = Executors.newFixedThreadPool(8);

    }

    private void start() throws Exception
    {

        System.out.println("Server started on port " + PORT);

        // Start accepting client connections
        acceptConnections();
    }

    private static void acceptConnections()
    {
        try
        {
            while (running)
            {
                // Accept client
                var client = server.accept();

                // Start client handler
                System.out.println("Client connected with address " + client.getInetAddress());

                threadPool.execute(new Worker(client));
            }
        }
        catch (Exception e)
        {
            if (running)
            {
                e.printStackTrace();
            }
        }
    }

    private void shutdown()
    {
        running = false;

        try
        {
            if (server != null && !server.isClosed())
            {
                server.close();
            }

            if (threadPool != null && !threadPool.isShutdown())
            {
                threadPool.shutdown();
            }

            // Save data before shutting down
            System.out.println("Server shutting down...");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        try
        {
            // Initialize data handler with default data
            DataHandler.getDataHandler();
            System.out.println("Data initialized with default values");

            // Start server
            Server.getInstance().start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
