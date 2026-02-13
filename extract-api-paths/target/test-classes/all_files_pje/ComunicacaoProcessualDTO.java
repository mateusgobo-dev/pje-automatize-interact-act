package br.jus.cnj.pje.webservice.client.domicilioeletronico.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.hibernate.LazyInitializationException;

import br.com.infox.cliente.component.ValidacaoAssinaturaProcessoDocumento;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.webservice.client.domicilioeletronico.converter.TipoProcessoDocumentoParaTipoIntimacaoConverter;
import br.jus.cnj.pje.webservice.client.domicilioeletronico.dto.enums.FormatoAudienciaEnum;
import br.jus.cnj.pje.webservice.client.domicilioeletronico.dto.enums.TipoAcaoEnum;
import br.jus.cnj.pje.webservice.client.domicilioeletronico.dto.enums.TipoComunicacaoEnum;
import br.jus.cnj.pje.webservice.client.domicilioeletronico.dto.enums.TipoIntimacaoEnum;
import br.jus.cnj.pje.webservice.client.domicilioeletronico.dto.enums.TipoPrazoEnum;
import br.jus.pje.nucleo.entidades.ProcessoAudiencia;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;
import br.jus.pje.nucleo.enums.SimNaoEnum;
import br.jus.pje.nucleo.util.DateUtil;

/**
 * Classe que representa uma comunicação processual usado pelo Domicílio
 * Eletrônico.
 * 
 */
public class ComunicacaoProcessualDTO implements Serializable {

	
	private static final long serialVersionUID = 1L;
	
	private String numeroComunicacao;
	private String numeroProcesso;
	private String nomeDestinatario;
	private String documentoDestinatario;
	private String tribunalOrigem;
	private String dataAjuizamento;
	private TipoComunicacaoEnum tipoComunicacao;
	private Boolean segredoJustica;
	private SimNaoEnum cienciaAutomatica;
	private String dataFinalCiencia;
	private Integer prazo;
	private TipoPrazoEnum tipoPrazo;
	private String nomeServidor;
	private String cpfServidor;
	private String sistemaProcesso;
	private Integer meioEnvio;
	private String leitoresAutorizados;
	private String linksDocumentos;
	private TipoAcaoEnum tipoAcao;
	private String dataAudiencia;
	private String horaAudiencia;
	private Integer tipoAudiencia;
	private FormatoAudienciaEnum formatoAudiencia;
	private Integer tipoIntimacao;
	private SimNaoEnum urgente;
	private SimNaoEnum juizoDigital;
	private FaseAtualDTO processo;
	private List<String> linksDocumentosAdicionais = new ArrayList<>();
	private Boolean comunicacaoPessoal;

	/**
	 * Construtor.
	 * 
	 * @param ppe
	 */
	public ComunicacaoProcessualDTO() {
		// Construtor.
	}
	
	/**
	 * Construtor.
	 * 
	 * @param ppe
	 */
	public ComunicacaoProcessualDTO(ProcessoParteExpediente ppe) {
		this(ppe, Authenticator.getUsuarioLogado());
	}

