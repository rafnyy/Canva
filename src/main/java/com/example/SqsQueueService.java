package com.example;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.*;

import java.util.List;

public class SqsQueueService implements QueueService {
    private AmazonSQSClient queue;
    private String url;

    public SqsQueueService(AmazonSQSClient sqsClient, String url) {
        this.queue = sqsClient;
        this.url = url;
    }

    @Override
    public void push(String message) {
        queue.sendMessage(new SendMessageRequest(url, message));
    }

    @Override
    public Message pull() {
        List<Message> messages = queue.receiveMessage(url).getMessages();

        if(messages.size() != 0) {
            return messages.get(0);
        }

        return null;
    }

    @Override
    public void delete(String receiptHandle) {
        queue.deleteMessage(new DeleteMessageRequest(url, receiptHandle));
    }

    @Override
    public void purge() {
        queue.purgeQueue(new PurgeQueueRequest(url));
        try {
            Thread.sleep(60000);
        }
        catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getTimeout() {
        return timeout;
    }
}
