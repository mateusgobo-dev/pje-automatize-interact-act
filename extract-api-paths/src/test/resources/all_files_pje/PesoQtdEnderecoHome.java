package br.com.infox.cliente.home;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import br.com.itx.component.AbstractHome;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.PesoQtdEndereco;

@Name(PesoQtdEnderecoHome.NAME)
public class PesoQtdEnderecoHome extends AbstractHome<PesoQtdEndereco> {

	private static final long serialVersionUID = 8381039224241419356L;
	public static final String NAME = "pesoQtdEnderecoHome";

	@Override
	protected boolean beforePersistOrUpdate() {

		// Não poderá haver cadastro de pesos para a mesma quantidade de
		// endereços
		if (verificaQuantidadeEnderecosCadastrada()) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR,
					"Já existe um peso cadastrado para esta quantidade de endereços.");
			return false;
		}
		return true;
	}

	private boolean verificaQuantidadeEnderecosCadastrada() {
		Query query;
		if (getInstance().getIdPesoQtdEndereco() != null) {
			query = EntityUtil.getEntityManager().createQuery(
					"select o from PesoQtdEndereco o " + "where o.nrEndereco = :num and o.idPesoQtdEndereco <> :id ");
			query.setParameter("id", getInstance().getIdPesoQtdEndereco());
		} else {
			query = EntityUtil.getEntityManager().createQuery(
					"select o from PesoQtdEndereco o " + "where o.nrEndereco = :num");
		}
		query.setParameter("num", getInstance().getNrEndereco());
		Integer result = query.getResultList().size();
		System.out.println(result);
		if (result > 0) {
			return true;
		} else {
			return false;
		}
	}

}
