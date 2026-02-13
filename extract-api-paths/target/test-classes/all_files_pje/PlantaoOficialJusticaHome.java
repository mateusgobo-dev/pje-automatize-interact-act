package br.com.infox.cliente.home;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.richfaces.function.RichFunction;

import br.com.infox.cliente.component.suggest.PessoaPlantaoSuggestBean;
import br.com.infox.cliente.component.tree.LocalizacaoPlantaoTreeHandler;
import br.com.infox.component.agenda.Agenda;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.Plantao;

@Name("plantaoOficialJusticaHome")
@BypassInterceptors
public class PlantaoOficialJusticaHome extends AbstractPlantaOficialJusticaHome<Plantao> {

	private static final long serialVersionUID = 1L;
	private Date dataInicio;
	private Date dataFim;
	private boolean diaTodo = true;
	
	@SuppressWarnings("unchecked")
	public void marcarPlantao() {
		Agenda agenda = (Agenda) getComponent("agenda");
		PessoaPlantaoSuggestBean ppsb = (PessoaPlantaoSuggestBean) getComponent("pessoaPlantaoSuggest");
		instance.setPessoa(ppsb.getInstance());

		Calendar dataAtual = Calendar.getInstance();
		dataAtual.set(Calendar.HOUR_OF_DAY, 0);
		dataAtual.set(Calendar.MINUTE, 0);
		dataAtual.set(Calendar.SECOND, 0);
		dataAtual.set(Calendar.MILLISECOND, 0);
		if (agenda.getCurrentDate().before(dataAtual.getTime())) {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(StatusMessage.Severity.ERROR,
					"Dia do plantão não pode ser anterior à data atual.");
			limparHorario();
			return;
		} else {
			instance.setDtPlantao(agenda.getCurrentDate());
		}

		if (instance.getPessoa() == null) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "É necessário selecionar um Ofical de Justiça");
			limparHorario();
			return;
		}
		if (instance.getHoraFinal() != null || instance.getHoraInicial() != null) {

			if ((instance.getHoraFinal() != null && instance.getHoraInicial() == null)
					|| (instance.getHoraFinal() == null && instance.getHoraInicial() != null)) {
				FacesMessages.instance().add(StatusMessage.Severity.ERROR, "É necessário informar os ambos horários");
				limparHorario();
				return;
			}
			if (instance.getHoraInicial().after(instance.getHoraFinal())) {
				FacesMessages.instance().add(StatusMessage.Severity.ERROR,
						"A hora final deve ser maior que a hora inicial.");
				limparHorario();
				return;
			}
			if (instance.getHoraInicial().equals(instance.getHoraFinal())) {
				FacesMessages.instance().add(StatusMessage.Severity.ERROR,
						"A hora final deve ser maior que a hora inicial.");
				limparHorario();
				return;
			}
		}
		StringBuilder sb = new StringBuilder();
		sb.append("select o from Plantao o where ");
		sb.append("o.pessoa.idUsuario = ?1 and o.dtPlantao = ?2");
		Query hql = getEntityManager().createQuery(sb.toString());
		hql.setParameter(1, instance.getPessoa().getIdUsuario());
		hql.setParameter(2, instance.getDtPlantao());
		List<Plantao> plantaoList = hql.getResultList();
		for (Plantao plantao : plantaoList) {
			/*
			 * PJE-JT: Athos: [PJE-550]: Alteracoes feitas pela JT: 2011-10-19
			 * correcao na tela de marcacao de plantao de oficial de justica.
			 */
			if (plantao.getHoraInicial() == null && !diaTodo) {
				getEntityManager().remove(plantao);
				break;

			} else if (plantao.getHoraInicial() != null && diaTodo) {
				getEntityManager().remove(plantao);

				/*
				 * PJE-JT: Fim.
				 */
			} else if (verificaChoqueHorario(plantao) != 0) {
				FacesMessages.instance()
						.add(StatusMessage.Severity.ERROR, "Já existe um plantão marcado nesse horário");
				limparHorario();
				return;
			}
		}
		persist();
		/*
		 * PJE-JT: Athos: [PJE-550]: Alteracoes feitas pela JT: 2011-10-19
		 * correcao na tela de marcacao de plantao de oficial de justica.
		 */
		Localizacao localizacao = getInstance().getLocalizacao();
		Pessoa pessoa = getInstance().getPessoa();
		super.newInstance();
		getInstance().setLocalizacao(localizacao);
		getInstance().setPessoa(pessoa);
		/*
		 * PJE-JT: Fim.
		 */
		limparHorario();
		diaTodo = true;
		agenda.refreshAgenda();
		refreshGrid("plantaoGrid");
	}

	private Long verificaChoqueHorario(Plantao plantao) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from Plantao o where o.pessoa.idUsuario = :usuario ");
		sb.append("and o.dtPlantao = :data and (:inicio between ");
		sb.append("o.horaInicial and o.horaFinal or ");
		sb.append(":fim between o.horaInicial and o.horaFinal or ");
		sb.append("o.horaInicial between :inicio and :fim or ");
		sb.append("o.horaFinal between :inicio and :fim)");
		Query hql = getEntityManager().createQuery(sb.toString());
		hql.setParameter("usuario", instance.getPessoa().getIdUsuario());
		hql.setParameter("data", instance.getDtPlantao());
		hql.setParameter("inicio", instance.getHoraInicial());
		hql.setParameter("fim", instance.getHoraFinal());
		return (Long) hql.getSingleResult();
	}

	/**
	 * [PJEII-850]
	 * Incluída verificação do campo Oficial de justiça a ser limpo. 
	 * @author Fernando Barreira
	 * @category PJE-JT
	 */
	public void limparSuggestPessoa() {
		if ((Boolean) RichFunction.findComponent("localizacaopessoaPlantaoSuggest").getAttributes().get("rendered")) {
			Contexts.removeFromAllContexts("pessoaPlantaoSuggest");
		} else {
			Contexts.removeFromAllContexts("oficialJusticaSuggest");
		}
	}

	private void limparTreeLocalizacao() {
		((LocalizacaoPlantaoTreeHandler) getComponent("localizacaoPlantaoTree")).clearTree();
	}

	@Override
	public void onClickFormTab() {
		newInstance();
		super.onClickFormTab();
	}

	@Override
	public void onClickSearchTab() {
		Contexts.removeFromAllContexts("plantaoSearch");
		super.onClickSearchTab();
		getLockedFields().clear();

		/*
		 * PJE-JT: Athos: [PJE-550]: Alteracoes feitas pela JT: 2011-10-19
		 * correcao na tela de marcacao de plantao de oficial de justica.
		 */
		limparHorario();
		diaTodo = true;
		dataInicio = null;
		dataFim = null;
		/*
		 * PJE-JT: Fim.
		 */
	}

	@Override
	public void newInstance() {
		super.newInstance();
		limparTreeLocalizacao();
		limparSuggestPessoa();
		Contexts.removeFromAllContexts("oficialJusticaSuggest");
	}

	/**
	 * @author reiser
	 * @since 1.2.0
	 * @category PJE-JT
	 * @return Plantão do oficial de justiça.
	 */
	@Override
	protected Plantao loadInstance() {
		Plantao plantao = super.loadInstance();

		if (plantao.getHoraInicial() != null) {
			dataInicio = new Date(plantao.getHoraInicial().getTime());
			dataFim = new Date(plantao.getHoraFinal().getTime());
		}

		diaTodo = plantao.getHoraInicial() != null ? false : true;

		return plantao;
	}

	@Observer("pessoaPlantaoSuggEvent")
	public void setPessoaPlantaoSuggestValue(Pessoa pessoa) {
		instance.setPessoa(pessoa);
		Agenda agenda = (Agenda) getComponent("agenda");
		agenda.setItems(null);
	}
	
	/**
	 * Sobrecarga de {@link #setPessoaPlantaoSuggestValue(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída
	 */
//A Sobrecarga desse método foi comentada devido a ISSUE de número PJEII-9709, pois quando esse método era invocado e recebia uma Pessoa
//Ou uma pessoa Física, lancava uma TypeMismatch
//	@Observer("pessoaPlantaoSuggEvent")
//	public void setPessoaPlantaoSuggestValue(PessoaFisicaEspecializada pessoa){
//		setPessoaPlantaoSuggestValue(pessoa != null ? pessoa.getPessoa() : (Pessoa) null);
//	}

	@Observer("evtSelectLocalizacaoPlantao")
	public void changeLocalizacao(Localizacao localizacao) {
		instance.setPessoa((Pessoa) null);
		((Plantao) getComponent("plantaoSearch")).setPessoa((Pessoa)null);
		limparSuggestPessoa();
	}
	
	public void limparHorario() {
		instance.setHoraFinal(null);
		instance.setHoraInicial(null);
	}

	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	public Date getDataFim() {
		return dataFim;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = dataFim;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public boolean isDiaTodo() {
		return diaTodo;
	}

	public void setDiaTodo(boolean diaTodo) {
		this.diaTodo = diaTodo;
	}

}