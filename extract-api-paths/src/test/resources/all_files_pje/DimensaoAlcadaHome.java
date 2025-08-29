package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.itx.component.AbstractHome;
import br.com.itx.util.FacesUtil;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.DimensaoAlcada;

@SuppressWarnings("serial")
@Name("dimensaoAlcadaHome")
public class DimensaoAlcadaHome extends AbstractHome<DimensaoAlcada> {

	@In
	private CompetenciaHome competenciaHome;
	
	public static DimensaoAlcadaHome instance(){
		return (DimensaoAlcadaHome) Component.getInstance("dimensaoAlcadaHome");
	}

	@Override
	public DimensaoAlcada getInstance() {
		if (competenciaHome.getInstance().getDimensaoAlcada() != null) {
			setInstance(competenciaHome.getInstance().getDimensaoAlcada());
		}
		return super.getInstance();
	}

	public List<DimensaoAlcada.TipoIntervalo> getTipoIntervaloItens() {
		List<DimensaoAlcada.TipoIntervalo> returnList = new ArrayList<DimensaoAlcada.TipoIntervalo>();
		for (DimensaoAlcada.TipoIntervalo tipoIntervalo : DimensaoAlcada.TipoIntervalo.values()) {
			returnList.add(tipoIntervalo);
		}
		return returnList;
	}

	public List<DimensaoAlcada.TipoCompetencia> getTipoCompetenciaItens() {
		List<DimensaoAlcada.TipoCompetencia> returnList = new ArrayList<DimensaoAlcada.TipoCompetencia>();
		for (DimensaoAlcada.TipoCompetencia tipoCompetencia : DimensaoAlcada.TipoCompetencia.values()) {
			returnList.add(tipoCompetencia);
		}
		return returnList;
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		boolean returnValue = true;
		FacesMessages.instance().clear();
		if (instance.getTipoIntervalo() == null) {
			FacesMessages.instance().add(Severity.ERROR, "Informe o Tipo de Intervalo!");
			returnValue = false;
		}

		if (instance.getTipoCompetencia() == null) {
			FacesMessages.instance().add(Severity.ERROR, "Informe a Competência!");
			returnValue = false;
		}

		/**
		 * Críticas para os intervalos
		 * Trata os diversos tipos de intervalos. Para cada tipo de intervalo um
		 * tipo de crítica é necessária.
		 * Tipo = "Tempo"
		 */
		if (instance.getTipoIntervalo() == DimensaoAlcada.TipoIntervalo.T) {
			if (instance.getIntervaloInicial() == null) {
				FacesMessages.instance().add(Severity.ERROR, "Informe o Intervalo Inicial!");
				returnValue = false;
			}

			if (instance.getIntervaloFinal() == null) {
				FacesMessages.instance().add(Severity.ERROR, "Informe o Intervalo Final!");
				returnValue = false;
			}

			if (instance.getIntervaloInicial() != null && instance.getIntervaloFinal() != null
					&& instance.getIntervaloInicial() > instance.getIntervaloFinal()) {
				FacesMessages.instance().add(Severity.ERROR,
						"O Intervalo Inicial não pode ser maior que o Intervalo Final!");
				returnValue = false;
			}

			/**
			 *  Verificar se está entre 0 e 10
			 */
			if (instance.getIntervaloInicial() != null
					&& instance.getIntervaloFinal() != null
					&& ((instance.getIntervaloInicial() < 0 || instance.getIntervaloInicial() > 10) || (instance
							.getIntervaloFinal() < 0 || instance.getIntervaloFinal() > 10))) {
				FacesMessages.instance().add(
						Severity.ERROR,
						"Nem o Intervalo Inicial, "
								+ "nem o Intervalo Final podem ter valor menor que 0 ou maior que 10!");
				returnValue = false;
			}

		} else if (instance.getTipoIntervalo() == DimensaoAlcada.TipoIntervalo.N) {

			/*
			 * UC006-Valor da Causa - Alçada - FE002 - Valor Final Inválido
			 * http://pje.csjt.jus.br/PJE_JT_DOC/
			 * 01_Concepcao/01_Levantamento_Inicial/04_Valor da
			 * Causa/UC006-Valor da Causa - Alçada.doc
			 * 
			 * 1. Ator informa campo VALOR INICIAL maior que zero (R$ 0,00); 2.
			 * Ator informa campo VALOR FINAL menor que valor inicial; 3.
			 * Sistema apresenta POPUP com mensagem indicando
			 * "Valor final deve ser nulo ou maior que valor inicial indicado.";
			 * 4. Sistema limpa campo VALOR FINAL, retorna ao formulário e
			 * aguarda ação do ator.
			 */

			/**
			 *  No caso do intervalo ser de zero a algum valor
			 */
			if (instance.getIntervaloInicial() == null && instance.getIntervaloFinal() != null) {
				instance.setIntervaloInicial(0.0D);
			}

			/**
			 *  No caso de intervalo ser null e null
			 */
			if ((instance.getIntervaloInicial() == null) && (instance.getIntervaloFinal() == null)) {
				instance.setIntervaloInicial(0.0D);
				instance.setIntervaloFinal(Double.POSITIVE_INFINITY);
			}

			/**
			 *  No caso do intervalo ser de algum valor a nada (null) (intervalo
			 * aberto a direita)
			 * Seta para +infinito somente para validação do intervalo logo
			 * abaixo. Depois define como null novamente o +infinito para
			 * persistir
			 */
			if (instance.getIntervaloInicial() != null && instance.getIntervaloFinal() == null) {
				instance.setIntervaloFinal(Double.POSITIVE_INFINITY);
			}

			if ((instance.getIntervaloInicial() > 0.0)
					&& (instance.getIntervaloFinal() < instance.getIntervaloInicial())) {
				FacesMessages.instance().add(Severity.ERROR,
						"Valor final deve ser nulo ou maior que valor inicial indicado.");
				returnValue = false;
			}

			/**
			 *  Se Intervalo Final for +infinito seta para null antes de
			 *  persistir.
			 */
			if (instance.getIntervaloFinal() == Double.POSITIVE_INFINITY) {
				instance.setIntervaloFinal(null);
			}
		}

		return returnValue;
	}

	@Override
	public String persist() {
		if (competenciaHome.getInstance().getDimensaoAlcada() == null) {
			competenciaHome.getInstance().setDimensaoAlcada(instance);
		}
		return super.persist();
	}

	@Override
	protected String afterPersistOrUpdate(String ret) {
		Competencia competencia = competenciaHome.getInstance();
		competencia.setDimensaoAlcada(getInstance());
		getEntityManager().flush();
		FacesMessages.instance().clear();
		if (ret.equalsIgnoreCase("persisted")){
			FacesMessages.instance().add(Severity.INFO, FacesUtil.getMessage("entity_messages", "competenciaDimensaoAlcada_created"));
		}
		if (ret.equalsIgnoreCase("updated")){
			FacesMessages.instance().add(Severity.INFO, FacesUtil.getMessage("entity_messages", "competenciaDimensaoAlcada_updated"));
		}
			
		return ret;
	}
	
	/*
	 * [PJEII-4950] Rodrigo S. Menezes: Criando método auxiliar para
	 * remoção da dimensão de alçada.
	 */
	/**
	 * Método para exclusão da Dimensão de Alçada
	 * @param DimensaoAlcada 
	 * 			Dimensão de Alçada a ser excluída. 
	 */
	@Override
	public String remove(DimensaoAlcada obj)
	{
		return super.remove(obj);
	}	
}
