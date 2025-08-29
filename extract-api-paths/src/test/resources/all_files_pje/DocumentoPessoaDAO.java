/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.DocumentoPessoa;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;

/**
 * Componente de acesso à entidade {@link DocumentoPessoa}.
 * 
 * @author cristof
 *
 */
@Name("documentoPessoaDAO")
public class DocumentoPessoaDAO extends BaseDAO<DocumentoPessoa> {

	@Override
	public Integer getId(DocumentoPessoa doc) {
		return doc.getIdDocumentoPessoa();
	}

	/**
	 * Método retorna o último termo de compromisso criado pelo advogado
	 * @param pessoaAdvogado
	 * @return Último documento assinado pelo advogado
	 */
	public DocumentoPessoa getUltimoTermoCompromisso(PessoaAdvogado pessoaAdvogado) {
		return getUltimoDocumentoPessoaPorTipo( pessoaAdvogado.getPessoa() , ParametroUtil.instance().getTipoProcessoDocumentoTermoCompromisso());
	}

	/**
	 * Método retorna o último termo de compromisso criado pelo jus postulandi
	 * @param pessoa
	 * @return Último documento criado pelo jus postulandi
	 */
	public DocumentoPessoa getUltimoTermoCompromissoJusPostulandi(Pessoa pessoa) {
		return getUltimoDocumentoPessoaPorTipo( pessoa , ParametroUtil.instance().getTipoProcessoDocumentoTermoCompromissoJusPostulandi());
	}
	
	/**
	 * Método retorna o último documento do tipo informado criado pela pessoa 
	 * @param pessoa
	 * @param tipoProcessoDocumento
	 * @return Último documento do tipo informado criado pela pessoa 
	 */
	public DocumentoPessoa getUltimoDocumentoPessoaPorTipo(Pessoa pessoa, TipoProcessoDocumento tipoProcessoDocumento) {
		Query query = this.getEntityManager().createQuery(
				"select o from DocumentoPessoa o " + "where o.tipoProcessoDocumento = :tipoDocumento "
						+ "and o.pessoa = :user " + "order by o.dataInclusao desc");
		query.setParameter("tipoDocumento", tipoProcessoDocumento);
		query.setParameter("user", pessoa);
		return EntityUtil.getSingleResult(query);
	}

}
