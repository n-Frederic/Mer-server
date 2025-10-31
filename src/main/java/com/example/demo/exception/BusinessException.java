package com.example.demo.exception;



public class BusinessException extends RuntimeException {

  // 错误码
  private final String errorCode;

  /**
   * 只包含错误消息的构造器
   */
  public BusinessException(String message) {
    this("BUSINESS_ERROR", message);
  }

  /**
   * 包含错误码和错误消息的构造器
   */
  public BusinessException(String errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  /**
   * 包含错误码、错误消息和原始异常的构造器
   */
  public BusinessException(String errorCode, String message, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
  }

  /**
   * 包含错误消息和原始异常的构造器
   */
  public BusinessException(String message, Throwable cause) {
    this("BUSINESS_ERROR", message, cause);
  }

  // 获取错误码
  public String getErrorCode() {
    return errorCode;
  }
}