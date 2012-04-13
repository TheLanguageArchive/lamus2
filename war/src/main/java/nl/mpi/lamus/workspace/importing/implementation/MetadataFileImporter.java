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

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import nl.mpi.corpusstructure.ArchiveAccessContext;
import nl.mpi.corpusstructure.ArchiveObjectsDB;
import nl.mpi.corpusstructure.NodeIdUtils;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.workspace.Workspace;
import nl.mpi.lamus.workspace.WorkspaceNode;
import nl.mpi.lamus.workspace.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
import nl.mpi.lamus.workspace.importing.FileImporter;
import nl.mpi.lamus.workspace.importing.WorkspaceFileExplorer;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.api.model.MetadataReference;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import nl.mpi.util.OurURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class MetadataFileImporter implements FileImporter<MetadataReference> {
    
    private static final Logger logger = LoggerFactory.getLogger(MetadataFileImporter.class);
    
    private ArchiveObjectsDB archiveObjectsDB;
    private WorkspaceDao workspaceDao;
    private MetadataAPI metadataAPI;
    private Workspace workspace;
    private WorkspaceNodeFactory workspaceNodeFactory;
    private WorkspaceFileHandler workspaceFileHandler;
    private WorkspaceFileExplorer workspaceFileExplorer;

    public void importFile(MetadataReference reference, int nodeArchiveID) {
        
        
        //TODO if not onsite: create external node
        //TODO setURID
        //TODO if no access: create forbidden node
        //TODO if unknown node: create forbidden node
        //TODO if needsProtection: create external node
        
        
        
        
        
        
        
        //TODO get more values to add to the node (e.g. from the file, using the metadataAPI)
        OurURL tempUrl = archiveObjectsDB.getObjectURL(NodeIdUtils.TONODEID(nodeArchiveID), ArchiveAccessContext.getFileUrlContext());
        URL nodeArchiveURL = tempUrl.toURL();
        WorkspaceNode workspaceNode = workspaceNodeFactory.getNewWorkspaceNode(workspace.getWorkspaceID(), nodeArchiveID, nodeArchiveURL);

        
                
        
        
        //TODO Check if node is still not locked?
        
        MetadataDocument metadataDocument = null;
        try {
            metadataDocument = metadataAPI.getMetadataDocument(workspaceNode.getArchiveURL());
        } catch(IOException ioex) {
            logger.error("Error importing Metadata Document " + workspaceNode.getArchiveURL(), ioex);
        } catch(MetadataException mdex) {
            logger.error("Error importing Metadata Document " + workspaceNode.getArchiveURL(), mdex);
        }
        
        if(metadataDocument == null) {
            
            
            return;
            //TODO THROW EXCEPTION INSTEAD
            
            
        }
        
        

        //TODO add some more information to the node (name, etc)
//        metadataDocument.getChildElement(NAME???)
        String nodeName = ""; //TODO get name from metadata file
        workspaceNode.setName(nodeName);
        String nodeType = metadataDocument.getType().getName(); //TODO it's metadata, so it should be CMDI? otherwise, should I get it based on what?
        workspaceNode.setType(nodeType);
        String nodeFormat = ""; //TODO get this based on what? typechecker?
        workspaceNode.setFormat(nodeFormat);
        URI profileSchemaURI = null; //TODO how to get this information?
        workspaceNode.setProfileSchemaURI(profileSchemaURI);
        String nodePid = ""; //TODO how to get the self-handle from CMDI?
        workspaceNode.setPid(nodePid);
        workspaceNode.setStatus(WorkspaceNodeStatus.NODE_ISCOPY);
        //TODO insert node in DB
        workspaceDao.addWorkspaceNode(workspaceNode);
        
        //TODO set top node ID in workspace (if reference is null), set workspace status / Save workspace
        if(reference == null) { //TODO find a better way of indicating this
            workspace.setTopNodeID(workspaceNode.getWorkspaceNodeID());
            workspaceDao.updateWorkspaceTopNode(workspace);
        }
        workspace.setStatusMessageInitialising();
        workspaceDao.updateWorkspaceStatusMessage(workspace);
        
        
        
        //TODO add information about parent link
        
        

        workspaceFileHandler.copyMetadataFileToWorkspace(workspace, workspaceNode, metadataAPI, metadataDocument);
        

        
        
        
        //TODO get metadata file links (references)
        if(metadataDocument instanceof ReferencingMetadataDocument) {
            Collection<Reference> links = ((ReferencingMetadataDocument) metadataDocument).getDocumentReferences();
//            exploreNodesBelow(workspaceTopNode, links);
            workspaceFileExplorer.explore(workspaceNode, links);
        }

        
        
        
        
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
}
