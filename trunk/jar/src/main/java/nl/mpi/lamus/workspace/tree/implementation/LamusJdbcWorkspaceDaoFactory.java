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
package nl.mpi.lamus.workspace.tree.implementation;

import javax.sql.DataSource;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.dao.implementation.LamusJdbcWorkspaceDao;
import nl.mpi.lamus.workspace.tree.WorkspaceDaoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @see WorkspaceDaoFactory
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class LamusJdbcWorkspaceDaoFactory implements WorkspaceDaoFactory {

    private final static Logger logger = LoggerFactory.getLogger(LamusJdbcWorkspaceDaoFactory.class);
    
    @Autowired
    @Qualifier("lamusDataSource")
    private transient DataSource lamusDataSource;
    
    /**
     * @see WorkspaceDaoFactory#createWorkspaceDao()
     */
    @Override
    public WorkspaceDao createWorkspaceDao() {
        logger.debug("Creating new LamusJdbcWorkspaceDao");
	return new LamusJdbcWorkspaceDao(lamusDataSource);
    }
    
}
