package com.company.tools;

/**
 * Helper class
 * Prints messages to the console using different colors
 */
public class Printer {

    public static final String ANSI_RESET = "\u001B[0m";            //Reset
    public static final String ANSI_RED = "\u001B[31m";             //Error messages
    public static final String ANSI_YELLOW = "\u001B[33m";          //OK messages
    public static final String ANSI_PURPLE = "\u001B[35m";          //Group message

    public static final String ANSI_CYAN = "\u001B[36m";            //Other's messages

    /**
     * Prints error message
     *
     * @param message to print
     */
    public static void printError(String message) {
        System.out.println(ANSI_RED + message + ANSI_RESET);
    }

    /**
     * Prints messages from another users
     *
     * @param message to print
     */
    public static void printOthersMessage(String message) {
        System.out.println(ANSI_CYAN + message + ANSI_RESET);
    }

    /**
     * Prints OK messages
     *
     * @param message to print
     */
    public static void printOkMessage(String message) {
        System.out.println(ANSI_YELLOW + message + ANSI_RESET);
    }

    /**
     * Prints group messages
     *
     * @param message to print
     */
    public static void printGroupMessage(String message) {
        System.out.println(ANSI_PURPLE + message + ANSI_RESET);
    }
}
