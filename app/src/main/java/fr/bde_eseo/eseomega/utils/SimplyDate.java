/**
 * Copyright (C) 2016 - François LEPAROUX
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.bde_eseo.eseomega.utils;

import android.content.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import fr.bde_eseo.eseomega.R;

/**
 * Created by François L. on 23/12/2015.
 * Simple date / time delay container
 */
public class SimplyDate {
    private long hours, days, weeks, months;
    private Date remote_date;
    private SimpleDateFormat simpleDateFormat;

    private static final long ONE_HOUR = 60*60*1000;    // 1 heure = 3600 * 1000 millis
    private static final long ONE_DAY = 24*ONE_HOUR;    // 1 jour = 24 heures
    private static final long ONE_WEEK = 7*ONE_DAY;     // 1 semaine = 7 jours
    private static final long ONE_MONTH = 30*ONE_DAY;   // En moyenne, 1 mois = 30 jours, la flemme de faire une fonction adéquate

    /**
     * If date fail : try with seconds or without (english style)
     * @param remote_date The date to convert as a string
     */
    public SimplyDate(String remote_date)  {
        try {
            simpleDateFormat = new SimpleDateFormat("dd/MM/yy hh:mm", Locale.FRANCE); // Thomas's way
            this.remote_date = simpleDateFormat.parse(remote_date);
            calcIntervalFromNow();
        } catch (ParseException e) {
            try {
                simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.FRANCE); // Good way, Thomas is a sheep
                this.remote_date = simpleDateFormat.parse(remote_date);
                calcIntervalFromNow();
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void calcIntervalFromNow() {
        long diff = Calendar.getInstance().getTime().getTime() - remote_date.getTime();

        //Log.d("Sim", "From : " + Calendar.getInstance().getTime().getTime() + " to " + remote_date.getTime() + " → " + diff);

        // Calc intervals
        hours = diff / ONE_HOUR;
        days = diff / ONE_DAY;
        weeks = diff / ONE_WEEK;
        months = diff / ONE_MONTH;
    }

    /**
     * Returns a string which says the interval
     */
    public String simplify(Context ctx) {
        if(ctx==null)return "";
        if (months == 0) {
            if (weeks == 0) {
                if (days == 0) {
                    if (hours == 0) {
                        return ctx.getString(R.string.date_simp_0);
                    } else {
                        return ctx.getString(R.string.date_simp_1) +" " + hours + " " + ctx.getString(R.string.date_simp_h) + (hours>1?"s":"");
                    }
                } else {
                    return ctx.getString(R.string.date_simp_1) +" " + days + " " + ctx.getString(R.string.date_simp_d) + (days>1?"s":"");
                }
            } else {
                return ctx.getString(R.string.date_simp_1) +" " + weeks + " " + ctx.getString(R.string.date_simp_w) + (weeks>1?"s":"");
            }
        } else {
            return ctx.getString(R.string.date_simp_1) +" " + months + " " + ctx.getString(R.string.date_simp_m);
        }
    }
}
