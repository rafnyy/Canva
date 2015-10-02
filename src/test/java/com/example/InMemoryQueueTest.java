package com.example;

import java.util.Timer;

public class InMemoryQueueTest extends AbstractQueueTest {
    @Override
    protected QueueService createFreshQueue() {
        return new InMemoryQueueService(new Utils(new UniqueIdentifierGenerator()), new Timer());
    }
}