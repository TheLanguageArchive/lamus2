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
package nl.mpi.lamus.workspace.importing.implementation;

import java.net.URI;
import java.net.URL;
import java.util.Calendar;
import nl.mpi.corpusstructure.ArchiveAccessContext;
import nl.mpi.corpusstructure.ArchiveObjectsDB;
import nl.mpi.corpusstructure.NodeIdUtils;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.configuration.Configuration;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.typechecking.FileTypeHandler;
import nl.mpi.lamus.typechecking.FileTypeHandlerFactory;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeLinkFactory;
import nl.mpi.lamus.workspace.factory.WorkspaceParentNodeReferenceFactory;
import nl.mpi.lamus.workspace.importing.FileImporter;
import nl.mpi.lamus.workspace.model.*;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspace;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNodeLink;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceParentNodeReference;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import nl.mpi.util.OurURL;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.*;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class ResourceFileImporterTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private FileImporter fileImporter;
    @Mock ArchiveObjectsDB mockArchiveObjectsDB;
    @Mock WorkspaceDao mockWorkspaceDao;
    @Mock Configuration mockConfiguration;
    @Mock ArchiveFileHelper mockArchiveFileHelper;
    @Mock FileTypeHandlerFactory mockFileTypeHandlerFactory;
    @Mock WorkspaceNodeFactory mockWorkspaceNodeFactory;
    @Mock WorkspaceParentNodeReferenceFactory mockWorkspaceParentNodeReferenceFactory;
    @Mock WorkspaceNodeLinkFactory mockWorkspaceNodeLinkFactory;
    private Workspace testWorkspace;
    
    @Mock ReferencingMetadataDocument mockReferencingMetadataDocument;
    @Mock Reference mockChildLink;
