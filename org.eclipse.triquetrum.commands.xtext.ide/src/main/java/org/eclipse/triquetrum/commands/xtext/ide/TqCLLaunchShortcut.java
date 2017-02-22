/*******************************************************************************
 * Copyright (c)  2017 Totaro Rodolfo
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *    Rodolfo Totaro - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.triquetrum.commands.xtext.ide;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.triquetrum.commands.interpreter.TqclInterpreter;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;


//TODO: TqCL Macro launcher
public class TqCLLaunchShortcut implements ILaunchShortcut {

	@Override
	public void launch(ISelection selection, String mode) {
		Object firstElement = ((IStructuredSelection) selection).getFirstElement();
		if (firstElement instanceof IFile) {
			try {
				IFile file = (IFile) firstElement;
				java.net.URI rawLocationURI = file.getRawLocationURI();
				
				IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
				MessageConsole messageConsole = new MessageConsole(file.getName(), "TqCL", null, false);
				consoleManager.addConsoles(new IConsole[]{messageConsole});
				MessageConsoleStream newMessageStream = messageConsole.newMessageStream();
//				newMessageStream
				
				TqclInterpreter interpreter = new TqclInterpreter();
				interpreter.interpret(file.getName(),file.getContents(), file.getParent().getLocationURI(),org.eclipse.triquetrum.workflow.model.CompositeActor.class);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void launch(IEditorPart editor, String mode) {
		// TODO Auto-generated method stub
		IEditorInput firstElement = editor.getEditorInput();
		if (firstElement instanceof IFile) {
			IFile file = (IFile) firstElement;

		}
	}

}
