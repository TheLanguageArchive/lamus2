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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import nl.mpi.bcarchive.typecheck.DeepFileType;
import nl.mpi.bcarchive.typecheck.FileType;
import nl.mpi.handle.util.HandleInfoRetriever;
import nl.mpi.handle.util.HandleManager;
import nl.mpi.handle.util.implementation.HandleInfoRetrieverImpl;
import nl.mpi.handle.util.implementation.HandleManagerImpl;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.cmdi.api.CMDIApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Configuration
@ImportResource("classpath:nl/mpi/lamus/spring/amsService.xml")
@ComponentScan("nl.mpi.lamus")
@Profile(value = {"production", "cmdi-adapter-csdb", "demoserver"})
public class LamusBeans {
    
    @Autowired
    @Qualifier("handlePrefix")
    private String handlePrefix;
    @Autowired
    @Qualifier("handleAdminKeyFile")
    private String handleAdminKeyFilePath;
    @Autowired
    @Qualifier("handleAdminUserHandleIndex")
    private String handleAdminUserHandleIndex;
    @Autowired
    @Qualifier("handleAdminUserHandle")
    private String handleAdminUserHandle;
    @Autowired
    @Qualifier("handleAdminHandlePassword")
    private String handleAdminPassword;
    
    
    @Bean
    @Qualifier("WorkspaceExecutorService")
    public ExecutorService workspaceExecutorService() {
        return Executors.newSingleThreadExecutor();
    }
    
    @Bean
    @Qualifier("CrawlCheckerExecutorService")
    public ScheduledExecutorService crawlCheckerExecutorService() {
        return Executors.newScheduledThreadPool(1);
    }
    
    //TODO change properties to initialise API
    @Bean
    public MetadataAPI metadataAPI() {
        return new CMDIApi();
    }
    
    @Bean
    public FileType typechecker() {
        return new FileType();
    }
    
    @Bean
    public DeepFileType deepTypechecker() {
        return new DeepFileType();
    }
    
    
    //TODO Load beans from AMS
//    @Bean
//    public AmsBridge amsBridge() {
//        return new Ams2Bridge();
//    }
//    
    
    @Bean
    public HandleInfoRetriever handleInfoRetriever() {
        return new HandleInfoRetrieverImpl(handlePrefix);
    }
    
    @Bean
    public HandleManager handleManager() throws FileNotFoundException, IOException {
        return new HandleManagerImpl(handleInfoRetriever(), handleUtil(), handlePrefix);
    }
    
    @Bean
    public nl.mpi.handle.util.implementation.HandleUtil handleUtil() {
        return new nl.mpi.handle.util.implementation.HandleUtil(handleAdminKeyFilePath, handleAdminUserHandleIndex, handleAdminUserHandle, handleAdminPassword);
    }
    
    @Bean
    public nl.mpi.metadata.api.util.HandleUtil metadataApiHandleUtil() {
        return new nl.mpi.metadata.api.util.HandleUtil();
    }
    
//    @Bean
//    public IAmsRemoteService amsRemoteService() {
//        HttpInvokerProxyFactoryBean proxy = new HttpInvokerProxyFactoryBean();
//        proxy.setServiceInterface(IAmsRemoteService.class);
//        proxy.setServiceUrl("http://lux16.mpi.nl/am/ams2-cmdi/api/v1/httpinvoker/AmsRemoteService");
//        IAmsRemoteService service = (IAmsRemoteService) proxy.getObject();
//        return service;
//    }
}
