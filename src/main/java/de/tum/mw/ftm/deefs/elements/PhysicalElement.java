package de.tum.mw.ftm.deefs.elements;

import de.tum.mw.ftm.deefs.Scenario;
import de.tum.mw.ftm.deefs.location.Position;

import java.util.Observable;

/**
 * Base Element for all physical elements in the behavior model.
 * implements observe functions for child-classes.
 *
 * @author Michael Wittmann
 * @see Observable
 */
public class PhysicalElement extends Observable {

	protected int id;                // Element Id
	protected Position position;    // Elements Position
	protected Scenario scenario;    // Scenario the element is placed in


	/**
	 * Basic constructor for PhysicalElement. The element's Position will be set to [0,0,0] by default.
	 *
	 * @param id       Element id. Ensure that unique ids are used. Only positive numbers unlike 0 are allowed.
	 * @param scenario
	 */
	public PhysicalElement(int id, Scenario scenario) {
		if (id <= 0) throw new RuntimeException("Id must have a vaule greater than 0");
		this.id = id;
		position = new Position(0, 0, 0);
		this.scenario = scenario;
	}


	/**
	 * Extended Constructor for PhysicalElement
	 *
	 * @param id       Element id. Ensure that unique ids are use. Only positive numbers unlike 0 are allowed.
	 * @param scenario
	 * @param position The Element's position.
	 */
	public PhysicalElement(int id, Scenario scenario, Position position) {
		this(id, scenario);
		this.position = position;
	}

	/**
	 * @return the id of this element
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the position of this element
	 */
	public Position getPosition() {
		return position;
	}

	/**
	 * Sets the position of this element
	 *
	 * @param position position of this element
	 */
	protected void setPosition(Position position) {
		this.position = position;
	}

}
