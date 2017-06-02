package com.andrew2070.Empires.exceptions;


public class SegmentException extends RuntimeException{

    public SegmentException(String message) {
        super(message);
    }

    public SegmentException(String message, Throwable cause) {
        super(message, cause);
    }
}
