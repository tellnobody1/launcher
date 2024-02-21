package xyz.uaapps.launcher;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import java.util.List;

public class QueryVariants {

    public static boolean check(String input, String target) {
        List<String> targets = Arrays.asList(
                target,
                target.replace("-", "")
        );
        List<String> inputs = Arrays.asList(
                convertChars(input, toCyrillic),
                convertChars(input, toLatin)
        );
        for (String i : inputs) for (String t : targets) if (t.contains(i)) return true;
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
