/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.akrogen.tkui.css.swt.engine.style;

import org.akrogen.tkui.css.core.dom.CSSStylableElement;
import org.akrogen.tkui.css.core.engine.CSSEngine;
import org.akrogen.tkui.css.swt.engine.CSSSWTEngineImpl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.w3c.dom.css.CSS2Properties;

public class CSSSWTEngineStyleTest {

	public static void main(String[] args) {
		try {
			Display display = new Display();
			// Instanciate SWT CSS Engine
			CSSEngine engine = new CSSSWTEngineImpl(display);
			// engine.parseStyleSheet(CSSCoreResources.getStyleCLass());

			/*---   UI SWT ---*/
			Shell shell = new Shell(display, SWT.SHELL_TRIM);
			FillLayout layout = new FillLayout();
			shell.setLayout(layout);

			Composite panel1 = new Composite(shell, SWT.NONE);
			panel1.setLayout(new FillLayout());

			// Label
			Label label1 = new Label(panel1, SWT.NONE);
			label1.setText("Label 0 [color:red;]");
			CSSStylableElement stylableElement = (CSSStylableElement) engine
					.getElement(label1);
			CSS2Properties style = stylableElement.getStyle();
			style.setBackgroundColor("red");
			style.setFontSize("20");
			style.setFontFamily("Roman");
			style.setFontWeight("bold");
			style.setFontStyle("italic");
			style.setColor("white");

			// Text
			Text text1 = new Text(panel1, SWT.NONE);
			text1.setText("bla bla bla...");

			/*---   End UI SWT  ---*/
			// Apply Styles
			engine.applyStyles(shell, true);

			shell.pack();
			shell.open();

			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}

			display.dispose();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
