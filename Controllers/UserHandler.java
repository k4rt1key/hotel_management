package src.Controllers;

import src.Models.User;
import src.Server.Database;

import java.time.LocalDateTime;

public class UserHandler
{

    // ==== CRUD ====

    // == CREATE ==
    public static String handleCreateUser(String username, String password)
    {
        var existingUser = findUser(username);

        if (existingUser != null)
        {
            return "409 ❌ User already exists";
        }

        var newUser = new User(username, password, false);

        Database.users.put(newUser.getUsername(), newUser);

        return "200 ✅ User created successfully: " + username + " (ID: " + newUser.getId() + ")";
    }

    // == READ ==
    public static String handleLogin(String username, String password)
    {
        User user = Database.users.get(username);

        if (user == null)
        {
            return "404 ❌ User not found";
        }

        if (user.validatePassword(password))
        {
            return "403 ❌ Invalid password";
        }

        return "200 ✅ Login successful. User: " + username + " (ID: " + user.getId() + "), Admin: " + (user.isAdmin() ? "Yes" : "No");
    }

    public static String listUsers()
    {
        var users = Database.users;

        if (users.isEmpty())
        {
            return "404 👤 No users found";
        }

        var response = new StringBuilder("200 👤 Users:\n");

        for (var user : users.values())
        {
            response.append("  User ID: ").append(user.getId())
                    .append(" - Username: ").append(user.getUsername())
                    .append(" - Admin: ").append(user.isAdmin() ? "Yes" : "No")
                    .append("\n");
        }

        String res = response.toString();

        response.setLength(0);

        return res;
    }

    // == DELETE ==
    public static String removeUser(String username)
    {
        try
        {
            if (username.equals("admin"))
            {
                return "403 ❌ Cannot remove admin user";
            }

            var targetUser = findUser(username);

            if (targetUser == null)
            {
                return "404 ❌ User not found";
            }

            // Check for future bookings
            var hasFutureBookings = Database.bookings.stream()
                    .anyMatch(booking -> booking.getUserId() == targetUser.getId()
                            && booking.getCheckOutTime().isAfter(LocalDateTime.now()));

            if (hasFutureBookings)
            {
                return "400 ❌ Cannot remove user with future bookings";
            }

            Database.users.remove(targetUser.getUsername());

            return "200 ✅ User removed successfully";
        }
        catch (Exception e)
        {
            return "500 ❌ Error removing user: " + e.getMessage();
        }
    }

    // ==== HELPER METHODS ====
    public static User findUser(String username)
    {
        return Database.users.get(username);
    }


}
