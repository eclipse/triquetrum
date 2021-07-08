/*******************************************************************************
 * Copyright (c) 2016,2020 iSencia Belgium NV.
 *  
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Erwin De Ley - initial API and implementation and/or initial documentation
 *     Christoph Läubrich - #350
 *******************************************************************************/
package org.eclipse.triquetrum.workflow.editor.features;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICreateContext;
import org.eclipse.graphiti.features.impl.AbstractCreateFeature;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.triquetrum.ErrorCode;
import org.eclipse.triquetrum.workflow.editor.BoCategory;
import org.eclipse.triquetrum.workflow.editor.ImageConstants;
import org.eclipse.triquetrum.workflow.editor.TriqFeatureProvider;
import org.eclipse.triquetrum.workflow.editor.util.EditorUtils;
import org.eclipse.triquetrum.workflow.model.Attribute;
import org.eclipse.triquetrum.workflow.model.CompositeActor;
import org.eclipse.triquetrum.workflow.model.Director;
import org.eclipse.triquetrum.workflow.model.Entity;
import org.eclipse.triquetrum.workflow.model.NamedObj;
import org.eclipse.triquetrum.workflow.model.Port;
import org.eclipse.triquetrum.workflow.model.Relation;
import org.eclipse.triquetrum.workflow.model.TriqFactory;
import org.eclipse.triquetrum.workflow.model.TriqPackage;
import org.eclipse.triquetrum.workflow.model.Vertex;
import org.eclipse.triquetrum.workflow.model.util.PtObjectBuilderAndApplierVisitor;

/**
 * Creates a new model element based on a drag-n-drop from the palette, after prompting the user for the name, or via an import of an existing Ptolemy II model.
 * <p>
 * An import starts from a known and configured Ptolemy object, so the Triq Diagram model element must wrap that given object. On the other hand, a
 * create-from-the-palette only knows about the type of the wrapped Ptolemy object and about some default metadata, but is free to construct a default new
 * instance of its wrapped Ptolemy object that will get its configuration info later.
 * </p>
 *
 */
public class ModelElementCreateFeature extends AbstractCreateFeature {

  private String group;

  private BoCategory category;
  private String elementName;
  private String wrappedClass;
  private String iconResource = ImageConstants.IMG_ACTOR;
  private String iconType = TriqFeatureProvider.ICONTYPE_IMG;
  private Map<String, String> properties = new HashMap<>();

  private ptolemy.kernel.util.NamedObj wrappedObject;

  public ModelElementCreateFeature(IFeatureProvider fp, String group, BoCategory category, String elementName) {
    super(fp, elementName, "Create a " + elementName);
    this.group = group;
    this.category = category;
    this.elementName = elementName;
  }

  public ModelElementCreateFeature(IFeatureProvider fp, String group, BoCategory category, String elementName, String wrappedClass, String iconResource,
      String iconType, Map<String, String> properties) {
    super(fp, elementName, "Create a " + elementName);
    this.group = group;
    this.category = category;
    this.elementName = elementName;
    this.wrappedClass = wrappedClass;
    this.iconResource = iconResource != null ? iconResource : this.iconResource;
    this.iconType = iconType != null ? iconType : this.iconType;
    if (properties != null) {
      this.properties.putAll(properties);
    }
  }

  public BoCategory getCategory() {
    return category;
  }

  public String getWrappedClass() {
    return wrappedClass;
  }

  public String getElementName() {
    return elementName;
  }

  /**
   * This can be used e.g. to determine the compartment in which to show this feature's creation tool in the palette.
   *
   * @return the group to which this feature belongs.
   */
  public String getGroup() {
    return group;
  }

  @Override
  public String getCreateImageId() {
    return TriqFeatureProvider.ICONTYPE_IMG.equalsIgnoreCase(iconType) ? iconResource : TriqFeatureProvider.DEFAULT_ACTOR_IMG;
  }

  public ptolemy.kernel.util.NamedObj getWrappedObject() {
    return wrappedObject;
  }

  public void setWrappedObject(ptolemy.kernel.util.NamedObj wrappedObject) {
    this.wrappedObject = wrappedObject;
  }

  @Override
  public boolean canCreate(ICreateContext context) {
    if (context.getTargetContainer() instanceof Diagram) {
      // make sure we can only set 1 Director per model level
      EClassifier eClassifier = TriqPackage.eINSTANCE.getEClassifier(category.name());
      if (TriqPackage.DIRECTOR == eClassifier.getClassifierID()) {
        CompositeActor model = EditorUtils.getSelectedModel();
        return model == null || model.getDirector() == null;
      }
      return true;
    } else {
      Object parentObject = getBusinessObjectForPictogramElement(context.getTargetContainer());
      return (parentObject instanceof CompositeActor);
    }
  }

  @Override
  public Object[] create(ICreateContext context) {
    try {
      CompositeActor model = (CompositeActor) getBusinessObjectForPictogramElement(context.getTargetContainer());
      if (model == null) {
        // this should only happen for newly created Diagrams
        if (context.getTargetContainer() == getDiagram()) {
          model = TriqFactory.eINSTANCE.createCompositeActor();
          model.setName(getDiagram().getName());
          // we need to force the construction of the root Ptolemy II toplevel here,
          // otherwise the addition of the new model element can not use ptolemy features
          // like determining unique names etc.
          model.buildWrappedObject();
          getDiagram().eResource().getContents().add(model);
          link(getDiagram(), model);
        } else {
          throw new RuntimeException(ErrorCode.ERROR + " - Inconsistent model : submodel null for shape " + context.getTargetContainer());
        }
      }

      // create new model element
      EClassifier eClassifier = TriqPackage.eINSTANCE.getEClassifier(category.name());
      NamedObj result = (NamedObj) TriqFactory.eINSTANCE.create((EClass) eClassifier);
      if (wrappedObject == null) {
        result.setName(EditorUtils.buildUniqueName(model, elementName));
        result.setWrappedType(wrappedClass);
        // TODO review if we can live with a palette with properties without a class specification
      } else {
        result.setWrappedObject(wrappedObject);
      }
			for (Map.Entry<String, String> attrEntry : properties.entrySet()) {
				String name = attrEntry.getKey();
				String value = attrEntry.getValue();
				result.setProperty(name, value, null);
			}

      if (result instanceof Director) {
        model.setDirector((Director) result);
      } else if (result instanceof Entity) {
        model.getEntities().add((Entity) result);
      } else if (result instanceof Vertex) {
        Relation relation = (Relation) context.getProperty(FeatureConstants.CONTAINER);
        if (relation == null) {
          relation = TriqFactory.eINSTANCE.createRelation();
          relation.setName(EditorUtils.buildUniqueName(model, "_R"));
          model.getRelations().add(relation);
          relation.welcome(new PtObjectBuilderAndApplierVisitor(), true);
        }
        relation.getAttributes().add((Vertex) result);
      } else if (result instanceof Attribute) {
        model.getAttributes().add((Attribute) result);
      } else if (result instanceof Port) {
        Port p = (Port) result;
        model.getPorts().add(p);
      }

      result.welcome(new PtObjectBuilderAndApplierVisitor(), true);

      // set the icon in the create context, so the add feature can add it in the element's shape
      context.putProperty("icon", iconResource);
      context.putProperty("iconType", iconType);
      // do the add
      addGraphicalRepresentation(context, result);
      // activate direct editing after object creation
      getFeatureProvider().getDirectEditingInfo().setActive(true);
      // return newly created business object(s)
      return new Object[] { result };
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
