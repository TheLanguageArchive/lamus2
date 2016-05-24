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
package nl.mpi.lamus.archive;

import java.net.URI;
import java.util.List;
import nl.mpi.lamus.workspace.model.WorkspaceNode;

/**
 * Class with some convenience methods to interact with
 * the corpus structure API.
 * @author guisil
 */
public interface CorpusStructureBridge {
    
    /**
     * Constant to be used in exporters that don't require the path
     * to be determined, so that it's ignored.
     */
    public static final String IGNORE_CORPUS_PATH = "IGNORE:CORPUS:PATH";
    
    /**
     * Determines the path (comprised of node names of the ancestor corpus nodes,
     * up to the closest top node) to be used as the base to the given node path in the archive.
     * @param node WorkspaceNode for which to determine the path
     * @return String containing the path to the closest top node
     */
    public String getCorpusNamePathToClosestTopNode(WorkspaceNode node);
    
    /**
     * Retrieves a list containing the URIs (as strings)
     * of both the ancestors and descendants of the given node.
     * @param nodeURI URI of the node
     * @return list with URIs (as strings) containing ancestors and descendants
     */
    public List<String> getURIsOfAncestorsAndDescendants(URI nodeURI);
    
    /**
     * Retrieves a list containing the URIs
     * of both the ancestors and descendants of the given node.
     * @param nodeURI URI of the node
     * @return list with URIs containing ancestors
     */
    public List<URI> getURIsOfAncestors(URI nodeURI);
    
    /**
     * Retrieves the name of the parent directory for the defined corpusstructure directory.
     * @param nodeURI URI of the node
     * @return list with URIs containing ancestors
     */
    public String getFolderNameBeforeCorpusstructure(String directory);
}
