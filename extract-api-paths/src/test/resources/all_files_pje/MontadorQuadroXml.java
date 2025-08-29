package br.jus.jt.estatistica.action;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.jboss.seam.annotations.Name;

import br.jus.pje.jt.entidades.estatistica.ItemElementar;
import br.jus.pje.jt.entidades.estatistica.ItemQuadro;
import br.jus.pje.jt.entidades.estatistica.Regiao;
import br.jus.pje.jt.entidades.estatistica.RegiaoQuadro;
import br.jus.pje.jt.entidades.estatistica.Relatorio;
import br.jus.pje.jt.entidades.estatistica.ValorItem;
import br.jus.pje.jt.entidades.estatistica.action.MontadorLayout;

@Name(MontadorQuadroXml.NAME)
public class MontadorQuadroXml implements MontadorLayout {
	
	public static final String NAME = "montadorQuadroXml";

	Relatorio relatorio;
	
	public String getFormatado(Object objeto) {
        String xml = getDocument(objeto).asXML(); 
        String linhas[] = xml.split("\n");
		return linhas[1]; 
	}
	
	private Document getDocument(Object objeto) {
		relatorio = (Relatorio) objeto;
    	Document document = DocumentHelper.createDocument();
    	Element raizElement = document.addElement( "<***Raiz***>" );  
    	formataElementoDOM(raizElement, objeto);
		return document; 
	}
	
	public Element formataElementoDOM(Element element, Object objeto) {
		relatorio = (Relatorio) objeto;
		return montaElerment(element); 
	}
	
    public void montaCabecalho(Element element, Object objeto) {
		relatorio = (Relatorio) objeto;
		
		String idTrt = relatorio.getIdTribunal();
		String uf = relatorio.getUf();
		String idMunicipio = relatorio.getIdMunicipio().toString();
		String idVara = relatorio.getIdVara().toString();
		String mes = relatorio.getPeriodo().getMes().toString();
		String ano = relatorio.getPeriodo().getAno().toString();
		String presidente = relatorio.getCabecalho().getPresidente();
		String diretor = relatorio.getCabecalho().getDiretor();
		
		/*
		 * [PJEII-5425] PJE-JT: Sérgio Ricardo : PJE-1.4.7 
		 * Adição de novos atributos ao cabeçalho do XML 
		 */										
		String endereco = relatorio.getOrgaoJulgador().getLocalizacao().getEndereco() == null ? "" : relatorio.getOrgaoJulgador().getLocalizacao().getEndereco().getNomeLogradouro();
		String cidade = relatorio.getOrgaoJulgador().getLocalizacao().getEndereco() == null ? "" : relatorio.getOrgaoJulgador().getLocalizacao().getEndereco().getNomeCidade();
		String bairro = relatorio.getOrgaoJulgador().getLocalizacao().getEndereco() == null ? "" : relatorio.getOrgaoJulgador().getLocalizacao().getEndereco().getNomeBairro();
		String cep = relatorio.getOrgaoJulgador().getLocalizacao().getEndereco() == null ? "" : relatorio.getOrgaoJulgador().getLocalizacao().getEndereco().getCep().getNumeroCep();

		String dddFone = relatorio.getOrgaoJulgador().getDddTelefone() == null ? "" : relatorio.getOrgaoJulgador().getDddTelefone();
		String numeroTelefone = relatorio.getOrgaoJulgador().getNumeroTelefone() == null ? "" : relatorio.getOrgaoJulgador().getNumeroTelefone();
		String numeroFax = relatorio.getOrgaoJulgador().getNumeroFax() == null ? "" : relatorio.getOrgaoJulgador().getNumeroFax();
		String email = relatorio.getOrgaoJulgador().getEmail() == null ? "" : relatorio.getOrgaoJulgador().getEmail();
		
		/*
		 * [PJEII-5425] PJE-JT: Sérgio Ricardo : PJE-1.4.7 
		 * FIM DA ALTERAÇÃO 
		 */														
		
		Element varaElement = element.addElement("VARA");
		
		Element trtElement = varaElement.addElement("TRT");
		trtElement.addText(idTrt);
		
		Element ufElement = varaElement.addElement("UF");
		ufElement.addText(uf);
		
		Element municipioElement = varaElement.addElement("MUNICIPIO");
		municipioElement.addText(idMunicipio);
		
		Element varaNumElement = varaElement.addElement("VARA_NUM");
		varaNumElement.addText(idVara);
		
		Element mesElement = varaElement.addElement("MES");
		mesElement.addText(mes);
		
		Element anoElement = varaElement.addElement("ANO");
		anoElement.addText(ano);
		
		/*
		 * [PJEII-5425] PJE-JT: Sérgio Ricardo : PJE-1.4.7 
		 * Adição de novos atributos ao cabeçalho do XML 
		 */												
		Element enderecoElement = varaElement.addElement("ENDERECO");
		enderecoElement.addText(endereco);
		
		Element bairroElement = varaElement.addElement("BAIRRO");
		bairroElement.addText(bairro);
		
		Element cidadeElement = varaElement.addElement("CIDADE");
		cidadeElement.addText(cidade);

		Element cepElement = varaElement.addElement("CEP");
		cepElement.addText(cep);

		Element dddElement = varaElement.addElement("DDD");
		dddElement.addText(dddFone);

		Element telefoneElement = varaElement.addElement("TELEFONES");
		telefoneElement.addText(numeroTelefone);

		Element faxElement = varaElement.addElement("FAX");
		faxElement.addText(numeroFax);

		Element emailElement = varaElement.addElement("EMAIL");
		emailElement.addText(email);

		/*
		 * [PJEII-5425] PJE-JT: Sérgio Ricardo : PJE-1.4.7 
		 * FIM DA ALTERAÇÃO 
		 */				
		
		Element presidentElement = varaElement.addElement("PRESIDENTE");
		presidentElement.addText(presidente);
		
		Element diretorElement = varaElement.addElement("DIRETOR");
		diretorElement.addText(diretor);
		
	}

