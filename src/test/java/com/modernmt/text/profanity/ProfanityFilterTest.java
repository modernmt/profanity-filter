package com.modernmt.text.profanity;

import com.modernmt.text.profanity.dictionary.Profanity;
import org.junit.Test;
import static org.junit.Assert.*;

public class ProfanityFilterTest {

    @Test
    public void testTextWithProfanity() {
        String lang = "en";
        String text = "Leave those scumbags alone!";

        ProfanityFilter filter = new ProfanityFilter();
        Profanity profanity = filter.find(lang, text);
        boolean hasProfanity = filter.test(lang, text);

        assertNotNull(profanity);
        assertEquals("scumbags", profanity.text());
        assertTrue(hasProfanity);
    }

    @Test
    public void testTextWithoutProfanity() {
        String lang = "en";
        String text = "This is just a text.";

        ProfanityFilter filter = new ProfanityFilter();
        Profanity profanity = filter.find(lang, text);
        boolean hasProfanity = filter.test(lang, text);

        assertNull(profanity);
        assertFalse(hasProfanity);
    }

    @Test
    public void testTextWithBelowThresholdProfanity() {
        String lang = "en";
        String text = "Leave those scumbags alone!";

        ProfanityFilter filter = new ProfanityFilter(.5f);
        Profanity profanity = filter.find(lang, text);
        boolean hasProfanity = filter.test(lang, text);

        assertNull(profanity);
        assertFalse(hasProfanity);
    }
}
