package org.apache.flume.source.rocketmq;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.MQPushConsumer;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel;
import com.google.common.base.Preconditions;
import org.apache.flume.Context;

/**
 *
 */
public class RocketMQSourceUtil {

    /**
     * Topic配置项，如：a1.sources.r1.topic=TestTopic
     */
    public static final String TOPIC_CONFIG = "topic";
    /**
     * Tags配置项，如：a1.sources.r1.tags=Tag1,Tag2
     */
    public static final String TAGS_CONFIG = "tags";
    public static final String TAGS_DEFAULT = "*";
    /**
     * Tags header name configuration, eg: a1.sources.r1.tagsHeaderName=name
     */
    public static final String TAGS_HEADER_NAME_CONFIG = "tagsHeaderName";
    public static final String TAGS_HEADER_NAME_DEFAULT = "tags";
    /**
     * Topic header name configuration, eg: a1.sources.r1.topicHeaderName=name
     */
    public static final String TOPIC_HEADER_NAME_CONFIG = "topicHeaderName";
    public static final String TOPIC_HEADER_NAME_DEFAULT = "topic";
    /**
     * 一次最多拉取条数配置项，如：a1.sources.r1.maxNums=150
     */
    public static final String MAXNUMS_CONFIG = "maxNums";
    public static final int MAXNUMS_DEFAULT = 32;
    /**
     * Consumer分组配置项，如：a1.sources.r1.consumerGroup=please_rename_unique_group_name
     */
    public static final String CONSUMER_GROUP_CONFIG = "consumerGroup";
    public static final String CONSUMER_GROUP_DEFAULT = "DEFAULT_CONSUMER";
    /**
     * Namesrv地址配置项，如：a1.sinks.s1.namesrvAddr=localhost:9876
     */
    public static final String NAMESRV_ADDR_CONFIG = "namesrvAddr";
    /**
     * 订阅方式配置项，如：a1.sources.r1.messageModel=BROADCASTING
     */
    public static final String MESSAGEMODEL_CONFIG = "messageModel";
    public static final String MESSAGEMODEL_DEFAULT = "CLUSTERING";

    //es timestamp
    public static final String ES_TIMESTAMP = "timestamp";

    public static final String ES_LONGLOG_KEY = "longLogKey";
    public static final String ES_LONGLOG_KEY_DEFAULT = "LongLogKey";
    /**
     * elasticsearch 索引保存天数
     */
    public static final String ES_TTL_CONFIG = "esLongLogTTL";
    public static final int ES_TTL_DEFAULT = 30 ;//30d.

    public static MQPushConsumer getConsumer(Context context) {
        final String consumerGroup = context.getString(CONSUMER_GROUP_CONFIG, CONSUMER_GROUP_DEFAULT);
        final String namesrvAddr = Preconditions.checkNotNull(context.getString(NAMESRV_ADDR_CONFIG), "RocketMQ namesrvAddr must be specified. For example: a1.sources.r1.namesrvAddr=127.0.0.1:9876");

        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(consumerGroup);
        try {
            consumer.setNamesrvAddr(namesrvAddr);
            consumer.setMessageModel(MessageModel.valueOf(context.getString(MESSAGEMODEL_CONFIG, MESSAGEMODEL_DEFAULT)));
            consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
            consumer.subscribe(Preconditions.checkNotNull(context.getString(RocketMQSourceUtil.TOPIC_CONFIG)),
                    context.getString(RocketMQSourceUtil.TAGS_CONFIG, RocketMQSourceUtil.TAGS_DEFAULT));
        } catch (MQClientException e) {
            e.printStackTrace();
        }

        return consumer;
    }

}
