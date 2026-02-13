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
package br.jus.pje.jt.entidades.estatistica;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.OrderBy;

import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;

/**
 * @author Sérgio Pacheco / Sérgio Simoes
 * @since 1.4.3
 * @category PJE-JT
 * @class Relatorio
 * @description Classe que representa o relatório de um Quadro do boletim. 
 */
@Entity
@Table(name = Relatorio.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_relatorio_boletim", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_relatorio_boletim"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Relatorio implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Relatorio,Integer> {
 
	public static final String TABLE_NAME = "tb_relatorio_boletim";
	private static final long serialVersionUID = 1L;
	
	private Integer idRelatorio;
	
	private Boolean valido;
	 
	private Usuario gerador;
	 
	private Date dataHoraGeracao;
	 
	private Quadro quadro;
	  
	private List<ValorItem> valoresItem = new ArrayList<ValorItem>(0);
	 
	private OrgaoJulgador orgaoJulgador;
	
	private List<Regiao> regioes = new ArrayList<Regiao>(0);
	 
	@Embedded
	private Cabecalho cabecalho;
	
	@Embedded	 
	private Periodo periodo;
	
	@Id
	@GeneratedValue(generator = "gen_relatorio_boletim")
	@Column(name = "id_relatorio", unique = true, nullable = false)
	public Integer getIdRelatorio() {
		return this.idRelatorio;
	}

	public void setIdRelatorio(Integer idRelatorio) {
		this.idRelatorio = idRelatorio;
	}

	@Column(name = "in_valido", nullable = false)
	public Boolean getValido() {
		return valido;
	}

	public void setValido(Boolean valido) {
		this.valido = valido;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario")
	public Usuario getGerador() {
		return gerador;
	}

	public void setGerador(Usuario gerador) {
		this.gerador = gerador;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_geracao")
	public Date getDataHoraGeracao() {
		return dataHoraGeracao;
	}

	public void setDataHoraGeracao(Date dataHoraGeracao) {
		this.dataHoraGeracao = dataHoraGeracao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_quadro", nullable = false)
	@ForeignKey(name = "fk_tb_relatorio_boletim_tb_quadro_boletim")
	@NotNull
	public Quadro getQuadro() {
		return quadro;
	}

	public void setQuadro(Quadro quadro) {
		this.quadro = quadro;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "relatorio")
	public List<ValorItem> getValoresItem() {
		return valoresItem;
	}

	public void setValoresItem(List<ValorItem> valoresItem) {
		this.valoresItem = valoresItem;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador")
	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}	
	
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "relatorio")
	@OrderBy(clause = "nr_ordem" )
	public List<Regiao> getRegioes() {
		return regioes;
	}

	public void setRegioes(List<Regiao> regioes) {
		this.regioes = regioes;
	}

	public Cabecalho getCabecalho() {
		return cabecalho;
	}

	public void setCabecalho(Cabecalho cabecalho) {
		this.cabecalho = cabecalho;
	}

	public Periodo getPeriodo() {
		return periodo;
	}

	public void setPeriodo(Periodo periodo) {
		this.periodo = periodo;
	}

	@Transient	
	public String getValorItem( ItemElementar itemElementar ) {
		for (ValorItem valor : valoresItem ) {
			if( valor.getItemElementar().equals(itemElementar))
				return valor.getValor();
		}
		return null;
	}
	
	@Transient
	public void setValorItem( ItemElementar itemElementar, String valor ) {
		for (ValorItem valorItem : valoresItem ) {
			if( valorItem.getItemElementar().equals(itemElementar))
				valorItem.setValor(valor);
		}
	}
	
	public void addValorItem( ItemElementar itemElementar, String valor ) {
		ValorItem valorItem = new ValorItem( itemElementar );
		valorItem.setValor(valor);		
		valoresItem.add(valorItem);
	}

	@Transient
	public String getIdTribunal() {
		return getOrgaoJulgador().getJurisdicao().getJurisdicao();
	}
	
	@Transient
	public String getUf() {
		return getOrgaoJulgador().getJurisdicao().getEstado().getCodEstado();
	}

	@Transient
	public Integer getIdMunicipio() {
		Integer idMunicipio = new Integer(getOrgaoJulgador().getJurisdicao().getMunicipioSede().getIdMunicipio()); 
		return idMunicipio;
	}

	@Transient
	public String getMunicipio() {
		return getOrgaoJulgador().getJurisdicao().getMunicipioSede().getMunicipio();
	}

	@Transient
	public String getEnderecoCompletoOrgaoJulgador() {
		Endereco endereco = orgaoJulgador.getLocalizacao().getEnderecoCompleto();
		return endereco == null ? "" : endereco.getEnderecoCompleto();
	}
	
	@Transient
	public Integer getIdVara() {
		return orgaoJulgador.getNumeroVara();
	}

	@Transient
	public String getHtml() {
		return "<b>Favor tratar em tela, não na entidade.</b>";
	}

	@Transient
	public String getJuizTitular() {		
		String juizTitular = "";
		List<String> juizes = new ArrayList<String>(0); 
		for (UsuarioLocalizacao usuarioLocalizacao : this.getOrgaoJulgador().getLocalizacao().getUsuarioLocalizacaoList()) {
			if ( usuarioLocalizacao.getPapel().getIdentificador().equals("magistrado")
					&& usuarioLocalizacao.getPapel().getAtivo() ) {
				juizes.add( usuarioLocalizacao.getUsuario().getNome() );
			} 
		}
		if( juizes.size() > 0) {
			juizTitular = juizes.get(0);
		}
		return juizTitular;
	}
	
	@Transient
	public String getDiretorSecretaria() {
		String diretorSecretaria = "";
		List<String> juizes = new ArrayList<String>(0); 
		for (UsuarioLocalizacao usuarioLocalizacao : this.getOrgaoJulgador().getLocalizacao().getUsuarioLocalizacaoList()) {
			if ( usuarioLocalizacao.getPapel().getIdentificador().equals("dirSecretaria")
					&& usuarioLocalizacao.getPapel().getAtivo() ) {
				juizes.add( usuarioLocalizacao.getUsuario().getNome() );
			} 
		}
		if( juizes.size() > 0) {
			diretorSecretaria = juizes.get(0);
		}
		return diretorSecretaria;
	}

	@Transient
	public List<Regiao> getRegioesDinamicas(RegiaoQuadro regiaoQuadro) {
		List<Regiao> regioesDoQuadro = new ArrayList<Regiao>(0);
		for (Regiao regiao : regioes) {
			if (regiao.getRegiaoQuadro().equals(regiaoQuadro) ) {
				regioesDoQuadro.add(regiao);
			}
		}
		return regioesDoQuadro;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Relatorio> getEntityClass() {
		return Relatorio.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdRelatorio();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
