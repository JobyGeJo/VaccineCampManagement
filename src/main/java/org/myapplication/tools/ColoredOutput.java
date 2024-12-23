package org.myapplication.tools;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

public class ColoredOutput {

    public static final String RESET = "\u001B[0m";

    public static String red(String s) { return "\u001B[31m" + s + RESET; }
    public static String cyan(String s) { return "\u001B[36m" + s + RESET; }
    public static String green(String s) { return "\u001B[32m" + s + RESET; }
    public static String yellow(String s) { return "\u001B[33m" + s + RESET; }

    public static void print(String color, String s) {
        try {
            Logger.getLogger(ColoredOutput.class.getName()).info('\n' + s + '\n');
            System.out.print(
                ColoredOutput.class
                .getMethod(color, String.class)
                .invoke(null, s)
            );
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            System.out.println(s);
        }
    }

    public static void println(String color, String s) {
        print(color, s);
        System.out.println();
    }

    public static void main(String[] args) {
        System.out.println(ColoredOutput.green("Hello World!"));

        ColoredOutput.println("cyan", "Hello World");
    }

}
