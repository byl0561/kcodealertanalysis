package com.kuaishou.kcode.common;

public class Pool<T> {
    private int idx = 0;
    private T[] holder = (T[]) new Object[32];

    public T get(){
        T resource = holder[--idx];
        holder[idx] = null;
        return resource;
    }

    public void put (T resource){
        // 扩容1.5倍
        if (idx == holder.length){
            T[] target = (T[]) new Object[holder.length + (holder.length >> 1)];
            System.arraycopy(holder, 0, target, 0, holder.length);
            holder = target;
        }
        holder[idx++] = resource;
    }

    public boolean isEmpty(){
        return idx == 0;
    }
}
