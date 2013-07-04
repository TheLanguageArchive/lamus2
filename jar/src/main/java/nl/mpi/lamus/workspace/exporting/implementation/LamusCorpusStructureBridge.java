/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.workspace.exporting.implementation;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import nl.mpi.corpusstructure.AccessInfo;
import nl.mpi.corpusstructure.ArchiveAccessContext;
import nl.mpi.corpusstructure.ArchiveObjectsDBWrite;
import nl.mpi.corpusstructure.NodeIdUtils;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.util.DateTimeHelper;
import nl.mpi.lamus.workspace.exporting.CorpusStructureBridge;
import nl.mpi.util.Checksum;
import nl.mpi.util.OurURL;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class LamusCorpusStructureBridge implements CorpusStructureBridge {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusCorpusStructureBridge.class);
    
    private final ArchiveObjectsDBWrite archiveObjectsDBW;
    private final DateTimeHelper dateTimeHelper;
    private final ArchiveFileHelper archiveFileHelper;
    
    @Autowired
    public LamusCorpusStructureBridge(
            @Qualifier("ArchiveObjectsDB") ArchiveObjectsDBWrite aodbw,
            DateTimeHelper dtHelper, ArchiveFileHelper afHelper) {
        
        this.archiveObjectsDBW = aodbw;
        this.dateTimeHelper = dtHelper;
        this.archiveFileHelper = afHelper;
    }

    @Override
    public boolean updateArchiveObjectsNodeURL(int archiveNodeID, URL oldArchiveNodeURL, URL newArchiveNodeURL) {
        
        if(newArchiveNodeURL == null) { //TODO IllegalArgumentException?
            String errorMessage = "LamusArchiveObjectsBridge.updateArchiveObjectsNodeURL: new URL is null";
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        if(archiveNodeID < 0 || oldArchiveNodeURL == null) { //TODO Check both? Check PID?
            logger.info("LamusArchiveObjectsBridge.updateArchiveObjectsNodeURL: node didn't exist in the archive before; nothing to update");
            return true;
        }

        if(oldArchiveNodeURL.sameFile(newArchiveNodeURL)) {
            logger.info("LamusArchiveObjectsBridge.updateArchiveObjectsNodeURL: old and new URLs point to the same file; nothing to update");
            return true;
        }
        
        ArchiveAccessContext archiveAccessContext = archiveObjectsDBW.getArchiveRoots();
        
        URI tableContextURI;
        try {
            
            //TODO URLs pointing to orphans location?
            
            tableContextURI = archiveAccessContext.inTableContext(newArchiveNodeURL.toURI());
        } catch (URISyntaxException ex) {
            String errorMessage = "new URL is not a valid URI";
            logger.error(errorMessage, ex);
            throw new IllegalArgumentException("new URL is not a valid URI", ex);
        }
        
        //TODO throw UpdateInProgressException?
        return archiveObjectsDBW.moveArchiveObject(NodeIdUtils.TONODEID(archiveNodeID), tableContextURI);
    }

    /**
     * @see CorpusStructureBridge#addNewNodeToCorpusStructure(java.net.URL, java.lang.String)
     */
    @Override
    public int addNewNodeToCorpusStructure(URL nodeArchiveURL, AccessInfo accessRights, String pid) {
        
        URI inTableContextURI = null;
        try {
            inTableContextURI = archiveObjectsDBW.getArchiveRoots().inTableContext(nodeArchiveURL.toURI());
        } catch (URISyntaxException ex) {
            throw new UnsupportedOperationException("exception not handled yet", ex);
        }
        
        long currentTimeInMs = dateTimeHelper.getCurrentDateTime().getTime();
        Timestamp currentTimestamp = new Timestamp(currentTimeInMs);
        
        boolean onsite;
        try {
            onsite = archiveFileHelper.isUrlLocal(new OurURL(nodeArchiveURL));
        } catch (MalformedURLException ex) {
            throw new UnsupportedOperationException("exception not handled yet", ex);
        }
        
        long size = 1; //TODO check the real size?
        
        //TODO At this point the PID must have been already assigned
        
        String newNodeID = archiveObjectsDBW.newArchiveObject(inTableContextURI, pid, currentTimestamp, onsite, size, currentTimestamp, accessRights);
        
        return NodeIdUtils.TOINT(newNodeID);
    }

    @Override
    public boolean linkNodesInCorpusStructure(int parentNodeArchiveID, int childNodeArchiveID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AccessInfo getDefaultAccessInfoForUser(String userID) {
        
        List<String> users = new ArrayList<String>();
        users.add(userID);
        
        AccessInfo defaultAccessRights = AccessInfo.create(AccessInfo.NOBODY, AccessInfo.NOBODY, AccessInfo.ACCESS_LEVEL_NONE);
        defaultAccessRights.setReadUsers(users);
        defaultAccessRights.setWriteUsers(users);
        
        return defaultAccessRights;
    }

    @Override
    public void ensureChecksum(int nodeArchiveID, URL nodeArchiveURL) {
        
        if(archiveObjectsDBW.isOnSite(NodeIdUtils.TONODEID(nodeArchiveID))) {
            File nodeArchiveFile = FileUtils.toFile(nodeArchiveURL);
            if(nodeArchiveFile.exists() && nodeArchiveFile.canRead() && nodeArchiveFile.isFile()) {
                String checksum = Checksum.create(nodeArchiveFile.getPath());
                
                //TODO TRY TO GET OLD CHECKSUM???
                if(archiveObjectsDBW.getObjectChecksum(NodeIdUtils.TONODEID(nodeArchiveID)) != null) {
                    throw new UnsupportedOperationException("LamusCorpusStructureBridge.ensureChecksum (when existing checksum not null) not implemented yet");
                } else {
                
                    archiveObjectsDBW.setObjectChecksum(NodeIdUtils.TONODEID(nodeArchiveID), checksum);
                }
            } else {
                throw new UnsupportedOperationException("LamusCorpusStructureBridge.ensureChecksum (when nodeArchiveFile doesn't exist OR can't be read OR is not file) not implemented yet");
            }
        } else {
            throw new UnsupportedOperationException("LamusCorpusStructureBridge.ensureChecksum (when node is not on site) not implemented yet");
        }
    }

    
    //TODO This should be done somewhere else and in a different way...
    @Override
    public String calculatePID(int nodeArchiveID) {
        
        String handlePrefix = archiveObjectsDBW.getArchiveRoots().getHandlePrefix();
        if(handlePrefix == null) {
            throw new UnsupportedOperationException("LamusCorpusStructureBridge.calculatePID (when handle prefix is null) not implemented yet");
        }
        return "hdl:" + NodeIdUtils.nodeIdToHandle(NodeIdUtils.TONODEID(nodeArchiveID), handlePrefix);
        //TODO move protocol to a properties file
    }

    @Override
    public void updateArchiveObjectsNodePID(int archiveNodeID, String pid) {
        
        archiveObjectsDBW.setArchiveObjectPid(NodeIdUtils.TONODEID(archiveNodeID), pid);
    }

}
