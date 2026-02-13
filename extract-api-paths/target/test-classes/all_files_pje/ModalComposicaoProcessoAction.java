package br.com.jt.pje.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.exceptions.NegocioException;
import br.com.infox.pje.service.PautaJulgamentoService;
import br.com.jt.pje.manager.ComposicaoProcessoSessaoManager;
import br.com.jt.pje.manager.ComposicaoSessaoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaMagistradoManager;
import br.jus.pje.jt.entidades.ComposicaoProcessoSessao;
import br.jus.pje.jt.entidades.ComposicaoSessao;
import br.jus.pje.jt.entidades.PautaSessao;
import br.jus.pje.jt.entidades.SessaoJT;
import br.jus.pje.jt.enums.SituacaoSessaoEnum;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.ProcessoTrf;


/**
 * Action para o modal que trata a composição do processo em uma sessão
 * @author junior
 */
@Name(ModalComposicaoProcessoAction.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class ModalComposicaoProcessoAction implements Serializable{
	
	private static final long serialVersionUID = -5189451542478126913L;
	public static final String NAME = "modalComposicaoProcessoAction";
	private ProcessoTrf processoTrf;
	private SessaoJT sessao;
	private PautaSessao pautaSessao;
	private ComposicaoSessao composicaoSessao;
	private List<ComposicaoProcessoSessao> listComposicaoProcesso = new ArrayList<ComposicaoProcessoSessao>();
	private ComposicaoProcessoSessao presidenteComposicaoProcesso;
	private Map<Integer, PessoaMagistrado> mapSubstituto = new HashMap<Integer, PessoaMagistrado>();
	
	@In
	protected ComposicaoSessaoManager composicaoSessaoManager;
	@In
	private ComposicaoProcessoSessaoManager composicaoProcessoSessaoManager;
	@In
	private PessoaMagistradoManager pessoaMagistradoManager;
	@In
	protected PautaJulgamentoService pautaJulgamentoService;
	
	public ModalComposicaoProcessoAction carregarComposicao(){
		return this;
	}
	
	public ModalComposicaoProcessoAction doProcesso(ProcessoTrf processoTrf){
		setProcessoTrf(processoTrf);
		return this;
	}
	
	public ModalComposicaoProcessoAction daPauta(PautaSessao pautaSessao){
		setPautaSessao(pautaSessao);
		return this;
	}
	
	public ModalComposicaoProcessoAction naSessao(SessaoJT sessao){
		setSessao(sessao);
		return this;
	}
	
	public void executar(){
		carregarComposicaoProcesso();
	}
	
	public List<ComposicaoSessao> listComposicaoSessaoSemComposicaoProcessoBySessaoProcesso(){
		List<ComposicaoSessao> composicaoSessaoList = composicaoSessaoManager.composicaoSessaoSemComposicaoProcessoBySessaoProcesso(getSessao(), getProcessoTrf());
		List<ComposicaoSessao> composicoes = new ArrayList<ComposicaoSessao>(0);
		boolean remover = false;
		for (ComposicaoSessao composicaoSessao : composicaoSessaoList) {
			for (ComposicaoProcessoSessao composicaoProcesso : listComposicaoProcesso) {
				if(composicaoProcesso.getComposicaoSessao().equals(composicaoSessao)){
					remover = true;
					composicoes.add(composicaoSessao);
				}
			}
		}
		
		if(remover){
			for (ComposicaoSessao composicao : composicoes) {
				composicaoSessaoList.remove(composicao);
			}
		}
		return composicaoSessaoList;
	}
	
	public void incluirComposicaoSessaoNaComposicaoProcesso(){
		if(composicaoSessao != null){
			ComposicaoProcessoSessao composicaoProcessoSessao = new ComposicaoProcessoSessao();
			composicaoProcessoSessao.setComposicaoSessao(composicaoSessao);
			composicaoProcessoSessao.setMagistradoSubstituto(null);
			composicaoProcessoSessao.setPautaSessao(getPautaSessao());
			composicaoProcessoSessao.setPresidente(false);
			
			listComposicaoProcesso.add(composicaoProcessoSessao);
			listComposicaoSessaoSemComposicaoProcessoBySessaoProcesso();
			composicaoSessao = null;
		}
	}
	
	public void removeComposicaoProcesso(ComposicaoProcessoSessao row){
		try {
			listComposicaoProcesso = composicaoProcessoSessaoManager.removerComposicaoProcesso(listComposicaoProcesso, row, getSessao());
		} catch (NegocioException e) { 
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, e.getMensagem());
			e.printStackTrace();
		}
	}
	
	public List<PessoaMagistrado> magistradoSubstitutoItems(OrgaoJulgador orgaoJulgador, ProcessoTrf processoTrf){
		return pessoaMagistradoManager.magistradoSubstitutoItems(orgaoJulgador, getSessao(), processoTrf);
	}
	
	public void atualizarComposicaoProcesso(){
		for (ComposicaoProcessoSessao cps : listComposicaoProcesso) {
			if(cps.getMagistradoSubstituto() == null){
				PessoaMagistrado substituto = mapSubstituto.get(cps.getIdComposicaoProcessoSessao());
				cps.setMagistradoSubstituto(substituto);
			}
		}
		try{
			composicaoProcessoSessaoManager.atualizarComposicaoProcesso(presidenteComposicaoProcesso, listComposicaoProcesso, getSessao());
		} catch (NegocioException e) { 
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, e.getMensagem());
			e.printStackTrace();
		}
	}
	
	private void carregarComposicaoProcesso(){
		listComposicaoProcesso.clear();
		listComposicaoProcesso.addAll(composicaoProcessoSessaoManager.getComposicaoProcessoByProcessoSessao(getProcessoTrf(), getSessao()));
		
		for(ComposicaoProcessoSessao cps: listComposicaoProcesso){
			mapSubstituto.put(cps.getIdComposicaoProcessoSessao(), cps.getMagistradoSubstituto());
			if(cps.getPresidente()){
				setPresidenteComposicaoProcesso(cps);
				break;
			}
		}
	}
	
	public boolean sessaoIniciada(){
		return getSessao()!= null && getSessao().getSituacaoSessao() != null
				&& getSessao().getSituacaoSessao().equals(SituacaoSessaoEnum.I);
	}
	
	public boolean sessaoEncerrada() {
		return pautaJulgamentoService.sessaoEncerrada(getSessao());
	}
	
	public boolean sessaoFechada() {
		return pautaJulgamentoService.sessaoFechada(getSessao());
	}
	
	/*
	 * inicio get e set
	 */

	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	public SessaoJT getSessao() {
		return sessao;
	}

	public void setSessao(SessaoJT sessao) {
		this.sessao = sessao;
	}

	public PautaSessao getPautaSessao() {
		return pautaSessao;
	}

	public void setPautaSessao(PautaSessao pautaSessao) {
		this.pautaSessao = pautaSessao;
	}
	
	public List<ComposicaoProcessoSessao> getListComposicaoProcesso() {
		return listComposicaoProcesso;
	}

	public void setListComposicaoProcesso(List<ComposicaoProcessoSessao> listComposicaoProcesso) {
		this.listComposicaoProcesso = listComposicaoProcesso;
	}
	
	public ComposicaoProcessoSessao getPresidenteComposicaoProcesso() {
		return presidenteComposicaoProcesso;
	}

	public void setPresidenteComposicaoProcesso(ComposicaoProcessoSessao presidenteComposicaoProcesso) {
		this.presidenteComposicaoProcesso = presidenteComposicaoProcesso;
	}

	public ComposicaoSessao getComposicaoSessao() {
		return composicaoSessao;
	}

	public void setComposicaoSessao(ComposicaoSessao composicaoSessao) {
		this.composicaoSessao = composicaoSessao;
	}
}