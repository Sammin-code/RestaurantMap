package com.blog.exception;

public class ValidationException extends RuntimeException {
  public ValidationException(String message) {
    super(message);
  }
}