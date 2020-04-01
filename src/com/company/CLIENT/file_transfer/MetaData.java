package com.company.CLIENT.file_transfer;

/**
 * Meta data of a file
 * Consists of file size, file name and a checksum
 */
public class MetaData {

    private String filePath;        //path to the file
    private byte[] bytes;           // bytes of the file

    public MetaData(String filePath) {
        this.filePath = filePath;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    /**
     * Calculates a unique checksum for the file
     *
     * @return checksum
     */
    private int calculateChecksum() {
        int sum = 0;
        for (int i = 0; i < bytes.length; i++) {
            sum += (bytes[i] * (i + 1));
        }
        return sum;
    }

    /**
     * Returns meta data of a file
     *
     * @return meta data
     */
    public String getMetaData() {
        return this.bytes.length + " " + filePath + " " + this.calculateChecksum();
    }
}
