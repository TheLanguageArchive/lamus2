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
    
    private String crawlerHostName = "host.mpi.nl";
    private String crawlerDomainName = "mpi.nl";
    private String handlePrefix = "01234";
    private String crawlerAmsUrl = "";
    private String crawlerMdsUrl = "";
    private String dbHttpRoot = "http://host.mpi.nl/archive/";
    private String dbLocalRoot = "file:/somewhere/archive/";
    private String crawlerDbDriverClassName = "org.hsqldb.jdbcDriver";
    private String crawlerDbUrl = "jdbc:hsqldb:mem:corpusstructure2";
    private String crawlerDbMaxActive = "100";
    private String crawlerDbMaxWait = "10000";
    private String crawlerDbTestOnBorrow = "true";
    private String crawlerDbUsername = "sa";
    private String crawlerDbPassword = "";
    private String crawlerConnectionDriverName = "org.apache.commons.dbcp.BasicDataSource";
    private String crawlerHdlProxyDomain = "http://hdl.handle.net";
    
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
        
        ReflectionTestUtils.setField(crawlerBridge, "crawlerHostName", crawlerHostName);
        ReflectionTestUtils.setField(crawlerBridge, "crawlerDomainName", crawlerDomainName);
        ReflectionTestUtils.setField(crawlerBridge, "handlePrefix", handlePrefix);
        ReflectionTestUtils.setField(crawlerBridge, "crawlerAmsUrl", crawlerAmsUrl);
        ReflectionTestUtils.setField(crawlerBridge, "crawlerMdsUrl", crawlerMdsUrl);
        ReflectionTestUtils.setField(crawlerBridge, "dbHttpRoot", dbHttpRoot);
        ReflectionTestUtils.setField(crawlerBridge, "dbLocalRoot", dbLocalRoot);
        ReflectionTestUtils.setField(crawlerBridge, "crawlerDbDriverClassName", crawlerDbDriverClassName);
        ReflectionTestUtils.setField(crawlerBridge, "crawlerDbUrl", crawlerDbUrl);
        ReflectionTestUtils.setField(crawlerBridge, "crawlerDbMaxActive", crawlerDbMaxActive);
        ReflectionTestUtils.setField(crawlerBridge, "crawlerDbMaxWait", crawlerDbMaxWait);
        ReflectionTestUtils.setField(crawlerBridge, "crawlerDbTestOnBorrow", crawlerDbTestOnBorrow);
        ReflectionTestUtils.setField(crawlerBridge, "crawlerDbUsername", crawlerDbUsername);
        ReflectionTestUtils.setField(crawlerBridge, "crawlerDbPassword", crawlerDbPassword);
        ReflectionTestUtils.setField(crawlerBridge, "crawlerConnectionDriverName", crawlerConnectionDriverName);
        ReflectionTestUtils.setField(crawlerBridge, "crawlerHdlProxyDomain", crawlerHdlProxyDomain);
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