package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.Cep;
import br.jus.pje.nucleo.entidades.FaixaCep;

@Name(FaixaCepHome.NAME)
public class FaixaCepHome extends AbstractHome<FaixaCep> {

	private static final long serialVersionUID = 1043049607511765725L;
	public static final String NAME = "faixaCepHome";
	private Cep cepFinal;
	private Cep cepInicial;
	private String cepFinalStr;
	private String cepInicialStr;

	@In
	private BairroHome bairroHome;

	@Override
	protected boolean beforePersistOrUpdate() {

		Long cepInicialFormatado = converterCep(cepInicial);
		Long cepFinalFormatado = converterCep(cepFinal);

		// Verificar CEP Inicial menor que CEP final
		if (cepInicialFormatado > cepFinalFormatado) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR,
					"O CEP Inicial não pode ser maior que o CEP final.");
			return false;
		}

		// Verificar se a faixa de CEP já foi vinculada ao bairro
		String queryString = "Select o.cepInicial from FaixaCep o " + "where o.bairro.idBairro = :bairro and ( "
				+ "( :cepI between o.cepInicial and o.cepFinal) or "
				+ "( :cepF   between o.cepInicial and o.cepFinal) or "
				+ "( :cepI <= o.cepInicial and :cepF >= o.cepFinal) " + ")";
		Query q = getEntityManager().createQuery(queryString);
		q.setParameter("bairro", bairroHome.getInstance().getIdBairro());
		q.setParameter("cepI", cepInicialFormatado);
		q.setParameter("cepF", cepFinalFormatado);
		q.setMaxResults(9);
		Integer result = q.getResultList().size();
		if (result > 0) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR,
					"Esta faixa de CEP (ou parte dela) já foi cadastrada neste Bairro.");
			return false;
		}

		// TODO
		// Verificar faixa de CEP inválida

		getInstance().setCepInicial(cepInicialFormatado);
		getInstance().setCepFinal(cepFinalFormatado);
		getInstance().setBairro(bairroHome.getInstance());

		return true;
	}

	@Override
	public String persist() {
		String retorno = null;
		retorno = super.persist();
		newInstance();
		return retorno;
	}

	/*
	 * @Override public String update() {
	 * System.out.println("************** TESTE - UPDATE *************"); if
	 * (bairroHome.getInstance() != null) {
	 * System.out.println(bairroHome.getInstance().getDsBairro()); } return
	 * super.update(); }
	 */

	@Override
	public void newInstance() {
		cepInicial = null;
		cepFinal = null;
		cepInicialStr = null;
		cepFinalStr = null;
		super.newInstance();
	}

	@Override
	public String remove(FaixaCep obj) {
		bairroHome.getInstance().getFaixasCep().remove(obj);
		String ret = super.remove(obj);
		return ret;
	}

	public Long converterCep(Cep value) {
		if (value != null && !value.getNumeroCep().equals("")) {
			String cep = value.getNumeroCep().replaceAll("\\-", "");
			return Long.parseLong(cep);
		}
		return null;
	}

	public List<Cep> pesquisaCeps(Object valor) {
		String txt = (String) valor;
		if (txt.length() > 3) {
			return pesquisaCep(txt);
		}
		return new ArrayList<Cep>(0);
	}

	@SuppressWarnings("unchecked")
	public List<Cep> pesquisaCep(String txt) {
		String queryString = "SELECT c FROM Cep AS c WHERE c.municipio.idMunicipio = :municipio AND c.numeroCep LIKE :codigo AND c.ativo = true";
		Query q = getEntityManager().createQuery(queryString);
		q.setParameter("codigo", txt + "%");
		q.setParameter("municipio", bairroHome.getInstance().getMunicipio().getIdMunicipio());
		q.setMaxResults(9);
		return q.getResultList();
	}

	/*
	 * public FaixasCep existeIntervaloDeFaixas(List<FaixasCep> listaFaixasCep,
	 * FaixasCep faixasCep) { Iterator<FaixasCep> itr =
	 * listaFaixasCep.iterator();
	 * 
	 * while (itr.hasNext()) { FaixasCep faixaCepAtual = itr.next();
	 * 
	 * if ((faixasCep.getNumFaixaCepInicial() >= faixaCepAtual
	 * .getNumFaixaCepInicial()) && (faixasCep.getNumFaixaCepInicial() <=
	 * faixaCepAtual .getNumFaixaCepFinal())) { return faixaCepAtual; } else if
	 * ((faixasCep.getNumFaixaCepFinal() >= faixaCepAtual
	 * .getNumFaixaCepInicial()) && (faixasCep.getNumFaixaCepFinal() <=
	 * faixaCepAtual .getNumFaixaCepFinal())) { return faixaCepAtual; } } return
	 * null; }
	 */

	public Cep getCepFinal() {
		return cepFinal;
	}

	public void setCepFinal(Cep cepFinal) {
		this.cepFinalStr = converterCep(cepFinal).toString();
		this.cepFinal = cepFinal;
	}

	public Cep getCepInicial() {
		return cepInicial;
	}

	public void setCepInicial(Cep cepInicial) {
		this.cepInicialStr = converterCep(cepInicial).toString();
		this.cepInicial = cepInicial;
	}

	public String getCepFinalStr() {
		return cepFinalStr;
	}

	public void setCepFinalStr(String cepFinalStr) {
		this.cepFinalStr = cepFinalStr;
	}

	public String getCepInicialStr() {
		return cepInicialStr;
	}

	public void setCepInicialStr(String cepInicialStr) {
		this.cepInicialStr = cepInicialStr;
	}

	public static String formataCep(int cep) {
		String cepFormatado = String.valueOf(cep);
		cepFormatado = cepFormatado.substring(0, 5).concat("-").concat(cepFormatado.substring(5));
		return cepFormatado;
	}

}
