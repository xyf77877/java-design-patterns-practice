package cn.xyf.framework.core.exception;

import cn.xyf.framework.core.exception.framework.BaseException;
import cn.xyf.framework.core.exception.framework.BasicErrorCode;

public class SysException extends BaseException {
    private static final long serialVersionUID = 4355163994767354840L;

    public SysException(String errMessage) {
        super(errMessage);
        setErrCode((IErrorCode) BasicErrorCode.SYS_ERROR);
    }

    public SysException(IErrorCode errCode, String errMessage) {
        super(errMessage);
        setErrCode(errCode);
    }

    public SysException(String errMessage, Throwable e) {
        super(errMessage, e);
        setErrCode((IErrorCode) BasicErrorCode.SYS_ERROR);
    }

    public SysException(String errMessage, IErrorCode errorCode, Throwable e) {
        super(errMessage, e);
        setErrCode(errorCode);
    }
}



