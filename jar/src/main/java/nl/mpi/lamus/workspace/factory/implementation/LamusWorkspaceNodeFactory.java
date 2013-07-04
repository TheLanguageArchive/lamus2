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

import java.net.MalformedURLException;
import java.net.URL;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.WorkspacePidValue;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import nl.mpi.metadata.api.model.HandleCarrier;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.api.model.Reference;
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

    @Override
    public WorkspaceNode getNewWorkspaceMetadataNode(int workspaceID, int archiveNodeID, MetadataDocument document)
            throws MalformedURLException {
        
        WorkspaceNode node = new LamusWorkspaceNode(workspaceID, archiveNodeID,
                document.getFileLocation().toURL(), document.getFileLocation().toURL());
        node.setName(document.getDisplayValue());
        node.setTitle(document.getDisplayValue());
        node.setType(WorkspaceNodeType.METADATA); //TODO it's metadata, so it should be CMDI? otherwise, should I get it based on what? What are the possible node types?
        node.setFormat(""); //TODO get this based on what? typechecker?
        node.setProfileSchemaURI(document.getDocumentType().getSchemaLocation());

        String nodePid = WorkspacePidValue.NONE.toString();
        //TODO Generate a new Handle at this point

	if (document instanceof HandleCarrier) {
	    nodePid = ((HandleCarrier) document).getHandle();
	} else {
	    logger.warn("Metadata document '" + document.getFileLocation().toString() + "' does not contain a handle.");
	}
        
        node.setPid(nodePid);
        node.setStatus(WorkspaceNodeStatus.NODE_ISCOPY);
        
        return node;
    }
    
    @Override
    public WorkspaceNode getNewWorkspaceResourceNode(int workspaceID, int archiveNodeID, URL url,
            Reference resourceReference, WorkspaceNodeType type, String mimetype) {
        
        String name = this.archiveFileHelper.getFileTitle(url.toString());
        
        WorkspaceNode node = new LamusWorkspaceNode(workspaceID, archiveNodeID,
                url, url);
        node.setName(name);
        node.setTitle("(type=" + mimetype + ")");
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
}
