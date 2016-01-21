/*
 * Copyright (C) 2016 Max Planck Institute for Psycholinguistics
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

import nl.mpi.lamus.archive.CorpusStructureServiceBridge;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.workspace.exporting.NodeExporterFactory;
import nl.mpi.lamus.workspace.exporting.UnlinkedAndDeletedNodesExportHandler;
import nl.mpi.lamus.workspace.exporting.WorkspaceExportRunnerFactory;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.springframework.test.util.ReflectionTestUtils;

/**
 *
 * @author guisil
 */
public class LamusWorkspaceExportRunnerFactoryTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Mock private WorkspaceDao mockWorkspaceDao;
    @Mock private NodeExporterFactory mockNodeExporterFactory;
    @Mock private UnlinkedAndDeletedNodesExportHandler mockUnlinkedAndDeletedNodesExportHandler;
    @Mock private CorpusStructureServiceBridge mockCorpusStructureServiceBridge;
    
    private WorkspaceExportRunnerFactory workspaceExportRunnerFactory;
    
    
    public LamusWorkspaceExportRunnerFactoryTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        workspaceExportRunnerFactory = new LamusWorkspaceExportRunnerFactory(mockWorkspaceDao, mockNodeExporterFactory, mockUnlinkedAndDeletedNodesExportHandler, mockCorpusStructureServiceBridge);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void testGetNewExportRunner() {
        
        WorkspaceExportRunner exportRunner = workspaceExportRunnerFactory.getNewExportRunner();
        
        assertEquals("'workspaceDao' different from expected", mockWorkspaceDao, ReflectionTestUtils.getField(exportRunner, "workspaceDao"));
        assertEquals("'nodeExporterFactory' different from expected", mockNodeExporterFactory, ReflectionTestUtils.getField(exportRunner, "nodeExporterFactory"));
        assertEquals("'unlinkedAndDeletedNodesExportHandler' different from expected", mockUnlinkedAndDeletedNodesExportHandler, ReflectionTestUtils.getField(exportRunner, "unlinkedAndDeletedNodesExportHandler"));
        assertEquals("'corpusStructureServiceBridge' different from expected", mockCorpusStructureServiceBridge, ReflectionTestUtils.getField(exportRunner, "corpusStructureServiceBridge"));
    }
}
