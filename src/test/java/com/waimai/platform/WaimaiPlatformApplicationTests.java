package com.waimai.platform;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class WaimaiPlatformApplicationTests {

    @Test
    void javaRuntimeIsSupported() {
        assertTrue(Runtime.version().feature() >= 21);
    }
}
