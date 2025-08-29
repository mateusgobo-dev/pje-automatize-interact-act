package br.com.itx.util;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;

import net.sf.jxls.transformer.XLSTransformer;
import br.com.itx.component.FileHome;
import br.com.itx.component.Util;
import br.com.itx.exception.ExcelExportException;

public abstract class AbstractExcelExport implements ExcelExport {

	private short[] columnsToHide;
	private String urlTemplate;
	private String fileNameDownload;
	private File fileTemp;
	private XLSTransformer transformer = new XLSTransformer();
	
	/**
	 * Usar quando só quiser os bytes do XLS
	 * @param urlTemplate URL do template
	 */
	public AbstractExcelExport(String urlTemplate) {
		this.urlTemplate = urlTemplate;
	}
	
	/**
	 * Usar quando quiser já fazer download do XLS
	 * @param urlTemplate URL do template
	 * @param fileNameDownload Nome do arquivo ao fazer download
	 */
	public AbstractExcelExport(String urlTemplate, String fileNameDownload) {
		this.urlTemplate = urlTemplate;
		this.fileNameDownload = fileNameDownload;
	}
	
	@Override
	public abstract byte[] generate() throws ExcelExportException;

	@Override
	public void download() throws ExcelExportException {
		byte[] report = generate();
		FileHome fh = FileHome.instance();
		fh.setContentType("application/xls");
		fh.setData(report);
		fh.setFileName(fileNameDownload);
		fh.setSize(report.length);
		fh.download();
	}

	@Override
	public void setColumnsToHide(short[] columnsToHide) {
		this.columnsToHide = columnsToHide;
	}

	@Override
	public short[] getColumnsToHide() {
		return columnsToHide;
	}
	
	protected XLSTransformer getTransformer() {
		return transformer;
	}
	
	protected String getUrlTemplate() {
		return urlTemplate;
	}
	
	protected String getFileNameDownload() {
		return fileNameDownload;
	}
	
	protected File getFileTemp() throws IOException {
		if (fileTemp == null) {
			File dir = new File(new Util().getContextRealPath() + "/WEB-INF/temp/");
			fileTemp = File.createTempFile(MessageFormat.format("{1,date,kkmmss}", new Date()), ".xls", dir);
		}
		return fileTemp;
	}
}
