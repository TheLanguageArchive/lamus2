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
package nl.mpi.lamus.workspace.exporting.implementation;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.util.DateTimeHelper;
import nl.mpi.lamus.workspace.exporting.CorpusStructureBridge;
import nl.mpi.lamus.workspace.management.WorkspaceAccessChecker;
import nl.mpi.util.Checksum;
import org.apache.commons.io.FileUtils;
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
import org.junit.runner.RunWith;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.stub;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({FileUtils.class, Checksum.class})
public class LamusCorpusStructureBridgeTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Mock CorpusStructureProvider mockCorpusStructureProvider;
    @Mock DateTimeHelper mockDateTimeHelper;
    @Mock ArchiveFileHelper mockArchiveFileHelper;
    @Mock WorkspaceAccessChecker mockWorkspaceAccessChecker;
    
    @Mock File mockFile;
    
    private CorpusStructureBridge corpusStructureBridge;
    
    public LamusCorpusStructureBridgeTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        corpusStructureBridge = new LamusCorpusStructureBridge(mockCorpusStructureProvider, mockDateTimeHelper, mockArchiveFileHelper, mockWorkspaceAccessChecker);
    }
    
    @After
    public void tearDown() {
    }


    @Test
    public void getChecksum() throws MalformedURLException {
        
        final URL nodeArchiveURL = new URL("file:/archive/some/url/file.cmdi");
        
        final String fakeChecksum = "thisisafakechecksum";
        
        context.checking(new Expectations() {{
            
            oneOf(mockFile).exists(); will(returnValue(Boolean.TRUE));
            oneOf(mockFile).canRead(); will(returnValue(Boolean.TRUE));
            oneOf(mockFile).isFile(); will(returnValue(Boolean.TRUE));
            
            oneOf(mockFile).getPath(); will(returnValue(nodeArchiveURL.getPath()));
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockFile);
        stub(method(Checksum.class, "create", String.class)).toReturn(fakeChecksum);
        
        String result = corpusStructureBridge.getChecksum(nodeArchiveURL);
        
        assertEquals("Returned checksum different from expected", fakeChecksum, result);
    }

}