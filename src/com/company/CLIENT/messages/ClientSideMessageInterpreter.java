package com.company.CLIENT.messages;

import com.company.CLIENT.ClientManager;
import com.company.CLIENT.enryption.AESEncryption;
import com.company.CLIENT.enryption.RSAEncryption;
import com.company.CLIENT.file_transfer.FileReceiver;
import com.company.CLIENT.file_transfer.FileSender;
import com.company.CLIENT.file_transfer.MetaData;
import com.company.tools.Printer;

import javax.crypto.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.*;
import java.util.Base64;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interprets incoming messages for the client
 * Depending on a type and content of the message performs different actions
 */
public class ClientSideMessageInterpreter {

    private KeyPair kp;                 //Private-public key pair
    private PrintWriter writer;         //writer to the server
    private Socket socket;              //socket of the server
    private String myName;              //name of the client

    /**
     * Types of state of the client
     */
    private enum State {
        CREATED,
        CONNECTED,
        LOGGED_IN,
        DISCONNECTED
    }

    private State state;                    //client's state
    private Scanner scanner;                //client's scanner

    private ClientManager clientManager;  //client's message manager

    public ClientSideMessageInterpreter(Socket socket, Scanner scanner, KeyPair kp) {
        this(socket);

        this.state = State.CREATED;
        this.scanner = scanner;
        this.myName = "";
        this.kp = kp;
        this.clientManager = new ClientManager();
    }

