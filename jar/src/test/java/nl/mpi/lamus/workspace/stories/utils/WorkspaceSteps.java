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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import javax.sql.DataSource;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.UnknownNodeException;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.ams.Ams2Bridge;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.NodeAccessException;
import nl.mpi.lamus.exception.WorkspaceAccessException;
import nl.mpi.lamus.exception.WorkspaceNodeNotFoundException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.exception.WorkspaceExportException;
import nl.mpi.lamus.exception.WorkspaceImportException;
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
import nl.mpi.metadata.identifierresolver.IdentifierResolver;
import nl.mpi.util.Checksum;
import nl.mpi.util.OurURL;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
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
    
    @Autowired
    @Qualifier("workspaceUploadDirectoryName")
    private String workspaceUplodaDirectoryName;
    
    @Autowired
    @Qualifier("corpusstructureDataSource")
    private DataSource corpusstructureDataSource;
    @Autowired
    @Qualifier("amsDataSource")
    private DataSource amsDataSource;
    @Autowired
    @Qualifier("lamusDataSource")
    private DataSource lamusDataSource;

//    @Autowired
//    @Qualifier("ArchiveObjectsDB")
//    private ArchiveObjectsDBWrite archiveObjectsDB;
//    @Autowired
//    @Qualifier("ArchiveObjectsDB")
//    private CorpusStructureDBWrite corpusStructureDB;
    
    @Autowired
    private CorpusStructureProvider corpusStructureProvider;
    
    @Autowired
    private NodeResolver nodeResolver;
    
    @Autowired
    private MetadataAPI metadataAPI;
    
    @Autowired
    private IdentifierResolver identifierResolver;
    
    @Autowired
    private Ams2Bridge ams2Bridge;

    @Autowired
    private WorkspaceDao workspaceDao;
    
    @Autowired
    private WorkspaceService workspaceService;
 
//    private int selectedNodeID;
    private URI selectedNodeURI;
    
    private String currentUserID;
    
    private Workspace createdWorkspace;
    
    private int createdWorkspaceID;
    
//    private int createdWorkspaceTopNodeArchiveID;
    private URI createdWorkspaceTopNodeArchiveURI;
    private int createdWorkspaceTopNodeWsID;
    
    private URL newlyInsertedNodeUrl;
    
    private String oldNodeArchiveChecksum;

