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
package nl.mpi.lamus.workspace.importing.implementation;

import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.workspace.importing.OrphanNodesImportHandler;
import nl.mpi.lamus.workspace.importing.WorkspaceImportRunnerFactory;
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
public class LamusWorkspaceImportRunnerFactoryTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Mock private WorkspaceDao mockWorkspaceDao;
    @Mock private TopNodeImporter mockTopNodeImporter;
    @Mock private OrphanNodesImportHandler mockOrphanNodesImportHandler;
    
    private WorkspaceImportRunnerFactory workspaceImportRunnerFactory;
    
    
    public LamusWorkspaceImportRunnerFactoryTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        workspaceImportRunnerFactory = new LamusWorkspaceImportRunnerFactory(mockWorkspaceDao, mockTopNodeImporter, mockOrphanNodesImportHandler);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void testGetNewImportRunner() {
        
        WorkspaceImportRunner importRunner = workspaceImportRunnerFactory.getNewImportRunner();
        
        assertEquals("'workspaceDao' different from expected", mockWorkspaceDao, ReflectionTestUtils.getField(importRunner, "workspaceDao"));
        assertEquals("'topNodeImporter' different from expected", mockTopNodeImporter, ReflectionTestUtils.getField(importRunner, "topNodeImporter"));
        assertEquals("'orphanNodesImportHandler' different from expected", mockOrphanNodesImportHandler, ReflectionTestUtils.getField(importRunner, "orphanNodesImportHandler"));
    }
}
