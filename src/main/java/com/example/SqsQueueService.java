package com.example;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.*;

import java.util.Arrays;
import java.util.List;

public class SqsQueueService implements QueueService {
    public final static String VISIBILITY_KEY = "VisibilityTimeout";

    private final AmazonSQSClient queue;
    private final String url;

    private static final int milliseconds = 1000;

    public SqsQueueService(String url, AmazonSQSClient sqsClient) {
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
    public String getUID() {
        return url;
    }

    @Override
    public void deleteQueue() {
        queue.deleteQueue(url);
    }

    @Override
    public int getTimeout() {
        GetQueueAttributesResult queueAttributes = queue.getQueueAttributes(new GetQueueAttributesRequest(url, Arrays.asList(VISIBILITY_KEY)));
        return Integer.parseInt(queueAttributes.getAttributes().get(VISIBILITY_KEY)) * milliseconds;
    }
}