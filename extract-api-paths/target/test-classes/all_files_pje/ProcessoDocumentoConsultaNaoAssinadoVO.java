package br.jus.cnj.pje.vo;

import java.util.Date;

import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;

public class ProcessoDocumentoConsultaNaoAssinadoVO {

	private String numeroProcesso;
	private String nomeParte;
	private String cpf;
	private String cnpj;
	private TipoProcessoDocumento tipoProcessoDocumento;
	private ClasseJudicial classeJudicial;
	private AssuntoTrf assuntoTrf;
	private boolean cpfSelecionado = true;
	private Date inseridoInicio;
	private Date inseridoFim;
	private boolean documentosCriadosApenasPeloUsuario = false;
	
	public String getNumeroProcesso() {
		return numeroProcesso;
	}
	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}
	public String getNomeParte() {
		return nomeParte;
	}
	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}
	public String getCpf() {
		return cpf;
	}
	public void setCpf(String cpf) {
		this.cpf = cpf;
	}
	public String getCnpj() {
		return cnpj;
	}
	public void setCnpj(String cnpj) {
		this.cnpj = cnpj;
	}
	public TipoProcessoDocumento getTipoProcessoDocumento() {
		return tipoProcessoDocumento;
	}
	public void setTipoProcessoDocumento(TipoProcessoDocumento tipoProcessoDocumento) {
		this.tipoProcessoDocumento = tipoProcessoDocumento;
	}
	public ClasseJudicial getClasseJudicial() {
		return classeJudicial;
	}
	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}
	public AssuntoTrf getAssuntoTrf() {
		return assuntoTrf;
	}
	public void setAssuntoTrf(AssuntoTrf assuntoTrf) {
		this.assuntoTrf = assuntoTrf;
	}
	public boolean isCpfSelecionado() {
		return cpfSelecionado;
	}
	public void setCpfSelecionado(boolean cpfSelecionado) {
		this.cpfSelecionado = cpfSelecionado;
	}
	public Date getInseridoInicio() {
		return inseridoInicio;
	}
	public void setInseridoInicio(Date inseridoInicio) {
		this.inseridoInicio = inseridoInicio;
	}
	public Date getInseridoFim() {
		return inseridoFim;
	}
	public void setInseridoFim(Date inseridoFim) {
		this.inseridoFim = inseridoFim;
	}
	public boolean isDocumentosCriadosApenasPeloUsuario() {
		return documentosCriadosApenasPeloUsuario;
	}
	public void setDocumentosCriadosApenasPeloUsuario(boolean documentosCriadosApenasPeloUsuario) {
		this.documentosCriadosApenasPeloUsuario = documentosCriadosApenasPeloUsuario;
	}

}