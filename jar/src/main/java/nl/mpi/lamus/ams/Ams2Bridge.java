package nl.mpi.lamus.ams;

//import java.io.BufferedReader;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.URLConnection;
//import java.net.URLEncoder;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.Set;
//import nl.mpi.common.util.Text;
//import nl.mpi.common.util.spring.SpringContextLoader;
//import nl.mpi.corpusstructure.NodeIdUtils;
//import nl.mpi.corpusstructure.UnknownNodeException;
//import nl.mpi.lat.ams.Constants;
//import nl.mpi.lat.ams.model.NodeAuth;
//import nl.mpi.lat.ams.model.NodeLicense;
//import nl.mpi.lat.ams.model.NodePcplLicense;
import java.net.URI;
//import nl.mpi.lat.ams.model.NodePcplRule;
//import nl.mpi.lat.ams.service.LicenseService;
//import nl.mpi.lat.ams.service.RuleService;
//import nl.mpi.lat.auth.authentication.AuthenticationService;
//import nl.mpi.lat.auth.authorization.AdvAuthorizationService;
//import nl.mpi.lat.auth.principal.LatPrincipal;
//import nl.mpi.lat.auth.principal.PrincipalService;
//import nl.mpi.lat.fabric.FabricService;
//import nl.mpi.lat.fabric.NodeID;
//import nl.mpi.latimpl.core.LatServiceImpl;
import nl.mpi.util.OurURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * $Id$
 *
 * implementation of AmsBridge using ams2 api
 *
 * @author	last modified by $Author$, created by mategg
 * @version	$Revision$
 */
@Component
public class Ams2Bridge implements AmsBridge { // extends LatServiceImpl {
    
    private final static Logger logger = LoggerFactory.getLogger(Ams2Bridge.class);

    
    private String baseURL;
    private String recalcURL;
    private String recalcParam;


    
    
    /**
     * @see lams.ams.AmsBridge#getStatus()
     */
    @Override
    public boolean getStatus() {
//        return this.getAuthorizationSrv() != null
//                && this.getAuthenticationSrv() != null
//                && this.getPrincipalSrv() != null;
        
        return true;
    }
//    
//    public String getBaseURL() {
//        return this.baseURL;
//    }
//
//    public void setBaseURL(String baseURL) {
//        this.baseURL = baseURL;
//    }
//    
//    public String getRecalcURL() {
//        return this.recalcURL;
//    }
//
//    public void setRecalcURL(String recalcURL) {
//        this.recalcURL = recalcURL;
//    }
//    
//    public String getRecalcParam() {
//        return this.recalcParam;
//    }
//
//    public void setRecalcParam(String recalcParam) {
//        this.recalcParam = recalcParam;
//    }

    /**
     * @see lams.ams.AmsBridge#close()
     */
    @Override
    public void close() {
//        // unimplemented: db access control handled by hibernate
//        logger.debug("closing ams2Bridge...");
        this.close(null);
    }

    /**
     * @see lams.ams.AmsBridge#close(java.lang.String)
     */
    @Override
    public void close(String reason) {
//        // unimplemented: db access control handled by hibernate
        logger.debug("closing ams2Bridge due to " + reason);
//        if (this.getFabricSrv() != null) {
//            this.getFabricSrv().close();
//        }
    }



    /**
     * provides the NodePcplRule target for all DomainEditor options
     *
     * @param userId the destined user's uid
     * @param ourl the target resource
     * @return target for all DomainEditor options
     */
//    private NodePcplRule getDomEditorRuleOpts(String userId, String nodeIdStr) {
//        NodeID nodeIDObj = this.getFabricSrv().newNodeID(nodeIdStr);
//        LatPrincipal pcpl = this.getPrincipalSrv().getUser(userId);
//        NodePcplRule result = this.getAuthorizationSrv().getEffectiveDomainEditorRule(nodeIDObj, pcpl);
//        return result;
//    }

