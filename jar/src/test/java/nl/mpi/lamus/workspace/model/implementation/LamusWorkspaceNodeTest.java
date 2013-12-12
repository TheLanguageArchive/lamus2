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
package nl.mpi.lamus.workspace.model.implementation;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.WorkspaceParentNodeReference;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceNodeTest {
    
    private int workspaceNodeID = 10;
    private int workspaceID = 1;
    private URI profileSchemaURI;
    private String workspaceNodeName = "nodeName";
    private String workspaceNodeTitle = "nodeTitle";
    private WorkspaceNodeType workspaceNodeType = WorkspaceNodeType.METADATA;
    private URL workspaceNodeURL;
    private URI archiveNodeURI;
    private URL archiveNodeURL;
    private URL originNodeURL;
    private WorkspaceNodeStatus workspaceNodeStatus = WorkspaceNodeStatus.NODE_ISCOPY;
    private String workspaceNodeFormat = "someFormat";
    
    public LamusWorkspaceNodeTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() throws URISyntaxException, MalformedURLException {
        
        this.profileSchemaURI = new URI("http://some.uri/file.xsd");
        this.workspaceNodeURL = new URL("file:/workspace/some.uri/file.cmdi");
        this.archiveNodeURI = new URI(UUID.randomUUID().toString());
        this.archiveNodeURL = new URL("file:/archive/some.url/file.cmdi");
        this.originNodeURL = this.archiveNodeURL;
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test for one of the constructors
     */
    @Test
    public void constructorWithAllParametersProperlyCreatesWorkspace() {

        WorkspaceNode testNode = new LamusWorkspaceNode(
                this.workspaceNodeID, this.workspaceID,
                this.profileSchemaURI, this.workspaceNodeName, this.workspaceNodeTitle,
                this.workspaceNodeType, this.workspaceNodeURL, this.archiveNodeURI,
                this.archiveNodeURL, this.originNodeURL,
                this.workspaceNodeStatus, this.workspaceNodeFormat);
        
        assertEquals("Value for 'workspaceNodeID' is not the expected one.", this.workspaceNodeID, testNode.getWorkspaceNodeID());
        assertEquals("Value for 'workspaceID' is not the expected one.", this.workspaceID, testNode.getWorkspaceID());
        assertEquals("Value for 'profileSchemaURI' is not the expected one.", this.profileSchemaURI, testNode.getProfileSchemaURI());
        assertEquals("Value for 'name' is not the expected one.", this.workspaceNodeName, testNode.getName());
        assertEquals("Value for 'title' is not the expected one.", this.workspaceNodeTitle, testNode.getTitle());
        assertEquals("Value for 'type' is not the expected one.", this.workspaceNodeType, testNode.getType());
        assertEquals("Value for 'workspaceURL' is not the expected one.", this.workspaceNodeURL, testNode.getWorkspaceURL());
        assertEquals("Value for 'archiveURI' is not the expected one.", this.archiveNodeURI, testNode.getArchiveURI());
        assertEquals("Value for 'originURL' is not the expected one.", this.originNodeURL, testNode.getOriginURL());
        assertEquals("Value for 'status' is not the expected one.", this.workspaceNodeStatus, testNode.getStatus());
        assertEquals("Value for 'format' is not the expected one.", this.workspaceNodeFormat, testNode.getFormat());
    }
    
    //TODO test unmodifiable collection (parent references)
    
    /**
     * Test of equals method, of class LamusWorkspace.
     */
    @Test
    public void nodesAreEqual() {

        WorkspaceNode testWorkspaceNode1 = new LamusWorkspaceNode(
                this.workspaceNodeID, this.workspaceID,
                this.profileSchemaURI, this.workspaceNodeName, this.workspaceNodeTitle,
                this.workspaceNodeType, this.workspaceNodeURL, this.archiveNodeURI,
                this.archiveNodeURL, this.originNodeURL,
                this.workspaceNodeStatus, this.workspaceNodeFormat);
        
        WorkspaceNode testWorkspaceNode2 = new LamusWorkspaceNode(
                this.workspaceNodeID, this.workspaceID,
                this.profileSchemaURI, this.workspaceNodeName, this.workspaceNodeTitle,
                this.workspaceNodeType, this.workspaceNodeURL, this.archiveNodeURI,
                this.archiveNodeURL, this.originNodeURL,
                this.workspaceNodeStatus, this.workspaceNodeFormat);
        
        assertEquals("Workspace objects are not equal.", testWorkspaceNode1, testWorkspaceNode2);
    }
    
    @Test
    public void nodesHaveSameHashCode() {

        WorkspaceNode testWorkspaceNode1 = new LamusWorkspaceNode(
                this.workspaceNodeID, this.workspaceID,
                this.profileSchemaURI, this.workspaceNodeName, this.workspaceNodeTitle,
                this.workspaceNodeType, this.workspaceNodeURL, this.archiveNodeURI,
                this.archiveNodeURL, this.originNodeURL,
                this.workspaceNodeStatus, this.workspaceNodeFormat);
        
        WorkspaceNode testWorkspaceNode2 = new LamusWorkspaceNode(
                this.workspaceNodeID, this.workspaceID,
                this.profileSchemaURI, this.workspaceNodeName, this.workspaceNodeTitle,
                this.workspaceNodeType, this.workspaceNodeURL, this.archiveNodeURI,
                this.archiveNodeURL, this.originNodeURL,
                this.workspaceNodeStatus, this.workspaceNodeFormat);
        
        assertEquals("Workspace objects don't have the same hashcode.", testWorkspaceNode1.hashCode(), testWorkspaceNode2.hashCode());
    }
    
    /**
     * Test of equals method, of class LamusWorkspace.
     */
    @Test
    public void nodesAreNotEqual() throws MalformedURLException {
        
        URL differentURL = new URL("http://some/different/url");
        WorkspaceNode testWorkspaceNode1 = new LamusWorkspaceNode(
                this.workspaceNodeID, this.workspaceID,
                this.profileSchemaURI, this.workspaceNodeName, this.workspaceNodeTitle,
                this.workspaceNodeType, this.workspaceNodeURL, this.archiveNodeURI,
                this.archiveNodeURL, this.originNodeURL,
                this.workspaceNodeStatus, this.workspaceNodeFormat);
        
        WorkspaceNode testWorkspaceNode2 = new LamusWorkspaceNode(
                this.workspaceNodeID, this.workspaceID,
                this.profileSchemaURI, this.workspaceNodeName, this.workspaceNodeTitle,
                this.workspaceNodeType, differentURL, this.archiveNodeURI,
                this.archiveNodeURL, this.originNodeURL,
                this.workspaceNodeStatus, this.workspaceNodeFormat);
        
        assertFalse("Workspace objects should not be equal.", testWorkspaceNode1.equals(testWorkspaceNode2));
    }
    
    @Test
    public void nodesHaveDifferentHashCodes() throws MalformedURLException {
        
        URL differentURL = new URL("http://some/different/url");
        WorkspaceNode testWorkspaceNode1 = new LamusWorkspaceNode(
                this.workspaceNodeID, this.workspaceID,
                this.profileSchemaURI, this.workspaceNodeName, this.workspaceNodeTitle,
                this.workspaceNodeType, this.workspaceNodeURL, this.archiveNodeURI,
                this.archiveNodeURL, this.originNodeURL,
                this.workspaceNodeStatus, this.workspaceNodeFormat);
        
        WorkspaceNode testWorkspaceNode2 = new LamusWorkspaceNode(
                this.workspaceNodeID, this.workspaceID,
                this.profileSchemaURI, this.workspaceNodeName, this.workspaceNodeTitle,
                this.workspaceNodeType, differentURL, this.archiveNodeURI,
                this.archiveNodeURL, this.originNodeURL,
                this.workspaceNodeStatus, this.workspaceNodeFormat);
        
        assertFalse("Workspace objects should not have the same hashcode.", testWorkspaceNode1.hashCode() == testWorkspaceNode2.hashCode());
    }
    
    @Test
    public void nodesComparedWithObjectOfDifferentType() {

        WorkspaceNode testWorkspaceNode1 = new LamusWorkspaceNode(
                this.workspaceNodeID, this.workspaceID,
                this.profileSchemaURI, this.workspaceNodeName, this.workspaceNodeTitle,
                this.workspaceNodeType, this.workspaceNodeURL, this.archiveNodeURI,
                this.archiveNodeURL, this.originNodeURL,
                this.workspaceNodeStatus, this.workspaceNodeFormat);
        
        WorkspaceNode testWorkspaceNode2 = new SomeOtherWorkspaceNode(
                this.workspaceNodeID, this.workspaceID,
                this.profileSchemaURI, this.workspaceNodeName, this.workspaceNodeTitle,
                this.workspaceNodeType, this.workspaceNodeURL, this.archiveNodeURI,
                this.archiveNodeURL, this.originNodeURL,
                this.workspaceNodeStatus, this.workspaceNodeFormat);
        
        assertFalse("Workspace objects should not be equal.", testWorkspaceNode1.equals(testWorkspaceNode2));
    }
    
    @Test
    public void testToString() {
        
        WorkspaceNode testWorkspaceNode = new LamusWorkspaceNode(
                this.workspaceNodeID, this.workspaceID,
                this.profileSchemaURI, this.workspaceNodeName, this.workspaceNodeTitle,
                this.workspaceNodeType, this.workspaceNodeURL, this.archiveNodeURI,
                this.archiveNodeURL, this.originNodeURL,
                this.workspaceNodeStatus, this.workspaceNodeFormat);
        String expectedString = "Workspace Node ID: " + testWorkspaceNode.getWorkspaceNodeID()
                + ", Workspace ID: " + testWorkspaceNode.getWorkspaceID()
                + ", Profile Schema URI: " + testWorkspaceNode.getProfileSchemaURI()
                + ", Name: " + testWorkspaceNode.getName()
                + ", Title: " + testWorkspaceNode.getTitle()
                + ", Type: " + testWorkspaceNode.getType()
                + ", Workspace URL: " + testWorkspaceNode.getWorkspaceURL()
                + ", Archive URI: " + testWorkspaceNode.getArchiveURI()
                + ", Archive URL: " + testWorkspaceNode.getArchiveURL()
                + ", Origin URL: " + testWorkspaceNode.getOriginURL()
                + ", Status: " + testWorkspaceNode.getStatus()
                + ", Format: " + testWorkspaceNode.getFormat();
        
        String actualString = testWorkspaceNode.toString();
        
        assertEquals(expectedString, actualString);
    }
}

class SomeOtherWorkspaceNode implements WorkspaceNode {
    
    private int workspaceNodeID;
    private int workspaceID;
    private URI profileSchemaURI;
    private String name;
    private String title;
    private WorkspaceNodeType type;
    private URL workspaceURL;
    private URI archiveURI;
    private URL archiveURL;
    private URL originURL;
    private WorkspaceNodeStatus status;
    private String format;
    private Collection<WorkspaceParentNodeReference> parentNodesReferences;
    
    public SomeOtherWorkspaceNode() {
        
    }
    
    public SomeOtherWorkspaceNode(int workspaceNodeID, int workspaceID,
            URI profileSchemaURI, String name, String title, WorkspaceNodeType type,
            URL workspaceURL, URI archiveURI, URL archiveURL, URL originURL,
            WorkspaceNodeStatus status, String format) {
        
        this.workspaceNodeID = workspaceNodeID;
        this.workspaceID = workspaceID;
        this.profileSchemaURI = profileSchemaURI;
        this.name = name;
        this.title = title;
        this.type = type;
        this.workspaceURL = workspaceURL;
        this.archiveURI = archiveURI;
        this.archiveURL = archiveURL;
        this.originURL = originURL;
        this.status = status;
        this.format = format;
    }

    @Override
    public int getWorkspaceNodeID() {
        return this.workspaceNodeID;
    }
    
    @Override
    public void setWorkspaceNodeID(int workspaceNodeID) {
        this.workspaceNodeID = workspaceNodeID;
    }

    @Override
    public int getWorkspaceID() {
        return this.workspaceID;
    }
    
    @Override
    public void setWorkspaceID(int workspaceID) {
        this.workspaceID = workspaceID;
    }

    @Override
    public URI getProfileSchemaURI() {
        return this.profileSchemaURI;
    }
    
    @Override
    public void setProfileSchemaURI(URI profileSchemaURI) {
        this.profileSchemaURI = profileSchemaURI;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }
    
    @Override
    public WorkspaceNodeType getType() {
        return this.type;
    }
    
    @Override
    public boolean isMetadata() {
        return WorkspaceNodeType.METADATA.equals(this.type);
    }
    
    @Override
    public void setType(WorkspaceNodeType type) {
        this.type = type;
    }

    @Override
    public URL getWorkspaceURL() {
        return this.workspaceURL;
    }
    
    @Override
    public void setWorkspaceURL(URL workspaceURL) {
        this.workspaceURL = workspaceURL;
    }

    @Override
    public URI getArchiveURI() {
        return this.archiveURI;
    }
    
    @Override
    public void setArchiveURI(URI archiveURI) {
        this.archiveURI = archiveURI;
    }
    
    @Override
    public URL getArchiveURL() {
        return this.archiveURL;
    }
    
    @Override
    public void setArchiveURL(URL archiveURL) {
        this.archiveURL = archiveURL;
    }

    @Override
    public URL getOriginURL() {
        return this.originURL;
    }
    
    @Override
    public void setOriginURL(URL originURL) {
        this.originURL = originURL;
    }

    @Override
    public WorkspaceNodeStatus getStatus() {
        return this.status;
    }

    @Override
    public boolean isExternal() {
        return WorkspaceNodeStatus.NODE_EXTERNAL.equals(this.status);
    }

    @Override
    public void setStatus(WorkspaceNodeStatus status) {
        this.status = status;
    }

    @Override
    public String getFormat() {
        return this.format;
    }
    
    @Override
    public void setFormat(String format) {
        this.format = format;
    }

    @Override
    public Collection<WorkspaceParentNodeReference> getParentNodesReferences() {
        return Collections.unmodifiableCollection(this.parentNodesReferences);
    }

    @Override
    public void setParentNodesReferences(Collection<WorkspaceParentNodeReference> parentNodesReferences) {
        this.parentNodesReferences = parentNodesReferences;
    }
    
    @Override
    public void addParentNodeReference(WorkspaceParentNodeReference parentNodeReference) {
        this.parentNodesReferences.add(parentNodeReference);
    }
    
}