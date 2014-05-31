/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Charles Bailey <cbailey32@bloomberg.net>,
 *     Henry Kleynhans <hkleynhans@bloomberg.net> - Bug 264072
 *******************************************************************************/
package org.eclipse.ui.internal.ide.actions;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.internal.handlers.RestartWorkbenchHandler;
import org.eclipse.ui.internal.ide.ChooseWorkspaceData;
import org.eclipse.ui.internal.ide.ChooseWorkspaceDialog;
import org.eclipse.ui.internal.ide.ChooseWorkspaceWithSettingsDialog;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;

/**
 * Implements the open workspace action. Opens a dialog prompting for a
 * directory and then restarts the IDE on that workspace.
 * 
 * @since 3.0
 */
public class OpenWorkspaceAction extends Action implements
		ActionFactory.IWorkbenchAction {

	/**
	 * Action responsible for opening the "Other..." dialog (ie: the workspace
	 * chooser).
	 * 
	 * @since 3.3
	 * 
	 */
	class OpenDialogAction extends Action {

		OpenDialogAction() {
			super(IDEWorkbenchMessages.OpenWorkspaceAction_other);
			setToolTipText(IDEWorkbenchMessages.OpenWorkspaceAction_toolTip);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.action.Action#run()
		 */
		public void run() {
			OpenWorkspaceAction.this.run();
		}
	}

	/**
	 * Action responsible for opening a specific workspace location
	 * 
	 * @since 3.3
	 */
	class WorkspaceMRUAction extends Action {

		private ChooseWorkspaceData data;

		private String location;

		WorkspaceMRUAction(String location, ChooseWorkspaceData data) {
			this.location = location; // preserve the location directly -
			// setText mucks with accelerators so we
			// can't necessarily use it safely for
			// manipulating the location later.
			setText(location);
			setToolTipText(location);
			this.data = data;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.action.Action#run()
		 */
		public void run() {
			data.workspaceSelected(location);
			data.writePersistedData();
			restart(location);
		}
	}

	private static final String PROP_VM = "eclipse.vm"; //$NON-NLS-1$

	private IWorkbenchWindow window;
	
	
	private IContributionItem[] getContributionItems() {
		ArrayList list = new ArrayList();
		final ChooseWorkspaceData data = new ChooseWorkspaceData(Platform
				.getInstanceLocation().getURL());
		data.readPersistedData();
		String current = data.getInitialDefault();
		String[] workspaces = data.getRecentWorkspaces();
		for (int i = 0; i < workspaces.length; i++) {
			if (workspaces[i] != null && !workspaces[i].equals(current)) {
				list.add(new ActionContributionItem(new WorkspaceMRUAction(
						workspaces[i], data)));
			}
		}
		if (list.size()>0) {
			list.add(new Separator());
		}
		return (IContributionItem[]) list
				.toArray(new IContributionItem[list.size()]);
	}
	
	class MenuCreator implements IMenuCreator {
		ArrayList menus = new ArrayList();

		private MenuManager dropDownMenuMgr;

		/**
		 * Creates the menu manager for the drop-down.
		 */
		private void createDropDownMenuMgr() {
			if (dropDownMenuMgr == null) {
				dropDownMenuMgr = new MenuManager();
				dropDownMenuMgr.setRemoveAllWhenShown(true);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
		 */
		public Menu getMenu(Control parent) {
			createDropDownMenuMgr();
			dropDownMenuMgr.addMenuListener(new IMenuListener() {
				public void menuAboutToShow(IMenuManager manager) {
					IContributionItem[] items = getContributionItems();
					for (int i = 0; i < items.length; i++) {
						manager.add(items[i]);
					}
					manager.add(new OpenDialogAction());
				}
			});
			return dropDownMenuMgr.createContextMenu(parent);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
		 */
		public Menu getMenu(Menu parent) {
			createDropDownMenuMgr();
			final Menu menu = new Menu(parent);
			menu.addListener(SWT.Show, new Listener() {
				public void handleEvent(Event event) {
					if (menu.isDisposed()) {
						return;
					}
					MenuItem[] items = menu.getItems();
					for (int i = 0; i < items.length; i++) {
						items[i].dispose();
					}
					IContributionItem[] contributions = getContributionItems();
					for (int i = 0; i < contributions.length; i++) {
						contributions[i].fill(menu, -1);
					}
					new ActionContributionItem(new OpenDialogAction()).fill(
							menu, -1);
				}
			});
			return menu;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.action.IMenuCreator#dispose()
		 */
		public void dispose() {
			if (dropDownMenuMgr != null) {
				dropDownMenuMgr.dispose();
				dropDownMenuMgr = null;
			}
			if (menus.size()>0) {
				for (Iterator i = menus.iterator(); i.hasNext();) {
					Menu m = (Menu) i.next();
					if (!m.isDisposed()) {
						m.dispose();
					}
				}
				menus.clear();
			}
		}
	}

	/**
	 * Set definition for this action and text so that it will be used for File
	 * -&gt; Open Workspace in the argument window.
	 * 
	 * @param window
	 *            the window in which this action should appear
	 */
	public OpenWorkspaceAction(IWorkbenchWindow window) {
		super(IDEWorkbenchMessages.OpenWorkspaceAction_text,
				IAction.AS_DROP_DOWN_MENU);

		if (window == null) {
			throw new IllegalArgumentException();
		}

		// TODO help?

		this.window = window;
		setToolTipText(IDEWorkbenchMessages.OpenWorkspaceAction_toolTip);
		setActionDefinitionId("org.eclipse.ui.file.openWorkspace"); //$NON-NLS-1$
		setMenuCreator(new MenuCreator());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		String path = promptForWorkspace();
		if (path == null) {
			return;
		}

		restart(path);
	}

	/**
	 * Restart the workbench using the specified path as the workspace location.
	 * 
	 * @param path
	 *            the location
	 * @since 3.3
	 */
	private void restart(String path) {
		if (!canRelaunch()) {
			return;
		}
		
		RestartWorkbenchHandler.restart(path);
	}

	/**
	 * Use the ChooseWorkspaceDialog to get the new workspace from the user.
	 * 
	 * @return a string naming the new workspace and null if cancel was selected
	 */
	private String promptForWorkspace() {
		// get the current workspace as the default
		ChooseWorkspaceData data = new ChooseWorkspaceData(Platform
				.getInstanceLocation().getURL());
		ChooseWorkspaceDialog dialog = new ChooseWorkspaceWithSettingsDialog(
				window.getShell(), data, true, false);
		dialog.prompt(true);

		// return null if the user changed their mind
		String selection = data.getSelection();
		if (selection == null) {
			return null;
		}

		// otherwise store the new selection and return the selection
		data.writePersistedData();
		return selection;
	}

	/**
	 * Check if we have the eclipse.vm property set in order to re-launch.
	 * 
	 * @return <code>true</code> if the property has been set.
	 */
	private boolean canRelaunch() {
		String property = System.getProperty(PROP_VM);
		if (property == null) {
			MessageDialog
					.openError(
							window.getShell(),
							IDEWorkbenchMessages.OpenWorkspaceAction_errorTitle,
							NLS
									.bind(
											IDEWorkbenchMessages.OpenWorkspaceAction_errorMessage,
											PROP_VM));
			return false;
		}
		
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#dispose()
	 */
	public void dispose() {
		window = null;
	}
}