	/**
	 * Construtor.
	 * 
	 * @param ppe
	 * @param usuario
	 */
	public ComunicacaoProcessualDTO(ProcessoParteExpediente ppe, Usuario usuario) {
		if (ppe != null) {
			ProcessoTrf processoTrf = ppe.getProcessoJudicial();
			ProcessoExpediente processoExpediente = ppe.getProcessoExpediente();
			TipoProcessoDocumento tipoProcessoDocumento = processoExpediente.getTipoProcessoDocumento();
			
			setNumeroComunicacao(String.valueOf(ppe.getIdProcessoParteExpediente()));
			setNumeroProcesso(processoTrf.getNumeroProcesso());

			setNomeDestinatario(ppe.getPessoaParte().getNome());
			setDocumentoDestinatario(ppe.getPessoaParte().getDocumentoCpfCnpj());
			setTribunalOrigem(ComponentUtil.getParametroUtil().getDomicilioEletronicoTribunalOrigem());
			setDataAjuizamento(DateUtil.dateToStringUSA(processoTrf.getDataAutuacao()));
			
			setSegredoJustica(processoTrf.getSegredoJustica());
			
			setDataFinalCiencia(DateUtil.dateToStringUSA(ppe.getDtPrazoLegal()));
			setPrazo(ppe.getPrazoLegal());
			setTipoPrazo(TipoPrazoEnum.get(ppe.getTipoPrazo().toString()));
			setSistemaProcesso("PJe");

			try {
				configuraDataHoraAudiencia(ppe);
				setUrgente(SimNaoEnum.getEnum(ppe.getProcessoExpediente().getUrgencia()));
			} catch (LazyInitializationException e) {
				processoTrf = ComponentUtil.getComponent(ProcessoTrfManager.class)
						.getProcessoTrfByIdProcessoTrf(ppe.getProcessoJudicial().getIdProcessoTrf());

				ppe.setProcessoJudicial(processoTrf);

				if (!ProjetoUtil.isNaoVazio(getDataAudiencia(), getHoraAudiencia())) {
					configuraDataHoraAudiencia(ppe);
				}

				setUrgente(SimNaoEnum.getEnum(ppe.getProcessoExpediente().getUrgencia()));
			}

			setFormatoAudiencia(BooleanUtils.isTrue(processoTrf.getPautaVirtual()) ? FormatoAudienciaEnum.V : FormatoAudienciaEnum.P );
			
			setNomeServidor(usuario.getNome());
			setCpfServidor(usuario.getLogin());
			
			setMeioEnvio(recuperaMeioEnvio(ppe.getProcessoExpediente().getMeioExpedicaoExpediente())); 
			setJuizoDigital(SimNaoEnum.S);

			//Verificar os campos abaixo
			
			setTipoAcao(TipoAcaoEnum.I); //INDIVIDUAL - I, COLETIVA - C
			
			setTipoComunicacao(recuperaTipoComunicacao(ppe));

			setCienciaAutomatica(getTipoComunicacao().isCitacao() ? SimNaoEnum.N : SimNaoEnum.S);
			
			TipoProcessoDocumentoParaTipoIntimacaoConverter converter = new TipoProcessoDocumentoParaTipoIntimacaoConverter();
			TipoIntimacaoEnum tipoIntimacaoEnum = converter.converter(tipoProcessoDocumento);
			setTipoIntimacao((tipoIntimacaoEnum != null ? tipoIntimacaoEnum.getCodigo() : null));
			setLinksDocumentos(ValidacaoAssinaturaProcessoDocumento.instance().geraUrlValidacaoExpediente(ppe)); //? DOCUMENTO DA INTIMAÇAO?
			setLeitoresAutorizados(ppe.getPessoaParte().getNome());
			setProcesso(new FaseAtualDTO(ppe.getProcessoJudicial()));
			setLinksDocumentosAdicionais(carregarLinksDocumentosAdicionais(ppe));
			setComunicacaoPessoal(true);
		}
	}

	private TipoComunicacaoEnum recuperaTipoComunicacao(ProcessoParteExpediente ppe) {
		if (ppe.getProcessoExpediente().getTipoProcessoDocumento() == null) {
			return TipoComunicacaoEnum.N;
		}
		Integer id = ppe.getProcessoExpediente().getTipoProcessoDocumento().getIdTipoProcessoDocumento();
		if(ParametroUtil.instance().getListaIdTipoProcessoDocumentoCitacao().contains(id)){
			return TipoComunicacaoEnum.C;
		}
		else if(ParametroUtil.instance().getListaIdTipoProcessoDocumentoIntimacao().contains(id)) {
			return TipoComunicacaoEnum.I;
		}
		else {
			return TipoComunicacaoEnum.N;
		}
	}
	
	private Integer recuperaMeioEnvio(ExpedicaoExpedienteEnum meio) {
		//DOMICÍLIO - 1, DJEN - 2, OFICIAL_JUSTICA - 3, CORREIOS - 4
		switch(meio) {
			case P: return 2;
			case C: return 4;
			case G: return 4;
			case M: return 3;
			default: return 1;
		}
	}

