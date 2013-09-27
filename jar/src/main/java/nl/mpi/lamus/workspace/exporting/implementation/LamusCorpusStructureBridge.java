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
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.util.DateTimeHelper;
import nl.mpi.lamus.workspace.exporting.CorpusStructureBridge;
import nl.mpi.lamus.workspace.management.WorkspaceAccessChecker;
import nl.mpi.util.Checksum;
import nl.mpi.util.OurURL;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @see CorpusStructureBridge
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class LamusCorpusStructureBridge implements CorpusStructureBridge {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusCorpusStructureBridge.class);
    
//    private final ArchiveObjectsDBWrite archiveObjectsDBW;
    private final CorpusStructureProvider corpusStructureProvider;
    private final DateTimeHelper dateTimeHelper;
    private final ArchiveFileHelper archiveFileHelper;
    private final WorkspaceAccessChecker workspaceAccessChecker;
    
    @Autowired
    public LamusCorpusStructureBridge(
            CorpusStructureProvider csProvider,
            DateTimeHelper dtHelper, ArchiveFileHelper afHelper, WorkspaceAccessChecker wsAccessChecker) {
        
        this.corpusStructureProvider = csProvider;
        this.dateTimeHelper = dtHelper;
        this.archiveFileHelper = afHelper;
        this.workspaceAccessChecker = wsAccessChecker;
    }

    /**
     * @see CorpusStructureBridge#updateArchiveObjectsNodeURL(int, java.net.URL, java.net.URL)
     */
//    @Override
//    public boolean updateArchiveObjectsNodeURL(int archiveNodeID, URL oldArchiveNodeURL, URL newArchiveNodeURL) {
//        
//        if(newArchiveNodeURL == null) { //TODO IllegalArgumentException?
//            String errorMessage = "LamusArchiveObjectsBridge.updateArchiveObjectsNodeURL: new URL is null";
//            logger.error(errorMessage);
//            throw new IllegalArgumentException(errorMessage);
//        }
//        if(archiveNodeID < 0 || oldArchiveNodeURL == null) { //TODO Check both? Check PID?
//            logger.info("LamusArchiveObjectsBridge.updateArchiveObjectsNodeURL: node didn't exist in the archive before; nothing to update");
//            return true;
//        }
//
//        if(oldArchiveNodeURL.sameFile(newArchiveNodeURL)) {
//            logger.info("LamusArchiveObjectsBridge.updateArchiveObjectsNodeURL: old and new URLs point to the same file; nothing to update");
//            return true;
//        }
//        
//        ArchiveAccessContext archiveAccessContext = archiveObjectsDBW.getArchiveRoots();
//        
//        URI tableContextURI;
//        try {
//            
//            //TODO URLs pointing to orphans location?
//            
//            tableContextURI = archiveAccessContext.inTableContext(newArchiveNodeURL.toURI());
//        } catch (URISyntaxException ex) {
//            String errorMessage = "new URL is not a valid URI";
//            logger.error(errorMessage, ex);
//            throw new IllegalArgumentException("new URL is not a valid URI", ex);
//        }
//        
//        //TODO throw UpdateInProgressException?
//        return archiveObjectsDBW.moveArchiveObject(NodeIdUtils.TONODEID(archiveNodeID), tableContextURI);
//    }

    /**
     * @see CorpusStructureBridge#addNewNodeToCorpusStructure(java.net.URL, java.lang.String)
     */
//    @Override
//    public int addNewNodeToCorpusStructure(URL nodeArchiveURL, String pid, String userID) {
//        
//        URI inTableContextURI = null;
//        try {
//            inTableContextURI = archiveObjectsDBW.getArchiveRoots().inTableContext(nodeArchiveURL.toURI());
//        } catch (URISyntaxException ex) {
//            throw new UnsupportedOperationException("exception not handled yet", ex);
//        }
//        
//        long currentTimeInMs = dateTimeHelper.getCurrentDateTime().getTime();
//        Timestamp currentTimestamp = new Timestamp(currentTimeInMs);
//        
//        boolean onsite;
//        try {
//            onsite = archiveFileHelper.isUrlLocal(new OurURL(nodeArchiveURL));
//        } catch (MalformedURLException ex) {
//            throw new UnsupportedOperationException("exception not handled yet", ex);
//        }
//        
//        long size = 1; //TODO check the real size?
//        
//        //TODO At this point the PID must have been already assigned
//        
//        AccessInfo accessRights = workspaceAccessChecker.getDefaultAccessInfoForUser(userID);
//        
//        String newNodeID = archiveObjectsDBW.newArchiveObject(inTableContextURI, pid, currentTimestamp, onsite, size, currentTimestamp, accessRights);
//        
//        return NodeIdUtils.TOINT(newNodeID);
//    }

    /**
     * @see CorpusStructureBridge#linkNodesInCorpusStructure(int, int)
     */
//    @Override
//    public boolean linkNodesInCorpusStructure(int parentNodeArchiveID, int childNodeArchiveID) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }

    /**
     * @see CorpusStructureBridge#getChecksum(java.net.URL)
     */
    @Override
    public String getChecksum(URL nodeURL) {
        File nodeArchiveFile = FileUtils.toFile(nodeURL);
        String checksum = null;
        if(nodeArchiveFile.exists() && nodeArchiveFile.canRead() && nodeArchiveFile.isFile()) {
            checksum = Checksum.create(nodeArchiveFile.getPath());
        } else {
            throw new UnsupportedOperationException("LamusCorpusStructureBridge.getChecksum (when nodeArchiveFile doesn't exist OR can't be read OR is not file) not implemented yet");
        }
        return checksum;
    }
    
    /**
     * @see CorpusStructureBridge#ensureChecksum(int, java.net.URL)
     */
//    @Override
//    public boolean ensureChecksum(int nodeArchiveID, URL nodeURL) {
//        
//        //TODO does this check make sense?
//        if(archiveObjectsDBW.isOnSite(NodeIdUtils.TONODEID(nodeArchiveID))) {
//
//            String newChecksum = getChecksum(nodeURL);
//            
//            //TODO TRY TO GET OLD CHECKSUM???
//            String oldChecksum = archiveObjectsDBW.getObjectChecksum(NodeIdUtils.TONODEID(nodeArchiveID));
//            
//            if(oldChecksum != null && oldChecksum.equals(newChecksum)) { //checksum didn't change
//                return Boolean.FALSE;
//            }
//            
//            archiveObjectsDBW.setObjectChecksum(NodeIdUtils.TONODEID(nodeArchiveID), newChecksum);
//            
//            return Boolean.TRUE;
//            
//        } else {
//            throw new UnsupportedOperationException("LamusCorpusStructureBridge.ensureChecksum (when node is not on site) not implemented yet");
//        }
//    }

    /**
     * @see CorpusStructureBridge#updateArchiveObjectsNodePID(int, java.lang.String)
     */
//    @Override
//    public void updateArchiveObjectsNodePID(int archiveNodeID, String pid) {
//        
//        archiveObjectsDBW.setArchiveObjectPid(NodeIdUtils.TONODEID(archiveNodeID), pid);
//    }

}
