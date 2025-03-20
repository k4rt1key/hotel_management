package src.Model;

public class User
{
    private static int userCounter = 0;

    private final int id;

    private final String username;

    private String password;

    private boolean isAdmin;

    public User(String username, String password, boolean isAdmin)
    {
        this.id = ++userCounter;

        this.username = username;

        this.password = password;

        this.isAdmin = isAdmin;
    }

    // Constructor for backward compatibility
    public User(int id, String username)
    {
        this.id = id;

        this.username = username;

        this.password = "default";

        this.isAdmin = false;

        // Update counter if this ID is larger
        if (id > userCounter)
        {
            userCounter = id;
        }
    }

    // Getters
    public int getId()
    {
        return id;
    }

    public String getUsername()
    {
        return username;
    }

    public boolean isAdmin()
    {
        return isAdmin;
    }

    public boolean validatePassword(String inputPassword)
    {
        return password.equals(inputPassword);
    }

    public void setPassword(String newPassword)
    {
        this.password = newPassword;
    }

}
