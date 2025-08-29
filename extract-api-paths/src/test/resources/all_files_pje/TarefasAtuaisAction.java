package br.jus.cnj.pje.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.cliente.component.ControleFiltros;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.HibernateUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.TarefaManager;

@Name(TarefasAtuaisAction.NAME)
@Scope(ScopeType.EVENT)
public class TarefasAtuaisAction implements Serializable{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "tarefasAtuaisAction"; 
	
	@In
	private TarefaManager tarefaManager;
	
	private Boolean exibirTarefasAtuais = Boolean.FALSE;
	private String nomeTarefaSelecionada;
	private Integer idProcessoTrfSelecionado;
	private Boolean visibilidadeTarefa;
	
	@Create
	public void init(){
		
	}
	
	public Boolean isTarefaVisivel(String nomeTarefa, Integer idProcessoTrf){
		Boolean tarefaVisivel = Boolean.FALSE;
		List<Integer> idsLocalizacoesFisicas = Authenticator.getIdsLocalizacoesFilhasAtuaisList();
		Integer idOjc = Authenticator.getIdOrgaoJulgadorColegiadoAtual();
		boolean isServidorExclusivoOJC = Authenticator.isServidorExclusivoColegiado();

		if(this.nomeTarefaSelecionada == null && this.idProcessoTrfSelecionado == null){
			this.nomeTarefaSelecionada = nomeTarefa;
			this.idProcessoTrfSelecionado = idProcessoTrf;
			tarefaVisivel = this.tarefaManager.isTarefaVisivel(nomeTarefa, idProcessoTrf, idsLocalizacoesFisicas, idOjc, isServidorExclusivoOJC);
			this.visibilidadeTarefa = tarefaVisivel;
		} else if(nomeTarefa.equalsIgnoreCase(nomeTarefaSelecionada) && idProcessoTrf.compareTo(this.idProcessoTrfSelecionado) == 0){
			tarefaVisivel = this.visibilidadeTarefa;
		} else {
			this.nomeTarefaSelecionada = nomeTarefa;
			tarefaVisivel = this.tarefaManager.isTarefaVisivel(nomeTarefa, idProcessoTrf, idsLocalizacoesFisicas, idOjc, isServidorExclusivoOJC);
			this.visibilidadeTarefa = tarefaVisivel;
		}
		
		return tarefaVisivel;
	}
	
	public List<String> nomesTarefasAtuais(Integer idProcesso) throws PJeBusinessException{
		List<String> listaNomes = new ArrayList<String>(0);
		
		HibernateUtil.disableAllFilters();
		listaNomes = this.tarefaManager.nomesTarefasAtuais(idProcesso);
		ControleFiltros.instance().iniciarFiltro(false, true);
		
		return listaNomes;
	}
	
	public Long getIdTaskInstance(String nomeTarefa, Integer idProcessoTrf){
		return this.tarefaManager.recuperarIdTaskInstanceByNomeTarefaAndIdProcessoTrf(nomeTarefa, idProcessoTrf);
	}
	
	public void toggleTarefasAtuais(){
		this.setExibirTarefasAtuais(!this.exibirTarefasAtuais);
	}
	
	public Boolean getExibirTarefasAtuais() {
		return exibirTarefasAtuais;
	}
	
	public void setExibirTarefasAtuais(Boolean exibirTarefasAtuais) {
		this.exibirTarefasAtuais = exibirTarefasAtuais;
	}
	
	public String getNomeTarefaSelecionada() {
		return nomeTarefaSelecionada;
	}
	
	public void setNomeTarefaSelecionada(String nomeTarefaSelecionada) {
		this.nomeTarefaSelecionada = nomeTarefaSelecionada;
	}
	
	public Integer getIdProcessoTrfSelecionado() {
		return idProcessoTrfSelecionado;
	}
	
	public void setIdProcessoTrfSelecionado(Integer idProcessoTrfSelecionado) {
		this.idProcessoTrfSelecionado = idProcessoTrfSelecionado;
	}
	
	public Boolean getVisibilidadeTarefa() {
		return visibilidadeTarefa;
	}
	
	public void setVisibilidadeTarefa(Boolean visibilidadeTarefa) {
		this.visibilidadeTarefa = visibilidadeTarefa;
	}

}
