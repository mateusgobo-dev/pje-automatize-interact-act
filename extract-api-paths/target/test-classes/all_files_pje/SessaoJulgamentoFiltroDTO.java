package br.jus.pje.nucleo.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.PrioridadeProcesso;
import br.jus.pje.nucleo.entidades.TipoPessoa;
import br.jus.pje.nucleo.entidades.TipoVoto;
import br.jus.pje.nucleo.enums.SessaoResultadoVotacaoEnum;
import br.jus.pje.nucleo.enums.SituacaoProcessoSessaoEnum;
import br.jus.pje.nucleo.enums.TipoInclusaoEnum;
import br.jus.pje.nucleo.util.DateUtil;

/**
 * 
 * @author luiz.mendes
 *
 */
public class SessaoJulgamentoFiltroDTO implements Serializable {
	private static final long serialVersionUID = 8418077339000000001L;
	
	private String numeroProcesso;
	private AssuntoTrf campoAssunto;
	private ClasseJudicial campoClasse;
	private String nomeParte; 
	private String codigoIMF; 
	private String codigoOAB;
	private PrioridadeProcesso prioridade;
	private OrgaoJulgador orgaoFiltro;
	private Date dataInicialDistribuicao;
	private Date dataFinalDistribuicao;
	private TipoVoto tipoVotoRelator;
	private SessaoResultadoVotacaoEnum sessaoResultadoVotacaoEnum;
	private TipoInclusaoEnum tipoInclusaoEnum;
	private SituacaoProcessoSessaoEnum situacaoProcessoSessaoEnum;
	private Boolean possuiProclamacaoAntecipada;
	private TipoPessoa tipoPessoa;

	//CONTRUTOR
	public SessaoJulgamentoFiltroDTO(
			String numeroProcesso, 
			AssuntoTrf campoAssunto, 
			ClasseJudicial campoClasse, 
			String nomeParte, 
			String codigoIMF, 
			String codigoOAB, 
			PrioridadeProcesso prioridade, 
			OrgaoJulgador orgaoFiltro, 
			Date dataInicialDistribuicao, Date dataFinalDistribuicao,
			TipoVoto tipoVotoRelator,
			SessaoResultadoVotacaoEnum sessaoResultadoVotacaoEnum, 
			TipoInclusaoEnum tipoInclusaoEnum,
			SituacaoProcessoSessaoEnum situacaoProcessoSessaoEnum,
			Boolean possuiProclamacaoAntecipada,
			TipoPessoa tipoPessoa) {
		
		this.numeroProcesso = numeroProcesso;
		this.setCampoAssunto(campoAssunto);
		this.setCampoClasse(campoClasse);
		this.setNomeParte(nomeParte);
		this.setCodigoIMF(codigoIMF);
		this.setCodigoOAB(codigoOAB);
		this.setPrioridade(prioridade);
		this.setOrgaoFiltro(orgaoFiltro);
		this.setDataInicialDistribuicao(dataInicialDistribuicao);
		this.setDataFinalDistribuicao(dataFinalDistribuicao);
		this.setTipoVotoRelator(tipoVotoRelator);
		this.setSessaoResultadoVotacaoEnum(sessaoResultadoVotacaoEnum);
		this.setTipoInclusaoEnum(tipoInclusaoEnum);
		this.setSituacaoProcessoSessaoEnum(situacaoProcessoSessaoEnum);
		this.setPossuiProclamacaoAntecipada(possuiProclamacaoAntecipada);
		this.setTipoPessoa(tipoPessoa);
	}
	
	//CONTRUTOR
	public SessaoJulgamentoFiltroDTO(SituacaoProcessoSessaoEnum situacaoProcessoSessaoEnum) {
		this.setSituacaoProcessoSessaoEnum(situacaoProcessoSessaoEnum);
	}

