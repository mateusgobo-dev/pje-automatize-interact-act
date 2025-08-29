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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.enums.AbrangenciaEnum;

@Entity
@Table(name = CalendarioEvento.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_calendario_evento", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_calendario_evento"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class CalendarioEvento implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<CalendarioEvento,Integer> {

	public static final String TABLE_NAME = "tb_calendario_eventos";
	private static final long serialVersionUID = 1L;

	private int idCalendarioEvento;
	private String dsEvento;
	private String dsAto;
	private Integer dtDia;
	private Integer dtMes;
	private Integer dtAno;
	private Integer dtDiaFinal;
	private Integer dtMesFinal;
	private Integer dtAnoFinal;
	private AbrangenciaEnum inAbrangencia;
	private Boolean ativo;
	private Estado estado;
	private Municipio municipio;
	private OrgaoJulgador orgaoJulgador;
	private Boolean inSuspendeDistribuicao = false;
	private Boolean inJudiciario = false;
	private Boolean inSuspendePrazo = false;
	private Boolean inFeriado = false;
	private Boolean indisponibilidadeSistema = false;
	private Date dataEvento;
	private Date dataEventoFim;
	private Boolean inPrazosRecalculados = false;

	public CalendarioEvento() {}
	
	public CalendarioEvento(Integer diaInicio, Integer mesInicio, Integer anoInicio, Integer diaFim, Integer mesFim, Integer anoFim, boolean inJudiciario, boolean inFeriado, boolean inSuspendePrazo, boolean indisponibilidadeSistema) {  
		this.dtDia = diaInicio;
		this.dtMes = mesInicio;
		this.dtAno = anoInicio;
		this.dtDiaFinal = diaFim;
		this.dtMesFinal = mesFim;
		this.dtAnoFinal = anoFim;
		this.inJudiciario = inJudiciario;
		this.inFeriado = inFeriado;		
		this.inSuspendePrazo = inSuspendePrazo;
		this.indisponibilidadeSistema = indisponibilidadeSistema;
	}

	@Id
	@GeneratedValue(generator = "gen_calendario_evento")
	@Column(name = "id_calendario_evento", unique = true, nullable = false)
	public int getIdCalendarioEvento() {
		return idCalendarioEvento;
	}

	public void setIdCalendarioEvento(int idCalendarioEvento) {
		this.idCalendarioEvento = idCalendarioEvento;
	}

	@Column(name = "ds_evento", nullable = false, length = 60)
	@NotNull
	@Length(max = 60)
	public String getDsEvento() {
		return dsEvento;
	}

	public void setDsEvento(String dsEvento) {
		this.dsEvento = dsEvento;
	}

	@Column(name = "ds_ato", length = 30)
	@Length(max = 30)
	public String getDsAto() {
		return dsAto;
	}

	public void setDsAto(String dsAto) {
		this.dsAto = dsAto;
	}

	@Column(name = "dt_dia", nullable = false)
	@NotNull
	public Integer getDtDia() {
		return dtDia;
	}

	public void setDtDia(Integer dtDia) {
		this.dtDia = dtDia;
	}

	@Column(name = "dt_mes", nullable = false)
	@NotNull
	public Integer getDtMes() {
		return dtMes;
	}

	public void setDtMes(Integer dtMes) {
		this.dtMes = dtMes;
	}

	@Transient
	public String getMesString() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MONTH, dtMes - 1);
		SimpleDateFormat sf = new SimpleDateFormat("MMMMM");
		return sf.format(calendar.getTime());
	}

	@Column(name = "dt_ano")
	public Integer getDtAno() {
		return dtAno;
	}

	public void setDtAno(Integer dtAno) {
		this.dtAno = dtAno;
	}

	@Column(name = "dt_dia_final")
	public Integer getDtDiaFinal() {
		return dtDiaFinal;
	}

	public void setDtDiaFinal(Integer dtDiaFinal) {
		this.dtDiaFinal = dtDiaFinal;
	}

	@Column(name = "dt_mes_final")
	public Integer getDtMesFinal() {
		return dtMesFinal;
	}

	public void setDtMesFinal(Integer dtMesFinal) {
		this.dtMesFinal = dtMesFinal;
	}

	@Column(name = "dt_ano_final")
	public Integer getDtAnoFinal() {
		return dtAnoFinal;
	}

	public void setDtAnoFinal(Integer dtAnoFinal) {
		this.dtAnoFinal = dtAnoFinal;
	}

	
	@Transient
	public String getConcatDateStr() {
		if (getDtMesFinal() != null || getDtDiaFinal() != null) {
			if (getDtAnoFinal() != null) {
				return ("De: " + (getDtDia() + "/" + getDtMes() + "/" + getDtAno()) + " a " + getDtDiaFinal() + "/"
						+ getDtMesFinal() + "/" + getDtAnoFinal());
			} else { //ano final é null
				
				Calendar dataInicial = Calendar.getInstance();
				Calendar dataFim = Calendar.getInstance();

				dataInicial.set(Calendar.DAY_OF_MONTH, getDtDia());
				dataInicial.set(Calendar.MONTH, getDtMes());
				dataInicial.set(Calendar.YEAR, 2000); //um ano qualquer
				
				dataFim.set(Calendar.DAY_OF_MONTH, getDtDiaFinal());
				dataFim.set(Calendar.MONTH, getDtMesFinal());
				dataFim.set(Calendar.YEAR, 2000); //um ano qualquer
				
				boolean utilizarStrAnoSeguinte = (dataFim.compareTo(dataInicial) < 0 );
				
				return ("De: " + (getDtDia() + "/" + getDtMes()
						+ " a " + getDtDiaFinal() + "/" + getDtMesFinal() + (utilizarStrAnoSeguinte ? " do ano seguinte" : "") ));	
			}
		} else {
			if (getDtAno() != null) {
				return (getDtDia() + "/" + getDtMes() + "/" + getDtAno());
			} else {
				return (getDtDia() + "/" + getDtMes() );
			}
		}
	}

	@Column(name = "in_abrangencia", length = 1)
	@Enumerated(EnumType.STRING)
	public AbrangenciaEnum getInAbrangencia() {
		return inAbrangencia;
	}

	public void setInAbrangencia(AbrangenciaEnum inAbrangencia) {
		this.inAbrangencia = inAbrangencia;
	}

	@Column(name = "in_ativo")
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_estado")
	public Estado getEstado() {
		return estado;
	}

	public void setEstado(Estado estado) {
		this.estado = estado;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_municipio")
	public Municipio getMunicipio() {
		return municipio;
	}

	public void setMunicipio(Municipio municipio) {
		this.municipio = municipio;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador")
	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	@Column(name = "in_suspende_distribuicao")
	public Boolean getInSuspendeDistribuicao() {
		return inSuspendeDistribuicao;
	}

	public void setInSuspendeDistribuicao(Boolean inSuspendeDistribuicao) {
		this.inSuspendeDistribuicao = inSuspendeDistribuicao;
	}

	@Column(name = "in_judiciario")
	public Boolean getInJudiciario() {
		return inJudiciario;
	}

	public void setInJudiciario(Boolean inJudiciario) {
		this.inJudiciario = inJudiciario;
	}

	@Column(name = "in_suspende_prazo")
	public Boolean getInSuspendePrazo() {
		return inSuspendePrazo;
	}

	public void setInSuspendePrazo(Boolean inSuspendePrazo) {
		this.inSuspendePrazo = inSuspendePrazo;
	}

	@Column(name = "in_feriado")
	public Boolean getInFeriado() {
		return inFeriado;
	}

	public void setInFeriado(Boolean inFeriado) {
		this.inFeriado = inFeriado;
	}
	
	@Column(name = "in_indisponibilidade_sistema")	
	public Boolean getIndisponibilidadeSistema() {
		return indisponibilidadeSistema;
	}
	
	public void setIndisponibilidadeSistema(Boolean indisponibilidadeSistema) {
		this.indisponibilidadeSistema = indisponibilidadeSistema;
	}
	
	@Column(name = "in_prazos_recalculados")	
	public Boolean getInPrazosRecalculados() {
		return inPrazosRecalculados;
	}
	
	public void setInPrazosRecalculados(Boolean inPrazosRecalculados) {
		this.inPrazosRecalculados = inPrazosRecalculados;
	}

	public void setDataEvento(Date dataEvento) {
		this.dataEvento = dataEvento;
	}

	@Transient
	public Date getDataEvento() {
		return dataEvento;
	}

	public void setDataEventoFim(Date dataEventoFim) {
		this.dataEventoFim = dataEventoFim;
	}

	@Transient
	public Date getDataEventoFim() {
		return dataEventoFim; 
	}

	@Transient
	public boolean estaNesteEvento(Date dataReferencia) {
		
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(dataReferencia);
		
		return estaNesteEvento(calendar);
	}
	
	@Transient
	public boolean estaNesteEvento(Calendar dataReferencia) {
		if(dataReferencia != null) {
			dataReferencia.set(Calendar.MILLISECOND, 0);
		}
		if (this.dtDiaFinal == null) {
			// Data certa
			if (dataReferencia.get(GregorianCalendar.DAY_OF_MONTH) == this.dtDia && dataReferencia.get(GregorianCalendar.MONTH) == this.dtMes - 1
					&& (this.dtAno == null || dataReferencia.get(GregorianCalendar.YEAR) == this.dtAno)) {
				return true;
			}
		} 
		else {
			
			// Intervalo de datas
			GregorianCalendar dataInicial = null;
			GregorianCalendar dataFinal = null;
			
			if (this.dtAno != null && this.dtAnoFinal != null) {
				// datas certas
				dataInicial = new GregorianCalendar(this.dtAno, this.dtMes - 1, this.dtDia, 0, 0, 0);
				dataFinal = new GregorianCalendar(this.dtAnoFinal, this.dtMesFinal - 1, this.dtDiaFinal, 23, 59, 59);
				dataFinal.set(Calendar.MILLISECOND, 999);
			} 
			else {
				// Intervalo periódico
				dataInicial = new GregorianCalendar(dataReferencia.get(GregorianCalendar.YEAR), this.dtMes - 1, this.dtDia, 0, 0, 0);
				dataFinal = new GregorianCalendar(dataReferencia.get(GregorianCalendar.YEAR), this.dtMesFinal - 1, this.dtDiaFinal, 23, 59, 59);
				dataFinal.set(Calendar.MILLISECOND, 999);
				if (dataFinal.compareTo(dataInicial) < 0) {
					if (dataReferencia.get(GregorianCalendar.MONTH) <= (this.dtMesFinal - 1)) {
						dataInicial.add(GregorianCalendar.YEAR, -1);
					} else {
						dataFinal.add(GregorianCalendar.YEAR, 1);
					}
				}
			}
			
			
			if (dataReferencia.compareTo(dataInicial) >= 0 && dataReferencia.compareTo(dataFinal) <= 0) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof CalendarioEvento)) {
			return false;
		}
		CalendarioEvento other = (CalendarioEvento) obj;
		if (getIdCalendarioEvento() != other.getIdCalendarioEvento()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdCalendarioEvento();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends CalendarioEvento> getEntityClass() {
		return CalendarioEvento.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdCalendarioEvento());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}
}
