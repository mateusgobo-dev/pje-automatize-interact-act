package br.jus.cnj.pje.entidades.vo;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.StringUtils;

import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.je.pje.entity.vo.CaixaAdvogadoProcuradorVO;
import br.jus.je.pje.entity.vo.JurisdicaoVO;
import br.jus.pje.nucleo.entidades.Assunto;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.PrioridadeProcesso;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.TipoSituacaoExpedienteEnum;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.nucleo.util.StringUtil;

public class PesquisaExpedientesVO {
	
	private Integer idProcessoParteExpediente;

	private Integer idProcessoTrf;
	private String numeroProcesso;
	private ProcessoTrf processoTrfObj;

	private String numeroSequenciaProcessoPattern;
	private List<IntervaloNumeroSequencialProcessoVO> intervalosNumerosSequenciais = new ArrayList<IntervaloNumeroSequencialProcessoVO>(0);

	private Integer numeroSequencia;
	private Integer digitoVerificador;
	private Integer numeroAno;
	private Integer ramoJustica;
	private Integer numeroOrgaoJustica;
	private Integer numeroOrigem;

	private String assuntoJudicial;
	private Assunto assuntoJudicialObj;
	private List<AssuntoTrf> assuntoTrfList = new ArrayList<AssuntoTrf>(0);
	
	private Boolean apenasPrioridade = false;
	private PrioridadeProcesso prioridadeObj;
	
	private OrgaoJulgador orgaoJulgadorObj;
	private OrgaoJulgadorColegiado orgaoJulgadorColegiadoObj;
	
	private String nomeParte;
	private String nomeDestinatario;
	private Date dataNascimentoInicial;
	private Date dataNascimentoFinal;

	private String classeJudicial;
	private ClasseJudicial classeJudicialObj;
	private List<ClasseJudicial> classeJudicialList = new ArrayList<ClasseJudicial>(0);

	private Date dataAutuacaoInicial;
	private Date dataAutuacaoFinal;
	private Date dataCriacaoExpedienteInicial;
	private Date dataCriacaoExpedienteFinal;
	
	private Integer idTipoProcessoDocumento;
	private String inMeioComunicacao;
	private String oabRepresentanteDestinatario;
	private String cpfDestinatario;
	private String cnpjDestinatario;
	private String outroDocumentoDestinatario;
	private Integer idCaixaAdvProc;
	private Integer idJurisdicao;

	private Boolean apenasSemCaixa = false;
	private Boolean apenasCaixasAtivas = false;
	private Boolean apenasCaixasComResultados = false;
	
	private TipoSituacaoExpedienteEnum tipoSituacaoExpediente;
	
	private Map<Field, Object> mapaAtributos;
	
	private Integer pessoaParteRepresentado;

	public PesquisaExpedientesVO() {
		salvarPesquisaInicial();
	}

	public PesquisaExpedientesVO(TipoSituacaoExpedienteEnum tipoSituacaoExpediente) {
		this.tipoSituacaoExpediente = tipoSituacaoExpediente;
		
		salvarPesquisaInicial();
	}

	public PesquisaExpedientesVO(TipoSituacaoExpedienteEnum tipoSituacaoExpediente, Integer idJurisdicao) {
		this.tipoSituacaoExpediente = tipoSituacaoExpediente;
		this.idJurisdicao = idJurisdicao;
		
		salvarPesquisaInicial();
	}

	public PesquisaExpedientesVO(TipoSituacaoExpedienteEnum tipoSituacaoExpediente, JurisdicaoVO jurisdicao) {
		this(tipoSituacaoExpediente, jurisdicao, null);
	}

	public PesquisaExpedientesVO(TipoSituacaoExpedienteEnum tipoSituacaoExpediente, JurisdicaoVO jurisdicaoVO, CaixaAdvogadoProcuradorVO caixaVO) {
		this.tipoSituacaoExpediente = tipoSituacaoExpediente;
		if(jurisdicaoVO != null) {
			this.idJurisdicao = jurisdicaoVO.getId();
		}
		
		if(caixaVO != null) {
			if(caixaVO.getPadrao()) {
				this.setApenasSemCaixa(true);
			}else {
				this.setIdCaixaAdvProc(caixaVO.getId());
			}
		}
		
		salvarPesquisaInicial();
	}

