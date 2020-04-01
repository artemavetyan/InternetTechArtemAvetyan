package com.company.CLIENT;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Manager that saves client's information
 */
public class ClientManager {
    private Set<String> receivedMetaData;               // meta data that user received from another user
    private Map<String, String> messagesToSend;         // messages to be sent to a user
    private Map<String, byte[]> filesToSend;            // files to be sent
    private Map<String, String> sessionKeys;

    public ClientManager() {
        this.messagesToSend = new HashMap<>();
        this.receivedMetaData = new HashSet<>();
        this.filesToSend = new HashMap<>();
        this.sessionKeys = new HashMap<>();
    }

    public Set<String> getReceivedMetaData() {
        return receivedMetaData;
    }

    public String getMessageToSend(String key) {
        String message = this.messagesToSend.get(key);
        this.messagesToSend.remove(key);
        return message;
    }

    public void putMessageToSend(String key, String message) {
        this.messagesToSend.put(key, message);
    }

    public void putFileToSend(String key, byte[] file) {
        this.filesToSend.put(key, file);
    }

    /**
     * Gets a file to be sent
     * Deletes it from the map
     *
     * @param key meta data of the file
     * @return file in bytes
     */
    public byte[] getFileToSend(String key) {
        byte[] file = this.filesToSend.get(key);
        this.filesToSend.remove(key);
        return file;
    }


    public void addMetaData(String metadata) {
        this.receivedMetaData.add(metadata);
    }

    public void deleteMetadata(String metadata) {
        this.receivedMetaData.remove(metadata);
    }

    /**
     * Generates a session key
     * Saves it with the name of a user as a key
     *
     * @param userName user with whom the session has been established
     * @return generated session key
     */
    public String generateSessionKeyWithUser(String userName) {
        byte[] array = new byte[7];
        new Random().nextBytes(array);
        String sessionKey = new String(array, StandardCharsets.UTF_8);

        this.sessionKeys.put(userName, sessionKey);
        return sessionKey;
    }

    public void putSessionKeyOfUser(String userName, String sessionKey) {
        this.sessionKeys.put(userName, sessionKey);
    }

    public String getSessionKeyOfUser(String userName) {
        return this.sessionKeys.get(userName);
    }

    public void removeSessionKey(String userName) {
        this.sessionKeys.remove(userName);
    }

    public Map<String, String> getSessionKeys() {
        return sessionKeys;
    }
}
