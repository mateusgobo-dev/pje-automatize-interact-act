/**
 *  pje
 *  Copyright (C) 2013 Conselho Nacional de Justiça
 *
 *  A propriedade intelectual deste programa, tanto quanto a seu código-fonte
 *  quanto a derivação compilada é propriedade da União Federal, dependendo
 *  o uso parcial ou total de autorização expressa do Conselho Nacional de Justiça.
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.pje.manager.ProcessoTrfManager;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.TipoDocumentoIdentificacaoDAO;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.TipoDocumentoIdentificacao;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;

/**
 * Componente de controle negocial da entidade {@link TipoDocumentoIdentificacao}.
 * 
 * @author cristof
 *
 */
@Name("tipoDocumentoIdentificacaoManager")
public class TipoDocumentoIdentificacaoManager extends BaseManager<TipoDocumentoIdentificacao> {
	
	@In
	private TipoDocumentoIdentificacaoDAO tipoDocumentoIdentificacaoDAO;

	/**
     * @return Instância da classe.
     */
    public static TipoDocumentoIdentificacaoManager instance() {
        return ComponentUtil.getComponent(TipoDocumentoIdentificacaoManager.class);
    }
    
	@Override
	protected TipoDocumentoIdentificacaoDAO getDAO() {
		return tipoDocumentoIdentificacaoDAO;
	}
	// importado de br.com.infox.pje.manager.TipoDocumentoIdentificacaoManager
	public List<TipoDocumentoIdentificacao> tipoDocumentoIdentificacaoItems(TipoPessoaEnum tipoPessoa) {
		return tipoDocumentoIdentificacaoDAO.tipoDocumentoIdentificacaoItems(tipoPessoa);
	}
	
	public TipoDocumentoIdentificacao carregarTipoDocumentoIdentificacao(String codigo, TipoPessoaEnum tipoPessoa){
		return tipoDocumentoIdentificacaoDAO.carregarTipoDocumentoIdentificacao(codigo, tipoPessoa);
	}
	
	public TipoDocumentoIdentificacao carregarTipoDocumentoIdentificacao(String codigo){
		return tipoDocumentoIdentificacaoDAO.carregarTipoDocumentoIdentificacao(codigo);
	}
	// fim da importacao;
	
	/**
	 * Método responsável por buscar na base de dados um documento de identificação de uma Pessoa pelo id do documento
	 * 	
	 * @param documentoIdentificacao
	 * @return <code>TipoDocumentoIdentificacao</code>
	 */
	public TipoDocumentoIdentificacao getTipoDocumentoIdentificacaobyDocumento(int documentoIdentificacao){
		return tipoDocumentoIdentificacaoDAO.getTipoDocumentoIdentificacaobyDocumento(documentoIdentificacao);
	}
}
