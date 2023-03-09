/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.flink.connector.rocketmq.legacy.common.util;

import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.metrics.Counter;
import org.apache.flink.metrics.Gauge;
import org.apache.flink.metrics.Meter;
import org.apache.flink.metrics.MeterView;
import org.apache.flink.metrics.SimpleCounter;

import java.io.Serializable;

/** RocketMQ connector metrics. */
public class MetricUtils {

    public static final String METRICS_TPS = "tps";
    // https://cwiki.apache.org/confluence/display/FLINK/FLIP-33%3A+Standardize+Connector+Metrics
    public static final String CURRENT_FETCH_EVENT_TIME_LAG = "currentFetchEventTimeLag";
    public static final String CURRENT_EMIT_EVENT_TIME_LAG = "currentEmitEventTimeLag";
    private static final String METRIC_GROUP_SOURCE = "source";
    private static final String METRIC_GROUP_SINK = "sink";
    private static final String METRICS_SINK_IN_TPS = "inTps";
    private static final String METRICS_SINK_OUT_BPS = "outBps";
    private static final String METRICS_SINK_OUT_LATENCY = "outLatency";
    public static final String SUFFIX_RATE = "PerSecond";
    public static final String IO_NUM_RECORDS_IN = "numRecordsIn";
    public static final String IO_NUM_RECORDS_OUT = "numRecordsOut";
    public static final String IO_NUM_RECORDS_IN_RATE = IO_NUM_RECORDS_IN + SUFFIX_RATE;
    public static final String IO_NUM_RECORDS_OUT_RATE = IO_NUM_RECORDS_OUT + SUFFIX_RATE;

    public static Meter registerSinkInTps(RuntimeContext context) {
        Counter parserCounter =
                context.getMetricGroup()
                        .addGroup(METRIC_GROUP_SINK)
                        .counter(METRICS_SINK_IN_TPS + "_counter", new SimpleCounter());
        return context.getMetricGroup()
                .addGroup(METRIC_GROUP_SINK)
                .meter(METRICS_SINK_IN_TPS, new MeterView(parserCounter, 60));
    }

    public static Meter registerOutBps(RuntimeContext context) {
        Counter bpsCounter =
                context.getMetricGroup()
                        .addGroup(METRIC_GROUP_SINK)
                        .counter(METRICS_SINK_OUT_BPS + "_counter", new SimpleCounter());
        return context.getMetricGroup()
                .addGroup(METRIC_GROUP_SINK)
                .meter(METRICS_SINK_OUT_BPS, new MeterView(bpsCounter, 60));
    }

    public static LatencyGauge registerOutLatency(RuntimeContext context) {
        return context.getMetricGroup()
                .addGroup(METRIC_GROUP_SINK)
                .gauge(METRICS_SINK_OUT_LATENCY, new LatencyGauge());
    }

    public static Meter registerNumRecordsInPerSecond(RuntimeContext context) {
        Counter numRecordsIn =
                context.getMetricGroup()
                        .addGroup(METRIC_GROUP_SOURCE)
                        .counter(IO_NUM_RECORDS_IN, new SimpleCounter());
        return context.getMetricGroup()
                .addGroup(METRIC_GROUP_SOURCE)
                .meter(IO_NUM_RECORDS_IN_RATE, new MeterView(numRecordsIn, 60));
    }

    public static Meter registerNumRecordsOutPerSecond(RuntimeContext context) {
        Counter numRecordsOut =
                context.getMetricGroup()
                        .addGroup(METRIC_GROUP_SINK)
                        .counter(IO_NUM_RECORDS_OUT, new SimpleCounter());
        return context.getMetricGroup()
                .addGroup(METRIC_GROUP_SINK)
                .meter(IO_NUM_RECORDS_OUT_RATE, new MeterView(numRecordsOut, 60));
    }

    public static class LatencyGauge implements Gauge<Double> {
        private double value;

        public void report(long timeDelta, long batchSize) {
            if (batchSize != 0) {
                this.value = (1.0 * timeDelta) / batchSize;
            }
        }

        @Override
        public Double getValue() {
            return value;
        }
    }

    public static class TimestampGauge implements Gauge<Long>, Serializable {
        private Long value;

        public void report(long delay) {
            this.value = delay;
        }

        @Override
        public Long getValue() {
            return value;
        }
    }
}
