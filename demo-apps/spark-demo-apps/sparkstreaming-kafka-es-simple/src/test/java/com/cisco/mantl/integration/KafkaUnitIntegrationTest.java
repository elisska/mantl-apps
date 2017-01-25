package com.cisco.mantl.integration;

/**
 * Created by dbort on 16.10.2015.
 *
 */
import kafka.producer.KeyedMessage;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class KafkaUnitIntegrationTest {

    @Rule
    public KafkaUnitRule kafkaUnitRule = new KafkaUnitRule(6000, 6001);

    @Ignore
    @Test
    public void junitRuleShouldHaveStartedKafka() throws Exception {
        //given
        String testTopic = "TestTopic";
        kafkaUnitRule.getKafkaUnit().createTopic(testTopic);
        KeyedMessage<String, String> keyedMessage = new KeyedMessage<String, String>(testTopic, "key", "value");

        //when
        kafkaUnitRule.getKafkaUnit().sendMessages(keyedMessage);
        List<String> messages = kafkaUnitRule.getKafkaUnit().readMessages(testTopic, 1);

        //then
        assertEquals(Arrays.asList("value"), messages);

    }
}
