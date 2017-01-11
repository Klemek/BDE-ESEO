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

package fr.bde_eseo.eseomega.events;

import android.content.Context;
import android.content.Intent;
import android.provider.CalendarContract;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.events.tickets.model.SubEventItem;
import fr.bde_eseo.eseomega.utils.DateUtils;
import fr.bde_eseo.eseomega.utils.JSONUtils;

/**
 * Created by François L. on 14/08/2015.
 */
public class EventItem {


    //private final static String JSON_KEY_ARRAY_COLOR = "color";
    private final static String JSON_KEY_ARRAY_TICKETS = "tickets";

    private static final String HOUR_PASS_ALLDAY = "00:02";
    private static final int MAX_CHAR_DESC = 36;
    private String name, details, club, url, lieu, imgUrl;
    private boolean signup;
    private int id;
    private boolean isHeader;
    private Date date, datefin;
    private int color; // aarrggbb, set alpha to 0xFF
    private String shorted;
    private Calendar cal, calFin;
    private boolean isPassed;
    private boolean isSignedUp;

    private ArrayList<SubEventItem> subEventItems;

    public EventItem(Context ctx, JSONObject obj) throws JSONException {

        this.name = obj.getString(Constants.JSON_EVENT_NAME);
        this.details = obj.getString(Constants.JSON_EVENT_DETAIL);

        String stdate = obj.getString(Constants.JSON_EVENT_DATE);
        String enddate = JSONUtils.getString(obj, Constants.JSON_EVENT_DATEFIN, stdate);

        // ARGB -> alpha 255
        setDateAsString(stdate, enddate);
        this.isHeader = false;

        // Calendar
        cal = new GregorianCalendar();
        cal.setTime(date);
        calFin = new GregorianCalendar();
        calFin.setTime(datefin);

        this.club = obj.getString(Constants.JSON_EVENT_CLUB);
        this.lieu = obj.getString(Constants.JSON_EVENT_LIEU);
        this.id = obj.getInt(Constants.JSON_EVENT_ID);
        this.signup = obj.getBoolean(Constants.JSON_EVENT_SIGNUP);


        this.url = JSONUtils.getString(obj, Constants.JSON_EVENT_URL, "");
        this.imgUrl = JSONUtils.getString(obj, Constants.JSON_EVENT_IMGURL, null, true);

        this.performShortedDetails(ctx);



            if (this.getDatefin().before(new Date())) {
                this.setIsPassed(true);
                this.color = 0xFF7F7F7F;
            } else {
                this.setIsPassed(false);
                this.color = 0xFF71DAEB;
                //The superior said no rainbow event list
                /*JSONArray colorsJSON = obj.getJSONArray(JSON_KEY_ARRAY_COLOR);
                int[] colors = new int[3];
                for (int a = 0; a < colorsJSON.length(); a++) {
                    colors[a] = colorsJSON.getInt(a); // TODO pass integer directly without using string
                }
                this.color = 0xFF000000 | (colors[0] << 16) | (colors[1] << 8) | (colors[2]);*/
            }

        subEventItems = new ArrayList<>();
        //TODO tickets system
        /*JSONArray tickets = obj.getJSONArray(JSON_KEY_ARRAY_TICKETS);
        for (int i=0;i<tickets.length();i++) {
            SubEventItem sei = new SubEventItem(tickets.getJSONObject(i));
            subEventItems.add(sei);
        }*/
    }

    public EventItem(Context ctx, String stdate, String enddate){
        setDateAsString(stdate, enddate);
        performShortedDetails(ctx);
    }

    public EventItem(String name) {
        this.name = name;
        this.isHeader = true;
    }

    public static String getShortedDetails(Context ctx, String stdate, String enddate) {
        EventItem ei = new EventItem(ctx, stdate, enddate);
        return ctx.getString(R.string.event_the) + " : " + ei.getDayAsString(ei.getDate()) + " " + ei.getShortedDetails();
    }

    public ArrayList<SubEventItem> getSubEventItems() {
        return subEventItems;
    }

    public boolean hasSubEventChildEnabled () {
        boolean en = false;

        for (int i=0;subEventItems != null && i<subEventItems.size() && !en;i++) {
            if (subEventItems.get(i).isAvailable())
                en = true;
        }

        return en;
    }

    public boolean hasSubEventChildPriced () {
        boolean en = false;

        for (int i=0;subEventItems != null && i<subEventItems.size() && !en;i++) {
            if (subEventItems.get(i).getPrice() >= 0.5)
                en = true;
        }

        return en;
    }

