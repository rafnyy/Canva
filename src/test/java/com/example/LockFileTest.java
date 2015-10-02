package com.example;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;


public class LockFileTest {
    private File file;
    private LockFile lockFile;

    private static final String lockSuffix = ".lock";
    private static final int holdLock = 500;

    @Before
    public void setUp() {
        UniqueIdentifierGenerator uniqueIdentifierGenerator = new UniqueIdentifierGenerator();
        String uid = uniqueIdentifierGenerator.nextUniqueId();
        file = new File(uid);
        lockFile = new LockFile(file);
    }

    @After
    public void tearDown() {
        file.delete();
    }

    @Test
    public void testLock() {
        lockFile.lock();
        File lockFileExists = new File(file.getAbsolutePath() + lockSuffix);
        assert(lockFileExists.exists());
        lockFile.unlock();
    }

    @Test
    public void testUnlock() {
        lockFile.lock();
        File lockFileExists = new File(file.getAbsolutePath() + lockSuffix);
        lockFile.unlock();
        assert(!lockFileExists.exists());
    }

    @Test
    public void testFileIsLocked() {
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

    private void startTimer(LockFile lockFile) {
        Timer timer = new Timer();
        timer.schedule(new LockTimer(lockFile), holdLock);
    }

    private class LockTimer extends TimerTask {
        private final LockFile lockFile;

         LockTimer(LockFile lockFile) {
            this.lockFile = lockFile;
        }

        public void run() {
            lockFile.unlock();
        }
    }
}