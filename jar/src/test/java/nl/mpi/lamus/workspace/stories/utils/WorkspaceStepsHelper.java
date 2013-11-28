/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.workspace.stories.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.CorpusNodeType;
import nl.mpi.archiving.corpusstructure.core.UnknownNodeException;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.corpusstructure.ArchiveObjectsDB;
import nl.mpi.corpusstructure.CorpusStructureDB;
import nl.mpi.corpusstructure.NodeIdUtils;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.WorkspaceStatus;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;

/**
 *
 * @author guisil
 */
public class WorkspaceStepsHelper {
        
    static URL copyFileToArchiveFolder(File archiveFolder, String filename) throws IOException {

        InputStream testIn = WorkspaceStepsHelper.class.getClassLoader().getResourceAsStream("test_files/example_archive_files/" + filename);
        File testFile = new File(archiveFolder, filename);
        OutputStream testOut = new FileOutputStream(testFile);
        IOUtils.copy(testIn, testOut);
        
        URL fileURL = testFile.toURI().toURL();
        
        return fileURL;
    }
    
    static void copyArchiveFromOriginalLocation(File archiveFolder) throws IOException {
        
        URL testArchiveTopNodeUrl = WorkspaceStepsHelper.class.getClassLoader().getResource("test_files/test_mini_archive/parent_collection.cmdi");
        
        File testArchiveTopNodeDirectoryOriginalLocation = 
                new File(FilenameUtils.getFullPathNoEndSeparator(testArchiveTopNodeUrl.getPath()));
        
        FileUtils.copyDirectory(testArchiveTopNodeDirectoryOriginalLocation, archiveFolder);
    }
    
    static void copyWorkspaceFromOriginalLocation(File workspaceFolder, int workspaceID, String topNodeFileName) throws IOException {
        
        URL testWorkspaceTopNodeUrl = WorkspaceStepsHelper.class.getClassLoader().
                getResource("test_files/test_workspace" + workspaceID + File.separator + topNodeFileName);
        
        File testArchiveNodeDirectoryOriginalLocation = 
                new File(FilenameUtils.getFullPathNoEndSeparator(testWorkspaceTopNodeUrl.getPath()));
        
        FileUtils.copyDirectory(testArchiveNodeDirectoryOriginalLocation, workspaceFolder);
    }
    
    static void copyFileToWorkspaceUploadDirectory(File workspaceUploadDirectory, String fileLocation) throws IOException {
        
        URL fileUrl = WorkspaceStepsHelper.class.getClassLoader().getResource(fileLocation);
        File fileOrigin = new File(FilenameUtils.getFullPath(fileUrl.getPath()), FilenameUtils.getName(fileUrl.getPath()));
        File fileDestination = new File(workspaceUploadDirectory, FilenameUtils.getName(fileUrl.getPath()));
        FileUtils.copyFile(fileOrigin, fileDestination);
    }
    
