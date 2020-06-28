package edu.utsa.tagger;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.*;

/**
 * This class is an XML model corresponding to a tag in the HED hierarchy.
 * 
 * @author Lauren Jett, Rebecca Strautman, Thomas Rognon, Jeremy Cockfield, Kay
 *         Robbins
 */
@XmlType(name = "unitClass")
@XmlAccessorType(XmlAccessType.FIELD)
public class UnitClassXmlModel {
	@XmlAttribute(name = "defaultUnits")
	private String defaultUnit = "";
	private String name = ""; // name of the unit class, e.g. Time, PhysicalLength...
	private UnitsXmlModel units;

//	public void addChild(UnitClassXmlModel child) {
//		unitClass.add(child);
//	}
//
	public String getDefault() {
		return defaultUnit;
	}

	public void setDefault(String defaultUnit) {
		this.defaultUnit = defaultUnit;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<UnitXmlModel> getUnits() {
		return units.getUnits();
	}

	public void setUnits(UnitsXmlModel units) {
		this.units = units;
	}
}
