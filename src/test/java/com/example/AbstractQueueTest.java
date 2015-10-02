package com.example;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.sqs.model.Message;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests that need to run on all implementations of QueueService
 */
public abstract class AbstractQueueTest {
    QueueService queue;

    /**
     * @return an empty implementation of a QueueService
     */
    protected abstract QueueService createFreshQueue();

    @Before
    public void before() {
        queue = createFreshQueue();
    }

    @Test
    public void testPushPull() {
        String body = "testPush";
        queue.push(body);
        assertEquals("Verifying that we can push and then pull a single element from a queue",
                body, queue.pull().getBody());
    }

    @Test
    public void testPullEmptyQueue() {
        assertEquals("Verifying that a pull from an empty queue returns null",
                null, queue.pull());
    }

    @Test(expected = AmazonServiceException.class)
    public void testPushNull() {
        queue.push(null);
    }

    @Test
    public void testDelete() {
        String body = "testDelete-one";
        queue.push(body);
        Message message = queue.pull();
        assertEquals("Verifying that we can push and then pull a single element from a queue",
                body, message.getBody());
        queue.delete(message.getReceiptHandle());
        assertEquals("Verifying that a delete actually removed the only element from the queue",
                null, queue.pull());
    }

    @Test
    public void testPushPullSameMessage() {
        String body = "testPushPullSameMessage-one";
        queue.push(body);
        queue.push(body);
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
    public void testVisibilityTimesOut() throws InterruptedException {
        String body = "testVisibilityTimesOut-one";
        queue.push(body);
        Message message = queue.pull();
        Thread.sleep(queue.getTimeout() * 2);
        assertEquals("Verifying that if we do not delete an element before the timeout, it reappears at the head of the queue",
                message.getBody(), queue.pull().getBody());
        queue.delete(message.getReceiptHandle());
        assertEquals("Verifying that a pull from an empty queue returns null",
                null, queue.pull());
    }

    @Test
    public void testFIFO() throws InterruptedException {
        String body1 = "testFIFO-one";
        queue.push(body1);
        Thread.sleep(12 * queue.getTimeout());
        String body2 = "testFIFO-two";
        queue.push(body2);
        Message message1 = queue.pull();
        queue.delete(message1.getReceiptHandle());
        Thread.sleep(12 * queue.getTimeout());
        Message message2 = queue.pull();
        queue.delete(message2.getReceiptHandle());
        assertEquals("Verifying that the first element we pull from the queue is the first element we pushed",
                body1, message1.getBody());
        assertEquals("Verifying that the second element we pull from the queue is the second element we pushed",
                body2, message2.getBody());
    }
}