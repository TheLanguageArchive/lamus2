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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import nl.mpi.archiving.corpusstructure.core.AccessInfo;
import nl.mpi.archiving.corpusstructure.writer.CorpusstructureWriter;
import nl.mpi.bcarchive.typecheck.FileType;
import nl.mpi.lamus.ams.Ams2Bridge;
import nl.mpi.lamus.ams.AmsBridge;
import nl.mpi.lamus.mock.MockAccessInfo;
import nl.mpi.lamus.mock.MockCorpusstructureWriter;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.cmdi.api.CMDIApi;
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
@Profile("production")
public class LamusBeans {
    
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
    
    private AccessInfo defaultAccessInfo;
    
    @Bean
    public AccessInfo defaultAccessInfo() {
        
        if(this.defaultAccessInfo == null) {
            this.defaultAccessInfo = new MockAccessInfo();
        }
        return this.defaultAccessInfo;
    }
    
    
    //TODO Load beans from AMS
    @Bean
    public AmsBridge amsBridge() {
        return new Ams2Bridge();
    }
    
    //TODO Proper implementation of CorpusstructureWriter
    @Bean
    public CorpusstructureWriter corpusstructureWriter() {
        return new MockCorpusstructureWriter();
    }
    
}