//    private int deletedNodeArchiveID;
    private URI deletedNodeArchiveURI;
    private URL deletedNodeArchiveURL;
    private int deletedNodeWsID;
    
    private TreeSnapshot selectedTreeArchiveSnapshot;
    
    private String uploadedFileInOriginalLocation;
    private WorkspaceNode uploadedFileNode;
    private WorkspaceNode uploadedFileParentNode;
    
    private WorkspaceNode unlinkedParentNode;
    private WorkspaceNode unlinkedChildNode;
    
    private WorkspaceNode deletedWsNodeParent;
    private WorkspaceNode deletedWsNode;
    
    private int numberOfChildNodesAfterLinking;
    
    
    @BeforeScenario
    public void beforeEachScenario() throws IOException {
        WorkspaceStepsHelper.clearLamusDatabase(this.lamusDataSource);
        WorkspaceStepsHelper.clearCsDatabase(this.corpusstructureDataSource);
        WorkspaceStepsHelper.clearAmsDatabase(this.amsDataSource);
        WorkspaceStepsHelper.clearDirectories(this.archiveFolder, this.workspaceBaseDirectory, this.trashCanFolder);
        clearVariables();
    }
    
    private void clearVariables() {

        this.createdWorkspace = null;
        this.currentUserID = null;
//        this.selectedNodeID = -1;
        this.selectedNodeURI = null;
        this.createdWorkspaceID = -1;
//        this.createdWorkspaceTopNodeArchiveID = -1;
        this.createdWorkspaceTopNodeArchiveURI = null;
        this.createdWorkspaceTopNodeWsID = -1;
        this.newlyInsertedNodeUrl = null;
        this.oldNodeArchiveChecksum = null;
        
//        this.deletedNodeArchiveID = -1;
        this.deletedNodeArchiveURI = null;
        this.deletedNodeArchiveURL = null;
        this.deletedNodeWsID = -1;
        
        this.selectedTreeArchiveSnapshot = null;
        
        this.uploadedFileInOriginalLocation = null;
        this.uploadedFileNode = null;
        this.uploadedFileParentNode = null;
        
        this.unlinkedParentNode = null;
        this.unlinkedChildNode = null;
        
        this.numberOfChildNodesAfterLinking = -1;
    }
    
    
    @Given("an archive")
    public void anArchive() throws IOException {
        
        assertNotNull("archiveFolder null, was not correctly injected", this.archiveFolder);
        assertTrue("archiveFolder does not exist, was not properly created", this.archiveFolder.exists());

        WorkspaceStepsHelper.insertArchiveInDBFromScript(this.corpusstructureDataSource);
        
        WorkspaceStepsHelper.copyArchiveFromOriginalLocation(this.archiveFolder);
        
        assertNotNull("corpusstructureDataSource null, was not correctly injected", this.corpusstructureDataSource);
//        assertNotNull("archiveObjectsDB null, was not correctly injected", this.archiveObjectsDB);
//        assertNotNull("corpusStructureDB null, was not correctly injected", this.corpusStructureDB);
//        assertTrue("corpusstructure database was not initialised", this.archiveObjectsDB.getStatus());
        
        assertNotNull("corpusStructureProvider null, was not correctly injected", this.corpusStructureProvider);
    }
    
    @Given("a metadata node with URI $childNodeUriStr which is a child of node with URI $parentNodeUriStr")
    public void aNodeWithIDWhichIsAChildOfNodeWIthID(@Named("childNodeURI") String childNodeUriStr, @Named("parentNodeURI") String parentNodeUriStr)
            throws UnknownNodeException, URISyntaxException {
        
        URI childNodeURI = new URI(childNodeUriStr);
        URI parentNodeURI = new URI(parentNodeUriStr);
        
//        Node parentNode = this.corpusStructureDB.getNode(NodeIdUtils.TONODEID(parentNodeID));
//        assertNotNull("Node with ID " + parentNodeID + " does not exist in the corpusstructure database", parentNode);
        
        CorpusNode parentNode = this.corpusStructureProvider.getNode(parentNodeURI);

//        Node[] childNodes = this.corpusStructureDB.getChildrenNodes(NodeIdUtils.TONODEID(parentNodeID));
//        assertNotNull("Node with ID " + parentNodeID + " should have children", childNodes);
        
        List<CorpusNode> childNodes = this.corpusStructureProvider.getChildNodes(parentNodeURI);
        assertNotNull("Node with URI " + parentNodeURI + " should have children", childNodes);

//        Node childNode = this.corpusStructureDB.getNode(NodeIdUtils.TONODEID(childNodeID));
//        assertNotNull("Node with ID " + childNodeID + " does not exist in the corpusstructure database", childNode);
        
        CorpusNode childNode = this.corpusStructureProvider.getNode(childNodeURI);
        assertNotNull("Node with URI " + childNodeURI + " does not exist in the corpusstructure database", childNode);
    }
    
    @Given("a user with ID $userID that has read and write access to the node with URI $nodeUriStr")
    public void aUserWithID(String userID, String nodeUriStr) throws URISyntaxException {
        
        URI nodeURI = new URI(nodeUriStr);
        
        assertNotNull("amsDataSource null, was not correctly injected", this.amsDataSource);
        
        WorkspaceStepsHelper.insertAmsDataInDBFromScript(this.amsDataSource);
        
        assertNotNull("Principal with ID " + userID + " is null", this.ams2Bridge.getPrincipalSrv().getPrincipal(userID));
        
        //TODO FIX NODEID...
        //TODO FIX NODEID...
        //TODO FIX NODEID...
//        assertTrue("Principal with ID " + userID + " has no write access to the node with ID " + nodeID, this.ams2Bridge.hasWriteAccess(userID, NodeIdUtils.TONODEID(nodeID)));
        
        assertTrue("Principal with ID " + userID + " has no write access to the node with URI " + nodeURI, this.ams2Bridge.hasWriteAccess(userID, nodeURI));
        
        this.currentUserID = userID;
    }
    
    @Given("a workspace with ID $workspaceID created by $userID in node $topNodeArchiveUriStr")
    public void aWorkspaceWithIDCreatedByUserInNode(
            int workspaceID, String userID, String topNodeArchiveUriStr)
            throws MalformedURLException, FileNotFoundException, IOException, MetadataException, UnknownNodeException, URISyntaxException, WorkspaceNotFoundException, WorkspaceNodeNotFoundException {
        
        URI topNodeArchiveURI = new URI(topNodeArchiveUriStr);
        
        //TODO copy resource file (zorro pdf) to workspace folder
        //TODO link node in file (metadata api)
        //TODO link node in database (ws api)
            //TODO node should have status as "NEWLY ADDED"
        
        this.currentUserID = userID;
        this.createdWorkspaceID = workspaceID;
//        this.createdWorkspaceTopNodeArchiveID = topNodeArchiveID;
        this.createdWorkspaceTopNodeArchiveURI = topNodeArchiveURI;
        
//        this.selectedTreeArchiveSnapshot = WorkspaceStepsHelper.createSelectedTreeArchiveSnapshot(this.corpusStructureDB, this.archiveObjectsDB, this.createdWorkspaceTopNodeArchiveID);
        this.selectedTreeArchiveSnapshot = WorkspaceStepsHelper.createSelectedTreeArchiveSnapshot(this.corpusStructureProvider, this.createdWorkspaceTopNodeArchiveURI);
        
//        OurURL nodeArchiveURL = this.archiveObjectsDB.getObjectURL(NodeIdUtils.TONODEID(topNodeArchiveID), ArchiveAccessContext.getFileUrlContext());
//        String nodeArchiveFilename = FilenameUtils.getName(nodeArchiveURL.getPath());
        CorpusNode topCorpusNode = this.corpusStructureProvider.getNode(topNodeArchiveURI);
        URL topNodeURL = this.nodeResolver.getUrl(topCorpusNode);
        String topNodeArchiveFilename = FilenameUtils.getName(topNodeURL.getPath());
        
        // filesystem
        File workspaceDirectory = new File(this.workspaceBaseDirectory, "" + workspaceID);
        
        WorkspaceStepsHelper.insertWorkspaceInDBFromScript(this.lamusDataSource, workspaceID);
        
        WorkspaceStepsHelper.copyWorkspaceFromOriginalLocation(workspaceDirectory, workspaceID, topNodeArchiveFilename);
        
        assertTrue("Workspace directory for workspace " + workspaceID + " should have been created", workspaceDirectory.exists());
        
        File topNodeFile = new File(workspaceDirectory, topNodeArchiveFilename);
        assertTrue("File for node " + topNodeArchiveURI + " should have been created", topNodeFile.exists());

        
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
    public void fileHasBeenLinkedToTheWorkspace(String filename) throws MalformedURLException, UnknownNodeException {
        
//        OurURL nodeArchiveURL = archiveObjectsDB.getObjectURL(NodeIdUtils.TONODEID(this.createdWorkspaceTopNodeArchiveID), ArchiveAccessContext.getFileUrlContext());
        CorpusNode node = this.corpusStructureProvider.getNode(this.createdWorkspaceTopNodeArchiveURI);
        URL nodeArchiveURL = this.nodeResolver.getUrl(node);
        
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
        
        this.numberOfChildNodesAfterLinking = childNodes.size();
    }
    
    @Given("the top node has had some metadata added")
    public void theTopNodeHasHadSomeMetadataAdded() throws IOException, MetadataException, UnknownNodeException, WorkspaceNodeNotFoundException {
        
//        Node archiveNode = this.corpusStructureDB.getNode(NodeIdUtils.TONODEID(this.createdWorkspaceTopNodeArchiveID));
        CorpusNode archiveNode = this.corpusStructureProvider.getNode(this.createdWorkspaceTopNodeArchiveURI);
        assertNotNull(archiveNode);
        WorkspaceNode wsNode = this.workspaceDao.getWorkspaceTopNode(this.createdWorkspaceID);
        assertNotNull(wsNode);
        assertFalse("The name of the node in the workspace should be different from its old name in the archive", archiveNode.getName().equals(wsNode.getName()));
        
//        this.oldNodeArchiveChecksum = this.archiveObjectsDB.getObjectChecksum(NodeIdUtils.TONODEID(this.createdWorkspaceTopNodeArchiveID));
        this.oldNodeArchiveChecksum = archiveNode.getFileInfo().getChecksum();
        
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
                
                //TODO FIX NODEID...
                //TODO FIX NODEID...
                //TODO FIX NODEID...
//                this.deletedNodeArchiveID = node.getArchiveNodeID();
                this.deletedNodeArchiveURI = node.getArchiveURI();
                this.deletedNodeArchiveURL = node.getArchiveURL();
                this.deletedNodeWsID = node.getWorkspaceID();
                break;
            }
        }
        
        Collection<WorkspaceNode> nodeChildren = this.workspaceDao.getChildWorkspaceNodes(this.deletedNodeWsID);
        assertTrue("List of child nodes should be empty", nodeChildren.isEmpty());
        Collection<WorkspaceNode> nodeParents = this.workspaceDao.getParentWorkspaceNodes(this.deletedNodeWsID);
        assertTrue("List of parent nodes should be empty", nodeParents.isEmpty());
    }
    
    @Given("a $type file was uploaded into the workspace")
    public void aFileWasUploadedIntoTheWorkspace(String type) throws IOException {
        
        String filename = null;
        String fileLocationToUpload = null;
        WorkspaceNodeType nodeType = null;
        String fileMimetype = null;
        if("metadata".equals(type)) {
            filename = "RandomMetadataFile.cmdi";
            fileLocationToUpload = "test_files/files_to_upload/" + filename;
            nodeType = WorkspaceNodeType.METADATA;
            fileMimetype = "text/x-cmdi+xml";
        } else if("resource".equals(type)) {
            filename = "RandomWrittenResourceFile.txt";
            fileLocationToUpload = "test_files/files_to_upload/" + filename;
            nodeType = WorkspaceNodeType.RESOURCE;
            fileMimetype = "text/plain";
        }
        
        this.uploadedFileInOriginalLocation = fileLocationToUpload;
        
        File workspaceDirectory = new File(this.workspaceBaseDirectory, "" + this.createdWorkspaceID);
        File workspaceUploadDirectory = new File(workspaceDirectory, this.workspaceUplodaDirectoryName);
        File uploadedFile = new File(workspaceUploadDirectory, FilenameUtils.getName(this.uploadedFileInOriginalLocation));
        
        WorkspaceStepsHelper.copyFileToWorkspaceUploadDirectory(workspaceUploadDirectory, fileLocationToUpload);
        assertTrue("Uploaded file should exist in the upload directory of the workspace", uploadedFile.exists());
        
        int wsNodeID = 2;
        WorkspaceStepsHelper.insertNodeWithoutParentInWSDB(this.lamusDataSource, uploadedFile,
                this.createdWorkspaceID, wsNodeID, nodeType, WorkspaceNodeStatus.NODE_UPLOADED, null, fileMimetype);
        
        
        Collection<WorkspaceNode> nodesFound =
                WorkspaceStepsHelper.findWorkspaceNodeForFile(this.workspaceDao, this.createdWorkspaceID, uploadedFile);
        
        assertNotNull("Node for uploaded file not found: list null", nodesFound);
        assertFalse("Node for uploaded file not found: list empty", nodesFound.isEmpty());
        assertEquals("Found more than one node for uploaded file: list size = " + nodesFound.size(), 1, nodesFound.size());
        this.uploadedFileNode = nodesFound.iterator().next();
    }
    
    
    @When("that user chooses to create a workspace in the node with URI $nodeUriStr")
    public void thatUserChoosesToCreateAWorkspaceInTheNodeWithID(String nodeUriStr) throws URISyntaxException, UnknownNodeException, NodeAccessException, WorkspaceImportException {

        URI nodeURI = new URI(nodeUriStr);
        
//        this.selectedNodeID =  nodeID;
        this.selectedNodeURI = nodeURI;
        
        assertNotNull("corpusstructureDataSource null, was not correctly injected", this.corpusstructureDataSource);
        assertNotNull("amsDataSource null, was not correctly injected", this.amsDataSource);
        assertNotNull("lamusDataSource null, was not correctly injected", this.lamusDataSource);
        assertNotNull("workspaceDao null, was not correctly injected", this.workspaceDao);

        //TODO FIX NODEID...
        //TODO FIX NODEID...
        //TODO FIX NODEID...
//        this.createdWorkspace = workspaceService.createWorkspace(this.currentUserID, this.selectedNodeID);
        this.createdWorkspace = this.workspaceService.createWorkspace(this.currentUserID, this.selectedNodeURI);
        assertNotNull("createdWorkspace null just after 'createWorkspace' was called", this.createdWorkspace);
    }
    
    @When("that user chooses to delete the workspace")
    public void thatUserChoosesToDeleteTheWorkspace() throws WorkspaceNotFoundException, WorkspaceAccessException, IOException {
        
        assertNotNull("corpusstructureDataSource null, was not correctly injected", this.corpusstructureDataSource);
        assertNotNull("amsDataSource null, was not correctly injected", this.amsDataSource);
        assertNotNull("lamusDataSource null, was not correctly injected", this.lamusDataSource);
        assertNotNull("workspaceDao null, was not correctly injected", this.workspaceDao);

        this.workspaceService.deleteWorkspace(this.currentUserID, this.createdWorkspaceID);
    }
    
    @When("that user chooses to submit the workspace")
    public void thatUserChoosesToSubmitTheWorkspace() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceExportException {
        
        boolean keepUnlinkedFiles = Boolean.TRUE;
        
        assertNotNull("corpusstructureDataSource null, was not correctly injected", this.corpusstructureDataSource);
        assertNotNull("amsDataSource null, was not correctly injected", this.amsDataSource);
        assertNotNull("lamusDataSource null, was not correctly injected", this.lamusDataSource);
        assertNotNull("workspaceDao null, was not correctly injected", this.workspaceDao);
        
        //TODO more assertions missing?
        this.workspaceService.submitWorkspace(this.currentUserID, this.createdWorkspaceID/*, keepUnlinkedFiles*/);
        
//        assertTrue("Result of the workspace submission should be true", result);
    }
    
    @When("that user chooses to upload a $type file into the workspace")
    public void thatUserChoosesToUploadAFileIntoTheWorkspace(String type) throws IOException {
        
        assertNotNull("corpusstructureDataSource null, was not correctly injected", this.corpusstructureDataSource);
        assertNotNull("amsDataSource null, was not correctly injected", this.amsDataSource);
        assertNotNull("lamusDataSource null, was not correctly injected", this.lamusDataSource);
        assertNotNull("workspaceDao null, was not correctly injected", this.workspaceDao);
        
        String fileLocationToUpload = null;
        String filename = null;
        File outFile = null;
        if("metadata".equals(type)) {
            filename = "RandomMetadataFile.cmdi";
            fileLocationToUpload = "test_files/files_to_upload/" + filename;
             outFile = new File(this.workspaceBaseDirectory, filename);
        } else if("resource".equals(type)) {
            filename = "RandomWrittenResourceFile.txt";
            fileLocationToUpload = "test_files/files_to_upload/" + filename;
            outFile = new File(this.workspaceBaseDirectory, filename);
        }
        
        
        InputStream inStream = WorkspaceSteps.class.getClassLoader().getResourceAsStream(fileLocationToUpload);
        int availableBytes = inStream.available();
        
        FileItem fileItem = new DiskFileItem("fileUpload", "text/x-cmdi+xml", Boolean.FALSE, filename, availableBytes, outFile);
        OutputStream outStream = fileItem.getOutputStream();
        
        int read = 0;
        byte[] bytes = new byte[1024];
        while ((read = inStream.read(bytes)) != -1) {
            outStream.write(bytes, 0, read);
        }
        
        inStream.close();
        outStream.flush();
        outStream.close();

        Collection<FileItem> fileItems = new ArrayList<FileItem>();
        fileItems.add(fileItem);
        
//        this.workspaceService.uploadFilesIntoWorkspace(this.currentUserID, this.createdWorkspaceID, fileItems);
//        
//        this.uploadedFileInOriginalLocation = fileLocationToUpload;
        
        throw new UnsupportedOperationException("Adjust this story to cope with the refactored methods");
    }
    
    @When("that user chooses to link the uploaded node to the top node of the workspace")
    public void thatUserChoosesToLinkTheUploadedNodeIntoTheWorkspaceTree() throws WorkspaceNodeNotFoundException, WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {
        
        WorkspaceNode wsTopNode = this.workspaceDao.getWorkspaceTopNode(this.createdWorkspaceID);
        this.uploadedFileParentNode = wsTopNode;
        
        this.workspaceService.linkNodes(this.currentUserID, wsTopNode, this.uploadedFileNode);
    }
    
    @When("that user chooses to unlink a $type node from the workspace tree")
    public void thatUserChoosesToUnlinkANodeFromTheWorkspaceTree() throws WorkspaceNodeNotFoundException, WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {
        
        WorkspaceNode wsTopNode = this.workspaceDao.getWorkspaceTopNode(this.createdWorkspaceID);
        this.unlinkedParentNode = wsTopNode;
        
        Collection<WorkspaceNode> childNodes = this.workspaceDao.getChildWorkspaceNodes(wsTopNode.getWorkspaceNodeID());
        assertTrue("Should have only one child", childNodes.size() == 1);
        WorkspaceNode childNode = childNodes.iterator().next();
        this.unlinkedChildNode = childNode;
        
        this.workspaceService.unlinkNodes(this.currentUserID, wsTopNode, childNode);
    }
    
    @When("that user chooses to delete a $type node from the workspace tree")
    public void thatUserChoosesToDeleteANodeFromTheWorkspaceTree() throws WorkspaceNodeNotFoundException, WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {
        
        WorkspaceNode wsTopNode = this.workspaceDao.getWorkspaceTopNode(this.createdWorkspaceID);
        this.deletedWsNodeParent = wsTopNode;
        this.unlinkedParentNode = wsTopNode;
        
        Collection<WorkspaceNode> childNodes = this.workspaceDao.getChildWorkspaceNodes(wsTopNode.getWorkspaceNodeID());
        assertTrue("Should have only one child", childNodes.size() == 1);
        WorkspaceNode childNode = childNodes.iterator().next();
        this.deletedWsNode = childNode;
        this.unlinkedChildNode = childNode;
        
        this.workspaceService.deleteNode(this.currentUserID, childNode);
    }
    
    
    @Then("the workspace is created in the database")
    public void theWorkspaceIsCreatedInTheDatabase() throws InterruptedException, WorkspaceNotFoundException, WorkspaceNodeNotFoundException {
        
        // database
        Workspace retrievedWorkspace = this.workspaceDao.getWorkspace(this.createdWorkspace.getWorkspaceID());
        WorkspaceNode retrievedWorkspaceTopNode = this.workspaceDao.getWorkspaceNode(retrievedWorkspace.getTopNodeID());
        assertNotNull("retrievedWorkspace null, was not properly created in the database", retrievedWorkspace);
        assertEquals("currentUserID (" + this.currentUserID + ") does not match the user ID in the retrieved workspace (" + retrievedWorkspace.getUserID() + ")", this.currentUserID, retrievedWorkspace.getUserID());
        
        //TODO FIX NODEID...
        //TODO FIX NODEID...
        //TODO FIX NODEID...
//        assertEquals("selectedNodeID (" + this.selectedNodeID + ") does not match the ID of the top node in the retrieved workspace (" + retrievedWorkspaceTopNode.getArchiveNodeID() + ")",
//                this.selectedNodeID, retrievedWorkspaceTopNode.getArchiveNodeID());
        assertEquals("selectedNodeURI (" + this.selectedNodeURI + ") does not match the ID of the top node in the retrieved workspace (" + retrievedWorkspaceTopNode.getArchiveURI() + ")",
                this.selectedNodeURI, retrievedWorkspaceTopNode.getArchiveURI());
        
        
        assertEquals("retrieved workspace does not have the expected status (expected = " + WorkspaceStatus.INITIALISED + "; retrieved = " + retrievedWorkspace.getStatus() + ")", WorkspaceStatus.INITIALISED, retrievedWorkspace.getStatus());
        
        //TODO check all nodes that should be there...
    }
    
    @Then("the workspace files are present in the proper location in the filesystem")
    public void theWorkspaceFilesArePresentInTheProperLocationInTheFilesystem() throws UnknownNodeException {
        
        // filesystem
        File workspaceDirectory = new File(this.workspaceBaseDirectory, "" + this.createdWorkspace.getWorkspaceID());
        assertTrue("Workspace directory for workspace " + this.createdWorkspace.getWorkspaceID() + " does not exist", workspaceDirectory.exists());
        
        //TODO check all files that should be there...
        
//        OurURL wsTopNodeURL = this.archiveObjectsDB.getObjectURL(NodeIdUtils.TONODEID(this.selectedNodeID), ArchiveAccessContext.getFileUrlContext());
        CorpusNode selectedNode = this.corpusStructureProvider.getNode(this.selectedNodeURI);
        URL selectedNodeURL = this.nodeResolver.getUrl(selectedNode);
        String wsTopNodeFilename = FilenameUtils.getName(selectedNodeURL.toString());
        File wsTopNodeFile = new File(workspaceDirectory, wsTopNodeFilename);
        assertTrue("Top node file doesn't exist in the workspace directory", wsTopNodeFile.exists());
        
        // the workspace won't include the child nodes, unless they are also metadata nodes
    }
    
    @Then("the workspace is removed both from database and filesystem")
    public void theWorkspaceIsDeleted() throws WorkspaceNotFoundException {
        
        // filesystem
        File workspaceDirectory = new File(this.workspaceBaseDirectory, "" + this.createdWorkspaceID);
        assertFalse("Workspace directory for workspace " + this.createdWorkspaceID + " still exists", workspaceDirectory.exists());
        
        // database
        Workspace retrievedWorkspace = this.workspaceDao.getWorkspace(this.createdWorkspaceID);
        assertNull("retrievedWorkspace not null, was not properly deleted from the database", retrievedWorkspace);
        Collection<WorkspaceNode> retrievedNodes = this.workspaceDao.getNodesForWorkspace(this.createdWorkspaceID);
        assertTrue("There should be no nodes associated with the deleted workspace", retrievedNodes.isEmpty());
    }
    
    @Then("the status of the workspace is marked as successfully submitted")
    public void theWorkspaceStatusIsMarkedAsSuccessfullySubmitted() throws WorkspaceNotFoundException {
        
        //TODO assert that the workspace is now marked as successfully submitted
            //TODO (maybe some other assertions)
        
        Workspace workspace = this.workspaceDao.getWorkspace(this.createdWorkspaceID);
        
        assertNotNull(workspace);
        assertEquals("Workspace status different from expected", WorkspaceStatus.SUBMITTED, workspace.getStatus());
        
        //TODO some other assertions?
    }
    
    @Then("the end date of the workspace is set")
    public void theEndDateOfTheWorkspaceWithIDIsSet() throws WorkspaceNotFoundException {
        
        Workspace workspace = this.workspaceDao.getWorkspace(this.createdWorkspaceID);
        
        assertNotNull(workspace);
        assertNotNull("End date of workspace should be set", workspace.getEndDate());
    }
    
    @Then("the new node is properly linked in the database, from parent node with URI $parentNodeUriStr")
    public void theNewNodeIsProperlyLinkedInTheDatabaseFromTheParentNodeWithID(String parentNodeUriStr) throws UnknownNodeException, URISyntaxException {
        
        URI parentNodeURI = new URI(parentNodeUriStr);
        
        //TODO assert that the new node is linked in the database
        
//        URL parentNodeURL = this.archiveObjectsDB.getObjectURL(NodeIdUtils.TONODEID(parentNodeID), ArchiveAccessContext.FILE_UX_URL).toURL();
        CorpusNode parentNode = this.corpusStructureProvider.getNode(parentNodeURI);
        URL parentNodeURL = this.nodeResolver.getUrl(parentNode);
        assertNotNull(parentNodeURL);

//        Node[] children = this.corpusStructureDB.getChildrenNodes(NodeIdUtils.TONODEID(parentNodeID));
        List<CorpusNode> children = this.corpusStructureProvider.getChildNodes(parentNodeURI);
        assertNotNull(children);
//        assertTrue("The node should have linked children in the database", children.length > 0);
        assertTrue("The node should have linked children in the database", !children.isEmpty());
        
        // URL is already confirmed above (URL retrieved for nodeID
        
    }
    
    @Then("the new node is properly linked from the parent file (node with URI $parentNodeUriStr) and was assigned an archive handle")
    public void theNewNodeIsProperlyLinkedFromTheParentFileAndWasAssignedAnArchiveHandle(String parentNodeUriStr)
            throws IOException, MetadataException, UnknownNodeException, URISyntaxException {
        
        URI parentNodeURI = new URI(parentNodeUriStr);
        
//        URL parentNodeURL = this.archiveObjectsDB.getObjectURL(NodeIdUtils.TONODEID(parentNodeID), ArchiveAccessContext.getFileUrlContext()).toURL();
        CorpusNode parentNode = this.corpusStructureProvider.getNode(parentNodeURI);
        URL parentNodeURL = this.nodeResolver.getUrl(parentNode);
        MetadataDocument parentDocument = this.metadataAPI.getMetadataDocument(parentNodeURL);
        
        assertNotNull("Metadata document " + parentNodeURL + " should not be null", parentDocument);
        assertTrue("Metadata document " + parentNodeURL + " should contain references",
                parentDocument instanceof ReferencingMetadataDocument);
        ReferencingMetadataDocument referencingParentDocument = (ReferencingMetadataDocument) parentDocument;
        List<Reference> childReferences = referencingParentDocument.getDocumentReferences();
        assertNotNull("List of references in metadata document " + parentNodeURL + " should not be null", childReferences);
        assertTrue("Metadata document " + parentNodeURL + " should have references", childReferences.size() > 0);
        
        assertEquals("Number of child references is different from expected", this.numberOfChildNodesAfterLinking, childReferences.size());
        
        boolean allChildrenHaveHandle = Boolean.TRUE;
        
        for(Reference child : childReferences) {
            
            if(child instanceof HandleCarrier) {
                URI handle = ((HandleCarrier) child).getHandle();
                if(handle != null) {
                    continue;
                }
            }
            allChildrenHaveHandle = Boolean.FALSE;
            break;
        }
        
        assertTrue("Child didn't get a handle assigned", allChildrenHaveHandle);
    }
    
    @Then("the new node exists in the corpusstructure database and is properly linked there")
    public void theNewNodeExistsInTheCorpusstructureDatabaseAndIsProperlyLinkedThere() {
        
        //TODO this should still fail because the node will only be added to the database by the crawler, which is not implemented yet
        
        fail("crawler is not implemented yet");
    }
    
    @Then("$filename is present in the proper location in the filesystem, under the directory of the parent node $parentNodeUriStr")
    public void theNewFileIsPresentInTheProperLocationInTheFilesystemUnderTheDirectoryOfTheParentNode(String filename, String parentNodeUriStr)
            throws UnknownNodeException, URISyntaxException, MalformedURLException {
        
        URI parentNodeURI = new URI(parentNodeUriStr);
        
//        URL parentURL = this.archiveObjectsDB.getObjectURL(NodeIdUtils.TONODEID(parentNodeID), ArchiveAccessContext.FILE_UX_URL).toURL();
        CorpusNode parentNode = this.corpusStructureProvider.getNode(parentNodeURI);
        URL parentURL = this.nodeResolver.getUrl(parentNode);
        
        String parentPath = FilenameUtils.getFullPathNoEndSeparator(parentURL.getPath());
        
        
        File expectedNodeFile = WorkspaceStepsHelper.getExpectedFileLocationForChildNode(filename, parentPath, parentURL);
        
        this.newlyInsertedNodeUrl = expectedNodeFile.toURI().toURL();
        
        assertTrue("New file doesn't exist in the expected location: " + expectedNodeFile.getAbsolutePath(), expectedNodeFile.exists());
        
    }
    
    @Then("the children of $filename are also present in the filesystem and were assigned archive handles")
    public void theChildrenOfFileAreAlsoPresentInTheFilesystemAndWereAssignedArchiveHandles(String filename)
            throws IOException, MetadataException {
        
        MetadataDocument document = this.metadataAPI.getMetadataDocument(this.newlyInsertedNodeUrl);
        assertTrue("Metadata document doesn't contain references" , document instanceof ReferencingMetadataDocument);
        
        List<Reference> references = ((ReferencingMetadataDocument) document).getDocumentReferences();
        assertNotNull("List of references in metadata document shouldn't be null", references);
        assertTrue("List of references in metadata document should have size larger than 0", references.size() > 0);
        
        for(Reference ref : references) {
            assertTrue("Reference should contain a handle", ref instanceof HandleCarrier);
            URI handle = ((HandleCarrier) ref).getHandle();
            assertNotNull("Handle should not be null", handle);
            
            URL refResolvedURL = this.identifierResolver.resolveIdentifier(document, ref.getURI());
            File refResolvedFile = FileUtils.toFile(refResolvedURL);
            assertTrue("Child " + refResolvedURL + " does not exist in the expected location", refResolvedFile.exists());
        }
    }
    
    @Then("the children of $filename are also present in the database")
    public void theChildrenOfFileAreAlsoPresentInTheDatabase(String filename)
            throws IOException, MetadataException, UnknownNodeException {
        
        MetadataDocument document = this.metadataAPI.getMetadataDocument(this.newlyInsertedNodeUrl);
        assertTrue("Metadata document doesn't contain references" , document instanceof ReferencingMetadataDocument);
        List<Reference> references = ((ReferencingMetadataDocument) document).getDocumentReferences();
        assertNotNull("List of references in metadata document shouldn't be null", references);
        assertTrue("List of references in metadata document should have size larger than 0", references.size() > 0);
        
        for(Reference ref : references) {
            
            CorpusNode refNode = this.corpusStructureProvider.getNode(ref.getURI());
            URL refURL = this.nodeResolver.getUrl(refNode);
            assertNotNull("Child not found in database", refURL);
            
//            String refID = this.archiveObjectsDB.getObjectId(refURL);
//            OurURL refURLWithContext = this.archiveObjectsDB.getObjectURL(refID, ArchiveAccessContext.FILE_UX_URL);
            
//            File refFile = new File(refURLWithContext.getPath());
//            File refFile = new File(refURL.getPath());
//            assertTrue("Child doesn't exist in the filesystem", refFile.exists());
        }
    }
    
    @Then("the metadata of the node with URI $archiveNodeUriStr has changed in the filesystem")
    public void theMetadataOfTheNodeWithURIHasChangedInTheFilesystem(String archiveNodeUriStr)
            throws UnknownNodeException, URISyntaxException {
        
        URI archiveNodeURI = new URI(archiveNodeUriStr);
        CorpusNode changedNode = this.corpusStructureProvider.getNode(archiveNodeURI);
        URL changedNodeURL = this.nodeResolver.getUrl(changedNode);
        String changedNodeChecksum = Checksum.create(changedNodeURL.getPath());
        
        assertFalse("Name of the node in the archive should have changed", this.oldNodeArchiveChecksum.equals(changedNodeChecksum));
    }
    
    @Then("the metadata of the node with URI $archiveNodeUriStr has changed in the database")
    public void theMetadataOfTheNodeWithURIHasChangedInTheDatabase(String archiveNodeUriStr) throws URISyntaxException, UnknownNodeException {

        URI archiveNodeURI = new URI(archiveNodeUriStr);
        CorpusNode changedNode = this.corpusStructureProvider.getNode(archiveNodeURI);
        String changedNodeChecksum = changedNode.getFileInfo().getChecksum();
        
        assertFalse("Name of the node in the archive should have changed", this.oldNodeArchiveChecksum.equals(changedNodeChecksum));
        
    }
    
    @Then("the deleted node has been moved to the trash folder in the filesystem")
    public void theDeletedNodeIsMarkedAsDeletedInTheDatabaseAndShouldNotBeLinkedToAnyNode() throws UnknownNodeException {
        
        File expectedFile = WorkspaceStepsHelper.getExpectedFileLocationForDeletedNode(
                this.trashCanFolder, this.createdWorkspaceID, this.deletedNodeArchiveURL, this.deletedNodeArchiveURI);
        
        assertTrue("Deleted file is not present in expected trash can location", expectedFile.exists());
        
    }
    
    @Then("the deleted node has been updated in the database")
    public void theDeletedNodeHasBeenUpdatedInTheDatabase() throws UnknownNodeException {
        
        File expectedFile = WorkspaceStepsHelper.getExpectedFileLocationForDeletedNode(
                this.trashCanFolder, this.createdWorkspaceID, this.deletedNodeArchiveURL, this.deletedNodeArchiveURI);
        
        CorpusNode deletedNode = this.corpusStructureProvider.getNode(this.deletedNodeArchiveURI);
        URL deletedNodeDbURL = this.nodeResolver.getUrl(deletedNode);
        String deletedNodeDbPath = deletedNodeDbURL.getPath();
        File deletedNodeFile = new File(deletedNodeDbPath);
        assertNotNull("Deleted node file object should not be null", deletedNodeFile);
        assertEquals("Deleted node location in DB is different from expected", expectedFile, deletedNodeFile);
    }
    
    @Then("no changes were made to the archive")
    public void noChangesWereMadeToTheArchive() throws UnknownNodeException {
        
//        TreeSnapshot finalSelectedTreeSnapshot = WorkspaceStepsHelper.createSelectedTreeArchiveSnapshot(this.corpusStructureDB, this.archiveObjectsDB, this.createdWorkspaceTopNodeArchiveID);
        TreeSnapshot finalSelectedTreeSnapshot = WorkspaceStepsHelper.createSelectedTreeArchiveSnapshot(this.corpusStructureProvider, this.createdWorkspaceTopNodeArchiveURI);
        boolean snapshotsAreSimilar = finalSelectedTreeSnapshot.equals(this.selectedTreeArchiveSnapshot);
        assertTrue("Snapshot of the selected tree different from expected", snapshotsAreSimilar);
    }
    
    @Then("the uploaded node is present in the workspace folder")
    public void theUploadedNodeIsPresentInTheWorkspaceFolder() {
        
        File workspaceDirectory = new File(this.workspaceBaseDirectory, "" + this.createdWorkspaceID);
        File workspaceUploadDirectory = new File(workspaceDirectory, this.workspaceUplodaDirectoryName);
        File uploadedFile = new File(workspaceUploadDirectory, FilenameUtils.getName(this.uploadedFileInOriginalLocation));
        
        assertTrue("File doesn't exist in expected location", uploadedFile.exists());
    }
    
    @Then("the uploaded node is present in the database")
    public void theUploadedNodeIsPresentInTheDatabase() {
        
        File workspaceDirectory = new File(this.workspaceBaseDirectory, "" + this.createdWorkspaceID);
        File workspaceUploadDirectory = new File(workspaceDirectory, this.workspaceUplodaDirectoryName);
        File uploadedFile = new File(workspaceUploadDirectory, FilenameUtils.getName(this.uploadedFileInOriginalLocation));
        
        Collection<WorkspaceNode> nodesFound =
                WorkspaceStepsHelper.findWorkspaceNodeForFile(this.workspaceDao, this.createdWorkspaceID, uploadedFile);
        
        assertNotNull("Node for uploaded file not found: list null", nodesFound);
        assertFalse("Node for uploaded file not found: list empty", nodesFound.isEmpty());
        assertEquals("Found more than one node for uploaded file: list size = " + nodesFound.size(), 1, nodesFound.size());
        this.uploadedFileNode = nodesFound.iterator().next();
    }
    
    @Then("the uploaded node has no parent node")
    public void theUploadedNodeHasNoParentNode() {
        
        Collection<WorkspaceNode> parentNodes = this.workspaceDao.getParentWorkspaceNodes(this.uploadedFileNode.getWorkspaceNodeID());
        
        assertNotNull("Parents list should not be null", parentNodes);
        assertTrue("Parents list should be empty", parentNodes.isEmpty());
    }
    
    @Then("that node is linked to the parent node in the database")
    public void thatNodeIsLinkedToTheParentNodeInTheDatabase() {
        
        Collection<WorkspaceNode> childNodes =
                this.workspaceDao.getChildWorkspaceNodes(this.uploadedFileParentNode.getWorkspaceNodeID());
        
        boolean nodeFound = Boolean.FALSE;
        for(WorkspaceNode node : childNodes) {
            if(this.uploadedFileNode.getWorkspaceNodeID() == node.getWorkspaceNodeID()) {
                nodeFound = true;
                break;
            }
        }
        
        assertTrue("Uploaded node not found among child nodes", nodeFound);
    }
    
    @Then("that node is included in the parent node, as a reference")
    public void thatNodeIsIncludedInTheParentNodeAsAReference() throws IOException, MetadataException, URISyntaxException {
        
        MetadataDocument tempParentDocument =
                this.metadataAPI.getMetadataDocument(this.uploadedFileParentNode.getWorkspaceURL());
        assertTrue("Parent document not a ReferencingMetadataDocument", tempParentDocument instanceof ReferencingMetadataDocument);

        ReferencingMetadataDocument parentDocument = (ReferencingMetadataDocument) tempParentDocument;
        List<Reference> references = parentDocument.getDocumentReferences();
        
        boolean referenceFound = false;
        for(Reference ref : references) {
            if(this.uploadedFileNode.getWorkspaceURL().toURI().equals(ref.getURI())) {
                referenceFound = true;
                break;
            }
        }
        
        assertTrue("Child reference not found", referenceFound);
    }
    
    @Then("that node in no longer linked to the former parent node in the database")
    public void thatNodeIsNoLongerLinkedToTheFormerParentnodeInTheDatabase() {
        
        Collection<WorkspaceNode> childNodes =
                this.workspaceDao.getChildWorkspaceNodes(this.unlinkedParentNode.getWorkspaceNodeID());
        
        boolean nodeFound = Boolean.FALSE;
        for(WorkspaceNode node : childNodes) {
            if(this.unlinkedChildNode.getWorkspaceNodeID() == node.getWorkspaceNodeID()) {
                nodeFound = true;
                break;
            }
        }
        
        assertFalse("Unlinked node should not be linked to the former parent", nodeFound);
    }
    
    @Then("that node is no longer included in the former parent node, as a reference")
    public void thatNodeIsNoLongerIncludedInTheFormerParentNodeAsAReference() throws IOException, MetadataException, URISyntaxException {
        
        MetadataDocument tempParentDocument =
                this.metadataAPI.getMetadataDocument(this.unlinkedParentNode.getWorkspaceURL());
        assertTrue("Parent document not a ReferencingMetadataDocument", tempParentDocument instanceof ReferencingMetadataDocument);

        ReferencingMetadataDocument parentDocument = (ReferencingMetadataDocument) tempParentDocument;
        List<Reference> references = parentDocument.getDocumentReferences();
        
        boolean referenceFound = false;
        for(Reference ref : references) {
            if(this.unlinkedChildNode.getWorkspaceURL().toURI().equals(ref.getURI())) {
                referenceFound = true;
                break;
            }
        }
        
        assertFalse("Reference to unlinked child should not exist in the former parent", referenceFound);
    }
    
    @Then("that node is marked as deleted in the database")
    public void thatNodeIsMarkedAsDeletedInTheDatabase() throws WorkspaceNodeNotFoundException {
        
        WorkspaceNode retrievedNode =
                this.workspaceDao.getWorkspaceNode(this.deletedWsNode.getWorkspaceNodeID());
        
        assertEquals("Node should be set as deleted", WorkspaceNodeStatus.NODE_DELETED, retrievedNode.getStatus());
    }
    
    //TODO OTHER CHECKS MISSING... ANNEX, CRAWLER AND OTHER STUFF
    
}
