package br.jus.csjt.pje.view.action;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.itx.component.AbstractHome;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.csjt.pje.business.service.ObrigacaoPagarService;
import br.jus.csjt.pje.commons.util.MunicipioIBGESuggestBean;
import br.jus.csjt.pje.commons.util.ParametroJtUtil;
import br.jus.pje.jt.entidades.GrupoEdicao;
import br.jus.pje.jt.entidades.ObrigacaoPagar;
import br.jus.pje.jt.entidades.ProcessoJT;
import br.jus.pje.jt.entidades.Rubrica;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(ProcessoJTHome.NAME)
@BypassInterceptors
public class ProcessoJTHome extends AbstractHome<ProcessoJT> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1195303805324267708L;
	public static final String NAME = "processoJTHome";
	public SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
	public static final DecimalFormat df = new DecimalFormat("0.00");

	public static ProcessoJTHome instance() {
		return ComponentUtil.getComponent(NAME);
	}

	public void setProcessoJTIdProcessoJT(Integer id) {
		setId(id);
	}

	public Integer getProcessoJTIdProcessoJT() {
		return (Integer) getId();
	}

	private ProcessoTrfHome getProcessoTrfHome() {
		return ComponentUtil.getComponent("processoTrfHome");
	}

	private MunicipioIBGESuggestBean getMunicipioIBGESuggestBean() {
		return ComponentUtil.getComponent(MunicipioIBGESuggestBean.NAME);
	}
	
	public void limpaProcessoJT(){
		
		setId(null);
		setInstance(null);
		if(MunicipioIBGESuggestBean.instance() != null)
			MunicipioIBGESuggestBean.instance().setEstado(null);
	}

	private ProcessoJT getProcessoJtPorId(int idProcessoJt) {
		ProcessoJT processoJt = null;
		try {
			processoJt = ((ProcessoJT) getEntityManager().createQuery(
					"FROM " + ProcessoJT.class.getSimpleName() + " WHERE idProcessoJt = " + idProcessoJt)
					.getSingleResult());
		} catch (NoResultException e) {
			// pode estar editando um processoTrf que nao possui processoJt
			// associado
		} catch (NonUniqueResultException e) {
			// mais de um processoJt para o processoTrf gerenciado
			throw new AplicationException("Mais de um processoJt para o processoTrf gerenciado.");
		}

		return processoJt;
	}

	@Override
	public ProcessoJT getInstance() {
		if (this.instance == null) {
			ProcessoTrfHome processoTrfHome = getProcessoTrfHome();
			if (processoTrfHome.isManaged()) {
				wire();
			}
		}
		return super.getInstance();
	}

	/**
	 * @author Kelly/Guilherme
	 * @since 1.2.0
	 * @category PJE-JT
	 * @return Carrega o ProcessoJT correspondente ao ProcessoTrf que esta em
	 *         uso.
	 */
	public void wire() {
		ProcessoTrf processoTrf = null;
		ProcessoJT processoJt = null;

		ProcessoTrfHome processoTrfHome = getProcessoTrfHome();
		MunicipioIBGESuggestBean municipioIBGESuggestBean = getMunicipioIBGESuggestBean();

		if (processoTrfHome.isManaged()) {
			processoTrf = processoTrfHome.getInstance();
			processoJt = getProcessoJtPorId(processoTrf.getIdProcessoTrf());

			if (processoJt != null) {
				setProcessoJTIdProcessoJT(processoJt.getIdProcessoJt());
				this.instance = processoJt;
				if (processoJt.getMunicipioIBGE() != null) {
					municipioIBGESuggestBean.setEstado(processoJt.getMunicipioIBGE().getUf());
				}
			}
		}
	}

	@Override
	public String persist() {
		boolean isErro = false;
		FacesMessages.instance().clear();

		if (getInstance().getMunicipioIBGE() == null) {
			isErro = true;
			FacesMessages.instance().addFromResourceBundle(Severity.INFO, "processoJT.erroMunicipioIBGE");
		}

		if (getInstance().getAtividadeEconomica() == null && !ParametroJtUtil.instance().csjt()) {
			isErro = true;
			FacesMessages.instance().addFromResourceBundle(Severity.INFO, "processoJT.erroAtividadeEconomica");
		}

		if (!isErro) {
			String message = "";
			ProcessoTrfHome processoTrfHome = (ProcessoTrfHome) Component.getInstance("processoTrfHome");
			this.instance.setProcessoTrf(processoTrfHome.getInstance());
			// O id do ProcessoJt eh o mesmo do ProcessoTrf vinculado.
			this.instance.setIdProcessoJt(processoTrfHome.getInstance().getIdProcessoTrf());
		  	ProcessoJT processoJT = getInstance();
		  		if (processoJT != null && !EntityUtil.getEntityManager().contains(processoJT)) {
		  		setInstance(EntityUtil.getEntityManager().merge(processoJT));
		  	}
			message = super.persist();
			return message;
		}

		return null;
	}

	@Override
	public String update() {
		boolean isErro = false;
		FacesMessages.instance().clear();

		if (getInstance().getMunicipioIBGE() == null) {
			isErro = true;
			FacesMessages.instance().addFromResourceBundle(Severity.INFO, "processoJT.erroMunicipioIBGE");
		}

		if (getInstance().getAtividadeEconomica() == null&& !ParametroJtUtil.instance().csjt()) {
			isErro = true;
			FacesMessages.instance().addFromResourceBundle(Severity.INFO, "processoJT.erroAtividadeEconomica");
		}

		if (!isErro) {
			String message = super.update();
			return message;
		}

		return null;
	}

	/**
	 * Variavel que retorna o codigo HTML de uma tabela contendo as obrigacoes
	 * de pagar do processo.
	 * 
	 * @author rodrigo
	 * @category PJE-JT
	 * @since versão 1.4.2
	 * 
	 * @return
	 */
	public String getObrigacoesPagarDataTable() {

		if (instance == null) {
			wire();
		}

		StringBuilder html = new StringBuilder();
		String table = "<table class=\"MsoNormalTable\" style=\"margin: auto auto auto 2.75pt; border-collapse: collapse; border: black 1pt solid; mso-table-layout-alt: fixed; "
				+ "mso-padding-alt: 2.75pt 2.75pt 2.75pt 2.75pt;\" cellspacing=\"0\" cellpadding=\"0\"><tbody>";

		String header = "<tr style='text-align: center; font-weight: bold;'><td style=\"border-bottom: black 1pt solid; border-left: black 1pt solid; background-color: transparent; width: 68.75pt; border-top: "
				+ "black 1pt solid; border-right: black 1pt solid; mso-border-top-alt: solid black .25pt; mso-border-left-alt: solid black .25pt; mso-border-bottom-alt: "
				+ "solid black .25pt; padding: 2.75pt;\" width=\"92\" valign=\"top\"><p class=\"Contedodetabela\" style=\"margin: 0cm 0cm 0pt;\"><span "
				+ "style=\"font-family: Times New Roman; font-size: small;\">Devedor</span></p></td>"
				+ "<td style=\"border-bottom: black 1pt solid; border-left: black 1pt solid; background-color: transparent; width: 30.95pt; border-top: "
				+ "black 1pt solid; border-right: black 1pt solid; mso-border-top-alt: solid black .25pt; mso-border-left-alt: solid black .25pt; mso-border-bottom-alt: "
				+ "solid black .25pt; padding: 2.75pt;\" width=\"41\" valign=\"top\"><p class=\"Contedodetabela\" style=\"margin: 0cm 0cm 0pt;\"><span "
				+ "style=\"font-family: Times New Roman; font-size: small;\">BO</span></p></td>"
				+ "<td style=\"border-bottom: black 1pt solid; border-left: black 1pt solid; background-color: transparent; width: 106.75pt; border-top: "
				+ "black 1pt solid; border-right: black 1pt solid; mso-border-top-alt: solid black .25pt; mso-border-left-alt: solid black .25pt; mso-border-bottom-alt: "
				+ "solid black .25pt; padding: 2.75pt;\" width=\"142\" valign=\"top\"><p class=\"Contedodetabela\" style=\"margin: 0cm 0cm 0pt;\"><span "
				+ "style=\"font-family: Times New Roman; font-size: small;\">Credor</span></p></td>"
				+ "<td style=\"border-bottom: black 1pt solid; border-left: black 1pt solid; background-color: transparent; width: 59.75pt; border-top: "
				+ "black 1pt solid; border-right: black 1pt solid; mso-border-top-alt: solid black .25pt; mso-border-left-alt: solid black .25pt; mso-border-bottom-alt: "
				+ "solid black .25pt; padding: 2.75pt;\" width=\"80\" valign=\"top\"><p class=\"Contedodetabela\" style=\"margin: 0cm 0cm 0pt;\"><span "
				+ "style=\"font-family: Times New Roman; font-size: small;\">Atualizado em:</span></p></td>"
				+ "<td style=\"border-bottom: black 1pt solid; border-left: black 1pt solid; background-color: transparent; width: 77.95pt; border-top: "
				+ "black 1pt solid; border-right: black 1pt solid; mso-border-top-alt: solid black .25pt; mso-border-left-alt: solid black .25pt; mso-border-bottom-alt: "
				+ "solid black .25pt; padding: 2.75pt;\" width=\"104\" valign=\"top\"><p class=\"Contedodetabela\" style=\"margin: 0cm 0cm 0pt;\"><span "
				+ "style=\"font-family: Times New Roman; font-size: small;\">Rubrica</span></p></td>"
				+ "<td style=\"border-bottom: black 1pt solid; border-left: black 1pt solid; background-color: transparent; width: 68.85pt; border-top: "
				+ "black 1pt solid; border-right: black 1pt solid; mso-border-top-alt: solid black .25pt; mso-border-left-alt: solid black .25pt; mso-border-bottom-alt:"
				+ " solid black .25pt; padding: 2.75pt;\" width=\"92\" valign=\"top\"><p class=\"Contedodetabela\" style=\"margin: 0cm 0cm 0pt;\"><span "
				+ "style=\"font-family: Times New Roman; font-size: small;\">Valor:</span></p></td>"
				+ "<td style=\"background-color: transparent; width: 69.25pt; mso-border-alt: solid black .25pt; border: black 1pt solid; padding: 2.75pt;\" "
				+ "width=\"92\" valign=\"top\"><p class=\"Contedodetabela\" style=\"margin: 0cm 0cm 0pt;\"><span style=\"font-family: Times New Roman; "
				+ "font-size: small;\">Total</span></p></td></tr>";

		String fechamento = "</tbody></table>";

		String newLine = "<tr>";
		String endLine = "</tr>";

		String newColumn = "<td style=\"border: black 1pt solid; background-color: transparent; padding: 2.75pt;\" valign=\"top\">";
		String newColumnRight = "<td style=\"border: black 1pt solid; background-color: transparent; padding: 2.75pt; text-align: right;\" valign=\"top\">";
		String endColumn = "</td>";

		String cellContentLine = "<p class=\"Contedodetabela\" style=\"margin: 0cm 0cm 0pt;\"><span style=\"font-family: Times New Roman; font-size: small;\">";
		String cellContentLineBO = "<p class=\"Contedodetabela\" style=\"margin: 0cm 0cm 0pt; text-align: center;\"><span style=\"font-family: Times New Roman; font-size: small;\">";
		String cellEndContentLine = "</span></p>";

		ObrigacaoPagarService obrigacaoPagarService = new ObrigacaoPagarService();
		List<GrupoEdicao> grupoEdicaoList = obrigacaoPagarService.obterGrupoEdicao(instance);

		if (grupoEdicaoList.size() > 0) {
			html.append(table);
			html.append(header);

		} else {
			html = new StringBuilder("Não existem obrigações de pagar neste processo");
		}

		for (GrupoEdicao grupoEdicao : grupoEdicaoList) {

			List<ObrigacaoPagar> obrigacaoPagarList = grupoEdicao.getObrigacaoPagarList();

			for (ObrigacaoPagar obrigacaoPagar : obrigacaoPagarList) {

				html.append(newLine);

				/* Devedores */
				String nomesDevedores = obrigacaoPagar.getNomesDevedores();
				html.append(newColumn);
				html.append(cellContentLine);
				html.append(nomesDevedores);
				html.append(cellEndContentLine);
				html.append(endColumn);

				/* BO */
				String boDevedores = obrigacaoPagar.getBODevedores();
				html.append(newColumn);
				html.append(cellContentLineBO);
				html.append(boDevedores);
				html.append(cellEndContentLine);
				html.append(endColumn);

				/* Credores */
				String nomesCredores = obrigacaoPagar.getNomesCredores();
				html.append(newColumn);
				html.append(cellContentLine);
				html.append(nomesCredores);
				html.append(cellEndContentLine);
				html.append(endColumn);

				List<Rubrica> rubricaList = obrigacaoPagar.getRubricaList();

				/* Data atualizacao */
				html.append(newColumn);
				for (Rubrica rubrica : rubricaList) {
					html.append(cellContentLineBO);
					html.append(sdf.format(rubrica.getDataCalculo()));
					html.append(cellEndContentLine);
				}
				html.append(endColumn);

				/* Rubricas */
				html.append(newColumn);
				for (Rubrica rubrica : rubricaList) {
					html.append(cellContentLine);
					html.append(rubrica.getDescricaoCompleta());
					html.append(cellEndContentLine);
				}
				html.append(endColumn);

				/* Valor */
				html.append(newColumnRight);
				for (Rubrica rubrica : rubricaList) {
					html.append(cellContentLine);
					html.append(df.format(rubrica.getValor()));
					html.append(cellEndContentLine);
				}
				html.append(endColumn);

				/* Total */
				html.append(newColumnRight);
				html.append(cellContentLine);
				html.append(df.format(obrigacaoPagar.getTotal()));
				html.append(cellEndContentLine);
				html.append(endColumn);
				html.append(endLine);
			}

		}

		html.append(fechamento);

		return html.toString();
	}
}