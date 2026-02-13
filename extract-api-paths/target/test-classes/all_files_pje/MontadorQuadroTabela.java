package br.jus.jt.estatistica.action;

import java.util.List;

import org.jboss.seam.annotations.Name;

import br.jus.pje.jt.entidades.estatistica.ItemElementar;
import br.jus.pje.jt.entidades.estatistica.ItemQuadro;
import br.jus.pje.jt.entidades.estatistica.Regiao;
import br.jus.pje.jt.entidades.estatistica.RegiaoQuadro;
import br.jus.pje.jt.entidades.estatistica.Relatorio;
import br.jus.pje.jt.entidades.estatistica.ValorItem;
import br.jus.pje.jt.entidades.estatistica.action.MontadorLayout;

import com.lowagie.text.html.HtmlEncoder;

@Name(MontadorQuadroTabela.NAME)
public class MontadorQuadroTabela implements MontadorLayout {

	public static final String NAME = "montadorQuadroTabela";
	
	final String DIREITA = "right"; 
	final String ESQUERDA = "left"; 
	
	Relatorio relatorio;
	Integer maxLinhas;
	Integer geracaoAtual;
	Integer maxGeracoes;
	Integer maxColunas;
	
	public String getFormatado(Object objeto) {
		relatorio = (Relatorio) objeto;
		maxLinhas = relatorio.getQuadro().profundidadeItens();
		maxGeracoes = relatorio.getQuadro().maxGeracoes();
		maxColunas = 100;

		geracaoAtual = 1;
		
		String nomeRegiao = relatorio.getQuadro().getTituloRegiao();
		
		StringBuilder cabecalhoHTML = new StringBuilder("");
		
		// Construção do Cabeçalho		
		String estiloTitulo = "align='center' style='font-size: 9pt; font-weight: bold; font-family: Times;'";

		cabecalhoHTML.append("<div style='page: modo_landscape;' >");

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

		cabecalhoHTML.append( "</P>" );
		cabecalhoHTML.append( "<P align='center' style='font-size: 9pt; font-weight: bold; font-family: Times;'>");
		
		String descricaoQuadro = relatorio.getQuadro().getDescricao();
		cabecalhoHTML.append( encodeHtml(descricaoQuadro) );
		
		cabecalhoHTML.append( "</P>" );
		
		cabecalhoHTML.append("</div>");
		
		StringBuilder tabelaHTML = new StringBuilder("");
				
		tabelaHTML.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\" >");
		tabelaHTML.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
		tabelaHTML.append("<head>");    
		tabelaHTML.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");
		tabelaHTML.append("<title>");
		tabelaHTML.append(encodeHtml("Processo Judicial Eletrônico: Estatística para o eGestão"));
		tabelaHTML.append("</title>");
		tabelaHTML.append("</head>");
		tabelaHTML.append("<body>");		
		
		tabelaHTML.append("<style type='text/css'>");
		tabelaHTML.append("@page modo_landscape { size : A4 landscape }");
		tabelaHTML.append("</style>");

		/*if( relatorio.getQuadro().getBoletim().getQuadros().size() != 
				relatorio.getQuadro().getOrdem()	) {
			tabelaHTML.append("<div style='page: modo_landscape; page-break-after:always;'  >");
		} else {
			tabelaHTML.append("<div style='page: modo_landscape;'  >");
		}*/
		tabelaHTML.append("<div style='page: modo_landscape; page-break-after:always;'  >");
		
		tabelaHTML.append("<div style='page: modo_landscape;' >");
	
			tabelaHTML.append("<TABLE border='1' width='145%' cellspacing='0' cellpadding='0' >");
			
			// Cabeçalho
			tabelaHTML.append("<TR><TD colspan=".concat(maxColunas.toString() )+">");
			tabelaHTML.append(cabecalhoHTML);
			tabelaHTML.append("</TD></TR>");
			
			tabelaHTML.append("<TR"+" BGCOLOR='#C0C0C0' >");
			
			if( (nomeRegiao!=null) && !("*".equals(nomeRegiao)) ) {
				tabelaHTML.append("<TD align='center' width:150px' "+" rowspan=".concat( maxLinhas.toString() )+">");
				tabelaHTML.append( nomeRegiao );
				tabelaHTML.append("</TD>");
			}
			
			for (int i = 1; i <= maxGeracoes; i++) {
				if ( relatorio.getQuadro().getRegioesQuadro().size() > 0 ) {
					tabelaHTML.append( montaHtmlGeracao( relatorio.getQuadro().getRegioesQuadro().get(0).getItensQuadroNGeracao(i) ) );
				}
			}
			
