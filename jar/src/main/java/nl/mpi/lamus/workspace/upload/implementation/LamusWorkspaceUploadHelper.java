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
package nl.mpi.lamus.workspace.upload.implementation;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.workspace.management.WorkspaceNodeLinkManager;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.upload.WorkspaceUploadHelper;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author guisil
 */
@Component
public class LamusWorkspaceUploadHelper implements WorkspaceUploadHelper {

    private MetadataAPI metadataAPI;
    private WorkspaceNodeLinkManager workspaceNodeLinkManager;
    
    @Autowired
    public LamusWorkspaceUploadHelper(MetadataAPI mdAPI, WorkspaceNodeLinkManager wsNodeLinkManager) {
        this.metadataAPI = mdAPI;
        this.workspaceNodeLinkManager = wsNodeLinkManager;
    }

    @Override
    public void assureLinksInWorkspace(int workspaceID, Collection<WorkspaceNode> nodesToCheck) {
        
        
        for(WorkspaceNode node : nodesToCheck) {
            
            if(!node.isMetadata()) {
                continue;
            }
            
            MetadataDocument document;
            try {
                document = metadataAPI.getMetadataDocument(node.getWorkspaceURL());
            } catch (IOException ex) {
                throw new UnsupportedOperationException("exception not handled yet");
            } catch (MetadataException ex) {
                throw new UnsupportedOperationException("exception not handled yet");
            }
            
            if(!(document instanceof ReferencingMetadataDocument)) {
                continue;
            }
            
            List<Reference> references = ((ReferencingMetadataDocument)document).getDocumentReferences();
            
            for(Reference ref : references) {
            
                URI refURI = ref.getURI();
                
                for(WorkspaceNode innerNode : nodesToCheck) {
                    
//                    if(refURI.toString().contains(FilenameUtils.getName(innerNode.getWorkspaceURL().toString()))) {
                    if(innerNode.getWorkspaceURL().toString().contains(refURI.toString())) { //check if the node URL contains the relative path that comes in the link reference
                        
                        try {
                            ref.setURI(innerNode.getWorkspaceURL().toURI());
                        } catch (URISyntaxException ex) {
                            throw new UnsupportedOperationException("exception not handled yet");
                        }
                        
                        try {
                            workspaceNodeLinkManager.linkNodes(node, innerNode);
                        } catch (WorkspaceException ex) {
                            throw new UnsupportedOperationException("exception not handled yet");
                        }
                        
                        break;
                    }
                }
            }
        }
        
    }
    
}
