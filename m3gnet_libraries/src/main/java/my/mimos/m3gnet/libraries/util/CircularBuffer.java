package my.mimos.m3gnet.libraries.util;

import java.lang.reflect.Array;

/**
 * Created by ariffin.ahmad on 8/2/2016.
 */
public class CircularBuffer<E> {
    public E[] array;
    private Class<E> c;
    private int next_pos  = 0;

    public CircularBuffer(Class<E> c, int size) {
        this.c    = c;
        array     = (E[]) Array.newInstance(this.c, size); //(E[]) new Object[size]; //new T[size];
        for (int i = 0; i < size; i++) {
            array[i] = null;
        }
        next_pos  = 0;
    }

    public void store(E data) {
        array[next_pos]  = data;
        next_pos        += 1;

        if (next_pos >= array.length)
            next_pos = 0;
    }

    public E getLatest() {
        int tmp_pos = next_pos - 1;
        if (tmp_pos < 0)
            tmp_pos = array.length - 1;
        return array[tmp_pos];
    }

    public E[] getArrayAsc() {
        E[] ret = (E[]) Array.newInstance(this.c, array.length);

        int tmp_pos = next_pos;
        for (int i = 0; i < ret.length; i++) {
            ret[i] = array[tmp_pos];
            tmp_pos += 1;
            if (tmp_pos >= array.length)
                tmp_pos = 0;
        }

        return ret;
    }

    public E[] getArrayDesc() {
        E[] ret = (E[]) Array.newInstance(this.c, array.length);

        int tmp_pos = this.next_pos - 1;
        for (int i = 0; i < this.array.length; i++) {
            if (tmp_pos < 0)
                tmp_pos = this.array.length - 1;
            ret[i] = this.array[tmp_pos];
            tmp_pos -= 1;
        }

        return ret;
    }
}
