package br.jus.pje.nucleo.dto;

import java.io.Serializable;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import br.jus.pje.nucleo.util.StringUtil;

public class ProcessoPushFilaDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer idProcessoPushFila;
	private Integer idProcesso;
	private List<String> listaEmail;
	private List<String> listaMovimentacao;
	private String nrProcesso;
	private Date dtAutuacao;
	private Date dtDistribuicao;
	private String nomeAutor;
	private BigInteger qtAutor;
	private String nomeReu;
	private BigInteger qtReu;
	private String orgaoJulgador;
	private String orgaoJulgadorColegiado = null;
	private String classeJudicial;
	private String assuntoPrincipal;
	private Boolean inSegredoJustica;
	private Date dtNascimentoAutor;
	private Date dtNascimentoReu;

	public Integer getIdProcessoPushFila() {
		return idProcessoPushFila;
	}

	public void setIdProcessoPushFila(Integer idProcessoPushFila) {
		this.idProcessoPushFila = idProcessoPushFila;
	}

	public Integer getIdProcesso() {
		return idProcesso;
	}

	public void setIdProcesso(Integer idProcesso) {
		this.idProcesso = idProcesso;
	}

	public List<String> getListaEmail() {
		return listaEmail;
	}

	public void setListaEmail(String listaEmail) {
		setListaEmail(Arrays.asList(listaEmail.split("\\|")));
	}

	public void setListaEmail(List<String> listaEmail) {
		this.listaEmail = listaEmail;
	}

	public List<String> getListaMovimentacao() {
		return listaMovimentacao;
	}

	public void setListaMovimentacao(String listaMovimentacao) {
		setListaMovimentacao(Arrays.asList(listaMovimentacao.split("\\|")));
	}

	public void setListaMovimentacao(List<String> listaMovimentacao) {

		DateFormat formatarData = new SimpleDateFormat("dd/MM/yyyy HH:mm");

		Comparator<String> cmp = (mov1, mov2) -> {
				Date dataMov1 = null;
				Date dataMov2 = null;
				try {
					dataMov1 = formatarData.parse(mov1);
					dataMov2 = formatarData.parse(mov2);
				} catch (ParseException e) {
					throw new IllegalArgumentException(e);
				}
				  return dataMov1.compareTo(dataMov2);
			};

		Collections.sort(listaMovimentacao,Collections.reverseOrder(cmp));
		this.listaMovimentacao = listaMovimentacao;
	}

	public String getNrProcesso() {
		return nrProcesso;
	}

	public void setNrProcesso(String nrProcesso) {
		this.nrProcesso = nrProcesso;
	}

	public Date getDtAutuacao() {
		return dtAutuacao;
	}

	public void setDtAutuacao(Date dtAutuacao) {
		this.dtAutuacao = dtAutuacao;
	}

	public Date getDtDistribuicao() {
		return dtDistribuicao;
	}

	public void setDtDistribuicao(Date dtDistribuicao) {
		this.dtDistribuicao = dtDistribuicao;
	}

	public String getNomeAutor() {

		if (StringUtil.isSet(this.nomeAutor) && isMenor(this.dtNascimentoAutor)) {
			setNomeAutor(StringUtil.obtemIniciais(this.nomeAutor));
		}
		return nomeAutor;
	}

	public void setNomeAutor(String nomeAutor) {
		this.nomeAutor = nomeAutor;
	}

	public BigInteger getQuantidadeAutores() {
		return qtAutor;
	}

	public void setQuantidadeAutores(BigInteger qtAutor) {
		this.qtAutor = qtAutor;
	}

	public String getNomeReu() {

		if (StringUtil.isSet(this.nomeReu) && isMenor(this.dtNascimentoReu)) {
			setNomeReu(StringUtil.obtemIniciais(nomeReu));
		}
		return nomeReu;
	}

	public void setNomeReu(String nomeReu) {
		this.nomeReu = nomeReu;
	}

	public BigInteger getQuantidadeReus() {
		return qtReu;
	}

	public void setQuantidadeReus(BigInteger qtReu) {
		this.qtReu = qtReu;
	}

	public String getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(String orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public String getOrgaoJulgadorColegiado() {
		return orgaoJulgadorColegiado;
	}

	public void setOrgaoJulgadorColegiado(String orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}

	public String getClasseJudicial() {
		return classeJudicial;
	}

	public void setClasseJudicial(String classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	public String getAssuntoPrincipal() {
		return assuntoPrincipal;
	}

	public void setAssuntoPrincipal(String assuntoPrincipal) {
		this.assuntoPrincipal = assuntoPrincipal;
	}

	public Boolean isSegredoJustica() {
		return inSegredoJustica;
	}

	public void setSegredoJustica(Boolean inSegredoJustica) {
		this.inSegredoJustica = inSegredoJustica;
	}

	public void setDtNascimentoAutor(Date dtNascimento) {
		this.dtNascimentoAutor = dtNascimento;
	}

	public Date getDtNascimentoAutor() {
		return dtNascimentoAutor;
	}

	public void setDtNascimentoReu(Date dtNascimento) {
		this.dtNascimentoReu = dtNascimento;
	}

	public Date getDtNascimentoReu() {
		return dtNascimentoReu;
	}

	private boolean isMenor(Date dtNascimento) {
		boolean isMenor = false;
		if (dtNascimento != null){
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(new Date().getTime() - dtNascimento.getTime());
			int idade = c.get(Calendar.YEAR) - 1970;
			isMenor = idade < 18;
		}
		return isMenor;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (getIdProcesso() == null ? 0 : getIdProcesso().hashCode());
		return result;
	}

	@Override
    public boolean equals(Object item) {
        if (!(item instanceof ProcessoPushFilaDTO)) {
            return false;
        }
        ProcessoPushFilaDTO itemPushFilaDTO = (ProcessoPushFilaDTO)item;
        return itemPushFilaDTO.getIdProcesso().equals(getIdProcesso());
    }	
	
}
