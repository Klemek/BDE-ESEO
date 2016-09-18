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

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by François L. on 17/08/2015.
 */
public class LacmdMenu extends LacmdRoot {
    private String mainElemStr;
    private int maxMainElem, maxSecoElem;
    public final static String ID_CAT_MENU = "lacmd-menus";

    public LacmdMenu(JSONObject obj) throws JSONException {
        super(obj.getString("name"), obj.getString("idstr"), 0, 1, obj.getDouble("price"), ID_CAT_MENU); // no ingredients, but elements yes
        // expressions
        mainElemStr = obj.getString("mainElemStr");
        maxMainElem = obj.getInt("nbMainElem");
        maxSecoElem = obj.getInt("nbSecoElem");
    }

    public String getMainElemStr() {
        return mainElemStr;
    }

    public int getMaxMainElem() {
        return maxMainElem;
    }

    public int getMaxSecoElem() {
        return maxSecoElem;
    }

    public LacmdMenu(LacmdMenu obj) {
        super(obj.getName(), obj.getIdstr(), obj.hasIngredients(), obj.hasElements(), obj.getPrice(), ID_CAT_MENU);
        mainElemStr = obj.getMainElemStr();
        maxMainElem = obj.getMaxMainElem();
        maxSecoElem = obj.getMaxSecoElem();
    }

    /*

    @Override
    public String toString() {
        return "LacmdMenu{" +
                "name='" + name + '\'' +
                ", idstr='" + idstr + '\'' +
                ", price=" + price +
                '}';
    }*/

    @Override
    public String toString() {
        return "LacmdMenu{" +
                "name='" + getName() + '\'' +
                ", idstr='" + idstr + '\'' +
                ", price=" + price +
                '}';
    }
}