	public SessaoJulgamentoFiltroDTO() {
		super();
	}

	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public AssuntoTrf getCampoAssunto() {
		return campoAssunto;
	}

	public void setCampoAssunto(AssuntoTrf campoAssunto) {
		this.campoAssunto = campoAssunto;
	}

	public ClasseJudicial getCampoClasse() {
		return campoClasse;
	}

	public void setCampoClasse(ClasseJudicial campoClasse) {
		this.campoClasse = campoClasse;
	}

	public PrioridadeProcesso getPrioridade() {
		return prioridade;
	}

	public void setPrioridade(PrioridadeProcesso prioridade) {
		this.prioridade = prioridade;
	}

	public OrgaoJulgador getOrgaoFiltro() {
		return orgaoFiltro;
	}

	public void setOrgaoFiltro(OrgaoJulgador orgaoFiltro) {
		this.orgaoFiltro = orgaoFiltro;
	}

	public Date getDataInicialDistribuicao() {
		return dataInicialDistribuicao;
	}

	public void setDataInicialDistribuicao(Date dataInicialDistribuicao) {		
		this.dataInicialDistribuicao = DateUtil.getBeginningOfDay(dataInicialDistribuicao);
	}

	public Date getDataFinalDistribuicao() {
		return dataFinalDistribuicao;
	}

	public void setDataFinalDistribuicao(Date dataFinalDistribuicao) {
		this.dataFinalDistribuicao = DateUtil.getEndOfDay(dataFinalDistribuicao);
	}

