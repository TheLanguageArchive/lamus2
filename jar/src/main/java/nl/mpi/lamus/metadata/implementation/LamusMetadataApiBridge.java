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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.handle.util.HandleManager;
import nl.mpi.lamus.cmdi.profile.AllowedCmdiProfiles;
import nl.mpi.lamus.cmdi.profile.CmdiProfile;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.metadata.MetadataApiBridge;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataElementException;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.HandleCarrier;
import nl.mpi.metadata.api.model.HeaderInfo;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.api.model.MetadataElement;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import nl.mpi.metadata.cmdi.api.CMDIConstants;
import nl.mpi.metadata.cmdi.api.model.CMDIContainerMetadataElement;
import nl.mpi.metadata.cmdi.api.model.CMDIMetadataElement;
import nl.mpi.metadata.cmdi.api.model.CMDIMetadataElementFactory;
import nl.mpi.metadata.cmdi.api.model.ResourceProxy;
import nl.mpi.metadata.cmdi.api.type.ComponentType;
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
    private final CMDIMetadataElementFactory metadataElementFactory;
    
    private final AllowedCmdiProfiles allowedCmdiProfiles;
    
    
    @Autowired
    public LamusMetadataApiBridge(MetadataAPI mdApi,
            WorkspaceFileHandler wsFileHandler, HandleManager hdlManager,
            CMDIMetadataElementFactory mdElementFactory, AllowedCmdiProfiles profiles) {
        this.metadataAPI = mdApi;
        this.workspaceFileHandler = wsFileHandler;
        this.handleManager = hdlManager;
        this.metadataElementFactory = mdElementFactory;
        this.allowedCmdiProfiles = profiles;
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
        
        return isMetadataDocumentValid(document);
    }

    /**
     * @see MetadataApiBridge#isMetadataDocumentValid(nl.mpi.metadata.api.model.MetadataDocument)
     */
    @Override
    public boolean isMetadataDocumentValid(MetadataDocument document) {
        
        try {
            metadataAPI.validateMetadataDocument(document, new DefaultHandler());
        } catch(SAXException ex) {
            logger.info("Validation error in file [" + document.getFileLocation() + "]", ex);
            return false;
        }
        
        logger.debug("Metadata file [" + document.getFileLocation() + "] is valid");
        return true;
    }

    /**
     * @see MetadataApiBridge#isMetadataReferenceAllowedInProfile(java.net.URI)
     */
    @Override
    public boolean isMetadataReferenceAllowedInProfile(URI profileLocation) {
        
        return isReferenceTypeAllowedInProfile(MetadataReferenceType.REFERENCE_TYPE_METADATA, profileLocation);
    }

    /**
     * @see MetadataApiBridge#isResourceReferenceAllowedInProfile(java.net.URI)
     */
    @Override
    public boolean isResourceReferenceAllowedInProfile(URI profileLocation) {
        
        return isReferenceTypeAllowedInProfile(MetadataReferenceType.REFERENCE_TYPE_RESOURCE, profileLocation);
    }

    /**
     * @see MetadataApiBridge#isReferenceTypeAPage(nl.mpi.metadata.api.model.Reference)
     */
    @Override
    public boolean isReferenceTypeAPage(Reference reference) {
        
        return  MetadataReferenceType.REFERENCE_TYPE_LANDING_PAGE.equals(reference.getType()) ||
                MetadataReferenceType.REFERENCE_TYPE_SEARCH_PAGE.equals(reference.getType()) ||
                MetadataReferenceType.REFERENCE_TYPE_SEARCH_SERVICE.equals(reference.getType());
    }

    /**
     * @see MetadataApiBridge#getComponentPathForProfileAndReferenceType(java.net.URI, java.lang.String, boolean)
     */
    @Override
    public String getComponentPathForProfileAndReferenceType(URI profileLocation, String referenceType, boolean isInfoLink) {
        
        CmdiProfile matchedProfile = getProfileWithLocation(profileLocation);
        
        if(matchedProfile != null) {
            Map<String, String> componentMap = matchedProfile.getComponentMap();
            if(componentMap != null && !componentMap.isEmpty()) {
                Set<Map.Entry<String, String>> entrySet = componentMap.entrySet();
                for(Map.Entry<String, String> entry : entrySet) {
                    String typeToCheck = referenceType;
                    if(isInfoLink) {
                        typeToCheck = "info";
                    }
                    if(Pattern.matches(entry.getKey(), typeToCheck)) {
                        return entry.getValue();
                    }
                }
            }
        }
        
        String message = "CMDI Profile [" + (matchedProfile != null ? matchedProfile.getId() : "null") + "] has no component types configured. Reference will not be added to parent.";
        logger.info(message);
        return null;
    }
    
    /**
     * @see MetadataApiBridge#createComponentPathWithin(nl.mpi.metadata.cmdi.api.model.CMDIContainerMetadataElement, java.lang.String)
     */
    @Override
    public CMDIContainerMetadataElement createComponentPathWithin(CMDIContainerMetadataElement root, String path)
            throws MetadataElementException {
        
        CMDIMetadataElement child;
        
        String[] elementNames = path.split("/");
        
        String componentName = elementNames[elementNames.length - 1];
        
        CMDIContainerMetadataElement currentParent = root;
        ComponentType currentType = root.getType();
        for(String elementName : elementNames) {
            if(elementName.isEmpty() || elementName.equals(root.getName())) {
                continue;
            }
            currentType = (ComponentType) currentType.getType(elementName);
            if(!componentName.equals(elementName)) {
                child = currentParent.getChildElement(elementName);
            } else {
                child = null;
            }
            
            if(child == null) {
                child = metadataElementFactory.createNewMetadataElement(currentParent, currentType);
                currentParent.addChildElement(child);
            }
            if(child instanceof CMDIContainerMetadataElement) {
                currentParent = (CMDIContainerMetadataElement) child;
            } else {
                throw new IllegalArgumentException("Element " + child + " is not an instance of CMDIContainerMetadataElement");
            }
        }
        
        return currentParent;
    }
    
    /**
     * @see MetadataApiBridge#addReferenceInComponent(nl.mpi.metadata.cmdi.api.model.CMDIContainerMetadataElement, nl.mpi.metadata.cmdi.api.model.ResourceProxy)
     */
    @Override
    public ResourceProxy addReferenceInComponent(CMDIContainerMetadataElement component, ResourceProxy resourceProxy) {
        
        return component.addDocumentResourceProxyReference(resourceProxy.getId());
    }

    /**
     * @see MetadataApiBridge#isReferenceAnInfoLink(nl.mpi.metadata.api.model.ReferencingMetadataDocument, nl.mpi.metadata.api.model.Reference)
     */
    @Override
    public boolean isReferenceAnInfoLink(ReferencingMetadataDocument document, Reference reference) {
        
        Collection<MetadataElement> refElements = document.getResourceProxyReferences(reference);
        for(MetadataElement el : refElements) {
            if(el.getType() != null && MetadataComponentType.COMPONENT_TYPE_INFO_LINK.equals(el.getType().getName())) { //info file
                return true;
            }
        }
        return false;
    }

    /**
     * @see MetadataApiBridge#isInfoLinkAllowedInProfile(java.net.URI)
     */
    @Override
    public boolean isInfoLinkAllowedInProfile(URI profileLocation) {
        
        CmdiProfile profile = getProfileWithLocation(profileLocation);
        return profile.getAllowInfoLinks();
    }
    
    
    private boolean isReferenceTypeAllowedInProfile(String referenceTypeToCheck, URI profileLocation) {
        
        CmdiProfile profile = getProfileWithLocation(profileLocation);
        List<String> allowedReferenceTypes = profile.getAllowedReferenceTypes();
        if(allowedReferenceTypes.contains(referenceTypeToCheck)) {
            return true;
        }
                
        return false;
    }
    
    private CmdiProfile getProfileWithLocation(URI profileLocation) {
        
        List<CmdiProfile> allowedProfiles = allowedCmdiProfiles.getProfiles();
        
        for(CmdiProfile profile : allowedProfiles) {
            if(profileLocation.toString().contains(profile.getId())) {
                return profile;
            }
        }
        return null;
    }
}
