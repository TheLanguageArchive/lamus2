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
package nl.mpi.lamus.typechecking;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Configuration
public class LamusTypecheckingTestProperties {
    
    @Bean
    public Map<File, File> customTypecheckerFolderToConfigFileMap() {
        
        Map<File, File> mapToReturn = new HashMap<File, File>();
        mapToReturn.put(new File("folder1"), new File("config_file1"));
        mapToReturn.put(new File("folder2"), new File("config_file2"));
        mapToReturn.put(new File("folder3"), new File("config_file3"));
        mapToReturn.put(new File("folder4"), new File("config_file4"));
        
        return mapToReturn;
    }
}
