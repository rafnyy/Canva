package com.example;

import com.amazonaws.services.sqs.model.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.concurrent.ConcurrentLinkedDeque;

public class InMemoryQueueService extends QueueWithOwnVisibilityTimer implements QueueService {
    private ConcurrentLinkedDeque<Message> queue;
    private Map<String, Message> invisible;

    private final Utils utils;

    public InMemoryQueueService(Utils utils, Timer timer) {
        super(timer);
        deleteQueue();
        this.utils = utils;
    }

    @Override
    public void push(String message) {
        Message awsMessage = utils.convertStringToMessage(message);
        queue.addLast(awsMessage);
    }

    @Override
    public Message pull() {
        Message message;
        try {
            message = queue.remove();
        } catch (NoSuchElementException e) {
            return null;
        }

        String receiptHandle = message.getReceiptHandle();
        invisible.put(receiptHandle, message);

        startTimer(receiptHandle);

        return message;
    }

    @Override
    public void delete(String receiptHandle) {
        invisible.remove(receiptHandle);
    }

    @Override
    public String getUID() {
        return null;
    }

    @Override
    public void deleteQueue() {
        queue = new ConcurrentLinkedDeque<>();
        invisible = new HashMap<>();
    }

    @Override
    protected void reAdd(Message e) {
        queue.addFirst(e);
    }

    @Override
    protected Message getMessageFromInvisible(String receiptHandler) {
        return invisible.get(receiptHandler);
    }
}