	public Integer getIdProcessoTrf() {
		return idProcessoTrf;
	}

	public void setIdProcessoTrf(Integer idProcessoTrf) {
		this.idProcessoTrf = idProcessoTrf;
	}

	public Boolean getApenasCaixasComResultados() {
		return apenasCaixasComResultados;
	}

	public void setApenasCaixasComResultados(Boolean apenasCaixasComResultados) {
		this.apenasCaixasComResultados = apenasCaixasComResultados;
	}

	public Integer getIdProcessoParteExpediente() {
		return idProcessoParteExpediente;
	}

	public void setIdProcessoParteExpediente(Integer idProcessoParteExpediente) {
		this.idProcessoParteExpediente = idProcessoParteExpediente;
	}

	public ProcessoTrf getProcessoTrfObj() {
		return processoTrfObj;
	}
	public void setProcessoTrfObj(ProcessoTrf processoTrfObj) {
		this.processoTrfObj = processoTrfObj;
	}

	public String getNumeroProcesso() {
		return numeroProcesso;
	}
	public void setNumeroProcesso(String numeroProcesso) {
		if(numeroProcesso != null) {
			String numeroProcessoLimpo = StringUtil.limparCharsNaoNumericos(numeroProcesso);
			
			if(StringUtils.isNotBlank(numeroProcessoLimpo)) {
				this.numeroProcesso = numeroProcesso;
			}
		}
	}
	
	public Integer getNumeroSequencia() {
		return numeroSequencia;
	}
	public void setNumeroSequencia(Integer numeroSequencia) {
		this.numeroSequencia = numeroSequencia;
	}
	
	public String getNumeroSequenciaProcessoPattern() {
		return numeroSequenciaProcessoPattern;
	}

	public void setNumeroSequenciaProcessoPattern(String numeroSequenciaProcessoPattern) {
		this.numeroSequenciaProcessoPattern = numeroSequenciaProcessoPattern;
		this.geraNumeroSequenciaPatterList(numeroSequenciaProcessoPattern);
	}
	
	public List<IntervaloNumeroSequencialProcessoVO> getIntervalosNumerosSequenciais() {
		return intervalosNumerosSequenciais;
	}

	public void setIntervalosNumerosSequenciais(List<IntervaloNumeroSequencialProcessoVO> intervalosNumerosSequenciais) {
		this.intervalosNumerosSequenciais = intervalosNumerosSequenciais;
	}

	/**
	 * Dada uma string com uma expressão regular, gera uma lista VOs de intervalo de número sequenciais
	 * 
	 * @return
	 */
	private void geraNumeroSequenciaPatterList(String pattern) {
		this.intervalosNumerosSequenciais = new ArrayList<IntervaloNumeroSequencialProcessoVO>(0);
		if(StringUtil.isNotEmpty(pattern)){
			List<String> intervalos = new CopyOnWriteArrayList<String>(Arrays.asList(pattern.split(";")));
			for (String intervalo : intervalos) {
				if (intervalo.length()>1){
					String[] strRange = intervalo.split("-");
					if(strRange.length==1) {
						// quando o intervalo informado só tem um número, o valor inicial e o final serão o mesmo valor
						this.intervalosNumerosSequenciais.add(new IntervaloNumeroSequencialProcessoVO(strRange[0].length(), Integer.parseInt(strRange[0]), Integer.parseInt(strRange[0])));
					}
					if (strRange.length==2){
						// não é possível indicar intervalos quando o número inicial tem quantidade de caractéres diferente do número final
						if(strRange[0].length() == strRange[1].length()) {
							this.intervalosNumerosSequenciais.add(new IntervaloNumeroSequencialProcessoVO(strRange[0].length(), Integer.parseInt(strRange[0]), Integer.parseInt(strRange[1])));
						}
					}
				}
			}
		}
	}

