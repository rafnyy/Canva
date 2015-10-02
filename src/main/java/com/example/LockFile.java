package com.example;

import java.io.File;
import java.io.IOException;

class LockFile {
    private final static String lockSuffix = ".lock";

    private final File lockFile;

    public LockFile(File file){
        this.lockFile = new File(file.getAbsolutePath() + lockSuffix);
    }

    /**
     * Lock the File file, by creating an empty lock file.
     * If the lock file already exists, wait until it does not.
     *
     * Simple implementation can lead to deadlocks, but since this is intended for dev and test environments
     * we are not concerned with malicious actors holding onto the lock indefinitely.
     * If a dead lock does happen, then that means there is a bug in the code that should be addressed else where.
     */
    public void lock() {
        while(lockFile.exists()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            lockFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete the lock file associated with File file.
     */
    public void unlock() {
        lockFile.delete();
    }
}
