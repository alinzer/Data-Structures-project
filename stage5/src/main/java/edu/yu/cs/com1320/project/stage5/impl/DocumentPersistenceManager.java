package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;
import jakarta.xml.bind.DatatypeConverter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;


///doens't work with binary= big probem. 


/**
 * created by the document store and given to the BTree via a call to BTree.setPersistenceManager
 */
public class DocumentPersistenceManager implements PersistenceManager<URI, Document> {
    // public static void main(String[] args) throws Exception {
    //     URI uri = new URI("http://edu.yu.cs/com1320/txt");
    //     DocumentImpl doc = new DocumentImpl(uri , "This is text content. Lots of it.");
    //     DocumentPersistenceManager pm = new DocumentPersistenceManager(new File(System.getProperty("user.dir")));
    //     pm.serialize(uri, doc);
    //     Document docu = pm.deserialize(uri);
    //     int h = 4;
    // }
    
    private File baseDir;
    private Gson gson;
    
    public DocumentPersistenceManager(File baseDir){
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        this.baseDir = baseDir;
        this.gson = new GsonBuilder()
            .registerTypeAdapter(DocumentImpl.class, new DocumentSerializer())
            .registerTypeAdapter(Document.class, new DocumentDeserializer())
            .setPrettyPrinting()
            .create();
    }

    @Override
    public void serialize(URI uri, Document val) throws IOException {
        String uriString = uri.getSchemeSpecificPart();
        new File (this.baseDir + uriString).mkdirs();
        File tmp = new File (this.baseDir + uriString, ".json");
        tmp.createNewFile();
        FileWriter fileW = new FileWriter (this.baseDir + uriString + ".json");
        fileW.write(gson.toJson(val));
        fileW.close();
    }

    @Override
    public Document deserialize(URI uri) throws IOException {
        if (!baseDir.exists()) {
            throw new IllegalArgumentException ("No document exists at requested uri to deserialize");
        }
        String uriString = uri.getSchemeSpecificPart() + ".json";
        BufferedReader br = new BufferedReader(new FileReader(this.baseDir + uriString));
        Document doc = this.gson.fromJson(br, Document.class);
        return doc;
    }

    @Override
    public boolean delete(URI uri) throws IOException {
        String uriString = uri.getSchemeSpecificPart();
        File tmp = new File (this.baseDir + uriString, ".json");
        return tmp.delete();
    }
    
    public class DocumentSerializer implements JsonSerializer<Document> {
        @Override
        public JsonElement serialize(Document doc, Type type, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("uri", doc.getKey().toString());
            if (doc.getDocumentTxt() != null) {
                obj.addProperty("text", doc.getDocumentTxt());
                obj.addProperty("countOfWords", gson.toJson(doc.getWordMap()));
            }
            else {
                obj.addProperty("binaryData", gson.toJson(DatatypeConverter.printBase64Binary(doc.getDocumentBinaryData())));
            }
            return obj;
        }
    }
    
    private class DocumentDeserializer implements JsonDeserializer<Document> {
        @Override
        public Document deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            Document doc; 
            URI uri = URI.create(jsonObject.get("uri").getAsString());
            if (jsonObject.get("text") != null) {
                String text = jsonObject.get("text").getAsString();
                Map<String,Integer> wordMap = new Gson().fromJson(jsonObject.get("countOfWords").getAsString(), Map.class);
                doc = new DocumentImpl(uri, text, wordMap);
            }
            else {
                byte[] binaryData = DatatypeConverter.parseBase64Binary(jsonObject.get("binaryData").getAsString());
                doc = new DocumentImpl(uri ,binaryData);
            }
            return doc;
        }
    }
    
}