	public Integer getDigitoVerificador() {
		return digitoVerificador;
	}
	public void setDigitoVerificador(Integer digitoVerificador) {
		this.digitoVerificador = digitoVerificador;
	}
	public Integer getNumeroAno() {
		return numeroAno;
	}
	public void setNumeroAno(Integer numeroAno) {
		this.numeroAno = numeroAno;
	}
	public Integer getRamoJustica() {
		return ramoJustica;
	}
	public void setRamoJustica(Integer ramoJustica) {
		this.ramoJustica = ramoJustica;
	}
	public Integer getNumeroOrgaoJustica() {
		return numeroOrgaoJustica;
	}
	public void setNumeroOrgaoJustica(Integer numeroOrgaoJustica) {
		this.numeroOrgaoJustica = numeroOrgaoJustica;
	}
	public Integer getNumeroOrigem() {
		return numeroOrigem;
	}
	public void setNumeroOrigem(Integer numeroOrigem) {
		this.numeroOrigem = numeroOrigem;
	}
	public String getAssuntoJudicial() {
		return assuntoJudicial;
	}
	public void setAssuntoJudicial(String assuntoJudicial) {
		if(assuntoJudicial != null && !StringUtil.fullTrim(assuntoJudicial).isEmpty()){
			this.assuntoJudicial = StringUtil.fullTrim(assuntoJudicial);
		}
	}
	public Assunto getAssuntoJudicialObj() {
		return assuntoJudicialObj;
	}
	public void setAssuntoJudicialObj(Assunto assuntoJudicialObj) {
		this.assuntoJudicialObj = assuntoJudicialObj;
	}
	
	public List<AssuntoTrf> getAssuntoTrfList() {
		return assuntoTrfList;
	}

	public void setAssuntoTrfList(List<AssuntoTrf> assuntoTrfList) {
		this.assuntoTrfList = assuntoTrfList;
	}

