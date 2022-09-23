import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.stage2.DocumentStore.DocumentFormat;
import edu.yu.cs.com1320.project.stage2.impl.*;
public class Test {
    public static void main(String[] args) throws URISyntaxException {
        // DocumentStoreImpl hash = new DocumentStoreImpl();
        int c = 0;
        HashTableImpl <Integer,String> hash = new HashTableImpl();
        for (int i = 1; i < 250000; i++) {
            hash.put(i,"Value");
        }
        for (int i = 250001; i < 500000; i++) {
            hash.put(i,"Value2");
        }
        for (int i = 1; i < 500000; i++) {
            if (hash.get(i) != null) {
                c++;
            }
        }
        System.out.println(c);
        
        /*String str1 = "1";
        URI tst1 = new URI(str1);
        String myString1 = "This is one";
        InputStream is1 = new ByteArrayInputStream(myString1.getBytes());
        // String str2 = "2";
        // URI tst2 = new URI(str2);
        // String myString2 = "This is two";
        // InputStream is2 = new ByteArrayInputStream(myString2.getBytes());
        // String str3 = "3";
        // URI tst3 = new URI(str3);
        // String myString3 = "This is three";
        // InputStream is3 = new ByteArrayInputStream(myString3.getBytes());
        // String str4 = "4";
        // URI tst4 = new URI(str4);
        // String myString4 = "This is four";
        // InputStream is4 = new ByteArrayInputStream(myString4.getBytes());
        // String str5 = "5";
        // URI tst5 = new URI(str5);
        // String myString5 = "This is five";
        // InputStream is5 = new ByteArrayInputStream(myString5.getBytes());
        String str6 = "6";
        URI tst6 = new URI(str6);
        String myString6 = "This is six";
        InputStream is6 = new ByteArrayInputStream(myString6.getBytes());
        // String str7 = "7";
        // URI tst7 = new URI(str7);
        // String myString7 = "This is seven";
        // InputStream is7 = new ByteArrayInputStream(myString7.getBytes());
        
        // String str8 = "8";
        // URI tst8 = new URI(str8);
        // String myString8 = "This is eight";
        // InputStream is8 = new ByteArrayInputStream(myString8.getBytes());
        
        // String str9 = "9";
        // URI tst9 = new URI(str9);
        // String myString9 = "This is nine";
        // InputStream is9 = new ByteArrayInputStream(myString9.getBytes());
        
        // String str10 = "10";
        // URI tst10 = new URI(str10);
        // String myString10 = "This is ten";
        // InputStream is10 = new ByteArrayInputStream(myString10.getBytes());
        
        // String str11 = "11";
        // URI tst11 = new URI(str11);
        // String myString11 = "This is eleven";
        // InputStream is11 = new ByteArrayInputStream(myString11.getBytes());
        String str12 = "12";
        URI tst12 = new URI(str12);
        String myString12 = "This is twelve";
        InputStream is12 = new ByteArrayInputStream(myString12.getBytes());
        // String str13 = "13";
        // URI tst13 = new URI(str13);
        // String myString13 = "This is thirteen";
        // InputStream is13 = new ByteArrayInputStream(myString13.getBytes());
        // String str14 = "14";
        // URI tst14 = new URI(str14);
        // String myString14 = "This is fourteen";
        // InputStream is14 = new ByteArrayInputStream(myString14.getBytes());
        // String str15 = "15";
        // URI tst15 = new URI(str15);
        // String myString15 = "This is fifteen";
        // InputStream is15 = new ByteArrayInputStream(myString15.getBytes());
        // String str16 = "16";
        // URI tst16 = new URI(str16);
        // String myString16 = "This is sixteen";
        // InputStream is16 = new ByteArrayInputStream(myString16.getBytes());
        // final byte[] CDRIVES = new byte[] { (byte)0xe0, 0x4f, (byte)0xd0,
        //     0x20, (byte)0xea, 0x3a, 0x69, 0x10, (byte)0xa2, (byte)0xd8, 0x08, 0x00, 0x2b,
        //     0x30, 0x30, (byte)0x9d };
        // InputStream is17 = new ByteArrayInputStream(CDRIVES);
        // // String str19 = "";
        // URI j = null;
        // DocumentImpl doc = new DocumentImpl(j, myString16);
        
        try {
            hash.putDocument(is1,tst1, DocumentFormat.TXT);
            hash.putDocument(is6,tst6, DocumentFormat.TXT);
            hash.putDocument(is12,tst12, DocumentFormat.TXT);
            hash.deleteDocument(tst1);
            System.out.println("woihfeqf");
            hash.undo(tst1);
            System.out.println("woihfeqf");
            
            /*hash.putDocument(is1,tst1, DocumentFormat.TXT);
            System.out.println(hash.stack.size());
            hash.putDocument(is2,tst2, DocumentFormat.TXT);
            System.out.println(hash.stack.size());
            hash.putDocument(is3,tst3, DocumentFormat.TXT);
            System.out.println(hash.stack.size());
            // System.out.println(hash.stack.peek());
            System.out.println(hash.getDocument(tst1));
            System.out.println(hash.getDocument(tst2));
            System.out.println(hash.getDocument(tst3));
            
            // System.out.println(hash.getDocument(tst4));
            System.out.println();
            // hash.putDocument(is4,tst3, DocumentFormat.TXT);
            hash.deleteDocument(tst2);
            System.out.println(hash.stack.size());
            // System.out.println(hash.stack.peek());
            System.out.println(hash.getDocument(tst1));
            System.out.println(hash.getDocument(tst2));
            System.out.println(hash.getDocument(tst3));

            // System.out.println(hash.getDocument(tst4));
            
            hash.undo(tst2);
            System.out.println(hash.stack.size());
            // hash.undo(tst3);
            
            System.out.println();
            // System.out.println(hash.stack.peek());
            System.out.println(hash.getDocument(tst1));
            System.out.println(hash.getDocument(tst2));
            System.out.println(hash.getDocument(tst3));

            // System.out.println(hash.getDocument(tst4));
            hash.putDocument(is1,tst1, DocumentFormat.TXT);
            hash.putDocument(is6,tst6, DocumentFormat.TXT);
            hash.deleteDocument(tst1);
            hash.undo();*/
            // hash.putDocument(is7,tst7, DocumentFormat.TXT);
            // hash.putDocument(is8,tst8, DocumentFormat.TXT);
            // int temp = hash.store.hashFunction(tst5);
            // hash.putDocument(is9,tst9, DocumentFormat.TXT);
            // System.out.println(hash.store.get(tst5));
            // hash.putDocument(is10,tst10, DocumentFormat.TXT);
            // hash.putDocument(is11,tst11, DocumentFormat.TXT);
            // hash.putDocument(is12,tst12, DocumentFormat.TXT);
            // hash.putDocument(is13,tst13, DocumentFormat.TXT);
            // hash.putDocument(is14,tst14, DocumentFormat.TXT);
            // hash.putDocument(is15,tst15, DocumentFormat.TXT);
            // hash.putDocument(is16,tst15, DocumentFormat.TXT);
            // hash.putDocument(is16,tst16, DocumentFormat.TXT);
            // hash.putDocument(is17, tst1, DocumentFormat.BINARY);
            
            // hash.deleteDocument(tst4);
            
        /*} catch (IOException e) {
            e.printStackTrace();
        }
        // hash.deleteDocument(tst2);
        //hash.store.printTable();
        // System.out.println();
        // System.out.println(hash.deleteDocument(j));
        // hash.undo();
        // System.out.println(hash.getDocument(tst1).getDocumentTxt());
        // System.out.println(hash.getDocument(tst2).getDocumentTxt());
        // System.out.println(hash.getDocument(tst3).getDocumentTxt());
        // hash.store.printTable();
        */
    }

}