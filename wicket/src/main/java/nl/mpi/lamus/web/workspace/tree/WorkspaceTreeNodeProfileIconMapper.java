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
package nl.mpi.lamus.web.workspace.tree;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nl.mpi.lamus.cmdi.profile.AllowedCmdiProfiles;
import nl.mpi.lamus.cmdi.profile.CmdiProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class used to map certain CMDI profiles with the appropriate icons.
 * @author guisil
 */
@Component
public class WorkspaceTreeNodeProfileIconMapper {
    
    private final AllowedCmdiProfiles allowedProfiles;
    private final Map<String, String> iconsMap;
    
    
    @Autowired
    public WorkspaceTreeNodeProfileIconMapper(AllowedCmdiProfiles allowedProfiles) {
        this.allowedProfiles = allowedProfiles;
        iconsMap = new HashMap<>();
        iconsMap.put("corpus", "corpus.png");
        iconsMap.put("session", "session.gif");
    }
    
    /**
     * @param profileLocation Profile location, which should contain the profile ID
     * @return name of the icon file to use for the given profile
     */
    public String matchProfileIdWithIconName(URI profileLocation) {
        
        if(allowedProfiles == null || profileLocation == null || profileLocation.toString().isEmpty()) {
            return "clarin.png";
        }
        
        String profileLocationStr = profileLocation.toString();
        
        List<CmdiProfile> profiles = allowedProfiles.getProfiles();
        
        for(CmdiProfile profile : profiles) {
            if(profileLocationStr.contains(profile.getId())) {
                if(profile.getDisplayIcon() == null || profile.getDisplayIcon().isEmpty()) {
                    continue;
                }
                if(iconsMap.containsKey(profile.getDisplayIcon())) {
                    return iconsMap.get(profile.getDisplayIcon());
                }
            }
        }
        
        return "clarin.png";
    }
}
