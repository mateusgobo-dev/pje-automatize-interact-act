package br.com.itx.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.sf.jxls.exception.ParsePropertyException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import br.com.itx.exception.ExcelExportException;

public final class ExcelExportUtil extends AbstractExcelExport {
	private Map<String, Object> bean = new HashMap<String, Object>();
	
	public ExcelExportUtil(String urlTemplate) {
		super(urlTemplate);
	}
	
	public ExcelExportUtil(String urlTemplate, String fileNameDownload) {
		super(urlTemplate, fileNameDownload);
	}
	
	@Override
	public byte[] generate() throws ExcelExportException {
		try {
			if (bean == null) {
				throw new ExcelExportException("O bean de dados não pode ser nulo!");
			}
			getTransformer().transformXLS(getUrlTemplate(), bean, getFileTemp().getAbsolutePath());
			byte[] report = FileUtil.readFile(getFileTemp());
			FileUtil.deleteFile(getFileTemp());
			return report;
		} catch (ParsePropertyException e) {
			throw new ExcelExportException(e);
		} catch (InvalidFormatException e) {
			throw new ExcelExportException(e);
		} catch (IOException e) {
			throw new ExcelExportException(e);
		}
	}
	
	public void setBean(Map<String, Object> bean) {
		this.bean = bean;
	}
	
	public static void downloadXLS(String urlTemplate, Map<String, Object> bean, String fileNameDownload) throws ExcelExportException {
		ExcelExportUtil util = new ExcelExportUtil(urlTemplate, fileNameDownload);
		util.setBean(bean);
		util.download();
	}
}