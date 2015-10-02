package com.example;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSClient;

public class SqsQueueTest extends AbstractMultiThreadQueueTest {
    @Override
    protected QueueService createFreshQueue() {
        QueueService queue = createQueueForNewThread();

        queue.purge();

        return queue;
    }

    @Override
    protected QueueService createQueueForNewThread() {
        BasicAWSCredentials credentials = new BasicAWSCredentials("AKIAJMF6TOUUWKPUXVXA", "esmnylDZdPGenTDiLRgoHbYRetfiefHOFF0p6Ubt");
        AmazonSQSClient client = new AmazonSQSClient(credentials);
        return new SqsQueueService(client, "https://sqs.us-west-2.amazonaws.com/028747645371/Canva");
    }
}
