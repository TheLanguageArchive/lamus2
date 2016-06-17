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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collection;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.corpusstructure.NodeIdUtils;
import nl.mpi.lamus.util.implementation.MockableURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Helper class for the AMS service implementation.
 * @author guisil
 */
@Component
public class AmsFakeRemoteServiceHelper {
    
    private static final Logger logger = LoggerFactory.getLogger(AmsFakeRemoteServiceHelper.class);

    @Autowired
    private CorpusStructureProvider corpusStructureProvider;
    @Autowired
    private NodeResolver nodeResolver;
    
    @Autowired
    @Qualifier("authBaseUrl")
    private String authBaseUrl;
    @Autowired
    @Qualifier("authRecalcUrl")
    private String authRecalcUrl;
    @Autowired
    @Qualifier("authRecalcCsdbUrl")
    private String authRecalcCsdbUrl;
    @Autowired
    @Qualifier("authRecalcWebserverUrl")
    private String authRecalcWebserverUrl;
    @Autowired
    @Qualifier("authRecalcParam")
    private String authRecalcParam;
    

    /**
     * Given a node URI, retrieves the corresponding node ID.
     * @param nodeURIs node URI
     * @return node ID, as a string
     */
    public String getTargetNodeIDsAsString(Collection<URI> nodeURIs) {
        
        StringBuilder targetNodeIDs = new StringBuilder();
        for(URI nodeURI : nodeURIs) {
            CorpusNode node = corpusStructureProvider.getNode(nodeURI);
            String nodeID = NodeIdUtils.TONODEID(Integer.parseInt(nodeResolver.getId(node)));

            if(targetNodeIDs.length() != 0) {
                targetNodeIDs.append(",");
            }
            targetNodeIDs.append(nodeID);
        }
        return targetNodeIDs.toString();
    }
    
    /**
     * Retrieves the appropriate URL to trigger the AMS recalculation.
     * @param triggerCorpusStructureTranscription true if transcription of the
     *  access rights to the corpusstructure database should take place
     * @param triggerWebServerTranscription true if the transcription of the
     *  access rights to the apache htaccess should take place
     * @param targetNodeIDs string containing the node IDs for which the
     *  recalculation should be triggered
     * @return URL to be used to trigger the recalculation
     */
    public MockableURL getRecalcUrl(boolean triggerCorpusStructureTranscription, boolean triggerWebServerTranscription, String targetNodeIDs)
            throws UnsupportedEncodingException, MalformedURLException {
        
        StringBuilder urlToReturn = new StringBuilder();
        urlToReturn.append(authBaseUrl).append("/");
        
        if(triggerCorpusStructureTranscription && triggerWebServerTranscription) {
            urlToReturn.append(authRecalcUrl);
        } else if(triggerCorpusStructureTranscription) {
            urlToReturn.append(authRecalcCsdbUrl);
        } else if(triggerWebServerTranscription) {
            urlToReturn.append(authRecalcWebserverUrl);
        } else {
            throw new IllegalArgumentException("Both 'triggerCorpusStructureTranscription' and 'triggerWebServerTranscription' are false. At least one should be true.");
        }
        
        urlToReturn.append("?").append(authRecalcParam).append("=");
        urlToReturn.append(URLEncoder.encode(targetNodeIDs, "UTF-8"));
        return new MockableURL(new URL(urlToReturn.toString()));
    }
    
    /**
     * Triggers the AMS recalculation with the given URL.
     * @param amsUrl URL to use to trigger the recalculation
     */
    public void sendCallToAccessRightsManagementSystem(MockableURL amsUrl)
            throws IOException {
        
        logger.info("ams2 recalculation called by " + amsUrl.getURL().toString());
        
        URLConnection servletConnection = amsUrl.openConnection();
        servletConnection.setDoInput(true);
        servletConnection.setDoOutput(false);
        servletConnection.setUseCaches(false); // for the connection to the CGI / servlet, that is
        servletConnection.setRequestProperty("Content-Type", "text");
        InputStream instr = servletConnection.getInputStream();
        StringBuilder reply;
        BufferedReader reader = new BufferedReader(new InputStreamReader(instr));
        try {
            reply = new StringBuilder("ams2 recalculation call replied:\n");
            String line;
            while((line = reader.readLine()) != null) {
                reply.append(line);
            }
        } finally {
            reader.close();
        }
        if (servletConnection instanceof HttpURLConnection) {
            ((HttpURLConnection)servletConnection).disconnect();
        }
        logger.info(reply.toString());
    }
}
