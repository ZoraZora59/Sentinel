package com.alibaba.csp.sentinel.dashboard.config;

import com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosConfigUtil;
import com.alibaba.nacos.api.config.ConfigService;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.util.Strings;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <h3>sentinel-parent</h3>
 * <h4>com.alibaba.csp.sentinel.dashboard.config</h4>
 * <p>ES配置</p>
 *
 * @author zora
 * @since 2020.10.18
 */
@Configuration
public class ElasticSearchConfig {
    private final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchConfig.class);

    @Value(value = "${elasticsearch.host}")
    private String[] usingHost;
    @Value(value = "${elasticsearch.port}")
    private int[] usingPort;

    @Bean
    public RestHighLevelClient[] getRestHighLevelClient(ConfigService configService) {
        try {
            String hosts = configService.getConfig(NacosConfigUtil.ELASTIC_SEARCH_HOSTS,
                    NacosConfigUtil.GROUP_ID, 3000);
            String ports = configService.getConfig(NacosConfigUtil.ELASTIC_SEARCH_PORTS,
                    NacosConfigUtil.GROUP_ID, 3000);
            if (Strings.isNotBlank(hosts) && Strings.isNotBlank(ports)) {
                String[] nacosHosts = hosts.split(",");
                int[] nacosPorts;
                {
                    String[] strPorts = ports.split(",");
                    nacosPorts = new int[strPorts.length];
                    for (int i = 0; i < strPorts.length; i++) {
                        String strPort = strPorts[i];
                        nacosPorts[i] = Integer.parseInt(strPort);
                    }
                }
                LOGGER.info("采用从Nacos配置中心获取到的ES连接参数.");
                usingHost = nacosHosts;
                usingPort = nacosPorts;
            }
        } catch (Exception exception) {
            LOGGER.error("从Nacos获取ES配置失败", exception);
        }

        if (usingHost.length != usingPort.length) {
            throw new IllegalArgumentException("ES的host与port配置必须对齐一致");
        } else {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < usingHost.length; i++) {
                builder.append(usingHost[i]).append(':').append(usingPort[i]).append(",");
            }
            builder.deleteCharAt(builder.lastIndexOf(","));
            LOGGER.info("当前使用的ElasticSearch配置为【{}】", builder.toString());
        }
        RestHighLevelClient[] clients = new RestHighLevelClient[usingHost.length];
        for (int i = 0; i < usingHost.length; i++) {
            RestClientBuilder builder = RestClient.builder(new HttpHost(usingHost[i], usingPort[i]));
            clients[i] = new RestHighLevelClient(builder);
        }
        return clients;
    }
}
