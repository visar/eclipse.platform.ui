/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.WorkingSet;
import org.eclipse.ui.tests.harness.util.ArrayUtil;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.eclipse.ui.tests.menus.ObjectContributionClasses.IA;
import org.eclipse.ui.tests.menus.ObjectContributionClasses.ICommon;
import org.eclipse.ui.tests.menus.ObjectContributionClasses.IModelElement;

public class IWorkingSetTest extends UITestCase {
    final static String WORKING_SET_NAME_1 = "ws1";

    final static String WORKING_SET_NAME_2 = "ws2";

    IWorkspace fWorkspace;

    IWorkingSet fWorkingSet;

	public String fChangeProperty;

	public IWorkingSet fChangeNewValue;

	public IWorkingSet fChangeOldValue;

    public IWorkingSetTest(String testName) {
        super(testName);
    }

    @Override
	protected void doSetUp() throws Exception {
        super.doSetUp();
        IWorkingSetManager workingSetManager = fWorkbench
                .getWorkingSetManager();

        fWorkspace = ResourcesPlugin.getWorkspace();
        fWorkingSet = workingSetManager.createWorkingSet(WORKING_SET_NAME_1,
                new IAdaptable[] { fWorkspace.getRoot() });
        
        workingSetManager.addWorkingSet(fWorkingSet);
    }
	@Override
	protected void doTearDown() throws Exception {
		IWorkingSetManager workingSetManager = fWorkbench
        .getWorkingSetManager();
		 workingSetManager.removeWorkingSet(fWorkingSet);
		super.doTearDown();
	}
    public void testGetElements() throws Throwable {
        assertEquals(fWorkspace.getRoot(), fWorkingSet.getElements()[0]);
    }

    public void testGetId() throws Throwable {
        assertEquals(null, fWorkingSet.getId());
        fWorkingSet.setId("bogusId");
        assertEquals("bogusId", fWorkingSet.getId());
        fWorkingSet.setId(null);
        assertEquals(null, fWorkingSet.getId());
    }

    public void testGetName() throws Throwable {
        assertEquals(WORKING_SET_NAME_1, fWorkingSet.getName());
    }

    public void testSetElements() throws Throwable {
        boolean exceptionThrown = false;

        try {
            fWorkingSet.setElements(null);
        } catch (RuntimeException exception) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        IProject p1 = FileUtil.createProject("TP1");
        IFile f1 = FileUtil.createFile("f1.txt", p1);
        IAdaptable[] elements = new IAdaptable[] { f1, p1 };
        fWorkingSet.setElements(elements);
        assertTrue(ArrayUtil.equals(elements, fWorkingSet.getElements()));

        fWorkingSet.setElements(new IAdaptable[] { f1 });
        assertEquals(f1, fWorkingSet.getElements()[0]);

        fWorkingSet.setElements(new IAdaptable[] {});
        assertEquals(0, fWorkingSet.getElements().length);
    }

    public void testSetId() throws Throwable {
        assertEquals(null, fWorkingSet.getId());
        fWorkingSet.setId("bogusId");
        assertEquals("bogusId", fWorkingSet.getId());
        fWorkingSet.setId(null);
        assertEquals(null, fWorkingSet.getId());
    }

    public void testSetName() throws Throwable {
        boolean exceptionThrown = false;

        try {
            fWorkingSet.setName(null);
        } catch (RuntimeException exception) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        fWorkingSet.setName(WORKING_SET_NAME_2);
        assertEquals(WORKING_SET_NAME_2, fWorkingSet.getName());

        exceptionThrown = false;
		try {
			String name = fWorkingSet.getName();
			// set same name
			fWorkingSet.setName(name);
		} catch (RuntimeException exception) {
			exceptionThrown = true;
		}
		assertFalse("Failed to setName when new name is same as old name",
				exceptionThrown);
		
        fWorkingSet.setName("");
        assertEquals("", fWorkingSet.getName());

        fWorkingSet.setName(" ");
        assertEquals(" ", fWorkingSet.getName());
    }
   
	public void testNoDuplicateWorkingSetName() throws Throwable {
		/* get workingSetManager */
		IWorkingSetManager workingSetManager = fWorkbench
				.getWorkingSetManager();

		/*
		 * check that initially workingSetManager contains "fWorkingSet"
		 */
		assertTrue(ArrayUtil.equals(new IWorkingSet[] { fWorkingSet },
				workingSetManager.getWorkingSets()));

		IWorkingSet wSet = workingSetManager.createWorkingSet(
				WORKING_SET_NAME_2, new IAdaptable[] {});
		workingSetManager.addWorkingSet(wSet);

		/* check that workingSetManager contains "fWorkingSet" and wSet */
		assertTrue(ArrayUtil.equals(new IWorkingSet[] { fWorkingSet, wSet },
				workingSetManager.getWorkingSets())
				|| ArrayUtil.equals(new IWorkingSet[] { wSet, fWorkingSet },
						workingSetManager.getWorkingSets()));

		String sameName = fWorkingSet.getName();
		boolean exceptionThrown = false;

		try {
			wSet.setName(sameName);
			/* Test failed,set original name for restoring state */
			wSet.setName(WORKING_SET_NAME_2);
		} catch (RuntimeException exception) {
			exceptionThrown = true;
		}
		assertTrue(exceptionThrown);

		/* restore state */
		workingSetManager.removeWorkingSet(wSet);
	}

