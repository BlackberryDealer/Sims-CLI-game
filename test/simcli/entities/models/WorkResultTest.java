package simcli.entities.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@link WorkResult} value object — factory methods and getters.
 */
@DisplayName("WorkResult — Factory Method & Value Object Tests")
public class WorkResultTest {

    @Test
    @DisplayName("success() creates a successful result with correct fields")
    void testSuccessFactory() {
        WorkResult result = WorkResult.success(150, true, false);
        assertTrue(result.isSuccess());
        assertEquals(150, result.getEarnings());
        assertTrue(result.isPromoted());
        assertFalse(result.isOverworked());
    }

    @Test
    @DisplayName("failure() creates a failed result with zero earnings")
    void testFailureFactory() {
        WorkResult result = WorkResult.failure("Too tired");
        assertFalse(result.isSuccess());
        assertEquals(0, result.getEarnings());
        assertFalse(result.isPromoted());
        assertFalse(result.isOverworked());
        assertEquals("Too tired", result.getMessage());
    }

    @Test
    @DisplayName("success() with overwork flag set correctly")
    void testOverworkFlag() {
        WorkResult result = WorkResult.success(100, false, true);
        assertTrue(result.isSuccess());
        assertTrue(result.isOverworked());
        assertFalse(result.isPromoted());
    }
}
