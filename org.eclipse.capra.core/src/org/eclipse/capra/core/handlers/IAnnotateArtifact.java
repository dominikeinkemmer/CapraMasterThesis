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
package org.eclipse.capra.core.handlers;

import org.eclipse.emf.ecore.EObject;

/**
 * Interface to be optionally implemented by artifact handler that supports
 * annotation.
 */
public interface IAnnotateArtifact {

	/**
	 * Annotate artifact with given text
	 *
	 * @param artifact
	 *            Artifact to be annotated
	 * @param annotation
	 *            Annotation text
	 * @throws AnnotationException
	 *             Exception thrown by handler if annotation fails
	 */
	void annotateArtifact(EObject artifact, String annotation) throws AnnotationException;
}