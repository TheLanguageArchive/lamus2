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

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import nl.mpi.archiving.corpusstructure.tools.crawler.Crawler;
import nl.mpi.archiving.corpusstructure.tools.crawler.cmdi.CmdiCrawler;
import nl.mpi.archiving.corpusstructure.tools.crawler.handler.utils.HandlerUtilities;
import nl.mpi.archiving.corpusstructure.tools.crawler.handler.utils.LocalFsUtilities;
import nl.mpi.lamus.archive.CrawlerBridge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 *
 * @author guisil
 */
@Component
public class LamusCrawlerBridge implements CrawlerBridge {
    
    @Autowired
    @Qualifier("crawler_hostname")
    private String hostName;
    
    @Autowired
    @Qualifier("crawler_domainname")
    private String domainName;
    
    @Autowired
    @Qualifier("crawler_prefixes")
    private String prefixes;
    
    @Autowired
    @Qualifier("crawler_amsurl")
    private String amsUrl;
    
    @Autowired
    @Qualifier("crawler_mdsurl")
    private String mdsUrl;
    
    @Autowired
    @Qualifier("crawler_httproot")
    private String httpRoot;
    
    @Autowired
    @Qualifier("crawler_localroot")
    private String localRoot;
    
    @Autowired
    @Qualifier("crawler_dbdriverclassname")
    private String dbDriverClassName;
    
    @Autowired
    @Qualifier("crawler_dburl")
    private String dbUrl;
    
    @Autowired
    @Qualifier("crawler_dbmaxactive")
    private String dbMaxActive;
    
    @Autowired
    @Qualifier("crawler_dbmaxwait")
    private String dbMaxWait;
    
    @Autowired
    @Qualifier("crawler_dbtestonborrow")
    private String dbTestOnBorrow;
    
    @Autowired
    @Qualifier("crawler_dbusername")
    private String dbUserName;
    
    @Autowired
    @Qualifier("crawler_dbpassword")
    private String dbPassword;
    
    @Autowired
    @Qualifier("crawler_connectiondrivername")
    private String connectionDriverName;
    
    /* specify the handle proxy to use for resolving of handles */
    @Autowired
    @Qualifier("crawler_hdlproxydomain")
    private String hdlProxyDomain;
    
    
    @Override
    public Crawler setUpCrawler() {
        
        Crawler crawler = null;
        
        
        //TODO CHANGE THIS - MAYBE PROVIDE THIS AS A BEAN, USING THE CONTEXT TO FILL IN THE PROPERTIES
        
        
        
        
        try {    
            /* crawler options */
            Properties archiveProperties = new Properties();
            archiveProperties.put("hostname", hostName);
            archiveProperties.put("domainname", domainName);
            archiveProperties.put("prefixes", prefixes);
            archiveProperties.put("amsurl", amsUrl);
            archiveProperties.put("mdsurl", mdsUrl);
            
            /* crawler jpa/database options */
            Map jpaProperties = new HashMap();
            
            String driverClassNameString = "DriverClassName=" + dbDriverClassName;
            String urlString = "Url=" + dbUrl;
            String maxActiveString = "MaxActive=" + dbMaxActive;
            String maxWaitString = "MaxWait=" + dbMaxWait;
            String testOnBorrowString = "TestOnBorrow=" + dbTestOnBorrow;
            String userNameString = "Username=" + dbUserName;
            String passwordString = "Password=" + dbPassword;
            
            jpaProperties.put(
                    "openjpa.ConnectionProperties",
                    driverClassNameString + "," + urlString + "," + maxActiveString + "," + maxWaitString + "," + testOnBorrowString + "," + userNameString + "," + passwordString);
            jpaProperties.put(
                        "openjpa.ConnectionDriverName",
                        connectionDriverName);
            
            /* create the crawler */
            crawler = new CmdiCrawler(jpaProperties, archiveProperties, hdlProxyDomain);
        } catch (URISyntaxException ex) {
            throw new UnsupportedOperationException("exception not handled yet");
        }
        
        return crawler;
    }

    @Override
    public HandlerUtilities setUpHandlerUtilities() {
        
        return new LocalFsUtilities(httpRoot, localRoot);
    }
    
}
