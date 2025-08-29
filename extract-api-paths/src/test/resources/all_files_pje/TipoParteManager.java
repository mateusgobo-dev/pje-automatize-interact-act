package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.TipoParteDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.entidades.TipoParteConfigClJudicial;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;

@Name(TipoParteManager.NAME)
public class TipoParteManager extends BaseManager<TipoParte>{
	public static final String NAME = "tipoParteManager";
	
	@In
	private TipoParteDAO tipoParteDAO;

	@Override
	protected TipoParteDAO getDAO() {
		return tipoParteDAO;
	}
	
	public List<TipoParte> findByNomeParticipacao(String nomeParticipacao) throws PJeBusinessException{
		return getDAO().findByNomeParticipacao(nomeParticipacao);
	}

	public List<TipoParte> recuperarPorTipoPrincipal(boolean tipoPrincipal,boolean ativo) {
		return getDAO().recuperarPorTipoPrincipal(tipoPrincipal,ativo);
	}

	/**
	 * Método responsável por recuperar o {@link TipoParte} através da
	 * {@link ClasseJudicial}
	 * 
	 * @param classeJudicial
	 *            a classe que se deseja recuperar o tipo da parte
	 * @param parte
	 *            o pólo da parte
	 * @return <code>TipoParte</code> da classe judicial
	 */
	public TipoParte tipoPartePorClasseJudicial(ClasseJudicial classeJudicial, ProcessoParteParticipacaoEnum parte) {
		TipoParte tipoParte = null;
		List<TipoParteConfigClJudicial> tipoParteConfigClJudicial = classeJudicial.getTipoParteConfigClJudicial();
		for (TipoParteConfigClJudicial tipoConfig : tipoParteConfigClJudicial) {
			if (tipoConfig.getTipoParteConfiguracao().getTipoParte().getTipoPrincipal()) {
				if (parte.equals(ProcessoParteParticipacaoEnum.A)) {
					if (tipoConfig.getTipoParteConfiguracao().getPoloAtivo() != null && tipoConfig.getTipoParteConfiguracao().getPoloAtivo()) {
						tipoParte = tipoConfig.getTipoParteConfiguracao().getTipoParte();
						break;
					}
				} else if (parte.equals(ProcessoParteParticipacaoEnum.P)) {
					if (tipoConfig.getTipoParteConfiguracao().getPoloPassivo() != null && tipoConfig.getTipoParteConfiguracao().getPoloPassivo()) {
						tipoParte = tipoConfig.getTipoParteConfiguracao().getTipoParte();
						break;
					}
				} else if (parte.equals(ProcessoParteParticipacaoEnum.T)) {
					if (tipoConfig.getTipoParteConfiguracao().getOutrosParticipantes() != null && tipoConfig.getTipoParteConfiguracao().getOutrosParticipantes()) {
						tipoParte = tipoConfig.getTipoParteConfiguracao().getTipoParte();
						break;
					}
				}
			}
		}
		return tipoParte;
	}

	public TipoParte findById(int id){
		return tipoParteDAO.find(id);
	}

	public boolean isFiscalLei(TipoParte tipoParte) {
		return tipoParte != null && tipoParte.equals((TipoParte)ComponentUtil.getComponent("tipoParteFiscalLei"));
	}

}
