<?xml version="1.0" encoding="UTF-8"?>
<schema
    xmlns="http://purl.oclc.org/dsdl/schematron"
    xmlns:sch="http://purl.oclc.org/dsdl/schematron"
    xmlns:cmd="http://www.clarin.eu/cmd/"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    queryBinding="xslt2"
    schemaVersion='ISO19757-3'
    defaultPhase='submit.phase'>
    <title>CMDI Profile Validation Schematron</title>
    
    <ns prefix="cmd" uri="http://www.clarin.eu/cmd/" />
    <ns prefix="xsi" uri="http://www.w3.org/2001/XMLSchema-instance" />
    
    <phase id="upload.phase">
        <active pattern="profile.check" />
        <active pattern="reference.validity.check" />
    </phase>
    <phase id="submit.phase">
        <active pattern="profile.check" />
        <active pattern="reference.presence.check" />
        <active pattern="reference.validity.check" />
        <active pattern="components.check" />
    </phase>
    
    <let name="profile" value="/cmd:CMD/cmd:Header/cmd:MdProfile" />
    <let name="allowedProfilesDocument" value="document('/lat/tomcat-corpman/conf/cmdi_allowed_profiles.xml')" />
    <let name="profileName" value="$allowedProfilesDocument//profile[@id = normalize-space($profile)]/@name" />
    <let name="profileAllowedReferenceTypes" value="$allowedProfilesDocument//profile[@id = normalize-space($profile)]/allowedReferenceTypes" />

    <pattern id="profile.check">
        <rule context="/cmd:CMD">
            <assert role="error" test="$allowedProfilesDocument//profile[@id = normalize-space(current()/cmd:Header/cmd:MdProfile)] or $allowedProfilesDocument//profile[@id = tokenize(normalize-space(current()/@xsi:schemaLocation), '/')[last() - 1]]">
                [CMDI Archive Restriction] the CMD profile of this record is not allowed in the archive.
            </assert>
        </rule>
    </pattern>

    <pattern id="reference.presence.check">
        <rule context="/cmd:CMD/cmd:Resources/cmd:ResourceProxyList">
            <assert role="error" test="count(cmd:ResourceProxy) ge 1">
                [CMDI Best Practice] There should be at least one /cmd:CMD/cmd:Resources/cmd:ResourceProxyList/cmd:ResourceProxy.
            </assert>
        </rule>
    </pattern>

    <pattern id="reference.validity.check">
        <rule context="/cmd:CMD/cmd:Resources/cmd:ResourceProxyList/cmd:ResourceProxy">
            <assert role="error" test="not($profileName) or $profileAllowedReferenceTypes/allowedReferenceType[text() = current()/cmd:ResourceType]">
                [CMDI Profile Restriction] the CMD profile of this record doesn't allow for this resource type.
            </assert>

            <assert role="warn" test="current()/cmd:ResourceType/@mimetype" >
                [CMDI Best Practice] Mimetype not present in ResourceProxy.
            </assert>
            
            <assert role="error" test="(current()/cmd:ResourceType[not(@mimetype)]) or (current()/cmd:ResourceType != 'Metadata' and current()/cmd:ResourceType != 'Resource') or (current()/cmd:ResourceType = 'Metadata' and current()/cmd:ResourceType/@mimetype = 'text/x-cmdi+xml') or (current()/cmd:ResourceType = 'Resource' and current()/cmd:ResourceType/@mimetype != 'text/x-cmdi+xml')">
                [CMDI Invalid reference] Mimetype not consistent with ResourceProxy type. 
            </assert>

            <assert role="error" test="not($profileName) or ($profileName != 'lat-corpus' or (current()/cmd:ResourceType != 'Resource' or /cmd:CMD/cmd:Components/cmd:lat-corpus/cmd:InfoLink[@ref = current()/@id] ))">
                [CMDI Profile Restriction] 'lat-corpus' doesn't allow referencing to resources, unless they're info links.
            </assert>

            <assert role="error" test="not($profileName) or (current()/cmd:ResourceType != 'Metadata' and current()/cmd:ResourceType != 'Resource') or ($profileName != 'lat-corpus' or /cmd:CMD/cmd:Components/cmd:lat-corpus/*[@ref = current()/@id]) and ($profileName != 'lat-session' or /cmd:CMD/cmd:Components/cmd:lat-session/cmd:Resources/*[@ref = current()/@id] or /cmd:CMD/cmd:Components/cmd:lat-session/*[@ref = current()/@id])">
                [CMDI Profile Restriction] There should be a 'ref' attribute for each resource proxy ('/cmd:CMD/cmd:Components/cmd:lat-corpus/*/@ref' for 'lat-corpus' and '/cmd:CMD/cmd:Components/cmd:lat-session/cmd:Resources/*/@ref' for 'lat-session'.
            </assert>
        </rule>
    </pattern>

    <pattern id="components.check">
        <rule context="//cmd:Components">
            <assert role="warning" test="current()/*[normalize-space(cmd:Title) != '']">
                [CMDI Best Practice] /cmd:CMD/cmd:Components/*/cmd:Title shouldn't be empty.
            </assert>
            <assert role="warning" test="current()/*/cmd:descriptions[normalize-space(cmd:Description) != '']">
                [CMDI Best Practice] /cmd:CMD/cmd:Components/*/cmd:descriptions/cmd:Description shouldn't be empty.
            </assert>
        </rule>
    </pattern>
    
</schema>