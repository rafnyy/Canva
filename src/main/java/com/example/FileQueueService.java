package com.example;

import com.amazonaws.services.sqs.model.Message;

import java.io.File;

public class FileQueueService implements QueueService {
    private File queue;

    public FileQueueService(File queue) {
        this.queue = queue;
    }

    @Override
    public void push(String message) {

    }

    @Override
    public Message pull() {
        return null;
    }

    @Override
    public void delete(String receiptHandle) {

    }

    @Override
    public void purge() {
        queue.delete();
    }

    @Override
    public int getTimeout() {
        return timeout;
    }
}