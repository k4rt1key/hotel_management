package src.Models;

public class Hotel
{
    private static int hotelCounter = 0; // TODO : Why static variable in POJO??

    private final int id;

    private String name;

    public Hotel(String name)
    {
        this.id = ++hotelCounter; // TODO : what about thread safety?

        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public int getId()
    {
        return id;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return "Hotel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
