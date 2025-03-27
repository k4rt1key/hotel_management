package src.Server;

import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server
{
    private static final int PORT = 8080;

    private static ServerSocket server;

    private final static ExecutorService threadPool = Executors.newFixedThreadPool(8);

    private static boolean running = true;

    public static void main(String[] args)
    {
        try
        {
            Runtime.getRuntime().addShutdownHook(new Thread(Server::shutdown));

            // Database.getDatabase(); // TODO : What is the purpose??
            // Server.getInstance().start(); // TODO : What is the purpose??

            Database.populateSeedData();

            server = new ServerSocket(PORT);

            server.setReuseAddress(true);

            System.out.println("Server started on port [" + PORT + "]");

            acceptConnections();

        }
        catch (Exception e)
        {
            System.out.println("Server error -> " + e.getMessage());
        }
    }
    

    // TODO: Why static variable access in constructor?
    // private Server() throws IOException { }

    private static void acceptConnections()
    {
        try
        {
            while (running)
            {
                var client = server.accept();

                System.out.println("Connected IP [" + client.getInetAddress().getHostAddress() + "] PORT [" + client.getPort() + "]");
                
                threadPool.execute(new Worker(client));
            }
        }
        catch (Exception e)
        {
            if (running)
            {
                System.out.println("Server error -> " + e.getMessage());
            }
        }
    }

    private static void shutdown()
    {
        running = false;

        try
        {
            if (server != null && !server.isClosed())
            {
                server.close();
            }

            if (!threadPool.isShutdown())
            {
                threadPool.shutdown();
            }

            System.out.println("Server shutdown successfully");
        }
        catch (Exception e)
        {
            System.out.println("Server error during shutdown -> " + e.getMessage());
        }
    }

}
