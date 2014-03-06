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
import nl.mpi.archiving.corpusstructure.tools.crawler.ResolverImpl;
import nl.mpi.archiving.corpusstructure.tools.crawler.cmdi.CmdiCrawler;
import nl.mpi.archiving.corpusstructure.tools.crawler.dao.ApplicationManagedDaoContainer;
import nl.mpi.archiving.corpusstructure.tools.crawler.dao.DaoContainer;
import nl.mpi.archiving.corpusstructure.tools.crawler.handler.utils.HandlerUtilities;
import nl.mpi.archiving.corpusstructure.tools.crawler.handler.utils.LocalFsUtilities;
import nl.mpi.lamus.archive.CrawlerBridge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @see CrawlerBridge
 * @author guisil
 */
@Component
public class LamusCrawlerBridge implements CrawlerBridge {
    
    @Autowired
    @Qualifier("crawlerHostName")
    private String crawlerHostName;
    
    @Autowired
    @Qualifier("crawlerDomainName")
    private String crawlerDomainName;
    
    @Autowired
    @Qualifier("handlePrefix")
    private String handlePrefix;
    
    @Autowired
    @Qualifier("crawlerAmsUrl")
    private String crawlerAmsUrl;
    
    @Autowired
    @Qualifier("crawlerMdsUrl")
    private String crawlerMdsUrl;
    
    @Autowired
    @Qualifier("dbHttpRoot")
    private String dbHttpRoot;
    
    @Autowired
    @Qualifier("dbLocalRoot")
    private String dbLocalRoot;
    
    @Autowired
    @Qualifier("crawlerDbDriverClassName")
    private String crawlerDbDriverClassName;
    
    @Autowired
    @Qualifier("crawlerDbUrl")
    private String crawlerDbUrl;
    
    @Autowired
    @Qualifier("crawlerDbMaxActive")
    private String crawlerDbMaxActive;
    
    @Autowired
    @Qualifier("crawlerDbMaxWait")
    private String crawlerDbMaxWait;
    
    @Autowired
    @Qualifier("crawlerDbTestOnBorrow")
    private String crawlerDbTestOnBorrow;
    
    @Autowired
    @Qualifier("crawlerDbUsername")
    private String crawlerDbUsername;
    
    @Autowired
    @Qualifier("crawlerDbPassword")
    private String crawlerDbPassword;
    
    @Autowired
    @Qualifier("crawlerConnectionDriverName")
    private String crawlerConnectionDriverName;
    
    // specify the handle proxy to use for resolving of handles
    @Autowired
    @Qualifier("crawlerHdlProxyDomain")
    private String crawlerHdlProxyDomain;
    
    
    @Override
    public Crawler setUpCrawler() {
        
        /* crawler options */
        Properties archiveProperties = new Properties();
        archiveProperties.put("hostname", crawlerHostName);
        archiveProperties.put("domainname", crawlerDomainName);
        archiveProperties.put("prefixes", handlePrefix);
        archiveProperties.put("amsurl", crawlerAmsUrl);
        archiveProperties.put("mdsurl", crawlerMdsUrl);

        /* crawler jpa/database options */
        Map jpaProperties = new HashMap();

        String driverClassNameString = "DriverClassName=" + crawlerDbDriverClassName;
        String urlString = "Url=" + crawlerDbUrl;
        String maxActiveString = "MaxActive=" + crawlerDbMaxActive;
        String maxWaitString = "MaxWait=" + crawlerDbMaxWait;
        String testOnBorrowString = "TestOnBorrow=" + crawlerDbTestOnBorrow;
        String userNameString = "Username=" + crawlerDbUsername;
        String passwordString = "Password=" + crawlerDbPassword;

        jpaProperties.put(
                "openjpa.ConnectionProperties",
                driverClassNameString + "," + urlString + "," + maxActiveString + "," + maxWaitString + "," + testOnBorrowString + "," + userNameString + "," + passwordString);
        jpaProperties.put(
                    "openjpa.ConnectionDriverName",
                    crawlerConnectionDriverName);

        // create the crawler

        DaoContainer daoContainer = ApplicationManagedDaoContainer.getInstanceWithProperties(jpaProperties);
        
        //TODO load properties from somewhere else?
        //TODO Provide this as a bean instead, using the context to fill in the properties?

        Crawler crawler = new CmdiCrawler(daoContainer);
        crawler.configure(archiveProperties);
        
        return crawler;
    }

    @Override
    public HandlerUtilities setUpHandlerUtilities() {
        
        //TODO Keep this implementation of the resolver, or use something else?
        return new LocalFsUtilities(new ResolverImpl(), dbHttpRoot, dbLocalRoot);
    }
    
}
