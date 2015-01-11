package pct.droid.tv.utils;

/*************************************************************************
 *  Compilation:  javac Capitalize.java
 *  Execution:    java Capitalize < input.txt
 *
 *  Read in a sequence of words from standard input and capitalize each
 *  one (make first letter uppercase; make rest lowercase).
 *
 *  % java Capitalize
 *  now is the time for all good
 *  Now Is The Time For All Good
 *  to be or not to be that is the question
 *  To Be Or Not To Be That Is The Question
 *
 *  Remark: replace sequence of whitespace with a single space.
 *
 *************************************************************************/

public class Capitalize {

    public static String capitalize(String s) {
        if (s.length() == 0) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}