package com.company.SERVER.file_transfer;

import com.company.SERVER.listeners.ClientListener;
import com.company.SERVER.messages.ServerSideMessageInterpreter;
import com.company.tools.Printer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static com.company.SERVER.Server.PORT_NUMBER;

/**
 * Thread that is responsible for file transfer
 */
public class FileTransferServer implements Runnable {

    private int portNumber;                                 //new port for the file transfer
    private static int lastPortNumber = PORT_NUMBER + 1;

    public FileTransferServer() {
        this.portNumber = lastPortNumber;
        lastPortNumber++;
    }

    public void run() {

        // Create a socket to wait for clients.
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(this.portNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Wait for an incoming client-connection request (blocking).
        int counter = 0;
        // Only until counter is 2 because we are only waiting for two clients - sender and receiver of a file
        while (counter < 2) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
            counter++;               // client has been connected -> increase the couter

            Printer.printOkMessage("New user has been connected for file transfer!");

            // new interpreter for the file transfer
            ServerSideMessageInterpreter interpreter = new ServerSideMessageInterpreter(socket);
            // new client listener for the file transfer
            ClientListener clientListener = new ClientListener(socket, interpreter, true);

            Thread cl = new Thread(clientListener);

            cl.start();
        }
    }

    public int getPortNumber() {
        return portNumber;
    }
}
