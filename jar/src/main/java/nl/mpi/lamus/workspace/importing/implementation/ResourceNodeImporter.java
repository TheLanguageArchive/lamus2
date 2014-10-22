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
    private final MetadataApiBridge metadataApiBridge;
    private final NodeDataRetriever nodeDataRetriever;
    
    private final WorkspaceNodeFactory workspaceNodeFactory;
    private final WorkspaceNodeLinkFactory workspaceNodeLinkFactory;
    
    private Workspace workspace = null;
    
    @Autowired
    public ResourceNodeImporter(CorpusStructureProvider csProvider,
            NodeResolver nodeResolver, WorkspaceDao wsDao, MetadataApiBridge mdApiBridge,
            NodeDataRetriever nodeDataRetriever , WorkspaceNodeFactory wsNodeFactory,
            WorkspaceNodeLinkFactory wsNodeLinkFactory) {
        this.corpusStructureProvider = csProvider;
        this.nodeResolver = nodeResolver;
        this.workspaceDao = wsDao;
        this.metadataApiBridge = mdApiBridge;
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
            Reference referenceFromParent) throws WorkspaceImportException {
        
        workspace = ws;
        
        if(workspace == null) {
            String errorMessage = "ResourceNodeImporter.importNode: workspace not set";
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
   
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
            throw new WorkspaceImportException(errorMessage, workspace.getWorkspaceID(), null);
        }

        URL childURL = nodeResolver.getUrl(childCorpusNode);
        if(childURL == null) {
            String errorMessage = "ResourceNodeImporter.importNode: error getting URL for link " + childURI;
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        File childFile = nodeResolver.getLocalFile(childCorpusNode);

        String childMimetype = referenceFromParent.getMimetype();

        if(nodeDataRetriever.shouldResourceBeTypechecked(referenceFromParent, childFile)) {
            
            TypecheckedResults typecheckedResults = null;    
            InputStream childInputStream = null;
            try {

                childInputStream = nodeResolver.getInputStream(childCorpusNode);
                
                typecheckedResults = nodeDataRetriever.triggerResourceFileCheck(childInputStream, childFile.getName());
            } catch(TypeCheckerException tcex) {
                String errorMessage = "ResourceNodeImporter.importNode: error during type checking";
                logger.error(errorMessage, tcex);
                throw new WorkspaceImportException(errorMessage, workspace.getWorkspaceID(), tcex);
            } catch (IOException ex) {
                throw new UnsupportedOperationException("not handled yet");
            } finally {
                IOUtils.closeQuietly(childInputStream);
            }
            
            nodeDataRetriever.verifyTypecheckedResults(childFile, referenceFromParent, typecheckedResults);
            childMimetype = typecheckedResults.getCheckedMimetype();
        }
        //TODO needsProtection?

        WorkspaceNode childNode = workspaceNodeFactory.getNewWorkspaceResourceNode(
                workspace.getWorkspaceID(), childURI, childURL, referenceFromParent, childMimetype, childCorpusNode.getName(), childCorpusNode.isOnSite());
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
		    + " in workspace " + workspace.getWorkspaceID();
	    throwWorkspaceImportException(errorMessage, ioex);
        }
    }
    
    private void throwWorkspaceImportException(String errorMessage, Exception cause) throws WorkspaceImportException {
        logger.error(errorMessage, cause);
        throw new WorkspaceImportException(errorMessage, workspace.getWorkspaceID(), cause);
    }
}
