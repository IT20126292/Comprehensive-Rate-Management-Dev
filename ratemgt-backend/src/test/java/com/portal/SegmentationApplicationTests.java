package com.portal;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = com.sampath.portal.SegmentationApplication.class)
@Disabled("Context load integration test disabled for CI speed")
class SegmentationApplicationTests {

    @Test
    void contextLoads() {
    }
}
