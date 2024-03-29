/*******************************************************************************
 * Copyright (c) 2016 Chalmers | University of Gothenburg, rt-labs and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 *   Contributors:
 *      Chalmers | University of Gothenburg and rt-labs - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.capra.ui.plantuml;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * Toggles between showing transitive and direct links
 * 
 * @author Anthony Anjorin, Salome Maro
 */
public class TransitivityDepthHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		String initialValue = getTransitivityDepth();
		InputDialog depthInput = new InputDialog(window.getShell(), "Transitivity depth",
				"Input the wished depth limit for transitivity. Enter 0 if no limit is wished.", initialValue, null);
		if (depthInput.open() == Window.OK) {
			String depth = depthInput.getValue();
			setTransitivityDepth(depth);
		}

		return null;
	}

	/**
	 * Gets the depth that was set by the user for transitivity returns 0 in
	 * case no depth was set or no depth limit is wanted
	 * 
	 * @return
	 */
	public static String getTransitivityDepth() {
		Preferences transitivity = getPreference();
		return transitivity.get("option", "0");
	}

	private static Preferences getPreference() {
		Preferences preferences = InstanceScope.INSTANCE.getNode("org.eclipse.capra.ui.plantuml.transitivityDepth");
		Preferences transitivity = preferences.node("transitivityDepth");
		return transitivity;
	}

	/**
	 * Sets whether the trace view is set to show transitive traces.
	 * 
	 * @param value
	 *            indicates whether transitive traces should be shown
	 */
	public static void setTransitivityDepth(String depth) {
		Preferences transitivity = getPreference();

		transitivity.put("option", depth);

		try {
			// forces the application to save the preferences
			transitivity.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}
}