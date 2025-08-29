package br.com.infox.pje.action;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.imageio.ImageIO;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceException;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.NumeroRpvUtil;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.component.tree.AbstractTreeHandler;
import br.com.infox.component.tree.EntityNode;
import br.com.infox.component.tree.SearchTree2GridList;
import br.com.infox.core.certificado.CertificadoException;
import br.com.infox.core.certificado.util.VerificaCertificadoPessoa;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.com.infox.pje.manager.RpvManager;
import br.com.infox.pje.manager.RpvManager.RpvException;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.MoneyFormat;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.PessoaPerito;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Rpv;
import br.jus.pje.nucleo.entidades.RpvEletronica;
import br.jus.pje.nucleo.entidades.RpvNaturezaDebito;
import br.jus.pje.nucleo.entidades.RpvParteDeducao;
import br.jus.pje.nucleo.entidades.RpvParteRepresentante;
import br.jus.pje.nucleo.entidades.RpvParteValorCompensar;
import br.jus.pje.nucleo.entidades.RpvPessoaParte;
import br.jus.pje.nucleo.entidades.RpvStatus;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.entidades.TipoPessoa;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.RpvPessoaParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.RpvPrecatorioEnum;
import br.jus.pje.nucleo.enums.RpvTipoCessaoEnum;
import br.jus.pje.nucleo.enums.RpvTipoFormaHonorarioEnum;
import br.jus.pje.nucleo.enums.TipoOrgaoPublicoEnum;
import br.jus.pje.nucleo.util.DateUtil;

