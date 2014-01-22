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
import nl.mpi.metadata.api.model.Reference;
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
    public void assureLinksInWorkspace(int workspaceID, Collection<WorkspaceNode> nodesToCheck) {
        
        
        for(WorkspaceNode node : nodesToCheck) {
            
            if(!node.isMetadata()) {
                continue;
            }
            
            MetadataDocument document = null;
            try {
                document = metadataAPI.getMetadataDocument(node.getWorkspaceURL());
            } catch (IOException ex) {
                logger.error("Document could not be loaded for " + node.getWorkspaceURL(), ex);
                continue;
            } catch (MetadataException ex) {
                logger.error("Document could not be loaded for " + node.getWorkspaceURL(), ex);
                continue;
            }
            
            if(!(document instanceof ReferencingMetadataDocument)) {
                continue;
            }
            
            ReferencingMetadataDocument referencingDocument = (ReferencingMetadataDocument) document;
            Collection<Reference> failedLinks = new ArrayList<Reference>();
            
            workspaceUploadReferenceHandler.matchReferencesWithNodes(workspaceID, nodesToCheck, node, referencingDocument, failedLinks);
            
            if(!failedLinks.isEmpty()) {
                
                //TODO SHOW ERROR
                //TODO SHOW ERROR
                //TODO SHOW ERROR
                //TODO SHOW ERROR
                //TODO SHOW ERROR
                logger.error("Some of the uploaded nodes could not be properly linked");
                //TODO SHOW ERROR
                //TODO SHOW ERROR
                //TODO SHOW ERROR
                //TODO SHOW ERROR
                //TODO SHOW ERROR
            }
        }
    }
    
}
