package net.optionfactory.pebbel.compiled;

public class Asd {
    public static void foo(String a, String b, String c) {
        String.join("X", a, b, c);
    }

    public static void foo(int a, int b, int c) {
        bar(a,b,c);
    }

    public static void bar(int... asd) {

    }
}
