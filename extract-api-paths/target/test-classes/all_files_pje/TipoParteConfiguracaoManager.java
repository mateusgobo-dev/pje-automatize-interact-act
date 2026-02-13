package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.TipoParteConfiguracaoDAO;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.entidades.TipoParteConfiguracao;

@Name(TipoParteConfiguracaoManager.NAME)
public class TipoParteConfiguracaoManager extends BaseManager<TipoParteConfiguracao>{

	public static final String NAME = "tipoParteConfiguracaoManager";	
	@In 
	TipoParteConfiguracaoDAO tipoParteConfiguracaoDAO; 

	@Override
	protected TipoParteConfiguracaoDAO getDAO() {
		return tipoParteConfiguracaoDAO;
	}
	
	/**
	 * de acordo com a {@link TipoParte} verifica se contem
	 * uma {@link TipoParteConfiguracao}
	 * @return
	 */
	public boolean isContemConfiguracao(TipoParte tipoParte) {
		return getDAO().isContemConfiguracao(tipoParte);
	}

 	/**
	 * Método responsável por recuperar as configurações de um tipo parte caso
	 * ela seja padrão ou não
	 * 
	 * @param tipoParte
	 *            parâmetro que se deseja recuperar as configurações
	 * @param padrao
	 *            caso a configuração seja padrão ou não
	 * @return <code>List</code> contendo as configurações de um tipo parte
	 */
	public List<TipoParteConfiguracao> recuperarPorTipoPartePadrao(TipoParte tipoParte, boolean padrao) {
		return tipoParteConfiguracaoDAO.recuperarPorTipoPartePadrao(tipoParte, padrao);
	}
}
