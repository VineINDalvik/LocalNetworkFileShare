package com.vine.dmall.localnetworkfiletranfer.log;

/**
 * Created by xiovine on 15/7/23.
 */
public class LogInfo {
    private String ip;
    private String requestPath;
    private String method;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
