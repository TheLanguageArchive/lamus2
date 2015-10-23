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
package nl.mpi.lamus.workspace.replace.action.implementation;

import java.util.List;
import nl.mpi.lamus.exception.ProtectedNodeException;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.workspace.replace.action.ReplaceActionExecutor;
import nl.mpi.lamus.workspace.replace.action.ReplaceActionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see ReplaceActionManager
 * 
 * @author guisil
 */
@Component
public class LamusReplaceActionManager implements ReplaceActionManager {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusReplaceActionManager.class);
    
    private ReplaceActionExecutor replaceActionExecutor;
    
    @Autowired
    public LamusReplaceActionManager(ReplaceActionExecutor actionExecutor) {
        replaceActionExecutor = actionExecutor;
    }
    
    /**
     * @see ReplaceActionManager#addActionToList(nl.mpi.lamus.workspace.replace.action.implementation.NodeReplaceAction, java.util.List)
     */
    @Override
    public void addActionToList(NodeReplaceAction action, List<NodeReplaceAction> list) {
        if(!list.contains(action)) {
            logger.debug("Action added to list: [" + action.toString() + "]");
            list.add(action);
        }
    }

    /**
     * @see ReplaceActionManager#applyActions(java.util.List)
     */
    @Override
    public void applyActions(List<NodeReplaceAction> list) throws WorkspaceException, ProtectedNodeException {
        
        for(NodeReplaceAction action : list) {
            logger.debug("Action to execute from list: [" + action.toString() + "]");
            replaceActionExecutor.execute(action);
        }
    }
}
