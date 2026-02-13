package br.jus.cnj.pje.util;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

public class IndexEvent extends PdfPageEventHelper {
	private int page;
	public boolean body;
	@Override
	public void onEndPage(PdfWriter writer, Document document) {
		if(body) {
			page++;
			ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, 
					new Phrase(page+""), (document.right() + document.left())/2 , document.bottom() - 18, 0);
		}
	}
}