    /**
     * @see nl.mpi.lamus.ams.AmsBridge#setUsedStorageSpace(java.lang.String, java.net.URI, long)
     */
    @Override
    public void setUsedStorageSpace(String uid, URI archiveNodeURI, long val) {
        
//TODO Temporarily commented out, until AMS is changed in order to support URIs instead of NodeIDs
        
//        try {
//            NodePcplRule target = this.getDomEditorRuleOpts(uid, nodeIdStr);
//            if (target == null) {
//                logger.error("found no NPR target for setting DomainEditor options");
//                return;
//            }
//            // npr is just a mimic, 
//            //	e.g. for ArchiveManager role incorporating DomainEditor options
//            if (target.isVirtual()) {
//                logger.debug("caught virtual " + target);
//                return;
//            }
//            // evil down cast: "only" 2^31 MB = 2^51 bytes space allowed
//            Integer mb = convertLongBToIntMB(val);
//            target.setUsedStorageMB(mb);
//            this.getAuthorizationSrv().save(target.getParent());
//
//            // trigger recalculation to re-export modified data from ams2 to csdb
//            // NOT necessarry here, cause used-storgage-space has no effect on/in ams2 (re)calculation
//            // <=> max- vs. used-storage-space is checked & handled in lamus itself
////		this.callAccessRightsManagementSystem(target.getParent().getNodeID().getMpiID());
//        } catch (RuntimeException rE) {
//            logger.error("could not set UsedStorageSpace", rE);
//            return;
//        }
    }

    /**
     * @see nl.mpi.lamus.ams.AmsBridge#getUsedStorageSpace(java.lang.String, java.net.URI)
     */
    @Override
    public long getUsedStorageSpace(String uid, URI archiveNodeURI) {
        
//TODO Temporarily commented out, until AMS is changed in order to support URIs instead of NodeIDs
        
//        try {
//            NodePcplRule target = this.getDomEditorRuleOpts(uid, nodeIdStr);
//            long usedStorageInBytes = AmsBridge.DEFAULT_MB.longValue();
//            if (target != null && target.getUsedStorageMB() != null) {
//                usedStorageInBytes = convertIntMBToLongB(target.getUsedStorageMB());
//            }
//            return usedStorageInBytes;
//        } catch (RuntimeException rE) {
//            logger.error("could not determine UsedStorageSpace, providing error-default "
//                    + AmsBridge.ERROR_MB, rE);
//            return AmsBridge.ERROR_MB.longValue();
//        }
        
        return 0;
    }
    
    private long convertIntMBToLongB(int valueInMB) {
        int valueInB = valueInMB * 1024 * 1024;
        return (long) valueInB;
    }
    
    private int convertLongBToIntMB(long valueInB) {
        long valueInMB = valueInB / 1024 / 1024;
        return (int) valueInMB;
    }

