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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


/**
 * <p>Java-Klasse für RolePermission complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="RolePermission">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://opcfoundation.org/UA/2011/03/UANodeSet.xsd>NodeId">
 *       &lt;attribute name="Permissions" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RolePermission", propOrder = {
    "value"
})
public class RolePermission {

    @XmlValue
    protected String value;
    @XmlAttribute(name = "Permissions")
    @XmlSchemaType(name = "unsignedInt")
    protected Long permissions;

    /**
     * Ruft den Wert der value-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * Legt den Wert der value-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Ruft den Wert der permissions-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getPermissions() {
        if (permissions == null) {
            return  0L;
        } else {
            return permissions;
        }
    }

    /**
     * Legt den Wert der permissions-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setPermissions(Long value) {
        this.permissions = value;
    }

}
