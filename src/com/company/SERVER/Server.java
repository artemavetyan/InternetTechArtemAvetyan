package com.company.SERVER;

import com.company.SERVER.listeners.ClientListener;
import com.company.SERVER.messages.ServerSideMessageInterpreter;
import com.company.tools.Printer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Server of the chat
 */
public class Server {

    public static final int PORT_NUMBER = 1337;

    public void run() {

        // Create a socket to wait for clients.
        ServerSocket serverSocket = null;

        ChatManager chatManager = new ChatManager();

        try {
            serverSocket = new ServerSocket(PORT_NUMBER);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            // Wait for an incoming client-connection request (blocking).
            Socket socket = null;
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Printer.printOkMessage("New user has been connected!");

            ServerSideMessageInterpreter interpreter = new ServerSideMessageInterpreter(socket);

            ChatManager.addUser(socket);

            ClientListener clientListener = new ClientListener(socket, interpreter, false);
            PingPong pingPong = new PingPong();

            Thread cl = new Thread(clientListener);
            Thread pp = new Thread(pingPong);

            cl.start();
            pp.start();
        }
    }
}
