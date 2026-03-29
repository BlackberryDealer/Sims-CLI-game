package simcli.entities.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@link Trait} enum — energy decay modifier values.
 */
@DisplayName("Trait Enum — Energy Decay Modifier Tests")
public class TraitTest {

    @Test
    @DisplayName("ACTIVE trait has 0.8x energy decay (lower = less drain)")
    void testActiveTrait() {
        assertEquals(0.8, Trait.ACTIVE.getEnergyDecayModifier(), 0.001);
    }

    @Test
    @DisplayName("LAZY trait has 1.5x energy decay (higher = more drain)")
    void testLazyTrait() {
        assertEquals(1.5, Trait.LAZY.getEnergyDecayModifier(), 0.001);
    }

    @Test
    @DisplayName("SOCIALITE trait has 1.0x energy decay (neutral)")
    void testSocialiteTrait() {
        assertEquals(1.0, Trait.SOCIALITE.getEnergyDecayModifier(), 0.001);
    }

    @Test
    @DisplayName("Trait enum has exactly 3 values after skill removal")
    void testTraitCount() {
        assertEquals(3, Trait.values().length,
                "Should have ACTIVE, LAZY, SOCIALITE — FAST_LEARNER was removed");
    }
}
