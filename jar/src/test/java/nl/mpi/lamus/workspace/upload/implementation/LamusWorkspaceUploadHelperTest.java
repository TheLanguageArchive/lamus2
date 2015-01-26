/*
 * Copyright (C) 2014 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.workspace.upload.implementation;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.TransformerException;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.metadata.MetadataApiBridge;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.upload.WorkspaceUploadHelper;
import nl.mpi.lamus.workspace.upload.WorkspaceUploadReferenceHandler;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
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
import org.junit.Rule;
import static org.junit.Assert.*;

/**
 *
 * @author guisil
 */
public class LamusWorkspaceUploadHelperTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private WorkspaceUploadHelper workspaceUploadHelper;
    
    @Mock MetadataAPI mockMetadataAPI;
    @Mock MetadataApiBridge mockMetadataApiBridge;
    @Mock WorkspaceUploadReferenceHandler mockWorkspaceUploadReferenceHandler;
    
    @Mock WorkspaceNode mockParentNode;
    @Mock WorkspaceNode mockChildNode;
    @Mock WorkspaceNode mockArchiveExternalNode;
    @Mock WorkspaceNode mockExternalNode;
    @Mock Reference mockChildReference;

    @Mock ReferencingMetadataDocument mockParentDocument;
    @Mock MetadataDocument mockChildDocument;
    @Mock File mockParentFile;
    @Mock File mockChildFile;
    
    @Mock CorpusNode mockCorpusNode;
    
    @Mock UploadProblem mockUploadProblem;
    
    public LamusWorkspaceUploadHelperTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        workspaceUploadHelper = new LamusWorkspaceUploadHelper(mockMetadataAPI, mockMetadataApiBridge, mockWorkspaceUploadReferenceHandler);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void assureLinksRelativePathReference() throws URISyntaxException, MalformedURLException, IOException, MetadataException, WorkspaceException {
        
        final int workspaceID = 1;
        
        final String parentFilename = "parent.cmdi";
        final URI parentFileURI = new URI("file:/workspaces/" + workspaceID + "/upload/" + parentFilename);
        final URL parentFileURL = parentFileURI.toURL();
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<>();
        nodesToCheck.add(mockChildNode);
        nodesToCheck.add(mockParentNode);
        
        final Collection<UploadProblem> failedLinks = new ArrayList<>();
        final Map<MetadataDocument, WorkspaceNode> documentsWithExternalSelfHandles = new HashMap<>();
        
        context.checking(new Expectations() {{
            
            // loop
            
            // first iteration - not metadata, so jumps to next iteration
            oneOf(mockChildNode).isMetadata(); will(returnValue(Boolean.FALSE));
            
            // second iteration - metadata, so continues in this iteration
            oneOf(mockParentNode).isMetadata(); will(returnValue(Boolean.TRUE));
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentFileURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentFileURL); will(returnValue(mockParentDocument));
            
            oneOf(mockWorkspaceUploadReferenceHandler).matchReferencesWithNodes(workspaceID, nodesToCheck, mockParentNode, mockParentDocument, documentsWithExternalSelfHandles);
                will(returnValue(failedLinks));
        }});
        
        Collection<UploadProblem> result = workspaceUploadHelper.assureLinksInWorkspace(workspaceID, nodesToCheck);
        
        assertTrue("Result different from expected", result.isEmpty());
    }
    
    @Test
    public void assureLinksPidMetadataReference() throws URISyntaxException, MalformedURLException, IOException, MetadataException, WorkspaceException {
        
        final int workspaceID = 1;
        
        final String parentFilename = "parent.cmdi";
        final URI parentFileURI = new URI("file:/workspaces/" + workspaceID + "/upload/" + parentFilename);
        final URL parentFileURL = parentFileURI.toURL();
        
        final String childFilename = "child.cmdi";
        final File childFile = new File("/workspaces/" + workspaceID + "/upload/" + FilenameUtils.getBaseName(parentFilename) + File.separator + childFilename);
        final URI childFileURI = childFile.toURI();
        final URL childFileURL = childFileURI.toURL();

        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<>();
        nodesToCheck.add(mockChildNode);
        nodesToCheck.add(mockParentNode);
        
        final Collection<UploadProblem> failedLinks = new ArrayList<>();
        final Map<MetadataDocument, WorkspaceNode> documentsWithExternalSelfHandles = new HashMap<>();
        
        context.checking(new Expectations() {{
            
            // loop
            
            // first iteration - metadata, so continues in this iteration
            oneOf(mockChildNode).isMetadata(); will(returnValue(Boolean.TRUE));
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(childFileURL));
            oneOf(mockMetadataAPI).getMetadataDocument(childFileURL); will(returnValue(mockChildDocument));
            
            // second iteration - metadata, so continues in this iteration
            oneOf(mockParentNode).isMetadata(); will(returnValue(Boolean.TRUE));
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentFileURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentFileURL); will(returnValue(mockParentDocument));
            
            oneOf(mockWorkspaceUploadReferenceHandler).matchReferencesWithNodes(workspaceID, nodesToCheck, mockParentNode, mockParentDocument, documentsWithExternalSelfHandles);
                will(returnValue(failedLinks));
        }});
        
        Collection<UploadProblem> result = workspaceUploadHelper.assureLinksInWorkspace(workspaceID, nodesToCheck);
        
        assertTrue("Result different from expected", result.isEmpty());
    }
    
    @Test
    public void assureLinksPidMetadataReference_FailedLink() throws URISyntaxException, MalformedURLException, IOException, MetadataException, WorkspaceException {
        
        final int workspaceID = 1;
        
        final String parentFilename = "parent.cmdi";
        final URI parentFileURI = new URI("file:/workspaces/" + workspaceID + "/upload/" + parentFilename);
        final URL parentFileURL = parentFileURI.toURL();
        
        final String childFilename = "child.cmdi";
        final File childFile = new File("/workspaces/" + workspaceID + "/upload/" + FilenameUtils.getBaseName(parentFilename) + File.separator + childFilename);
        final URI childFileURI = childFile.toURI();
        final URL childFileURL = childFileURI.toURL();

        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<>();
        nodesToCheck.add(mockChildNode);
        nodesToCheck.add(mockParentNode);
        
        final Collection<UploadProblem> failedLinks = new ArrayList<>();
        failedLinks.add(mockUploadProblem);
        final Map<MetadataDocument, WorkspaceNode> documentsWithExternalSelfHandles = new HashMap<>();
        
        context.checking(new Expectations() {{
            
            // loop
            
            // first iteration - metadata, so continues in this iteration
            oneOf(mockChildNode).isMetadata(); will(returnValue(Boolean.TRUE));
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(childFileURL));
            oneOf(mockMetadataAPI).getMetadataDocument(childFileURL); will(returnValue(mockChildDocument));
            
            // second iteration - metadata, so continues in this iteration
            oneOf(mockParentNode).isMetadata(); will(returnValue(Boolean.TRUE));
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentFileURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentFileURL); will(returnValue(mockParentDocument));
            
            oneOf(mockWorkspaceUploadReferenceHandler).matchReferencesWithNodes(workspaceID, nodesToCheck, mockParentNode, mockParentDocument, documentsWithExternalSelfHandles);
                will(returnValue(failedLinks));
        }});
        
        Collection<UploadProblem> result = workspaceUploadHelper.assureLinksInWorkspace(workspaceID, nodesToCheck);
        
        assertTrue("Result different from expected", result.containsAll(failedLinks));
    }
    
    @Test
    public void assureLinksPidMetadataReference_ExternalSelfHandle() throws URISyntaxException, MalformedURLException, IOException, MetadataException, WorkspaceException, TransformerException {
        
        final int workspaceID = 1;
        
        final String parentFilename = "parent.cmdi";
        final URI parentFileURI = new URI("file:/workspaces/" + workspaceID + "/upload/" + parentFilename);
        final URL parentFileURL = parentFileURI.toURL();
        
        final String childFilename = "child.cmdi";
        final File childFile = new File("/workspaces/" + workspaceID + "/upload/" + FilenameUtils.getBaseName(parentFilename) + File.separator + childFilename);
        final URI childFileURI = childFile.toURI();
        final URL childFileURL = childFileURI.toURL();

        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<>();
        nodesToCheck.add(mockChildNode);
        nodesToCheck.add(mockParentNode);
        
        final Collection<UploadProblem> failedLinks = new ArrayList<>();
        failedLinks.add(mockUploadProblem);
        final Map<MetadataDocument, WorkspaceNode> documentsWithExternalSelfHandles = new HashMap<>();
        
        context.checking(new Expectations() {{
            
            // loop
            
            // first iteration - metadata, so continues in this iteration
            oneOf(mockChildNode).isMetadata(); will(returnValue(Boolean.TRUE));
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(childFileURL));
            oneOf(mockMetadataAPI).getMetadataDocument(childFileURL); will(returnValue(mockChildDocument));
            
            // second iteration - metadata, so continues in this iteration
            oneOf(mockParentNode).isMetadata(); will(returnValue(Boolean.TRUE));
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentFileURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentFileURL); will(returnValue(mockParentDocument));
            
            oneOf(mockWorkspaceUploadReferenceHandler).matchReferencesWithNodes(workspaceID, nodesToCheck, mockParentNode, mockParentDocument, documentsWithExternalSelfHandles);
                will(doAll(AddEntryToMap.putElements(mockParentDocument, mockParentNode), returnValue(failedLinks)));
        }});
        
            
        context.checking(new Expectations() {{
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentFileURL));
            oneOf(mockMetadataApiBridge).removeSelfHandleAndSaveDocument(mockParentDocument, parentFileURL);
        }});
        
        Collection<UploadProblem> result = workspaceUploadHelper.assureLinksInWorkspace(workspaceID, nodesToCheck);
        
        assertTrue("Result different from expected", result.containsAll(failedLinks));
    }
    
    @Test
    public void assureLinksPidResourceReference() throws URISyntaxException, MalformedURLException, IOException, MetadataException, WorkspaceException {
        
        final int workspaceID = 1;
        
        final File uploadDirectory = new File("file:/workspaces/" + workspaceID + "/upload");
        
        final String parentFilename = "parent.cmdi";
        final URI parentFileURI = new URI(uploadDirectory.getPath() + File.pathSeparator + parentFilename);
        final URL parentFileURL = parentFileURI.toURL();

        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<>();
        nodesToCheck.add(mockChildNode);
        nodesToCheck.add(mockParentNode);
        
        final Collection<UploadProblem> failedLinks = new ArrayList<>();
        final Map<MetadataDocument, WorkspaceNode> documentsWithExternalSelfHandles = new HashMap<>();
        
        context.checking(new Expectations() {{
            
            // loop
            
            // first iteration - not metadata, so jumps to next iteration
            oneOf(mockChildNode).isMetadata(); will(returnValue(Boolean.FALSE));
            
            // second iteration - metadata, so continues in this iteration
            oneOf(mockParentNode).isMetadata(); will(returnValue(Boolean.TRUE));
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentFileURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentFileURL); will(returnValue(mockParentDocument));
            
            oneOf(mockWorkspaceUploadReferenceHandler).matchReferencesWithNodes(workspaceID, nodesToCheck, mockParentNode, mockParentDocument, documentsWithExternalSelfHandles);
                will(returnValue(failedLinks));
        }});
        
        Collection<UploadProblem> result = workspaceUploadHelper.assureLinksInWorkspace(workspaceID, nodesToCheck);
        
        assertTrue("Result different from expected", result.isEmpty());
    }
    
    @Test
    public void assureLinksArchiveExternalPidResourceReference() throws URISyntaxException, MalformedURLException, IOException, MetadataException, WorkspaceException {
        
        final int workspaceID = 1;
        
        final File uploadDirectory = new File("file:/workspaces/" + workspaceID + "/upload");
        
        final String parentFilename = "parent.cmdi";
        final URL parentFileURL = new URL(uploadDirectory.getPath() + File.pathSeparator + parentFilename);

        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<>();
        nodesToCheck.add(mockChildNode);
        nodesToCheck.add(mockParentNode);
        
        final Collection<UploadProblem> failedLinks = new ArrayList<>();
        final Map<MetadataDocument, WorkspaceNode> documentsWithExternalSelfHandles = new HashMap<>();
        
        context.checking(new Expectations() {{
            
            // loop
            
            // first iteration - not metadata, so jumps to next iteration
            oneOf(mockChildNode).isMetadata(); will(returnValue(Boolean.FALSE));
            
            // second iteration - metadata, so continues in this iteration
            oneOf(mockParentNode).isMetadata(); will(returnValue(Boolean.TRUE));
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentFileURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentFileURL); will(returnValue(mockParentDocument));
            
            oneOf(mockWorkspaceUploadReferenceHandler).matchReferencesWithNodes(workspaceID, nodesToCheck, mockParentNode, mockParentDocument, documentsWithExternalSelfHandles);
                will(returnValue(failedLinks));
        }});
        
        Collection<UploadProblem> result = workspaceUploadHelper.assureLinksInWorkspace(workspaceID, nodesToCheck);
        
        assertTrue("Result different from expected", result.isEmpty());
    }
    
    @Test
    public void assureLinksExternalReference() throws URISyntaxException, MalformedURLException, IOException, MetadataException, WorkspaceException {
        
        final int workspaceID = 1;
        
        final String parentFilename = "parent.txt";
        final URI parentFileURI = new URI("file:/workspaces/" + workspaceID + "/upload/" + parentFilename);
        final URL parentFileURL = parentFileURI.toURL();
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<>();
        nodesToCheck.add(mockChildNode);
        nodesToCheck.add(mockParentNode);
        
        final Collection<UploadProblem> failedLinks = new ArrayList<>();
        final Map<MetadataDocument, WorkspaceNode> documentsWithExternalSelfHandles = new HashMap<>();
        
        context.checking(new Expectations() {{
            
            // loop
            
            // first iteration - not metadata, so jumps to next iteration
            oneOf(mockChildNode).isMetadata(); will(returnValue(Boolean.FALSE));
            
            // second iteration - metadata, so continues in this iteration
            oneOf(mockParentNode).isMetadata(); will(returnValue(Boolean.TRUE));
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentFileURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentFileURL); will(returnValue(mockParentDocument));            
            
            oneOf(mockWorkspaceUploadReferenceHandler).matchReferencesWithNodes(workspaceID, nodesToCheck, mockParentNode, mockParentDocument, documentsWithExternalSelfHandles);
                will(returnValue(failedLinks));
        }});
        
        Collection<UploadProblem> result = workspaceUploadHelper.assureLinksInWorkspace(workspaceID, nodesToCheck);
        
        assertTrue("Result different from expected", result.isEmpty());
    }
    
    
    //TODO Exception situations
    //TODO Exception situations
}