package com.example;

import java.io.File;

public class FileQueueTest extends AbstractMultiThreadQueueTest {
    private File file = new File("FileQueue");

    @Override
    protected QueueService createFreshQueue() {
        QueueService queue = createQueueForNewThread();
        queue.purge();
        return queue;
    }

    @Override
    protected QueueService createQueueForNewThread() {
        return new FileQueueService(file);
    }
}