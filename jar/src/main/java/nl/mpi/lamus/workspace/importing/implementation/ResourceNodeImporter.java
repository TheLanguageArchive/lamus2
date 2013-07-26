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
import nl.mpi.corpusstructure.ArchiveAccessContext;
import nl.mpi.corpusstructure.ArchiveObjectsDB;
import nl.mpi.corpusstructure.NodeIdUtils;
import nl.mpi.corpusstructure.UnknownNodeException;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.typechecking.FileTypeHandler;
import nl.mpi.lamus.typechecking.TypecheckedResults;
import nl.mpi.lamus.workspace.exception.NodeImporterException;
import nl.mpi.lamus.workspace.exception.TypeCheckerException;
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
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Node importer specific for resource files.
 * @see NodeImporter
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class ResourceNodeImporter implements NodeImporter<ResourceReference> {

    private static final Logger logger = LoggerFactory.getLogger(ResourceNodeImporter.class);
    
    private final ArchiveObjectsDB archiveObjectsDB;
    private final WorkspaceDao workspaceDao;
    
    private final NodeDataRetriever nodeDataRetriever;
    
    private final ArchiveFileHelper archiveFileHelper;
    private final FileTypeHandler fileTypeHandler;
    private final WorkspaceNodeFactory workspaceNodeFactory;
    private final WorkspaceParentNodeReferenceFactory workspaceParentNodeReferenceFactory;
    private final WorkspaceNodeLinkFactory workspaceNodeLinkFactory;
    
    private int workspaceID = -1;
    
    @Autowired
    public ResourceNodeImporter(@Qualifier("ArchiveObjectsDB") ArchiveObjectsDB aoDB, WorkspaceDao wsDao,
            NodeDataRetriever nodeDataRetriever ,ArchiveFileHelper archiveFileHelper, FileTypeHandler fileTypeHandler,
            WorkspaceNodeFactory wsNodeFactory, WorkspaceParentNodeReferenceFactory wsParentNodeReferenceFactory,
            WorkspaceNodeLinkFactory wsNodeLinkFactory) {
        this.archiveObjectsDB = aoDB;
        this.workspaceDao = wsDao;
        this.archiveFileHelper = archiveFileHelper;
        this.fileTypeHandler = fileTypeHandler;
        this.workspaceNodeFactory = wsNodeFactory;
        this.workspaceParentNodeReferenceFactory = wsParentNodeReferenceFactory;
        this.workspaceNodeLinkFactory = wsNodeLinkFactory;
        this.nodeDataRetriever = nodeDataRetriever;
    }
    
    
    /**
     * @see NodeImporter#importNode(nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.metadata.api.model.ReferencingMetadataDocument, nl.mpi.metadata.api.model.Reference, int)
     */
    @Override
    public void importNode(int wsID, WorkspaceNode parentNode, ReferencingMetadataDocument parentDocument,
            Reference childLink, int childNodeArchiveID) throws NodeImporterException {
        
        workspaceID = wsID;
        
        if(workspaceID < 0) {
            String errorMessage = "ResourceNodeImporter.importNode: workspace not set";
            logger.error(errorMessage);
            throw new NodeImporterException(errorMessage, workspaceID, this.getClass(), null);
        }
   
        URI childURI = childLink.getURI();
        OurURL childURL = null;
        try {
            childURL = nodeDataRetriever.getResourceURL(childLink);
            if(childURL == null) {
                String errorMessage = "Error getting URL for link " + childLink.getURI();
                logger.error(errorMessage);
                throw new NodeImporterException(errorMessage, workspaceID, this.getClass(), null);
            }
        } catch (MalformedURLException muex) {
            String errorMessage = "Error getting URL for link " + childLink.getURI();
            logger.error(errorMessage, muex);
            throw new NodeImporterException(errorMessage, workspaceID, this.getClass(), muex);
        } catch (UnknownNodeException unex) {
	    String errorMessage = "Error getting object URL for node ID " + childNodeArchiveID;
	    logger.error(errorMessage, unex);
	    throw new NodeImporterException(errorMessage, workspaceID, this.getClass(), unex);
        }

        
        //TODO change this call - reuse/refactor the new method in ArchiveFileHelper instead
        OurURL childURLWithContext = this.archiveObjectsDB.getObjectURL(NodeIdUtils.TONODEID(childNodeArchiveID), ArchiveAccessContext.getFileUrlContext());

        WorkspaceNodeType childType = WorkspaceNodeType.UNKNOWN; //TODO What to use here? Is this field supposed to exist?
        String childMimetype = childLink.getMimetype();

        if(nodeDataRetriever.shouldResourceBeTypechecked(childLink, childURLWithContext, childNodeArchiveID)) {
            
            TypecheckedResults typecheckedResults = null;    
            try {

                // REALLY NECESSARY TO CALL A SEPARATE CLASS FOR THIS?
                typecheckedResults = nodeDataRetriever.getResourceFileChecked(childNodeArchiveID, childLink, childURL, childURLWithContext);

            } catch(TypeCheckerException tcex) {
                String errorMessage = "ResourceNodeImporter.importNode: error during type checking";
                logger.error(errorMessage, tcex);
                throw new NodeImporterException(errorMessage, workspaceID, this.getClass(), tcex);
            }
            
            nodeDataRetriever.verifyTypecheckedResults(childURL, childLink, typecheckedResults);
            
            childType = typecheckedResults.getCheckedNodeType();
            childMimetype = typecheckedResults.getCheckedMimetype();
        }
        //TODO needsProtection?

        //TODO create node accordingly and add it to the database
        WorkspaceNode childNode = workspaceNodeFactory.getNewWorkspaceResourceNode(
                workspaceID, childNodeArchiveID, childURLWithContext.toURL(), childLink, childType, childMimetype);
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
