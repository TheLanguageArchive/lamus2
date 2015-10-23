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
package nl.mpi.lamus.archive.permissions;

import java.io.FileNotFoundException;
import java.io.IOException;
import nl.mpi.lamus.archive.permissions.implementation.ApaPermission;

/**
 *
 * @author guisil
 */
public interface PermissionAdjusterHelper {
    
    /**
     * Loads the permission information from the configuration file.
     */
    public void loadConfiguredPermissions() throws FileNotFoundException, IOException;
    
    /**
     * Gets the current permissions for the given path.
     * @param path
     * @return 
     */
    public ApaPermission getCurrentPermissionsForPath(String path);
    
    /**
     * Gets the desired permissions for the given path from the configuration file.
     * @param path
     * @return 
     */
    public ApaPermission getDesiredPermissionsForPath(String path);
    
    /**
     * Compares the current and desired permissions for the given path
     * and adjusts them if necessary.
     * @param path
     * @param currentPermissions
     * @param desiredPermissions
     * @return true if successful
     */
    public boolean checkAndRepairFile(String path, ApaPermission currentPermissions, ApaPermission desiredPermissions);
}
