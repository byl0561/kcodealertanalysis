package com.kuaishou.kcode.utils;

import com.kuaishou.kcode.Configuration;

public class MathUtil extends Configuration {
    private MathUtil(){}

    private static ThreadLocal<SmallHeap> queueLocal;

    public static void init(){
        queueLocal = new ThreadLocal<SmallHeap>(){
            @Override
            protected SmallHeap initialValue() {
                return new SmallHeap();
            }
        };
    }

    public static short get99th(short[] nums, int length){
        SmallHeap smallHeap = queueLocal.get();
        smallHeap.clear();
        int count = length / 100 + 1;
        for (int i = 0; i < length; i++){
            short num = nums[i];
            if (smallHeap.getSize() < count){
                smallHeap.offer(num);
            }
            else if (num > smallHeap.peek()){
                smallHeap.remove();
                smallHeap.offer(num);
            }
        }
        return smallHeap.peek();
    }

    private static class SmallHeap{
        public SmallHeap (){
            heap = new short[RECORDS_SIZE];
            size = 0;
        }

        short[] heap;
        // 堆的第一个元素下标从1开始
        int size;

        void clear() {
            size = 0;
        }

        int getSize(){
            return size;
        }

        short peek(){
            return heap[1];
        }

        void remove(){
            heap[1] = heap[size--];
            sink(1);
        }

        void offer(short b){
            // 扩容1.5倍
            if (++size == heap.length){
                short[] target = new short[heap.length + (heap.length >> 1)];
                System.arraycopy(heap, 0, target, 0, heap.length);
                heap = target;
            }

            heap[size] = b;
            swim(size);
        }

        private void swap(int idx1, int idx2){
            short tmp = heap[idx1];
            heap[idx1] = heap[idx2];
            heap[idx2] = tmp;
        }

        // 下沉
        private void sink(int idx){
            int minChildIdx;
            int leftChildIdx = (idx << 1);
            if (leftChildIdx + 1 <= size){
                minChildIdx = heap[leftChildIdx] < heap[leftChildIdx+1] ? leftChildIdx : leftChildIdx+1;
            }
            else if (leftChildIdx == size){
                minChildIdx = leftChildIdx;
            }
            else {
                return;
            }
            if (heap[idx] > heap[minChildIdx]){
                swap(idx, minChildIdx);
                sink(minChildIdx);
            }
        }

        // 上浮
        private void swim(int idx){
            if (idx == 1){
                return;
            }
            int fatherIdx = idx >> 1;
            if (heap[idx] < heap[fatherIdx]){
                swap(idx, fatherIdx);
                swim(fatherIdx);
            }
        }

    }

}
