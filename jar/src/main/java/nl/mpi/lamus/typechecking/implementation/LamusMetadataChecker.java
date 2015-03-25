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

import com.helger.commons.io.resource.FileSystemResource;
import com.helger.schematron.ISchematronResource;
import com.helger.schematron.svrl.SVRLFailedAssert;
import com.helger.schematron.svrl.SVRLUtils;
import com.helger.schematron.xslt.SchematronResourceSCH;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.transform.stream.StreamSource;
import nl.mpi.lamus.typechecking.MetadataChecker;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;
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
    @Qualifier("schematronFile_upload")
    private File schematronFile_upload;
    @Autowired
    @Qualifier("schematronFile_submit")
    private File schematronFile_submit;
    private static final String uploadPhase = "upload.phase";
    private static final String submitPhase = "submit.phase";

    /**
     * @see MetadataChecker#validateUploadedFile(java.io.File)
     */
    @Override
    public Collection<MetadataValidationIssue> validateUploadedFile(File metadataFile) throws Exception {

        final ISchematronResource schRes = getSchematronResource(schematronFile_upload, uploadPhase);

        return validateFile(schRes, metadataFile);
    }

    /**
     * @see MetadataChecker#validateSubmittedFile(java.io.File)
     */
    @Override
    public Collection<MetadataValidationIssue> validateSubmittedFile(File metadataFile) throws Exception {

        final ISchematronResource schRes = getSchematronResource(schematronFile_submit, submitPhase);

        return validateFile(schRes, metadataFile);
    }

    private ISchematronResource getSchematronResource(File schFile, String phase) {

        final ISchematronResource schRes = new SchematronResourceSCH(new FileSystemResource(schFile), phase, (String) null);

        if (!schRes.isValidSchematron()) {
            throw new IllegalArgumentException("Invalid Schematron");
        }

        return schRes;
    }

    private Collection<MetadataValidationIssue> validateFile(ISchematronResource schRes, File fileToValidate) throws Exception {

        Collection<MetadataValidationIssue> issues = new ArrayList<>();

        SchematronOutputType result = schRes.applySchematronValidationToSVRL(new StreamSource(fileToValidate));

        if (result == null) {
            return issues;
        }

        List<SVRLFailedAssert> failedAssertions = SVRLUtils.getAllFailedAssertions(result);

        for (SVRLFailedAssert failure : failedAssertions) {
            issues.add(new MetadataValidationIssue(fileToValidate, failure.getTest(), failure.getText().trim(), failure.getRole()));
        }

        return issues;
    }
}