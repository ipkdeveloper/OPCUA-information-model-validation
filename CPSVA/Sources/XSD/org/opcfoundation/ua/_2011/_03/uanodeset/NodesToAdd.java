//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// �nderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.02.19 um 02:06:22 PM CET 
//


package org.opcfoundation.ua._2011._03.uanodeset;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse f�r NodesToAdd complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="NodesToAdd">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="UAObject" type="{http://opcfoundation.org/UA/2011/03/UANodeSet.xsd}UAObject"/>
 *         &lt;element name="UAVariable" type="{http://opcfoundation.org/UA/2011/03/UANodeSet.xsd}UAVariable"/>
 *         &lt;element name="UAMethod" type="{http://opcfoundation.org/UA/2011/03/UANodeSet.xsd}UAMethod"/>
 *         &lt;element name="UAView" type="{http://opcfoundation.org/UA/2011/03/UANodeSet.xsd}UAView"/>
 *         &lt;element name="UAObjectType" type="{http://opcfoundation.org/UA/2011/03/UANodeSet.xsd}UAObjectType"/>
 *         &lt;element name="UAVariableType" type="{http://opcfoundation.org/UA/2011/03/UANodeSet.xsd}UAVariableType"/>
 *         &lt;element name="UADataType" type="{http://opcfoundation.org/UA/2011/03/UANodeSet.xsd}UADataType"/>
 *         &lt;element name="UAReferenceType" type="{http://opcfoundation.org/UA/2011/03/UANodeSet.xsd}UAReferenceType"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NodesToAdd", propOrder = {
    "uaObjectOrUAVariableOrUAMethod"
})
public class NodesToAdd {

    @XmlElements({
        @XmlElement(name = "UAObject", type = UAObject.class),
        @XmlElement(name = "UAVariable", type = UAVariable.class),
        @XmlElement(name = "UAMethod", type = UAMethod.class),
        @XmlElement(name = "UAView", type = UAView.class),
        @XmlElement(name = "UAObjectType", type = UAObjectType.class),
        @XmlElement(name = "UAVariableType", type = UAVariableType.class),
        @XmlElement(name = "UADataType", type = UADataType.class),
        @XmlElement(name = "UAReferenceType", type = UAReferenceType.class)
    })
    protected List<UANode> uaObjectOrUAVariableOrUAMethod;

    /**
     * Gets the value of the uaObjectOrUAVariableOrUAMethod property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the uaObjectOrUAVariableOrUAMethod property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUAObjectOrUAVariableOrUAMethod().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link UAObject }
     * {@link UAVariable }
     * {@link UAMethod }
     * {@link UAView }
     * {@link UAObjectType }
     * {@link UAVariableType }
     * {@link UADataType }
     * {@link UAReferenceType }
     * 
     * 
     */
    public List<UANode> getUAObjectOrUAVariableOrUAMethod() {
        if (uaObjectOrUAVariableOrUAMethod == null) {
            uaObjectOrUAVariableOrUAMethod = new ArrayList<UANode>();
        }
        return this.uaObjectOrUAVariableOrUAMethod;
    }

}
