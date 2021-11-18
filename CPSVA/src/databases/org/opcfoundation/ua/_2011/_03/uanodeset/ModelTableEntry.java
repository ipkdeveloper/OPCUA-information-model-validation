//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.02.19 um 02:06:22 PM CET 
//


package databases.org.opcfoundation.ua._2011._03.uanodeset;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java-Klasse für ModelTableEntry complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ModelTableEntry">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="RolePermissions" type="{http://opcfoundation.org/UA/2011/03/UANodeSet.xsd}ListOfRolePermissions" minOccurs="0"/>
 *         &lt;element name="RequiredModel" type="{http://opcfoundation.org/UA/2011/03/UANodeSet.xsd}ModelTableEntry" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="ModelUri" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="Version" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="PublicationDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="AccessRestrictions" type="{http://opcfoundation.org/UA/2011/03/UANodeSet.xsd}AccessRestriction" default="0" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ModelTableEntry", propOrder = {
    "rolePermissions",
    "requiredModel"
})
public class ModelTableEntry {

    @XmlElement(name = "RolePermissions")
    protected ListOfRolePermissions rolePermissions;
    @XmlElement(name = "RequiredModel")
    protected List<ModelTableEntry> requiredModel;
    @XmlAttribute(name = "ModelUri", required = true)
    protected String modelUri;
    @XmlAttribute(name = "Version")
    protected String version;
    @XmlAttribute(name = "PublicationDate")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar publicationDate;
    @XmlAttribute(name = "AccessRestrictions")
    protected Short accessRestrictions;

    /**
     * Ruft den Wert der rolePermissions-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ListOfRolePermissions }
     *     
     */
    public ListOfRolePermissions getRolePermissions() {
        return rolePermissions;
    }

    /**
     * Legt den Wert der rolePermissions-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ListOfRolePermissions }
     *     
     */
    public void setRolePermissions(ListOfRolePermissions value) {
        this.rolePermissions = value;
    }

    /**
     * Gets the value of the requiredModel property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the requiredModel property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRequiredModel().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ModelTableEntry }
     * 
     * 
     */
    public List<ModelTableEntry> getRequiredModel() {
        if (requiredModel == null) {
            requiredModel = new ArrayList<ModelTableEntry>();
        }
        return this.requiredModel;
    }

    /**
     * Ruft den Wert der modelUri-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getModelUri() {
        return modelUri;
    }

    /**
     * Legt den Wert der modelUri-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setModelUri(String value) {
        this.modelUri = value;
    }

    /**
     * Ruft den Wert der version-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Legt den Wert der version-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Ruft den Wert der publicationDate-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getPublicationDate() {
        return publicationDate;
    }

    /**
     * Legt den Wert der publicationDate-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setPublicationDate(XMLGregorianCalendar value) {
        this.publicationDate = value;
    }

    /**
     * Ruft den Wert der accessRestrictions-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public short getAccessRestrictions() {
        if (accessRestrictions == null) {
            return ((short) 0);
        } else {
            return accessRestrictions;
        }
    }

    /**
     * Legt den Wert der accessRestrictions-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setAccessRestrictions(Short value) {
        this.accessRestrictions = value;
    }

}