	public void testNoDuplicateWorkingSetNamesDifferentLabels()
			throws Throwable {
		/* get workingSetManager */
		IWorkingSetManager workingSetManager = fWorkbench
				.getWorkingSetManager();
		/*
		 * check that initially workingSetManager contains "fWorkingSet"
		 */
		assertTrue(ArrayUtil.equals(new IWorkingSet[] { fWorkingSet },
				workingSetManager.getWorkingSets()));

		String sameName = fWorkingSet.getName();
		IWorkingSet wSet = workingSetManager.createWorkingSet(sameName,
				new IAdaptable[] {});
		wSet.setLabel(WORKING_SET_NAME_2);

		/*
		 * Expected to throw an error as the wSet has the same name as
		 * fWorkingSet
		 */
		boolean exceptionThrown = false;
		try {
			workingSetManager.addWorkingSet(wSet);
			/* Test failed, restore state */
			workingSetManager.removeWorkingSet(wSet);
		} catch (RuntimeException exception) {
			exceptionThrown = true;
		}
		assertTrue(exceptionThrown);
	}

	public void testBug37183() throws CoreException {

		class TestPropertyChangeListener implements IPropertyChangeListener {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				fChangeProperty = event.getProperty();
				fChangeNewValue = (IWorkingSet) event.getNewValue();
				fChangeOldValue = (IWorkingSet) event.getOldValue();
			}
		}
		fChangeOldValue = null;
		IPropertyChangeListener workingSetListener = new TestPropertyChangeListener();
		fWorkbench.getWorkingSetManager().addPropertyChangeListener(
				workingSetListener);

		assertEquals(WORKING_SET_NAME_1, fWorkingSet.getLabel());
		fWorkingSet.setLabel(WORKING_SET_NAME_2);
		assertNotNull(fChangeOldValue);
		assertEquals(WORKING_SET_NAME_1, fChangeOldValue.getLabel());
		assertEquals(WORKING_SET_NAME_2, fWorkingSet.getLabel());

		fChangeOldValue = null;
		assertEquals(WORKING_SET_NAME_1, fWorkingSet.getName());
		fWorkingSet.setName(WORKING_SET_NAME_2);
		assertNotNull(fChangeOldValue);
		assertEquals(WORKING_SET_NAME_1, fChangeOldValue.getName());
		assertEquals(WORKING_SET_NAME_2, fChangeOldValue.getLabel());

		IProject p1 = FileUtil.createProject("TP1");
		IFile f1 = FileUtil.createFile("f1.txt", p1);
		fChangeOldValue = null;
		assertEquals(1, fWorkingSet.getElements().length);
		assertEquals(WORKING_SET_NAME_2, fWorkingSet.getName());
		assertEquals(WORKING_SET_NAME_2, fWorkingSet.getLabel());
		IAdaptable[] elements = new IAdaptable[] { f1, p1 };
		fWorkingSet.setElements(elements);
		assertNotNull(fChangeOldValue);
		assertEquals(1, fChangeOldValue.getElements().length);
		assertEquals(WORKING_SET_NAME_2, fChangeOldValue.getName());
		assertEquals(WORKING_SET_NAME_2, fChangeOldValue.getLabel());
		assertEquals(2, fWorkingSet.getElements().length);
		assertEquals(WORKING_SET_NAME_2, fWorkingSet.getName());
		assertEquals(WORKING_SET_NAME_2, fWorkingSet.getLabel());

