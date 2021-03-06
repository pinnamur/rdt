/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Jesper Kamstrup Linnet (eclipse@kamstrup-linnet.dk) - initial API and implementation 
 *          (report 36180: Callers/Callees view)
 *******************************************************************************/
package org.rubypeople.rdt.internal.ui.callhierarchy;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.rubypeople.rdt.core.IMethod;
import org.rubypeople.rdt.internal.ui.IRubyHelpContextIds;
import org.rubypeople.rdt.internal.ui.RubyPlugin;
import org.rubypeople.rdt.internal.ui.dialogs.StatusInfo;
import org.rubypeople.rdt.internal.ui.wizards.dialogfields.DialogField;
import org.rubypeople.rdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.rubypeople.rdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.rubypeople.rdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.rubypeople.rdt.ui.RubyElementLabelProvider;

public class HistoryListAction extends Action {
	
	private class HistoryListDialog extends StatusDialog {
		
		private ListDialogField fHistoryList;
		private IStatus fHistoryStatus;
		private IMethod fResult;
		
		private HistoryListDialog(Shell shell, IMethod[] elements) {
			super(shell);
			setTitle(CallHierarchyMessages.HistoryListDialog_title); 
			
			String[] buttonLabels= new String[] { 
				CallHierarchyMessages.HistoryListDialog_remove_button, 
			};
					
			IListAdapter adapter= new IListAdapter() {
				public void customButtonPressed(ListDialogField field, int index) {
					doCustomButtonPressed();
				}
				public void selectionChanged(ListDialogField field) {
					doSelectionChanged();
				}
				
				public void doubleClicked(ListDialogField field) {
					doDoubleClicked();
				}				
			};
		
			RubyElementLabelProvider labelProvider= new RubyElementLabelProvider(RubyElementLabelProvider.SHOW_QUALIFIED | RubyElementLabelProvider.SHOW_ROOT);
			
			fHistoryList= new ListDialogField(adapter, buttonLabels, labelProvider);
			fHistoryList.setLabelText(CallHierarchyMessages.HistoryListDialog_label); 
			fHistoryList.setElements(Arrays.asList(elements));
			
			ISelection sel;
			if (elements.length > 0) {
				sel= new StructuredSelection(elements[0]);
			} else {
				sel= new StructuredSelection();
			}
			
			fHistoryList.selectElements(sel);
		}

			
		/*
		 * @see Dialog#createDialogArea(Composite)
		 */
		protected Control createDialogArea(Composite parent) {
			initializeDialogUnits(parent);
			
			Composite composite= (Composite) super.createDialogArea(parent);
			
			Composite inner= new Composite(composite, SWT.NONE);
			inner.setLayoutData(new GridData(GridData.FILL_BOTH));
			inner.setFont(composite.getFont());

			LayoutUtil.doDefaultLayout(inner, new DialogField[] { fHistoryList }, true, 0, 0);
			LayoutUtil.setHeightHint(fHistoryList.getListControl(null), convertHeightInCharsToPixels(12));
			LayoutUtil.setHorizontalGrabbing(fHistoryList.getListControl(null));

			applyDialogFont(composite);		
			return composite;
		}

		/**
		 * Method doCustomButtonPressed.
		 */
		private void doCustomButtonPressed() {
			fHistoryList.removeElements(fHistoryList.getSelectedElements());
		}
		
		private void doDoubleClicked() {
			if (fHistoryStatus.isOK()) {
				okPressed();
			}
		}
		
		
		private void doSelectionChanged() {
        	StatusInfo status= new StatusInfo();
        	List selected= fHistoryList.getSelectedElements();
        	if (selected.size() != 1) {
        		status.setError(""); //$NON-NLS-1$
        		fResult= null;
        	} else {
        		fResult= (IMethod) selected.get(0);
        	}
        	fHistoryList.enableButton(0, fHistoryList.getSize() > selected.size() && selected.size() != 0);			
        	fHistoryStatus= status;
        	updateStatus(status);	
        }
				
		public IMethod getResult() {
			return fResult;
		}
		
		public IMethod[] getRemaining() {
			List elems= fHistoryList.getElements();
			return (IMethod[]) elems.toArray(new IMethod[elems.size()]);
		}	
		
		/*
		 * @see org.eclipse.jface.window.Window#configureShell(Shell)
		 */
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell, IRubyHelpContextIds.HISTORY_LIST_DIALOG);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.window.Window#create()
		 */
		public void create() {
			setShellStyle(getShellStyle() | SWT.RESIZE);
			super.create();
		}

	}
	
	private CallHierarchyViewPart fView;
	
	public HistoryListAction(CallHierarchyViewPart view) {
		fView= view;
		setText(CallHierarchyMessages.HistoryListAction_label); 
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IRubyHelpContextIds.HISTORY_LIST_ACTION);
	}
		
	/*
	 * @see IAction#run()
	 */
	public void run() {
		IMethod[] historyEntries= fView.getHistoryEntries();
		HistoryListDialog dialog= new HistoryListDialog(RubyPlugin.getActiveWorkbenchShell(), historyEntries);
		if (dialog.open() == Window.OK) {
			fView.setHistoryEntries(dialog.getRemaining());
			fView.setMethod(dialog.getResult());
		}
	}

}

