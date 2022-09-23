
package edu.yu.cs.com1320.project.impl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.yu.cs.com1320.project.Trie;

public class TrieImpl<Value> implements Trie<Value>{
    private static final int alphabetSize = 36; // extended ASCII
    private Node<Value> root;
    
    private static class Node<Value> {
        private Set<Value> val;
        private Node<Value>[] links = new Node[TrieImpl.alphabetSize];
        
        private Node() {
            this.val = new HashSet<>();
        }
    }
    
    public TrieImpl() {
        this.root = new Node<>();
    }
    
    /**
     * add the given value at the given key
     * @param key
     * @param val
     */
    @Override
    public void put(String key, Object val) {
        String lowerKey = key.toLowerCase();
        // deleteAll the value from this key
        if (val == null || key == null) {
            throw new IllegalArgumentException("no value or key entered");
        }
        else {
            this.root = put(this.root, lowerKey, val, 0);
        }
    }
    /**
     *
     * @param x
     * @param key
     * @param val
     * @param d
     * @return
     */
    private Node<Value> put(Node<Value> x, String key, Object val, int d) {
        //create a new node
        if (x == null) {
            x = new Node<Value>();
        }
        //we've reached the last node in the key,
        //set the value for the key and return the node
        if (d == key.length()) {
            x.val.add((Value)val);
            return x;
        }
        //proceed to the next node in the chain of nodes that
        //forms the desired key
        char c = key.charAt(d);
        int intC = (int)c;
        if (intC > 70) {
            intC = intC - 97;
        }
        else if (intC > 45) {
            intC = intC - 23;
        }
        
        x.links[intC] = this.put(x.links[intC], key, val, d + 1);
        return x;
    }

    /**
     * get all exact matches for the given key, sorted in descending order.
     * Search is CASE INSENSITIVE.
     * @param key
     * @param comparator used to sort  values
     * @return a List of matching Values, in descending order
     */
    @Override
    public List<Value> getAllSorted(String key, Comparator<Value> comparator) {
        if (comparator == null) {
            throw new IllegalArgumentException ("Comparator entered is null");
        }
        String lowerKey = key.toLowerCase();
        Node<Value> x = this.get(this.root, lowerKey, 0);
        if (x == null) {
            return new ArrayList<Value>();
        }
        List<Value> list = new ArrayList<Value>(x.val);
        Collections.sort(list, comparator);
        return list;
    }
    
    private Node<Value> get(Node<Value> x, String key, int d) {
        //link was null - return null, indicating a miss
        if (x == null) {
            return null;
        }
        //we've reached the last node in the key,
        //return the node
        if (d == key.length()) {
            return x;
        }
        //proceed to the next node in the chain of nodes that
        //forms the desired key
        char c = key.charAt(d);
        int intC = (int)c;
        if (intC > 70) {
            intC = intC - 97;
        }
        else if (intC > 45) {
            intC = intC - 23;
        }
        return this.get(x.links[intC], key, d + 1);
    }

    /**
     * get all matches which contain a String with the given prefix, sorted in descending order.
     * For example, if the key is "Too", you would return any value that contains "Tool", "Too", "Tooth", "Toodle", etc.
     * Search is CASE INSENSITIVE.
     * @param prefix
     * @param comparator used to sort values
     * @return a List of all matching Values containing the given prefix, in descending order
     */
    @Override
    public List<Value> getAllWithPrefixSorted(String prefix, Comparator<Value> comparator) {
        if (comparator == null) {
            throw new IllegalArgumentException ("Comparator entered is null");
        }
        String lowerPrefix = prefix.toLowerCase();
        Set<Value> values = new HashSet<Value>();
        List<Value> valueList = new ArrayList<Value>();
        // get the node of the given prefix
        Node<Value> x = this.get(this.root, lowerPrefix, 0);
        
        // get all values that lives under the given prefix
        if (x != null) {
            this.collect(x, new StringBuilder(lowerPrefix), values);
            valueList = new ArrayList<Value>(values);
        }
        Collections.sort(valueList, comparator);
        return valueList;
    }
    