			for (RegiaoQuadro regiaoQuadro : relatorio.getQuadro().getRegioesQuadro()) {
				if( regiaoQuadro.getDinamica() ) {
					for (Regiao regiao : relatorio.getRegioes()) {
						if(regiaoQuadro.equals( regiao.getRegiaoQuadro() )) {
							// Regiao
							tabelaHTML.append("<TR>");
							if( (nomeRegiao!=null) && !("*".equals(nomeRegiao)) ) {
								montaCelula(tabelaHTML, regiao.getDescricao(), ESQUERDA);
							}
							// Valores
							tabelaHTML.append( montaValoresFilhos( regiao.getValoresItem() ) );				
							tabelaHTML.append("</TR>");						
						}
					}
				} else {
					// Regiao
					tabelaHTML.append("<TR>");
					if( (nomeRegiao!=null) && !("*".equals(nomeRegiao)) ) {
						montaCelula(tabelaHTML, regiaoQuadro.getDescricao(), ESQUERDA);
					}
					// Valores
					tabelaHTML.append( montaValoresItemFilhos( regiaoQuadro.getItensQuadro() ) );				
					tabelaHTML.append("</TR>");
				}
			}
			
			tabelaHTML.append("</TABLE>");
			
		tabelaHTML.append("</DIV>");

		tabelaHTML.append("</div>");
		
		tabelaHTML.append("</body>");		
		tabelaHTML.append("</html>");		
		
		return tabelaHTML.toString(); 
	}
	
	private String montaValoresFilhos(List<ValorItem> filhos) {
		StringBuilder fragmentoHTML = new StringBuilder("");
		
		for (ValorItem valorItem : filhos) {
			String valor = valorItem.getValor();
			
			// TODO: split do valor
			String[] valores = valor.split("\\|");			
			if (valores.length > 1) {
				valor = valores[valores.length-1];
			}			
			
			montaCelula(fragmentoHTML, valor, DIREITA);		
		}
		
		return fragmentoHTML.toString();
	}
	
	private String montaValoresItemFilhos(List<ItemQuadro> filhos) {
		StringBuilder fragmentoHTML = new StringBuilder("");
		
		for (ItemQuadro itemQuadro : filhos) {
			if( itemQuadro.isItemElementar() ) {
				String valor = relatorio.getValorItem( (ItemElementar) itemQuadro );
				montaCelula(fragmentoHTML, valor, DIREITA);		
			}
		}
		
		return fragmentoHTML.toString();
	}

	private void montaCelula(StringBuilder fragmentoHTML, String valor, String alinhamento) {
		valor = (valor==null) ? "" : valor;
		fragmentoHTML.append("<TD align="+alinhamento+" style='font-size: 8pt; font-family: Times'>");
		fragmentoHTML.append( valor );
		fragmentoHTML.append("</TD>");
	}
	
	private Object montaHtmlGeracao(List<ItemQuadro> itensQuadroNGeracao) {
		StringBuilder fragmentoHTML = new StringBuilder("");
		
		if ( !(itensQuadroNGeracao.size() > 0) ) {
			return "";
		}
		
		for (ItemQuadro itemQuadro : itensQuadroNGeracao) {
			fragmentoHTML.append("<TD"+ strColspan(itemQuadro) +" "+ strRowspan(itemQuadro) +  " BGCOLOR='#C0C0C0' align='center' style='font-size: 9pt; font-family: Times' >");
			fragmentoHTML.append( itemQuadro.getDescricao() );
			fragmentoHTML.append("</TD>");
		}

		fragmentoHTML.append("</TR><TR>");
		
		return fragmentoHTML.toString();
	}
	
	private String strRowspan(ItemQuadro itemQuadro) {
		if( itemQuadro.isItemElementar() ) { 
			Integer span = maxLinhas - itemQuadro.geracao() + 1;			
			return span > 1 ? " rowspan=".concat( span.toString() ) : ""; 
		}
		return "";
	}
	
	private String strColspan(ItemQuadro itemQuadro) {
		if( !itemQuadro.isItemElementar() ) { 
			Integer span = itemQuadro.qtdElementares();			
			return span > 1 ? " colspan=".concat( span.toString() ) : ""; 
		}
		return "";
	}
	
	public String encodeHtml(String texto) {		
		return texto==null ? "" : HtmlEncoder.encode(texto); 		
	}
	
}


