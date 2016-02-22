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
package nl.mpi.lamus.typechecking.implementation;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import javax.annotation.Resource;
import nl.mpi.lamus.typechecking.TypecheckerConfiguration;
import nl.mpi.lamus.typechecking.TypecheckerJudgement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @see TypecheckerConfiguration
 * 
 * @author guisil
 */
@Component
public class LamusTypecheckerConfiguration implements TypecheckerConfiguration {
    
    Logger logger = LoggerFactory.getLogger(LamusTypecheckerConfiguration.class);
    
    @Resource
    @Qualifier("customTypecheckerSpecialConfigFolders")
    private Collection<String> customTypecheckerSpecialConfigFolders;

    /**
     * @see TypecheckerConfiguration#getAcceptableJudgementForLocation(java.io.File)
     */
    @Override
    public TypecheckerJudgement getAcceptableJudgementForLocation(File fileToCheck) {
        
        if(isChildOfAnyPathInCollection(Paths.get(fileToCheck.toURI()))) {
            return TypecheckerJudgement.ARCHIVABLE_SHORTTERM;
        }
        
        return TypecheckerJudgement.ARCHIVABLE_LONGTERM;
    }
    

    private boolean isChildOfAnyPathInCollection(Path pathToCheck) {
        for(String folder : customTypecheckerSpecialConfigFolders) {
            if(pathToCheck.startsWith(folder)) {
                return true;
            }
        }
        return false;
    }
}
