/*
 *
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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
 *
 */

package com.alibaba.nacos.client.monitor.config;

import com.alibaba.nacos.client.env.NacosClientProperties;
import com.alibaba.nacos.client.monitor.MetricsMonitor;
import com.alibaba.nacos.common.utils.ConvertUtils;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Tags;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Config metrics management.
 *
 * @author <a href="https://github.com/FAWC438">FAWC438</a>
 */
public class ConfigMetrics {
    
    // Micrometer recommends using <b>. (dots)</b> to uniformly separate meter names, see <a
    // href="https://micrometer.io/docs/concepts#_naming_meters">official docs</a> .
    
    private static final String METRIC_MODULE_NAME = "config";
    
    private static final String NACOS_CONFIG_METRICS_ENABLE_PROPERTY = "nacos.metrics.config.enable";
    
    private static final String DEFAULT_METER_NAME = "nacos.monitor";
    
    private static final String TIMER_METER_NAME = "nacos.client.config.timer";
    
    private static final String COMMON_METER_NAME = "nacos.client.config.common";
    
    private static final String COUNTER_METER_NAME = "nacos.client.config.counter";
    
    /**
     * Property {@value NACOS_CONFIG_METRICS_ENABLE_PROPERTY} aims to control which module (config or naming, here is
     * config) is <b>not</b> monitored by Micrometer. It's default value is <b>true</b> so that users who want to
     * monitor whole Nacos client can ignore this property and just only need to set <tt>nacos.metrics.enable</tt> in
     * {@link MetricsMonitor}.
     */
    public static boolean isEnable() {
        return ConvertUtils.toBoolean(
                NacosClientProperties.PROTOTYPE.getProperty(NACOS_CONFIG_METRICS_ENABLE_PROPERTY, "true"))
                && MetricsMonitor.isEnable();
    }
    
    // ------------------------ Gauges ------------------------
    
    // “heisen-gauge” principal: https://micrometer.io/docs/concepts#_gauges
    // DO NOT interact with the gauge object directly. Rather, interacting with the thing that will cause the gauge.
    
    private static final AtomicInteger LISTENER_CONFIG_COUNT_GAUGE = MetricsMonitor.getNacosMeterRegistry()
            .gauge(DEFAULT_METER_NAME, Tags.of("module", METRIC_MODULE_NAME, "name", "listenerConfigCount"),
                    new AtomicInteger(0));
    
    private static final AtomicInteger SERVER_NUMBER_GAUGE = MetricsMonitor.getNacosMeterRegistry()
            .gauge(COMMON_METER_NAME, Tags.of("module", METRIC_MODULE_NAME, "name", "serverNumber"),
                    new AtomicInteger(0));
    
    /**
     * Set the value of <b>listenConfigCount</b> gauge.
     * <b>listenConfigCount</b> is to record the number of listening configs. As a matter of fact, this value reflects
     * the actual size of the config <tt>cacheMap</tt>
     *
     * @param count the count of listened configs
     */
    public static void setListenerConfigCountGauge(int count) {
        if (LISTENER_CONFIG_COUNT_GAUGE != null && isEnable()) {
            LISTENER_CONFIG_COUNT_GAUGE.set(count);
        }
    }
    
    /**
     * Set the value of <b>serverNumber</b> gauge.
     * <b>serverNumber</b> is to record the number of nacos servers.
     *
     * @param count the count of servers
     */
    public static void setServerNumberGauge(int count) {
        if (SERVER_NUMBER_GAUGE != null && isEnable()) {
            SERVER_NUMBER_GAUGE.set(count);
        }
    }
    
    /**
     * Gauge the size of a collection. Only used when the reference to the collection is unchanged. It is best not to
     * call this function in a multithreaded code block
     *
     * @param meterName  meter name, get the value in the public constant of this class
     * @param tagName    tag name
     * @param collection collection
     * @param <T>        collection type
     */
    public static <T extends Collection<?>> void gaugeCollectionSize(String meterName, String tagName, T collection) {
        if (isEnable()) {
            MetricsMonitor.getNacosMeterRegistry()
                    .gaugeCollectionSize(meterName, Tags.of("module", METRIC_MODULE_NAME, "name", tagName), collection);
        }
    }
    
