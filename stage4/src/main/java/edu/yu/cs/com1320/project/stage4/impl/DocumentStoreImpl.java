package edu.yu.cs.com1320.project.stage4.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import edu.yu.cs.com1320.project.GenericCommand;
import edu.yu.cs.com1320.project.CommandSet;
import edu.yu.cs.com1320.project.Undoable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage4.Document;
import edu.yu.cs.com1320.project.stage4.DocumentStore;

public class DocumentStoreImpl implements DocumentStore {
    private HashTableImpl<URI, DocumentImpl> table;
    private StackImpl<Undoable> stack;
    private TrieImpl<Document> trie;
    private MinHeapImpl<Document> heap;
    private int maxDocCount, maxByteCount;
    private int currentDocCount, currentByteCount;
    private boolean hasMaxDocCount, hasMaxByteCount;

    public DocumentStoreImpl() {
        this.table = new HashTableImpl<>();
        this.stack = new StackImpl<>();
        this.trie = new TrieImpl<>();
        this.heap = new MinHeapImpl<>();
        this.currentDocCount = this.currentByteCount = 0;
        this.hasMaxDocCount = this.hasMaxByteCount = false;
    }

    /**
     * @param input  the document being put
     * @param uri    unique identifier for the document
     * @param format indicates which type of document format is being passed
     * 
     *               If InputStream is null, this is a delete, and thus return
     *               either the hashCode of the deleted doc
     *               or 0 if there is no doc to delete.
     * @throws IOException              if there is an issue reading input
     * @throws IllegalArgumentException if uri or format are null
     */
    @Override
    public int putDocument(InputStream input, URI uri, DocumentFormat format) throws IOException {
        // Creates a temp document in order to save the hashcode of the previous
        // document or 0 if there is none, into a variable
        DocumentImpl oldDoc = this.table.get(uri);
        int hashToReturn;
        if (oldDoc != null) {
            hashToReturn = oldDoc.hashCode();
        } else {
            hashToReturn = 0;
        }
        if (uri == null || uri.toASCIIString().isBlank()) {
            throw new IllegalArgumentException("No URI entered");
        }
        // Checks to see if the imput is null, and calls delete document if it is.
        // Returns the hashcode of the document
        if (input == null) {
            this.deleteDocument(uri);
            return hashToReturn;
        }
        if (format == null) {
            throw new IllegalArgumentException("No format entered");
        }
        // Transfers the input into a string or byte[] depending on requested format.
        // Calls HT's put
        this.makeAndPutDoc(input, uri, format, oldDoc);
        return hashToReturn;
    }

    private void makeAndPutDoc(InputStream input, URI uri, DocumentFormat format, DocumentImpl oldDoc)
            throws IOException {
        byte[] binaryData = input.readAllBytes();
        DocumentImpl docToPut;
        if (format == DocumentFormat.TXT) {
            String text = new String(binaryData);
            docToPut = new DocumentImpl(uri, text);
        } else {
            docToPut = new DocumentImpl(uri, binaryData);
        }
        if ((this.hasMaxByteCount && this.getByteCountOfDoc(docToPut) > this.maxByteCount) || (this.hasMaxDocCount && this.maxDocCount == 0)) {
            throw new IllegalArgumentException("Byte count of document entered is too high or max doc count is 0, so can't add a document");
        }
        //add to hashtable
        this.table.put(uri, docToPut);
        //add to trie and deletion of oldDoc from trie if necessary
        if (docToPut.getDocumentBinaryData() == null) {
            this.addDocToTrie(docToPut);
        }
        if (oldDoc != null && oldDoc.getDocumentBinaryData() == null) {
            this.deleteDocFromTrie(oldDoc);
        }
        //delete oldDoc from heap and lowers the doc count
        if (oldDoc != null) {
            this.deleteFromHeap(oldDoc);
            this.currentDocCount --;
            this.currentByteCount -= this.getByteCountOfDoc(oldDoc);
        }
        //add to heap
        this.addToHeap(docToPut, uri);
        this.currentDocCount ++;
        this.currentByteCount += this.getByteCountOfDoc(uri);
        this.pushNewCommandForPut(docToPut, oldDoc, uri);
    }
    
