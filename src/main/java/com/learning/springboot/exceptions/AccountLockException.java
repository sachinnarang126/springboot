package com.learning.springboot.exceptions;

public class AccountLockException extends RuntimeException {
    public AccountLockException(String msg) {
        super(msg);
    }
}
