package com.company.CLIENT.messages;

import java.net.SocketException;

/**
 * Represents a message on the client side
 */
public class ClientSideMessage {

    /**
     * Possible types of client side message
     */
    enum Type {
        PING,
        DSCN,
        LOGIN,
        QUIT,
        CRITICAL_ERROR,
        SOFT_ERROR,
        BROADCAST_OR_LOGIN,      //can be either broadcast message or message containing user's login DEPENDING
                                // on the state of the user
        YOUR_BROADCAST,
        OTHERS_BROADCAST,
        HELO,
        GET_USERS,
        CREATE_GROUP,
        GET_GROUPS,
        JOIN_GROUP,
        GROUP_MESSAGE,
        LEAVE_GROUP,
        SUCCESS,
        KICK,
        PM,
        FILE_TRANS,
        READY_FOR_FILE,
        OPEN_PORT_TO_RECEIVE,
        FILE,
        FILE_STATUS,
        REQUEST_PUBLIC_KEY,
        PUBLIC_KEY,
        SHOW_MENU
    }

    /**
     * Types of message's sender
     */
    public enum Sender {
        CLIENT,
        SERVER
    }

    private Sender sender;      //sender of the message
    private Type type;          //type of the message
    private String content;     // content of the message

    public ClientSideMessage(String line, Sender sender) {
        this.sender = sender;
        init(line);

    }

    /**
     * Special constructor in case of an exception
     *
     * @param se     socket exception
     * @param sender sender of the message
     */
    public ClientSideMessage(SocketException se, Sender sender) {
        this.sender = sender;
        init(se);
    }

    /**
     * initializes a critical error message
     *
     * @param se socket exception
     */
    private void init(SocketException se) {
        this.type = Type.CRITICAL_ERROR;
        this.content = "The server is down!";
    }

    /**
     * Initializes a message.
     * Translates a line to a message with type
     *
     * @param line line to translate to a message
     */
    private void init(String line) {
        //Messages are divided by sender -> server or client
        if (this.sender == Sender.SERVER) {

            if (line.equals("PING")) {
                this.type = Type.PING;
                this.content = "PONG";
            } else if (line.startsWith("DSCN")) {
                this.type = Type.DSCN;
                this.content = "You've been disconnected!";
            } else if (line.startsWith("-ERR")) {
                this.type = Type.CRITICAL_ERROR;
                this.content = line.replace("-ERR ", "").trim();
            } else if (line.startsWith("HELO Welkom")) {
                this.type = Type.LOGIN;
                this.content = "HI! You've been connected!\nPlease enter your login:";
            } else if (line.startsWith("+OK HELO")) {
                this.type = Type.HELO;
                this.content = "Welcome! You can now write your messages!";
            } else if (line.startsWith("+OK BCST")) {
                this.type = Type.YOUR_BROADCAST;
                this.content = line.replace("+OK BCST", "".trim());
            } else if (line.startsWith("BCST [")) {
                this.type = Type.OTHERS_BROADCAST;
                this.content = line.replace("BCST ", "").trim();
            } else if (line.equals("+OK Goodbye")) {
                this.type = Type.QUIT;
                this.content = "Goodbye!";
            } else if (line.startsWith("PM ")) {
                this.type = Type.PM;
                this.content = line.replace("PM ", "");
            } else if (line.startsWith("-SOFT_ERR")) {
                this.type = Type.SOFT_ERROR;
                this.content = line.replace("-SOFT_ERR ", "");
            } else if (line.startsWith("GROUP_MESSAGE ")) {
                this.type = Type.GROUP_MESSAGE;
                this.content = line.replace("GROUP_MESSAGE ", "");
            } else if (line.startsWith("+SUCCESS")) {
                this.type = Type.SUCCESS;
                this.content = line.replace("+SUCCESS ", "");
            } else if (line.startsWith("FILE_TRANS")) {
                this.type = Type.FILE_TRANS;
                this.content = line.replace("FILE_TRANS ", "");
            } else if (line.startsWith("READY_FOR_FILE")) {
                this.type = Type.READY_FOR_FILE;
                this.content = line.replace("READY_FOR_FILE ", "");
            } else if (line.startsWith("FILE ")) {
                this.type = Type.FILE;
                this.content = line.replaceAll("FILE ", "");
            } else if (line.startsWith("OPEN_PORT_TO_RECEIVE")) {
                this.type = Type.OPEN_PORT_TO_RECEIVE;
                this.content = line.replaceAll("OPEN_PORT_TO_RECEIVE ", "");
            } else if (line.startsWith("FILE_STATUS")) {
                this.type = Type.FILE_STATUS;
                this.content = line.replaceAll("FILE_STATUS ", "");
            } else if (line.startsWith("REQUEST_PUBLIC")) {
                this.type = Type.REQUEST_PUBLIC_KEY;
                this.content = line.replaceAll("REQUEST_PUBLIC_KEY ", "");
            } else if (line.startsWith("PUBLIC_KEY")) {
                this.type = Type.PUBLIC_KEY;
                this.content = line.replaceAll("PUBLIC_KEY ", "");
            }
        } else {
            if (line.equals("q".trim())) {
                this.type = Type.QUIT;
                this.content = "QUIT";

            } else if (line.equals("*all".trim())) {
                this.type = Type.GET_USERS;
                this.content = "GET_USERS " + line;

            } else if (line.matches("\\s*@[a-zA-Z0-9_]+\\s.+")) {
                this.type = Type.PM;
                this.content = "PM " + line;

            } else if (line.matches("/create_group\\s\\S+")) {
                this.type = Type.CREATE_GROUP;
                this.content = "CREATE_GROUP " + line.replace("/create_group ", "");

            } else if (line.equals("*groups".trim())) {
                this.type = Type.GET_GROUPS;
                this.content = "GET_GROUPS " + line;
            } else if (line.matches("/join\\s\\S+")) {
                this.type = Type.JOIN_GROUP;
                this.content = "JOIN_GROUP " + line.replace("/join ", "");
            } else if (line.startsWith("@@")) {
                this.type = Type.GROUP_MESSAGE;
                this.content = "GROUP_MESSAGE " + line;
            } else if (line.matches("/leave\\s\\S+")) {
                this.type = Type.LEAVE_GROUP;
                this.content = "LEAVE_GROUP " + line.replace("/leave ", "");
            } else if (line.matches("/kick\\s\\S+\\s\\S+")) {
                this.type = Type.KICK;
                this.content = "KICK " + line.replace("/kick ", "");
            } else if (line.matches("/send_file\\s@\\S+\\s\\S+")) {
                this.type = Type.FILE_TRANS;
                this.content = "FILE_TRANS " + line.replace("/send_file ", "");
            } else if (line.startsWith("/show_menu")) {
                this.type = Type.SHOW_MENU;
                this.content = "";
            } else {
                this.type = Type.BROADCAST_OR_LOGIN;
                this.content = line;
            }
        }
    }

    public Type getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public Sender getSender() {
        return sender;
    }
}
