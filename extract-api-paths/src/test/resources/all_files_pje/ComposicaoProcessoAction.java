/**
 * pje-web
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.cnj.pje.view;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.manager.PessoaMagistradoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoPautaProcessoComposicaoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoPautaProcessoTrfManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioLocalizacaoMagistradoServidorManager;
import br.jus.cnj.pje.nucleo.service.ComposicaoJulgamentoService;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCargo;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoComposicao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.enums.TipoAtuacaoMagistradoEnum;

/**
 * @author Everton Nogueira
 * Componente de controle do popup (/Painel/SecretarioSessao/popUpComposicaoProcesso.xhtml) do Painel do secretário da sessão.
 */
@Name(ComposicaoProcessoAction.NAME)
@Scope(ScopeType.PAGE)
public class ComposicaoProcessoAction {
	
	public static final String NAME = "composicaoProcessoAction";

	@RequestParameter("idSessaoPTRF")
	private Integer idSessaoPTRF;
	
	private List<SessaoPautaProcessoComposicao> listComposicaoProcesso;
	private SessaoPautaProcessoComposicao participanteComposicao;
	private SessaoPautaProcessoTrf sessaoPautaProcessoTrf;
	
	private boolean alterar;

	private List<OrgaoJulgador> listOrgaoJulgadorColegiadoSessao;
	private List<OrgaoJulgador> listOrgaoJulgadorOutrosColegiados;
	private List<UsuarioLocalizacaoMagistradoServidor> listMagistrados;

	private UsuarioLocalizacaoMagistradoServidor magistradoSelecionado;
	private PessoaMagistrado magistradoPresente;
	private OrgaoJulgador orgaoJulgador;
	
	@PostConstruct
	public void init(){
		setParticipanteComposicao(new SessaoPautaProcessoComposicao());
	}
	
	public void incluir(){
		if(!estaNaComposicao(getParticipanteComposicao().getOrgaoJulgador())){
			getParticipanteComposicao().setSessaoPautaProcessoTrf(getSessaoPautaProcessoTrf());
			getParticipanteComposicao().setTipoAtuacaoMagistrado(calculaTipoAtuacaoNovoParticipante(participanteComposicao));
			getParticipanteComposicao().setMagistradoPresente(getMagistradoPresente());
			getParticipanteComposicao().setCargoAtuacao(getCargoAtuacao());
			getParticipanteComposicao().setImpedidoSuspeicao(Boolean.FALSE);
			getParticipanteComposicao().setDefinidoPorUsuario(Boolean.FALSE);
			setOrgaoJulgador(getParticipanteComposicao().getOrgaoJulgador());
			SessaoPautaProcessoComposicaoManager sessaoPautaProcessoComposicaoManager = ComponentUtil.getComponent(SessaoPautaProcessoComposicaoManager.class);
			sessaoPautaProcessoComposicaoManager.insereComposicaoProcesso(getParticipanteComposicao());
			finalizaAcao("Registro inserido com sucesso!");
		}else{
			FacesMessages.instance().add(Severity.ERROR, "O Órgão Julgador selecionado já está na composição deste processo!");
		}
	}

	private boolean estaNaComposicao(OrgaoJulgador orgaoJulgadorSelecionado) {
		for (SessaoPautaProcessoComposicao sessaoPautaProcessoComposicao : listComposicaoProcesso) {
			if(sessaoPautaProcessoComposicao.getOrgaoJulgador().equals(orgaoJulgadorSelecionado)){
				return true;
			}
		}
		return false;
	}

	public void excluir(SessaoPautaProcessoComposicao composicao){
		try {
			SessaoPautaProcessoComposicaoManager sessaoPautaProcessoComposicaoManager = ComponentUtil.getComponent(SessaoPautaProcessoComposicaoManager.class);
			SessaoPautaProcessoComposicao entidadeDelete = sessaoPautaProcessoComposicaoManager.findById(composicao.getIdSessaoPautaProcessoComposicao());
			sessaoPautaProcessoComposicaoManager.remove(entidadeDelete);
			finalizaAcao("Registro excluído com sucesso!");
		} catch (PJeBusinessException e) {
			FacesMessages.instance().add(Severity.ERROR, e.getMessage());
		}
	}
	
	public void alterar(){
		getParticipanteComposicao().setMagistradoPresente(getMagistradoPresente());
		getParticipanteComposicao().setCargoAtuacao(getCargoAtuacao());
		SessaoPautaProcessoComposicaoManager sessaoPautaProcessoComposicaoManager = ComponentUtil.getComponent(SessaoPautaProcessoComposicaoManager.class);
		setParticipanteComposicao(sessaoPautaProcessoComposicaoManager.updateComposicaoProcesso(getParticipanteComposicao()));
		finalizaAcao("Registro alterado com sucesso!");
		setAlterar(false);
	}
	
