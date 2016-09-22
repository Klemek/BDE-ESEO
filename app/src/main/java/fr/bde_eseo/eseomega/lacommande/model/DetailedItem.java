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

package fr.bde_eseo.eseomega.lacommande.model;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import fr.bde_eseo.eseomega.Constants;

/**
 * Created by François L. on 20/07/2015.
 */
public class DetailedItem extends HistoryItem {

    private String instructions, imgUrl;
    private int idlydia;
    private boolean paidbefore, lydiaEnabled;

    public DetailedItem(Context ctx, JSONObject obj, int idcmd) throws JSONException {

        super(
                ctx,
                obj.getString("resume"),
                obj.getInt("status"),
                obj.getDouble("price"),
                obj.getString("datetime"),
                idcmd,
                obj.getInt("modcmd"),
                obj.getString("strcmd"),
                true
        );

        instructions = obj.getString("instructions");
        imgUrl = obj.getString("imgurl");
        idlydia = obj.getInt("idlydia");
        paidbefore = obj.getInt("paidbefore") == 1;
        lydiaEnabled = obj.getBoolean("lydia_enabled");

    }

    public String getInstructions() {
        return instructions;
    }

    public String getImgUrl() {
        return Constants.URL_ASSETS + imgUrl;
    }

    public int getIdlydia() {
        return idlydia;
    }

    public boolean isPaidbefore() {
        return paidbefore;
    }

    public boolean isLydiaEnabled() {
        return lydiaEnabled;
    }
}