	public String getNomeParte() {
		return nomeParte;
	}

	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}

	public String getCodigoIMF() {
		return codigoIMF;
	}

	public void setCodigoIMF(String codigoIMF) {
		this.codigoIMF = codigoIMF;
	}

	public String getCodigoOAB() {
		return codigoOAB;
	}

	public void setCodigoOAB(String codigoOAB) {
		this.codigoOAB = codigoOAB;
	}

	public TipoVoto getTipoVotoRelator() {
		return tipoVotoRelator;
	}

	public void setTipoVotoRelator(TipoVoto tipoVotoRelator) {
		this.tipoVotoRelator = tipoVotoRelator;
	}

	public SessaoResultadoVotacaoEnum getSessaoResultadoVotacaoEnum() {
		return sessaoResultadoVotacaoEnum;
	}

	public void setSessaoResultadoVotacaoEnum(SessaoResultadoVotacaoEnum sessaoResultadoVotacaoEnum) {
		this.sessaoResultadoVotacaoEnum = sessaoResultadoVotacaoEnum;
	}

	public TipoInclusaoEnum getTipoInclusaoEnum() {
		return tipoInclusaoEnum;
	}

	public void setTipoInclusaoEnum(TipoInclusaoEnum tipoInclusaoEnum) {
		this.tipoInclusaoEnum = tipoInclusaoEnum;
	}

	public SituacaoProcessoSessaoEnum getSituacaoProcessoSessaoEnum() {
		return situacaoProcessoSessaoEnum;
	}

	public void setSituacaoProcessoSessaoEnum(SituacaoProcessoSessaoEnum situacaoProcessoSessaoEnum) {
		this.situacaoProcessoSessaoEnum = situacaoProcessoSessaoEnum;
	}

	public Boolean getPossuiProclamacaoAntecipada() {
		return possuiProclamacaoAntecipada;
	}

	public void setPossuiProclamacaoAntecipada(Boolean possuiProclamacaoAntecipada) {
		this.possuiProclamacaoAntecipada = possuiProclamacaoAntecipada;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 79 * hash + Objects.hashCode(this.numeroProcesso);
		hash = 79 * hash + Objects.hashCode(this.campoAssunto);
		hash = 79 * hash + Objects.hashCode(this.campoClasse);
		hash = 79 * hash + Objects.hashCode(this.nomeParte);
		hash = 79 * hash + Objects.hashCode(this.codigoIMF);
		hash = 79 * hash + Objects.hashCode(this.codigoOAB);
		hash = 79 * hash + Objects.hashCode(this.prioridade);
		hash = 79 * hash + Objects.hashCode(this.orgaoFiltro);
		hash = 79 * hash + Objects.hashCode(this.dataInicialDistribuicao);
		hash = 79 * hash + Objects.hashCode(this.dataFinalDistribuicao);
		hash = 79 * hash + Objects.hashCode(this.tipoVotoRelator);
		hash = 79 * hash + Objects.hashCode(this.sessaoResultadoVotacaoEnum);
		hash = 79 * hash + Objects.hashCode(this.tipoInclusaoEnum);
		hash = 79 * hash + Objects.hashCode(this.situacaoProcessoSessaoEnum);
		hash = 79 * hash + Objects.hashCode(this.possuiProclamacaoAntecipada);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final SessaoJulgamentoFiltroDTO other = (SessaoJulgamentoFiltroDTO) obj;
		if (!Objects.equals(this.numeroProcesso, other.numeroProcesso)) {
			return false;
		}
		if (!Objects.equals(this.nomeParte, other.nomeParte)) {
			return false;
		}
		if (!Objects.equals(this.codigoIMF, other.codigoIMF)) {
			return false;
		}
		if (!Objects.equals(this.codigoOAB, other.codigoOAB)) {
			return false;
		}
		if (!Objects.equals(this.campoAssunto, other.campoAssunto)) {
			return false;
		}
		if (!Objects.equals(this.campoClasse, other.campoClasse)) {
			return false;
		}
		if (!Objects.equals(this.prioridade, other.prioridade)) {
			return false;
		}
		if (!Objects.equals(this.orgaoFiltro, other.orgaoFiltro)) {
			return false;
		}
		if (!Objects.equals(this.dataInicialDistribuicao, other.dataInicialDistribuicao)) {
			return false;
		}
		if (!Objects.equals(this.dataFinalDistribuicao, other.dataFinalDistribuicao)) {
			return false;
		}
		if (!Objects.equals(this.tipoVotoRelator, other.tipoVotoRelator)) {
			return false;
		}
		if (this.sessaoResultadoVotacaoEnum != other.sessaoResultadoVotacaoEnum) {
			return false;
		}
		if (this.tipoInclusaoEnum != other.tipoInclusaoEnum) {
			return false;
		}
		if (this.situacaoProcessoSessaoEnum != other.situacaoProcessoSessaoEnum) {
			return false;
		}
		if (!Objects.equals(this.possuiProclamacaoAntecipada, other.possuiProclamacaoAntecipada)) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return "SessaoJulgamentoFiltroDTO{" + "numeroProcesso=" + numeroProcesso + ", campoAssunto=" + campoAssunto
				+ ", campoClasse=" + campoClasse + ", nomeParte=" + nomeParte + ", codigoIMF=" + codigoIMF
				+ ", codigoOAB=" + codigoOAB + ", prioridade=" + prioridade + ", orgaoFiltro=" + orgaoFiltro
				+ ", dataInicialDistribuicao=" + dataInicialDistribuicao + ", dataFinalDistribuicao="
				+ dataFinalDistribuicao + ", tipoVotoRelator=" + tipoVotoRelator + ", sessaoResultadoVotacaoEnum="
				+ sessaoResultadoVotacaoEnum + ", tipoInclusaoEnum=" + tipoInclusaoEnum
				+ ", situacaoProcessoSessaoEnum=" + situacaoProcessoSessaoEnum + ", possuiProclamacaoAntecipada="
				+ possuiProclamacaoAntecipada + ", tipoPessoa=" + tipoPessoa + '}';
	}

	public TipoPessoa getTipoPessoa() {
		return tipoPessoa;
	}

	public void setTipoPessoa(TipoPessoa tipoPessoa) {
		this.tipoPessoa = tipoPessoa;
	}
		
}