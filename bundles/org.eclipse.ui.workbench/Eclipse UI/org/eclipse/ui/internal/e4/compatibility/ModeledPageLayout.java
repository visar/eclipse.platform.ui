/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MPartDescriptor;
import org.eclipse.e4.ui.model.application.MPartSashContainer;
import org.eclipse.e4.ui.model.application.MPartStack;
import org.eclipse.e4.ui.model.application.MPerspective;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPlaceholderFolderLayout;
import org.eclipse.ui.IViewLayout;

public class ModeledPageLayout implements IPageLayout {

	private MApplication application;
	private MPerspective perspModel;
	private IPerspectiveDescriptor descriptor;
	private ArrayList newWizardShortcuts = new ArrayList();
	private ArrayList perspectiveShortcut = new ArrayList();
	private ArrayList showInPart = new ArrayList();
	private ArrayList showViewShortcut = new ArrayList();
	private ArrayList actionSet = new ArrayList();

	public ModeledPageLayout(MApplication application, MPerspective perspModel,
			IPerspectiveDescriptor descriptor) {
		this.application = application;
		// Create the editor area stack
		this.perspModel = perspModel;
		this.descriptor = descriptor;

		MPartSashContainer esc = MApplicationFactory.eINSTANCE.createPartSashContainer();
		MPartStack editorArea = MApplicationFactory.eINSTANCE.createPartStack();
		esc.getChildren().add(editorArea);
		editorArea.setId(getEditorArea());
		esc.setId(getEditorArea());

		// editorArea.setName("Editor Area");

		// perspModel.getChildren().add(esc);
	}

	public MPerspective getModel() {
		return perspModel;
	}

	public void addActionSet(String actionSetId) {
		actionSet.add(actionSetId);
	}

	public void addFastView(String viewId) {
	}

	public void addFastView(String viewId, float ratio) {
	}

	public void addNewWizardShortcut(String id) {
		newWizardShortcuts.add(id);
	}

	public void addPerspectiveShortcut(String id) {
		perspectiveShortcut.add(id);
	}

	public void addPlaceholder(String viewId, int relationship, float ratio,
			String refId) {
		insertView(viewId, relationship, ratio, refId, false, true);
	}

	public void addShowInPart(String id) {
		showInPart.add(id);
	}

	public void addShowViewShortcut(String id) {
		showViewShortcut.add(id);
	}

	public void addStandaloneView(String viewId, boolean showTitle,
			int relationship, float ratio, String refId) {
		MPart viewModel = insertView(viewId, relationship, ratio, refId, true,
				false);

		// Set the state
		if (viewModel != null) {
			// viewModel.setShowTitle(showTitle);
		}
	}

	public void addStandaloneViewPlaceholder(String viewId, int relationship,
			float ratio, String refId, boolean showTitle) {
		MPart viewModel = insertView(viewId, relationship, ratio, refId, false,
				false);

		// Set the state
		if (viewModel != null) {
			// viewModel.setShowTitle(showTitle);
		}
	}

	public void addView(String viewId, int relationship, float ratio,
			String refId) {
		insertView(viewId, relationship, ratio, refId, true, true);
	}

	public IFolderLayout createFolder(String folderId, int relationship,
			float ratio, String refId) {
		MPartStack stack = insertStack(folderId, relationship, ratio, refId,
				true);
		return new ModeledFolderLayout(application, stack);
	}

	public IPlaceholderFolderLayout createPlaceholderFolder(String folderId,
			int relationship, float ratio, String refId) {
		MPartStack Stack = insertStack(folderId, relationship, ratio, refId,
				false);
		return new ModeledPlaceholderFolderLayout(application, Stack);
	}

	public IPerspectiveDescriptor getDescriptor() {
		return descriptor;
	}

	public static String internalGetEditorArea() {
		return IPageLayout.ID_EDITOR_AREA;
	}

	public String getEditorArea() {
		return internalGetEditorArea();
	}

	public int getEditorReuseThreshold() {
		return 0;
	}

	public IPlaceholderFolderLayout getFolderForView(String id) {
		MPart view = findPart(perspModel, id);
		if (view == null)
			return null;

		MUIElement stack = view.getParent();
		if (stack == null || !(stack instanceof MPartStack))
			return null;

		return new ModeledPlaceholderFolderLayout(application, (MPartStack) stack);
	}

