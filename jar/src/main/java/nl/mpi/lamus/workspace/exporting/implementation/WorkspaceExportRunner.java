/*
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
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

import java.util.Collection;
import java.util.concurrent.Callable;
import nl.mpi.lamus.archive.CorpusStructureServiceBridge;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.CrawlerInvocationException;
import nl.mpi.lamus.exception.NodeUrlUpdateException;
import nl.mpi.lamus.exception.WorkspaceNodeNotFoundException;
import nl.mpi.lamus.exception.WorkspaceExportException;
import nl.mpi.lamus.workspace.exporting.NodeExporter;
import nl.mpi.lamus.workspace.exporting.NodeExporterFactory;
import nl.mpi.lamus.workspace.exporting.UnlinkedAndDeletedNodesExportHandler;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceExportPhase;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceReplacedNodeUrlUpdate;
import nl.mpi.lamus.workspace.model.WorkspaceSubmissionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Runner that will trigger a thread that performs
 * the export of the nodes from the workspace back into the archive.
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class WorkspaceExportRunner implements Callable<Boolean> {

    private final  WorkspaceDao workspaceDao;
    private final NodeExporterFactory nodeExporterFactory;
    private final UnlinkedAndDeletedNodesExportHandler unlinkedAndDeletedNodesExportHandler;
    private final CorpusStructureServiceBridge corpusStructureServiceBridge;
    
    private Workspace workspace;
    private boolean keepUnlinkedFiles;
    private WorkspaceSubmissionType submissionType;
    
    @Autowired
    public WorkspaceExportRunner(WorkspaceDao wsDao, NodeExporterFactory exporterFactory,
            UnlinkedAndDeletedNodesExportHandler dnExportHandler,
            CorpusStructureServiceBridge csServiceBridge) {
        this.workspaceDao = wsDao;
        this.nodeExporterFactory = exporterFactory;
        this.unlinkedAndDeletedNodesExportHandler = dnExportHandler;
        this.corpusStructureServiceBridge = csServiceBridge;
    }
    
    /**
     * Setter for the workspace to submit
     * @param ws workspace to be used for the export
     */
    public void setWorkspace(Workspace ws) {
        this.workspace = ws;
    }
    
    /**
     * Setter for the boolean that indicates if the unlinked files are to be kept
     * @param keepUnlinkedFiles true if unlinked files are to be kept
     */
    public void setKeepUnlinkedFiles(boolean keepUnlinkedFiles) {
        this.keepUnlinkedFiles = keepUnlinkedFiles;
    }
    
    /**
     * Setter for the enumeration that indicates if the intention is to
     * submit or delete the workspace
     * @param submissionType indicates if the workspace should be submitted or deleted
     */
    public void setSubmissionType(WorkspaceSubmissionType submissionType) {
        this.submissionType = submissionType;
    }
    

    /**
     * The export process is started in a separate thread.
     * The nodes will be explored and copied, starting with the top node.
     * @return true if export is successful
     */
    @Override
    public Boolean call() throws WorkspaceNodeNotFoundException, WorkspaceExportException, CrawlerInvocationException, NodeUrlUpdateException {
        
        if(workspace == null) {
            throw new IllegalStateException("Workspace not set");
        }
        if(submissionType == null) {
            throw new IllegalStateException("It should be specified what type of submission (submit or delete) to perform");
        }
        
        if(WorkspaceSubmissionType.DELETE_WORKSPACE.equals(submissionType)) {
            
            unlinkedAndDeletedNodesExportHandler.exploreUnlinkedAndDeletedNodes(workspace, keepUnlinkedFiles, submissionType, WorkspaceExportPhase.UNLINKED_NODES_EXPORT);
            
        } else if(WorkspaceSubmissionType.SUBMIT_WORKSPACE.equals(submissionType)) {

            unlinkedAndDeletedNodesExportHandler.exploreUnlinkedAndDeletedNodes(workspace, keepUnlinkedFiles, submissionType, WorkspaceExportPhase.UNLINKED_NODES_EXPORT);
            
            Collection<WorkspaceReplacedNodeUrlUpdate> replacedNodesUrlUpdates = workspaceDao.getReplacedNodeUrlsToUpdateForWorkspace(workspace.getWorkspaceID());
            if(!replacedNodesUrlUpdates.isEmpty()) {
                corpusStructureServiceBridge.updateReplacedNodesUrls(replacedNodesUrlUpdates);
            }
            
            WorkspaceNode topNode = workspaceDao.getWorkspaceTopNode(workspace.getWorkspaceID());

            NodeExporter topNodeExporter = nodeExporterFactory.getNodeExporterForNode(workspace, topNode, WorkspaceExportPhase.TREE_EXPORT);
            topNodeExporter.exportNode(workspace, null, null, topNode, keepUnlinkedFiles, submissionType, WorkspaceExportPhase.TREE_EXPORT);

            

            // crawler service
            String crawlerID = corpusStructureServiceBridge.callCrawler(topNode.getArchiveURI());
            workspace.setCrawlerID(crawlerID);
            workspaceDao.updateWorkspaceCrawlerID(workspace);
        
        } else {
            throw new UnsupportedOperationException("Type of submission not supported");
        }
        
        return Boolean.TRUE;
    }

}
