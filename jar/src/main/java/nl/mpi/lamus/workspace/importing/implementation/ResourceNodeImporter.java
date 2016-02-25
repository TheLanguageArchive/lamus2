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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import javax.xml.transform.TransformerException;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.handle.util.HandleParser;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.typechecking.TypecheckedResults;
import nl.mpi.lamus.exception.WorkspaceImportException;
import nl.mpi.lamus.exception.TypeCheckerException;
import nl.mpi.lamus.metadata.MetadataApiBridge;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeLinkFactory;
import nl.mpi.lamus.workspace.importing.NodeDataRetriever;
import nl.mpi.lamus.workspace.importing.NodeImporter;
import nl.mpi.lamus.workspace.model.*;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.HandleCarrier;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import nl.mpi.metadata.api.model.ResourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Node importer specific for resource files.
 * @see NodeImporter
 * 
 * @author guisil
 */
@Component
public class ResourceNodeImporter implements NodeImporter<ResourceReference> {

    private static final Logger logger = LoggerFactory.getLogger(ResourceNodeImporter.class);
    
    @Autowired
    private CorpusStructureProvider corpusStructureProvider;
    @Autowired
    private NodeResolver nodeResolver;
    @Autowired
    private WorkspaceDao workspaceDao;
    @Autowired
    private MetadataApiBridge metadataApiBridge;
    @Autowired
    private NodeDataRetriever nodeDataRetriever;
    @Autowired
    private WorkspaceNodeFactory workspaceNodeFactory;
    @Autowired
    private WorkspaceNodeLinkFactory workspaceNodeLinkFactory;
    @Autowired
    private NodeUtil nodeUtil;
    @Autowired
    private HandleParser handleParser;

    
    /**
     * @see NodeImporter#importNode(
     *      nl.mpi.lamus.workspace.model.Workspace, nl.mpi.lamus.workspace.model.WorkspaceNode,
     *      nl.mpi.metadata.api.model.ReferencingMetadataDocument, nl.mpi.metadata.api.model.Reference)
     */
    @Override
    public void importNode(Workspace workspace, WorkspaceNode parentNode, ReferencingMetadataDocument parentDocument,
            Reference referenceFromParent) throws WorkspaceImportException {
        
        if(workspace == null) {
            String errorMessage = "ResourceNodeImporter.importNode: workspace not set";
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        
        int workspaceID = workspace.getWorkspaceID();
   
        URI childURI = null;
        if(referenceFromParent instanceof HandleCarrier) {
            URI handleInFile = ((HandleCarrier) referenceFromParent).getHandle();
            if(handleInFile != null) {
                childURI = handleParser.prepareAndValidateHandleWithHdlPrefix(handleInFile);
                if(!handleInFile.equals(childURI)) {
                    try {
                        ((HandleCarrier) referenceFromParent).setHandle(childURI);
                    } catch (MetadataException | UnsupportedOperationException | IllegalArgumentException ex) {
                        logger.info("Couldn't update handle in parent reference. Current handle is: " + handleInFile);
                    }
                }
            }
        }
        
        if(childURI == null) {
            childURI = referenceFromParent.getURI();
        }
        
        logger.debug("Importing node into new workspace; workspaceID: " + workspaceID + "; nodeURI: " + childURI);
            
        CorpusNode childCorpusNode = corpusStructureProvider.getNode(childURI);
        if(childCorpusNode == null) {
            String errorMessage = "ResourceNodeImporter.importNode: error getting node " + childURI;
            logger.error(errorMessage);
            throw new WorkspaceImportException(errorMessage, workspaceID, null);
        }

        boolean childOnSite = childCorpusNode.isOnSite();
        File childLocalFile = null;
        URL childArchiveURL = null;
        
        try {
            if(childOnSite) {
                childLocalFile = nodeResolver.getLocalFile(childCorpusNode);
                if(childLocalFile == null) {
                    String errorMessage = "ResourceNodeImporter.importNode: error getting URL for link " + childURI;
                    logger.error(errorMessage);
                    throw new IllegalArgumentException(errorMessage);
                }
                childArchiveURL = childLocalFile.toURI().toURL();
            } else {
                    childArchiveURL = nodeResolver.getUrl(childCorpusNode);
            }
        } catch(MalformedURLException ex) {
            String errorMessage = "ResourceNodeImporter.importNode: error getting URL for link " + childURI;
            logger.error(errorMessage, ex);
            throw new IllegalArgumentException(errorMessage);
        }

        String childMimetype = referenceFromParent.getMimetype();

        if(nodeDataRetriever.shouldResourceBeTypechecked(referenceFromParent, childLocalFile, childCorpusNode)) {
            
            TypecheckedResults typecheckedResults = null;
            try {
                typecheckedResults = nodeDataRetriever.triggerResourceFileCheck(childArchiveURL, childLocalFile.getName());
            } catch(TypeCheckerException tcex) {
                String errorMessage = "ResourceNodeImporter.importNode: error during type checking";
                logger.error(errorMessage, tcex);
                throw new WorkspaceImportException(errorMessage, workspaceID, tcex);
            }
            
            nodeDataRetriever.verifyTypecheckedResults(childLocalFile, referenceFromParent, typecheckedResults);
            childMimetype = typecheckedResults.getCheckedMimetype();
        }
        
        WorkspaceNodeType childNodeType = nodeUtil.convertMimetype(childMimetype);
        boolean childToBeProtected = nodeDataRetriever.isNodeToBeProtected(childURI);
        
        if(metadataApiBridge.isReferenceAnInfoLink(parentDocument, referenceFromParent)) {
            childNodeType = WorkspaceNodeType.RESOURCE_INFO;
        }

        WorkspaceNode childNode = workspaceNodeFactory.getNewWorkspaceNode(
                workspaceID, childURI, childArchiveURL, referenceFromParent,
                childMimetype, childNodeType, childCorpusNode.getName(), childOnSite, childToBeProtected);
        workspaceDao.addWorkspaceNode(childNode);
        if(!childToBeProtected) {
            workspaceDao.lockNode(childURI, workspaceID);
        }
        
        WorkspaceNodeLink nodeLink = workspaceNodeLinkFactory.getNewWorkspaceNodeLink(
                parentNode.getWorkspaceNodeID(), childNode.getWorkspaceNodeID());
        workspaceDao.addWorkspaceNodeLink(nodeLink);
        
        referenceFromParent.setLocation(null);
        try {
            metadataApiBridge.saveMetadataDocument(parentDocument, parentNode.getWorkspaceURL());
        } catch (IOException | TransformerException | MetadataException ioex) {
            String errorMessage = "Failed to save file " + parentNode.getWorkspaceURL()
		    + " in workspace " + workspaceID;
	    throwWorkspaceImportException(workspaceID, errorMessage, ioex);
        }
    }
    
    
    private void throwWorkspaceImportException(int workspaceID, String errorMessage, Exception cause) throws WorkspaceImportException {
        logger.error(errorMessage, cause);
        throw new WorkspaceImportException(errorMessage, workspaceID, cause);
    }
}
