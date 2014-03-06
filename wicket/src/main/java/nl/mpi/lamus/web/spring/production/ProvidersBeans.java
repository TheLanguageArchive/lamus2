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
package nl.mpi.lamus.web.spring.production;

import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProviderFactory;
import nl.mpi.archiving.tree.GenericTreeModelProvider;
import nl.mpi.archiving.tree.corpusstructure.CorpusStructureTreeModelProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;

/**
 *
 * @author guisil
 */
//@Configuration
//@Profile(value = {"production, demoserver"})
public class ProvidersBeans {
    
//    @Autowired
//    private CorpusStructureProviderFactory csdbFactory;
//  
//    @Bean
//    @Scope("prototype")
//    @Qualifier("createWorkspaceTreeProvider")
//    public GenericTreeModelProvider createWorkspaceTreeProvider() {
//        return new CorpusStructureTreeModelProvider(csdbFactory);
//    }
    
    
    
    
//    <bean id="corpusStructureDataSource" class="org.springframework.jndi.JndiObjectFactoryBean">
//	<property name="jndiName" value="java:comp/env/jdbc/CSDB2"/>   
//	<property name="resourceRef" value="true" />
//    </bean>
//    
//    
//    <bean id="archiveDao" class="nl.mpi.archiving.corpusstructure.core.database.dao.impl.ArchiveDaoImpl" />
//    <bean id="aoDao" class="nl.mpi.archiving.corpusstructure.core.database.dao.impl.ArchiveObjectsDaoImpl" />
//    <bean id="csDao" class="nl.mpi.archiving.corpusstructure.core.database.dao.impl.CorpusStructureDaoImpl" />
//    
//    <!-- Factory for CorpusStructure/AccessInfo. This pattern is used to allow for transactional instantiation of the providers. -->
//    <bean id="providerFactory" class="nl.mpi.lamus.cmdi.providers.ProductionCorpusStructureProviderFactory" />
//    
//    <!-- CSDB created from factory -->
//    <bean id="corpusStructureProvider" factory-bean="providerFactory" factory-method="createCorpusStructureProvider" />
//    
//    <!--<bean id="nodeResolver" class="nl.mpi.archiving.corpusstructure.provider.db.service.impl.CorpusStructureProviderNodeResolver" />-->
//    
//    <bean id="createWorkspaceTreeProvider" class="nl.mpi.archiving.tree.corpusstructure.CorpusStructureTreeModelProvider">
//	<constructor-arg ref="corpusStructureProvider" />
//    </bean>
}
