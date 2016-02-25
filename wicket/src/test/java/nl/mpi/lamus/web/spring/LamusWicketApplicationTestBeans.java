/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.web.spring;

import java.io.File;
import nl.mpi.archiving.tree.GenericTreeModelProvider;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.web.LamusWicketApplication;
import nl.mpi.lamus.web.model.mock.MockGenericTreeModelProviderFactory;
import nl.mpi.lamus.web.model.mock.MockWorkspace;
import nl.mpi.lamus.web.model.mock.MockWorkspaceService;
import nl.mpi.lamus.web.model.mock.MockWorkspaceTreeNode;
import nl.mpi.lamus.web.session.LamusSessionFactory;
import nl.mpi.lamus.web.session.mock.MockLamusSessionFactory;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import org.apache.wicket.Application;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 *
 * @author guisil
 */
@Configuration
@ComponentScan("nl.mpi.lamus.web")
@Profile("testing")
public class LamusWicketApplicationTestBeans {
    
    private MockLamusSessionFactory lamusSessionFactory;
    
    private LamusWicketApplication wicketApplication;
    
    private Workspace mockWorkspace;
    private WorkspaceTreeNode mockWorkspaceTreeNode;
    private MockGenericTreeModelProviderFactory mockTreeModelProviderFactory;
    
    @Bean
    public Application wicketApplication() {
        if(wicketApplication == null) {
            wicketApplication = new LamusWicketApplication(lamusSessionFactory);
        }
        return wicketApplication;
    }
    
    private Workspace mockWorkspace() {
        if(mockWorkspace == null) {
            mockWorkspace = new MockWorkspace();
        }
        return mockWorkspace;
    }
    
    private WorkspaceTreeNode mockWorkspaceTreeNode() {
        if(mockWorkspaceTreeNode == null) {
            mockWorkspaceTreeNode = new MockWorkspaceTreeNode();
        }
        return mockWorkspaceTreeNode;
    }
    
    private MockGenericTreeModelProviderFactory mockTreeModelProviderFactory() {
        if(mockTreeModelProviderFactory == null) {
            mockTreeModelProviderFactory = new MockGenericTreeModelProviderFactory();
        }
        return mockTreeModelProviderFactory;
    }
    
    @Bean
    public WorkspaceService workspaceService() {
        return new MockWorkspaceService(mockWorkspace(), mockWorkspaceTreeNode());
    }
    
    @Bean
    public GenericTreeModelProvider createWorkspaceTreeProvider() {
        return mockTreeModelProviderFactory().createTreeModelProvider(mockWorkspaceTreeNode());
    }
    
    @Bean
    @Qualifier("workspaceBaseDirectory")
    public File workspaceBaseDirectory() {
         return new File("/something/workspace/folder");
    }

    @Bean
    @Qualifier("workspaceUploadDirectoryName")
    public String workspaceUploadDirectoryName() {
        return "upload";
    }
    
    @Bean
    public LamusSessionFactory sessionFactory() {
        if(lamusSessionFactory == null) {
            lamusSessionFactory = new MockLamusSessionFactory();
            lamusSessionFactory.setUserId("testUser@mpi.test");
            lamusSessionFactory.setAuthenticated(Boolean.TRUE);
        }
        return lamusSessionFactory;
    }
}
