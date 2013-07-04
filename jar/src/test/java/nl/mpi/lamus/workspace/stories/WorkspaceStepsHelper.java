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
package nl.mpi.lamus.workspace.stories;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import nl.mpi.corpusstructure.NodeIdUtils;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.WorkspaceStatus;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 *
 * @author guisil
 */
public class WorkspaceStepsHelper {
        
    static URL copyFileToArchiveFolder(File archiveFolder, String filename) throws IOException {

        InputStream testIn = WorkspaceStepsHelper.class.getClassLoader().getResourceAsStream("example_archive_files/" + filename);
        File testFile = new File(archiveFolder, filename);
        OutputStream testOut = new FileOutputStream(testFile);
        IOUtils.copy(testIn, testOut);
        
        URL fileURL = testFile.toURI().toURL();
        
        return fileURL;
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
    
    static void insertWorkspaceInDB(DataSource lamusDataSource, File workspaceBaseDirectory, File archiveFolder, int workspaceID, String userID) throws FileNotFoundException, IOException {
        insertWorkspaceInDB(lamusDataSource, workspaceBaseDirectory, workspaceID, userID, 1, new File(archiveFolder, "1.cmdi").toURI().toURL());
    }
    
    static void insertWorkspaceInDB(DataSource lamusDataSource, File workspaceBaseDirectory, int workspaceID, String userID, int topNodeArchiveID, URL topNodeArchiveURL) throws FileNotFoundException, IOException {

        String currentDate = new Timestamp(Calendar.getInstance().getTimeInMillis()).toString();
        File workspaceDirectory = new File(workspaceBaseDirectory, "" + workspaceID);
        File testFile = new File(workspaceDirectory, topNodeArchiveID + ".cmdi");
        
        String topNodePID = "hdl:SOMETHING/00-0000-0000-0000-0000-" + topNodeArchiveID;
        String topNodeFormat = "text/x-cmdi+xml";
        
        String insertString = "";
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(lamusDataSource);
        Map<String, String> map = new HashMap<String, String>();
        
        insertWorkspace(insertString, template, map, workspaceID, userID, currentDate);
        insertWorkspaceNode(insertString, template, map, -1, topNodeArchiveID, topNodeArchiveURL, topNodeArchiveURL,
                WorkspaceNodeType.METADATA, WorkspaceNodeStatus.NODE_ISCOPY,
                topNodePID, topNodeFormat,
                workspaceID, testFile);
        
        workspaceDirectory.mkdirs();
        InputStream testIn = WorkspaceStepsHelper.class.getClassLoader().getResourceAsStream("example_archive_files/" + topNodeArchiveID + ".cmdi");
        OutputStream testOut = new FileOutputStream(testFile);
        IOUtils.copy(testIn, testOut);
    }
    
    static void insertWorkspaceInDBWithNewlyLinkedNode(DataSource lamusDataSource, File workspaceBaseDirectory, int workspaceID, String userID, int topNodeArchiveID, URL topNodeArchiveURL)
            throws MalformedURLException, FileNotFoundException, IOException {
        
        String currentDate = new Timestamp(Calendar.getInstance().getTimeInMillis()).toString();
        File workspaceDirectory = new File(workspaceBaseDirectory, "" + workspaceID);
        File topNodeFile = new File(workspaceDirectory, topNodeArchiveID + ".cmdi");
        int resourceNodeArchiveID = topNodeArchiveID + 1;
        File resourceFile = new File(workspaceDirectory, resourceNodeArchiveID + ".pdf");
        File resourceOriginFile = new File("/some/directory/" + resourceNodeArchiveID + ".pdf");

        String topNodePID = "hdl:SOMETHING/00-0000-0000-0000-0000-" + topNodeArchiveID;
        String topNodeFormat = "text/x-cmdi+xml";
        String resourceNodePID = "hdl:SOMETHING/00-0000-0000-0000-0000-" + resourceNodeArchiveID;
        String resourceNodeFormat = "application/pdf";
        
        String insertString = "";
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(lamusDataSource);
        Map<String, String> map = new HashMap<String, String>();

        insertWorkspace(insertString, template, map, workspaceID, userID, currentDate);
        insertWorkspaceNode(insertString, template, map, -1, topNodeArchiveID, topNodeArchiveURL, topNodeArchiveURL,
                WorkspaceNodeType.METADATA, WorkspaceNodeStatus.NODE_ISCOPY,
                topNodePID, topNodeFormat,
                workspaceID, topNodeFile);
        insertWorkspaceNode(insertString, template, map, topNodeArchiveID, resourceNodeArchiveID, null, resourceOriginFile.toURI().toURL(),
                WorkspaceNodeType.RESOURCE_WR, WorkspaceNodeStatus.NODE_UPLOADED,
                resourceNodePID, resourceNodeFormat,
                workspaceID, resourceFile);
        
        workspaceDirectory.mkdirs();
        
        InputStream testIn = WorkspaceStepsHelper.class.getClassLoader().getResourceAsStream("example_archive_files_linked/" + topNodeArchiveID + ".cmdi");
        OutputStream testOut = new FileOutputStream(topNodeFile);
        IOUtils.copy(testIn, testOut);
        
        testIn = WorkspaceStepsHelper.class.getClassLoader().getResourceAsStream("example_archive_files_linked/" + resourceNodeArchiveID + ".pdf");
        testOut = new FileOutputStream(resourceFile);
        IOUtils.copy(testIn, testOut);
    }
    
    static void insertWorkspace(String insertString, NamedParameterJdbcTemplate template, Map<String, String> map,
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
    
    static void insertWorkspaceNode(String insertString, NamedParameterJdbcTemplate template, Map<String, String> map,
            int parentNodeArchiveID, int nodeArchiveID, URL nodeArchiveURL, URL nodeOriginURL, WorkspaceNodeType nodeType, WorkspaceNodeStatus nodeStatus,
            String nodePID, String nodeFormat,
            int workspaceID, File nodeFile) throws MalformedURLException {
        
        insertString = "INSERT INTO node (workspace_node_id, workspace_id, archive_node_id, archive_url, origin_url, name, type, workspace_url, status, pid, format) "
                + "VALUES (:workspace_node_id, :workspace_id, :archive_node_id, :archive_url, :origin_url, :name, :type, :workspace_url, :status, :pid, :format);";
        map.clear();
        map.put("workspace_node_id", "" + nodeArchiveID);
        map.put("workspace_id", "" + workspaceID);
        if(parentNodeArchiveID == -1) {
            map.put("archive_node_id", "" + nodeArchiveID);
        } else {
            map.put("archive_node_id", "-1");
        }
        map.put("archive_url", (nodeArchiveURL != null ? nodeArchiveURL.toString() : ""));
        map.put("origin_url", (nodeOriginURL != null ? nodeOriginURL.toString() : ""));
        map.put("name", "testNode");
        map.put("type", nodeType.toString());
        map.put("workspace_url", nodeFile.toURI().toURL().toString());
        map.put("status", nodeStatus.toString());
        map.put("pid", nodePID);
        map.put("format", nodeFormat);
        template.update(insertString, map);
        
        if(parentNodeArchiveID == -1) {
            insertString = "UPDATE workspace SET top_node_id = :top_node_id, top_node_archive_id = :top_node_archive_id, top_node_archive_url = :top_node_archive_url WHERE workspace_id = :workspace_id;";
            map.clear();
            map.put("top_node_id", "" + nodeArchiveID);
            map.put("top_node_archive_id", "" + nodeArchiveID);
            map.put("top_node_archive_url", nodeArchiveURL.toString());
            map.put("workspace_id", "" + workspaceID);
            template.update(insertString, map);
        } else {
            insertString = "INSERT INTO node_link (parent_workspace_node_id, child_workspace_node_id, child_uri) "
                    + "VALUES (:parent_workspace_node_id, :child_workspace_node_id, :child_uri);";
            map.clear();
            map.put("parent_workspace_node_id", "" + parentNodeArchiveID);
            map.put("child_workspace_node_id", "" + nodeArchiveID);
            map.put("child_uri", nodeFile.toURI().toString());
            template.update(insertString, map);
        }
    }
    
    static void clearCsDatabaseAndFilesystem(DataSource corpusstructureDataSource, File archiveFolder) throws IOException {
        
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(corpusstructureDataSource);
        
        String deleteCorpusStructureSql = "DELETE FROM corpusstructure;";
        String deleteCorpusNodesSql = "DELETE FROM corpusnodes;";
        String deleteArchiveObjectsSql = "DELETE FROM archiveobjects;";
        String deleteAccessGroupsSql = "DELETE FROM accessgroups;";
        
        Map<String, String> map = new HashMap<String, String>();
        
        template.update(deleteCorpusStructureSql, map);
        template.update(deleteCorpusNodesSql, map);
        template.update(deleteArchiveObjectsSql, map);
        template.update(deleteAccessGroupsSql, map);
        
        FileUtils.cleanDirectory(archiveFolder);
    }
    
    static void clearLamusDatabaseAndFilesystem(DataSource lamusDataSource, File workspaceBaseDirectory) throws IOException {
        
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(lamusDataSource);
        
        String deleteNodeLinkSql = "DELETE FROM node_link;";
        String deleteNodeSql = "DELETE FROM node;";
        String deleteWorkspaceSql = "DELETE FROM workspace;";
        
        Map<String, String> map = new HashMap<String, String>();
        
        template.update(deleteNodeLinkSql, map);
        template.update(deleteNodeSql, map);
        template.update(deleteWorkspaceSql, map);
        
        FileUtils.cleanDirectory(workspaceBaseDirectory);
    }
    
    static void clearAmsDatabase(DataSource amsDataSource) {
        
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(amsDataSource);
        
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
}
