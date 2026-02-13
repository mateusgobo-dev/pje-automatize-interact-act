package br.jus.pje.nucleo.dto;

import java.io.Serializable;
import java.text.SimpleDateFormat;

import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.enums.AdiadoVistaEnum;
import br.jus.pje.nucleo.enums.JulgamentoEnum;
import br.jus.pje.nucleo.enums.TipoSituacaoPautaEnum;

public class SessaoJulgamentoDTO implements Serializable {
	private static final long serialVersionUID = 8418077339751737085L;
	
	private static final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
	
	private Integer numero_ordem;
	private String nomeOrgaoJulgador;
	private String nomeMagistrado;
	private String numero_processo;
	private Boolean processo_segredo_justica = false;
	private Sessao sessao;
	private TipoSituacaoPautaEnum situacaoJulgamento;
	private SessaoPautaProcessoTrf sessaoPautaProcessoTrf;
	private AdiadoVistaEnum adiadoVista;
	private boolean retiradoJulgamento;
	private OrgaoJulgador orgaoJulgadorRetiradaJulgamento;
	private OrgaoJulgador orgaoJulgadorVencedor;
	private Boolean preferencia;
	private Boolean maioriaDetectada;
	private Boolean sustentacaoOral;
	private String advogadoSustentacaoOral;
	private String classeJudicial;
	private String nomeParaExibicaoProcesso;
	private String dataAutuacao;
	private String proclamacaoDecisao;
	private String horaUltimaAtualizacaoDadosTela;
	private int quantidadeAnotacoes;
	private String toggleProclamacaoDecisao = "false";
	private JulgamentoEnum julgamentoEnum;
	
	public SessaoJulgamentoDTO() {
	}
	
	public SessaoJulgamentoDTO(SessaoPautaProcessoTrf sppt , Sessao sessao, String nomePartesProcesso, String dataAtualizacaoDados, String nomeMagistrado) {
		this.sessaoPautaProcessoTrf = sppt;
		this.numero_ordem = sppt.getNumeroOrdem();
		this.sessao = sessao;
		this.numero_processo = sppt.getConsultaProcessoTrf().getNumeroProcesso();
		this.processo_segredo_justica = sppt.getConsultaProcessoTrf().getSegredoJustica();
		this.situacaoJulgamento = sppt.getSituacaoJulgamento();
		this.preferencia = sppt.getPreferencia();
		this.maioriaDetectada = sppt.getMaioriaDetectada();
		this.sustentacaoOral = sppt.getSustentacaoOral();
		this.advogadoSustentacaoOral = sppt.getAdvogadoSustentacaoOral();
		this.classeJudicial = sppt.getConsultaProcessoTrf().getClasseJudicial() + " (" + sppt.getConsultaProcessoTrf().getCodigoClasseJudicial() + ')';
		this.nomeParaExibicaoProcesso = nomePartesProcesso;
		this.orgaoJulgadorVencedor = sppt.getOrgaoJulgadorVencedor();
		this.dataAutuacao = formatter.format(sppt.getProcessoTrf().getDataAutuacao());
		this.horaUltimaAtualizacaoDadosTela = dataAtualizacaoDados;
		this.adiadoVista = sppt.getAdiadoVista();
		this.retiradoJulgamento = sppt.getRetiradaJulgamento();
		this.nomeMagistrado = nomeMagistrado;
		
		this.nomeOrgaoJulgador = sppt.getConsultaProcessoTrf().getOrgaoJulgador();
		this.julgamentoEnum = sppt.getJulgamentoEnum();
	}

	public Integer getNumeroOrdem() {
		return numero_ordem;
	}
	
	public String getNomeOrgaosJulgadores() {
		return nomeOrgaoJulgador+" - "+nomeMagistrado;
	}
	
	public String getNumeroProcesso() {
		return numero_processo;
	}
	
	public Boolean isProcessoSegredoJustica() {
		return processo_segredo_justica;
	}
	
	public ProcessoTrf getProcessoTrf() {
		return sessaoPautaProcessoTrf.getProcessoTrf();
	}
	
	public Sessao getSessao() {
		return sessao;
	}
	
	public OrgaoJulgador getOrgaoJulgador() {
		return getProcessoTrf().getOrgaoJulgador();
	}
	
	public int getIdProcessoTrf() {
		return sessaoPautaProcessoTrf.getConsultaProcessoTrf().getIdProcessoTrf();
	}
	
	public int getIdSessaoPautaProcessoTrf() {
		return sessaoPautaProcessoTrf.getIdSessaoPautaProcessoTrf();
	}
	
	public int getIdSessao() {
		return sessao.getIdSessao();
	}

	public TipoSituacaoPautaEnum getSituacaoJulgamento() {
		return situacaoJulgamento;
	}

	public void setSituacaoJulgamento(TipoSituacaoPautaEnum situacaoJulgamento) {
		this.situacaoJulgamento = situacaoJulgamento;
	}
	
