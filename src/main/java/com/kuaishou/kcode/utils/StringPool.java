package com.kuaishou.kcode.utils;

import java.util.Objects;

public class StringPool {
    private StringPool(){}

    private static Node root;

    public static String getCachedString(byte[] source, int start){
        Node node = root;
        int sourceIdx = start;
        for (; source[sourceIdx] != ','; sourceIdx++){
            int idx = toIdx(source[sourceIdx]);
            if (Objects.isNull(node.nextNode[idx])){
                return buildDict(source, start, node, sourceIdx);
            }
            else {
                node = node.nextNode[idx];
            }
        }
        if (Objects.isNull(node.value)){
            return buildDict(source, start, node, sourceIdx);
        }
        else {
            return node.value;
        }
    }

    public static String getCachedString(String source, int start){
        Node node = root;
        int sourceIdx = start;
        for (; source.charAt(sourceIdx) != ','; sourceIdx++){
            int idx = toIdx((byte) source.charAt(sourceIdx));
            if (Objects.isNull(node.nextNode[idx])){
                return buildDict(source, start, node, sourceIdx);
            }
            else {
                node = node.nextNode[idx];
            }
        }
        if (Objects.isNull(node.value)){
            return buildDict(source, start, node, sourceIdx);
        }
        else {
            return node.value;
        }
    }

    public static void init(){
        root = new Node();
    }

    private static synchronized String buildDict(String source, int start, Node node, int sourceIdx){
        for (; source.charAt(sourceIdx) != ','; sourceIdx++){
            int idx = toIdx((byte) source.charAt(sourceIdx));
            if (Objects.isNull(node.nextNode[idx])){
                node.nextNode[idx] = new Node();
            }
            node = node.nextNode[idx];
        }
        if (Objects.isNull(node.value)){
            StringBuilder sb = new StringBuilder();
            for (int i = start; i < sourceIdx; i++){
                sb.append(source.charAt(i));
            }
            node.value = sb.toString();
        }
        return node.value;
    }

    private static synchronized String buildDict(byte[] source, int start, Node node, int sourceIdx){
        for (; source[sourceIdx] != ','; sourceIdx++){
            int idx = toIdx(source[sourceIdx]);
            if (Objects.isNull(node.nextNode[idx])){
                node.nextNode[idx] = new Node();
            }
            node = node.nextNode[idx];
        }
        if (Objects.isNull(node.value)){
            node.value = new String(source, start, sourceIdx - start);
        }
        return node.value;
    }

    private static int toIdx(byte b){
        int idx;
        if (b >= '0' && b <= '9'){
            idx = b - '0';
        }
        else if (b >= 'a' && b <= 'z'){
            idx = b - 'a' + 10;
        }
        else if (b >= 'A' && b <= 'Z'){
            idx = b - 'A' + 36;
        }
        else if (b == '_'){
            idx = 62;
        }
        else {
            throw new RuntimeException("byte 不在 [0-9,a-z,A-Z,_] 范围内, byte: " + (char) b);
        }
        return idx;
    }

    private static class Node{
        private static final int CAPACITY = 10 + 26 + 26 + 1;

        private String value;
        private Node[] nextNode = new Node[CAPACITY];
    }
}