	private void finalizaAcao(String mensagem){
		try {
			FacesMessages.instance().add(Severity.INFO, mensagem);
			SessaoPautaProcessoComposicaoManager sessaoPautaProcessoComposicaoManager = ComponentUtil.getComponent(SessaoPautaProcessoComposicaoManager.class);
			sessaoPautaProcessoComposicaoManager.flush();
			limparFormulario();
			listComposicaoProcesso = null;
			listComposicaoProcesso = getListComposicaoProcesso();
			setParticipanteComposicao(new SessaoPautaProcessoComposicao());
		} catch (PJeBusinessException e) {
			FacesMessages.instance().add(Severity.ERROR, e.getMessage());
		}
	}
	
	public void initAlterar(SessaoPautaProcessoComposicao composicao) throws PJeBusinessException{
		SessaoPautaProcessoComposicaoManager sessaoPautaProcessoComposicaoManager = ComponentUtil.getComponent(SessaoPautaProcessoComposicaoManager.class);
		composicao = sessaoPautaProcessoComposicaoManager.findById(composicao.getIdSessaoPautaProcessoComposicao());
		
		setParticipanteComposicao(composicao);
		UsuarioLocalizacaoMagistradoServidorManager usuarioLocalizacaoMagistradoServidorManager = ComponentUtil.getComponent(UsuarioLocalizacaoMagistradoServidorManager.class);
		setMagistradoSelecionado(usuarioLocalizacaoMagistradoServidorManager.obterLocalizacaoAtivaPriorizandoColegiado(composicao.getMagistradoPresente().getIdUsuario(), composicao.getOrgaoJulgador(), composicao.getSessaoPautaProcessoTrf().getSessao().getOrgaoJulgadorColegiado() ));
		setAlterar(true);
	}
	
	
	public void limparFormulario(){
		setParticipanteComposicao(new SessaoPautaProcessoComposicao());
		setMagistradoSelecionado(null);
		setAlterar(Boolean.FALSE);
	}
	
	private TipoAtuacaoMagistradoEnum calculaTipoAtuacaoNovoParticipante(SessaoPautaProcessoComposicao participanteComposicao){
		if(participanteComposicao.getOrgaoJulgador() == null){
			participanteComposicao.setOrgaoJulgador(getOrgaoJulgador());
		}
		ComposicaoJulgamentoService composicaoJulgamentoService = ComponentUtil.getComponent(ComposicaoJulgamentoService.class);
		return composicaoJulgamentoService.obterTipoAtuacaoMagistrado(participanteComposicao.getSessaoPautaProcessoTrf().getProcessoTrf(), participanteComposicao.getOrgaoJulgador());
	}
	
	public PessoaMagistrado getMagistradoPresente() {
		if(magistradoSelecionado != null){ 
			PessoaMagistrado magistrado = null;
			try {
				PessoaMagistradoManager pessoaMagistradoManager = ComponentUtil.getComponent(PessoaMagistradoManager.class);
				magistrado = pessoaMagistradoManager.findById(magistradoSelecionado.getUsuarioLocalizacao().getUsuario().getIdUsuario());
			} catch (PJeBusinessException e) {
				FacesMessages.instance().add(Severity.ERROR, e.getMessage());
			}
			this.magistradoPresente = magistrado;
		}
		return magistradoPresente;
	}
	
	public OrgaoJulgadorCargo getCargoAtuacao() {
		if (magistradoSelecionado != null){
			return magistradoSelecionado.getOrgaoJulgadorCargo();
		}
		return null;
	}
	
	public List<UsuarioLocalizacaoMagistradoServidor> getListMagistrados() {
		if(participanteComposicao.getOrgaoJulgador() != null){
			UsuarioLocalizacaoMagistradoServidorManager usuarioLocalizacaoMagistradoServidorManager = ComponentUtil.getComponent(UsuarioLocalizacaoMagistradoServidorManager.class);
			listMagistrados = usuarioLocalizacaoMagistradoServidorManager.obterLocalizacoesMagistrados(participanteComposicao.getOrgaoJulgador(), null, null, null, null);
		}
		return listMagistrados;
	}
	
	public void setListMagistrados(List<UsuarioLocalizacaoMagistradoServidor> listMagistrados) {
		this.listMagistrados = listMagistrados;
	}
	
