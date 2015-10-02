package com.example;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.SetQueueAttributesRequest;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class SqsQueueTest extends AbstractMultiThreadQueueTest {
    @Override
    protected QueueService createFreshQueue() {
        return createQueueForNewThread(null);
    }

    @Override
    protected QueueService createQueueForNewThread(String queueURL) {
        BasicAWSCredentials credentials = new BasicAWSCredentials("AKIAJMF6TOUUWKPUXVXA", "esmnylDZdPGenTDiLRgoHbYRetfiefHOFF0p6Ubt");
        AmazonSQSClient client = new AmazonSQSClient(credentials);

        if (queueURL == null) {
            UniqueIdentifierGenerator uniqueIdentifierGenerator = new UniqueIdentifierGenerator();
            String uid = uniqueIdentifierGenerator.nextUniqueId();
            CreateQueueResult createQueueResult = client.createQueue(uid);
            queueURL = createQueueResult.getQueueUrl();
            Map<String, String> attributes = ImmutableMap.<String, String>builder().put(SqsQueueService.VISIBILITY_KEY, "5").build();
            SetQueueAttributesRequest setQueueAttributesRequest = new SetQueueAttributesRequest(queueURL, attributes);
            client.setQueueAttributes(setQueueAttributesRequest);
        }

        return new SqsQueueService(queueURL, client);
    }
}
