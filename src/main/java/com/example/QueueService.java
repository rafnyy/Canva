package com.example;

import com.amazonaws.services.sqs.model.Message;

public interface QueueService {

    /**
     * pushes a message onto a queue.
     *
     * @param message The actual message, gets placed in the Message object's body
     */
    void push(String message);

    /**
     * retrieves a single message from a queue.
     */
    Message pull();

    /**
     * deletes a message from the queue that was received by pull().
     *
     * @param receiptHandle unique ID receieved during pull()
     */
    void delete(String receiptHandle);

    String getUID();

    /**
     * Completely empties the queue, useful for testing.
     */
    void deleteQueue();

    int getTimeout();
}