package br.jus.jt.estatistica.action;

import java.util.List;

import org.jboss.seam.annotations.Name;

import br.jus.pje.jt.entidades.estatistica.ItemElementar;
import br.jus.pje.jt.entidades.estatistica.ItemQuadro;
import br.jus.pje.jt.entidades.estatistica.RegiaoQuadro;
import br.jus.pje.jt.entidades.estatistica.Relatorio;
import br.jus.pje.jt.entidades.estatistica.action.MontadorLayout;

import com.lowagie.text.html.HtmlEncoder;

@Name(MontadorQuadroPadrao.NAME)
public class MontadorQuadroPadrao implements MontadorLayout {

	public static final String NAME = "montadorQuadroPadrao";
	
	Relatorio relatorio;
	Integer maxColunas;
	
	public String encodeHtml(String texto) {		
		return texto==null ? "" : HtmlEncoder.encode(texto); 		
	}
	
	public String getFormatado(Object objeto) {
		relatorio = (Relatorio) objeto;
		if(relatorio==null) {
			return "";
		}
		
		maxColunas = relatorio.getQuadro().profundidadeItens();
		
		StringBuilder cabecalhoHTML = new StringBuilder("");
		
		// Construção do Cabeçalho		
		String estiloTitulo = "align='center' style='font-size: 9pt; font-weight: bold; font-family: Times;'";
		
		cabecalhoHTML.append( "<P "+estiloTitulo+">");
		cabecalhoHTML.append( encodeHtml("JUSTIÇA DO TRABALHO")+"<BR/>" );
		cabecalhoHTML.append( encodeHtml("BOLETIM ESTATÍSTICO")+"<BR/>" );
		cabecalhoHTML.append( encodeHtml("VARAS DO TRABALHO") );
		cabecalhoHTML.append( "</P>");
		
		cabecalhoHTML.append( "<P align='center' style='font-size: 9pt; font-family: Times;'>");
		cabecalhoHTML.append( "TRT: "+ relatorio.getIdTribunal().toString() );
		cabecalhoHTML.append( " - UF: "+ relatorio.getUf() );
		cabecalhoHTML.append( encodeHtml(" - MUNICÍPIO: ")+ relatorio.getIdMunicipio()+" - " + encodeHtml(relatorio.getMunicipio()) );
		cabecalhoHTML.append( encodeHtml(" - VT: ")+ relatorio.getOrgaoJulgador().getSigla() );
		cabecalhoHTML.append( encodeHtml(" - MÊS/ANO: ")+ relatorio.getPeriodo().getMes().toString()+"/"+relatorio.getPeriodo().getAno().toString()+"<BR/>" );

		/*
		if (relatorio.getOrgaoJulgador().getLocalizacao().getEndereco() != null) {			
			cabecalhoHTML.append( encodeHtml("ENDEREÇO: ")+ encodeHtml(relatorio.getEnderecoCompletoOrgaoJulgador())+"<BR/>" );
		}
		
		StringBuilder contatos = new StringBuilder("");

		String fone = relatorio.getOrgaoJulgador().getNumeroTelefoneFormatado();
		contatos.append( "".equals(fone) ? "" : "FONE: "+ fone+" - " );
		
		String numeroFax = relatorio.getOrgaoJulgador().getNumeroTelefoneFormatado();
		if (numeroFax != null && !"".equals(numeroFax) ) {
			contatos.append( " FAX: "+  "(" + relatorio.getOrgaoJulgador().getDddFax() + ") " + numeroFax+" - " );
		}
		
		String email = relatorio.getOrgaoJulgador().getEmail();
		if(email != null && !"".equals(email.trim()) ) {
			contatos.append( " E-MAIL: "+ encodeHtml(email)+"<BR/>" );
		}
		
		if(contatos != null && !"".equals(contatos.toString().trim()) ) {
			cabecalhoHTML.append( contatos+"<BR/>" );
		}
		*/
		
		cabecalhoHTML.append( "JUIZ TITULAR: "+ encodeHtml(relatorio.getJuizTitular())+"<BR/>" );
		cabecalhoHTML.append( "DIRETOR SECRETARIA: "+ encodeHtml(relatorio.getDiretorSecretaria())+"<BR/>" );

		cabecalhoHTML.append( "</P>" );
		cabecalhoHTML.append( "<P align='center' style='font-size: 9pt; font-weight: bold; font-family: Times;'>");
		
		String descricaoQuadro = relatorio.getQuadro().getDescricao();
		cabecalhoHTML.append( encodeHtml(descricaoQuadro) );
		
		cabecalhoHTML.append( "</P>" );
		
		StringBuilder tabelaHTML = new StringBuilder("");
		
		for (RegiaoQuadro regiaoQuadro : relatorio.getQuadro().getRegioesQuadro()) {
			tabelaHTML.append("<TR>");
			tabelaHTML.append("<TD style='font-size: 9pt; font-weight: bold; font-family: Times;' align='center'"+" colspan='".concat( Integer.valueOf(maxColunas+1).toString() )+"' BGCOLOR='#C0C0C0' >");

			tabelaHTML.append( encodeHtml(regiaoQuadro.getDescricao()) );
			tabelaHTML.append("</TD>");
			tabelaHTML.append("</TR>");			
		
			tabelaHTML.append( montaHtmlFilhos( regiaoQuadro.getItensQuadroNGeracao(1) ) );
		}
		
		// Construção da Página
		StringBuilder paginaHTML = new StringBuilder("");
		
		paginaHTML.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\" >");
		paginaHTML.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
		paginaHTML.append("<head>");    
		paginaHTML.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");
		paginaHTML.append("<title>");
		paginaHTML.append(encodeHtml("Processo Judicial Eletrônico: Estatística para o eGestão"));
		paginaHTML.append("</title>");
		paginaHTML.append("</head>");
		paginaHTML.append("<body>");
		
