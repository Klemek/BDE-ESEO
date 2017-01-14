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

package fr.bde_eseo.eseomega.rooms;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.R;

/**
 * Created by François L. on 23/03/16.
 */
class RoomItem {

    private String name, number, batiment, info, details;
    private int floor;
    private boolean isHeader;

    /*"nom":"Local Animâ€™",
        "num":"CS001",
        "bat":"C",
        "etage":-1,
        "info"*/

    public RoomItem (String headerName) {
        this.name = headerName;
        this.isHeader = true;
    }

    public RoomItem (Context ctx, JSONObject obj) throws JSONException {
        this.name = obj.getString(Constants.JSON_ROOM_NAME);
        this.number = obj.getString(Constants.JSON_ROOM_NUM);
        this.batiment = obj.getString(Constants.JSON_ROOM_BAT);
        this.floor = obj.getInt(Constants.JSON_ROOM_FLO);
        this.info = obj.getString(Constants.JSON_ROOM_NFO);
        this.isHeader = false;

        ArrayList<String> arrayDetails = new ArrayList<>();
        if (number.length() > 0) arrayDetails.add(number);
        if (batiment.length() > 0 && ctx != null) arrayDetails.add(ctx.getString(R.string.room_bat) +" "+ batiment);
        if(ctx != null)arrayDetails.add(ctx.getString(R.string.room_floor) +" "+ floor);
        if (info.length() > 0) arrayDetails.add(info);

        this.details = "";

        for (int i=0;i<arrayDetails.size();i++) {
            details += arrayDetails.get(i);
            if (i != arrayDetails.size() - 1) {
                details += " • ";
            }
        }
}

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public String getBatiment() {
        return batiment;
    }

    public String getInfo() {
        return info;
    }

    public int getFloor() {
        return floor;
    }

    public String getDetails() {
        return details;
    }

    public boolean isHeader() {
        return isHeader;
    }
}
