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
import nl.mpi.metadata.api.model.HandleCarrier;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.api.model.MetadataElement;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import nl.mpi.util.OurURL;
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
        WorkspaceStepsHelper.clearLamusDatabaseAndFilesystem(this.lamusDataSource, this.workspaceBaseDirectory);
        WorkspaceStepsHelper.clearCsDatabaseAndFilesystem(this.corpusstructureDataSource, this.archiveFolder);
        WorkspaceStepsHelper.clearAmsDatabase(this.amsDataSource);
        clearVariables();
    }
    
    private void clearVariables() {
        this.topNodeURL = null;
        this.createdWorkspace = null;
        this.currentUserID = null;
        this.selectedNodeID = -1;
        this.createdWorkspaceID = -1;
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
    
    @Given("a top node with ID $nodeID")
    public void aNodeWithIDWhichIsTheTopNode(@Named("nodeID") int nodeArchiveID) throws IOException {
        String filename = nodeArchiveID + ".cmdi";
        URL nodeArchiveURL = WorkspaceStepsHelper.copyFileToArchiveFolder(this.archiveFolder, filename);
        this.topNodeURL = nodeArchiveURL;
        int nodeType = 2;
        String nodeFormat = "text/cmdi";
        int parentNodeID = -1;
        WorkspaceStepsHelper.insertNodeInCSDB(this.corpusstructureDataSource, nodeArchiveID, nodeArchiveURL, nodeType, nodeFormat, true, parentNodeID);
        
        Node node = this.corpusStructureDB.getNode(NodeIdUtils.TONODEID(nodeArchiveID));
        assertNotNull("Node with ID " + nodeArchiveID + " does not exist in the corpusstructure database", node);
        
        this.selectedNodeID = nodeArchiveID;
    }
    
    @Given("a node with ID $childNodeID which is a child of node with ID $parentNodeID")
    public void aNodeWithIDWhichIsAChildOfNodeWIthID(@Named("childNodeID") int childNodeID, @Named("parentNodeID") int parentNodeID) throws IOException {
        
        Node parentNode = this.corpusStructureDB.getNode(NodeIdUtils.TONODEID(parentNodeID));
        assertNotNull("Node with ID " + parentNodeID + " does not exist in the corpusstructure database", parentNode);

        String childFilename = childNodeID + ".cmdi";
        URL childNodeURL = WorkspaceStepsHelper.copyFileToArchiveFolder(this.archiveFolder, childFilename);
        int childNodeType = 2;
        String childNodeFormat = "text/cmdi";
        WorkspaceStepsHelper.insertNodeInCSDB(this.corpusstructureDataSource, childNodeID, childNodeURL, childNodeType, childNodeFormat, false, parentNodeID);
        
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
        WorkspaceStepsHelper.insertDataInAmsDB(this.amsDataSource);
        
        assertNotNull("Principal with ID " + userID + " is null", this.ams2Bridge.getPrincipalSrv().getPrincipal(userID));
        assertTrue("Principal with ID " + userID + " has no write access to the node with ID " + nodeID, this.ams2Bridge.hasWriteAccess(userID, NodeIdUtils.TONODEID(nodeID)));
        
        this.currentUserID = userID;
    }
    
//    @Given("a workspace with ID $workspaceID created by user with ID $userID")
//    public void aWorkspaceWithIDCreatedByUserWithID(int workspaceID, String userID) throws FileNotFoundException, IOException {
//
//        this.currentUserID = userID;
//        this.createdWorkspaceID = workspaceID;
//        
//        WorkspaceStepsHelper.insertWorkspaceInDB(this.lamusDataSource, this.workspaceBaseDirectory, this.archiveFolder, workspaceID, userID);
//
//        //filesystem
//        File workspaceDirectory = new File(this.workspaceBaseDirectory, "" + workspaceID);
//        assertTrue("Workspace directory for workspace " + workspaceID + " should have been created", workspaceDirectory.exists());
//        
//        // database
//        Workspace retrievedWorkspace = this.workspaceDao.getWorkspace(workspaceID);
//        assertNotNull("retrievedWorkspace null, was not properly created in the database", retrievedWorkspace);
//    }
    
//    @Given("a workspace with ID $workspaceID created by user with ID $userID in node with ID $nodeArchiveID")
//    public void aWorkspaceWithIDCreatedByUserWithIDInNodeWithID(int workspaceID, String userID, int nodeArchiveID) throws FileNotFoundException, IOException {
//
//        this.currentUserID = userID;
//        this.createdWorkspaceID = workspaceID;
//        
//        OurURL nodeArchiveURL = archiveObjectsDB.getObjectURL(NodeIdUtils.TONODEID(nodeArchiveID), ArchiveAccessContext.getFileUrlContext());
//        
//        WorkspaceStepsHelper.insertWorkspaceInDB(this.lamusDataSource, this.workspaceBaseDirectory, this.archiveFolder, workspaceID, userID, nodeArchiveID, nodeArchiveURL.toURL());
//
//        // filesystem
//        File workspaceDirectory = new File(this.workspaceBaseDirectory, "" + workspaceID);
//        assertTrue("Workspace directory for workspace " + workspaceID + " should have been created", workspaceDirectory.exists());
//        
//        // database
//        Workspace retrievedWorkspace = this.workspaceDao.getWorkspace(workspaceID);
//        assertNotNull("retrievedWorkspace null, was not properly created in the database", retrievedWorkspace);
//    }
    
    @Given("a workspace with ID $workspaceID created by user with ID $userID in node with ID $nodeID to which a new node has been linked")
    public void aWorkspaceWithIDCreatedByUserWithIDInNodeWithIDToWhichANewNodeHasBeenLinked(int workspaceID, String userID, int nodeArchiveID)
            throws MalformedURLException, FileNotFoundException, IOException, MetadataException {
        
        //TODO copy resource file (zorro pdf) to workspace folder
        //TODO link node in file (metadata api)
        //TODO link node in database (ws api)
            //TODO node should have status as "NEWLY ADDED"
        
        this.currentUserID = userID;
        this.createdWorkspaceID = workspaceID;
        
        OurURL nodeArchiveURL = archiveObjectsDB.getObjectURL(NodeIdUtils.TONODEID(nodeArchiveID), ArchiveAccessContext.getFileUrlContext());
        
        WorkspaceStepsHelper.insertWorkspaceInDBWithNewlyLinkedNode(this.lamusDataSource, this.workspaceBaseDirectory, workspaceID, userID, nodeArchiveID, nodeArchiveURL.toURL());
        
        // filesystem
        File workspaceDirectory = new File(this.workspaceBaseDirectory, "" + workspaceID);
        assertTrue("Workspace directory for workspace " + workspaceID + " should have been created", workspaceDirectory.exists());
        File topNodeFile = new File(workspaceDirectory, nodeArchiveID + ".cmdi");
        assertTrue("File for node " + nodeArchiveID + " should have been created", topNodeFile.exists());
        int resourceNodeID = nodeArchiveID + 1;
        File resourceFile = new File(workspaceDirectory, resourceNodeID + ".pdf");
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
        
        //TODO reference in parent file has to be set properly
        childReferences.get(0).setURI(resourceFile.toURI());
        
        assertEquals("URI of the child reference in the metadata document is different from expected",
                resourceFile.toURI(), childReferences.get(0).getURI());

        // database
        Workspace retrievedWorkspace = this.workspaceDao.getWorkspace(workspaceID);
        assertNotNull("retrievedWorkspace null, was not properly created in the database", retrievedWorkspace);
        
        WorkspaceNode topNode = this.workspaceDao.getWorkspaceNode(retrievedWorkspace.getTopNodeID());
        assertNotNull("Top node of workspace " + workspaceID + " should not be null", topNode);
        assertEquals("URL of top node is different from expected", topNodeFile.toURI().toURL(), topNode.getWorkspaceURL());
        
        Collection<WorkspaceNode> childNodes = this.workspaceDao.getChildWorkspaceNodes(topNode.getWorkspaceNodeID());
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
        
        boolean result = this.workspaceService.submitWorkspace(currentUserID, createdWorkspaceID/*, keepUnlinkedFiles*/);
        
        assertTrue("Result of the workspace submission should be true", result);
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
        
        URL parentNodeURL = this.archiveObjectsDB.getObjectURL(NodeIdUtils.TONODEID(parentNodeID), ArchiveAccessContext.getFileUrlContext()).toURL();
        MetadataDocument parentDocument = this.metadataAPI.getMetadataDocument(parentNodeURL);
        
        assertNotNull("Metadata document " + parentNodeURL + " should not be null", parentDocument);
        assertTrue("Metadata document " + parentNodeURL + " should contain references",
                parentDocument instanceof ReferencingMetadataDocument);
        ReferencingMetadataDocument referencingParentDocument = (ReferencingMetadataDocument) parentDocument;
        List<Reference> childReferences = referencingParentDocument.getDocumentReferences();
        assertNotNull("List of references in metadata document " + parentNodeURL + " should not be null", childReferences);
        assertTrue("Metadata document " + parentNodeURL + " should have one reference", childReferences.size() == 1);
        
        URI childNodeURI = this.archiveObjectsDB.getObjectURI(NodeIdUtils.TONODEID(newNodeID));
        
        String childHandleInArchive = this.archiveObjectsDB.getObjectPID(NodeIdUtils.TONODEID(newNodeID));

        assertNotNull("URI of the child node in the database should not be null", childNodeURI);
        assertTrue(childReferences.get(0) instanceof HandleCarrier);
        String childHandleInReference = ((HandleCarrier) childReferences.get(0)).getHandle();
        
//        URI childRelativeURI = childReferences.get(0).getURI();
//        File parentFile = new File(parentNodeURL.toString());
//        File childFileInDocument = new File(FilenameUtils.getFullPath(parentFile.getPath()), childRelativeURI.toString());
//        File expectedChildFile = new File(childNodeURI.toString());
        
//        assertEquals("URI of the child reference in the metadata document is different from expected",
//                childNodeURI, childReferences.get(0).getURI());
//                expectedChildFile, childFileInDocument);
        
        assertEquals("Handle in reference from parent file is different from the one stored in the archive for that same node",
                childHandleInArchive, childHandleInReference);
        
    }
    
    @Then("the file corresponding to the node with ID $newNodeID is present in the proper location in the filesystem, under the directory of the parent node :parentNodeID")
    public void theFileCorrespondingToTheNodeWithIDIsPresentInTheProperLocationInTheFilesystem(int newNodeID, int parentNodeID) {
        
        File parentNodeDirectory = new File(this.archiveFolder, "" + parentNodeID);
        assertTrue(parentNodeDirectory.exists());
        
        File newNodeFile = new File(parentNodeDirectory, newNodeID + ".pdf");
        assertTrue("File for node " + newNodeID + " should exist in the proper location: " + newNodeFile.getPath(), newNodeFile.exists());
        
    }
    
    //TODO OTHER CHECKS MISSING... ANNEX, CRAWLER AND OTHER STUFF
}
