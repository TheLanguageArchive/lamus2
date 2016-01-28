/*
 * Copyright (C) 2016 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.workspace.management.implementation;

import java.net.URI;
import java.util.List;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.archive.CorpusStructureBridge;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.PreLockedNodeException;
import nl.mpi.lamus.workspace.management.PreLockChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see PreLockChecker
 * @author guisil
 */
@Component
public class LamusPreLockChecker implements PreLockChecker {
    
    private final CorpusStructureBridge corpusStructureBridge;
    private final CorpusStructureProvider corpusStructureProvider;
    private final NodeResolver nodeResolver;
    private final WorkspaceDao workspaceDao;
    
    @Autowired
    public LamusPreLockChecker(
            CorpusStructureBridge csBridge, CorpusStructureProvider csProvider,
            NodeResolver nResolver, WorkspaceDao wsDao) {
        corpusStructureBridge = csBridge;
        corpusStructureProvider = csProvider;
        nodeResolver = nResolver;
        workspaceDao = wsDao;
    }

    /**
     * @see PreLockChecker#ensureNoNodesInPathArePreLocked(java.net.URI)
     */
    @Override
    public void ensureNoNodesInPathArePreLocked(URI nodeURI) throws PreLockedNodeException {
        
        if(nodeURI == null) {
            throw new IllegalArgumentException("Node URI cannot be null");
        }
        
        List<String> urisToCheck = corpusStructureBridge.getURIsOfAncestorsAndDescendants(nodeURI);
        if(!urisToCheck.isEmpty()) {
            if(workspaceDao.isAnyOfNodesPreLocked(urisToCheck)) {
                String message = "A workspace is already being created in the path of node " + nodeURI;
                throw new PreLockedNodeException(message, nodeURI);
            }
        }
    }
    
}
