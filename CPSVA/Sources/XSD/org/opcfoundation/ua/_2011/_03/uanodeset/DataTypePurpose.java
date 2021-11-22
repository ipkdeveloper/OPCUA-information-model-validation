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
 * <p>Java-Klasse für DataTypePurpose.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="DataTypePurpose">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Normal"/>
 *     &lt;enumeration value="ServicesOnly"/>
 *     &lt;enumeration value="CodeGenerator"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "DataTypePurpose")
@XmlEnum
public enum DataTypePurpose {

    @XmlEnumValue("Normal")
    NORMAL("Normal"),
    @XmlEnumValue("ServicesOnly")
    SERVICES_ONLY("ServicesOnly"),
    @XmlEnumValue("CodeGenerator")
    CODE_GENERATOR("CodeGenerator");
    private final String value;

    DataTypePurpose(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DataTypePurpose fromValue(String v) {
        for (DataTypePurpose c: DataTypePurpose.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
