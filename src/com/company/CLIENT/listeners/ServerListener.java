package com.company.CLIENT.listeners;

import com.company.CLIENT.messages.ClientSideMessage;
import com.company.CLIENT.messages.ClientSideMessageInterpreter;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

/**
 * Thread that listens to messages from the server
 */
public class ServerListener implements Runnable {

    private BufferedReader reader;
    private Socket socket;                              //Socket to listen to
    private ClientSideMessageInterpreter interpreter;   //interpreter of incoming messages

    public ServerListener(Socket socket, ClientSideMessageInterpreter interpreter) {
        this.socket = socket;
        try {
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.interpreter = interpreter;
    }

    @Override
    public void run() {
        String line = "";
        while (!socket.isClosed()) {
            try {
                //While the connection is active -> listen to the socket
                line = reader.readLine();
                //Translate the line from server to readable message
                ClientSideMessage message = new ClientSideMessage(line, ClientSideMessage.Sender.SERVER);
                //interpret the message
                interpreter.interpret(message);
            } catch (SocketException se) {
                se.printStackTrace();
                //If the connection was unexpectedly lost -> send an exception
                ClientSideMessage exceptionMessage = new ClientSideMessage(se, ClientSideMessage.Sender.SERVER);
                interpreter.interpret(exceptionMessage);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