    /**
     * @see nl.mpi.lamus.ams.AmsBridge#getMaxStorageSpace(java.lang.String, java.net.URI)
     */
    @Override
    public long getMaxStorageSpace(String uid, URI archiveNodeURI) {
        
//TODO Temporarily commented out, until AMS is changed in order to support URIs instead of NodeIDs
        
//        try {
//            NodePcplRule target = this.getDomEditorRuleOpts(uid, nodeIdStr);
//            // no target -> no domain-editor(options)
//            long maxStorageInBytes = AmsBridge.DEFAULT_MB.longValue();
//            if (target != null && target.getMaxStorageMB() != null) {
//                maxStorageInBytes = convertIntMBToLongB(target.getMaxStorageMB());
//            }
//            //contract from ams2-api: value null means unlimited = MAX_MB
//            return maxStorageInBytes;
//        } catch (RuntimeException rE) {
//            logger.error("could not determine MaxStorageSpace, providing error-default "
//                    + ERROR_MB, rE);
//            return ERROR_MB.longValue();
//        }
        
        return 1024 * 1024 * 1024;
    }
    /**
     * @see lams.ams.AmsBridge#getMailAddress(java.lang.String)
     */
//	public String getMailAddress(String uid) {
//		try {
//			LatUser user = this.getPrincipalSrv().getUser(uid);
//			return user != null ? user.getEmail() : null;
//		} catch(Exception eE) {
//			LOG.error("failed to load user from ams2: " + uid, eE);
//			return null;
//		}
//	}
    /**
     * @see lams.ams.AmsBridge#getRealName(java.lang.String)
     */
//	public String getRealName(String uid) {
//		try {
//			LatUser user = this.getPrincipalSrv().getUser(uid);
//			return user != null ? user.getDisplayName() : null;
//		} catch(Exception eE) {
//			LOG.error("failed to load user from ams2: " + uid, eE);
//			return null;
//		}
//	}
    /**
     * transcribes given ourURL into a NodeID
     *
     * @param ourl
     * @return NodeID equivalent of given OurURL
     */
//	private NodeID toNodeID(OurURL ourl) {
//		NodeID result = this.getFabricSrv().newNodeID(ourl);
//		//we have to throw the unknownNodeException if the url is not known in the AO table
//		if(result == null)
//			throw new UnknownNodeException("AmsBridge:toNodeID(): URL not in AO table: "+ourl);
//		return result;
//	}
    /**
     * @see nl.mpi.lamus.ams.AmsBridge#callAccessRightsManagementSystem(java.net.URI)
     */
    @Override
    public void callAccessRightsManagementSystem(URI recalcDomainArchiveURI) {
            
            throw new UnsupportedOperationException("Ams2Bridge.callAccessRightsManagementSystem not implemented yet");
            
//		try {
//			// build & check target url
//			if(Text.empty(this.baseURL) || Text.empty(this.recalcURL)) {
//				LOG.warn("no ams-url configured, access rights will not be updated");
//				return;
//			}
//			StringBuilder amsurl = new StringBuilder();
//			amsurl.append(this.baseURL).append("/").append(this.recalcURL);
//			if(Text.notEmpty(recalcDomainMpiID) && Text.notEmpty(this.recalcParam)) {
//				amsurl.append("?").append(this.recalcParam).append("=")
//					.append( URLEncoder.encode(recalcDomainMpiID,"UTF-8"));
//			}
//			
//			// the actual call
//			OurURL amsurlServlet = new OurURL(amsurl.toString());
//			LOG.info("ams2 recalculation called by " + amsurlServlet);
//			
//			URLConnection servletConnection = amsurlServlet.openConnection();
//			servletConnection.setDoInput(true);
//			servletConnection.setDoOutput(false);
//			servletConnection.setUseCaches(false); // for the connection to the CGI / servlet, that is
//			servletConnection.setRequestProperty("Content-Type", "text");
//			InputStream instr = servletConnection.getInputStream();
//			BufferedReader reader = new BufferedReader(new InputStreamReader(instr));
//			StringBuilder reply = new StringBuilder("ams2 recalculation call replied:\n");
//			String line;
//			while((line = reader.readLine()) != null) {
//				reply.append(line);
//			}
//			reader.close();
//			if (servletConnection instanceof HttpURLConnection)
//				((HttpURLConnection)servletConnection).disconnect();
//			LOG.info(reply.toString());
//		} catch(Exception eE) {
//			LOG.error("!! failed to call ams2 recalculation !!", eE);
//		}
    }
    /**
     * @see lams.ams.AmsBridge#replaceNodeAms(String, String, String)
     */
//	public boolean replaceNodeAms(String oldNodeId, String newNodeId, String userId) {
//		
//		LOG.debug("Ams2Bridge.replaceNodeAms: AMS node replacement triggered. Old node: " + oldNodeId + "; New node: " + newNodeId);
//		
//		try {
//			Node oldNode = getFabricSrv().getNode(new NodeIDImpl(oldNodeId));
//			NodeID oldNodeIdParam = new NodeIDImpl(oldNodeId);
//			NodeID newNodeIdParam = new NodeIDImpl(newNodeId);
//			LatUser user = getPrincipalSrv().getUser(userId);
//			
//			// retrieve the domain managers, curators and editors of the old node, so that these rules can be used later
//			//  (they should be part of the default rules to be applied to the versioned node)
//			List<LatPrincipal> oldNodeDomainManagers = getAuthorizationSrv().getDomainManagers(oldNode);
//			List<LatPrincipal> oldNodeDomainCurators = getAuthorizationSrv().checkDomainCurator(oldNode);
//			List<LatPrincipal> oldNodeDomainEditors = getAuthorizationSrv().getDomainEditors(oldNode);
//	
//			// copy node-principals from the old node to the new one, delete them from the old node
//			copyNodePcpls(user, oldNodeIdParam, newNodeIdParam);
//			
//			// copy node-licenses from the old node to the new one, delete them from the old node
//			copyNodeLicenses(user, oldNodeIdParam, newNodeIdParam);
//			
//			// set the default rules (forbid everybody + domain managers, curators, editors) for the versioned node
//			setDefaultRulesForVersionedNode(user, oldNode, oldNodeDomainManagers, oldNodeDomainCurators, oldNodeDomainEditors);
//			
//		} catch(UnknownNodeException ex) {
//			LOG.error("Ams2Bridge.replaceNodeAms: problem retrieving node " + oldNodeId + " from the database", ex);
//			return false;
//		}
//		
//		return true;
//	}
    /**
     * Creates node-principals (NodeAuth) for the new node, based on the
     * existing ones (in the old node). So that the new node has exactly the
     * same access rights as the old one had (in this case the rules). It also
     * deletes the node-principals from the old node afterwards.
     *
     * @param user Current user
     * @param oldNodeId	ID of the old node (replaced)
     * @param newNodeId	ID of the new node (replacing)
     */
//	private void copyNodePcpls(LatUser user, NodeID oldNodeId, NodeID newNodeId) {
//		
//		List<NodeAuth> oldNodePcpls = getAuthorizationSrv().getNodeAuths(oldNodeId);
//		List<NodeAuth> newNodePcpls = new ArrayList<NodeAuth>();
//		
//		for(NodeAuth nodePcpl : oldNodePcpls) {
//			NodeAuth newNodePcpl = getAuthorizationSrv().newNodeAuth();
//			newNodePcpl.setNodeID(newNodeId);
//			newNodePcpl.setPcpl(nodePcpl.getPcpl());
//
//			Set<NodePcplLicense> nodeAcceptedLicenses = nodePcpl.getAcceptedLicenses();
//			
//			for(NodePcplLicense nodeAcceptedLicense : nodeAcceptedLicenses) {
//				newNodePcpl.addLicenseAccptance(nodeAcceptedLicense);
//			}
//			
//			newNodePcpl.setCreatedOn(nodePcpl.getCreatedOn());
//			newNodePcpl.setCreator(nodePcpl.getCreator());
//			
//			newNodePcpl.setLastModifier(nodePcpl.getLastModifier());
//			newNodePcpl.setLastModOn(nodePcpl.getLastModOn());
//
//			Set<NodePcplRule> nodePrincipalRules = nodePcpl.getRules();
//			
//			for(NodePcplRule nodePrincipalRule : nodePrincipalRules) {
//				NodePcplRule newNodePcplRule = getAuthorizationSrv().newNodeRule();
//
//				Date now = new Date();
//				
//				newNodePcplRule.setCreatedOn(now);
//				newNodePcplRule.setCreator(user);
//				newNodePcplRule.setLastModifier(user);
//				newNodePcplRule.setLastModOn(now);
//				newNodePcplRule.setMaxStorageMB(nodePrincipalRule.getMaxStorageMB());
//				newNodePcplRule.setNature(nodePrincipalRule.getNature());
//				newNodePcplRule.setPriority(nodePrincipalRule.getPriority());
//				newNodePcplRule.setRule(nodePrincipalRule.getRule());
//				newNodePcplRule.setUsedStorageMB(nodePrincipalRule.getUsedStorageMB());
//				
//				newNodePcpl.addRule(newNodePcplRule);
//			}
//			
//			newNodePcpls.add(newNodePcpl);
//		}
//		
//		getAuthorizationSrv().delete(oldNodePcpls);
//		getAuthorizationSrv().save(newNodePcpls);
//		
//	}
    /**
     * Creates node-licenses (NodeLicense) for the new node, based on the
     * existing ones (in the old node). So that the new node has exactly the
     * same access rights as the old one had (in this case the licenses). It
     * also deletes the node-licenses from the old node afterwards.
     *
     * @param user Current user
     * @param oldNodeId	ID of the old node
     * @param newNodeId	ID of the new node
     */
//	private void copyNodeLicenses(LatUser user, NodeID oldNodeId, NodeID newNodeId) {
//		List<NodeLicense> oldNodeLicenses = getLicenseSrv().getNodeLicenses(oldNodeId);
//		List<NodeLicense> newNodeLicenses = new ArrayList<NodeLicense>();
//		
//		for(NodeLicense nodeLicense : oldNodeLicenses) {
//			NodeLicense newNodeLicense = getLicenseSrv().newNodeLicense();
//			newNodeLicense.setNodeID(newNodeId);
//			newNodeLicense.setLicense(nodeLicense.getLicense());
//
//			Date now = new Date();
//			
//			newNodeLicense.setCreatedOn(now);
//			newNodeLicense.setCreator(user);
//			newNodeLicense.setLastModifier(user);
//			newNodeLicense.setLastModOn(now);
//
//			newNodeLicenses.add(newNodeLicense);
//		}
//		
//		getLicenseSrv().delete(oldNodeLicenses);
//		getLicenseSrv().save(newNodeLicenses);
//		
//	}
    /**
     * Creates a node-principal (NodeAuth) and default rule for versioned nodes
     * (forbid to everybody except Archive Managers and Domain Curators, Editors
     * and Managers), which is then applied to the versioned node.
     *
     * @param user User to become creator/modifier of the rules
     * @param node Versioned node, in which the rules will be applied
     * @param oldNodeDomainManagers List of domain managers to be applied in the
     * versioned node (they were domain managers of the node before being
     * versioned)
     * @param oldNodeDomainCurators	List of domain curators to be applied in the
     * versioned node (they were domain curators of the node before being
     * versioned)
     * @param oldNodeDomainEditors List of domain editors to be applied in the
     * versioned node (they were domain editors of the node before being
     * versioned)
     */
//	private void setDefaultRulesForVersionedNode(LatUser user, Node node,
//			List<LatPrincipal> oldNodeDomainManagers, List<LatPrincipal> oldNodeDomainCurators, List<LatPrincipal> oldNodeDomainEditors) {
//		
//		List<LatPrincipal> everybody = new ArrayList<LatPrincipal>();
//		everybody.add(getPrincipalSrv().getEverybody());
//		setRules(user, node.getID(), everybody, NodePcplRule.NATURE_CONSTRAINT, NodePcplRule.PRIORITY_FORBID, getRuleSrv().getRuleForbid());
//		
//		if(oldNodeDomainManagers != null && oldNodeDomainManagers.size() > 0) {
//			setRules(user, node.getID(), oldNodeDomainManagers, NodePcplRule.NATURE_PERMISSION, NodePcplRule.PRIORITY_DMANAGER, getRuleSrv().getRuleDM());
//		}
//		if(oldNodeDomainCurators != null && oldNodeDomainCurators.size() > 0) {
//			setRules(user, node.getID(), oldNodeDomainCurators, NodePcplRule.NATURE_PERMISSION, NodePcplRule.PRIORITY_DMANAGER, getRuleSrv().getRuleDC());
//		}
//		if(oldNodeDomainEditors != null && oldNodeDomainEditors.size() > 0) {
//			setRules(user, node.getID(), oldNodeDomainEditors, NodePcplRule.NATURE_PERMISSION, NodePcplRule.PRIORITY_DMANAGER, getRuleSrv().getRuleDE());
//		}
//		
//	}
    /**
     * Applies the given rule, paired with a principal (from the provided list)
     * to the given node.
     *
     * @param user LatUser to be set as creator/modifier of the
     * node_principal/nodepcpl_rule
     * @param nodeId ID of the node in which the rules will be applied
     * @param principals List of principals for whom the rules will be applied
     * @param nature Nature of the rule (permission/constraint)
     * @param priority Priority of the rule
     * @param rule Rule
     */
//	private void setRules(LatUser user, NodeID nodeId, List<LatPrincipal> principals, Integer nature, Integer priority, AbstractRule rule) {
//		
//		for(LatPrincipal principal : principals) {
//			
//			Date now = new Date();
//			
//			NodeAuth nodePcpl = getAuthorizationSrv().getNodeAuth(nodeId, principal);
//			if(nodePcpl == null) {
//				nodePcpl = getAuthorizationSrv().newNodeAuth();
//				nodePcpl.setNodeID(nodeId);
//				nodePcpl.setPcpl(principal);
//				nodePcpl.setCreatedOn(now);
//				nodePcpl.setCreator(user);
//				nodePcpl.setLastModifier(user);
//				nodePcpl.setLastModOn(now);
//			}
//			
//			NodePcplRule pcplRule = getRuleSrv().newDefaultOptions(rule);
//			pcplRule.setCreatedOn(now);
//			pcplRule.setCreator(user);
//			pcplRule.setLastModifier(user);
//			pcplRule.setLastModOn(now);
//			pcplRule.setNature(nature);
//			pcplRule.setPriority(priority);
//			
//			pcplRule.setRule(rule);
//			nodePcpl.addRule(pcplRule);
//			
//			getAuthorizationSrv().save(nodePcpl);
//		}
//		
//	}
}