    /**
     * Special constructor for file transfer
     *
     * @param socket socket for file transfer
     */
    public ClientSideMessageInterpreter(Socket socket) {
        try {
            this.writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.socket = socket;
        this.state = State.LOGGED_IN;
    }

    public void setClientManager(ClientManager clientManager) {
        this.clientManager = clientManager;
    }

    public void setMyName(String myName) {
        this.myName = myName;
    }

    /**
     * Interprets incoming message
     * Performs actions depending on a type, sender and content of a message
     *
     * @param message message to interpret
     */
    public void interpret(ClientSideMessage message) {
        if (message.getSender() == ClientSideMessage.Sender.SERVER) {       // message has been sent from the server
            if (message.getType() == ClientSideMessage.Type.PING) {         // if is ping message ->
                writer.println(message.getContent());                       // -> send pong message back
            } else if (message.getType() == ClientSideMessage.Type.DSCN) {  // if is disconnect message ->
                Printer.printError(message.getContent());
                try {
                    this.socket.close();                                    // close the socket
                    this.state = State.DISCONNECTED;                        // set state to disconnected
                    this.scanner.close();                                   // close the scanner
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (message.getType() == ClientSideMessage.Type.LOGIN) { // if is login message ->
                Printer.printOkMessage(message.getContent());               // -> print it
                this.state = State.CONNECTED;                               // set state to connected
            } else if (message.getType() == ClientSideMessage.Type.CRITICAL_ERROR) {
                Printer.printError(message.getContent());
                try {
                    this.socket.close();
                    this.state = State.DISCONNECTED;
                    this.scanner.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (message.getType() == ClientSideMessage.Type.OTHERS_BROADCAST) {
                Printer.printOthersMessage(message.getContent());
            } else if (message.getType() == ClientSideMessage.Type.HELO) {
                Printer.printOkMessage(message.getContent());
                this.state = State.LOGGED_IN;
                printMenu();
            } else if (message.getType() == ClientSideMessage.Type.QUIT) {
                Printer.printOkMessage(message.getContent());
                this.state = State.DISCONNECTED;
                this.scanner.close();
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (message.getType() == ClientSideMessage.Type.PM) {        // if is private message ->
                String[] encoded = message.getContent().split(" ");      // -> split message into:
                String sender = encoded[0];                                     // 1) sender
                String encodedPM = encoded[1];                                  // 2) encoded private message

                String sessionKeyOfSender =
                        this.clientManager.getSessionKeyOfUser(sender);        // get a session key of sender

                if (sessionKeyOfSender == null) {                               // if session key is not found ->

                    String encodedSessionKey = encoded[2];                      // -> get a new session key from message

                    try {
                        sessionKeyOfSender = RSAEncryption.decryptText(encodedSessionKey,
                                kp.getPrivate()); // decrypt the session key using your private key
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (IllegalBlockSizeException e) {
                        e.printStackTrace();
                    } catch (BadPaddingException e) {
                        e.printStackTrace();
                    } catch (NoSuchPaddingException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    this.clientManager.putSessionKeyOfUser(sender, sessionKeyOfSender);         // save the session key
                }
                String decodedMessage = AESEncryption.decrypt(encodedPM, sessionKeyOfSender);   //decode the message


                Printer.printOthersMessage("[" + sender + "] " + decodedMessage);               //print the message
            } else if (message.getType() == ClientSideMessage.Type.SOFT_ERROR) {
                Printer.printError(message.getContent());
            } else if (message.getType() == ClientSideMessage.Type.GROUP_MESSAGE) {
                Printer.printGroupMessage(message.getContent());
            } else if (message.getType() == ClientSideMessage.Type.SUCCESS) {
                Printer.printOkMessage(message.getContent());
            } else if (message.getType() == ClientSideMessage.Type.FILE_TRANS) {               // if is a file transfer

                String sender = retrieveSenderFromMessage(message.getContent());              // get sender of the file

                String metaData = message.getContent().replaceAll("@\\S+\\s", "");  //get metadata

                this.clientManager.addMetaData(metaData);                                  // save the metadata

                if (!Files.exists(Paths.get(myName))) {                    //create folder with your name
                    try {
                        Files.createDirectory(Paths.get(myName));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Printer.printOkMessage("User " + sender + " sends you a file!");
                String messageToSend = "READY_FOR_FILE " + message.getContent();
                writer.println(messageToSend);                       //tell server that you are ready to receive a file
            } else if (message.getType() == ClientSideMessage.Type.READY_FOR_FILE) {    //if receiver is ready for file

                int portNumber = getPortNumber(message.getContent());                  // get a new port number

                String senderAndMetaData = message.getContent().replaceAll(" " + portNumber, "");

                FileSender fileSender =
                        new FileSender(portNumber, "<" + myName + " " + senderAndMetaData, clientManager);
                Thread fileSend = new Thread(fileSender);
                fileSend.start();                                   //start  new thread to transfer the file

            } else if (message.getType() == ClientSideMessage.Type.FILE) {      //if is a file

                String filePath = getFilePath(message.getContent());            //get path of the file
                MetaData md = new MetaData(filePath);                           //initialize meta data for received file
                //get encoded file
                String encodedFile =
                        message.getContent().replaceAll("<\\S+\\s@\\S+\\s\\d+\\s\\S+\\.\\S+\\s-?\\d+\\s", "");

                filePath = checkIfFileNameIsUnique(filePath); //translate path to a file name and check if it is unique

                byte[] file = Base64.getDecoder().decode(encodedFile);  //decode the file

                md.setBytes(file);
                String metaData = md.getMetaData();                     //get metadata of the file

                String sender = retrieveSenderFromMessage(message.getContent());        //get sender of the file

                //if meta data of the received file matches previously saved meta data
                if (this.clientManager.getReceivedMetaData().contains(metaData)) {
                    try {
                        //save the file
                        Files.write(Paths.get(myName + "\\" + filePath), file,
                                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String messageToSend = "FILE_STATUS file has successfully been delivered!" + "@" + sender;
                    Printer.printOkMessage("File " + filePath + " has successfully been saved!");
                    writer.println(messageToSend);          //tell server that transaction went ok

                }
                //if meta data of the received file doesn't match previously saved meta data
                else {
                    String messageToSend = "FILE_STATUS File has been corrupted and not delivered!" + "@" + sender;
                    Printer.printOkMessage("File " + filePath + " has been corrupted and not saved!");
                    writer.println(messageToSend);                  //tell server that transaction failed
                }

                this.clientManager.deleteMetadata(metaData);       //delete that meta data from your list

                try {
                    socket.close();                                 //close file-receiving socket
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Thread.currentThread().interrupt();                 //stop file receiving thread
            } else if (message.getType() ==
                    ClientSideMessage.Type.OPEN_PORT_TO_RECEIVE) {   //if server tells to open the file receiving port
                int portNumber = Integer.parseInt(message.getContent());            //get the file receiving port
                FileReceiver fr = new FileReceiver(portNumber, myName, clientManager);
                Thread fileReceiver = new Thread(fr);
                fileReceiver.start();                                           // start the file receiving thread

            } else if (message.getType() == ClientSideMessage.Type.FILE_STATUS) {  //if is file status message
                Printer.printOkMessage(message.getContent());
                try {
                    socket.close();                                             //close the file sending socket
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Thread.currentThread().interrupt();                             //stop the file sending thread
            } else if (message.getType() ==
                    ClientSideMessage.Type.REQUEST_PUBLIC_KEY) {    //if server requests your public key

                PublicKey publicKey = kp.getPublic();               //get your public key
                byte[] pk = publicKey.getEncoded();
                String encodedPublicKey = Base64.getEncoder().encodeToString(pk);    //encode your public key
                String messageToSend = "PUBLIC_KEY " +
                        message.getContent().replaceAll("\\s", " " + encodedPublicKey + " ");
                String sender = retrieveSenderFromMessage(message.getContent());
                if (clientManager.getSessionKeys().containsKey(sender)) {   //in case there is an old session key stored
                    clientManager.removeSessionKey(sender);                 //remove it
                }
                writer.println(messageToSend);                  //send your public key to the server

            } else if (message.getType() == ClientSideMessage.Type.PUBLIC_KEY) {    //if is someone's public key
                String timeStamp = getTimeStampFromMessage(message.getContent());      //get timestamp from the message
                String pm = clientManager.getMessageToSend(timeStamp);            // get the message by the time stamp
                String publicKeyString =
                        getPublicKeyFromMessageAsString(message.getContent()); //get public key from message as string

                PublicKey pk = RSAEncryption.getPublicKeyFromString(publicKeyString); //translate public key from string

                String destination = retrieveSenderFromMessage(message.getContent()); //get sender from the message

                String sessionKey = this.clientManager.generateSessionKeyWithUser(destination);//generate a session key

                //Encrypt pm with session key
                String encryptedPM = AESEncryption.encrypt(pm, sessionKey);
                //Encrypt session key using public key of destination
                String encryptedSessionKey = "";
                try {
                    encryptedSessionKey = RSAEncryption.encryptText(sessionKey, pk);
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                }

                String messageToSend = "PM @" + destination + " " + encryptedPM + " " + encryptedSessionKey;

                writer.println(messageToSend);  //send an encrypted message and the new session key
            }

        } else {                                              //if the message is from client
            if (state == State.CONNECTED) {                  // if is connected
                if (message.getType() == ClientSideMessage.Type.BROADCAST_OR_LOGIN) { // this is a login message
                    this.myName = message.getContent();
                    writer.println("HELO " + message.getContent());                 // send your login to the server
                } else if (message.getType() == ClientSideMessage.Type.QUIT) {
                    writer.println(message.getContent());                           //tell server that you are quiting
                } else {
                    Printer.printError("You are not authorised to write this message!");
                }
            } else if (this.state == State.LOGGED_IN) {                                 //if you are logged in
                if (message.getType() == ClientSideMessage.Type.BROADCAST_OR_LOGIN) {   // this is a broadcast message
                    writer.println("BCST " + message.getContent());
                } else if (message.getType() == ClientSideMessage.Type.FILE_TRANS) {    //if is a file transfer message

                    String path = retrievePathFromMessage(message.getContent());   //get path of file from the message

                    MetaData md = new MetaData(path);                           //initialize new metadata for the file

                    byte[] bytes = null;
                    try {
                        bytes = Files.readAllBytes(Paths.get(path));            //read the file using path

                        md.setBytes(bytes);

                        String metaData = md.getMetaData();                     // get meta data of the file

                        clientManager.putFileToSend(metaData, bytes);        // save the file so later you can send it

                        writer.println(message.getContent().replace(path, metaData));   //send meta data to the server

                    } catch (IOException e) {
                        Printer.printError("No such file!");
                    }

                } else if (message.getType() == ClientSideMessage.Type.PM) {            //if is a private message
                    String destination = retrieveSenderFromMessage(message.getContent()); //get destination from message

                    //Get session key of the destination
                    String sessionKeyOfDestination = this.clientManager.getSessionKeyOfUser(destination);
                    //get the private message content
                    String pm = message.getContent().replaceAll("\\s*PM\\s@[a-zA-Z0-9_]+\\s", "");
                    if (sessionKeyOfDestination == null) {                  //if session key is not found

                        //generate a timestamp to save the private message content
                        String timeStamp = String.valueOf(System.currentTimeMillis());
                        //Save the pm using timestamp
                        this.clientManager.putMessageToSend(timeStamp, pm);
                        //Request a public key of the destination from the server
                        writer.println("REQUEST_PUBLIC_KEY @" + destination + " " + timeStamp);
                    } else {                                                //if session key is found
                        //Encrypt the message using your session key
                        String encryptedPM = AESEncryption.encrypt(pm, sessionKeyOfDestination);
                        String messageToSend = "PM @" + destination + " " + encryptedPM;
                        writer.println(messageToSend);                      // send the message
                    }

                } else if (message.getType() == ClientSideMessage.Type.SHOW_MENU) {
                    printMenu();
                } else {

                    writer.println(message.getContent());
                }
            } else {
                Printer.printError("You are not authorised to write this message!");
            }
        }
    }

    /**
     * Retrieves public key from a message as a string using regex
     *
     * @param content content of the message
     * @return public key as string
     */
    private String getPublicKeyFromMessageAsString(String content) {
        String filterOne = content.replaceAll("@\\S+\\s", "");
        int end = filterOne.indexOf(" ");
        return filterOne.substring(0, end);
    }

    /**
     * Retrieves a time stamp from the message
     *
     * @param content content of the message
     * @return time stamp as a string
     */
    private String getTimeStampFromMessage(String content) {
        int start = content.lastIndexOf(" ");
        return content.substring(start + 1);
    }

    /**
     * Retrieve port number from the message
     *
     * @param message message to look a port number in
     * @return port number
     */
    private int getPortNumber(String message) {
        int start = message.lastIndexOf(" ");
        return Integer.parseInt(message.substring(start + 1));
    }

    /**
     * Gets file name from the path and checks if it is unique
     *
     * @param path path of the file
     * @return file name
     */
    private String checkIfFileNameIsUnique(String path) {
        path = retrieveFileNameFromPath(path);
        int indexOfExtention = path.indexOf(".");
        String nameWithoutExtention = path.substring(0, indexOfExtention);
        String extention = path.substring(indexOfExtention);
        if (Files.exists(Paths.get(myName + "\\" + path))) {
            int i = 1;
            String result = path;
            while (Files.exists(Paths.get(myName + "\\" + result))) {
                result = nameWithoutExtention + "(" + i + ")" + extention;
            }
            return result;
        } else {
            return path;
        }
    }

    /**
     * Retruves the sender from a message
     *
     * @param message message to find the sender in
     * @return sender's name
     */
    private String retrieveSenderFromMessage(String message) {
        Pattern p = Pattern.compile("@\\S+");   // the pattern to search for
        Matcher m = p.matcher(message);
        m.find();
        return m.group().replace("@", "");
    }

    /**
     * Retrieves path of a file from the message
     *
     * @param message message to find file path in
     * @return file path as string
     */
    private String retrievePathFromMessage(String message) {
        String temp = message.replace("FILE_TRANS ", "");
        int startIndex = temp.lastIndexOf(" ");
        return temp.substring(startIndex + 1);
    }

    /**
     * Retrieves path of the file using regex
     *
     * @param message message to find file path in
     * @return file path
     */
    private String getFilePath(String message) {
        Pattern p = Pattern.compile("\\S+\\.\\S+");   // the pattern to search for
        Matcher m = p.matcher(message);
        m.find();

        return m.group();
    }

    /**
     * Retrieves file name from the path
     *
     * @param path path to retrieve a file name from
     * @return file name
     */
    private String retrieveFileNameFromPath(String path) {
        if (path.contains("\\")) {
            int start = path.lastIndexOf('\\') + 1;
            return path.substring(start);
        } else {

            return path;
        }
    }

    /**
     * Prints the menu
     */
    private void printMenu() {
        System.out.println();
        System.out.println("***** MENU *****");
        System.out.println("Bellow you can see your options fo this chat!");
        System.out.println("1) To type a broadcast message - just type your message");
        System.out.println("2) To type a private message - @username your message");
        System.out.println("3) To get all users - *all");
        System.out.println("4) To create a group - /create_group myAwesomeGroupname");
        System.out.println("5) To join a group - +join groupname");
        System.out.println("6) To send a group message - @@groupname message");
        System.out.println("7) To leave a group - /leave groupname");
        System.out.println("8) To kick user from a group - /kick groupname username");
        System.out.println("9) To send a file - /send_file @username filepath");
        System.out.println("10) To show menu - /show_menu");
        System.out.println("GOOD LUCK!");
    }
}
