package edu.yu.cs.com1320.project.stage3;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;

import edu.yu.cs.com1320.project.impl.TrieImpl;
import org.junit.jupiter.api.Test;

public class TrieAPITest {
    
    @Test
    public void interfaceCount() {//tests that the class only implements one interface and its the correct one
        @SuppressWarnings("rawtypes")
        Class[] classes = TrieImpl.class.getInterfaces();
        assertTrue(classes.length == 1);
        assertTrue(classes[0].getName().equals("edu.yu.cs.com1320.project.Trie"));
    }
    
    @Test
    public void methodCount() {
        Method[] methods = TrieImpl.class.getDeclaredMethods();
        int publicMethodCount = 0;
        for (Method method : methods) {
            if (Modifier.isPublic(method.getModifiers())) {
                publicMethodCount++;
            }
        }
        assertTrue(publicMethodCount == 6);
    }
    
    @Test
    public void fieldCount() {
        Field[] fields = TrieImpl.class.getFields();
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
        Class[] classes = TrieImpl.class.getClasses();
        assertTrue(classes.length == 0);
    }
    
    @Test
    public void constructorExists() {
        new TrieImpl<String>();
    }
}
