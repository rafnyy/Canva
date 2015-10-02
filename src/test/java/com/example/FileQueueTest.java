package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Timer;

public class FileQueueTest extends AbstractMultiThreadQueueTest {

    @Override
    protected QueueService createFreshQueue() {
        UniqueIdentifierGenerator uniqueIdentifierGenerator = new UniqueIdentifierGenerator();
        String uid = uniqueIdentifierGenerator.nextUniqueId();

        return createQueueForNewThread(uid);
    }

    @Override
    protected QueueService createQueueForNewThread(String uid) {
        return new FileQueueService(uid, new Utils(new UniqueIdentifierGenerator()), new Timer(), new ObjectMapper());
    }
}