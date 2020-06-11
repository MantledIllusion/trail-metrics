package com.mantledillusion.metrics.trail;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { TestJmsConfiguration.class })
public class TrailMetricsJmsMessageConverterWrapperTest {

    private static final String MESSAGE = "message";

    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private TestJmsReceiver receiver;
    @Autowired
    private TrailMetricsJmsMessageConverterWrapper wrapper;

    private String testName;

    private String determineTestName() {
        return Thread.currentThread().getStackTrace()[2].getMethodName();
    }

    @Test
    public void testSendStrict() {
        this.wrapper.setIncomingMode(TrailBehaviourMode.OPTIONAL);
        this.wrapper.setOutgoingMode(TrailBehaviourMode.STRICT);

        String testName = determineTestName();
        Assertions.assertThrows(MessageConversionException.class,
                () -> this.jmsTemplate.convertAndSend(TestJmsReceiver.QUEUE, testName));
    }

    @Test
    public void testSendLenient() {
        this.wrapper.setIncomingMode(TrailBehaviourMode.OPTIONAL);
        this.wrapper.setOutgoingMode(TrailBehaviourMode.LENIENT);

        String testName = determineTestName();
        this.jmsTemplate.convertAndSend(TestJmsReceiver.QUEUE, testName);
        Assertions.assertNotNull(this.receiver.await(testName));
    }

    @Test
    public void testSendOptional() {
        this.wrapper.setIncomingMode(TrailBehaviourMode.OPTIONAL);
        this.wrapper.setOutgoingMode(TrailBehaviourMode.OPTIONAL);

        String testName = determineTestName();
        this.jmsTemplate.convertAndSend(TestJmsReceiver.QUEUE, testName);
        Assertions.assertNull(this.receiver.await(testName));
    }

    @Test
    public void testReceiveLenient() {
        this.wrapper.setIncomingMode(TrailBehaviourMode.LENIENT);
        this.wrapper.setOutgoingMode(TrailBehaviourMode.OPTIONAL);

        String testName = determineTestName();
        this.jmsTemplate.convertAndSend(TestJmsReceiver.QUEUE, testName);
        Assertions.assertNotNull(this.receiver.await(testName));
    }
}