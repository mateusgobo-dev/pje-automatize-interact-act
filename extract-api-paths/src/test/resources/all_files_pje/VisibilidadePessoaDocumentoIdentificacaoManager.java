package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.VisibilidadePessoaDocumentoIdentificacaoDao;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.VisibilidadePessoaDocumentoIdentificacao;

@Name(VisibilidadePessoaDocumentoIdentificacaoManager.NAME)
public class VisibilidadePessoaDocumentoIdentificacaoManager extends BaseManager<VisibilidadePessoaDocumentoIdentificacao>{
	
	public static final String NAME = "visibilidadePessoaDocumentoIdentificacaoManager";
	
	@In
	private VisibilidadePessoaDocumentoIdentificacaoDao visibilidadePessoaDocumentoIdentificacaoDao;

	@Override
	protected VisibilidadePessoaDocumentoIdentificacaoDao getDAO() {
		return visibilidadePessoaDocumentoIdentificacaoDao;
	}

	/**
	 * metodo responsavel por recuperar todas as visibilidades em documentos de identificacao da pessoa passada em parametro.
	 * @param pessoa
	 * @return
	 * @throws Exception 
	 */
	public List<VisibilidadePessoaDocumentoIdentificacao> recuperarVisibilidades(Pessoa pessoa) throws Exception {
		return visibilidadePessoaDocumentoIdentificacaoDao.recuperarVisibilidades(pessoa);
	}

	public VisibilidadePessoaDocumentoIdentificacao recuperarPorId(Long id) {
		return visibilidadePessoaDocumentoIdentificacaoDao.find(id);
	}
}