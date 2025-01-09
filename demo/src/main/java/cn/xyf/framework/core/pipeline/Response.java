package cn.xyf.framework.core.pipeline;

import java.io.Serializable;

public class Response implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean isSuccess;

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Response)) return false;
        Response other = (Response) o;
        if (!other.canEqual(this)) return false;
        if (isSuccess() != other.isSuccess()) return false;
        Object this$errCode = getErrCode(), other$errCode = other.getErrCode();
        if ((this$errCode == null) ? (other$errCode != null) : !this$errCode.equals(other$errCode)) return false;
        Object this$errMessage = getErrMessage(), other$errMessage = other.getErrMessage();
        return !((this$errMessage == null) ? (other$errMessage != null) : !this$errMessage.equals(other$errMessage));
    }

    private String errCode;
    private String errMessage;

    public boolean canEqual(Object other) {
        return other instanceof Response;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + (isSuccess() ? 79 : 97);
        Object $errCode = getErrCode();
        result = result * 59 + (($errCode == null) ? 0 : $errCode.hashCode());
        Object $errMessage = getErrMessage();
        return result * 59 + (($errMessage == null) ? 0 : $errMessage.hashCode());
    }


    public boolean isSuccess() {
        return this.isSuccess;
    }

    public void setSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public String getErrCode() {
        return this.errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    public String getErrMessage() {
        return this.errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }


    public String toString() {
        return "Response [isSuccess=" + this.isSuccess + ", errCode=" + this.errCode + ", errMessage=" + this.errMessage + "]";
    }

    public static Response buildFailure(String errCode, String errMessage) {
        Response response = new Response();
        response.setSuccess(false);
        response.setErrCode(errCode);
        response.setErrMessage(errMessage);
        return response;
    }

    public static Response buildSuccess() {
        Response response = new Response();
        response.setSuccess(true);
        return response;
    }
}