    static void insertNodeInCSDB(DataSource corpusstructureDataSource, int nodeID, URL fileURL, int nodeType, String nodeFormat, boolean isFirstNode, int parentNodeID) {
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(corpusstructureDataSource);

        String insertString;
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
        map.put("url", fileURL.toString());
        map.put("crawltime", new Timestamp(Calendar.getInstance().getTimeInMillis()).toString());
        map.put("onsite", Boolean.toString(true));
        map.put("readrights", "everybody");
        map.put("writerights", "everybody");
        map.put("pid", "node:" + nodeID);
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
    
    static void insertDataInAmsDB(DataSource amsDataSource) {
        
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(amsDataSource);
        
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
    
    static void insertWorkspaceInDB(DataSource lamusDataSource, File workspaceBaseDirectory, File archiveFolder, int workspaceID, String userID)
            throws FileNotFoundException, IOException, URISyntaxException {
        insertWorkspaceInDB(lamusDataSource, workspaceBaseDirectory, workspaceID, userID, 1, new File(archiveFolder, "1.cmdi").toURI().toURL(), new URI(UUID.randomUUID().toString()));
    }
    
    static void insertWorkspaceInDB(DataSource lamusDataSource, File workspaceBaseDirectory, int workspaceID, String userID, int topWsNodeID, URL topNodeArchiveURL, URI topNodeArchiveURI)
            throws FileNotFoundException, IOException {

        String currentDate = new Timestamp(Calendar.getInstance().getTimeInMillis()).toString();
        File workspaceDirectory = new File(workspaceBaseDirectory, "" + workspaceID);
        File testFile = new File(workspaceDirectory, topWsNodeID + ".cmdi");
        
//        String topNodePID = "hdl:SOMETHING/00-0000-0000-0000-0000-" + topWsNodeID;
        String topNodeFormat = "text/x-cmdi+xml";
        
        String insertString = "";
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(lamusDataSource);
        Map<String, String> map = new HashMap<String, String>();
        
        insertWorkspace(insertString, template, map, workspaceID, userID, currentDate);
        insertWorkspaceNode(insertString, template, map, -1, topWsNodeID, topNodeArchiveURL, topNodeArchiveURI, topNodeArchiveURL,
                WorkspaceNodeType.METADATA, WorkspaceNodeStatus.NODE_ISCOPY,
                topNodeFormat, "testNode",
                workspaceID, testFile);
        
        workspaceDirectory.mkdirs();
        InputStream testIn = WorkspaceStepsHelper.class.getClassLoader().getResourceAsStream("test_files/example_archive_files/" + topWsNodeID + ".cmdi");
        OutputStream testOut = new FileOutputStream(testFile);
        IOUtils.copy(testIn, testOut);
    }
    
    static void insertArchiveInDBFromScript(DataSource corpusstructureDataSource) {
        
        String scriptPath = "nl/mpi/lamus/workspace/stories/utils/hsql_corpusstructure_insert.sql";
        Resource scriptResource = new ClassPathResource(scriptPath);
        JdbcTemplate template = new JdbcTemplate(corpusstructureDataSource);
        JdbcTestUtils.executeSqlScript(template, scriptResource, false);
    }
    
    static void insertAmsDataInDBFromScript(DataSource amsDataSource) {
        
        String scriptPath = "nl/mpi/lamus/workspace/stories/utils/hsql_ams2_insert.sql";
        Resource scriptResource = new ClassPathResource(scriptPath);
        JdbcTemplate template = new JdbcTemplate(amsDataSource);
        JdbcTestUtils.executeSqlScript(template, scriptResource, false);
    }
    
    static void insertWorkspaceInDBFromScript(DataSource lamusDataSource, int workspaceID) {
        
        String scriptPath = "nl/mpi/lamus/workspace/stories/utils/hsql_lamus2_insertws" + workspaceID + FilenameUtils.EXTENSION_SEPARATOR + "sql";
        Resource scriptResource = new ClassPathResource(scriptPath);
        JdbcTemplate template = new JdbcTemplate(lamusDataSource);
        JdbcTestUtils.executeSqlScript(template, scriptResource, false);
    }
    
    static void insertWorkspaceInDBWithNewlyLinkedNode(DataSource lamusDataSource, File workspaceBaseDirectory,
            int workspaceID, String userID, int topNodeWsID, URL topNodeArchiveURL, URI topNodeArchiveURI, String type)
            throws MalformedURLException, FileNotFoundException, IOException {
        
        String currentDate = new Timestamp(Calendar.getInstance().getTimeInMillis()).toString();
        File workspaceDirectory = new File(workspaceBaseDirectory, "" + workspaceID);
        File topNodeFile = new File(workspaceDirectory, topNodeWsID + ".cmdi");
        int childWsNodeID = topNodeWsID + 1;
        File childFile = null;
        File childOriginFile = null;

        String topNodePID = "node:" + topNodeWsID;
        String topNodeFormat = "text/x-cmdi+xml";
        String childNodePID = "node:" + childWsNodeID;
        String childNodeFormat = "";
        WorkspaceNodeType childNodeType = null;
        
        if("resource".equals(type)) {
            childNodeFormat = "application/pdf";
            childFile = new File(workspaceDirectory, childWsNodeID + ".pdf");
            childOriginFile = new File("/some/directory/" + childWsNodeID + ".pdf");
            childNodeType = WorkspaceNodeType.RESOURCE_WR;
        } else if("metadata".equals(type)) {
            childNodeFormat = "text/cmdi";
            childFile = new File(workspaceDirectory, childWsNodeID + ".cmdi");
            childOriginFile = new File("/some/directory/" + childWsNodeID + ".cmdi");
            childNodeType = WorkspaceNodeType.METADATA;
        }
        
        String insertString = "";
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(lamusDataSource);
        Map<String, String> map = new HashMap<String, String>();

        insertWorkspace(insertString, template, map, workspaceID, userID, currentDate);
        insertWorkspaceNode(insertString, template, map, -1, topNodeWsID, topNodeArchiveURL, topNodeArchiveURI, topNodeArchiveURL,
                WorkspaceNodeType.METADATA, WorkspaceNodeStatus.NODE_ISCOPY,
                topNodeFormat, "testNode",
                workspaceID, topNodeFile);
        insertWorkspaceNode(insertString, template, map, topNodeWsID, childWsNodeID, null, null, childOriginFile.toURI().toURL(),
                childNodeType, WorkspaceNodeStatus.NODE_UPLOADED,
                childNodeFormat, "testNode",
                workspaceID, childFile);
        
        workspaceDirectory.mkdirs();
        
        InputStream testIn = WorkspaceStepsHelper.class.getClassLoader().getResourceAsStream("test_files/example_archive_files_linked/" + topNodeWsID + ".cmdi" + type);
        OutputStream testOut = new FileOutputStream(topNodeFile);
        IOUtils.copy(testIn, testOut);
        
        if("resource".equals(type)) {
            testIn = WorkspaceStepsHelper.class.getClassLoader().getResourceAsStream("test_files/example_archive_files_linked/" + childWsNodeID + ".pdf");
        } else if("metadata".equals(type)) {
            testIn = WorkspaceStepsHelper.class.getClassLoader().getResourceAsStream("test_files/example_archive_files_linked/" + childWsNodeID + ".cmdi");
        }
        testOut = new FileOutputStream(childFile);
        IOUtils.copy(testIn, testOut);
    }
    
    static private void insertWorkspace(String insertString, NamedParameterJdbcTemplate template, Map<String, String> map,
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
    
    static private void insertWorkspaceNode(String insertString, NamedParameterJdbcTemplate template, Map<String, String> map,
            int parentWsNodeID, int wsNodeID, URL nodeArchiveURL, URI nodeArchiveURI, URL nodeOriginURL, WorkspaceNodeType nodeType, WorkspaceNodeStatus nodeStatus,
            String nodeFormat, String nodeName,
            int workspaceID, File nodeFile) throws MalformedURLException {
        
        insertString = "INSERT INTO node (workspace_node_id, workspace_id, archive_url, archive_uri, origin_url, name, type, workspace_url, status, format) "
                + "VALUES (:workspace_node_id, :workspace_id, :archive_url, :archive_uri, :origin_url, :name, :type, :workspace_url, :status, :format);";
        map.clear();
        map.put("workspace_node_id", "" + wsNodeID);
        map.put("workspace_id", "" + workspaceID);
//        if(parentNodeArchiveID == -1) {
//            map.put("archive_node_id", "" + nodeArchiveID);
//        } else {
//            map.put("archive_node_id", "-1");
//        }
        map.put("archive_url", (nodeArchiveURL != null ? nodeArchiveURL.toString() : ""));
        map.put("archive_uri", (nodeArchiveURI != null ? nodeArchiveURI.toString() : ""));
        map.put("origin_url", (nodeOriginURL != null ? nodeOriginURL.toString() : ""));
        map.put("name", "testNode");
        map.put("type", nodeType.toString());
        map.put("workspace_url", nodeFile.toURI().toURL().toString());
        map.put("status", nodeStatus.toString());
        map.put("format", nodeFormat);
        template.update(insertString, map);
        
        if(parentWsNodeID == -1 && nodeArchiveURL != null && nodeArchiveURI != null) {
            insertString = "UPDATE workspace SET top_node_id = :top_node_id, top_node_archive_url = :top_node_archive_url, top_node_archive_uri = :top_node_archive_uri WHERE workspace_id = :workspace_id;";
            map.clear();
            map.put("top_node_id", "" + wsNodeID);
            map.put("top_node_archive_url", nodeArchiveURL.toString());
            map.put("top_node_archive_uri", nodeArchiveURI.toString());
            map.put("workspace_id", "" + workspaceID);
            template.update(insertString, map);
        } else if (parentWsNodeID > -1) {
            insertString = "INSERT INTO node_link (parent_workspace_node_id, child_workspace_node_id, child_uri) "
                    + "VALUES (:parent_workspace_node_id, :child_workspace_node_id, :child_uri);";
            map.clear();
            map.put("parent_workspace_node_id", "" + parentWsNodeID);
            map.put("child_workspace_node_id", "" + wsNodeID);
            map.put("child_uri", (nodeArchiveURI != null ? nodeArchiveURI.toString() : ""));
            template.update(insertString, map);
        }
    }
    
    static void insertNodeWithoutParentInWSDB(DataSource lamusDataSource, File file, int workspaceID, int wsNodeID,
            WorkspaceNodeType nodeType, WorkspaceNodeStatus nodeStatus, String nodePID, String nodeFormat) throws MalformedURLException {
        
        String insertString = "";
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(lamusDataSource);
        Map<String, String> map = new HashMap<String, String>();
        
        URL fileURL = file.toURI().toURL();
        String filename = FilenameUtils.getName(fileURL.getPath());
        
        insertWorkspaceNode(insertString, template, map,
                -1, wsNodeID, null, null, fileURL, nodeType, nodeStatus, nodeFormat, filename, workspaceID, file);
    }
    
    static void clearCsDatabase(DataSource corpusstructureDataSource) {
        
//        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(corpusstructureDataSource);
//        
//        String deleteCorpusStructureSql = "DELETE FROM corpusstructure;";
//        String deleteCorpusNodesSql = "DELETE FROM corpusnodes;";
//        String deleteArchiveObjectsSql = "DELETE FROM archiveobjects;";
//        String deleteAccessGroupsSql = "DELETE FROM accessgroups;";
//        
//        Map<String, String> map = new HashMap<String, String>();
//        
//        template.update(deleteCorpusStructureSql, map);
//        template.update(deleteCorpusNodesSql, map);
//        template.update(deleteArchiveObjectsSql, map);
//        template.update(deleteAccessGroupsSql, map);
        
        String scriptPath = "nl/mpi/lamus/workspace/stories/utils/hsql_corpusstructure_delete.sql";
        Resource scriptResource = new ClassPathResource(scriptPath);
        JdbcTemplate template = new JdbcTemplate(corpusstructureDataSource);
        JdbcTestUtils.executeSqlScript(template, scriptResource, false);
    }
    
    static void clearLamusDatabase(DataSource lamusDataSource) {
        
//        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(lamusDataSource);
//        
//        String deleteNodeLinkSql = "DELETE FROM node_link;";
//        String deleteNodeSql = "DELETE FROM node;";
//        String deleteWorkspaceSql = "DELETE FROM workspace;";
//        
//        Map<String, String> map = new HashMap<String, String>();
//        
//        template.update(deleteNodeLinkSql, map);
//        template.update(deleteNodeSql, map);
//        template.update(deleteWorkspaceSql, map);
        
        String scriptPath = "nl/mpi/lamus/workspace/stories/utils/hsql_lamus2_delete.sql";
        Resource scriptResource = new ClassPathResource(scriptPath);
        JdbcTemplate template = new JdbcTemplate(lamusDataSource);
        JdbcTestUtils.executeSqlScript(template, scriptResource, false);
    }
    
    static void clearAmsDatabase(DataSource amsDataSource) {
        
//        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(amsDataSource);
        
//        String deleteNodePrincipalRuleSql = "DELETE FROM \"nodepcpl_rule\";";// WHERE id > 0;";
//        String deleteNodePrincipalSql = "DELETE FROM \"node_principal\";";
//        
//        String deletePrincipalRuleSql = "DELETE FROM \"principal_rule\";";
//        
//        String deleteLicenseSql = "DELETE FROM \"license\";";
//        String deleteRuleSql = "DELETE FROM \"rule\";";
//        
//        String deleteUserSql = "DELETE FROM \"user\";";// WHERE id > 0;";
//        String deletePrincipalSql = "DELETE FROM \"principal\";";// WHERE id > 0;";
//        
//        Map<String, String> map = new HashMap<String, String>();
//        
//        template.update(deleteNodePrincipalRuleSql, map);
//        template.update(deleteNodePrincipalSql, map);
//        
//        template.update(deletePrincipalRuleSql, map);
//        
//        template.update(deleteLicenseSql, map);
//        template.update(deleteRuleSql, map);
//        
//        template.update(deleteUserSql, map);
//        template.update(deletePrincipalSql, map);
        
        
        String scriptPath = "nl/mpi/lamus/workspace/stories/utils/hsql_ams2_delete.sql";
        Resource scriptResource = new ClassPathResource(scriptPath);
        JdbcTemplate template = new JdbcTemplate(amsDataSource);
        JdbcTestUtils.executeSqlScript(template, scriptResource, false);
    }
    
    static void clearDirectories(File archiveDirectory, File workspaceBaseDirectory, File trashcanDirectory) throws IOException {
        
        FileUtils.cleanDirectory(archiveDirectory);
        FileUtils.cleanDirectory(workspaceBaseDirectory);
        FileUtils.cleanDirectory(trashcanDirectory);
    }
    
    static TreeSnapshot createSelectedTreeArchiveSnapshot(CorpusStructureProvider csProvider, URI selectedNodeURI) throws UnknownNodeException {
        
//        Timestamp topNodeTimestamp = aoDB.getObjectTimestamp(NodeIdUtils.TONODEID(selectedNode));
        CorpusNode selectedNode = csProvider.getNode(selectedNodeURI);
        Date selectedNodeDate = selectedNode.getFileInfo().getFileTime();
//        String topNodeChecksum = aoDB.getObjectChecksum(NodeIdUtils.TONODEID(selectedNode));
        String selectedNodeChecksum = selectedNode.getFileInfo().getChecksum();
//        NodeSnapshot topNodeSnapshot = new NodeSnapshot(NodeIdUtils.TONODEID(selectedNode), topNodeTimestamp, topNodeChecksum);
        NodeSnapshot selectedNodeSnapshot = new NodeSnapshot(selectedNodeURI, selectedNodeDate, selectedNodeChecksum);
        List<NodeSnapshot> otherNodeSnapshots = new ArrayList<NodeSnapshot>();
        
//        String[] descendantsIDs = csDB.getDescendants(NodeIdUtils.TONODEID(selectedNode), -1, "*");
        
        //TODO CorpusNodeType doesn't allow to select in this method "ALL TYPES" (previously '-1'); using a workaround at the moment
        List<String> allFormats = new ArrayList<String>();
        allFormats.add("*");

        
        
        throw new UnsupportedOperationException("CHANGE CALL TO CS PROVIDER TO GET DESCENDANTS");
    //TODO REPLACE THIS CALL    
//        List<URI> descendantsURIs = csProvider.getDescendants(selectedNodeURI, CorpusNodeType.RESOURCE_OTHER, allFormats);
//        for(URI descendantURI : descendantsURIs) {
//            CorpusNode descendantNode = csProvider.getNode(descendantURI);
//            Date descendantTimestamp = descendantNode.getFileInfo().getFileTime();
//            String descendantChecksum = descendantNode.getFileInfo().getChecksum();
//            NodeSnapshot descendantSnapshot = new NodeSnapshot(descendantURI, selectedNodeDate, descendantChecksum);
//            otherNodeSnapshots.add(descendantSnapshot);
//        }
//        
//        TreeSnapshot treeSnapshot = new TreeSnapshot(selectedNodeSnapshot, otherNodeSnapshots);
//        return treeSnapshot;
    }
    
    static Collection<WorkspaceNode> findWorkspaceNodeForFile(WorkspaceDao workspaceDao, int workspaceID, File file) {
        
        Collection<WorkspaceNode> workspaceNodes = workspaceDao.getNodesForWorkspace(workspaceID);
        Collection<WorkspaceNode> nodesFound = new ArrayList<WorkspaceNode>();
        for(WorkspaceNode currentNode : workspaceNodes) {
            String nodeFilename = FilenameUtils.getName(currentNode.getWorkspaceURL().getPath());
            if(file.getName().equals(nodeFilename)) {
                nodesFound.add(currentNode);
            }
        }
        
        return nodesFound;
    }
    
    static File getExpectedFileLocationForDeletedNode(
            File trashCanFolder, int createdWorkspaceID, URL deletedNodeArchiveURL, URI deletedNodeArchiveURI) {
        
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int year = Calendar.getInstance().get(Calendar.YEAR);
        String yearMonth = "" + year + "-" + (month < 10 ? "0" + month: month);
        File trashYearMonthFolder = new File(trashCanFolder, yearMonth);
        File trashWorkspaceFolder = new File(trashYearMonthFolder, "" + createdWorkspaceID);
        
        String deletedNodeFilename = FilenameUtils.getName(deletedNodeArchiveURL.getPath());
        String versionFilename = "v" + deletedNodeArchiveURI + "__." + deletedNodeFilename;
        
        return new File(trashWorkspaceFolder, versionFilename);
    }
    
    static File getExpectedFileLocationForChildNode(String filename, String parentPath, URL parentURL) {
        
        String expectedPath = null;
        
        if(!"cmdi".equals(FilenameUtils.getExtension(filename))) {
            String resourcePath = FilenameUtils.concat(parentPath, FilenameUtils.getBaseName(parentURL.getPath()));
            if("pdf".equals(FilenameUtils.getExtension(filename))) {
                String writtenResourcePath = FilenameUtils.concat(resourcePath, "Annotations");
                expectedPath = writtenResourcePath;
            } else if("jpg".equals(FilenameUtils.getExtension(filename))) {
                String mediaResourcePath = FilenameUtils.concat(resourcePath, "Media");
            } else {
                expectedPath = FilenameUtils.concat(parentPath, FilenameUtils.getBaseName(parentURL.toString()));
            }
        } else {
            expectedPath = FilenameUtils.concat(parentPath, FilenameUtils.getBaseName(parentURL.toString()));
        }
        
        return new File(expectedPath, filename);
    }
}
