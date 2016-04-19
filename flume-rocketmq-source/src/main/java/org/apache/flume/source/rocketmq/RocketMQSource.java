package org.apache.flume.source.rocketmq;

import com.alibaba.rocketmq.client.consumer.MQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListener;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.message.MessageQueue;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.PollableSource;
import org.apache.flume.conf.Configurable;
import org.apache.flume.event.SimpleEvent;
import org.apache.flume.source.AbstractSource;
import org.joda.time.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Desc flume rocketMQ source
 */
public class RocketMQSource extends AbstractSource implements Configurable, PollableSource {

    private static final Logger LOG = LoggerFactory.getLogger(RocketMQSource.class);

    private String topicHeaderName;
    private String tagsHeaderName;
    private int maxNums;
    private MQPushConsumer consumer;
    private String longLogKey;
    private int esTTL ;

    protected final BlockingQueue<List<MessageExt>> batchQueue  = new LinkedBlockingQueue<>();

    @Override
    public void configure(Context context) {
        // 初始化配置项
        topicHeaderName = context.getString(RocketMQSourceUtil.TOPIC_HEADER_NAME_CONFIG, RocketMQSourceUtil.TOPIC_HEADER_NAME_DEFAULT);
        tagsHeaderName = context.getString(RocketMQSourceUtil.TAGS_HEADER_NAME_CONFIG, RocketMQSourceUtil.TAGS_HEADER_NAME_DEFAULT);
        maxNums = context.getInteger(RocketMQSourceUtil.MAXNUMS_CONFIG, RocketMQSourceUtil.MAXNUMS_DEFAULT);
        longLogKey = context.getString(RocketMQSourceUtil.ES_LONGLOG_KEY,RocketMQSourceUtil.ES_LONGLOG_KEY_DEFAULT);
        esTTL = context.getInteger(RocketMQSourceUtil.ES_TTL_CONFIG,RocketMQSourceUtil.ES_TTL_DEFAULT);

        // 初始化Consumer
        consumer = Preconditions.checkNotNull(RocketMQSourceUtil.getConsumer(context));
        consumer.registerMessageListener((MessageListenerConcurrently) buildMessageListener());

    }

    @Override
    public Status process() throws EventDeliveryException {
        List<Event> eventList = Lists.newArrayList();
        try {
            List<MessageExt> msgList = new ArrayList<>();
            try {
                msgList = batchQueue.take();
            } catch (InterruptedException e) {
                LOG.error("Msg take exception:"+e.getMessage());
                return Status.BACKOFF;
            }
            if (msgList == null) {
                return Status.BACKOFF;
            }

            for(MessageExt messageExt:msgList){
                Event event = new SimpleEvent();

                Map<String, String>  headers = Maps.newHashMap();
                headers.put(topicHeaderName, messageExt.getTopic());
                headers.put(tagsHeaderName, messageExt.getTags());
                headers.put(RocketMQSourceUtil.ES_TIMESTAMP, String.valueOf(DateTimeUtils.currentTimeMillis()));

                String  skyNetMessageBody = new String(messageExt.getBody(),"utf-8");
                List<String> splits = Lists.newArrayList(
                        Splitter.on('\t').trimResults()
                                .split(skyNetMessageBody));

                //get message body
                if(splits.size() < 4){
                    LOG.error("Invalid format message: " + skyNetMessageBody);
                    continue;
                }

                String messageBody = splits.get(3);

                if(LOG.isDebugEnabled()){
                    LOG.debug("MQ Received message body: {}",messageBody);
                }

                if(CommonUtils.isBadJson(messageBody)){
                    LOG.error("Invalid json format message: " + messageBody);
                    continue;
                }

                //event.setBody(messageExt.getBody());
                event.setBody(messageBody.getBytes());
                event.setHeaders(headers);
                eventList.add(event);
            }

            getChannelProcessor().processEventBatch(eventList);

        } catch (Exception e) {
            LOG.error("RocketMQSource consume message exception", e);
            return Status.BACKOFF;
        }
        return Status.READY;
    }

    @Override
    public synchronized void start() {
        try {
            // 启动Consumer
            consumer.start();
        } catch (MQClientException e) {
            LOG.error("RocketMQSource start consumer failed", e);
            Throwables.propagate(e);
        }
        super.start();
    }

    @Override
    public synchronized void stop() {
        // 停止Consumer
        consumer.shutdown();
        super.stop();
    }


    /**
     * buildMessageListener
     * @return
     */
    public MessageListener buildMessageListener() {
        MessageListener listener = new MessageListenerConcurrently() {
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                                                            ConsumeConcurrentlyContext context) {
                boolean isSuccess = RocketMQSource.this.consumeMessage(msgs,
                        context.getMessageQueue());

                if (isSuccess) {
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                } else {
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            }

        };
        LOG.debug("Successfully create concurrently listener !");
        return listener;
    }


    /**
     * consumeMessage
     * @param msgs
     * @param mq
     * @return
     */
    public boolean consumeMessage(List<MessageExt> msgs, MessageQueue mq) {
        LOG.debug("Receiving {} messages {} from MQ {} !", new Object[] { msgs.size(), msgs, mq });

        if (msgs == null || msgs.isEmpty()) {
            return true;
        }

        return  batchQueue.offer(msgs);

    }


}
