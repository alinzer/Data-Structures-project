package edu.yu.cs.com1320.project.stage2.impl;

import java.net.URI;
import java.util.Arrays;

import edu.yu.cs.com1320.project.stage2.Document;

public class DocumentImpl implements Document {
    private URI uri;
    private String text;
    private byte[] binaryData;

    public DocumentImpl(URI uri, String txt) {
        if (txt == null || txt.length() == 0) {
            throw new IllegalArgumentException ("Text entered is null or blank");
        }
        if (uri == null || uri.toASCIIString().isBlank()) {
            throw new IllegalArgumentException ("URI entered is null or blank");
        }
        this.uri = uri;
        this.text = txt;
        this.binaryData = null;
    }

    public DocumentImpl(URI uri, byte[] binaryData) {
        if (binaryData == null || binaryData.length == 0) {
            throw new IllegalArgumentException ("Data entered is null or blank");
        }
        if (uri == null || uri.toASCIIString().isBlank()) {
            throw new IllegalArgumentException ("URI entered is null or blank");
        }
        this.uri = uri;
        this.binaryData = binaryData;
        this.text = null;
    }
    
    /**
     * @return content of text document
     */
    @Override
    public String getDocumentTxt() {
        return this.text;
    }

    /**
     * @return content of binary data document
     */
    @Override
    public byte[] getDocumentBinaryData() {
        if (this.binaryData == null) {
            return null;
        }
        return this.binaryData.clone();
    }

    /**
     * @return URI which uniquely identifies this document
     */
    @Override
    public URI getKey() {
        return this.uri;

    }

    @Override
    public boolean equals(Object other) {
        if (other.hashCode() == this.hashCode()) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = uri.hashCode();
        result = 31 * result + (text != null ? text.hashCode() : 0); 
        result = 31 * result + Arrays.hashCode(binaryData);
        return result;
    }
}