package java03.team01.FAMS.utils;

import org.springframework.cglib.core.Local;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class
Utils {

    public static boolean DateTimeFormatCheck(String localDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            LocalDate.parse(localDate, formatter);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

//    public static DateTimeFormatter getDateTimeFormatter() {
//        return DateTimeFormatter.ofPattern("yyyy-MM-dd");
//    }

    public static String getFileType(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return filename.substring(dotIndex + 1);
    }

    public static boolean isValidNumber(String number) {
        String formatRegex = "\\d+(\\.\\d+)?";
        Pattern pattern = Pattern.compile(formatRegex);
        Matcher matcher = pattern.matcher(number);
        return matcher.matches();
    }
}
