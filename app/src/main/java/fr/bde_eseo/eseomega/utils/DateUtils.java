package fr.bde_eseo.eseomega.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    static public Date fromString(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", getLocale());

        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(sdf.parse(date));
            cal.add(Calendar.HOUR_OF_DAY, 2);
            return cal.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    static public Date oldfromString(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", getLocale());

        try {
            return sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    static public Locale getLocale() {
        return Locale.FRANCE;
    }

}
