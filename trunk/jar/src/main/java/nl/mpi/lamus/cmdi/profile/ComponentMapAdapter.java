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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Part of the classes to be populated with the content of the XML file listing
 * CMDI allowed profiles. This particular class is related to the mapping of the
 * components where to add references to resource proxies.
 * @author guisil
 */
public class ComponentMapAdapter extends XmlAdapter<ComponentMap, Map<String, String>> {

    @Override
    public Map<String, String> unmarshal(ComponentMap v) throws Exception {
        Map<String, String> map = new HashMap<>();
        for(ComponentMapEntry entry : v.getEntries()) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    @Override
    public ComponentMap marshal(Map<String, String> v) throws Exception {
        ComponentMap compMap = new ComponentMap();
        Set<Entry<String, String>> entrySet = v.entrySet();
        for(Entry<String, String> entry : entrySet) {
            compMap.addEntry(new ComponentMapEntry(entry.getKey(), entry.getValue()));
        }
        return compMap;
    }
    
}
