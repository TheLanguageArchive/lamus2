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
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.NodeNotFoundException;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.archive.ArchivePidHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author guisil
 */
@Component
public class LamusArchivePidHelper implements ArchivePidHelper {

    private CorpusStructureProvider corpusStructureProvider;
    private NodeResolver nodeResolver;
    
    @Autowired
    public LamusArchivePidHelper(CorpusStructureProvider provider, NodeResolver resolver) {
        corpusStructureProvider = provider;
        nodeResolver = resolver;
    }
    
    @Override
    public URI getPidForNode(URI nodeURI) throws NodeNotFoundException {
        
        CorpusNode node = corpusStructureProvider.getNode(nodeURI);
        if(node == null) {
            String message = "Node with URI '" + nodeURI + "' not found";
            throw new NodeNotFoundException(nodeURI, message);
        }
        
        return nodeResolver.getPID(node);
    }
    
}
