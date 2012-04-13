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
package nl.mpi.lamus.filesystem.implementation;

import java.io.*;
import javax.xml.transform.TransformerException;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.workspace.Workspace;
import nl.mpi.lamus.workspace.WorkspaceNode;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.MetadataDocument;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceFileHandler implements WorkspaceFileHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusWorkspaceFileHandler.class);

    public void copyMetadataFileToWorkspace(Workspace workspace, WorkspaceNode workspaceNode, MetadataAPI metadataAPI, MetadataDocument metadataDocument) {
        
        
//Copy file to workspace directory (give it a different name - based on the node ID)
//        String workspaceDirectory = configuration.getWorkspaceBaseDirectory();
//        File workspaceNodeFile = new File(workspaceDirectory + File.separator + workspaceTopNode.getWorkspaceNodeID());
        File workspaceNodeFile = null;// = workspaceFileHandler.getWorkspaceNodeFile(workspace, workspaceTopNode);
        
        //TODO create node file in filesystem
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(workspaceNodeFile);
            metadataAPI.writeMetadataDocument(metadataDocument, outputStream);
        } catch(FileNotFoundException fnfex) {
            logger.error("Problem with file for node " + workspaceNode.getWorkspaceNodeID() + " in workspace + " + workspace.getWorkspaceID(), fnfex);
            //TODO do something more, throw again?
        } catch(IOException ioex) {
            logger.error("Problem writing file " + workspaceNodeFile.getAbsolutePath(), ioex);
            //TODO do something more, throw again?
        } catch(TransformerException tex) {
            logger.error("Problem writing file " + workspaceNodeFile.getAbsolutePath(), tex);
            //TODO do something more, throw again?
        } catch(MetadataException mdex) {
            logger.error("Problem writing file " + workspaceNodeFile.getAbsolutePath(), mdex);
            //TODO do something more, throw again?
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
        
        
        throw new UnsupportedOperationException("Not supported yet.");
        
    }
    
}
