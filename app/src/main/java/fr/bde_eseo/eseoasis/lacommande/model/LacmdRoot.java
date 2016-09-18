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

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by François L. on 18/08/2015.
 * Classe mère pour l'héritage et la récupération de paramètres communs menus / élements
 */
public class LacmdRoot {
    protected String name, idstr, objectType;
    protected double price;
    protected ArrayList<LacmdRoot> items;

    public ArrayList<LacmdRoot> getItems() {
        return items;
    }

    public void setItems(ArrayList<LacmdRoot> items) {
        this.items = items;
    }

    public int hasIngredients() {

        return hasIngredients;
    }

    public int hasElements() {
        return hasElements;
    }

    protected int hasIngredients;
    protected int hasElements;

    public double getPrice() {
        return price;
    }

    public String getFormattedPrice() {
        return (new DecimalFormat("0.00").format(calcRealPrice(false)) + "€");
    }

    public LacmdRoot(String name, String idstr, int hasIngredients, int hasElements, double price, String objType) {
        this.name = name;
        this.idstr = idstr;
        this.hasIngredients = hasIngredients;
        this.hasElements = hasElements;
        this.price = price;
        this.objectType = objType;
    }

    public LacmdRoot (LacmdRoot root) {
        this.name = root.getName();
        this.idstr = root.getIdstr();
        this.hasIngredients = root.hasIngredients();
        this.hasElements = root.hasElements();
        this.price = root.getPrice();
        this.objectType = root.objectType;
        if (root.getItems() == null)
            this.items = null;
        else
            this.items = new ArrayList<>(root.getItems());
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {

        return name;
    }

    public String getIdstr() {

        return idstr;
    }

    public String getObjectType() {
        return objectType;
    }

    @Override
    public String toString() {
        return "LacmdRoot{" +
                "name='" + name + '\'' +
                ", idstr='" + idstr + '\'' +
                '}';
    }

    // Returns a String style jambon, fromage, tomate
    // single : true if u want to display title of item, false otherwise
    // True : title (a, b, c)
    // False : a, b, c
    public String getFriendlyString (boolean single) {
        String friendly = "";
        if (single) friendly += name;
        if (items != null && items.size() > 0) {
            if (single) friendly += " (";
            for (int i = 0; i < items.size(); i++) {
                if (i != 0) {
                    //if (single)
                        friendly += ", ";
                    //else friendly += "\n - ";
                }
                friendly += items.get(i).getFriendlyString(true);
            }
            if (single) friendly += ") ";
        }
        return friendly;
    }

    // Returns the price as double
    // If root is ingredient : price = element's priceuni
    // If root is element : (priceuni or, if parent is menu, price more) + (nbitems-hasingredients) -> > 0 * itemprice
    // If root is menu : menu price + elements
    public double calcRealPrice (boolean parentIsMenu) {
        double realPrice = 0.0;

        switch (objectType) {
            case LacmdIngredient.ID_CAT_INGREDIENT:  // Ingredient
                realPrice += price;
                break;
            case LacmdElement.ID_CAT_ELEMENT:  // Element
                if (items != null) {
                    for (int i = 0; i < items.size(); i++) {
                        if (i >= hasIngredients) {// More than max ingredients allowed
                            realPrice += items.get(i).calcRealPrice(false);
                        }
                    }
                }
                if (!parentIsMenu) { // Lonely item : price is priceuni -> price in Parent Object
                    realPrice += price;
                } else { // Element in a menu : price is pricemore
                    realPrice += ((LacmdElement) this).getPricemore();
                }
                break;
            case LacmdMenu.ID_CAT_MENU:  // Menu
                if (items != null) {
                    for (int i = 0; i < items.size(); i++) {
                        realPrice += items.get(i).calcRealPrice(true);
                    }
                }
                realPrice += price;
                break;
        }

        return realPrice;
    }

    // Returns a JSON object as String
    public String toJSONString () {
        String strJson = "{";
        switch (objectType) {
            case LacmdElement.ID_CAT_ELEMENT:
                strJson += "\"element\":\"" + idstr + "\"";
                if (items != null && items.size() > 0) {
                    strJson += ", \"items\":[";
                    for (int i=0;i<items.size();i++) {
                        if (i!=0) strJson += ", ";
                        strJson += "\"" + items.get(i).idstr + "\"";
                    }
                    strJson += "]";
                }
                break;

            case LacmdMenu.ID_CAT_MENU:
                strJson += "\"menu\":\"" + idstr + "\"";
                if (items != null && items.size() > 0) {
                    strJson += ", \"items\":[";
                    for (int i=0;i<items.size();i++) {
                        if (i!=0) strJson += ", ";
                        strJson += items.get(i).toJSONString();
                    }
                    strJson += "]";
                }
                break;
        }
        strJson += "}";
        return strJson;
    }
}