	public SessaoPautaProcessoTrf getSessaoPautaProcessoTrf() {
		return sessaoPautaProcessoTrf;
	}
	
	public int getSessaoPautaProcessoTrfId() {
		return sessaoPautaProcessoTrf.getIdSessaoPautaProcessoTrf();
	}
	
	public OrgaoJulgador getOrgaoJulgadorPedidoVista() {
		return sessaoPautaProcessoTrf.getOrgaoJulgadorPedidoVista();
	}
	
	public void setAdiadoVista(AdiadoVistaEnum av) {
		adiadoVista = av;
	}
	
	public AdiadoVistaEnum getAdiadoVista() {
		return adiadoVista;
	}
	
	public void setRetiradoJulgamento(boolean retJulg) {
		retiradoJulgamento = retJulg;
	}
	
	public boolean getRetiradoJulgamento() {
		return retiradoJulgamento;
	}
	
	public void setOrgaoJulgadorRetiradaJulgamento(OrgaoJulgador oj) {
		orgaoJulgadorRetiradaJulgamento = oj;
	}
	
	public void setOrgaoJulgadorVencedorJulgamento(OrgaoJulgador oj) {
		orgaoJulgadorVencedor = oj;
	}
	
	public OrgaoJulgador getOrgaoJulgadorRetiradaJulgamento() {
		return orgaoJulgadorRetiradaJulgamento;
	}
	
	public OrgaoJulgador getOrgaoJulgadorVencedor() {
		return orgaoJulgadorVencedor;
	}
	
	public Boolean getPreferencia() {
		return preferencia;
	}
	
	public void setPreferencia(Boolean pref) {
		preferencia = pref;
	}
	
	public Boolean getMaioriaDetectada() {
		return maioriaDetectada;
	}
	
	public void setMaioriaDetectada(Boolean maioriaDetect) {
		maioriaDetectada = maioriaDetect;
	}
	
	public Boolean getSustentacaoOral() {
		return sustentacaoOral;
	}
	
	public void setSustentacaoOral(Boolean sustentacaoOral) {
		this.sustentacaoOral = sustentacaoOral;
	}
	
	public String getAdvogadoSustentacaoOral() {
		return advogadoSustentacaoOral;
	}
	
	public void setAdvogadoSustentacaoOral(String adv) {
		advogadoSustentacaoOral = adv;
	}
	
	public String getClasseJudicial() {
		return classeJudicial;
	}
	
	public String getNomeParaExibicaoProcesso() {
		return nomeParaExibicaoProcesso;
	}
	
	public String getDataAutuacao() {
		return dataAutuacao;
	}
	
	public String getProclamacaoDecisao() {
		return proclamacaoDecisao;
	}
	
	public void setProclamacaoDecisao(String procDec) {
		proclamacaoDecisao = procDec;
	}
	
	public boolean getJulgamentoCelere() {
		return (maioriaDetectada != null && maioriaDetectada);
	}

	public String getHoraUltimaAtualizacaoDadosTela() {
		return horaUltimaAtualizacaoDadosTela;
	}

	public void setHoraUltimaAtualizacaoDadosTela(String horaUltimaAtualizacaoDadosTela) {
		this.horaUltimaAtualizacaoDadosTela = horaUltimaAtualizacaoDadosTela;
	}

	/**
	 * @return the quantidadeAnotacoes
	 */
	public int getQuantidadeAnotacoes() {
		return quantidadeAnotacoes;
	}

	/**
	 * @param quantidadeAnotacoes the quantidadeAnotacoes to set
	 */
	public void setQuantidadeAnotacoes(int quantidadeAnotacoes) {
		this.quantidadeAnotacoes = quantidadeAnotacoes;
	}

	public String getToggleProclamacaoDecisao() {
		return toggleProclamacaoDecisao;
	}

	public void setToggleProclamacaoDecisao(String toggleProclamacaoDecisao) {
		this.toggleProclamacaoDecisao = toggleProclamacaoDecisao;
	}

	/**
	 * @return the julgamentoEnum
	 */
	public JulgamentoEnum getJulgamentoEnum() {
		return julgamentoEnum;
	}

	/**
	 * @param julgamentoEnum the julgamentoEnum to set
	 */
	public void setJulgamentoEnum(JulgamentoEnum julgamentoEnum) {
		this.julgamentoEnum = julgamentoEnum;
	}

	@Override
	public String toString() {
		return sessaoPautaProcessoTrf.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sessaoPautaProcessoTrf == null) ? 0 : sessaoPautaProcessoTrf.getIdSessaoPautaProcessoTrf().hashCode());
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
		SessaoJulgamentoDTO other = (SessaoJulgamentoDTO) obj;
		if (sessaoPautaProcessoTrf == null) {
			if (other.sessaoPautaProcessoTrf != null)
				return false;
		} else if (!sessaoPautaProcessoTrf.getIdSessaoPautaProcessoTrf().equals(other.sessaoPautaProcessoTrf.getIdSessaoPautaProcessoTrf()))
			return false;
		return true;
	}
}