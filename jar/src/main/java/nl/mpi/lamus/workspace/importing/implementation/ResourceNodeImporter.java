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
import nl.mpi.archiving.corpusstructure.core.UnknownNodeException;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.typechecking.FileTypeHandler;
import nl.mpi.lamus.typechecking.TypecheckedResults;
import nl.mpi.lamus.exception.WorkspaceImportException;
import nl.mpi.lamus.exception.TypeCheckerException;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeLinkFactory;
import nl.mpi.lamus.workspace.factory.WorkspaceParentNodeReferenceFactory;
import nl.mpi.lamus.workspace.importing.NodeDataRetriever;
import nl.mpi.lamus.workspace.importing.NodeImporter;
import nl.mpi.lamus.workspace.model.*;
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
    
    private final ArchiveFileHelper archiveFileHelper;
    private final FileTypeHandler fileTypeHandler;
    private final WorkspaceNodeFactory workspaceNodeFactory;
    private final WorkspaceParentNodeReferenceFactory workspaceParentNodeReferenceFactory;
    private final WorkspaceNodeLinkFactory workspaceNodeLinkFactory;
    
    private Workspace workspace = null;
    
    @Autowired
    public ResourceNodeImporter(CorpusStructureProvider csProvider,
            NodeResolver nodeResolver, WorkspaceDao wsDao,
            NodeDataRetriever nodeDataRetriever ,ArchiveFileHelper archiveFileHelper, FileTypeHandler fileTypeHandler,
            WorkspaceNodeFactory wsNodeFactory, WorkspaceParentNodeReferenceFactory wsParentNodeReferenceFactory,
            WorkspaceNodeLinkFactory wsNodeLinkFactory) {
        this.corpusStructureProvider = csProvider;
        this.nodeResolver = nodeResolver;
        this.workspaceDao = wsDao;
        this.archiveFileHelper = archiveFileHelper;
        this.fileTypeHandler = fileTypeHandler;
        this.workspaceNodeFactory = wsNodeFactory;
        this.workspaceParentNodeReferenceFactory = wsParentNodeReferenceFactory;
        this.workspaceNodeLinkFactory = wsNodeLinkFactory;
        this.nodeDataRetriever = nodeDataRetriever;
    }
    
    
    /**
     * @see NodeImporter#importNode(
     *      nl.mpi.lamus.workspace.model.Workspace, nl.mpi.lamus.workspace.model.WorkspaceNode,
     *      nl.mpi.metadata.api.model.ReferencingMetadataDocument, nl.mpi.metadata.api.model.Reference, java.net.URI)
     */
    @Override
    public void importNode(Workspace ws, WorkspaceNode parentNode, ReferencingMetadataDocument parentDocument,
            Reference childLink, URI childNodeArchiveURI) throws WorkspaceImportException {
        
        workspace = ws;
        
        if(workspace == null) {
            String errorMessage = "ResourceNodeImporter.importNode: workspace not set";
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
   
        URI childURI = childLink.getURI();
        CorpusNode childCorpusNode = null;
        URL childURL = null;
        OurURL childOurURL = null;
        try {
            
            childCorpusNode = corpusStructureProvider.getNode(childURI);
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
        } catch (UnknownNodeException unex) {
	    String errorMessage = "ResourceNodeImporter.importNode: error getting object URL for node " + childURI;
	    logger.error(errorMessage, unex);
	    throw new WorkspaceImportException(errorMessage, workspace.getWorkspaceID(), unex);
        }


        WorkspaceNodeType childType = WorkspaceNodeType.UNKNOWN; //TODO What to use here? Is this field supposed to exist?
        String childMimetype = childLink.getMimetype();

        if(nodeDataRetriever.shouldResourceBeTypechecked(childLink, childOurURL)) {
            
            TypecheckedResults typecheckedResults = null;    
            try {

                // REALLY NECESSARY TO CALL A SEPARATE CLASS FOR THIS?
                typecheckedResults = nodeDataRetriever.triggerResourceFileCheck(childOurURL);

            } catch(TypeCheckerException tcex) {
                String errorMessage = "ResourceNodeImporter.importNode: error during type checking";
                logger.error(errorMessage, tcex);
                throw new WorkspaceImportException(errorMessage, workspace.getWorkspaceID(), tcex);
            }
            
            nodeDataRetriever.verifyTypecheckedResults(childOurURL, childLink, typecheckedResults);
            
            childType = typecheckedResults.getCheckedNodeType();
            childMimetype = typecheckedResults.getCheckedMimetype();
        }
        //TODO needsProtection?

        //TODO create node accordingly and add it to the database
        WorkspaceNode childNode = workspaceNodeFactory.getNewWorkspaceResourceNode(
                workspace.getWorkspaceID(), childURI, childURL, childLink, childType, childMimetype, childCorpusNode.getName());
        workspaceDao.addWorkspaceNode(childNode);
        
        //TODO add parent link in the database
        WorkspaceParentNodeReference parentNodeReference = 
                workspaceParentNodeReferenceFactory.getNewWorkspaceParentNodeReference(parentNode, childLink);
        
        WorkspaceNodeLink nodeLink = workspaceNodeLinkFactory.getNewWorkspaceNodeLink(
                parentNodeReference.getParentWorkspaceNodeID(), childNode.getWorkspaceNodeID(), childURI);
        workspaceDao.addWorkspaceNodeLink(nodeLink);
        
        //TODO DO NOT copy file to workspace folder... it will only exist as a link in the DB
            // and any changes made to it will be done during workspace submission
        
        //TODO something
    }
}
