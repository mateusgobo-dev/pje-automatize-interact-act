package br.jus.jt.estatistica.action;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import br.jus.jt.estatistica.list.RelatorioList;
import br.jus.pje.jt.entidades.estatistica.Relatorio;
import br.jus.pje.jt.entidades.estatistica.action.MontadorLayout;

/**
 * @author Sérgio Pacheco / Sérgio Simoes
 * @since 1.4.3
 * @category PJE-JT
 * @class InstanciaBoletim
 * @description Classe que representa uma instancia de um boletim de orgao julgador. 
 */
public class InstanciaBoletim {
	 
	private List<Relatorio> relatorios = new ArrayList<Relatorio>(0);
	private MontadorQuadroXml montadorXML = new MontadorQuadroXml();
	
	public List<Relatorio> getRelatorios() {
		return relatorios;
	}

	public void addRelatorio(Relatorio relatorio) {
		this.relatorios.add(relatorio);
	}
	
	public String getXML() {
    	Document document = DocumentHelper.createDocument();
    	document.setXMLEncoding("ISO-8859-1");
    	document.addDocType("BOLETIM_VARA", "SYSTEM", "http://www3.tst.jus.br/dtd/estatistica_vara.dtd");
    	Element raizElement = document.addElement( "BOLETIM_VARA" );    	
    	if( relatorios.size() > 0) {
    		montadorXML.montaCabecalho(raizElement, relatorios.get(0));
    	} else {
    		return "";
    	}
    	Element boletimElement = raizElement.addElement( "BOLETIM" );    	
    	for (Relatorio relatorio : relatorios) {
    		montadorXML.formataElementoDOM(boletimElement, relatorio);
		}       	
    	return document.asXML();
	}

	public String relatorioXML(RelatorioList relatorioList) {
		String stringXML = "";
		
		if( relatorioList != null ) {
			relatorios = relatorioList.list();
			stringXML = getXML();
		}
		return stringXML;
	}

	
	public String relatorioHtml(RelatorioList relatorioList) {
		String stringHtml = "";
		
		if( relatorioList != null ) {
			relatorios = relatorioList.list();
			stringHtml = getHtml(relatorios);
		}
		return stringHtml;
	}

	private String getHtml(List<Relatorio> relatorios) {
		String str = "";
		for (Relatorio relatorio : relatorios) {
			MontadorLayout montador;
			if( relatorio.getQuadro().getTipoQuadro().toString().equals("P")) {
				montador = new MontadorQuadroPadrao();
			} else if( relatorio.getQuadro().getTipoQuadro().toString().equals("T") ) { 
				montador = new MontadorQuadroTabela();
			} else {
				return "";
			}
			str = str.concat( montador.getFormatado(relatorio) );			
		}
		return str;
	}

}
 
