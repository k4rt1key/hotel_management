package src.Model;

import java.util.concurrent.locks.ReentrantLock;

public class Room
{
    public enum RoomType
    {
        SINGLE_ROOM,

        DOUBLE_ROOM,

        DELUX_ROOM,

        SUITE
    }

    private static int roomCounter = 0;

    private final int id;

    private RoomType type;

    private int price;

    private int hotelId;

    private String roomNumber;

    private final ReentrantLock lock;

    public Room(String roomNumber, int price, RoomType type, int hotelId)
    {
        this.id = ++roomCounter;

        this.roomNumber = roomNumber;

        this.price = price;

        this.type = type;

        this.hotelId = hotelId;

        this.lock = new ReentrantLock();
    }

    // Getters
    public int getId()
    {
        return id;
    }

    public RoomType getType()
    {
        return type;
    }

    public int getPrice()
    {
        return price;
    }

    public int getHotel()
    {
        return hotelId;
    }

    public String getRoomNumber()
    {
        return roomNumber;
    }

    public ReentrantLock getLock()
    {
        return lock;
    }

    // Setters for updateable fields
    public void setPrice(int price)
    {
        this.price = price;
    }

    public void setHotelId(int hotelId)
    {
        this.hotelId = hotelId;
    }

    public void setRoomNumber(String roomNumber)
    {
        this.roomNumber = roomNumber;
    }

    public void setType(RoomType type)
    {
        this.type = type;
    }
}