	/**
	 * Retorna uma lista de orgãos julgadores, com os orgãos julgadores do colegiado da sessão primeiro.
	 */
	public List<OrgaoJulgador> getListOrgaosJulgadores() {
		List<OrgaoJulgador> list = new ArrayList<OrgaoJulgador>();
		List<OrgaoJulgador> tmp = new ArrayList<OrgaoJulgador>();
		list.addAll(getListOrgaoJulgadorColegiadoSessao());
		tmp.addAll(getListOrgaoJulgadorOutrosColegiados());
		tmp.removeAll(list);
		list.addAll(tmp);
		return list;
	}
	
	/**
	 * Retorna uma lista de Orgãos Julgadores de acordo com o colegiado da sessão.
	 */
	
	public List<OrgaoJulgador> getListOrgaoJulgadorColegiadoSessao() {
		if(listOrgaoJulgadorColegiadoSessao == null){
			if(getSessaoPautaProcessoTrf() != null){
				listOrgaoJulgadorColegiadoSessao = consultarOrgaosColegiadosSessao(getSessaoPautaProcessoTrf());
			}
		}
		return listOrgaoJulgadorColegiadoSessao;
	}
	
	private List<OrgaoJulgador> consultarOrgaosColegiadosSessao(SessaoPautaProcessoTrf processoPautado){
		OrgaoJulgadorManager orgaoJulgadorManager = ComponentUtil.getComponent(OrgaoJulgadorManager.class);
		return orgaoJulgadorManager.orgaosPorColegiado(processoPautado.getSessao().getOrgaoJulgadorColegiado());
	}
	
	public void setListOrgaoJulgadorColegiadoSessao(List<OrgaoJulgador> listOrgaoJulgadorColegiadoSessao) {
		this.listOrgaoJulgadorColegiadoSessao = listOrgaoJulgadorColegiadoSessao;
	}
	
	/**
	 * Retorna uma lista de todos os Orgãos Julgadores ativos.
	 */
	public List<OrgaoJulgador> getListOrgaoJulgadorOutrosColegiados() {
		if(listOrgaoJulgadorOutrosColegiados == null && getListOrgaoJulgadorColegiadoSessao() != null){
			OrgaoJulgadorManager orgaoJulgadorManager = ComponentUtil.getComponent(OrgaoJulgadorManager.class);
			listOrgaoJulgadorOutrosColegiados = orgaoJulgadorManager.findAll();
			listOrgaoJulgadorOutrosColegiados.removeAll(getListOrgaoJulgadorColegiadoSessao());
		}
		return listOrgaoJulgadorOutrosColegiados;
	}
	
	public void setListOrgaoJulgadorOutrosColegiados(List<OrgaoJulgador> listOrgaoJulgadorOutrosColegiados) {
		this.listOrgaoJulgadorOutrosColegiados = listOrgaoJulgadorOutrosColegiados;
	}
	
	public List<SessaoPautaProcessoComposicao> getListComposicaoProcesso() throws PJeBusinessException {
		if(listComposicaoProcesso == null){
			if(getSessaoPautaProcessoTrf() != null){ 
				SessaoPautaProcessoComposicaoManager sessaoPautaProcessoComposicaoManager = ComponentUtil.getComponent(SessaoPautaProcessoComposicaoManager.class);
				listComposicaoProcesso = sessaoPautaProcessoComposicaoManager.findBySessaoPautaProcessoTrf(getSessaoPautaProcessoTrf());
			}
		}
		return listComposicaoProcesso;
	}
	
	public void setListComposicaoProcesso(List<SessaoPautaProcessoComposicao> listComposicaoProcesso) {
		this.listComposicaoProcesso = listComposicaoProcesso;
	}

	public SessaoPautaProcessoComposicao getParticipanteComposicao() {
		return participanteComposicao;
	}

	public void setParticipanteComposicao(SessaoPautaProcessoComposicao participanteComposicao) {
		this.participanteComposicao = participanteComposicao;
	}

	public UsuarioLocalizacaoMagistradoServidor getMagistradoSelecionado() {
		return magistradoSelecionado;
	}

	public void setMagistradoSelecionado(UsuarioLocalizacaoMagistradoServidor magistradoSelecionado) {
		this.magistradoSelecionado = magistradoSelecionado;
	}

	public boolean isAlterar() {
		return alterar;
	}

	public void setAlterar(boolean alterar) {
		this.alterar = alterar;
	}

	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}	
	
	public SessaoPautaProcessoTrf getSessaoPautaProcessoTrf() {
		if(sessaoPautaProcessoTrf == null){
			if(idSessaoPTRF != null){
				SessaoPautaProcessoTrfManager sessaoPautaProcessoTrfManager = ComponentUtil.getComponent(SessaoPautaProcessoTrfManager.class);
				sessaoPautaProcessoTrf = sessaoPautaProcessoTrfManager.getSessaoPautaProcessoTrfByID(idSessaoPTRF);
			}
		}
		return sessaoPautaProcessoTrf;
	}
}
