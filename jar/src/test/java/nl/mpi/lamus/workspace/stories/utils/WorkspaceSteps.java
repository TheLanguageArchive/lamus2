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
package nl.mpi.lamus.workspace.stories.utils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import javax.sql.DataSource;
import nl.mpi.corpusstructure.*;
import nl.mpi.lamus.ams.Ams2Bridge;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.workspace.model.*;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.HandleCarrier;
import nl.mpi.metadata.api.model.HeaderInfo;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import nl.mpi.metadata.cmdi.api.CMDIConstants;
import nl.mpi.metadata.cmdi.api.model.CMDIDocument;
import nl.mpi.util.OurURL;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jbehave.core.annotations.*;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

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
    
    @Autowired
    @Qualifier("trashCanBaseDirectory")
    private File trashCanFolder;
    
//    private URL topNodeURL;
    
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
    
    private int createdWorkspaceTopNodeArchiveID;
    private int createdWorkspaceTopNodeWsID;
    
    private URL newlyInsertedNodeUrl;
    
    private String oldNodeArchiveChecksum;

    private int deletedNodeArchiveID;
    private int deletedNodeWsID;
    
    private TreeSnapshot selectedTreeArchiveSnapshot;
    
    
    @BeforeScenario
    public void beforeEachScenario() throws IOException {
        WorkspaceStepsHelper.clearLamusDatabase(this.lamusDataSource);
        WorkspaceStepsHelper.clearCsDatabase(this.corpusstructureDataSource);
        WorkspaceStepsHelper.clearAmsDatabase(this.amsDataSource);
        WorkspaceStepsHelper.clearDirectories(this.archiveFolder, this.workspaceBaseDirectory, this.trashCanFolder);
        clearVariables();
    }
    
    private void clearVariables() {
//        this.topNodeURL = null;
        this.createdWorkspace = null;
        this.currentUserID = null;
        this.selectedNodeID = -1;
        this.createdWorkspaceID = -1;
        this.createdWorkspaceTopNodeArchiveID = -1;
        this.createdWorkspaceTopNodeWsID = -1;
        this.newlyInsertedNodeUrl = null;
        this.oldNodeArchiveChecksum = null;
    }
    
    
    @Given("an archive")
    public void anArchive() throws IOException {
        
        assertNotNull("archiveFolder null, was not correctly injected", this.archiveFolder);
        assertTrue("archiveFolder does not exist, was not properly created", this.archiveFolder.exists());

        WorkspaceStepsHelper.insertArchiveInDBFromScript(this.corpusstructureDataSource);
        
        WorkspaceStepsHelper.copyArchiveFromOriginalLocation(this.archiveFolder);
        
        assertNotNull("corpusstructureDataSource null, was not correctly injected", this.corpusstructureDataSource);
        assertNotNull("archiveObjectsDB null, was not correctly injected", this.archiveObjectsDB);
        assertNotNull("corpusStructureDB null, was not correctly injected", this.corpusStructureDB);
        assertTrue("corpusstructure database was not initialised", this.archiveObjectsDB.getStatus());
    }
    
