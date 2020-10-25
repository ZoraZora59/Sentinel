package com.alibaba.csp.sentinel.dashboard.repository.metric;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.RandomUtils;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.collapse.CollapseBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <h3>sentinel-parent</h3>
 * <h4>com.alibaba.csp.sentinel.dashboard.repository.metric</h4>
 * <p>数据落盘到ES</p>
 *
 * @author zora
 * @since 2020.10.18
 */
@Component
@Primary
public class ElasticSearchMetricsRepository implements MetricsRepository<MetricEntity> {
    private static final String ES_INDEX = "sentinel_metrics";
    @Autowired
    private RestHighLevelClient[] esRestClient;
    private final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchMetricsRepository.class);

    private RestHighLevelClient randomClient() {
        return esRestClient[RandomUtils.nextInt(0, esRestClient.length)];
    }

    /**
     * Save the metric to the storage repository.
     *
     * @param metric metric data to save
     */
    @Override
    public void save(MetricEntity metric) {
        IndexRequest indexRequest = new IndexRequest(ES_INDEX);
        indexRequest.source(XContentType.JSON, JSON.toJSONString(metric));
        try {
            randomClient().index(indexRequest, RequestOptions.DEFAULT);
        } catch (IOException ioException) {
            LOGGER.error("数据落盘ES失败，数据摘要[App={}, Resource={}, passQps={}, blockQps={}]", metric.getApp(), metric.getResource(), metric.getPassQps(), metric.getBlockQps(), ioException);
        }
    }

    /**
     * Save all metrics to the storage repository.
     *
     * @param metrics metrics to save
     */
    @Override
    public void saveAll(Iterable<MetricEntity> metrics) {
        BulkRequest bulkRequest = new BulkRequest();
        metrics.forEach(i -> {
            IndexRequest indexRequest = new IndexRequest(ES_INDEX);
            indexRequest.source(JSON.toJSONString(i), XContentType.JSON);
            bulkRequest.add(indexRequest);
        });
        try {
            randomClient().bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (IOException ioException) {
            LOGGER.error("数据批量落盘ES失败", ioException);
        }
    }

    /**
     * Get all metrics by {@code appName} and {@code resourceName} between a period of time.
     *
     * @param app       application name for Sentinel
     * @param resource  resource name
     * @param startTime start timestamp
     * @param endTime   end timestamp
     * @return all metrics in query conditions
     */
    @Override
    public List<MetricEntity> queryByAppAndResourceBetween(String app, String resource, long startTime, long endTime) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder
                .must(QueryBuilders.termQuery("app.keyword", app))
                .must(QueryBuilders.termQuery("resource.keyword", resource))
                .must(QueryBuilders.rangeQuery("timestamp").gte(startTime).lt(endTime));
        sourceBuilder.query(boolQueryBuilder);
        sourceBuilder.sort("timestamp", SortOrder.ASC);
        SearchRequest rq = new SearchRequest();
        rq.indices(ES_INDEX);
        rq.source(sourceBuilder);
        try {
            SearchResponse rp = randomClient().search(rq, RequestOptions.DEFAULT);
            if (rp.status() != RestStatus.OK || rp.getHits().getTotalHits().value <= 0) {
                return Collections.emptyList();
            }
            return Arrays.stream(rp.getHits().getHits()).map(metadata -> {
                Map<String, Object> map = metadata.getSourceAsMap();
                MetricEntity entity = new MetricEntity();
                entity.setId((Long) map.get("id"));
                entity.setApp((String) map.get("app"));
                entity.setTimestamp(Date.from(Instant.ofEpochMilli((Long) map.get("timestamp"))));
                entity.setGmtCreate(Date.from(Instant.ofEpochMilli((Long) map.get("gmtCreate"))));
                entity.setResource((String) map.get("resource"));
                entity.setPassQps(((Integer) map.get("passQps")).longValue());
                entity.setBlockQps(((Integer) map.get("blockQps")).longValue());
                entity.setSuccessQps(((Integer) map.get("successQps")).longValue());
                entity.setExceptionQps(((Integer) map.get("exceptionQps")).longValue());
                return entity;
            }).collect(Collectors.toList());
        } catch (IOException ioException) {
            LOGGER.error("查询app[{}]中资源[{}]的统计时失败", app, resource, ioException);
            return Collections.emptyList();
        }
    }

    /**
     * List resource name of provided application name.
     *
     * @param app application name
     * @return list of resources
     */
    @Override
    public List<String> listResourcesOfApp(String app) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.must(QueryBuilders.termQuery("app.keyword", app));
        sourceBuilder.query(boolQueryBuilder);
        sourceBuilder.fetchSource("resource.keyword", null);
        sourceBuilder.collapse(new CollapseBuilder("resource.keyword"));
        SearchRequest rq = new SearchRequest();
        rq.indices(ES_INDEX);
        rq.source(sourceBuilder);
        try {
            SearchResponse rp = randomClient().search(rq, RequestOptions.DEFAULT);
            if (rp.status() != RestStatus.OK || rp.getHits().getTotalHits().value <= 0) {
                return Collections.emptyList();
            }
            return Arrays.stream(rp.getHits().getHits()).map(metadata -> {
                Map<String, DocumentField> map = metadata.getFields();
                return (String) map.get("resource.keyword").getValue();
            }).collect(Collectors.toList());
        } catch (IOException ioException) {
            LOGGER.error("查询app[{}]中所有资源的统计时失败", app, ioException);
            return Collections.emptyList();
        }
    }
}
