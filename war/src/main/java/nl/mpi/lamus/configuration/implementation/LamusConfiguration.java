package nl.mpi.lamus.configuration.implementation;


import java.io.File;
import java.util.Collection;
import nl.mpi.lamus.ams.AmsBridge;
import nl.mpi.lamus.configuration.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class LamusConfiguration implements Configuration {

    @Autowired
    private AmsBridge amsBridge;
    /** ingest request storage quota, default 10 GB */
    private long defaultMaxStorageSpace = 10 * 1024 * 1024 * 1024; // in bytes
    /** ingest request allowed inactivity period before starting to send warning emails to its owner, default 60 days */
    private int numberOfDaysOfInactivityAllowedSinceLastSession = 60; // 2 months
    /** ingest request allowed period (since creation) before starting to send warning emails to its owner, default 180 days */
    private int totalNumberOfDaysAllowedUntilExpiry = 180; // 6 months
    /** ingest request allowed period, since the last warning email, before sending the next one, default 30 days */
    private int numberOfDaysOfInactivityAllowedSinceLastWarningEmail = 30;
    
    /** file size limit for which the typechecker should check a file again (when importing from the archive), default 8MB **/
    private long typeReCheckSizeLimit = 8 * 1024 * 1024;
    
//    private final static LamusConfiguration instance = new LamusConfiguration();
    
//    public LamusConfiguration() {
//        
//    }
    
//    public static LamusConfiguration getInstance() {
//        return instance;
//    }
    
    
    public AmsBridge getAmsBridge() {
        return this.amsBridge;
    }
    
//    @Autowired
//    public void setAmsBridge(AmsBridge amsBridge) {
//        this.amsBridge = amsBridge;
//    }
    
    public long getDefaultMaxStorageSpace() {
        //TODO Check if it is overwritten in some configuration file
        return this.defaultMaxStorageSpace;
    }
    
    public int getNumberOfDaysOfInactivityAllowedSinceLastSession() {
        return this.numberOfDaysOfInactivityAllowedSinceLastSession;
    }
    
    public int getTotalNumberOfDaysAllowedUntilExpiry() {
        return this.totalNumberOfDaysAllowedUntilExpiry;
    }
    
    public int getNumberOfDaysOfInactivityAllowedSinceLastWarningEmail() {
        return this.numberOfDaysOfInactivityAllowedSinceLastWarningEmail;
    }

    public File getWorkspaceBaseDirectory() {
        //TODO load this from properties/context
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public Collection<File> getRelaxedTypeCheckFolders() {
        //TODO load this from properties/context
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public File getRelaxedTypeCheckConfigFile() {
        //TODO load this from properties/context
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getTypeReCheckSizeLimit() {
        return this.typeReCheckSizeLimit;
    }
    
}
