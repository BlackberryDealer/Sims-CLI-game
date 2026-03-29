package simcli.entities.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@link SocialAction} enum — verifying field values
 * and ensuring the skill system has been fully removed.
 */
@DisplayName("SocialAction Enum — Social Interaction Value Tests")
public class SocialActionTest {

    @Test
    @DisplayName("CHAT has correct attribute values")
    void testChatValues() {
        SocialAction chat = SocialAction.CHAT;
        assertEquals("Chat", chat.getDisplayName());
        assertEquals(5, chat.getRelationshipChange());
        assertEquals(10, chat.getHappinessChange());
        assertEquals(-5, chat.getEnergyChange());
        assertEquals(30, chat.getSocialChange());
    }

    @Test
    @DisplayName("JOKE has correct attribute values")
    void testJokeValues() {
        SocialAction joke = SocialAction.JOKE;
        assertEquals("Joke", joke.getDisplayName());
        assertEquals(10, joke.getRelationshipChange());
        assertEquals(15, joke.getHappinessChange());
        assertEquals(-10, joke.getEnergyChange());
        assertEquals(70, joke.getSocialChange());
    }

    @Test
    @DisplayName("ARGUE has negative relationship and happiness changes")
    void testArgueValues() {
        SocialAction argue = SocialAction.ARGUE;
        assertTrue(argue.getRelationshipChange() < 0,
                "Arguing should decrease relationship");
        assertTrue(argue.getHappinessChange() < 0,
                "Arguing should decrease happiness");
    }

    @Test
    @DisplayName("SocialAction enum has exactly 3 values")
    void testEnumCount() {
        assertEquals(3, SocialAction.values().length,
                "Should have CHAT, JOKE, ARGUE");
    }
}
