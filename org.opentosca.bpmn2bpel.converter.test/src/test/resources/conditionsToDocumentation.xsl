<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
  version="1.0">

  <xsl:output method="xml" />
  
  <!-- Copy everything -->
  <xsl:template match="*">
    <xsl:copy>
      <xsl:copy-of select="@*" />
      <xsl:apply-templates />
    </xsl:copy>
  </xsl:template>

  <!-- Do adjustments for the loop condition -->
  <!--  TODO: check for FormalExpression and Documentation Element. If one of that exists, do not match -->
  <xsl:template match="bpmn:loopCondition">
    <xsl:element name="bpmn:loopCondition">
      <bpmn:documentation>
        <xsl:apply-templates />
      </bpmn:documentation>
    </xsl:element>
  </xsl:template>

</xsl:stylesheet> 