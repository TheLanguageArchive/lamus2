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
package nl.mpi.lamus.workspace.factory.implementation;

import java.net.URL;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.WorkspacePidValue;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import nl.mpi.metadata.api.model.HandleCarrier;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.api.model.Reference;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see WorkspaceNodeFactory
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class LamusWorkspaceNodeFactory implements WorkspaceNodeFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusWorkspaceNodeFactory.class);
    
    private final ArchiveFileHelper archiveFileHelper;
    
    @Autowired
    public LamusWorkspaceNodeFactory(ArchiveFileHelper archiveFileHelper) {
        this.archiveFileHelper = archiveFileHelper;
    }

    /**
     * @see WorkspaceNodeFactory#getNewWorkspaceNode(int, int, java.net.URL)
     */
    @Override
    public WorkspaceNode getNewWorkspaceNode(int workspaceID, int archiveNodeID, URL archiveNodeURL) {
        
        WorkspaceNode node = new LamusWorkspaceNode(workspaceID, archiveNodeID, archiveNodeURL, archiveNodeURL);
        //TODO add other values as well

        return node;
    }

    /**
     * @see WorkspaceNodeFactory#getNewWorkspaceMetadataNode(int, int, java.net.URL, java.lang.String, nl.mpi.metadata.api.model.MetadataDocument)
     */
    @Override
    public WorkspaceNode getNewWorkspaceMetadataNode(int workspaceID, int archiveNodeID, URL archiveNodeURL, String archiveNodePID, MetadataDocument document) {
        
        WorkspaceNode node = new LamusWorkspaceNode(workspaceID, archiveNodeID, archiveNodeURL, archiveNodeURL);
//                document.getFileLocation().toURL(), document.getFileLocation().toURL());
        node.setName(document.getDisplayValue());
        node.setTitle(document.getDisplayValue());
        node.setType(WorkspaceNodeType.METADATA); //TODO it's metadata, so it should be CMDI? otherwise, should I get it based on what? What are the possible node types?
        node.setFormat(""); //TODO get this based on what? typechecker?
        node.setProfileSchemaURI(document.getDocumentType().getSchemaLocation());

        String nodePID = archiveNodePID;
//        String nodePid = WorkspacePidValue.NONE.toString();
        //TODO Generate a new Handle at this point

	if (document instanceof HandleCarrier) {
	    nodePID = ((HandleCarrier) document).getHandle();
	} else {
            
            //TODO can't assume that the document always has a handle
            
	    logger.warn("Metadata document '" + document.getFileLocation().toString() + "' does not contain a handle.");
	}
        
        node.setPid(nodePID);
        node.setStatus(WorkspaceNodeStatus.NODE_ISCOPY);
        
        return node;
    }
    
    /**
     * @see WorkspaceNodeFactory#getNewWorkspaceResourceNode(int, int, java.net.URL, nl.mpi.metadata.api.model.Reference, nl.mpi.lamus.workspace.model.WorkspaceNodeType, java.lang.String)
     */
    @Override
    public WorkspaceNode getNewWorkspaceResourceNode(int workspaceID, int archiveNodeID, URL url,
            Reference resourceReference, WorkspaceNodeType type, String mimetype) {
        
        String name = FilenameUtils.getName(url.getPath());
        
        WorkspaceNode node = new LamusWorkspaceNode(workspaceID, archiveNodeID,
                url, url);
        node.setName(name);
        node.setTitle("(type=" + mimetype + ")"); //TODO CHANGE THIS
        node.setType(type);
        node.setFormat(mimetype);
        
        String nodePid = WorkspacePidValue.NONE.toString();
        //TODO Generate a new Handle at this point?
        
        if(resourceReference instanceof HandleCarrier) {
            nodePid = ((HandleCarrier) resourceReference).getHandle();
        } else {
            logger.warn("Resource reference '" + url.toString() + "' does not contain a handle.");
        }
        node.setPid(nodePid);
        
        //ALWAYS?
        node.setStatus(WorkspaceNodeStatus.NODE_VIRTUAL);
        
        
        return node;
    }

    /**
     * @see WorkspaceNodeFactory#getNewWorkspaceNodeFromFile(int, java.net.URL, java.net.URL,
     *      nl.mpi.lamus.workspace.model.WorkspaceNodeType, java.lang.String, nl.mpi.lamus.workspace.model.WorkspaceNodeStatus)
     */
    @Override
    public WorkspaceNode getNewWorkspaceNodeFromFile(int workspaceID, URL originURL, URL workspaceURL,
        WorkspaceNodeType type, String mimetype, WorkspaceNodeStatus status) {
        
        WorkspaceNode node = new LamusWorkspaceNode();
        node.setWorkspaceID(workspaceID);
        String displayValue = FilenameUtils.getName(FilenameUtils.getName(workspaceURL.getPath()));
        node.setName(displayValue);
        node.setTitle(displayValue);
        node.setOriginURL(originURL);
        node.setWorkspaceURL(workspaceURL);
        node.setType(type);
        node.setFormat(mimetype);
        node.setStatus(status);
        
        return node;
    }
}
