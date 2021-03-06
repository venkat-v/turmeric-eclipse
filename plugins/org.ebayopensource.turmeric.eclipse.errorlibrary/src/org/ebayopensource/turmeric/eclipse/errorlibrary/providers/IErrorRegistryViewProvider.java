/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
/**
 * 
 */
package org.ebayopensource.turmeric.eclipse.errorlibrary.providers;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;

/**
 * The Interface IErrorRegistryViewProvider.
 *
 * @author yayu
 * @since 1.0.0
 */
public interface IErrorRegistryViewProvider {
	
	/**
	 * Create build system specific client area.
	 *
	 * @param parent the parent
	 * @param errorViewer the error viewer
	 * @throws Exception the exception
	 */
	public void postClientAreaCreation(Composite parent, 
			TreeViewer errorViewer) throws Exception;

	/**
	 * Create build system specific context menu.
	 *
	 * @param mgr the mgr
	 * @param errorViewer the error viewer
	 * @throws Exception the exception
	 */
	public void createContextMenu( 
			MenuManager mgr, 
			TreeViewer errorViewer) throws Exception;
	
	/**
	 * Create build system specific tool bar.
	 *
	 * @param mgr the mgr
	 * @param errorViewer the error viewer
	 * @throws Exception the exception
	 */
	public void createToolBar(IToolBarManager mgr, TreeViewer errorViewer) throws Exception;
	
}
