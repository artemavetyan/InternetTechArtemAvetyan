package com.company.SERVER.model;

import java.net.Socket;

public class User {

    private String login;

    private Socket fileTransferSocket;              //special socket for file transfer

    public User(String login) {

        this.login = login;
        this.fileTransferSocket = null;             //initially there is no file transfer socket
    }

    public String getLogin() {
        return this.login;
    }

    public Socket getFileTransferSocket() {
        return fileTransferSocket;
    }

    public void setFileTransferSocket(Socket fileTransferSocket) {
        this.fileTransferSocket = fileTransferSocket;
    }

    @Override
    public String toString() {
        return '\'' + login + '\'';
    }
}
