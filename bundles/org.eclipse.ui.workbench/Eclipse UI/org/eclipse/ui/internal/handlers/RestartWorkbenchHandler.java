/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Manuel Selva <manuel.selva@st.com> - Bug 197922
 *     Charles Bailey <cbailey32@bloomberg.net>,
 *     Henry Kleynhans <hkleynhans@bloomberg.net> - Bug 264072
 *******************************************************************************/

package org.eclipse.ui.internal.handlers;

import java.util.Map;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.PlatformUI;

/**
 * Provide a Handler for the Restart Workbench command.
 * 
 */
public class RestartWorkbenchHandler extends AbstractHandler {

	private static final String PROP_VM = "eclipse.vm"; //$NON-NLS-1$

	private static final String PROP_VMARGS = "eclipse.vmargs"; //$NON-NLS-1$

	private static final String PROP_COMMANDS = "eclipse.commands"; //$NON-NLS-1$

	private static final String PROP_EXIT_CODE = "eclipse.exitcode"; //$NON-NLS-1$

	private static final String PROP_EXIT_DATA = "eclipse.exitdata"; //$NON-NLS-1$

	private static final String CMD_DATA = "-data"; //$NON-NLS-1$

	private static final String CMD_VMARGS = "-vmargs"; //$NON-NLS-1$

	private static final String NEW_LINE = "\n"; //$NON-NLS-1$

	private static final Integer EXIT_RELAUNCH = new Integer(24);

	public static final String PARM_WORKSPACE = "eclipse.relaunch.workspace"; //$NON-NLS-1$ 

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event){
		restart(getWorkspace(event.getParameters()));
		return null;
	}

	public static void restart(final String path) {
		final String cmdline = buildCommandLine(path);
		if (cmdline != null) {
			System.setProperty(PROP_EXIT_CODE, Integer.toString(EXIT_RELAUNCH));
			System.setProperty(PROP_EXIT_DATA, cmdline);
		}
		PlatformUI.getWorkbench().restart();
	}

	private String getWorkspace(final Map parameters) {
		if (parameters.containsKey(PARM_WORKSPACE)) {
			return (String) parameters.get(PARM_WORKSPACE);
		}

		return Platform.getInstanceLocation().getURL().getPath();
	}

	/**
	 * Create and return a string with command line options for eclipse.exe that
	 * will launch a new workbench that is the same as the currently running
	 * one, but using the argument directory as its workspace.
	 * 
	 * @param workspace
	 *            the directory to use as the new workspace
	 * @return a string of command line options or null on error
	 */
	private static String buildCommandLine(String workspace) {
		String property = System.getProperty(PROP_VM);
		if (property == null) {
			return null;
		}

		StringBuffer result = new StringBuffer(512);
		result.append(property);
		result.append(NEW_LINE);

		// append the vmargs and commands. Assume that these already end in \n
		String vmargs = System.getProperty(PROP_VMARGS);
		if (vmargs != null) {
			result.append(vmargs);
		}

		// append the rest of the args, replacing or adding -data as required
		property = System.getProperty(PROP_COMMANDS);
		if (property == null) {
			result.append(CMD_DATA);
			result.append(NEW_LINE);
			result.append(workspace);
			result.append(NEW_LINE);
		} else {
			// find the index of the arg to replace its value
			int cmd_data_pos = property.lastIndexOf(CMD_DATA);
			if (cmd_data_pos != -1) {
				cmd_data_pos += CMD_DATA.length() + 1;
				result.append(property.substring(0, cmd_data_pos));
				result.append(workspace);
				result.append(property.substring(property.indexOf('\n', cmd_data_pos)));
			} else {
				result.append(CMD_DATA);
				result.append(NEW_LINE);
				result.append(workspace);
				result.append(NEW_LINE);
				result.append(property);
			}
		}

		// put the vmargs back at the very end (the eclipse.commands property
		// already contains the -vm arg)
		if (vmargs != null) {
			result.append(CMD_VMARGS);
			result.append(NEW_LINE);
			result.append(vmargs);
		}

		return result.toString();
	}

}
