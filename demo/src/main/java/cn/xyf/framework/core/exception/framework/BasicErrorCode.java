package cn.xyf.framework.core.exception.framework;

import cn.xyf.framework.core.exception.IErrorCode;

public enum BasicErrorCode implements IErrorCode {
    BIZ_ERROR("BIZ_ERROR", "通用的业务逻辑错误"),

    FRAMEWORK_ERROR("FRAMEWORK_ERROR", "框架错误"),

    SYS_ERROR("SYS_ERROR", "未知的系统错误");

    private String errCode;
    private String errDesc;

    BasicErrorCode(String errCode, String errDesc) {
        this.errCode = errCode;
        this.errDesc = errDesc;
    }


    public String getErrCode() {
        return this.errCode;
    }


    public String getErrDesc() {
        return this.errDesc;
    }
}



