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

import nl.mpi.lamus.workspace.importing.implementation.ImportProblem;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.xml.transform.TransformerException;
import nl.mpi.lamus.metadata.MetadataApiBridge;
import nl.mpi.lamus.workspace.model.NodeUtil;
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
    
    private final MetadataAPI metadataAPI;
    private final MetadataApiBridge metadataApiBridge;
    private final WorkspaceUploadReferenceHandler workspaceUploadReferenceHandler;
    private final NodeUtil nodeUtil;
    
    @Autowired
    public LamusWorkspaceUploadHelper(MetadataAPI mdAPI, MetadataApiBridge mdApiBridge,
            WorkspaceUploadReferenceHandler wsUploadReferenceHandler, NodeUtil nodeUtil) {
        this.metadataAPI = mdAPI;
        this.metadataApiBridge = mdApiBridge;
        this.workspaceUploadReferenceHandler = wsUploadReferenceHandler;
        this.nodeUtil = nodeUtil;
    }

    /**
     * @see WorkspaceUploadHelper#assureLinksInWorkspace(int, java.util.Collection)
     */
    @Override
    public Collection<ImportProblem> assureLinksInWorkspace(int workspaceID, Collection<WorkspaceNode> nodesToCheck) {
        
        Collection<ImportProblem> allFailedLinks = new ArrayList<>();
        Map<MetadataDocument, WorkspaceNode> documentsWithInvalidSelfHandles = new HashMap<>();
        
        for(WorkspaceNode node : nodesToCheck) {
            
            if(!nodeUtil.isNodeMetadata(node)) {
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
            
            Collection<ImportProblem> failedLinks =
                    workspaceUploadReferenceHandler.matchReferencesWithNodes(
                    workspaceID, nodesToCheck, node, referencingDocument, documentsWithInvalidSelfHandles);
            
            allFailedLinks.addAll(failedLinks);
        }
        
        //remove external self-handles, if any
        Set<Map.Entry<MetadataDocument, WorkspaceNode>> entries = documentsWithInvalidSelfHandles.entrySet();
        if(!entries.isEmpty()) {
            for(Map.Entry<MetadataDocument, WorkspaceNode> entry : entries) {
                try {
                    metadataApiBridge.removeSelfHandleAndSaveDocument(entry.getKey(), entry.getValue().getWorkspaceURL());
                } catch (IOException | TransformerException | MetadataException ex) {
                    logger.error("Invalid self-handle could not be removed from document " + entry.getValue().getWorkspaceURL(), ex);
                    continue;
                }
            }
        }
        
        return allFailedLinks;
    }
    
}
