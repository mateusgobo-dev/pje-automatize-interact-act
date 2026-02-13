package br.jus.cnj.pje.webservice.client.domicilioeletronico.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.hibernate.LazyInitializationException;

import com.google.common.base.MoreObjects;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.manager.PessoaMagistradoManager;
import br.jus.pdpj.commons.models.enums.TribunalEnum;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.Cep;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.Municipio;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrfConexao;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * Classe que representa um processo usado pelo Domicï¿½lio Eletrï¿½nico.
 * 
 */
public class ProcessoDTO implements Serializable {

	
	private static final long serialVersionUID = 1L;
	
	private String numeroProcesso;
	private String siglaTribunal;
	private String uf;
	private String nomeClasseProcessual;
	private String nomeJuiz;
	private String nomeOrgaoJulgador;
	private String valorCausa;
	private String numeroGrau;
	private List<AssuntoDTO> assuntos = new ArrayList<>();
	private List<ProcessoVinculadoDTO> processoVinculado = new ArrayList<>();
	private List<ParteDTO> partes = new ArrayList<>();
	private List<String> numeroProcessoRelacionado;
	private String codigoLocalidade;
	private String codigoIBGE;
	private Integer codigoOrgaoJulgador;
	private boolean intervencaoMP;
	private String codigoClasseProcessual;

	/**
	 * Construtor.
	 */
	public ProcessoDTO() {
		//Construtor
	}
	
	
	/**
	 * Construtor.
	 * 
	 * @param processo
	 */
	public ProcessoDTO(ProcessoTrf processo) {
		if (processo != null) {
			String ativaParametroCodigoCalsseProcessual = ComponentUtil.getParametroDAO()
					.valueOf(Parametros.PJE_DOMICILIO_ELETRONICO_CODIGO_CLASSE_PROCESSUAL);

			boolean isCodigoClasseProecessualHabilitado = Boolean.parseBoolean(ativaParametroCodigoCalsseProcessual);

			try {
				Jurisdicao jurisdicao = processo.getJurisdicao();
				OrgaoJulgadorColegiado orgaoJulgadorColegiado = processo.getOrgaoJulgadorColegiado();
				OrgaoJulgador orgaoJulgador = processo.getOrgaoJulgador();
				adicionarOrgaoJulgador(orgaoJulgador);
				adicionarOrgaoJulgadorColegiado(orgaoJulgadorColegiado);
				adicionarNomeMagistrado(orgaoJulgador, orgaoJulgadorColegiado);
				adicionarJurisdicao(jurisdicao);
				adicionarLocalizacao(orgaoJulgador);
				String orgaoJustica = ParametroUtil.instance().recuperarNumeroOrgaoJustica();
				TribunalEnum tribunal = TribunalEnum.findByJTR(orgaoJustica);
				String grau = ParametroUtil.instance().getCodigoInstanciaAtual().substring(0, 1);
				setNumeroProcesso(processo.getNumeroProcesso());
				setSiglaTribunal(tribunal.getSigla());
				setNomeClasseProcessual(processo.getClasseJudicialStr());
				setValorCausa(StringUtil.formatarValorMoedaSemPontos(processo.getValorCausa()));
				setNumeroGrau(grau);
				setAssuntos(converterAssuntoTrfParaAssuntoDTO(processo.getAssuntoTrfList()));
				setNumeroProcessoRelacionado(processo.getProcessoTrfConexaoListStr());
				setPartes(converterProcessoParteParaParteDTO(processo.getProcessoParteAutorReuList()));
				setProcessoVinculado(converterNumeroProcessoParaProcessoVinculadoDTO(processo.getProcessoTrfConexaoList()));
				setIntervencaoMP(processo.getClasseJudicial().getExigeFiscalLei());
				if (isCodigoClasseProecessualHabilitado) {
					setCodigoClasseProcessual(processo.getClasseJudicial().getCodClasseJudicial());
				}
			} catch (LazyInitializationException e) {
				processo = ComponentUtil.getComponent(ProcessoTrfManager.class)
						.getProcessoTrfByIdProcessoTrf(processo.getIdProcessoTrf());

				setAssuntos(converterAssuntoTrfParaAssuntoDTO(processo.getAssuntoTrfList()));
				setNumeroProcessoRelacionado(processo.getProcessoTrfConexaoListStr());
				setPartes(converterProcessoParteParaParteDTO(processo.getProcessoParteAutorReuList()));
				setProcessoVinculado(converterNumeroProcessoParaProcessoVinculadoDTO(processo.getProcessoTrfConexaoList()));
				setIntervencaoMP(processo.getClasseJudicial().getExigeFiscalLei());
				if (isCodigoClasseProecessualHabilitado) {
					setCodigoClasseProcessual(processo.getClasseJudicial().getCodClasseJudicial());
				}
			}
		}
	}

