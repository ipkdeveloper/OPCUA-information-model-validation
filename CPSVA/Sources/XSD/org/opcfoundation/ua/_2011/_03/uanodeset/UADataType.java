//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.02.19 um 02:06:22 PM CET 
//


package org.opcfoundation.ua._2011._03.uanodeset;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für UADataType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="UADataType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://opcfoundation.org/UA/2011/03/UANodeSet.xsd}UAType">
 *       &lt;sequence>
 *         &lt;element name="Definition" type="{http://opcfoundation.org/UA/2011/03/UANodeSet.xsd}DataTypeDefinition" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Purpose" type="{http://opcfoundation.org/UA/2011/03/UANodeSet.xsd}DataTypePurpose" default="Normal" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UADataType", propOrder = {
    "definition"
})
public class UADataType
    extends UAType
{

    @XmlElement(name = "Definition")
    protected DataTypeDefinition definition;
    @XmlAttribute(name = "Purpose")
    protected DataTypePurpose purpose;

    /**
     * Ruft den Wert der definition-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DataTypeDefinition }
     *     
     */
    public DataTypeDefinition getDefinition() {
        return definition;
    }

    /**
     * Legt den Wert der definition-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DataTypeDefinition }
     *     
     */
    public void setDefinition(DataTypeDefinition value) {
        this.definition = value;
    }

    /**
     * Ruft den Wert der purpose-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DataTypePurpose }
     *     
     */
    public DataTypePurpose getPurpose() {
        if (purpose == null) {
            return DataTypePurpose.NORMAL;
        } else {
            return purpose;
        }
    }

    /**
     * Legt den Wert der purpose-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DataTypePurpose }
     *     
     */
    public void setPurpose(DataTypePurpose value) {
        this.purpose = value;
    }

}
