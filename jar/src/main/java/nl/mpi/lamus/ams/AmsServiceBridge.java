package nl.mpi.lamus.ams;

import java.net.URI;
import java.util.Collection;
import nl.mpi.lamus.workspace.model.WorkspaceNodeReplacement;

/**
 * Interface encapsulating AMS (Access Management System) related functionalities.
 * Partly adapted from the old LAMUS.
 * @author guisil
 */
public interface AmsServiceBridge {

    /**
     * provides the used-storage-space of the given user(uid) on the given archive node
     * @param uid destined user's uid
     * @param archiveNodeURI URI of the archive ndoe
     * @return  the used-storage-space of the given user(uid) on the given resource(nodeIdStr)
     */
    public long getUsedStorageSpace(String uid, URI archiveNodeURI);
    
    /**
     * provides the maximum-storage-space of the given user(uid) on the given archive node
     * @param uid destined user's uid
     * @param archiveNodeURI URI of the archive node
     * @return maximum-storage-space of the given user(uid) on the given resource(nodeIdStr)
     */
    public long getMaxStorageSpace(String uid, URI archiveNodeURI);
    
    /**
     * Retrieves the email address for the given user ID.
     * @param uid ID of the user
     * @return Email address of the user
     */
    public String getMailAddress(String uid);

    /**
     * Triggers the recalculation of the resource access rights for the updated part of the archive,
     * propagate them to the Apache htaccess file and signal the webserver.
     * 
     * @param topNodes Collection of URIs of the nodes to be recalculated
     */
    public void triggerAccessRightsRecalculation(Collection<URI> topNodes);
    
    /**
     * Triggers the recalculation of the resource access rights for the updated part of the archive
     * and propagates them to the Apache htaccess file and signal the webserver.
     * This particular recalculation includes the nodes which were replaced
     * in the workspace.
     * So a complete recalculation is triggered for the top node and then
     * another one for the versioned nodes.
     * 
     * @param topNodes Collection of URIs of the nodes to be recalculated
     * @param nodeReplacements Collection of node replacements
     */
    public void triggerAccessRightsRecalculationWithVersionedNodes(Collection<URI> topNodeURIs, Collection<WorkspaceNodeReplacement> nodeReplacements);
    
    /**
     * Triggers the node replacements in AMS, specifically copying and setting
     * the appropriate rules for the replaced nodes and their new versions.
     * 
     * @param nodeReplacements Collection of node replacements
     * @param userID ID of the current user
     */
    public void triggerAmsNodeReplacements(Collection<WorkspaceNodeReplacement> nodeReplacements, String userID);

}