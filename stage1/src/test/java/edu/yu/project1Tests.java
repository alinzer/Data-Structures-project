package edu.yu;

import org.junit.jupiter.api.Test;
// import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

// import edu.yu.cs.com1320.project.stage1.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage1.impl.DocumentStoreImpl;
import edu.yu.cs.com1320.project.stage1.DocumentStore.DocumentFormat;


public class project1Tests {
    
    class IncorrectBehaviorException extends RuntimeException{
        public IncorrectBehaviorException(String message){
            super(message);
        }
    };  
  
    // 
    @Test
    public void putOneDocumentTest() throws URISyntaxException, IOException {      
        DocumentStoreImpl docStore = new DocumentStoreImpl();
        URI uri = this.putDoc(docStore, "1", "iStream1", DocumentFormat.TXT);
        assert(docStore.getDocument(uri) != null);
    }
    
    @Test
    public void fillDocStoreTest() throws URISyntaxException, IOException {
        DocumentStoreImpl docStore = new DocumentStoreImpl();
        URI[] uriA = this.fillDocStore(docStore);
        for (URI uri : uriA) {
            assert(docStore.getDocument(uri) != null);
        }
    }
    
    
    
    
    private URI putDoc (DocumentStoreImpl docStore, String uriString, String inputStreamString, DocumentFormat format) throws URISyntaxException, IOException {
        URI uri = new URI(uriString);
        InputStream iStream = new ByteArrayInputStream(inputStreamString.getBytes());
        docStore.putDocument(iStream, uri, format);     
        return uri;
    }
    
    private URI[] fillDocStore (DocumentStoreImpl docStore) throws URISyntaxException, IOException {
        URI uri1 = this.putDoc(docStore, "1", "iStream1", DocumentFormat.TXT);
        URI uri2 = this.putDoc(docStore, "2", "iStream2", DocumentFormat.TXT);
        URI uri3 = this.putDoc(docStore, "3", "iStream3", DocumentFormat.TXT);
        URI uri4 = this.putDoc(docStore, "4", "iStream4", DocumentFormat.TXT);
        URI uri5 = this.putDoc(docStore, "5", "iStream5", DocumentFormat.TXT);
        URI[] uriA = new URI[5];
        uriA[0] = uri1;
        uriA[1] = uri2;
        uriA[2] = uri3;
        uriA[3] = uri4;
        uriA[4] = uri5;
        return uriA;
    }   
}
