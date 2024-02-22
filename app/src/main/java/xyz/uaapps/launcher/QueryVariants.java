package xyz.uaapps.launcher;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QueryVariants {

    public static boolean check(String input, Set<String> targets) {
        var allTargets = new LinkedList<String>();
        for (var target : targets) {
            allTargets.add(target.toLowerCase());
            allTargets.add(target.toLowerCase().replace("-", ""));
        }

        Set<String> inputs = new HashSet<>();
        inputs.add(convertChars(input, toCyrillic));
        inputs.add(convertChars(input, toLatin));

        var maps = Arrays.asList("мапа", "мапи", "карта", "карти", "map", "maps");
        if (containsAny(input, maps)) inputs.addAll(maps);

        for (String i : inputs) for (String t : allTargets) if (t.contains(i)) return true;
        return false;
    }

    private static boolean containsAny(String input, List<String> elements) {
        for (var element : elements) if (input.contains(element)) return true;
        return false;
    }

    private static String convertChars(String input, Map<String, String> converter) {
        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            var c1 = Character.toString(c);
            var r = converter.get(c1);
            if (r == null) result.append(c1);
            else result.append(r);
        }
        return result.toString();
    }

    private static final Map<String, String> toCyrillic = new HashMap<>();
    static {
        toCyrillic.put("a", "а");
        toCyrillic.put("b", "б");
        toCyrillic.put("c", "к");
        toCyrillic.put("d", "д");
        toCyrillic.put("e", "е");
        toCyrillic.put("f", "ф");
        toCyrillic.put("g", "г");
        toCyrillic.put("h", "г");
        toCyrillic.put("i", "і");
        toCyrillic.put("j", "ж");
        toCyrillic.put("k", "к");
        toCyrillic.put("l", "л");
        toCyrillic.put("m", "м");
        toCyrillic.put("n", "н");
        toCyrillic.put("o", "о");
        toCyrillic.put("p", "п");
        toCyrillic.put("q", "к");
        toCyrillic.put("r", "р");
        toCyrillic.put("s", "с");
        toCyrillic.put("t", "т");
        toCyrillic.put("u", "у");
        toCyrillic.put("v", "в");
        toCyrillic.put("w", "в");
        toCyrillic.put("x", "х");
        toCyrillic.put("y", "и");
        toCyrillic.put("z", "з");
    }

    private static final Map<String, String> toLatin = new HashMap<>();
    static {
        toLatin.put("а", "a");
        toLatin.put("б", "b");
        toLatin.put("в", "v");
        toLatin.put("г", "g");
        toLatin.put("ґ", "g");
        toLatin.put("д", "d");
        toLatin.put("е", "e");
        toLatin.put("є", "ye");
        toLatin.put("ж", "zh");
        toLatin.put("з", "z");
        toLatin.put("и", "y");
        toLatin.put("і", "i");
        toLatin.put("ї", "ii");
        toLatin.put("й", "i");
        toLatin.put("к", "k");
        toLatin.put("л", "l");
        toLatin.put("м", "m");
        toLatin.put("н", "n");
        toLatin.put("о", "o");
        toLatin.put("п", "p");
        toLatin.put("р", "r");
        toLatin.put("с", "s");
        toLatin.put("т", "t");
        toLatin.put("у", "u");
        toLatin.put("ф", "f");
        toLatin.put("х", "x");
        toLatin.put("ц", "ts");
        toLatin.put("ч", "ch");
        toLatin.put("ш", "sh");
        toLatin.put("щ", "shch");
        toLatin.put("ю", "yu");
        toLatin.put("я", "ya");
    }
}
