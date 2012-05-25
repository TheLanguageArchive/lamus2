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
package nl.mpi.lamus.typechecking.implementation;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import javax.annotation.Resource;
import nl.mpi.bcarchive.typecheck.FileType;
import nl.mpi.lamus.typechecking.FileTypeFactory;
import nl.mpi.lamus.typechecking.FileTypeHandler;
import nl.mpi.lamus.typechecking.FileTypeHandlerFactory;
import nl.mpi.lamus.workspace.model.TypeMapper;
import nl.mpi.lamus.workspace.model.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class LamusFileTypeHandlerFactory implements FileTypeHandlerFactory {

    private static final Logger logger = LoggerFactory.getLogger(LamusFileTypeHandlerFactory.class);
    
    private final FileTypeFactory fileTypeFactory;
    private final TypeMapper typeMapper;
    
    @Resource
    private Map<File, File> customTypecheckerFolderToConfigFileMap;
    
    @Autowired
    public LamusFileTypeHandlerFactory(FileTypeFactory fileTypeFactory, TypeMapper typeMapper) {
        this.fileTypeFactory = fileTypeFactory;
        this.typeMapper = typeMapper;
    }
    
    //TODO in order to inject dependencies, maybe it's better to have the configuration receive the workspace object and,
        // based on that, decide which is the type configuration file to use
    
    public FileTypeHandler getNewFileTypeHandlerForWorkspace(Workspace workspace) {
        
//        Collection<File> relaxedTypeCheckFolders = configuration.getRelaxedTypeCheckFolders();
        FileType typeCheckerToUse = null;
        if(customTypecheckerFolderToConfigFileMap == null || customTypecheckerFolderToConfigFileMap.isEmpty()
                || workspace.getTopNodeArchiveURL() == null) {
             typeCheckerToUse = fileTypeFactory.getNewFileTypeWithDefaultConfigFile();
        } else {
            URL topNodeURL = workspace.getTopNodeArchiveURL();
            
            outerloop:
//            for (File folder : relaxedTypeCheckFolders) {
            for(File folder : customTypecheckerFolderToConfigFileMap.keySet()) {
                File temp = new File(topNodeURL.getPath());
                while (temp != null) { // check if this folder is a parent of the temp
                    if (temp.equals(folder)) {
                        File relaxedTypeCheckConfigFile = customTypecheckerFolderToConfigFileMap.get(folder);
                        typeCheckerToUse = fileTypeFactory.getNewFileTypeWithConfigFile(relaxedTypeCheckConfigFile);
                        break outerloop;
                    }
                    temp = temp.getParentFile();
                } // loop towards root directory
            } // loop over relaxedTypeCheckFolders
            if(typeCheckerToUse == null) {
                typeCheckerToUse = fileTypeFactory.getNewFileTypeWithDefaultConfigFile();
            }
        }
        
        return new LamusFileTypeHandler(typeCheckerToUse, typeMapper);
    }
    
}
