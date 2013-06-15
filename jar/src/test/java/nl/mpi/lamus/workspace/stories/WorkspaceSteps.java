/*
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.mpi.lamus.workspace.stories;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.sql.Timestamp;
import java.util.*;
import javax.sql.DataSource;
import nl.mpi.corpusstructure.*;
import nl.mpi.lamus.ams.Ams2Bridge;
import nl.mpi.lamus.ams.AmsBridge;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.dao.implementation.LamusJdbcWorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceDirectoryHandler;
import nl.mpi.lamus.filesystem.implementation.LamusWorkspaceDirectoryHandler;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.service.implementation.LamusWorkspaceService;
import nl.mpi.lamus.workspace.factory.WorkspaceFactory;
import nl.mpi.lamus.workspace.factory.implementation.LamusWorkspaceFactory;
import nl.mpi.lamus.workspace.management.WorkspaceAccessChecker;
import nl.mpi.lamus.workspace.management.implementation.LamusWorkspaceAccessChecker;
import nl.mpi.lamus.workspace.management.implementation.LamusWorkspaceManager;
import nl.mpi.lamus.workspace.model.*;
import nl.mpi.lat.ams.dao.*;
import nl.mpi.lat.ams.service.LicenseService;
import nl.mpi.lat.ams.service.RuleService;
import nl.mpi.lat.ams.service.impl.*;
import nl.mpi.lat.auth.authentication.EncryptionService;
import nl.mpi.lat.auth.authorization.AdvAuthorizationService;
import nl.mpi.lat.auth.principal.PrincipalService;
import nl.mpi.lat.fabric.FabricService;
import nl.mpi.latimpl.auth.authentication.UnixCryptSrv;
import nl.mpi.latimpl.fabric.FabricSrv;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.api.model.MetadataElement;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.FileUtils;
import org.hibernate.SessionFactory;
import org.jbehave.core.annotations.*;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class WorkspaceSteps {
    

    private static final Logger logger = LoggerFactory.getLogger(WorkspaceSteps.class);
    
    @Autowired
    @Qualifier("archiveFolder")
    private File archiveFolder;
    
    @Autowired
    @Qualifier("workspaceBaseDirectory")
    private File workspaceBaseDirectory;
    
    private URL topNodeURL;
    
    @Autowired
    @Qualifier("corpusstructureDataSource")
    private DataSource corpusstructureDataSource;
    @Autowired
    @Qualifier("amsDataSource")
    private DataSource amsDataSource;
    @Autowired
    @Qualifier("lamusDataSource")
    private DataSource lamusDataSource;

    @Autowired
    @Qualifier("ArchiveObjectsDB")
    private ArchiveObjectsDBWrite archiveObjectsDB;
    @Autowired
    @Qualifier("ArchiveObjectsDB")
    private CorpusStructureDBWrite corpusStructureDB;
    
    @Autowired
    private MetadataAPI metadataAPI;
    
    @Autowired
    private Ams2Bridge ams2Bridge;

    @Autowired
    private WorkspaceDao workspaceDao;
    
    @Autowired
    private WorkspaceService workspaceService;
 
    private int selectedNodeID;
    
    private String currentUserID;
    
    private Workspace createdWorkspace;
    
    private int createdWorkspaceID;
    
    
    @BeforeStory
    public void beforeStory() throws IOException {
        cleanLamusDatabaseAndFilesystem();
        cleanCsDatabaseAndFilesystem();
        cleanAmsDatabase();
    }
    
      
    private URL copyFileToArchiveFolder(String filename, boolean isTopNode) throws IOException {

        InputStream testIn = getClass().getClassLoader().getResourceAsStream("example_archive_files/" + filename);
        File testFile = new File(this.archiveFolder, filename);
        OutputStream testOut = new FileOutputStream(testFile);
        IOUtils.copy(testIn, testOut);
        
        URL fileURL = testFile.toURI().toURL();
        
        if(isTopNode) {
            this.topNodeURL = fileURL;
        }
        
        return fileURL;
    }

    private void insertNodeInCSDB(int nodeID, URL fileURL, int nodeType, String nodeFormat, boolean isFirstNode, int parentNodeID) {
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(this.corpusstructureDataSource);

        String insertString = "";
        Map<String, String> map = new HashMap<String, String>();
        
        if(isFirstNode) {
            insertString = "INSERT INTO accessgroups VALUES (:md5, :aclstring);";
            map.put("md5", "everybody");
            map.put("aclstring", "everybody");
            template.update(insertString, map);
        }
        
        insertString = "INSERT INTO archiveobjects (nodeid, url, crawltime, onsite, readrights, writerights, pid, accesslevel) "
                + "VALUES (:nodeid, :url, :crawltime, :onsite, :readrights, :writerights, :pid, :accesslevel);";
        map.clear();
        map.put("nodeid", "" + nodeID);
        map.put("url", fileURL.toString()); //"http://someserver.mpi.nl/corpora/Node_CMDIfied_IMDI.cmdi");
        map.put("crawltime", new Timestamp(Calendar.getInstance().getTimeInMillis()).toString());
        map.put("onsite", Boolean.toString(true));
        map.put("readrights", "everybody");
        map.put("writerights", "everybody");
        map.put("pid", "hdl:SOMETHING/00-0000-0000-0000-0000-" + nodeID);
        map.put("accesslevel", "1");
        template.update(insertString, map);

        insertString = "INSERT INTO corpusnodes (nodeid, nodetype, format, name, title) "
                + "VALUES (:nodeid, :nodetype, :format, :name, :title);";
        map.clear();
        map.put("nodeid", "" + nodeID);
        map.put("nodetype", "" + nodeType);
        map.put("format", nodeFormat);
        map.put("name", FilenameUtils.getBaseName(fileURL.getPath()));
        map.put("title", FilenameUtils.getBaseName(fileURL.getPath()));
        template.update(insertString, map);

        if(isFirstNode) {
            insertString = "INSERT INTO corpusstructure (nodeid, canonical, valid) "
                    + "VALUES (:nodeid, :canonical, :valid);";
            map.clear();
            map.put("nodeid", "" + nodeID);
            map.put("canonical", "t");
            map.put("valid", "t");
        } else {
            insertString = "INSERT INTO corpusstructure (nodeid, canonical, valid, vpath0, vpath) "
                    + "VALUES (:nodeid, :canonical, :valid, :vpath0, :vpath);";
            map.clear();
            map.put("nodeid", "" + nodeID);
            map.put("canonical", "t");
            map.put("valid", "t");
            map.put("vpath0", "" + parentNodeID);
            map.put("vpath", "/" + NodeIdUtils.TONODEID(parentNodeID));
        }
        template.update(insertString, map);
    }
    
    private void insertDataInAmsDB() {
        
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(this.amsDataSource);
        
        String insertString = "INSERT INTO \"principal\" (id, uid, name, nature, host_institute, host_srv, creator, created_on, lastmodifier, last_mod_on) "
                + "VALUES (:id, :uid, :name, :nature, :host_institute, :host_srv, :creator, :created_on, :creator, :created_on);";
        Map<String, String> map = new HashMap<String, String>();
        map.put("id", "1");
        map.put("uid", "testUser");
        map.put("name", "TestUser");
        map.put("nature", "USR");
        map.put("host_institute", "MPINLA");
        map.put("host_srv", "ams2/ldap");
        map.put("creator", "1");
        map.put("created_on", new Timestamp(Calendar.getInstance().getTimeInMillis()).toString());
        template.update(insertString, map);

        insertString = "INSERT INTO \"user\" "
                + "VALUES (:id, :firstname, :email, :organisation, :address, :passwd, :creator, :created_on, :creator, :created_on);";
        map.clear();
        map.put("id", "1");
        map.put("firstname", "Test");
        map.put("email", "testUser@mpi.nl");
        map.put("organisation", "mpi 4 psycholinguistics");
        map.put("address", "wundtlaan 1\n6525 xd nijmegen\nthe netherlands");
        map.put("passwd", "{CRYPT}.GDJxqz1Ipii6");
        map.put("creator", "1");
        map.put("created_on", new Timestamp(Calendar.getInstance().getTimeInMillis()).toString());
        template.update(insertString, map);
        
//        insertString = "INSERT INTO \"principal\" (id, uid, name, nature, host_institute, host_srv, creator, created_on, lastmodifier, last_mod_on) "
//                + "VALUES (:id, :uid, :name, :nature, :host_institute, :host_srv, :creator, :created_on, :creator, :created_on);";
//        map.clear();
//        map.put("id", "-1");
//        map.put("uid", "everybody");
//        map.put("name", "-== EVERYBODY ==-");
//        map.put("nature", "ALL");
//        map.put("host_institute", "MPINLA");
//        map.put("host_srv", "ams2/ldap");
//        map.put("creator", "1");
//        map.put("created_on", new Timestamp(Calendar.getInstance().getTimeInMillis()).toString());
//        template.update(insertString, map);
//        
//        insertString = "INSERT INTO \"principal\" (id, uid, name, nature, host_institute, host_srv, creator, created_on, lastmodifier, last_mod_on) "
//                + "VALUES (:id, :uid, :name, :nature, :host_institute, :host_srv, :creator, :created_on, :creator, :created_on);";
//        map.clear();
//        map.put("id", "-2");
//        map.put("uid", "anyAuthenticatedUser");
//        map.put("name", "-== REGISTERED USERS ==-");
//        map.put("nature", "ALL");
//        map.put("host_institute", "MPINLA");
//        map.put("host_srv", "ams2/ldap");
//        map.put("creator", "1");
//        map.put("created_on", new Timestamp(Calendar.getInstance().getTimeInMillis()).toString());
//        template.update(insertString, map);
        
        insertString = "INSERT INTO \"node_principal\" (id, node_id, pcpl_id, creator, created_on, lastmodifier, last_mod_on) "
                + "VALUES (:id, :node_id, :pcpl_id, :creator, :created_on, :creator, :created_on);";
        map.clear();
        map.put("id", "1");
        map.put("node_id", "1");
        map.put("pcpl_id", "1");
        map.put("creator", "1");
        map.put("created_on", new Timestamp(Calendar.getInstance().getTimeInMillis()).toString());
        template.update(insertString, map);
        
//        insertString = "INSERT INTO \"rule\" VALUES(:id, :species, :realm, :competence, :disposition, :name,  :creator, :created_on, :creator, :created_on);";
//        map.clear();
//        map.put("id", "120");
//        map.put("species", "DE");
//        map.put("realm", "0110");
//        map.put("competence", "0010");
//        map.put("disposition", "-40");
//        map.put("name", "Domain Editor");
//        map.put("creator", "1");
//        map.put("created_on", new Timestamp(Calendar.getInstance().getTimeInMillis()).toString());
//        template.update(insertString, map);

        insertString = "INSERT INTO \"nodepcpl_rule\" (id, node_pcpl_id, rule_id, nature, priority, max_storage_mb, used_storage_mb, creator, created_on, lastmodifier, last_mod_on) "
                + "VALUES (:id, :node_pcpl_id, :rule_id, :nature, :priority, :max_storage_mb, :used_storage_mb, :creator, :created_on, :creator, :created_on);";
        map.clear();
        map.put("id", "1");
        map.put("node_pcpl_id", "1");
        map.put("rule_id", "120");
        map.put("nature", "1");
        map.put("priority", "150");
        map.put("max_storage_mb", "50000");
        map.put("used_storage_mb", "10000");
        map.put("creator", "1");
        map.put("created_on", new Timestamp(Calendar.getInstance().getTimeInMillis()).toString());
        template.update(insertString, map);        
    }

    private void insertWorkspaceInDB(int workspaceID, String userID) throws FileNotFoundException, IOException {
        insertWorkspaceInDB(workspaceID, userID, 1);
    }
    
    private void insertWorkspaceInDB(int workspaceID, String userID, int topNodeID) throws FileNotFoundException, IOException {

        String currentDate = new Timestamp(Calendar.getInstance().getTimeInMillis()).toString();
        File workspaceDirectory = new File(this.workspaceBaseDirectory, "" + workspaceID);
        File testFile = new File(workspaceDirectory, topNodeID + ".cmdi");
        
        String insertString = "";
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(this.lamusDataSource);
        Map<String, String> map = new HashMap<String, String>();
        
        insertWorkspace(insertString, template, map, workspaceID, userID, currentDate);
        insertWorkspaceNode(insertString, template, map, -1, topNodeID,
                WorkspaceNodeType.METADATA, WorkspaceNodeStatus.NODE_ISCOPY,
                workspaceID, testFile);
        
        workspaceDirectory.mkdirs();
        InputStream testIn = getClass().getClassLoader().getResourceAsStream("example_archive_files/" + topNodeID + ".cmdi");
        OutputStream testOut = new FileOutputStream(testFile);
        IOUtils.copy(testIn, testOut);
    }
    
    private void insertWorkspaceInDBWithNewlyLinkedNode(int workspaceID, String userID, int topNodeID)
            throws MalformedURLException, FileNotFoundException, IOException {
        
        String currentDate = new Timestamp(Calendar.getInstance().getTimeInMillis()).toString();
        File workspaceDirectory = new File(this.workspaceBaseDirectory, "" + workspaceID);
        File topNodeFile = new File(workspaceDirectory, topNodeID + ".cmdi");
        int resourceNodeID = topNodeID + 1;
        File resourceFile = new File(workspaceDirectory, resourceNodeID + ".pdf");

        String insertString = "";
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(this.lamusDataSource);
        Map<String, String> map = new HashMap<String, String>();

        insertWorkspace(insertString, template, map, workspaceID, userID, currentDate);
        insertWorkspaceNode(insertString, template, map, -1, topNodeID,
                WorkspaceNodeType.METADATA, WorkspaceNodeStatus.NODE_ISCOPY,
                workspaceID, topNodeFile);
        insertWorkspaceNode(insertString, template, map, topNodeID, resourceNodeID,
                WorkspaceNodeType.RESOURCE_WR, WorkspaceNodeStatus.NODE_UPLOADED,
                workspaceID, resourceFile);
        
        workspaceDirectory.mkdirs();
        
        InputStream testIn = getClass().getClassLoader().getResourceAsStream("example_archive_files_linked/" + topNodeID + ".cmdi");
        OutputStream testOut = new FileOutputStream(topNodeFile);
        IOUtils.copy(testIn, testOut);
        
        testIn = getClass().getClassLoader().getResourceAsStream("example_archive_files_linked/" + resourceNodeID + ".pdf");
        testOut = new FileOutputStream(resourceFile);
        IOUtils.copy(testIn, testOut);
    }
    
    private void insertWorkspace(String insertString, NamedParameterJdbcTemplate template, Map<String, String> map,
            int workspaceID, String userID, String currentDate) {
        
        insertString = "INSERT INTO workspace (workspace_id, user_id, start_date, session_start_date, status, message) "
                + "VALUES (:workspace_id, :user_id, :start_date, :session_start_date, :status, :message);";
        
        map.put("workspace_id", "" + workspaceID);
        map.put("user_id", userID);
        map.put("start_date", currentDate);
        map.put("session_start_date", currentDate);
        map.put("status", WorkspaceStatus.INITIALISED.toString());
        map.put("message", "some message");
        template.update(insertString, map);
    }
    private void insertWorkspaceNode(String insertString, NamedParameterJdbcTemplate template, Map<String, String> map,
            int parentNodeID, int nodeID, WorkspaceNodeType nodeType, WorkspaceNodeStatus nodeStatus,
            int workspaceID, File nodeFile) throws MalformedURLException {
        
        insertString = "INSERT INTO node (workspace_node_id, workspace_id, archive_node_id, name, type, workspace_url, status, format) "
                + "VALUES (:workspace_node_id, :workspace_id, :archive_node_id, :name, :type, :workspace_url, :status, :format);";
        map.clear();
        map.put("workspace_node_id", "" + nodeID);
        map.put("workspace_id", "" + workspaceID);
        if(parentNodeID == -1) {
            map.put("archive_node_id", "" + nodeID);
        } else {
            map.put("archive_node_id", "");
        }
        map.put("name", "testNode");
        map.put("type", nodeType.toString());
        map.put("workspace_url", nodeFile.toURI().toURL().toString());
        map.put("status", nodeStatus.toString());
        map.put("format", "cmdi");
        template.update(insertString, map);
        
        if(parentNodeID == -1) {
            insertString = "UPDATE workspace SET top_node_id = :top_node_id WHERE workspace_id = :workspace_id;";
            map.clear();
            map.put("top_node_id", "" + parentNodeID);
            map.put("workspace_id", "" + workspaceID);
            template.update(insertString, map);
        } else {
            insertString = "INSERT INTO node_link (parent_workspace_node_id, child_workspace_node_id, child_uri) "
                    + "VALUES (:parent_workspace_node_id, :child_workspace_node_id, :child_uri);";
            map.clear();
            map.put("parent_workspace_node_id", "" + parentNodeID);
            map.put("child_workspace_node_id", "" + nodeID);
            map.put("child_uri", nodeFile.toURI().toString());
            template.update(insertString, map);
        }
    }
    
    private void cleanCsDatabaseAndFilesystem() throws IOException {
        
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(this.corpusstructureDataSource);
        
        String deleteCorpusStructureSql = "DELETE FROM corpusstructure;";
        String deleteCorpusNodesSql = "DELETE FROM corpusnodes;";
        String deleteArchiveObjectsSql = "DELETE FROM archiveobjects;";
        String deleteAccessGroupsSql = "DELETE FROM accessgroups;";
        
        Map<String, String> map = new HashMap<String, String>();
        
        template.update(deleteCorpusStructureSql, map);
        template.update(deleteCorpusNodesSql, map);
        template.update(deleteArchiveObjectsSql, map);
        template.update(deleteAccessGroupsSql, map);
        
        FileUtils.cleanDirectory(this.archiveFolder);
        
        this.topNodeURL = null;
    }
    
    private void cleanLamusDatabaseAndFilesystem() throws IOException {
        
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(this.lamusDataSource);
        
        String deleteNodeLinkSql = "DELETE FROM node_link;";
        String deleteNodeSql = "DELETE FROM node;";
        String deleteWorkspaceSql = "DELETE FROM workspace;";
        
        Map<String, String> map = new HashMap<String, String>();
        
        template.update(deleteNodeLinkSql, map);
        template.update(deleteNodeSql, map);
        template.update(deleteWorkspaceSql, map);
        
        FileUtils.cleanDirectory(this.workspaceBaseDirectory);
        
        this.createdWorkspace = null;
        this.currentUserID = null;
        this.selectedNodeID = -1;
        this.createdWorkspaceID = -1;
    }
    
    private void cleanAmsDatabase() {
        
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(this.amsDataSource);
        
        String deleteNodePrincipalRuleSql = "DELETE FROM \"nodepcpl_rule\" WHERE id > 0;";
        String deleteNodePrincipalSql = "DELETE FROM \"node_principal\";";
        String deleteUserSql = "DELETE FROM \"user\" WHERE id > 0;";
        String deletePrincipalSql = "DELETE FROM \"principal\" WHERE id > 0;";
        
        Map<String, String> map = new HashMap<String, String>();
        
        template.update(deleteNodePrincipalRuleSql, map);
        template.update(deleteNodePrincipalSql, map);
        template.update(deleteUserSql, map);
        template.update(deletePrincipalSql, map);
    }
    
    @Given("an archive")
    public void anArchive() {
        
        assertNotNull("archiveFolder null, was not correctly injected", this.archiveFolder);
        assertTrue("archiveFolder does not exist, was not properly created", this.archiveFolder.exists());
        
        assertNotNull("corpusstructureDataSource null, was not correctly injected", this.corpusstructureDataSource);
        assertNotNull("archiveObjectsDB null, was not correctly injected", this.archiveObjectsDB);
        assertNotNull("corpusStructureDB null, was not correctly injected", this.corpusStructureDB);
        assertTrue("corpusstructure database was not initialised", this.archiveObjectsDB.getStatus());
    }
    
    @Given("a node with ID $nodeID which is the top node")
    public void aNodeWithIDWhichIsTheTopNode(@Named("nodeID") int nodeID) throws IOException {
        String filename = nodeID + ".cmdi";
        URL nodeURL = copyFileToArchiveFolder(filename, true);
        int nodeType = 2;
        String nodeFormat = "text/cmdi";
        int parentNodeID = -1;
        insertNodeInCSDB(nodeID, nodeURL, nodeType, nodeFormat, true, parentNodeID);
        
        Node node = this.corpusStructureDB.getNode(NodeIdUtils.TONODEID(nodeID));
        assertNotNull("Node with ID " + nodeID + " does not exist in the corpusstructure database", node);
        
        this.selectedNodeID = nodeID;
    }
    
    @Given("a node with ID $childNodeID which is a child of node with ID $parentNodeID")
    public void aNodeWithIDWhichIsAChildOfNodeWIthID(@Named("childNodeID") int childNodeID, @Named("parentNodeID") int parentNodeID) throws IOException {
        
        Node parentNode = this.corpusStructureDB.getNode(NodeIdUtils.TONODEID(parentNodeID));
        assertNotNull("Node with ID " + parentNodeID + " does not exist in the corpusstructure database", parentNode);

        String childFilename = childNodeID + ".cmdi";
        URL childNodeURL = copyFileToArchiveFolder(childFilename, false);
        int childNodeType = 2;
        String childNodeFormat = "text/cmdi";
        insertNodeInCSDB(childNodeID, childNodeURL, childNodeType, childNodeFormat, false, parentNodeID);
        
        Node[] childNodes = this.corpusStructureDB.getChildrenNodes(NodeIdUtils.TONODEID(parentNodeID));
        assertNotNull("Node with ID " + parentNodeID + " should have children", childNodes);
        assertTrue("Node with ID " + parentNodeID + " should have one child", childNodes.length == 1);
        assertNotNull("Node with ID " + parentNodeID + " should have one non-null child", childNodes[0]);
        assertTrue("Node with ID " + parentNodeID + " should have one child with ID " + childNodeID, NodeIdUtils.TOINT(childNodes[0].getNodeId()) == childNodeID);
                
        Node childNode = this.corpusStructureDB.getNode(NodeIdUtils.TONODEID(childNodeID));
        assertNotNull("Node with ID " + childNodeID + " does not exist in the corpusstructure database", childNode);
    }
    
    @Given("a user with ID $userID that has read and write access to the node with ID $nodeID")
    public void aUserWithID(String userID, int nodeID) {
        
        assertNotNull("amsDataSource null, was not correctly injected", this.amsDataSource);
        insertDataInAmsDB();
        
        assertNotNull("Principal with ID " + userID + " is null", this.ams2Bridge.getPrincipalSrv().getPrincipal(userID));
        assertTrue("Principal with ID " + userID + " has no write access to the node with ID " + nodeID, this.ams2Bridge.hasWriteAccess(userID, NodeIdUtils.TONODEID(nodeID)));
        
        this.currentUserID = userID;
    }
    
    @Given("a workspace with ID $workspaceID created by user with ID $userID")
    public void aWorkspaceWithIDCreatedByUserWithID(int workspaceID, String userID) throws FileNotFoundException, IOException {

        this.currentUserID = userID;
        this.createdWorkspaceID = workspaceID;
        
        insertWorkspaceInDB(workspaceID, userID);

        //filesystem
        File workspaceDirectory = new File(this.workspaceBaseDirectory, "" + workspaceID);
        assertTrue("Workspace directory for workspace " + workspaceID + " should have been created", workspaceDirectory.exists());
        
        // database
        Workspace retrievedWorkspace = this.workspaceDao.getWorkspace(workspaceID);
        assertNotNull("retrievedWorkspace null, was not properly created in the database", retrievedWorkspace);
    }
    
    @Given("a workspace with ID $workspaceID created by user with ID $userID in node with ID $nodeID")
    public void aWorkspaceWithIDCreatedByUserWithIDInNodeWithID(int workspaceID, String userID, int nodeID) throws FileNotFoundException, IOException {

        this.currentUserID = userID;
        this.createdWorkspaceID = workspaceID;
        
        insertWorkspaceInDB(workspaceID, userID, nodeID);

        // filesystem
        File workspaceDirectory = new File(this.workspaceBaseDirectory, "" + workspaceID);
        assertTrue("Workspace directory for workspace " + workspaceID + " should have been created", workspaceDirectory.exists());
        
        // database
        Workspace retrievedWorkspace = this.workspaceDao.getWorkspace(workspaceID);
        assertNotNull("retrievedWorkspace null, was not properly created in the database", retrievedWorkspace);
    }
    
    @Given("a workspace with ID $workspaceID created by user with ID $userID in node with ID $nodeID to which a new node has been linked")
    public void aWorkspaceWithIDCreatedByUserWithIDInNodeWithIDToWhichANewNodeHasBeenLinked(int workspaceID, String userID, int nodeID)
            throws MalformedURLException, FileNotFoundException, IOException, MetadataException {
        
        //TODO copy resource file (zorro pdf) to workspace folder
        //TODO link node in file (metadata api)
        //TODO link node in database (ws api)
            //TODO node should have status as "NEWLY ADDED"
        
        this.currentUserID = userID;
        this.createdWorkspaceID = workspaceID;
        
        insertWorkspaceInDBWithNewlyLinkedNode(workspaceID, userID, nodeID);
        
        // filesystem
        File workspaceDirectory = new File(this.workspaceBaseDirectory, "" + workspaceID);
        assertTrue("Workspace directory for workspace " + workspaceID + " should have been created", workspaceDirectory.exists());
        File topNodeFile = new File(workspaceDirectory, nodeID + ".cmdi");
        assertTrue("File for node " + nodeID + " should have been created", topNodeFile.exists());
        int resourceNodeID = nodeID + 1;
        File resourceFile = new File(workspaceDirectory, resourceNodeID + ".cmdi");
        assertTrue("File for resource " + resourceNodeID + " should have been created", resourceFile.exists());
        
        // metadata
        MetadataDocument topNodeDocument = metadataAPI.getMetadataDocument(topNodeFile.toURI().toURL());
        assertNotNull("Metadata document " + topNodeFile.getPath() + " should not be null", topNodeDocument);
        assertTrue("Metadata document " + topNodeFile.getPath() + " should contain references",
                topNodeDocument instanceof ReferencingMetadataDocument);
        ReferencingMetadataDocument referencingTopNodeDocument = (ReferencingMetadataDocument) topNodeDocument;
        List<Reference> childReferences = referencingTopNodeDocument.getDocumentReferences();
        assertNotNull("List of references in metadata document " + topNodeFile.getPath() + " should not be null", childReferences);
        assertTrue("Metadata document " + topNodeFile.getPath() + " should have one reference", childReferences.size() == 1);
        assertEquals("URI of the child reference in the metadata document is different from expected",
                resourceFile.toURI(), childReferences.get(0).getURI());

        // database
        Workspace retrievedWorkspace = this.workspaceDao.getWorkspace(workspaceID);
        assertNotNull("retrievedWorkspace null, was not properly created in the database", retrievedWorkspace);
        
        WorkspaceNode topNode = this.workspaceDao.getWorkspaceNode(retrievedWorkspace.getTopNodeID());
        assertNotNull("Top node of workspace " + workspaceID + " should not be null", topNode);
        assertEquals("URL of top node is different from expected", topNodeFile.toURI().toURL(), topNode.getWorkspaceURL());
        
        Collection<WorkspaceNode> childNodes = this.workspaceDao.getChildWorkspaceNodes(workspaceID);
        assertNotNull("List of child nodes of top node should not be null", childNodes);
        assertTrue("Top node should have one child", childNodes.size() == 1);
        WorkspaceNode childNode = childNodes.iterator().next();
        assertNotNull("Child of top node should not be null", childNode);
        assertEquals("Child of top node different from expected", resourceFile.toURI().toURL(), childNode.getWorkspaceURL());
    }
    
    @When("that user chooses to create a workspace in that node")
    public void thatUserChoosesToCreateAWorkspaceInThatNode() {

        assertNotNull("corpusstructureDataSource null, was not correctly injected", this.corpusstructureDataSource);
        assertNotNull("amsDataSource null, was not correctly injected", this.amsDataSource);
        assertNotNull("lamusDataSource null, was not correctly injected", this.lamusDataSource);
        assertNotNull("workspaceDao null, was not correctly injected", this.workspaceDao);

        this.createdWorkspace = workspaceService.createWorkspace(this.currentUserID, this.selectedNodeID);
        assertNotNull("createdWorkspace null just after 'createWorkspace' was called", this.createdWorkspace);
    }
    
    @When("that user chooses to delete the workspace")
    public void thatUserChoosesToDeleteTheWorkspace() {
        
        assertNotNull("corpusstructureDataSource null, was not correctly injected", this.corpusstructureDataSource);
        assertNotNull("amsDataSource null, was not correctly injected", this.amsDataSource);
        assertNotNull("lamusDataSource null, was not correctly injected", this.lamusDataSource);
        assertNotNull("workspaceDao null, was not correctly injected", this.workspaceDao);

        this.workspaceService.deleteWorkspace(this.currentUserID, this.createdWorkspaceID);
    }
    
    @When("that user chooses to submit the workspace")
    public void thatUserChoosesToSubmitTheWorkspace() {
        
        boolean keepUnlinkedFiles = Boolean.TRUE;
        
        assertNotNull("corpusstructureDataSource null, was not correctly injected", this.corpusstructureDataSource);
        assertNotNull("amsDataSource null, was not correctly injected", this.amsDataSource);
        assertNotNull("lamusDataSource null, was not correctly injected", this.lamusDataSource);
        assertNotNull("workspaceDao null, was not correctly injected", this.workspaceDao);
        
        //TODO more assertions missing?
        
        this.workspaceService.submitWorkspace(currentUserID, createdWorkspaceID/*, keepUnlinkedFiles*/);
        
    }
    
    @Then("a workspace is created in that node for that user")
    public void aWorkspaceIsCreatedInThatNodeForThatUser() throws InterruptedException {
        
        
        // filesystem
        File workspaceDirectory = new File(this.workspaceBaseDirectory, "" + this.createdWorkspace.getWorkspaceID());
        assertTrue("Workspace directory for workspace " + this.createdWorkspace.getWorkspaceID() + " does not exist", workspaceDirectory.exists());
        
        // database
        Workspace retrievedWorkspace = this.workspaceDao.getWorkspace(this.createdWorkspace.getWorkspaceID());
        WorkspaceNode retrievedWorkspaceTopNode = this.workspaceDao.getWorkspaceNode(retrievedWorkspace.getTopNodeID());
        assertNotNull("retrievedWorkspace null, was not properly created in the database", retrievedWorkspace);
        assertEquals("currentUserID (" + this.currentUserID + ") does not match the user ID in the retrieved workspace (" + retrievedWorkspace.getUserID() + ")", this.currentUserID, retrievedWorkspace.getUserID());
        assertEquals("selectedNodeID (" + this.selectedNodeID + ") does not match the ID of the top node in the retrieved workspace (" + retrievedWorkspaceTopNode.getArchiveNodeID() + ")", this.selectedNodeID, retrievedWorkspaceTopNode.getArchiveNodeID());
        
        assertEquals("retrieved workspace does not have the expected status (expected = " + WorkspaceStatus.INITIALISED + "; retrieved = " + retrievedWorkspace.getStatus() + ")", WorkspaceStatus.INITIALISED, retrievedWorkspace.getStatus());
    }
    
    @Then("the workspace is deleted")
    public void theWorkspaceIsDeleted() {
        
        // filesystem
        File workspaceDirectory = new File(this.workspaceBaseDirectory, "" + this.createdWorkspaceID);
        assertFalse("Workspace directory for workspace " + this.createdWorkspaceID + " still exists", workspaceDirectory.exists());
        
        // database
        Workspace retrievedWorkspace = this.workspaceDao.getWorkspace(this.createdWorkspaceID);
        assertNull("retrievedWorkspace not null, was not properly deleted from the database", retrievedWorkspace);
        Collection<WorkspaceNode> retrievedNodes = this.workspaceDao.getNodesForWorkspace(this.createdWorkspaceID);
        assertTrue("There should be no nodes associated with the deleted workspace", retrievedNodes.isEmpty());
    }
    
