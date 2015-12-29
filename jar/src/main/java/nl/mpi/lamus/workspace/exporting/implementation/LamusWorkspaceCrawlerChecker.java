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
import nl.mpi.lamus.ams.AmsServiceBridge;
import nl.mpi.lamus.archive.CorpusStructureServiceBridge;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.CrawlerStateRetrievalException;
import nl.mpi.lamus.exception.VersionCreationException;
import nl.mpi.lamus.workspace.exporting.WorkspaceCrawlerChecker;
import nl.mpi.lamus.workspace.exporting.WorkspaceMailer;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNodeReplacement;
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
    private final AmsServiceBridge amsBridge;

    @Autowired
    public LamusWorkspaceCrawlerChecker(WorkspaceDao wsDao, CorpusStructureServiceBridge csServiceBridge,
        WorkspaceMailer wsMailer, AmsServiceBridge amsBridge) {
        workspaceDao = wsDao;
        corpusStructureServiceBridge = csServiceBridge;
        workspaceMailer = wsMailer;
        this.amsBridge = amsBridge;
    }
    
    /**
     * @see WorkspaceCrawlerChecker#checkCrawlersForSubmittedWorkspaces()
     */
    @Override
    public void checkCrawlersForSubmittedWorkspaces() throws CrawlerStateRetrievalException {
        
        logger.debug("Checking if there are submitted workspaces to be finalised");
        
        Collection<Workspace> submittedWorkspaces = workspaceDao.getWorkspacesInFinalStage();
        
        logger.debug("Found " + submittedWorkspaces.size() + " submitted workspaces");
        
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
    
    
    private void finaliseWorkspace(Workspace workspace, boolean crawlerWasSuccessful) {
        
        logger.debug("Finalising workspace " + workspace.getWorkspaceID() + (crawlerWasSuccessful ? " (successful)" : " (failed)"));
        
        boolean versioningWasSuccessful = Boolean.TRUE;
        
        // version creation service
        Collection<WorkspaceNodeReplacement> nodeReplacements = workspaceDao.getNodeReplacementsForWorkspace(workspace.getWorkspaceID());
        
        if(!nodeReplacements.isEmpty()) {
            
            logger.debug("Creating archive versions of replaced nodes for workspace " + workspace.getWorkspaceID());
            
            try {
                corpusStructureServiceBridge.createVersions(nodeReplacements);
            } catch(VersionCreationException ex) {

                logger.error("Error during archive versioning for workspace " + workspace.getWorkspaceID());

                versioningWasSuccessful = Boolean.FALSE;
            }
            
            if(versioningWasSuccessful) {
                amsBridge.triggerAmsNodeReplacements(nodeReplacements, workspace.getUserID());
            }
        }
        
        if(!crawlerWasSuccessful) {
            workspace.setStatus(WorkspaceStatus.ERROR_CRAWLING);
            workspace.setMessage("Data was successfully moved to the archive but the crawler failed.");
        } else if(!versioningWasSuccessful) {
            workspace.setStatus(WorkspaceStatus.ERROR_VERSIONING);
            workspace.setMessage("Data was successfully moved to the archive, the crawler was successful but archive versioning failed.");
        } else {
            workspace.setStatus(WorkspaceStatus.SUCCESS);
            workspace.setMessage("Data was successfully moved to the archive and the crawler was successful.");
        }
        
        workspaceDao.updateWorkspaceStatusMessage(workspace);
        
        if(crawlerWasSuccessful && versioningWasSuccessful) {
            workspaceDao.cleanWorkspaceNodesAndLinks(workspace);
            
            logger.debug("Triggering access rigths recalculation for workspace " + workspace.getWorkspaceID());
            
//            amsBridge.triggerAccessRightsRecalculation(workspace.getTopNodeArchiveURI());
            
            if(!nodeReplacements.isEmpty()) {
//                amsBridge.triggerAccessRightsRecalculationForVersionedNodes(nodeReplacements, workspace.getTopNodeArchiveURI());
                amsBridge.triggerAccessRightsRecalculationWithVersionedNodes(workspace.getTopNodeArchiveURI(), nodeReplacements);
            } else {
                amsBridge.triggerAccessRightsRecalculation(workspace.getTopNodeArchiveURI());
            }
        }
        
        //TODO some more details about the situation (especially in case of failure)
        
        logger.debug("Sending email to owner of workspace " + workspace.getWorkspaceID());
        
        workspaceMailer.sendWorkspaceFinalMessage(workspace, crawlerWasSuccessful, versioningWasSuccessful);
    }
}
