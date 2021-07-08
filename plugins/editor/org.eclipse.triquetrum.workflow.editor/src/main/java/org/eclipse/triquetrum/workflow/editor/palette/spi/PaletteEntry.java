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

import java.util.Collections;

import org.eclipse.triquetrum.workflow.editor.BoCategory;

public class PaletteEntry extends PaletteConfigurationElement {

	public PaletteEntry(String entryName, BoCategory category, Class<?> actorClass) {
		super("entry", "org.eclipse.triquetrum.workflow.editor", Collections.singletonMap(DISPLAY_NAME, entryName));
		putAttribute(TYPE, category.name());
		putAttribute(CLASS, actorClass.getName());
	}

}
