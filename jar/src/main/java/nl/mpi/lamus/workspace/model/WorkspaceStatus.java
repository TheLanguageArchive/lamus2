/*
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.mpi.lamus.workspace.model;

/**
 * Enumeration for the different status values a workspace can have during its lifetime.
 * @author guisil
 */
public enum WorkspaceStatus {
    
    /**
     * The request is unitialised, the object was just created.
     */
    UNINITIALISED,
    
    /**
     * The request is initialising, workspace not yet finished.
     */
    INITIALISING,
    
    /**
     * An error occurred during initialisation.
     */
    ERROR_INITIALISATION,
    
    /**
     * The request is initialised, workspace is finished and in use.
     */
    INITIALISED,
    
    /**
     * The owner is not connected, the request is dormant.
     */
    SLEEPING,
    
    /**
     * The request was submitted by the user (will move the workspace to the archive)
     * Workspaces will move to UPDATING_ARCHIVE and SUCCESS
     * or ERROR_MOVING_DATA from this state.
     */
    SUBMITTED,
    
    /**
     * The request timed out.
     */
    TIMEOUT,
    
    /**
     * The request was refused.
     */
    REFUSED,
    
    /**
     * Workspace was submitted and the data successfully moved to the archive.
     */
    SUCCESS,
    
    /**
     * Workspace was submitted but there was an error moving the data to the archive.
     */
    ERROR_MOVING_DATA,
    
    /**
     * The data was successfully moved from the workspace to the archive BUT the
     * archive crawler and AMS2 still have to recalculate the archive database
     * contents which describe the updated part...! If IR gets stuck here, use
     * ArchiveCrawler and AMS2 manually and leave this IR state or delete the IR
     * afterwards.
     */
    UPDATING_ARCHIVE,
    
    /**
     * An error occurred when crawling.
     */
    ERROR_CRAWLING,
    
    /**
     * An error occurred when creating versions of replaced nodes.
     */
    ERROR_VERSIONING;

    
    @Override
    public String toString() {
        return (name().charAt(0) + name().substring(1).toLowerCase()).replace("_", " - ");
    }
}