package edu.yu.cs.com1320.project.stage3;

import edu.yu.cs.com1320.project.impl.BTreeImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BTreeImplTest {

    private BTreeImpl<String,String> table;

    @BeforeEach
    public void initTable(){
        this.table = new BTreeImpl<>();
        this.table.put("Key1", "Value1");
        this.table.put("Key2","Value2");
        this.table.put("Key3","Value3");
        this.table.put("Key4","Value4");
        this.table.put("Key5","Value5");
        this.table.put("Key6","Value6");
    }
    @Test
    public void testGet() {
        assertEquals("Value1",this.table.get("Key1"));
        assertEquals("Value2",this.table.get("Key2"));
        assertEquals("Value3",this.table.get("Key3"));
        assertEquals("Value4",this.table.get("Key4"));
        assertEquals("Value5",this.table.get("Key5"));
        // assertEquals("Value6",this.table.get("Key6"));
    }
    @Test
    public void testGetChained() {
        //second node in chain
        assertEquals("Value6",this.table.get("Key6"));
        //second node in chain after being modified
        this.table.put("Key6","Value6+1");
        assertEquals("Value6+1",this.table.get("Key6"));
        //check that other values still come back correctly
        testGet();
    }
    @Test
    public void testGetMiss() {
        assertEquals(null,this.table.get("Key20"));
    }
    @Test
    public void testPutReturnValue() {
        assertEquals("Value3",this.table.put("Key3","Value3+1"));
        assertEquals("Value6",this.table.put("Key6", "Value6+1"));
        assertEquals(null,this.table.put("Key7","Value7"));
    }
    @Test
    public void testGetChangedValue () {
        BTreeImpl<String, String> table = new BTreeImpl<String, String>();
        String key1 = "hello";
        String value1 = "how are you today?";
        String value2 = "HI!!!";
        table.put(key1, value1);
        assertEquals(value1,table.get(key1));
        table.put(key1, value2);
        assertEquals(value2,table.get(key1));
    }
    @Test
    public void testDeleteViaPutNull() {
        BTreeImpl<String, String> table = new BTreeImpl<String, String>();
        String key1 = "hello";
        String value1 = "how are you today?";
        String value2 = null;
        table.put(key1, value1);
        table.put(key1, value2);
        assertEquals(value2,table.get(key1));
    }
    @Test
    public void testSeparateChaining () {
        BTreeImpl<Integer, String> table = new BTreeImpl<Integer, String>();
        for(int i = 0; i <= 23; i++) {
            table.put(i, "entry " + i);
        }
        assertEquals("entry 12",table.put(12, "entry 12+1"));
        assertEquals("entry 12+1",table.get(12));
        assertEquals("entry 23",table.get(23));
    }
}