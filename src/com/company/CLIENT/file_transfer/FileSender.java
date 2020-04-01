package com.company.CLIENT.file_transfer;

import com.company.CLIENT.ClientManager;
import com.company.CLIENT.listeners.ServerListener;
import com.company.CLIENT.messages.ClientSideMessageInterpreter;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Thread that is responsible for sending a file
 */
public class FileSender implements Runnable {

    private int portNumber;                 //new port for sending files
    private String transferInfo;            //contains sender, receiver and meta data of the file
    private ClientManager clientManager;  //message manager of the sender

    public FileSender(int portNumber, String transferInfo, ClientManager clientManager) {
        this.portNumber = portNumber;
        this.transferInfo = transferInfo;
        this.clientManager = clientManager;
    }

    @Override
    public void run() {
        // Set up a connection with the server.
        // If you are connecting locally use SERVER_ADDRESS = “127.0.0.1”.
        Socket socket = null;
        try {
            socket = new Socket("127.0.0.1", portNumber);   //connecting on the new socket
        } catch (IOException e) {
            e.printStackTrace();
        }

        PrintWriter writer = null;
        try {
            writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String metaData = getMetaData(transferInfo);        //retrieve meta data from the transfer info
        byte[] fileBytes =
                clientManager.getFileToSend(metaData);  //get bytes from the manager using meta data as a key

        String file = Base64.getEncoder().encodeToString(fileBytes);    //encode the bytes to string

        String messageToSend =
                "FILE " + transferInfo + " " + file;     //send protocol, transfer info and the file to the receiver

        writer.println(messageToSend);

        ClientSideMessageInterpreter interpreter =
                new ClientSideMessageInterpreter(socket);    //new interpreter for the sender

        interpreter.setClientManager(clientManager);      //set the message manager of sender to his/her
                                                            // file transfer thread

        ServerListener serverListener = new ServerListener(socket, interpreter);

        Thread sl = new Thread(serverListener);

        sl.start();
    }

    /**
     * Retrieves meta data from the transfer info message
     * @param transferInfo transfer information
     * @return meta data of the file
     */
    private String getMetaData(String transferInfo) {
        Pattern p = Pattern.compile("\\d+\\s\\S+\\.\\S+\\s-?\\d+");   // the pattern to search for
        Matcher m = p.matcher(transferInfo);
        m.find();

        return m.group();
    }
}
