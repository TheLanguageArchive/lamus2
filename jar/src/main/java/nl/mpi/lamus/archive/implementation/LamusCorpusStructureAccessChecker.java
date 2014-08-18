/*
 * Copyright (C) 2014 Max Planck Institute for Psycholinguistics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.mpi.lamus.archive.implementation;

import java.net.URI;
import java.util.Collection;
import nl.mpi.archiving.corpusstructure.core.ArchiveUser;
import nl.mpi.archiving.corpusstructure.core.NodeNotFoundException;
import nl.mpi.archiving.corpusstructure.provider.AccessInfoProvider;
import nl.mpi.lamus.archive.CorpusStructureAccessChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see CorpusStructureAccessChecker
 * @author guisil
 */
@Component
public class LamusCorpusStructureAccessChecker implements CorpusStructureAccessChecker {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusCorpusStructureAccessChecker.class);
 
    private final AccessInfoProvider accessInfoProvider;
    
    @Autowired
    public LamusCorpusStructureAccessChecker(AccessInfoProvider aiProvider) {
        accessInfoProvider = aiProvider;
    }
    
    /**
     * @see CorpusStructureAccessChecker#hasWriteAccess(java.lang.String, java.net.URI)
     */
    @Override
    public boolean hasWriteAccess(String userId, URI archiveNodeURI) throws NodeNotFoundException {
        
        logger.debug("Checking if user {} has write access to node '{}'", userId, archiveNodeURI);
        
        Collection<ArchiveUser> writers = accessInfoProvider.getWriteRights(archiveNodeURI);
        
        for(ArchiveUser user : writers) {
            if(userId.equals(user.getUid())) {
                logger.debug("User {} has write access to node '{}'", userId, archiveNodeURI);
                return true;
            }
        }
        
        logger.debug("User {} doesn't have write access to node '{}'", userId, archiveNodeURI);
        return false;
    }
}
