package com.company.SERVER;

import com.company.SERVER.model.Group;
import com.company.SERVER.model.User;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manages users and groups
 * Contains some helper functions
 */
public class ChatManager {

    private static List<Group> groups;
    private static Map<Socket, User> users;

    public ChatManager() {
        groups = new ArrayList<>();
        users = new HashMap<>();
    }

    public static void addUser(String login, Socket socket) {
        users.put(socket, new User(login));
    }

    public static void addUser(Socket socket) {
        users.put(socket, null);
    }

    public static void addGroup(String admin, String groupName) {
        groups.add(new Group(admin, groupName));
    }

    public static List<Group> getGroups() {
        return groups;
    }

    public static Map<Socket, User> getUsers() {
        return users;
    }

    /**
     * Checks if the login is unique
     *
     * @param login login to be checked
     * @return whether a given login is unique
     */
    public static boolean isUniqueLogin(String login) {
        for (User user : users.values()) {
            if (user != null && user.getLogin().equals(login)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if login is valid
     *
     * @param login to check
     * @return whether a given login is valid
     */
    public static boolean isValidLogin(String login) {
        String regex = "[a-zA-Z0-9_]+";
        return login.matches(regex);
    }

    public static void deleteUser(Socket socket) {
        users.remove(socket);
    }

    /**
     * Finds user by login
     *
     * @param login login of the user to find
     * @return socket of the found user
     */
    public static Socket findUserByLogin(String login) {
        for (Socket socket : users.keySet()) {
            if (users.get(socket) != null && users.get(socket).getLogin().equals(login)) {
                return socket;
            }
        }
        return null;
    }

    /**
     * Retrieves a content of message
     *
     * @param message message to retrieve content from
     * @return content of the message
     */
    public static String retrieveMessageFromPrivateMessage(String message) {
        return message.replaceAll("^@[a-zA-Z0-9_]+\\s+", "");
    }

    /**
     * Retrieves login from a message
     *
     * @param message message to find a lofin from
     * @return login
     */
    public static String retrieveLoginFromPrivateMessage(String message) {

        Pattern p = Pattern.compile("@\\S+");   // the pattern to search for
        Matcher m = p.matcher(message);
        m.find();
        return m.group().replace("@", "");

    }

    /**
     * Retrieves file sender from a message
     *
     * @param message message to find the sender in
     * @return sender of the file
     */
    public static String getFileSenderLogin(String message) {
        Pattern p = Pattern.compile("<\\S+");   // the pattern to search for
        Matcher m = p.matcher(message);
        m.find();
        return m.group().replace("<", "");
    }

    /**
     * Retrieves content from a group message
     *
     * @param message message to get the content from
     * @return content of the message
     */
    public static String retrieveContentFromGroupMessage(String message) {
        return message.replaceAll("^@@\\S+\\s", "");
    }

    /**
     * Retrieves group name from a message
     *
     * @param message message to get a group name from
     * @return group name
     */
    public static String retrieveGroupNameFromMessage(String message) {
        int start = message.lastIndexOf('@');
        int end = message.indexOf(' ');
        return message.substring(start + 1, end);
    }

    /**
     * Finds a group by its name
     *
     * @param groupName name of the group to be found
     * @return group
     */
    public static Group findGroupByName(String groupName) {
        for (Group group : groups) {
            if (group.getGroupName().equals(groupName)) {
                return group;
            }
        }
        return null;
    }

    /**
     * Retrieves a group name kick out message
     *
     * @param message message to get a group name from
     * @return group name
     */
    public static String retrieveGroupNameFromKickMessage(String message) {
        int end = message.indexOf(" ");
        return message.substring(0, end);
    }

    /**
     * Finds user by its file transfer socket
     *
     * @param socket file transfer socket of the user to be found
     * @return user with that file transfer socket
     */
    public static String findUserByFileSocket(Socket socket) {
        for (User u : users.values()) {
            if (u.getFileTransferSocket() == socket) {
                return u.getLogin();
            }
        }
        return null;
    }
}
