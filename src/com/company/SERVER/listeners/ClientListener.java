package com.company.SERVER.listeners;

import com.company.SERVER.messages.ServerSideMessage;
import com.company.SERVER.messages.ServerSideMessageInterpreter;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

/**
 * Thread that listens to client's incoming messages
 */
public class ClientListener implements Runnable {

    private BufferedReader reader;
    private PrintWriter printWriter;
    private Socket socket;

    private boolean forFileTrans;                       //check if this listener is for file transfer

    private ServerSideMessageInterpreter interpreter;

    public ClientListener(Socket socket,
                          ServerSideMessageInterpreter interpreter, boolean forFileTrans) {
        this.interpreter = interpreter;
        this.socket = socket;
        this.forFileTrans = forFileTrans;

        try {
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            this.printWriter = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String line = "";

        //if this is listener for a file transfer -> DO NOt send the hello message
        if (!forFileTrans) {
            printWriter.println("HELO Welkom ");
        }
        while (!socket.isClosed()) {

            try {
                line = reader.readLine();                                   //read the line from the client

                ServerSideMessage message = new ServerSideMessage(line);    // translate the line to a message

                interpreter.interpret(message);                            // interprete the message

            } catch (SocketException | NullPointerException se) {
                //If the connection was unexpectedly lost -> send an exception
                ServerSideMessage exceptionMessage = new ServerSideMessage(se);
                interpreter.interpret(exceptionMessage);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
