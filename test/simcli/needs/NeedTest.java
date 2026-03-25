package simcli.needs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import simcli.needs.*;
import simcli.entities.actors.Sim;
import simcli.entities.models.Gender;
import simcli.entities.models.Job;

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

    private Sim createDummySim() {
        return new Sim("Dummy", 20, Gender.MALE, Job.UNEMPLOYED);
    }

    // -----------------------------------------------------------------------
    // Initial value
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("All needs start at MAX_VALUE (100)")
    void testAllNeedsStartAtMax() {
        assertEquals(Need.MAX_VALUE, new Hunger().getValue());
        assertEquals(Need.MAX_VALUE, new Energy().getValue());
        assertEquals(Need.MAX_VALUE, new Hygiene().getValue());
        assertEquals(Need.MAX_VALUE, new Happiness().getValue());
        assertEquals(Need.MAX_VALUE, new Social().getValue());
    }

    // -----------------------------------------------------------------------
    // decay() uses base decay rate
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Hunger.decay(1.0) decreases by its base decay rate (2)")
    void testHungerDecayRate() {
        Hunger h = new Hunger();
        h.calculateDecay(createDummySim(), 1.0);
        assertEquals(Need.MAX_VALUE - 2, h.getValue(),
                "Hunger base decay rate is 2");
    }

    @Test
    @DisplayName("Energy.decay(1.0) decreases by its base decay rate (2)")
    void testEnergyDecayRate() {
        Energy e = new Energy();
        e.calculateDecay(createDummySim(), 1.0);
        assertEquals(Need.MAX_VALUE - 2, e.getValue(),
                "Energy base decay rate is 2");
    }

    @Test
    @DisplayName("Hygiene.decay(1.0) decreases by its base decay rate (2)")
    void testHygieneDecayRate() {
        Hygiene hy = new Hygiene();
        hy.calculateDecay(createDummySim(), 1.0);
        assertEquals(Need.MAX_VALUE - 2, hy.getValue(),
                "Hygiene base decay rate is 2");
    }

    @Test
    @DisplayName("Happiness.decay(1.0) decreases by its base decay rate (2)")
    void testFunDecayRate() {
        Happiness f = new Happiness();
        f.calculateDecay(createDummySim(), 1.0);
        assertEquals(Need.MAX_VALUE - 2, f.getValue(),
                "Happiness base decay rate is 2");
    }

    @Test
    @DisplayName("Social.decay(1.0) decreases by its base decay rate (5)")
    void testSocialDecayRate() {
        Social s = new Social();
        s.calculateDecay(createDummySim(), 1.0);
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
        h.calculateDecay(createDummySim(), 2.0);
        assertEquals(Need.MAX_VALUE - 4, h.getValue(),
                "Hunger decay with 2.0 multiplier should subtract 4");
    }

    @Test
    @DisplayName("decay(0.5) applies half the base rate (rounded)")
    void testDecayWithFractionalMultiplier() {
        Energy e = new Energy();
        e.calculateDecay(createDummySim(), 0.5);
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
