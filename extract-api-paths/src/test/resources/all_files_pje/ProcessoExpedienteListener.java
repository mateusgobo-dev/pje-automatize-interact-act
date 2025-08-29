package br.jus.cnj.pje.entidades.listeners;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;

/**
 * Classe responsável por monitorar eventos JPA relacionados a criação,
 * atualização, exclusão e recuperação da entidade {@link ProcessoExpediente}.
 */
public class ProcessoExpedienteListener {

	public void prePersist(ProcessoExpediente processoExpediente) {
		if (processoExpediente != null && processoExpediente.getProcessoTrf() != null
				&& processoExpediente.getProcessoTrf().getOrgaoJulgador() != null) {
			configurarOrgaoJulgador(processoExpediente);
		}
	}

	public void configurarOrgaoJulgador(ProcessoExpediente pe) {
		OrgaoJulgador orgaoJulgadorPlantao = ParametroUtil.instance().getOrgaoJulgadorPlantao();
		Integer idLocalizacaoAtual = Authenticator.getIdLocalizacaoAtual();
		if (orgaoJulgadorPlantao != null && idLocalizacaoAtual != null
				&& orgaoJulgadorPlantao.getLocalizacao().getIdLocalizacao() == idLocalizacaoAtual) {
			pe.setOrgaoJulgador(orgaoJulgadorPlantao);
		} else {
			pe.setOrgaoJulgador(pe.getProcessoTrf().getOrgaoJulgador());
		}
	}

}
