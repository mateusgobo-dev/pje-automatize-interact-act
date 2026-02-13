package br.com.infox.cliente.home;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.component.suggest.TipoParteSuggestBean;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.entidades.TipoParteConfigClJudicial;
import br.jus.pje.nucleo.enums.APTEnum;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;

@Name(TipoParteClasseJudicialHome.NAME)
@BypassInterceptors
public class TipoParteClasseJudicialHome extends AbstractTipoParteClasseJudicialHome<TipoParteConfigClJudicial> {

	public static final String NAME = "tipoParteClasseJudicialHome";

	private static final long serialVersionUID = 1L;

	private TipoParteSuggestBean getTipoParteSuggestBean() {
		return getComponent("tipoParteSuggest");
	}

	@Override
	public String persist() {
		String persist = super.persist();
		refreshGrid("tipoParteClasseJudicialGrid");
		return persist;
	}

	@Override
	public void newInstance() {
		super.newInstance();
	}

	@Override
	public String update() {
		String update = super.update();
		refreshGrid("tipoParteClasseJudicialGrid");
		newInstance();
		return update;
	}

	@Override
	public String remove(TipoParteConfigClJudicial obj) {
		setInstance(obj);
		return super.remove(obj);
	}

	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		if (isManaged() && changed) {
			if (getInstance().getTipoParteConfiguracao() != null && getInstance().getTipoParteConfiguracao().getTipoParte() != null) {
				getTipoParteSuggestBean().setInstance(getInstance().getTipoParteConfiguracao().getTipoParte());
			}
		}
		if (id == null) {
			getTipoParteSuggestBean().setInstance(null);
		}
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		return super.beforePersistOrUpdate();
	}

	public String getSituacao(String classificacaoParte) {
		return APTEnum.valueOf(classificacaoParte).getLabel();
	}

	@SuppressWarnings("unchecked")
	public List<TipoParte> getTipoPartesRepresentantes(ProcessoParteParticipacaoEnum p, ClasseJudicial cj) {
		String processoParteParticipacao = new String();
		StringBuilder sb = new StringBuilder();
		sb.append("select o.tipoParteConfiguracao.tipoParte from TipoParteConfigClJudicial o ");
		sb.append("where o.tipoParteConfiguracao.tipoParte.ativo = true ");
		sb.append("and o.tipoParteConfiguracao.tipoParte.tipoPrincipal = false ");
		sb.append("and o.classeJudicial = :cj ");
		processoParteParticipacao = montarParteParticipacao(p);
		sb.append(processoParteParticipacao);
		sb.append("order by o.tipoParteConfiguracao.tipoParte.tipoParte");
		Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
		q.setParameter("cj", cj);
		List<TipoParte> tipoPartes = q.getResultList();
		return tipoPartes;
	}
	
	//Método criado em 21/05/2012 por Rafael Barros devido à ISSUE PJEII-1206
	//Retirando a opção de adição de ADVOGADO do polo PASSIVO por ADVOGADO do polo ATIVO
	@SuppressWarnings("unchecked")
	public List<TipoParte> getTipoPartesRepresentantesSemAdvogado(ProcessoParteParticipacaoEnum p, ClasseJudicial cj) {
		String processoParteParticipacao = new String();
		StringBuilder sb = new StringBuilder();
		sb.append("select o.tipoParteConfiguracao.tipoParte from TipoParteConfigClJudicial o ");
		sb.append("where o.tipoParteConfiguracao.tipoParte.ativo = true ");
		sb.append("and o.tipoParteConfiguracao.tipoParte.tipoPrincipal = false ");
		sb.append("and o.tipoParteConfiguracao.tipoParte.idTipoParte <> 7 "); //Retirando o advogado da consulta, devido à issue PJEII-1206		
		sb.append("and o.classeJudicial = :cj ");
		processoParteParticipacao = montarParteParticipacao(p);
		sb.append(processoParteParticipacao);
		sb.append("order by o.tipoParteConfiguracao.tipoParte.tipoParte");
		Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
		q.setParameter("cj", cj);
		List<TipoParte> tipoPartes = q.getResultList();
		return tipoPartes;
	}
	
	/**
	 * Conforme {@link ProcessoParteParticipacaoEnum} monta a query adequada.
	 * @param p
	 * @return
	 */
	private String montarParteParticipacao(ProcessoParteParticipacaoEnum p) {
		String processoParteParticipacao = new String();
		if(p.equals(ProcessoParteParticipacaoEnum.A)){
			processoParteParticipacao = "and o.tipoParteConfiguracao.poloAtivo = true ";
		}else if(p.equals(ProcessoParteParticipacaoEnum.P)){
			processoParteParticipacao = "and o.tipoParteConfiguracao.poloPassivo = true ";
		}else{
			processoParteParticipacao = "and o.tipoParteConfiguracao.outrosParticipantes = true ";
		}
		return processoParteParticipacao;
	}
	
	public static TipoParteClasseJudicialHome instance() {
		return ComponentUtil.getComponent("tipoParteClasseJudicialHome");
	}
}
