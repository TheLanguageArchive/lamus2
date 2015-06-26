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
package nl.mpi.lamus.workspace.model;

import nl.mpi.archiving.corpusstructure.core.CorpusNodeType;
import nl.mpi.lamus.cmdi.profile.CmdiProfile;

/**
 * Utilities related with nodes.
 * @author guisil
 */
public interface NodeUtil {
    
    /**
     * @param corpusNodeType archive node type to convert
     * @return equivalent workspace node type
     */
    public WorkspaceNodeType convertArchiveNodeType(CorpusNodeType corpusNodeType);
    
    /**
     * @param mimetype mimetype to convert
     * @return equivalent workspace node type
     */
    public WorkspaceNodeType convertMimetype(String mimetype);
    
    /**
     * @param wsNode workspace node to check
     * @return true if node has a metadata type
     */
    public boolean isNodeMetadata(WorkspaceNode wsNode);
    
    /**
     * @param wsNodeType workspace node type to check
     * @return true if type corresponds to metadata
     */
    public boolean isTypeMetadata(WorkspaceNodeType wsNodeType);
    
    /**
     * @param profile Profile to check
     * @return true if the profile object corresponds either to
     * "lat-corpus" or "lat-session".
     */
    public boolean isProfileLatCorpusOrSession(CmdiProfile profile);
}
