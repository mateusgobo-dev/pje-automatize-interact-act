package br.com.infox.cliente.home;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.ModificacaoParametroDistribuicao;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCargo;

@Name("modificacaoParametroDistribuicaoHome")
public class ModificacaoParametroDistribuicaoHome extends AbstractHome<ModificacaoParametroDistribuicao> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6670466009237264235L;
	private Jurisdicao jurisdicao;

	public static ModificacaoParametroDistribuicaoHome instance() {
		return ComponentUtil.getComponent("modificacaoParametroDistribuicaoHome");
	}

	public void setIdModificacaoParametroDistribuicao(Integer id) {
		setId(id);
	}

	public Integer getIdModificacaoParametroDistribuicao() {
		return (Integer) getId();
	}

	public Jurisdicao getJurisdicao() {
		return jurisdicao;
	}

	public void setJurisdicao(Jurisdicao jurisdicao) {
		this.jurisdicao = jurisdicao;
	}

	@Override
	public String persist() {
		String sql = "SELECT ojc FROM OrgaoJulgador oj, OrgaoJulgadorCargo ojc" +
				 	 "	WHERE oj = ojc.orgaoJulgador AND oj.jurisdicao = :idJurisdicao";
		Query query = getEntityManager().createQuery(sql);
		query.setParameter("idJurisdicao", getJurisdicao());

		List<OrgaoJulgadorCargo> list = query.getResultList();

		if (list.size() == 0) {
			FacesMessages.instance().add(Severity.ERROR,
					"A jurisdição selecionada não possui orgão julgador vinculado!");
			return null;
		}

		for (int i = 0; i < list.size(); i++) {
			ModificacaoParametroDistribuicao m = new ModificacaoParametroDistribuicao();
			OrgaoJulgadorCargo ocj = list.get(i);
			m.setValorAntigoAcumuladorDistribuicao(ocj.getAcumuladorDistribuicao());

			// Zerar acumuladorda distribuição conforme solicitado na issue
			// PJE-698
			ocj.setAcumuladorDistribuicao(0.0);
			getEntityManager().persist(ocj);
			m.setOrgaoJulgadorCargo(ocj);

			m.setDataInclusao(new Date());
			m.setResponsavel(Authenticator.getPessoaLogada());
			m.setAtoNormativo(getInstance().getAtoNormativo());
			m.setDescricao(getInstance().getDescricao());

			getEntityManager().persist(m);
		}

		getEntityManager().flush();
		
		super.newInstance();
		jurisdicao = null;

		FacesMessages.instance().add(Severity.INFO, "Registro cadastrado com sucesso!");

		return "persist";
	}
}
