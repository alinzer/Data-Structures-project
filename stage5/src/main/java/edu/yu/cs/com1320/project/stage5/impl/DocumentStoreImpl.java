package edu.yu.cs.com1320.project.stage5.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;


import edu.yu.cs.com1320.project.GenericCommand;
import edu.yu.cs.com1320.project.CommandSet;
import edu.yu.cs.com1320.project.Undoable;
import edu.yu.cs.com1320.project.impl.BTreeImpl;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.DocumentStore;

public class DocumentStoreImpl implements DocumentStore {
    private StackImpl<Undoable> stack;
    private TrieImpl<UriBTree> trie;
    private MinHeapImpl<UriBTree> heap;
    private BTreeImpl<URI, Document> bTree;
    private int maxDocCount, maxByteCount;
    private int currentDocCount, currentByteCount;
    private List<URI> disk;
   
    //Seperate class to hold a uri and btree
    private class UriBTree implements Comparable<UriBTree>{
        private BTreeImpl<URI, Document> uriTree;
        private URI uri;
        
        private UriBTree (URI uri, BTreeImpl<URI, Document> bTree) {
            this.uri = uri;
            this.uriTree = bTree;
        }
    
        @Override
        public int compareTo(UriBTree o) {
            return this.uriTree.get(uri).compareTo(o.uriTree.get(o.uri));
        }
        
        @Override
        public boolean equals(Object o) {
            // If the object is compared with itself then return true 
            if (o == this) {
                return true;
            }
            /* Check if o is an instance of Complex or not
            "null instanceof [type]" also returns false */
            if (!(o instanceof UriBTree)) {
                return false;
            }
            UriBTree ub = (UriBTree)o;
            return this.uri.equals(ub.getUri());
        }
        
        protected URI getUri() {
            return this.uri;
        }
        
        protected BTreeImpl<URI, Document> getBTree() {
            return this.uriTree;
        }
        
        
    }
    //constructers for DocumentStore
    public DocumentStoreImpl() {
        this.stack = new StackImpl<>();
        this.trie = new TrieImpl<>();
        this.heap = new MinHeapImpl<>();
        this.bTree = new BTreeImpl<>();
        this.bTree.setPersistenceManager(new DocumentPersistenceManager(new File(System.getProperty("user.dir"))));
        this.currentDocCount = this.currentByteCount = 0;
        this.maxDocCount = this.maxByteCount = Integer.MAX_VALUE;
        this.disk = new ArrayList<>();
    }
    
    public DocumentStoreImpl(File baseDir) {
        this.stack = new StackImpl<>();
        this.trie = new TrieImpl<>();
        this.heap = new MinHeapImpl<>();
        this.bTree = new BTreeImpl<>();
        this.bTree.setPersistenceManager(new DocumentPersistenceManager(baseDir));
        this.currentDocCount = this.currentByteCount = 0;
        this.maxDocCount = this.maxByteCount = Integer.MAX_VALUE;
        this.disk = new ArrayList<>();
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
        DocumentImpl oldDoc = (DocumentImpl) this.getFrombTree(uri);
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
        while (this.currentDocCount > this.maxDocCount || this.currentByteCount > this.maxByteCount) {
            this.deleteFromHeapFully();
        }
        return hashToReturn;
    }

