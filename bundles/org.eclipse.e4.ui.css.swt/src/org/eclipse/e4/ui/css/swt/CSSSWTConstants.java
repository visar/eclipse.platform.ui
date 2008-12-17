/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt;

import org.eclipse.e4.ui.css.core.dom.properties.CSSBorderProperties;

public class CSSSWTConstants {

	/**
	 * Key value for setting and getting the CSS class name of  a widget.
	 * Clients may rely on the value of this key if they want to avoid a dependency on this package.
	 * @see Widget.getData(String) Widget.setData(String, Object)
	 */
	public static final String CSS_CLASS_NAME_KEY = "org.eclipse.e4.ui.css.CssClassName";

	/**
	 * Key value for setting and getting the CSS ID of a widget.
	 * Clients may rely on the value of this key if they want to avoid a dependency on this package.
	 * @see Widget.getData(String) Widget.setData(String, Object)
	 */
	public static final String CSS_ID_KEY = "org.eclipse.e4.ui.css.id";

	
	/**
	 * Constant used to store {@link CSSBorderProperties} instance into SWT
	 * control data.
	 */
	public static final String CONTROL_CSS2BORDER_KEY = "org.eclipse.e4.ui.core.css.swt.CONTROL_CSS2BORDER_KEY";

	/**
	 * Constant used to store {@link CSS2FontProperties} instance into SWT
	 * control data.
	 */
	// public static final String CONTROL_CSS2FONT_KEY =
	// "org.eclipse.e4.ui.core.css.swt.CONTROL_CSS2FONT_KEY";
	/**
	 * Constant used to store String Text into SWT control data.
	 */
	public static final String TEXT_KEY = "org.eclipse.e4.ui.core.css.swt.TEXT_KEY";

}
