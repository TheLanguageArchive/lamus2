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

import java.net.URL;

/**
 * Interface for some operations to be performed using the corpusstructure API.
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public interface CorpusStructureBridge {
    
    /**
     * Updates the archive URL for the given node
     * @param archiveNodeID ID of the node to update
     * @param oldArchiveNodeURL Old archive URL for the given node
     * @param newArchiveNodeURL New archive URL for the given node
     * @return true if node was properly updated or if there is nothing to update (node is new or URLs are the same)
     * @throws IllegalArgumentException when new URL is null or not a valid URI
     */
    public boolean updateArchiveObjectsNodeURL(int archiveNodeID, URL oldArchiveNodeURL, URL newArchiveNodeURL);
    
    /**
     * Adds a new node to the corpusstructure database (archiveobjects table)
     * @param nodeArchiveURL archive URL of the node to add
     * @param username user who will get access to the node
     * @return ID of the newly created node
     */
    public int addNewNodeToCorpusStructure(URL nodeArchiveURL, String pid, String userID);
    
    /**
     * Links two nodes in the corpusstructure database
     * @param parentNodeArchiveID ID of the parent node
     * @param childNodeArchiveID ID of the child node
     * @return true if successful
     */
    public boolean linkNodesInCorpusStructure(int parentNodeArchiveID, int childNodeArchiveID);
    
    /**
     * Calculates the MD5 checksum for the given file
     * @param nodeURL URL of the file
     * @return MD5 checksum
     */
    public String getChecksum(URL nodeURL);
    
    /**
     * Ensures a correct MD5 checksum in the database.
     * If there is none (new node), a new one is generated.
     * If there is one already, checks if it matches the given file, and recalculates if not.
     * @param nodeArchiveID ID of the node to update
     * @param nodeURL URL of the file with which the MD5 checksum should match
     * @return true if MD5 checksum was updated
     */
    public boolean ensureChecksum(int nodeArchiveID, URL nodeURL);
    
    /**
     * Generates a PID for the given node
     * @param nodeArchiveID ID of the node
     * @return PID for the given node
     */
    public String calculatePID(int nodeArchiveID);

    /**
     * Updates the PID for the given node
     * @param archiveNodeID ID of the node to update
     * @param pid New PID for the given node
     */
    public void updateArchiveObjectsNodePID(int archiveNodeID, String pid);
}
