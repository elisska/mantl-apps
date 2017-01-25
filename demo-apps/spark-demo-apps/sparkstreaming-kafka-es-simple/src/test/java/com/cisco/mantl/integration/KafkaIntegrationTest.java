package com.cisco.mantl.integration;

/**
 * Created by dbort on 16.10.2015.
 */
import kafka.producer.KeyedMessage;
import org.junit.*;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class KafkaIntegrationTest {

    private KafkaUnit kafkaUnitServer;

    @Before
    public void setUp() throws Exception {
        kafkaUnitServer = new KafkaUnit(5000, 5001);
        kafkaUnitServer.startup();
    }

    @After
    public void shutdown() throws Exception {
        kafkaUnitServer.shutdown();
    }

    @Ignore
    @Test
    public void kafkaServerIsAvailable() throws Exception {
        //given
        String testTopic = "TestTopic";
        kafkaUnitServer.createTopic(testTopic);
        KeyedMessage<String, String> keyedMessage = new KeyedMessage<String, String>(testTopic, "key", "value");

        //when
        kafkaUnitServer.sendMessages(keyedMessage);
        List<String> messages = kafkaUnitServer.readMessages(testTopic, 1);

        //then
        assertEquals(Arrays.asList("value"), messages);
    }

    @Ignore
    @Test(expected = ComparisonFailure.class)
    public void shouldThrowComparisonFailureIfMoreMessagesRequestedThanSent() throws Exception {
        //given
        String testTopic = "TestTopic";
        kafkaUnitServer.createTopic(testTopic);
        KeyedMessage<String, String> keyedMessage = new KeyedMessage<String, String>(testTopic, "key", "value");

        //when
        kafkaUnitServer.sendMessages(keyedMessage);

        try {
            kafkaUnitServer.readMessages(testTopic, 2);
            fail("Expected ComparisonFailure to be thrown");
        } catch (ComparisonFailure e) {
            assertEquals("Wrong value for 'expected'", "2", e.getExpected());
            assertEquals("Wrong value for 'actual'", "1", e.getActual());
            assertEquals("Wrong error message", "Incorrect number of messages returned", e.getMessage());
        }
    }
}
