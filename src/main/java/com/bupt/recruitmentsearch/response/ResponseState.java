package com.bupt.recruitmentsearch.response;

public enum ResponseState {
    SUCCESS(true,20000,"操作成功"),
    FAILED(false,40000,"操作失败"),
    ERROR_404(false,40004,"页面丢失"),
    ERROR_403(false,40003,"权限不足"),
    ERROR_504(false,50004,"系统繁忙，请稍后重试"),
    ERROR_505(false,50005,"请求错误，请检查数据"),


    JOIN_IN_SUCCESS(true,20001,"注册成功"),
    LOGIN_FAILED(false,49999,"登录失败"),
    LOGIN_SUCCESS(true,20001,"登陆成功"),
    GET_RESOURCE_FAILED(false,40001,"获取资源失败"),
    ACCOUNT_NOT_LOGIN(false,40002,"账号未登录"),
    PERMISSION_DENIED(false,40003,"无权操作"),
    ACCOUNT_DENIED(false,40003,"账号被禁止"),
    ;

    ResponseState(boolean success,int code,String message){
        this.code=code;
        this.success=success;
        this.message=message;
    }
    private int code;
    private String message;
    private boolean success;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
