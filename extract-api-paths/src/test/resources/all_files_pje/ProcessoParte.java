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
import java.util.Collections;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import br.jus.pje.nucleo.anotacoes.IndexedEntity;
import br.jus.pje.nucleo.anotacoes.Mapping;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;
import br.jus.pje.nucleo.enums.TipoNomeAlternativoEnum;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;
import br.jus.pje.nucleo.util.StringUtil;

@Entity
@javax.persistence.Cacheable(true)
@Cache(region = "FechamentoPautaSessaoJulgamento", usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Table(name = "tb_processo_parte")
@IndexedEntity(id="idProcessoParte", value="parte", owners={"processoTrf"},
	mappings={
		@Mapping(beanPath="pessoa", mappedPath="pessoa"),
		@Mapping(beanPath="tipoParte.tipoParte", mappedPath="tipo"),
		@Mapping(beanPath="inParticipacao.label", mappedPath="polo"),
		@Mapping(beanPath="parteSigilosa", mappedPath="sigilosa")
})
@org.hibernate.annotations.GenericGenerator(name = "gen_processo_parte", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_processo_parte"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoParte implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoParte,Integer>, Comparable<ProcessoParte> {

	private static final long serialVersionUID = 1L;

	private int idProcessoParte;
	private TipoParte tipoParte;
	private Pessoa pessoa;
	private Integer idPessoa;
	private ProcessoTrf processoTrf;
	private ProcessoParteParticipacaoEnum inParticipacao;
	private Procuradoria procuradoria;

	private Boolean checkado = Boolean.FALSE;
	private Boolean checkVisibilidade = Boolean.FALSE;
	private Integer prazoLegal;
	private Integer prazoProcessual;
	private Boolean parteSigilosa = Boolean.FALSE;
	private Boolean partePrincipal = Boolean.FALSE;
	private Boolean isEnderecoDesconhecido = Boolean.FALSE;
	private ProcessoParteSituacaoEnum inSituacao = ProcessoParteSituacaoEnum.A;
	private Boolean isAtivo = Boolean.TRUE;
	private Boolean isBaixado = Boolean.FALSE;
	private Boolean isSuspenso = false;
	private String	formatar;
	private Integer ordem = 1;

	private PessoaNomeAlternativo pessoaNomeAlternativo;

	private List<ProcessoParteAdvogado> processoParteAdvogadoList = new ArrayList<>(0);
	private List<ProcessoParteRepresentante> processoParteRepresentanteList = new ArrayList<>(0);
	private List<ProcessoParteRepresentante> processoParteRepresentanteList2 = new ArrayList<>(0);
	private List<ProcessoParteEndereco> processoParteEnderecoList = new ArrayList<>(0);
	private List<InformacaoCriminalRelevante> informacaoCriminalRelevanteList = new ArrayList<>(0);
	private List<ProcessoParteHistorico> processoParteHistoricoList = new ArrayList<>(0);
	private List<ProcessoParteVisibilidadeSigilo> visualizadores = new ArrayList<>(0);

	public ProcessoParte() {
		this.ordem = 1;
	}
	
	public ProcessoParte(ProcessoParteParticipacaoEnum inParticipacao, Boolean partePrincipal) {
		this.inParticipacao = inParticipacao;
		this.partePrincipal = partePrincipal;
		this.ordem = 1;
	}

	@Id
	@GeneratedValue(generator = "gen_processo_parte")
	@Column(name = "id_processo_parte", unique = true, nullable = false)
	public int getIdProcessoParte() {
		return idProcessoParte;
	}

	public void setIdProcessoParte(int idProcessoParte) {
		this.idProcessoParte = idProcessoParte;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_tipo_parte", nullable = false)
	@NotNull
	public TipoParte getTipoParte() {
		return this.tipoParte;
	}

	public void setTipoParte(TipoParte tipoParte) {
		this.tipoParte = tipoParte;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_pessoa", nullable = false)
	@NotNull
	public Pessoa getPessoa() {
		return this.pessoa;
	}

	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}
	
	@Column(name="id_pessoa", insertable=false, updatable=false)
	public Integer getIdPessoa() {
		return idPessoa;
	}

	public void setIdPessoa(Integer idPessoa) {
		this.idPessoa = idPessoa;
	}

	/**
	 * Sobrecarga de {@link #setPessoa(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída.
	 */
	public void setPessoa(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoa(pessoa.getPessoa());
		} else {
			setPessoa((Pessoa)null);
		}
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_processo_trf", nullable = false, updatable = false)
	@NotNull
	public ProcessoTrf getProcessoTrf() {
		return this.processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	/**
	 * Retorna informações da parte no padrão "Nome da Parte - CPF|CNPJ : 9999999999 (IMPETRANTE)"
	 * @return informações da parte conforme descritivo acima. 
	 */
	@Override
	public String toString() {
		String ret = "";
		if (pessoa != null) {
			String informacoesPessoa = obterInformacoes();
			if (!informacoesPessoa.isEmpty() && getTipoParte() != null && getTipoParte().toString() != null ){
				ret = informacoesPessoa.concat(" (").concat(getTipoParte().toString()).concat(")");
			}
		}
		return ret;		 
	}

	public String toStringSemSegredo() {
		String ret = "";
		if (pessoa != null) {
			String informacoesPessoa = obterInformacoesSemSegredo();
			if (!informacoesPessoa.isEmpty() && getTipoParte() != null && getTipoParte().toString() != null) {
				ret = informacoesPessoa.concat(" (").concat(getTipoParte().toString()).concat(")");
			}
		}
		return ret;
	}

	/**
	 * Retorna informações referente a pessoa no padrão "Nome da Pessoa - CPF|CNPJ : 999999999"
	 * Se a pessoa nao tiver um documento CPF|CNPJ retorna apenas "Nome da Pessoa"
	 * @return informações da pessoa conforme descritivo acima.
	 */
	public String obterInformacoes(){
		String ret = getNomeParte();

		if (parteSigilosa == false) {
			String documento = this.getPessoa().getDocumentoCpfCnpj(true);
			if (documento != null) {
				ret = ret.concat(" - ").concat(documento);
			}
		}

		return ret;		
	}

	public String obterInformacoesSemSegredo() {
		String ret = getNomeParteSemSegredo();

		String documento = this.getPessoa().getDocumentoCpfCnpj(true);
		if (documento != null) {
			ret = ret.concat(" - ").concat(documento);
		}
		return ret;
	}

	@Column(name = "in_participacao", length = 1)
	@Enumerated(EnumType.STRING)
	public ProcessoParteParticipacaoEnum getInParticipacao() {
		return inParticipacao;
	}

	public void setInParticipacao(ProcessoParteParticipacaoEnum inParticipacao) {
		this.inParticipacao = inParticipacao;
	}

	@Transient
	public String getPolo() {
		return tipoParte.getTipoParte();
	}

	/**
	 * Método utilizado para retornar o tipo da parte na impressão do detalhe do
	 * processo da consulta pública
	 * 
	 * @return
	 */
	@Transient
	public String getPoloTipoParteStr() {
		return tipoParte.getTipoParte();
	}

	@Transient
	public String getTipoParteStr() {
		return pessoa.getNome();
	}

	@Transient
	public Boolean getCheckado() {
		return checkado;
	}

	public void setCheckado(Boolean checkado) {
		this.checkado = checkado;
	}

	@Transient
	public Boolean getCheckVisibilidade() {
		return checkVisibilidade;
	}

	public void setCheckVisibilidade(Boolean checkVisibilidade) {
		this.checkVisibilidade = checkVisibilidade;
	}

	@Transient
	public Integer getPrazoLegal() {
		return prazoLegal;
	}

	public void setPrazoLegal(Integer prazoLegal) {
		this.prazoLegal = prazoLegal;
	}

	@Transient
	public Integer getPrazoProcessual() {
		return prazoProcessual;
	}

	public void setPrazoProcessual(Integer prazoProcessual) {
		this.prazoProcessual = prazoProcessual;
	}

	@Column(name = "in_segredo", nullable = false)
	@NotNull
	public Boolean getParteSigilosa() {
		return this.parteSigilosa;
	}

	public void setParteSigilosa(Boolean parteSigilosa) {
		this.parteSigilosa = parteSigilosa;
	}

	@Column(name = "in_parte_principal", nullable = false)
	@NotNull
	public Boolean getPartePrincipal() {
		return this.partePrincipal;
	}

	public void setPartePrincipal(Boolean partePrincipal) {
		this.partePrincipal = partePrincipal;
	}

	@Transient
	public String getNomeParte() {
		String nomeParte = StringUtils.EMPTY;

		if (parteSigilosa) {
			nomeParte = "Em segredo de justiça";
		} else if (pessoa != null) {
			nomeParte = getNomeParteSemSegredo();
		}
		return nomeParte;
	}

	@Transient
	public String getNomeParteSemSegredo() {
		String nomeParte = StringUtils.EMPTY;

		if (this.pessoaNomeAlternativo != null
				&& (this.getPessoa().getInTipoPessoa().equals(TipoPessoaEnum.F)
						|| this.getPessoa().getInTipoPessoa().equals(TipoPessoaEnum.J))
				&& this.pessoaNomeAlternativo.getTipoNomeAlternativo() == TipoNomeAlternativoEnum.O) {
			nomeParte = this.pessoaNomeAlternativo.getPessoaNomeAlternativo();
		} else if (this.getPessoa().getNome() != null) {
			nomeParte = this.getPessoa().getNome();
		}
		if (StringUtil.isSet(nomeParte)) {
			if (this.getPessoa() instanceof PessoaFisica) {
				PessoaFisica pessoaFisica = (PessoaFisica) this.getPessoa();

				String nomeSocialParte = pessoaFisica.getNomeSocial();

				if (nomeSocialParte == null) {
					Hibernate.initialize(pessoaFisica.getNomeSocial());

					nomeSocialParte = pessoaFisica.getNomeSocial();
				}

				if (nomeSocialParte != null && !StringUtil.isEmpty(nomeSocialParte)) {
					if (this.getPessoa().isMenor()) {
						nomeParte = StringUtil.retornarNomeExibicao(StringUtil.obtemIniciais(nomeParte),
								StringUtil.obtemIniciais(nomeSocialParte));
					} else {
						nomeParte = StringUtil.retornarNomeExibicao(nomeParte, nomeSocialParte);
					}
				} else if (this.getPessoa().isMenor()) {
					nomeParte = StringUtil.obtemIniciais(nomeParte);
				}
			}
		}
		return nomeParte;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "processoParte")
	public List<ProcessoParteAdvogado> getProcessoParteAdvogadoList() {
		return this.processoParteAdvogadoList;
	}

	public void setProcessoParteAdvogadoList(List<ProcessoParteAdvogado> processoParteAdvogadoList) {
		this.processoParteAdvogadoList = processoParteAdvogadoList;
	}

	// Lista de representantes desta parte
	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "processoParte", orphanRemoval = true)
	public List<ProcessoParteRepresentante> getProcessoParteRepresentanteList() {
		return this.processoParteRepresentanteList;
	}

	public void setProcessoParteRepresentanteList(List<ProcessoParteRepresentante> processoParteRepresentanteList) {
		this.processoParteRepresentanteList = processoParteRepresentanteList;
	}

	// Lista de quem este processoParte representa
	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "parteRepresentante", orphanRemoval = true)
	public List<ProcessoParteRepresentante> getProcessoParteRepresentanteList2() {
		return this.processoParteRepresentanteList2;
	}

	public void setProcessoParteRepresentanteList2(List<ProcessoParteRepresentante> processoParteRepresentanteList2) {
		this.processoParteRepresentanteList2 = processoParteRepresentanteList2;
	}
	
	@Transient
	public List<ProcessoParteRepresentante> getProcessoParteRepresentanteListAtivos() {
		List<ProcessoParteRepresentante> processoParteRepresentantesAtivos = new ArrayList<ProcessoParteRepresentante>();		

		for(ProcessoParteRepresentante representante : this.getProcessoParteRepresentanteList()){
		 if(representante.getInSituacao().equals(ProcessoParteSituacaoEnum.A)){
			 processoParteRepresentantesAtivos.add(representante); 
		 }
		}
		
 		return processoParteRepresentantesAtivos;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "processoParte")
	public List<ProcessoParteEndereco> getProcessoParteEnderecoList() {
		return this.processoParteEnderecoList;
	}

	public void setProcessoParteEnderecoList(List<ProcessoParteEndereco> processoParteEnderecoList) {
		this.processoParteEnderecoList = processoParteEnderecoList;
	}

	@OneToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY, mappedBy = "processoParte")
	public List<ProcessoParteHistorico> getProcessoParteHistoricoList() {
		return processoParteHistoricoList;
	}

	public void setProcessoParteHistoricoList(List<ProcessoParteHistorico> processoParteHistoricoList) {
		this.processoParteHistoricoList = processoParteHistoricoList;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "processoParte")
	public List<InformacaoCriminalRelevante> getInformacaoCriminalRelevanteList() {
		return informacaoCriminalRelevanteList;
	}

	public void setInformacaoCriminalRelevanteList(List<InformacaoCriminalRelevante> informacaoCriminalRelevanteList) {
		this.informacaoCriminalRelevanteList = informacaoCriminalRelevanteList;
	}

	@Transient
	public TipoParte getTipoPartePrincipalPoloAtivo() {
		try {
			return this.processoTrf.getListaPartePrincipalAtivo().get(0).getTipoParte();
		} catch (NullPointerException e) {
			return null;
		}
	}

	@Transient
	public TipoParte getTipoPartePrincipalPoloPassivo() {
		try {
			return this.processoTrf.getListaPartePrincipalPassivo().get(0).getTipoParte();
		} catch (NullPointerException e) {
			return null;
		}
	}

	@Column(name = "in_endereco_desconhecido", nullable = false)
	@NotNull
	public Boolean getIsEnderecoDesconhecido() {
		return isEnderecoDesconhecido;
	}

	public void setIsEnderecoDesconhecido(Boolean isEnderecoDesconhecido) {
		this.isEnderecoDesconhecido = isEnderecoDesconhecido;
	}

	@Column(name = "in_situacao", length = 1)
	@Enumerated(EnumType.STRING)
	public ProcessoParteSituacaoEnum getInSituacao() {
		return inSituacao;
	}

	public void setInSituacao(ProcessoParteSituacaoEnum inSituacao) {
		this.inSituacao = inSituacao;
	}

	@Transient
	public Boolean getIsAtivo() {
		this.isAtivo = (this.getInSituacao() == ProcessoParteSituacaoEnum.A);
		return isAtivo;
	}

	@Transient
	public List<IcrPrisao> getIcrPrisoesAtivas() {
		List<IcrPrisao> lista = new ArrayList<IcrPrisao>(0);
		for (InformacaoCriminalRelevante aux : getInformacaoCriminalRelevanteList()) {
			if (aux instanceof IcrPrisao) {
				if (((IcrPrisao) aux).getAtivo()) {
					lista.add((IcrPrisao) aux);
				}
			}
		}
		if (lista != null && !lista.isEmpty()) {
			return lista;
		}
		return null;
	}

	@Transient
	public EstabelecimentoPrisional getEstabelecimentoPrisionalAtual() {
		List<IcrPrisao> prisoes = getIcrPrisoesAtivas();
		if (prisoes != null && !prisoes.isEmpty()) {
			Collections.sort(prisoes);
			if (prisoes.get(prisoes.size() - 1).getUltimaTransferencia() != null) {
				return prisoes.get(prisoes.size() - 1).getUltimaTransferencia().getEstabelecimentoPrisional();
			} else {
				return prisoes.get(prisoes.size() - 1).getEstabelecimentoPrisional();
			}
		}
		return null;
	}

	@Transient
	public String getProcessoParteEnderecoStr() {
		StringBuilder sb = new StringBuilder();
		if (getProcessoParteEnderecoList().size() > 0) {
			for (ProcessoParteEndereco ppe : getProcessoParteEnderecoList()) {
				if (sb.length() > 0) {
					sb.append("\n");
				}
				sb.append("Endereço: " + ppe.getEndereco().getEnderecoCompleto());
			}
		}
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ProcessoParte)) {
			return false;
		}
		ProcessoParte other = (ProcessoParte) obj;
		if (getIdProcessoParte() != other.getIdProcessoParte() 
				|| getInParticipacao() != other.getInParticipacao()) {
			return false;
		}
		return true;
	}

	/**
	 * @author Rafael Barros / Sérgio Pacheco
	 * @since 1.2.0
	 * @see
	 * @category PJE-JT
	 * @return retorna a lista de enderecos da parte no processo
	 */
	@Transient
	public List<Endereco> getEnderecos() {
		List<Endereco> enderecos = new ArrayList<Endereco>();

		for (ProcessoParteEndereco endereco : processoParteEnderecoList) {
			enderecos.add(endereco.getEndereco());
		}

		return enderecos;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoParte();
		return result;
	}

	@Override
	public int compareTo(ProcessoParte p) {
		int a = 0;
		if (this.getOrdem()==null){
			if (getIdProcessoParte() < p.getIdProcessoParte()){
				a = -1;
			}
			if (getIdProcessoParte() > p.getIdProcessoParte()){
				a = 1;
			}
		}else{
			if (p.getOrdem() == null) {
				a = -1;
			} else {
				if (this.ordem < p.getOrdem()){
					a = -1;
				}
				if (this.ordem > p.getOrdem()){
					a = 1;
				}
			}
		}
		return a;
	}
	
    @Transient
    public Boolean getIsBaixado() {
        this.isBaixado = (this.getInSituacao() == ProcessoParteSituacaoEnum.B);
        return isBaixado;
    }
    
    @Transient
    public boolean getIsSuspenso() {
        this.isSuspenso = (this.getInSituacao() == ProcessoParteSituacaoEnum.S);
        return isSuspenso;
    }    
    
    @Transient
    public String getNomeParteSituacao(){
		StringBuilder sb = new StringBuilder();
		
		sb.append(tipoParte);
		sb.append(" - ");
		sb.append(toString());
		
		switch (inSituacao) {
			case A:
				sb.append("");
				break;
			case B:
				sb.append(" - Baixado");
				break;
			case S:
				sb.append(" - Suspenso");
				break;
			case I:
				sb.append(" - Inativo");
				break;
		}
		
		return sb.toString();
    }

    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_procuradoria")
	public Procuradoria getProcuradoria() {
		return procuradoria;
	}

	public void setProcuradoria(Procuradoria procuradoria) {
		this.procuradoria = procuradoria;
	}

	@Transient
	public String getFormatar() {
		return formatar;
	}

	public void setFormatar(String formatar) {
		this.formatar = formatar;
	}
	
	@Column(name = "nr_ordem", nullable = false)
	public Integer getOrdem() {
		return ordem;
	}
	public void setOrdem(Integer ordem) {
		this.ordem = ordem;
	}

	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoParte> getEntityClass() {
		return ProcessoParte.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoParte());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy="processoParte")
	public List<ProcessoParteVisibilidadeSigilo> getVisualizadores() {
		return visualizadores;
	}

	public void setVisualizadores(List<ProcessoParteVisibilidadeSigilo> visualizadores) {
		this.visualizadores = visualizadores;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_pessoa_nome_alternativo")
	public PessoaNomeAlternativo getPessoaNomeAlternativo() {
		return pessoaNomeAlternativo;
	}

	public void setPessoaNomeAlternativo(PessoaNomeAlternativo pessoaNomeAlternativo) {
		this.pessoaNomeAlternativo = pessoaNomeAlternativo;
	}
}
