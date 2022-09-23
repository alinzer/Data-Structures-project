package edu.yu.cs.com1320.project.stage4.impl;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import edu.yu.cs.com1320.project.stage4.Document;

public class DocumentImpl implements Document {
    private URI uri;
    private String text;
    private byte[] binaryData;
    private HashMap<String,Integer> countOfWords;
    private long lastUseTime;

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
        this.countOfWords = new HashMap<>();
        String temp = txt.toLowerCase().replaceAll("[^\\p{IsDigit}\\p{IsAlphabetic}\\p{IsSpace}]", "");
        temp = temp.replaceAll("  ", " ");
        String[] words = temp.split(" ");
        for (String word : words) {
            if (this.countOfWords.containsKey(word)) {
                this.countOfWords.put(word, countOfWords.get(word) + 1);
            }
            else{
                this.countOfWords.put(word, 1);
            }
        }
        
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

    /**
     * how many times does the given word appear in the document?
     * @param word
     * @return the number of times the given words appears in the document. If it's a binary document, return 0.
     */
    @Override
    public int wordCount(String word) {
        String lowerWord = word.toLowerCase();
        if (this.getDocumentTxt() == null) {
            return 0;
        }
        if (!this.getWords().contains(lowerWord)) {
            return 0;
        }
        return this.countOfWords.get(lowerWord);
    }

    /**
     * @return all the words that appear in the document
     */
    @Override
    public Set<String> getWords() {
        if (this.getDocumentTxt() == null) {
            return new HashSet<String>();
        }
        return this.countOfWords.keySet();
    }

    @Override
    public int compareTo(Document o) {
        if (this.getLastUseTime() > o.getLastUseTime()) {
            return 1;
        }
        if (this.getLastUseTime() < o.getLastUseTime()) {
            return -1;
        }
        return 0;
    }

    @Override
    public long getLastUseTime() {
        return this.lastUseTime;
    }

    @Override
    public void setLastUseTime(long timeInNanoseconds) {
        this.lastUseTime = timeInNanoseconds;
    }
}