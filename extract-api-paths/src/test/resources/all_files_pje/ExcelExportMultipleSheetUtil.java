package br.com.itx.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jxls.exception.ParsePropertyException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import br.com.itx.exception.ExcelExportException;

/**
 * Gera um XLS com múltiplas sheets. No template, os dados podem ser acessados por ${list}
 */
public final class ExcelExportMultipleSheetUtil extends AbstractExcelExport {

	private Map<String, Object> bean = new HashMap<String, Object>();
	// Lista contendo listas dos dados a serem exibidos. Cada lista equivale a uma sheet
	private List<? extends List<?>> dados;
	// Nomes de cada sheet
	private List<String> sheetNames;
	
	public ExcelExportMultipleSheetUtil(String urlTemplate) {
		super(urlTemplate);
	}
	
	public ExcelExportMultipleSheetUtil(String urlTemplate, String fileNameDownload) {
		super(urlTemplate, fileNameDownload);
	}

	@Override
	public byte[] generate() throws ExcelExportException {
		try {
			if (dados == null || sheetNames == null) {
				throw new ExcelExportException("A lista de dados e a lista dos nomes das planilhas não podem ser nulas!");
			}
			InputStream inTemplate = new FileInputStream(getUrlTemplate());
			Workbook workbook = getTransformer().transformMultipleSheetsList(inTemplate, dados, sheetNames, "list", bean, 0);
			FileUtil.close(inTemplate);
			
			// Preserva alguns estilos da primeira sheet nas novas sheets
			Sheet first = workbook.getSheetAt(0);
			first.setZoom(1, 1);
			for (int i = 1; i < workbook.getNumberOfSheets(); i++) {
				Sheet sheet = workbook.getSheetAt(i);
				sheet.setDisplayFormulas(first.isDisplayFormulas());
				sheet.setDisplayGridlines(first.isDisplayGridlines());
				sheet.setDisplayZeros(first.isDisplayZeros());
				sheet.setDisplayRowColHeadings(first.isDisplayRowColHeadings());
				sheet.setDisplayGuts(first.getDisplayGuts());
				sheet.setPrintGridlines(first.isPrintGridlines());
				sheet.setZoom(1, 1);
			}
			
			net.sf.jxls.util.Util.writeToFile(getFileTemp().getAbsolutePath(), workbook);
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
	
	/**
	 * @param dados Lista contendo listas dos dados a serem exibidos. Cada lista equivale a uma sheet
	 */
	public void setDados(List<? extends List<?>> dados) {
		this.dados = dados;
	}
	
	/**
	 * @param sheetNames Nomes de cada sheet
	 */
	public void setSheetNames(List<String> sheetNames) {
		this.sheetNames = sheetNames;
	}
}
