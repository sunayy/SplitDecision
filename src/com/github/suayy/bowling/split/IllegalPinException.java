package com.github.suayy.bowling.split;

public class IllegalPinException extends IllegalArgumentException {

    public static final String ILLEGAL_VALUE = "Illegal number";
    public static final String NUMBER_TOO_BIG = "Number too big";
    public static final String NUMBER_TOO_SMALL = "Number too small";

    public IllegalPinException() { super(); }
    public IllegalPinException(String message) { super(message); }
}
