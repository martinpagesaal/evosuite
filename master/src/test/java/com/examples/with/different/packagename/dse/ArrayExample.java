package com.examples.with.different.packagename.dse;

public class ArrayExample {

    public ArrayExample() {
    }

    public static boolean firstPositive(int[] array) {

        if (array==null) {
            return false;
        } else if (array.length==0) {
            return false;
        } else {
            return array[0] > 0;
        }
    }

    public static boolean vocals(char[] array) {
        if(array == null) {
            return false;
        } else if(array.length != 5) {
            return false;
        } else if(array[0] != 'a') {
            return false;
        } else if(array[1] != 'e') {
            return false;
        } else if(array[2] != 'i') {
            return false;
        } else if(array[3] != 'o') {
            return false;
        } else if(array[4] != 'u') {
            return false;
        } else {
            return true;
        }
    }

    public static int sum(int[] array) {
        if(array == null) {
            return 0;
        } else if(array.length == 0) {
            return 0;
        }

        int res = 0;
        for(int i = 0; i < array.length; i++) {
            res += array[i];
        }

        return res;
    }
}
