package com.alphaStS.utils;

import java.util.Arrays;

// ArrayDeque doesn't implement equals
public class CircularArray<T> {
    public Object[] arr = new Object[3];
    public int start = 0;
    public int end = 0;
    public int size = 0;

    public CircularArray() {}

    public CircularArray(CircularArray<T> other) {
        arr = Arrays.copyOf(other.arr, other.arr.length);
        start = other.start;
        end = other.end;
        size = other.size;
    }

    public int size() {
        return size;
    }

    public void addFirst(T ob) {
        int next;
        if (size == arr.length) {
            var newArr = new Object[arr.length * 2];
            if (start == 0) {
                System.arraycopy(arr, 0, newArr, 0, arr.length);
            } else {
                System.arraycopy(arr, start, newArr, 0, arr.length - start);
                System.arraycopy(arr, 0, newArr, arr.length - start, end);
            }
            arr = newArr;
            next = arr.length - 1;
            end = arr.length / 2;
        } else {
            next = (start - 1) < 0 ? arr.length - 1: start - 1;
        }
        arr[next] = ob;
        start = next;
        size++;
    }

    public void addLast(T ob) {
        int next = end % arr.length;
        if (size == arr.length) {
            var newArr = new Object[arr.length * 2];
            if (start == 0) {
                System.arraycopy(arr, 0, newArr, 0, arr.length);
            } else {
                System.arraycopy(arr, start, newArr, 0, arr.length - start);
                System.arraycopy(arr, 0, newArr, arr.length - start, end);
            }
            start = 0;
            next = arr.length;
            arr = newArr;
        }
        arr[next] = ob;
        end = next + 1;
        size++;
    }

    public T pollFirst() {
        if (size == 0) {
            return null;
        }
        Object o = arr[start];
        arr[start] = null;
        start = (start + 1) % arr.length;
        size--;
        return (T) o;
    }

    public void clear() {
        for (int i = end > start ? start : 0; i < end; i++) {
            arr[i] = null;
        }
        if (end < start) {
            for (int i = start; i < arr.length; i++) {
                arr[i] = null;
            }
        }
        start = 0;
        end = 0;
        size = 0;
    }

    @Override public String toString() {
        return "CircularArray{" +
                "arr=" + Arrays.toString(arr) +
                ", start=" + start +
                ", end=" + end +
                ", size=" + size +
                '}';
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CircularArray<?> that = (CircularArray<?>) o;
        if (size() != that.size()) {
            return false;
        }
        for (int i = start, j = that.start; i < start + size; i++, j++) {
            if (!that.arr[j % that.arr.length].equals(arr[i % arr.length])) {
                return false;
            }
        }
        return true;
    }

    @Override public int hashCode() {
        int result = 1237;
        int end2 = end > start ? end : end + arr.length;
        for (int i = start; i < end2; i++) {
            result = 31 * result + arr[i % arr.length].hashCode();
        }
        return result;
    }
}
