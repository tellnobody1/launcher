package github.tellnobody1.launcher;

import java.util.*;

public class QueryVariants {

    public static <T> List<T> checkAll(String prefixString, Map<T, Set<String>> allTargets) {
        var results = new LinkedList<T>();
        for (var targets : allTargets.entrySet())
            if (check(prefixString, targets.getValue()))
                results.add(targets.getKey());
        return results;
    }

    private static boolean check(String lowerCasedInput, Set<String> targets) {
        var allTargets = new LinkedList<String>();
        for (var target : targets) {
            allTargets.add(target.toLowerCase());
            allTargets.add(target.toLowerCase().replaceAll("-", ""));
            allTargets.add(target.toLowerCase().replaceAll("'", "").replaceAll("’", ""));
            allTargets.add(target.toLowerCase().replaceAll("ь", ""));
        }

        var input = lowerCasedInput.replaceAll("'", "");
        Set<String> inputs = new HashSet<>();
        inputs.addAll(convertChars(input, toCyrillic));
        inputs.addAll(convertChars(input, toLatin));

        var groups = List.of(
                List.of("мапа", "мапи", "карта", "карти", "map", "maps"),
                List.of("пошта", "mail")
        );
        for (var group : groups)
            if (containsAny(input, group)) inputs.addAll(group);

        for (String i : inputs) for (String t : allTargets) if (t.contains(i)) return true;
        return false;
    }

    private static boolean containsAny(String input, List<String> elements) {
        for (var element : elements) if (element.contains(input)) return true;
        return false;
    }

    private static List<String> convertChars(String input, Map<String, List<String>> converter) {
        List<String> results = new LinkedList<>();
        results.add("");
        for (char ch : input.toCharArray()) {
            String key = String.valueOf(ch);
            if (converter.containsKey(key)) {
                List<String> newResults = new LinkedList<>();
                for (String prefix : results)
                    for (String cyrillicChar : converter.get(key))
                        newResults.add(prefix + cyrillicChar);
                results = newResults;
            } else {
                // If the character is not in the map, add it as-is
                for (int i = 0; i < results.size(); i++)
                    results.set(i, results.get(i) + ch);
            }
        }
        return results;
    }

    private static final Map<String, List<String>> toCyrillic = new HashMap<>();
    static {
        toCyrillic.put("a", List.of("а"));
        toCyrillic.put("b", List.of("б"));
        toCyrillic.put("c", List.of("к"));
        toCyrillic.put("d", List.of("д"));
        toCyrillic.put("e", List.of("е"));
        toCyrillic.put("f", List.of("ф"));
        toCyrillic.put("g", List.of("г"));
        toCyrillic.put("h", List.of("г"));
        toCyrillic.put("i", List.of("і"));
        toCyrillic.put("j", List.of("ж"));
        toCyrillic.put("k", List.of("к"));
        toCyrillic.put("l", List.of("л"));
        toCyrillic.put("m", List.of("м"));
        toCyrillic.put("n", List.of("н"));
        toCyrillic.put("o", List.of("о"));
        toCyrillic.put("p", List.of("п"));
        toCyrillic.put("q", List.of("к"));
        toCyrillic.put("r", List.of("р"));
        toCyrillic.put("s", List.of("с"));
        toCyrillic.put("t", List.of("т"));
        toCyrillic.put("u", List.of("у"));
        toCyrillic.put("v", List.of("в"));
        toCyrillic.put("w", List.of("в"));
        toCyrillic.put("x", List.of("х"));
        toCyrillic.put("y", List.of("и"));
        toCyrillic.put("z", List.of("з"));
    }

    private static final Map<String, List<String>> toLatin = new HashMap<>();
    static {
        toLatin.put("а", List.of("a"));
        toLatin.put("б", List.of("b"));
        toLatin.put("в", List.of("v"));
        toLatin.put("г", List.of("g"));
        toLatin.put("ґ", List.of("g"));
        toLatin.put("д", List.of("d"));
        toLatin.put("е", List.of("e"));
        toLatin.put("є", List.of("ye"));
        toLatin.put("ж", List.of("zh"));
        toLatin.put("з", List.of("z"));
        toLatin.put("и", List.of("y"));
        toLatin.put("і", List.of("i"));
        toLatin.put("ї", List.of("ii"));
        toLatin.put("й", List.of("i"));
        toLatin.put("к", List.of("c", "k"));
        toLatin.put("л", List.of("l"));
        toLatin.put("м", List.of("m"));
        toLatin.put("н", List.of("n"));
        toLatin.put("о", List.of("o"));
        toLatin.put("п", List.of("p"));
        toLatin.put("р", List.of("r"));
        toLatin.put("с", List.of("s"));
        toLatin.put("т", List.of("t"));
        toLatin.put("у", List.of("u"));
        toLatin.put("ф", List.of("f"));
        toLatin.put("х", List.of("x"));
        toLatin.put("ц", List.of("ts"));
        toLatin.put("ч", List.of("ch"));
        toLatin.put("ш", List.of("sh"));
        toLatin.put("щ", List.of("shch"));
        toLatin.put("ь", List.of());
        toLatin.put("ю", List.of("yu"));
        toLatin.put("я", List.of("ya"));
    }
}
