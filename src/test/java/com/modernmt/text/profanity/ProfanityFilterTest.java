package com.modernmt.text.profanity;

import com.modernmt.text.profanity.dictionary.Profanity;
import org.junit.Test;
import static org.junit.Assert.*;

public class ProfanityFilterTest {

    @Test
    public void testTextWithProfanity() {
        ProfanityFilter filter = new ProfanityFilter();
        Profanity profanity = filter.find("en", "Leave those scumbags alone!");

        assertNotNull(profanity);
        assertEquals("scumbags", profanity.text());
    }

    @Test
    public void testTextWithoutProfanity() {
        ProfanityFilter filter = new ProfanityFilter();
        Profanity profanity = filter.find("en", "This is just a text.");
        assertNull(profanity);
    }

    @Test
    public void testTextWithBelowThresholdProfanity() {
        ProfanityFilter filter = new ProfanityFilter(.5f);
        Profanity profanity = filter.find("en", "Leave those scumbags alone!");
        assertNull(profanity);
    }
}
