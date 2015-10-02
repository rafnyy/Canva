package com.example;

import org.junit.Test;

import java.util.Timer;

import static org.junit.Assert.assertEquals;

public class InMemoryQueueTest extends AbstractQueueTest {
    @Override
    protected QueueService createFreshQueue() {
        return new InMemoryQueueService(new Utils(new UniqueIdentifierGenerator()), new Timer());
    }

    @Test
    public void testGetUID() {
        assertEquals("Verifying that getUID() always returns null since it is not applicable for this implementation",
                null, queue.getUID());
    }
}