package com.quicktap.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Kafka 配置
 * 支持异步任务和事件驱动两种模式
 *
 * 注意: 当前版本暂未启用 Kafka 功能
 * 如需启用，请取消注释下面的导入和配置代码
 */
@Configuration
public class KafkaConfig {

    /**
     * Kafka Listener Container Factory 配置（暂未启用）
     * 支持手动提交消息偏移量
     *
     * 启用方式:
     * 1. 取消注释下面的导入语句
     * 2. 添加 @EnableKafka 注解到类上
     * 3. 确保 Maven 已下载 spring-kafka 依赖
     *
     * import org.springframework.kafka.annotation.EnableKafka;
     * import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
     * import org.springframework.kafka.listener.ContainerProperties;
     * import org.springframework.kafka.listener.KafkaListenerContainerFactory;
     * import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
     * import org.springframework.kafka.core.ConsumerFactory;
     */
    /*
    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Object>>
            kafkaListenerContainerFactory(@Autowired ConsumerFactory<String, Object> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
            new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);
        // 设置并发消费者数量
        factory.setConcurrency(3);
        // 手动提交偏移量
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        return factory;
    }
    */
}
