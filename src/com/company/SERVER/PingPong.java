package com.company.SERVER;

import com.company.SERVER.messages.ServerSideMessageInterpreter;
import com.company.tools.Printer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

/**
 * Thread for ping pong exchange
 */
public class PingPong implements Runnable {

    @Override
    public void run() {

        while (!ChatManager.getUsers().isEmpty()) {                                 //while there are any users online

            Set<Socket> users = new HashSet<>(ChatManager.getUsers().keySet());

            for (Socket socket : users) {                                           //loop through users and send pings
                PrintWriter writer = null;
                try {
                    writer = new PrintWriter(socket.getOutputStream(), true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                writer.println("PING");

                if (ChatManager.getUsers().get(socket) != null) {

                    Printer.printOkMessage("PING : " + ChatManager.getUsers().get(socket).getLogin());
                } else {
                    Printer.printOkMessage("PING : " + socket.getPort());
                }
                try {
                    Thread.sleep(3000);                                  //wait three seconds and send ping again
                    ServerSideMessageInterpreter.checkPongForUser(socket);      //check if user responded to the ping
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
