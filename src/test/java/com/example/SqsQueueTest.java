package com.example;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.SetQueueAttributesRequest;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * These are actually functional tests instead of unit tests. These are not intended to run as part of continuous
 * integration since they are not guaranteed to pass because of the dependency on the outside service, SQS.
 *
 * What these are valuable for is sporadic or nightly runs to check if SQS service starts functioning differently and
 * we would need to adjust our implementations of the other QueueServices.
 *
 * This test class is also valuable to develop all the tests in the abstract test classes. The idea is to get the
 * abstract tests classes working with the SQS tests first. Then we can do test driven development on the other QueueServices
 * and be confident that they are functioning the same as SQS.
 */
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
