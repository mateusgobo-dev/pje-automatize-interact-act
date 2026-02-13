package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.home.SessaoComposicaoOrdemHome;
import br.com.infox.cliente.home.SessaoPautaProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.SessaoProcessoDocumentoVotoDAO;
import br.jus.cnj.pje.entidades.vo.SessaoComposicaoVotoLoteVO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.manager.SessaoPautaProcessoComposicaoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoPautaProcessoTrfManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoVotoManager;
import br.jus.cnj.pje.nucleo.manager.TipoVotoManager;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoComposicao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoVoto;
import br.jus.pje.nucleo.enums.TipoAtuacaoMagistradoEnum;
import br.jus.pje.nucleo.enums.TipoSituacaoPautaEnum;


@Name(PopUpVotoSessaoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class PopUpVotoSessaoAction implements Serializable {
	
	private static final int CONT_INICIAL_VOGAL_COM_RELATOR_E_REVISOR = 2;

	private static final int CONT_INICIAL_VOGAL_COM_RELATOR = 1;

	private static final long serialVersionUID = -1357987721221220761L;
	
	public static final String NAME = "popUpVotoSessaoAction";
	
	private Integer id;
	private SessaoPautaProcessoTrf sessaoPautaProcessoTrf;
	private HashMap<String, Long> placar;
	private SessaoProcessoDocumentoVoto votoRelator;
	private SessaoProcessoDocumento relatorio;
	private Map<Integer,SessaoProcessoDocumentoVoto> mapVotos = new HashMap<Integer, SessaoProcessoDocumentoVoto>(0);
	private Map<Integer,SessaoProcessoDocumento> mapRelatorios = new HashMap<Integer, SessaoProcessoDocumento>(0);
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public SessaoPautaProcessoTrf getSessaoPautaProcessoTrf() {
		return sessaoPautaProcessoTrf;
	}	
	
	public void inicializar() throws Exception {
		if (getId() != null) {
			SessaoPautaProcessoTrfManager sessaoPautaProcessoTrfManager = ComponentUtil.getComponent(SessaoPautaProcessoTrfManager.class);
			sessaoPautaProcessoTrf = sessaoPautaProcessoTrfManager.findById(getId());
		}
		SessaoPautaProcessoTrfHome sessaoPautaProcessoTrfHome = ComponentUtil.getComponent(SessaoPautaProcessoTrfHome.class);
		sessaoPautaProcessoTrfHome.limpaDecisoes();
		SessaoProcessoDocumentoVotoManager sessaoProcessoDocumentoVotoManager = ComponentUtil.getComponent(SessaoProcessoDocumentoVotoManager.class);
		placar = sessaoProcessoDocumentoVotoManager.recuperarPlacar(getSessaoPautaProcessoTrf());
		SessaoProcessoDocumentoVotoDAO sessaoProcessoDocumentoVotoDAO = ComponentUtil.getComponent(SessaoProcessoDocumentoVotoDAO.class);
		votoRelator = sessaoProcessoDocumentoVotoDAO.recuperarVotoDoRelator(getSessaoPautaProcessoTrf());
		List<ProcessoTrf> processos = new ArrayList<ProcessoTrf>(1);
		processos.add(sessaoPautaProcessoTrf.getProcessoTrf());
		List<SessaoProcessoDocumentoVoto> vot = sessaoProcessoDocumentoVotoManager.recuperarVotosPorSessaoEhProcessos(sessaoPautaProcessoTrf.getSessao(),processos);
		List<Integer> idsOrgaoJulgadoresImpedidos = new ArrayList<Integer>(vot.size());
		for(SessaoProcessoDocumentoVoto v : vot){
			if(v.getOrgaoJulgador() != null){
				mapVotos.put(v.getOrgaoJulgador().getIdOrgaoJulgador(),v);
				if (v.getTipoVoto()!=null && "I".equals(v.getTipoVoto().getContexto())){
					idsOrgaoJulgadoresImpedidos.add(v.getOrgaoJulgador().getIdOrgaoJulgador());
				}
			}
		}
		if (idsOrgaoJulgadoresImpedidos.size() > 0){
			getSessaoPautaProcessoComposicaoManager().atualizaVotosImpedidos(idsOrgaoJulgadoresImpedidos,getSessaoPautaProcessoTrf());
		}
		TipoProcessoDocumento tipoRelatorio = ParametroUtil.instance().getTipoProcessoDocumentoRelatorio();
		SessaoProcessoDocumentoManager sessaoProcessoDocumentoManager = ComponentUtil.getComponent(SessaoProcessoDocumentoManager.class);
		List<SessaoProcessoDocumento> relatorios = sessaoProcessoDocumentoManager.getListaSessaoProcessoDocumentoByTipo(sessaoPautaProcessoTrf.getSessao(), tipoRelatorio, processos.get(0).getProcesso());
		for (SessaoProcessoDocumento sessaoProcessoDocumento : relatorios) {
			if (sessaoProcessoDocumento.getOrgaoJulgador()!=null){
				mapRelatorios.put(sessaoProcessoDocumento.getOrgaoJulgador().getIdOrgaoJulgador(), sessaoProcessoDocumento);
			}
		}
		
		tratarVencedorPadrao();
		atualizarProclamacaoJulgamento(sessaoPautaProcessoTrf);
	}
	
	private void tratarVencedorPadrao(){
		if (sessaoPautaProcessoTrf != null && sessaoPautaProcessoTrf.getOrgaoJulgadorVencedor() == null){
			sessaoPautaProcessoTrf.setOrgaoJulgadorVencedor(sessaoPautaProcessoTrf.getProcessoTrf().getOrgaoJulgador());
			try {
				SessaoPautaProcessoTrfManager sessaoPautaProcessoTrfManager = ComponentUtil.getComponent(SessaoPautaProcessoTrfManager.class);
				sessaoPautaProcessoTrf = sessaoPautaProcessoTrfManager.alterar(sessaoPautaProcessoTrf);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public HashMap<String, Long> getPlacar() {		
		return placar;
	}	
	
	public void marcarPresente(SessaoPautaProcessoComposicao sppc) {
		salvarSessaoPautaProcessoComposicao(sppc);
	}
	
	public List<SessaoPautaProcessoComposicao> obterVotantesComposicao(){
		return getSessaoPautaProcessoComposicaoManager().findBySessaoPautaProcessoTrf(getSessaoPautaProcessoTrf(), true);
	}
	
	public void marcarImpedidoSuspeicao(SessaoPautaProcessoComposicao sppc) {			
		atualizaVotoMagistrado(sppc);
		salvarSessaoPautaProcessoComposicao(sppc);
	}
	
	/**
	 * Metodo responsavel por atualizar o voto do magistrado para votacao vogal
	 * @param sessaoPautaProcessoComposicao
	 * @throws PJeBusinessException
	 */
	private void atualizaVotoMagistrado(SessaoPautaProcessoComposicao sppc) {
		try {
			SessaoProcessoDocumentoVoto sessaoProcessoDocumentoVoto = buscarVoto(sppc);
			if (sessaoProcessoDocumentoVoto!=null) {
				if (sppc.getImpedidoSuspeicao()){
					sessaoProcessoDocumentoVoto.setImpedimentoSuspeicao(true);
					TipoVotoManager tipoVotoManager = ComponentUtil.getComponent(TipoVotoManager.class);
					sessaoProcessoDocumentoVoto.setTipoVoto(tipoVotoManager.recuperaImpedido());
				}else{
					sessaoProcessoDocumentoVoto.setImpedimentoSuspeicao(false);
					sessaoProcessoDocumentoVoto.setTipoVoto(null);
				}
				getSessaoProcessoDocumentoManager().persistAndFlush(sessaoProcessoDocumentoVoto);
			}else {
				FacesMessages.instance().add(Severity.WARN, "O sistema não localizou o voto do magistrado");
			}
		} catch (PJeBusinessException e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao persistir objeto sessaoProcessoDocumentoVoto: " + e.getLocalizedMessage());
		}
	}
	
	public void salvarSessaoPautaProcessoComposicao(SessaoPautaProcessoComposicao sppc) {
		try {
			getSessaoPautaProcessoComposicaoManager().salvarComposicaoComDefinicaoDoUsuario(sppc);		}
		catch (Exception e) {
			sppc.setOrdemVotacao(null);
			FacesMessages.instance().add(Severity.ERROR, e.getMessage());
		}
	}

	private SessaoPautaProcessoComposicaoManager getSessaoPautaProcessoComposicaoManager() {
		SessaoPautaProcessoComposicaoManager sessaoPautaProcessoComposicaoManager = ComponentUtil.getComponent(SessaoPautaProcessoComposicaoManager.class);
		return sessaoPautaProcessoComposicaoManager;
	}

	public SessaoProcessoDocumento buscarRelatorio(SessaoPautaProcessoComposicao sppc, TipoProcessoDocumento tipo) {
		return relatorio;
	}
	
	public SessaoProcessoDocumento buscarRelatorio(SessaoPautaProcessoComposicao sppc) {
		if(mapRelatorios.containsKey(sppc.getOrgaoJulgador().getIdOrgaoJulgador())){
			return mapRelatorios.get(sppc.getOrgaoJulgador().getIdOrgaoJulgador());
		}
		return null;
	}
	
	public SessaoProcessoDocumentoVoto buscarVoto(SessaoPautaProcessoComposicao sppc) {
		if(mapVotos.containsKey(sppc.getOrgaoJulgador().getIdOrgaoJulgador())){
			return mapVotos.get(sppc.getOrgaoJulgador().getIdOrgaoJulgador());
		}
		return null;

	}
	
	public void definirRelatorParaAcordao(SessaoPautaProcessoComposicao relatorParaAcordao) {
		if (relatorParaAcordao != null) {
			getSessaoPautaProcessoTrf().setOrgaoJulgadorVencedor(relatorParaAcordao.getOrgaoJulgador());
			atualizarProclamacaoJulgamento(getSessaoPautaProcessoTrf());
		}
	}
	
	private void atualizarProclamacaoJulgamento(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		SessaoProcessoDocumentoVotoManager sessaoProcessoDocumentoVotoManager = 
				ComponentUtil.getComponent(SessaoProcessoDocumentoVotoManager.class);
		
		sessaoPautaProcessoTrf.setProclamacaoDecisao(
				sessaoProcessoDocumentoVotoManager.recuperarTextoProclamacaoJulgamentoAntecipada(sessaoPautaProcessoTrf));
	}
	
	/**
	 * Verifica se o órgão julagdor de um item de composição de pauta de um processo é
	 * o relator do processo.
	 * @param sppc SessaoPautaProcessoComposicao item de composição de pauta de um processo 
	 * @return true caso se tratar do relator do processo.
	 */
	public Boolean isRelatorProcesso(SessaoPautaProcessoComposicao sppc){
		return sppc.getSessaoPautaProcessoTrf().getProcessoTrf().getOrgaoJulgador().equals(sppc.getOrgaoJulgador());
	}
	
	
	public void iniciarVotacao(SessaoPautaProcessoComposicao sppc) {
		ComponentUtil.getComponent(WinVotoAction.class).iniciarVotacao(sppc);	
	}
	
	public SessaoProcessoDocumentoVoto getVotoRelator() {
		return votoRelator;
	}
	
	public void alterarSituacaoParaAguardandoJulgamento() {		
		try {
			SessaoPautaProcessoTrfManager sessaoPautaProcessoTrfManager = ComponentUtil.getComponent(SessaoPautaProcessoTrfManager.class);
			sessaoPautaProcessoTrfManager.alterarSituacaoParaAguardandoJulgamento(getSessaoPautaProcessoTrf());
		}
		catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao realizar a operação, mensagem interna: {0}", e.getMessage());
		}
	}
	
	public void alterarSituacaoParaEmJulgamento() {
		try {
			SessaoPautaProcessoTrfManager sessaoPautaProcessoTrfManager = ComponentUtil.getComponent(SessaoPautaProcessoTrfManager.class);
			sessaoPautaProcessoTrfManager.alterarSituacaoParaEmJulgamento(getSessaoPautaProcessoTrf());

			FacesMessages.instance().add(Severity.INFO, "A situação do processo da pauta foi alterado com sucesso!");
		}
		catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao realizar a operação, mensagem interna: {0}", e.getMessage());
		}
	}
	
	public void marcarPreferencia() {
		try {
			SessaoPautaProcessoTrfManager sessaoPautaProcessoTrfManager = ComponentUtil.getComponent(SessaoPautaProcessoTrfManager.class);
			sessaoPautaProcessoTrfManager.marcarPreferencia(getSessaoPautaProcessoTrf());
		}
		catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao realizar a operação, mensagem interna: {0}", e.getMessage());
		}		
	}
	
	public void adiar() {
		try {
			SessaoPautaProcessoTrfManager sessaoPautaProcessoTrfManager = ComponentUtil.getComponent(SessaoPautaProcessoTrfManager.class);
			sessaoPautaProcessoTrfManager.adiar(getSessaoPautaProcessoTrf());
		}
		catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao realizar a operação, mensagem interna: {0}", e.getMessage());
		}
	}

	public void retirarDePauta() {
		try {
			SessaoPautaProcessoTrfManager sessaoPautaProcessoTrfManager = ComponentUtil.getComponent(SessaoPautaProcessoTrfManager.class);
			sessaoPautaProcessoTrfManager.retirarDePauta(getSessaoPautaProcessoTrf());
		}
		catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao realizar a operação, mensagem interna: {0}", e.getMessage());
		}
	}
	
	public void registrarPedidoSustentacaoOral() {
		SessaoPautaProcessoTrfHome sessaoPautaProcessoTrfHome = ComponentUtil.getComponent(SessaoPautaProcessoTrfHome.class);
		sessaoPautaProcessoTrfHome.setMostraSustentacaoOral(true);
	}
	
	public void registrarPedidoVista() {
		SessaoPautaProcessoTrfHome sessaoPautaProcessoTrfHome = ComponentUtil.getComponent(SessaoPautaProcessoTrfHome.class);
		sessaoPautaProcessoTrfHome.acaoBtnLegenda("pedidoVista", getSessaoPautaProcessoTrf());
	}
	
	private void encerrarVotacaoLote() {
		SessaoPautaProcessoTrfHome sessaoPautaProcessoTrfHome = ComponentUtil.getComponent(SessaoPautaProcessoTrfHome.class);
		sessaoPautaProcessoTrfHome.verificaAction("votacaoLote");
		sessaoPautaProcessoTrfHome.limparVariaveisVotacaoLote();
	}
	
	public void registrarJulgamento() {
		try {					
			SessaoPautaProcessoTrfManager sessaoPautaProcessoTrfManager = ComponentUtil.getComponent(SessaoPautaProcessoTrfManager.class);
			if(Boolean.getBoolean(ParametroUtil.getParametro(Parametros.BLOQUEIO_REGISTO_JULGAMENTO_VOTACAO_IMCOMPLETA).toLowerCase()) && !sessaoPautaProcessoTrfManager.isTodosMagistradosVotantesComVotoRegistrado(getSessaoPautaProcessoTrf())) {
				throw new Exception(MessageFormat.format("Não foram registrados os votos de todos os magistrados da composição do processo {0}.", getSessaoPautaProcessoTrf().getProcessoTrf().getNumeroProcesso()));
			}
			sessaoPautaProcessoTrfManager.registrarJulgamento(getSessaoPautaProcessoTrf());
		}
		catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao realizar a operação, mensagem interna: {0}", e.getMessage());
		}
	}
	
	public void atualizar() {
		try {
			SessaoPautaProcessoTrfManager sessaoPautaProcessoTrfManager = ComponentUtil.getComponent(SessaoPautaProcessoTrfManager.class);
			sessaoPautaProcessoTrf = sessaoPautaProcessoTrfManager.alterar(getSessaoPautaProcessoTrf());
			ComponentUtil.getSessaoProcessoDocumentoVotoManager().atualizarTextoDaProclamacaoDoVotoRelator(sessaoPautaProcessoTrf);
			FacesMessages.instance().add(Severity.INFO, "O processo da pauta foi atualizado com sucesso!");
		}
		catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao realizar a operação, mensagem interna: {0}", e.getMessage());
		}
	}
	
	/**
	 * Obtem o nome do órgão julgador do processo.
	 * @param orgaoJulgador - órgão julgador do qual será obtido o nome.
	 * @return String - nome do órgão julgador do processo.
	 */
	public String obterNomeOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		SessaoPautaProcessoTrfManager sessaoPautaProcessoTrfManager = ComponentUtil.getComponent(SessaoPautaProcessoTrfManager.class);
		return sessaoPautaProcessoTrfManager.obterNomeOrgaoJulgador(sessaoPautaProcessoTrf, orgaoJulgador);
	}

	public SessaoProcessoDocumento getRelatorio() {
		return relatorio;
	}

	public void setRelatorio(SessaoProcessoDocumento relatorio) {
		this.relatorio = relatorio;
	}

	public Map<Integer,SessaoProcessoDocumentoVoto> getMapVotos() {
		return mapVotos;
	}

	public void setMapVotos(Map<Integer,SessaoProcessoDocumentoVoto> mapVotos) {
		this.mapVotos = mapVotos;
	}

	public void removerVoto(SessaoPautaProcessoComposicao s){
		SessaoComposicaoOrdemHome.instance().removerVotoSecretario(s, sessaoPautaProcessoTrf.getIdSessaoPautaProcessoTrf());
		mapVotos.remove(s.getOrgaoJulgador().getIdOrgaoJulgador());
	}

	public void unanime(){
		TipoVotoManager tipoVotoManager = ComponentUtil.getComponent(TipoVotoManager.class);
		TipoVoto acompanhaRelator = tipoVotoManager.recuperaAcompanhaRelator();
		if(sessaoPautaProcessoTrf == null){
			return;
		}
		sessaoPautaProcessoTrf.setOrgaoJulgadorVencedor(sessaoPautaProcessoTrf.getProcessoTrf().getOrgaoJulgador());
		List<SessaoPautaProcessoComposicao> votantes = sessaoPautaProcessoTrf.getComposicoesVotantes();
		SessaoProcessoDocumentoVotoManager sessaoProcessoDocumentoVotoManager = ComponentUtil.getComponent(SessaoProcessoDocumentoVotoManager.class);
		for(SessaoPautaProcessoComposicao votante : votantes){
			if(votante.getOrgaoJulgador().equals(sessaoPautaProcessoTrf.getProcessoTrf().getOrgaoJulgador())){
				continue;
			}
			SessaoProcessoDocumentoVoto voto = montarVoto(votante, acompanhaRelator, votante.getSessaoPautaProcessoTrf().getProcessoTrf().getOrgaoJulgador());			
			sessaoProcessoDocumentoVotoManager.persist(voto);
		}
		try {
			sessaoProcessoDocumentoVotoManager.flush();
		} catch (PJeBusinessException e) {
			e.printStackTrace();
			FacesMessages.instance().add(Severity.ERROR, "Erro ao realizar votação unânime, mensagem interna: {0}", e.getMessage());
		}
	}
	
	public void votacaoLoteUnanime(List<SessaoComposicaoVotoLoteVO> sessaoComposicaoVotoLoteVOList){
		TipoVotoManager tipoVotoManager = ComponentUtil.getComponent(TipoVotoManager.class);
		TipoVoto acompanhaRelator = tipoVotoManager.recuperaAcompanhaRelator();
		OrgaoJulgador orgaoJulgadorRelator = null;
		
		for(SessaoComposicaoVotoLoteVO vo : sessaoComposicaoVotoLoteVOList){
			if(vo.isRelator()){
				orgaoJulgadorRelator = vo.getOrgaoJulgador();
				break;
			}
		}

		for(SessaoComposicaoVotoLoteVO vo : sessaoComposicaoVotoLoteVOList){
			if(!vo.isRelator()){
				vo.setTipoVoto(acompanhaRelator);
				vo.setOrgaoJulgadorAcompanhado(orgaoJulgadorRelator);
			}
		}				
	}
		
	public void registarVotacaoLote(List<SessaoComposicaoVotoLoteVO> sessaoComposicaoVotoLoteVOList, List<SessaoPautaProcessoTrf> sessaoPautaProcessoTrfList){
		SessaoProcessoDocumentoVoto voto = null;
		
		if(!validarVotacaoLote(sessaoComposicaoVotoLoteVOList)){
			return;
		}

		SessaoProcessoDocumentoVotoManager sessaoProcessoDocumentoVotoManager = ComponentUtil.getComponent(SessaoProcessoDocumentoVotoManager.class);
		for(SessaoPautaProcessoTrf sppt : sessaoPautaProcessoTrfList) {
			
			if(TipoSituacaoPautaEnum.AJ.equals(sppt.getSituacaoJulgamento())){
				sppt.setSituacaoJulgamento(TipoSituacaoPautaEnum.EJ);
			}
			for(SessaoPautaProcessoComposicao votante : sppt.getComposicoesVotantes()){
				for(SessaoComposicaoVotoLoteVO vo: sessaoComposicaoVotoLoteVOList){
					if(votante.getOrgaoJulgador().equals(vo.getOrgaoJulgador())){
						voto = montarVoto(votante, vo.getTipoVoto(), vo.getOrgaoJulgadorAcompanhado());
						sessaoProcessoDocumentoVotoManager.persist(voto);
						break;
					}
				}
			}
		}		
		
		try {
			sessaoProcessoDocumentoVotoManager.flush();
			encerrarVotacaoLote();
		} catch (PJeBusinessException e) {
			e.printStackTrace();
			FacesMessages.instance().add(Severity.ERROR, "Erro ao realizar votação em lote, mensagem interna: {0}", e.getMessage());
		}
		
	}
	
	private boolean validarVotacaoLote(List<SessaoComposicaoVotoLoteVO> sessaoComposicaoVotoLoteVOList) {
		FacesMessages.instance().clear();
		for(SessaoComposicaoVotoLoteVO vo : sessaoComposicaoVotoLoteVOList){
			if(vo.getTipoVoto() == null){
				FacesMessages.instance().add(Severity.ERROR, "O campo tipo de voto é obrigatório!");
				return false;
			}
		}
		return true;
	}

	private SessaoProcessoDocumentoVoto montarVoto(SessaoPautaProcessoComposicao votante, TipoVoto tipoVoto, OrgaoJulgador ojAcompanhado){
		SessaoProcessoDocumentoVoto voto = buscarVoto(votante);
		
		if(voto == null){
			voto = new SessaoProcessoDocumentoVoto();
			voto.setProcessoTrf(votante.getSessaoPautaProcessoTrf().getProcessoTrf());
			voto.setSessao(votante.getSessaoPautaProcessoTrf().getSessao());
			voto.setOrgaoJulgador(votante.getOrgaoJulgador());

		}
		voto.setTipoVoto(tipoVoto);
		voto.setDtVoto(new Date());
		voto.setLiberacao(true);
		if(ojAcompanhado != null){
			voto.setOjAcompanhado(ojAcompanhado);
		}else{
			voto.setOjAcompanhado(votante.getOrgaoJulgador());
		}
		voto.setImpedimentoSuspeicao(false);
		
		return voto;
	}

	public Map<Integer, SessaoProcessoDocumento> getMapRelatorios() {
		return mapRelatorios;
	}

	public void setMapRelatorios(Map<Integer, SessaoProcessoDocumento> mapRelatorios) {
		this.mapRelatorios = mapRelatorios;
	}

	/**
	 * Verifica se h, nos autos do processo atual, um documento do tipo 
	 * acrdo assinado para a atual sesso de julgamento.
	 *  
	 * @return Verdadeiro se h, nos autos do processo atual, um documento do tipo 
	 * acrdo assinado para a atual sesso de julgamento. Falso, caso contrrio.
	 */
	public boolean isAcordaoAssinado(){
		SessaoProcessoDocumentoManager sessaoProcessoDocumentoManager = ComponentUtil.getComponent(SessaoProcessoDocumentoManager.class);
		return sessaoProcessoDocumentoManager.isAcordaoAssinado(this.sessaoPautaProcessoTrf.getProcessoTrf(),
				this.sessaoPautaProcessoTrf.getSessao());
	}
	
	/**
	 * Metodo responsvel por montar a lista de ordens de votao
	 * 
	 * @param sppc
	 *            SessaoPautaProcessoComposicao item de composio de pauta de um
	 *            processo
	 * @return
	 */
	public List<SelectItem> getListaOrdemVotacao(SessaoPautaProcessoComposicao sppc) {
		ArrayList<SelectItem> listaOrdemVotacao = new ArrayList<SelectItem>();
		OrgaoJulgador revisor = sppc.getSessaoPautaProcessoTrf().getProcessoTrf().getOrgaoJulgadorRevisor();
		int contador = CONT_INICIAL_VOGAL_COM_RELATOR;
		if (revisor != null) {
			contador = CONT_INICIAL_VOGAL_COM_RELATOR_E_REVISOR;			
		}	
		if (TipoAtuacaoMagistradoEnum.RELAT.equals(sppc.getTipoAtuacaoMagistrado())) {
			listaOrdemVotacao.add(new SelectItem(0, sppc.getTipoAtuacaoMagistrado().getLabel()));
		} else if (TipoAtuacaoMagistradoEnum.REVIS.equals(sppc.getTipoAtuacaoMagistrado())) {
			listaOrdemVotacao.add(new SelectItem(1, sppc.getTipoAtuacaoMagistrado().getLabel()));
		} else {
			while (contador < sppc.getSessaoPautaProcessoTrf().getComposicoesVotantes().size()) {
				SelectItem s = new SelectItem();
				s.setValue(contador);
				if (revisor != null) {
					s.setLabel((contador - 1) + "º " + TipoAtuacaoMagistradoEnum.VOGAL.getLabel());
				} else {
					s.setLabel(contador + "º " + TipoAtuacaoMagistradoEnum.VOGAL.getLabel());
				}
				listaOrdemVotacao.add(s);
				contador++;
			}
		}
		return listaOrdemVotacao;
	}

	public void resetOrdemVotacao() {

		List<SessaoPautaProcessoComposicao> ppcls = sessaoPautaProcessoTrf.getSessaoPautaProcessoComposicaoList();
		for (SessaoPautaProcessoComposicao sessaoPautaProcessoComposicao : ppcls) {
			if (!isRelatorProcesso(sessaoPautaProcessoComposicao)) {
				sessaoPautaProcessoComposicao.setOrdemVotacao(null);
				salvarSessaoPautaProcessoComposicao(sessaoPautaProcessoComposicao);

			}
		}

	}
	/**
	 * Recupera uma lista de  SessaoPautaProcessoComposicao ordenado pelo atributo ordenador
	 * @return
	 */
	public List<SessaoPautaProcessoComposicao> getListaSessaoPautaProcessoComposicaoOrdenado() {

		SessaoPautaProcessoComposicaoManager sessaoPautaProcessoComposicaoManager = ComponentUtil
				.getComponent(SessaoPautaProcessoComposicaoManager.class);
		List<SessaoPautaProcessoComposicao> sessaoPautaProcessoComposicaoList = sessaoPautaProcessoComposicaoManager
				.getListaSessaoPautaProcessoComposicaoOrdenado(sessaoPautaProcessoTrf);

		return sessaoPautaProcessoComposicaoList;
	}

	private SessaoProcessoDocumentoManager getSessaoProcessoDocumentoManager(){
		SessaoProcessoDocumentoManager sessaoProcessoDocumentoManager = ComponentUtil.getComponent(SessaoProcessoDocumentoManager.class); 
		return sessaoProcessoDocumentoManager;
	}
	

}