    /**
     * Gauge the size of a map. Only used when the reference to the map is <b>unchanged</b>. It is best not to call this
     * function in a multithreaded code block
     *
     * @param meterName meter name, get the value in the public constant of this class
     * @param tagName   tag name
     * @param map       map
     * @param <T>       map type
     */
    public static <T extends Map<?, ?>> void gaugeMapSize(String meterName, String tagName, T map) {
        if (isEnable()) {
            MetricsMonitor.getNacosMeterRegistry()
                    .gaugeMapSize(meterName, Tags.of("module", METRIC_MODULE_NAME, "name", tagName), map);
        }
    }
    
    // ------------------------ Counters ------------------------
    
    private static final Counter SYNC_WITH_SERVER_COUNTER = MetricsMonitor.getNacosMeterRegistry()
            .counter(COUNTER_METER_NAME, Tags.of("module", METRIC_MODULE_NAME, "name", "syncWithServer"));
    
    private static final Counter QUERY_SUCCESS_COUNTER = MetricsMonitor.getNacosMeterRegistry()
            .counter(COUNTER_METER_NAME, Tags.of("module", METRIC_MODULE_NAME, "name", "querySuccess"));
    
    private static final Counter QUERY_FAILED_COUNTER = MetricsMonitor.getNacosMeterRegistry()
            .counter(COUNTER_METER_NAME, Tags.of("module", METRIC_MODULE_NAME, "name", "queryFailed"));
    
    private static final Counter PUBLISH_SUCCESS_COUNTER = MetricsMonitor.getNacosMeterRegistry()
            .counter(COUNTER_METER_NAME, Tags.of("module", METRIC_MODULE_NAME, "name", "publishSuccess"));
    
    private static final Counter PUBLISH_FAILED_COUNTER = MetricsMonitor.getNacosMeterRegistry()
            .counter(COUNTER_METER_NAME, Tags.of("module", METRIC_MODULE_NAME, "name", "publishFailed"));
    
    private static final Counter REMOVE_SUCCESS_COUNTER = MetricsMonitor.getNacosMeterRegistry()
            .counter(COUNTER_METER_NAME, Tags.of("module", METRIC_MODULE_NAME, "name", "removeSuccess"));
    
    private static final Counter REMOVE_FAILED_COUNTER = MetricsMonitor.getNacosMeterRegistry()
            .counter(COUNTER_METER_NAME, Tags.of("module", METRIC_MODULE_NAME, "name", "removeFailed"));
    
    private static final Counter SERVER_REQUEST_HANDLE_COUNTER = MetricsMonitor.getNacosMeterRegistry()
            .counter(COUNTER_METER_NAME, Tags.of("module", METRIC_MODULE_NAME, "name", "serverRequestHandle"));
    
    /**
     * Increment the value of <tt>SYNC_WITH_SERVER_COUNTER</tt> counter. This metric is to record the number of sync
     * times between client and server.
     */
    public static void incSyncWithServerCounter() {
        if (isEnable()) {
            SYNC_WITH_SERVER_COUNTER.increment();
        }
    }
    
    /**
     * Increment the value of <tt>QUERY_SUCCESS_COUNTER</tt> counter. This metric is to record the number of successful
     * config queries.
     */
    public static void incQuerySuccessCounter() {
        if (isEnable()) {
            QUERY_SUCCESS_COUNTER.increment();
        }
    }
    
    /**
     * Increment the value of <tt>QUERY_FAILED_COUNTER</tt> counter. This metric is to record the number of failed
     * config queries.
     */
    public static void incQueryFailedCounter() {
        if (isEnable()) {
            QUERY_FAILED_COUNTER.increment();
        }
    }
    
    /**
     * Increment the value of <tt>PUBLISH_SUCCESS_COUNTER</tt> counter. This metric is to record the number of
     * successful config publishes.
     */
    public static void incPublishSuccessCounter() {
        if (isEnable()) {
            PUBLISH_SUCCESS_COUNTER.increment();
        }
    }
    
    /**
     * Increment the value of <tt>PUBLISH_FAILED_COUNTER</tt> counter. This metric is to record the number of failed
     * config publishes.
     */
    public static void incPublishFailedCounter() {
        if (isEnable()) {
            PUBLISH_FAILED_COUNTER.increment();
        }
    }
    
