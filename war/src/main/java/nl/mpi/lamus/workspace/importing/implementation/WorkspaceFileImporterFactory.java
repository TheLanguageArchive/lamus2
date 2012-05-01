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
package nl.mpi.lamus.workspace.importing.implementation;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import nl.mpi.lamus.workspace.exception.FileImporterInitialisationException;
import nl.mpi.lamus.workspace.importing.FileImporter;
import nl.mpi.lamus.workspace.importing.FileImporterFactory;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.metadata.api.model.MetadataReference;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ResourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class WorkspaceFileImporterFactory implements FileImporterFactory {

    private static final Logger logger = LoggerFactory.getLogger(WorkspaceFileImporterFactory.class);
    
    //TODO Inject using Spring instead of initialising here
    private static Map<Class<? extends Reference>, Class<? extends FileImporter>> fileImporterTypeMap = createFileImporterTypeMap();
    
    //TODO should there be a different type of FileImporter for the top node?
    private static final Class<? extends FileImporter> importerTypeForTopNode = MetadataFileImporter.class;

    private static Map<Class<? extends Reference>, Class<? extends FileImporter>> createFileImporterTypeMap() {
        Map<Class<? extends Reference>, Class<? extends FileImporter>> map = 
                new HashMap<Class<? extends Reference>, Class<? extends FileImporter>>();
        map.put(MetadataReference.class, MetadataFileImporter.class);
        map.put(ResourceReference.class, ResourceFileImporter.class);
        return map;
    }
    
    private final Workspace workspace;
    
    public WorkspaceFileImporterFactory(Workspace workspace) {
        this.workspace = workspace;
    }
    
    Map<Class<? extends Reference>, Class<? extends FileImporter>> getFileImporterTypeMap() {
        return Collections.unmodifiableMap(fileImporterTypeMap);
    }
    
    /**
     * 
     * @param referenceType
     * @return
     */
    public Class<? extends FileImporter> getFileImporterTypeForReference(Class<? extends Reference> referenceType) {
        
        Class<? extends FileImporter> importerType = getFileImporterTypeMap().get(referenceType);
        return importerType;
    }
    
    /**
     * 
     * @return
     */
    public Class<? extends FileImporter> getFileImporterTypeForTopNode() {
        
        return importerTypeForTopNode;
    }
    
    /**
     * 
     * @param importerType
     * @return
     * @throws FileImporterInitialisationException 
     */
    public FileImporter getNewFileImporterOfType(Class<? extends FileImporter> importerType) throws FileImporterInitialisationException {
        
        Constructor<? extends FileImporter> importerConstructor;
        try {
            importerConstructor = importerType.getDeclaredConstructor();
            
            //TODO this should not be done using the default constructor but using Spring injection
            
        } catch (NoSuchMethodException ex) {
            throw new FileImporterInitialisationException(
                    "FileImporter subtype does not have a default constructor.", workspace, importerType, ex);
        } catch (SecurityException ex) {
            throw new FileImporterInitialisationException(
                    "FileImporter subtype's information is not accessible.", workspace, importerType, ex);
        }
        
        FileImporter importer = null;
        try {
            importer = importerConstructor.newInstance();
        } catch (InstantiationException ex) {
            throw new FileImporterInitialisationException(
                    "FileImporter subtype could not be instantiated because it is an abstract class.", workspace, importerType, ex);
        } catch (IllegalAccessException ex) {
            throw new FileImporterInitialisationException(
                    "FileImporter subtype's constructor is inaccessible.", workspace, importerType, ex);
        } catch (IllegalArgumentException ex) {
            throw new FileImporterInitialisationException(
                    "FileImporter subtype's constructor has different parameters from expected.", workspace, importerType, ex);
        } catch (InvocationTargetException ex) {
            throw new FileImporterInitialisationException(
                    "FileImporter subtype's constructor threw an exception.", workspace, importerType, ex);
        }
        
        return importer;
    }    
}
