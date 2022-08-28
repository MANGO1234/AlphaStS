package com.alphaStS;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.alphaStS.utils.CircularArray;
import org.junit.Test;

import java.util.Arrays;

public class AppTest {
    @Test
    public void testCircularArray() {
        var arr = new CircularArray<Integer>();
        arr.addFirst(3);
        arr.addFirst(2);
        arr.addFirst(1);
        assertEquals(arr.pollFirst().intValue(), 1);
        arr.addFirst(0);
        arr.addFirst(-1);
        assertEquals(arr.pollFirst().intValue(), -1);
        assertEquals(arr.pollFirst().intValue(), 0);
        assertEquals(arr.pollFirst().intValue(), 2);
        assertEquals(arr.pollFirst().intValue(), 3);

        assertEquals(arr.size(), 0);
        arr.addLast(1);
        arr.addLast(2);
        arr.addLast(3);
        arr.addLast(4);
        assertEquals(arr.size(), 4);
        assertEquals(arr.pollFirst().intValue(), 1);
        assertEquals(arr.pollFirst().intValue(), 2);
        assertEquals(arr.size(), 2);
        arr.addLast(5);
        arr.addLast(6);
        arr.addFirst(0);
        assertEquals(arr.size(), 5);
        assertEquals(arr.pollFirst().intValue(), 0);
        assertEquals(arr.pollFirst().intValue(), 3);
        assertEquals(arr.pollFirst().intValue(), 4);
        assertEquals(arr.pollFirst().intValue(), 5);
        assertEquals(arr.pollFirst().intValue(), 6);

        arr.addLast(1);
        arr.addLast(2);
        arr.addLast(3);
        arr.addLast(4);
        assertEquals(arr.size(), 4);
        arr.clear();
        assertEquals(arr.size(), 0);

        var arr2 = new CircularArray<Integer>();
        assertEquals(arr, arr2);
        arr.addLast(1);
        arr.addLast(2);
        arr.addLast(3);
        arr.addLast(4);
        assertNotEquals(arr, arr2);
        arr2.addLast(1);
        arr2.addLast(2);
        arr2.addLast(3);
        arr2.addLast(4);
        assertEquals(arr, arr2);
        arr2.pollFirst();
        assertNotEquals(arr, arr2);
        arr.pollFirst();
        assertEquals(arr, arr2);
    }
}
