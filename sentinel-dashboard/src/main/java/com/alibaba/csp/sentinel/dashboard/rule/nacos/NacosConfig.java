/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.ConfigService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Properties;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
@Configuration
public class NacosConfig {

    @Value(value = "${sentinel.nacos.config.addr}")
    private String nacosAddr;
    @Value(value = "${sentinel.nacos.config.nameSpace}")
    private String nameSpace;

    @Bean
    public Converter<List<FlowRuleEntity>, String> flowRuleEntityEncoder() {
        return JSON::toJSONString;
    }

    @Bean
    public Converter<String, List<FlowRuleEntity>> flowRuleEntityDecoder() {
        return s -> JSON.parseArray(s, FlowRuleEntity.class);
    }

    @Bean
    public ConfigService nacosConfigService() throws Exception {
//        return ConfigFactory.createConfigService(nacosAddr);
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, nacosAddr);
        properties.put(PropertyKeyConst.NAMESPACE, nameSpace);
        return ConfigFactory.createConfigService(properties);
    }
    /*
    服务中yml配置示例:
    sentinel:
      transport:
        dashboard: localhost:8080
      datasource:
        flow:
          nacos:
            serverAddr: localhost:8848
            groupId: SENTINEL_GROUP
            dataId: ${spring.application.name}-flow-rules
            ruleType: flow
        param-flow:
          nacos:
            serverAddr: localhost:8848
            groupId: SENTINEL_GROUP
            dataId: ${spring.application.name}-param-rules
            ruleType: param-flow
     */
}
