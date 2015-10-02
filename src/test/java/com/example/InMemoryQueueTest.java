package com.example;

public class InMemoryQueueTest extends AbstractQueueTest {
    @Override
    protected QueueService createFreshQueue() {
        return new InMemoryQueueService(new Utils(new UniqueIdentifierGenerator()));
    }
}