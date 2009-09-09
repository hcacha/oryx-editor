//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.08.07 at 02:01:49 PM CEST 
//


package de.hpi.bpmn2_0.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;


/**
 * <p>Java class for ViewDefinition complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ViewDefinition">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.com/dd/1.0.0}NamedElement">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.omg.com/dd/1.0.0}constraint" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.omg.com/dd/1.0.0}styleDefinition" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.omg.com/dd/1.0.0}childDefinition" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *       &lt;attribute name="abstract" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="superDefinition" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *       &lt;attribute name="contextType" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ViewDefinition", namespace = "http://www.omg.com/dd/1.0.0", propOrder = {
    "constraint",
    "styleDefinition",
    "childDefinition"
})
@XmlSeeAlso({
    NodeDefinition.class,
    ConnectorDefinition.class,
    DiagramDefinition.class
})
public abstract class ViewDefinition
    extends NamedElement
{

    @XmlElement(namespace = "http://www.omg.com/dd/1.0.0")
    protected List<Constraint> constraint;
    @XmlElement(namespace = "http://www.omg.com/dd/1.0.0")
    protected List<StyleDefinition> styleDefinition;
    @XmlElement(namespace = "http://www.omg.com/dd/1.0.0")
    protected List<ChildDefinition> childDefinition;
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String id;
    @XmlAttribute(name = "abstract")
    protected Boolean _abstract;
    @XmlAttribute
    protected QName superDefinition;
    @XmlAttribute
    protected QName contextType;

    /**
     * Gets the value of the constraint property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the constraint property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConstraint().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Constraint }
     * 
     * 
     */
    public List<Constraint> getConstraint() {
        if (constraint == null) {
            constraint = new ArrayList<Constraint>();
        }
        return this.constraint;
    }

    /**
     * Gets the value of the styleDefinition property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the styleDefinition property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStyleDefinition().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link StyleDefinition }
     * 
     * 
     */
    public List<StyleDefinition> getStyleDefinition() {
        if (styleDefinition == null) {
            styleDefinition = new ArrayList<StyleDefinition>();
        }
        return this.styleDefinition;
    }

    /**
     * Gets the value of the childDefinition property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the childDefinition property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getChildDefinition().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ChildDefinition }
     * 
     * 
     */
    public List<ChildDefinition> getChildDefinition() {
        if (childDefinition == null) {
            childDefinition = new ArrayList<ChildDefinition>();
        }
        return this.childDefinition;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the abstract property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAbstract() {
        return _abstract;
    }

    /**
     * Sets the value of the abstract property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAbstract(Boolean value) {
        this._abstract = value;
    }

    /**
     * Gets the value of the superDefinition property.
     * 
     * @return
     *     possible object is
     *     {@link QName }
     *     
     */
    public QName getSuperDefinition() {
        return superDefinition;
    }

    /**
     * Sets the value of the superDefinition property.
     * 
     * @param value
     *     allowed object is
     *     {@link QName }
     *     
     */
    public void setSuperDefinition(QName value) {
        this.superDefinition = value;
    }

    /**
     * Gets the value of the contextType property.
     * 
     * @return
     *     possible object is
     *     {@link QName }
     *     
     */
    public QName getContextType() {
        return contextType;
    }

    /**
     * Sets the value of the contextType property.
     * 
     * @param value
     *     allowed object is
     *     {@link QName }
     *     
     */
    public void setContextType(QName value) {
        this.contextType = value;
    }

}
