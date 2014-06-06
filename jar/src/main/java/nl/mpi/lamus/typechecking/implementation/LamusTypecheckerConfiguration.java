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

import java.net.URL;
import java.util.Map;
import javax.annotation.Resource;
import nl.mpi.lamus.typechecking.TypecheckerConfiguration;
import nl.mpi.lamus.typechecking.TypecheckerJudgement;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @see TypecheckerConfiguration
 * 
 * @author guisil
 */
@Component
public class LamusTypecheckerConfiguration implements TypecheckerConfiguration {
    
    @Resource
    @Qualifier("customTypecheckerFolderToConfigFileMap")
    private Map<String, String> customTypecheckerFolderToConfigFileMap;

    /**
     * @see TypecheckerConfiguration#getAcceptableJudgementForLocation(java.net.URL)
     */
    @Override
    public TypecheckerJudgement getAcceptableJudgementForLocation(URL urlToCheck) {
        
        if(customTypecheckerFolderToConfigFileMap.containsKey(urlToCheck.getPath())) {
            
            //TODO this must be changed/improved
                // use something similar to the old LAMUS
                // OR implement something more complex, with several configuration files
                    // and more flexible in terms of judgements
            return TypecheckerJudgement.ARCHIVABLE_SHORTTERM;
        }
        
        return TypecheckerJudgement.ARCHIVABLE_LONGTERM;
    }
    
}
