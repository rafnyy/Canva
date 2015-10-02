package com.example;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;

import java.util.List;

public class SqsQueueService implements QueueService {
    private final AmazonSQSClient queue;
    private final String url;

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

        if (messages.size() != 0) {
            return messages.get(0);
        }

        return null;
    }

    @Override
    public void delete(String receiptHandle) {
        queue.deleteMessage(new DeleteMessageRequest(url, receiptHandle));
    }

    @Override
    public void deleteQueue() {
        queue.deleteQueue(url);
//        queue.purgeQueue(new PurgeQueueRequest(url));
//        try {
//            Thread.sleep(60000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public int getTimeout() {
        return 5000;
    }
}
