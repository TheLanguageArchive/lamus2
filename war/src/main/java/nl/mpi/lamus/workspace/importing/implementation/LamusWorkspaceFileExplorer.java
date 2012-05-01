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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.logging.Level;
import nl.mpi.corpusstructure.ArchiveObjectsDB;
import nl.mpi.corpusstructure.NodeIdUtils;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.workspace.exception.FileImporterException;
import nl.mpi.lamus.workspace.exception.FileImporterInitialisationException;
import nl.mpi.lamus.workspace.importing.FileImporter;
import nl.mpi.lamus.workspace.importing.FileImporterFactory;
import nl.mpi.lamus.workspace.importing.WorkspaceFileExplorer;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceParentNodeReference;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceParentNodeReference;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
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

    public void explore(WorkspaceNode nodeToExplore, ReferencingMetadataDocument nodeDocument, Collection<Reference> linksInNode) {
        
        
        //TODO for each link call recursive method to explore it
        

        for(Reference currentLink : linksInNode) {
        
            //TODO check if the file does exist
            
            String currentNodeArchiveIdStr = archiveObjectsDB.getObjectId(currentLink.getURI());
            if(currentNodeArchiveIdStr == null) {
                //TODO node doesn't exist?
                logger.error("PROBLEMS GETTING NODE ID");
                //TODO throw exception
            }

            int currentNodeArchiveID = NodeIdUtils.TOINT(currentNodeArchiveIdStr);
            
            //TODO check here if it's already locked or not?
            
            //TODO check if it is Metadata or Resource node
            
            try {
                Class<? extends FileImporter> linkImporterType = fileImporterFactory.getFileImporterTypeForReference(currentLink.getClass());
                FileImporter linkImporter = fileImporterFactory.getNewFileImporterOfType(linkImporterType);
//                WorkspaceParentNodeReference parentNodeReference = 
//                        new LamusWorkspaceParentNodeReference(nodeToExplore.getWorkspaceNodeID(), currentLink);
                linkImporter.importFile(nodeToExplore, nodeDocument, currentLink, currentNodeArchiveID);
            } catch (FileImporterInitialisationException fiiex) {
                String errorMessage = "ERROR ERROR";
                logger.error(errorMessage, fiiex);
                //TODO LOG PROPERLY
                //TODO THROW EXCEPTION OR RETURN?
            } catch (FileImporterException fiex) {
                String errorMessage = "ERROR ERROR";
                logger.error(errorMessage, fiex);
                //TODO LOG PROPERLY
                //TODO THROW EXCEPTION OR RETURN?
            }
            
        }
        
//        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
