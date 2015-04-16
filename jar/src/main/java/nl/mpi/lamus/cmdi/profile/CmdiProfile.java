package nl.mpi.lamus.cmdi.profile;

import java.net.URI;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

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

/**
 * Part of the classes to be populated with the content of the XML file listing
 * CMDI allowed profiles.
 * This particular one represents the "profile" element in the XML file.
 * @author guisil
 */
@XmlRootElement(name = "profile")
@XmlAccessorType(XmlAccessType.FIELD)
public class CmdiProfile {
    
    @XmlAttribute
    private String id;
    
    @XmlAttribute
    private String name;
    
    @XmlElement
    private URI location;
    
    @XmlElementWrapper(name = "allowedReferenceTypes")
    @XmlElement(name = "allowedReferenceType")
    private List<String> allowedReferenceTypes;
    
    @XmlElement
    private String displayIcon;
    
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    public URI getLocation() {
        return location;
    }
    public void setLocation(URI location) {
        this.location = location;
    }
    
    public List<String> getAllowedReferenceTypes() {
        return allowedReferenceTypes;
    }
    public void setAllowedReferenceTypes(List<String> allowedReferenceTypes) {
        this.allowedReferenceTypes = allowedReferenceTypes;
    }
    
    public String getDisplayIcon() {
        return displayIcon;
    }
    public void setDisplayIcon(String displayIcon) {
        this.displayIcon = displayIcon;
    }
}
