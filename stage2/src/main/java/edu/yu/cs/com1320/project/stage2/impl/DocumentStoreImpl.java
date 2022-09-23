package edu.yu.cs.com1320.project.stage2.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.function.Function;

import edu.yu.cs.com1320.project.Command;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.stage2.Document;
import edu.yu.cs.com1320.project.stage2.DocumentStore;

public class DocumentStoreImpl implements DocumentStore {
    private HashTableImpl<URI, DocumentImpl> store;
    private StackImpl<Command> stack;

    public DocumentStoreImpl() {
        this.store = new HashTableImpl<>();
        this.stack = new StackImpl<>();
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
        this.makeAndPutDoc(input, uri, format, oldDoc); 
        return hashToReturn;
    }

    private void makeAndPutDoc (InputStream input, URI uri, DocumentFormat format, DocumentImpl oldDoc) throws IOException {
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
        this.pushNewCommandForPut(oldDoc, uri);
        }    
    
    private void pushNewCommandForPut (DocumentImpl oldDoc, URI uri){
        Function<URI,Boolean> undo;  
        //need to undo the putDoc. If oldDoc == null then this is a stam put. Undo would be to remove it.
        //if oldDoc != null then this is a replace. Would need to call replace again with old doc replacing the new one
        if (oldDoc == null) {
            undo = (URI) -> {
                this.store.put(uri, null);
                return true;
            };
        }
        else {
            undo = (URI) -> {
                this.store.put(uri,oldDoc);
                return true;
            };
            System.out.println(this.stack.size());
        }
        this.stack.push(new Command(uri, undo));  
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
        if (uri == null) {
            return false;
        }
        this.pushNewCommandForDelete(uri);
        if (this.getDocument(uri) == null) {
            return false;
        }
        this.store.put(uri, null);
        return true;
    }
    
    //makes the undo for a delete call and pushes it to the stack
    private void pushNewCommandForDelete (URI uri){
        Function<URI,Boolean> undo;  
        DocumentImpl tempDoc = this.store.get(uri);
        // Entry<URI,DocumentImpl> = new ;
        undo = (URI) -> {
            this.store.put(uri, tempDoc);
            return true;
        };
        this.stack.push(new Command(uri, undo)); 
    }
    
    
    
    /**
     * undo the last put or delete command
     * @throws IllegalStateException if there are no actions to be undone, i.e. the command stack is empty
     */
    @Override
    public void undo() throws IllegalStateException {
        if (this.stack.peek() == null) {
            throw new IllegalStateException("no actions to undo");
        }   
        this.stack.pop().undo();
    }

    /**
     * undo the last put or delete that was done with the given URI as its key
     * @param uri
     * @throws IllegalStateException if there are no actions on the command stack for the given URI
     */
    @Override
    public void undo(URI uri) throws IllegalStateException {  
        // have to creat a temp stack and push from original until we find the uri we want. then call undo
        StackImpl<Command> temp = new StackImpl<>();
        Boolean uriPresent = false;
        while (this.stack.peek() != null) {
            Command current = this.stack.pop();
            if (current.getUri().equals(uri)) {
                current.undo();
                uriPresent = true;
                break;
            }
            else {
                temp.push(current);
            }
        }
        if (!uriPresent) {
            throw new IllegalStateException("Action requested to undo does not exist");
        }
        while (temp.peek() != null) {
            this.stack.push(temp.pop());
        }
    }
}