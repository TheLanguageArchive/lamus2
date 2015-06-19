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

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Part of the classes to be populated with the content of the XML file listing
 * CMDI allowed profiles.
 * This particular one represents the root element of the file ("allowedProfiles").
 * @author guisil
 */
@XmlRootElement(name = "allowedProfiles")
@XmlAccessorType(XmlAccessType.FIELD)
public class AllowedCmdiProfiles {
    
    @XmlElement(name = "profile")
    private List<CmdiProfile> profiles;
    
    public List<CmdiProfile> getProfiles() {
        return profiles;
    }
    public void setProfiles(List<CmdiProfile> profiles) {
        this.profiles = profiles;
    }
    
    public CmdiProfile getProfile(String profileId) {
        for(CmdiProfile p : profiles) {
            if(profileId.equals(p.getId()) || profileId.contains(p.getId())) {
                return p;
            }
        }
        return null;
    }
}
