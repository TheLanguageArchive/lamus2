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
package nl.mpi.lamus.ams.implementation;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.corpusstructure.NodeIdUtils;
import nl.mpi.lat.ams.AmsLicense;
import nl.mpi.lat.ams.AmsLicenseFactory;
import nl.mpi.lat.ams.IAmsRemoteService;
import nl.mpi.lat.ams.model.License;
import nl.mpi.lat.ams.service.LicenseService;
import nl.mpi.lat.auth.authorization.AdvAuthorizationService;
import nl.mpi.lat.auth.principal.LatUser;
import nl.mpi.lat.auth.principal.PrincipalService;
import nl.mpi.lat.fabric.FabricService;
import nl.mpi.lat.fabric.NodeID;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author guisil
 */
@Component
public class AmsFakeRemoteService implements IAmsRemoteService {
    
    @Autowired
    private LicenseService licenseService;
    @Autowired
    private PrincipalService principalService;
    @Autowired
    private FabricService fabricService;
    @Autowired
    private AdvAuthorizationService authorizationService;

    @Autowired
    private CorpusStructureProvider corpusStructureProvider;
    @Autowired
    private NodeResolver nodeResolver;
    @Autowired
    private AmsFakeRemoteServiceHelper remoteServiceHelper;
    
    
    @Override
    public List<AmsLicense> getLicenseAcceptance(String nodeid) {
        
        List<AmsLicense> amsLicenses = new ArrayList<>();
        
        NodeID mpiNodeId = fabricService.newNodeID(NodeIdUtils.TONODEID(Integer.parseInt(nodeid)));
        List<License> licenses = licenseService.getLicenses(mpiNodeId);
        for (License license : licenses) {
            AmsLicense amsLicense = AmsLicenseFactory.getNewAmsLicense();
            try {
                BeanUtils.copyProperties(amsLicense, license);
                amsLicense.setLinkToLicense(licenseService.getLicenseLink(license));
                amsLicenses.add(amsLicense);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return amsLicenses;
    }

    @Override
    public String getUserEmailAddress(String userid) {
        LatUser user = principalService.getUser(userid);
        return user.getEmail();
    }

    @Override
    public void replaceNodesWithDefaultAccessRules(Map<URI, URI> replacements, String userid) {
        
        Set<Map.Entry<URI, URI>> replacementEntries = replacements.entrySet();
        
        for(Map.Entry<URI, URI> entry : replacementEntries) {
            
            try {
                CorpusNode oldNode = corpusStructureProvider.getNode(entry.getKey());
                String oldNodeStringID = NodeIdUtils.TONODEID(Integer.parseInt(nodeResolver.getId(oldNode)));
                CorpusNode newNode = corpusStructureProvider.getNode(entry.getValue());
                String newNodeStringID = NodeIdUtils.TONODEID(Integer.parseInt(nodeResolver.getId(newNode)));
            
                NodeID oldNodeID = fabricService.newNodeID(oldNodeStringID);
                NodeID newNodeID = fabricService.newNodeID(newNodeStringID);

                LatUser user = principalService.getUser(userid);
            
                authorizationService.performReplaceActionsOnNode(oldNodeID, newNodeID, user);
                
            } catch (RuntimeException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    @Override
    public void triggerRightsRecalculation(Collection<URI> nodeURIs, boolean triggerCorpusStructureTranscription, boolean triggerWebServerTranscription) {
        
        // converting the URI collection into a string with appended node IDs
        String targetNodeIDs = remoteServiceHelper.getTargetNodeIDsAsString(nodeURIs);
        
        //TODO force recalculation?
        try {
            URL urlToTriggerRecalc = remoteServiceHelper.getRecalcUrl(triggerCorpusStructureTranscription, triggerWebServerTranscription, targetNodeIDs);
            remoteServiceHelper.sendCallToAccessRightsManagementSystem(urlToTriggerRecalc);
        } catch (UnsupportedEncodingException | MalformedURLException ex) {
            throw new RuntimeException("Error constructing AMS recalculation URL", ex);
        } catch (IOException ex) {
            throw new RuntimeException("Error invoking AMS rights recalculation", ex);
        }
    }
    
    
    
    
    
}
