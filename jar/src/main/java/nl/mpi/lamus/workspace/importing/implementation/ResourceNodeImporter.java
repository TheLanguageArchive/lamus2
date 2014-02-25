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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.typechecking.TypecheckedResults;
import nl.mpi.lamus.exception.WorkspaceImportException;
import nl.mpi.lamus.exception.TypeCheckerException;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeLinkFactory;
import nl.mpi.lamus.workspace.importing.NodeDataRetriever;
import nl.mpi.lamus.workspace.importing.NodeImporter;
import nl.mpi.lamus.workspace.model.*;
import nl.mpi.metadata.api.model.HandleCarrier;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import nl.mpi.metadata.api.model.ResourceReference;
import nl.mpi.util.OurURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Node importer specific for resource files.
 * @see NodeImporter
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class ResourceNodeImporter implements NodeImporter<ResourceReference> {

    private static final Logger logger = LoggerFactory.getLogger(ResourceNodeImporter.class);
    
    private final CorpusStructureProvider corpusStructureProvider;
    private final NodeResolver nodeResolver;
    private final WorkspaceDao workspaceDao;
    
    private final NodeDataRetriever nodeDataRetriever;
    
    private final WorkspaceNodeFactory workspaceNodeFactory;
    private final WorkspaceNodeLinkFactory workspaceNodeLinkFactory;
    
    private Workspace workspace = null;
    
    @Autowired
    public ResourceNodeImporter(CorpusStructureProvider csProvider,
            NodeResolver nodeResolver, WorkspaceDao wsDao,
            NodeDataRetriever nodeDataRetriever , WorkspaceNodeFactory wsNodeFactory,
            WorkspaceNodeLinkFactory wsNodeLinkFactory) {
        this.corpusStructureProvider = csProvider;
        this.nodeResolver = nodeResolver;
        this.workspaceDao = wsDao;
        this.workspaceNodeFactory = wsNodeFactory;
        this.workspaceNodeLinkFactory = wsNodeLinkFactory;
        this.nodeDataRetriever = nodeDataRetriever;
    }
    
    
    /**
     * @see NodeImporter#importNode(
     *      nl.mpi.lamus.workspace.model.Workspace, nl.mpi.lamus.workspace.model.WorkspaceNode,
     *      nl.mpi.metadata.api.model.ReferencingMetadataDocument, nl.mpi.metadata.api.model.Reference)
     */
    @Override
    public void importNode(Workspace ws, WorkspaceNode parentNode, ReferencingMetadataDocument parentDocument,
            Reference childLink) throws WorkspaceImportException {
        
        workspace = ws;
        
        if(workspace == null) {
            String errorMessage = "ResourceNodeImporter.importNode: workspace not set";
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
   
        URI childURI = null;
        if(childLink instanceof HandleCarrier) {
            childURI = ((HandleCarrier) childLink).getHandle();
        }
        
        if(childURI == null) {
            childURI = childLink.getURI();
        }
        
        CorpusNode childCorpusNode = null;
        URL childURL = null;
        OurURL childOurURL = null;
        try {
            
            childCorpusNode = corpusStructureProvider.getNode(childURI);
            if(childCorpusNode == null) {
                String errorMessage = "ResourceNodeImporter.importNode: error getting node " + childURI;
                logger.error(errorMessage);
                throw new WorkspaceImportException(errorMessage, workspace.getWorkspaceID(), null);
            }
            
            childURL = nodeResolver.getUrl(childCorpusNode);
            if(childURL == null) {
                String errorMessage = "ResourceNodeImporter.importNode: error getting URL for link " + childURI;
                logger.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
            childOurURL = new OurURL(childURL);
            
        } catch (MalformedURLException muex) {
            String errorMessage = "ResourceNodeImporter.importNode: error getting URL for link " + childURI;
            logger.error(errorMessage, muex);
            throw new WorkspaceImportException(errorMessage, workspace.getWorkspaceID(), muex);
        }

        String childMimetype = childLink.getMimetype();

        if(nodeDataRetriever.shouldResourceBeTypechecked(childLink, childOurURL)) {
            
            TypecheckedResults typecheckedResults = null;    
            try {

                typecheckedResults = nodeDataRetriever.triggerResourceFileCheck(childOurURL);

            } catch(TypeCheckerException tcex) {
                String errorMessage = "ResourceNodeImporter.importNode: error during type checking";
                logger.error(errorMessage, tcex);
                throw new WorkspaceImportException(errorMessage, workspace.getWorkspaceID(), tcex);
            }
            
            nodeDataRetriever.verifyTypecheckedResults(childOurURL, childLink, typecheckedResults);
            
//            childType = typecheckedResults.getCheckedNodeType();
            childMimetype = typecheckedResults.getCheckedMimetype();
        }
        //TODO needsProtection?

        WorkspaceNode childNode = workspaceNodeFactory.getNewWorkspaceResourceNode(
                workspace.getWorkspaceID(), childURI, childURL, childLink, childMimetype, childCorpusNode.getName(), childCorpusNode.isOnSite());
        workspaceDao.addWorkspaceNode(childNode);
        
        WorkspaceNodeLink nodeLink = workspaceNodeLinkFactory.getNewWorkspaceNodeLink(
                parentNode.getWorkspaceNodeID(), childNode.getWorkspaceNodeID(), childURI);
        workspaceDao.addWorkspaceNodeLink(nodeLink);
        
        //TODO DO NOT copy file to workspace folder... it will only exist as a link in the DB
            // and any changes made to it will be done during workspace submission
        
    }
}
