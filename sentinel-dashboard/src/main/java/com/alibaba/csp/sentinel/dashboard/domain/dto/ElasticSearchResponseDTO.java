package com.alibaba.csp.sentinel.dashboard.domain.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * <h3>sentinel-parent</h3>
 * <h4>com.alibaba.csp.sentinel.dashboard.domain.dto</h4>
 * <p>es搜索结果</p>
 *
 * @author zora
 * @since 2020.10.20
 */
public class ElasticSearchResponseDTO implements Serializable {
    private Map<String, Map<String, List<String>>> field;

    public Map<String, Map<String, List<String>>> getField() {
        return field;
    }

    public void setField(Map<String, Map<String, List<String>>> field) {
        this.field = field;
    }

    @Override
    public String toString() {
        return "ElasticSearchResponseDTO{" +
                "field=" + field +
                '}';
    }
}
