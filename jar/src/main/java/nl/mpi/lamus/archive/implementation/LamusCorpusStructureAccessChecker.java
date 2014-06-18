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
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.AccessInfoProvider;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.archive.CorpusStructureAccessChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author guisil
 */
@Component
public class LamusCorpusStructureAccessChecker implements CorpusStructureAccessChecker {
 
    private final AccessInfoProvider accessInfoProvider;
    
    @Autowired
    public LamusCorpusStructureAccessChecker(AccessInfoProvider aiProvider) {
        accessInfoProvider = aiProvider;
    }
    
    
    @Override
    public boolean hasWriteAccess(String userId, URI archiveNodeURI) {
        
        // NOT A SERVICE YET, BUT SHOULD PROBABLY BE
        // USING THE AMS SERVICE WASN'T WORKING DUE TO LAZY LOADING PROBLEMS WITH HIBERNATE... TRYING DIFFERENT APPROACH
        
        
        
        Collection<ArchiveUser> writers = accessInfoProvider.getWriteRights(archiveNodeURI);
        
        for(ArchiveUser user : writers) {
            if(userId.equals(user.getUid())) {
                return true;
            }
        }
        
        return false;
    }
}