	private Element montaElerment(Element element) {
		Element quadroElement = element.addElement( relatorio.getQuadro().getNome() );
        for (RegiaoQuadro regiaoQuadro : relatorio.getQuadro().getRegioesQuadro()) {
        	
        	if( regiaoQuadro.getDinamica() ) {
        		Integer qtdProcesso = 0 ;
				for (Regiao regiao : relatorio.getRegioesDinamicas(regiaoQuadro)) {

	        		Element localElement = quadroElement;
	        		
	        		if( !(regiao.getNome().trim().isEmpty() || "*".equals(regiao.getNome().trim())) ) { // Convenção: Nó com nome "*" ou sem nome não participa do XML
	        			/*
	        			 * [PJEII-5425] PJE-JT: Sérgio Ricardo : PJE-1.4.7 
	        			 * Correções do XML para o quadro 11 
	        			 */										
	        			localElement = quadroElement.addElement( regiao.getNome() );
	        		}
	        		
	        		for (ValorItem valorItem : regiao.getValoresItem()) {
	        			if( !("*".equals(valorItem.getItemElementar().getNome().trim())) ) {	        				
	        				Element no = localElement.addElement( valorItem.getItemElementar().getNome() );
	        				String valor = valorItem.getValor();
	        				valor = (valor==null) ? "" : valor;
	        				
	        				/*
	        				 * [PJEII-5425] PJE-JT: Sérgio Ricardo : PJE-1.4.7 
	        				 * Correções do XML para o quadro 11 
	        				 */											        			
	        				String[] valores = valor.split("\\|");			
	        				if (valores.length > 1) {
	        					valor = valores[0];
	        				}			
	        				
	        				no.addText(valor);
	        			}
					}
				
				}
				
/*				Element noTotal = quadroElement.addElement( "Q11_TOTAL_QUANTIDADE" );
				noTotal.addText("0");*/
        		
        	} else {

        		Element localElement = quadroElement;
        		
        		if( !(regiaoQuadro.getNome().trim().isEmpty() || "*".equals(regiaoQuadro.getNome().trim())) ) { // Convenção: Nó com nome "*" ou sem nome não participa do XML
                    localElement = quadroElement.addElement( regiaoQuadro.getNome() );
        		}
        		
				for (ItemQuadro item : regiaoQuadro.getItensQuadroNGeracao(1)) {
					montaNo(localElement, item);
				}
        	}
		}
        return element;
    }
    
	private void montaNo(Element noPai, ItemQuadro item) {
		Element no = noPai;
		
		if (!( item.getNome().trim().isEmpty() || "*".equals(item.getNome().trim()) )) { // Convenção: Nó com nome "*" ou sem nome não participa do XML
			no = noPai.addElement( item.getNome() );
			System.out.println(item.getNome());
		}
		
		if ( item.isItemElementar() ) {	// Cláusula base
			if (!"*".equals(item.getNome().trim())) { // teste que atende à uma especificidade do quadro IV, item 05
				String valor = relatorio.getValorItem( (ItemElementar) item );
				valor = (valor==null) ? "" : valor;
				no.addText(valor);		
				System.out.println(item.getNome()+" = "+valor);
			}
		} else {						// Cláusula de indução
			for (ItemQuadro filho : item.getFilhosProximaGeracao()) {
				montaNo(no, filho);				
			}
		}
	}
    	 
}
 
