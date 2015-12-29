<?xml version="1.0" encoding="UTF-8"?>
<sch:schema
    xmlns="http://purl.oclc.org/dsdl/schematron"
    xmlns:sch="http://purl.oclc.org/dsdl/schematron"
    queryBinding="xslt2"
    schemaVersion='ISO19757-3'
    defaultPhase='submit.phase'>
    <sch:title>CMDI Profile Validation Schematron</sch:title>
    
    <sch:ns prefix="cmd" uri="http://www.clarin.eu/cmd/" />
    <sch:ns prefix="xsi" uri="http://www.w3.org/2001/XMLSchema-instance" />
    
    <phase id="submit.phase">
        <active pattern="submit-profile.check" />
        <active pattern="submit-reference.presence.check" />
        <active pattern="submit-reference.validity.check" />
        <active pattern="submit-components.check" />
    </phase>
    
    <sch:let name="profile" value="/cmd:CMD/cmd:Header/cmd:MdProfile" />
    <sch:let name="allowedProfilesDocument" value="document('/lat/tomcat-corpman/conf/cmdi_allowed_profiles.xml')" />
    <sch:let name="profileName" value="$allowedProfilesDocument//profile[@id = normalize-space($profile)]/@name" />
    <sch:let name="profileAllowedReferenceTypes" value="$allowedProfilesDocument//profile[@id = normalize-space($profile)]/allowedReferenceTypes" />

    <sch:pattern id="submit-profile.check">
        <sch:rule context="/cmd:CMD">
            <sch:assert id="submit-assert.profile.allowed" role="error" test="$allowedProfilesDocument//profile[@id = normalize-space(current()/cmd:Header/cmd:MdProfile)] or $allowedProfilesDocument//profile[@id = tokenize(normalize-space(current()/@xsi:schemaLocation), '/')[last() - 1]]">
                [CMDI Archive Restriction] the CMD profile of this record is not allowed in the archive.
            </sch:assert>
        </sch:rule>
    </sch:pattern>

    <sch:pattern id="submit-reference.presence.check">
        <sch:rule context="/cmd:CMD/cmd:Resources/cmd:ResourceProxyList">
            <sch:assert id="submit-assert.reference.present" role="error" test="count(cmd:ResourceProxy) ge 1">
                [CMDI Best Practice] There should be at least one /cmd:CMD/cmd:Resources/cmd:ResourceProxyList/cmd:ResourceProxy.
            </sch:assert>
        </sch:rule>
    </sch:pattern>

    <sch:pattern id="submit-reference.validity.check">
        <sch:rule context="/cmd:CMD/cmd:Resources/cmd:ResourceProxyList/cmd:ResourceProxy">
            <sch:assert id="submit-assert.reference.valid" role="error" test="not($profileName) or $profileAllowedReferenceTypes/allowedReferenceType[text() = current()/cmd:ResourceType]">
                [CMDI Profile Restriction] the CMD profile of this record doesn't allow for this resource type.
            </sch:assert>

            <sch:assert id="submit-assert.reference.mimetype.present" role="warn" test="current()/cmd:ResourceType/@mimetype" >
                [CMDI Best Practice] Mimetype not present in ResourceProxy.
            </sch:assert>
            
            <sch:assert id="submit-assert.reference.mimetype.valid" role="error" test="(current()/cmd:ResourceType[not(@mimetype)])
                        or (current()/cmd:ResourceType != 'Metadata' and current()/cmd:ResourceType != 'Resource')
                        or (current()/cmd:ResourceType = 'Metadata' and current()/cmd:ResourceType/@mimetype = 'text/x-cmdi+xml')
                        or (current()/cmd:ResourceType = 'Resource' and current()/cmd:ResourceType/@mimetype != 'text/x-cmdi+xml')">
                [CMDI Invalid reference] Mimetype not consistent with ResourceProxy type. 
            </sch:assert>

            <sch:assert id="submit-assert.reference.corpus.resource" role="error" test="not($profileName)
                or ($profileName != 'lat-corpus' or (current()/cmd:ResourceType != 'Resource' or /cmd:CMD/cmd:Components/cmd:lat-corpus/cmd:InfoLink[@ref = current()/@id] ))">
                [CMDI Profile Restriction] 'lat-corpus' doesn't allow referencing to resources, unless they're info links.
            </sch:assert>

            <sch:assert id="submit-assert.reference.component.present" role="error" test="not($profileName)
                        or (current()/cmd:ResourceType != 'Metadata' and current()/cmd:ResourceType != 'Resource')
                        or ($profileName != 'lat-corpus' or /cmd:CMD/cmd:Components/cmd:lat-corpus/*[@ref = current()/@id]) 
                            and ($profileName != 'lat-session' or /cmd:CMD/cmd:Components/cmd:lat-session/cmd:Resources/*[@ref = current()/@id] or /cmd:CMD/cmd:Components/cmd:lat-session/*[@ref = current()/@id])">
                [CMDI Profile Restriction] There should be a 'ref' attribute for each resource proxy ('/cmd:CMD/cmd:Components/cmd:lat-corpus/*/@ref' for 'lat-corpus' and '/cmd:CMD/cmd:Components/cmd:lat-session/cmd:Resources/*/@ref' for 'lat-session'.
            </sch:assert>
        </sch:rule>
    </sch:pattern>

    <sch:pattern id="submit-components.check">
        <sch:rule context="//cmd:Components">
            <sch:assert id="submit-assert.title.present" role="warning" test="current()/*[normalize-space(cmd:Title) != '']">
                [CMDI Best Practice] /cmd:CMD/cmd:Components/*/cmd:Title shouldn't be empty.
            </sch:assert>
            <sch:assert id="submit-assert.description.present" role="warning" test="current()/*/cmd:descriptions[normalize-space(cmd:Description) != '']">
                [CMDI Best Practice] /cmd:CMD/cmd:Components/*/cmd:descriptions/cmd:Description shouldn't be empty.
            </sch:assert>
        </sch:rule>
    </sch:pattern>
    
</sch:schema>