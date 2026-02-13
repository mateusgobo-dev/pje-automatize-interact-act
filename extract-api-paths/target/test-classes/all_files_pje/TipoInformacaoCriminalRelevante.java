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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_tipo_icr")
public class TipoInformacaoCriminalRelevante implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private String codigo;
	private String descricao;
	private Boolean inAtivo;
	private Boolean exigeTipificacaoDelito;
	
//	private List<InformacaoCriminalRelevante> ICRList = new ArrayList<InformacaoCriminalRelevante>(0);

	// ----------------------------- CONSTRUTOR ---------------------------
	public TipoInformacaoCriminalRelevante() {
	}

	public TipoInformacaoCriminalRelevante(TipoIcrEnum codigo) {
		this.codigo = codigo.name();
	}

	public TipoInformacaoCriminalRelevante(String codigo) {
		this.codigo = codigo;
	}
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof TipoInformacaoCriminalRelevante){
			TipoInformacaoCriminalRelevante tic = (TipoInformacaoCriminalRelevante) obj;
			return this.getCodigo().equals(tic.getCodigo());
		}else if(obj instanceof TipoIcrEnum){
			TipoIcrEnum t = (TipoIcrEnum) obj;
			String codigo = getCodigo();
			if (codigo == null || codigo.equals(""))
				return false;
			if (t.name().equals(codigo))
				return true;
			return false;
		}
		return super.equals(obj);
	}

	// ----------------------------- ID -----------------------------------
	@Id
	@Column(name = "cd_tipo_icr", unique = true, nullable = false)
	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	@Column(name = "ds_tipo_icr", unique = true, nullable = false)
	@NotNull
	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@Override
	public String toString() {
		return this.descricao;
	}

	// ------------------------------ DEMAIS ------------------------------
	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getInAtivo() {
		return inAtivo;
	}

	public void setInAtivo(Boolean inAtivo) {
		this.inAtivo = inAtivo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((codigo == null) ? 0 : codigo.hashCode());
		return result;
	}

	@Column(name = "in_exige_tipificacao_delito", nullable = false)
	@NotNull
	public Boolean getExigeTipificacaoDelito() {
		return exigeTipificacaoDelito;
	}

	public void setExigeTipificacaoDelito(Boolean exigeTipificacaoDelito) {
		this.exigeTipificacaoDelito = exigeTipificacaoDelito;
	}

	@Transient
	public boolean exigeTipificacaoDelito() {
		return getExigeTipificacaoDelito();
	}

	public static enum TipoIcrEnum {
		NULL("Nulo"), AAU("Atribuição de Autoria dos Fatos"), SEN("Sentença"), IND("Indiciamento"), OFD(
				"Oferecimento da Denúncia"), ADD("Aditamento da Denúncia"), ADQ("Aditamento da Queixa"), RCD(
				"Recebimento da Denúncia"), PRI("Prisão"), FUG("Fuga"), SOL("Soltura"), TRR("Transferência do Réu"), SAP(
				"Sentença Absolutória"), CQA("Cadastro de Queixa"), NRQ("Nã Recebimento da Queixa"), SAI(
				"Sentença Absolutória Imprópria"), SAS("Sentença Absolvição Sumária"), SPR("Sentença de Pronúncia "), SEI(
				"Sentença de Impronúncia "), SEP("Sentença de Extinção da Punibilidade"), DAS(
				"Decisão em Instância Superior - Anulação de Sentença"), DAP(
				"Decisão em Instância Superior - Absolvição"), DAI(
				"Decisão em Instância Superior - Absolvição Imprópria"), DEP(
				"Decisão em Instância Superior - Extinção da Punibilidade"), SCS(
				"Decisão em Instância Superior - Sentença Condenatória"), CRQ("Cadastrar Recebimento de Queixa"), TRP(
				"Transação Penal"), ETP("Encerrar Transação Penal"), STP("Suspender Transação Penal"), ESP(
				"Encerrar Suspensão do Processo"), SUS("Suspensao do Processo"), SCO("Sentença Condenatória"), SSP(
				"Suspender Suspensao do Processo"), RSP("Retomar Suspensão do Processo"), DES(
				"Desclassificação do Processo"), NRD("Não Recebimento da Denuncia");
		private String label;

		TipoIcrEnum(String label) {
			this.label = label;
		}

		public String getLabel() {
			return this.label;
		}

		@Override
		public String toString() {
			return this.name();
		}

		public Boolean equals(TipoInformacaoCriminalRelevante classeTipo) {
			if (classeTipo == null || classeTipo.getCodigo() == null || classeTipo.getCodigo().equals(""))
				return false;
			if (classeTipo.getCodigo().equals(name()))
				return true;
			return false;
		}

		public static TipoIcrEnum getTipo(TipoInformacaoCriminalRelevante classeTipo) {
			if (classeTipo == null || classeTipo.getCodigo() == null || classeTipo.getCodigo().equals(""))
				return TipoIcrEnum.NULL;
			for (TipoIcrEnum tipo : values()) {
				if (tipo.name().equals(classeTipo.getCodigo()))
					return tipo;
			}
			return TipoIcrEnum.NULL;
		}
		/*
		 * public static boolean exigeTipificacaoDelito(
		 * TipoInformacaoCriminalRelevante classeTipo) { if
		 * (classeTipo.equals(TipoIcrEnum.IND) ||
		 * classeTipo.equals(TipoIcrEnum.OFD) ||
		 * classeTipo.equals(TipoIcrEnum.RCD) ||
		 * classeTipo.equals(TipoIcrEnum.ADD) ||
		 * classeTipo.equals(TipoIcrEnum.CQA) ||
		 * classeTipo.equals(TipoIcrEnum.SAI) ||
		 * classeTipo.equals(TipoIcrEnum.SEP) ||
		 * classeTipo.equals(TipoIcrEnum.SEI) ||
		 * classeTipo.equals(TipoIcrEnum.SAS) ||
		 * classeTipo.equals(TipoIcrEnum.DAI) ||
		 * classeTipo.equals(TipoIcrEnum.DEP) ||
		 * classeTipo.equals(TipoIcrEnum.NRQ) ||
		 * classeTipo.equals(TipoIcrEnum.AAU) ||
		 * classeTipo.equals(TipoIcrEnum.ADQ) ||
		 * classeTipo.equals(TipoIcrEnum.SCO) ||
		 * classeTipo.equals(TipoIcrEnum.DES)) { return true; } return false; }
		 * 
		 * public static boolean exigeCadastroPena(
		 * TipoInformacaoCriminalRelevante classeTipo) { if
		 * (classeTipo.equals(TipoIcrEnum.SCO)) { return true; } return false; }
		 */
	}
	
//	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "tipoInformacaoCriminalRelevante")
//	public List<InformacaoCriminalRelevante> getICRList() {
//		return this.ICRList;
//	}
//
//	public void setProcessoDocumentoList(List<InformacaoCriminalRelevante> ICRList) {
//		this.ICRList = ICRList;
//	}
}
