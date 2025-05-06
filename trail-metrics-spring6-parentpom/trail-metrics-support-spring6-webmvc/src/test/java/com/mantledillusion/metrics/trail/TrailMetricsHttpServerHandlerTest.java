package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Event;
import com.mantledillusion.metrics.trail.api.Measurement;
import com.mantledillusion.metrics.trail.api.MeasurementType;
import org.hamcrest.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opentest4j.AssertionFailedError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@WebAppConfiguration
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestMvcConfiguration.class)
@TestPropertySource(locations = "/application.properties")
public class TrailMetricsHttpServerHandlerTest {

    private static final Matcher<String> UUID_MATCHER = new TypeSafeMatcher<String>() {

        @Override
        protected boolean matchesSafely(String item) {
            try {
                UUID.fromString(item);
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

        @Override
        public void describeTo(Description description) {}
    };

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void testSupportedEndpoint() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/supported")).
                andExpect(MockMvcResultMatchers.header().string(TrailMetricsHttpServerInterceptor.DEFAULT_HEADER_NAME, UUID_MATCHER));

        this.mockMvc.perform(MockMvcRequestBuilders.get("/supported").
                header(TrailMetricsHttpServerInterceptor.DEFAULT_HEADER_NAME, TestRestController.TRAIL_ID)).
                andExpect(MockMvcResultMatchers.header().string(TrailMetricsHttpServerInterceptor.DEFAULT_HEADER_NAME, TestRestController.TRAIL_ID.toString()));
    }

    @Test
    public void testUnsupportedEndpoint() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/unsupported")).
                andExpect(MockMvcResultMatchers.header().doesNotExist(TrailMetricsHttpServerInterceptor.DEFAULT_HEADER_NAME));
    }

    @Test
    public void testParameteredEndpoint() throws Exception {
        AtomicReference<Event> eventReference = new AtomicReference<>();
        MetricsTrailConsumer consumer = MetricsTrailConsumer.from("test", (consumerId, correlationId, event) -> eventReference.set(event));
        MetricsTrailSupport.addPersistentHook(consumer, MetricsTrailListener.ReferenceMode.HARD);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/parametered/{first}/subpath/{second}", 123, 456)).andReturn();

        MetricsTrailSupport.removePersistentHook(consumer);

        Event event = eventReference.get();
        Assertions.assertNotNull(event);
        Assertions.assertNotNull(event.getMeasurements());

        Measurement method = assertMeasurement(event, "method");
        Assertions.assertEquals(MeasurementType.STRING, method.getType());
        Assertions.assertEquals("POST", method.getValue());

        Measurement endpoint = assertMeasurement(event, "endpoint");
        Assertions.assertEquals(MeasurementType.STRING, endpoint.getType());
        Assertions.assertEquals("/parametered/{0}/subpath/{1}", endpoint.getValue());

        Measurement parameter0 = assertMeasurement(event, "parameters.0");
        Assertions.assertEquals(MeasurementType.STRING, parameter0.getType());
        Assertions.assertEquals("123", parameter0.getValue());

        Measurement parameter1 = assertMeasurement(event, "parameters.1");
        Assertions.assertEquals(MeasurementType.STRING, parameter1.getType());
        Assertions.assertEquals("456", parameter1.getValue());

    }

    private Measurement assertMeasurement(Event event, String key) {
        return event.getMeasurements().stream()
                .filter(measurement -> key.equals(measurement.getKey()))
                .reduce((a, b) -> {
                    throw new AssertionFailedError();
                })
                .orElseThrow(AssertionFailedError::new);
    }
}