    // Like : Heure · club · lieu · description (size limited -> ~35 chars)
    // V2.1 :
    // À : heure début si != 00:02 · Fin : date fin si != date debut + heure fin si != heure debut
    // Par : club si != null
    public void performShortedDetails (Context ctx) {
        this.shorted = "";
        if(ctx==null)return;
        String outFormat = "";
        String stTime = getTimeAsString(this.date);
        String dayStartStr = getDayAsString(this.date);
        String endTime = getTimeAsString(this.datefin);
        String dayEndStr = getDayAsString(this.datefin);

        if (stTime.length() > 0) outFormat += ctx.getString(R.string.at) + " : " + stTime;
        if (stTime.length() > 0 && (!endTime.equals(stTime) || !dayEndStr.equals(dayStartStr))) outFormat += " · " + ctx.getString(R.string.event_end) + " : ";
        if (!dayEndStr.equals(dayStartStr)) outFormat += dayEndStr + " ";
        if (!endTime.equals(stTime)) outFormat += endTime;
        if (outFormat.length() > 1 && club != null && club.length() > 0) outFormat += "\n";
        if (club != null && club.length() > 0) outFormat += ctx.getString(R.string.event_from)+ " : " + club;

        this.shorted = outFormat;
    }

    // Get only, no operation -> faster in adapter
    public String getShortedDetails () {
        return shorted;
    }

    public Intent toCalendarIntent () {
        Intent calIntent = new Intent(Intent.ACTION_INSERT);
        calIntent.setType("vnd.android.cursor.item/event");
        calIntent.putExtra(CalendarContract.Events.TITLE, name);
        calIntent.putExtra(CalendarContract.Events.EVENT_LOCATION, (lieu!=null?lieu:""));
        calIntent.putExtra(CalendarContract.Events.DESCRIPTION, (details!=null?details:""));

        SimpleDateFormat sdf = new SimpleDateFormat("HH'h'mm", DateUtils.getLocale());
        String sDate = sdf.format(this.date);
        boolean allDay = sDate.equals(HOUR_PASS_ALLDAY);
        calIntent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, allDay);
        calIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, cal.getTimeInMillis());
        if (!allDay) calIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, calFin.getTimeInMillis());

        return calIntent;
    }

    public void setIsHeader(boolean isHeader) {
        this.isHeader = isHeader;
    }

    public String getMonthHeader() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", DateUtils.getLocale());
        return sdf.format(this.date).toUpperCase(DateUtils.getLocale());
    }

    // If equal to hour_pass (00h02 ?) set it all day, no specific hour
    public String getTimeAsString (Date d) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH':'mm", DateUtils.getLocale());
        String sDate = sdf.format(d);
        if (sDate.equals(HOUR_PASS_ALLDAY))
            sDate = "";
        return sDate;
    }

    public void setDateAsString(String dateAsString, String dateAsStringEnd) {
        this.datefin = DateUtils.fromString(dateAsStringEnd);
        this.date = DateUtils.fromString(dateAsString);
    }

    public String getDayAsString(Date d) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd MMM", DateUtils.getLocale());
        String sDate = sdf.format(d);
        if (sDate.equals(HOUR_PASS_ALLDAY))
            sDate = "";
        return sDate;
    }

    // Number as String, why ? for TextView call !
    public String getDayNumero () {
        SimpleDateFormat sdf = new SimpleDateFormat("dd", DateUtils.getLocale());
        return sdf.format(this.date);
    }

    public String getDayName () {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE", DateUtils.getLocale());
        return sdf.format(this.date);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDateString(Context ctx) {
        return getDayAsString(date) + " " + ctx.getString(R.string.at).toLowerCase() + " " + getTimeAsString(date);
    }

    public String getDateFinString(Context ctx) {
        return getDayAsString(datefin) + " " + ctx.getString(R.string.at).toLowerCase() + " " + getTimeAsString(datefin);
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getUrl() {
        return url;
    }

    public String getLieu() {
        return lieu;
    }

    public void setIsPassed(boolean isPassed) {
        this.isPassed = isPassed;
    }

    public boolean isPassed () {
        return isPassed;
    }

    public Date getDatefin() {
        return datefin;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public boolean isSignup(){
        return signup;
    }

    public int getId(){
        return id;
    }

    public boolean isSignedUp(){
        return isSignedUp;
    }

    public void setSignedUp(boolean isSignedUp){
        this.isSignedUp = isSignedUp;
    }
}
