package com.company.CLIENT;

import com.company.CLIENT.enryption.RSAEncryption;
import com.company.CLIENT.listeners.UserListener;
import com.company.CLIENT.listeners.ServerListener;
import com.company.CLIENT.messages.ClientSideMessageInterpreter;

import java.io.IOException;
import java.net.Socket;

import java.security.KeyPair;

import java.util.Scanner;

/**
 * Client of the chat
 */
public class Client {

    public void run() {

        // Set up a connection with the server.
        // If you are connecting locally use SERVER_ADDRESS = “127.0.0.1”.
        Socket socket = null;
        try {
            socket = new Socket("127.0.0.1", 1337);
        } catch (IOException e) {
            e.printStackTrace();
        }

        KeyPair kp = RSAEncryption.getKeyPair();                //generate a key pair for the client

        Scanner scanner = new Scanner(System.in);

        ClientSideMessageInterpreter interpreter = new ClientSideMessageInterpreter(socket, scanner, kp);

        UserListener userListener = new UserListener(scanner, interpreter);
        ServerListener serverListener = new ServerListener(socket, interpreter);

        Thread cl = new Thread(userListener);
        Thread sl = new Thread(serverListener);

        cl.start();             //listen to the user input

        sl.start();            //listen to the server input
    }
}
