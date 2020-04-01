package com.company.SERVER;

public class ServerMain {

    public static void main(String[] args) {
        new ServerMain().run();
    }

    public void run() {
        Server server = new Server();

        server.run();
    }
}
