package com.company.CLIENT.listeners;

import com.company.CLIENT.messages.ClientSideMessage;
import com.company.CLIENT.messages.ClientSideMessageInterpreter;

import java.util.Scanner;

/**
 * Thread that listens to the user input
 */
public class UserListener implements Runnable {

    private Scanner scanner;                            //scanner to read user input from
    private ClientSideMessageInterpreter interpreter;   //intrepreter for incoming user input

    public UserListener(Scanner scanner, ClientSideMessageInterpreter interpreter) {
        this.scanner = scanner;
        this.interpreter = interpreter;
    }

    @Override
    public void run() {

        String userInput = "";

        while (this.scanner.hasNext()) {
            //While the connection is open -> read user input
            userInput = scanner.nextLine();

            //Make a message out of user input
            ClientSideMessage message = new ClientSideMessage(userInput, ClientSideMessage.Sender.CLIENT);
            //Interprete the message
            interpreter.interpret(message);
        }
    }
}
