package edu.utsa.tagger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This class is an XML model corresponding to a tag in the HED hierarchy.
 * 
 * @author Lauren Jett, Rebecca Strautman, Thomas Rognon, Jeremy Cockfield, Kay
 *         Robbins
 */
@XmlType(propOrder = {}, name = "unitModifiers")
@XmlAccessorType(XmlAccessType.FIELD)
public class UnitModifiersXmlModel {

	private ArrayList<UnitModifierXmlModel> unitModifier = new ArrayList<UnitModifierXmlModel>();

	public void setUnitModifiers(ArrayList<UnitModifierXmlModel> unitModifier) {
		this.unitModifier = unitModifier;
	}

	public ArrayList<UnitModifierXmlModel> getUnitModifiers() {
		return unitModifier;
	}

}