    private void addToHeap(Document doc, URI uri) {
        doc.setLastUseTime(System.nanoTime());
        // while total doc count > allowed || total byte count > allowed,
        // delete the document with lowest time and delete from everywhere
        while ((this.hasMaxDocCount && (this.currentDocCount + 1) > this.maxDocCount) || (this.hasMaxByteCount && (this.currentByteCount + this.getByteCountOfDoc(uri)) > this.maxByteCount)) {
           this.deleteFromHeapFully();
        }
        this.heap.insert(doc);
        this.heap.reHeapify(doc);
    }
    
    private void deleteFromHeap(Document doc) {
        doc.setLastUseTime(Integer.MIN_VALUE);
        this.heap.reHeapify(doc);
        this.heap.remove();
    }
    
    private void deleteFromHeapFully() {
        Document docToDelete = this.heap.remove();
        URI uriToDelete = docToDelete.getKey();
        this.currentDocCount --;
        this.currentByteCount -= this.getByteCountOfDoc(docToDelete);
        this.deleteDocFromTrie((DocumentImpl) docToDelete);
        this.deleteURIFromStack(uriToDelete);
        this.table.put(uriToDelete, null);
    }
    
    private void deleteURIFromStack (URI uri) {
        StackImpl<Undoable> temp = new StackImpl<>();
        while (this.stack.peek() != null) {
            Undoable current = this.stack.pop();
            //i think the best way is if !target uri, then push back
            //else just keep it popped off
            if (current instanceof GenericCommand) {
                if (!((GenericCommand<URI>) current).getTarget().equals(uri)) {
                    temp.push(current);
                }
            }
            else if (current instanceof CommandSet) {
                CommandSet<URI> commandSet = ((CommandSet<URI>) current);
                Iterator<GenericCommand<URI>> iterator = commandSet.iterator();
                while (iterator.hasNext()) {
                    if (iterator.next().getTarget().equals(uri)) {
                        iterator.remove();
                    }
                }
                if (commandSet.size() != 0) {
                    temp.push(current);
                }
            }
        }
        while (temp.peek() != null) {
            this.stack.push(temp.pop());
        }
    }

    private void addDocToTrie(DocumentImpl doc) {
        for (String word : doc.getWords()) {
            this.trie.put(word, doc);
        }
    }

    private void pushNewCommandForPut(DocumentImpl doc, DocumentImpl oldDoc, URI uri) {
        Function<URI, Boolean> undo;
        undo = (URI) -> {
            this.currentDocCount --;
            this.currentByteCount -= this.getByteCountOfDoc(uri);
            //if oldDoc is null, then it's just a delete, if oldDoc != null then it calls a replace- interaction with HashTable
            this.table.put(uri, oldDoc);
            //interaction with trie
            if (doc.getDocumentBinaryData() == null) {
                this.deleteDocFromTrie(doc);
            }
            if (oldDoc != null && oldDoc.getDocumentBinaryData() == null) {
                this.addDocToTrie(oldDoc);
            }
            //interaction with heap
            this.deleteFromHeap(doc);
            if (oldDoc != null) {
                this.addToHeap(oldDoc, uri);
                this.currentDocCount ++;
                this.currentByteCount += this.getByteCountOfDoc(oldDoc);
            }
            return true;
        };
        this.stack.push(new GenericCommand<URI>(uri, undo));
    }

    /**
     * @param uri the unique identifier of the document to get
     * @return the given document
     */
    @Override
    public Document getDocument(URI uri) {
        Document doc = this.table.get(uri);
        if (doc != null) {
            this.newLastUseTime(doc);
        }
        return doc;
    }

    /**
     * @param uri the unique identifier of the document to delete
     * @return true if the document is deleted, false if no document exists with
     *         that URI
     */

    @Override
    public boolean deleteDocument(URI uri) {
        DocumentImpl doc = this.table.get(uri);
        if (uri == null) {
            return false;
        }
        this.pushNewCommandForDelete(doc, uri);
        if (this.getDocument(uri) == null) {
            return false;
        }
        this.currentDocCount --;
        this.currentByteCount -= this.getByteCountOfDoc(uri);
        //delete from hashtable
        this.table.put(uri, null);
        //delete from trie
        if (doc.getDocumentBinaryData() == null) {
            this.deleteDocFromTrie(doc);
        }
        //delete from heap
        this.deleteFromHeap(doc);
        return true;
    }

