package br.com.jt.pje.action;

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.util.Strings;

import br.com.infox.DAO.SearchField;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.editor.action.DocumentoAction;
import br.com.infox.editor.action.EditorAction;
import br.com.infox.editor.manager.AnotacaoManager;
import br.com.infox.editor.manager.ProcessoDocumentoEstruturadoManager;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.manager.ProcessoDocumentoTrfLocalManager;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.com.infox.utils.ItensLegendas;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.jt.pje.manager.DocumentoValidacaoHashManager;
import br.com.jt.pje.manager.HistoricoTipoVotoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinPessoaAssinaturaManager;
import br.jus.pje.jt.entidades.AnotacaoVoto;
import br.jus.pje.jt.entidades.DocumentoVoto;
import br.jus.pje.jt.entidades.HistoricoTipoVoto;
import br.jus.pje.jt.entidades.PautaSessao;
import br.jus.pje.jt.entidades.SessaoJT;
import br.jus.pje.jt.entidades.Voto;
import br.jus.pje.jt.enums.SituacaoAnaliseEnum;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.editor.ProcessoDocumentoEstruturado;

@Name(VotoAction.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class VotoAction extends AbstractVotoSessaoAction {
		
	private static final TipoProcessoDocumento TIPO_PROCESSO_DOCUMENTO_ACORDAO = ParametroUtil.instance().getTipoProcessoDocumentoAcordao();
	public static final String NAME = "votoAction";
	private static final long serialVersionUID = 1L;
	
	private boolean situacaoAnalise;
	private Boolean aptoPauta;
	private Boolean votoLiberado;
	private Boolean removerDocumentoEstruturado = false;
	
	private Boolean isVotacaoAntecipada;
	@In
	private ProcessoTrfManager processoTrfManager;
	@In
	private ModalComposicaoProcessoAction modalComposicaoProcessoAction;
	@In
	private ProcessoDocumentoBinPessoaAssinaturaManager processoDocumentoBinPessoaAssinaturaManager;
	@In
	private ProcessoDocumentoEstruturadoManager processoDocumentoEstruturadoManager;
	@In
	private AnotacaoManager anotacaoManager;
	@In
	private HistoricoTipoVotoManager historicoTipoVotoManager;
	@In
	private DocumentoValidacaoHashManager documentoValidacaoHashManager;
	@In
	private ProcessoDocumentoTrfLocalManager processoDocumentoTrfLocalManager;
	
	@Override
	public void inicializar(){
		setProcessoTrf(findById(ProcessoTrf.class, getIdProcesso()));
		
		if(getProcessoTrf().getSelecionadoJulgamento()){
			aptoPauta = false;
		}else if(getProcessoTrf().getSelecionadoPauta()){
			aptoPauta = true;
		}
		
		OrgaoJulgador orgaoJulgadorAtual = Authenticator.getOrgaoJulgadorAtual();
		
		if(getIdPautaSessao() != null){
			setPautaSessao(findById(PautaSessao.class, getIdPautaSessao()));
			setSessao(findById(SessaoJT.class, getPautaSessao().getSessao().getIdSessao()));
			setOrgaoJulgadorRedator(pautaSessaoManager.getOrgaoJulgadorRedatorByProcessoSessao(getProcessoTrf(), getSessao()));
			if(orgaoJulgadorAtual == null){
				setVoto(votoManager.getVotoProcessoByOrgaoJulgadorSessao(getProcessoTrf(), getOrgaoJulgadorRedator(), getSessao()));
				setAnotacaoVoto(anotacaoVotoManager.getAnotacaoVotoSemOJByProcessoSessaoEColegiado(getProcessoTrf(), 
								   getVoto().getSessao(),
								   Authenticator.getOrgaoJulgadorColegiadoAtual()));
				super.inicializar(orgaoJulgadorAtual);
				setSituacaoAnalise(getPautaSessao().getSituacaoAnalise().equals(SituacaoAnaliseEnum.A));
				return;
			}
		}else{
			PautaSessao ps = pautaSessaoManager.getUltimaPautaByProcesso(getProcessoTrf());
			Voto v = votoManager.getUltimoVotoMagistradoByProcessoOrgaoJulgador(getProcessoTrf(), orgaoJulgadorAtual);
			if(ps != null && v != null && v.getSessao() != null){
				setPautaSessao(ps);
				setSessao(ps.getSessao());
			}
		}
		
		super.inicializar();
		
		/* Verifica se está na sessão e não possui voto!
		 * Caso não possua, copia o voto do relator para o OJ 
		 * sem a conclusão e sem liberação! 
		 */
		if(getProcessoTrf() != null && getProcessoTrf().getOrgaoJulgador() != orgaoJulgadorAtual 
				&& getIdPautaSessao() != null 
				&& (getVoto() == null || (getVoto() != null && getVoto().getIdVoto() == 0))){
			try {
				copiarVotoRelator();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
		setVotoLiberado(getVoto() != null && getVoto().getLiberacao());
	}
	
	public void inicializar(PautaSessao pautaSessao){
		if (pautaSessao == null || pautaSessao.getProcessoTrf() == null) {
			throw new NullPointerException("Pauta Sessao ou ProcessoTrf está null!");
		}
		setOrgaoJulgadorVoto(null);
		setIdProcesso(pautaSessao.getProcessoTrf().getIdProcessoTrf());
		setIdPautaSessao(pautaSessao.getIdPautaSessao());
		EditorAction.instance().carregarAcordao(pautaSessao.getProcessoTrf());
		inicializar();
		// replicando comportamento do popupVotoProcesso.page.xml
		// para abrir somente leitura se não for relator
		DocumentoAction documentoAction = ComponentUtil.getComponent(DocumentoAction.NAME);
		if (!this.isRelator()) {
			documentoAction.setAbrirSomenteLeitura(true);
		} else {
			documentoAction.setAbrirSomenteLeitura(false);
		}
	}
	
	@Override
	public void getVotoProcessoByOrgaoJulgadorSessao() {
		super.getVotoProcessoByOrgaoJulgadorSessao();
		setVotoLiberado(getVoto().getLiberacao());
	}
	
	@Override
	public void persist(){
		persist(true);
	}
	
	public void persist(Boolean isDentroFluxo){
		if (isDentroFluxo) {
			marcarAptoPautaJulgamento();
		
			if(marcouParaSessao()){
				lancarMovimentoAptoParaSessao();
			}
		}
		
		super.persist();
		
		if(getPautaSessao() != null){
			atualizaPautaResultadoVotacao();
		}
		
		setVotoLiberado(getVoto().getLiberacao());
		
		construirModeloEstruturado();
		
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, "Registro inserido com sucesso.");
	}
	
	public void persistForaFluxo(){
		persist(false);
	}

	private void marcarAptoPautaJulgamento() {
		if(aptoPauta != null){
			if(aptoPauta){
				getProcessoTrf().setSelecionadoPauta(true);
				getProcessoTrf().setSelecionadoJulgamento(false);
			}else{
				getProcessoTrf().setSelecionadoJulgamento(true);
				getProcessoTrf().setSelecionadoPauta(false);
			}
			// Se outra pessoa alem de magistrado puder marca para pauta ou julgamento 
			// adicionar condição
			getProcessoTrf().setPessoaRelator(((PessoaFisica)Authenticator.getUsuarioLogado()).getPessoaMagistrado());
//			ProcessoJT processoJT = findById(ProcessoJT.class, getProcessoTrf().getIdProcessoTrf());
//			processoJT.setOrgaoJulgadorRelatorOriginario(getProcessoTrf().getOrgaoJulgador());
//			update(processoJT);
		}
	}
	
	private boolean marcouParaSessao(){
		return (getProcessoTrf().getSelecionadoPauta() || getProcessoTrf().getSelecionadoJulgamento()) && !processoTrfManager.isProcessoAptoParaSessao(getProcessoTrf());
	}
	
	private void lancarMovimentoAptoParaSessao(){
		votoManager.lancarMovimentoAptoParaSessao(getProcessoTrf().getProcesso());
	}
	
	@Override
	protected void persistVoto(){
		if(getPautaSessao() != null){
			getVoto().setSessao(getSessao());
		}else{
			PautaSessao pautaSessao = pautaSessaoManager.getPautaSessaoAbertaByProcesso(getProcessoTrf());
			if(pautaSessao != null){
				getVoto().setSessao(pautaSessao.getSessao());
			}
		}
		
		super.persistVoto();
	}
	
	@Override
	public void assinar() {
		update();
		FacesMessages.instance().clear();
		assinarProcessoDocumento();
		FacesMessages.instance().add(Severity.INFO, "Acórdão criado e assinado com sucesso.");
		MagistradoSessaoJulgamentoAction msjc =  br.com.itx.util.ComponentUtil.getComponent("magistradoSessaoJulgamentoAction");
		
    	Boolean processoEstaNaTarefa =  msjc.isProcessoNaTarefa(processoTrf, "idTarefaAssinarAcordao");
		if(processoEstaNaTarefa) {
			msjc.movimentarAposAssinatura(getProcessoTrf());
		}
	}
	
	public void update(){
		update(true);
	}
	
	public void update(Boolean isDentroFluxo){
		if (isDentroFluxo) {
			marcarAptoPautaJulgamento();
		
			if(marcouParaSessao()){
				lancarMovimentoAptoParaSessao();
			}
		} else {
			SecretarioSessaoJulgamentoAction ssja = ComponentUtil.getComponent(SecretarioSessaoJulgamentoAction.NAME);
			ssja.setProcessoTrf(processoTrf);
	    	ssja.updateDispositivosSessaoAndVoto();
		}

		super.update();
		
		if(Authenticator.getOrgaoJulgadorAtual() == null){
			setOrgaoJulgadorRedator(getComposicaoRedator().getComposicaoSessao().getOrgaoJulgador());
			getPautaSessao().setOrgaoJulgadorRedator(getOrgaoJulgadorRedator());
			
			if(getComposicaoRedator().getMagistradoSubstituto() != null){
				getPautaSessao().setMagistradoRedator(getComposicaoRedator().getMagistradoSubstituto());
			}else if(getComposicaoRedator().getComposicaoSessao().getMagistradoSubstituto() != null){
				getPautaSessao().setMagistradoRedator(getComposicaoRedator().getComposicaoSessao().getMagistradoSubstituto());
			}else {
				getPautaSessao().setMagistradoRedator(getComposicaoRedator().getComposicaoSessao().getMagistradoPresente());
			}
			
			pautaSessaoManager.atualizarPauta(situacaoAnalise, getPautaSessao());
		}
		
		//Se existe pauta
		if(getPautaSessao() != null){
			atualizaPautaResultadoVotacao();
		}
		
		setVotoLiberado(getVoto().getLiberacao());
		
		construirModeloEstruturado();
		
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, "Registro alterado com sucesso.");
	}
	
	public void updateForaFluxo(){
		update(false);
	}

	@Override
	protected void atualizarVoto(){
		if(getPautaSessao() != null 
		   && getPautaSessao().getSituacaoAnalise().equals(SituacaoAnaliseEnum.A) 
		   && ocorreuAlteracaoNoVoto())
		{
			getPautaSessao().setDataSituacaoAnalise(new Date());
			getPautaSessao().setUsuarioSituacaoAnalise(Authenticator.getUsuarioLogado());
			pautaSessaoManager.gravarHistoricoAnalise(getPautaSessao());
			getPautaSessao().setSituacaoAnalise(SituacaoAnaliseEnum.R);
			update(getPautaSessao());
		}
		super.atualizarVoto();
 	}

	private void atualizaPautaResultadoVotacao() {
		votoManager.atualizaPautaResultadoVotacao(getProcessoTrf(),getPautaSessao(), composicaoSessaoManager.getOrgaoJulgadorBySessao(getSessao()));
	}
	
	public Voto getUltimoVotoMagistrado(ProcessoTrf processoTrf){
		return votoManager.getUltimoVotoMagistradoByProcessoOrgaoJulgador(processoTrf, Authenticator.getOrgaoJulgadorAtual());
	}
	
	//TODO Remover para um manager
	public boolean existeVotoRelator(PautaSessao pautaSessao){
		OrgaoJulgador orgaoJulgador = Authenticator.getOrgaoJulgadorAtual();
		if(orgaoJulgador != null && orgaoJulgador.equals(pautaSessao.getProcessoTrf().getOrgaoJulgador())){
			return true;
		}
		return votoManager.existeVotoRelatorLiberado(pautaSessao.getProcessoTrf(), pautaSessao.getSessao());
	}
	
	public String[][] getItemsLegenda() {
		if(Authenticator.getOrgaoJulgadorAtual() == null){
			return ItensLegendas.LEGENDAS_ARRAY_VOTO_SECRETARIO;
		}
		return ItensLegendas.LEGENDAS_ARRAY_VOTO;
	}
	
	public void pesquisarProcessos(String sigla) {
		getElaboracaoVotoList().setSearchFieldMap(new HashMap<String, SearchField>());
		getElaboracaoVotoList().addSearchFields();
		if (!Strings.isEmpty(sigla)) {
			if (sigla.equals(ItensLegendas.SIGLAS_LEGENDAS_VOTO[0]) || sigla.equals(ItensLegendas.SIGLAS_LEGENDAS_VOTO[10]) || 
					sigla.equals(ItensLegendas.SIGLAS_LEGENDAS_VOTO[11])) {
				filtroDivergencia(sigla);
			} else if (sigla.equals(ItensLegendas.SIGLAS_LEGENDAS_VOTO[1]) || sigla.equals(ItensLegendas.SIGLAS_LEGENDAS_VOTO[12]) ) { 
				filtroDestaque(sigla);
			} else if (sigla.equals(ItensLegendas.SIGLAS_LEGENDAS_VOTO[13]) || sigla.equals(ItensLegendas.SIGLAS_LEGENDAS_VOTO[14])) {
				filtroAnotacao(sigla);
			} else if (sigla.equals(ItensLegendas.SIGLAS_LEGENDAS_VOTO[5]) || sigla.equals(ItensLegendas.SIGLAS_LEGENDAS_VOTO[6]) 
					|| sigla.equals(ItensLegendas.SIGLAS_LEGENDAS_VOTO[7]) || sigla.equals(ItensLegendas.SIGLAS_LEGENDAS_VOTO[9])){
				filtroVoto(sigla);
			}
			getMapLegenda().put(sigla, !getMapLegenda().get(sigla));
			getElaboracaoVotoList().addSearchFields(getMapLegenda());
		}
	}
	
	@Override
	public void iniciarLegenda(){
		getMapLegenda().put(ItensLegendas.SIGLAS_LEGENDAS_VOTO[0], false);	// Divergência
		getMapLegenda().put(ItensLegendas.SIGLAS_LEGENDAS_VOTO[1], false);	// Destaque
		getMapLegenda().put(ItensLegendas.SIGLAS_LEGENDAS_VOTO[3], false);	// Preferência
		getMapLegenda().put(ItensLegendas.SIGLAS_LEGENDAS_VOTO[4], false);	// Sustentação oral
		getMapLegenda().put(ItensLegendas.SIGLAS_LEGENDAS_VOTO[5], false);	// Voto não elaborado 
		getMapLegenda().put(ItensLegendas.SIGLAS_LEGENDAS_VOTO[6], false);	// Voto elaborado e não liberado
		getMapLegenda().put(ItensLegendas.SIGLAS_LEGENDAS_VOTO[7], false);	// Voto elaborado e liberado
		getMapLegenda().put(ItensLegendas.SIGLAS_LEGENDAS_VOTO[8], false);	// Não analisado
		getMapLegenda().put(ItensLegendas.SIGLAS_LEGENDAS_VOTO[9], false);	// Voto do relator não liberado
		
		/**
		 * (fernando.junior - 17/01/2013) Adição de novas legendas
		 */
		getMapLegenda().put(ItensLegendas.SIGLAS_LEGENDAS_VOTO[10], false);	// Divergência com análise pendente
		getMapLegenda().put(ItensLegendas.SIGLAS_LEGENDAS_VOTO[11], false);	// Divergência não concluída/liberada
		getMapLegenda().put(ItensLegendas.SIGLAS_LEGENDAS_VOTO[12], false);	// Destaque não concluído/liberado
		getMapLegenda().put(ItensLegendas.SIGLAS_LEGENDAS_VOTO[13], false);	// Anotação
		getMapLegenda().put(ItensLegendas.SIGLAS_LEGENDAS_VOTO[14], false);	// Anotação não concluída
	}
	
	public void removeVotos(ProcessoTrf processoTrf) {
		if (processoTrf == null){
			FacesMessages.instance().add(Severity.ERROR, "Favor escolher o processo.");
			return;
		}
		List<Voto> votosByProcesso = votoManager.getVotosByProcesso(processoTrf);
		if (votosByProcesso != null) {
			for (Voto voto : votosByProcesso){
				List<DocumentoVoto> documentoVotoByVoto = documentoVotoManager.getDocumentoVotoByVoto(voto);
				if (documentoVotoByVoto != null) {
					for (DocumentoVoto documentoVoto : documentoVotoByVoto){
						if(documentoVoto.getTipoProcessoDocumento().getIdTipoProcessoDocumento() != ParametroUtil.instance().getTipoProcessoDocumentoAcordao().getIdTipoProcessoDocumento()) {
							/*
							 * [PJEII-7131] Removendo registros de documentos de votos que possam restar nas tabelas tb_doc_validacao_hash e tb_processo_documento_trf.
							 * Possivemente os registros eram inseridos erroneamente em versões anteriores.
							 */
							documentoValidacaoHashManager.removerDaTabelaDocumentoValidacaoHash(documentoVoto);
							processoDocumentoTrfLocalManager.removerDaTabelaProcessoDocumentoTrf(documentoVoto);
							
							documentoVotoManager.remove(documentoVoto);
						} else {
							documentoVotoManager.removerDaTabelaDocumentoVoto(documentoVoto);
						}
					}
				}

				/*
				 * [PJEII-4692] Bruno R. A. Sales. Alterações feitas pela JT: ao remover um voto, o sistema não estava removendo as anotações (AnotacaoVoto) relacionadas. Isso estava causando problemas
				 * na inserção de um novo voto/anotação (já existia uma anotação para o processo na sessão).
				 */
				// Remove as anotações dos votos a serem removidos.
				SessaoJT sessao = voto.getSessao();				
				List<AnotacaoVoto> anotacoesByProcessoSessaoOrgaoJulgadorEColegiado = anotacaoVotoManager.getAnotacoesBySessaoProcesso(sessao, processoTrf);
				if(anotacoesByProcessoSessaoOrgaoJulgadorEColegiado != null){
					for (AnotacaoVoto anotacaoVoto : anotacoesByProcessoSessaoOrgaoJulgadorEColegiado) {
						anotacaoVotoManager.remove(anotacaoVoto);
					}
				}
				/* [PJEII-4692] Fim. */
				
				/*
				 * [PJEII-5051] David Vieira; Alterações feitas pela JT: ao remover um voto, o sistema não estava removendo as anotações (AnotacaoVoto) relacionadas. Isso estava causando problemas
				 * na inserção de um novo voto/anotação (já existia uma anotação para o processo na sessão).
				 */
				List<HistoricoTipoVoto> allHistoricoTipoVoto = historicoTipoVotoManager.getAllHistoricoTipoVoto(voto);
				if(allHistoricoTipoVoto != null){
					for (HistoricoTipoVoto historicoTipoVoto : allHistoricoTipoVoto) {
						historicoTipoVotoManager.remove(historicoTipoVoto);
					}
				}
				
				/* [PJEII-5051] Fim. */
				
				
				votoManager.remove(voto);
			}
		}
		processoTrf.setSelecionadoPauta(false);
		processoTrf.setSelecionadoJulgamento(false);
		processoTrfManager.update(processoTrf);
		FacesMessages.instance().add(Severity.INFO, MessageFormat.format("Votos do processo ''{0}'' removidos com sucesso !", processoTrf.getNumeroProcesso()));
		
		if (this.removerDocumentoEstruturado) {
			/**
		 	* PJE-JT Antonio Lucas PJEII-5787
		 	* remove apenas a parte estruturada do documento, 
		 	* pois o sistema estava tentando carregar o documento antigo quando na verdade deveria criar um novo
		 	*/
			processoDocumentoEstruturadoManager.removerApenasParteEstruturadaDoUltimoProcessoDocumentoEstruturadoAssociadoAoProcessoTrf(processoTrf);
		}
	}
	
	public void desmarcarAptoPautaJulgamento(){
		setAptoPauta(null);
	}
	
	public void validarMarcacaoAptoAoLiberarVoto(){
		if(marcouParaLiberarVoto() && aptoPauta == null){
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, "Para liberar o acesso ao voto deve informar se o processo está Apto para Pauta ou Apto para Julgamento.");
			getVoto().setLiberacao(false);
			votoLiberado = false;
		}
		else {
			votoLiberado = true;
		}
	}

	private boolean marcouParaLiberarVoto() {
		return getVoto() != null && getVoto().getLiberacao();
	}
	
	public void copiarVotoRelator() throws InstantiationException, IllegalAccessException{
		Voto votoRelator = votoManager.getUltimoVotoByOrgaoJulgadorProcessoSessao(getProcessoTrf().getOrgaoJulgador(), getProcessoTrf(), getSessao());
		Voto novoVoto = EntityUtil.cloneEntity(votoRelator, false);
		novoVoto.setTipoVoto(null);
		novoVoto.setLiberacao(false);
		novoVoto.setOrgaoJulgador(Authenticator.getOrgaoJulgadorAtual());
		novoVoto.setDataInclusao(new Date());
		persist(novoVoto);
		
		documentoVotoManager.copiarDocumentos(votoRelator, novoVoto);
		
		setVoto(novoVoto);
		carregarDocumentosDoVoto();
		carregarModelosDeDocumentos();
		construirModeloIntegra();
		
		setOrgaoJulgadorVoto(getVoto().getOrgaoJulgador());
		FacesMessages.instance().clear();
	}
	
	public void carregaModalComposicaoProcesso(){
		modalComposicaoProcessoAction.carregarComposicao()
									 	.doProcesso(getProcessoTrf())
									 	.daPauta(getPautaSessao())
									 	.naSessao(getSessao())
									 .executar();
	}
	
	/**
	 * <html>
	 * Somente &eacute; permitido criar novo voto em um processo que j&aacute; possui voto se o usu&aacute;rio
	 * for Redator do processo e j&aacute; existir um ac&oacute;rd&atilde;o assinado
	 * </html>
	 */
	public boolean podeCriarNovoVoto(){
		return getVoto() != null  
				&& getIdPautaSessao() == null
				&& isRedatorDoProcesso() 
				&& existeAcordaoPublicadoNoProcesso();
	}
	
	public boolean podeReformarAcordao(){
		return getVoto() != null  
				&& isRedatorDoProcesso() 
				&& existeAcordaoPublicadoNoProcesso();
	}

	private boolean isRedatorDoProcesso() {
		return getPautaSessao() != null 
				&& getOrgaoJulgadorRedator() != null 
				&& getOrgaoJulgadorRedator().getIdOrgaoJulgador() == getPautaSessao().getOrgaoJulgadorRedator().getIdOrgaoJulgador();
	}
	
	private boolean existeAcordaoPublicadoNoProcesso(){
		return processoDocumentoManager.isAcordaoPublicado(ultimoAcordaoAssinado(getProcessoTrf(), getVoto()));
	}
	
	public void persistNovoVoto(){
		try {
			Voto novoVoto = EntityUtil.cloneEntity(getVoto(), false);
			novoVoto.getProcessoTrf().setSelecionadoPauta(false);
			novoVoto.getProcessoTrf().setSelecionadoJulgamento(false);
			novoVoto.setLiberacao(false);
			novoVoto.setDataInclusao(new Date());
			novoVoto.setSessao(null);
			persist(novoVoto);
			
			documentoVotoManager.copiarDocumentos(getVoto(), novoVoto);
			
			setIdPautaSessao(null);
			setPautaSessao(null);
			setSessao(null);
			
			inicializar();
		} catch (Exception e) {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, "Não foi possível criar um novo voto!");
			e.printStackTrace();
		} 
		
	}
	
	public ProcessoDocumento ultimoAcordaoAssinado(ProcessoTrf processoTrf){
		return processoDocumentoManager.getUltimoProcessoDocumentoAssinado(TIPO_PROCESSO_DOCUMENTO_ACORDAO, processoTrf.getProcesso());
	}
	
	public ProcessoDocumento ultimoAcordaoAssinado(ProcessoTrf processoTrf, Voto voto){
		return documentoVotoManager.getUltimoDocumentoVotoAssinado(TIPO_PROCESSO_DOCUMENTO_ACORDAO, processoTrf.getProcesso(), voto);
	}
	
	public ProcessoDocumento ultimoAcordaoPublicado(ProcessoTrf processoTrf){
		return processoDocumentoManager.getUltimoAcordaoPublicadoDejt(processoTrf);
	} 
	
	public String getPessoasAssinaturaAcordao(){
		ProcessoDocumento ultimoAcordaoAssinado = ultimoAcordaoAssinado(getProcessoTrf(), getVoto());
		if(ultimoAcordaoAssinado != null){
			List<Pessoa> list = processoDocumentoBinPessoaAssinaturaManager.listaPessoasAssinaramDocumento(ultimoAcordaoAssinado.getProcessoDocumentoBin());
			StringBuilder sb = new StringBuilder();
			for(Pessoa p: list){
				if(sb.toString().isEmpty()){
					sb.append(p.getNome());
				}else{
					sb.append(", ");
					sb.append(p.getNome());
				}
			}
			return sb.toString();
		}
		return "";
	}
	
	/*
	 * inicio dos get e set
	*/
	
	public boolean isSituacaoAnalise() {
		return situacaoAnalise;
	}

	public void setSituacaoAnalise(boolean situacaoAnalise) {
		this.situacaoAnalise = situacaoAnalise;
	}

	public Boolean getAptoPauta() {
		return aptoPauta;
	}

	public void setAptoPauta(Boolean aptoPauta) {
		this.aptoPauta = aptoPauta;
	}

	public Boolean getVotoLiberado() {
		return votoLiberado;
	}

	public void setVotoLiberado(Boolean votoLiberado) {
		this.votoLiberado = votoLiberado;
	}

	public void carregarProcesso(Integer idProcesso) {
		super.setIdProcesso(idProcesso);
		this.inicializar();
	}
	
	public boolean isRelator() {
		if (Authenticator.getOrgaoJulgadorAtual() != null) {
			return Authenticator.getOrgaoJulgadorAtual().equals(getProcessoTrf().getOrgaoJulgador());
		} else {
			return false;
		}
	}
	
	/**
	 * Verifica a existência de divergências para o documento, retornando um nº indicando o tipo de divergência encontrada (se houver):
	 * 0 - O documento não possui divergências
	 * 1 - Divergência pendente
	 * 2 - Divergência não concluída/liberada
	 * 3 - Divergência
	 * 
	 * @author fernando.junior (17/01/2013) 
	 * 
	 * [PJEII-5293] Método alterado para que verificasse as divergências pelo documento, não pelos tópicos.
	 * @author fernando.junior (05/02/2013) 
	 */
	public int existeDivergencia(int idProcessoTrf) {
		ProcessoDocumentoEstruturado documento = processoDocumentoEstruturadoManager.getUltimoAcordaoEstruturadoByIdProcessoTrf(idProcessoTrf);
		
		if ( documento != null && documento.getIdProcessoDocumentoEstruturado() != null ) {
			return anotacaoManager.temDivergencias(documento);
		}
		
		return 0;
	}
	
	/**
	 * Verifica a existência de destaques para o documento, retornando um nº indicando o tipo de destaque encontrado (se houver):
	 * 0 - O documento não possui destaques
	 * 1 - Destaque não concluído/liberado
	 * 2 - Destaque
	 * 
	 * @author fernando.junior (17/01/2013)
	 * 
	 * [PJEII-5293] Método alterado para que verificasse os destaques pelo documento, não pelos tópicos.
	 * @author fernando.junior (05/02/2013) 
	 */
	public int existeDestaque(int idProcessoTrf) {
		ProcessoDocumentoEstruturado documento = processoDocumentoEstruturadoManager.getUltimoAcordaoEstruturadoByIdProcessoTrf(idProcessoTrf);
		
		if (documento != null && documento.getIdProcessoDocumentoEstruturado() != null) {
			return anotacaoManager.temDestaques(documento);
		}
		
		return 0;
	}
	
	/**
	 * Verifica a existência de anotação para o documento, retornando um nº indicando o tipo de anotação:
	 * 0 - O documento não possui anotações
	 * 1 - Anotação não concluída
	 * 2 - Anotação
	 * 
	 * @author fernando.junior (17/01/2013)
	 * 
	 * [PJEII-5293] Método alterado para que verificasse as anotações pelo documento, não pelos tópicos.
	 * @author fernando.junior (05/02/2013) 
	 */
	public int existeAnotacao(int idProcessoTrf) {
		ProcessoDocumentoEstruturado documento = processoDocumentoEstruturadoManager.getUltimoAcordaoEstruturadoByIdProcessoTrf(idProcessoTrf);
		
		if (documento != null && documento.getIdProcessoDocumentoEstruturado() != null) {
			return anotacaoManager.temAnotacoes(documento);
		}
		
		return 0;
	}

	public Boolean getIsVotacaoAntecipada() {
		return isVotacaoAntecipada;
	}

	public void setIsVotacaoAntecipada(Boolean isVotacaoAntecipada) {
		this.isVotacaoAntecipada = isVotacaoAntecipada;
	}
	
	public Integer countAcordaoAssinado(PautaSessao pautaSessao, Voto voto) {
        return processoDocumentoBinPessoaAssinaturaManager.countDocumentoAcordaoAssinado(pautaSessao.getProcessoTrf(),
            getSessao(), voto);
    }

	public Boolean getRemoverDocumentoEstruturado() {
		return removerDocumentoEstruturado;
	}

	public void setRemoverDocumentoEstruturado(
			Boolean removerDocumentoEstruturado) {
		this.removerDocumentoEstruturado = removerDocumentoEstruturado;
	}
}
