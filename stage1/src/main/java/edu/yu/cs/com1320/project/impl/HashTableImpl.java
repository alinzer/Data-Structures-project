package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.HashTable;

public class HashTableImpl <Key, Value> implements HashTable<Key,Value> {
    private class Entry<Key, Value> {
        private Key key;
        private Value value;     
        private Entry<Key,Value> next;
        private Entry<Key,Value> previous;
        
        private Entry(Key k, Value v) {
            if (k == null) {
                throw new IllegalArgumentException();
            }
            this.key = k;
            this.value = v;
        }
    }
    // Now the actual class HashTableImpl starts  
    private Entry<Key,Value>[] hTable;
    
    public HashTableImpl() {
        this.hTable = new Entry[5];
    }

    private int hashFunction(Key key) {
        return (key.hashCode() & 0x7fffffff) % this.hTable.length;
    }
     
    /**
     * @param k the key whose value should be returned
     * @return the value that is stored in the HashTable for k, or null if there is no such key in the table
     */
    @Override
    public Value get(Key k) {
        if (k == null) {
            return null;
        }
        int index = this.hashFunction(k);
        Entry<Key,Value> current = this.hTable[index];
        while (current != null) {
            if (current.key.equals(k)) {
                return current.value;
            }
            current =  current.next;
        }
        return null;     
    }

    /**
     * @param k the key at which to store the value
     * @param v the value to store.
     * To delete an entry, put a null value.
     * @return if the key was already present in the HashTable, return the previous value stored for the key. If the key was not already present, return null.
     */
    
    @Override
    public Value put(Key k, Value v) {        
        int index = this.hashFunction(k);
        Entry<Key,Value> current = this.hTable[index];
        Value oldValue = this.get(k);
        Entry<Key, Value> putEntry = new Entry<Key, Value> (k, v);
        if (v == null) {
            this.delete(k);
            return oldValue;           
        }
        if (current == null) {
            this.hTable[index] = putEntry;
            return null;
        }
        else {
            while (current != null){
                if (this.currentUriEquality(k, current, putEntry, index)) {                                  
                    break;
                }
                else if (current.next == null) {
                    putEntry.previous = current;
                    current.next = putEntry;
                    break;
                }
                current = current.next;
            }
            return oldValue;
        }
    }
    
    private void delete(Key k) {
        int index = this.hashFunction(k);
        Entry<Key,Value> current = this.hTable[index];     
        while (current != null) {
            if (current.key.equals(k)) {                  
                if (current.previous == null) {
                    this.hTable[index] = null;
                }
                else {
                    current.previous.next = current.next;
                }
                return;
            }
            current = current.next;
        }
    }
    
    private boolean currentUriEquality (Key k, Entry <Key,Value> current, Entry <Key,Value> putEntry, int index) {
        if (current.key.equals(k)) {           
            putEntry.previous = current.previous;
            putEntry.next = current.next;
            if (current.previous == null) {
                this.hTable[index] = putEntry;
            }
            else {
                putEntry.previous.next = putEntry;  
            }                         
            return true;
        }
        return false;
    }
    
    /*private void printTable() {
        for (int i = 0; i < 5; i++) {
            if (this.table[i] != null) {
                DocumentImpl doc = (DocumentImpl)this.table[i].value;
                System.out.println(doc.getDocumentTxt());
                if(this.table[i].next != null) {
                    DocumentImpl doc2 = (DocumentImpl)this.table[i].next.value;
                    System.out.println(doc.getDocumentTxt() + " : " + doc2.getDocumentTxt());
                    if(this.table[i].next.next != null) {
                        DocumentImpl doc3 = (DocumentImpl)this.table[i].next.next.value;
                        System.out.println(doc.getDocumentTxt() + " : " + doc2.getDocumentTxt() + " : " + doc3.getDocumentTxt());
                        if(this.table[i].next.next.next != null) {
                            DocumentImpl doc4 = (DocumentImpl)this.table[i].next.next.next.value;
                            System.out.println(doc.getDocumentTxt() + " : " + doc2.getDocumentTxt() + " : " + doc3.getDocumentTxt() + " : " + doc4.getDocumentTxt());
                            
                        }
                    }
                }
            }
            else {
                System.out.println(this.table[i]);
            }                   
        }       
    }*/
    
}