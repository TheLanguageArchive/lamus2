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
package nl.mpi.lamus.workspace.exporting.implementation;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;
import java.util.UUID;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.workspace.exception.WorkspaceNodeFilesystemException;
import nl.mpi.lamus.workspace.exporting.NodeExporter;
import nl.mpi.lamus.workspace.exporting.WorkspaceTreeExporter;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.WorkspaceStatus;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspace;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.MetadataDocument;
import org.apache.commons.io.FilenameUtils;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;

/**
 *
 * @author guisil
 */
public class GeneralNodeExporterTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Mock MetadataAPI mockMetadataAPI;
    @Mock WorkspaceFileHandler mockWorkspaceFileHandler;
    @Mock WorkspaceTreeExporter mockWorkspaceTreeExporter;
    
    @Mock MetadataDocument mockMetadataDocument;
    @Mock StreamResult mockStreamResult;
    
    private NodeExporter generalNodeExporter;
    private Workspace workspace;
    
    public GeneralNodeExporterTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        workspace = new LamusWorkspace(1, "someUser", -1, -1, null,
                Calendar.getInstance().getTime(), null, Calendar.getInstance().getTime(), null,
                0L, 10000L, WorkspaceStatus.SUBMITTED, "Workspace submitted", "archiveInfo/something");
        
        generalNodeExporter = new GeneralNodeExporter(mockMetadataAPI, mockWorkspaceFileHandler, mockWorkspaceTreeExporter);
        generalNodeExporter.setWorkspace(workspace);
    }
    
    @After
    public void tearDown() {
    }



    @Test
    public void exportTopNode() throws MalformedURLException, URISyntaxException, IOException, MetadataException, WorkspaceNodeFilesystemException {
    
        /*
         * File already exists in the archive.
         * Should it be copied anyway?
         * If not, there should be some indication about it being unchanged.
         * But maybe there is a risk that afterwards the file will be moved (because of something changing in the parent, for instance) - but still the file can be moved as it is...
         * 
         * option 1 - copy always in any case
         * option 2 - find a way of checking efficiently for differences, and copy only if they exist
         */
        
        
//        final int parentNodeWsID = 1;
//        final int parentNodeArchiveID = 50;
//        final URL parentNodeArchiveURL = new URL("file:/archive/root/somenode/parentnode.cmdi");
        
        final int nodeWsID = 10;
        final int nodeArchiveID = 100;
        final String nodeFilename = "someNode.cmdi";
        final URL nodeWsURL = new URL("file:/workspace/" + workspace.getWorkspaceID() + File.separator + nodeFilename);
        final File nodeWsFile = new File(nodeWsURL.getPath());
        final URL nodeArchiveURL = new URL("file:/archive/location/" + nodeFilename);
        final File nodeArchiveFile = new File(nodeArchiveURL.getPath());
        final String nodeName = "someNode";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.METADATA; //TODO change this
        final String nodeFormat = "text/cmdi";
        final URI nodeSchemaLocation = new URI("http://some.location");
        final String nodePid = UUID.randomUUID().toString();
        final WorkspaceNode node = new LamusWorkspaceNode(nodeWsID, workspace.getWorkspaceID(), nodeArchiveID, nodeSchemaLocation,
                nodeName, "", nodeType, nodeWsURL, nodeArchiveURL, nodeArchiveURL, WorkspaceNodeStatus.NODE_ISCOPY, nodePid, nodeFormat);
        workspace.setTopNodeID(nodeWsID);
        workspace.setTopNodeArchiveID(nodeArchiveID);
        workspace.setTopNodeArchiveURL(nodeArchiveURL);
        
        
        //TODO copy file from workspace folder to archive folder
        //TODO check if there are differences in the data present in the database? (or is this supposed to be done in the crawler?)
        //TODO call the tree exporter for this node in case it's metadata (can have children)
        
        context.checking(new Expectations() {{
            
            oneOf(mockMetadataAPI).getMetadataDocument(nodeWsURL); will(returnValue(mockMetadataDocument));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(nodeArchiveFile); will(returnValue(mockStreamResult));
            
            oneOf(mockWorkspaceFileHandler).copyMetadataFile(node, mockMetadataAPI, mockMetadataDocument, nodeWsFile, mockStreamResult);
            
            oneOf(mockWorkspaceTreeExporter).explore(workspace, node);
        }});
        
        generalNodeExporter.exportNode(null, node);
    }
}