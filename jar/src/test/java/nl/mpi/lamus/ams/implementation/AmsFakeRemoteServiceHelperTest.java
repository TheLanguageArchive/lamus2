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
package nl.mpi.lamus.ams.implementation;

import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.util.implementation.MockableURL;
import org.hibernate.engine.jdbc.ReaderInputStream;
import org.jmock.Expectations;
import static org.jmock.Expectations.returnValue;
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
public class AmsFakeRemoteServiceHelperTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Mock CorpusStructureProvider mockCorpusStructureProvider;
    @Mock NodeResolver mockNodeResolver;
    
    @Mock CorpusNode mockCorpusNode_1;
    @Mock CorpusNode mockCorpusNode_2;
    @Mock CorpusNode mockCorpusNode_3;
    
    @Mock MockableURL mockUrl;
    @Mock HttpURLConnection mockUrlConnection;
    
    private final String authBaseUrl = "http://server/ams-cmdi";
    private final String authRecalcUrl = "recalc/page";
    private final String authRecalcCsdbUrl = "recalc_csdb/page";
    private final String authRecalcWebserverUrl = "recalc_webserver/page";
    private final String authRecalcParam = "nodeid";
    
    
    private AmsFakeRemoteServiceHelper amsFakeRemoteServiceHelper;
    
    public AmsFakeRemoteServiceHelperTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        amsFakeRemoteServiceHelper = new AmsFakeRemoteServiceHelper();
        
        ReflectionTestUtils.setField(amsFakeRemoteServiceHelper, "corpusStructureProvider", mockCorpusStructureProvider);
        ReflectionTestUtils.setField(amsFakeRemoteServiceHelper, "nodeResolver", mockNodeResolver);
        
        ReflectionTestUtils.setField(amsFakeRemoteServiceHelper, "authBaseUrl", authBaseUrl);
        ReflectionTestUtils.setField(amsFakeRemoteServiceHelper, "authRecalcUrl", authRecalcUrl);
        ReflectionTestUtils.setField(amsFakeRemoteServiceHelper, "authRecalcCsdbUrl", authRecalcCsdbUrl);
        ReflectionTestUtils.setField(amsFakeRemoteServiceHelper, "authRecalcWebserverUrl", authRecalcWebserverUrl);
        ReflectionTestUtils.setField(amsFakeRemoteServiceHelper, "authRecalcParam", authRecalcParam);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void getTargetNodeIDsAsString_OneNodes() throws URISyntaxException {
        
        final URI nodeURI_1 = new URI("hdl:" + UUID.randomUUID().toString());
        final Collection<URI> nodeURIs = new ArrayList<>();
        nodeURIs.add(nodeURI_1);
        
        final String nodeID_1 = "11";
        final String nodeMpiID_1 = "MPI" + nodeID_1 + "#";
        
        final String expectedString = nodeMpiID_1;
        
        context.checking(new Expectations() {{
            
            //loop - first iteration
            oneOf(mockCorpusStructureProvider).getNode(nodeURI_1); will(returnValue(mockCorpusNode_1));
            oneOf(mockNodeResolver).getId(mockCorpusNode_1); will(returnValue(nodeID_1));
        }});
        
        String result = amsFakeRemoteServiceHelper.getTargetNodeIDsAsString(nodeURIs);
        
        assertEquals("Result different from expected", expectedString, result);
    }
    
    @Test
    public void getTargetNodeIDsAsString_SeveralNodes() throws URISyntaxException {
        
        final URI nodeURI_1 = new URI("hdl:" + UUID.randomUUID().toString());
        final URI nodeURI_2 = new URI("hdl:" + UUID.randomUUID().toString());
        final URI nodeURI_3 = new URI("hdl:" + UUID.randomUUID().toString());
        final Collection<URI> nodeURIs = new ArrayList<>();
        nodeURIs.add(nodeURI_1);
        nodeURIs.add(nodeURI_2);
        nodeURIs.add(nodeURI_3);
        
        final String nodeID_1 = "11";
        final String nodeMpiID_1 = "MPI" + nodeID_1 + "#";
        final String nodeID_2 = "12";
        final String nodeMpiID_2 = "MPI" + nodeID_2 + "#";
        final String nodeID_3 = "13";
        final String nodeMpiID_3 = "MPI" + nodeID_3 + "#";
        
        final String expectedString = nodeMpiID_1 + "," + nodeMpiID_2 + "," + nodeMpiID_3;
        
        context.checking(new Expectations() {{
            
            //loop - first iteration
            oneOf(mockCorpusStructureProvider).getNode(nodeURI_1); will(returnValue(mockCorpusNode_1));
            oneOf(mockNodeResolver).getId(mockCorpusNode_1); will(returnValue(nodeID_1));
            //loop - second iteration
            oneOf(mockCorpusStructureProvider).getNode(nodeURI_2); will(returnValue(mockCorpusNode_2));
            oneOf(mockNodeResolver).getId(mockCorpusNode_2); will(returnValue(nodeID_2));
            //loop - third iteration
            oneOf(mockCorpusStructureProvider).getNode(nodeURI_3); will(returnValue(mockCorpusNode_3));
            oneOf(mockNodeResolver).getId(mockCorpusNode_3); will(returnValue(nodeID_3));
        }});
        
        String result = amsFakeRemoteServiceHelper.getTargetNodeIDsAsString(nodeURIs);
        
        assertEquals("Result different from expected", expectedString, result);
    }

    @Test
    public void getRecalcUrl_onlyCsdb() throws UnsupportedEncodingException, MalformedURLException {
        
        final String targetNodeIDs = "MPI11#,MPI12#";
        final MockableURL expectedUrl = new MockableURL(new URL(authBaseUrl + "/" + authRecalcCsdbUrl + "?" + authRecalcParam + "=" + URLEncoder.encode(targetNodeIDs, "UTF-8")));
        
        MockableURL result = amsFakeRemoteServiceHelper.getRecalcUrl(Boolean.TRUE, Boolean.FALSE, targetNodeIDs);
        
        assertEquals("URL different from expected", expectedUrl, result);
    }
    
    @Test
    public void getRecalcUrl_onlyWebserver() throws UnsupportedEncodingException, MalformedURLException {
        
        final String targetNodeIDs = "MPI11#,MPI12#";
        final MockableURL expectedUrl = new MockableURL(new URL(authBaseUrl + "/" + authRecalcWebserverUrl + "?" + authRecalcParam + "=" + URLEncoder.encode(targetNodeIDs, "UTF-8")));
        
        MockableURL result = amsFakeRemoteServiceHelper.getRecalcUrl(Boolean.FALSE, Boolean.TRUE, targetNodeIDs);
        
        assertEquals("URL different from expected", expectedUrl, result);
    }
    
    @Test
    public void getRecalcUrl_complete() throws UnsupportedEncodingException, MalformedURLException {
        
        final String targetNodeIDs = "MPI11#,MPI12#";
        final MockableURL expectedUrl = new MockableURL(new URL(authBaseUrl + "/" + authRecalcUrl + "?" + authRecalcParam + "=" + URLEncoder.encode(targetNodeIDs, "UTF-8")));
        
        MockableURL result = amsFakeRemoteServiceHelper.getRecalcUrl(Boolean.TRUE, Boolean.TRUE, targetNodeIDs);
        
        assertEquals("URL different from expected", expectedUrl, result);
    }

    @Test
    public void sendCallToAccessRightsManagementSystem() throws Exception {
        
        final String expectedString = "some text to read and test the method and bla bla bla";
        final StringReader reader = new StringReader(expectedString);
        final InputStream fakeInputStream = new ReaderInputStream(reader);
        
        context.checking(new Expectations() {{
            
            oneOf(mockUrl).openConnection(); will(returnValue(mockUrlConnection));
            oneOf(mockUrlConnection).setDoInput(Boolean.TRUE);
            oneOf(mockUrlConnection).setDoOutput(Boolean.FALSE);
            oneOf(mockUrlConnection).setUseCaches(Boolean.FALSE);
            oneOf(mockUrlConnection).setRequestProperty("Content-Type", "text");
            oneOf(mockUrlConnection).getInputStream(); will(returnValue(fakeInputStream));
            
            oneOf(mockUrlConnection).disconnect();
        }});
        
        amsFakeRemoteServiceHelper.sendCallToAccessRightsManagementSystem(mockUrl);
    }
}