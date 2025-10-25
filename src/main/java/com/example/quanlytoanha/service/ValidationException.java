package com.example.quanlytoanha.service;

/**
 * Lớp ngoại lệ tùy chỉnh cho các lỗi liên quan đến dữ liệu đầu vào.
 */
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}
