flume 日志收集：
	1.flume-rocketmq-source  flume 从rocketmq 拉取消息
	2.flume-ng-elasticsearch-sink flume 将日志数据写入es
	改动要点:
		a). flume-rocketmq-source: 将mq消息消费模式由pull改为push模式，并加入解析天网消息格式 (主要代码文件：RocketMQSource.java)
		b). flume-ng-elasticsearch-sink: 统一日志索引分离----正常索引，异常索引，长日志索引
		    1).  根据日志消息字段进行正常，异常，长日志判断，将消息分别写到不同索引 (主要代码文件：ElasticSearchSink.java)
		    2).  根据日志消息字段，创建不同类型索引 (主要代码文件: TimeBasedIndexNameBuilder.java)
		    3).  将mq消息内容安按照json格式重新解析，并写入 (主要代码文件:ElasticSearchLogStashEventSerializer.java)



	3.线上环境和线下环境flume配置文件：
	    a). flume-conf/online -> 线上环境配置文件 (by 2016-04-14)
	    b). flume-conf/offline -> 线下环境配置文件 (by 2016-04-14)
