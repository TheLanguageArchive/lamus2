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
import javax.annotation.Resource;
import nl.mpi.bcarchive.typecheck.FileType;
import nl.mpi.handle.util.HandleInfoRetriever;
import nl.mpi.handle.util.HandleManager;
import nl.mpi.handle.util.implementation.HandleInfoRetrieverImpl;
import nl.mpi.handle.util.implementation.HandleManagerImpl;
import nl.mpi.lamus.ams.Ams2Bridge;
import nl.mpi.lamus.ams.AmsBridge;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.cmdi.api.CMDIApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Configuration
@ComponentScan("nl.mpi.lamus")
@Profile(value = {"production", "cmdi-adapter-csdb"})
public class LamusBeans {
    
    @Autowired
    @Qualifier("handlePrefix")
    private String handlePrefix;
    @Autowired
    @Qualifier("handleProxy")
    private String handleProxy;
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
    public ExecutorService executorService() {
        return Executors.newSingleThreadExecutor();
    }
    
    //TODO change properties to initialise API
    @Bean
    public MetadataAPI metadataAPI() {
        return new CMDIApi();
    }
    
    @Bean
    public FileType typeChecker() {
        return new FileType();
    }
    
    
    //TODO Load beans from AMS
    @Bean
    public AmsBridge amsBridge() {
        return new Ams2Bridge();
    }
    
    
    @Bean
    public HandleInfoRetriever handleInfoRetriever() {
        return new HandleInfoRetrieverImpl(handlePrefix, handleProxy);
    }
    
    @Bean
    public HandleManager handleManager() throws FileNotFoundException, IOException {
        return new HandleManagerImpl(handleInfoRetriever(), handleUtil(), handlePrefix, handleProxy);
    }
    
    @Bean
    public nl.mpi.handle.util.implementation.HandleUtil handleUtil() {
        return new nl.mpi.handle.util.implementation.HandleUtil(handleAdminKeyFilePath, handleAdminUserHandleIndex, handleAdminUserHandle, handleAdminPassword);
    }
    
    @Bean
    public nl.mpi.metadata.api.util.HandleUtil metadataApiHandleUtil() {
        return new nl.mpi.metadata.api.util.HandleUtil();
    }
}
