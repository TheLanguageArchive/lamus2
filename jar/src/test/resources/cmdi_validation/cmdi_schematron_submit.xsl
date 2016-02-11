<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<stylesheet version="2.0"
            xmlns="http://www.w3.org/1999/XSL/Transform"
            xmlns:cmd="http://www.clarin.eu/cmd/"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
<!--Implementers: please note that overriding process-prolog or process-root is 
    the preferred method for meta-stylesheets to use where possible. -->
<param name="archiveDirParameter" />
  <param name="archiveNameParameter" />
  <param name="fileNameParameter" />
  <param name="fileDirParameter" />
  <variable name="document-uri">
    <value-of select="document-uri(/)" />
  </variable>

<!--PHASES-->


<!--PROLOG-->
<output indent="yes" method="xml" omit-xml-declaration="no" standalone="yes" />

<!--XSD TYPES FOR XSLT2-->


<!--KEYS AND FUNCTIONS-->


<!--DEFAULT RULES-->


<!--MODE: SCHEMATRON-SELECT-FULL-PATH-->
<!--This mode can be used to generate an ugly though full XPath for locators-->
<template match="*" mode="schematron-select-full-path">
    <apply-templates mode="schematron-get-full-path" select="." />
  </template>

<!--MODE: SCHEMATRON-FULL-PATH-->
<!--This mode can be used to generate an ugly though full XPath for locators-->
<template match="*" mode="schematron-get-full-path">
    <apply-templates mode="schematron-get-full-path" select="parent::*" />
    <text>/</text>
    <choose>
      <when test="namespace-uri()=''">
        <value-of select="name()" />
      </when>
      <otherwise>
        <text>*:</text>
        <value-of select="local-name()" />
        <text>[namespace-uri()='</text>
        <value-of select="namespace-uri()" />
        <text>']</text>
      </otherwise>
    </choose>
    <variable name="preceding" select="count(preceding-sibling::*[local-name()=local-name(current())                                   and namespace-uri() = namespace-uri(current())])" />
    <text>[</text>
    <value-of select="1+ $preceding" />
    <text>]</text>
  </template>
  <template match="@*" mode="schematron-get-full-path">
    <apply-templates mode="schematron-get-full-path" select="parent::*" />
    <text>/</text>
    <choose>
      <when test="namespace-uri()=''">@<value-of select="name()" />
</when>
      <otherwise>
        <text>@*[local-name()='</text>
        <value-of select="local-name()" />
        <text>' and namespace-uri()='</text>
        <value-of select="namespace-uri()" />
        <text>']</text>
      </otherwise>
    </choose>
  </template>

<!--MODE: SCHEMATRON-FULL-PATH-2-->
<!--This mode can be used to generate prefixed XPath for humans-->
<template match="node() | @*" mode="schematron-get-full-path-2">
    <for-each select="ancestor-or-self::*">
      <text>/</text>
      <value-of select="name(.)" />
      <if test="preceding-sibling::*[name(.)=name(current())]">
        <text>[</text>
        <value-of select="count(preceding-sibling::*[name(.)=name(current())])+1" />
        <text>]</text>
      </if>
    </for-each>
    <if test="not(self::*)">
      <text />/@<value-of select="name(.)" />
    </if>
  </template>
<!--MODE: SCHEMATRON-FULL-PATH-3-->
<!--This mode can be used to generate prefixed XPath for humans 
	(Top-level element has index)-->
<template match="node() | @*" mode="schematron-get-full-path-3">
    <for-each select="ancestor-or-self::*">
      <text>/</text>
      <value-of select="name(.)" />
      <if test="parent::*">
        <text>[</text>
        <value-of select="count(preceding-sibling::*[name(.)=name(current())])+1" />
        <text>]</text>
      </if>
    </for-each>
    <if test="not(self::*)">
      <text />/@<value-of select="name(.)" />
    </if>
  </template>

<!--MODE: GENERATE-ID-FROM-PATH -->
<template match="/" mode="generate-id-from-path" />
  <template match="text()" mode="generate-id-from-path">
    <apply-templates mode="generate-id-from-path" select="parent::*" />
    <value-of select="concat('.text-', 1+count(preceding-sibling::text()), '-')" />
  </template>
  <template match="comment()" mode="generate-id-from-path">
    <apply-templates mode="generate-id-from-path" select="parent::*" />
    <value-of select="concat('.comment-', 1+count(preceding-sibling::comment()), '-')" />
  </template>
  <template match="processing-instruction()" mode="generate-id-from-path">
    <apply-templates mode="generate-id-from-path" select="parent::*" />
    <value-of select="concat('.processing-instruction-', 1+count(preceding-sibling::processing-instruction()), '-')" />
  </template>
  <template match="@*" mode="generate-id-from-path">
    <apply-templates mode="generate-id-from-path" select="parent::*" />
    <value-of select="concat('.@', name())" />
  </template>
  <template match="*" mode="generate-id-from-path" priority="-0.5">
    <apply-templates mode="generate-id-from-path" select="parent::*" />
    <text>.</text>
    <value-of select="concat('.',name(),'-',1+count(preceding-sibling::*[name()=name(current())]),'-')" />
  </template>

<!--MODE: GENERATE-ID-2 -->
<template match="/" mode="generate-id-2">U</template>
  <template match="*" mode="generate-id-2" priority="2">
    <text>U</text>
    <number count="*" level="multiple" />
  </template>
  <template match="node()" mode="generate-id-2">
    <text>U.</text>
    <number count="*" level="multiple" />
    <text>n</text>
    <number count="node()" />
  </template>
  <template match="@*" mode="generate-id-2">
    <text>U.</text>
    <number count="*" level="multiple" />
    <text>_</text>
    <value-of select="string-length(local-name(.))" />
    <text>_</text>
    <value-of select="translate(name(),':','.')" />
  </template>
<!--Strip characters-->  <template match="text()" priority="-1" />

<!--SCHEMA SETUP-->
<template match="/">
    <ns0:schematron-output schemaVersion="ISO19757-3" title="CMDI Profile Validation Schematron" xmlns:ns0="http://purl.oclc.org/dsdl/svrl">
      <attribute name="phase">submit.phase</attribute>
      <comment>
        <value-of select="$archiveDirParameter" />   
		 <value-of select="$archiveNameParameter" />  
		 <value-of select="$fileNameParameter" />  
		 <value-of select="$fileDirParameter" />
      </comment>
      <ns0:ns-prefix-in-attribute-values prefix="cmd" uri="http://www.clarin.eu/cmd/" />
      <ns0:ns-prefix-in-attribute-values prefix="xsi" uri="http://www.w3.org/2001/XMLSchema-instance" />
      <ns0:active-pattern>
        <attribute name="document">
          <value-of select="document-uri(/)" />
        </attribute>
        <attribute name="id">profile.check</attribute>
        <attribute name="name">profile.check</attribute>
        <apply-templates />
      </ns0:active-pattern>
      <apply-templates mode="M9" select="/" />
      <ns0:active-pattern>
        <attribute name="document">
          <value-of select="document-uri(/)" />
        </attribute>
        <attribute name="id">reference.presence.check</attribute>
        <attribute name="name">reference.presence.check</attribute>
        <apply-templates />
      </ns0:active-pattern>
      <apply-templates mode="M10" select="/" />
      <ns0:active-pattern>
        <attribute name="document">
          <value-of select="document-uri(/)" />
        </attribute>
        <attribute name="id">reference.validity.check</attribute>
        <attribute name="name">reference.validity.check</attribute>
        <apply-templates />
      </ns0:active-pattern>
      <apply-templates mode="M11" select="/" />
      <ns0:active-pattern>
        <attribute name="document">
          <value-of select="document-uri(/)" />
        </attribute>
        <attribute name="id">components.check</attribute>
        <attribute name="name">components.check</attribute>
        <apply-templates />
      </ns0:active-pattern>
      <apply-templates mode="M12" select="/" />
    </ns0:schematron-output>
  </template>

<!--SCHEMATRON PATTERNS-->
<ns0:text xmlns:ns0="http://purl.oclc.org/dsdl/svrl">CMDI Profile Validation Schematron</ns0:text>
  <param name="profile" select="/cmd:CMD/cmd:Header/cmd:MdProfile" />
  <param name="allowedProfilesDocument" select="document('target/test-classes/cmdi_validation/cmdi_allowed_profiles.xml')" />
  <param name="profileName" select="$allowedProfilesDocument//profile[@id = normalize-space($profile)]/@name" />
  <param name="profileAllowedReferenceTypes" select="$allowedProfilesDocument//profile[@id = normalize-space($profile)]/allowedReferenceTypes" />

<!--PATTERN profile.check-->


	<!--RULE -->
<template match="/cmd:CMD" mode="M9" priority="1000">
    <ns0:fired-rule context="/cmd:CMD" xmlns:ns0="http://purl.oclc.org/dsdl/svrl" />

		<!--ASSERT error-->
<choose>
      <when test="$allowedProfilesDocument//profile[@id = normalize-space(current()/cmd:Header/cmd:MdProfile)] or $allowedProfilesDocument//profile[@id = tokenize(normalize-space(current()/@xsi:schemaLocation), '/')[last() - 1]]" />
      <otherwise>
        <ns0:failed-assert test="$allowedProfilesDocument//profile[@id = normalize-space(current()/cmd:Header/cmd:MdProfile)] or $allowedProfilesDocument//profile[@id = tokenize(normalize-space(current()/@xsi:schemaLocation), '/')[last() - 1]]" xmlns:ns0="http://purl.oclc.org/dsdl/svrl">
          <attribute name="role">error</attribute>
          <attribute name="location">
            <apply-templates mode="schematron-select-full-path" select="." />
          </attribute>
          <ns0:text>
                [CMDI Archive Restriction] the CMD profile of this record is not allowed in the archive.
            </ns0:text>
        </ns0:failed-assert>
      </otherwise>
    </choose>
    <apply-templates mode="M9" select="*|comment()|processing-instruction()" />
  </template>
  <template match="text()" mode="M9" priority="-1" />
  <template match="@*|node()" mode="M9" priority="-2">
    <apply-templates mode="M9" select="*|comment()|processing-instruction()" />
  </template>

<!--PATTERN reference.presence.check-->


	<!--RULE -->
<template match="/cmd:CMD/cmd:Resources/cmd:ResourceProxyList" mode="M10" priority="1000">
    <ns0:fired-rule context="/cmd:CMD/cmd:Resources/cmd:ResourceProxyList" xmlns:ns0="http://purl.oclc.org/dsdl/svrl" />

		<!--ASSERT error-->
<choose>
      <when test="count(cmd:ResourceProxy) ge 1" />
      <otherwise>
        <ns0:failed-assert test="count(cmd:ResourceProxy) ge 1" xmlns:ns0="http://purl.oclc.org/dsdl/svrl">
          <attribute name="role">error</attribute>
          <attribute name="location">
            <apply-templates mode="schematron-select-full-path" select="." />
          </attribute>
          <ns0:text>
                [CMDI Best Practice] There should be at least one /cmd:CMD/cmd:Resources/cmd:ResourceProxyList/cmd:ResourceProxy.
            </ns0:text>
        </ns0:failed-assert>
      </otherwise>
    </choose>
    <apply-templates mode="M10" select="*|comment()|processing-instruction()" />
  </template>
  <template match="text()" mode="M10" priority="-1" />
  <template match="@*|node()" mode="M10" priority="-2">
    <apply-templates mode="M10" select="*|comment()|processing-instruction()" />
  </template>

<!--PATTERN reference.validity.check-->


	<!--RULE -->
<template match="/cmd:CMD/cmd:Resources/cmd:ResourceProxyList/cmd:ResourceProxy" mode="M11" priority="1000">
    <ns0:fired-rule context="/cmd:CMD/cmd:Resources/cmd:ResourceProxyList/cmd:ResourceProxy" xmlns:ns0="http://purl.oclc.org/dsdl/svrl" />

		<!--ASSERT error-->
<choose>
      <when test="not($profileName) or $profileAllowedReferenceTypes/allowedReferenceType[text() = current()/cmd:ResourceType]" />
      <otherwise>
        <ns0:failed-assert test="not($profileName) or $profileAllowedReferenceTypes/allowedReferenceType[text() = current()/cmd:ResourceType]" xmlns:ns0="http://purl.oclc.org/dsdl/svrl">
          <attribute name="role">error</attribute>
          <attribute name="location">
            <apply-templates mode="schematron-select-full-path" select="." />
          </attribute>
          <ns0:text>
                [CMDI Profile Restriction] the CMD profile of this record doesn't allow for this resource type.
            </ns0:text>
        </ns0:failed-assert>
      </otherwise>
    </choose>

		<!--ASSERT warn-->
<choose>
      <when test="current()/cmd:ResourceType/@mimetype" />
      <otherwise>
        <ns0:failed-assert test="current()/cmd:ResourceType/@mimetype" xmlns:ns0="http://purl.oclc.org/dsdl/svrl">
          <attribute name="role">warn</attribute>
          <attribute name="location">
            <apply-templates mode="schematron-select-full-path" select="." />
          </attribute>
          <ns0:text>
                [CMDI Best Practice] Mimetype not present in ResourceProxy.
            </ns0:text>
        </ns0:failed-assert>
      </otherwise>
    </choose>

		<!--ASSERT error-->
<choose>
      <when test="(current()/cmd:ResourceType[not(@mimetype)]) or (current()/cmd:ResourceType != 'Metadata' and current()/cmd:ResourceType != 'Resource') or (current()/cmd:ResourceType = 'Metadata' and current()/cmd:ResourceType/@mimetype = 'text/x-cmdi+xml') or (current()/cmd:ResourceType = 'Resource' and current()/cmd:ResourceType/@mimetype != 'text/x-cmdi+xml')" />
      <otherwise>
        <ns0:failed-assert test="(current()/cmd:ResourceType[not(@mimetype)]) or (current()/cmd:ResourceType != 'Metadata' and current()/cmd:ResourceType != 'Resource') or (current()/cmd:ResourceType = 'Metadata' and current()/cmd:ResourceType/@mimetype = 'text/x-cmdi+xml') or (current()/cmd:ResourceType = 'Resource' and current()/cmd:ResourceType/@mimetype != 'text/x-cmdi+xml')" xmlns:ns0="http://purl.oclc.org/dsdl/svrl">
          <attribute name="role">error</attribute>
          <attribute name="location">
            <apply-templates mode="schematron-select-full-path" select="." />
          </attribute>
          <ns0:text>
                [CMDI Invalid reference] Mimetype not consistent with ResourceProxy type. 
            </ns0:text>
        </ns0:failed-assert>
      </otherwise>
    </choose>

		<!--ASSERT error-->
<choose>
      <when test="not($profileName) or ($profileName != 'lat-corpus' or (current()/cmd:ResourceType != 'Resource' or /cmd:CMD/cmd:Components/cmd:lat-corpus/cmd:InfoLink[@ref = current()/@id] ))" />
      <otherwise>
        <ns0:failed-assert test="not($profileName) or ($profileName != 'lat-corpus' or (current()/cmd:ResourceType != 'Resource' or /cmd:CMD/cmd:Components/cmd:lat-corpus/cmd:InfoLink[@ref = current()/@id] ))" xmlns:ns0="http://purl.oclc.org/dsdl/svrl">
          <attribute name="role">error</attribute>
          <attribute name="location">
            <apply-templates mode="schematron-select-full-path" select="." />
          </attribute>
          <ns0:text>
                [CMDI Profile Restriction] 'lat-corpus' doesn't allow referencing to resources, unless they're info links.
            </ns0:text>
        </ns0:failed-assert>
      </otherwise>
    </choose>

		<!--ASSERT error-->
<choose>
      <when test="not($profileName) or (current()/cmd:ResourceType != 'Metadata' and current()/cmd:ResourceType != 'Resource') or ($profileName != 'lat-corpus' or /cmd:CMD/cmd:Components/cmd:lat-corpus/*[@ref = current()/@id]) and ($profileName != 'lat-session' or /cmd:CMD/cmd:Components/cmd:lat-session/cmd:Resources/*[@ref = current()/@id] or /cmd:CMD/cmd:Components/cmd:lat-session/*[@ref = current()/@id])" />
      <otherwise>
        <ns0:failed-assert test="not($profileName) or (current()/cmd:ResourceType != 'Metadata' and current()/cmd:ResourceType != 'Resource') or ($profileName != 'lat-corpus' or /cmd:CMD/cmd:Components/cmd:lat-corpus/*[@ref = current()/@id]) and ($profileName != 'lat-session' or /cmd:CMD/cmd:Components/cmd:lat-session/cmd:Resources/*[@ref = current()/@id] or /cmd:CMD/cmd:Components/cmd:lat-session/*[@ref = current()/@id])" xmlns:ns0="http://purl.oclc.org/dsdl/svrl">
          <attribute name="role">error</attribute>
          <attribute name="location">
            <apply-templates mode="schematron-select-full-path" select="." />
          </attribute>
          <ns0:text>
                [CMDI Profile Restriction] There should be a 'ref' attribute for each resource proxy ('/cmd:CMD/cmd:Components/cmd:lat-corpus/*/@ref' for 'lat-corpus' and '/cmd:CMD/cmd:Components/cmd:lat-session/cmd:Resources/*/@ref' for 'lat-session'.
            </ns0:text>
        </ns0:failed-assert>
      </otherwise>
    </choose>
    <apply-templates mode="M11" select="*|comment()|processing-instruction()" />
  </template>
  <template match="text()" mode="M11" priority="-1" />
  <template match="@*|node()" mode="M11" priority="-2">
    <apply-templates mode="M11" select="*|comment()|processing-instruction()" />
  </template>

<!--PATTERN components.check-->


	<!--RULE -->
<template match="//cmd:Components" mode="M12" priority="1000">
    <ns0:fired-rule context="//cmd:Components" xmlns:ns0="http://purl.oclc.org/dsdl/svrl" />

		<!--ASSERT warning-->
<choose>
      <when test="current()/*[normalize-space(cmd:Title) != '']" />
      <otherwise>
        <ns0:failed-assert test="current()/*[normalize-space(cmd:Title) != '']" xmlns:ns0="http://purl.oclc.org/dsdl/svrl">
          <attribute name="role">warning</attribute>
          <attribute name="location">
            <apply-templates mode="schematron-select-full-path" select="." />
          </attribute>
          <ns0:text>
                [CMDI Best Practice] /cmd:CMD/cmd:Components/*/cmd:Title shouldn't be empty.
            </ns0:text>
        </ns0:failed-assert>
      </otherwise>
    </choose>

		<!--ASSERT warning-->
<choose>
      <when test="current()/*/cmd:descriptions[normalize-space(cmd:Description) != '']" />
      <otherwise>
        <ns0:failed-assert test="current()/*/cmd:descriptions[normalize-space(cmd:Description) != '']" xmlns:ns0="http://purl.oclc.org/dsdl/svrl">
          <attribute name="role">warning</attribute>
          <attribute name="location">
            <apply-templates mode="schematron-select-full-path" select="." />
          </attribute>
          <ns0:text>
                [CMDI Best Practice] /cmd:CMD/cmd:Components/*/cmd:descriptions/cmd:Description shouldn't be empty.
            </ns0:text>
        </ns0:failed-assert>
      </otherwise>
    </choose>
    <apply-templates mode="M12" select="*|comment()|processing-instruction()" />
  </template>
  <template match="text()" mode="M12" priority="-1" />
  <template match="@*|node()" mode="M12" priority="-2">
    <apply-templates mode="M12" select="*|comment()|processing-instruction()" />
  </template>
</stylesheet>
