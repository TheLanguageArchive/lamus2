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
package nl.mpi.lamus.spring;

import nl.mpi.annot.search.lib.SearchClient;
import nl.mpi.corpusstructure.ArchiveObjectsDBWrite;
import nl.mpi.corpusstructure.CorpusStructureDB;
import nl.mpi.versioning.manager.VersioningAPI;
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
@ContextConfiguration(classes = {EmbeddedDatabaseBeans.class},
    loader = AnnotationConfigContextLoader.class)
@ActiveProfiles("testing")
public class EmbeddedDatabaseBeansTest {
    
    @Autowired
    @Qualifier("ArchiveObjectsDB")
    private ArchiveObjectsDBWrite archiveObjectsDBBean;
    
    @Autowired
    @Qualifier("CorpusStructureDB")
    private CorpusStructureDB corpusStructureDBBean;
    
    @Autowired
    private VersioningAPI versioningAPI;
    
    @Autowired
    private SearchClient searchClient;
    
    public EmbeddedDatabaseBeansTest() {
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
        assertNotNull(versioningAPI);
        assertNotNull(searchClient);
    }
}
