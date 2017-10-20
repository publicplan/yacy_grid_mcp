/**
 *  SchemaDeclaration
 *  Copyright 2011 by Michael Peter Christen
 *  First released 14.04.2011 at http://yacy.net
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

package net.yacy.grid.io.index;

import org.json.JSONObject;

public interface MappingDeclaration {

    /**
     * this shall be implemented as enum, thus shall have the name() method
     * @return the name of the enum constant
     */
    public String name(); // default field name (according to SolCell default schema) <= enum.name()
    
    /**
     * @return the default or custom solr field name to use for solr requests
     */
    public String getSolrFieldName();

    public MappingType getType();

    public boolean isIndexed();

    public boolean isStored();

    public boolean isMultiValued();

    public boolean isSearchable();

    public boolean isOmitNorms();
    
    public boolean isDocValue();

    public String getComment();

    public String getFacetname();
    
    public String getDisplayname();
    
    public String getFacettype();

    public String getFacetmodifier();

    /**
     * @return true when this field is mandatory for proper operation
     */
    public boolean isMandatory();

    public void setSolrFieldName(String name);
    
    public JSONObject toJSON();

}