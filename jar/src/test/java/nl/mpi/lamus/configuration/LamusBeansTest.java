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
package nl.mpi.lamus.configuration;

import java.util.concurrent.Executor;
import nl.mpi.corpusstructure.ArchiveObjectsDB;
import nl.mpi.corpusstructure.CorpusStructureDB;
import nl.mpi.lamus.ams.AmsBridge;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.dao.implementation.LamusJdbcWorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceDirectoryHandler;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.workspace.factory.WorkspaceFactory;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeLinkFactory;
import nl.mpi.lamus.workspace.factory.WorkspaceParentNodeReferenceFactory;
import nl.mpi.lamus.workspace.importing.WorkspaceFileExplorer;
import nl.mpi.lamus.workspace.importing.WorkspaceImportRunner;
import nl.mpi.lamus.workspace.importing.implementation.FileImporterFactoryBean;
import nl.mpi.lamus.workspace.management.NodeAccessChecker;
import nl.mpi.lamus.workspace.management.WorkspaceManager;
import nl.mpi.metadata.api.MetadataAPI;
import static org.junit.Assert.assertNotNull;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {LamusBeans.class, LamusJdbcWorkspaceDao.class, EmbeddedDatabaseBeans.class, JndiDatabaseBeans.class}, loader = AnnotationConfigContextLoader.class)
@ActiveProfiles("testing")
public class LamusBeansTest {
    
    @Autowired
    @Qualifier("ArchiveObjectsDB")
    private ArchiveObjectsDB archiveObjectsDBBean;
    @Autowired
    @Qualifier("CorpusStructureDB")
    private CorpusStructureDB corpusStructureDBBean;
    
    @Autowired
    private AmsBridge amsBridgeBean;
    @Autowired
    private WorkspaceDao workspaceDaoBean;
    @Autowired
    private NodeAccessChecker nodeAccessCheckerBean;
    
    @Autowired
    private Executor executorBean;
    @Autowired
    private WorkspaceFactory workspaceFactoryBean;
    @Autowired
    private WorkspaceDirectoryHandler workspaceDirectoryHandlerBean;
    
    @Autowired
    private MetadataAPI metadataAPI;
    @Autowired
    private WorkspaceNodeFactory workspaceNodeFactory;
    @Autowired
    private WorkspaceParentNodeReferenceFactory workspaceParentNodeReferenceFactory;
    @Autowired
    private WorkspaceNodeLinkFactory workspaceNodeLinkFactory;
    @Autowired
    private WorkspaceFileHandler workspaceFileHandler;
    
    @Autowired
    private FileImporterFactoryBean fileImporterFactoryBean;

    @Autowired
    private WorkspaceFileExplorer workspaceFileExplorer;

    @Autowired
    private WorkspaceImportRunner workspaceImportRunner;
    
    @Autowired
    private WorkspaceManager workspaceManagerBean;
    
    @Autowired
    private WorkspaceService workspaceServiceBean;
    
    public LamusBeansTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testLoadBeans() {
        
        assertNotNull(archiveObjectsDBBean);
        assertNotNull(corpusStructureDBBean);
        
        assertNotNull(amsBridgeBean);
        assertNotNull(workspaceDaoBean);        
        assertNotNull(nodeAccessCheckerBean);
        
        assertNotNull(executorBean);
        assertNotNull(workspaceFactoryBean);
        assertNotNull(workspaceDirectoryHandlerBean);
        
        assertNotNull(metadataAPI);
        assertNotNull(workspaceNodeFactory);
        assertNotNull(workspaceParentNodeReferenceFactory);
        assertNotNull(workspaceNodeLinkFactory);
        assertNotNull(workspaceFileHandler);
        
        assertNotNull(fileImporterFactoryBean);
        
        assertNotNull(workspaceFileExplorer);
        
        assertNotNull(workspaceImportRunner);
        
        assertNotNull(workspaceManagerBean);
        
        assertNotNull(workspaceServiceBean);
        
        
        
    }
}
