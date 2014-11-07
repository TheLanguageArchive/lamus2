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
import java.util.ArrayList;
import java.util.Collection;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.upload.WorkspaceUploadHelper;
import nl.mpi.lamus.workspace.upload.WorkspaceUploadReferenceHandler;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see WorkspaceUploadHelper
 * 
 * @author guisil
 */
@Component
public class LamusWorkspaceUploadHelper implements WorkspaceUploadHelper {

    private static final Logger logger = LoggerFactory.getLogger(LamusWorkspaceUploadHelper.class);
    
    private MetadataAPI metadataAPI;
    private WorkspaceUploadReferenceHandler workspaceUploadReferenceHandler;
    
    @Autowired
    public LamusWorkspaceUploadHelper(MetadataAPI mdAPI,
            WorkspaceUploadReferenceHandler wsUploadReferenceHandler) {
        this.metadataAPI = mdAPI;
        this.workspaceUploadReferenceHandler = wsUploadReferenceHandler;
    }

    /**
     * @see WorkspaceUploadHelper#assureLinksInWorkspace(int, java.util.Collection)
     */
    @Override
    public Collection<UploadProblem> assureLinksInWorkspace(int workspaceID, Collection<WorkspaceNode> nodesToCheck) {
        
        Collection<UploadProblem> allFailedLinks = new ArrayList<>();
        
        for(WorkspaceNode node : nodesToCheck) {
            
            if(!node.isMetadata()) {
                continue;
            }
            
            MetadataDocument document;
            try {
                document = metadataAPI.getMetadataDocument(node.getWorkspaceURL());
            } catch (IOException | MetadataException ex) {
                logger.error("Document could not be loaded for " + node.getWorkspaceURL(), ex);
                continue;
            }
            
            if(!(document instanceof ReferencingMetadataDocument)) {
                continue;
            }
            
            ReferencingMetadataDocument referencingDocument = (ReferencingMetadataDocument) document;
            
            Collection<UploadProblem> failedLinks = workspaceUploadReferenceHandler.matchReferencesWithNodes(workspaceID, nodesToCheck, node, referencingDocument);
            
            allFailedLinks.addAll(failedLinks);
        }
        return allFailedLinks;
    }
    
}
