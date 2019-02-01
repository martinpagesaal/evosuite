package com.examples.with.different.packagename.dse;

import org.evosuite.utils.LoggingUtils;

public class ArrayExample {

    public ArrayExample() {
    }

//    public static boolean firstPositive(int[] array) {
//
//        if (array==null) {
//            return false;
//        } else if (array.length==0) {
//            return false;
//        } else {
//            return array[0] > 0;
//        }
//    }
//
//    public static boolean vocals(char[] array) {
//        if(array == null) {
//            return false;
//        } else if(array.length != 5) {
//            return false;
//        } else if(array[0] != 'a') {
//            return false;
//        } else if(array[1] != 'e') {
//            return false;
//        } else if(array[2] != 'i') {
//            return false;
//        } else if(array[3] != 'o') {
//            return false;
//        } else if(array[4] != 'u') {
//            return false;
//        } else {
//            return true;
//        }
//    }
//
//    public static int sum(int[] array) {
//        if(array == null) {
//            return 0;
//        } else if(array.length == 0) {
//            return 0;
//        }
//
//        int res = 0;
//        for(int i = 0; i < array.length; i++) {
//            res += array[i];
//        }
//
//        return res;
//    }
//
    public static int largoArr(int[] array) {
        if(array == null) {
            return 0;
        } else if(array.length == 1) {
            return 1;
        } else if(array.length == 2) {
            return 2;
        } else if(array.length == 3) {
            return 3;
        } else {
            return array.length;
        }
    }

    public boolean largoInt(int c) {
        char[] arr = new char[c];
        if(arr.length == 0) {
            return true;
        } else if(arr.length == 1) {
            return true;
        } else if(arr.length == 2) {
            return true;
        } else {
            return false;
        }

    }
}
