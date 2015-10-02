package com.example;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.sqs.model.Message;

class Utils {
    private final UniqueIdentifierGenerator uniqueIdentifierGenerator;

    public Utils(UniqueIdentifierGenerator uniqueIdentifierGenerator) {
        this.uniqueIdentifierGenerator = uniqueIdentifierGenerator;
    }

    public Message convertStringToMessage(String message) {
        if (message == null) {
            throw new AmazonServiceException("Cannot push a null message to a queue");
        }

        Message awsMessage = new Message();
        awsMessage.setBody(message);
        awsMessage.setReceiptHandle(uniqueIdentifierGenerator.nextUniqueId());
        return awsMessage;
    }
}
