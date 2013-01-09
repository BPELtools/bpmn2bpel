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

  <!-- Do adjustments for the condition elements -->
  <!-- documentation element: correct expression
       evaluatesToTypeRef attribute: indicator for formal expression -->

  <xsl:template match="bpmn:condition[not(bpmn:documentation) and not(@evaluatesToTypeRef)]">
    <xsl:element name="bpmn:condition">
      <bpmn:documentation>
        <xsl:apply-templates />
      </bpmn:documentation>
    </xsl:element>
  </xsl:template>

  <xsl:template match="bpmn:loopCondition[not(bpmn:documentation) and not(@evaluatesToTypeRef)]">
    <xsl:element name="bpmn:loopCondition">
      <bpmn:documentation>
        <xsl:apply-templates />
      </bpmn:documentation>
    </xsl:element>
  </xsl:template>
  
  <xsl:template match="bpmn:completionCondition[not(bpmn:documentation) and not(@evaluatesToTypeRef)]">
    <xsl:element name="bpmn:completionCondition">
      <bpmn:documentation>
        <xsl:apply-templates />
      </bpmn:documentation>
    </xsl:element>
  </xsl:template>

</xsl:stylesheet> 