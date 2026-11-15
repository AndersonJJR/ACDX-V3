package com.erp.brazil.hr.infrastructure.notification;

public class NotificationException extends RuntimeException {
    public NotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}