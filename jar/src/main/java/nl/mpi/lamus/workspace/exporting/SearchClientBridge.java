/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.workspace.exporting;

/**
 * Interface for some operations to be performed using the annotation search client.
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public interface SearchClientBridge {
    
    /**
     * Adds the given node to the annotation search database
     * 
     * @param archiveNodeID ID of the node to add
     */
    public void addNode(int archiveNodeID);
    
    /**
     * Removes the given node from the annotation search database
     * 
     * @param archiveNodeID ID of the node to remove
     */
    public void removeNode(int archiveNodeID);
    
    /**
     * Checks if the given file format is usable in the annotation search database
     * @param format Format to be checked
     * @return true if the format is searchable
     */
    public boolean isFormatSearchable(String format);
}
