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

package fr.bde_eseo.eseoasis.lacommande.model;

import android.content.Context;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import fr.bde_eseo.eseoasis.R;

/**
 * Created by François L. on 20/07/2015.
 */
public class HistoryItem {

    public final static int STATUS_PREPARING = 0;
    public final static int STATUS_READY = 1;
    public final static int STATUS_DONE = 2;
    public final static int STATUS_NOPAID = 3;

    private String commandName;
    private int commandStatus;
    private String commandDate, commandStr;
    private double commandPrice;
    private boolean isHeader, isFooter;
    private int commandNumber, commandModulo;
    private Context ctx;

    public HistoryItem (Context ctx, String commandName, int commandStatus, double commandPrice, String commandDate, int commandNumber, int commandModulo, String commandStr, boolean simpleDate) {
        this.commandName = commandName;
        this.commandStatus = commandStatus;
        this.commandPrice = commandPrice;
        this.commandDate = commandDate;
        this.commandDate = getFrenchDate(simpleDate);
        this.commandNumber = commandNumber;
        this.isHeader = false;
        this.commandModulo = commandModulo;
        this.commandStr = commandStr;
        this.ctx = ctx;
    }

    // Header / footer (if true)
    public HistoryItem (String titleName, boolean isFooter) {
        this.commandName = titleName;
        this.isHeader = true;
        this.isFooter = isFooter;
    }

    public String getCommandName() {
        return commandName;
    }

    public int getCommandStatus() {
        return commandStatus;
    }

    public String getCommandStatusAsString () {
        switch (commandStatus) {
            case STATUS_DONE:
                return ctx.getString(R.string.cmd_status_finished_tostring);
            case STATUS_PREPARING:
                return ctx.getString(R.string.cmd_status_preparing_tostring);
            case STATUS_READY:
                return ctx.getString(R.string.cmd_status_ready_tostring);
            case STATUS_NOPAID:
                return ctx.getString(R.string.cmd_status_unpaid_tostring);
            default:
                return ctx.getString(R.string.cmd_status_error_tostring);
        }
    }

    public String getCommandDate() {
        return commandDate;
    }

    public double getCommandPrice() {
        return commandPrice;
    }

    public String getCommandPriceAsString(){
        return new DecimalFormat("0.00").format(commandPrice) + "€";
    }

    public String getCommandNumberAsString() {
        return commandStr + new DecimalFormat("000").format(commandModulo);
    }

    public boolean isHeader() {
        return isHeader;
    }

    public boolean isFooter() {
        return isFooter;
    }

    public int getCommandNumber() {
        return commandNumber;
    }

    public String toString() {
        return "Command Data = {\""+getCommandName()+"\" the "+getCommandDate()+", price = "+commandPrice+"€, status = "+getCommandStatusAsString()+"}";
    }

    public Date getParsedDate () {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRANCE);
        Date date = null;
        try {
            date = format.parse(this.commandDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public String getFrenchDate (boolean simpleDate) {
        Date d = getParsedDate();
        SimpleDateFormat sdf;
        if (simpleDate)
            sdf = new SimpleDateFormat("EEEE dd MMMM yyyy", Locale.FRANCE);
        else
            sdf = new SimpleDateFormat("E dd MMMM yyyy, 'à' HH:mm", Locale.FRANCE);
        return sdf.format(d);
    }
}
