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
package nl.mpi.lamus.web.spring.mock;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.web.model.mock.MockWorkspace;
import nl.mpi.lamus.web.model.mock.MockWorkspaceService;
import nl.mpi.lamus.web.model.mock.MockWorkspaceTreeNode;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 *
 * @author guisil
 */
@Configuration
@Profile("mock")
public class MockWorkspaceBeans {
    
    @Bean
    public WorkspaceService workspaceService() throws URISyntaxException, MalformedURLException {
        return new MockWorkspaceService(mockCreatedWorkspace(), mockWorkspaceTreeNode());
    }
    
    @Bean
    public Workspace mockCreatedWorkspace() {
        Workspace mockCreatedWorkspace = new MockWorkspace();
        mockCreatedWorkspace.setWorkspaceID(1);
        return mockCreatedWorkspace;
    }
    
    @Bean
    public WorkspaceTreeNode mockWorkspaceTreeNode() throws URISyntaxException, MalformedURLException {
        
        MockWorkspaceTreeNode mockWorkspaceTreeNode = new MockWorkspaceTreeNode();
        mockWorkspaceTreeNode.setName("workspaceTreeNode");
        mockWorkspaceTreeNode.setArchiveURI(new URI("node:120"));
        mockWorkspaceTreeNode.setArchiveURL(new URL("http://google.nl"));
        mockWorkspaceTreeNode.setWorkspaceID(1);
        mockWorkspaceTreeNode.setWorkspaceNodeID(10);
        mockWorkspaceTreeNode.setType(WorkspaceNodeType.METADATA);

        List<WorkspaceTreeNode> children = new ArrayList<WorkspaceTreeNode>();

        MockWorkspaceTreeNode child1 = new MockWorkspaceTreeNode();
        child1.setParent(mockWorkspaceTreeNode);
        child1.setName("WorkspaceChild1");
        child1.setWorkspaceID(1);
        child1.setWorkspaceNodeID(12);
        child1.setType(WorkspaceNodeType.RESOURCE_WR);
        child1.setArchiveURI(new URI("node:100"));
        children.add(child1);

        MockWorkspaceTreeNode child2 = new MockWorkspaceTreeNode();
        child2.setParent(mockWorkspaceTreeNode);
        child2.setName("WorkspaceChild2");
        child2.setWorkspaceID(1);
        child2.setWorkspaceNodeID(13);
        child2.setType(WorkspaceNodeType.RESOURCE_MR);
        child2.setArchiveURI(new URI("node:200"));
        children.add(child2);

        mockWorkspaceTreeNode.setChildren(children);

        return mockWorkspaceTreeNode;
    }
}
