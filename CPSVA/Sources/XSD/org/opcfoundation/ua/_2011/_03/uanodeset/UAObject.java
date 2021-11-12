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
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für UAObject complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="UAObject">
 *   &lt;complexContent>
 *     &lt;extension base="{http://opcfoundation.org/UA/2011/03/UANodeSet.xsd}UAInstance">
 *       &lt;attribute name="EventNotifier" type="{http://opcfoundation.org/UA/2011/03/UANodeSet.xsd}EventNotifier" default="0" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UAObject")
public class UAObject
    extends UAInstance
{

    @XmlAttribute(name = "EventNotifier")
    protected Short eventNotifier;

    /**
     * Ruft den Wert der eventNotifier-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public short getEventNotifier() {
        if (eventNotifier == null) {
            return ((short) 0);
        } else {
            return eventNotifier;
        }
    }

    /**
     * Legt den Wert der eventNotifier-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setEventNotifier(Short value) {
        this.eventNotifier = value;
    }

}
