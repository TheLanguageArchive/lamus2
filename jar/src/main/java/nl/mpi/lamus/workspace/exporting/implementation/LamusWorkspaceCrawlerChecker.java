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
package nl.mpi.lamus.workspace.exporting.implementation;

import java.util.Collection;
import nl.mpi.lamus.archive.CorpusStructureServiceBridge;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.workspace.exporting.WorkspaceCrawlerChecker;
import nl.mpi.lamus.workspace.exporting.WorkspaceMailer;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see WorkspaceCrawlerChecker
 * @author guisil
 */
@Component
public class LamusWorkspaceCrawlerChecker implements WorkspaceCrawlerChecker {
    
    private static Logger logger = LoggerFactory.getLogger(LamusWorkspaceCrawlerChecker.class);

    private final WorkspaceDao workspaceDao;
    private final CorpusStructureServiceBridge corpusStructureServiceBridge;
    private final WorkspaceMailer workspaceMailer;

    @Autowired
    public LamusWorkspaceCrawlerChecker(WorkspaceDao wsDao, CorpusStructureServiceBridge csServiceBridge,
        WorkspaceMailer wsMailer) {
        workspaceDao = wsDao;
        corpusStructureServiceBridge = csServiceBridge;
        workspaceMailer = wsMailer;
    }
    
    /**
     * @see WorkspaceCrawlerChecker#checkCrawlersForSubmittedWorkspaces()
     */
    @Override
    public void checkCrawlersForSubmittedWorkspaces() {
        
        logger.debug("Checking in there are submitted workspaces to be finalised");
        
        Collection<Workspace> submittedWorkspaces = workspaceDao.getWorkspacesInFinalStage();
        
        for(Workspace ws : submittedWorkspaces) {
            
            String crawlerID = ws.getCrawlerID();
            
            String crawlerState = corpusStructureServiceBridge.getCrawlerState(crawlerID);
            
            if("STARTED".equals(crawlerState)) {
                continue;
            }
            
            if("SUCCESS".equals(crawlerState)) {
                finaliseWorkspace(ws, true);
            }
            
            if("CRASHED".equals(crawlerState)) {
                finaliseWorkspace(ws, false);
            }
        }
    }
    
    
    private void finaliseWorkspace(Workspace workspace, boolean isSuccessful) {
        
        if(isSuccessful) {
            workspace.setStatus(WorkspaceStatus.DATA_MOVED_SUCCESS);
            workspace.setMessage("Data was successfully moved to the archive and the crawler was successful.");
        } else {
            workspace.setStatus(WorkspaceStatus.DATA_MOVED_ERROR);
            workspace.setMessage("Data was successfully moved to the archive but the crawler failed.");
        }
        
        workspaceDao.updateWorkspaceStatusMessage(workspace);
        
        //TODO some more details about the situation (especially in case of failure)
        
        workspaceMailer.sendWorkspaceFinalMessage(workspace, isSuccessful);
    }
}
