package com.example;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.sqs.model.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InMemoryQueueService implements QueueService {
    private ConcurrentLinkedDeque<Message> queue;
    private Map<String, Message> invisible;

    private UniqueIdentifierGenerator uniqueIdentifierGenerator;

    public InMemoryQueueService(UniqueIdentifierGenerator uniqueIdentifierGenerator) {
        purge();
        this.uniqueIdentifierGenerator = uniqueIdentifierGenerator;
    }

    @Override
    public void push(String message) {
        if(message == null) {
            throw new AmazonServiceException("Cannot push a null message to a queue");
        }

        Message awsMessage = new Message();
        awsMessage.setBody(message);
        awsMessage.setReceiptHandle(uniqueIdentifierGenerator.nextUniqueId());
        queue.addLast(awsMessage);
    }

    @Override
    public Message pull() {
        Message message;
        try {
            message = queue.remove();
        }
        catch (NoSuchElementException e) {
            return null;
        }

        String receiptHandle = message.getReceiptHandle();
        invisible.put(receiptHandle, message);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new InvisibilityTimer(receiptHandle));

        return message;
    }

    @Override
    public void delete(String receiptHandle) {
        invisible.remove(receiptHandle);
    }

    @Override
    public void purge() {
        queue = new ConcurrentLinkedDeque<>();
        invisible = new HashMap<>();
    }

    @Override
    public int getTimeout() {
        return timeout;
    }

    private void reAdd(Message e) {
        queue.addFirst(e);
    }

    /**
     * This class will sleep until the timeout is up for a Message, if if the timeout is hit and the Message is still
     * invisible, it will reinsert it at the front of the queue.
     */
    private class InvisibilityTimer implements Runnable {
        private String receiptHandler;

        protected InvisibilityTimer(String receiptHandler) {
            this.receiptHandler = receiptHandler;
        }

        public void run() {
            try {
                Thread.sleep(timeout);
                Message message = invisible.get(receiptHandler);
                if (message != null) {
                    reAdd(message);
                }
            } catch (InterruptedException e) {
               e.printStackTrace();
            }
        }
    }
}