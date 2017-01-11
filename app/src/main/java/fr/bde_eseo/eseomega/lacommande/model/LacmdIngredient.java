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

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by François L. on 17/08/2015.
 */
public class LacmdIngredient extends LacmdRoot {
    public final static String ID_CAT_INGREDIENT = "lacmd-ingredients";
    private int stock;

    public LacmdIngredient (JSONObject obj) throws JSONException {
        super(obj.getString("name"), obj.getString("idstr"), 0, 0, obj.getDouble("priceuni"), ID_CAT_INGREDIENT);
        this.stock = obj.getInt("stock");
    }

    @Override
    public String toString() {
        return "LacmdIngredient{" +
                "name='" + name + '\'' +
                ", idstr='" + idstr + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                '}';
    }

    public String getName() {
        return name;
    }

    public String getIdstr() {
        return idstr;
    }

    public double getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }
}
