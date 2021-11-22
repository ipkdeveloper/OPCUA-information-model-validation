//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.02.19 um 02:06:22 PM CET 
//


package org.opcfoundation.ua._2011._03.uanodeset;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für ReleaseStatus.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="ReleaseStatus">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Released"/>
 *     &lt;enumeration value="Draft"/>
 *     &lt;enumeration value="Deprecated"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ReleaseStatus")
@XmlEnum
public enum ReleaseStatus {

    @XmlEnumValue("Released")
    RELEASED("Released"),
    @XmlEnumValue("Draft")
    DRAFT("Draft"),
    @XmlEnumValue("Deprecated")
    DEPRECATED("Deprecated");
    private final String value;

    ReleaseStatus(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ReleaseStatus fromValue(String v) {
        for (ReleaseStatus c: ReleaseStatus.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
