package br.com.infox.cliente.home;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

import br.jus.pje.nucleo.entidades.ProcessoDocumento;

/**
 * [PJEII-18255] Orlando Resende (20/11/2014) Modificação da ordenação dos
 * registros pela data da juntada, inclusão do documento superior, ordem e id.
 * 
 * <p>Essa classe é responsável por implementar um Comparator para ordenar objetos da classe ProcessoDocumento.
 *  <li> 1.	Data de juntada
 *  <li> 2.	Data de inclusão do documento principal se houver, senão do próprio documento.
 *  <li> 3.	Número da ordem, se houver.
 *  <li> 4.	Data de inclusão do documento.
 *  <li> 5.	Id do documento
 *
 * 
 * @author T318523
 *
 */
public class ProcessoDocumentoComparator implements Comparator<ProcessoDocumento>, Serializable {
	private static final long serialVersionUID = 1L;

	@Override
	public int compare(ProcessoDocumento pd1, ProcessoDocumento pd2) {
		int comparacao = 0;
		
		// Compara pela data de juntada
		if ((comparacao = compareDateWithNull(pd1.getDataJuntada(),pd2.getDataJuntada())) != 0) {
			return comparacao;
		}

		Date dataInclusao1 = pd1.getDocumentoPrincipal() != null ? pd1.getDocumentoPrincipal().getDataInclusao() : pd1.getDataInclusao();
		Date dataInclusao2 = pd2.getDocumentoPrincipal() != null ? pd2.getDocumentoPrincipal().getDataInclusao() : pd2.getDataInclusao();

		// Compara pela data de inclusão, preferenciamente pela data de inclusão do documento principal.
		if ((comparacao = compareDateWithNull(dataInclusao1, dataInclusao2)) != 0) {
			return comparacao;
		}
		
		Integer numeroOrdem1 = pd1.getNumeroOrdem() == null ? 0 : pd1.getNumeroOrdem();
		Integer numeroOrdem2 = pd2.getNumeroOrdem() == null ? 0 : pd2.getNumeroOrdem();

		// Compara pelo número da ordem
		if ((comparacao = numeroOrdem1.compareTo(numeroOrdem2)) != 0) {
			return comparacao;
		}

		// Compara pela data de inclusão do documento.
		if ((comparacao = compareDateWithNull(pd1.getDataInclusao(),pd2.getDataInclusao())) != 0) {
			return comparacao;
		}

		// Se tudo for igual, Compara pelo ID.
		return new Integer(pd1.getIdProcessoDocumento()).compareTo(pd2.getIdProcessoDocumento());

	}

	public static int compareDateWithNull(Date objeto1, Date objeto2) {
		if (objeto1 == null && objeto2 == null) {
			return 0;
		} else if (objeto1 == null) {
			return 1;
		} else if (objeto2 == null) {
			return -1;
		} else {
			return objeto1.compareTo(objeto2);
		}

	}

}
