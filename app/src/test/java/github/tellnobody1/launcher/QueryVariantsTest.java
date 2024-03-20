package github.tellnobody1.launcher;

import java.util.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import static org.junit.Assert.assertEquals;
import static github.tellnobody1.launcher.QueryVariants.checkAll;

@RunWith(Parameterized.class)
public class QueryVariantsTest {
    private final String query;
    private final Map<String, Set<String>> targets;
    private final List<String> expectedResult;

    public QueryVariantsTest(String query, Map<String, Set<String>> targets, List<String> expectedResult) {
        this.query = query;
        this.targets = targets;
        this.expectedResult = expectedResult;
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {"прок", Map.of("key1", Set.of("ProCredit")), List.of("key1")},
            {"silp", Map.of("key2", Set.of("Сільпо")), List.of("key2")},
            {"мап", Map.of("key3", Set.of("Карти")), List.of("key3")},
            {"пошт", Map.of("key4", Set.of("Gmail")), List.of("key4")}
        });
    }

    @Test
    public void testCheckAll() {
        var result = checkAll(query, targets);
        assertEquals(expectedResult, result);
    }
}
