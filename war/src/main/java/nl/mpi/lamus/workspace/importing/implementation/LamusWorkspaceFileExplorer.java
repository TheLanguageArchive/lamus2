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
package nl.mpi.lamus.workspace.importing.implementation;

import java.lang.reflect.InvocationTargetException;
import nl.mpi.lamus.workspace.importing.WorkspaceFileExplorer;
import nl.mpi.lamus.workspace.importing.FileImporter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.logging.Level;
import nl.mpi.corpusstructure.ArchiveObjectsDB;
import nl.mpi.corpusstructure.NodeIdUtils;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.workspace.LamusWorkspaceNode;
import nl.mpi.lamus.workspace.WorkspaceNode;
import nl.mpi.lamus.workspace.exception.FileImporterInitialisationException;
import nl.mpi.lamus.workspace.importing.FileImporterFactory;
import nl.mpi.metadata.api.model.Reference;
import org.apache.xmlbeans.impl.xb.xsdschema.ListDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceFileExplorer implements WorkspaceFileExplorer {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusWorkspaceFileExplorer.class);
    
    private final ArchiveObjectsDB archiveObjectsDB;
    private final WorkspaceDao workspaceDao;
    private final FileImporterFactory fileImporterFactory;
    
    public LamusWorkspaceFileExplorer(ArchiveObjectsDB aoDB, WorkspaceDao wsDao, FileImporterFactory importerFactory) {
        this.archiveObjectsDB = aoDB;
        this.workspaceDao = wsDao;
        this.fileImporterFactory = importerFactory;
    }

    public void explore(WorkspaceNode nodeToExplore, Collection<Reference> linksInNode) {
        
        
        //TODO for each link call recursive method to explore it
        

        for(Reference currentLink : linksInNode) {
        
            //TODO check if node already exists in DB
            WorkspaceNode currentNode = null;
//            WorkspaceNode currentNode = workspaceDao.getWorkspaceNode(topNodeArchiveID); //TODO different method with archiveID

            String currentNodeArchiveIdStr = archiveObjectsDB.getObjectId(currentLink.getURI());
            if(currentNodeArchiveIdStr == null) {
                //TODO node doesn't exist?
            }

            int currentNodeArchiveID = NodeIdUtils.TOINT(currentNodeArchiveIdStr);
            URL currentNodeArchiveURL = null;
            try {
                currentNodeArchiveURL = currentLink.getURI().toURL();
            } catch(MalformedURLException muex) {
                //TODO problems with URL
                logger.error("PROBLEMS GETTING URL FOR NODE", muex);
            }
            
            if(workspaceDao.isNodeLocked(currentNodeArchiveID)) {
                //TODO check if it exists under the same workspace (wouldn't make sense if it's another one, though...)
                
            } else {

                
                
                //TODO check if it is Metadata or Resource node

                try {
                    Class<? extends FileImporter> linkImporterType = fileImporterFactory.getFileImporterTypeForReference(currentLink.getClass());
                    FileImporter linkImporter = fileImporterFactory.getNewFileImporterOfType(linkImporterType);
                    linkImporter.importFile(currentLink, currentNodeArchiveID);
                } catch (FileImporterInitialisationException ex) {
                    java.util.logging.Logger.getLogger(LamusWorkspaceFileExplorer.class.getName()).log(Level.SEVERE, null, ex);
                }                
           
                
                
                
                
                
                //TODO following code should be part of importer
                
                //TODO create the node in DB
                currentNode = new LamusWorkspaceNode();
                currentNode.setArchiveNodeID(currentNodeArchiveID);
                currentNode.setArchiveURL(currentNodeArchiveURL);
                currentNode.setOriginURL(currentNodeArchiveURL);
                currentNode.setWorkspaceID(nodeToExplore.getWorkspaceID());
//                currentNode.setPid();
                
                
                //TODO create link in DB
                
                
                //TODO if MetadataReference: recursive call to exploreNodesBelow
                
            }
            
            //TODO link it to the parent in DB (it's the same either if it was already created or if it wasn't)
            
        
        }
        
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
