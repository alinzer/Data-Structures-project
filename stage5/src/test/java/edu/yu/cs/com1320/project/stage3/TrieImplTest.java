package edu.yu.cs.com1320.project.stage3;

import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage5.impl.DocumentImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class TrieImplTest {
    private TrieImpl<Integer> trie = new TrieImpl<>();
    
    @BeforeEach
    public void initTrie() {
        this.trie.put("Key1", 1);
        this.trie.put("Key1", 2);
        this.trie.put("Key1", 8);
        this.trie.put("Key", 4);
        this.trie.put("Kangaroo", 9);
        this.trie.put("Kang", 5);
    }
    
    @Test
    public void testGetAllSorted() throws Exception {
        List<Integer> docs = this.trie.getAllSorted("Key1", Comparator.naturalOrder());
        List<Integer> temp = Arrays.asList(1, 2, 8);
        assertEquals(temp, docs);
        assertFalse(docs.contains(4));
        assertFalse(docs.contains(5));
        // test get on key that isn't in trie
        List<Integer> temp2 = new ArrayList<Integer>();
        List<Integer> docs2 = this.trie.getAllSorted("Key2", Comparator.naturalOrder());
        assertEquals(temp2, docs2);
    }
    
    @Test
    public void testGetAllWithPrefixSorted() throws Exception {
        List<Integer> docs = this.trie.getAllWithPrefixSorted("K", Comparator.naturalOrder());
        List<Integer> temp = Arrays.asList(1, 2, 4, 5, 8, 9);
        assertEquals(temp, docs);
        assertFalse(docs.contains(7));
        // test get on key that isn't in trie
        List<Integer> temp2 = new ArrayList<Integer>();
        List<Integer> docs2 = this.trie.getAllWithPrefixSorted("Key2", Comparator.naturalOrder());
        assertEquals(temp2, docs2);
    }
    
    @Test
    public void testDeleteAll() throws Exception {
        this.trie.deleteAll("Key1");
        List<Integer> docs = this.trie.getAllSorted("Key1", Comparator.naturalOrder());
        assertEquals(0, docs.size());
    }
    
    @Test
    public void testDelete() throws Exception {
        List<Integer> docs = this.trie.getAllWithPrefixSorted("K", Comparator.naturalOrder());
        assertEquals(6, docs.size());
        this.trie.delete("Kangaroo", 9);
        docs = this.trie.getAllWithPrefixSorted("K", Comparator.naturalOrder());
        // assertEquals(2, docs.size()); 
    }
    
    @Test
    public void testDeleteAllWithPrefix() throws Exception {
        List<Integer> docs = this.trie.getAllWithPrefixSorted("K", Comparator.naturalOrder());
        assertEquals(6, docs.size());
        this.trie.deleteAllWithPrefix("K");
        docs = this.trie.getAllWithPrefixSorted("K", Comparator.naturalOrder());
        assertEquals(0, docs.size());
    }
    
    @Test
    public void testDeleteAllAncestor() throws Exception {
        TrieImpl<Integer> pract = new TrieImpl<>();
        pract.put("A", 9);
        pract.put("Abcd", 9);
        pract.put("Abcd", 6);
        pract.deleteAll("abcd");
        List<Integer> docs = this.trie.getAllWithPrefixSorted("K", Comparator.naturalOrder());
    }
    
    @Test
    public void testDeleteAncestor() throws Exception {
        TrieImpl<Integer> pract = new TrieImpl<>();
        pract.put("A", 9);
        pract.put("Abcd", 9);
        pract.delete("abcd", 9);
        List<Integer> docs = this.trie.getAllWithPrefixSorted("K", Comparator.naturalOrder());
    }
    
}
