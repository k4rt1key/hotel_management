package src.Models;

public class User
{
    private static int userCounter = 0;

    private final int id;

    private final String username;

    private String password;

    private final boolean isAdmin;

    public User(String username, String password, boolean isAdmin)
    {
        this.id = ++userCounter;

        this.username = username;

        this.password = password;

        this.isAdmin = isAdmin;
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
        return !password.equals(inputPassword);
    }

    public void setPassword(String newPassword)
    {
        this.password = newPassword;
    }

}
