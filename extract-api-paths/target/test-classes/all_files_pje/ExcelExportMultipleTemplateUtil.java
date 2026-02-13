package br.com.itx.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import net.sf.jxls.exception.ParsePropertyException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;

import br.com.itx.exception.ExcelExportException;

/** 
 * Gera um XLS a partir de um template que possui várias sheets, cada uma com um template 
 */
public final class ExcelExportMultipleTemplateUtil extends AbstractExcelExport {

	// Nome das sheets, em ordem, conforme estão no template
	private List<String> templateSheetNameList;
	// Nome das sheets no XLS gerado, em ordem
	private List<String> sheetNameList;
	// Parâmetros para os templates, em ordem
	private List<Map<String, Object>> beanParamsList;
	
	public ExcelExportMultipleTemplateUtil(String urlTemplate) {
		super(urlTemplate);
	}

	public ExcelExportMultipleTemplateUtil(String urlTemplate, String fileNameDownload) {
		super(urlTemplate, fileNameDownload);
	}

	@Override
	public byte[] generate() throws ExcelExportException {
		try {
			if (templateSheetNameList == null || sheetNameList == null || beanParamsList == null) {
				throw new ExcelExportException("A lista de templates, a lista dos nomes das planilhas e a lista de beans não podem ser nulas!");
			}
			InputStream inTemplate = new FileInputStream(getUrlTemplate());
			Workbook workbook = getTransformer().transformXLS(inTemplate, templateSheetNameList, sheetNameList, beanParamsList);
			FileUtil.close(inTemplate);
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

	/**
	 * @param templateSheetNameList Nome das sheets, em ordem, conforme estão no template
	 */
	public void setTemplateSheetNameList(List<String> templateSheetNameList) {
		this.templateSheetNameList = templateSheetNameList;
	}
	
	/** 
	 * @param sheetNameList Nome das sheets no XLS gerado, em ordem
	 */
	public void setSheetNameList(List<String> sheetNameList) {
		this.sheetNameList = sheetNameList;
	}
	
	/**
	 * @param beanParamsList Parâmetros para os templates, em ordem
	 */
	public void setBeanParamsList(List<Map<String, Object>> beanParamsList) {
		this.beanParamsList = beanParamsList;
	}
}
