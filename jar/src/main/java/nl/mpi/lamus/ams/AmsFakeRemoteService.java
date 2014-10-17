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
package nl.mpi.lamus.ams;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.corpusstructure.NodeIdUtils;
import nl.mpi.lat.ams.AmsLicense;
import nl.mpi.lat.ams.IAmsRemoteService;
import nl.mpi.lat.ams.export.RecalcTriggerService;
import nl.mpi.lat.ams.model.License;
import nl.mpi.lat.ams.service.LicenseService;
import nl.mpi.lat.auth.authorization.AdvAuthorizationService;
import nl.mpi.lat.auth.authorization.export.AuthorizationExportService;
import nl.mpi.lat.auth.principal.LatUser;
import nl.mpi.lat.auth.principal.PrincipalService;
import nl.mpi.lat.fabric.FabricService;
import nl.mpi.lat.fabric.NodeID;
import nl.mpi.latimpl.fabric.NodeIDImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    @Qualifier("integratedExportSrv")
    private AuthorizationExportService integratedExportService;
    @Autowired
    @Qualifier("cachedCorpusDbExportSrv")
    private AuthorizationExportService cachedCorpusDbExportService;
    @Autowired
    @Qualifier("webserverExportSrv")
    private AuthorizationExportService webserverExportService;
    @Autowired
    private RecalcTriggerService recalcTriggerService;

    @Autowired
    private CorpusStructureProvider corpusStructureProvider;
    @Autowired
    private NodeResolver nodeResolver;

    
    @Override
    public List<AmsLicense> getLicenseAcceptance(String nodeid) {
        
        List<AmsLicense> amsLicenses = new ArrayList<>();
        
        int nodeIdNum = Integer.parseInt(nodeid);
        
        NodeID mpiNodeId = new NodeIDImpl(nodeIdNum);
        List<License> licenses = licenseService.getLicenses(mpiNodeId);
        for (License license : licenses) {
            AmsLicense amsLicense = new AmsLicense();
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
//                String oldNodeStringID = temporary.getStringNodeIdForVersionedURI(entry.getKey());
                CorpusNode oldNode = corpusStructureProvider.getNode(entry.getKey());
                String oldNodeStringID = NodeIdUtils.TONODEID(Integer.parseInt(nodeResolver.getId(oldNode)));
//                String newNodeStringID = temporary.getStringNodeIdForURI(entry.getValue().toString());
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
        
        // selecting one of the available authorization export services
        AuthorizationExportService selectedExportService = selectExportService(triggerCorpusStructureTranscription, triggerWebServerTranscription);
        
        // converting the URI collection into a set of node IDs
        Set<NodeID> targetNodeIDs = new HashSet<>();
        for(URI nodeURI : nodeURIs) {
//            String nodeID = temporary.getStringNodeIdForURI(nodeURI.toString());
            CorpusNode node = corpusStructureProvider.getNode(nodeURI);
            String nodeID = NodeIdUtils.TONODEID(Integer.parseInt(nodeResolver.getId(node)));
            targetNodeIDs.add(fabricService.newNodeID(nodeID));
        }
        
        
        //TODO force recalculation?
        
        recalcTriggerService.triggerRecalculation(selectedExportService, targetNodeIDs, false);
    }

    @Override
    public void triggerRightsRecalculationWithVersionedNodes(URI topNodeURI, Collection<URI> versionedNodeURIs) {
        
        // converting the URI collection into a set of node IDs
        Set<NodeID> targetNodeIDs = new LinkedHashSet<>();
//        String topNodeID = temporary.getStringNodeIdForURI(topNode.toString());
        CorpusNode topNode = corpusStructureProvider.getNode(topNodeURI);
        String topNodeID = NodeIdUtils.TONODEID(Integer.parseInt(nodeResolver.getId(topNode)));
        targetNodeIDs.add(fabricService.newNodeID(topNodeID));
        
        recalcTriggerService.triggerRecalculation(integratedExportService, targetNodeIDs, false);
        
        Set<NodeID> versionedNodeIDs = new LinkedHashSet<>();
        for(URI currentNodeURI : versionedNodeURIs) {
//            String nodeID = temporary.getStringNodeIdForVersionedURI(currentNode);
            CorpusNode currentNode = corpusStructureProvider.getNode(currentNodeURI);
            String currentNodeID = NodeIdUtils.TONODEID(Integer.parseInt(nodeResolver.getId(currentNode)));
            versionedNodeIDs.add(fabricService.newNodeID(currentNodeID));
        }
        
        //TODO force recalculation?
        
        recalcTriggerService.triggerRecalculation(integratedExportService, versionedNodeIDs, false);
    }

    @Override
    public void triggerRightsRecalculationForVersionedNodes(Collection<URI> nodeURIs, boolean triggerCorpusStructureTranscription, boolean triggerWebServerTranscription) {
        
        // selecting one of the available authorization export services
        AuthorizationExportService selectedExportService = selectExportService(triggerCorpusStructureTranscription, triggerWebServerTranscription);
        
        // converting the URI collection into a set of node IDs
        Set<NodeID> targetNodeIDs = new HashSet<>();
        for(URI nodeURI : nodeURIs) {
//            String nodeID = temporary.getStringNodeIdForVersionedURI(nodeURI);
            CorpusNode node = corpusStructureProvider.getNode(nodeURI);
            String nodeID = NodeIdUtils.TONODEID(Integer.parseInt(nodeResolver.getId(node)));
            targetNodeIDs.add(fabricService.newNodeID(nodeID));
        }
        
        //TODO force recalculation?
        
        recalcTriggerService.triggerRecalculation(selectedExportService, targetNodeIDs, false);
    }
    
    private AuthorizationExportService selectExportService(boolean triggerCorpusStructureTranscription, boolean triggerWebServerTranscription) {
        
        if(triggerCorpusStructureTranscription && triggerWebServerTranscription) {
            return integratedExportService;
        } else if(triggerCorpusStructureTranscription) {
            return cachedCorpusDbExportService;
        } else if(triggerWebServerTranscription) {
            return webserverExportService;
        } else {
            throw new IllegalArgumentException("Both 'triggerCorpusStructureTranscription' and 'triggerWebServerTranscription' are false. At least one should be true.");
        }
    }
    
}
