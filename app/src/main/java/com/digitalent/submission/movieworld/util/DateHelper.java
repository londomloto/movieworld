package com.digitalent.submission.movieworld.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateHelper {
    public static String formatDate(String date, String language) {
        if (date == null) {
            return "";
        }

        DateFormat input, output;
        Locale locale = Locale.forLanguageTag(language);

        input = new SimpleDateFormat("yyyy-MM-dd", Locale.forLanguageTag("en"));

        if (language.equals("id-ID")) {
            output = new SimpleDateFormat("dd MMMM yyyy", locale);
        } else {
            output = new SimpleDateFormat("MMMM dd, yyyy", locale);
        }

        Date objDate;
        String fmtDate;

        try {
            objDate = input.parse(date);
            fmtDate = output.format(objDate);
        } catch (ParseException e) {
            fmtDate = date;
        }

        return fmtDate;
    }
}