	/**
	 * @return the numeroComunicacao
	 */
	public String getNumeroComunicacao() {
		return numeroComunicacao;
	}
	
	/**
	 * @param numeroComunicacao the numeroComunicacao to set
	 */
	public void setNumeroComunicacao(String numeroComunicacao) {
		this.numeroComunicacao = numeroComunicacao;
	}
	
	/**
	 * @return the numeroProcesso
	 */
	public String getNumeroProcesso() {
		return numeroProcesso;
	}
	
	/**
	 * @param numeroProcesso the numeroProcesso to set
	 */
	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}
	
	/**
	 * @return the nomeDestinatario
	 */
	public String getNomeDestinatario() {
		return nomeDestinatario;
	}
	
	/**
	 * @param nomeDestinatario the nomeDestinatario to set
	 */
	public void setNomeDestinatario(String nomeDestinatario) {
		this.nomeDestinatario = nomeDestinatario;
	}
	
	/**
	 * @return the documentoDestinatario
	 */
	public String getDocumentoDestinatario() {
		return documentoDestinatario;
	}
	
	/**
	 * @param documentoDestinatario the documentoDestinatario to set
	 */
	public void setDocumentoDestinatario(String documentoDestinatario) {
		this.documentoDestinatario = documentoDestinatario;
	}
	
	/**
	 * @return the tribunalOrigem
	 */
	public String getTribunalOrigem() {
		return tribunalOrigem;
	}
	
	/**
	 * @param tribunalOrigem the tribunalOrigem to set
	 */
	public void setTribunalOrigem(String tribunalOrigem) {
		this.tribunalOrigem = tribunalOrigem;
	}
	
	/**
	 * @return the dataAjuizamento
	 */
	public String getDataAjuizamento() {
		return dataAjuizamento;
	}
	
	/**
	 * @param dataAjuizamento the dataAjuizamento to set
	 */
	public void setDataAjuizamento(String dataAjuizamento) {
		this.dataAjuizamento = dataAjuizamento;
	}
	
	/**
	 * @return the tipoComunicacao
	 */
	public TipoComunicacaoEnum getTipoComunicacao() {
		return tipoComunicacao;
	}
	
	/**
	 * @param tipoComunicacao the tipoComunicacao to set
	 */
	public void setTipoComunicacao(TipoComunicacaoEnum tipoComunicacao) {
		this.tipoComunicacao = tipoComunicacao;
	}
	
	/**
	 * @return the segredoJustica
	 */
	public Boolean getSegredoJustica() {
		return segredoJustica;
	}
	
	/**
	 * @param segredoJustica the segredoJustica to set
	 */
	public void setSegredoJustica(Boolean segredoJustica) {
		this.segredoJustica = segredoJustica;
	}
	
	/**
	 * @return the cienciaAutomatica
	 */
	public SimNaoEnum getCienciaAutomatica() {
		return cienciaAutomatica;
	}
	
	/**
	 * @param cienciaAutomatica the cienciaAutomatica to set
	 */
	public void setCienciaAutomatica(SimNaoEnum cienciaAutomatica) {
		this.cienciaAutomatica = cienciaAutomatica;
	}
	
	/**
	 * @return the dataFinalCiencia
	 */
	public String getDataFinalCiencia() {
		return dataFinalCiencia;
	}
	
	/**
	 * @param dataFinalCiencia the dataFinalCiencia to set
	 */
	public void setDataFinalCiencia(String dataFinalCiencia) {
		this.dataFinalCiencia = dataFinalCiencia;
	}
	
	/**
	 * @return the prazo
	 */
	public Integer getPrazo() {
		return prazo;
	}
	
	/**
	 * @param prazo the prazo to set
	 */
	public void setPrazo(Integer prazo) {
		this.prazo = prazo;
	}
	
	/**
	 * @return the tipoPrazo
	 */
	public TipoPrazoEnum getTipoPrazo() {
		return tipoPrazo;
	}
	
	/**
	 * @param tipoPrazo the tipoPrazo to set
	 */
	public void setTipoPrazo(TipoPrazoEnum tipoPrazo) {
		this.tipoPrazo = tipoPrazo;
	}
	
	/**
	 * @return the nomeServidor
	 */
	public String getNomeServidor() {
		return nomeServidor;
	}
	
	/**
	 * @param nomeServidor the nomeServidor to set
	 */
	public void setNomeServidor(String nomeServidor) {
		this.nomeServidor = nomeServidor;
	}
	
	/**
	 * @return the cpfServidor
	 */
	public String getCpfServidor() {
		return cpfServidor;
	}
	
	/**
	 * @param cpfServidor the cpfServidor to set
	 */
	public void setCpfServidor(String cpfServidor) {
		this.cpfServidor = cpfServidor;
	}
	
	/**
	 * @return the sistemaProcesso
	 */
	public String getSistemaProcesso() {
		return sistemaProcesso;
	}
	
	/**
	 * @param sistemaProcesso the sistemaProcesso to set
	 */
	public void setSistemaProcesso(String sistemaProcesso) {
		this.sistemaProcesso = sistemaProcesso;
	}
	
	/**
	 * @return the meioEnvio
	 */
	public Integer getMeioEnvio() {
		return meioEnvio;
	}
	
	/**
	 * @param meioEnvio the meioEnvio to set
	 */
	public void setMeioEnvio(Integer meioEnvio) {
		this.meioEnvio = meioEnvio;
	}
	
	/**
	 * @return the leitoresAutorizados
	 */
	public String getLeitoresAutorizados() {
		return leitoresAutorizados;
	}
	
	/**
	 * @param leitoresAutorizados the leitoresAutorizados to set
	 */
	public void setLeitoresAutorizados(String leitoresAutorizados) {
		this.leitoresAutorizados = leitoresAutorizados;
	}
	
	/**
	 * @return the linksDocumentos
	 */
	public String getLinksDocumentos() {
		return linksDocumentos;
	}
	
	/**
	 * @param linksDocumentos the linksDocumentos to set
	 */
	public void setLinksDocumentos(String linksDocumentos) {
		this.linksDocumentos = linksDocumentos;
	}
	
	/**
	 * @return the tipoAcao
	 */
	public TipoAcaoEnum getTipoAcao() {
		return tipoAcao;
	}
	
	/**
	 * @param tipoAcao the tipoAcao to set
	 */
	public void setTipoAcao(TipoAcaoEnum tipoAcao) {
		this.tipoAcao = tipoAcao;
	}
	
	/**
	 * @return the dataAudiencia
	 */
	public String getDataAudiencia() {
		return dataAudiencia;
	}
	
	/**
	 * @param dataAudiencia the dataAudiencia to set
	 */
	public void setDataAudiencia(String dataAudiencia) {
		this.dataAudiencia = dataAudiencia;
	}
	
	/**
	 * @return the horaAudiencia
	 */
	public String getHoraAudiencia() {
		return horaAudiencia;
	}
	
	/**
	 * @param horaAudiencia the horaAudiencia to set
	 */
	public void setHoraAudiencia(String horaAudiencia) {
		this.horaAudiencia = horaAudiencia;
	}
	
	/**
	 * @return the tipoAudiencia
	 */
	public Integer getTipoAudiencia() {
		return tipoAudiencia;
	}
	
	/**
	 * @param tipoAudiencia the tipoAudiencia to set
	 */
	public void setTipoAudiencia(Integer tipoAudiencia) {
		this.tipoAudiencia = tipoAudiencia;
	}
	
	/**
	 * @return the formatoAudiencia
	 */
	public FormatoAudienciaEnum getFormatoAudiencia() {
		return formatoAudiencia;
	}
	
	/**
	 * @param formatoAudiencia the formatoAudiencia to set
	 */
	public void setFormatoAudiencia(FormatoAudienciaEnum formatoAudiencia) {
		this.formatoAudiencia = formatoAudiencia;
	}
	
	/**
	 * @return the tipoIntimacao
	 */
	public Integer getTipoIntimacao() {
		return tipoIntimacao;
	}
	
	/**
	 * @param tipoIntimacao the tipoIntimacao to set
	 */
	public void setTipoIntimacao(Integer tipoIntimacao) {
		this.tipoIntimacao = tipoIntimacao;
	}
	
	/**
	 * @return the urgente
	 */
	public SimNaoEnum getUrgente() {
		return urgente;
	}
	
	/**
	 * @param urgente the urgente to set
	 */
	public void setUrgente(SimNaoEnum urgente) {
		this.urgente = urgente;
	}
	
	/**
	 * @return the juizoDigital
	 */
	public SimNaoEnum getJuizoDigital() {
		return juizoDigital;
	}
	
	/**
	 * @param juizoDigital the juizoDigital to set
	 */
	public void setJuizoDigital(SimNaoEnum juizoDigital) {
		this.juizoDigital = juizoDigital;
	}


	/**
	 * @return the processo
	 */
	public FaseAtualDTO getProcesso() {
		return processo;
	}

	/**
	 * @param processo the processo to set
	 */
	public void setProcesso(FaseAtualDTO processo) {
		this.processo = processo;
	}

	/**
	 * @return linksDocumentosAdicionais
	 */
	public List<String> getLinksDocumentosAdicionais() {
		if (linksDocumentosAdicionais == null) {
			linksDocumentosAdicionais = new ArrayList<>();
		}
		return linksDocumentosAdicionais;
		
	}

	/**
	 * @param linksDocumentosAdicionais
	 */
	public void setLinksDocumentosAdicionais(List<String> linksDocumentosAdicionais) {
		this.linksDocumentosAdicionais = linksDocumentosAdicionais;
		
	}

	/**
	 * @param ppe
	 * @return Lista de links dos anexos do expediente.
	 */
	private List<String> carregarLinksDocumentosAdicionais(ProcessoParteExpediente ppe) {
		List<String> resultado = new ArrayList<>();
		if (ppe != null && ppe.getProcessoExpediente() != null && ProjetoUtil.isNotVazio(ppe.getProcessoExpediente().getProcessoDocumentoExpedienteList())) {
			ProcessoDocumento documentoExpediente = ppe.getProcessoDocumento();
			
			List<ProcessoDocumentoExpediente> anexos = ppe.getProcessoExpediente().getProcessoDocumentoExpedienteList();
			for (ProcessoDocumentoExpediente anexo : anexos) {
				if (documentoExpediente.getIdProcessoDocumento() != anexo.getProcessoDocumento().getIdProcessoDocumento()) {
					resultado.add(ValidacaoAssinaturaProcessoDocumento.instance().geraUrlValidacaoExpediente(ppe, anexo));
				}
			}
		}
		return resultado;
	}

	public Boolean getComunicacaoPessoal() {
		return comunicacaoPessoal;
	}

	public void setComunicacaoPessoal(Boolean comunicacaoPessoal) {
		this.comunicacaoPessoal = comunicacaoPessoal;
	}

	/**
	 * Atribui Data e Hora da audiência.
	 * 
	 * @param ppe ProcessoParteExpediente
	 */
	protected void configuraDataHoraAudiencia(ProcessoParteExpediente ppe) {
		
		if (ppe.getProcessoExpediente().getSessao() != null) {
			setDataAudiencia(DateUtil.dateToStringUSA(ppe.getProcessoExpediente().getSessao().getDataSessao()));
			setHoraAudiencia(DateUtil.dateToStringUSA(ppe.getProcessoExpediente().getSessao().getHorarioInicio()));
		}else {
			ProcessoTrf processoTrf = ppe.getProcessoJudicial();
			if (ProjetoUtil.isNotVazio(processoTrf.getProcessoAudienciaList())) {
				for (ProcessoAudiencia processoAudiencia : processoTrf.getProcessoAudienciaList())
				{
					if(BooleanUtils.isTrue(processoAudiencia.getAtivo())) {
						setDataAudiencia(DateUtil.dateToStringUSA(processoAudiencia.getDtInicio()));
						setHoraAudiencia(DateUtil.dateToHourISO8601(processoAudiencia.getDtInicio()));
						break;
					}
				}
			}
		}
	}
}
