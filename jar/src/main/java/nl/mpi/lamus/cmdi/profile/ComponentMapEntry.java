/*
 * Copyright (C) 2015 Max Planck Institute for Psycholinguistics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.mpi.lamus.cmdi.profile;

import javax.xml.bind.annotation.XmlElement;

/**
 * Part of the classes to be populated with the content of the XML file listing
 * CMDI allowed profiles. This particular class is related to the mapping of the
 * components where to add references to resource proxies.
 * @author guisil
 */
public class ComponentMapEntry {
    
    @XmlElement(name = "key")
    private String key;
    
    @XmlElement(name = "value")
    private String value;
    
    public ComponentMapEntry() {
        
    }
    
    public ComponentMapEntry(String key, String value) {
        this.key = key;
        this.value = value;
    }
    
    
    public String getKey() {
        return key;
    }
    public String getValue() {
        return value;
    }
}
