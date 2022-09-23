package edu.yu.cs.com1320.project.stage4;

import edu.yu.cs.com1320.project.Utils;
import edu.yu.cs.com1320.project.stage4.Document;
import edu.yu.cs.com1320.project.stage4.DocumentStore;
import edu.yu.cs.com1320.project.stage4.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage4.impl.DocumentStoreImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentStoreImplTest {

    //variables to hold possible values for doc1
    private URI uri1;
    private String txt1;

    //variables to hold possible values for doc2
    private URI uri2;
    String txt2;
    
    private URI uri3;
    String txt3;

    @BeforeEach
    public void init() throws Exception {
        //init possible values for doc1
        this.uri1 = new URI("http://edu.yu.cs/com1320/project/doc1");
        this.txt1 = "This is the text of doc1, in plain text. No fancy file format - just plain old String";

        //init possible values for doc2
        this.uri2 = new URI("http://edu.yu.cs/com1320/project/doc2");
        this.txt2 = "Text for doc2. A plain old String.";
        
        this.uri3 = new URI("http://edu.yu.cs/com1320/project/doc3");
        this.txt3 = "Hello text of doc3. text document";
    }

    @Test
    public void testPutBinaryDocumentNoPreviousDocAtURI() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        int returned = store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.BINARY);
        assertTrue(returned == 0);
    }

    @Test
    public void testPutTxtDocumentNoPreviousDocAtURI() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        int returned = store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        assertTrue(returned == 0);
    }

    @Test
    public void testPutDocumentWithNullArguments() throws IOException{
        DocumentStore store = new DocumentStoreImpl();
        try {
            store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), null, DocumentStore.DocumentFormat.TXT);
            fail("null URI should've thrown IllegalArgumentException");
        }catch(IllegalArgumentException e){}
        try {
            store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), this.uri1, null);
            fail("null format should've thrown IllegalArgumentException");
        }catch(IllegalArgumentException e){}
    }

    @Test
    public void testPutNewVersionOfDocumentBinary() throws IOException {
        //put the first version
        DocumentStore store = new DocumentStoreImpl();
        int returned = store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.BINARY);
        assertTrue(returned == 0);
        Document doc1 = store.getDocument(this.uri1);
        assertArrayEquals(this.txt1.getBytes(),doc1.getDocumentBinaryData(),"failed to return correct binary text");

        //put the second version, testing both return value of put and see if it gets the correct text
        int expected = doc1.hashCode();
        returned = store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri1, DocumentStore.DocumentFormat.BINARY);

        assertEquals(expected, returned,"should return hashcode of the old document");
        assertArrayEquals(this.txt2.getBytes(),store.getDocument(this.uri1).getDocumentBinaryData(),"failed to return correct data");
    }

    @Test
    public void testPutNewVersionOfDocumentTxt() throws IOException {
        //put the first version
        DocumentStore store = new DocumentStoreImpl();
        int returned = store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        assertTrue(returned == 0);
        assertEquals(this.txt1,store.getDocument(this.uri1).getDocumentTxt(),"failed to return correct text");

        //put the second version, testing both return value of put and see if it gets the correct text
        returned = store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        assertTrue(Utils.calculateHashCode(this.uri1, this.txt1,null) == returned,"should return hashcode of old text");
        assertEquals(this.txt2,store.getDocument(this.uri1).getDocumentTxt(),"failed to return correct text");
    }

    @Test
    public void testGetTxtDoc() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        int returned = store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        assertTrue(returned == 0);
        assertEquals(this.txt1,store.getDocument(this.uri1).getDocumentTxt(),"did not return a doc with the correct text");
    }

    @Test
    public void testGetTxtDocAsBinary() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        int returned = store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        assertTrue(returned == 0);
        assertNull(store.getDocument(this.uri1).getDocumentBinaryData(),"a text doc should return null for binary");
    }

    @Test
    public void testGetBinaryDocAsBinary() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        int returned = store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.BINARY);
        assertTrue(returned == 0);
        assertArrayEquals(this.txt2.getBytes(),store.getDocument(this.uri2).getDocumentBinaryData(),"failed to return correct binary array");
    }

    @Test
    public void testGetBinaryDocAsTxt() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        int returned = store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.BINARY);
        assertTrue(returned == 0);
        assertNull(store.getDocument(this.uri2).getDocumentTxt(),"binary doc should return null for text");
    }

    @Test
    public void testDeleteDoc() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        store.deleteDocument(this.uri1);
        assertNull(store.getDocument(this.uri1),"calling get on URI from which doc was deleted should've returned null");
    }

    @Test
    public void testDeleteDocReturnValue() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        //should return true when deleting a document
        assertEquals(true,store.deleteDocument(this.uri1),"failed to return true when deleting a document");
        //should return false if I try to delete the same doc again
        assertEquals(false,store.deleteDocument(this.uri1),"failed to return false when trying to delete that which was already deleted");
        //should return false if I try to delete something that was never there to begin with
        assertEquals(false,store.deleteDocument(this.uri2),"failed to return false when trying to delete that which was never there to begin with");
    }

    @Test
    public void testUndoAfterPutNew() throws IOException {
        //put the first version
        DocumentStoreImpl store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        assertEquals(this.txt1,store.getDocument(this.uri1).getDocumentTxt(),"failed to return correct text");
        //Undo the put
        store.undo();
        assertNull(store.getDocument(this.uri1),"calling get on URI from which doc was deleted should've returned null");
    }
    
    @Test
    public void testUndoAfterPutReplace() throws IOException, URISyntaxException {
        String txt3 = "This is number 3";
        DocumentStore store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
        assertEquals(this.txt1,store.getDocument(this.uri1).getDocumentTxt(),"failed to return correct text");
        assertEquals(this.txt2,store.getDocument(uri2).getDocumentTxt(),"failed to return correct text");
        store.putDocument(new ByteArrayInputStream(txt3.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        assertEquals(txt3,store.getDocument(this.uri1).getDocumentTxt(),"failed to return correct text");
        assertEquals(this.txt2,store.getDocument(uri2).getDocumentTxt(),"failed to return correct text");
        store.undo();
        assertEquals(this.txt1,store.getDocument(this.uri1).getDocumentTxt(),"failed to return correct text");
        assertEquals(this.txt2,store.getDocument(uri2).getDocumentTxt(),"failed to return correct text");
    }
    
    @Test
    public void testUndoAfterDelete() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        store.deleteDocument(this.uri1);
        assertNull(store.getDocument(this.uri1),"calling get on URI from which doc was deleted should've returned null");
        store.undo();
        assertEquals(this.txt1,store.getDocument(this.uri1).getDocumentTxt(),"failed to return correct text");
    }
    
    @Test 
    public void testUndoPutNewWithURI() throws IOException, URISyntaxException {
        //Tries to undo the first put.
        URI uri3 = new URI("http://edu.yu.cs/com1320/project/doc3");
        DocumentStore store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),uri3, DocumentStore.DocumentFormat.TXT);
        assertEquals(this.txt1,store.getDocument(this.uri1).getDocumentTxt(),"failed to return correct text");
        assertEquals(this.txt2,store.getDocument(uri3).getDocumentTxt(),"failed to return correct text");
        store.undo(this.uri1);
        assertNull(store.getDocument(this.uri1),"calling get on URI from which doc was deleted should've returned null");
        assertEquals(this.txt2,store.getDocument(uri3).getDocumentTxt(),"failed to return correct text");
    }
    
    @Test
    public void testUndoMultipleDelete() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),uri2, DocumentStore.DocumentFormat.TXT);
        assertEquals(this.txt1,store.getDocument(this.uri1).getDocumentTxt(),"failed to return correct text");
        assertEquals(this.txt2,store.getDocument(uri2).getDocumentTxt(),"failed to return correct text");
        store.deleteDocument(this.uri1);
        assertNull(store.getDocument(this.uri1),"calling get on URI from which doc was deleted should've returned null");
        assertEquals(this.txt2,store.getDocument(uri2).getDocumentTxt(),"failed to return correct text");
        store.undo();
        assertEquals(this.txt1,store.getDocument(this.uri1).getDocumentTxt(),"failed to return correct text");
        assertEquals(this.txt2,store.getDocument(uri2).getDocumentTxt(),"failed to return correct text");
    }
    
    @Test
    public void testUndoPutChained() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),uri2, DocumentStore.DocumentFormat.TXT);
        assertEquals(this.txt1,store.getDocument(this.uri1).getDocumentTxt(),"failed to return correct text");
        assertEquals(this.txt2,store.getDocument(uri2).getDocumentTxt(),"failed to return correct text");
        store.undo(this.uri1);
        assertNull(store.getDocument(this.uri1),"calling get on URI from which doc was deleted should've returned null");
        assertEquals(this.txt2,store.getDocument(uri2).getDocumentTxt(),"failed to return correct text");
    }
    
    //Test interaction with the trie
    @Test
    public void testSearch() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        String txt4 = "This is a test. This is a test.";
        String txt3 = "This is a test.";
        String txt2 = "this this this";
        store.putDocument(new ByteArrayInputStream(txt4.getBytes()), this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(txt3.getBytes()), this.uri2, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(txt2.getBytes()), this.uri3, DocumentStore.DocumentFormat.TXT);
        Document doc1 = store.getDocument(this.uri1);
        Document doc2 = store.getDocument(this.uri2);
        Document doc3 = store.getDocument(this.uri3);
        List<Document> list1 = store.search("this");
        List<Document> expected = List.of(doc3, doc1, doc2);
        assertEquals(expected, list1);
    }
    @Test
    public void testSearchByPrefix() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        String txt2 = "the the the ";       
        String txt3 = "Theater thpwigfhgpiegv friend aoghag";
        String txt4 = "t ";
        store.putDocument(new ByteArrayInputStream(txt2.getBytes()), this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(txt3.getBytes()), this.uri2, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(txt4.getBytes()), this.uri3, DocumentStore.DocumentFormat.TXT);
        Document doc1 = store.getDocument(this.uri1);
        Document doc2 = store.getDocument(this.uri2);
        Document doc3 = store.getDocument(this.uri3);
        
        List<Document> list1 = store.searchByPrefix("t");
        List<Document> expected = List.of(doc1, doc2, doc3);
        assertEquals(expected, list1);
    }
    
    @Test
    public void testDeleteAll() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        String txt4 = "This is a test. This is a test.";
        String txt3 = "This is a test.";
        String txt2 = "this this this";
        store.putDocument(new ByteArrayInputStream(txt4.getBytes()), this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(txt3.getBytes()), this.uri2, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(txt2.getBytes()), this.uri3, DocumentStore.DocumentFormat.TXT);
        assertNotNull(store.getDocument(this.uri1));
        assertNotNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        Set<URI> set = store.deleteAll("this");
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri3));
        assertEquals(0, store.search("this").size());
        assertEquals(3,set.size());
    }
    
    @Test
    public void testDeleteAllWithPrefix() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        String txt2 = "the the";       
        String txt3 = "Theater t thpwigfhgpiegv";
        String txt4 = "t t";
        store.putDocument(new ByteArrayInputStream(txt2.getBytes()), this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(txt3.getBytes()), this.uri2, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(txt4.getBytes()), this.uri3, DocumentStore.DocumentFormat.TXT);
        assertNotNull(store.getDocument(this.uri1));
        assertNotNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        Set<URI> set = store.deleteAllWithPrefix("the");
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertEquals(1, store.searchByPrefix("t").size());
        assertEquals(2,set.size());
    }
    
    @Test
    public void testUndoDeleteAll() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        String txt2 = "the the";       
        String txt3 = "Theater the t thpwigfhgpiegv";
        String txt4 = "t t";
        store.putDocument(new ByteArrayInputStream(txt2.getBytes()), this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(txt3.getBytes()), this.uri2, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(txt4.getBytes()), this.uri3, DocumentStore.DocumentFormat.TXT);
        store.deleteAll("the");
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        store.undo();
        assertNotNull(store.getDocument(this.uri1));
        assertNotNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
    }
    
    @Test
    public void testUndoURIDelete() throws Exception {
        //Tests an individual undo call
        //Test removing a command set only when all commands are removed
        DocumentStore store = new DocumentStoreImpl();
        String txt2 = "the the";       
        String txt3 = "Theater the t thpwigfhgpiegv";
        String txt4 = "t t";
        store.putDocument(new ByteArrayInputStream(txt2.getBytes()), this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(txt3.getBytes()), this.uri2, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(txt4.getBytes()), this.uri3, DocumentStore.DocumentFormat.TXT);
        store.deleteAll("the");
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        store.undo(this.uri1);
        assertNotNull(store.getDocument(this.uri1));
        store.undo(this.uri2);
        assertNotNull(store.getDocument(this.uri2));
    }
    
    @Test
    public void testDeleteFail() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        store.deleteDocument(this.uri1);
        String txt3 = "Theater the t thpwigfhgpiegv";
        store.putDocument(new ByteArrayInputStream(txt3.getBytes()), this.uri2, DocumentStore.DocumentFormat.TXT);
        store.undo(this.uri1);
    }
    @Test
    public void testReHeapifyGetDoc () throws IOException {
        //tests doing an action to smallest number
        DocumentStore store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()),this.uri3, DocumentStore.DocumentFormat.TXT);
        store.getDocument(uri1);
    }
    
    @Test
    public void testInsertDocWhenFullDoc () throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        store.setMaxDocumentCount(2);
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
        assertEquals(this.txt1,store.getDocument(this.uri1).getDocumentTxt(),"failed to return correct text");
        assertEquals(this.txt2,store.getDocument(uri2).getDocumentTxt(),"failed to return correct text");
        //puts a 3rd document which should put the store over the limit and delete the oldest document
        store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()),this.uri3, DocumentStore.DocumentFormat.TXT);
        assertEquals(this.txt2,store.getDocument(this.uri2).getDocumentTxt(),"failed to return correct text");
        assertEquals(this.txt3,store.getDocument(uri3).getDocumentTxt(),"failed to return correct text");
        assertNull(store.getDocument(uri1), "failed to return correct text");
    }
    @Test
    public void testInsertDocWhenFullDoc2 () throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        store.setMaxDocumentCount(2);
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
        assertEquals(this.txt1,store.getDocument(this.uri1).getDocumentTxt(),"failed to return correct text");
        assertEquals(this.txt2,store.getDocument(uri2).getDocumentTxt(),"failed to return correct text");
        //makes doc with uri1 no longer the oldest document, but uri2
        store.getDocument(uri1);
        //puts a 3rd document which should put the store over the limit and delete the oldest document
        store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()),this.uri3, DocumentStore.DocumentFormat.TXT);
        assertEquals(this.txt1,store.getDocument(this.uri1).getDocumentTxt(),"failed to return correct text");
        assertEquals(this.txt3,store.getDocument(uri3).getDocumentTxt(),"failed to return correct text");
        assertNull(store.getDocument(uri2), "failed to return correct text");
    }
    
    @Test
    public void testInsertDocWhenFullByte () throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        store.setMaxDocumentBytes(90);
        store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()),this.uri3, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
        assertEquals(this.txt3,store.getDocument(this.uri3).getDocumentTxt(),"failed to return correct text");
        assertEquals(this.txt2,store.getDocument(uri2).getDocumentTxt(),"failed to return correct text");
        //puts a document which should put the byte count over limit and require deletion of 2 documents
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        assertEquals(this.txt1,store.getDocument(this.uri1).getDocumentTxt(),"failed to return correct text");
        assertNull(store.getDocument(uri3),"failed to return correct text");
        assertNull(store.getDocument(uri2), "failed to return correct text");
        //undos the previous put
        store.undo();
        assertNull(store.getDocument(uri1), "failed to return correct text");
        assertNull(store.getDocument(uri2), "failed to return correct text");
        assertNull(store.getDocument(uri3), "failed to return correct text");
    }
    
    @Test
    public void testUndoPutOverLimit () throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
        store.deleteDocument(this.uri1);
        store.setMaxDocumentCount(1);
        store.undo();
        assertEquals(this.txt1,store.getDocument(this.uri1).getDocumentTxt(),"failed to return correct text");
        assertNull(store.getDocument(uri2),"failed to return correct text");
    }
    
    @Test
    public void testUndoPutOverLimitByte () throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
        store.deleteDocument(this.uri1);
        store.setMaxDocumentBytes(90);
        store.undo();
        assertEquals(this.txt1,store.getDocument(this.uri1).getDocumentTxt(),"failed to return correct text");
        assertNull(store.getDocument(uri2),"failed to return correct text");
    }
    
    @Test
    public void testCommandDelete () throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(txt1.getBytes()), this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(txt3.getBytes()), this.uri3, DocumentStore.DocumentFormat.TXT);
        store.deleteAll("of");
        store.putDocument(new ByteArrayInputStream(txt1.getBytes()), this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(txt3.getBytes()), this.uri3, DocumentStore.DocumentFormat.TXT);
        
        store.setMaxDocumentCount(2);
        store.putDocument(new ByteArrayInputStream(txt2.getBytes()), this.uri2, DocumentStore.DocumentFormat.TXT);
    }

    
    
    
}