//    @Mock URI mockChildURI;
//    @Mock URL mockChildURL;
//    @Mock URL mockParentURL;
    @Mock FileTypeHandler mockFileTypeHandler;
    
    public ResourceFileImporterTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        testWorkspace = new LamusWorkspace(1, "someUser", -1,
                Calendar.getInstance().getTime(), null, Calendar.getInstance().getTime(), null,
                0L, 10000L, WorkspaceStatus.INITIALISING, "Workspace initialising", "archiveInfo/something");
        fileImporter = new ResourceFileImporter(mockArchiveObjectsDB, mockWorkspaceDao, mockConfiguration,
                mockArchiveFileHelper, mockFileTypeHandlerFactory, mockWorkspaceNodeFactory,
                mockWorkspaceParentNodeReferenceFactory, mockWorkspaceNodeLinkFactory, testWorkspace);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of importFile method, of class ResourceFileImporter.
     */
    @Test
    public void importResourceFileSuccessfully() throws Exception {

        final int parentWorkspaceNodeID = 1;
        final int childWorkspaceNodeID = 10;
        final int childNodeArchiveID = 100;
        final String childNodeName = "filename.txt";
        final String childNodeLabel = "file name label";
        final String childNodeTitle = "NO TITLE YET"; //TODO How should this look like?
        final WorkspaceNodeType childNodeType = WorkspaceNodeType.UNKNOWN; //TODO WHat to use here?
        final String childNodeMimetype = "text/plain";
        final URI childNodeSchemaLocation = new URI("http://some.location");
        final String childNodePid = "somePid";
        final OurURL archiveFileUrlWithContext = new OurURL("http://lux16.mpi.nl/corpora/");
        final String childNodeUrlProtocol = "http";
        final URI childLinkURI = new URI("http://some.uri/filename.txt"); //TODO Where to get this from? What to do with it?
        final URL parentURL = new URL("http://some.uri/filename.cmdi");
        
        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), 1, childNodeSchemaLocation,
                "parent label", "", WorkspaceNodeType.METADATA, parentURL, parentURL, parentURL, WorkspaceNodeStatus.NODE_ISCOPY, "aPid", "cmdi");
        final WorkspaceNode testChildNode = new LamusWorkspaceNode(childWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeArchiveID, childNodeSchemaLocation,
                childNodeLabel, "", childNodeType, childLinkURI.toURL(), childLinkURI.toURL(), childLinkURI.toURL(), WorkspaceNodeStatus.NODE_CREATED, childNodePid, childNodeMimetype);
        final WorkspaceParentNodeReference testParentNodeReference = new LamusWorkspaceParentNodeReference(parentWorkspaceNodeID, mockChildLink);
        final WorkspaceNodeLink testNodeLink = new LamusWorkspaceNodeLink(parentWorkspaceNodeID, childWorkspaceNodeID, childLinkURI);
        
        context.checking(new Expectations() {{
            
            oneOf (mockChildLink).getURI(); will(returnValue(childLinkURI));
            oneOf (mockArchiveFileHelper).getFileBasename(childLinkURI.toString()); will(returnValue(childNodeName));
            oneOf (mockArchiveFileHelper).getFileTitle(childLinkURI.toString()); will(returnValue(childNodeLabel));
            
            //TODO check type
            
            // get mimetype
            oneOf (mockChildLink).getMimetype(); will(returnValue(childNodeMimetype));
            // get FileTypeHandler
            oneOf (mockFileTypeHandlerFactory).getNewFileTypeHandlerForWorkspace(testWorkspace); will(returnValue(mockFileTypeHandler));
            // is onsite ?
            oneOf (mockArchiveObjectsDB).isOnSite(NodeIdUtils.TONODEID(childNodeArchiveID)); will(returnValue(true));
                // if not onsite, set as unspecified or something like that and set URID as NONE
                // otherwise, if url belongs to local server but is outside of the archive, treat as not onsite
            //TODO catch exceptions thrown by getObjectURL?
            oneOf (mockArchiveObjectsDB).getObjectURL(NodeIdUtils.TONODEID(childNodeArchiveID), ArchiveAccessContext.getFileUrlContext()); will(returnValue(archiveFileUrlWithContext));
            // if protocol null, empty or file (file in archive), check if file is bigger than the typecheck limit
//            oneOf (mockChildLink).getURI(); will(returnValue(childLinkURI));
//            oneOf (mockChildURI).toURL(); will(returnValue(mockChildURL));
//            oneOf (mockChildURL).getProtocol(); will(returnValue(childNodeUrlProtocol));
                // if so and not orphan, do not typecheck
                // if so and orphan, do typecheck (warn for large file)
            // calculateCV (change this) if typecheck is to be done
            oneOf (mockFileTypeHandler).calculateCV(childLinkURI.toURL(), childNodeName, childNodeType, null);
            // if type unspecified and typecheck to be done, warn
            oneOf (mockFileTypeHandler).getFormat(); will(returnValue(childNodeMimetype));
            // if type differs from suggested mimetype, use mimetype from typecheck calculation (check also if it is unspecified)
            oneOf (mockWorkspaceNodeFactory).getNewWorkspaceNode(testWorkspace.getWorkspaceID(), childNodeArchiveID, childLinkURI.toURL());
                will(returnValue(testChildNode));
            // needs protection?
            
            //TODO SOMETHING MISSING HERE?
            
            // create node (depending on some values, it can be an external one, a protected one, etc)
            oneOf (mockWorkspaceDao).addWorkspaceNode(testChildNode);
            // create link between parent and child
            oneOf (mockWorkspaceParentNodeReferenceFactory).getNewWorkspaceParentNodeReference(testParentNode, mockChildLink);
                will(returnValue(testParentNodeReference));
            oneOf (mockWorkspaceNodeLinkFactory).getNewWorkspaceNodeLink(parentWorkspaceNodeID, childWorkspaceNodeID, childLinkURI);
                will(returnValue(testNodeLink));
            oneOf (mockWorkspaceDao).addWorkspaceNodeLink(testNodeLink);
            
            //TODO SOMETHING MISSING HERE?
            
        }});
        
        //TODO PID SHOULD BE COMING FROM THE CHILD LINK (HandleCarrier)
        fileImporter.importFile(testParentNode, mockReferencingMetadataDocument, mockChildLink, childNodeArchiveID);
        
    }
}
