package com.example;

import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.*;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class FileQueueService extends QueueWithOwnVisibilityTimer implements QueueService {
    private static final String invisibleSuffix = ".invisible";

    private final File queue;
    private final File invisible;
    private final Utils utils;
    private final ObjectMapper mapper;

    public FileQueueService(File queue, Utils utils, ObjectMapper mapper) {
        this.queue = queue;
        this.invisible = new File(queue.getAbsolutePath() + invisibleSuffix);
        this.utils = utils;
        this.mapper = mapper;
    }

    @Override
    public void push(String message) {
        Message awsMessage = utils.convertStringToMessage(message);
        try {
            String serializedMessage = serializeMessage(awsMessage);

            LockFile lock = new LockFile(queue);
            lock.lock();
            try (FileWriter fileWriter = new FileWriter(queue, true);
                 PrintWriter queueWriter = new PrintWriter(fileWriter)) {
                queueWriter.println(serializedMessage);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            lock.unlock();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Message pull() {
        String serializeMessage = null;

        LockFile queueLock = new LockFile(queue);
        queueLock.lock();

        // Get the first message in the queue
        try (Scanner queuePull = new Scanner(queue)) {
            serializeMessage = queuePull.nextLine();
        } catch (NoSuchElementException e) {
            // queue is empty, return null
            queueLock.unlock();
            return null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if(serializeMessage == null) {
            queueLock.unlock();
            return null;
        }

        // remove that message from the queue
        Message message = deserializeMessage(serializeMessage);
        removeLine(queue, message.getReceiptHandle());
        queueLock.unlock();

        LockFile invisibleLock = new LockFile(invisible);
        invisibleLock.lock();

        // write that message to the invisible File
        try (FileWriter fileWriter = new FileWriter(invisible, true);
             PrintWriter invisibleWriter = new PrintWriter(fileWriter)) {
            invisibleWriter.println(serializeMessage);
            startTimer(message.getReceiptHandle());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        invisibleLock.unlock();

        return message;
    }

    @Override
    public void delete(String receiptHandle) {
        removeLine(invisible, receiptHandle);
    }

    @Override
    public void deleteQueue() {
        queue.delete();
        invisible.delete();
    }

    @Override
    protected void reAdd(Message message) {
        LockFile queueLock = new LockFile(queue);
        queueLock.lock();
        // reAdd this message to the start of the queue File
        try (RandomAccessFile queueReAdd = new RandomAccessFile(queue, "rwd")) {
            queueReAdd.seek(0);
            queueReAdd.write(serializeMessage(message).getBytes());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        queueLock.unlock();
    }

    @Override
    protected Message getMessageFromInvisible(String receiptHandler) {
        Message message;

        LockFile invisibleLock = new LockFile(invisible);
        invisibleLock.lock();
        message = removeLine(invisible, receiptHandler);
        invisibleLock.unlock();

        return message;
    }

    /**
     * Removes the serialized Message line from the File file that has the same receiptHandle
     * @return the removed Message
     */
    private Message removeLine(File file, String receiptHandle) {
        StringBuilder stringBuilder = new StringBuilder("");
        Message message = null;

        try (Scanner reader = new Scanner(queue)) {
            String serializedMessage;

            while ((serializedMessage = reader.nextLine()) != null) {
                Message messageToCheck = deserializeMessage(serializedMessage);
                if (receiptHandle.equals(messageToCheck.getReceiptHandle())) {
                    message = messageToCheck;
                } else {
                    stringBuilder.append(serializedMessage).append("\n");
                }
            }
        } catch (NoSuchElementException e) {
            // reached the end of file, continue
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(stringBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return message;
    }

    private String serializeMessage(Message message) throws JsonProcessingException {
        mapper.configure(SerializationFeature.INDENT_OUTPUT, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        return mapper.writeValueAsString(message);
    }

    private Message deserializeMessage(String serializedMessage) {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try {
            return mapper.readValue(serializedMessage, Message.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}