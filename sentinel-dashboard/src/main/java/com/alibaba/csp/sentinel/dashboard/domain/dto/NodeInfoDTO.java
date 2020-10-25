package com.alibaba.csp.sentinel.dashboard.domain.dto;

import java.io.Serializable;

/**
 * <h3>sentinel-parent</h3>
 * <h4>com.alibaba.csp.sentinel.dashboard.domain.dto</h4>
 * <p>节点信息</p>
 *
 * @author zora
 * @since 2020.10.20
 */
public class NodeInfoDTO implements Serializable {

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    private String ip;
    private Integer port;

    @Override
    public String toString() {
        return "NodeInfo{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }
}
