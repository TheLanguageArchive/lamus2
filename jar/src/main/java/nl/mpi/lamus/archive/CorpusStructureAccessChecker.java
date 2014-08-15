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
package nl.mpi.lamus.archive;

import java.net.URI;

/**
 * Interface providing access checking functionality regarding
 * the corpus structure database.
 * @author guisil
 */
public interface CorpusStructureAccessChecker {
    
    /**
     * Checks if the given user has write access to the given node.
     * 
     * @param userId ID of the user
     * @param archiveNodeURI URI of the node
     * @return true if the user has access to the node
     */
    public boolean hasWriteAccess(String userId, URI archiveNodeURI);
}
