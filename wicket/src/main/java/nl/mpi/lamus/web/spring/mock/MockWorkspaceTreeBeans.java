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

import java.util.ArrayList;
import java.util.List;
import nl.mpi.archiving.tree.GenericTreeModelProviderFactory;
//import nl.mpi.archiving.tree.WorkspaceTreeNode;
import nl.mpi.lamus.web.model.mock.MockCorpusNode;
import nl.mpi.lamus.web.model.mock.MockGenericTreeModelProviderFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 *
 * @author guisil
 */
@Configuration
@Profile("mock")
public class MockWorkspaceTreeBeans {
    
    @Bean
    public GenericTreeModelProviderFactory workspaceTreeProviderFactory() {
        return new MockGenericTreeModelProviderFactory();
    }
    
//    @Bean
//    @Qualifier("workspaceRootNode")
//    public WorkspaceTreeNode workspaceRootNode() {
//        
//        MockCorpusNode workspaceRootNode = new MockCorpusNode();
//        workspaceRootNode.setName("Workspace");
//
//        List<GenericTreeNode> children = new ArrayList<GenericTreeNode>();
//
//        MockCorpusNode child1 = new MockCorpusNode();
//        child1.setParent(workspaceRootNode);
//        child1.setName("WorkspaceChild1");
////        child1.setNodeId(101);
//        children.add(child1);
//
//        MockCorpusNode child2 = new MockCorpusNode();
//        child2.setParent(workspaceRootNode);
//        child2.setName("WorkspaceChild2");
////        child2.setNodeId(102);
//        children.add(child2);
//
//        workspaceRootNode.setChildren(children);
//        
//        return workspaceRootNode;
//    }
}
