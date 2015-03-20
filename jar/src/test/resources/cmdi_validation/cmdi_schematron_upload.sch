<?xml version="1.0" encoding="UTF-8"?>
<sch:schema
    xmlns="http://purl.oclc.org/dsdl/schematron"
    xmlns:sch="http://purl.oclc.org/dsdl/schematron"
    queryBinding="xslt2"
    schemaVersion='ISO19757-3'
    defaultPhase='upload.phase'>
    <sch:title>CMDI Profile Validation Schematron</sch:title>
    
    <sch:ns prefix="cmd" uri="http://www.clarin.eu/cmd/" />
    <sch:ns prefix="xsi" uri="http://www.w3.org/2001/XMLSchema-instance" />

    <phase id="upload.phase">
        <active pattern="profile.check" />
    </phase>
    
    <sch:let name="profile" value="/cmd:CMD/cmd:Header/cmd:MdProfile" />
    <sch:let name="allowedProfilesDocument" value="document('target/test-classes/cmdi_validation/cmdi_allowed_profiles.xml')" />

    <sch:pattern id="profile.check">
        <sch:rule context="/cmd:CMD">
            <sch:assert id="assert.profile.allowed" role="error" test="$allowedProfilesDocument//profile[@id = normalize-space(current()/cmd:Header/cmd:MdProfile)] 
                            or $allowedProfilesDocument//profile[@id = tokenize(normalize-space(current()/@xsi:schemaLocation), '/')[last() - 1]]">
                [CMDI Archive Restriction] the CMD profile of this record is not allowed in the archive.
            </sch:assert>
        </sch:rule>
    </sch:pattern>
    
</sch:schema>