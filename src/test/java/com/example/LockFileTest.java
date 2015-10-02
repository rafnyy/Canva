package com.example;

import com.amazonaws.services.sqs.model.Message;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LockFileTest {
    private String uid;
    private File file;
    private LockFile lockFile;

    private static final String lockSuffix = ".lock";
    private static final int holdLock = 1000;

    @Before
    public void setUp() throws Exception {
        UniqueIdentifierGenerator uniqueIdentifierGenerator = new UniqueIdentifierGenerator();
        uid = uniqueIdentifierGenerator.nextUniqueId();
        file = new File(uid);
        lockFile = new LockFile(file);
    }

    @After
    public void tearDown() throws Exception {
        file.delete();
    }

    @Test
    public void testLock() throws Exception {
        lockFile.lock();
        File lockFileExists = new File(file.getAbsolutePath() + lockSuffix);
        assert(lockFileExists.exists());
        lockFile.unlock();
    }

    @Test
    public void testUnlock() throws Exception {
        lockFile.lock();
        File lockFileExists = new File(file.getAbsolutePath() + lockSuffix);
        lockFile.unlock();
        assert(!lockFileExists.exists());
    }

    @Test
    public void testFileIsLocked() throws Exception {
        lockFile.lock();
        File lockFileExists = new File(file.getAbsolutePath() + lockSuffix);
        assert(lockFileExists.exists());
        startTimer(lockFile);
        File file2 = new File(file.getAbsolutePath());
        LockFile lockFile2 = new LockFile(file2);
        lockFile2.lock();
        assert(lockFileExists.exists());
        lockFile2.unlock();
    }

    void startTimer(LockFile lockFile) {
        Timer timer = new Timer();
        timer.schedule(new LockTimer(lockFile), 500);
    }

    class LockTimer extends TimerTask {
        private final LockFile lockFile;

         LockTimer(LockFile lockFile) {
            this.lockFile = lockFile;
        }

        public void run() {
            lockFile.unlock();
        }
    }
}