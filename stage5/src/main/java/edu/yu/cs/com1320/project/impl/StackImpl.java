package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Command;
import edu.yu.cs.com1320.project.CommandSet;
import edu.yu.cs.com1320.project.GenericCommand;
// import edu.yu.cs.com1320.project.Command;
import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.Undoable;

public class StackImpl<T> implements Stack<T>{
    private class Entry<T> {
        private T command;
        private Entry<T> next;
        
        private Entry (T element) {
            this.command = element;
            this.next = null;
        }
    }
    private Entry<T> head;
    private int size;
    
    public StackImpl(){        
        this.head = new Entry<>(null);
        this.size = 0;
    }
    
    /**
     * @param element object to add to the Stack
     */
    @Override
    public void push(T element) {
        Entry<T> current = new Entry<>(element);
        current.next = head;
        head = current;
        this.size++;
    }
    
    /**
     * removes and returns element at the top of the stack
     * @return element at the top of the stack, null if the stack is empty
     */
    @Override
    public T pop() {
        Entry<T> valueToReturn = new Entry<>(null);
        valueToReturn = this.head;
        this.head = this.head.next;
        this.size--;
        return valueToReturn.command;
    }
    
    /**
     *
     * @return the element at the top of the stack without removing it
     */
    @Override
    public T peek() {
        return this.head.command;
    }
    /**
     *
     * @return how many elements are currently in the stack
     */
    @Override
    public int size() {
        return this.size;
    }   
}
