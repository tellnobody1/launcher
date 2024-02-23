package xyz.uaapps.launcher;

import static org.junit.Assert.assertTrue;
import static xyz.uaapps.launcher.QueryVariants.check;

import org.junit.Test;

import java.util.Set;

public class QueryVariantsTest {

    @Test
    public void testCheck() {
        Set<String> targets = Set.of("ProCredit");
        boolean result = check("прок", targets);
        assertTrue(result);
    }
}