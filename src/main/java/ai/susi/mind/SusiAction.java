/**
 *  SusiAction
 *  Copyright 29.06.2016 by Michael Peter Christen, @0rb1t3r
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *  
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program in the file lgpl21.txt
 *  If not, see <http://www.gnu.org/licenses/>.
 */

package ai.susi.mind;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import net.yacy.grid.mcp.Data;
import net.yacy.grid.tools.JSONList;

/**
 * An action is an application on the information deduced during inferences on mind states
 * as they are represented in an argument. If we want to produce respond sentences or if
 * we want to visualize deduces data as a graph or in a picture, thats an action.
 */
public class SusiAction {
    
    private JSONObject json;

    /**
     * initialize an action using a json description.
     * @param json
     */
    public SusiAction(JSONObject json) {
        this.json = json;
    }

    /**
     * if the action contains more String attributes where these strings are named, they can be retrieved here
     * @param attr the name of the string attribute
     * @return the action string
     */
    public String getStringAttr(String attr) {
        return this.json.has(attr) ? this.json.getString(attr) : "";
    }

    /**
     * An action is backed with a JSON data structure. That can be retrieved here.
     * @return the json structure of the action
     */
    public JSONObject toJSONClone() {
        JSONObject j = new JSONObject(true);
        this.json.keySet().forEach(key -> j.put(key, this.json.get(key))); // make a clone
        if (j.has("expression")) {
            j.remove("phrases");
            j.remove("select");
        }
        return j;
    }
    
    public boolean hasAsset(String name) {
        if (!this.json.has("assets")) return false;
        JSONObject assets = this.json.getJSONObject("assets");
        return assets.has(name);
    }
    
    public JSONList getJSONListAsset(String name) {
        if (!this.json.has("assets")) return null;
        JSONObject assets = this.json.getJSONObject("assets");
        JSONArray jsonlist = assets.getJSONArray(name);
        try {
			return new JSONList(jsonlist);
		} catch (IOException e) {
            Data.logger.warn("error in getJSONListAsset with name " + name, e);
			return null;
		}
    }
    
    /**
     * toString
     * @return return the json representation of the object as a string
     */
    public String toString() {
        return toJSONClone().toString();
    }
}
