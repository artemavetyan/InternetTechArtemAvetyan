package com.company.SERVER.messages;

/**
 * Represents a message on the client's side
 */
public class ServerSideMessage {

    /**
     * Types of a message
     */
    enum Type {
        LOGIN,
        QUIT,
        BROADCAST,
        GET_USERS,
        PM,
        PONG,
        CREATE_GROUP,
        GET_GROUPS,
        JOIN_GROUP,
        GROUP_MESSAGE,
        LEAVE_GROUP,
        KICK,
        ERROR,
        FILE_TRANS,
        READY_FOR_FILE,
        OPEN_PORT_TO_RECEIVE,
        FILE,
        FILE_STATUS,
        REQUEST_PUBLIC_KEY,
        PUBLIC_KEY
    }

    private String content;                         //content of the message
    private Type type;                              //type of the message

    public ServerSideMessage(String content) {
        init(content);
    }

    /**
     * Special constructor for handling an exception
     *
     * @param se exception to handle
     */
    public ServerSideMessage(Exception se) {
        init(se);
    }

    /**
     * Initializes an eror message
     *
     * @param se socket to handle
     */
    private void init(Exception se) {
        this.type = Type.ERROR;
        this.content = "";
    }

    /**
     * Initializes a message
     * Translates a line into a message with a type and content
     *
     * @param line line to translate
     */
    private void init(String line) {
        if (line.startsWith("HELO")) {
            this.type = Type.LOGIN;
            this.content = line.replaceAll("HELO ", "");
        } else if (line.startsWith("BCST")) {
            this.type = Type.BROADCAST;
            this.content = line.replaceAll("BCST ", "");
        } else if (line.equals("QUIT")) {
            this.type = Type.QUIT;
            this.content = "+OK Goodbye";
        } else if (line.startsWith("GET_USERS")) {
            this.type = Type.GET_USERS;
            this.content = "";
        } else if (line.startsWith("PM")) {
            this.type = Type.PM;
            this.content = line.replace("PM ", "");
        } else if (line.startsWith("CREATE_GROUP")) {
            this.type = Type.CREATE_GROUP;
            this.content = line.replace("CREATE_GROUP ", "");
        } else if (line.startsWith("GET_GROUPS")) {
            this.type = Type.GET_GROUPS;
            this.content = "";
        } else if (line.startsWith("JOIN_GROUP")) {
            this.type = Type.JOIN_GROUP;
            this.content = line.replace("JOIN_GROUP ", "");
        } else if (line.startsWith("GROUP_MESSAGE")) {
            this.type = Type.GROUP_MESSAGE;
            this.content = line.replace("GROUP_MESSAGE ", "");
        } else if (line.startsWith("LEAVE_GROUP")) {
            this.type = Type.LEAVE_GROUP;
            this.content = line.replace("LEAVE_GROUP ", "");
        } else if (line.startsWith("KICK")) {
            this.type = Type.KICK;
            this.content = line.replace("KICK ", "");
        } else if (line.equals("PONG")) {
            this.type = Type.PONG;
            this.content = "PONG ";
        } else if (line.startsWith("FILE_TRANS")) {
            this.type = Type.FILE_TRANS;
            this.content = line.replace("FILE_TRANS ", "");
        } else if (line.startsWith("READY_FOR_FILE")) {
            this.type = Type.READY_FOR_FILE;
            this.content = line.replace("READY_FOR_FILE ", "");
        } else if (line.startsWith("FILE <")) {
            this.type = Type.FILE;
            this.content = line;
        } else if (line.startsWith("OPEN_PORT_TO_RECEIVE")) {
            this.type = Type.OPEN_PORT_TO_RECEIVE;
            this.content = line.replaceAll("OPEN_PORT_TO_RECEIVE ", "");
        } else if (line.startsWith("FILE_STATUS")) {
            this.type = Type.FILE_STATUS;
            this.content = line;
        } else if (line.startsWith("REQUEST_PUBLIC")) {
            this.type = Type.REQUEST_PUBLIC_KEY;
            this.content = line.replaceAll("REQUEST_PUBLIC_KEY ", "");
        } else if (line.startsWith("PUBLIC_KEY")) {
            this.type = Type.PUBLIC_KEY;
            this.content = line;
        }
    }

    public String getContent() {
        return content;
    }

    public Type getType() {
        return type;
    }
}
