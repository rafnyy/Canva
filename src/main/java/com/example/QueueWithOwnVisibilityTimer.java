package com.example;

import com.amazonaws.services.sqs.model.Message;

import java.util.Timer;
import java.util.TimerTask;

abstract class QueueWithOwnVisibilityTimer {
    private final int timeout = 1000;

    protected abstract void reAdd(Message e);

    protected abstract Message getMessageFromInvisible(String receiptHandler);

    void startTimer(String receiptHandle) {
        Timer timer = new Timer();
        timer.schedule(new InvisibilityTimer(receiptHandle), timeout);
    }

    /**
     * TimerTask that will check if the Message is still
     * invisible, and if so,  will reinsert it at the front of the queue.
     */
    class InvisibilityTimer extends TimerTask {
        private final String receiptHandler;

        InvisibilityTimer(String receiptHandler) {
            this.receiptHandler = receiptHandler;
        }

        public void run() {
            Message message = getMessageFromInvisible(receiptHandler);
            if (message != null) {
                reAdd(message);
            }
        }
    }

    public int getTimeout() {
        return timeout;
    }
}