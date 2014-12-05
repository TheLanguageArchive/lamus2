<?xml version="1.0" encoding="UTF-8"?>
<sch:schema
    xmlns="http://purl.oclc.org/dsdl/schematron"
    xmlns:sch="http://purl.oclc.org/dsdl/schematron"
    queryBinding="xslt2"
    schemaVersion='ISO19757-3'
    defaultPhase='profilePhase'>
    <sch:title>CMDI Profile Validation Schematron</sch:title>
    
    <sch:ns prefix="cmd" uri="http://www.clarin.eu/cmd/" />
    <sch:ns prefix="xsi" uri="http://www.w3.org/2001/XMLSchema-instance" />

    <phase id="profilePhase">
        <active pattern="profile.check" />
    </phase>

    <sch:pattern id="profile.check">
        <sch:rule context="/cmd:CMD">
            <sch:assert test="document('target/test-classes/cmdi_allowed_profiles.xml')//profile[@id = normalize-space(current()/cmd:Header/cmd:MdProfile)] or document('target/test-classes/cmdi_allowed_profiles.xml')//profile[@id = tokenize(normalize-space(current()/@xsi:schemaLocation), '/')[last() - 1]]">Profile not allowed</sch:assert>
        </sch:rule>
    </sch:pattern>
    
</sch:schema>