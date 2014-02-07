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
import nl.mpi.lamus.metadata.MetadataApiBridge;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.HeaderInfo;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.cmdi.api.CMDIConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see MetadataApiBridge
 * 
 * @author guisil
 */
@Component
public class LamusMetadataApiBridge implements MetadataApiBridge {

    private static final Logger logger = LoggerFactory.getLogger(LamusMetadataApiBridge.class);
    
    private MetadataAPI metadataAPI;
    
    
    @Autowired
    public LamusMetadataApiBridge(MetadataAPI mdApi) {
        this.metadataAPI = mdApi;
    }
    
    /**
     * @see MetadataApiBridge#getSelfHandleFromFile(java.net.URL)
     */
    @Override
    public URI getSelfHandleFromFile(URL fileURL) {
        MetadataDocument document;
        try {
            document = metadataAPI.getMetadataDocument(fileURL);
        } catch (IOException ex) {
            logger.warn("Error retrieving metadata document for URL " + fileURL, ex);
            return null;
        } catch (MetadataException ex) {
            logger.warn("Error retrieving metadata document for URL " + fileURL, ex);
            return null;
        }
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
     * @see MetadataApiBridge#getNewSelfHandleHeaderInfo(java.net.URI)
     */
    @Override
    public HeaderInfo getNewSelfHandleHeaderInfo(URI handle) {
        return new HeaderInfo(CMDIConstants.CMD_HEADER_MD_SELF_LINK, handle.toString());
    }
    
}
