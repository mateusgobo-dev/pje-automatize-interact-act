package br.com.infox.cliente.home;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.Messages;
import org.jboss.seam.international.StatusMessage;

import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.TipoProcedimentoOrigem;

@Name("tipoProcedimentoOrigemHome")
@BypassInterceptors
public class TipoProcedimentoOrigemHome extends AbstractTipoProcedimentoOrigemHome<TipoProcedimentoOrigem> {

	private static final long serialVersionUID = 1L;

	/*
	 * *****************************************************************
	 * PERSISTÊNCIA
	 * ******************************************************************
	 */

	@Override
	public String inactive(TipoProcedimentoOrigem tipoProcedimentoOrigem) {
		tipoProcedimentoOrigem.setAtivo(Boolean.FALSE);
		String result = super.inactive(tipoProcedimentoOrigem);
		refreshGrid("tipoProcedimentoOrigemGrid");
		return result;
	}

	@Override
	public String remove(TipoProcedimentoOrigem tipoProcedimentoOrigem) {
		return this.inactive(tipoProcedimentoOrigem);
	}

	@Override
	public String persist() {
		String ret = null;
		try {
			if (validarTipoProcedimentoOrigem()) {
				ret = super.persist();
				refreshGrid("tipoProcedimentoOrigemGrid");
				newInstance();
				clearForm();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return ret;
	}

	@Override
	public String update() {
		String ret = null;
		try {
			if (validarTipoProcedimentoOrigem()) {
				ret = super.update();
				refreshGrid("tipoProcedimentoOrigemGrid");
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return ret;
	}

	/*
	 * *************************************************** VALIDAÇÕES - REGRAS
	 * DE NEGÓCIO ****************************************************
	 */

	public void imprimirMensagem(String mensagem) {
		FacesMessages.instance().add(StatusMessage.Severity.ERROR, mensagem);
	}

	public boolean validarTipoProcedimentoOrigem() {
		if (existeTipoProcedimentoOrigem()) {
			String mensagem = Messages.instance().get("tipoOrigem.existeTipoOrigem");
			imprimirMensagem(mensagem);
			return false;
		} else {
			return true;
		}
	}

	@SuppressWarnings("unchecked")
	public boolean existeTipoProcedimentoOrigem() {
		StringBuilder sb = new StringBuilder();

		if (getInstance().getId() == null) { // PERSIST
			sb.append("SELECT o FROM TipoProcedimentoOrigem o");
		} else if (getInstance().getId() != null) { // UPDATE
			sb.append("SELECT o FROM TipoProcedimentoOrigem o where o.id <>"
					+ getInstance().getId());
		}

		Query q = getEntityManager().createQuery(sb.toString());

		List<TipoProcedimentoOrigem> listaTipoProcedimentoOrigem = q.getResultList();

		String dsTipoProcedimento = getInstance().getDsTipoProcedimento().trim().toUpperCase();

		for (TipoProcedimentoOrigem tipoProcedimentoOrigem : listaTipoProcedimentoOrigem) {
			String auxDsTipoProcedimento = tipoProcedimentoOrigem.getDsTipoProcedimento().trim().toUpperCase();

			if (dsTipoProcedimento.equals(auxDsTipoProcedimento)) {
				return true;
			}
		}

		return false;
	}

	/*
	 * ************************************************************* MÉTODOS DE
	 * NEGÓCIO ****************************************************
	 */

	public static TipoProcedimentoOrigemHome instance() {
		return ComponentUtil.getComponent("tipoProcedimentoOrigemHome");
	}

	@Override
	public void newInstance() {
		refreshGrid("tipoProcedimentoOrigemGrid");
		super.newInstance();
	}

	/*
	 * ************************************************************* GETTER'S
	 * AND SETTER'S ****************************************************
	 */
}
