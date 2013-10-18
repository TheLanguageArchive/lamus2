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
import nl.mpi.archiving.tree.GenericTreeModelProvider;
import nl.mpi.archiving.tree.LinkedTreeModelProvider;
import nl.mpi.archiving.tree.LinkedTreeNode;
import nl.mpi.archiving.tree.corpusstructure.CorpusStructureArchiveNode;
import nl.mpi.lamus.web.model.mock.MockCorpusNode;
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
public class MockArchiveTreeBeans {
    
    @Bean
    public GenericTreeModelProvider createWorkspaceTreeProvider() {
        return new LinkedTreeModelProvider(archiveRootNode());
    }
    
    @Bean
    @Qualifier("archiveRootNode")
    public LinkedTreeNode archiveRootNode() {

        MockCorpusNode archiveRootNode = new MockCorpusNode();
        archiveRootNode.setName("RootNode");

        List<CorpusStructureArchiveNode> children = new ArrayList<CorpusStructureArchiveNode>();

        MockCorpusNode child1 = new MockCorpusNode();
        child1.setParent(archiveRootNode);
        child1.setName("Child1");
//        child1.setNodeId(101);
        children.add(child1);

        MockCorpusNode child2 = new MockCorpusNode();
        child2.setParent(archiveRootNode);
        child2.setName("Child2");
//        child2.setNodeId(102);
        children.add(child2);

        archiveRootNode.setChildren(children);

        return archiveRootNode;
    }
}
