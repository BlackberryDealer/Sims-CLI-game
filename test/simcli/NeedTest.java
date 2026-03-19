package simcli;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import simcli.needs.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Need class hierarchy refactor.
 *
 * Coverage:
 *  - All concrete needs start at MAX_VALUE (100)
 *  - increase() clamps at MAX_VALUE
 *  - decrease() clamps at 0
 *  - decay(multiplier) reduces value by baseDecayRate * multiplier
 *  - Each subclass (Hunger, Energy, Hygiene, Fun, Social) has correct decay rate
 *  - setValue() clamps within [0, MAX_VALUE]
 */
@DisplayName("Need Hierarchy — Base Decay Logic Tests")
public class NeedTest {

    // -----------------------------------------------------------------------
    // Initial value
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("All needs start at MAX_VALUE (100)")
    void testAllNeedsStartAtMax() {
        assertEquals(Need.MAX_VALUE, new Hunger().getValue());
        assertEquals(Need.MAX_VALUE, new Energy().getValue());
        assertEquals(Need.MAX_VALUE, new Hygiene().getValue());
        assertEquals(Need.MAX_VALUE, new Fun().getValue());
        assertEquals(Need.MAX_VALUE, new Social().getValue());
    }

    // -----------------------------------------------------------------------
    // decay() uses base decay rate
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Hunger.decay(1.0) decreases by its base decay rate (5)")
    void testHungerDecayRate() {
        Hunger h = new Hunger();
        h.decay(1.0);
        assertEquals(Need.MAX_VALUE - 5, h.getValue(),
                "Hunger base decay rate is 5");
    }

    @Test
    @DisplayName("Energy.decay(1.0) decreases by its base decay rate (2)")
    void testEnergyDecayRate() {
        Energy e = new Energy();
        e.decay(1.0);
        assertEquals(Need.MAX_VALUE - 2, e.getValue(),
                "Energy base decay rate is 2");
    }

    @Test
    @DisplayName("Hygiene.decay(1.0) decreases by its base decay rate (5)")
    void testHygieneDecayRate() {
        Hygiene hy = new Hygiene();
        hy.decay(1.0);
        assertEquals(Need.MAX_VALUE - 5, hy.getValue(),
                "Hygiene base decay rate is 5");
    }

    @Test
    @DisplayName("Fun.decay(1.0) decreases by its base decay rate (2)")
    void testFunDecayRate() {
        Fun f = new Fun();
        f.decay(1.0);
        assertEquals(Need.MAX_VALUE - 2, f.getValue(),
                "Fun base decay rate is 2");
    }

    @Test
    @DisplayName("Social.decay(1.0) decreases by its base decay rate (5)")
    void testSocialDecayRate() {
        Social s = new Social();
        s.decay(1.0);
        assertEquals(Need.MAX_VALUE - 5, s.getValue(),
                "Social base decay rate is 5");
    }

    // -----------------------------------------------------------------------
    // decay() with multiplier
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("decay(2.0) applies double the base rate")
    void testDecayWithMultiplier() {
        Hunger h = new Hunger();
        h.decay(2.0);
        assertEquals(Need.MAX_VALUE - 10, h.getValue(),
                "Hunger decay with 2.0 multiplier should subtract 10");
    }

    @Test
    @DisplayName("decay(0.5) applies half the base rate (rounded)")
    void testDecayWithFractionalMultiplier() {
        Energy e = new Energy();
        e.decay(0.5);
        // 2 * 0.5 = 1.0, rounded = 1
        assertEquals(Need.MAX_VALUE - 1, e.getValue(),
                "Energy decay with 0.5 multiplier should subtract 1");
    }

    // -----------------------------------------------------------------------
    // increase / decrease clamping
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("increase() caps at MAX_VALUE")
    void testIncreaseClamps() {
        Hunger h = new Hunger();
        h.increase(50);
        assertEquals(Need.MAX_VALUE, h.getValue(), "Should not exceed MAX_VALUE");
    }

    @Test
    @DisplayName("decrease() floors at 0")
    void testDecreaseClamps() {
        Hunger h = new Hunger();
        h.decrease(200);
        assertEquals(0, h.getValue(), "Should not go below 0");
    }

    // -----------------------------------------------------------------------
    // setValue() clamping
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("setValue() clamps negative values to 0")
    void testSetValueClampsNegative() {
        Hunger h = new Hunger();
        h.setValue(-10);
        assertEquals(0, h.getValue());
    }

    @Test
    @DisplayName("setValue() clamps values above MAX_VALUE")
    void testSetValueClampsAboveMax() {
        Hunger h = new Hunger();
        h.setValue(500);
        assertEquals(Need.MAX_VALUE, h.getValue());
    }

    @Test
    @DisplayName("setValue() accepts valid in-range value")
    void testSetValueAcceptsValid() {
        Hunger h = new Hunger();
        h.setValue(42);
        assertEquals(42, h.getValue());
    }
}
