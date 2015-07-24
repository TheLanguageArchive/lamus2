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
import nl.mpi.metadata.api.MetadataElementException;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.HeaderInfo;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import nl.mpi.metadata.cmdi.api.model.CMDIContainerMetadataElement;
import nl.mpi.metadata.cmdi.api.model.ResourceProxy;

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
            throws MetadataException, IOException, TransformerException;
    
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
    
    /**
     * Given a Metadata profile, it checks if a Metadata reference is allowed.
     * @param profileLocation Profile to check
     * @return true if a Metadata reference is allowed in the profile
     */
    public boolean isMetadataReferenceAllowedInProfile(URI profileLocation);
    
    /**
     * Given a Metadata profile, it checks if a Resource reference is allowed.
     * @param profileLocation Profile to check
     * @return true if a Resource reference is allowed in the profile
     */
    public boolean isResourceReferenceAllowedInProfile(URI profileLocation);
    
    /**
     * Checks if the type of the given reference is among the ones that point
     * to pages (not Metadata or Resource)
     * @param reference Reference to check
     * @return true if reference type is "LandingPage", "SearchPage" or "SearchService"
     */
    public boolean isReferenceTypeAPage(Reference reference);
    
    /**
     * Given a Metadata profile and a reference type, retrieves the appropriate component path.
     * @param profileLocation Profile to check
     * @param referenceType Reference type to check
     * @param isInfoLink true if the reference should refer to an info link
     * @return Appropriate component path for the given parameters (null if reference is not to be enforced)
     */
    public String getComponentPathForProfileAndReferenceType(URI profileLocation, String referenceType, boolean isInfoLink);
    
    /**
     * Creates a component within the given element. If other elements in the path
     * are missing, they're also created.
     * @param root Element to check
     * @param path Relative path to the component
     * @return Element corresponding to the created component
     */
    public CMDIContainerMetadataElement createComponentPathWithin(CMDIContainerMetadataElement root, String path)
            throws MetadataElementException;
    
    /**
     * Given a Metadata document and the name of a component, it adds the
     * appropriate reference to the component, creating the component if needed.
     * @param component Metadata document to edit
     * @param resourceProxy  Resource proxy to reference
     * @return Created reference
     */
    public ResourceProxy addReferenceInComponent(CMDIContainerMetadataElement component, ResourceProxy resourceProxy);
    
    /**
     * Given a metadata document and a reference in that document, checks if
     * it refers to an info link.
     * @param document Metadata document where the reference belongs
     * @param reference Reference to check
     * @return true if the reference corresponds to an info link
     */
    public boolean isReferenceAnInfoLink(ReferencingMetadataDocument document, Reference reference);
    
    /**
     * Checks if the given profile allows the creation of info links.
     * @param profileLocation Profile to check
     * @return true if the given profile allows info links to be created
     */
    public boolean isInfoLinkAllowedInProfile(URI profileLocation);
}
