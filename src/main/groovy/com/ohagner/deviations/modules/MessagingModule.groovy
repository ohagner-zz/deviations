package com.ohagner.deviations.modules

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.ohagner.deviations.config.Constants
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import groovy.util.logging.Slf4j

import static com.ohagner.deviations.config.AppConfig.envOrProperty

@Slf4j
class MessagingModule extends AbstractModule {

    MessagingModule.Config config

    @Override
    protected void configure() {
        config = MessagingModule.Config.getInstance()
        initializeQueues()
    }

    @Provides
    Channel createMessagingChannel() {
        ConnectionFactory connectionFactory = new ConnectionFactory()
        connectionFactory.setUri(config.connectionUri)
        connectionFactory.setAutomaticRecoveryEnabled(true)
        Connection connection = connectionFactory.newConnection()
        return connection.createChannel()
    }

    private void initializeQueues() {
        log.info "Initializing queues"
        createMessagingChannel().queueDeclare(Constants.WATCHES_TO_PROCESS_QUEUE_NAME, false, false, false, null)
    }

    static class Config {

        public static final String CONNECTION_URI = "RABBITMQ_CONNECTION_URI"

        String connectionUri

        static MessagingModule.Config getInstance() {
            MessagingModule.Config instance = new MessagingModule.Config()
            instance.with {
                connectionUri = envOrProperty(CONNECTION_URI)
            }
            return instance
        }
    }
}
