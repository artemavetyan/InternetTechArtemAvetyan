package com.company.CLIENT.file_transfer;

import com.company.CLIENT.ClientManager;
import com.company.CLIENT.listeners.ServerListener;
import com.company.CLIENT.messages.ClientSideMessageInterpreter;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Thread that is responsible for receiving files
 */
public class FileReceiver implements Runnable {

    private int portNumber;                     //new port for the file transferring
    private String myName;                      // name of the client who is receiving a file
    private ClientManager clientManager;      //message manager of that client

    public FileReceiver(int portNumber, String myName, ClientManager clientManager) {
        this.portNumber = portNumber;
        this.myName = myName;
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

        ClientSideMessageInterpreter interpreter =
                new ClientSideMessageInterpreter(socket);  //new interpreter for file receiving

        interpreter.setMyName(myName);                     //set name of the client

        interpreter.setClientManager(clientManager);     //set message manager of the client to his/het
                                                           // file transfer thread

        ServerListener serverListener = new ServerListener(socket, interpreter); //new server listener for file transfer

        Thread sl = new Thread(serverListener);

        PrintWriter writer = null;
        try {
            writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        writer.println("OPEN_PORT_TO_RECEIVE " + myName);   //let server know that client has opened a
                                                            // new socket to receive a file

        sl.start();
    }
}