		fWorkbench.getWorkingSetManager().removePropertyChangeListener(
				workingSetListener);
	}

    public void testIsEmpty() {
		fWorkingSet.setElements(new IAdaptable[] {});
		assertTrue(fWorkingSet.isEmpty());
		fWorkingSet.setElements(new IAdaptable[] { new IAdaptable() {
			@Override
			public Object getAdapter(Class adapter) {
				return null;
			}
		} });
		assertFalse(fWorkingSet.isEmpty());
	}
    
    
    public void testApplicableTo_ResourceWorkingSet() {
		fWorkingSet.setId("org.eclipse.ui.resourceWorkingSetPage");
		assertEquals("org.eclipse.ui.resourceWorkingSetPage", fWorkingSet
				.getId());
		IAdaptable[] adapted = fWorkingSet.adaptElements(new IAdaptable[] {ResourcesPlugin.getWorkspace()
				.getRoot()});
		assertEquals(1, adapted.length);
		assertTrue(adapted[0] instanceof IWorkspaceRoot);
    }
    
    public void testApplicableTo_DirectComparison() {

		fWorkingSet.setId("org.eclipse.ui.tests.api.MockWorkingSet");
		Foo myFoo = new Foo();
		IAdaptable[] adapted = fWorkingSet.adaptElements(new IAdaptable[] {myFoo});
		assertEquals(1, adapted.length);
		assertTrue(adapted[0] instanceof Foo);
    }
    
    public void testApplicableTo_Inheritance() {
    	fWorkingSet.setId("org.eclipse.ui.tests.api.MockWorkingSet");
		Bar myBar = new Bar();
		IAdaptable[] adapted = fWorkingSet.adaptElements(new IAdaptable[] {myBar});
		assertEquals(1, adapted.length);
		assertTrue(adapted[0] instanceof Bar);
	}
    
    public void testApplicableTo_Adapter1() {
    	fWorkingSet.setId("org.eclipse.ui.tests.api.MockWorkingSet");
    	ToFoo tc = new ToFoo();
    	IAdaptable[] adapted = fWorkingSet.adaptElements(new IAdaptable[] {tc});
		assertEquals(1, adapted.length);
		assertTrue(adapted[0] instanceof Foo);
    }
    
    public void testApplicableTo_AdapterManager1() {
    	fWorkingSet.setId("org.eclipse.ui.tests.api.MockWorkingSet");
    	IAImpl ia = new IAImpl();
    	IAdaptable[] adapted = fWorkingSet.adaptElements(new IAdaptable[] {ia});
		assertEquals(1, adapted.length);
		assertTrue(adapted[0] instanceof ICommon);
    }
    
    /**
     * Tests that adaptable=false is working.  ModelElement has a registered adapter to IResource that should not be used.
     */
    public void testApplicableTo_AdapterManager2() {
    	fWorkingSet.setId("org.eclipse.ui.tests.api.MockWorkingSet");
    	ModelElement element = new ModelElement();
    	assertTrue(fWorkingSet.adaptElements(new IAdaptable[] {element}).length == 0);
    }
    
    /**
	 * Tests to verify that we don't fall down in the event that the factory
	 * throws an exception while restoring a working set.
	 */
	public void testBadFactory_Restore() {
		fWorkingSet
				.setElements(new IAdaptable[] { new BadElementFactory.BadElementInstance() });
		IMemento m = XMLMemento.createWriteRoot("ws");
		fWorkingSet.saveState(m);
		BadElementFactory.fail = true;
		IWorkingSet copy = new WorkingSet(fWorkingSet.getName(), fWorkingSet.getId(), m) {};
		try {
			assertFalse(BadElementFactory.failAttempted);
			IAdaptable [] elements = copy.getElements();
			assertTrue(BadElementFactory.failAttempted);
			assertEquals("Element array should be empty", 0, elements.length);
		}
		catch (RuntimeException e) {
			fail("Error getting elements for broken factory", e);
		}
	}
	
	/**
	 * Tests to verify that we don't fall down in the event that the persistable
	 * throws an exception while saving a working set.
	 */
	public void testBadFactory_Save() {
		fWorkingSet
				.setElements(new IAdaptable[] { new BadElementFactory.BadElementInstance() });
		IMemento m = XMLMemento.createWriteRoot("ws");
		BadElementFactory.BadElementInstance.fail = true;
		assertFalse(BadElementFactory.BadElementInstance.failAttempted);
		try {
			fWorkingSet.saveState(m);
			assertTrue(BadElementFactory.BadElementInstance.failAttempted);
		} catch (RuntimeException e) {
			fail("Error saving elements for broken persistable", e);
		}
	}
    
    public static class Foo implements IAdaptable {

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
		 */
		@Override
		public Object getAdapter(Class adapter) {
			// TODO Auto-generated method stub
			return null;
		}	
    }
    
    public static class Bar extends Foo {
    	
    }
    
    public class ToFoo implements IAdaptable {

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
		 */
		@Override
		public Object getAdapter(Class adapter) {
			if (adapter == Foo.class) {
				return new Foo() {};
			}
			return null;
		}
    	
    }
    
    public static class IAImpl implements IA, IAdaptable {

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
		 */
		@Override
		public Object getAdapter(Class adapter) {
			// TODO Auto-generated method stub
			return null;
		}	
    }
    
    public static class ModelElement implements IModelElement, IAdaptable {

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
		 */
		@Override
		public Object getAdapter(Class adapter) {
			return null;
		}
    	
    }
}