import com.lowagie.text.pdf.Barcode128;
@Name(RpvAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class RpvAction implements Serializable {

	private static final String TAB_DETALHE_RPV_TAB = "detalheRpvTab";
	private static final String TAB_SEARCH = "search";
	private static final long serialVersionUID = 1L;
	public static final String NAME = "rpvAction";
	private static final LogProvider log = Logging.getLogProvider(RpvAction.class);

	private boolean flagModalCessionario = false;
	private boolean flagValorDeducao = false;
	private boolean flagValorCompensar = false; 
	private String mensagemOutrosRessarcimentoCustas;
	private RpvPessoaParte parteSelecionadaModalDeducao;
	private RpvPessoaParte parteSelecionadaModalValorCompensar;
	private double valorCompensarModalCompensar = 0;
	private String identificacaoModalCompensar;
	private RpvNaturezaDebito rpvNaturezaDebito;
	private List<RpvPessoaParte> advogadosSelecionadosBeneficiarioList = new ArrayList<RpvPessoaParte>(0);
	
	@In
	private ProcessoTrfManager processoTrfManager;
	
	@In
	private RpvManager rpvManager;
	
	private Rpv rpv;	
	private String motivoCancelamento;
	private String idRpvCancelamento;
	
	private ProcessoTrf processoTrf;
	private String tab = TAB_SEARCH;
	private boolean novoCadastro = false;
	private String flagTelaCadastro = "tela1";
	private SearchTree2GridList<ProcessoParte> searchTree2GridRpvPoloAtivoList;
	private SearchTree2GridList<ProcessoParte> searchTree2GridRpvPoloPassivoList;
	private SearchTree2GridList<RpvPessoaParte> searchTree2GridPoloAtivoList;
	private SearchTree2GridList<RpvPessoaParte> searchTree2GridPoloPassivoList;
	private List<Pessoa> peritosList = new ArrayList<Pessoa>(0);
	private Pessoa herdeiro;
	private Pessoa representante;
	private Integer idRpv;
	private Boolean sucessInMethod = Boolean.FALSE;
	private String motivoDevolucao;
	private double valorPagoPessoaAnterior = 0; 
	private String conteudoHtml;
	private String certChain;
	private String signature;
	private Boolean isOficio = Boolean.FALSE;
	
	public Integer getIdRpv() {
		return idRpv;
	}

	public void setIdRpv(Integer idRpv) {
		this.idRpv = idRpv;
	}
	
	public Boolean getSucessInMethod() {
		return sucessInMethod;

	}

	public void setSucessInMethod(Boolean sucessInMethod) {
		this.sucessInMethod = sucessInMethod;
	}

	public Rpv getRpv() {
		return this.rpv;
	}	
	
	public void setRpv(Rpv rpv) {
		this.rpv = rpv;
	}

	public void setarRpv(){
		if(this.rpv == null || this.rpv.getIdRpv() == 0){
			setRpvPorId(getIdRpv());
			if(this.rpv != null){
				setProcessoTrf(rpv.getProcessoTrf());
			}
		}
	}
	public void setProcessoTrf(ProcessoTrf processoTrf) {
		representante = null;
		herdeiro = null;
		searchTree2GridPoloAtivoList = null;
		searchTree2GridPoloPassivoList = null;
		searchTree2GridRpvPoloAtivoList = null;
		searchTree2GridRpvPoloPassivoList = null;
		this.setFlagTelaCadastro("tela1");
		if(processoTrf==null){
			tab = TAB_SEARCH;
		} else {
			tab = TAB_DETALHE_RPV_TAB;
			this.processoTrf = processoTrf;
			ProcessoTrfHome.instance().setId(processoTrf.getIdProcessoTrf()); 
		}
		Contexts.removeFromAllContexts("processoTrfRpvDocumentoGrid");
	}

	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setTab(String tab) {
		this.tab = tab;
	}

	public String getTab() {
		return tab;
	}
	
	public void setNovoCadastro(boolean novoCadastro) {
		if(novoCadastro){
			rpv = rpvManager.criaNovaInstanciaRpv(processoTrf);
			this.herdeiro = null;
			this.representante = null;
			this.rpv.setBeneficiario(null);
			if(rpv.getBeneficiario() == null && getAutores().size() == 1){
				rpv.setBeneficiario(getAutores().get(0));
				if(getHerdeiros().size() == 1){
					this.herdeiro = getHerdeiros().get(0);
				}
			}
			searchTree2GridRpvPoloAtivoList = null;
			searchTree2GridRpvPoloPassivoList = null;
			advogadosSelecionadosBeneficiarioList = new ArrayList<RpvPessoaParte>(0);
		}
		this.novoCadastro = novoCadastro;
	}

	public boolean getNovoCadastro() {
		return novoCadastro;
	}

	/**
	 * Método usado para limpar os dados da pequisa de rpv exibindo a tela de
	 * pesquisa.
	 */
	public void limpar(){
		flagModalCessionario = false;
		flagValorDeducao = false;
		flagValorCompensar = false; 
		parteSelecionadaModalDeducao = null;
		parteSelecionadaModalValorCompensar = null;
		valorCompensarModalCompensar = 0;
		rpvNaturezaDebito = null;		
		representante = null;
		herdeiro = null;
		searchTree2GridPoloAtivoList = null;
		searchTree2GridPoloPassivoList = null;
		searchTree2GridRpvPoloAtivoList = null;
		searchTree2GridRpvPoloPassivoList = null;
		processoTrf = null;
		rpv = null;
		novoCadastro = false;
	}
	
	public void listarRequisitorios(){
		flagTelaCadastro = "tela1";
		flagModalCessionario = false;
		flagValorDeducao = false;
		flagValorCompensar = false; 
		parteSelecionadaModalDeducao = null;
		parteSelecionadaModalValorCompensar = null;
		valorCompensarModalCompensar = 0;
		rpvNaturezaDebito = null;		
		representante = null;
		herdeiro = null;
		searchTree2GridPoloAtivoList = null;
		searchTree2GridPoloPassivoList = null;
		searchTree2GridRpvPoloAtivoList = null;
		searchTree2GridRpvPoloPassivoList = null;
		rpv = null;
		novoCadastro = false;
	}
	
	/**
	 * Método uado no botão voltar da tela 1 do cadastro de rpv limpando 
	 * cancelando o novo cadastro ou a edição
	 */
	public void limparDadosRpv(){
		rpv = null;
		novoCadastro = false;
		advogadosSelecionadosBeneficiarioList = new ArrayList<RpvPessoaParte>(0);
	}
	
	/**
	 * Método que traz a rpv através do id usado na edição da rpv 
	 * @param idRpv
	 */
	public void setRpvPorId(int idRpv){
		representante = null;
		herdeiro = null;
		searchTree2GridPoloAtivoList = null;
		searchTree2GridPoloPassivoList = null;
		searchTree2GridRpvPoloAtivoList = null;
		searchTree2GridRpvPoloPassivoList = null;
		
		this.rpv = (Rpv) EntityUtil.getEntityManager().find(Rpv.class, idRpv);
	}
	
	public void copiarRpv(int idRpv){
		representante = null;
		herdeiro = null;
		searchTree2GridPoloAtivoList = null;
		searchTree2GridPoloPassivoList = null;
		searchTree2GridRpvPoloAtivoList = null;
		searchTree2GridRpvPoloPassivoList = null;		
		try {
			this.rpv = (Rpv) EntityUtil.getEntityManager().find(Rpv.class, idRpv);
			Rpv rpvCopy = EntityUtil.cloneEntity(this.rpv, Boolean.FALSE);
			rpvCopy.setRpvStatus(ParametroUtil.instance().getStatusRpvEmElaboracao());
			rpvCopy.setNumeroOrigemProcesso(null);
			rpvCopy.setNumeroRpvPrecatorio(null);
			rpvCopy.setNumeroSequencia(null);
			rpvCopy.setNumeroVara(null);
			rpvCopy.setDataCadastro(new Date());
			rpvCopy.setPessoaCadastro(Authenticator.getPessoaLogada());
			
			RpvStatus statusRpvRejeitada = ParametroUtil.instance().getStatusRpvRejeitada();
			if(statusRpvRejeitada.equals(this.rpv.getRpvStatus())){
				rpvCopy.setRpvRejeitado(this.rpv);
			}
			
			rpvManager.persist(rpvCopy);
			this.rpv = rpvCopy; 
			
			montarPoloAtivoRpv(rpv);
			
			for (RpvPessoaParte adv : rpv.getListaParteAtivo()) {
				TipoParte tipoParteAdvogado = ParametroUtil.instance().getTipoParteAdvogado();
				if(adv.getTipoParte().equals(tipoParteAdvogado)){
					this.advogadosSelecionadosBeneficiarioList.add(adv);
				}
			}			
			
			Integer numeroVara  = rpv.getProcessoTrf().getOrgaoJulgador().getNumeroVara();
			numeroVara = numeroVara == null ? 0 : numeroVara;
			Integer numeroOrigem = rpv.getProcessoTrf().getNumeroOrigem();
			numeroOrigem = numeroOrigem == null ? 0 : numeroOrigem;
			NumeroRpvUtil.numerarRpv(rpv, numeroVara, numeroOrigem);			
			
		} catch (InstantiationException e) {
			FacesMessages.instance().add(Severity.ERROR, 
			"Erro ao copiar RPV.");
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			FacesMessages.instance().add(Severity.ERROR, 
			"Erro ao copiar RPV.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Verifica se existe pericias para o processo
	 * @return true ou false
	 */
	public boolean isPericia(){
		if(processoTrf == null){
			return false;
		}
		return processoTrf.getProcessoPericiaList().size() > 0;
	}
	
	/**
	 * Método que verifica se existem outras rpvs não canceladas ou rejeitadas 
	 * para o processo com crédito somente advogado
	 * @return true ou false
	 */
	public boolean existeOutrasRpvsComCreditoSomenteAdvogado(){
		if(processoTrf == null){
			return false;
		}
		List<Rpv> rpvsByProcessoTrfList = processoTrfManager.getRpvsByProcessoTrfList(processoTrf);
		for (Rpv rpvSuc : rpvsByProcessoTrfList) {
			if(rpvSuc.getInCreditoSomenteAdvogado() && !rpvSuc.equals(rpv)){
				return true;
			}
		}
		return false;
	}

	public void setFlagTelaCadastro(String flagTelaCadastro) {
		searchTree2GridPoloAtivoList = null;
		setIsOficio(Boolean.FALSE);
		this.flagTelaCadastro = flagTelaCadastro;
	}

	public String getFlagTelaCadastro() {
		return flagTelaCadastro;
	}
	
	public ProcessoParte getAutorCabeca(){
		if(processoTrf == null){
			return null;
		}
		return processoTrfManager.getAutorCabeca(processoTrf);
	}
	
	public void persistRpv(){
		try {
			rpv.setDataCadastro(new Date());
			rpv.setPessoaCadastro(Authenticator.getPessoaLogada());
			rpv.setAutorCabecaAcao(getAutorCabeca().getPessoa());
			rpv.setRpvStatus(ParametroUtil.instance().getStatusRpvEmElaboracao());
			
			if(rpv.getInCreditoSomenteAdvogado()){
				rpv.setInCessionario(false);
				rpv.setInReembolsoHonorariosSecao(false);
				rpv.setValorCustas(0.0);
				rpv.setInPagamentoDiretoPerito(false);
			}
			
			if(rpv.getInValorCompensar()){
				rpv.setInReembolsoHonorariosSecao(false);
			}
			
			if(rpv.getInReembolsoHonorariosSecao()){
				rpv.setInMultaAstreintes(false);
				rpv.setInCessionario(false);
				rpv.setInTipoFormaHonorario(RpvTipoFormaHonorarioEnum.R);
				rpv.setValorCustas(0.0);
				rpv.setInCreditoSomenteAdvogado(false);
			}			
			
			if(rpv.getInRessarcimentoCustas()){
				rpv.setInCessionario(false);
				rpv.setInReembolsoHonorariosSecao(false);
				rpv.setInPagamentoDiretoPerito(false);
				rpv.setInCreditoSomenteAdvogado(false);
			}		
			
			if(rpv.getInMultaAstreintes()){
				rpv.setInReembolsoHonorariosSecao(false);
				rpv.setInRessarcimentoCustas(false);
			}
			
			if(rpv.getInCessionario()){
				rpv.setInTipoFormaHonorario(RpvTipoFormaHonorarioEnum.R);
			}
			
			if(rpv.getInRpvPrecatorio().equals(RpvPrecatorioEnum.P) && 
					rpv.getInCessionario() && rpv.getInValorCompensar()){
				rpv.setTipoCessao(RpvTipoCessaoEnum.P);
			}			
			
			searchTree2GridPoloAtivoList = null;
			
			rpvManager.persistRpv(rpv);
			montarPoloAtivoRpv(rpv);
			
			for (RpvPessoaParte adv : rpv.getListaParteAtivo()) {
				TipoParte tipoParteAdvogado = ParametroUtil.instance().getTipoParteAdvogado();
				if(adv.getTipoParte().equals(tipoParteAdvogado)){
					this.advogadosSelecionadosBeneficiarioList.add(adv);
				}
			}
			
			
			
			Integer numeroVara  = rpv.getProcessoTrf().getOrgaoJulgador().getNumeroVara();
			numeroVara = numeroVara == null ? 0 : numeroVara;
			Integer numeroOrigem = rpv.getProcessoTrf().getNumeroOrigem();
			numeroOrigem = numeroOrigem == null ? 0 : numeroOrigem;
			NumeroRpvUtil.numerarRpv(rpv, numeroVara, numeroOrigem);
			
			setFlagTelaCadastro("tela2");
		} catch (RpvException e) {
			FacesMessages.instance().add(Severity.ERROR, e.getMessage());
			e.printStackTrace();
		}
	}
	
	public boolean existeOutroRessarcimentoCustas() {
		if (rpv == null || rpv.getProcessoTrf() == null || rpv.getBeneficiario() == null) {
			return false;
		}
		List<Rpv> listRpvRessarcimentoCustas = rpvManager.listRpvRessarcimentoCustas(rpv);
		return  listRpvRessarcimentoCustas.size() > 0;
	}
	
	public void updateRpv(){
		try {
			if(peritosList.size() == 0){
				List<Pessoa> list = new ArrayList<Pessoa>(0);
				TipoParte tipoPartePerito = ParametroUtil.instance().getTipoPartePerito();
				for (RpvPessoaParte pessoaParte : rpv.getRpvParteList()) {
					if(pessoaParte.getTipoParte() == tipoPartePerito){
						list.add(pessoaParte.getPessoa());
					}
				}
				peritosList = list;
			}

			if(rpv.getInCreditoSomenteAdvogado()){
				rpv.setInCessionario(false);
				rpv.setInReembolsoHonorariosSecao(false);
				rpv.setValorCustas(0.0);
				rpv.setInPagamentoDiretoPerito(false);
			}
			
			if(rpv.getInValorCompensar()){
				rpv.setInReembolsoHonorariosSecao(false);
			}
			
			if(rpv.getInReembolsoHonorariosSecao()){
				rpv.setInMultaAstreintes(false);
				rpv.setInCessionario(false);
				rpv.setInTipoFormaHonorario(RpvTipoFormaHonorarioEnum.R);
				rpv.setValorCustas(0.0);
				rpv.setInCreditoSomenteAdvogado(false);
			}			
			
			if(rpv.getInRessarcimentoCustas()){
				rpv.setInCessionario(false);
				rpv.setInReembolsoHonorariosSecao(false);
				rpv.setInPagamentoDiretoPerito(false);
				rpv.setInCreditoSomenteAdvogado(false);
			}

			if(rpv.getInMultaAstreintes()){
				rpv.setInReembolsoHonorariosSecao(false);
				rpv.setInRessarcimentoCustas(false);
			}			
			
			if(rpv.getInRpvPrecatorio().equals(RpvPrecatorioEnum.P) && 
					rpv.getInCessionario() && rpv.getInValorCompensar()){
				rpv.setTipoCessao(RpvTipoCessaoEnum.P);
			}			
			
			atualizaRpvParteSecaoJudiciaria();			
			
			rpvManager.updateRpv(rpv);

			atualizaRepresentanteParte();
			
			advogadosSelecionadosBeneficiarioList = new ArrayList<RpvPessoaParte>(0);
			
			for (RpvPessoaParte adv : rpv.getListaParteAtivo()) {
				TipoParte tipoParteAdvogado = ParametroUtil.instance().getTipoParteAdvogado();
				if(adv.getTipoParte().equals(tipoParteAdvogado)){
					this.advogadosSelecionadosBeneficiarioList.add(adv);
				}
			}

			if(advogadosSelecionadosBeneficiarioList.isEmpty()){
				addAdvogadosBeneficiarioReal();
			}
			
			atualizaPoloTerceiro(rpv);
			searchTree2GridPoloAtivoList = null;
			searchTree2GridPoloPassivoList = null;
			setFlagTelaCadastro("tela2");
		} catch (RpvException e) {
			FacesMessages.instance().add(Severity.ERROR, e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void addAdvogadosBeneficiarioReal(){
		List<ProcessoParteRepresentante> repProcessoTrfList = 
		processoTrfManager.getListRepresentanteByPessoaAndProcessoTrf(processoTrf, getBeneficiarioReal());
		TipoParte tipoParteAdvogado = ParametroUtil.instance().getTipoParteAdvogado();
		for (ProcessoParteRepresentante adv : repProcessoTrfList) {
			if (adv.getParteRepresentante().getTipoParte().equals(tipoParteAdvogado)) {
					RpvPessoaParte parteAdv = new RpvPessoaParte();
					parteAdv.setPessoa(adv.getParteRepresentante().getPessoa());
					parteAdv.setIncapaz(rpvManager.isInCapaz(adv.getParteRepresentante().getPessoa()));
					parteAdv.setRpv(rpv);
					parteAdv.setTipoParte(tipoParteAdvogado);
					parteAdv.setInParticipacao(RpvPessoaParteParticipacaoEnum.A);
					rpvManager.persisteRpvParte(parteAdv);
					rpv.getRpvParteList().add(parteAdv);
					
					advogadosSelecionadosBeneficiarioList.add(parteAdv);
					
					RpvParteRepresentante representanteRpvAdv = new RpvParteRepresentante();
					representanteRpvAdv.setRpvPessoaParte(getRpvParteBeneficiarioReal());
					representanteRpvAdv.setRpvPessoaRepresentante(parteAdv);
					rpvManager.persisteRpvRepresentante(representanteRpvAdv);
					getRpvParteBeneficiarioReal().getRpvRepresentanteList().add(representanteRpvAdv);
			}
		}	
	}
	
	public void atualizaPoloTerceiro(Rpv rpv){
		//Validar cessionario
		if(!rpv.getInCessionario()){
			rpvManager.removeCessionariosRpv(rpv);
		}
		
		//Validar Perito
		if(!rpv.getInPagamentoDiretoPerito()){
			rpvManager.removePeritosRpv(rpv);
		}		
	}	
	
	public void updateTela2(){
		try {
			if(rpv.getInPagamentoDiretoPerito()){
				rpvManager.removePeritosRpv(rpv);
				for (Pessoa perito : peritosList) {
					rpvManager.addPeritoRpv(perito, rpv);
				}
			}
			
			if(!rpv.getInReembolsoHonorariosSecao() && isParteNaoAssistidaPorAdvogado()){
				if(!isEntidade(getBeneficiarioReal()) && 
					rpv.getInRpvPrecatorio().equals(RpvPrecatorioEnum.P)){
					FacesMessages.instance().add(Severity.ERROR, 
					"É obrigatório pelo menos um advogado para o beneficiário.");
					return; 
				}
			}
			
			if(!isParteNaoAssistidaPorAdvogado() && advogadosSelecionadosBeneficiarioList.isEmpty()){
				FacesMessages.instance().add(Severity.ERROR, 
				"É obrigatório a seleção de pelo menos um advogado para o beneficiário da requisição.");
				return; 
			}
			
			if(!validaSucumbencia()) {
				return;
			}

			atualizaAdvogadosBeneficiario();
			
			rpvManager.validarPartesRpv(rpv);
			
			rpvManager.update(rpv);
			
			rpvManager.atualizaParteReu(rpv);
			
			searchTree2GridPoloAtivoList = null;
			searchTree2GridPoloPassivoList = null;			
			setFlagTelaCadastro("tela3");
		} catch (RpvException e) {
			FacesMessages.instance().add(Severity.ERROR, e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Método que verifica se uma pessoa é uma entidade
	 * @param pessoa
	 * @return boolean
	 */
	public boolean isEntidade(Pessoa pessoa){
		TipoPessoa tipoPessoaEntidade = ParametroUtil.instance().getTipoPessoaEntidade();
		if(pessoa.getTipoPessoa().equals(tipoPessoaEntidade) || 
			(pessoa.getTipoPessoa().getTipoPessoaSuperior()!= null &&
			pessoa.getTipoPessoa().getTipoPessoaSuperior().equals(tipoPessoaEntidade))){
			return true;
		}
		return false;
	}	
	
	/**
	 * Remove ou aiciona seçao judiciária a rpv
	 */
	private void atualizaRpvParteSecaoJudiciaria(){
		RpvPessoaParte secaoParte = getRpvParteSecaoJudiciaria();
		if(rpv.getInReembolsoHonorariosSecao() && secaoParte == null){
			secaoParte = new RpvPessoaParte();
			secaoParte.setPessoa(ParametroUtil.instance().getPessoaSecaoJudiciaria());
			secaoParte.setTipoParte(getRpvParteBeneficiarioReal().getTipoParte());
			secaoParte.setRpv(rpv);
			secaoParte.setInParticipacao(RpvPessoaParteParticipacaoEnum.A);
			rpvManager.persisteRpvParte(secaoParte);
			rpv.getRpvParteList().add(secaoParte);
		} else if(!rpv.getInReembolsoHonorariosSecao() && getRpvParteSecaoJudiciaria() != null){
			rpvManager.remove(secaoParte);
			rpv.getRpvParteList().remove(secaoParte);
		}		
	}

	/**
	 * Método que retorna o RpvPessoaParte que representa a seçao judiciária
	 * @return RpvPessoaParte
	 */
	public RpvPessoaParte getRpvParteSecaoJudiciaria(){
		for (RpvPessoaParte parte : rpv.getRpvParteList()) {
			if(isPessoaParteSecao(parte.getPessoa())){
				return parte;
			}
		}
		return null;
	} 

	/**
	 * Método que retorna o RpvPessoaParte que representa o beneficiário real
	 * @return RpvPessoaParte
	 */	
	public RpvPessoaParte getRpvParteBeneficiarioReal(){
		for (RpvPessoaParte parte : rpv.getRpvParteList()) {
			if(parte.getPessoa().equals(getBeneficiarioReal())){
				return parte;
			}
		}
		return null;
	} 
	
	/**
	 * Método que pega a primeira prioridade da lista. Se tiver mais de uma, ele concatenda com 
	 * " e outras." e se tiver somente uma, ele retonar a prioridade.
	 * @return prioridade
	 */
	public String retornaPrioridade(){
		int qtdPrioridade = processoTrf.getPrioridadeProcessoList().size();
		String prioridade = processoTrf.getPrioridadeProcessoList().get(0).toString();  
		if (qtdPrioridade > 1) {
			prioridade = prioridade + " e outras.";
		}
		return prioridade;
	}	
	
	private boolean validaSucumbencia() {
		if(rpv.getInCreditoSomenteAdvogado()) {
			if (advogadosSelecionadosBeneficiarioList.size() > 1){
				FacesMessages.instance().add(Severity.ERROR, 
						"Para requisitórios sucumbênciais é obrigatório a seleção de um e somente um advogado.");
				return false;
			}

			List<Rpv> rpvsByProcessoTrfList = processoTrfManager.getRpvsByProcessoTrfList(processoTrf);
			
			RpvStatus cancelada = ParametroUtil.instance().getStatusRpvCancelada();
			RpvStatus devolvida = ParametroUtil.instance().getStatusRpvDevolvida();

			List<Pessoa> pessoaAdvList = new ArrayList<Pessoa>(0);
			
			for(RpvPessoaParte parteAdv : advogadosSelecionadosBeneficiarioList){
				pessoaAdvList.add(parteAdv.getPessoa());
			}
			
			for (Rpv rpvSucubencial : rpvsByProcessoTrfList) {
				if(rpvSucubencial.getInCreditoSomenteAdvogado() && !rpvSucubencial.equals(rpv) &&
				   !(rpvSucubencial.getRpvStatus().equals(devolvida) || rpvSucubencial.getRpvStatus().equals(cancelada))){
					for (RpvPessoaParte rpvParte : rpvSucubencial.getListaParteAtivo()) {
						for (RpvParteRepresentante representante : rpvParte.getRpvRepresentanteList()) {
							TipoParte tipoParteAdvogado = ParametroUtil.instance().getTipoParteAdvogado();
							RpvPessoaParte rpvPessoaRepresentante = representante.getRpvPessoaRepresentante();
							if(rpvPessoaRepresentante.getTipoParte().equals(tipoParteAdvogado) &&
									pessoaAdvList.contains(rpvPessoaRepresentante.getPessoa())){
								FacesMessages.instance().add(Severity.ERROR, 
										"O Advogado seleciona já possui requisitório Nº {0}. Não é permitido expedição de duas sucumbências para o mesmo Advogado.", rpvSucubencial.getNumeroRpvPrecatorio());
								return false;
							}
						}
					}
				}
			}
		}
		return true;
	}

	public void atualizaAdvogadosBeneficiario(){
		List<RpvPessoaParte> partesRemovidas = new ArrayList<RpvPessoaParte>(0);
		for (RpvPessoaParte rpvParte : rpv.getListaParteAtivo()) {
			List<RpvParteRepresentante> repRemovidos = new ArrayList<RpvParteRepresentante>(0);
			for (RpvParteRepresentante representante : rpvParte.getRpvRepresentanteList()) {
				TipoParte tipoParteAdvogado = ParametroUtil.instance().getTipoParteAdvogado();
				if(representante.getRpvPessoaRepresentante().getTipoParte().equals(tipoParteAdvogado) 
					&& !advogadosSelecionadosBeneficiarioList.contains(representante.getRpvPessoaRepresentante())){
					rpvManager.remove(representante);
					repRemovidos.add(representante);
					rpvManager.remove(representante.getRpvPessoaRepresentante());
					partesRemovidas.add(representante.getRpvPessoaRepresentante());
				}
			}
			rpvParte.getRpvRepresentanteList().removeAll(repRemovidos);
		}
		rpv.getRpvParteList().removeAll(partesRemovidas);
	}

	@SuppressWarnings("unchecked")
	public SearchTree2GridList<ProcessoParte> getSearchTree2GridRpvPoloAtivoList() {
		if (searchTree2GridRpvPoloAtivoList == null) {
			ProcessoParte searchBean = (ProcessoParte) Component.getInstance("processoParteSearch");
			AbstractTreeHandler<ProcessoParte> treeHandler = 
				(AbstractTreeHandler<ProcessoParte>) Component.getInstance("rpvProcessoPartePoloAtivoTree");
			treeHandler.clearTree();
			searchTree2GridRpvPoloAtivoList = new SearchTree2GridList<ProcessoParte>(searchBean, treeHandler);
		}
		return searchTree2GridRpvPoloAtivoList;
	}
	
	@SuppressWarnings("unchecked")
	public SearchTree2GridList<ProcessoParte> getSearchTree2GridRpvPoloPassivoList() {
		if (searchTree2GridRpvPoloPassivoList == null) {
			ProcessoParte searchBean = (ProcessoParte) Component.getInstance("processoParteSearch");
			AbstractTreeHandler<ProcessoParte> treeHandler = 
				(AbstractTreeHandler<ProcessoParte>) Component.getInstance("rpvProcessoPartePoloPassivoTree");
			treeHandler.clearTree();
			searchTree2GridRpvPoloPassivoList = new SearchTree2GridList<ProcessoParte>(searchBean, treeHandler);
		}
		return searchTree2GridRpvPoloPassivoList;
	}	
	
	public static RpvAction instance(){
		return (RpvAction) Component.getInstance("rpvAction");
	}
	
	/**
	 * Lista dos cessionarios da rpv
	 * @return RpvPessoaParte
	 */
	public List<RpvPessoaParte> listCessionario(){
		return rpvManager.listCessionarioByRpv(rpv);
	}

	public void setFlagValorDeducao(boolean flagValorDeducao) {
		this.flagValorDeducao = flagValorDeducao;
	}

	public boolean getFlagValorDeducao() {
		return flagValorDeducao;
	}
	
	public boolean getFlagValorCompensar() {
		return flagValorCompensar;
	}
	
	public void setFlagValorCompensar(boolean flagValorCompensar) {
		this.flagValorCompensar = flagValorCompensar;
	}
	
	public void setFlagModalCessionario(boolean flagModalCessionario) {
		this.flagModalCessionario = flagModalCessionario;
	}

	public boolean getFlagModalCessionario() {
		return flagModalCessionario;
	}
	
	public void addCessionario(Pessoa pessoa){
		rpvManager.addCessionario(rpv, pessoa);
		FacesMessages.instance().add(Severity.INFO, "Cessionário adicionado com sucesso.");
	}
	
	public void removeCessionario(RpvPessoaParte pessoaParte){
		rpvManager.removeCessionario(pessoaParte);
		FacesMessages.instance().add(Severity.INFO, "Cessionário removido com sucesso.");
	}	
	
	public List<PessoaPerito> processoPeritosList(){
		return processoTrfManager.getPeritosProcesso(processoTrf);
	}

	public void setPeritosList(List<Pessoa> peritosList) {
		this.peritosList = peritosList;
	}

	public List<Pessoa> getPeritosList() {
		return peritosList;
	}
	
	@SuppressWarnings("unchecked")
	public SearchTree2GridList<RpvPessoaParte> getSearchTree2GridPoloAtivoList() {
		if (searchTree2GridPoloAtivoList == null) {
			RpvPessoaParte searchBean = new RpvPessoaParte();
			AbstractTreeHandler<RpvPessoaParte> treeHandler = 
				(AbstractTreeHandler<RpvPessoaParte>) Component.getInstance("rpvPoloAtivoTree");
			treeHandler.clearTree();
			searchTree2GridPoloAtivoList = new SearchTree2GridList<RpvPessoaParte>(searchBean, treeHandler);
		}
		return searchTree2GridPoloAtivoList;
	}
	
	
	@SuppressWarnings("unchecked")
	public SearchTree2GridList<RpvPessoaParte> getSearchTree2GridPoloPassivoList() {
		if (searchTree2GridPoloPassivoList == null) {
			RpvPessoaParte searchBean = new RpvPessoaParte();
			AbstractTreeHandler<RpvPessoaParte> treeHandler = 
				(AbstractTreeHandler<RpvPessoaParte>) Component.getInstance("rpvPoloPassivoTree");
			treeHandler.clearTree();
			searchTree2GridPoloPassivoList = new SearchTree2GridList<RpvPessoaParte>(searchBean, treeHandler);
		}
		return searchTree2GridPoloPassivoList;
	}
	
	public String getAdvogadoRpv(){
		String adv = "";
		int cont = 0;
		for (RpvPessoaParte rpp : this.rpv.getRpvParteList()) {
			if(rpp.getTipoParte() == ParametroUtil.instance().getTipoParteAdvogado() && cont == 0){
				adv = rpp.getPessoa().getNome();
				cont++;
			}else if(rpp.getTipoParte() == ParametroUtil.instance().getTipoParteAdvogado()){
				cont++;
			}
		}
		if(cont > 1){
			adv = adv + " e OUTRO(S)";
		}
		return adv;
	}
	
	public Pessoa getHerdeiro() {
		if(herdeiro == null && rpv != null && rpv.getIdRpv() != 0){
			for (RpvPessoaParte parteHerdeiro : rpv.getRpvParteList()) {
				TipoParte tipoParteHerdeiro = ParametroUtil.instance().getTipoParteHerdeiro();
				if(parteHerdeiro.getTipoParte().equals(tipoParteHerdeiro)){
					herdeiro = parteHerdeiro.getPessoa();
					break;
				}
			}
		}
		return herdeiro;
	}
	
	public void setHerdeiro(Pessoa herdeiro) {
		this.herdeiro = herdeiro;
	}
	
	public Pessoa getRepresentante() {
		if(representante == null && rpv != null && rpv.getIdRpv() != 0 && getRpvParteBeneficiarioReal() != null){
			for (RpvParteRepresentante parteRepresentante : getRpvParteBeneficiarioReal().getRpvRepresentanteList()) {
				if(isTipoRepresentante(parteRepresentante.getRpvPessoaRepresentante().getTipoParte())){
					representante = parteRepresentante.getRpvPessoaRepresentante().getPessoa();
					break;
				}
			}
		}
		return representante;
	}	
	
	/**
	 * Verifica se um tipo parte do representante é difenrente de Advogado ou de Herdeiro 
	 * @param tipoParte
	 * @return boolean
	 */
	public boolean isTipoRepresentante(TipoParte tipoParte){
		TipoParte tipoParteAdvogado = ParametroUtil.instance().getTipoParteAdvogado();
		TipoParte tipoParteHerdeiro = ParametroUtil.instance().getTipoParteHerdeiro();
		return !(tipoParte.equals(tipoParteAdvogado) || tipoParte.equals(tipoParteHerdeiro)); 
	}
	
	public void setRepresentante(Pessoa representante) {
		this.representante = representante;
	}
	
	/**
	 * Verifica se um tipo parte é advogado ou procurador
	 * @param tipoParte
	 * @return
	 */
	public boolean isAdvogadoOrProcurador(TipoParte tipoParte){
		TipoParte tipoParteAdvogado = ParametroUtil.instance().getTipoParteAdvogado();
		TipoParte tipoParteProcurador = ParametroUtil.instance().getTipoParteProcurador();
		return (tipoParte == tipoParteAdvogado || tipoParte == tipoParteProcurador);
	}
	
	public boolean isPessoaFisica(Pessoa pessoa){
		return pessoa instanceof PessoaFisica;
	}

	public boolean isPessoaJuridica(Pessoa pessoa){
		return pessoa instanceof PessoaJuridica;
	}	
	
	public void updateTela3(){
		if(!validaRepresentantesBeneficiarioReal()){
			return;
		}		
		if(!validarValoresPartes()){
			return;
		}
		rpv.setRpvStatus(ParametroUtil.instance().getStatusRpvElaborado());
		rpvManager.update(rpv);
		setFlagTelaCadastro("tela4");
	}
	
	public boolean validarValoresPartes() {
		ParametroUtil param = ParametroUtil.instance();
		TipoParte tipoParteAdvogado = param.getTipoParteAdvogado();
		Pessoa secaoJudiciaria = param.getPessoaSecaoJudiciaria();
		TipoParte tipoParteCessionario = param.getTipoParteCessionario();
		TipoParte tipoPartePerito = param.getTipoPartePerito();
		
		double salarioMinimo = 0;
		try {
			salarioMinimo = Double.parseDouble(ParametroUtil.getParametro("valorSalarioMinimo").replaceAll(",","."));
		} catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR, 
					"Erro ao calcular valor do salario mínimo, favor ajustar o valor do parâmetro 'valorSalarioMinimo'.");			
			e.printStackTrace();
			return false;	
		}

		double qtdSalarios = 0;
		try {
			qtdSalarios = Double.parseDouble(ParametroUtil.getParametro("quantidadeSalarios").replaceAll(",","."));
		} catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR, 
					"Erro ao calcular valor do salario mínimo, favor ajustar o valor do parâmetro 'quantidadeSalarios'.");
			e.printStackTrace();
			return false;	
		}		
		
		double tetoMaximo = 0;		
		boolean flagExibeQtdSalarios = true;  
		//Verificação de polo passivo para calcular o teto máximo do rpv quando o réu 
		//for um orgão publico
		if(rpv.getInRpvPrecatorio().equals(RpvPrecatorioEnum.R) 
				& rpv.getReu() instanceof PessoaJuridica){
			PessoaJuridica pj = (PessoaJuridica) rpv.getReu();
			if(pj.getTipoOrgaoPublico() != null && 
			!pj.getTipoOrgaoPublico().equals(TipoOrgaoPublicoEnum.F)){
				if(pj.getTipoOrgaoPublico().equals(TipoOrgaoPublicoEnum.E)){
				  if(isNotNullAndNoZero(pj.getValorLimiteRpv())){
					  tetoMaximo = pj.getValorLimiteRpv();
					  flagExibeQtdSalarios = false;
				  } else {
					  qtdSalarios = 40;
				  }
				} else if(pj.getTipoOrgaoPublico().equals(TipoOrgaoPublicoEnum.M)){
					if(isNotNullAndNoZero(pj.getValorLimiteRpv())){
						tetoMaximo = pj.getValorLimiteRpv();
						flagExibeQtdSalarios = false;
					} else{
						qtdSalarios = 30;
					}					
				}
			}
		}
		
		if(tetoMaximo == 0){
			tetoMaximo = qtdSalarios * salarioMinimo;		
		}		
		
		double totalPagoBeneficiario = 0;
		double totalPagoHonorarioBeneficiario = 0;
		double totalPagoContratualAdv = 0;
		double totalPagoSucubenciaAdv = 0;
		double totalPagoExecucao = 0;
		double totalPagoSecaoJudiciaria = 0;
		
		for (RpvPessoaParte poloAtivo : rpv.getListaParteAtivo()) {
			//[Validações de Advogados]
			if(poloAtivo.getTipoParte().equals(tipoParteAdvogado)){
				if(!rpv.getInCreditoSomenteAdvogado()){
					poloAtivo.setValorPagoSucumbencia(0.0);
				}
				
				totalPagoContratualAdv += poloAtivo.getValorPagoContratual();
				totalPagoSucubenciaAdv += poloAtivo.getValorPagoSucumbencia();
				totalPagoExecucao += poloAtivo.getValorExecucao();
				
				if(rpv.getInRpvPrecatorio().equals(RpvPrecatorioEnum.P) && rpv.getInValorCompensar()
						&& !poloAtivo.getRpvParteValorCompensarList().isEmpty()){
					try {
						double limiteValorComp = rpvManager.calculaLimiteValorCompensarPRC(poloAtivo, false);
						double totalCompensar = totalVolorCompensarParte(poloAtivo);
						if(totalCompensar > limiteValorComp){
							FacesMessages.instance().add(Severity.ERROR, 
									"O valor máximo para o total de valor a compensar de " + poloAtivo.getPessoa() + " é de " + formatValorRS(limiteValorComp));							
							return false;	
						}
					} catch (RpvException e) {
						e.printStackTrace();
						FacesMessages.instance().add(Severity.ERROR, e.getMessage());
						return false;	
					}
				}				
				
				if(!poloAtivo.getRpvParteValorCompensarList().isEmpty() && !(poloAtivo.getValorIntegralDebito() > 0)){
					FacesMessages.instance().add(Severity.ERROR, 
					"O campo Valor Integral do Débito é obrigatório para o Beneficiário " + poloAtivo.getPessoa());
					return false;					
				}
							
				if(rpv.getInRpvPrecatorio().equals(RpvPrecatorioEnum.R)){
					if(poloAtivo.getValorExecucao() > tetoMaximo){
						if(flagExibeQtdSalarios){
							FacesMessages.instance().add(Severity.ERROR,
									"O valor máximo de execução permitido para requisitório " +
									"do tipo RPV é de "+ formatValorRS(tetoMaximo) + 
									" ("+ qtdSalarios +" salários mínimos)");
						} else {
							FacesMessages.instance().add(Severity.ERROR,
									"O valor máximo de execução permitido para requisitório " +
									"do tipo RPV é de "+ formatValorRS(tetoMaximo));							
						}
						return false;					
					}
				}
				
				if(rpv.getInRpvPrecatorio().equals(RpvPrecatorioEnum.P) && rpv.getInValorCompensar() &&
				   totalVolorCompensarParte(poloAtivo) > tetoMaximo){
					FacesMessages.instance().add(Severity.ERROR, 
					"O valor a compensar de " + poloAtivo.getPessoa() + " excede o limite permitido.");
					return false;				
				}
			//[Validação Seção Judiciária]	
			} else if(poloAtivo.getPessoa().equals(secaoJudiciaria)){
				if(rpv.getAssuntoPrincipal().getInExigeNM()){
					poloAtivo.setMotivoNaoPreenchimentoNumMes("Seção Judiciária");
				}
				totalPagoSecaoJudiciaria += poloAtivo.getValorPagoPessoa();
			//[Validação do Beneficiário Real]	
			} else if(isBeneficiarioReal(poloAtivo.getPessoa())){
				
				if(!isNotNullAndNoZero(poloAtivo.getNumeroMesExercicioAnterior())){
					poloAtivo.setValorExercicioAnterior(0.0);
				}
				
				if(isNotNullAndNoZero(poloAtivo.getNumeroMesExercicioAnterior())){
					poloAtivo.setMotivoNaoPreenchimentoNumMes(null);
				}
				
				if(!isNotNullAndNoZero(poloAtivo.getNumeroMesExercicioCorrente())){
					poloAtivo.setValorExercicioCorrente(0.0);
				}

				if(rpv.getAssuntoPrincipal().getInExigeNM() && 
					(isNotNullAndNoZero(poloAtivo.getValorExercicioAnterior()) ||
					 isNotNullAndNoZero(poloAtivo.getValorExercicioAnterior()))
					 && Strings.isEmpty(poloAtivo.getMotivoNaoPreenchimentoNumMes())){
					
					double totalNM = poloAtivo.getValorExercicioAnterior() + poloAtivo.getValorExercicioCorrente();
					
					if(!(totalNM == poloAtivo.getValorPagoPessoa())){
						FacesMessages.instance().add(Severity.ERROR, 
						"A soma do 'Valor Exercício Anterior' com o 'Valor Exercício Corrente' deve ser igual ao 'Valor (sem Honorários Contratuais/Cessão)'.");
					return false;												
					}
				}
				
				if(rpv.getAssuntoPrincipal().getPss() && !poloAtivo.getInIsentoPss()){
					
					if(poloAtivo.getInIsentoPss() && poloAtivo.getValorPss() > 0){
						poloAtivo.setValorPss(0.0);
					}
					
					if(poloAtivo.getValorPss() > poloAtivo.getValorPagoPessoa()){
						FacesMessages.instance().add(Severity.ERROR, 
								"O Valor do PSSS tem de ser menor ou igual ao Valor (sem Honorários Contratuais/Cessão).");
						return false;						
					}
				}				
				
				totalPagoExecucao += poloAtivo.getValorExecucao();
				
				if(rpv.getValorCustas() != null && rpv.getValorCustas() > 0 && rpv.getInRessarcimentoCustas()){
				   poloAtivo.setValorPagoPessoa(rpv.getValorCustas());
				}
				
				if (rpv.getInRpvPrecatorio().equals(RpvPrecatorioEnum.P) && rpv.getInValorCompensar() &&
					!poloAtivo.getRpvParteValorCompensarList().isEmpty()){
					if(!(rpv.getDataBaseCalculo().equals(poloAtivo.getDataBaseValorCompensar()) 
						&& rpv.getDataBaseCalculo().equals(poloAtivo.getDataTransitoJulgadoCompensa()))){
						FacesMessages.instance().add(Severity.ERROR, "A 'Data Base de Cálculo do requisitório', a 'Data "+
														"Base do Valor a Compensar' e a 'Data do Trânsito em Julgado da Decisão " +
														"que Homologou a Compensação' devem ser iguais.");
						return false;						
					}
				}
				
				if (rpv.getInRpvPrecatorio().equals(RpvPrecatorioEnum.P) && rpv.getInValorCompensar()
					&& totalVolorCompensarParte(poloAtivo) > tetoMaximo) {
					FacesMessages.instance().add(Severity.ERROR, "O valor a compensar de " + 
							poloAtivo.getPessoa() + " excede o limite permitido.");
					return false;
				}				
				
				totalPagoBeneficiario += poloAtivo.getValorPagoPessoa();
				totalPagoHonorarioBeneficiario += poloAtivo.getValorHonorario();
				
				if(rpv.getInRpvPrecatorio().equals(RpvPrecatorioEnum.P) && rpv.getInValorCompensar()){
					try {
						double limiteValorComp = rpvManager.calculaLimiteValorCompensarPRC(poloAtivo, true);
						double totalCompensar = totalVolorCompensarParte(poloAtivo);
						if(totalCompensar > limiteValorComp){
							FacesMessages.instance().add(Severity.ERROR, 
									"O valor máximo para o total de valor a compensar de " + poloAtivo.getPessoa() + " é de " + formatValorRS(limiteValorComp));							
							return false;	
						}
					} catch (RpvException e) {
						e.printStackTrace();
						FacesMessages.instance().add(Severity.ERROR, e.getMessage());
						return false;	
					}
				}
			}
		}
		
		double totalPagoPerito = 0;
		if(rpv.getInPagamentoDiretoPerito()){
			for (RpvPessoaParte poloTerceiro : rpv.getListaParteTerceiro()) {
				if(poloTerceiro.getTipoParte().equals(tipoPartePerito)){
					totalPagoPerito += poloTerceiro.getValorPagoPessoa(); 
				}
			}
			
			if(totalPagoPerito == 0){
				FacesMessages.instance().add(Severity.ERROR, 
				"O valor do(s) Perito(s) é(são) de preenchimento obrigatório.");
				return false;
			}			
		}
		
		double totalPagoCessionario = 0;
		if(rpv.getInCessionario()){
			for (RpvPessoaParte poloTerceiro : rpv.getListaParteTerceiro()) {
				if(poloTerceiro.getTipoParte().equals(tipoParteCessionario)){
					totalPagoCessionario += poloTerceiro.getValorPagoPessoa();
				}
			}		
			if(totalPagoCessionario == 0){
				FacesMessages.instance().add(Severity.ERROR, 
				"O valor da Cessão é de preenchimento obrigatório.");
				return false;
			}
			
			if(rpv.getTipoCessao().equals(RpvTipoCessaoEnum.P) && totalPagoBeneficiario == 0){
				FacesMessages.instance().add(Severity.ERROR, 
				"Em requisições do tipo parcial é obrigatório que o cedente possua valor.");
				return false;				
			}
		}
		
		if(totalPagoHonorarioBeneficiario > 0 || totalPagoCessionario > 0 || totalPagoContratualAdv > 0){
			double totalHonorarioComCessao = totalPagoContratualAdv + totalPagoCessionario;
			if(totalPagoHonorarioBeneficiario != totalHonorarioComCessao){
				FacesMessages.instance().add(Severity.ERROR, 
				"O total Honorários Contratuais/Cessão devem ser iguais ao total do Valor dos Honorários Contratuais/Cessão.");
				return false;			
			}
		}
		double custas = 0;
		
		if(!rpv.getInRessarcimentoCustas()){
			custas = rpv.getValorCustas() == null ? 0 : rpv.getValorCustas();
		}
		
		double valorRequisitado = totalPagoBeneficiario + totalPagoContratualAdv + 
				totalPagoSucubenciaAdv + totalPagoSecaoJudiciaria + 
				totalPagoPerito + totalPagoCessionario + custas;
		
		if(rpv.getInRpvPrecatorio().equals(RpvPrecatorioEnum.R)){
			if(isParteNaoAssistidaPorAdvogado()){
				qtdSalarios = 20;
				flagExibeQtdSalarios = true;
				tetoMaximo = qtdSalarios * salarioMinimo;
			} 
			
			if(valorRequisitado > tetoMaximo){
				if(flagExibeQtdSalarios){
					FacesMessages.instance().add(Severity.ERROR, 
							"O valor devido ao beneficiário somado aos honorários " +
							"contratuais não deve ultrapassar " + formatValorRS(tetoMaximo) +
							" (" + qtdSalarios + " salários mínimos)");
				} else {
					FacesMessages.instance().add(Severity.ERROR, 
							"O valor devido ao beneficiário somado aos honorários " +
							"contratuais não deve ultrapassar " + formatValorRS(tetoMaximo));					
				}
				return false;				
			}
			
		}
		
		rpv.setValorTotalExecucao(totalPagoExecucao);
		
		rpv.setValorRequisitado(valorRequisitado);
		
		return true;
	}
	
	private boolean validaRepresentantesBeneficiarioReal(){
		RpvPessoaParte beneficiarioReal = getRpvParteBeneficiarioReal();
		List<RpvParteRepresentante> representantesBeneficiario = new ArrayList<RpvParteRepresentante>(0);
		for (RpvParteRepresentante rep : beneficiarioReal.getRpvRepresentanteList()) {
			if(isTipoRepresentante(rep.getRpvPessoaRepresentante().getTipoParte())){
				representantesBeneficiario.add(rep);
			}
		}
		
		if(beneficiarioReal.getIncapaz() && representantesBeneficiario.isEmpty()){
			if(existeRepresentanteParteProcesso()){
				FacesMessages.instance().add(Severity.ERROR,
						"O " + beneficiarioReal.getTipoParte() + " " + beneficiarioReal.getPessoa() + 
						" é incapaz e não possui representante associado. " +
						"Favor voltar a Tela 1 e selecionar um representante.");						
			} else {
				FacesMessages.instance().add(Severity.ERROR,
						"O " + beneficiarioReal.getTipoParte() + " " + beneficiarioReal.getPessoa() + 
						" é incapaz e não possui representante. Favor incluir dados do " +
				"representante em Consulta / Retificação da Autuação.");
			}
			return false;
		} else if(!beneficiarioReal.getIncapaz() && !representantesBeneficiario.isEmpty()){
			this.representante = null;
			for (RpvParteRepresentante rpvParteRepresentante : representantesBeneficiario) {
				rpvManager.remove(rpvParteRepresentante);
				beneficiarioReal.getRpvRepresentanteList().remove(rpvParteRepresentante);
				rpvManager.remove(rpvParteRepresentante.getRpvPessoaRepresentante());
				rpv.getRpvParteList().remove(rpvParteRepresentante.getRpvPessoaRepresentante());
			}
		}
		return true;
	}	
	
	/**
	 * Método usado para alertar a necessidade de atualizar a data base de claculo
	 * @return mensagem caso haja necessidade
	 */
	public String msgAjustarDataBaseCalculo(){
		if(rpv == null || rpv.getDataBaseCalculo() == null){
			return null;
		}
		long dias = DateUtil.diferencaDias(new Date(), rpv.getDataBaseCalculo());
		if(dias > 365){
			return "Favor verificar a necessidade de atualizar a Data da Base Cálculo.";
		}
		return null;
	}
	
	/**
	 * Retorna o valor liquido a receber do beneficiário
	 * @param rpvPessoaParte
	 * @return valorLiquidoReceber
	 */
	public double getValorLiquidoReceber(RpvPessoaParte rpvPessoaParte){
		double valorPago = rpvPessoaParte.getValorPagoPessoa();
		double valorPss = rpvPessoaParte.getValorPss();
		double valorIr = rpvPessoaParte.getValorIR();
		double valorDeducoes = totalVolorCompensarParte(rpvPessoaParte);
		
		if(rpvPessoaParte.getRpv().getValorCustas() != null && rpvPessoaParte.getRpv().getValorCustas() > 0){
			valorPago += rpvPessoaParte.getRpv().getValorCustas(); 
		}		
		
		return valorPago - (valorPss + valorIr + valorDeducoes); 
	}	
	
	/**
	 * Verifica se um valor não é nulo e maior que zero
	 * @param valor
	 * @return boolean
	 */
	public boolean isNotNullAndNoZero(Double valor){
		if(valor == null){
			return false;
		} else if(valor > 0){
			return true;			
		}
		return false;
	}
	
	/**
	 * Verifica se um valor não é nulo e maior que zero
	 * @param valor
	 * @return boolean
	 */
	public boolean isNotNullAndNoZero(Integer valor){
		if(valor == null){
			return false;
		} else if(valor > 0){
			return true;			
		}
		return false;
	}
	
	public double totalVolorCompensarParte(RpvPessoaParte parte){
		double total = 0;
		for(RpvParteValorCompensar valorCompensar : parte.getRpvParteValorCompensarList()){
			total += valorCompensar.getValorCompensar();
		}
		return total;
	}
	
	public String getCpfDataNascimento(Pessoa pessoa){
		if(pessoa != null){
			String dn = getDataNascimento(pessoa);
			dn = Strings.isEmpty(dn) ? "" : " Data de Nascimento: " + dn;
			return pessoa.getDocumentoCpfCnpj() + dn ;
		}
		return "";
	}
	
	public String getDataNascimento(Pessoa pessoa){
		if(pessoa != null){
			if (pessoa instanceof PessoaFisica) {
				return ((PessoaFisica) pessoa).getDataNascimentoFormatada();
			}
			if (EntityUtil.isHibernateProxy(pessoa.getClass())) {
				PessoaFisica pessoaFisica;
				try {
					pessoaFisica = EntityUtil.find(PessoaFisica.class, pessoa.getIdUsuario());
				} catch (EntityNotFoundException e) {
					return null;
				}			
				if(pessoaFisica != null){
					return pessoaFisica.getDataNascimentoFormatada();
				}
			}
		}
		return null;
	}
	
	public void cancelarRpv(int idRpv, String mensagem){
		Rpv rpv = (Rpv) EntityUtil.getEntityManager().find(Rpv.class, idRpv);
		rpv.setRpvStatus(ParametroUtil.instance().getStatusRpvCancelada());
		rpv.setMotivoCancelamento(mensagem);
		rpv.setDataCancelamento(new Date());
		rpv.setPessoaCancelamento(Authenticator.getPessoaLogada());
		rpvManager.update(rpv);
	}
	
	public void cancelarRpv() {
		if (Strings.isEmpty(idRpvCancelamento) || Strings.isEmpty(motivoCancelamento)) {
			FacesMessages.instance().add(Severity.ERROR, "O motivo do cancelamento é obrigatorio.");
			sucessInMethod = false;

		} else {
			cancelarRpv(Integer.parseInt(idRpvCancelamento), motivoCancelamento);
			FacesMessages.instance().add(Severity.INFO, "Rpv cancelada com sucesso.");
			sucessInMethod = true;

		}
		idRpvCancelamento = null;
		motivoCancelamento = null;
	}
	
	public void validarRpv(int idRpv){
		PessoaMagistrado pm = null;
		Rpv rpv = (Rpv) EntityUtil.getEntityManager().find(Rpv.class, idRpv);
		rpv.setRpvStatus(ParametroUtil.instance().getStatusRpvFinalizada());
		rpv.setDataValidacao(new Date());
		if(Pessoa.instanceOf(Authenticator.getPessoaLogada(), PessoaMagistrado.class)){
			pm = EntityUtil.find(PessoaMagistrado.class, Authenticator.getPessoaLogada().getIdUsuario());
		}
		if(rpv.getDataConferencia() == null){
			rpv.setDataConferencia(new Date());
		}
		rpv.setMagistradoValidacao(pm);
		rpvManager.update(rpv);
	}
	
	public void validarRpv() {
		if (rpv.getIdRpv() == 0) {
			FacesMessages.instance().add(Severity.ERROR, "Selecione uma RPV.");
			sucessInMethod = false;

		} else {
			validarRpv(rpv.getIdRpv());
			FacesMessages.instance().add(Severity.INFO, "Rpv validada com sucesso.");
			sucessInMethod = true;

		}
	}
	
	public void enviarRpvConferencia(int idRpv){
		Rpv rpv = (Rpv) EntityUtil.getEntityManager().find(Rpv.class, idRpv);
		rpv.setRpvStatus(ParametroUtil.instance().getStatusRpvEmConferencia());
		rpv.setPessoaConferencia(Authenticator.getPessoaLogada());
		rpvManager.update(rpv);
	}
	
	public void enviarRpvConferencia() {
		if (rpv.getIdRpv() == 0) {
			FacesMessages.instance().add(Severity.ERROR, "Selecione uma RPV.");
			sucessInMethod = false;

		} else {
			enviarRpvConferencia(rpv.getIdRpv());
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, "Rpv enviada para conferência com sucesso.");
			sucessInMethod = true;

		}
	}
	
	public void enviarRpvValidacao(int idRpv){
		Rpv rpv = (Rpv) EntityUtil.getEntityManager().find(Rpv.class, idRpv);
		rpv.setRpvStatus(ParametroUtil.instance().getStatusRpvEmValidacao());
		rpv.setDataConferencia(new Date());
		rpv.setPessoaConferencia(Authenticator.getPessoaLogada());
		rpvManager.update(rpv);
	}
	
	public void enviarRpvValidacao() {
		if (rpv.getIdRpv() == 0) {
			FacesMessages.instance().add(Severity.ERROR, "Selecione uma RPV.");
			sucessInMethod = false;

		} else {
			enviarRpvValidacao(rpv.getIdRpv());
			FacesMessages.instance().add(Severity.INFO, "Rpv enviada para validação com sucesso.");
			sucessInMethod = true;

		}
	}
	
	public void devolverRpv(int idRpv, String mensagem){
		Rpv rpv = (Rpv) EntityUtil.getEntityManager().find(Rpv.class, idRpv);
		rpv.setRpvStatus(ParametroUtil.instance().getStatusRpvDevolvida());
		rpv.setDataDevolucao(new Date());
		rpv.setPessoaDevolucao(Authenticator.getPessoaLogada());
		rpv.setMotivoDevolucao(mensagem);
		rpvManager.update(rpv);
	}
	
	public void devolverRpv() {
		if (rpv.getIdRpv() == 0) {
			FacesMessages.instance().add(Severity.ERROR, "Selecione uma RPV.");
			sucessInMethod = false;

		} else {
			devolverRpv(rpv.getIdRpv(), getMotivoDevolucao());
			FacesMessages.instance().add(Severity.INFO, "Rpv devolvida com sucesso.");
			sucessInMethod = true;

		}
	}
	/**
	 * Formata para valor monetário
	 * @param valor
	 * @return retorna valor no formato 1.000,00
	 */
	public String formatValorRS(double valor){
		 return NumberFormat.getCurrencyInstance().format(valor);
	}
	
	/**
	 * Lista de autores
	 * @return list Pessoa
	 */
	public List<Pessoa> getAutores(){
		List<ProcessoParte> processoParteAutoreList = 
			processoTrfManager.getProcessoParteAutoreList(processoTrf);
		List<Pessoa> autorList = new ArrayList<Pessoa>(0);
		for (ProcessoParte processoParte : processoParteAutoreList) {
			autorList.add(processoParte.getPessoa());
		}
		return autorList;
	}
	
	public void changeHerdeiro(){
		if(getHerdeiros().size() == 1){
			this.herdeiro = getHerdeiros().get(0);
		} else {
			this.herdeiro = null;
		}
	}
	
	/**
	 * Método que verificia se a parte do processo tem representante cadastrado 
	 * @return boolean
	 */	
	public boolean existeRepresentanteParteProcesso(){
		if (rpv.getBeneficiario() == null) {
			return false;
		}
		Pessoa representado = rpv.getBeneficiario(); 
		if (herdeiro != null) {
			representado = herdeiro;
		}
		
		List<ProcessoParteRepresentante> listRepresentante = 
			processoTrfManager.getListRepresentanteByPessoaAndProcessoTrf(processoTrf, representado);
		for (ProcessoParteRepresentante processoParteRepresentante : listRepresentante) {
			if(isTipoRepresentante(processoParteRepresentante.getParteRepresentante().getTipoParte())){
				return true;
			}
		}

		return false;
	}
	
	/**
	 * Método que retorna os representantes do beneficiário ou do herdeiro do beneficiário
	 * @return lista de Pessoa
	 */
	public List<Pessoa> getRepresentantes() {
		List<Pessoa> representanteList = new ArrayList<Pessoa>(0);
		if (rpv.getBeneficiario() == null) {
			return representanteList;
		}
		Pessoa representado = rpv.getBeneficiario(); 
		if (herdeiro != null) {
			representado = herdeiro;
		}
		if(existeRepresentanteParteProcesso()) {
			List<ProcessoParteRepresentante> repList = processoTrfManager
					.getListRepresentanteByPessoaAndProcessoTrf(
							processoTrf, representado);
			for (ProcessoParteRepresentante processoParteRepresentante : repList) {
				TipoParte tipoParte = processoParteRepresentante.getParteRepresentante().getTipoParte();
				if(isTipoRepresentante(tipoParte)){
					representanteList.add(processoParteRepresentante.getParteRepresentante().getPessoa());
				}
			}
		}
		return representanteList;
	}
	
	public void atualizaRepresentanteParte(){
		RpvParteRepresentante parteRep = null;
		for (RpvParteRepresentante rep : getRpvParteBeneficiarioReal().getRpvRepresentanteList()) {
			if(isTipoRepresentante(rep.getRpvPessoaRepresentante().getTipoParte())){
				parteRep = rep;
				break;
			}
		}
		
		if(this.representante == null && parteRep != null){
			rpvManager.remove(parteRep);
			getRpvParteBeneficiarioReal().getRpvRepresentanteList().remove(parteRep);
			rpvManager.remove(parteRep.getRpvPessoaRepresentante());
			rpv.getRpvParteList().remove(parteRep.getRpvPessoaRepresentante());
		} else if(existeRepresentanteParteProcesso() && this.representante != null && parteRep == null) {
			RpvPessoaParte pessoaParte = new RpvPessoaParte();
			pessoaParte.setTipoParte(getTipoParteRepresentante(this.representante));
			pessoaParte.setRpv(rpv);
			pessoaParte.setPessoa(this.representante);
			pessoaParte.setInParticipacao(RpvPessoaParteParticipacaoEnum.A);
			rpvManager.persist(pessoaParte);
			rpv.getRpvParteList().add(pessoaParte);			
			parteRep = new RpvParteRepresentante();
			parteRep.setRpvPessoaParte(getRpvParteBeneficiarioReal());
			parteRep.setRpvPessoaRepresentante(pessoaParte);
			rpvManager.persist(parteRep);
			getRpvParteBeneficiarioReal().getRpvRepresentanteList().add(parteRep);
		} else if(existeRepresentanteParteProcesso() && this.representante != null && parteRep != null 
				&& !parteRep.getRpvPessoaRepresentante().getPessoa().equals(this.representante)) {
			parteRep.getRpvPessoaRepresentante().setPessoa(this.representante);
			rpvManager.update(parteRep.getRpvPessoaRepresentante());
		}
	}
	
	/**
	 * Retorna o tipo parte do representante
	 * @param pessoa
	 * @return TipoParte
	 */
	private TipoParte getTipoParteRepresentante(Pessoa pessoa){
		List<ProcessoParteRepresentante> listRepresentante = 
			processoTrfManager.getListRepresentanteByPessoaAndProcessoTrf(processoTrf, getBeneficiarioReal());
		for (ProcessoParteRepresentante processoParteRepresentante : listRepresentante) {
			if(isTipoRepresentante(processoParteRepresentante.getParteRepresentante().getTipoParte())
			&& processoParteRepresentante.getParteRepresentante().getPessoa().equals(pessoa)){
				return processoParteRepresentante.getParteRepresentante().getTipoParte();
			}
		}
		return ParametroUtil.instance().getTipoParteRepresentante(); 
	}
	
	public boolean isObrigatorioRepresentante(){
		if(getBeneficiarioReal()!= null && 
				  (rpvManager.isInCapaz(getBeneficiarioReal()) || rpvManager.isMenor(getBeneficiarioReal())) 
				  && (getRpvParteBeneficiarioReal() == null || 
				  (getRpvParteBeneficiarioReal() != null && getRpvParteBeneficiarioReal().getIncapaz()))){
					return true;
				}		
				return false;
	}
	
	/**
	 * Herdeiros do beneficiáro
	 * @return lista de pessoas
	 */
	public List<Pessoa> getHerdeiros(){
		List<Pessoa> herdeiroList = new ArrayList<Pessoa>(0);
		if(rpv.getBeneficiario() == null){
			return herdeiroList;
		}
		if(rpvManager.isObito(rpv.getBeneficiario())){
			List<ProcessoParteRepresentante> representantes = 
				processoTrfManager.getListRepresentanteByPessoaAndProcessoTrf(processoTrf, 
						rpv.getBeneficiario());
			TipoParte tipoParteHerdeiro = ParametroUtil.instance().getTipoParteHerdeiro();
			for (ProcessoParteRepresentante processoParteRepresentante : representantes) {
				if(processoParteRepresentante.getTipoRepresentante() == tipoParteHerdeiro){
					herdeiroList.add(processoParteRepresentante.getRepresentante());
				}	
			}
			return herdeiroList;
		}
		return herdeiroList;
	}
	
	/**
	 * Verifica se na rpv tem partes incapazes
	 * @return true ou false
	 */
	public boolean existeParteIncapazRpv(){
		if(rpv == null){
			return false;
		}
		for (RpvPessoaParte pessoaParte : rpv.getRpvParteList()) {
			if(rpvManager.isInCapaz(pessoaParte.getPessoa()) || pessoaParte.getIncapaz()){
				return true;
			}
		}
		return false;
	}
	
	public boolean mostrarBotaoCancelar(int idRpv){
		Rpv rpv = EntityUtil.find(Rpv.class, idRpv);
		RpvStatus statusRpvEmElaboracao = ParametroUtil.instance().getStatusRpvEmElaboracao();
		RpvStatus statusRpvEmConferencia = ParametroUtil.instance().getStatusRpvEmConferencia();
		RpvStatus statusRpvEmValidacao = ParametroUtil.instance().getStatusRpvEmValidacao();
		RpvStatus statusRpvDevolvida = ParametroUtil.instance().getStatusRpvDevolvida();
		RpvStatus statusRpvFinalizada = ParametroUtil.instance().getStatusRpvFinalizada();
		RpvStatus statusRpvElaborarado = ParametroUtil.instance().getStatusRpvElaborado();
		Papel diretorSecretaria = ParametroUtil.instance().getPapelDiretorSecretaria();
		Papel papelMagistrado = ParametroUtil.instance().getPapelMagistrado();
		
		if(rpv.getRpvStatus().equals(statusRpvEmElaboracao) || rpv.getRpvStatus().equals(statusRpvElaborarado)){
			return true;
		}
		if(rpv.getRpvStatus().equals(statusRpvEmConferencia) && Authenticator.getPapelAtual() == diretorSecretaria){
			return true;
		}		
		if(rpv.getRpvStatus().equals(statusRpvEmValidacao) && Authenticator.getPapelAtual() == papelMagistrado){
			return true;
		}
		if(rpv.getRpvStatus().equals(statusRpvFinalizada) && 
				Authenticator.getPapelAtual() == papelMagistrado && !rpv.getReu().getAtraiCompetencia()){
			//TODO Aguardando verifcação para saber quando um réu é orgão federal (Pessoa atrai competencia)
			return true;
		}
		return false;
	}

	public void gerarPNGBarcode(OutputStream out, Object data) throws IOException {
		if (this.rpv == null) {
			return;
		}
		Barcode128 code128 = new Barcode128();
		code128.setCode(rpv.getNumeroRpvPrecatorio());
		java.awt.Image createAwtImage = code128.createAwtImage(Color.BLACK, Color.WHITE);

		BufferedImage bffImg 
        	= new BufferedImage(createAwtImage.getWidth(null),createAwtImage.getHeight(null), BufferedImage.TYPE_3BYTE_BGR); 
		Graphics offg = bffImg.createGraphics(); 
		offg.drawImage(createAwtImage, 0, 0, null); 	
		ImageIO.write(bffImg, "png", out);
	}

	public String numeroExtenso(BigDecimal num){
	    MoneyFormat mf = new MoneyFormat();
	    //Locale set para ENGLISH pois o banco retorna o valor com separadores de decimal como . (ponto)
	    DecimalFormat formatarDouble = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
	    formatarDouble.setMaximumFractionDigits(2);
	    String result;
	    String numero[] = new String[1];
	    numero[0] = formatarDouble.format(num);
	    result = mf.execute(numero);
	    return result;
	}
	
	public String getOabAdvogado(Pessoa p){
		if(Pessoa.instanceOf(p, PessoaAdvogado.class)){
			PessoaAdvogado pa = EntityUtil.find(PessoaAdvogado.class, p.getIdUsuario());
			if(pa != null){
				return pa.getOabFormatado();
			}
		}
		return "";
	}
	
	public void calculaPercentualAdvogados(RpvPessoaParte beneficiario){
		double valorHonorarios = 0;
		double valorComHonorarios = beneficiario.getValorPagoPessoa();
		if(valorComHonorarios == valorPagoPessoaAnterior){
			return;
		}
		for (RpvPessoaParte parte : rpv.getListaParteAtivo()) {
			if(parte.getTipoParte().equals(ParametroUtil.instance().getTipoParteAdvogado())){
				double valorPagoContratual = (parte.getValorPercentualHonorario() * valorComHonorarios) / 100;
				valorHonorarios += valorPagoContratual;
				parte.setValorPagoContratual(valorPagoContratual);
			}
		}
		valorPagoPessoaAnterior = valorComHonorarios - valorHonorarios;
		beneficiario.setValorPagoPessoa(valorComHonorarios - valorHonorarios);
		beneficiario.setValorHonorario(valorHonorarios);
		
	}
	
	public void montarPoloAtivoRpv(Rpv rpv) {
		List<ProcessoParte> processoParteList = rpv.getProcessoTrf()
				.getProcessoParteList();
		TipoParte tipoParteBeneficiario = null;
		for (ProcessoParte processoParte : processoParteList) {
			if (processoParte.getPessoa().equals(rpv.getBeneficiario())
					&& processoParte.getInParticipacao().equals(
							ProcessoParteParticipacaoEnum.A)) {
				tipoParteBeneficiario = processoParte.getTipoParte();	
				//Adiciona o beneficiário
				RpvPessoaParte parteBeneficiario = new RpvPessoaParte();
				parteBeneficiario.setPessoa(rpv.getBeneficiario());
				parteBeneficiario.setIncapaz(rpvManager.isInCapaz(rpv.getBeneficiario()) || 
											 rpvManager.isMenor(rpv.getBeneficiario()));				
				parteBeneficiario.setTipoParte(processoParte.getTipoParte());
				parteBeneficiario.setRpv(rpv);
				parteBeneficiario.setInParticipacao(RpvPessoaParteParticipacaoEnum.A);
				rpvManager.persisteRpvParte(parteBeneficiario);
				rpv.getRpvParteList().add(parteBeneficiario);
				
				//Adiciona o herdeiro caso exista
				RpvPessoaParte parteHerdeiro = new RpvPessoaParte();
				if (herdeiro != null) {
					TipoParte tipoParteHerdeiro = ParametroUtil.instance().getTipoParteHerdeiro();
					parteHerdeiro.setPessoa(herdeiro);
					parteHerdeiro.setIncapaz(rpvManager.isInCapaz(herdeiro));
					parteHerdeiro.setRpv(rpv);
					parteHerdeiro.setTipoParte(tipoParteHerdeiro);
					parteHerdeiro.setInParticipacao(RpvPessoaParteParticipacaoEnum.A);
					rpvManager.persisteRpvParte(parteHerdeiro);
					rpv.getRpvParteList().add(parteHerdeiro);
					
					RpvParteRepresentante representanteRpvHe = new RpvParteRepresentante();
					representanteRpvHe.setRpvPessoaParte(parteBeneficiario);
					representanteRpvHe.setRpvPessoaRepresentante(parteHerdeiro);
					rpvManager.persisteRpvRepresentante(representanteRpvHe);
					parteBeneficiario.getRpvRepresentanteList().add(representanteRpvHe);
				}
				
				//Adicona representante ao beneficiario ou herdeiro caso exista
				RpvPessoaParte representado = parteBeneficiario; 
				if (herdeiro != null) {
					representado = parteHerdeiro;
				}
				if (existeRepresentanteParteProcesso() && representante != null) {

					RpvPessoaParte parteRepresentante = new RpvPessoaParte();
					parteRepresentante.setPessoa(representante);
					parteRepresentante.setIncapaz(rpvManager.isInCapaz(representante));
					parteRepresentante.setRpv(rpv);
					parteRepresentante.setTipoParte(getTipoParteRepresentante(representante));
					parteRepresentante.setInParticipacao(RpvPessoaParteParticipacaoEnum.A);
					rpvManager.persisteRpvParte(parteRepresentante);
					rpv.getRpvParteList().add(parteRepresentante);
					
					RpvParteRepresentante representanteRpv = new RpvParteRepresentante();
					representanteRpv.setRpvPessoaParte(representado);
					representanteRpv.setRpvPessoaRepresentante(parteRepresentante);
					rpvManager.persisteRpvRepresentante(representanteRpv);
					representado.getRpvRepresentanteList().add(representanteRpv);
				}

				//Adicionando advogados da parte ativa
				List<ProcessoParteRepresentante> listAdvogados = 
								processoParte.getProcessoParteRepresentanteList();
				
				for (ProcessoParteRepresentante advogado : listAdvogados) {
					TipoParte tipoAdvogado = ParametroUtil.instance().getTipoParteAdvogado();					
					if(advogado.getParteRepresentante() != null &&
						advogado.getParteRepresentante().getTipoParte().equals(tipoAdvogado)){
						RpvPessoaParte parteAdv = new RpvPessoaParte();
						parteAdv.setPessoa(advogado.getParteRepresentante().getPessoa());
						parteAdv.setIncapaz(rpvManager.isInCapaz(advogado.getParteRepresentante().getPessoa()));
						parteAdv.setRpv(rpv);
						parteAdv.setTipoParte(tipoAdvogado);
						parteAdv.setInParticipacao(RpvPessoaParteParticipacaoEnum.A);
						rpvManager.persisteRpvParte(parteAdv);
						rpv.getRpvParteList().add(parteAdv);
						
						RpvParteRepresentante representanteRpvAdv = new RpvParteRepresentante();
						representanteRpvAdv.setRpvPessoaParte(parteBeneficiario);
						representanteRpvAdv.setRpvPessoaRepresentante(parteAdv);
						rpvManager.persisteRpvRepresentante(representanteRpvAdv);
						parteBeneficiario.getRpvRepresentanteList().add(representanteRpvAdv);
					}
				}				
				
			}
		}
		
		//Adiciona seção como parte
		if(rpv.getInReembolsoHonorariosSecao()){
			RpvPessoaParte secaoParte = new RpvPessoaParte();
			secaoParte.setPessoa(ParametroUtil.instance().getPessoaSecaoJudiciaria());
			TipoParte tipoParteAutor = ParametroUtil.instance().getTipoParteAutor();
			secaoParte.setTipoParte(tipoParteBeneficiario != null ? tipoParteBeneficiario : tipoParteAutor);
			secaoParte.setRpv(rpv);
			secaoParte.setInParticipacao(RpvPessoaParteParticipacaoEnum.A);
			rpvManager.persisteRpvParte(secaoParte);
			rpv.getRpvParteList().add(secaoParte);
		}
	}
	
	public String getMotivoCancelamento() {
		return motivoCancelamento;
	}
	
	public void setMotivoCancelamento(String motivoCancelamento) {
		this.motivoCancelamento = motivoCancelamento;
	}
	
	public String getIdRpvCancelamento() {
		return idRpvCancelamento;
	}
	
	public void setIdRpvCancelamento(String idRpvCancelamento) {
		this.idRpvCancelamento = idRpvCancelamento;
	}
	
	public String getMensagemOutrosRessarcimentoCustas() {
		return mensagemOutrosRessarcimentoCustas;
	}
	
	/**
	 * Ao pegar a parte é atualizado os dados de dedução
	 * @return RpvPessoaParte
	 */
	public RpvPessoaParte getParteSelecionadaModalDeducao() {
		return this.parteSelecionadaModalDeducao;
	}
	
	public void setParteSelecionadaModalDeducaoId(String idParteStr){
		int idParte = Integer.parseInt(idParteStr);
		this.parteSelecionadaModalDeducao = EntityUtil.find(RpvPessoaParte.class, idParte);
		
		double honorariosAdv = this.parteSelecionadaModalDeducao.getValorHonorario();
		
		RpvParteDeducao rpvParteDeducao;
		if(this.parteSelecionadaModalDeducao.getRpvParteDeducao() == null) {
			rpvParteDeducao = new RpvParteDeducao();
			rpvParteDeducao.setRpvPessoaParte(this.parteSelecionadaModalDeducao);
			rpvParteDeducao.setValorHonorarioAdvogado(honorariosAdv);
			rpvParteDeducao.setValorPss(this.parteSelecionadaModalDeducao.getValorPss());
			this.parteSelecionadaModalDeducao.setRpvParteDeducao(rpvParteDeducao);
			rpvManager.persist(rpvParteDeducao);
		} else {
			this.parteSelecionadaModalDeducao.getRpvParteDeducao().setValorHonorarioAdvogado(honorariosAdv);
			this.parteSelecionadaModalDeducao.getRpvParteDeducao().setValorPss(this.parteSelecionadaModalDeducao.getValorPss());
			rpvManager.update(this.parteSelecionadaModalDeducao.getRpvParteDeducao());
		}
	}
	
	public void setParteSelecionadaModalDeducao(RpvPessoaParte parteSelecionadaModalDeducao) {
		this.parteSelecionadaModalDeducao = parteSelecionadaModalDeducao;
	}
	
	/**
	 *
	 * @return RpvPessoaParte
	 */
	public RpvPessoaParte getParteSelecionadaModalValorCompensar() {
		return this.parteSelecionadaModalValorCompensar;
	}
	
	public void setParteSelecionadaModalValorCompensarId(String idParteStr){
		int idParte = Integer.parseInt(idParteStr);
		this.parteSelecionadaModalValorCompensar = EntityUtil.find(RpvPessoaParte.class, idParte);
	}
	
	public void setParteSelecionadaModalValorCompensar(RpvPessoaParte parteSelecionadaModalValorCompensar) {
		this.parteSelecionadaModalValorCompensar = parteSelecionadaModalValorCompensar;
	}
	
	public double getValorCompensarModalCompensar() {
		return valorCompensarModalCompensar;
	}
	
	public void setValorCompensarModalCompensar(double valorCompensarModalCompensar) {
		this.valorCompensarModalCompensar = valorCompensarModalCompensar;
	}
	
	public String getIdentificacaoModalCompensar() {
		return identificacaoModalCompensar;
	}
	
	public void setIdentificacaoModalCompensar(String identificacaoModalCompensar) {
		this.identificacaoModalCompensar = identificacaoModalCompensar;
	}
	
	public RpvNaturezaDebito getRpvNaturezaDebito() {
		return rpvNaturezaDebito;
	}
	
	public void setRpvNaturezaDebito(RpvNaturezaDebito rpvNaturezaDebito) {
		this.rpvNaturezaDebito = rpvNaturezaDebito;
	}
	
	public List<RpvPessoaParte> getAdvogadosSelecionadosBeneficiarioList() {
		return advogadosSelecionadosBeneficiarioList;
	}
	
	public void setAdvogadosSelecionadosBeneficiarioList(
			List<RpvPessoaParte> advogadosSelecionadosBeneficiarioList) {
		this.advogadosSelecionadosBeneficiarioList = advogadosSelecionadosBeneficiarioList;
	}
	
	public Boolean emValidacao(){
		RpvStatus statusRpvEmValidacao = ParametroUtil.instance().getStatusRpvEmValidacao();
		return rpv.getRpvStatus().equals(statusRpvEmValidacao);
	}
	
	public Boolean emConferencia(){
		RpvStatus statusRpvEmConferencia = ParametroUtil.instance().getStatusRpvEmConferencia();
		return rpv.getRpvStatus().equals(statusRpvEmConferencia);
	}
	
	public Boolean emElaboracao(){
		RpvStatus statusRpvEmElaboracao = ParametroUtil.instance().getStatusRpvEmElaboracao();
		return rpv.getRpvStatus().equals(statusRpvEmElaboracao);
	}
	
	public boolean devolvida(){
		RpvStatus statusRpvDevolvida = ParametroUtil.instance().getStatusRpvDevolvida();
		return rpv.getRpvStatus().equals(statusRpvDevolvida);
	}	
	
	public boolean elaborado(){
		RpvStatus statusRpvElaborarado = ParametroUtil.instance().getStatusRpvElaborado();
		return rpv.getRpvStatus().equals(statusRpvElaborarado);		
	}
	
	public Boolean finalizada(){
		RpvStatus statusRpvFinalizada = ParametroUtil.instance().getStatusRpvFinalizada();
		return rpv.getRpvStatus().equals(statusRpvFinalizada);
	}
	
	/**
	 * Traz o nome do Juiz Federal da Vara selecionada (Orgão Julgador)
	 * @return String com o nome
	 */	
	public String getJuizFederal(){
		Usuario juizFederal = processoTrfManager.getJuizFederal(processoTrf.getOrgaoJulgador());
		return juizFederal != null ? juizFederal.getNome() : "";
	}

	public String getMotivoDevolucao() {
		return motivoDevolucao;
	}

	public void setMotivoDevolucao(String motivoDevolucao) {
		this.motivoDevolucao = motivoDevolucao;
	}
	
	/**
	 * Verifica se a data base de cálculo informada é relativa a anos anteriores ao atual
	 * @param dataBase
	 * @return true ou false
	 */
	public boolean isDataBaseAnosAnteriores(Date dataBase){
		Calendar calendar = Calendar.getInstance();
		int anoAtual = calendar.get(Calendar.YEAR);
		calendar.setTime(dataBase);
		int anoBase = calendar.get(Calendar.YEAR);
		return anoBase < anoAtual;
	}

	/**
	 * Valida se o mesExercicioCorrente é igual ou menor que a quantidade de meses 
	 * informada na data base de cálculo, isso se a data base informada for do ano 
	 * corrente.
	 * @param RpvPessoaParte
	 */
	@SuppressWarnings("unchecked")
	public void validaNMExercicioCorrente(ActionEvent actionEvent){
		EntityNode<RpvPessoaParte> node = 
			(EntityNode<RpvPessoaParte>) 
			FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get("row");
		RpvPessoaParte rpvPessoaParte = node.getEntity();
		Integer mesExercicioCorrente = rpvPessoaParte.getNumeroMesExercicioCorrente();
		if(mesExercicioCorrente == null || mesExercicioCorrente.equals(0)){
			return;
		}
		Calendar calendar = Calendar.getInstance();
		int anoAtual = calendar.get(Calendar.YEAR);
		calendar.setTime(rpv.getDataBaseCalculo());
		int anoBase = calendar.get(Calendar.YEAR);
		if(anoAtual == anoBase){
			int mesBase = calendar.get(Calendar.MONTH);
			mesBase += 1;
			if(mesExercicioCorrente > mesBase){
				rpvPessoaParte.setNumeroMesExercicioCorrente(null);
				FacesMessage facesMessage = new FacesMessage("NM Ex. Corrente inválido para a data base de cálculo informada.");
				String clientId = actionEvent.getComponent().getParent().getClientId(
						FacesContext.getCurrentInstance());
				FacesContext.getCurrentInstance().addMessage(clientId, facesMessage);				
			}
		}
	}
	
	/**
	 * Retorna o valor dedução formatado
	 * @param rpvPessoaParte
	 * @return string
	 */
	public String valorDeducaoStr(RpvPessoaParte rpvPessoaParte){
		Double total = 0.0;
		if(rpvPessoaParte.getRpvParteDeducao() != null){
			RpvParteDeducao rpvParteDeducao = rpvPessoaParte.getRpvParteDeducao();
			total += rpvParteDeducao.getValorDespesaJudicial() != null ? rpvParteDeducao.getValorDespesaJudicial() : 0;
			total += rpvParteDeducao.getValorPensaoAlimenticia() != null ? rpvParteDeducao.getValorPensaoAlimenticia() : 0 ;
		}
		return NumberFormat.getCurrencyInstance().format(total);
	}
	
	public void updateDeducoes(){
		rpvManager.update(parteSelecionadaModalDeducao.getRpvParteDeducao());
		FacesMessages.instance().add(Severity.INFO, "Deduções atualizadas com sucesso.");
	}

	public void addValorCompensar(){

		if(rpvNaturezaDebito == null){
			FacesMessages.instance().add(Severity.ERROR, "A natureza de débitos é obrigatória.");
			return;
		}
		if(valorCompensarModalCompensar == 0){
			FacesMessages.instance().add(Severity.ERROR, "O valor a compensar tem de ser maior que 0.");
			return;
		}

		if(parteSelecionadaModalValorCompensar.getRpvParteValorCompensarList().size() == 15){
			FacesMessages.instance().add(Severity.ERROR, "Limite máximo de 15 compensações atingido.");
			return;			
		}
		
		RpvParteValorCompensar rpvParteValorCompensar = new RpvParteValorCompensar();
		rpvParteValorCompensar.setCodigoIdentificacao(identificacaoModalCompensar);
		rpvParteValorCompensar.setValorCompensar(valorCompensarModalCompensar);
		rpvParteValorCompensar.setRpvNaturezaDebito(rpvNaturezaDebito);
		rpvParteValorCompensar.setRpvPessoaParte(parteSelecionadaModalValorCompensar);
		
		rpvManager.persist(rpvParteValorCompensar);
		parteSelecionadaModalValorCompensar.getRpvParteValorCompensarList().add(rpvParteValorCompensar);
		identificacaoModalCompensar = null;
		valorCompensarModalCompensar = 0;
		rpvNaturezaDebito = null;
		FacesMessages.instance().add(Severity.INFO, "Compensação inserida com sucesso.");
	}

	/**
	 * Retorna o valor total a compensar formatado.
	 * 
	 * @param rpvPessoaParte Parte de onde serão obtidos os valores a compensar.
	 * @return o valor total a compensar formatado
	 */
	public String valorACompensarStr(RpvPessoaParte rpvPessoaParte){
		Double total = 0.0;
		List<RpvParteValorCompensar> rpvParteValorCompensarList = rpvPessoaParte.getRpvParteValorCompensarList();
		for (RpvParteValorCompensar rpvParteValorCompensar : rpvParteValorCompensarList) {
			total += rpvParteValorCompensar.getValorCompensar();
		}
		return NumberFormat.getCurrencyInstance().format(total) + " - Natureza de Débitos Compensados";
	}
		
	public Boolean isEletronica(){
		if(rpv != null && rpv.getIdRpv() != 0){
			boolean f = EntityUtil.find(RpvEletronica.class, rpv.getIdRpv()) != null;
			if(f){
				return true;
			}
		}
		return false;
	}

	public String getOabAndStatus(Pessoa pessoa){
		if(Pessoa.instanceOf(pessoa, PessoaAdvogado.class)){
			PessoaAdvogado advogado = ((PessoaFisica) pessoa).getPessoaAdvogado();
			String status = rpvManager.getStatusOab(advogado);
			String oab = advogado.getOabFormatado();
			
			return "(OAB: " + oab + " /Situação da OAB: " + status +")";
		}
		return null;
	}
	
	public boolean isParteNaoAssistidaPorAdvogado(){
		if(getRpvParteBeneficiarioReal().getRpvRepresentanteList().isEmpty()){
			return true;
		} 
		TipoParte tipoParteAdvogado = ParametroUtil.instance().getTipoParteAdvogado();
		for(RpvParteRepresentante rep : getRpvParteBeneficiarioReal().getRpvRepresentanteList()){
			if(rep.getRpvPessoaRepresentante().getTipoParte().equals(tipoParteAdvogado)){
				return false;
			}
		}
		return true;
	}	
	
	public Boolean showButtons(){
		if(rpv != null && rpv.getIdRpv() != 0){
			if(isEletronica() || rpv.getInRpvPrecatorio().equals(RpvPrecatorioEnum.P)){
				if(!rpv.getInEnvioTrf()){
					return true;
				}
			}else{
				return false;
			}
		}
		return false;
	}
	
	public String getConteudoHtml() {
		return conteudoHtml;
	}


	public void setConteudoHtml(String conteudoHtml) {
		this.conteudoHtml = conteudoHtml;
	}
	
	public String getCertChain() {
		return certChain;
	}


	public void setCertChain(String certChain) {
		this.certChain = certChain;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}
	
	public RpvEletronica cadastrarRpvEletronica(){
		RpvEletronica rpvEletronica = new RpvEletronica();
		try {
			validarRpv();
			PessoaMagistrado pm = null;
			rpvEletronica.setIdRpv(rpv.getIdRpv());
			rpvEletronica.setDocumentoRpv(conteudoHtml);
			rpvEletronica.setDataAssinatura(rpv.getDataValidacao());
			if(Pessoa.instanceOf(Authenticator.getPessoaLogada(), PessoaMagistrado.class)){
				pm = EntityUtil.find(PessoaMagistrado.class, Authenticator.getPessoaLogada().getIdUsuario());
			}
			rpvEletronica.setMagistradoAssinatura(pm);
			rpvManager.persist(rpv);
		} catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR, e.getMessage());
			log.error("Erro no cadastro de RpvEletronica: " + e.getMessage(), e);
		}
		return rpvEletronica;
	}
	
	public void assinarRpvEletronica(){
		RpvEletronica rpvEletronica = cadastrarRpvEletronica();
		if (rpvEletronica.getIdRpv() != 0){
			try {
				VerificaCertificadoPessoa.verificaCertificadoPessoaLogada(certChain);
				assinar(rpvEletronica);
				FacesMessages.instance().clear();
				FacesMessages.instance().add(Severity.INFO, "Rpv Assinada/Validada com Sucesso.");
			} catch (CertificadoException e) {
				String msgErro = "Erro na verificação do certificado: " + e.getMessage();
				FacesMessages.instance().add(Severity.ERROR, msgErro);
				log.error("Erro na verificação do certificado: " + e.getMessage(), e);
			}
		}
	}
	
	public void assinar(RpvEletronica rpvEletronica){
		try {
			rpvEletronica.setCertChain(certChain);
			rpvEletronica.setSignature(signature);
			rpvManager.update(rpvEletronica);
		} catch (PersistenceException e) {
			String msgErro = "Erro ao tentar assinar a rpv: " + e.getMessage();
			FacesMessages.instance().add(Severity.ERROR, msgErro);
			log.error("Erro ao tentar assinar a rpv: " + e.getMessage(), e);
		}

	}
	
	/**
	 * Retorna oa descrição do tipo documento natureza de débito passando como 
	 * parametro o código do tipo documento
	 * @param codTipo
	 * @return
	 */
	public String getTipoDocumentoNaturezaDebitoStr(String codTipo){	
		if(codTipo == null){
			return "";
		} else if(codTipo.equals("1")){
			return "DARF";
		} else if(codTipo.equals("2")){
			return "GPS";			
		} else if(codTipo.equals("3")){
			return "GRU";			
		} else {
			return codTipo;
		}
	}

	public void excluirValorCompensar(RpvParteValorCompensar rpvParteValorCompensar){
		rpvManager.remove(rpvParteValorCompensar);
		parteSelecionadaModalValorCompensar.getRpvParteValorCompensarList().remove(rpvParteValorCompensar);
		FacesMessages.instance().add(Severity.INFO, "Compensação removida com sucesso.");
	}
	
	/**
	 * Método que retorna o beneficiário real da rpv, isse se faz necessário 
	 * devido a regra de herdeiro
	 * @return pessoa
	 */
	public Pessoa getBeneficiarioReal(){
		if(herdeiro != null){
			return herdeiro;
		} else {
			return rpv.getBeneficiario();
		}
	}
	
	/**
	 * Verifica se uma pessoa é beneficiária da rpv
	 * @param pessoa
	 * @return boolean
	 */
	public boolean isBeneficiarioReal(Pessoa pessoa){
		return getBeneficiarioReal().equals(pessoa);
	}
	
	public boolean isPessoaParteSecao(Pessoa pessoa){
		return ParametroUtil.instance().getPessoaSecaoJudiciaria().equals(pessoa);
	}

	public void limparObservacaoSucumbencia(){
		if(!rpv.getInCreditoSomenteAdvogado()){
			rpv.setObservacaoSucumbencia(null);
		}
	}
	
	public void limparObsRessarcimentoCustas(){
		if(!rpv.getInRessarcimentoCustas()){
			rpv.setObsRessarcimentoCustas(null);
		}		
	}
	
	public String getPolosProcessoTrf(){
		return "(" + getPoloAtivoProcessoTrf(processoTrf) + ") X (" + getPoloPassivoProcessoTrf(processoTrf)+")";
	}

	 public String getPoloAtivoProcessoTrf(ProcessoTrf processoTrf) {
		return getParteProcessoTrf(processoTrf.getListaParteAtivo());
	 } 
	 
	 public String getPoloPassivoProcessoTrf(ProcessoTrf processoTrf) {
		return getParteProcessoTrf(processoTrf.getListaPartePassivo());
	 }

	 private String getParteProcessoTrf(List<ProcessoParte> partes) {
		if(partes.size() == 1){
		   return partes.get(0).getNomeParte();
		}
	  
		List<ProcessoParte> listParte = new ArrayList<ProcessoParte>();
		  for (ProcessoParte parte : partes) {
			   if(!parte.getTipoParte().getTipoParte().matches(("ADVOGADO|PROCURADOR"))){
				   listParte.add(parte);
			   }
		  }
		  String nome = "";
		  String sufixo = "";
		  if(!listParte.isEmpty()){
			   nome = listParte.get(0).getNomeParte();
			   int tam = listParte.size();
			   if(tam == 2){
				   sufixo = " e outro";
			   }else if(tam > 2){
				   sufixo = " e outros";
			   }
		 }
		 return nome + sufixo;
	 }
	 
	 public boolean validaValorPss(RpvPessoaParte rpvPessoaParte){
		 Double valorPss = rpvPessoaParte.getValorPss();
		 if(valorPss == null || valorPss == 0){
			 return true;	 
		 } else if(valorPss > rpvPessoaParte.getValorPagoPessoa()){
			 rpvPessoaParte.setValorPss(0.0);
			 FacesMessages.instance().add(Severity.ERROR, "O Valor do PSSS tem de ser menor ou igual ao Valor (sem Honorários Contratuais/Cessão).");
			 return false;
		 }
		 return true;
	 }
	 
	@SuppressWarnings("unchecked")
	public void validaValorPss(ActionEvent actionEvent) {
		EntityNode<RpvPessoaParte> node = (EntityNode<RpvPessoaParte>) FacesContext
				.getCurrentInstance().getExternalContext().getRequestMap().get(
						"row");

		RpvPessoaParte rpvPessoaParte = node.getEntity();
		Double valorPss = rpvPessoaParte.getValorPss();

		if (valorPss == null || valorPss == 0) {
			return;
		} else if (valorPss > rpvPessoaParte.getValorPagoPessoa()) {
			rpvPessoaParte.setValorPss(0.0);
			FacesMessage facesMessage = new FacesMessage(
					"O Valor do PSSS tem de ser menor ou igual ao Valor (sem Honorários Contratuais/Cessão).");
			String clientId = actionEvent.getComponent().getParent()
					.getClientId(FacesContext.getCurrentInstance());
			FacesContext.getCurrentInstance()
					.addMessage(clientId, facesMessage);
		}
	} 
	
	/**
	 * Método que recebe o label e um valor Double retornando o label concatendao
	 * com o valor formatado, caso o calor seja nulo ou zero retorna o label 
	 * concatenado com um traço  
	 * @param label
	 * @param valor
	 * @return Ex.: Valor PSS: R$ 20,00 ou Valor Pss: - 
	 */
	public String formataValorOficio(String label, Double valor){
		if(isNotNullAndNoZero(valor)){
			return label + formatValorRS(valor);
		}
		return label;
	}
	
	public Boolean getIsOficio() {
		return isOficio;
	}

	public void setIsOficio(Boolean isOficio) {
		this.isOficio = isOficio;
	}

	public void changeIsOficio(){
		setIsOficio(Boolean.TRUE);
		searchTree2GridPoloAtivoList = null;
	}	
}
