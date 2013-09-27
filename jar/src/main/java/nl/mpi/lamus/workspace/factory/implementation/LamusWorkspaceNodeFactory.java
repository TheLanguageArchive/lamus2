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

import java.net.URI;
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
     * @see WorkspaceNodeFactory#getNewWorkspaceNode(int, java.net.URI, java.net.URL)
     */
    @Override
    public WorkspaceNode getNewWorkspaceNode(int workspaceID, URI archiveNodeURI, URL archiveNodeURL) {
        
        WorkspaceNode node = new LamusWorkspaceNode(workspaceID, archiveNodeURI, archiveNodeURL);
        //TODO add other values as well

        return node;
    }

    
    //TODO CREATING NEW NODES WITH ALREADY THE ARCHIVE NODE URI???
    
    
    /**
     * @see WorkspaceNodeFactory#getNewWorkspaceMetadataNode(int, java.net.URI, java.net.URL, nl.mpi.metadata.api.model.MetadataDocument)
     */
    @Override
    public WorkspaceNode getNewWorkspaceMetadataNode(int workspaceID, URI archiveNodeURI, URL archiveNodeURL, MetadataDocument document) {
        
        WorkspaceNode node = new LamusWorkspaceNode(workspaceID, archiveNodeURI, archiveNodeURL);
        node.setName(document.getDisplayValue());
        node.setTitle(document.getDisplayValue());
        node.setType(WorkspaceNodeType.METADATA); //TODO it's metadata, so it should be CMDI? otherwise, should I get it based on what? What are the possible node types?
        node.setFormat(""); //TODO get this based on what? typechecker?
        node.setProfileSchemaURI(document.getDocumentType().getSchemaLocation());

        node.setStatus(WorkspaceNodeStatus.NODE_ISCOPY);
        
        return node;
    }
    
    /**
     * @see WorkspaceNodeFactory#getNewWorkspaceResourceNode(int, java.net.URI, java.net.URL, nl.mpi.metadata.api.model.Reference,
     *      nl.mpi.lamus.workspace.model.WorkspaceNodeType, java.lang.String, java.lang.String)
     */
    @Override
    public WorkspaceNode getNewWorkspaceResourceNode(int workspaceID, URI archiveNodeURI, URL archiveNodeURL,
            Reference resourceReference, WorkspaceNodeType type, String mimetype, String name) {
        
        WorkspaceNode node = new LamusWorkspaceNode(workspaceID, archiveNodeURI, archiveNodeURL);
        node.setName(name);
        node.setTitle("(type=" + mimetype + ")"); //TODO CHANGE THIS
        node.setType(type);
        node.setFormat(mimetype);
        
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