	public IViewLayout getViewLayout(String id) {
		MPart view = findPart(perspModel, id);
		if (view == null || !(view instanceof MPart))
			return null;

		return new ModeledViewLayout((MPart) view);
	}

	public boolean isEditorAreaVisible() {
		return true;
	}

	public boolean isFixed() {
		return false;
	}

	public void setEditorAreaVisible(boolean showEditorArea) {
	}

	public void setEditorReuseThreshold(int openEditors) {
	}

	public void setFixed(boolean isFixed) {
		// perspModel.setFixed(isFixed);
	}

	private static int plRelToSwt(int rel) {
		switch (rel) {
		case IPageLayout.BOTTOM:
			return SWT.BOTTOM;
		case IPageLayout.LEFT:
			return SWT.LEFT;
		case IPageLayout.RIGHT:
			return SWT.RIGHT;
		case IPageLayout.TOP:
			return SWT.TOP;
		default:
			return 0;
		}
	}

	public static MPart createViewModel(MApplication application, String id, boolean visible) {
		for (MPartDescriptor descriptor : application.getDescriptors()) {
			if (descriptor.getId().equals(id)) {
				MPart part = (MPart) EcoreUtil.copy((EObject) descriptor);
				part.setVisible(visible);
				return part;
			}
		}

		throw new RuntimeException("Unknown id: " + id); //$NON-NLS-1$
		// MPart viewModel = MApplicationFactory.eINSTANCE.createPart();
		//
		// // HACK!! allow Contributed parts in a perspective
		//		if (id.indexOf("platform:") >= 0) { //$NON-NLS-1$
		// viewModel.setURI(id);
		//			viewModel.setLabel("Contrib View"); //$NON-NLS-1$
		// return viewModel;
		// }
		//
		// viewModel
		// .setURI("platform:/plugin/org.eclipse.ui.workbench/org.eclipse.ui.internal.e4.compatibility.CompatibilityView");
		// viewModel.setId(id);
		//
		// // Get the actual view name from the extension registry
		// IConfigurationElement[] views = ExtensionUtils
		// .getExtensions(IWorkbenchRegistryConstants.PL_VIEWS);
		// IConfigurationElement viewContribution =
		// ExtensionUtils.findExtension(
		// views, id);
		// if (viewContribution != null) {
		//			viewModel.setLabel(viewContribution.getAttribute("name")); //$NON-NLS-1$
		//
		// // Convert the relative path into a bundle URI
		//			String imagePath = viewContribution.getAttribute("icon"); //$NON-NLS-1$
		// if (imagePath != null) {
		//				imagePath = imagePath.replace("$nl$", ""); //$NON-NLS-1$//$NON-NLS-2$
		// if (imagePath.charAt(0) != '/') {
		// imagePath = '/' + imagePath;
		// }
		// String bundleId = viewContribution.getContributor().getName();
		//				String imageURI = "platform:/plugin/" + bundleId + imagePath; //$NON-NLS-1$
		// viewModel.setIconURI(imageURI);
		// }
		// } else
		// viewModel.setLabel(id); // No registered view, create error part?
		//
		// // sets its visibility (false == placeholder)
		// viewModel.setVisible(visible);
		//
		// return viewModel;
	}

	public static MPartStack createStack(String id, boolean visible) {
		MPartStack newStack = MApplicationFactory.eINSTANCE.createPartStack();
		newStack.setId(id);
		newStack.setVisible(visible);
		return newStack;
	}

	private MPart insertView(String viewId, int relationship, float ratio,
			String refId, boolean visible, boolean withStack) {
		MUIElement refModel = findPart(perspModel, refId);
		if (refModel == null || !(refModel instanceof MPart))
			return null;

		MPart viewModel = createViewModel(application, viewId, visible);

		if (withStack) {
			String stackId = viewId + "MStack"; // Default id...basically unusable //$NON-NLS-1$
			MPartStack stack = insertStack(stackId, relationship, ratio, refId,
					visible);
			stack.getChildren().add(viewModel);
		} else {
			insert(viewModel, refModel, plRelToSwt(relationship), ratio);
		}

		return viewModel;
	}

