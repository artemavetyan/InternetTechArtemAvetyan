package com.company.SERVER.messages;

import com.company.SERVER.ChatManager;
import com.company.SERVER.file_transfer.FileTransferServer;
import com.company.SERVER.model.Group;
import com.company.tools.Printer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import java.util.ArrayList;
import java.util.List;

/**
 * Interprets incoming messages for the server
 * Depending on a type and content of the message performs different actions
 */
public class ServerSideMessageInterpreter {

    private Socket socket;                          //server socket
    private PrintWriter writer;

    private static List<Socket> pongers = new ArrayList<>();    //client's who sent pong message

    public ServerSideMessageInterpreter(Socket socket) {
        try {
            this.writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.socket = socket;
    }

    /**
     * Checks whether a client has responded to a ping message
     *
     * @param socket client's socket
     */
    public static void checkPongForUser(Socket socket) {
        //if our list contains the client's socket that means that (s)he has responded to the ping
        if (pongers.contains(socket)) {
            pongers.remove(socket);         //remove client from the list
        } else {
            //if client is not in the list, that means that (s)he didn't reply to the ping message
            //therefore - disconnect that client
            PrintWriter writer = null;
            try {
                writer = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            writer.println("DSCN Pong timeout");
            pongers.remove(socket);
            ChatManager.deleteUser(socket);     //delete user from our users list
        }
    }

    /**
     * Interprets incoming messages
     * Performs actions depending on a type, sender and content of a message
     *
     * @param message message to interpret
     */
    public void interpret(ServerSideMessage message) {
        if (message.getType() == ServerSideMessage.Type.LOGIN) {        //if user sends it's login ->
            if (!ChatManager.isUniqueLogin(message.getContent())) {     // -> check whether it is unique
                writer.println("-ERR user already logged in");
                try {
                    socket.close();                                     //if it is not unique -> disconnect the user
                    ChatManager.deleteUser(socket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                if (!ChatManager.isValidLogin(message.getContent())) {  //check whether the login is valid
                    writer.println(
                            "-ERR username has an invalid format (only characters, numbers and underscores are allowed)");
                    try {
                        socket.close();                                 //if is not valid -> disconnect the user
                        ChatManager.deleteUser(socket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {                                                //if login is unique and valid -> save the user
                    ChatManager.addUser(message.getContent(), socket);
                    writer.println("+OK HELO " + message.getContent());
                }
            }
        } else if (message.getType() == ServerSideMessage.Type.QUIT) {
            //disconnect the user
            writer.println(message.getContent());
            try {
                socket.close();
                ChatManager.deleteUser(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (message.getType() == ServerSideMessage.Type.BROADCAST) {
            writer.println("+OK BCST " + message.getContent());
            sendBroadcastMessage(message.getContent());
        } else if (message.getType() == ServerSideMessage.Type.GET_USERS) {
            String response = ChatManager.getUsers().values().toString().replaceAll("null", "unknown");

            writer.println("+SUCCESS " + response);
        } else if (message.getType() == ServerSideMessage.Type.PM) {

            String destinationLogin = ChatManager.retrieveLoginFromPrivateMessage(message.getContent());
            Socket destinationSocket = ChatManager.findUserByLogin(destinationLogin);

            if (destinationSocket == null) {
                //no user found
                writer.println("-SOFT_ERR no such user!");
            } else {
                writer.println("+OK PM");
                PrintWriter destination = null;
                String sender = ChatManager.getUsers().get(socket).getLogin();
                try {
                    destination = new PrintWriter(destinationSocket.getOutputStream(), true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                destination.println(
                        "PM " + sender + " " + ChatManager.retrieveMessageFromPrivateMessage(message.getContent()));
                //send pm
            }
        } else if (message.getType() == ServerSideMessage.Type.REQUEST_PUBLIC_KEY) {
            String destinationLogin = ChatManager.retrieveLoginFromPrivateMessage(message.getContent());
            Socket destinationSocket = ChatManager.findUserByLogin(destinationLogin);

            if (destinationSocket == null) {
                //no user found
                writer.println("-SOFT_ERR no such user!");
            } else {
                PrintWriter destination = null;
                String sender = ChatManager.getUsers().get(socket).getLogin();
                try {
                    destination = new PrintWriter(destinationSocket.getOutputStream(), true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                destination.println(
                        "REQUEST_PUBLIC_KEY " + message.getContent().replaceAll(destinationLogin, sender));

            }
        } else if (message.getType() == ServerSideMessage.Type.PUBLIC_KEY) {
            String destinationLogin = ChatManager.retrieveLoginFromPrivateMessage(message.getContent());
            Socket destinationSocket = ChatManager.findUserByLogin(destinationLogin);

            PrintWriter destination = null;
            String sender = ChatManager.getUsers().get(socket).getLogin();
            try {
                destination = new PrintWriter(destinationSocket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            destination.println(message.getContent().replaceAll(destinationLogin, sender));


        } else if (message.getType() == ServerSideMessage.Type.CREATE_GROUP) {
            String groupName = message.getContent();
            if (ChatManager.findGroupByName(groupName) != null) {
                writer.println("-SOFT_ERR a group with that name already exists!");
            } else {
                String admin = ChatManager.getUsers().get(socket).getLogin();
                ChatManager.addGroup(admin, groupName);
                writer.println("+SUCCESS group \"" + groupName + "\" has been created!");
            }
        } else if (message.getType() == ServerSideMessage.Type.GET_GROUPS) {
            if (ChatManager.getGroups().isEmpty()) {
                writer.println("-SOFT_ERR there are no groups yet");
            } else {
                String response = ChatManager.getGroups().toString();
                writer.println("+SUCCESS " + response);
            }
        } else if (message.getType() == ServerSideMessage.Type.JOIN_GROUP) {
            Group group = ChatManager.findGroupByName(message.getContent());
            if (group == null) {
                writer.println("-SOFT_ERR no such group!");
            } else {
                String sender = ChatManager.getUsers().get(socket).getLogin();
                if (group.getMembers().contains(sender) || group.getAdmin().equals(sender)) {
                    writer.println("-SOFT_ERR you are already a member of that group!");
                } else if (group.getBanned().contains(sender)) {
                    writer.println("-SOFT_ERR you are banned from that group!");
                } else {
                    group.addMember(sender);
                    writer.println("+SUCCESS you have successfully entered group " + group.getGroupName());
                }
            }
        } else if (message.getType() == ServerSideMessage.Type.GROUP_MESSAGE) {
            String groupName = ChatManager.retrieveGroupNameFromMessage(message.getContent());
            Group group = ChatManager.findGroupByName(groupName);
            if (group == null) {
                writer.println("-SOFT_ERR no such group!");
            } else {
                String sender = ChatManager.getUsers().get(socket).getLogin();
                if (!group.getMembers().contains(sender) && !group.getAdmin().equals(sender)) {
                    writer.println("-SOFT_ERR you are not allowed to write in that group!");
                } else {
                    String groupMessage = ChatManager.retrieveContentFromGroupMessage(message.getContent());
                    sendGroupMessage(group, groupMessage);
                }
            }

        } else if (message.getType() == ServerSideMessage.Type.LEAVE_GROUP) {
            String groupName = message.getContent();
            Group group = ChatManager.findGroupByName(groupName);
            if (group == null) {
                writer.println("-SOFT_ERR no such group!");
            } else {
                String sender = ChatManager.getUsers().get(socket).getLogin();
                if (!group.getMembers().contains(sender) && !group.getAdmin().equals(sender)) {
                    writer.println("-SOFT_ERR you are not a member of that group!");
                } else if (group.getAdmin().equals(sender)) {
                    writer.println("-SOFT_ERR you are an admin, you can't leave the group!");
                } else {
                    group.removeMember(sender);
                    writer.println("+SUCCESS you have successfully left the group \"" + groupName + "\" !");
                }
            }
        } else if (message.getType() == ServerSideMessage.Type.KICK) {
            String groupName = ChatManager.retrieveGroupNameFromKickMessage(message.getContent());
            Group group = ChatManager.findGroupByName(groupName);
            if (group == null) {
                writer.println("-SOFT_ERR no such group!");
            } else {
                String sender = ChatManager.getUsers().get(socket).getLogin();
                if (!group.getAdmin().equals(sender)) {
                    writer.println("-SOFT_ERR you are not allowed to kick members out that group!");
                } else {
                    String memberToKick = message.getContent().replace(groupName, "").trim();
                    if (group.getAdmin().equals(memberToKick)) {
                        writer.println(
                                "-SOFT_ERR why would you kick yourself out of your group :)?");
                    } else if (!group.getMembers().contains(memberToKick)) {
                        writer.println(
                                "-SOFT_ERR user \"" + memberToKick + "\" is not a member of group \"" + groupName +
                                        "\" !");

                    } else {
                        group.kickMember(memberToKick);
                        sendGroupMessage(group,
                                "user \"" + memberToKick + "\" has been kicked out of group \"" + groupName +
                                        "\"");
                        writer.println(
                                "+SUCCESS user \"" + memberToKick + "\" has been kicked out of group \"" + groupName +
                                        "\"");
                        Socket kickedUser = ChatManager.findUserByLogin(memberToKick);
                        PrintWriter kickedUserWriter = null;
                        try {
                            kickedUserWriter = new PrintWriter(kickedUser.getOutputStream(), true);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        kickedUserWriter.println("+SUCCESS you have been kicked out of the group \"" + groupName +
                                "\""); //you have been kicked out of group
                    }
                }
            }
        } else if (message.getType() == ServerSideMessage.Type.PONG) {
            pongers.add(socket);
            if (ChatManager.getUsers().get(socket) != null) {

                Printer.printOkMessage(message.getContent() + " : " + ChatManager.getUsers().get(socket).getLogin());
            } else {
                Printer.printOkMessage(message.getContent() + " : " + socket.getPort());
            }
        } else if (message.getType() == ServerSideMessage.Type.ERROR) {
            String msg = "User ";
            if (ChatManager.getUsers().get(socket) != null) {

                msg += ChatManager.getUsers().get(socket).getLogin();

            } else if (ChatManager.findUserByFileSocket(socket) != null) {
                msg += ChatManager.findUserByFileSocket(socket);
            } else {
                msg += socket.getPort();
            }
            msg += " is unexpectedly shut down";

            Printer.printError(msg);
            try {
                socket.close();
                ChatManager.deleteUser(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (message.getType() == ServerSideMessage.Type.FILE_TRANS) {
            //check if receiver exists
            String receiver = ChatManager.retrieveLoginFromPrivateMessage(message.getContent());
            Socket receiverSocket = ChatManager.findUserByLogin(receiver);
            if (receiverSocket == null) {
                writer.println("-SOFT_ERR no such user!");
            } else if (receiverSocket == this.socket) {
                writer.println("-SOFT_ERR you can't send a file to yourself!");

            } else {
                PrintWriter destination = null;
                try {
                    destination = new PrintWriter(receiverSocket.getOutputStream(), true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String sender = ChatManager.getUsers().get(socket).getLogin();
                String messageToSend = "FILE_TRANS " + message.getContent().replaceAll(receiver, sender);
                destination.println(messageToSend);
            }

        } else if (message.getType() == ServerSideMessage.Type.READY_FOR_FILE) {

            FileTransferServer fileTransferServer = new FileTransferServer();
            int portNumber = fileTransferServer.getPortNumber();

            Thread fileServ = new Thread(fileTransferServer);
            fileServ.start();
            //send port number to clients

            String destination = ChatManager.retrieveLoginFromPrivateMessage(message.getContent());
            Socket destinationSocket = ChatManager.findUserByLogin(destination);
            PrintWriter dest = null;
            try {
                dest = new PrintWriter(destinationSocket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String sender = ChatManager.getUsers().get(socket).getLogin();
            String messageToSender =
                    "READY_FOR_FILE " + message.getContent().replaceAll(destination, sender) + " " + portNumber;
            dest.println(messageToSender);
            String messageToReceiver = "OPEN_PORT_TO_RECEIVE " + portNumber;
            writer.println(messageToReceiver);

        } else if (message.getType() == ServerSideMessage.Type.FILE) {

            String sender = ChatManager.getFileSenderLogin(message.getContent());
            ChatManager.getUsers().get(ChatManager.findUserByLogin(sender)).setFileTransferSocket(socket);


            String destinationLogin = ChatManager.retrieveLoginFromPrivateMessage(message.getContent());
            Socket destinationSocket =
                    ChatManager.getUsers().get(ChatManager.findUserByLogin(destinationLogin)).getFileTransferSocket();

            PrintWriter destWriter = null;
            try {
                destWriter = new PrintWriter(destinationSocket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }

            String messageToSend = message.getContent().replaceAll(destinationLogin, sender);
            destWriter.println(messageToSend);

        } else if (message.getType() == ServerSideMessage.Type.OPEN_PORT_TO_RECEIVE) {
            String name = message.getContent();
            ChatManager.getUsers().get(ChatManager.findUserByLogin(name)).setFileTransferSocket(socket);
        } else if (message.getType() == ServerSideMessage.Type.FILE_STATUS) {
            String destinationLogin = ChatManager.retrieveLoginFromPrivateMessage(message.getContent());
            Socket destinationSocket =
                    ChatManager.getUsers().get(ChatManager.findUserByLogin(destinationLogin)).getFileTransferSocket();

            String messageToSend = message.getContent().replaceAll("@\\S+", "");

            PrintWriter writer = null;

            try {
                writer = new PrintWriter(destinationSocket.getOutputStream(), true);

            } catch (IOException e) {

                e.printStackTrace();
            }

            writer.println(messageToSend);

            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                destinationSocket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Sends a broadcast message
     *
     * @param message message to be broadcasted
     */
    private void sendBroadcastMessage(String message) {
        //Get the sender
        String sender = ChatManager.getUsers().get(this.socket).getLogin();

        //Go through all connected clients
        for (Socket socket : ChatManager.getUsers().keySet()) {
            //Send message to everyone except from the sender
            if (socket != this.socket) {
                PrintWriter writer = null;
                try {
                    writer = new PrintWriter(socket.getOutputStream(), true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                writer.println("BCST [" + sender + "] " + message);
            }
        }
    }

    /**
     * Sends a group message
     *
     * @param group   group to broadcast the message in
     * @param message message to be sent
     */
    private void sendGroupMessage(Group group, String message) {
        //Get the sender
        String sender = ChatManager.getUsers().get(this.socket).getLogin();
        //Go through each group member
        for (String member : group.getMembers()) {
            //Send message to everyone except the sender
            if (!member.equals(sender)) {
                //Get group member's socket
                Socket destination = ChatManager.findUserByLogin(member);
                PrintWriter writer = null;
                try {
                    writer = new PrintWriter(destination.getOutputStream(), true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //Send the message
                writer.println("GROUP_MESSAGE " + group.getGroupName() + ": [" + sender + "] " + message);
            }
        }
        //Because admin is stored separately, but not among group members, we need to send him/her the message separately
        //Unless the sender is admin!
        if (!sender.equals(group.getAdmin())) {
            Socket destination = ChatManager.findUserByLogin(group.getAdmin());
            PrintWriter writer = null;
            try {
                writer = new PrintWriter(destination.getOutputStream(), true);
            } catch (IOException e) {

                e.printStackTrace();
            }
            writer.println("GROUP_MESSAGE " + group.getGroupName() + ": [" + sender + "] " + message);
        }
    }
}
