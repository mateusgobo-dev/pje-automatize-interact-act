package br.jus.cnj.pje.nucleo.manager;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Query;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;

/**
 * Componente criado para isolar o acesso do listener PessoaListener ao carregar os documentos na entidade pessoa
 *
 */
@Name("pessoaListenerComponente")
@AutoCreate
@Scope(ScopeType.EVENT)
public class PessoaListenerComponente implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "pessoaListenerComponente";

	public List<PessoaDocumentoIdentificacao> recuperarDocumentosPessoa(Pessoa p){
		String qStr = "SELECT doc FROM PessoaDocumentoIdentificacao AS doc " +
				" left join fetch doc.tipoDocumento tipo" +
				"	WHERE doc.ativo = true and doc.usadoFalsamente = false" +
				"		AND doc.pessoa = :pessoa ";
		Query query = EntityUtil.createQuery(qStr, true, true, "PessoaListenerComponente.recuperarDocumentosPessoa")
			.setParameter("pessoa", p);
		List<PessoaDocumentoIdentificacao> lista = query.getResultList();
		return lista;
	}
	
}
