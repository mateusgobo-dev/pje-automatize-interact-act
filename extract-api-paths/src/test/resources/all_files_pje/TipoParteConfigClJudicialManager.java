package br.com.infox.pje.manager;

import java.util.List;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.TipoParteConfigClJudicialDAO;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.TipoParteConfigClJudicial;

@Name(TipoParteConfigClJudicialManager.NAME)
@AutoCreate
public class TipoParteConfigClJudicialManager extends BaseManager<TipoParteConfigClJudicial>{

	public static final String NAME = "tipoParteConfigClJudicialManager";
	
	@In
	private TipoParteConfigClJudicialDAO tipoParteConfigClJudicialDAO;

	@Override
	protected TipoParteConfigClJudicialDAO getDAO() {
		return tipoParteConfigClJudicialDAO;
	}
	
	/**
	 * Método responsável por recuperar a configuração de acordo com a
	 * {@link ClasseJudicial}
	 * 
	 * @param classeJudicial
	 *            parâmetro que se deseja verificar se existe configuração
	 * @return <code>List</code> contendo tipos de partes e suas configurações
	 *         da classe judicial passada por parâmetro
	 */
	public List<TipoParteConfigClJudicial> recuperarTipoParteConfiguracao(ClasseJudicial classeJudicial) {
		return getDAO().recuperarTipoParteConfiguracao(classeJudicial);
	}

	/**
	 * Método responsável por verificar se uma configuração de um tipo de parte
	 * para uma classe judicial existe
	 * 
	 * @param tipoParteConfigClJudicial
	 *            parâmetro que se deseja verificar se existe
	 * @return <code>Boolean</code> caso a configuração exista
	 */
	public boolean existeTipoParteConfigClJudicial(TipoParteConfigClJudicial tipoParteConfigClJudicial) {
		return getDAO().existeTipoParteConfigClJudicial(tipoParteConfigClJudicial);
	}
	
}
