package com.example;

import com.amazonaws.services.sqs.model.Message;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests that need to run on all implementations of QueueService in which the queue can be modified by multiple JVMs
 */
public abstract class AbstractMultiThreadQueueTest extends AbstractQueueTest {
    /**
     * This creates a new QueueService object that references an existing queue.
     * This queue has no guarantees on what is in it and it is expected that other threads may be modifying it.
     *
     * @return a new QueueService pointing at an existing queue
     */
    protected abstract QueueService createQueueForNewThread();

    @Test
    public void testPushFromTwoProducers() {
        String body = "testPushFromTwoProducers";
        queue.push(body);
        QueueService queue2 = createQueueForNewThread();
        queue2.push(body);
        Message message1 = queue.pull();
        queue.delete(message1.getReceiptHandle());
        Message message2 = queue.pull();
        queue.delete(message2.getReceiptHandle());
        assertEquals("Verifying that the first element we pull from the queue is the first element we pushed",
                body, message1.getBody());
        assertEquals("Verifying that the second element we pull from the queue is the second element we pushed",
                body, message2.getBody());
    }

    @Test
    public void testPullFromTwoConsumers() {
        String body = "testPullFromTwoConsumers";
        queue.push(body);
        queue.push(body);
        Message message1 = queue.pull();
        queue.delete(message1.getReceiptHandle());
        QueueService queue2 = createQueueForNewThread();
        Message message2 = queue2.pull();
        queue2.delete(message2.getReceiptHandle());
        assertEquals("Verifying that the first element we pull from the queue is the first element we pushed",
                body, message1.getBody());
        assertEquals("Verifying that the second element we pull from the queue is the second element we pushed",
                body, message2.getBody());
    }

    @Test
    public void testVisibilityTimesOutDifferentProducerAndConsumer() throws InterruptedException {
        String body = "testVisibilityTimesOutDifferentProducerAndConsumer";
        queue.push(body);
        Message message = queue.pull();
        Thread.sleep(queue.getTimeout() * 2);
        QueueService queue2 = createQueueForNewThread();
        assertEquals("Verifying that if we do not delete an element before the timeout, it reappears at the head of the queue",
                message.getBody(), queue2.pull().getBody());
        queue2.delete(message.getReceiptHandle());
        assertEquals("Verifying that a pull from an empty queue returns null",
                null, queue2.pull());
    }

    @After
    public void cleanup() {
        queue.deleteQueue();
    }
}
