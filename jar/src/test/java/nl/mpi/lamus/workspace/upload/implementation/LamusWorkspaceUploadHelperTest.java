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
import java.util.List;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.workspace.management.WorkspaceNodeLinkManager;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.upload.WorkspaceUploadHelper;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
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
import static org.junit.Assert.*;
import org.junit.Rule;

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
    @Mock WorkspaceNodeLinkManager mockWorkspaceNodeLinkManager;
    
    @Mock WorkspaceNode mockParentNode;
    @Mock WorkspaceNode mockChildNode;
    @Mock Reference mockChildReference;

    @Mock ReferencingMetadataDocument mockParentDocument;
    @Mock File mockParentFile;
    @Mock File mockChildFile;
    
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
        
        workspaceUploadHelper = new LamusWorkspaceUploadHelper(mockMetadataAPI, mockWorkspaceNodeLinkManager);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of assureLinksInWorkspace method, of class LamusWorkspaceUploadHelper.
     */
    @Test
    public void assureLinksInWorkspace() throws URISyntaxException, MalformedURLException, IOException, MetadataException, WorkspaceException {
        
        final int workspaceID = 1;
        
        final String childFilename = "child.txt";
        final File childFile = new File("/workspaces/" + workspaceID + "/upload/" + childFilename);
        final URI childFileURI = childFile.toURI();
        final URL childFileURL = childFileURI.toURL();

        final String parentFilename = "parent.txt";
        final URI parentFileURI = new URI("file:/workspaces/" + workspaceID + "/upload/" + parentFilename);
        final URL parentFileURL = parentFileURI.toURL();
        
        final URI childReferenceURI = new URI(FilenameUtils.getBaseName(parentFilename) + File.pathSeparator + childFilename);
        
        Collection<WorkspaceNode> nodesToCheck = new ArrayList<WorkspaceNode>();
        nodesToCheck.add(mockChildNode);
        nodesToCheck.add(mockParentNode);
        
        final List<Reference> parentDocumentReferences = new ArrayList<Reference>();
        parentDocumentReferences.add(mockChildReference);
        
        context.checking(new Expectations() {{
            
            // loop
            
            
            // first iteration - not metadata, so jumps to next iteration
            oneOf(mockChildNode).isMetadata(); will(returnValue(Boolean.FALSE));
            
            // second iteration - metadata, so continues in this iteration
            oneOf(mockParentNode).isMetadata(); will(returnValue(Boolean.TRUE));
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentFileURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentFileURL); will(returnValue(mockParentDocument));
            oneOf(mockParentDocument).getDocumentReferences(); will(returnValue(parentDocumentReferences));
            
            // loop through the references trying to find a match
            //TODO what if the reference URI is a handle?
            oneOf(mockChildReference).getURI(); will(returnValue(childReferenceURI));
            
            // match
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(childFileURL));
            //change reference to point to the current location
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(childFileURL));
            oneOf(mockChildReference).setURI(childFileURI);
            //add link in database
            oneOf(mockWorkspaceNodeLinkManager).linkNodes(mockParentNode, mockChildNode);
            
            // out of the loop
        }});
        
        
        //TODO check links in the metadata files
        //TODO if any of those links is part of the collection, add the link in the database
        
        //TODO check links also from the workspace tree to the collection of files? maybe later?
        
        workspaceUploadHelper.assureLinksInWorkspace(workspaceID, nodesToCheck);
    }
}