    private void deleteDocFromTrie(DocumentImpl doc) {
        for (String word : doc.getWords()) {
            this.trie.delete(word, doc);
        }
    }

    // makes the undo for a delete call and pushes it to the stack
    private void pushNewCommandForDelete(DocumentImpl doc, URI uri) {
        Function<URI, Boolean> undo;
        //if nothing is being deleted
        if (this.getDocument(uri) == null) {
            undo = (URI) -> {
                return true;
            };
        }
        else {
            undo = (URI) -> {
                //put doc in hashtable
                this.table.put(uri, doc);
                //add doc to trie
                if (doc.getDocumentBinaryData() == null) {
                    this.addDocToTrie(doc);
                }
                //add doc to heap
                this.addToHeap(doc, uri);
                this.currentDocCount ++;
                this.currentByteCount += this.getByteCountOfDoc(uri);
                return true;
            };
        }
        this.stack.push(new GenericCommand<URI>(uri, undo));
    }

    /**
     * undo the last put or delete command
     * 
     * @throws IllegalStateException if there are no actions to be undone, i.e. the
     *                               command stack is empty
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
     * 
     * @param uri
     * @throws IllegalStateException if there are no actions on the command stack
     *                               for the given URI
     */
    @Override
    public void undo(URI uri) throws IllegalStateException {
        // have to creat a temp stack and push from original until we find the uri we
        // want. then call undo
        StackImpl<Undoable> temp = new StackImpl<>();
        Boolean uriPresent = false;
        while (this.stack.peek() != null) {
            Undoable current = this.stack.pop();
            if (current instanceof GenericCommand) {
                if (((GenericCommand<URI>) current).getTarget().equals(uri)) {
                    current.undo();
                    uriPresent = true;
                    break;
                }
            }
            else if (current instanceof CommandSet) {
                CommandSet<URI> commandSet = ((CommandSet<URI>) current);
                if (commandSet.containsTarget(uri)) {
                    commandSet.undo(uri);
                    uriPresent = true;
                    if (commandSet.size() != 0) {
                        this.stack.push(commandSet);
                    }
                    break;
                }
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

    /**
     * Retrieve all documents whose text contains the given keyword.
     * Documents are returned in sorted, descending order, sorted by the number of
     * times the keyword appears in the document.
     * Search is CASE INSENSITIVE.
     * 
     * @param keyword
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    @Override
    public List<Document> search(String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        List<Document> results = this.trie.getAllSorted(lowerKeyword, new Comparator<Document>(){
            @Override
            public int compare(Document d1, Document d2) {
                return Integer.compare(d2.wordCount(lowerKeyword), (d1.wordCount(lowerKeyword)));
            }
        });
        for (Document doc : results) {
            this.newLastUseTime(doc);
        }
        return results;
    }

    /**
     * Retrieve all documents whose text starts with the given prefix
     * Documents are returned in sorted, descending order, sorted by the number of
     * times the prefix appears in the document.
     * Search is CASE INSENSITIVE.
     * 
     * @param keywordPrefix
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    @Override
    public List<Document> searchByPrefix(String keywordPrefix) {
        String lowerKeyword = keywordPrefix.toLowerCase();
        
        //get list of all words that have the prefix 
        //somehow need to compare the amount of times that all of the words appear in the doc
        // right now it's only comparing the docs using the prefix goofah  
        List<Document> list = this.trie.getAllWithPrefixSorted(lowerKeyword, new Comparator<Document>(){
            @Override
            public int compare(Document d1, Document d2) {
                int doc1 = 0;
                int doc2 = 0;
                for (String word : d1.getWords()){
                    if (word.startsWith(lowerKeyword)){
                        doc1 += d1.wordCount(word);
                    }
                }
                for (String word : d2.getWords()){
                    if (word.startsWith(lowerKeyword)){
                        doc2 += d2.wordCount(word);
                    }
                }
                return Integer.compare(doc2, doc1);
            }
        });
        for (Document doc : list) {
            this.newLastUseTime(doc);
        }
        return list;
    }
    
    /**
     * Completely remove any trace of any document which contains the given keyword
     * 
     * @param keyword
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAll(String keyword) {
        return this.deleteAndMakeURI(keyword, "stam");
    }

    /**
     * Completely remove any trace of any document which contains a word that has
     * the given prefix
     * Search is CASE INSENSITIVE.
     * 
     * @param keywordPrefix
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAllWithPrefix(String keywordPrefix) {
        return this.deleteAndMakeURI(keywordPrefix, "prefix");
    }
    
    private Set<URI> deleteAndMakeURI (String keyword, String typeOfDelete) {
        String lowerKeyword = keyword.toLowerCase();
        Set<Document> results;
        //calls whichever delete is relevant and saves the list of deleted
        if (typeOfDelete.equals("stam")) {
            results = this.trie.deleteAll(lowerKeyword);
        }    
        else {
            results = this.trie.deleteAllWithPrefix(lowerKeyword);
        }
        //sets up a map from uri of deleted docs to the docs
        HashMap<URI, DocumentImpl> uriToDoc = new HashMap<>();
        for (Document doc : results) {
            DocumentImpl docI = (DocumentImpl)doc;
            URI uri = docI.getKey();
            uriToDoc.put(uri, docI);
        }
        //deletes each document from heap
        //deletes each document from trie
        //deletes each document from hashtable
        for (URI uri : uriToDoc.keySet()) {
            Document docToDelete  = uriToDoc.get(uri);
            this.deleteFromHeap(docToDelete);
            this.currentDocCount --;
            this.currentByteCount -= this.getByteCountOfDoc(uri);
            this.deleteDocFromTrie((DocumentImpl) docToDelete);
            this.table.put(uri, null);
        }
        
        Function<URI, Boolean> undo;
        CommandSet<URI> command = new CommandSet<>();
        for (URI uri : uriToDoc.keySet()) {
            undo = (URI) -> {
            //reputs the doc in the hashtable
            //reputs the doc in the trie
            //reputs the doc in the hash
                this.table.put(uri, uriToDoc.get(uri));
                this.addDocToTrie(uriToDoc.get(uri));
                this.addToHeap(uriToDoc.get(uri), uri);
                this.currentDocCount ++;
                this.currentByteCount += this.getByteCountOfDoc(uri);
                return true;
            };
            //add a command with the above uri to commandset
            if (!command.containsTarget(uri)) {
                command.addCommand(new GenericCommand<URI>(uri, undo));
            }
        }
        this.stack.push(command);
        return uriToDoc.keySet();
    }
    
    @Override
    public void setMaxDocumentCount(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException ("limit must be positive");
        }
        this.hasMaxDocCount = true;
        this.maxDocCount = limit;
        while (this.currentDocCount > this.maxDocCount || (this.hasMaxByteCount && this.currentByteCount > this.maxByteCount)) {
            this.deleteFromHeapFully();
        }
    }

    @Override
    public void setMaxDocumentBytes(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException ("limit must be positive");
        }
        this.hasMaxByteCount = true;
        this.maxByteCount = limit;
        while ((this.hasMaxDocCount && this.currentDocCount > this.maxDocCount) || this.currentByteCount > this.maxByteCount) {
            this.deleteFromHeapFully();
        }
    }
    
    private int getByteCountOfDoc(URI uri) {
        DocumentImpl doc = (DocumentImpl) this.table.get(uri);
        if (doc.getDocumentTxt() != null) {
            return doc.getDocumentTxt().getBytes().length;
        }
        return doc.getDocumentBinaryData().length;
    }
    
    private int getByteCountOfDoc(Document doc) {
        if (doc.getDocumentTxt() != null) {
            return doc.getDocumentTxt().getBytes().length;
        }
        return doc.getDocumentBinaryData().length;
    }
    
    
    private void newLastUseTime(Document doc) {
        doc.setLastUseTime(System.nanoTime()); 
        this.heap.reHeapify(doc);
    }
    
}