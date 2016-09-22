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

import java.text.DecimalFormat;

import fr.bde_eseo.eseomega.Constants;

/**
 * Created by François L. on 21/07/2015.
 */
public class LacmdCategory {

    private String name;
    private String imgUrl;
    private double beginPrice;
    private String smallText;
    private String catname;

    public LacmdCategory(JSONObject obj) throws JSONException {
        this.name = obj.getString("name");
        this.imgUrl = obj.getString("imgUrl");
        this.beginPrice = obj.getDouble("firstPrice");
        this.catname = obj.getString("catname");
        this.smallText = obj.getString("briefText");
    }

    public String getName() {
        return name;
    }

    public String getImgUrl() {
        return Constants.URL_ASSETS + imgUrl;
    }

    public String getBeginPriceAsStr() {
        return "À partir de " + new DecimalFormat("0.00").format(beginPrice) + "€";
    }

    public String getCatname() {
        return catname;
    }

    public String getSmallText() {
        return smallText;
    }

    @Override
    public String toString() {
        return "LacmdCategory{" +
                "name='" + name + '\'' +
                ", imgUrl='" + imgUrl + '\'' +
                ", beginPrice=" + beginPrice +
                ", smallText='" + smallText + '\'' +
                ", catname='" + catname + '\'' +
                '}';
    }
}
