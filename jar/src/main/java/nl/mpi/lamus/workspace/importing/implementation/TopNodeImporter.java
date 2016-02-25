/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
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

import java.net.URI;
import nl.mpi.lamus.exception.WorkspaceImportException;
import nl.mpi.lamus.workspace.model.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Importer specific for the top node of the workspace.
 * 
 * @author guisil
 */
@Component
public class TopNodeImporter {
    
    private static final Logger logger = LoggerFactory.getLogger(TopNodeImporter.class);
    
    @Autowired
    private MetadataNodeImporter metadataNodeImporter;
    
    
    /**
     * Imports the top node into the workspace, by invoking the correct importer
     * with only the parameters that matter in this case.
     * 
     * @param workspace workspace where to import the node
     * @param childNodeArchiveURI archive URI of the current node
     */
    public void importNode(Workspace workspace, URI childNodeArchiveURI) throws WorkspaceImportException {
        
        logger.debug("Importing top node of workspace; nodeURI: " + childNodeArchiveURI);
        
        workspace.setTopNodeArchiveURI(childNodeArchiveURI);
        metadataNodeImporter.importNode(workspace, null, null, null);
    }
    
}
