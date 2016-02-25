/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
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
import java.net.URISyntaxException;
import java.util.UUID;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.WorkspaceImportException;
import nl.mpi.lamus.metadata.MetadataApiBridge;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
import nl.mpi.lamus.workspace.importing.NodeDataRetriever;
import nl.mpi.lamus.workspace.importing.WorkspaceFileImporter;
import nl.mpi.lamus.workspace.importing.WorkspaceNodeExplorer;
import nl.mpi.lamus.workspace.management.WorkspaceNodeLinkManager;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.metadata.api.MetadataAPI;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Rule;
import org.springframework.test.util.ReflectionTestUtils;

/**
 *
 * @author guisil
 */
public class TopNodeImporterTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private TopNodeImporter topNodeImporter;
    
    @Mock MetadataNodeImporter mockMetadataNodeImporter;
    
    @Mock CorpusStructureProvider mockCsProvider;
    @Mock NodeResolver mockNodeResolver;
    @Mock WorkspaceDao mockWsDao;
    @Mock MetadataAPI mockMetadataAPI;
    @Mock MetadataApiBridge mockMetadataApiBridge;
    @Mock WorkspaceNodeLinkManager mockNodeLinkManager;
    @Mock WorkspaceFileImporter mockFileImporter;
    @Mock WorkspaceNodeFactory mockNodeFactory;
    @Mock WorkspaceNodeExplorer mockWorkspaceNodeExplorer;
    @Mock NodeDataRetriever mockNodeDataRetriever;
    
    @Mock Workspace mockWorkspace;
    
    
    public TopNodeImporterTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        topNodeImporter = new TopNodeImporter();
        ReflectionTestUtils.setField(topNodeImporter, "nodeDataRetriever", mockNodeDataRetriever);
        ReflectionTestUtils.setField(topNodeImporter, "metadataNodeImporter", mockMetadataNodeImporter);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void importTopNode() throws URISyntaxException, WorkspaceImportException {
        
        final URI nodeArchiveURI = new URI(UUID.randomUUID().toString());
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspace).setTopNodeArchiveURI(nodeArchiveURI);
            oneOf(mockMetadataNodeImporter).importNode(mockWorkspace, null, null, null);
        }});
        
        topNodeImporter.importNode(mockWorkspace, nodeArchiveURI);
    }
}