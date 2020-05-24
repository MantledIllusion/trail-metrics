package com.mantledillusion.metrics.trail;

import org.hamcrest.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

@WebAppConfiguration
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { TestMvcConfiguration.class })
public class TrailMetricsHttpServerInterceptorTest {

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
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void testCreateTrail() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/")).
                andExpect(MockMvcResultMatchers.header().string(TrailMetricsHttpServerInterceptor.DEFAULT_HEADER_NAME, UUID_MATCHER));

        this.mockMvc.perform(MockMvcRequestBuilders.get("/").
                header(TrailMetricsHttpServerInterceptor.DEFAULT_HEADER_NAME, TestRestController.TRAIL_ID)).
                andExpect(MockMvcResultMatchers.header().string(TrailMetricsHttpServerInterceptor.DEFAULT_HEADER_NAME, TestRestController.TRAIL_ID.toString()));
    }
}