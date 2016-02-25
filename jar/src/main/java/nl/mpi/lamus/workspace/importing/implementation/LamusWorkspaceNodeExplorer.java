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
package nl.mpi.lamus.workspace.importing.implementation;

import java.io.IOException;
import java.util.Collection;
import javax.xml.transform.TransformerException;
import nl.mpi.lamus.exception.UnusableReferenceTypeException;
import nl.mpi.lamus.exception.WorkspaceImportException;
import nl.mpi.lamus.metadata.MetadataApiBridge;
import nl.mpi.lamus.workspace.importing.NodeImporter;
import nl.mpi.lamus.workspace.importing.NodeImporterAssigner;
import nl.mpi.lamus.workspace.importing.WorkspaceNodeExplorer;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see WorkspaceNodeExplorer
 * 
 * @author guisil
 */
@Component
public class LamusWorkspaceNodeExplorer implements WorkspaceNodeExplorer {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusWorkspaceNodeExplorer.class);
    
    @Autowired
    private NodeImporterAssigner nodeImporterAssigner;
    @Autowired
    private MetadataApiBridge metadataApiBridge;

    
    /**
     * @see WorkspaceNodeExplorer#explore(
     *          nl.mpi.lamus.workspace.model.Workspace, nl.mpi.lamus.workspace.model.WorkspaceNode,
     *          nl.mpi.metadata.api.model.ReferencingMetadataDocument, java.util.Collection)
     */
    @Override
    public void explore(Workspace workspace, WorkspaceNode nodeToExplore, ReferencingMetadataDocument nodeDocument, Collection<Reference> linksInNode)
        throws WorkspaceImportException {
        
        logger.debug("Exploring references in metadata node to import; workspaceID: " + workspace.getWorkspaceID() + "; nodeID: " + nodeToExplore.getWorkspaceNodeID());
        
        for(Reference currentLink : linksInNode) {

            NodeImporter linkImporterToUse = null;
            try {
                linkImporterToUse = nodeImporterAssigner.getImporterForReference(currentLink);
            } catch(UnusableReferenceTypeException ex) {
                logger.warn(ex.getMessage());
                continue;
            } catch(IllegalArgumentException ex) {
                String errorMessage = "Error getting file importer";
                throw new WorkspaceImportException(errorMessage, workspace.getWorkspaceID(), ex);
            }
            
            linkImporterToUse.importNode(workspace, nodeToExplore, nodeDocument, currentLink);
        }
        
        try {
            metadataApiBridge.saveMetadataDocument(nodeDocument, nodeToExplore.getWorkspaceURL());
        } catch (IOException | TransformerException | MetadataException ioex) {
            String errorMessage = "Failed to save file " + nodeToExplore.getWorkspaceURL()
		    + " in workspace " + workspace.getWorkspaceID();
	    throwWorkspaceImportException(workspace.getWorkspaceID(), errorMessage, ioex);
        }
    }
    
    
    private void throwWorkspaceImportException(int workspaceID, String errorMessage, Exception cause) throws WorkspaceImportException {
        logger.error(errorMessage, cause);
        throw new WorkspaceImportException(errorMessage, workspaceID, cause);
    }
    
    
}
