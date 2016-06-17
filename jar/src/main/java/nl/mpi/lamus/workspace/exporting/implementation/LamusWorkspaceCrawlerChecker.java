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

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.ams.AmsServiceBridge;
import nl.mpi.lamus.archive.CorpusStructureServiceBridge;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.CrawlerStateRetrievalException;
import nl.mpi.lamus.exception.VersionCreationException;
import nl.mpi.lamus.workspace.exporting.WorkspaceCrawlerChecker;
import nl.mpi.lamus.workspace.exporting.WorkspaceMailer;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
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
    
    private static final Logger logger = LoggerFactory.getLogger(LamusWorkspaceCrawlerChecker.class);

    private final WorkspaceDao workspaceDao;
    private final CorpusStructureServiceBridge corpusStructureServiceBridge;
    private final WorkspaceMailer workspaceMailer;
    private final AmsServiceBridge amsBridge;
    private final CorpusStructureProvider corpusStructureProvider;

    @Autowired
    public LamusWorkspaceCrawlerChecker(WorkspaceDao wsDao, CorpusStructureServiceBridge csServiceBridge,
        WorkspaceMailer wsMailer, AmsServiceBridge amsBridge, CorpusStructureProvider csProvider) {
        workspaceDao = wsDao;
        corpusStructureServiceBridge = csServiceBridge;
        workspaceMailer = wsMailer;
        this.amsBridge = amsBridge;
        this.corpusStructureProvider = csProvider;
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
        
        Collection<WorkspaceNode> descendantNodes = workspaceDao.getMetadataNodesInTreeForWorkspace(workspace.getWorkspaceID());
        
        Set<URI> canoninalParents = new HashSet<URI>();
        for (WorkspaceNode node : descendantNodes) {
        	URI canonicalParent = corpusStructureProvider.getCanonicalParent(node.getArchiveURI());
        	if (node.isProtected() && canonicalParent != null) {
                logger.debug("Worspace child protected: [" + node.getName() + "] adding canonical parent for rights recalcualtion: [" + canonicalParent.toString() + "]");
        		canoninalParents.add(canonicalParent);
        	}
        }
        
        if(crawlerWasSuccessful && versioningWasSuccessful) {
            workspaceDao.cleanWorkspaceNodesAndLinks(workspace);
            
            logger.debug("Triggering access rigths recalculation for workspace " + workspace.getWorkspaceID());
            
            URI topNodeArchiveURI = workspace.getTopNodeArchiveURI();
            canoninalParents.add(topNodeArchiveURI);
            logger.debug("Added workspace top node for rights recalculation: " + topNodeArchiveURI);
            
            if(!nodeReplacements.isEmpty()) {
            	amsBridge.triggerAccessRightsRecalculationWithVersionedNodes(canoninalParents, nodeReplacements);
            } else {
            	amsBridge.triggerAccessRightsRecalculation(canoninalParents);
            }
        }
        
        logger.debug("Sending email to owner of workspace " + workspace.getWorkspaceID());
        
        workspaceMailer.sendWorkspaceFinalMessage(workspace, crawlerWasSuccessful, versioningWasSuccessful);
    }
}