	private void adicionarJurisdicao(Jurisdicao jurisdicao) {
		if (jurisdicao != null) {
			Integer id = MoreObjects.firstNonNull(jurisdicao.getNumeroOrigem(), jurisdicao.getIdJurisdicao());
			setCodigoLocalidade(String.valueOf(id));
		}
	}

	private void adicionarLocalizacao(OrgaoJulgador orgaoJulgador) {
		if (orgaoJulgador != null
				&& orgaoJulgador.getLocalizacao().getEndereco() != null
				&& orgaoJulgador.getLocalizacao().getEndereco().getCep().getMunicipio() != null) {

			Localizacao localizacao = orgaoJulgador.getLocalizacao();
			Endereco endereco = localizacao.getEndereco();
			Cep cep = endereco.getCep();
			Municipio municipio = cep.getMunicipio();
			setCodigoIBGE(municipio.getCodigoIbge());
		}
	}
	
	private void adicionarOrgaoJulgadorColegiado(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		if (orgaoJulgadorColegiado != null) {
			setCodigoOrgaoJulgador(orgaoJulgadorColegiado.getIdOrgaoJulgadorColegiado());
			setNomeOrgaoJulgador(orgaoJulgadorColegiado.getOrgaoJulgadorColegiado());
		
			setUf(obterUf(orgaoJulgadorColegiado.getJurisdicao()));
		} 
	}

	private void adicionarOrgaoJulgador( OrgaoJulgador orgaoJulgador) {
		  if (orgaoJulgador != null) {
				setCodigoOrgaoJulgador(orgaoJulgador.getIdOrgaoJulgador());
				setNomeOrgaoJulgador(orgaoJulgador.getOrgaoJulgador());
				PessoaMagistrado magistrado = PessoaMagistradoManager.instance().getMagistradoTitular(orgaoJulgador);
				setNomeJuiz((magistrado != null ? magistrado.getNome() : null));
				setUf(obterUf(orgaoJulgador.getJurisdicao()));
			}
	 }
	 
