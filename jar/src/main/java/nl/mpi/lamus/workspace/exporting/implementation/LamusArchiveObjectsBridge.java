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

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import nl.mpi.corpusstructure.ArchiveAccessContext;
import nl.mpi.corpusstructure.ArchiveObjectsDBWrite;
import nl.mpi.corpusstructure.NodeIdUtils;
import nl.mpi.lamus.workspace.exporting.ArchiveObjectsBridge;
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
public class LamusArchiveObjectsBridge implements ArchiveObjectsBridge {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusArchiveObjectsBridge.class);
    
    private final ArchiveObjectsDBWrite archiveObjectsDBW;
    
    @Autowired
    public LamusArchiveObjectsBridge(@Qualifier("ArchiveObjectsDB") ArchiveObjectsDBWrite aodbw) {
        
        this.archiveObjectsDBW = aodbw;
    }

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

}
