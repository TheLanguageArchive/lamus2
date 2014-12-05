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
package nl.mpi.lamus.typechecking.implementation;

import com.helger.schematron.ISchematronResource;
import com.helger.schematron.xslt.SchematronResourceSCH;
import java.io.File;
import javax.xml.transform.stream.StreamSource;
import nl.mpi.lamus.typechecking.MetadataChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @see MetadataChecker
 * @author guisil
 */
@Component
public class LamusMetadataChecker implements MetadataChecker {
    
    @Autowired
    @Qualifier("schematronFile")
    private File schematronFile;

    private static final String profilePhase = "profilePhase";
    
    
    /**
     * @see MetadataChecker#isProfileAllowed(java.io.File)
     */
    @Override
    public boolean isProfileAllowed(File metadataFile) throws Exception {
        
        final ISchematronResource schResXslt_MdProfilePhase = SchematronResourceSCH.fromFile(schematronFile, profilePhase, (String) null);
        
        if(!schResXslt_MdProfilePhase.isValidSchematron()) {
            throw new IllegalArgumentException("Invalid Schematron");
        }
        return schResXslt_MdProfilePhase.getSchematronValidity(new StreamSource(metadataFile)).isValid();
    }
}
