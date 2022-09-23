package edu.yu.cs.com1320.project.stage4;

import edu.yu.cs.com1320.project.impl.MinHeapImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class MinHeapImplTest {
    private MinHeapImpl<Integer> heap;
    
    @BeforeEach
    public void initHeap() {
        this.heap = new MinHeapImpl<Integer>();
        
    }
    
    @Test
    public void testReHeapifyLower () {
        this.heap.insert(2);
        this.heap.insert(10);
        this.heap.insert(3);
        this.heap.insert(4);
        this.heap.insert(1);
        this.heap.insert(5);
        this.heap.insert(6);
        this.heap.insert(8);
        this.heap.insert(7);
        this.heap.insert(9);
        
    }
    
    
}
