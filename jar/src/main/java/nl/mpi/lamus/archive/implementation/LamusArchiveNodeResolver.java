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

import java.net.MalformedURLException;
import java.net.URL;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.provider.db.service.impl.CorpusStructureProviderNodeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Extension of CorpusStructureProviderNodeResolver, which
 * resolves a node from its external URI to the local one, if necessary.
 * @author guisil
 */
@Component
public class LamusArchiveNodeResolver extends CorpusStructureProviderNodeResolver {

    private static final Logger logger = LoggerFactory.getLogger(LamusArchiveNodeResolver.class);
    
    @Autowired
    @Qualifier("dbHttpRoot")
    private String dbHttpRoot;
    @Autowired
    @Qualifier("dbLocalRoot")
    private String dbLocalRoot;
    
    @Override
    public URL getUrl(CorpusNode node) {
        URL dbUrl = super.getUrl(node);
        
        if(dbUrl.toString().startsWith(dbHttpRoot)) {
            try {
                return new URL(dbUrl.toString().replace(dbHttpRoot, dbLocalRoot));
            } catch (MalformedURLException ex) {
                logger.warn("Couldn't replace node http URI by a local one (" + dbUrl + ")", ex);
            }
        }
        
        return dbUrl;
    }
}
