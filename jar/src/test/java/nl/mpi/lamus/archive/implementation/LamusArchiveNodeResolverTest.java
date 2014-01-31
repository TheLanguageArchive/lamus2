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
package nl.mpi.lamus.archive.implementation;

import java.net.MalformedURLException;
import java.net.URL;
import nl.mpi.archiving.corpusstructure.core.database.pojo.Archiveobject;
import nl.mpi.archiving.corpusstructure.provider.db.model.CorpusNodeImpl;
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
import org.springframework.test.util.ReflectionTestUtils;

/**
 *
 * @author guisil
 */
public class LamusArchiveNodeResolverTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private LamusArchiveNodeResolver nodeResolver;
    
    private final String httpRoot = "http://some/archive/folder/cmdi_test/";
    private final String localRoot = "file:/some/local/folder/cmdi_test/";
    
    @Mock CorpusNodeImpl mockCorpusNode;
    @Mock Archiveobject mockArchiveObject;
    
    public LamusArchiveNodeResolverTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        nodeResolver = new LamusArchiveNodeResolver();
        ReflectionTestUtils.setField(nodeResolver, "httpRoot", httpRoot);
        ReflectionTestUtils.setField(nodeResolver, "localRoot", localRoot);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void getUrlHttpRoot() throws MalformedURLException {

        final String filePath = "subfolder/file.cmdi";
        final String dbUriStr = httpRoot + filePath;
        final URL dbUrl = new URL(dbUriStr);
        final URL expectedUrl = new URL(localRoot + filePath);
        
        context.checking(new Expectations() {{
            
            oneOf(mockCorpusNode).getArchiveObject(); will(returnValue(mockArchiveObject));
            oneOf(mockArchiveObject).getUri(); will(returnValue(dbUriStr));
        }});
        
        URL retrievedUrl = nodeResolver.getUrl(mockCorpusNode);
        
        assertEquals("Retrieved URL different from expected", expectedUrl, retrievedUrl);
    }
    
    @Test
    public void getUrlLocalRoot() throws MalformedURLException {

        final String filePath = "subfolder/file.cmdi";
        final String dbUriStr = localRoot + filePath;
        final URL dbUrl = new URL(dbUriStr);
        final URL expectedUrl = new URL(localRoot + filePath);
        
        context.checking(new Expectations() {{
            
            oneOf(mockCorpusNode).getArchiveObject(); will(returnValue(mockArchiveObject));
            oneOf(mockArchiveObject).getUri(); will(returnValue(dbUriStr));
        }});
        
        URL retrievedUrl = nodeResolver.getUrl(mockCorpusNode);
        
        assertEquals("Retrieved URL different from expected", expectedUrl, retrievedUrl);
    }
    
    @Test
    public void getUrlExternal() throws MalformedURLException {

        final String filePath = "subfolder/file.cmdi";
        final String dbUriStr = "http://some/external/folder/" + filePath;
        final URL dbUrl = new URL(dbUriStr);
        final URL expectedUrl = dbUrl;
        
        context.checking(new Expectations() {{
            
            oneOf(mockCorpusNode).getArchiveObject(); will(returnValue(mockArchiveObject));
            oneOf(mockArchiveObject).getUri(); will(returnValue(dbUriStr));
        }});
        
        URL retrievedUrl = nodeResolver.getUrl(mockCorpusNode);
        
        assertEquals("Retrieved URL different from expected", expectedUrl, retrievedUrl);
    }
}