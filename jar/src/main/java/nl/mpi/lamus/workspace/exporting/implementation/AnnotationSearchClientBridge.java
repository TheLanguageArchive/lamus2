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
package nl.mpi.lamus.workspace.exporting.implementation;

import java.sql.SQLException;
import nl.mpi.annot.search.lib.SearchClient;
import nl.mpi.corpusstructure.NodeIdUtils;
import nl.mpi.lamus.workspace.exporting.SearchClientBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class AnnotationSearchClientBridge implements SearchClientBridge {
    
    private static final Logger logger = LoggerFactory.getLogger(AnnotationSearchClientBridge.class);

    private final SearchClient searchClient;
    
    public AnnotationSearchClientBridge(SearchClient sClient) {
        this.searchClient = sClient;
    }
    
    public void removeNode(int archiveNodeID) {
        try {
            searchClient.remove(NodeIdUtils.TONODEID(archiveNodeID));
        } catch (SQLException ex) {
            logger.error("Problems when calling Annex in order to remove node " + archiveNodeID + " from the annotation search database", ex);
            //TODO throw another exception? return something??
        }
        searchClient.close();
    }
    
}