	private void adicionarNomeMagistrado(OrgaoJulgador orgaoJulgador, OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		PessoaMagistrado magistrado = PessoaMagistradoManager.instance().getMagistradoTitular(orgaoJulgador,
				orgaoJulgadorColegiado);
		 setNomeJuiz((magistrado != null ? magistrado.getNome() : null));
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
	 * @return the siglaTribunal
	 */
	public String getSiglaTribunal() {
		return siglaTribunal;
	}

	/**
	 * @param siglaTribunal the siglaTribunal to set
	 */
	public void setSiglaTribunal(String siglaTribunal) {
		this.siglaTribunal = siglaTribunal;
	}

	/**
	 * @return the uf
	 */
	public String getUf() {
		return uf;
	}

	/**
	 * @param uf the uf to set
	 */
	public void setUf(String uf) {
		this.uf = uf;
	}

	/**
	 * @return the nomeClasseProcessual
	 */
	public String getNomeClasseProcessual() {
		return nomeClasseProcessual;
	}

	/**
	 * @param nomeClasseProcessual the nomeClasseProcessual to set
	 */
	public void setNomeClasseProcessual(String nomeClasseProcessual) {
		this.nomeClasseProcessual = nomeClasseProcessual;
	}

	/**
	 * @return the nomeJuiz
	 */
	public String getNomeJuiz() {
		return nomeJuiz;
	}

	/**
	 * @param nomeJuiz the nomeJuiz to set
	 */
	public void setNomeJuiz(String nomeJuiz) {
		this.nomeJuiz = nomeJuiz;
	}

	/**
	 * @return the nomeOrgaoJulgador
	 */
	public String getNomeOrgaoJulgador() {
		return nomeOrgaoJulgador;
	}

	/**
	 * @param nomeOrgaoJulgador the nomeOrgaoJulgador to set
	 */
	public void setNomeOrgaoJulgador(String nomeOrgaoJulgador) {
		this.nomeOrgaoJulgador = nomeOrgaoJulgador;
	}

	/**
	 * @return the valorCausa
	 */
	public String getValorCausa() {
		return valorCausa;
	}

	/**
	 * @param valorCausa the valorCausa to set
	 */
	public void setValorCausa(String valorCausa) {
		this.valorCausa = valorCausa;
	}

	/**
	 * @return the numeroGrau
	 */
	public String getNumeroGrau() {
		return numeroGrau;
	}

	/**
	 * @param numeroGrau the numeroGrau to set
	 */
	public void setNumeroGrau(String numeroGrau) {
		this.numeroGrau = numeroGrau;
	}

	/**
	 * @return the assuntos
	 */
	public List<AssuntoDTO> getAssuntos() {
		return assuntos;
	}

	/**
	 * @param assuntos the assuntos to set
	 */
	public void setAssuntos(List<AssuntoDTO> assuntos) {
		this.assuntos = assuntos;
	}
	
	/**
	 * @return the processoVinculado
	 */
	public List<ProcessoVinculadoDTO> getProcessoVinculado() {
		return processoVinculado;
	}

	/**
	 * @param processoVinculado the processoVinculado to set
	 */
	public void setProcessoVinculado(List<ProcessoVinculadoDTO> processoVinculado) {
		this.processoVinculado = processoVinculado;
	}

	/**
	 * @return the partes
	 */
	public List<ParteDTO> getPartes() {
		return partes;
	}

	/**
	 * @param partes the partes to set
	 */
	public void setPartes(List<ParteDTO> partes) {
		this.partes = partes;
	}
	
	/**
	 * @return the numeroProcessoRelacionado
	 */
	public List<String> getNumeroProcessoRelacionado() {
		return numeroProcessoRelacionado;
	}

	/**
	 * @param numeroProcessoRelacionado the numeroProcessoRelacionado to set
	 */
	public void setNumeroProcessoRelacionado(List<String> numeroProcessoRelacionado) {
		this.numeroProcessoRelacionado = numeroProcessoRelacionado;
	}

	/**
	 * @param assuntos the assuntos to set
	 */
	protected List<AssuntoDTO> converterAssuntoTrfParaAssuntoDTO(List<AssuntoTrf> assuntos) {
		List<AssuntoDTO> dtos = new ArrayList<>();
			
		if (assuntos != null) {
			for (AssuntoTrf assunto : assuntos) {
				dtos.add(new AssuntoDTO(assunto));
			}
		}
		
		return dtos;
	}
	
	/**
	 * @param assuntos the partes to set
	 */
	protected List<ParteDTO> converterProcessoParteParaParteDTO(List<ProcessoParte> partes) {
		List<ParteDTO> dtos = new ArrayList<>();
			
		if (partes != null) {
			for (ProcessoParte parte : partes) {
				if (BooleanUtils.isTrue(parte.getIsAtivo()) && BooleanUtils.isFalse(parte.getIsBaixado())) {
					dtos.add(new ParteDTO(parte));
				}
			}
		}
		
		return dtos;
	}
	
	/**
	 * Converte uma lista de número de processo para lista de ProcessoVinculadoDTO.
	 * @param processos
	 * @return Lista de ProcessoVinculadoDTO.
	 */
	protected List<ProcessoVinculadoDTO> converterNumeroProcessoParaProcessoVinculadoDTO(List<ProcessoTrfConexao> processos) {
		List<ProcessoVinculadoDTO> resultado = new ArrayList<>();
		if (ProjetoUtil.isNotVazio(processos)) {
			for (ProcessoTrfConexao processo : processos) {
				ProcessoVinculadoDTO vinculado = new ProcessoVinculadoDTO(); 
				vinculado.setNumeroProcesso(processo.getNumeroProcesso());
				resultado.add(vinculado);
			}
		}
		return resultado;
	}
	
	/**
	 * @param jurisdicao
	 * @return UF da jurisdição.
	 */
	protected String obterUf(Jurisdicao jurisdicao) {
		return (jurisdicao != null ? jurisdicao.getEstado().getCodEstado() : null);
	}

	/**
	 * @return codigoLocalidade
	 */
	public String getCodigoLocalidade() {
		return codigoLocalidade;
	}

	/**
	 * @param codigoLocalidade
	 */
	public void setCodigoLocalidade(String codigoLocalidade) {
		this.codigoLocalidade = codigoLocalidade;
	}

	/**
	 * @return codigoIbge
	 */
	public String getCodigoIBGE() {
		return codigoIBGE;
	}

	/**
	 * @param codigoIBGE
	 */
	public void setCodigoIBGE(String codigoIBGE) {
		this.codigoIBGE = codigoIBGE;
	}

	/**
	 * @return codigoOrgaoJulgador
	 */
	public Integer getCodigoOrgaoJulgador() {
		return codigoOrgaoJulgador;
	}

	/**
	 * @param codigoOrgaoJulgador
	 */
	public void setCodigoOrgaoJulgador(Integer codigoOrgaoJulgador) {
		this.codigoOrgaoJulgador = codigoOrgaoJulgador;
	}

	/**
	 * @return intervencaoMP
	 */
	public boolean isIntervencaoMP() {
		return intervencaoMP;
	}

	/**
	 * @param intervencaoMP
	 */
	public void setIntervencaoMP(boolean intervencaoMP) {
		this.intervencaoMP = intervencaoMP;
	}

	public String getCodigoClasseProcessual() {
		return codigoClasseProcessual;
	}

	public void setCodigoClasseProcessual(String codigoClasseProcessual) {
		this.codigoClasseProcessual = codigoClasseProcessual;
	}
}