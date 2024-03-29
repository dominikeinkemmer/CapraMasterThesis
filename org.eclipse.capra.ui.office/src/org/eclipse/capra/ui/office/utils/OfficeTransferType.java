/*******************************************************************************
 * Copyright (c) 2016 Chalmers | University of Gothenburg, rt-labs and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Chalmers | University of Gothenburg and rt-labs - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.capra.ui.office.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.capra.ui.office.model.CapraOfficeObject;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;

/**
 * The OfficeTransferType object provides the logic necessary to drag and drop a
 * non-native-java OfficeObject from one swt view (OfficeView) to another
 * (SelectionView). It does that by extending the ByteArrayTransfer class and
 * overriding the javaToNative and nativeToJava methods.
 *
 * Code adapted from IBM example at:
 * http://www.java2s.com/Tutorial/Java/0280__SWT/DragandDropdefinemyowndatatransfertype.htm
 *
 * @author Dusan Kalanj
 *
 */
public class OfficeTransferType extends ByteArrayTransfer {

	private static final String MIME_TYPE = "capra_office";
	private static final int MIME_TYPE_ID = registerType(MIME_TYPE);

	private static OfficeTransferType _instance = new OfficeTransferType();

	/** Provides an instance of the class. */
	public static OfficeTransferType getInstance() {
		return _instance;
	}

	@Override
	protected int[] getTypeIds() {
		return new int[] { MIME_TYPE_ID };
	}

	@Override
	protected String[] getTypeNames() {
		return new String[] { MIME_TYPE };
	}

	@Override
	protected boolean validate(Object object) {
		return checkMyType(object);
	}

	@SuppressWarnings("unchecked")
	private boolean checkMyType(Object object) {
		if (object == null || !(object instanceof ArrayList)) {
			return false;
		}

		if (object instanceof ArrayList<?>) {
			ArrayList<Object> objectList = (ArrayList<Object>) object;
			if (!(objectList.get(0) instanceof CapraOfficeObject)) {
				return false;
			}
		}

		ArrayList<CapraOfficeObject> officeObjects = (ArrayList<CapraOfficeObject>) object;
		if (officeObjects.isEmpty()) {
			return false;
		}

		return true;
	}

	/** Converts a java byte[] to a platform specific representation. */
	@SuppressWarnings("unchecked")
	@Override
	public void javaToNative(Object object, TransferData transferData) {
		if (!checkMyType(object) || !isSupportedType(transferData)) {
			DND.error(DND.ERROR_INVALID_DATA);
		}

		ArrayList<CapraOfficeObject> officeObjects = (ArrayList<CapraOfficeObject>) object;

		try (ByteArrayOutputStream out = new ByteArrayOutputStream();
				DataOutputStream writeOut = new DataOutputStream(out)) {

			for (int i = 0; i < officeObjects.size(); i++) {
				CapraOfficeObject currOfficeObject = officeObjects.get(i);

				byte[] buffer = currOfficeObject.getData().getBytes();
				writeOut.writeInt(buffer.length);
				writeOut.write(buffer);

				buffer = currOfficeObject.getUri().getBytes();
				writeOut.writeInt(buffer.length);
				writeOut.write(buffer);
			}

			byte[] bufferOut = out.toByteArray();

			super.javaToNative(bufferOut, transferData);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Converts a platform specific representation of a byte array to a java
	 * byte[].
	 */
	@Override
	public Object nativeToJava(TransferData transferData) {
		if (isSupportedType(transferData)) {
			byte[] buffer = (byte[]) super.nativeToJava(transferData);

			if (buffer == null) {
				return null;
			}

			ArrayList<CapraOfficeObject> officeObjects = new ArrayList<CapraOfficeObject>();

			try (ByteArrayInputStream in = new ByteArrayInputStream(buffer);
					DataInputStream readIn = new DataInputStream(in)) {

				while (readIn.available() > 0) {

					CapraOfficeObject currOfficeObject = new CapraOfficeObject();

					int size = readIn.readInt();
					byte[] objectData = new byte[size];
					readIn.read(objectData);

					size = readIn.readInt();
					byte[] objectUri = new byte[size];
					readIn.read(objectUri);

					currOfficeObject.setData(new String(objectData));
					currOfficeObject.setUri(new String(objectUri));

					officeObjects.add(currOfficeObject);
				}

			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}

			return officeObjects;
		}

		return null;
	}
}
