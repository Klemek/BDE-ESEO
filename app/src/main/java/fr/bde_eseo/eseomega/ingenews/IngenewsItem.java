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

package fr.bde_eseo.eseomega.ingenews;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

import fr.bde_eseo.eseomega.utils.SimplyDate;

/**
 * Created by François L. on 22/12/2015.
 * Sur la bonne idée de Mr Naudet
 */
public class IngenewsItem {

    private final static double MBYTE = 1048576.0;
    private final static String MBYTE_STR = " Mio";
    private final static double KBYTE = 1024.0;
    private final static String KBYTE_STR = " Kio";
    private final static double BYTE = 1.0;
    private final static String BYTE_STR = " octets";
    private int id;
    private long size;
    private String name, date, file, details, imgLink;


    /**
     * Constructeur depuis un objet JSON (depuis le serveur / cache)
     */
    public IngenewsItem(JSONObject obj, Context ctx) throws JSONException {
        this.id = obj.getInt("id");
        this.name = obj.getString("name");
        this.date = obj.getString("date");
        this.file = obj.getString("file");
        this.size = obj.getLong("size");
        this.imgLink = obj.getString("img");

        SimplyDate simplyDate = new SimplyDate(date);
        this.details = simplyDate.simplify(ctx) + " · " + getFormattedSize();
        //Log.d("INGE", name + " , " + details);
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getFile() {
        return file;
    }

    public String getImgLink() {
        return imgLink;
    }

    public String getFormattedSize() {
        if (size >= MBYTE) {
            return new DecimalFormat("0.0").format(size / MBYTE) + MBYTE_STR;
        } else if (size >= KBYTE && size < MBYTE) {
            return new DecimalFormat("0.0").format(size / KBYTE) + KBYTE_STR;
        } else {
            return new DecimalFormat("0.0").format(size / BYTE) + BYTE_STR;
        }
    }

    public String getDetails() {
        return details;
    }
}
