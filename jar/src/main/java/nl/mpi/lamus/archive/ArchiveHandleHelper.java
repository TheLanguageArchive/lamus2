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

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import javax.xml.transform.TransformerException;
import net.handle.hdllib.HandleException;
import nl.mpi.archiving.corpusstructure.core.NodeNotFoundException;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.metadata.api.MetadataException;

/**
 * Utility class regarding archive handles.
 * @author guisil
 */
public interface ArchiveHandleHelper {
    
    /**
     * Given a node URI, retrieves the archive handle.
     * @param nodeURI URI of the node
     * @return URI representing the archive handle
     */
    public URI getArchiveHandleForNode(URI nodeURI) throws NodeNotFoundException;

    /**
     * Deletes the handle for a given node.
     * This includes deleting the handle from the handle server, the self-handle
     * (if this is a metadata node) from the metadata document and from the
     * lamus2 database.
     * 
     * @param node Node to have the archive handle deleted
     * @param currenLocation current location of the node
     */
    public void deleteArchiveHandle(WorkspaceNode node, URL currenLocation)
            throws HandleException, IOException, TransformerException, MetadataException;
}