    private void collect(Node<Value> x, StringBuilder prefix, Collection<Value> values) {
        if (x.val != null) {
           values.addAll(x.val);
        }
        
        for (char c = 0; c < TrieImpl.alphabetSize; c++) {
            int intC = (int)c;
            if (intC > 70) {
                intC = intC - 97;
            }
            else if (intC > 45) {
                intC = intC - 23;
            }
            if (x.links[intC] != null) {
                prefix.append(intC);
                this.collect(x.links[intC], prefix, values);
                prefix.deleteCharAt(prefix.length() - 1);
            }
        }
    }

    /**
     * Delete the subtree rooted at the last character of the prefix.
     * Search is CASE INSENSITIVE.
     * @param prefix
     * @return a Set of all Values that were deleted.
     */
    @Override
    public Set<Value> deleteAllWithPrefix(String prefix) {
        String lowerPrefix = prefix.toLowerCase();
        Set<Value> values = new HashSet<Value>();
        
        // get the node of the given prefix
        Node<Value> x = this.get(this.root, lowerPrefix, 0);
        
        if (x == null) {
            return values;
        }
        // get all values that lives under the given prefix that are going to be deleted
        this.collect(x, new StringBuilder(lowerPrefix), values);
        //Set the node of the prefix to null, which effectively deletes the entire subtree
        char removed = lowerPrefix.charAt(lowerPrefix.length() - 1);
        Node<Value> parent = this.get(this.root, lowerPrefix.substring(0, lowerPrefix.length() - 1), 0);
        int intC = (int)removed;
        if (intC > 70) {
            intC = intC - 97;
        }
        else if (intC > 45) {
            intC = intC - 23;
        }
        parent.links[intC] = null;
        return values;
    }

    /**
     * Delete all values from the node of the given key (do not remove the values from other nodes in the Trie)
     * @param key
     * @return a Set of all Values that were deleted.
     */
    @Override
    public Set<Value> deleteAll(String key) {
        String lowerKey = key.toLowerCase();
        Set<Value> deletedValues = new HashSet<>();
        this.root = this.deleteAll(this.root, lowerKey, 0, deletedValues);
        return deletedValues;
    }
    
    private Node<Value> deleteAll(Node<Value> x, String key, int d, Set<Value> deletedValues) {
        if (x == null) {
            return null;
        }
        //we're at the node to del - set the val to null
        if (d == key.length()) {
            deletedValues.addAll(x.val);
            x.val.clear();
        }
        //continue down the trie to the target node
        else {
            char c = key.charAt(d);
            int intC = (int)c;
            if (intC > 70) {
                intC = intC - 97;
            }
            else if (intC > 45) {
                intC = intC - 23;
            }
            x.links[intC] = this.deleteAll(x.links[intC], key, d + 1, deletedValues);
        }
        //this node has a val – do nothing, return the node
        if (x.val != null && x.val.size() != 0) {
            return x;
        }
        //remove subtrie rooted at x if it is completely empty	
        for (int c = 0; c < TrieImpl.alphabetSize; c++) {
            int intC = (int)c;
            if (intC > 70) {
                intC = intC - 97;
            }
            else if (intC > 45) {
                intC = intC - 23;
            }
            if (x.links[intC] != null) {
                return x; //not empty
            }
        }
        //empty - set this link to null in the parent
        return null;
    }

    /**
     * Remove the given value from the node of the given key (do not remove the value from other nodes in the Trie)
     * @param key
     * @param val
     * @return the value which was deleted. If the key did not contain the given value, return null.
     */
    @Override
    public Value delete(String key, Object val) {
        Value returnVal = null;
        String lowerKey = key.toLowerCase();
        Node<Value> x = this.get(this.root, lowerKey, 0);
        if (x == null) {
            return null;
        }
        for (Value value : x.val) {
            if (value.equals(val)){
                returnVal = value;
                x.val.remove(value);
                break;
            }
        }
        if (x.val != null && x.val.size() == 0) {
            Node<Value> hey = this.deleteAll(this.root, lowerKey, 0, new HashSet<Value>());
        }
        return returnVal;
    }
    
}
