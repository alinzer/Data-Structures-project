package edu.yu.cs.com1320.project.stage3;

import edu.yu.cs.com1320.project.Utils;
import edu.yu.cs.com1320.project.stage4.impl.DocumentImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class DocumentImplTest {
    private URI textUri;
    private String textString;

    private URI binaryUri;
    private byte[] binaryData;

    @BeforeEach
    public void setUp() throws Exception {
        this.textUri = new URI("http://edu.yu.cs/com1320/txt");
        this.textString = "This is text content. Lots of it.";

        this.binaryUri = new URI("http://edu.yu.cs/com1320/binary");
        this.binaryData = "This is a PDF, brought to you by Adobe.".getBytes();
    }

    @Test
    public void testGetTextDocumentAsTxt() {
        DocumentImpl textDocument = new DocumentImpl(this.textUri, this.textString);
        assertEquals(this.textString, textDocument.getDocumentTxt());
    }

    @Test
    public void testGetDocumentBinaryData() {
        DocumentImpl binaryDocument = new DocumentImpl(this.binaryUri, this.binaryData);
        assertArrayEquals(this.binaryData,binaryDocument.getDocumentBinaryData());
    }

    @Test
    public void testGetTextDocumentTextHashCode() {
        DocumentImpl textDocument = new DocumentImpl(this.textUri, this.textString);
        int code = Utils.calculateHashCode(this.textUri, this.textString,null);
        assertEquals(code, textDocument.hashCode());
    }

    @Test
    public void testGetBinaryDocumentTextHashCode() {
        DocumentImpl binaryDocument = new DocumentImpl(this.binaryUri, this.binaryData);
        int code = Utils.calculateHashCode(this.binaryUri, null, this.binaryData);
        assertEquals(code, binaryDocument.hashCode());
    }

    @Test
    public void testGetTextDocumentKey() {
        DocumentImpl textDocument = new DocumentImpl(this.textUri, this.textString);
        assertEquals(this.textUri, textDocument.getKey());
    }

    @Test
    public void testGetBinaryDocumentKey() {
        DocumentImpl binaryDocument = new DocumentImpl(this.binaryUri, this.binaryData);
        assertEquals(this.binaryUri, binaryDocument.getKey());
    }
    
    @Test
    public void testWordCount() {
        this.textString = "This it text content. Lots of it.";
        DocumentImpl textDocument = new DocumentImpl(this.textUri, this.textString);
        assertEquals(2, textDocument.wordCount("it"));
        assertEquals(0, textDocument.wordCount("is"));
    }
    
    @Test
    public void testWordCountBinaryDoc() {
        DocumentImpl binaryDocument = new DocumentImpl(this.binaryUri, this.binaryData);
        assertEquals(0, binaryDocument.wordCount("it"));
        Set<String> words = binaryDocument.getWords();
    }
    
    @Test
    public void testGetWords() {
        DocumentImpl textDocument = new DocumentImpl(this.textUri, this.textString);
        Set<String> words = textDocument.getWords();
        assertEquals(7, words.size());
        assertTrue(words.contains("this"));
        assertTrue(words.contains("is"));
        assertTrue(words.contains("text"));
        assertTrue(words.contains("content"));
        assertTrue(words.contains("lots"));
        assertTrue(words.contains("of"));
        assertTrue(words.contains("it"));
    }
    
}