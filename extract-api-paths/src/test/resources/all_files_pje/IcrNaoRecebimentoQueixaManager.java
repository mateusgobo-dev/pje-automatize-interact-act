package br.com.infox.cliente.home.icrrefactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name("icrNRQManeger")
@Scope(ScopeType.EVENT)
public class IcrNaoRecebimentoQueixaManager extends InformacaoCriminalRelevanteManager<InformacaoCriminalRelevante> {
	private List<InformacaoCriminalRelevante> listaIcr = new ArrayList<InformacaoCriminalRelevante>();

	@SuppressWarnings("unchecked")
	@Override
	public void prePersist(InformacaoCriminalRelevante entity) throws IcrValidationException {
		super.prePersist(entity);
		// TODO modificar para oferecimento de Queixa e, quando esta icr estiver
		// pronta.
		ProcessoTrf processoTrf = InformacaoCriminalRelevanteHome.getHomeInstance().getProcessoTrf();
		boolean temRecebimentoQueixa = false;
		StringBuilder validaExisteQueixa = new StringBuilder();
		validaExisteQueixa.append("SELECT icr FROM InformacaoCriminalRelevante icr INNER JOIN icr.processoParte pp ");
		validaExisteQueixa.append(" WHERE icr.ativo = true");
		validaExisteQueixa.append(" and (icr.tipo = 'CRQ' or icr.tipo = 'COQ')");
		validaExisteQueixa.append(" and pp.processoTrf = :processoTrf");
		validaExisteQueixa.append(" order by icr.data");
		Query query = getEntityManager().createQuery(validaExisteQueixa.toString());
		query.setParameter("processoTrf", processoTrf);
		listaIcr = query.getResultList();
		for (InformacaoCriminalRelevante icr : listaIcr) {
			if (icr.getTipo().getCodigo() == "CRQ") {
				throw new IcrValidationException(
						"O réu "
								+ processoTrf.getNomeParte()
								+ "não possui "
								+ "INFORMAÇÃO CRIMINAL RELEVANTE de oferecimento de queixa cadastrada, pré-requesito deste cadastro."
								+ " Favor cadastra-la.");
			} else if (icr.getTipo().getCodigo().equals("COQ")) {
				temRecebimentoQueixa = true;
			}
		}
		if (temRecebimentoQueixa == true) {
			throw new IcrValidationException("O réu " + processoTrf.getNomeParte() + "já possui "
					+ "INFORMAÇÃO CRIMINAL RELEVANTE de recebimento de queixa cadastrada. Operação não permitida.");
		}
	}

	@Override
	public Date getDtPublicacao(InformacaoCriminalRelevante entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean possuiDataPublicacao() {
		// TODO Auto-generated method stub
		return false;
	}
}
