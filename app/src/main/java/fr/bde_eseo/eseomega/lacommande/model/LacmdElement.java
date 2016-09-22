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
public class LacmdElement extends LacmdRoot {
    private String idcat;
    private double pricemore;
    private int stock, outofmenu;
    public final static String ID_CAT_ELEMENT = "lacmd-elements";

    public LacmdElement(String name, String idstr, double priceuni, double pricemore, int stock, int outofmenu, int hasingredients) {
        super(name, idstr, hasingredients, 0, priceuni, ID_CAT_ELEMENT);
        this.pricemore = pricemore;
        this.stock = stock;
        this.outofmenu = outofmenu;
    }

    public LacmdElement (JSONObject obj) throws JSONException {
        super(obj.getString("name"), obj.getString("idstr"), obj.getInt("hasingredients"), 0, obj.getDouble("priceuni"), ID_CAT_ELEMENT);
        this.pricemore = obj.getDouble("pricemore");
        this.stock = obj.getInt("stock");
        this.outofmenu = obj.getInt("outofmenu");
        this.idcat = obj.getString("idcat");
    }

    public LacmdElement (LacmdElement obj) {
        super(obj.getName(), obj.getIdstr(), obj.hasIngredients(), obj.hasElements(), obj.getPrice(), ID_CAT_ELEMENT);
        this.pricemore = obj.getPricemore();
        this.stock = obj.stock;
        this.outofmenu = obj.outofmenu;
        this.idcat = getIdcat();
    }

    public String getIdcat() {
        return idcat;
    }

    public double getPricemore() {
        return pricemore;
    }

    public int getOutofmenu() {
        return outofmenu;
    }

    @Override
    public String toString() {
        return "LacmdElement{" +
                "name='" + getName() + '\'' +
                ", idstr='" + idstr + '\'' +
                ", priceuni=" + price +
                ", pricemore=" + pricemore +
                ", stock=" + stock +
                ", outofmenu=" + outofmenu +
                ", hasingredients=" + hasIngredients +
                '}';
    }
}
