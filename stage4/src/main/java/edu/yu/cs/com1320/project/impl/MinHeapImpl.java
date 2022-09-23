package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.MinHeap;

import java.lang.Comparable;
import java.util.Arrays;
import java.util.NoSuchElementException;

public class MinHeapImpl<E extends Comparable<E>> extends MinHeap<E> {
    public MinHeapImpl() {
        this.elements = (E[]) new Comparable[10];
    }

    @Override
    public void reHeapify(E element) {
        if (this.getArrayIndex(element) == -1) {
            throw new NoSuchElementException("reheapify was called on element not in the heap");
        }
        int correct = this.elementInCorrectLocation(element);
        if (correct == 0) {
            return;
        }
        if (correct == 1) {
            this.upHeap((this.getArrayIndex(element)));
            return;
        }
        this.downHeap((this.getArrayIndex(element)));
    }
    
    private int elementInCorrectLocation (E element) {
        int elementIndex = this.getArrayIndex(element); 
        int leftChild = 2 * elementIndex;
        int rightChild = 2 * elementIndex + 1;
        int parent = elementIndex / 2;
        //Checks to see if the element is currently in the correct location
        if (this.elements[parent] != null && isGreater(parent, elementIndex)) {
            return 1;
        }
        if ((leftChild < this.elements.length && this.elements[leftChild] != null && isGreater(elementIndex, leftChild)) || ((rightChild < this.elements.length && this.elements[rightChild] != null && isGreater(elementIndex, rightChild)))){
            return -1;
        }
        return 0;
    }

    @Override
    protected int getArrayIndex(E element) {
        for (int i = 1; i < this.elements.length; i++) {
            if (this.elements[i].compareTo(element) == 0) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void doubleArraySize() {
        this.elements = Arrays.copyOf(this.elements, this.elements.length * 2);
    }
    
}
