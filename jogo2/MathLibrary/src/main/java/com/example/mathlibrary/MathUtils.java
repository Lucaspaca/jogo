package com.example.mathlibrary;



public class MathUtils {


    public static boolean isInRestrictedRegion(int x, int y, int regionLeft, int regionRight, int regionBottom, int regionTop) {
        return x >= regionLeft && x <= regionRight &&
                y >= regionBottom && y <= regionTop;
    }
}