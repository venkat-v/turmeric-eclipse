/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.eclipse.registry.consumer.models;

/**
 * Assertions Severity Model.
 *
 * @author ramurthy
 */

public enum AssertionsSeverityModel {

	 /** The MUST. */
 	MUST,
	 
 	/** The SHOULD. */
 	SHOULD,
	 
 	/** The MAY. */
 	MAY;

	 /**
 	 * Value.
 	 *
 	 * @return the string
 	 */
 	public String value() {
		 return name();
	 }
}
