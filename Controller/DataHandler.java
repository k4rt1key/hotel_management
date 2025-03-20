package src.Controller;

import src.Model.Booking;
import src.Model.Hotel;
import src.Model.Room;
import src.Model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class DataHandler
{
    private static DataHandler instance;

    private final List<User> users;

    private final List<Hotel> hotels;

    private final List<Room> rooms;

    private final CopyOnWriteArrayList<Booking> bookings;

    private DataHandler()
    {
        users = new ArrayList<>();

        hotels = new ArrayList<>();

        rooms = new ArrayList<>();

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

    private void initializeDefaultData() {
        // Create admin user
        users.add(new User("admin", "admin", true));

        users.add(new User("user", "user", false));

        // Create sample hotels
        Hotel hotel1 = new Hotel("Grand Plaza");

        Hotel hotel2 = new Hotel("Ocean View Resort");

        Hotel hotel3 = new Hotel("Mountain Retreat");

        hotels.add(hotel1);

        hotels.add(hotel2);

        hotels.add(hotel3);

        // Create sample rooms for each hotel
        // Hotel 1: Grand Plaza
        rooms.add(new Room("A101", 100, Room.RoomType.SINGLE_ROOM, hotel1.getId()));

        rooms.add(new Room("A102", 100, Room.RoomType.SINGLE_ROOM, hotel1.getId()));

        rooms.add(new Room("B101", 150, Room.RoomType.DOUBLE_ROOM, hotel1.getId()));

        rooms.add(new Room("B102", 150, Room.RoomType.DOUBLE_ROOM, hotel1.getId()));

        rooms.add(new Room("C101", 250, Room.RoomType.DELUX_ROOM, hotel1.getId()));

        rooms.add(new Room("D101", 350, Room.RoomType.SUITE, hotel1.getId()));

        // Hotel 2: Ocean View Resort
        rooms.add(new Room("A201", 120, Room.RoomType.SINGLE_ROOM, hotel2.getId()));

        rooms.add(new Room("A202", 120, Room.RoomType.SINGLE_ROOM, hotel2.getId()));

        rooms.add(new Room("B201", 170, Room.RoomType.DOUBLE_ROOM, hotel2.getId()));

        rooms.add(new Room("B202", 170, Room.RoomType.DOUBLE_ROOM, hotel2.getId()));

        rooms.add(new Room("C201", 270, Room.RoomType.DELUX_ROOM, hotel2.getId()));

        rooms.add(new Room("D201", 370, Room.RoomType.SUITE, hotel2.getId()));

        // Hotel 3: Mountain Retreat
        rooms.add(new Room("A301", 90, Room.RoomType.SINGLE_ROOM, hotel3.getId()));

        rooms.add(new Room("A302", 90, Room.RoomType.SINGLE_ROOM, hotel3.getId()));

        rooms.add(new Room("B301", 140, Room.RoomType.DOUBLE_ROOM, hotel3.getId()));

        rooms.add(new Room("B302", 140, Room.RoomType.DOUBLE_ROOM, hotel3.getId()));

        rooms.add(new Room("C301", 240, Room.RoomType.DELUX_ROOM, hotel3.getId()));

        rooms.add(new Room("D301", 340, Room.RoomType.SUITE, hotel3.getId()));
    }

    // Getters for the collections
    public List<User> getUsers()
    {
        return users;
    }

    public List<Hotel> getHotels()
    {
        return hotels;
    }

    public List<Room> getRooms()
    {
        return rooms;
    }

    public List<Booking> getBookings()
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

            }).toList();
        }

        return bookings;
    }
}
