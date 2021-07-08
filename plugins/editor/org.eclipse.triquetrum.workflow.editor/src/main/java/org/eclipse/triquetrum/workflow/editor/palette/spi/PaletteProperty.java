/*******************************************************************************
 * Copyright (c) 2020 Christoph Läubrich
 *  
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Christoph Läubrich - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.triquetrum.workflow.editor.palette.spi;

import java.util.HashMap;
import java.util.Map;

public class PaletteProperty extends PaletteConfigurationElement {

	public PaletteProperty(String name, String value) {
		super(PROPERTY, "org.eclipse.triquetrum.workflow.editor", createProperty(name, value));
	}

	private static Map<String, String> createProperty(String name, String value) {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("name", name);
		map.put("value", value);
		return map;
	}

}
