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

/**
 * Interface for adjusting the permissions of files submitted from a certain
 * workspace according to the configuration file.
 * @author guisil
 */
public interface PermissionAdjuster {
    
    /**
     * Checks the file permissions for a given submitted workspace and
     * compares them with the desired permissions in the
     * configuration file, changing them if necessary.
     * @param workspaceID 
     */
    public void adjustPermissions(int workspaceID);
}