	private MPartStack insertStack(String stackId, int relationship,
			float ratio, String refId, boolean visible) {
		MUIElement refModel = findPart(perspModel, refId);
		if (refModel == null || !(refModel instanceof MPart))
			return null;

		// If the 'refModel' is -not- a stack then find one
		// This covers cases where the defining layout is adding
		// Views relative to other views and relying on the stacks
		// being automatically created.
		if (!(refModel instanceof MPartStack)) {
			while (refModel.getParent() != null) {
				refModel = refModel.getParent();
				if (refModel instanceof MPartStack)
					break;
			}
			if (!(refModel instanceof MPartStack))
				return null;
		}

		MPartStack stack = createStack(stackId, visible);
		insert(stack, (MPart) refModel, plRelToSwt(relationship), ratio);

		return stack;
	}

	public static void replace(MUIElement relTo,
			MElementContainer<MUIElement> newParent) {
		if (relTo == null || newParent == null)
			return;

		MElementContainer<MUIElement> parent = relTo.getParent();
		if (parent == null)
			return;

		List kids = parent.getChildren();
		if (kids == null)
			return;

		kids.add(kids.indexOf(relTo), newParent);
		kids.remove(relTo);
	}

	public static void insertParent(MElementContainer<MUIElement> newParent,
			MUIElement relTo) {
		if (newParent == null || relTo == null)
			return;

		MPart curParent = (MPart) relTo.getParent();
		if (curParent != null) {
			replace(relTo, newParent);
		}

		// Move the child under the new parent
		newParent.getChildren().add(relTo);
	}

	public static void insert(MUIElement toInsert, MUIElement relTo,
			int swtSide, int ratio) {
		if (toInsert == null || relTo == null)
			return;

		MElementContainer<MUIElement> relParent = relTo.getParent();

		boolean isStack = true;

		// Create the new sash if we're going to need one
		MPartSashContainer newSash = null;
		if ((swtSide == SWT.TOP || swtSide == SWT.BOTTOM) && !isStack) {
			newSash = MApplicationFactory.eINSTANCE.createPartSashContainer();
			String label = "Vertical Sash[" + toInsert.getId() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
			newSash.setId(label);
			newSash.setHorizontal(false);
		} else if ((swtSide == SWT.LEFT || swtSide == SWT.RIGHT) && !isStack) {
			newSash = MApplicationFactory.eINSTANCE.createPartSashContainer();
			String label = "Horizontal Sash[" + toInsert.getId() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
			newSash.setId(label);
			newSash.setHorizontal(true);
		}

		List parts;
		if (newSash == null && relParent != null) {
			parts = relParent.getChildren();
		} else {
			MUIElement vscElement = newSash;
			MElementContainer<MUIElement> container = (MElementContainer<MUIElement>) vscElement;
			insertParent(container, relTo);
			parts = newSash.getChildren();

			// List<Integer> weights = newSash.getWeights();
			// weights.add(ratio);
			// weights.add(100 - ratio);
		}

		// Insert the part in the correct location
		int index = parts.indexOf(relTo);
		if (swtSide == SWT.BOTTOM || swtSide == SWT.RIGHT) {
			index++;

		}

		parts.add(index, toInsert);
	}

	public static void insert(MUIElement toInsert, MUIElement relTo,
			int swtSide, float ratio) {
		int pct = (int) (ratio * 100);
		insert(toInsert, relTo, swtSide, pct);
	}

	public static MUIElement findElementById(MUIElement element, String id) {
		if (id == null || id.length() == 0)
			return null;

		// is it me?
		if (id.equals(element.getId()))
			return element;

		// Recurse if this is a container
		if (element instanceof MElementContainer) {
			EList<MUIElement> children = ((MElementContainer<MUIElement>) element)
					.getChildren();
			MUIElement foundElement = null;
			for (MUIElement childME : children) {
				foundElement = findElementById(childME, id);
				if (foundElement != null)
					return foundElement;
			}
		}

		return null;
	}

	public static MPart findPart(MUIElement toSearch, String id) {
		if (toSearch == null)
			return null;

		MUIElement found = findElementById(toSearch, id);
		if (found instanceof MPart)
			return (MPart) found;

		return null;
	}

	/**
	 * @return
	 */
	public ArrayList getNewWizardShortcuts() {
		return newWizardShortcuts;
	}

	/**
	 * @return
	 */
	public ArrayList getShowViewShortcuts() {
		return showViewShortcut;
	}

	/**
	 * @return
	 */
	public ArrayList getPerspectiveShortcuts() {
		return perspectiveShortcut;
	}

	/**
	 * @return
	 */
	public ArrayList getShowInPartIds() {
		return showInPart;
	}
}