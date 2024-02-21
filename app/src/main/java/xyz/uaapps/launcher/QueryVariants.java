package xyz.uaapps.launcher;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.N;

import java.util.stream.Collectors;

public class QueryVariants {
    public static boolean check(String input, String target) {
        if (SDK_INT >= N) {
            if (target.contains(input.chars().mapToObj(QueryVariants::toCyrillic).collect(Collectors.joining())))
                return true;
            else if (target.contains(input.chars().mapToObj(QueryVariants::toLatin).collect(Collectors.joining())))
                return true;
            else
                return false;
        } else {
            return target.contains(input);
        }
    }

    private static String toCyrillic(int x) {
        return String.valueOf(switch (x) {
            case 'a' -> 'а';
            case 'b' -> 'б';
            case 'c' -> 'к';
            case 'd' -> 'д';
            case 'e' -> 'е';
            case 'f' -> 'ф';
            case 'g' -> 'г';
            case 'h' -> 'г';
            case 'i' -> 'і';
            case 'j' -> 'ж';
            case 'k' -> 'к';
            case 'l' -> 'л';
            case 'm' -> 'м';
            case 'n' -> 'н';
            case 'o' -> 'о';
            case 'p' -> 'п';
            case 'q' -> 'к';
            case 'r' -> 'р';
            case 's' -> 'с';
            case 't' -> 'т';
            case 'u' -> 'у';
            case 'v' -> 'в';
            case 'w' -> 'в';
            case 'x' -> 'х';
            case 'y' -> 'и';
            case 'z' -> 'з';
            default -> (char) x;
        });
    }

    private static String toLatin(int x) {
        return switch (x) {
            case 'а' -> "a";
            case 'б' -> "b";
            case 'в' -> "v";
            case 'г' -> "g";
            case 'ґ' -> "g";
            case 'д' -> "d";
            case 'е' -> "e";
            case 'є' -> "ye";
            case 'ж' -> "zh";
            case 'з' -> "z";
            case 'и' -> "y";
            case 'і' -> "i";
            case 'ї' -> "ii";
            case 'й' -> "i";
            case 'к' -> "k";
            case 'л' -> "l";
            case 'м' -> "m";
            case 'н' -> "n";
            case 'о' -> "o";
            case 'п' -> "p";
            case 'р' -> "r";
            case 'с' -> "s";
            case 'т' -> "t";
            case 'у' -> "u";
            case 'ф' -> "f";
            case 'х' -> "x";
            case 'ц' -> "ts";
            case 'ч' -> "ch";
            case 'ш' -> "sh";
            case 'щ' -> "shch";
            case 'ю' -> "yu";
            case 'я' -> "ya";
            default -> Character.valueOf((char) x).toString();
       };
    }
}
