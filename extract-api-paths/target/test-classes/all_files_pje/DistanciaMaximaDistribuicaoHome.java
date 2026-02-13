package br.com.infox.cliente.home;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.DistanciaMaximaDistribuicao;

@Name("distanciaMaximaDistribuicaoHome")
@BypassInterceptors
public class DistanciaMaximaDistribuicaoHome extends AbstractHome<DistanciaMaximaDistribuicao> {

	private static final long serialVersionUID = 1L;

	public void setDistanciaMaximaDistribuicaoIdDistanciaMaximaDistribuicao(Integer id) {
		setId(id);
	}

	public Integer getDistanciaMaximaDistribuicaoIdDistanciaMaximaDistribuicao() {
		return (Integer) getId();
	}

	public static DistanciaMaximaDistribuicaoHome instance() {
		return ComponentUtil.getComponent("distanciaMaximaDistribuicaoHome");
	}

	@Override
	public String remove(DistanciaMaximaDistribuicao distanciaMaximaDistribuicao) {
		setInstance(distanciaMaximaDistribuicao);
		String ret = super.remove();
		newInstance();
		refreshGrid("distanciaMaximaDistribuicaoGrid");
		return ret;
	}

	@Override
	public String inactive(DistanciaMaximaDistribuicao dist) {
		setInstance(dist);
		String ret = super.remove();
		refreshGrid("distanciaMaximaDistribuicaoGrid");
		return ret;
	}

	@Override
	protected boolean beforePersistOrUpdate() {

		if (instance != null 
				&& instance.getIntervaloFinal() != null 
				&& instance.getIntervaloFinal().equals(instance.getIntervaloInicial())) {
			FacesMessages.instance().add(Severity.ERROR,
					"O valor final do intervalo não pode ser igual ao valor inicial.");
			return false;
		}
		if (instance.getIntervaloFinal() < instance.getIntervaloInicial()) {
			FacesMessages.instance().add(Severity.ERROR,
					"O valor final do intervalo não pode ser menor que valor o inicial.");
			return false;
		}
		if (instance.getDistanciaMaxima() < instance.getMaiorPesoProcesso()) {
			FacesMessages.instance().add(
					Severity.ERROR,
					"A distância máxima de distribuição não pode ser menor que "
							+ instance.getMaiorPesoProcesso().toString() + ".");
			return false;
		}

		String query = "select o from DistanciaMaximaDistribuicao o "
				+ "where ( :intervaloInicial between vl_intervalo_inicial and vl_intervalo_final "
				+ "or :intervaloFinal between vl_intervalo_inicial and vl_intervalo_final) ";
		// "and o.idDistancia != :idDistancia";

		Query q = getEntityManager().createQuery(query);
		q.setParameter("intervaloInicial", instance.getIntervaloInicial());
		q.setParameter("intervaloFinal", instance.getIntervaloFinal());
		// q.setParameter("idDistancia", instance.getIdDistancia());
		List<DistanciaMaximaDistribuicao> list = q.getResultList();
		if (list.size() > 1 || (list.size() == 1 && !list.get(0).equals(instance))) {
			FacesMessages.instance().add(Severity.ERROR,
					"O intervalo informado já está contido em um ou mais intervalos previamente cadastrados.");
			return false;
		}

		return super.beforePersistOrUpdate();
	}
	/*
	 * @Override protected String afterPersistOrUpdate(String ret) {
	 * if(this.getInstance().getTipoDocumento().getIdentificador() &&
	 * this.getInstance().getAtivo()) { Pessoa pessoa =
	 * (Pessoa)EntityUtil.getEntityManager().find(Pessoa.class,
	 * getInstance().getPessoa().getIdUsuario());
	 * pessoa.setNome(getInstance().getNome());
	 * EntityUtil.getEntityManager().persist(pessoa);
	 * EntityUtil.getEntityManager().flush(); } return
	 * super.afterPersistOrUpdate(ret); }
	 */

}
