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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import javax.xml.transform.TransformerException;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
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
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Node importer specific for resource files.
 * @see NodeImporter
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
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
            childURI = ((HandleCarrier) referenceFromParent).getHandle();
        }
        
        if(childURI == null) {
            childURI = referenceFromParent.getURI();
        }
            
        CorpusNode childCorpusNode = corpusStructureProvider.getNode(childURI);
        if(childCorpusNode == null) {
            String errorMessage = "ResourceNodeImporter.importNode: error getting node " + childURI;
            logger.error(errorMessage);
            throw new WorkspaceImportException(errorMessage, workspaceID, null);
        }

        File childLocalFile = nodeResolver.getLocalFile(childCorpusNode);
        if(childLocalFile == null) {
            String errorMessage = "ResourceNodeImporter.importNode: error getting URL for link " + childURI;
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        URL childURL;
        try {
            childURL = childLocalFile.toURI().toURL();
        } catch (MalformedURLException ex) {
            throw new UnsupportedOperationException("exception not handled yet");
        }

        String childMimetype = referenceFromParent.getMimetype();

        if(nodeDataRetriever.shouldResourceBeTypechecked(referenceFromParent, childLocalFile, childCorpusNode)) {
            
            TypecheckedResults typecheckedResults = null;    
            InputStream childInputStream = null;
            try {

                childInputStream = nodeResolver.getInputStream(childCorpusNode);
                
                typecheckedResults = nodeDataRetriever.triggerResourceFileCheck(childInputStream, childLocalFile.getName());
            } catch(TypeCheckerException tcex) {
                String errorMessage = "ResourceNodeImporter.importNode: error during type checking";
                logger.error(errorMessage, tcex);
                throw new WorkspaceImportException(errorMessage, workspaceID, tcex);
            } catch (IOException ex) {
                throw new UnsupportedOperationException("not handled yet");
            } finally {
                IOUtils.closeQuietly(childInputStream);
            }
            
            nodeDataRetriever.verifyTypecheckedResults(childLocalFile, referenceFromParent, typecheckedResults);
            childMimetype = typecheckedResults.getCheckedMimetype();
        }
        
        boolean childToBeProtected = nodeDataRetriever.isNodeToBeProtected(childURI);

        WorkspaceNode childNode = workspaceNodeFactory.getNewWorkspaceResourceNode(
                workspaceID, childURI, childURL, referenceFromParent,
                childMimetype, childCorpusNode.getName(), childCorpusNode.isOnSite(), childToBeProtected);
        workspaceDao.addWorkspaceNode(childNode);
        
        WorkspaceNodeLink nodeLink = workspaceNodeLinkFactory.getNewWorkspaceNodeLink(
                parentNode.getWorkspaceNodeID(), childNode.getWorkspaceNodeID());
        workspaceDao.addWorkspaceNodeLink(nodeLink);
        
        //TODO DO NOT copy file to workspace folder... it will only exist as a link in the DB
            // and any changes made to it will be done during workspace submission
        
        
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
