package edu.yu.cs.com1320.project.stage3;

import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.impl.StackImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StackImplTest {
    
    private Stack<String> stack;
    
    @BeforeEach
    public void initStack() {
        this.stack = new StackImpl<String>();
    }
    
    @Test
    public void testPopEmpty() {
        assertEquals(null,this.stack.pop());
    }
    
    @Test
    public void testPeekEmpty() {
        assertEquals(null,this.stack.peek());
    }
    
    @Test
    public void testPushandPeek() {
        this.stack.push("Hello");
        assertEquals("Hello",this.stack.peek());
    }
    @Test
    public void testPushandPop() {
        this.stack.push("Hello");
        assertEquals("Hello",this.stack.pop());
    }
    @Test
    public void testMultiplePush() {
        this.stack.push("Hello");
        this.stack.push("Hey");
        this.stack.push("Hi");
        assertEquals("Hi",this.stack.pop());
        assertEquals("Hey",this.stack.pop());
        assertEquals("Hello",this.stack.pop());
    }
    @Test
    public void testStackSize() {
        assertEquals(0, this.stack.size());
        this.stack.push("Hello");
        assertEquals(1, this.stack.size());
        this.stack.push("Hey");
        assertEquals(2, this.stack.size());
        this.stack.push("Hi");
        assertEquals(3, this.stack.size());
        this.stack.peek();
        assertEquals(3, this.stack.size());
        this.stack.pop();
        assertEquals(2, this.stack.size());
        this.stack.pop();
        assertEquals(1, this.stack.size());
        this.stack.pop();
        assertEquals(0, this.stack.size());
    }
    
}
