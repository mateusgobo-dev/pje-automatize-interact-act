package br.jus.cnj.pje.servicos;

import java.io.Serializable;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import br.jus.cnj.pje.business.dao.VisibilidadePessoaDocumentoIdentificacaoDao;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.VisibilidadePessoaDocumentoIdentificacao;


@Name(VisibilidadePessoaDocumentoIdentificacaoService.NAME)
@Scope(ScopeType.EVENT)
public class VisibilidadePessoaDocumentoIdentificacaoService implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "visibilidadePessoaDocumentoIdentificacaoService";

	@In(create = true) 
	private transient VisibilidadePessoaDocumentoIdentificacaoDao visibilidadePessoaDocumentoIdentificacaoDao;

	public boolean incluirVisibilidade(Pessoa pessoa, PessoaDocumentoIdentificacao documento) {
		if (!verificaSePossuiVisibilidade(pessoa, documento)) {
			VisibilidadePessoaDocumentoIdentificacao visibilidade = new VisibilidadePessoaDocumentoIdentificacao(documento, pessoa);
			visibilidadePessoaDocumentoIdentificacaoDao.persist(visibilidade);
			visibilidadePessoaDocumentoIdentificacaoDao.flush();
			return true;
		} 
		return false;
	}	
	public boolean verificaSePossuiVisibilidade(Pessoa pessoa, PessoaDocumentoIdentificacao documento) {
		return visibilidadePessoaDocumentoIdentificacaoDao.existeVisibilidade(pessoa, documento);
	}
	
	public boolean verificaSeNumeroDoDocumentoEIgualAoDocumentoCadastrado(Pessoa pessoa, PessoaDocumentoIdentificacao documento) {
		return visibilidadePessoaDocumentoIdentificacaoDao.verificaSeDocumentoInformadoIgualAoCadastrado(pessoa, documento);
	}
	
	public boolean verificaSeExisteDocumentoInformado(Pessoa pessoa, PessoaDocumentoIdentificacao documento) {
		return visibilidadePessoaDocumentoIdentificacaoDao.verificaSeExisteDocumentoInformado(pessoa, documento);
	}

	public PessoaDocumentoIdentificacao verificaSeDocumentoJaExistePorNomeTipoNumeroDtExpEOrgExp(Pessoa pessoa, PessoaDocumentoIdentificacao documento) {
		return visibilidadePessoaDocumentoIdentificacaoDao.verificaSeDocumentoJaExistePorNomeTipoNumeroDtExpEOrgExp(pessoa, documento);
	}
	
	public Boolean verificaSeExisteDocumentoAtivoPorTipoPessoa(PessoaDocumentoIdentificacao documento, Pessoa pessoa) {
		return visibilidadePessoaDocumentoIdentificacaoDao.verificaSeExisteDocumentoAtivoPorTipoPessoa(documento, pessoa);
	}
	
	/**
	 * metodo para verificar se o documento é utilizado por outra pessoa
	 * @param documento
	 * @return true se documento é utilizado por outra pesso
	 */
	public Boolean verificaDisponibilidadeNumeroDocumento(PessoaDocumentoIdentificacao documento) {
		return visibilidadePessoaDocumentoIdentificacaoDao.verificaDisponibilidadeNumeroDocumento(documento);
	}
}
