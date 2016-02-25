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
 *
 * @author guisil
 */
public enum WorkspaceNodeStatus {

    /**
     * Node (Metadata) is an unchanged copy from the Archive.
     */
    ARCHIVE_COPY,
    
    /**
     * Node was uploaded, has no file counterpart in the Archive.
     */
    UPLOADED,

    /**
     * Node (Metadata) was created without physical file
     * counterpart in the WS or the Archive.
     */
    CREATED,
    
    /**
     * Node is deleted from the DB.
     * Should later be deleted from the Archive.
     */
    DELETED,
    
    /**
     * Node (Resource) is virtual, just a placeholder for a file in the Archive.
     */
    VIRTUAL,
    
    /**
     * Node is external. Points to some URL in the outside world.
     */
    EXTERNAL,
    
    /**
     * Node is external and has been deleted from the DB.
     * Should later be deleted from the Archive.
     */
    EXTERNAL_DELETED,
    
    /**
     * Node got replaced by another node.
     */
    REPLACED;

    
    @Override
    public String toString() {
        return (name().charAt(0) + name().substring(1).toLowerCase()).replace("_", " ");
    }
}
