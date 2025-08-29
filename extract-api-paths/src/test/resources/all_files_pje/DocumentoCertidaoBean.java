package br.jus.cnj.pje.view;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.entidades.ProcessoAssunto;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;
import br.jus.pje.nucleo.util.DateUtil;

public class DocumentoCertidaoBean implements Serializable {
	
	private static final long serialVersionUID = 7014805176692383870L;
	
	private String numeroProcesso;
	private String orgaoJulgador;
	private String jurisdicao;
	private String classeJudicial;
	private String valorCausa;
	private String medidaUrgencia;
	private String ativos;
	private String passivos;
	private String terceiros;
	private String documentos;
	private String tituloPoloAtivo;
	private String tituloPoloPassivo;
	private Date dataHoraProtocolacao;
	private List<ProcessoAssunto> assuntos;
	private String responsavel;
	
	public void carregarDadosCertidao(ProcessoDocumento processoDocumento) {
		ProcessoTrf processoTrf = processoDocumento.getProcessoTrf();
		this.numeroProcesso = processoTrf.getProcesso().getNumeroProcesso();
		this.orgaoJulgador = processoTrf.getOrgaoJulgador().getOrgaoJulgador();
		this.jurisdicao = processoTrf.getJurisdicao().getJurisdicao();
		this.classeJudicial = processoTrf.getClasseJudicial().getClasseJudicial();
		this.assuntos = processoTrf.getProcessoAssuntoList();
		this.valorCausa = processoTrf.getVlCausa();
		
		this.tituloPoloAtivo = definirTituloPolo(processoTrf.getListaPartePrincipalAtivo());
		this.tituloPoloPassivo = definirTituloPolo(processoTrf.getListaPartePrincipalPassivo());
		
		this.dataHoraProtocolacao = processoDocumento.getDataJuntada() != null ? processoDocumento.getDataJuntada() : processoDocumento.getDataInclusao();
		this.responsavel = processoDocumento.getNomeUsuarioJuntada();
		carregarDadosPartes(processoTrf.getProcessoParteList());
		carregarDadosDocumentos(processoDocumento);
		carregarDadosMedidaUrgencia(processoTrf);
	}

	private String definirTituloPolo(List<ProcessoParte> listaProcessoParte) {
		String tituloPolo = "";
		if (CollectionUtilsPje.isNotEmpty(listaProcessoParte) && listaProcessoParte.get(0) != null) {
			tituloPolo = listaProcessoParte.get(0).getTipoParte().getTipoParte(); 
		}
		return tituloPolo;
	}
	
	private void carregarDadosPartes(List<ProcessoParte> partes){
		this.ativos = new String();
		this.passivos = new String();
		this.terceiros = new String();
		
		for(ProcessoParte parte : partes){			
			if(parte.getInSituacao().equals(ProcessoParteSituacaoEnum.A)){
				StringBuilder texto = new StringBuilder(" - ");
				texto.append(parte.getNomeParte());
				texto.append(" (" + parte.getTipoParte().getTipoParte() + ")");
				texto.append("</br>");
				
				if(parte.getInParticipacao().equals(ProcessoParteParticipacaoEnum.A)){
					this.ativos = this.ativos.concat(texto.toString());
				}
				if(parte.getInParticipacao().equals(ProcessoParteParticipacaoEnum.P)){
					this.passivos = this.passivos.concat(texto.toString());
				}
				if(parte.getInParticipacao().equals(ProcessoParteParticipacaoEnum.T)){
					this.terceiros = this.terceiros.concat(texto.toString());
				}
			}
		}
		if(this.terceiros.isEmpty()){
			this.terceiros = "Não existem outros interessados vinculados.";
		}
	}
	
	private void carregarDadosDocumentos(ProcessoDocumento processoDocumento){
		StringBuilder texto = new StringBuilder();
		texto.append("<table style=\"border:none; width:100%; text-align:left; font-size:x-large; font-family:Arial;\">");
		texto.append("<tr>");
		texto.append("<th>Documento</th>");
		texto.append("<th>Tipo</th>");
		texto.append("<th>Tamanho (KB)</th>");
		texto.append("</tr>");
		texto.append("<tr>");
		texto.append("<td>" + processoDocumento.getProcessoDocumento() + "</td>");
		texto.append("<td>" + processoDocumento.getTipoProcessoDocumento().getTipoProcessoDocumento() + "</td>");
		texto.append("<td>" + obterTamanhoKBytes(processoDocumento.getProcessoDocumentoBin()) + "</td>");
		texto.append("</tr>");
		for(ProcessoDocumento documento : processoDocumento.getDocumentosVinculados()){
			texto.append("<tr>");
			texto.append("<td>" + documento.getProcessoDocumento() + "</p></td>");
			texto.append("<td>" + documento.getTipoProcessoDocumento().getTipoProcessoDocumento() + "</td>");
			texto.append("<td>" + obterTamanhoKBytes(documento.getProcessoDocumentoBin()) + "</td>");
			texto.append("</tr>");
		}
		texto.append("</table>");
		this.documentos = texto.toString();
	}
	
	private String obterTamanhoKBytes(ProcessoDocumentoBin processoDocumentoBin){
		float tamanho;
		if (processoDocumentoBin.isBinario()) {
			tamanho = processoDocumentoBin.getSize();
		} else {
			tamanho = processoDocumentoBin.getModeloDocumento().length(); 
		}
		return String.format("%.2f", tamanho/1024);
	}
	
	private void carregarDadosMedidaUrgencia(ProcessoTrf processoTrf){
		Boolean tutelaLiminar = processoTrf.getTutelaLiminar() == null ? Boolean.FALSE : processoTrf.getTutelaLiminar();
		Boolean apreciadoTutelaLiminar = processoTrf.getApreciadoTutelaLiminar() == null ? Boolean.FALSE : processoTrf.getApreciadoTutelaLiminar();
		this.medidaUrgencia = (tutelaLiminar && !apreciadoTutelaLiminar) ? "Sim" : "Não";
	}

	// GETTERs
	
	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public String getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public String getJurisdicao() {
		return jurisdicao;
	}

	public String getClasseJudicial() {
		return classeJudicial;
	}

	public String getValorCausa() {
		return valorCausa;
	}

	public String getMedidaUrgencia() {
		return medidaUrgencia;
	}
	
	public String getAtivos() {
		return ativos;
	}
	
	public String getPassivos() {
		return passivos;
	}
	
	public String getTerceiros() {
		return terceiros;
	}

	public String getDocumentos() {
		return documentos;
	}

	public String getTituloPoloAtivo() {
		return tituloPoloAtivo;
	}

	public String getTituloPoloPassivo() {
		return tituloPoloPassivo;
	}

	public String getAssuntoPrincipal(){
		for(ProcessoAssunto assunto: this.assuntos){
			if(assunto.getAssuntoPrincipal()){
				return assunto.getAssuntoTrf().getAssuntoCompletoFormatado();
			}
		}
		return null;
	}
	
	public String getAssuntos(){
		StringBuilder texto = new StringBuilder();
		for(ProcessoAssunto assunto : this.assuntos){
			texto.append(" - ");
			texto.append(assunto.getAssuntoTrf().getAssuntoCompletoFormatado());
			texto.append("</br>");
		}
		return texto.toString();
	}
	
	public String usuarioResponsavel(){
		return responsavel;
	}
	
	public String dataHoraProtocolacao(){
		return DateUtil.dateToString(this.dataHoraProtocolacao, "dd/MM/yyyy HH:mm");
	}

}
