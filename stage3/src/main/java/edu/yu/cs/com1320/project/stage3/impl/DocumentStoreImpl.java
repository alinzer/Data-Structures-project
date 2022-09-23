package edu.yu.cs.com1320.project.stage3.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import edu.yu.cs.com1320.project.GenericCommand;
import edu.yu.cs.com1320.project.CommandSet;
import edu.yu.cs.com1320.project.Undoable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage3.Document;
import edu.yu.cs.com1320.project.stage3.DocumentStore;

public class DocumentStoreImpl implements DocumentStore {
    private HashTableImpl<URI, DocumentImpl> store;
    private StackImpl<Undoable> stack;
    private TrieImpl<Document> trie;

    public DocumentStoreImpl() {
        this.store = new HashTableImpl<>();
        this.stack = new StackImpl<>();
        this.trie = new TrieImpl<>();
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
        DocumentImpl oldDoc = this.store.get(uri);
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
        this.store.put(uri, docToPut);
        if (docToPut.getDocumentBinaryData() == null) {
            this.addDocToTrie(docToPut);
        }
        if (oldDoc != null && oldDoc.getDocumentBinaryData() == null) {
            this.deleteDocFromTrie(oldDoc);
        }
        this.pushNewCommandForPut(docToPut, oldDoc, uri);
    }

    private void addDocToTrie(DocumentImpl doc) {
        for (String word : doc.getWords()) {
            this.trie.put(word, doc);
        }
    }

    private void pushNewCommandForPut(DocumentImpl doc, DocumentImpl oldDoc, URI uri) {
        Function<URI, Boolean> undo;
        // need to undo the putDoc. If oldDoc == null then this is a stam put. Undo
        // would be to remove it.
        // if oldDoc != null then this is a replace. Would need to call replace again
        // with old doc replacing the new one
        if (doc.getDocumentBinaryData() != null) {
            undo = (URI) -> {
                this.store.put(uri, oldDoc);
                return true;
            };
        } 
        else {
            undo = (URI) -> {
                this.store.put(uri, oldDoc);
                this.deleteDocFromTrie(doc);
                if (oldDoc != null) {
                    this.addDocToTrie(oldDoc);
                }
                return true;
            };
        }
        this.stack.push(new GenericCommand<URI>(uri, undo));
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
     * @return true if the document is deleted, false if no document exists with
     *         that URI
     */

    @Override
    public boolean deleteDocument(URI uri) {
        DocumentImpl doc = this.store.get(uri);
        if (uri == null) {
            return false;
        }
        this.pushNewCommandForDelete(doc, uri);
        if (this.getDocument(uri) == null) {
            return false;
        }
        this.store.put(uri, null);
        if (doc.getDocumentBinaryData() == null) {
            this.deleteDocFromTrie(doc);
        }
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
        if (this.getDocument(uri) == null) {
            undo = (URI) -> {
                return true;
            };
        }
        else if (doc.getDocumentBinaryData() != null) {
            undo = (URI) -> {
                this.store.put(uri, doc);
                return true;
            };
        } else {
            undo = (URI) -> {
                this.store.put(uri, doc);
                this.addDocToTrie(doc);
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
        return this.trie.getAllSorted(lowerKeyword, new Comparator<Document>(){
            @Override
            public int compare(Document d1, Document d2) {
                return Integer.compare(d2.wordCount(lowerKeyword), (d1.wordCount(lowerKeyword)));
            }
        });
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
        if (typeOfDelete.equals("stam")) {
            results = this.trie.deleteAll(lowerKeyword);
        }    
        else {
            results = this.trie.deleteAllWithPrefix(lowerKeyword);
        }
        HashMap<URI, DocumentImpl> uriToDoc = new HashMap<>();
        for (Document doc : results) {
            DocumentImpl docI = (DocumentImpl)doc;
            URI uri = docI.getKey();
            uriToDoc.put(uri, docI);
        }
        //deletes each document that was deleted from the docStore
        //deletes each document from the rest of the trie;
        for (URI uri : uriToDoc.keySet()) {
            this.store.put(uri, null);
            this.deleteDocFromTrie(uriToDoc.get(uri));
        }
        
        Function<URI, Boolean> undo;
        CommandSet<URI> command = new CommandSet<>();
        for (URI uri : uriToDoc.keySet()) {
            undo = (URI) -> {
            //reputs the doc in the documentStore
            //reputs the doc in the trie
                this.store.put(uri, uriToDoc.get(uri));
                this.addDocToTrie(uriToDoc.get(uri));
                return true;
            };
            //add a new command to the command set
            if (!command.containsTarget(uri)) {
                command.addCommand(new GenericCommand<URI>(uri, undo));
            }
        }
        this.stack.push(command);
        return uriToDoc.keySet();
    }
}