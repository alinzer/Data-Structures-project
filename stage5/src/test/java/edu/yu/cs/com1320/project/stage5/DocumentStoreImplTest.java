package edu.yu.cs.com1320.project.stage5;

import edu.yu.cs.com1320.project.Utils;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.DocumentStore;
import edu.yu.cs.com1320.project.stage5.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage5.impl.DocumentStoreImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
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
    public void testOverflow() throws IOException {
        DocumentStoreImpl store = new DocumentStoreImpl();
        store.setMaxDocumentCount(2);
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()),this.uri3, DocumentStore.DocumentFormat.TXT);
        //use debug to make sure doc1 got removed from heap and now has reference not val
        String uriString = uri1.getSchemeSpecificPart();
        File tmp = new File (new File(System.getProperty("user.dir")) + uriString, ".json");
        assertTrue(tmp.exists());
        store.getDocument(uri1);
        assertFalse(tmp.exists());
        
    }
    
    @Test
    public void testPutDisk() throws Exception {
        DocumentStoreImpl store = new DocumentStoreImpl();
        store.setMaxDocumentCount(1);
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
        store.setMaxDocumentCount(2);
        String uriString = uri1.getSchemeSpecificPart();
        File tmp = new File (new File(System.getProperty("user.dir")) + uriString, ".json");
        assertTrue(tmp.exists());
        store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        assertFalse(tmp.exists());
        
        
    }
    
}
