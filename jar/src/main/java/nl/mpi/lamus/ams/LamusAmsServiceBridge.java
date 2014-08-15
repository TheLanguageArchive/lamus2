package nl.mpi.lamus.ams;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import nl.mpi.lamus.workspace.model.WorkspaceNodeReplacement;
import nl.mpi.lat.ams.IAmsRemoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see AmsServiceBridge
 * @author guisil
 */
@Component
public class LamusAmsServiceBridge implements AmsServiceBridge {
    
    private final static Logger logger = LoggerFactory.getLogger(LamusAmsServiceBridge.class);

    @Autowired
    private IAmsRemoteService amsRemoteService;
    
    
    /**
     * @see AmsServiceBridge#getUsedStorageSpace(java.lang.String, java.net.URI)
     */
    @Override
    public long getUsedStorageSpace(String uid, URI archiveNodeURI) {
        
        //TODO does it make sense to still use this?
        
        return 0;
    }

    /**
     * @see AmsServiceBridge#getMaxStorageSpace(java.lang.String, java.net.URI)
     */
    @Override
    public long getMaxStorageSpace(String uid, URI archiveNodeURI) {
        
        //TODO does it make sense to still use this?
        
        return 1024 * 1024 * 1024;
    }
    
    /**
     * @see lams.ams.AmsServiceBridge#getMailAddress(java.lang.String)
     */
    @Override
    public String getMailAddress(String uid) {
        
        logger.debug("Getting email address for user {}", uid);
        
        return amsRemoteService.getUserEmailAddress(uid);
    }

    /**
     * @see AmsServiceBridge#triggerAccessRightsRecalculation(java.net.URI)
     */
    @Override
    public void triggerAccessRightsRecalculation(URI topNode) {
        
        logger.debug("Triggering access rights recalculation for node {}", topNode);
        
        Collection<URI> targetURIs = new ArrayList<>();
        targetURIs.add(topNode);
        
        amsRemoteService.triggerRightsRecalculation(targetURIs, true, true);
    }
    
    /**
     * @see AmsServiceBridge#triggerAccessRightsRecalculationWithVersionedNodes(java.net.URI, java.util.Collection)
     */
    @Override
    public void triggerAccessRightsRecalculationWithVersionedNodes(URI topNode, Collection<WorkspaceNodeReplacement> nodeReplacements) {
        
        logger.debug("Triggering access rights recalculation for top node and versioned nodes");
        
        Collection<URI> versionedNodes = new ArrayList<>();
        for(WorkspaceNodeReplacement replacement : nodeReplacements) {
            versionedNodes.add(replacement.getOldNodeURI());
        }
        
        amsRemoteService.triggerRightsRecalculationWithVersionedNodes(topNode, versionedNodes);
    }

    /**
     * @see AmsServiceBridge#triggerAccessRightsRecalculationForVersionedNodes(java.util.Collection, java.net.URI)
     */
    @Override
    public void triggerAccessRightsRecalculationForVersionedNodes(Collection<WorkspaceNodeReplacement> nodeReplacements, URI topNode) {
        
        logger.debug("Triggering access rights recalculation for versioned nodes");
        
        Collection<URI> versionedNodes = new ArrayList<>();
        for(WorkspaceNodeReplacement replacement : nodeReplacements) {
            versionedNodes.add(replacement.getOldNodeURI());
        }
        
        amsRemoteService.triggerRightsRecalculationForVersionedNodes(versionedNodes, true, false);
        
        Collection<URI> targetURIs = new ArrayList<>();
        targetURIs.add(topNode);
        
        amsRemoteService.triggerRightsRecalculation(targetURIs, false, true);
    }

    /**
     * @see AmsServiceBridge#triggerAmsNodeReplacements(java.util.Collection, java.lang.String)
     */
    @Override
    public void triggerAmsNodeReplacements(Collection<WorkspaceNodeReplacement> nodeReplacements, String userID) {
        
        logger.debug("Triggering AMS node replacements");
        
        Map<URI, URI> replacementsMap = new HashMap<>();
        
        for(WorkspaceNodeReplacement replacement : nodeReplacements) {
            replacementsMap.put(replacement.getOldNodeURI(), replacement.getNewNodeURI());
        }
        
        amsRemoteService.replaceNodesWithDefaultAccessRules(replacementsMap, userID);
    }

}
