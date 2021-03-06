package com.gtohelper.utility;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

class CardResolverTest {

    @Test
    void resolveToString() {
        assertEquals("2c", CardResolver.resolveToString(1));
        assertEquals("9c", CardResolver.resolveToString(8));
        assertNotEquals("9c", CardResolver.resolveToString(9));
        assertEquals("Ad", CardResolver.resolveToString(26));
        assertEquals("2h", CardResolver.resolveToString(27));
        assertEquals("2s", CardResolver.resolveToString(40));
        assertEquals("As", CardResolver.resolveToString(52));
    }
}