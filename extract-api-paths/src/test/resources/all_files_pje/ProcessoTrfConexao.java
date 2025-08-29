/**
 * pje-comum
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.nucleo.entidades;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.enums.PrevencaoEnum;
import br.jus.pje.nucleo.enums.TipoConexaoEnum;

@Entity
@Table(name = "tb_processo_trf_conexao")
@org.hibernate.annotations.GenericGenerator(name = "gen_processo_trf_conexao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_processo_trf_conexao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoTrfConexao implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoTrfConexao,Integer> {

	private static final long serialVersionUID = 1L;

	private int idProcessoTrfConexao;
	private ProcessoTrf processoTrfConexo;
	private ProcessoTrf processoTrf;
	private String sessaoJudiciaria;
	private String orgaoJulgador;
	private String numeroProcesso;
	private String linkSessaoJudiciaria;
	private String classeJudicial;
	private TipoConexaoEnum tipoConexao;
	private PrevencaoEnum prevencao = PrevencaoEnum.PE;
	private Pessoa pessoaFisica;
	private Date dtPossivelPrevencao;
	private Date dtValidaPrevencao;
	private Date dataRegistro;
	private String assunto;
	private List<AssuntoTrf> assuntos;
	private ProcessoDocumento processoDocumento;
	private String listaPoloAtivo;
	private String listaPoloPassivo;
	private Boolean ativo = Boolean.TRUE;
	private String justificativa;

	public ProcessoTrfConexao() { }

	@Id
	@GeneratedValue(generator = "gen_processo_trf_conexao")
	@Column(name = "id_processo_trf_conexao", nullable = false)
	public int getIdProcessoTrfConexao() {
		return idProcessoTrfConexao;
	}

	public void setIdProcessoTrfConexao(int idProcessoConexao) {
		this.idProcessoTrfConexao = idProcessoConexao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf_conexo")
	public ProcessoTrf getProcessoTrfConexo() {
		return processoTrfConexo;
	}

	public void setProcessoTrfConexo(ProcessoTrf processoTrfConexo) {
		this.processoTrfConexo = processoTrfConexo;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf")
	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@Column(name = "ds_sessao_judiciaria", length = 200)
	@Length(max = 200)
	public String getSessaoJudiciaria() {
		return sessaoJudiciaria;
	}

	public void setSessaoJudiciaria(String sessaoJudiciaria) {
		this.sessaoJudiciaria = sessaoJudiciaria;
	}

	@Column(name = "ds_orgao_julgador", length = 200)
	@Length(max = 200)
	public String getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(String orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	@Column(name = "nr_processo", length = 30)
	@Length(max = 30)
	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	@Column(name = "tp_tipo_conexao", length = 2)
	@Enumerated(EnumType.STRING)
	public TipoConexaoEnum getTipoConexao() {
		return tipoConexao;
	}

	public void setTipoConexao(TipoConexaoEnum tipoConexao) {
		this.tipoConexao = tipoConexao;
	}

	@Column(name = "in_valida_prenvencao", length = 2)
	@Enumerated(EnumType.STRING)
	public PrevencaoEnum getPrevencao() {
		return prevencao;
	}

	public void setPrevencao(PrevencaoEnum prevencao) {
		this.prevencao = prevencao;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_pess_fsca_validou_prevencao")
	public Pessoa getPessoaFisica() {
		return pessoaFisica;
	}

	public void setPessoaFisica(Pessoa pessoaFisica) {
		this.pessoaFisica = pessoaFisica;
	}
	
	/**
	 * Sobrecarga do método {@link #setPessoaFisica(PessoaFisica)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída.
	 */
	public void setPessoaFisica(PessoaFisicaEspecializada pessoa){
		setPessoaFisica(pessoa != null ? pessoa.getPessoa() : (PessoaFisica) null);
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_possivel_prevencao")
	public Date getDtPossivelPrevencao() {
		return dtPossivelPrevencao;
	}

	public void setDtPossivelPrevencao(Date dtPossivelPrevencao) {
		this.dtPossivelPrevencao = dtPossivelPrevencao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_valida_prevencao")
	public Date getDtValidaPrevencao() {
		return dtValidaPrevencao;
	}

	public void setDtValidaPrevencao(Date dtValidaPrevencao) {
		this.dtValidaPrevencao = dtValidaPrevencao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_registro")
	public Date getDataRegistro() {
		return dataRegistro;
	}

	public void setDataRegistro(Date dataRegistro) {
		this.dataRegistro = dataRegistro;
	}

	@Transient
	public List<String> getAssuntoTrf() {
		if (processoTrfConexo != null) {
			List<String> ret = new ArrayList<String>();
			for(ProcessoAssunto pa: processoTrfConexo.getProcessoAssuntoList()){
				ret.add(pa.toString());
			}
			return ret.size() == 0 ? null : ret;
		}
		return null;
	}

	public void setAssunto(String assunto) {
		this.assunto = assunto;
	}

	@Column(name = "ds_assunto_principal", length = 200, nullable = true)
	@Length(max = 200)
	public String getAssunto() {
		return assunto;
	}

	@Transient
	public List<AssuntoTrf> getAssuntos() {
		return assuntos;
	}

	public void setAssuntos(List<AssuntoTrf> assuntos) {
		this.assuntos = assuntos;
	}

	@Transient
	public List<String> getPoloAtivo() {
		if (processoTrfConexo != null) {
			List<String> ret = new ArrayList<String>();
			for(ProcessoParte pp: processoTrfConexo.getProcessoPartePoloAtivoSemAdvogadoList()){
				ret.add(pp.toString());
			}
			return ret.size() == 0 ? null : ret;
		}
		return null;
	}

	@Transient
	public List<String> getPoloPassivo() {
		if (processoTrfConexo != null) {
			List<String> ret = new ArrayList<String>();
			for(ProcessoParte pp: processoTrfConexo.getProcessoPartePoloPassivoSemAdvogadoList()){
				ret.add(pp.toString());
			}
			return ret.size() == 0 ? null : ret;
		}
		return null;
	}

	@ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.REFRESH })
	@JoinColumn(name = "id_processo_documento")
	public ProcessoDocumento getProcessoDocumento() {
		return processoDocumento;
	}

	public void setProcessoDocumento(ProcessoDocumento processoDocumento) {
		this.processoDocumento = processoDocumento;
	}

	@Column(name = "ds_link_sessao_judiciaria", length = 200)
	@Length(max = 200)
	public String getLinkSessaoJudiciaria() {
		return linkSessaoJudiciaria;
	}

	public void setLinkSessaoJudiciaria(String linkSessaoJudiciaria) {
		this.linkSessaoJudiciaria = linkSessaoJudiciaria;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_polo_ativo")
	public String getListaPoloAtivo() {
		return listaPoloAtivo;
	}

	public void setListaPoloAtivo(String listaPoloAtivo) {
		this.listaPoloAtivo = listaPoloAtivo;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_polo_passivo")
	public String getListaPoloPassivo() {
		return listaPoloPassivo;
	}

	public void setListaPoloPassivo(String listaPoloPassivo) {
		this.listaPoloPassivo = listaPoloPassivo;
	}

	@Column(name = "ds_classe_judicial", length = 200)
	@Length(max = 200)
	public String getClasseJudicial() {
		if (processoTrfConexo != null && processoTrfConexo.getClasseJudicial() != null) {
			return processoTrfConexo.getClasseJudicial().getClasseJudicial();
		}
		return classeJudicial;
	}

	public void setClasseJudicial(String classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Transient
	/**
	 * Método que retorna o numero do processo com a sigla do cargo.
	 */
	public String getNumeroProcessoCargo() {
		if(processoTrfConexo != null){
			return processoTrfConexo.getNumeroProcesso();
		}
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ProcessoTrfConexao)) {
			return false;
		}
		ProcessoTrfConexao other = (ProcessoTrfConexao) obj;
		if (getIdProcessoTrfConexao() != other.getIdProcessoTrfConexao()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoTrfConexao();
		return result;
	}

	@Column(name = "ds_justificativa", length = 200)
	@Length(max = 200)
	public String getJustificativa() {
		return justificativa;
	}

	public void setJustificativa(String justificativa) {
		this.justificativa = justificativa;
	}

	/**
	 * Método utilizado na tela de lançar movimentos pelo complemento dinâmico
	 * "número do processo".
	 * 
	 * @author Guilherme Bispo
	 * @category PJE-JT
	 * @return Retorna o número do processo conexo formatado. @
	 */
	@Override
	public String toString() {
		if (processoTrfConexo != null) {
			return processoTrfConexo.getNumeroProcesso();
		}
		return super.toString();
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoTrfConexao> getEntityClass() {
		return ProcessoTrfConexao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoTrfConexao());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
