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
package nl.mpi.lamus.metadata.implementation;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.handle.util.HandleManager;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.metadata.MetadataApiBridge;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.HandleCarrier;
import nl.mpi.metadata.api.model.HeaderInfo;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.cmdi.api.CMDIConstants;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @see MetadataApiBridge
 * 
 * @author guisil
 */
@Component
public class LamusMetadataApiBridge implements MetadataApiBridge {

    private static final Logger logger = LoggerFactory.getLogger(LamusMetadataApiBridge.class);
    
    private final MetadataAPI metadataAPI;
    private final WorkspaceFileHandler workspaceFileHandler;
    private final HandleManager handleManager;
    
    @Autowired
    public LamusMetadataApiBridge(MetadataAPI mdApi, WorkspaceFileHandler wsFileHandler, HandleManager hdlManager) {
        this.metadataAPI = mdApi;
        this.workspaceFileHandler = wsFileHandler;
        this.handleManager = hdlManager;
    }
    
    /**
     * @see MetadataApiBridge#getSelfHandleFromFile(java.net.URL)
     */
    @Override
    public URI getSelfHandleFromFile(URL fileURL) {
        
        logger.debug("Retrieving self handle from metadata file; fileUrl: " + fileURL);
        
        MetadataDocument document;
        try {
            document = metadataAPI.getMetadataDocument(fileURL);
        } catch (IOException | MetadataException ex) {
            logger.warn("Error retrieving metadata document for URL " + fileURL, ex);
            return null;
        }
        
        return getSelfHandleFromDocument(document);
    }

    /**
     * @see MetadataApiBridge#getSelfHandleFromDocument(nl.mpi.metadata.api.model.MetadataDocument)
     */
    @Override
    public URI getSelfHandleFromDocument(MetadataDocument document) {
        
        logger.debug("Retrieving self handle from document: " + document.getFileLocation());
        
        HeaderInfo selfLink = document.getHeaderInformation(CMDIConstants.CMD_HEADER_MD_SELF_LINK);
        if(selfLink == null) {
            return null;
        } else {
            try {
                return new URI(selfLink.getValue());
            } catch (URISyntaxException ex) {
                logger.warn("Error creating URI for handle " + selfLink.getValue(), ex);
                return null;
            }
        }
    }

    /**
     * @see MetadataApiBridge#addSelfHandleAndSaveDocument(nl.mpi.metadata.api.model.MetadataDocument, java.net.URI, java.net.URL)
     */
    @Override
    public void addSelfHandleAndSaveDocument(MetadataDocument document, URI handleUri, URL targetLocation) throws URISyntaxException, MetadataException, IOException, TransformerException {
        
        logger.debug("Adding self handle with URI '{}' in metadata document '{}'", handleUri, targetLocation);
        
        HeaderInfo newInfo = getNewSelfHandleHeaderInfo(handleManager.prepareHandleWithHdlPrefix(handleUri));
        document.putHeaderInformation(newInfo);
        saveMetadataDocument(document, targetLocation);
    }
    
    /**
     * @see MetadataApiBridge#removeSelfHandleAndSaveDocument(java.net.URL)
     */
    @Override
    public void removeSelfHandleAndSaveDocument(URL fileURL) throws IOException, TransformerException, MetadataException {
        
        MetadataDocument document = metadataAPI.getMetadataDocument(fileURL);

        removeSelfHandleAndSaveDocument(document, fileURL);
    }

    /**
     * @see MetadataApiBridge#removeSelfHandleAndSaveDocument(nl.mpi.metadata.api.model.MetadataDocument, java.net.URL)
     */
    @Override
    public void removeSelfHandleAndSaveDocument(MetadataDocument document, URL targetLocation) throws IOException, TransformerException, MetadataException {
       
        logger.debug("Removing self handle from metadata document '{}'", targetLocation);
        
        if(document instanceof HandleCarrier) {
            HandleCarrier documentWithHandle = (HandleCarrier) document;
            documentWithHandle.setHandle(null);
            saveMetadataDocument((MetadataDocument) documentWithHandle, targetLocation);
        } else {
            logger.debug("Document in '{}' doesn't contain a self handle", targetLocation);
        }
    }

    /**
     * @see MetadataApiBridge#getNewSelfHandleHeaderInfo(java.net.URI)
     */
    @Override
    public HeaderInfo getNewSelfHandleHeaderInfo(URI handle) {
        return new HeaderInfo(CMDIConstants.CMD_HEADER_MD_SELF_LINK, handle.toString());
    }

    /**
     * @see MetadataApiBridge#saveMetadataDocument(nl.mpi.metadata.api.model.MetadataDocument, java.net.URL)
     */
    @Override
    public void saveMetadataDocument(MetadataDocument document, URL targetURL) throws IOException, TransformerException, MetadataException {
        
        logger.debug("Saving metadata document; targetUrl: " + targetURL);
        
        StreamResult documentStreamResult = workspaceFileHandler.getStreamResultForNodeFile(FileUtils.toFile(targetURL));
        metadataAPI.writeMetadataDocument(document, documentStreamResult);
    }

    /**
     * @see MetadataApiBridge#isMetadataFileValid(java.net.URL)
     */
    @Override
    public boolean isMetadataFileValid(URL fileURL) {
        
        logger.debug("Validating metadata file [" + fileURL + "]");
        
        MetadataDocument document;
        try {
            document = metadataAPI.getMetadataDocument(fileURL);
        } catch (IOException | MetadataException ex) {
            logger.info("Error getting document from file [" + fileURL + "]", ex);
            return false;
        }
        
        try {
            metadataAPI.validateMetadataDocument(document, new DefaultHandler());
        } catch(SAXException ex) {
            logger.info("Validation error in file [" + fileURL + "]", ex);
            return false;
        }
        
        logger.debug("Metadata file [" + fileURL + "] is valid");
        return true;
    }
}
