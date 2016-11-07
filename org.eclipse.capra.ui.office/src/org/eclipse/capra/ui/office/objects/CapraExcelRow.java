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

package org.eclipse.capra.ui.office.objects;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.capra.ui.office.exceptions.CapraOfficeObjectNotFound;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSelection;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheetView;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheetViews;

import com.google.common.io.Files;

/**
 * This class extends the CapraOfficeObject and provides an object to describe a
 * single MS Excel row.
 *
 * @author Dusan Kalanj
 *
 */
public class CapraExcelRow extends CapraOfficeObject {

	/**
	 * RegEx of characters (tabs, newlines, carriage returns and invisible
	 * control characters) to be replaced with white-spaces in the Office View.
	 */
	private static final String LINE_BREAKS_AND_CONTROL_REQE = "[\r\n\t\\p{C}]+";

	private static final DataFormatter FORMATTER = new DataFormatter();

	/**
	 * Delimiter between excel cells as displayed in the Office View.
	 */
	private static final String CELL_DELIMITER = " | ";

	/**
	 * Delimiter between sheetId and rowId as used in the uri.
	 */
	private static final String ID_DELIMITER = "-";

	/**
	 * A constant that is used if the row index isn't found when opening object
	 * details.
	 */
	private static final int NO_ROW_INDEX = -1;

	/**
	 * A constant that is used if the last cell in the row isn't found.
	 */
	private static final String NO_LAST_CELL_REFERENCE = "-1";

	/**
	 * A constructor that generates a new instance of CapraExcelRow where the
	 * parent properties are extracted from the provided Excel row and File
	 * object that contains the row.
	 * 
	 * @param officeFile
	 *            a File object representing the Office document
	 * @param row
	 *            an Excel row, extracted from an Excel document
	 * 
	 */
	public CapraExcelRow(File officeFile, Row row) {
		super();

		String rowId = FORMATTER.formatCellValue(row.getCell(0));
		StringBuilder rowBuilder = new StringBuilder();
		rowBuilder.append("ID " + rowId + ": ");

		boolean firstCellSet = false;

		for (int j = 1; j < row.getLastCellNum(); j++) {
			Cell cell = row.getCell(j);
			String cellValue = FORMATTER.formatCellValue(cell);
			if (!cellValue.isEmpty()) {
				if (!firstCellSet) {
					rowBuilder.append(cellValue);
					firstCellSet = true;
				} else {
					rowBuilder.append(CELL_DELIMITER + cellValue);
				}
			}
		}

		if (firstCellSet) {
			Pattern p = Pattern.compile(LINE_BREAKS_AND_CONTROL_REQE);
			Matcher m = p.matcher(rowBuilder);

			String rowContent = (m.replaceAll(" ")).trim();
			String rowUriEnd = row.getSheet().getSheetName() + ID_DELIMITER + rowId;
			String rowUri = CapraOfficeObject.createUri(officeFile, rowUriEnd);

			this.setData(rowContent);
			this.setUri(rowUri);
		}
	}

	@Override
	public void showOfficeObjectInNativeEnvironment() throws CapraOfficeObjectNotFound {

		String fileType = Files.getFileExtension(getFile().getAbsolutePath());
		String rowId = getRowId();
		String sheetName = getSheetName();

		Sheet sheet;

		try (FileInputStream in = new FileInputStream(getFile())) {
			if (fileType.equals(CapraOfficeObject.XLSX)) {
				sheet = new XSSFWorkbook(in).getSheet(sheetName);
			} else {
				sheet = new HSSFWorkbook(in).getSheet(sheetName);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		DataFormatter formatter = new DataFormatter();
		int rowIndex = NO_ROW_INDEX;
		String lastCellReference = NO_LAST_CELL_REFERENCE;
		for (int i = 0; i <= sheet.getLastRowNum(); i++) {
			Row row = sheet.getRow(i);
			if (row != null) {
				String currRowID = formatter.formatCellValue(row.getCell(0));
				if (currRowID.equals(rowId)) {
					rowIndex = i;
					lastCellReference = CellReference.convertNumToColString(row.getLastCellNum()) + (rowIndex + 1);
					break;
				}
			}
		}

		if (rowIndex == NO_ROW_INDEX || lastCellReference == NO_LAST_CELL_REFERENCE)
			throw new CapraOfficeObjectNotFound(getRowId());

		int firstDisplayedRowIndex = (rowIndex - 2 > 0) ? rowIndex - 2 : 1;
		if (fileType.equals(CapraOfficeObject.XLSX)) {
			XSSFSheet xssfSheet = XSSFSheet.class.cast(sheet);
			int sheetIndex = xssfSheet.getWorkbook().getSheetIndex(xssfSheet);
			xssfSheet.getWorkbook().setActiveSheet(sheetIndex);

			CTSheetViews ctSheetViews = xssfSheet.getCTWorksheet().getSheetViews();
			CTSheetView ctSheetView = ctSheetViews.getSheetViewArray(ctSheetViews.sizeOfSheetViewArray() - 1);
			ctSheetView.setTopLeftCell("A" + firstDisplayedRowIndex);

			CTSelection ctSelection = ctSheetView.addNewSelection();
			ctSelection.setActiveCell("A" + (rowIndex + 1));
			ctSelection.setSqref(Arrays.asList("A" + (rowIndex + 1) + ":" + lastCellReference));

		} else {
			HSSFSheet hssfSheet = HSSFSheet.class.cast(sheet);
			hssfSheet.setActive(true);
			hssfSheet.showInPane((short) (rowIndex), (short) 0);

			HSSFRow row = hssfSheet.getRow(rowIndex);
			HSSFCell cell = row.getCell(0);
			cell.setAsActiveCell(); // TODO doesn't work - bug (but xls doesn't
									// necessarily even have to be supported)
		}

		try (FileOutputStream out = new FileOutputStream(getFile())) {
			sheet.getWorkbook().write(out);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// TODO If Excel is already open, then this doesn't trigger. Is there a
		// way to refresh the application?
		try {
			Desktop.getDesktop().open(getFile());
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	public String getSheetName() {
		String itemId = getId();
		int lastIndexOfDelimiter = itemId.lastIndexOf(ID_DELIMITER);
		return itemId.substring(0, lastIndexOfDelimiter);
	}

	public String getRowId() {
		String itemId = getId();
		int lastIndexOfDelimiter = itemId.lastIndexOf(ID_DELIMITER);
		return itemId.substring(lastIndexOfDelimiter + 1);
	}
}
