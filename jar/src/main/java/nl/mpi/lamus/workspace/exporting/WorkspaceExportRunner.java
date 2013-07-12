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
package nl.mpi.lamus.workspace.exporting;

import java.util.Collection;
import java.util.concurrent.Callable;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class WorkspaceExportRunner implements Callable<Boolean> {

    private WorkspaceDao workspaceDao;
    private NodeExporterFactory nodeExporterFactory;
    
    private Workspace workspace;
    private boolean keepUnlinkedFiles;

    @Autowired
    public WorkspaceExportRunner(WorkspaceDao wsDao, NodeExporterFactory exporterFactory) {
        this.workspaceDao = wsDao;
        this.nodeExporterFactory = exporterFactory;
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
    
    @Override
    public Boolean call() {
        
        //1. save imdi files - NOT NEEDED (?)
        //2. consistency checks - (?)
        //2.9 (?)
        
        // update message according to what's being done?
        
        //3.0 send removed files (deleted) to the trashcan (SetIngestLocations.trashDeletedFiles)
        //3.4 version links (first time) - move replaced files which don't get a version (equivalent to corpus???) to a place where they will be overwritten
        //3.3 rename replaced virtual resources, to avoid name clashes later (?)
        //3.2 determine archive urls for nodes that weren't in the archive and update those in the lamus db
        
        //4 update links in the metadata files, so that they point to the right location when in the archive
        
        //5.1 close all imdi files (remove them from cache) in the workspace and save them - NEEDED ?
        //5.2 copy the files into the archive and update urls in the csdb where needed
        //5.3 allocate urids (handles) and ao entries for all new nodes to enter the archive
        //5.4 version links (second time) - linking between new and old versions of updated resources
        //5.5 unlink all children of unlinked/free nodes in order to make them free too (???)
        //5.6 clean up free nodes (?)
        
        //6.1 clean up workspace database
        //6.2 call archive crawler, update csdb
         // fetch top node id and adjust unix permissions
         // set access rights to the top node for nobody and call ams2 recalculation
         // update status and message
        
        //7 update user, ingest request information and send email
        
        
//        Collection<WorkspaceNode> workspaceNodes = workspaceDao.getNodesForWorkspace(workspace.getWorkspace());
//        
//        for(WorkspaceNode currentNode : workspaceNodes) {
//            
//            NodeExporter currentNodeExporter = nodeExporterFactory.getNodeExporterForNode(currentNode);
//            currentNodeExporter.exportNode(currentNode);
//        }
        
        WorkspaceNode topNode = workspaceDao.getWorkspaceTopNode(workspace.getWorkspaceID());
//        workspaceTreeExporter.explore(topNode);
        
        NodeExporter topNodeExporter = nodeExporterFactory.getNodeExporterForNode(workspace, topNode);
        topNodeExporter.exportNode(null, topNode);
        
        //TODO Export deleted nodes...
        
        //TODO take care of unlinked nodes in the workspace...
        //TODO cleanup WS DB / filesystem
        
        //TODO call crawler
        
        //TODO fix permissions
        
        //TODO call AMS recalculation
        
        
        //update user/request information and send email

        
        
        return Boolean.TRUE;
    }

}
