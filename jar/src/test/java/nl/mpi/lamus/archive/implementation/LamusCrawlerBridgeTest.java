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

import nl.mpi.archiving.corpusstructure.tools.crawler.Crawler;
import nl.mpi.archiving.corpusstructure.tools.crawler.handler.utils.HandlerUtilities;
import nl.mpi.lamus.archive.CrawlerBridge;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.util.ReflectionTestUtils;

/**
 *
 * @author guisil
 */
public class LamusCrawlerBridgeTest {
    
    private CrawlerBridge crawlerBridge;
    
    private String hostName = "host.mpi.nl";
    private String domainName = "mpi.nl";
    private String prefixes = "01234";
    private String amsUrl = "";
    private String mdsUrl = "";
    private String httpRoot = "http://host.mpi.nl/archive/";
    private String localRoot = "file:/somewhere/archive/";
    private String dbDriverClassName = "org.hsqldb.jdbcDriver";
    private String dbUrl = "jdbc:hsqldb:mem:corpusstructure2";
    private String dbMaxActive = "100";
    private String dbMaxWait = "10000";
    private String dbTestOnBorrow = "true";
    private String dbUserName = "sa";
    private String dbPassword = "";
    private String connectionDriverName = "org.apache.commons.dbcp.BasicDataSource";
    private String hdlProxyDomain = "http://hdl.handle.net";
    
    public LamusCrawlerBridgeTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.HSQL)
                .setName("corpusstructure2")
                .build();
        
        crawlerBridge = new LamusCrawlerBridge();
        
        ReflectionTestUtils.setField(crawlerBridge, "hostName", hostName);
        ReflectionTestUtils.setField(crawlerBridge, "domainName", domainName);
        ReflectionTestUtils.setField(crawlerBridge, "prefixes", prefixes);
        ReflectionTestUtils.setField(crawlerBridge, "amsUrl", amsUrl);
        ReflectionTestUtils.setField(crawlerBridge, "mdsUrl", mdsUrl);
        ReflectionTestUtils.setField(crawlerBridge, "httpRoot", httpRoot);
        ReflectionTestUtils.setField(crawlerBridge, "localRoot", localRoot);
        ReflectionTestUtils.setField(crawlerBridge, "dbDriverClassName", dbDriverClassName);
        ReflectionTestUtils.setField(crawlerBridge, "dbUrl", dbUrl);
        ReflectionTestUtils.setField(crawlerBridge, "dbMaxActive", dbMaxActive);
        ReflectionTestUtils.setField(crawlerBridge, "dbMaxWait", dbMaxWait);
        ReflectionTestUtils.setField(crawlerBridge, "dbTestOnBorrow", dbTestOnBorrow);
        ReflectionTestUtils.setField(crawlerBridge, "dbUserName", dbUserName);
        ReflectionTestUtils.setField(crawlerBridge, "dbPassword", dbPassword);
        ReflectionTestUtils.setField(crawlerBridge, "connectionDriverName", connectionDriverName);
        ReflectionTestUtils.setField(crawlerBridge, "hdlProxyDomain", hdlProxyDomain);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void setUpCrawler() {
        
        Crawler result = crawlerBridge.setUpCrawler();
        
        assertNotNull("Retrieved crawler should not be null", result);
    }
    
    @Test
    public void setUpHandlerUtilities() {
        
        HandlerUtilities result = crawlerBridge.setUpHandlerUtilities();
        
        assertNotNull("Retrieved handler utilities should not be null", result);
    }
}