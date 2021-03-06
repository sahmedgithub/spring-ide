/*******************************************************************************
 * Copyright (c) 2006, 2008, 20014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Kris De Volder - copied from SpellingAnnotation to become
 *     					'SpringPropertyAnnotation'.
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.reconciling;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.text.source.Annotation;

/**
 * Spelling annotation.
 *
 * @since 3.3
 */
@SuppressWarnings("restriction")
public class SpringPropertyAnnotation extends Annotation /*implements IQuickFixableAnnotation*/ {

	/** Annotation type for error annotations */
	public static final String ERROR_TYPE = org.eclipse.jdt.internal.ui.javaeditor.JavaMarkerAnnotation.ERROR_ANNOTATION_TYPE;
	public static final String WARNING_TYPE = org.eclipse.jdt.internal.ui.javaeditor.JavaMarkerAnnotation.WARNING_ANNOTATION_TYPE;
		//Could use our own annotation type (but then we also have to declare it somehow to make it show error style marker)

	public static final Set<String> TYPES = new HashSet<String>();
	static {
		TYPES.add(ERROR_TYPE);
		TYPES.add(WARNING_TYPE);
	}

	private SpringPropertyProblem fProblem;

	/**
	 * Creates a new annotation of given type.
	 */
	public SpringPropertyAnnotation(SpringPropertyProblem problem) {
		super(problem.getSeverity(), false, problem.getMessage());
		fProblem = problem;
	}

//	/*
//	 * @see org.eclipse.jface.text.quickassist.IQuickFixableAnnotation#isQuickFixable()
//	 */
//	public boolean isQuickFixable() {
//		return true;
//	}
//
//	/*
//	 * @see org.eclipse.jface.text.quickassist.IQuickFixableAnnotation#isQuickFixableStateSet()
//	 */
//	public boolean isQuickFixableStateSet() {
//		return true;
//	}
//
//	/*
//	 * @see org.eclipse.jface.text.quickassist.IQuickFixableAnnotation#setQuickFixable(boolean)
//	 */
//	public void setQuickFixable(boolean state) {
//		// always true
//	}

	/**
	 * Returns the spelling problem.
	 *
	 * @return the spelling problem
	 */
	public SpringPropertyProblem getSpringPropertyProblem() {
		return fProblem;
	}

}
