package src.Controller;

import src.Model.Booking;
import src.Model.Hotel;
import src.Model.Room;
import src.Model.User;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class DataHandler
{
    private static DataHandler instance;

    private final HashMap<String, User> users;

    private final HashMap<Integer, Hotel> hotels;

    private final HashMap<Integer, Room> rooms;

    private final CopyOnWriteArrayList<Booking> bookings;
    

    private DataHandler()
    {
        users = new HashMap<>();

        hotels = new HashMap<>();

        rooms = new HashMap<>();

        bookings = new CopyOnWriteArrayList<>();

        // Initialize with default data
        initializeDefaultData();
    }

    public static DataHandler getDataHandler()
    {
        if (instance == null)
        {
            instance = new DataHandler();
        }

        return instance;
    }

    private void initializeDefaultData() 
    {
        User admin = new User("admin", "admin", true);

        User user = new User("user", "user", false);
        // Create admin user
        users.put(admin.getUsername(), admin);

        users.put(user.getUsername(), user);

        // Create sample hotels
        var hotel1 = new Hotel("Grand Plaza");

        var hotel2 = new Hotel("Ocean View Resort");

        var hotel3 = new Hotel("Mountain Retreat");

        hotels.put(hotel1.getId(), hotel1);

        hotels.put(hotel2.getId(), hotel2);

        hotels.put(hotel3.getId(), hotel3);

        // Create sample rooms for each hotel
        // Hotel 1: Grand Plaza
        Room room1 = new Room("A101", 100, Room.RoomType.SINGLE_ROOM, hotel1.getId());

        Room room2 = new Room("A102", 100, Room.RoomType.SINGLE_ROOM, hotel1.getId());

        Room room3 = new Room("B101", 150, Room.RoomType.DOUBLE_ROOM, hotel1.getId());

        Room room4 = new Room("B102", 150, Room.RoomType.DOUBLE_ROOM, hotel1.getId());

        Room room5 = new Room("C101", 250, Room.RoomType.DELUX_ROOM, hotel1.getId());

        Room room6 = new Room("D101", 350, Room.RoomType.SUITE, hotel1.getId());

        rooms.put(room1.getId(), room1);

        rooms.put(room2.getId(), room2);

        rooms.put(room3.getId(), room3);

        rooms.put(room4.getId(), room4);

        rooms.put(room5.getId(), room5);

        rooms.put(room6.getId(), room6);

        // Hotel 2: Ocean View Resort
        Room room7 = new Room("A201", 120, Room.RoomType.SINGLE_ROOM, hotel2.getId());

        Room room8 = new Room("A202", 120, Room.RoomType.SINGLE_ROOM, hotel2.getId());

        Room room9 = new Room("A202", 120, Room.RoomType.SINGLE_ROOM, hotel2.getId());

        Room room10 = new Room("B201", 170, Room.RoomType.DOUBLE_ROOM, hotel2.getId());

        Room room11 = new Room("B202", 170, Room.RoomType.DOUBLE_ROOM, hotel2.getId());

        Room room12 = new Room("C201", 270, Room.RoomType.DELUX_ROOM, hotel2.getId());

        Room room13 = new Room("C202", 270, Room.RoomType.DELUX_ROOM, hotel2.getId());

        Room room14 = new Room("D201", 370, Room.RoomType.SUITE, hotel2.getId());

        // Hotel 3: Mountain Retreat
        Room room15 = new Room("A301", 90, Room.RoomType.SINGLE_ROOM, hotel3.getId());

        Room room16 = new Room("A302", 90, Room.RoomType.SINGLE_ROOM, hotel3.getId());

        Room room17 = new Room("B301", 140, Room.RoomType.DOUBLE_ROOM, hotel3.getId());

        Room room18 = new Room("B302", 140, Room.RoomType.DOUBLE_ROOM, hotel3.getId());

        Room room19 = new Room("C301", 240, Room.RoomType.DELUX_ROOM, hotel3.getId());

        Room room20 = new Room("D301", 340, Room.RoomType.SUITE, hotel3.getId());

        rooms.put(room1.getId(), room1);

        rooms.put(room2.getId(), room2);

        rooms.put(room3.getId(), room3);

        rooms.put(room4.getId(), room4);

        rooms.put(room5.getId(), room5);

        rooms.put(room6.getId(), room6);

        rooms.put(room7.getId(), room7);

        rooms.put(room8.getId(), room8);

        rooms.put(room9.getId(), room9);

        rooms.put(room10.getId(), room10);

        rooms.put(room11.getId(), room11);

        rooms.put(room12.getId(), room12);

        rooms.put(room13.getId(), room13);

        rooms.put(room14.getId(), room14);

        rooms.put(room15.getId(), room15);

        rooms.put(room16.getId(), room16);

        rooms.put(room17.getId(), room17);

        rooms.put(room18.getId(), room18);

        rooms.put(room19.getId(), room19);

        rooms.put(room20.getId(), room20);        
        
    }

    // Getters for the collections
    public HashMap<String, User> getUsers()
    {
        return users;
    }

    public HashMap<Integer, Hotel> getHotels()
    {
        return hotels;
    }

    public HashMap<Integer, Room> getRooms()
    {
        return rooms;
    }

    public CopyOnWriteArrayList<Booking> getBookings()
    {
        return bookings;
    }

    public List<Booking> getBookings(User user)
    {
        if (!user.isAdmin())
        {
            return bookings.stream().filter((b) ->
            {
                return b.getUserId() == user.getId();

            }).collect(Collectors.toList());
        }

        return bookings;
    }
}
