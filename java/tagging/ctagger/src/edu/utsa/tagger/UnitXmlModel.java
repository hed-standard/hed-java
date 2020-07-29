package edu.utsa.tagger;

import javax.xml.bind.annotation.*;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This class is an XML model corresponding to a tag in the HED hierarchy.
 * 
 * @author Lauren Jett, Rebecca Strautman, Thomas Rognon, Jeremy Cockfield, Kay
 *         Robbins
 */
@XmlType(propOrder = {}, name = "unit")
@XmlAccessorType(XmlAccessType.FIELD)
public class UnitXmlModel {
    @XmlAttribute(name="SIUnit")
	private boolean sIUnit = false;
    @XmlAttribute
	private boolean unitSymbol = false;
    @XmlValue
	private String name; // name of the unit class, e.g. Time, PhysicalLength...

    public UnitXmlModel() {

	}
	public boolean isSIUnit() {
		return sIUnit;
	}
	public boolean isUnitSymbol() {
		return unitSymbol;
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	public void setsIUnit(boolean si) {this.sIUnit = si;}
	public void setUnitSymbol(boolean unitSymbol) {this.unitSymbol = unitSymbol;}

}