//    @Given("a top node with ID $nodeID")
//    public void aNodeWithIDWhichIsTheTopNode(@Named("nodeID") int nodeArchiveID) throws IOException {
////        String filename = nodeArchiveID + ".cmdi";
////        URL nodeArchiveURL = WorkspaceStepsHelper.copyFileToArchiveFolder(this.archiveFolder, filename);
////        this.topNodeURL = nodeArchiveURL;
////        int nodeType = 2;
////        String nodeFormat = "text/cmdi";
////        int parentNodeID = -1;
////        WorkspaceStepsHelper.insertNodeInCSDB(this.corpusstructureDataSource, nodeArchiveID, nodeArchiveURL, nodeType, nodeFormat, true, parentNodeID);
////        
//        Node node = this.corpusStructureDB.getNode(NodeIdUtils.TONODEID(nodeArchiveID));
//        assertNotNull("Node with ID " + nodeArchiveID + " does not exist in the corpusstructure database", node);
//        
//        this.selectedNodeID = nodeArchiveID;
//    }
    
    @Given("a metadata node with ID $childNodeID which is a child of node with ID $parentNodeID")
    public void aNodeWithIDWhichIsAChildOfNodeWIthID(@Named("childNodeID") int childNodeID, @Named("parentNodeID") int parentNodeID) throws IOException {
//        
        Node parentNode = this.corpusStructureDB.getNode(NodeIdUtils.TONODEID(parentNodeID));
        assertNotNull("Node with ID " + parentNodeID + " does not exist in the corpusstructure database", parentNode);
//
//        String childFilename = childNodeID + ".cmdi";
//        URL childNodeURL = WorkspaceStepsHelper.copyFileToArchiveFolder(this.archiveFolder, childFilename);
//        int childNodeType = 2;
//        String childNodeFormat = "text/cmdi";
//        WorkspaceStepsHelper.insertNodeInCSDB(this.corpusstructureDataSource, childNodeID, childNodeURL, childNodeType, childNodeFormat, false, parentNodeID);
//        
        Node[] childNodes = this.corpusStructureDB.getChildrenNodes(NodeIdUtils.TONODEID(parentNodeID));
        assertNotNull("Node with ID " + parentNodeID + " should have children", childNodes);
//        assertTrue("Node with ID " + parentNodeID + " should have one child", childNodes.length == 1);
//        assertNotNull("Node with ID " + parentNodeID + " should have one non-null child", childNodes[0]);
//        assertTrue("Node with ID " + parentNodeID + " should have one child with ID " + childNodeID, NodeIdUtils.TOINT(childNodes[0].getNodeId()) == childNodeID);
//                
        Node childNode = this.corpusStructureDB.getNode(NodeIdUtils.TONODEID(childNodeID));
        assertNotNull("Node with ID " + childNodeID + " does not exist in the corpusstructure database", childNode);
    }
    
    @Given("a user with ID $userID that has read and write access to the node with ID $nodeID")
    public void aUserWithID(String userID, int nodeID) {
        
        assertNotNull("amsDataSource null, was not correctly injected", this.amsDataSource);
//        WorkspaceStepsHelper.insertDataInAmsDB(this.amsDataSource);
        
        WorkspaceStepsHelper.insertAmsDataInDBFromScript(this.amsDataSource);
        
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
    
    @Given("a workspace with ID $workspaceID created by $userID in node $topNodeArchiveID")
    public void aWorkspaceWithIDCreatedByUserInNode(
            int workspaceID, String userID, int topNodeArchiveID)
            throws MalformedURLException, FileNotFoundException, IOException, MetadataException {
        
        //TODO copy resource file (zorro pdf) to workspace folder
        //TODO link node in file (metadata api)
        //TODO link node in database (ws api)
            //TODO node should have status as "NEWLY ADDED"
        
        this.currentUserID = userID;
        this.createdWorkspaceID = workspaceID;
        this.createdWorkspaceTopNodeArchiveID = topNodeArchiveID;
        
        this.selectedTreeArchiveSnapshot = WorkspaceStepsHelper.createSelectedTreeArchiveSnapshot(this.corpusStructureDB, this.archiveObjectsDB, this.createdWorkspaceTopNodeArchiveID);
        
        OurURL nodeArchiveURL = this.archiveObjectsDB.getObjectURL(NodeIdUtils.TONODEID(topNodeArchiveID), ArchiveAccessContext.getFileUrlContext());
        String nodeArchiveFilename = FilenameUtils.getName(nodeArchiveURL.getPath());
        
        // filesystem
        File workspaceDirectory = new File(this.workspaceBaseDirectory, "" + workspaceID);
        
        WorkspaceStepsHelper.insertWorkspaceInDBFromScript(this.lamusDataSource, workspaceID);
        
        WorkspaceStepsHelper.copyWorkspaceFromOriginalLocation(workspaceDirectory, workspaceID, nodeArchiveFilename);
        
        assertTrue("Workspace directory for workspace " + workspaceID + " should have been created", workspaceDirectory.exists());
        
        File topNodeFile = new File(workspaceDirectory, nodeArchiveFilename);
        assertTrue("File for node " + topNodeArchiveID + " should have been created", topNodeFile.exists());

        
        // metadata
        MetadataDocument topNodeDocument = metadataAPI.getMetadataDocument(topNodeFile.toURI().toURL());
        assertNotNull("Metadata document " + topNodeFile.getPath() + " should not be null", topNodeDocument);
        assertTrue("Metadata document " + topNodeFile.getPath() + " should contain references",
                topNodeDocument instanceof ReferencingMetadataDocument);
        ReferencingMetadataDocument referencingTopNodeDocument = (ReferencingMetadataDocument) topNodeDocument;
        List<Reference> childReferences = referencingTopNodeDocument.getDocumentReferences();
        assertNotNull("List of references in metadata document " + topNodeFile.getPath() + " should not be null", childReferences);
        assertTrue("Metadata document " + topNodeFile.getPath() + " should have references", childReferences.size() > 0);

        // database
        Workspace retrievedWorkspace = this.workspaceDao.getWorkspace(workspaceID);
        assertNotNull("retrievedWorkspace null, was not properly created in the database", retrievedWorkspace);
        
        WorkspaceNode topNode = this.workspaceDao.getWorkspaceNode(retrievedWorkspace.getTopNodeID());
        assertNotNull("Top node of workspace " + workspaceID + " should not be null", topNode);
        assertEquals("URL of top node is different from expected", topNodeFile.toURI().toURL(), topNode.getWorkspaceURL());
        this.createdWorkspaceTopNodeWsID = topNode.getWorkspaceNodeID();
    }
    
    @Given("$filename has been linked to the workspace")
    public void fileHasBeenLinkedToTheWorkspace(String filename) throws MalformedURLException {
        
        OurURL nodeArchiveURL = archiveObjectsDB.getObjectURL(NodeIdUtils.TONODEID(this.createdWorkspaceTopNodeArchiveID), ArchiveAccessContext.getFileUrlContext());
        String nodeArchiveFilename = FilenameUtils.getName(nodeArchiveURL.getPath());
        
        File workspaceDirectory = new File(this.workspaceBaseDirectory, "" + this.createdWorkspaceID);
        
        File resourceDirectory = new File(workspaceDirectory, FilenameUtils.getBaseName(nodeArchiveFilename));
        File resourceFile = new File(resourceDirectory, filename);
        
        
        Collection<WorkspaceNode> childNodes = this.workspaceDao.getChildWorkspaceNodes(this.createdWorkspaceTopNodeWsID);
        assertNotNull("List of child nodes of top node should not be null", childNodes);
        assertTrue("Top node should have children", childNodes.size() > 0);
        WorkspaceNode childNode = childNodes.iterator().next();
        assertNotNull("Child of top node should not be null", childNode);
        assertEquals("Child of top node different from expected", resourceFile.toURI().toURL(), childNode.getWorkspaceURL());
    }
    
    @Given("the top node has had some metadata added")
    public void theTopNodeHasHadSomeMetadataAdded() throws IOException, MetadataException {
        
        Node archiveNode = this.corpusStructureDB.getNode(NodeIdUtils.TONODEID(this.createdWorkspaceTopNodeArchiveID));
        assertNotNull(archiveNode);
        WorkspaceNode wsNode = this.workspaceDao.getWorkspaceTopNode(this.createdWorkspaceID);
        assertNotNull(wsNode);
        assertFalse("The name of the node in the workspace should be different from its old name in the archive", archiveNode.getName().equals(wsNode.getName()));
        
        this.oldNodeArchiveChecksum = this.archiveObjectsDB.getObjectChecksum(NodeIdUtils.TONODEID(this.createdWorkspaceTopNodeArchiveID));
        
        MetadataDocument document = this.metadataAPI.getMetadataDocument(wsNode.getWorkspaceURL());
        //TODO There's no such fixed concept (name) in CMDI...
            // Does this make sense in the database anyway?
        
        document.putHeaderInformation(new HeaderInfo(CMDIConstants.CMD_HEADER_MD_COLLECTION_DISPLAY_NAME, "somename"));
    }
    
    @Given("one of the resource nodes, $filename, is deleted")
    public void oneOfTheResourceNodesIsDeleted(String filename) {
        
        //TODO make sure the file is not among the retrieved nodes of the workspace
            // and that the node is marked as deleted in the workspace database, and is not linked to any node
        
        Collection<WorkspaceNode> workspaceNodes = this.workspaceDao.getNodesForWorkspace(createdWorkspaceID);
        for(WorkspaceNode node : workspaceNodes) {
            if(node.getArchiveURL().getPath().contains(filename)) {
                assertEquals("Status of the node should be deleted", WorkspaceNodeStatus.NODE_DELETED, node.getStatus());
                this.deletedNodeArchiveID = node.getArchiveNodeID();
                this.deletedNodeWsID = node.getWorkspaceID();
                break;
            }
        }
        
        Collection<WorkspaceNode> nodeChildren = this.workspaceDao.getChildWorkspaceNodes(this.deletedNodeWsID);
        assertTrue("List of child nodes should be empty", nodeChildren.isEmpty());
        Collection<WorkspaceNode> nodeParents = this.workspaceDao.getParentWorkspaceNodes(this.deletedNodeWsID);
        assertTrue("List of parent nodes should be empty", nodeParents.isEmpty());
    }
    
    
    @When("that user chooses to create a workspace in the node with ID $nodeID")
    public void thatUserChoosesToCreateAWorkspaceInTheNodeWithID(int nodeID) {

        this.selectedNodeID =  nodeID;
        
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
    
    @Then("the workspace is created in the database")
    public void theWorkspaceIsCreatedInTheDatabase() throws InterruptedException {
        
        // database
        Workspace retrievedWorkspace = this.workspaceDao.getWorkspace(this.createdWorkspace.getWorkspaceID());
        WorkspaceNode retrievedWorkspaceTopNode = this.workspaceDao.getWorkspaceNode(retrievedWorkspace.getTopNodeID());
        assertNotNull("retrievedWorkspace null, was not properly created in the database", retrievedWorkspace);
        assertEquals("currentUserID (" + this.currentUserID + ") does not match the user ID in the retrieved workspace (" + retrievedWorkspace.getUserID() + ")", this.currentUserID, retrievedWorkspace.getUserID());
        assertEquals("selectedNodeID (" + this.selectedNodeID + ") does not match the ID of the top node in the retrieved workspace (" + retrievedWorkspaceTopNode.getArchiveNodeID() + ")", this.selectedNodeID, retrievedWorkspaceTopNode.getArchiveNodeID());
        
        assertEquals("retrieved workspace does not have the expected status (expected = " + WorkspaceStatus.INITIALISED + "; retrieved = " + retrievedWorkspace.getStatus() + ")", WorkspaceStatus.INITIALISED, retrievedWorkspace.getStatus());
        
        //TODO check all nodes that should be there...
    }
    
    @Then("the workspace files are present in the proper location in the filesystem")
    public void theWorkspaceFilesArePresentInTheProperLocationInTheFilesystem() {
        
        // filesystem
        File workspaceDirectory = new File(this.workspaceBaseDirectory, "" + this.createdWorkspace.getWorkspaceID());
        assertTrue("Workspace directory for workspace " + this.createdWorkspace.getWorkspaceID() + " does not exist", workspaceDirectory.exists());
        
        //TODO check all files that should be there...
        
        OurURL wsTopNodeURL = this.archiveObjectsDB.getObjectURL(NodeIdUtils.TONODEID(this.selectedNodeID), ArchiveAccessContext.getFileUrlContext());
        String wsTopNodeFilename = FilenameUtils.getName(wsTopNodeURL.toString());
        File wsTopNodeFile = new File(workspaceDirectory, wsTopNodeFilename);
        assertTrue("Top node file doesn't exist in the workspace directory", wsTopNodeFile.exists());
        
        // the workspace won't include the child nodes, unless they are also metadata nodes
    }
    
    
    @Then("the workspace is removed both from database and filesystem")
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
    
    @Then("the status of the workspace is marked as successfully submitted")
    public void theWorkspaceStatusIsMarkedAsSuccessfullySubmitted() {
        
        //TODO assert that the workspace is now marked as successfully submitted
            //TODO (maybe some other assertions)
        
        Workspace workspace = this.workspaceDao.getWorkspace(this.createdWorkspaceID);
        
        assertNotNull(workspace);
        assertEquals("Workspace status different from expected", WorkspaceStatus.SUBMITTED, workspace.getStatus());
        
        //TODO some other assertions?
    }
    
    @Then("the end date of the workspace is set")
    public void theEndDateOfTheWorkspaceWithIDIsSet() {
        
        Workspace workspace = this.workspaceDao.getWorkspace(this.createdWorkspaceID);
        
        assertNotNull(workspace);
        assertNotNull("End date of workspace should be set", workspace.getEndDate());
    }
    
    @Then("the new node is properly linked in the database, from parent node with ID $parentNodeID")
    public void theNewNodeIsProperlyLinkedInTheDatabaseFromTheParentNodeWithID(int parentNodeID) {
        
        //TODO assert that the new node is linked in the database
        
        URL parentNodeURL = this.archiveObjectsDB.getObjectURL(NodeIdUtils.TONODEID(parentNodeID), ArchiveAccessContext.FILE_UX_URL).toURL();
        assertNotNull(parentNodeURL);
//        String parentFilename = FilenameUtils.getName(parentNodeURL.getPath());
//        assertEquals(parentNodeID + ".cmdi", parentFilename);
        
        Node[] children = this.corpusStructureDB.getChildrenNodes(NodeIdUtils.TONODEID(parentNodeID));
        assertNotNull(children);
        assertTrue("The node should have linked children in the database", children.length > 0);
        
        
        
//        URL newNodeURL = this.archiveObjectsDB.getObjectURL(NodeIdUtils.TONODEID(newNodeID), ArchiveAccessContext.FILE_UX_URL).toURL();
//        assertNotNull("New node does not exist with ID " + newNodeID, newNodeURL);
//        String newNodeFilename = FilenameUtils.getName(newNodeURL.getPath());
//        assertEquals("New node has a filename in the database different from expected", newNodeID + ".pdf", newNodeFilename);
//        Node[] childNodes = this.corpusStructureDB.getChildrenNodes(NodeIdUtils.TONODEID(parentNodeID));
//        assertNotNull("Node " + parentNodeID + " should have children in the corpusstructure database", childNodes);
//        assertTrue("Number of children of node " + parentNodeID + " should be one", childNodes.length == 1);
//        assertEquals("Child of node " + parentNodeID + " has an ID different from expected",
//                newNodeID, NodeIdUtils.TOINT(childNodes[0].getNodeId()));
        
        // URL is already confirmed above (URL retrieved for nodeID
        
    }
    
    @Then("the new node, $nodeFilename, is properly linked from the parent file (node with ID $parentNodeID) and exists in the corpusstructure database")
    public void theNewNodeIsProperlyLinkedFromTheParentFile(String nodeFilename, int parentNodeID) throws IOException, MetadataException {
        
        URL parentNodeURL = this.archiveObjectsDB.getObjectURL(NodeIdUtils.TONODEID(parentNodeID), ArchiveAccessContext.getFileUrlContext()).toURL();
        MetadataDocument parentDocument = this.metadataAPI.getMetadataDocument(parentNodeURL);
        
        assertNotNull("Metadata document " + parentNodeURL + " should not be null", parentDocument);
        assertTrue("Metadata document " + parentNodeURL + " should contain references",
                parentDocument instanceof ReferencingMetadataDocument);
        ReferencingMetadataDocument referencingParentDocument = (ReferencingMetadataDocument) parentDocument;
        List<Reference> childReferences = referencingParentDocument.getDocumentReferences();
        assertNotNull("List of references in metadata document " + parentNodeURL + " should not be null", childReferences);
        assertTrue("Metadata document " + parentNodeURL + " should have references", childReferences.size() > 0);
        
        boolean childFound = false;
        String childHandle = null;
        
        for(Reference child : childReferences) {
            
            if(child instanceof HandleCarrier) {
                childHandle = ((HandleCarrier)child).getHandle();
            } else {
                continue;
            }
            
            OurURL childUrl = this.archiveObjectsDB.getObjectURLForPid(childHandle);
            if(childUrl.toString().contains(nodeFilename)) {
                childFound = true;
                String childID = this.archiveObjectsDB.getObjectId(childUrl);
                OurURL childUrlWithContext = this.archiveObjectsDB.getObjectURL(childID, ArchiveAccessContext.FILE_UX_URL);
                this.newlyInsertedNodeUrl = childUrlWithContext.toURL();
                break;
            }
        }
        
        assertTrue("Expected child was not found", childFound);
        
    }
    
    @Then("$filename is present in the proper location in the filesystem, under the directory of the parent node $parentNodeID")
    public void thenewFileIsPresentInTheProperLocationInTheFilesystemUnderTheDirectoryOfTheParentNode(String filename, int parentNodeID) {
        
//        File parentNodeDirectory = new File(this.archiveFolder, "" + parentNodeID);
//        assertTrue(parentNodeDirectory.exists());
        
        URL parentURL = this.archiveObjectsDB.getObjectURL(NodeIdUtils.TONODEID(parentNodeID), ArchiveAccessContext.FILE_UX_URL).toURL();
        
        String parentPath = FilenameUtils.getFullPathNoEndSeparator(parentURL.getPath());
        
        
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
        File expectedNodeFile = new File(expectedPath, filename);
        
//        File baseDirectoryForWrittenResource = new File(this.archiveFolder, "Annotations");
//        File newNodeFile = new File(baseDirectoryForWrittenResource, newNodeID + ".pdf");
//        assertTrue("File for node " + newNodeID + " should exist in the proper location: " + newNodeFile.getPath(), newNodeFile.exists());
        
        assertTrue("New file doesn't exist in the expected location: " + expectedNodeFile.getAbsolutePath(), expectedNodeFile.exists());
        
    }
    
    @Then("the children of $filename are also present in the database and in the filesystem")
    public void theChildrenOfFileAreAlsoPresentInTheDatabase(String filename) throws IOException, MetadataException {
        
        MetadataDocument document = this.metadataAPI.getMetadataDocument(this.newlyInsertedNodeUrl);
        assertTrue("Metadata document doesn't contain references" ,document instanceof ReferencingMetadataDocument);
        List<Reference> references = ((ReferencingMetadataDocument) document).getDocumentReferences();
        assertNotNull("List of references in metadata document shouldn't be null", references);
        assertTrue("List of references in metadata document should have size larger than 0", references.size() > 0);
        
        for(Reference ref : references) {
            assertTrue("Reference should contain a handle", ref instanceof HandleCarrier);
            String handle = ((HandleCarrier) ref).getHandle();
            assertNotNull("Handle should not be null", handle);
            OurURL refURL = this.archiveObjectsDB.getObjectURLForPid(handle);
            assertNotNull("Child not found in database", refURL);
            
            String refID = this.archiveObjectsDB.getObjectId(refURL);
            OurURL refURLWithContext = this.archiveObjectsDB.getObjectURL(refID, ArchiveAccessContext.FILE_UX_URL);
            
            File refFile = new File(refURLWithContext.getPath());
            assertTrue("Child doesn't exist in the filesystem", refFile.exists());
        }
    }
    
    @Then("the name of the node with ID $archiveNodeID has changed both in the database and in the filesystem")
    public void theNameOfTheNodeWithIDHasChanged(int archiveNodeID) {
        
        String changedNodeChecksum = this.archiveObjectsDB.getObjectChecksum(NodeIdUtils.TONODEID(archiveNodeID));
        
        assertFalse("Name of the node in the archive should have changed", this.oldNodeArchiveChecksum.equals(changedNodeChecksum));
    }
    
    @Then("the deleted node has been moved to the trash folder in the filesystem")
    public void theDeletedNodeIsMarkedAsDeletedInTheDatabaseAndShouldNotBeLinkedToAnyNode() {
        
        String deletedNodePath = this.archiveObjectsDB.getObjectURL(NodeIdUtils.TONODEID(this.deletedNodeArchiveID), ArchiveAccessContext.FILE_UX_URL).toString();
        assertTrue("Deleted node should be located in the trash can folder", deletedNodePath.contains(this.trashCanFolder.getPath()));
        
        File deletedNodeFile = new File(deletedNodePath);
        assertNotNull("Deleted node file object should not be null", deletedNodeFile);
        
        
        //TODO This would be done only by the ArchiveCrawler? So at this point there would still be a link in the database...
        
//        CorpusNode deletedCorpusNode = this.corpusStructureDB.getCorpusNode(NodeIdUtils.TONODEID(this.deletedNodeArchiveID));
//        assertNull("Deleted corpus node object should be null", deletedCorpusNode);
        
        
        //TODO some more checks?
    }
    
    @Then("no changes were made to the archive")
    public void noChangesWereMadeToTheArchive() {
        
        TreeSnapshot finalSelectedTreeSnapshot = WorkspaceStepsHelper.createSelectedTreeArchiveSnapshot(this.corpusStructureDB, this.archiveObjectsDB, this.createdWorkspaceTopNodeArchiveID);
        boolean snapshotsAreSimilar = finalSelectedTreeSnapshot.equals(this.selectedTreeArchiveSnapshot);
        assertTrue("Snapshot of the selected tree different from expected", snapshotsAreSimilar);
    }
    
    //TODO OTHER CHECKS MISSING... ANNEX, CRAWLER AND OTHER STUFF
}