	public Boolean getApenasPrioridade() {
		return apenasPrioridade;
	}
	public void setApenasPrioridade(Boolean apenasPrioridade) {
		this.apenasPrioridade = apenasPrioridade;
	}
	public PrioridadeProcesso getPrioridadeObj() {
		return prioridadeObj;
	}
	public void setPrioridadeObj(PrioridadeProcesso prioridade) {
		if(prioridade != null && prioridade.getIdPrioridadeProcesso() > 0) {
			this.prioridadeObj = prioridade;
			this.setApenasPrioridade(true);
		}
	}
	public String getNomeParte() {
		return nomeParte;
	}
	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}
	public String getNomeDestinatario() {
		return nomeDestinatario;
	}
	public void setNomeDestinatario(String nomeDestinatario) {
		if(nomeDestinatario != null && !StringUtil.fullTrim(nomeDestinatario).isEmpty()){
			this.nomeDestinatario = StringUtil.fullTrim(nomeDestinatario);
		}
	}
	public Date getDataNascimentoInicial() {
		return dataNascimentoInicial;
	}

	public void setDataNascimentoInicial(Date dataNascimentoInicial) {
		this.dataNascimentoInicial = dataNascimentoInicial;
	}
	
	public Date getDataNascimentoFinal() {
		return dataNascimentoFinal;
	}

	public void setDataNascimentoFinal(Date dataNascimentoFinal) {
		this.dataNascimentoFinal = DateUtil.getEndOfDay(dataNascimentoFinal);
	}

	public String getClasseJudicial() {
		return classeJudicial;
	}
	public void setClasseJudicial(String classeJudicial) {
		if(classeJudicial != null && !StringUtil.fullTrim(classeJudicial).isEmpty()){
			this.classeJudicial = StringUtil.fullTrim(classeJudicial);
		}
	}
	public ClasseJudicial getClasseJudicialObj() {
		return classeJudicialObj;
	}
	public void setClasseJudicialObj(ClasseJudicial classeJudicialObj) {
		this.classeJudicialObj = classeJudicialObj;
	}
	public List<ClasseJudicial> getClasseJudicialList() {
		return classeJudicialList;
	}

	public void setClasseJudicialList(List<ClasseJudicial> classeJudicialList) {
		this.classeJudicialList = classeJudicialList;
	}

	public OrgaoJulgador getOrgaoJulgadorObj() {
		return orgaoJulgadorObj;
	}

	public void setOrgaoJulgadorObj(OrgaoJulgador orgaoJulgadorObj) {
		this.orgaoJulgadorObj = orgaoJulgadorObj;
	}

	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiadoObj() {
		return orgaoJulgadorColegiadoObj;
	}

	public void setOrgaoJulgadorColegiadoObj(OrgaoJulgadorColegiado orgaoJulgadorColegiadoObj) {
		this.orgaoJulgadorColegiadoObj = orgaoJulgadorColegiadoObj;
	}
	
	public Date getDataCriacaoExpedienteInicial() {
		return dataCriacaoExpedienteInicial;
	}
	
	public void setIntervaloDataCriacaoExpediente(Date dataCriacaoInicial, Date dataCriacaoFinal) {
		this.setDataCriacaoExpedienteInicial(dataCriacaoInicial);
		this.setDataCriacaoExpedienteFinal(dataCriacaoFinal);
	}

	public void setDataCriacaoExpedienteInicial(Date dataCriacaoExpedienteInicial) {
		this.dataCriacaoExpedienteInicial = dataCriacaoExpedienteInicial;
	}

	public Date getDataCriacaoExpedienteFinal() {
		return dataCriacaoExpedienteFinal;
	}

	public void setDataCriacaoExpedienteFinal(Date dataCriacaoExpedienteFinal) {
		this.dataCriacaoExpedienteFinal = DateUtil.getEndOfDay(dataCriacaoExpedienteFinal);
	}

	public Date getDataAutuacaoInicial() {
		return dataAutuacaoInicial;
	}
	public void setDataAutuacaoInicial(Date dataAutuacaoInicial) {
		this.dataAutuacaoInicial = dataAutuacaoInicial;
	}
	public Date getDataAutuacaoFinal() {
		return dataAutuacaoFinal;
	}
	public void setDataAutuacaoFinal(Date dataAutuacaoFinal) {
		if(dataAutuacaoFinal != null){
			this.dataAutuacaoFinal = DateUtil.getEndOfDay(dataAutuacaoFinal);
		}
	}

	public void setIntervaloDataAutuacao(Date dataAutuacaoInicial, Date dataAutuacaoFinal) {
		this.setDataAutuacaoInicial(dataAutuacaoInicial);
		this.setDataAutuacaoFinal(dataAutuacaoFinal);
	}
	
	public Integer getIdTipoProcessoDocumento() {
		return idTipoProcessoDocumento;
	}
	public void setIdTipoProcessoDocumento(Integer idTipoProcessoDocumento) {
		this.idTipoProcessoDocumento = idTipoProcessoDocumento;
	}
	public String getInMeioComunicacao() {
		return inMeioComunicacao;
	}
	public void setInMeioComunicacao(String inMeioComunicacao) {
		this.inMeioComunicacao = inMeioComunicacao;
	}
	public String getOabRepresentanteDestinatario() {
		return oabRepresentanteDestinatario;
	}
	public void setOabRepresentanteDestinatario(String oabRepresentanteDestinatario) {
		if(oabRepresentanteDestinatario != null && !StringUtil.fullTrim(oabRepresentanteDestinatario).isEmpty()){
			this.oabRepresentanteDestinatario = StringUtil.fullTrim(oabRepresentanteDestinatario);
		}
	}
	
	public void setDocumentoIdentificacaoDestinatario(String documentoIdentificacao) {
		if(documentoIdentificacao == null || StringUtil.fullTrim(documentoIdentificacao).isEmpty()){
			return;
		}
		String insc = documentoIdentificacao.replaceAll("\\D", "");
		if(insc != null && !insc.isEmpty()){
			if(insc.length() == 11){
				this.setCpfDestinatario(insc);
			}else if(insc.length() == 14){
				this.setCnpjDestinatario(insc);
			}else {
				this.setOutroDocumentoDestinatario(StringUtil.fullTrim(documentoIdentificacao));
			}
		}
	}

	public String getCpfDestinatario() {
		return cpfDestinatario;
	}
	public void setCpfDestinatario(String cpfDestinatario) {
		if(cpfDestinatario != null && !StringUtil.fullTrim(cpfDestinatario).isEmpty()){
			String cpf = StringUtil.fullTrim(cpfDestinatario).replaceAll("\\D", "");
		
			if(cpf.length() == 11 && InscricaoMFUtil.verificaCPF(cpf)){
				this.cpfDestinatario = InscricaoMFUtil.acrescentaMascaraCPF(cpf);
			}
		}
	}
	public String getCnpjDestinatario() {
		return cnpjDestinatario;
	}
	public void setCnpjDestinatario(String cnpjDestinatario) {
		if(cnpjDestinatario != null && !StringUtil.fullTrim(cnpjDestinatario).isEmpty()){
			String cnpj = StringUtil.fullTrim(cnpjDestinatario).replaceAll("\\D", "");
		
			if(cnpj.length() == 14 && InscricaoMFUtil.verificaCNPJ(cnpj)){
				this.cnpjDestinatario = InscricaoMFUtil.mascaraCnpj(cnpj);
			}
		}
	}
	public String getOutroDocumentoDestinatario() {
		return outroDocumentoDestinatario;
	}
	public void setOutroDocumentoDestinatario(String outroDocumentoDestinatario) {
		if(outroDocumentoDestinatario != null && !StringUtil.fullTrim(outroDocumentoDestinatario).isEmpty()){
			this.outroDocumentoDestinatario = StringUtil.fullTrim(outroDocumentoDestinatario);
		}
	}
	public void setDocumentoIdentificacao(String documentoIdentificacao) {
		if(documentoIdentificacao == null || StringUtil.fullTrim(documentoIdentificacao).isEmpty()){
			return;
		}
		String insc = documentoIdentificacao.replaceAll("\\D", "");
		if(insc != null && !insc.isEmpty()){
			if(insc.length() == 11 && InscricaoMFUtil.verificaCPF(insc)){
				this.setCpfDestinatario(insc);
			}else if(insc.length() == 14 && InscricaoMFUtil.verificaCNPJ(insc)){
				this.setCnpjDestinatario(insc);
			}else {
				this.setOutroDocumentoDestinatario(insc);
			}
		}
	}
	public Integer getIdCaixaAdvProc() {
		return idCaixaAdvProc;
	}
	public void setIdCaixaAdvProc(Integer idCaixaAdvProc) {
		if(idCaixaAdvProc != null) {
			apenasSemCaixa = false;
		}
		this.idCaixaAdvProc = idCaixaAdvProc;
	}

	public Boolean getApenasCaixasAtivas() {
		return apenasCaixasAtivas;
	}

	public void setApenasCaixasAtivas(Boolean apenasCaixasAtivas) {
		this.apenasCaixasAtivas = apenasCaixasAtivas;
	}

	public Boolean getApenasSemCaixa() {
		if(apenasSemCaixa == null) {
			apenasSemCaixa = false;
		}
		return apenasSemCaixa;
	}
	public void setApenasSemCaixa(Boolean apenasSemCaixa) {
		if(apenasSemCaixa) {
			idCaixaAdvProc = null;
		}
		this.apenasSemCaixa = apenasSemCaixa;
	}

	public String getIdTipoSituacaoExpediente() {
		return this.getTipoSituacaoExpediente().name();
	}

	
	public TipoSituacaoExpedienteEnum getTipoSituacaoExpediente() {
		return tipoSituacaoExpediente;
	}
	public void setTipoSituacaoExpediente(TipoSituacaoExpedienteEnum tipoSituacaoExpediente) {
		this.tipoSituacaoExpediente = tipoSituacaoExpediente;
	}
	/**
	 * @return idJurisdicao.
	 */
	public Integer getIdJurisdicao() {
		return idJurisdicao;
	}
	/**
	 * @param idJurisdicao.
	 */
	public void setIdJurisdicao(Integer idJurisdicao) {
		this.idJurisdicao = idJurisdicao;
	}

	private void salvarPesquisaInicial() {
		mapaAtributos = new HashMap<Field, Object>(0);
		for(Field f: PesquisaExpedientesVO.class.getDeclaredFields()){
			try {
				mapaAtributos.put(f, f.get(this));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean isPesquisaAlterada() {
		for (Field field : mapaAtributos.keySet()) {
			try {
				Object value = field.get(this);
				if(value != null) {
					if(!value.equals(mapaAtributos.get(field))) {
						return true;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		
		result = prime * result + ((apenasCaixasAtivas == null) ? 0 : apenasCaixasAtivas.hashCode());
		result = prime * result + ((idProcessoParteExpediente == null) ? 0 : idProcessoParteExpediente.hashCode());
		result = prime * result + ((apenasCaixasComResultados == null) ? 0 : apenasCaixasComResultados.hashCode());
		result = prime * result + ((apenasPrioridade == null) ? 0 : apenasPrioridade.hashCode());
		result = prime * result + ((apenasSemCaixa == null) ? 0 : apenasSemCaixa.hashCode());
		result = prime * result + ((assuntoJudicial == null) ? 0 : assuntoJudicial.hashCode());
		result = prime * result + ((assuntoJudicialObj == null) ? 0 : assuntoJudicialObj.hashCode());
		result = prime * result + ((idCaixaAdvProc == null) ? 0 : idCaixaAdvProc.hashCode());
		result = prime * result + ((classeJudicial == null) ? 0 : classeJudicial.hashCode());
		result = prime * result + ((classeJudicialObj == null) ? 0 : classeJudicialObj.hashCode());
		result = prime * result + ((cnpjDestinatario == null) ? 0 : cnpjDestinatario.hashCode());
		result = prime * result + ((cpfDestinatario == null) ? 0 : cpfDestinatario.hashCode());
		result = prime * result + ((dataAutuacaoFinal == null) ? 0 : dataAutuacaoFinal.hashCode());
		result = prime * result + ((dataAutuacaoInicial == null) ? 0 : dataAutuacaoInicial.hashCode());
		result = prime * result + ((dataCriacaoExpedienteInicial == null) ? 0 : dataCriacaoExpedienteInicial.hashCode());
		result = prime * result + ((dataCriacaoExpedienteFinal == null) ? 0 : dataCriacaoExpedienteFinal.hashCode());
		result = prime * result + ((digitoVerificador == null) ? 0 : digitoVerificador.hashCode());
		result = prime * result + ((idJurisdicao == null) ? 0 : idJurisdicao.hashCode());
		result = prime * result + ((idTipoProcessoDocumento == null) ? 0 : idTipoProcessoDocumento.hashCode());
		result = prime * result + ((inMeioComunicacao == null) ? 0 : inMeioComunicacao.hashCode());
		result = prime * result + ((nomeDestinatario == null) ? 0 : nomeDestinatario.hashCode());
		result = prime * result + ((nomeParte == null) ? 0 : nomeParte.hashCode());
		result = prime * result + ((numeroAno == null) ? 0 : numeroAno.hashCode());
		result = prime * result + ((numeroOrgaoJustica == null) ? 0 : numeroOrgaoJustica.hashCode());
		result = prime * result + ((numeroOrigem == null) ? 0 : numeroOrigem.hashCode());
		result = prime * result + ((numeroProcesso == null) ? 0 : numeroProcesso.hashCode());
		result = prime * result + ((processoTrfObj == null) ? 0 : processoTrfObj.hashCode());
		
		result = prime * result + ((numeroSequencia == null) ? 0 : numeroSequencia.hashCode());
		result = prime * result
				+ ((oabRepresentanteDestinatario == null) ? 0 : oabRepresentanteDestinatario.hashCode());
		result = prime * result + ((orgaoJulgadorColegiadoObj == null) ? 0 : orgaoJulgadorColegiadoObj.hashCode());
		result = prime * result + ((orgaoJulgadorObj == null) ? 0 : orgaoJulgadorObj.hashCode());
		result = prime * result + ((outroDocumentoDestinatario == null) ? 0 : outroDocumentoDestinatario.hashCode());
		result = prime * result + ((prioridadeObj == null) ? 0 : prioridadeObj.hashCode());
		result = prime * result + ((ramoJustica == null) ? 0 : ramoJustica.hashCode());
		result = prime * result + ((tipoSituacaoExpediente == null) ? 0 : tipoSituacaoExpediente.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PesquisaExpedientesVO other = (PesquisaExpedientesVO) obj;
		if (apenasCaixasAtivas == null) {
			if (other.apenasCaixasAtivas != null)
				return false;
		} else if (!apenasCaixasAtivas.equals(other.apenasCaixasAtivas))
			return false;
		if (idProcessoParteExpediente == null) {
			if (other.idProcessoParteExpediente != null)
				return false;
		} else if (!idProcessoParteExpediente.equals(other.idProcessoParteExpediente))
			return false;
		if (apenasCaixasComResultados == null) {
			if (other.apenasCaixasComResultados != null)
				return false;
		} else if (!apenasCaixasComResultados.equals(other.apenasCaixasComResultados))
			return false;
		if (apenasPrioridade == null) {
			if (other.apenasPrioridade != null)
				return false;
		} else if (!apenasPrioridade.equals(other.apenasPrioridade))
			return false;
		if (apenasSemCaixa == null) {
			if (other.apenasSemCaixa != null)
				return false;
		} else if (!apenasSemCaixa.equals(other.apenasSemCaixa))
			return false;
		if (assuntoJudicial == null) {
			if (other.assuntoJudicial != null)
				return false;
		} else if (!assuntoJudicial.equals(other.assuntoJudicial))
			return false;
		if (assuntoJudicialObj == null) {
			if (other.assuntoJudicialObj != null)
				return false;
		} else if (!assuntoJudicialObj.equals(other.assuntoJudicialObj))
			return false;
		if (idCaixaAdvProc == null) {
			if (other.idCaixaAdvProc != null)
				return false;
		} else if (!idCaixaAdvProc.equals(other.idCaixaAdvProc))
			return false;
		if (classeJudicial == null) {
			if (other.classeJudicial != null)
				return false;
		} else if (!classeJudicial.equals(other.classeJudicial))
			return false;
		if (classeJudicialObj == null) {
			if (other.classeJudicialObj != null)
				return false;
		} else if (!classeJudicialObj.equals(other.classeJudicialObj))
			return false;
		if (cnpjDestinatario == null) {
			if (other.cnpjDestinatario != null)
				return false;
		} else if (!cnpjDestinatario.equals(other.cnpjDestinatario))
			return false;
		if (cpfDestinatario == null) {
			if (other.cpfDestinatario != null)
				return false;
		} else if (!cpfDestinatario.equals(other.cpfDestinatario))
			return false;
		if (dataAutuacaoFinal == null) {
			if (other.dataAutuacaoFinal != null)
				return false;
		} else if (!dataAutuacaoFinal.equals(other.dataAutuacaoFinal))
			return false;
		if (dataAutuacaoInicial == null) {
			if (other.dataAutuacaoInicial != null)
				return false;
		} else if (!dataAutuacaoInicial.equals(other.dataAutuacaoInicial))
			return false;
		if (dataCriacaoExpedienteFinal == null) {
			if (other.dataCriacaoExpedienteFinal != null)
				return false;
		} else if (!dataCriacaoExpedienteFinal.equals(other.dataCriacaoExpedienteFinal))
			return false;
		if (dataCriacaoExpedienteInicial == null) {
			if (other.dataCriacaoExpedienteInicial != null)
				return false;
		} else if (!dataCriacaoExpedienteInicial.equals(other.dataCriacaoExpedienteInicial))
			return false;
		if (digitoVerificador == null) {
			if (other.digitoVerificador != null)
				return false;
		} else if (!digitoVerificador.equals(other.digitoVerificador))
			return false;
		if (idJurisdicao == null) {
			if (other.idJurisdicao != null)
				return false;
		} else if (!idJurisdicao.equals(other.idJurisdicao))
			return false;
		if (idTipoProcessoDocumento == null) {
			if (other.idTipoProcessoDocumento != null)
				return false;
		} else if (!idTipoProcessoDocumento.equals(other.idTipoProcessoDocumento))
			return false;
		if (inMeioComunicacao == null) {
			if (other.inMeioComunicacao != null)
				return false;
		} else if (!inMeioComunicacao.equals(other.inMeioComunicacao))
			return false;
		if (nomeDestinatario == null) {
			if (other.nomeDestinatario != null)
				return false;
		} else if (!nomeDestinatario.equals(other.nomeDestinatario))
			return false;
		if (nomeParte == null) {
			if (other.nomeParte != null)
				return false;
		} else if (!nomeParte.equals(other.nomeParte))
			return false;
		if (numeroAno == null) {
			if (other.numeroAno != null)
				return false;
		} else if (!numeroAno.equals(other.numeroAno))
			return false;
		if (numeroOrgaoJustica == null) {
			if (other.numeroOrgaoJustica != null)
				return false;
		} else if (!numeroOrgaoJustica.equals(other.numeroOrgaoJustica))
			return false;
		if (numeroOrigem == null) {
			if (other.numeroOrigem != null)
				return false;
		} else if (!numeroOrigem.equals(other.numeroOrigem))
			return false;
		if (numeroProcesso == null) {
			if (other.numeroProcesso != null)
				return false;
		} else if (!numeroProcesso.equals(other.numeroProcesso))
			return false;
		if (processoTrfObj == null) {
			if (other.processoTrfObj != null)
				return false;
		} else if (!processoTrfObj.equals(other.processoTrfObj))
			return false;
		if (numeroSequencia == null) {
			if (other.numeroSequencia != null)
				return false;
		} else if (!numeroSequencia.equals(other.numeroSequencia))
			return false;
		if (oabRepresentanteDestinatario == null) {
			if (other.oabRepresentanteDestinatario != null)
				return false;
		} else if (!oabRepresentanteDestinatario.equals(other.oabRepresentanteDestinatario))
			return false;
		if (orgaoJulgadorColegiadoObj == null) {
			if (other.orgaoJulgadorColegiadoObj != null)
				return false;
		} else if (!orgaoJulgadorColegiadoObj.equals(other.orgaoJulgadorColegiadoObj))
			return false;
		if (orgaoJulgadorObj == null) {
			if (other.orgaoJulgadorObj != null)
				return false;
		} else if (!orgaoJulgadorObj.equals(other.orgaoJulgadorObj))
			return false;
		if (outroDocumentoDestinatario == null) {
			if (other.outroDocumentoDestinatario != null)
				return false;
		} else if (!outroDocumentoDestinatario.equals(other.outroDocumentoDestinatario))
			return false;
		if (prioridadeObj == null) {
			if (other.prioridadeObj != null)
				return false;
		} else if (!prioridadeObj.equals(other.prioridadeObj))
			return false;
		if (ramoJustica == null) {
			if (other.ramoJustica != null)
				return false;
		} else if (!ramoJustica.equals(other.ramoJustica))
			return false;
		if (tipoSituacaoExpediente != other.tipoSituacaoExpediente)
			return false;
		
		return true;
	}

	public Integer getPessoaParteRepresentado() {
		return pessoaParteRepresentado;
	}

	public void setPessoaParteRepresentado(Integer pessoaParteRepresentado) {
		this.pessoaParteRepresentado = pessoaParteRepresentado;
	}
}
