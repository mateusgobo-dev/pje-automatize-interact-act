package br.com.infox.bpm.taskPage.remessacnj;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoParte;

public class ManifestacaoProcessualMetaData implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		private Integer idProcessoTrfOrigem;
		private Boolean sigiloso;
		private ClasseJudicial classeJudicial;
		private List<AssuntoTrf> assuntos = new ArrayList<AssuntoTrf>(0);
		private List<ProcessoParte> poloAtivo = new ArrayList<ProcessoParte>(0);
		private List<ProcessoParte> poloPassivo = new ArrayList<ProcessoParte>(0);
		private List<ProcessoDocumento> documentos = new ArrayList<ProcessoDocumento>(0);
	
		
		public Integer getIdProcessoTrfOrigem() {
			return idProcessoTrfOrigem;
		}
		
		public void setIdProcessoTrfOrigem(Integer idProcessoTrfOrigem) {
			this.idProcessoTrfOrigem = idProcessoTrfOrigem;
		}
		
		public Boolean getSigiloso() {
			return sigiloso;
		}
		
		public void setSigiloso(Boolean sigiloso) {
			this.sigiloso = sigiloso;
		}
		
		public ClasseJudicial getClasseJudicial() {
			return classeJudicial;
		}
		
		public void setClasseJudicial(ClasseJudicial classeJudicial) {
			this.classeJudicial = classeJudicial;
		}
		
		public List<AssuntoTrf> getAssuntos() {
			return assuntos;
		}
		
		public void setAssuntos(List<AssuntoTrf> assuntos) {
			this.assuntos = assuntos;
		}
		
		public List<ProcessoParte> getPoloAtivo() {
			return poloAtivo;
		}
		
		public void setPoloAtivo(List<ProcessoParte> poloAtivo) {
			this.poloAtivo = poloAtivo;
		}
		
		public List<ProcessoParte> getPoloPassivo() {
			return poloPassivo;
		}
		
		public void setPoloPassivo(List<ProcessoParte> poloPassivo) {
			this.poloPassivo = poloPassivo;
		}
		
		public List<ProcessoDocumento> getDocumentos() {
			return documentos;
		}
		
		public List<ProcessoParte> getPolos() {
			List<ProcessoParte> polos = new ArrayList<ProcessoParte>(getPoloAtivo());
			polos.addAll(getPoloPassivo());
			return polos;
		}
		
		public void setDocumentos(List<ProcessoDocumento> documentos) {
			this.documentos = documentos;
		}
		
	}