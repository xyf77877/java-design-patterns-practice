package cn.xyf.framework.core.exception.framework;

public class FrameworkException extends BaseException {
    private static final long serialVersionUID = 1L;

    public FrameworkException(String errMessage) {
        super(errMessage);
        setErrCode(BasicErrorCode.FRAMEWORK_ERROR);
    }

    public FrameworkException(String errMessage, Throwable e) {
        super(errMessage, e);
        setErrCode(BasicErrorCode.FRAMEWORK_ERROR);
    }
}



