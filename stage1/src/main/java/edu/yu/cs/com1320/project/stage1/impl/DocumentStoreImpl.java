package edu.yu.cs.com1320.project.stage1.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.stage1.Document;
import edu.yu.cs.com1320.project.stage1.DocumentStore;

public class DocumentStoreImpl implements DocumentStore {
    private HashTableImpl<URI, DocumentImpl> store;

    public DocumentStoreImpl() {
        this.store = new HashTableImpl<>();
    }
    
    /**
     * @param input the document being put
     * @param uri unique identifier for the document
     * @param format indicates which type of document format is being passed
     * 
     * If InputStream is null, this is a delete, and thus return either the hashCode of the deleted doc 
     * or 0 if there is no doc to delete.
     * @throws IOException if there is an issue reading input
     * @throws IllegalArgumentException if uri or format are null
     */
    @Override
    public int putDocument(InputStream input, URI uri, DocumentFormat format) throws IOException {
        //Creates a temp document in order to save the hashcode of the previous document or 0 if there is none, into a variable
        DocumentImpl oldDoc = this.store.get(uri);         
        int hashToReturn;
        if (oldDoc != null) {
            hashToReturn = oldDoc.hashCode();            
        } 
        else {
            hashToReturn = 0;
        } 
        if (uri == null || uri.toASCIIString().isBlank()) {
            throw new IllegalArgumentException("No URI entered");
        }   
        //Checks to see if the imput is null, and calls delete document if it is. Returns the hashcode of the document
        if (input == null) {
            this.deleteDocument(uri);
            return hashToReturn;
        }          
        if (format == null) {
            throw new IllegalArgumentException("No format entered");
        }      
        //Transfers the input into a string or byte[] depending on requested format. Calls HT's put 
        this.makeAndPutDoc(input, uri, format); 
        return hashToReturn;
    }

    private void makeAndPutDoc (InputStream input, URI uri, DocumentFormat format) throws IOException {
        byte[] binaryData = input.readAllBytes();
        DocumentImpl docToPut;
        if (format == DocumentFormat.TXT) {
            String text = new String(binaryData);
            docToPut = new DocumentImpl(uri, text);
        }
        else {
            docToPut = new DocumentImpl(uri, binaryData);
        }   
        this.store.put(uri, docToPut);    
    }
    /**
     * @param uri the unique identifier of the document to get
     * @return the given document
     */
    @Override
    public Document getDocument(URI uri) {
        return this.store.get(uri);
    }

    /**
     * @param uri the unique identifier of the document to delete
     * @return true if the document is deleted, false if no document exists with that URI
     */
    
    @Override
    public boolean deleteDocument(URI uri) {
        if (uri == null || this.store.get(uri) == null) {
            return false;
        }
        this.store.put(uri, null);       
        return true;
    }
}