		tabelaHTML.append("<style type='text/css'>");
		tabelaHTML.append("@page modo_portrait { size : A4 portrait; }");
		tabelaHTML.append("</style>");
	
/*		if( relatorio.getQuadro().getBoletim().getQuadros().size() != 
				relatorio.getQuadro().getOrdem()	) {
			paginaHTML.append("<div style='page: modo_portrait; page-break-after:always;'>");
		} else {
			paginaHTML.append("<div style='page: modo_portrait;'>");
		}		
*/	
		paginaHTML.append("<div style='page: modo_portrait; page-break-after:always; '>");
		
		paginaHTML.append("<TABLE border='1px' width='100%' cellspacing='0' cellpadding='0' >");

		paginaHTML.append("<TR><TD colspan=100>");
		paginaHTML.append(cabecalhoHTML.toString());
		paginaHTML.append("</TD></TR>");
		
		paginaHTML.append(tabelaHTML.toString());
		
		paginaHTML.append("</TABLE>");
	
		paginaHTML.append("</div>");
		
		//tabelaHTML.append("<p style='page-break-after:always;'/>");
		//paginaHTML.append("<p style='page-break-after:always;'/>");
		
		paginaHTML.append("</body>");		
		paginaHTML.append("</html>");
		
		return paginaHTML.toString(); 
	}
	
	private String montaHtmlFilhos(List<ItemQuadro> filhos) {
		StringBuilder fragmentoHTML = new StringBuilder("");
		
		for (ItemQuadro itemQuadro : filhos) {
			
			if (itemQuadro.geracao() == 1) {				
				fragmentoHTML.append("<TR>");				
			}

			fragmentoHTML.append("<TD"+ strColspan(itemQuadro) +" "+ strRowspan(itemQuadro) +" align='left' style='font-size: 8pt; font-family: Times'>");
			fragmentoHTML.append(encodeHtml(itemQuadro.getDescricao()));
			fragmentoHTML.append("</TD>");
			
			if( itemQuadro.getFilhos().size() > 0) { // Cláusula de Indução
				fragmentoHTML.append( montaHtmlFilhos(itemQuadro.getFilhosProximaGeracao()) );
			} else { // Cláusula Base
				if( itemQuadro.isItemElementar() ) {				
					String valor = relatorio.getValorItem( (ItemElementar) itemQuadro );
					valor = (valor==null) ? "" : valor;
					fragmentoHTML.append("<TD  align='right' style='font-size: 8pt; font-family: Times; width:50px'>");
					fragmentoHTML.append(encodeHtml(valor));
					fragmentoHTML.append("</TD>");
					fragmentoHTML.append("</TR>");
				}
			}
			
		}
		
		return fragmentoHTML.toString();
	}

	private String strColspan(ItemQuadro itemQuadro) {
		if( itemQuadro.isItemElementar() ) { 
			Integer span = maxColunas - itemQuadro.geracao() + 1;			
			return span > 0 ? " colspan='".concat( span.toString() ).concat("'") : ""; 
		}
		return "";
	}
	
	private String strRowspan(ItemQuadro itemQuadro) {
		if( !itemQuadro.isItemElementar() ) { 
			Integer span = itemQuadro.qtdElementares();			
			return span > 0 ? " rowspan='".concat( span.toString() ).concat("'") : ""; 
		}
		return "";
	}

}
 

