package br.com.itx.util;

import br.com.itx.exception.ExcelExportException;

public interface ExcelExport {
	byte[] generate() throws ExcelExportException;
	void download() throws ExcelExportException;
	void setColumnsToHide(short[] columnsToHide);
	short[] getColumnsToHide();
}
