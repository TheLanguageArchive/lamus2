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
package nl.mpi.lamus.archive.implementation;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import javax.json.JsonObject;
import nl.mpi.lamus.archive.CorpusStructureServiceBridge;
import nl.mpi.lamus.archive.JsonTransformationHandler;
import nl.mpi.lamus.exception.VersionCreationException;
import nl.mpi.lamus.util.JerseyHelper;
import nl.mpi.lamus.workspace.model.WorkspaceNodeReplacement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @see CorpusStructureServiceBridge
 * @author guisil
 */
@Component
public class LamusCorpusStructureServiceBridge implements CorpusStructureServiceBridge {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusCorpusStructureServiceBridge.class);

    private final JsonTransformationHandler jsonTransformationHandler;
    private final JerseyHelper jerseyHelper;
    
    @Autowired
    @Qualifier("corpusStructureServiceLocation")
    private String corpusStructureServiceLocation;
    @Autowired
    @Qualifier("corpusStructureServiceVersioningPath")
    private String corpusStructureServiceVersioningPath;
    @Autowired
    @Qualifier("corpusStructureServiceVersionCreationPath")
    private String corpusStructureServiceVersionCreationPath;
    @Autowired
    @Qualifier("corpusStructureServiceCrawlerPath")
    private String corpusStructureServiceCrawlerPath;
    @Autowired
    @Qualifier("corpusStructureServiceCrawlerStartPath")
    private String corpusStructureServiceCrawlerStartPath;
    
    @Autowired
    public LamusCorpusStructureServiceBridge(JsonTransformationHandler jsonTransformationHandler, JerseyHelper jerseyHelper) {
        this.jsonTransformationHandler = jsonTransformationHandler;
        this.jerseyHelper = jerseyHelper;
    }
    
    /**
     * @see CorpusStructureServiceBridge#createVersions(java.util.Collection)
     */
    @Override
    public void createVersions(Collection<WorkspaceNodeReplacement> nodeReplacements) throws VersionCreationException {
        
        
        //TODO 
        
        JsonObject requestJsonObject =
                jsonTransformationHandler.createJsonObjectFromNodeReplacementCollection(nodeReplacements);
        
        JsonObject responseJsonObject =
                jerseyHelper.postRequestWithJson(
                    requestJsonObject,
                    corpusStructureServiceLocation,
                    corpusStructureServiceVersioningPath,
                    corpusStructureServiceVersionCreationPath);
        
        Collection<WorkspaceNodeReplacement> responseNodeReplacements;
        try {
            //TODO HANDLE RESPONSE JSON OBJECT
            
            responseNodeReplacements =
                    jsonTransformationHandler.createNodeReplacementCollectionFromJsonObject(responseJsonObject);
        } catch (URISyntaxException ex) {
            String errorMessage = "Error with a URI during version creation";
            logger.error(errorMessage, ex);
            throw new VersionCreationException(errorMessage, ex);
        }
        
        for(WorkspaceNodeReplacement replacement : responseNodeReplacements) {
            logger.debug("Retrieving result of replacement. Old node: " + replacement.getOldNodeURI().toString() + " ; New node: " + replacement.getNewNodeURI().toString());
            if(!"OK".equals(replacement.getReplacementStatus().toUpperCase())) {
                String errorMessage = "Error during version creation. Status: " + replacement.getReplacementStatus() + "; error: " + replacement.getReplacementError();
                logger.error(errorMessage);
                throw new VersionCreationException(errorMessage, null);
            }
        }
    }

    /**
     * @see CorpusStructureServiceBridge#callCrawler(java.net.URI)
     */
    @Override
    public void callCrawler(URI nodeUri) {
        
        
        JsonObject responseJsonObject =
                jerseyHelper.postRequestWithUri(
                    nodeUri,
                    corpusStructureServiceLocation,
                    corpusStructureServiceCrawlerPath,
                    corpusStructureServiceCrawlerStartPath);
        
        String crawlId = jsonTransformationHandler.getCrawlIdFromJsonObject(responseJsonObject);
        
        
        //TODO CHECK IF OK??
        
        //TODO RETURN
        
//        return crawlId;
    }
    
}
