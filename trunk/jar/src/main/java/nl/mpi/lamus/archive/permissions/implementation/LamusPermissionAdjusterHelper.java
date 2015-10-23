/*
 * Copyright (C) 2014 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.archive.permissions.implementation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.regex.Pattern;
import mpi.jnatools.jnaChmodChgrp;
import nl.mpi.lamus.archive.permissions.PermissionAdjusterHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Adapted from the old LAMUS.
 * @see PermissionAdjusterHelper
 * @author guisil
 */
@Component
public class LamusPermissionAdjusterHelper implements PermissionAdjusterHelper {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusPermissionAdjusterHelper.class);
    
    private TreeMap<String, ApaPermission> configuredPermissions;
    
    @Autowired
    @Qualifier("permissionConfigFile")
    private File permissionConfigFile;

    /**
     * @see PermissionAdjusterHelper#loadConfiguredPermissions()
     */
    @Override
    public void loadConfiguredPermissions() throws FileNotFoundException, IOException {
        
        configuredPermissions = new TreeMap<>();
        
        if(permissionConfigFile == null) {
            throw new IOException("Configuration file not specified");
        }
        
        try (BufferedReader input = new BufferedReader(new FileReader(permissionConfigFile))) {
            String line = null; // not declared within while loop
            // readLine returns null at EOF. Returns line without newline.
            while (( line = input.readLine()) != null) {
                // filter out comments and blank lines
                if ((line.startsWith("#")) || (line.trim().length()==0))
                    continue;
                String[] parts = line.split("\\s+");
                if (parts.length != 4)
                    continue; // unparseable line
                String path =  parts[0]; // path - convert wildcards to regexp:
                path = path.replace(".", "\\.").replace("*", ".*").replace("?", ".");
                int mode = Integer.parseInt(parts[1], 8); // mode (octal string)
                // String owner = parts[2]; // owner (ignored here)
                String group = parts[3]; // group
                configuredPermissions.put(path, new ApaPermission(group, mode));
            }
        }
    }

    /**
     * @see PermissionAdjusterHelper#getCurrentPermissionsForPath(java.lang.String)
     */
    @Override
    public ApaPermission getCurrentPermissionsForPath(String path) {
        
        int groupid = jnaChmodChgrp.getGid(path);
        int perm = jnaChmodChgrp.getMode(path);
        return new ApaPermission(groupid, perm);
    }

    @Override
    public ApaPermission getDesiredPermissionsForPath(String path) {
        
        ApaPermission mostSpecificPermission = null;
        for (Iterator<String> it = configuredPermissions.keySet().iterator(); it.hasNext(); ) {
            String regex = it.next(); // keys are regexp path strings
            if (Pattern.matches(regex, path)) { // NOTE: must match whole string!
                mostSpecificPermission = configuredPermissions.get(regex);
                // NOTE: cannot return first match, as later rules are more
                // specific and need priority over earlier, generic rules!
                // In Java 1.6, TreeMap.descendingMap() would help us here.
            }
        }
        
        return mostSpecificPermission;
    }
    
    /**
     * @see PermissionAdjusterHelper#checkAndRepairFile(
     *      java.lang.String, nl.mpi.lamus.archive.permissions.implementation.ApaPermission,
     *      nl.mpi.lamus.archive.permissions.implementation.ApaPermission)
     */
    @Override
    public boolean checkAndRepairFile(String path, ApaPermission currentPermissions, ApaPermission desiredPermissions) {
        
        if (desiredPermissions == null) { // give up if no desired permissions defined here
            logger.warn("No target permissions defined, file unchanged: " + path);
            return false; // no changes
        }
        if ((desiredPermissions.compare(currentPermissions) & 2) != 0) { // wrong mode, change:
            logger.info("Changing mode from: " + Integer.toString(currentPermissions.getMode(), 8) +
                " to: " + Integer.toString(desiredPermissions.getMode(), 8) + " for: " + path);
            jnaChmodChgrp.chmod(path, desiredPermissions.getMode());
        }
        if ((desiredPermissions.compare(currentPermissions) & 1) != 0) { // wrong group, change:
            logger.info("Changing group from: " + currentPermissions.getGroupName() + "/" + currentPermissions.getGID() +
                " to: " + desiredPermissions.getGroupName() + "/" + desiredPermissions.getGID() + " for: " + path);
            jnaChmodChgrp.chgrp(path, desiredPermissions.getGID());
        }
        return (desiredPermissions.compare(currentPermissions) != 0); // true if anything was wrong
    }
}
