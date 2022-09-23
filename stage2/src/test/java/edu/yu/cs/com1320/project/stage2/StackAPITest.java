package edu.yu.cs.com1320.project.stage2;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;

import edu.yu.cs.com1320.project.impl.StackImpl;
import org.junit.jupiter.api.Test;

public class StackAPITest {
    
    @Test
    public void interfaceCount() {//tests that the class only implements one interface and its the correct one
        @SuppressWarnings("rawtypes")
        Class[] classes = StackImpl.class.getInterfaces();
        assertTrue(classes.length == 1);
        assertTrue(classes[0].getName().equals("edu.yu.cs.com1320.project.Stack"));
    }
    
    @Test
    public void methodCount() {
        Method[] methods = StackImpl.class.getDeclaredMethods();
        int publicMethodCount = 0;
        for (Method method : methods) {
            if (Modifier.isPublic(method.getModifiers())) {
                publicMethodCount++;
            }
        }
        assertTrue(publicMethodCount == 4);
    }
    
    @Test
    public void fieldCount() {
        Field[] fields = StackImpl.class.getFields();
        int publicFieldCount = 0;
        for (Field field : fields) {
            if (Modifier.isPublic(field.getModifiers())) {
                publicFieldCount++;
            }
        }
        assertTrue(publicFieldCount == 0);
    }
    
    @Test
    public void subClassCount() {
        @SuppressWarnings("rawtypes")
        Class[] classes = StackImpl.class.getClasses();
        assertTrue(classes.length == 0);
    }
    
    @Test
    public void constructorExists() {
        new StackImpl<String>();
    }
    
    @Test
    public void pushExists() throws URISyntaxException{
        try {
            new StackImpl<String>().push("Hello");
        } catch (Exception e) {}//catch any run time error this input might cause. This is meant to test method existence, not correctness
    }
    
    @Test
    public void popExists() throws URISyntaxException{
        try {
            new StackImpl<String>().pop();
        } catch (Exception e) {}//catch any run time error this input might cause. This is meant to test method existence, not correctness
    }
    
    @Test
    public void peekExists() throws URISyntaxException{
        try {
            new StackImpl<String>().peek();
        } catch (Exception e) {}//catch any run time error this input might cause. This is meant to test method existence, not correctness
    }
    
    @Test
    public void sizeExists() throws URISyntaxException{
        try {
            new StackImpl<String>().size();
        } catch (Exception e) {}//catch any run time error this input might cause. This is meant to test method existence, not correctness
    }
    
}