    private void makeAndPutDoc(InputStream input, URI uri, DocumentFormat format, DocumentImpl oldDoc)
            throws IOException {
        byte[] binaryData = input.readAllBytes();
        DocumentImpl docToPut;
        if (format == DocumentFormat.TXT) {
            String text = new String(binaryData);
            docToPut = new DocumentImpl(uri, text, null);
        } else {
            docToPut = new DocumentImpl(uri, binaryData);
        }
        if (this.getByteCountOfDoc(docToPut) > this.maxByteCount || this.maxDocCount == 0) {
            throw new IllegalArgumentException("Byte count of document entered is too high or max doc count is 0, so can't add a document");
        }
        //add to bTree
        this.bTree.put(uri, docToPut);
        //add to trie and deletion of oldDoc from trie if necessary
        if (docToPut.getDocumentBinaryData() == null) {
            this.addDocToTrie(docToPut);
        }
        if (oldDoc != null && oldDoc.getDocumentBinaryData() == null) {
            this.deleteDocFromTrie(oldDoc);
        }
        
        //delete oldDoc from heap and lowers the doc count
        if (oldDoc != null) {
            this.deleteFromHeap(oldDoc, uri);
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
        UriBTree ub = new UriBTree(uri, this.bTree);
        this.heap.insert(ub);
        this.heap.reHeapify(ub);
        while (this.currentDocCount > this.maxDocCount || this.currentByteCount > this.maxByteCount) {
            this.deleteFromHeapFully();
        }
    }
    
    private void deleteFromHeap(Document doc, URI uri) {
        doc.setLastUseTime(0);
        UriBTree ub = new UriBTree(uri, this.bTree);
        this.heap.reHeapify(ub);
        this.heap.remove();
    }
    
    private void deleteFromHeapFully() {
        UriBTree ub = this.heap.remove();
        URI uriToDelete = ub.getUri();
        Document docToDelete = this.getFrombTree(uriToDelete);
        this.currentDocCount --;
        this.currentByteCount -= this.getByteCountOfDoc(docToDelete);
        // this.deleteURIFromStack(uriToDelete);
        try {
            this.bTree.moveToDisk(uriToDelete);
            this.disk.add(uriToDelete);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private void addDocToTrie(Document doc) {
        UriBTree ub = new UriBTree(doc.getKey(), this.bTree);
        for (String word : doc.getWords()) {
            this.trie.put(word, ub);
        }
    }

    private void pushNewCommandForPut(DocumentImpl doc, DocumentImpl oldDoc, URI uri) {
        Function<URI, Boolean> undo;
        undo = (URI) -> {
            this.currentDocCount --;
            this.currentByteCount -= this.getByteCountOfDoc(uri);
            //interaction with heap
            this.deleteFromHeap(doc, uri);
            //if oldDoc is null, then it's just a delete, if oldDoc != null then it calls a replace
            //interaction with bTree
            this.bTree.put(uri, oldDoc);
            //interaction with trie
            if (doc.getDocumentBinaryData() == null) {
                this.deleteDocFromTrie(doc);
            }
            if (oldDoc != null && oldDoc.getDocumentBinaryData() == null) {
                this.addDocToTrie(oldDoc);
            }
            if (oldDoc != null) {
                this.addToHeap(oldDoc, uri);
                this.currentDocCount ++;
                this.currentByteCount += this.getByteCountOfDoc(oldDoc);
            }
            while (this.currentDocCount > this.maxDocCount || this.currentByteCount > this.maxByteCount) {
                this.deleteFromHeapFully();
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
        Document doc = this.getFrombTree(uri);
        if (!this.disk.contains(uri) && doc != null) {
            this.newLastUseTime(doc);
        }
        while (this.currentDocCount > this.maxDocCount || this.currentByteCount > this.maxByteCount) {
            this.deleteFromHeapFully();
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
        DocumentImpl doc = (DocumentImpl) this.getFrombTree(uri);
        if (uri == null) {
            return false;
        }
        this.pushNewCommandForDelete(doc, uri);
        if (this.getFrombTree(uri) == null) {
            return false;
        }
        this.currentDocCount --;
        this.currentByteCount -= this.getByteCountOfDoc(uri);
        //delete from heap
        this.deleteFromHeap(doc, uri);
        //delete from bTree
        this.bTree.put(uri, null);
        //delete from trie
        if (doc.getDocumentBinaryData() == null) {
            this.deleteDocFromTrie(doc);
        }
        while (this.currentDocCount > this.maxDocCount || this.currentByteCount > this.maxByteCount) {
            this.deleteFromHeapFully();
        }
        return true;
    }

    private void deleteDocFromTrie(DocumentImpl doc) {
        UriBTree ub = new UriBTree(doc.getKey(), this.bTree);
        for (String word : doc.getWords()) {
            this.trie.delete(word, ub);
        }
    }

    // makes the undo for a delete call and pushes it to the stack
    private void pushNewCommandForDelete(DocumentImpl doc, URI uri) {
        Function<URI, Boolean> undo;
        //if nothing is being deleted
        if (this.getFrombTree(uri) == null) {
            undo = (URI) -> {
                return true;
            };
        }
        else {
            undo = (URI) -> {
                //put doc in bTree
                this.bTree.put(uri, doc);
                //add doc to trie
                if (doc.getDocumentBinaryData() == null) {
                    this.addDocToTrie(doc);
                }
                //add doc to heap
                this.addToHeap(doc, uri);
                this.currentDocCount ++;
                this.currentByteCount += this.getByteCountOfDoc(uri);
                while (this.currentDocCount > this.maxDocCount || this.currentByteCount > this.maxByteCount) {
                    this.deleteFromHeapFully();
                }
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
        List<UriBTree> results = this.trie.getAllSorted(lowerKeyword, new Comparator<UriBTree>(){
            @Override
            public int compare(UriBTree ub1, UriBTree ub2) {
                Document d1 = ub1.getBTree().get(ub1.getUri());
                Document d2 = ub1.getBTree().get(ub2.getUri());
                return Integer.compare(d2.wordCount(lowerKeyword), (d1.wordCount(lowerKeyword)));
            }
        });
        List<Document> docs = new ArrayList<Document>();
        for (UriBTree ub : results) {
            Document doc = this.getFrombTree(ub.getUri());
            this.newLastUseTime(doc);
            docs.add(doc);
        }
        while (this.currentDocCount > this.maxDocCount || this.currentByteCount > this.maxByteCount) {
            this.deleteFromHeapFully();
        }
        return docs;
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
        List<UriBTree> results = this.trie.getAllWithPrefixSorted(lowerKeyword, new Comparator<UriBTree>(){
            @Override
            public int compare(UriBTree ub1, UriBTree ub2) {
                Document d1 = ub1.getBTree().get(ub1.getUri());
                Document d2 = ub1.getBTree().get(ub2.getUri());
                int doc1 = 0;
                int doc2 = 0;
                if (d1 != null) {
                    for (String word : d1.getWords()){
                        if (word.startsWith(lowerKeyword)){
                            doc1 += d1.wordCount(word);
                        }
                    }
                }
                if (d2 != null) {
                    for (String word : d2.getWords()){
                        if (word.startsWith(lowerKeyword)){
                            doc2 += d2.wordCount(word);
                        }
                    }
                }
                return Integer.compare(doc2, doc1);
            }
        });
        List<Document> docs = new ArrayList<Document>();
        for (UriBTree ub : results) {
            Document doc = this.getFrombTree(ub.getUri());
            this.newLastUseTime(doc);
            docs.add(doc);
        }
        while (this.currentDocCount > this.maxDocCount || this.currentByteCount > this.maxByteCount) {
            this.deleteFromHeapFully();
        }
        return docs;
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
        Set<UriBTree> results;
        //calls whichever delete is relevant and saves the list of deleted
        if (typeOfDelete.equals("stam")) {
            results = this.trie.deleteAll(lowerKeyword);
        }    
        else {
            results = this.trie.deleteAllWithPrefix(lowerKeyword);
        }
        //sets up a map from uri of deleted docs to the docs
        HashMap<URI, Document> uriToDoc = new HashMap<>();
        for (UriBTree ub : results) {
            URI uri = ub.getUri();
            Document docI = this.getFrombTree(uri);
            uriToDoc.put(uri, docI);
        }
        //deletes each document from heap
        //deletes each document from trie
        //deletes each document from bTree
        for (URI uri : uriToDoc.keySet()) {
            Document docToDelete  = uriToDoc.get(uri);
            this.deleteDocFromTrie((DocumentImpl) docToDelete);
            this.deleteFromHeap(docToDelete, uri);
            this.currentDocCount --;
            this.currentByteCount -= this.getByteCountOfDoc(uri);
            this.bTree.put(uri, null);
        }
        
        Function<URI, Boolean> undo;
        CommandSet<URI> command = new CommandSet<>();
        for (URI uri : uriToDoc.keySet()) {
            undo = (URI) -> {
            //reputs the doc in the bTree
            //reputs the doc in the trie
            //reputs the doc in the hash
                UriBTree ub = new UriBTree(uri, this.bTree);
                this.bTree.put(uri, uriToDoc.get(uri));
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
        while (this.currentDocCount > this.maxDocCount || this.currentByteCount > this.maxByteCount) {
            this.deleteFromHeapFully();
        }
        return uriToDoc.keySet();
    }
    
    @Override
    public void setMaxDocumentCount(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException ("limit must be positive");
        }
        this.maxDocCount = limit;
        while (this.currentDocCount > this.maxDocCount || this.currentByteCount > this.maxByteCount) {
            this.deleteFromHeapFully();
        }
    }

    @Override
    public void setMaxDocumentBytes(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException ("limit must be positive");
        }
        this.maxByteCount = limit;
        while (this.currentDocCount > this.maxDocCount || this.currentByteCount > this.maxByteCount) {
            this.deleteFromHeapFully();
        }
    }
    
    private int getByteCountOfDoc(URI uri) {
        DocumentImpl doc = (DocumentImpl) this.bTree.get(uri);
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
        URI uri = doc.getKey();
        doc.setLastUseTime(System.nanoTime()); 
        UriBTree ub = new UriBTree(uri, this.bTree);
        this.heap.reHeapify(ub);
    }
    
    private Document getFrombTree(URI uri){
        Document doc = this.bTree.get(uri);
        if(this.disk.contains(uri)) {
            this.currentDocCount ++;
            this.currentByteCount += this.getByteCountOfDoc(uri);
            this.addToHeap(doc, uri);
            this.disk.remove(uri);
        }
        // this.newLastUseTime(doc);
        return doc;
    }
    
}