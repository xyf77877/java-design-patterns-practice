package cn.xyf.framework.core.exception;

import cn.xyf.framework.core.exception.framework.BaseException;
import cn.xyf.framework.core.exception.framework.BasicErrorCode;

public class BizException extends BaseException {
    private static final long serialVersionUID = 1L;

    public BizException(String errMessage) {
        super(errMessage);
        setErrCode((IErrorCode) BasicErrorCode.BIZ_ERROR);
    }

    public BizException(IErrorCode errCode, String errMessage) {
        super(errMessage);
        setErrCode(errCode);
    }

    public BizException(String errMessage, Throwable e) {
        super(errMessage, e);
        setErrCode((IErrorCode) BasicErrorCode.BIZ_ERROR);
    }
}



