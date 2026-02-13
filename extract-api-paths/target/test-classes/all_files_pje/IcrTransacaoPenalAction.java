package br.com.infox.cliente.home.icrrefactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.international.StatusMessage.Severity;
import br.jus.pje.nucleo.entidades.AcompanhamentoCondicaoTransacaoPenal;
import br.jus.pje.nucleo.entidades.CondicaoIcrTransacaoPenal;
import br.jus.pje.nucleo.entidades.IcrTransacaoPenal;
import br.jus.pje.nucleo.entidades.TipoPena;
import br.jus.pje.nucleo.enums.GeneroPenaEnum;
import br.jus.pje.nucleo.enums.OcorrenciaLembreteEnum;
import br.jus.pje.nucleo.enums.SituacaoAcompanhamentoIcrTransacaoPenalEnum;
import br.jus.pje.nucleo.enums.UnidadeMultaEnum;

@Name("icrTransacaoPenalAction")
@Scope(ScopeType.CONVERSATION)
public class IcrTransacaoPenalAction extends
		InformacaoCriminalRelevanteAction<IcrTransacaoPenal, IcrTransacaoPenalManager>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2037446317961343849L;
	private GeneroPenaEnum generoPena;
	private CondicaoIcrTransacaoPenal condicaoIcrTransacaoPenal;
	private AcompanhamentoCondicaoTransacaoPenal acompanhamentoEdit;
	private List<TipoPena> tiposCondicoes = new ArrayList<TipoPena>(0);
	public boolean possuiCondicoes = false;

	private String tab = null;

	public String getTab(){
		return tab;
	}

	public void setTab(String tab){
		this.tab = tab;
	}

	public List<TipoPena> getTiposCondicoes(){
		return tiposCondicoes;
	}

	public void setTiposCondicoes(List<TipoPena> tiposCondicoes){
		this.tiposCondicoes = tiposCondicoes;
	}

	public GeneroPenaEnum getGeneroPena(){
		return generoPena;
	}

	public void setGeneroPena(GeneroPenaEnum generoPena){
		this.generoPena = generoPena;
	}

	public GeneroPenaEnum[] getGeneros(){
		GeneroPenaEnum[] generos = {GeneroPenaEnum.MU, GeneroPenaEnum.RD};
		return generos;
	}

	public void buscarTiposCondicoes(){
		if (getGeneroPena() != null){
			getTiposCondicoes().clear();
			getTiposCondicoes().addAll(getManager().recuperarTiposCondicoes(getGeneroPena()));
			// deixando, apenas, os tipos de pena q ainda nao foram usados
			if (getInstance().getCondicaoIcrTransacaoList() != null){
				for (CondicaoIcrTransacaoPenal aux : getInstance().getCondicaoIcrTransacaoList()){
					getTiposCondicoes().remove(aux.getTipoPena());
				}
			}
		}
		else{
			getTiposCondicoes().clear();
		}
	}

	public List<TipoPena> buscarTiposCondicoes2(){
		List<TipoPena> tipoPenas = getManager().recuperarTiposCondicoes(getGeneroPena());
		// if (getInstance().getCondicaoIcrTransacaoList() != null) {
		// for (CondicaoIcrTransacaoPenal aux : getInstance().getCondicaoIcrTransacaoList()) {
		// tipoPenas.remove(aux.getTipoPena());
		// }
		// }
		return tipoPenas;
	}

	// @Override
	// public void insert() {
	// try {
	// inicializaParaGravacao();
	// // inclusão em lote
	// if (getHome().getReusSelecionados() != null && !getHome().getReusSelecionados().isEmpty()) {
	// List<IcrTransacaoPenal> entityList = new ArrayList<IcrTransacaoPenal>(0);
	// for (ProcessoParte reu : getHome().getReusSelecionados()) {
	// IcrTransacaoPenal newInstance = getEntityClass().newInstance();
	// // PropertyUtils.copyProperties(newInstance, getInstance());
	// newInstance = EntityUtil.cloneEntity(getInstance(), false);
	// newInstance.setProcessoParte(reu);
	// newInstance.setAtivo(true);
	// newInstance.getProcessoEventoList().addAll(getInstance().getProcessoEventoList());
	// for (CondicaoIcrTransacaoPenal aux : getInstance().getCondicaoIcrTransacaoList()) {
	// CondicaoIcrTransacaoPenal condicao = new CondicaoIcrTransacaoPenal();
	// condicao = EntityUtil.cloneEntity(aux, false);
	// condicao.setIcrTransacaoPenal(newInstance);
	// newInstance.getCondicaoIcrTransacaoList().add(condicao);
	// }
	// entityList.add(newInstance);
	// }
	// getManager().persistAll(entityList);
	// }
	// // inclusão simples
	// else {
	// getManager().persist(getInstance());
	// }
	// addMessage(Severity.INFO, "InformacaoCriminalRelevante_created", null);
	// postInsertNavigation();
	// } catch (IcrValidationException e) {
	// addMessage(Severity.ERROR, e.getMessage(), null, e.getParams());
	// } catch (Exception e) {
	// addMessage(Severity.ERROR, e.getMessage(), e);
	// }
	// }

	/*
	 * ==================OPERAÇÕES CONDICÕES========
	 */
	public CondicaoIcrTransacaoPenal getCondicaoIcrTransacaoPenal(){
		return condicaoIcrTransacaoPenal;
	}

	public void setCondicaoIcrTransacaoPenal(CondicaoIcrTransacaoPenal condicaoIcrTransacaoPenal){
		this.condicaoIcrTransacaoPenal = condicaoIcrTransacaoPenal;
		// if (getCondicaoIcrTransacaoPenal() != null && getCondicaoIcrTransacaoPenal().getTipoPena() == null)
		// getCondicaoIcrTransacaoPenal().setTipoPena(getManager().getTipoPena(getCondicaoIcrTransacaoPenal()));
		if (getCondicaoIcrTransacaoPenal() != null && getCondicaoIcrTransacaoPenal().getTipoPena() != null){
			setGeneroPena(getCondicaoIcrTransacaoPenal().getTipoPena().getGeneroPena());
			buscarTiposCondicoes();
		}
	}

	public SituacaoAcompanhamentoIcrTransacaoPenalEnum[] getSituacoesAcompanhamento(){
		return SituacaoAcompanhamentoIcrTransacaoPenalEnum.values();
	}

	public OcorrenciaLembreteEnum[] getOcorrenciasLembrete(){
		return OcorrenciaLembreteEnum.values();
	}

	public void novaCondicao(){
		CondicaoIcrTransacaoPenal nova = new CondicaoIcrTransacaoPenal();
		nova.setIcrTransacaoPenal(getInstance());
		setCondicaoIcrTransacaoPenal(nova);
	}

	public void fecharModalCondicao(){
		setCondicaoIcrTransacaoPenal(null);
	}

	public void vincularCondicao(){
		try{
			getManager().validarCondicao(getCondicaoIcrTransacaoPenal());
			if (getInstance() != null && getInstance().getCondicaoIcrTransacaoList() != null){
				if (getInstance().getCondicaoIcrTransacaoList().contains(getCondicaoIcrTransacaoPenal())){
					getInstance().getCondicaoIcrTransacaoList().remove(getCondicaoIcrTransacaoPenal());
				}
				List<AcompanhamentoCondicaoTransacaoPenal> l = getCondicaoIcrTransacaoPenal().getAcompanhamentos();
				if ((l == null || l.size() == 0) && (getCondicaoIcrTransacaoPenal().getQuantidadeTarefasCumprir() != null && getCondicaoIcrTransacaoPenal().getQuantidadeTarefasCumprir() > 0)) {
					for(int i=0; i<getCondicaoIcrTransacaoPenal().getQuantidadeTarefasCumprir(); i++) {
						AcompanhamentoCondicaoTransacaoPenal a = new AcompanhamentoCondicaoTransacaoPenal();
						a.setCondicaoIcrTransacaoPenal(getCondicaoIcrTransacaoPenal());
						a.setDataCumprimento(getCondicaoIcrTransacaoPenal().getDataTerminoPrevistoAcompanhamento());
						a.setDataPrevista(getCondicaoIcrTransacaoPenal().getDataInicioAcompanhamento());
						a.setNumeroSequencia(i+1);						
						a.setObservacoes(getCondicaoIcrTransacaoPenal().getObservacaoAcompanhamento());
						l.add(a);

					}
				}
				getInstance().getCondicaoIcrTransacaoList().add(getCondicaoIcrTransacaoPenal());
			}
			setCondicaoIcrTransacaoPenal(null);
		} catch (IcrValidationException e){
			addMessage(Severity.ERROR, e.getMessage(), e);
		}
	}

	public void editarCondicao(CondicaoIcrTransacaoPenal condicao){
		if (condicao != null){
			setCondicaoIcrTransacaoPenal(condicao);
		}
	}

	public void removerCondicao(CondicaoIcrTransacaoPenal condicao){
		getInstance().getCondicaoIcrTransacaoList().remove(condicao);
		if (getInstance().getId() != null){
			update();
		}
	}

	public UnidadeMultaEnum[] getUnidadesMulta(){
		return UnidadeMultaEnum.values();
	}

	/*********** OPERAÇÕES DE ACOMPANHAMENTO ***********/
	public void adicionarAcompanhamento(CondicaoIcrTransacaoPenal condicao){
		if (condicao != null){
			setTab("tabAcompanhamento");
			getAcompanhamentoEdit().setCondicaoIcrTransacaoPenal(condicao);
		}
	}
	
	public AcompanhamentoCondicaoTransacaoPenal getAcompanhamentoEdit(){
		if (acompanhamentoEdit == null){
			acompanhamentoEdit = new AcompanhamentoCondicaoTransacaoPenal();
		}
		return acompanhamentoEdit;
	}

	public void setAcompanhamentoEdit(AcompanhamentoCondicaoTransacaoPenal acompanhamentoEdit){
		this.acompanhamentoEdit = acompanhamentoEdit;
	}

	public void carregarAcompanhamentos(){
		acompanhamentoEdit = new AcompanhamentoCondicaoTransacaoPenal();
	}

	public void adicionarTarefa(){
		if (!acompanhamentoEdit.getCondicaoIcrTransacaoPenal().getAcompanhamentos().contains(acompanhamentoEdit)){
			acompanhamentoEdit.getCondicaoIcrTransacaoPenal().getAcompanhamentos().add(acompanhamentoEdit);
			CondicaoIcrTransacaoPenal cond = acompanhamentoEdit.getCondicaoIcrTransacaoPenal();
			List<AcompanhamentoCondicaoTransacaoPenal> l = cond.getAcompanhamentos();
			Integer max = 0;
			Integer tmp = 0;
			for(AcompanhamentoCondicaoTransacaoPenal x: l) {
				tmp = x.getNumeroSequencia();
				if (tmp != null && tmp > max) max = tmp;
			}
			acompanhamentoEdit.setNumeroSequencia(++max);
			novoAcompanhamento();
		}
	}

	public void novoAcompanhamento(){
		CondicaoIcrTransacaoPenal cond = acompanhamentoEdit.getCondicaoIcrTransacaoPenal();
		acompanhamentoEdit = new AcompanhamentoCondicaoTransacaoPenal();
		acompanhamentoEdit.setCondicaoIcrTransacaoPenal(cond);
		
	}

	public void editarAcompanhamento(AcompanhamentoCondicaoTransacaoPenal acompanhamento){
		acompanhamentoEdit = acompanhamento;
	}

	public void removerAcompanhamento(AcompanhamentoCondicaoTransacaoPenal acompanhamento){
		acompanhamento.getCondicaoIcrTransacaoPenal().getAcompanhamentos().remove(acompanhamento);
	}

	@Override
	protected void setDtPublicacao(Date dtPublicacao){
		//
	}

	public List<CondicaoIcrTransacaoPenal> getCondicaoIcrTransacaoList(){
		List<CondicaoIcrTransacaoPenal> l = getInstance().getCondicaoIcrTransacaoList();
		List<CondicaoIcrTransacaoPenal> r = new ArrayList<CondicaoIcrTransacaoPenal>();

		for (CondicaoIcrTransacaoPenal i : l){
			if (i.getDataInicioAcompanhamento() != null){
				r.add(i);
			}
		}

		return r;
	}

	public boolean isPossuiCondicoes() {
		return possuiCondicoes;
	}
	

}
