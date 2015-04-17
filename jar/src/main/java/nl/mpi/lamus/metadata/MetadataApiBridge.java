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
package nl.mpi.lamus.metadata;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.xml.transform.TransformerException;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.HeaderInfo;
import nl.mpi.metadata.api.model.MetadataDocument;

/**
 * Provides some functionality that interacts with the Metadata API.
 * 
 * @author guisil
 */
public interface MetadataApiBridge {
    
    /**
     * Retrieves the self link (handle) from the given file, if it has one.
     * 
     * @param fileURL URL of the file to check
     * @return URI corresponding to the self link, null if none is found
     */
    public URI getSelfHandleFromFile(URL fileURL);
    
    /**
     * Retrieves the self link (handle) from the given MetadataDocument, if it has one.
     * 
     * @param document MetadataDocument to check
     * @return URI corresponding to the self link, null if none is found
     */
    public URI getSelfHandleFromDocument(MetadataDocument document);
    
    /**
     * Adds a self handle in the given document, given the intended URI
     * and saves the document in the given location.
     * @param document MetadataDocument object
     * @param handleUri intended URI for the handle
     * @param targetLocation location where the document should be saved
     */
    public void addSelfHandleAndSaveDocument(MetadataDocument document, URI handleUri, URL targetLocation)
            throws URISyntaxException, MetadataException, IOException, TransformerException;
    
    /**
     * Removes the self link (handle) from the given file, if it has one,
     * and saves the document in the end.
     * 
     * @param fileURL URL of the file to have the self link removed
     */
    public void removeSelfHandleAndSaveDocument(URL fileURL)
            throws IOException, TransformerException, MetadataException;
    
    /**
     * Removes the self link (handle) from the given document, if it has one,
     * and saves it in the end.
     * 
     * @param document MetadataDocument to have the self link removed
     * @param targetLocation location where the file should be saved
     */
    public void removeSelfHandleAndSaveDocument(MetadataDocument document, URL targetLocation)
            throws IOException, TransformerException, MetadataException;    
    /**
     * Creates a new HeaderInfo object containing the given handle
     * @param handle
     * @return HeaderInfo object containing the handle
     */
    public HeaderInfo getNewSelfHandleHeaderInfo(URI handle);
    
    /**
     * Saves given document in the given location
     * @param document document to save
     * @param targetURL location where the file should be saved
     */
    public void saveMetadataDocument(MetadataDocument document, URL targetURL)
            throws IOException, TransformerException, MetadataException;
    
    /**
     * Validates the given metadata file.
     * @param fileURL URL of the file to check
     * @return true if the file is valid
     */
    public boolean isMetadataFileValid(URL fileURL);
    
    /**
     * Validates the given metadata document.
     * @param document MetadataDocument to check
     * @return true if the document is valid
     */
    public boolean isMetadataDocumentValid(MetadataDocument document);
}
