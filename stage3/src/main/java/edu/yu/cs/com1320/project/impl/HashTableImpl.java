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
            this.next = null;
            this.previous = null;
        }
    }
    
    // Now the actual class HashTableImpl starts  
    private Entry<Key,Value>[] hTable;
    private int currentEntries;
    private int tableSize;
    
    public HashTableImpl() {
        this.hTable = new Entry[5];
        this.currentEntries = 0;
        this.tableSize = 5;
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
        Value oldValue = this.get(k);
        if (v != null && oldValue == null && currentEntries > 0.75 * tableSize) {
            resize();
        }      
        int index = this.hashFunction(k);
        Entry<Key,Value> current = this.hTable[index];
        Entry<Key, Value> putEntry = new Entry<> (k, v);
        if (v == null) {
            return this.nullValue(k, oldValue);
        }
        else if (current == null) {
            this.hTable[index] = putEntry;
            this.currentEntries++;
            return oldValue;
        }
        else {
            while (current != null){
                if (this.currentKeyEquality(k, current, putEntry, index)) {
                    break;
                }
                else if (current.next == null) {
                    this.ifCurrentNextIsNull(current, putEntry);
                    break;
                }
                current = current.next;
            }
            return oldValue;
        }       
    }
    
    private void ifCurrentNextIsNull (Entry<Key,Value> current, Entry<Key,Value> putEntry) {
        putEntry.previous = current;
        current.next = putEntry;
        this.currentEntries++; 
    }
    
    private Value nullValue (Key k, Value oldValue) {
        if (this.delete(k)) {
            return oldValue;
        }  
        else {
            return null;
        } 
    }
    
    private boolean delete(Key k) {
        int index = this.hashFunction(k);
        Entry<Key,Value> current = this.hTable[index];     
        while (current != null) {
            if (current.key.equals(k)) {                  
                if (current.previous == null) {
                    this.hTable[index] = current.next;
                }
                else {
                    if (current.next != null) {
                        current.next.previous = current.previous;
                    }
                    current.previous.next = current.next;
                }
                this.currentEntries--;
                return true;
            }
            current = current.next;
        }
        return false;
    }
    
    private boolean currentKeyEquality (Key k, Entry <Key,Value> current, Entry <Key,Value> putEntry, int index) {
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
    
    private void resize() {
        Entry[] entries = new Entry[this.currentEntries];
        int currentInd = 0;
        for (int i = 0; i < this.tableSize; i++) {
            Entry<Key,Value> current = this.hTable[i];
            while (current != null) {
                entries[currentInd] = current;
                currentInd++;
                current = current.next;
            }
        } 
        this.hTable = new Entry[this.tableSize * 2];
        this.currentEntries = 0;
        for (int i = 0; i < entries.length; i++) {
            Key key = (Key)entries[i].key;
            Value value = (Value)entries[i].value;
            this.put(key, value);
        }
        this.tableSize = tableSize * 2;
    }
}