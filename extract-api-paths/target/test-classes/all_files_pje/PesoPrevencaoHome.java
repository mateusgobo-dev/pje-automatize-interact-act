package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.DistanciaMaximaDistribuicao;
import br.jus.pje.nucleo.entidades.PesoPrevencao;

@Name("pesoPrevencaoHome")
public class PesoPrevencaoHome extends AbstractHome<PesoPrevencao> {

	private static final long serialVersionUID = 1L;

	public void setPesoPrevencaoIdPesoPrevencao(Integer id) {
		setId(id);
	}

	public Integer getPesoPrevencaoIdPesoPrevencao() {
		return (Integer) getId();
	}

	@Override
	public String inactive(PesoPrevencao dist) {
		String ret = super.inactive(dist);
		refreshGrid("pesoPrevencaoGrid");
		return ret;
	}

	@Override
	public void newInstance() {
		super.newInstance();
		getInstance().setAtivo(Boolean.TRUE);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected boolean beforePersistOrUpdate() {

		if (instance.getIntervaloInicial() != null && instance.getIntervaloFinal() != null) {
			if (instance.getIntervaloFinal() < instance.getIntervaloInicial()) {
				FacesMessages.instance().add(Severity.ERROR,
						"O valor final do " + "intervalo não pode ser menor que o valor inicial.");
				if (isManaged()) {
					getEntityManager().refresh(instance);
				}
				return false;
			}
		}

		if (instance.getAtivo()) {
			String query = "select o from PesoPrevencao o where 1=1 ";

			if (instance.getIntervaloInicial() != null && instance.getIntervaloFinal() != null) {
				query += "and ( :intervaloInicial between coalesce(vl_intervalo_inicial,0) and coalesce(vl_intervalo_final,0) "
						+ "or :intervaloFinal between coalesce(vl_intervalo_inicial,0) and coalesce(vl_intervalo_final,0)) ";
			} else if (instance.getIntervaloInicial() != null) {
				query += "and :intervaloInicial between coalesce(vl_intervalo_inicial,0) and coalesce(vl_intervalo_final,0) ";
			} else if (instance.getIntervaloFinal() != null) {
				query += "and :intervaloFinal between coalesce(vl_intervalo_inicial,0) and coalesce(vl_intervalo_final,0) ";
			}

			if (instance.getIdPesoPrevencao() != null) {
				query += "and o.idPesoPrevencao <> :idPesoPrevencao ";
			}

			query += "and o.ativo = true ";

			Query q = getEntityManager().createQuery(query);

			if (instance.getIntervaloInicial() != null) {
				q.setParameter("intervaloInicial", instance.getIntervaloInicial());
			}

			if (instance.getIntervaloFinal() != null) {
				q.setParameter("intervaloFinal", instance.getIntervaloFinal());
			}

			if (instance.getIdPesoPrevencao() != null) {
				q.setParameter("idPesoPrevencao", instance.getIdPesoPrevencao());
			}

			List<DistanciaMaximaDistribuicao> list = q.getResultList();
			if (list.size() > 0) {
				FacesMessages.instance().add(Severity.ERROR,
						"O intervalo informado já está " + "contido em um ou mais intervalos previamente cadastrados.");
				if (isManaged()) {
					getEntityManager().refresh(instance);
				}
				return false;

			}
		}

		return true;
	}

	public List<PesoPrevencao.TipoIntervalo> getTipoIntervaloItens() {
		List<PesoPrevencao.TipoIntervalo> returnList = new ArrayList<PesoPrevencao.TipoIntervalo>();
		for (PesoPrevencao.TipoIntervalo tipoIntervalo : PesoPrevencao.TipoIntervalo.values()) {
			returnList.add(tipoIntervalo);
		}
		return returnList;
	}
}
