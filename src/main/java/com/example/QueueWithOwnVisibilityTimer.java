package com.example;

import com.amazonaws.services.sqs.model.Message;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

abstract class QueueWithOwnVisibilityTimer {
    private final int timeout = 1000;

    protected abstract void reAdd(Message e);

    protected abstract Message getMessageFromInvisible(String receiptHandler);

    void startTimer(String receiptHandle) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new InvisibilityTimer(receiptHandle));
    }

    /**
     * This class will sleep until the timeout is up for a Message, if if the timeout is hit and the Message is still
     * invisible, it will reinsert it at the front of the queue.
     */
    class InvisibilityTimer implements Runnable {
        private final String receiptHandler;

        InvisibilityTimer(String receiptHandler) {
            this.receiptHandler = receiptHandler;
        }

        public void run() {
            try {
                Thread.sleep(timeout);
                Message message = getMessageFromInvisible(receiptHandler);
                if (message != null) {
                    reAdd(message);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public int getTimeout() {
        return timeout;
    }
}