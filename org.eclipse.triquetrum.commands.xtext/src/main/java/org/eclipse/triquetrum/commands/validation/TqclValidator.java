/*******************************************************************************
 * Copyright (c)  2017 Totaro Rodolfo
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *    Rodolfo Totaro - initial API and implementation and/or initial documentation
 *******************************************************************************/
/*
 * generated by Xtext 2.10.0
 */
package org.eclipse.triquetrum.commands.validation;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.triquetrum.commands.api.TqCLServices;
import org.eclipse.triquetrum.commands.api.services.EntityDescriptor;
import org.eclipse.triquetrum.commands.api.services.ParameterDescriptor;
import org.eclipse.triquetrum.commands.api.services.PortDescriptor;
import org.eclipse.triquetrum.commands.api.services.TqCLLibraryException;
import org.eclipse.triquetrum.commands.api.services.TqCLLibraryProvider;
import org.eclipse.triquetrum.commands.tqcl.Category;
import org.eclipse.triquetrum.commands.tqcl.Connect;
import org.eclipse.triquetrum.commands.tqcl.ConnectionPort;
import org.eclipse.triquetrum.commands.tqcl.Insert;
import org.eclipse.triquetrum.commands.tqcl.Library;
import org.eclipse.triquetrum.commands.tqcl.Parameter;
import org.eclipse.triquetrum.commands.tqcl.TqclPackage;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.validation.Check;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

/**
 * This class contains custom validation rules.
 *
 * See
 * https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#validation
 */
public class TqclValidator extends AbstractTqclValidator {

	public static final String INVALID_ENTITY_CLASS = "invalidEntityClass";

	public static final String INVALID_LIBRARY = "invalidLibrary";

	public static final String INVALID_PARAMETER = "invalidParameter";

	public static final String DUPLICATED_ENTITY_NAME = "duplicatedEntityName";
	
	public static final String INVALID_PORT_NAME = "invalidPortName";

	/**
	 * Check that the library is supported by a {@link TqCLLibraryProvider}
	 * @param library
	 */
	@Check
	public void checkLibrary(Library library) {
		String name = library.getName();
		Set<String> libraryNames = TqCLServices.getInstance().getTqclLibraryProvider().getLibraryNames();
		if (!libraryNames.contains(name)) {
			warning("Invalid Library Name", TqclPackage.Literals.LIBRARY__NAME, INVALID_LIBRARY);
		}
	}

	/**
	 * Check that entity exists in the imported libraries
	 * @param library
	 */
	@Check
	public void checkEntityInLibrary(Insert insert) {
		String entityClass = TqCLUtils.cleanEntityClassName(insert.getEntityClass());
		List<Library> libraries = EcoreUtil2.getAllContentsOfType(EcoreUtil2.getRootContainer(insert), Library.class);
		for (Library library : libraries) {
			String libraryName = library.getName();
			TqCLLibraryProvider tqclLibraryProvider = TqCLServices.getInstance().getTqclLibraryProvider();
			if (tqclLibraryProvider.hasElementInLibrary(entityClass, libraryName, insert.getCategory().getName())) {
				return;
			}

		}
		String message = MessageFormat.format("Entity class {0} not found in imported library", entityClass);
		error(message, TqclPackage.Literals.INSERT__ENTITY_CLASS, INVALID_ENTITY_CLASS);
	}

	/**
	 * Check that the name used for model element is an unique identifier
	 * @param insert
	 */
	@Check
	public void checkNameUnique(Insert insert) {
		String name = insert.getName();
		EObject rootContainer = EcoreUtil2.getRootContainer(insert);
		TreeIterator<EObject> eAllContents = rootContainer.eAllContents();
		while (eAllContents.hasNext()) {
			EObject eObject = (EObject) eAllContents.next();
			if (eObject instanceof Insert) {
				Insert otherInsert = (Insert) eObject;
				if (otherInsert != insert) {
					if (otherInsert.getName().equals(name)) {
						error("Duplicated entity name", TqclPackage.Literals.INSERT__NAME, DUPLICATED_ENTITY_NAME);
					}
				}

			}
		}
	}

	/**
	 * Check that the ports to connect already exist in the model
	 * @param connectionPort
	 */
	@Check
	public void checkConnectionPortName(ConnectionPort connectionPort) {
		String featureName = connectionPort.eContainmentFeature().getName();
		if(featureName.equals(TqclPackage.Literals.CONNECT__FROM.getName()))
		{
			checkPortName(connectionPort,false,true);
			
		}
		if(featureName.equals(TqclPackage.Literals.CONNECT__TO.getName()))
		{
			checkPortName(connectionPort,true,false);
		}
		
	}

	private void checkPortName(ConnectionPort connectionPort,boolean input,boolean output) {
		List<PortDescriptor> actorInsertPorts = TqCLUtils.getActorInsertPorts(connectionPort.getActor(), input,
				output);
		Collection<String> portNames = Collections2.transform(actorInsertPorts,
				new Function<PortDescriptor, String>() {
					@Override
					public String apply(PortDescriptor portDescriptor) {
						return portDescriptor.getDisplayName();
					}
				});

		String portToCheck = connectionPort.getPort();
		if (!portNames.contains(portToCheck)) {
			if(input)
			{
				error("Invalid input port name", TqclPackage.Literals.CONNECTION_PORT__PORT, INVALID_PORT_NAME);
			}
			else if(output)
			{
				error("Invalid output port name", TqclPackage.Literals.CONNECTION_PORT__PORT, INVALID_PORT_NAME);
			}
			else
			{
				error("Invalidport name", TqclPackage.Literals.CONNECTION_PORT__PORT, INVALID_PORT_NAME);
			}
		}
	}

	/**
	 * Check that the used parameter exists for the inserted actor
	 * @param insert
	 */
	@Check
	public void checkEntityParameters(Insert insert) {
		TqCLLibraryProvider tqclLibraryProvider = TqCLServices.getInstance().getTqclLibraryProvider();
		EList<EObject> eContents = insert.eContainer().eContents();
		for (EObject eObject : eContents) {
			if (eObject instanceof Library) {
				Library library = (Library) eObject;
				try {
					Category category = TqCLUtils.getCategory(insert);
					String entityClass = insert.getEntityClass();
					EntityDescriptor entityDescriptor = null;
					switch (category) {
					case ACTOR:
						entityDescriptor = tqclLibraryProvider.getActor(TqCLUtils.cleanEntityName(entityClass));
						break;
					case DIRECTOR:
						entityDescriptor = tqclLibraryProvider.getDirector(TqCLUtils.cleanEntityName(entityClass));
						break;
					case PARAMETER:
						entityDescriptor = tqclLibraryProvider.getParameterType(TqCLUtils.cleanEntityName(entityClass));
						break;

					default:
						break;
					}

					if (entityDescriptor != null) {
						List<ParameterDescriptor> parameters = entityDescriptor.getParameters();
						EList<Parameter> tqclParameters = insert.getParameters();
						for (Parameter tqclParameter : tqclParameters) {
							boolean found = false;
							for (ParameterDescriptor parameterDescriptor : parameters) {
								if (parameterDescriptor.getDisplayName()
										.equals(TqCLUtils.cleanParameterName(tqclParameter.getId()))) {
									found = true;
									break;
								}
							}
							if (!found) {
								String message = MessageFormat.format("Invalid parameter name {0} for actor {1}",
										tqclParameter.getId(), entityDescriptor.getClazz());
								error(message, tqclParameter, TqclPackage.Literals.PARAMETER__ID, INVALID_PARAMETER);
							}
						}
					}
				} catch (TqCLLibraryException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

}