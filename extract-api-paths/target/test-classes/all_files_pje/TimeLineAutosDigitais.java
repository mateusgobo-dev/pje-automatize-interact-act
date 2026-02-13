package br.jus.je.pje.entity.vo;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import br.jus.pje.nucleo.enums.PJeEnum;
import br.jus.pje.nucleo.enums.TipoOrigemAcaoEnum;
import br.jus.pje.nucleo.util.DateUtil;

public class TimeLineAutosDigitais implements Comparable<TimeLineAutosDigitais> {

	public enum TipoDadoENUM implements PJeEnum{
		
		D("Documento"), M("Movimento");
		
		private String label;
		
		private TipoDadoENUM(String label) {
			this.label = label;
		}

		@Override
		public String getLabel() {
			return label;
		}
		
	}
		
	private TipoDadoENUM tipoDadoEnum;
	private TipoOrigemAcaoEnum tipoOrigem;
	private Date data;
	private String textoApresentacao;
	private int id;
	private List<TimeLineAutosDigitais> documentos;
	private List<TimeLineAutosDigitais> movimentos;
	private String tipoDocumento;
	private boolean ativo;
	private boolean sigiloso;
	private boolean lido;
	private int idProcessoDocumento;
	private Integer idTipoProcessoDocumento;
	
	public TimeLineAutosDigitais() {
	}
	
	public TimeLineAutosDigitais(TipoDadoENUM tipoDado, String textoApresentacao, Date data, int id, TipoOrigemAcaoEnum tipoOrigem, String tipoDocumento, boolean ativo, boolean sigiloso) {
		this(tipoDado, textoApresentacao, data, id, tipoOrigem, tipoDocumento, ativo, sigiloso, new LinkedList<TimeLineAutosDigitais>());
	}
	
	public TimeLineAutosDigitais(TipoDadoENUM tipoDado, String textoApresentacao, Date data, int id,TipoOrigemAcaoEnum tipoOrigem, String tipoDocumento, boolean ativo, boolean sigiloso, List<TimeLineAutosDigitais> documentos) {
		this(tipoDado, textoApresentacao, data, id, tipoOrigem, tipoDocumento, ativo, sigiloso, documentos, new LinkedList<TimeLineAutosDigitais>(), Boolean.FALSE);
	}

	public TimeLineAutosDigitais(TipoDadoENUM tipoDado, String textoApresentacao, Date data, int id,TipoOrigemAcaoEnum tipoOrigem, String tipoDocumento, boolean ativo, boolean sigiloso, List<TimeLineAutosDigitais> documentos, List<TimeLineAutosDigitais> movimentos, boolean lido) {
		setTipoDadoEnum(tipoDado);
		setTextoApresentacao(textoApresentacao);
		setData(data);
		setId(id);
		setTipoOrigem(tipoOrigem);
		setTipoDocumento(tipoDocumento);
		setAtivo(ativo);
		setSigiloso(sigiloso);
		setDocumentos(documentos);
		setMovimentos(movimentos);
		setLido(lido);
	}

	public TipoDadoENUM getTipoDadoEnum() {
		return tipoDadoEnum;
	}
	public void setTipoDadoEnum(TipoDadoENUM tipoDadoEnum) {
		this.tipoDadoEnum = tipoDadoEnum;
	}
	public Date getData() {
		return data;
	}
	public void setData(Date data) {
		this.data = data;
	}
	public String getTextoApresentacao() {
		return textoApresentacao;
	}
	public void setTextoApresentacao(String textoApresentacao) {
		this.textoApresentacao = textoApresentacao;
	}
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public TipoOrigemAcaoEnum getTipoOrigem() {
		return tipoOrigem;
	}
	
	public void setTipoOrigem (TipoOrigemAcaoEnum tipoOrigem) {
		this.tipoOrigem = tipoOrigem;
	}
	
	public List<TimeLineAutosDigitais> getDocumentos() {
		return documentos;
	}
	
	public void setDocumentos(List<TimeLineAutosDigitais> documentos) {
		this.documentos = documentos;
	}
	
	public List<TimeLineAutosDigitais> getMovimentos() {
		return movimentos;
	}

	public void setMovimentos(List<TimeLineAutosDigitais> movimentos) {
		this.movimentos = movimentos;
	}

	public String getTipoDocumento() {
		return tipoDocumento;
	}

	public void setTipoDocumento(String tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}

	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	@Override
	public int compareTo(TimeLineAutosDigitais o) {
		return data.compareTo(o.getData())*-1;
	}

	public boolean isSigiloso() {
		return sigiloso;
	}

	public void setSigiloso(boolean sigiloso) {
		this.sigiloso = sigiloso;
	}
	
	public String getTextoTipoOrigem() {
		String tipo = null;
		switch (tipoOrigem) {
			case PA:
				tipo = "Polo ativo";
				break;
				
			case PP:
				tipo = "Polo passivo";
				break;
				
			case OU:
				tipo = "Outros interessados";
				break;
				
			case I:
				tipo = "Interno";
				break;
				
			case E:
				tipo = "Externo";
				break;
	
			default:
				tipo = "Interno";
				break;
		}
		return tipo;
	}
	
	public String getIconeTipoDocumento() {
		String tipo = null;
		
		if(getTipoDocumento().equalsIgnoreCase("text/html")) {
			tipo = "file-text";
		}

		else if(getTipoDocumento().equalsIgnoreCase("application/pdf")) {
			tipo = "file-pdf";
		}

		else if(StringUtils.containsIgnoreCase(getTipoDocumento(), "audio/")) {
			tipo = "file-audio";
		}

		else if(StringUtils.containsIgnoreCase(getTipoDocumento(), "video/")) {
			tipo = "file-video";
		}

		else if(StringUtils.containsIgnoreCase(getTipoDocumento(), "image/")) {
			tipo = "file-image";
		}
		else {
			tipo = "file";
		}
		
		return tipo;
	}
	
	
	public String getHora() {
		return DateUtil.dateToString(data,"HH:mm");
	}

	public boolean getLido() {
		return lido;
	}

	public void setLido(boolean lido) {
		this.lido = lido;
	}


	public int getIdProcessoDocumento() {
		return idProcessoDocumento;
	}

	public void setIdProcessoDocumento(int idProcessoDocumento) {
		this.idProcessoDocumento = idProcessoDocumento;
	}
	
	public Integer getIdTipoProcessoDocumento() {
		return idTipoProcessoDocumento;
	}
	
	public void setIdTipoProcessoDocumento(Integer idTipoProcessoDocumento) {
		this.idTipoProcessoDocumento = idTipoProcessoDocumento;
	}
}