    /**
     * Increment the value of <tt>REMOVE_SUCCESS_COUNTER</tt> counter. This metric is to record the number of successful
     * config removes.
     */
    public static void incRemoveSuccessCounter() {
        if (isEnable()) {
            REMOVE_SUCCESS_COUNTER.increment();
        }
    }
    
    /**
     * Increment the value of <tt>REMOVE_FAILED_COUNTER</tt> counter. This metric is to record the number of failed
     * config removes.
     */
    public static void incRemoveFailedCounter() {
        if (isEnable()) {
            REMOVE_FAILED_COUNTER.increment();
        }
    }
    
    /**
     * Increment the value of <tt>SERVER_REQUEST_HANDLE_SUCCESS_COUNTER</tt> counter. This metric is to record the
     * number of handled requests from Nacos server.
     */
    public static void incServerRequestHandleCounter() {
        if (isEnable()) {
            SERVER_REQUEST_HANDLE_COUNTER.increment();
        }
    }
    
    // ------------------------ Timers ------------------------
    
    // For evey meter, Micrometer will generate an id (key) by its name, description and tags.
    // Then the meters will be stored in a ConcurrentHashMap as a cache.
    // So it is OK to build a new timer meter every time.
    
    /**
     * Record the HTTP request time in config module.
     *
     * @param method   request method
     * @param url      request url
     * @param code     response code
     * @param duration request duration, unit: ms
     */
    public static void recordConfigRequestTimer(String method, String url, String code, long duration) {
        if (isEnable()) {
            MetricsMonitor.getNacosMeterRegistry().timer(TIMER_METER_NAME,
                    Tags.of("module", METRIC_MODULE_NAME, "method", method, "url", url, "code", code, "name",
                            "configRequest")).record(duration, TimeUnit.MILLISECONDS);
        }
    }
    
    /**
     * Record the notification time for a single config change on the client.
     *
     * @param clientName client name
     * @param dataId     config data id
     * @param group      config group
     * @param tenant     config tenant
     * @param duration   request duration, unit: ms
     */
    public static void recordNotifyCostDurationTimer(String clientName, String dataId, String group, String tenant,
            long duration) {
        if (isEnable()) {
            MetricsMonitor.getNacosMeterRegistry().timer(TIMER_METER_NAME,
                    Tags.of("module", METRIC_MODULE_NAME, "clientName", clientName, "dataId", dataId, "group", group,
                            "tenant", tenant, "name", "notifyCostDuration")).record(duration, TimeUnit.MILLISECONDS);
        }
    }
    
    /**
     * Record the duration of a rpc request on the client.
     *
     * @param connectionType connection type, such as: GRPC
     * @param currentServer  current rpc server
     * @param rpcResultCode  rpc result code, usually <b>200</b> means success
     * @param duration       request duration, unit: ms
     */
    public static void recordRpcCostDurationTimer(String connectionType, String currentServer, String rpcResultCode,
            long duration) {
        if (isEnable()) {
            MetricsMonitor.getNacosMeterRegistry().timer(TIMER_METER_NAME,
                            Tags.of("module", METRIC_MODULE_NAME, "connectionType", connectionType, "currentServer",
                                    currentServer, "rpcResultCode", rpcResultCode, "name", "rpcCostDuration"))
                    .record(duration, TimeUnit.MILLISECONDS);
        }
    }
    
    /**
     * Record the duration of a server request on the client.
     *
     * @param requestType request type, using getSimpleName() of the request class normally
     * @param duration    request duration, unit: ms
     */
    public static void recordHandleServerRequestCostDurationTimer(String requestType, long duration) {
        if (isEnable()) {
            MetricsMonitor.getNacosMeterRegistry().timer(TIMER_METER_NAME,
                    Tags.of("module", METRIC_MODULE_NAME, "requestType", requestType, "name",
                            "handleServerRequestCostDuration")).record(duration, TimeUnit.MILLISECONDS);
        }
    }
    
    // ------------------------ Others ------------------------
    
    public static String getMetricModuleName() {
        return METRIC_MODULE_NAME;
    }
    
    public static String getDefaultMeterName() {
        return DEFAULT_METER_NAME;
    }
    
    public static String getTimerMeterName() {
        return TIMER_METER_NAME;
    }
    
    public static String getCommonMeterName() {
        return COMMON_METER_NAME;
    }
    
    public static String getCounterMeterName() {
        return COUNTER_METER_NAME;
    }
}