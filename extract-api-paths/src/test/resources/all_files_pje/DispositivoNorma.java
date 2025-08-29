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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

import br.jus.pje.nucleo.anotacoes.ChildList;
import br.jus.pje.nucleo.anotacoes.Parent;
import br.jus.pje.nucleo.enums.UsoDispositivoEnum;

@Entity
@Table(name = "tb_dispositivo_norma")
@org.hibernate.annotations.GenericGenerator(name = "gen_dispositivo_norma", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_dispositivo_norma"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class DispositivoNorma implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<DispositivoNorma,Integer>, Comparable<DispositivoNorma> {

	private static final long serialVersionUID = 1L;

	// Primary key
	private int idDispositivoNorma;

	// Join
	private NormaPenal normaPenal;
	private TipoDispositivoNorma tipoDispositivoNorma;
	private TipoPena tipoPena;
	private MultaPenaPrivativa multaPenaPrivativa;
	private DispositivoNorma dispositivoNormaPai;

	// List DispositivoNormaPai
	private List<DispositivoNorma> dispositivoNormaList = new ArrayList<DispositivoNorma>(0);

	// Columns
	private String dsSimbolo;
	private String dsIdentificador;
	private Date dtInicioVigencia;
	private Date dtFimVigencia;
	private String dsTextoDispositivo;
	private Boolean inPrevisaoPenaRestritiva = false;
	private UsoDispositivoEnum usoDispositivo;
	private AssuntoTrf assuntoTrf;
	private AssuntoTrf assuntoAtoInfracional;

	private Boolean inHediondo = false;
	private Date dtHediondo;
	private Boolean inPrevisaoPenaPrivativa = false;
	private Integer nrPenaMinimaAnos;
	private Integer nrPenaMinimaMeses;
	private Integer nrPenaMinimaDias;
	private Integer nrPenaMaximaAnos;
	private Integer nrPenaMaximaMeses;
	private Integer nrPenaMaximaDias;
	private Integer numeroOrdem;
	private Boolean inPrevisaoPenaMulta = false;
	private Boolean ativo = true;
	private Boolean permitirAssociacaoMultipla = false;

	// CONCATENAR: DS_SIMBOLO, DS_IDENTIFICADOR E DS_TEXTO_DISPOSITIVO

	@SuppressWarnings("unused")
	private String descricaoPeriodoVigencia;

	// Constructor
	public DispositivoNorma() {

	}

	public DispositivoNorma(Integer id) {
		this.idDispositivoNorma = id;
	}

	@Override
	public String toString() {
		return getConcatenadorDispositivo();
	}

	public boolean seuPaiEh(String tipoDispositivo) {
		DispositivoNorma pai = this.getDispositivoNormaPai();
		return pai != null && pai.getTipoDispositivoNorma().getDsTipoDispositivo().equals(tipoDispositivo);
	}

	public boolean naoEh(String... tipoDispositivo) {
		for (String descricao : tipoDispositivo) {
			if (this.getTipoDispositivoNorma().getDsTipoDispositivo().equals(descricao)) {
				return false;
			}
		}
		return true;
	}

	public boolean eh(String... tipoDispositivo) {
		for (String descricao : tipoDispositivo) {
			if (this.getTipoDispositivoNorma().getDsTipoDispositivo().equals(descricao)) {
				return true;
			}
		}
		return false;
	}

	// GETTER'S AND SETTER'S
	@Id
	@GeneratedValue(generator = "gen_dispositivo_norma")
	@Column(name = "id_dispositivo_norma")
	public int getIdDispositivoNorma() {
		return idDispositivoNorma;
	}

	public void setIdDispositivoNorma(int idDispositivoNorma) {
		this.idDispositivoNorma = idDispositivoNorma;
	}

	@Column(name = "ds_simbolo", nullable = true)
	public String getDsSimbolo() {
		return dsSimbolo;
	}

	public void setDsSimbolo(String dsSimbolo) {
		this.dsSimbolo = dsSimbolo;
	}

	@Column(name = "ds_identificador", nullable = false)
	@NotNull
	public String getDsIdentificador() {
		return dsIdentificador;
	}

	public void setDsIdentificador(String dsIdentificador) {
		this.dsIdentificador = dsIdentificador;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_inicio_vigencia", nullable = false)
	@NotNull
	public Date getDtInicioVigencia() {
		return dtInicioVigencia;
	}

	public void setDtInicioVigencia(Date dtInicioVigencia) {
		this.dtInicioVigencia = dtInicioVigencia;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_fim_vigencia", nullable = true)
	public Date getDtFimVigencia() {
		return dtFimVigencia;
	}

	public void setDtFimVigencia(Date dtFimVigencia) {
		this.dtFimVigencia = dtFimVigencia;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_texto_dispositivo", nullable = false)
	@NotNull
	public String getDsTextoDispositivo() {
		return dsTextoDispositivo;
	}

	public void setDsTextoDispositivo(String dsTextoDispositivo) {
		this.dsTextoDispositivo = dsTextoDispositivo;
	}

	@Column(name = "in_previsao_pena_restritiva", nullable = false)
	@NotNull
	public Boolean getInPrevisaoPenaRestritiva() {
		return inPrevisaoPenaRestritiva;
	}

	public void setInPrevisaoPenaRestritiva(Boolean inPrevisaoPenaRestritiva) {
		this.inPrevisaoPenaRestritiva = inPrevisaoPenaRestritiva;
	}

	@Column(name = "in_uso_dispositivo", nullable = true)
	@Enumerated(EnumType.STRING)
	public UsoDispositivoEnum getUsoDispositivo() {
		return usoDispositivo;
	}

	public void setUsoDispositivo(UsoDispositivoEnum usoDispositivo) {
		this.usoDispositivo = usoDispositivo;
	}

	@Column(name = "in_hediondo", nullable = false)
	@NotNull
	public Boolean getInHediondo() {
		return inHediondo;
	}

	public void setInHediondo(Boolean inHediondo) {
		this.inHediondo = inHediondo;
	}

	@Column(name = "in_previsao_pena_privativa", nullable = true)
	public Boolean getInPrevisaoPenaPrivativa() {
		return inPrevisaoPenaPrivativa;
	}

	public void setInPrevisaoPenaPrivativa(Boolean inPrevisaoPenaPrivativa) {
		this.inPrevisaoPenaPrivativa = inPrevisaoPenaPrivativa;
	}

	@Column(name = "in_previsao_pena_multa", nullable = false)
	public Boolean getInPrevisaoPenaMulta() {
		return inPrevisaoPenaMulta;
	}

	public void setInPrevisaoPenaMulta(Boolean inPrevisaoPenaMulta) {
		this.inPrevisaoPenaMulta = inPrevisaoPenaMulta;
	}

	@Column(name = "dt_hediondo", nullable = true)
	public Date getDtHediondo() {
		return dtHediondo;
	}

	public void setDtHediondo(Date dtHediondo) {
		this.dtHediondo = dtHediondo;
	}

	@Column(name = "nr_pena_minima_anos", nullable = true)
	public Integer getNrPenaMinimaAnos() {
		return nrPenaMinimaAnos;
	}

	public void setNrPenaMinimaAnos(Integer nrPenaMinimaAnos) {
		this.nrPenaMinimaAnos = nrPenaMinimaAnos;
	}

	@Column(name = "nr_pena_minima_meses", nullable = true)
	public Integer getNrPenaMinimaMeses() {
		return nrPenaMinimaMeses;
	}

	public void setNrPenaMinimaMeses(Integer nrPenaMinimaMeses) {
		this.nrPenaMinimaMeses = nrPenaMinimaMeses;
	}

	@Column(name = "nr_pena_minima_dias", nullable = true)
	public Integer getNrPenaMinimaDias() {
		return nrPenaMinimaDias;
	}

	public void setNrPenaMinimaDias(Integer nrPenaMinimaDias) {
		this.nrPenaMinimaDias = nrPenaMinimaDias;
	}

	@Column(name = "nr_pena_maxima_anos", nullable = true)
	public Integer getNrPenaMaximaAnos() {
		return nrPenaMaximaAnos;
	}

	public void setNrPenaMaximaAnos(Integer nrPenaMaximaAnos) {
		this.nrPenaMaximaAnos = nrPenaMaximaAnos;
	}

	@Column(name = "nr_pena_maxima_meses", nullable = true)
	public Integer getNrPenaMaximaMeses() {
		return nrPenaMaximaMeses;
	}

	public void setNrPenaMaximaMeses(Integer nrPenaMaximaMeses) {
		this.nrPenaMaximaMeses = nrPenaMaximaMeses;
	}

	@Column(name = "nr_pena_maxima_dias", nullable = true)
	public Integer getNrPenaMaximaDias() {
		return nrPenaMaximaDias;
	}

	public void setNrPenaMaximaDias(Integer nrPenaMaximaDias) {
		this.nrPenaMaximaDias = nrPenaMaximaDias;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Column(name = "in_permite_associacao_multipla", nullable = false)
	@NotNull
	public Boolean getPermitirAssociacaoMultipla() {
		return permitirAssociacaoMultipla;
	}

	public void setPermitirAssociacaoMultipla(Boolean permitirAssociacaoMultipla) {
		this.permitirAssociacaoMultipla = permitirAssociacaoMultipla;
	}

	@ManyToOne
	@JoinColumn(name = "id_tipo_dispositivo", nullable = false)
	public TipoDispositivoNorma getTipoDispositivoNorma() {
		return tipoDispositivoNorma;
	}

	public void setTipoDispositivoNorma(TipoDispositivoNorma tipoDispositivoNorma) {
		this.tipoDispositivoNorma = tipoDispositivoNorma;
	}

	@ManyToOne
	@JoinColumn(name = "id_dispositivo_norma_pai", nullable = true)
	@Parent
	public DispositivoNorma getDispositivoNormaPai() {
		return dispositivoNormaPai;
	}

	public void setDispositivoNormaPai(DispositivoNorma dispositivoNormaPai) {
		this.dispositivoNormaPai = dispositivoNormaPai;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "dispositivoNormaPai")
	@ChildList
	@OrderBy("numeroOrdem")
	public List<DispositivoNorma> getDispositivoNormaList() {
		return dispositivoNormaList;
	}

	public void setDispositivoNormaList(List<DispositivoNorma> dispositivoNormaList) {
		this.dispositivoNormaList = dispositivoNormaList;
	}

	@ManyToOne()
	@JoinColumn(name = "id_norma_penal", nullable = false)
	@NotNull
	public NormaPenal getNormaPenal() {
		return normaPenal;
	}

	public void setNormaPenal(NormaPenal normaPenal) {
		this.normaPenal = normaPenal;
	}

	@ManyToOne
	@JoinColumn(name = "id_tipo_pena", nullable = true)
	public TipoPena getTipoPena() {
		return tipoPena;
	}

	public void setTipoPena(TipoPena tipoPena) {
		this.tipoPena = tipoPena;
	}

	@ManyToOne
	@JoinColumn(name = "id_multa_pena_privativa", nullable = true)
	public MultaPenaPrivativa getMultaPenaPrivativa() {
		return multaPenaPrivativa;
	}

	public void setMultaPenaPrivativa(MultaPenaPrivativa multaPenaPrivativa) {
		this.multaPenaPrivativa = multaPenaPrivativa;
	}

	@ManyToOne
	@JoinColumn(name = "id_assunto_trf", nullable = true)
	public AssuntoTrf getAssuntoTrf() {
		return assuntoTrf;
	}

	public void setAssuntoTrf(AssuntoTrf assuntoTrf) {
		this.assuntoTrf = assuntoTrf;
	}

	@ManyToOne
	@JoinColumn(name = "id_assunto_trf_ato_infracional", nullable = true)
	public AssuntoTrf getAssuntoAtoInfracional() {
		return assuntoAtoInfracional;
	}

	public void setAssuntoAtoInfracional(AssuntoTrf assuntoAtoInfracional) {
		this.assuntoAtoInfracional = assuntoAtoInfracional;
	}

	@NotNull
	@Column(name = "nr_ordem", nullable = false)
	public Integer getNumeroOrdem() {
		return numeroOrdem;
	}

	public void setNumeroOrdem(Integer numeroOrdem) {
		this.numeroOrdem = numeroOrdem;
	}

	@Transient
	public String getConcatenadorDispositivo() {
		String result = "";
		if (getDsSimbolo() != null) {
			result += getDsSimbolo();
		}

		if (getDsIdentificador() != null) {
			result += " " + getDsIdentificador();
			if (getDsSimbolo() != null) {
				if (getDsSimbolo().equals("§") && !getDsIdentificador().toLowerCase().contains("parágrafo único")) {
					result += "º";
				}
			}
		}

		if (getDsTextoDispositivo() != null) {
			result += " - " + getDsTextoDispositivo().replaceAll("\\<.*?>", "");
		}

		return result.trim();
	}

	@Transient
	public String getDescricaoPeriodoVigencia() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		String ini = "";
		String fim = "";
		if (getDtFimVigencia() != null) {
			ini = sdf.format(getDtInicioVigencia());
			fim = sdf.format(getDtFimVigencia());
			return ini + " - " + fim;
		} else {
			ini = sdf.format(getDtInicioVigencia());
			return ini + " - em aberto";
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getAtivo() == null) ? 0 : getAtivo().hashCode());
		result = prime * result + ((getDispositivoNormaList() == null) ? 0 : getDispositivoNormaList().hashCode());
		result = prime * result + ((getDispositivoNormaPai() == null) ? 0 : getDispositivoNormaPai().hashCode());
		result = prime * result + ((getDsIdentificador() == null) ? 0 : getDsIdentificador().hashCode());
		result = prime * result + ((getDsSimbolo() == null) ? 0 : getDsSimbolo().hashCode());
		result = prime * result + ((getDsTextoDispositivo() == null) ? 0 : getDsTextoDispositivo().hashCode());
		result = prime * result + ((getDtFimVigencia() == null) ? 0 : getDtFimVigencia().hashCode());
		result = prime * result + ((getDtHediondo() == null) ? 0 : getDtHediondo().hashCode());
		result = prime * result + ((getDtInicioVigencia() == null) ? 0 : getDtInicioVigencia().hashCode());
		result = prime * result + getIdDispositivoNorma();
		result = prime * result + ((getInHediondo() == null) ? 0 : getInHediondo().hashCode());
		result = prime * result + ((getInPrevisaoPenaMulta() == null) ? 0 : getInPrevisaoPenaMulta().hashCode());
		result = prime * result + ((getInPrevisaoPenaPrivativa() == null) ? 0 : getInPrevisaoPenaPrivativa().hashCode());
		result = prime * result + ((getInPrevisaoPenaRestritiva() == null) ? 0 : getInPrevisaoPenaRestritiva().hashCode());
		result = prime * result + ((getUsoDispositivo() == null) ? 0 : getUsoDispositivo().hashCode());
		result = prime * result + ((getMultaPenaPrivativa() == null) ? 0 : getMultaPenaPrivativa().hashCode());
		result = prime * result + ((getNormaPenal() == null) ? 0 : getNormaPenal().hashCode());
		result = prime * result + ((getNrPenaMaximaAnos() == null) ? 0 : getNrPenaMaximaAnos().hashCode());
		result = prime * result + ((getNrPenaMaximaDias() == null) ? 0 : getNrPenaMaximaDias().hashCode());
		result = prime * result + ((getNrPenaMaximaMeses() == null) ? 0 : getNrPenaMaximaMeses().hashCode());
		result = prime * result + ((getNrPenaMinimaAnos() == null) ? 0 : getNrPenaMinimaAnos().hashCode());
		result = prime * result + ((getNrPenaMinimaDias() == null) ? 0 : getNrPenaMinimaDias().hashCode());
		result = prime * result + ((getNrPenaMinimaMeses() == null) ? 0 : getNrPenaMinimaMeses().hashCode());
		result = prime * result + ((getTipoDispositivoNorma() == null) ? 0 : getTipoDispositivoNorma().hashCode());
		result = prime * result + ((getTipoPena() == null) ? 0 : getTipoPena().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof DispositivoNorma))
			return false;
		DispositivoNorma other = (DispositivoNorma) obj;
		if (getAtivo() == null) {
			if (other.getAtivo() != null)
				return false;
		} else if (!getAtivo().equals(other.getAtivo()))
			return false;
		if (getDispositivoNormaList() == null) {
			if (other.getDispositivoNormaList() != null)
				return false;
		} else if (!getDispositivoNormaList().equals(other.getDispositivoNormaList()))
			return false;
		if (getDispositivoNormaPai() == null) {
			if (other.getDispositivoNormaPai() != null)
				return false;
		} else if (!getDispositivoNormaPai().equals(other.getDispositivoNormaPai()))
			return false;
		if (getDsIdentificador() == null) {
			if (other.getDsIdentificador() != null)
				return false;
		} else if (!getDsIdentificador().equals(other.getDsIdentificador()))
			return false;
		if (getDsSimbolo() == null) {
			if (other.getDsSimbolo() != null)
				return false;
		} else if (!getDsSimbolo().equals(other.getDsSimbolo()))
			return false;
		if (getDsTextoDispositivo() == null) {
			if (other.getDsTextoDispositivo() != null)
				return false;
		} else if (!getDsTextoDispositivo().equals(other.getDsTextoDispositivo()))
			return false;
		if (getDtFimVigencia() == null) {
			if (other.getDtFimVigencia() != null)
				return false;
		} else if (!getDtFimVigencia().equals(other.getDtFimVigencia()))
			return false;
		if (getDtHediondo() == null) {
			if (other.getDtHediondo() != null)
				return false;
		} else if (!getDtHediondo().equals(other.getDtHediondo()))
			return false;
		if (getDtInicioVigencia() == null) {
			if (other.getDtInicioVigencia() != null)
				return false;
		} else if (!getDtInicioVigencia().equals(other.getDtInicioVigencia()))
			return false;
		if (getIdDispositivoNorma() != other.getIdDispositivoNorma())
			return false;
		if (getInHediondo() == null) {
			if (other.getInHediondo() != null)
				return false;
		} else if (!getInHediondo().equals(other.getInHediondo()))
			return false;
		if (getInPrevisaoPenaMulta() == null) {
			if (other.getInPrevisaoPenaMulta() != null)
				return false;
		} else if (!getInPrevisaoPenaMulta().equals(other.getInPrevisaoPenaMulta()))
			return false;
		if (getInPrevisaoPenaPrivativa() == null) {
			if (other.getInPrevisaoPenaPrivativa() != null)
				return false;
		} else if (!getInPrevisaoPenaPrivativa().equals(other.getInPrevisaoPenaPrivativa()))
			return false;
		if (getInPrevisaoPenaRestritiva() == null) {
			if (other.getInPrevisaoPenaRestritiva() != null)
				return false;
		} else if (!getInPrevisaoPenaRestritiva().equals(other.getInPrevisaoPenaRestritiva()))
			return false;
		if (getUsoDispositivo() == null) {
			if (other.getUsoDispositivo() != null)
				return false;
		} else if (!getUsoDispositivo().equals(other.getUsoDispositivo()))
			return false;
		if (getMultaPenaPrivativa() == null) {
			if (other.getMultaPenaPrivativa() != null)
				return false;
		} else if (!getMultaPenaPrivativa().equals(other.getMultaPenaPrivativa()))
			return false;
		if (getNormaPenal() == null) {
			if (other.getNormaPenal() != null)
				return false;
		} else if (!getNormaPenal().equals(other.getNormaPenal()))
			return false;
		if (getNrPenaMaximaAnos() == null) {
			if (other.getNrPenaMaximaAnos() != null)
				return false;
		} else if (!getNrPenaMaximaAnos().equals(other.getNrPenaMaximaAnos()))
			return false;
		if (getNrPenaMaximaDias() == null) {
			if (other.getNrPenaMaximaDias() != null)
				return false;
		} else if (!getNrPenaMaximaDias().equals(other.getNrPenaMaximaDias()))
			return false;
		if (getNrPenaMaximaMeses() == null) {
			if (other.getNrPenaMaximaMeses() != null)
				return false;
		} else if (!getNrPenaMaximaMeses().equals(other.getNrPenaMaximaMeses()))
			return false;
		if (getNrPenaMinimaAnos() == null) {
			if (other.getNrPenaMinimaAnos() != null)
				return false;
		} else if (!getNrPenaMinimaAnos().equals(other.getNrPenaMinimaAnos()))
			return false;
		if (getNrPenaMinimaDias() == null) {
			if (other.getNrPenaMinimaDias() != null)
				return false;
		} else if (!getNrPenaMinimaDias().equals(other.getNrPenaMinimaDias()))
			return false;
		if (getNrPenaMinimaMeses() == null) {
			if (other.getNrPenaMinimaMeses() != null)
				return false;
		} else if (!getNrPenaMinimaMeses().equals(other.getNrPenaMinimaMeses()))
			return false;
		if (getTipoDispositivoNorma() == null) {
			if (other.getTipoDispositivoNorma() != null)
				return false;
		} else if (!getTipoDispositivoNorma().equals(other.getTipoDispositivoNorma()))
			return false;
		if (getTipoPena() == null) {
			if (other.getTipoPena() != null)
				return false;
		} else if (!getTipoPena().equals(other.getTipoPena()))
			return false;
		return true;
	}

	@Transient
	public String getIdLexml() {

		String idLexml = getPrefixo(this);
		DispositivoNorma dispositivoNorma = this;

		while (dispositivoNorma.getDispositivoNormaPai() != null) {
			dispositivoNorma = dispositivoNorma.getDispositivoNormaPai();
			idLexml = getPrefixo(dispositivoNorma) + "_" + idLexml;
		}

		return idLexml;

	}

	@Transient
	private String getPrefixo(DispositivoNorma dispositivoNorma) {

		String dsIdentificador = dispositivoNorma.getDsIdentificador();

		dsIdentificador = dsIdentificador.replaceAll("-", "");

		// se o identificador contem letras, tratar a string
		if (!dsIdentificador.replaceAll("\\D", "").equals(dsIdentificador)) {
			String numero = dsIdentificador.substring(0, dsIdentificador.length() - 1);
			String letra = dsIdentificador.replaceAll(numero, "");

			dsIdentificador = numero + "-" + letra.toUpperCase();
		}
		if (dispositivoNorma.getTipoDispositivoNorma().getDsTipoDispositivo().equals("Artigo")) {
			return "art" + dsIdentificador;
		}
		if (dispositivoNorma.getTipoDispositivoNorma().getDsTipoDispositivo().equals("Parágrafo")) {
			return "par" + dsIdentificador;
		}
		if (dispositivoNorma.getTipoDispositivoNorma().getDsTipoDispositivo().equals("Inciso")) {
			String returnValue = "inc" + dsIdentificador;
			if (dispositivoNorma.getDispositivoNormaPai().getTipoDispositivoNorma().getDsTipoDispositivo()
					.equals("Artigo")) {
				returnValue = "cpt_" + returnValue;
			}
			return returnValue;
		}
		if (dispositivoNorma.getTipoDispositivoNorma().getDsTipoDispositivo().equals("Alínea")) {
			return "ali" + dsIdentificador;
		}
		if (dispositivoNorma.getTipoDispositivoNorma().getDsTipoDispositivo().equals("Item")) {
			return "itm" + dsIdentificador;
		}
		if (dispositivoNorma.getTipoDispositivoNorma().getDsTipoDispositivo().equals("Parte")) {
			return "prt" + dsIdentificador;
		}

		return null;
	}

	@Override
	public int compareTo(DispositivoNorma other) {
		final int BEFORE = -1;
		final int EQUAL = 0;
		final int AFTER = 1;

		if (this.getDispositivoNormaPai() != null && other.getDispositivoNormaPai() != null) {
			if (this.getDispositivoNormaPai().getIdDispositivoNorma() == this.getDispositivoNormaPai()
					.getIdDispositivoNorma()) {
				if (getNumeroOrdem().equals(other.getNumeroOrdem())) {
					return EQUAL;
				} else {
					return this.getNumeroOrdem().compareTo(other.getNumeroOrdem());
				}
			} else if (this.getDispositivoNormaPai().getIdDispositivoNorma() < this.getDispositivoNormaPai()
					.getIdDispositivoNorma()) {
				return BEFORE;
			} else {
				return AFTER;
			}
		} else {
			return this.getNumeroOrdem().compareTo(other.getNumeroOrdem());
		}
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends DispositivoNorma> getEntityClass() {
		return DispositivoNorma.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdDispositivoNorma());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