//    @Then("the workspace is successfully submitted")
//    public void theWorkspaceIsSuccessfullySubmitted() {
//        
//        fail("not implemented yet");
//        
//        //TODO decide what it means exactly to submit successfully
//         // (what checks need to be made to evaluate that)
//    }
    
    @Then("the status of the workspace with ID $workspaceID is marked as successfully submitted")
    public void theWorkspaceStatusIsMarkedAsSuccessfullySubmitted(int workspaceID) {
        
        //TODO assert that the workspace is now marked as successfully submitted
            //TODO (maybe some other assertions)
        
        Workspace workspace = this.workspaceDao.getWorkspace(workspaceID);
        
        assertNotNull(workspace);
        assertEquals("Workspace status different from expected", WorkspaceStatus.SUBMITTED, workspace.getStatus());
        
        //TODO some other assertions?
    }
    
    @Then("the end date of the workspace with ID $workspaceID is set")
    public void theEndDateOfTheWorkspaceWithIDIsSet(int workspaceID) {
        
        Workspace workspace = this.workspaceDao.getWorkspace(workspaceID);
        
        assertNotNull(workspace);
        assertNotNull("End date of workspace should be set", workspace.getEndDate());
    }
    
    @Then("And the new node, with the new ID $newNodeID, is properly linked in the database, from parent node with ID $parentNodeID")
    public void theNewNodeWithTheNewIDIsProperlyLinkedInTheDatabaseFromTheParentNodeWithID(int newNodeID, int parentNodeID) {
        
        //TODO assert that the new node is linked in the database
        
        URL parentNodeURL = this.archiveObjectsDB.getObjectURL(NodeIdUtils.TONODEID(parentNodeID), ArchiveAccessContext.FILE_UX_URL).toURL();
        assertNotNull(parentNodeURL);
        String parentFilename = FilenameUtils.getName(parentNodeURL.getPath());
        assertEquals(parentNodeID + ".cmdi", parentFilename);
        URL newNodeURL = this.archiveObjectsDB.getObjectURL(NodeIdUtils.TONODEID(newNodeID), ArchiveAccessContext.FILE_UX_URL).toURL();
        assertNotNull("New node does not exist with ID " + newNodeID, newNodeURL);
        String newNodeFilename = FilenameUtils.getName(newNodeURL.getPath());
        assertEquals("New node has a filename in the database different from expected", newNodeID + ".pdf", newNodeFilename);
        Node[] childNodes = this.corpusStructureDB.getChildrenNodes(NodeIdUtils.TONODEID(parentNodeID));
        assertNotNull("Node " + parentNodeID + " should have children in the corpusstructure database", childNodes);
        assertTrue("Number of children of node " + parentNodeID + " should be one", childNodes.length == 1);
        assertEquals("Child of node " + parentNodeID + " has an ID different from expected",
                newNodeID, NodeIdUtils.TOINT(childNodes[0].getNodeId()));
        
        // URL is already confirmed above (URL retrieved for nodeID
        
    }
    
    @Then("the new node, with ID $newNodeID, is properly linked from the parent file (node with ID $parentNodeID)")
    public void theNewNodeIsProperlyLinkedFromTheParentFile(int newNodeID, int parentNodeID) throws IOException, MetadataException {
        
        URL parentNodeURL = this.archiveObjectsDB.getObjectURL(NodeIdUtils.TONODEID(parentNodeID), ArchiveAccessContext.FILE_UX_URL).toURL();
        MetadataDocument parentDocument = this.metadataAPI.getMetadataDocument(parentNodeURL);
        
        assertNotNull("Metadata document " + parentNodeURL + " should not be null", parentDocument);
        assertTrue("Metadata document " + parentNodeURL + " should contain references",
                parentDocument instanceof ReferencingMetadataDocument);
        ReferencingMetadataDocument referencingParentDocument = (ReferencingMetadataDocument) parentDocument;
        List<Reference> childReferences = referencingParentDocument.getDocumentReferences();
        assertNotNull("List of references in metadata document " + parentNodeURL + " should not be null", childReferences);
        assertTrue("Metadata document " + parentNodeURL + " should have one reference", childReferences.size() == 1);
        
        URI childNodeURI = this.archiveObjectsDB.getObjectURI(NodeIdUtils.TONODEID(newNodeID));
        assertNotNull("URI of the child node in the database should not be null", childNodeURI);
        assertEquals("URI of the child reference in the metadata document is different from expected",
                childNodeURI, childReferences.get(0).getURI());
        
    }
    
    @Then("the file corresponding to the node with ID $newNodeID is present in the proper location in the filesystem, under the directory of the parent node :parentNodeID")
    public void theFileCorrespondingToTheNodeWithIDIsPresentInTheProperLocationInTheFilesystem(int newNodeID, int parentNodeID) {
        
        File parentNodeDirectory = new File(this.archiveFolder, "" + parentNodeID);
        assertTrue(parentNodeDirectory.exists());
        
        File newNodeFile = new File(parentNodeDirectory, newNodeID + ".pdf");
        assertTrue("File for node " + newNodeID + " should exist in the proper location: " + newNodeFile.getPath(), newNodeFile.exists());
        
    }
}
