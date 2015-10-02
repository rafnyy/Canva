package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.Timer;

public class FileQueueTest extends AbstractMultiThreadQueueTest {
    private File file;

    @Override
    protected QueueService createFreshQueue() {
        UniqueIdentifierGenerator uniqueIdentifierGenerator = new UniqueIdentifierGenerator();
        String uid = uniqueIdentifierGenerator.nextUniqueId();

        file  = new File(uid);
        return createQueueForNewThread();
    }

    @Override
    protected QueueService createQueueForNewThread() {
        return new FileQueueService(new Timer(), file, new Utils(new UniqueIdentifierGenerator()), new ObjectMapper());
    }
}