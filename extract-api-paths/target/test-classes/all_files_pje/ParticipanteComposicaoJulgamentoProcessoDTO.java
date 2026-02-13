package br.jus.pje.nucleo.dto;

import java.io.Serializable;

import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCargo;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.TipoAtuacaoMagistradoEnum;

public class ParticipanteComposicaoJulgamentoProcessoDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private ProcessoTrf processo;
	private OrgaoJulgador orgaoJulgador;
	private OrgaoJulgadorCargo cargoVinculacao;
	private OrgaoJulgadorCargo cargoPrincipalDistribuicao;
	private PessoaMagistrado magistrado;
	private TipoAtuacaoMagistradoEnum tipoAtuacaoMagistrado;	
	private Integer ordemDeVotacao;
	
	public ProcessoTrf getProcesso() {
		return processo;
	}
	public void setProcesso(ProcessoTrf processo) {
		this.processo = processo;
	}
	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}
	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}
	public OrgaoJulgadorCargo getCargoVinculacao() {
		return cargoVinculacao;
	}
	public void setCargoVinculacao(OrgaoJulgadorCargo cargoVinculacao) {
		this.cargoVinculacao = cargoVinculacao;
	}
	public OrgaoJulgadorCargo getCargoPrincipalDistribuicao() {
		return cargoPrincipalDistribuicao;
	}
	public void setCargoPrincipalDistribuicao(OrgaoJulgadorCargo cargoPrincipalDistribuicao) {
		this.cargoPrincipalDistribuicao = cargoPrincipalDistribuicao;
	}
	public PessoaMagistrado getMagistrado() {
		return magistrado;
	}
	public void setMagistrado(PessoaMagistrado magistrado) {
		this.magistrado = magistrado;
	}
	public TipoAtuacaoMagistradoEnum getTipoAtuacaoMagistrado() {
		return tipoAtuacaoMagistrado;
	}
	public void setTipoAtuacaoMagistrado(TipoAtuacaoMagistradoEnum tipoAtuacaoMagistrado) {
		this.tipoAtuacaoMagistrado = tipoAtuacaoMagistrado;
	}
	public Integer getOrdemDeVotacao() {
		return ordemDeVotacao;
	}
	public void setOrdemDeVotacao(Integer ordemDeVotacao) {
		this.ordemDeVotacao = ordemDeVotacao;
	}
}