package br.com.infox.ibpm.util;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.Log;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.nucleo.dto.BinarioACs;
import br.jus.cnj.pje.pjecommons.model.services.autoridadescertificadoras.Binario;
import br.jus.cnj.pje.pjecommons.model.services.autoridadescertificadoras.MetaDadosConjuntoACs;
import br.jus.cnj.pje.webservice.PjeEurekaRegister;
import br.jus.cnj.pje.webservice.client.AutoridadesCertificadorasRestClient;
import br.jus.pje.nucleo.Eventos;

@Name(GerenciadorAutoridadesCertificadoras.NAME)
@Scope(ScopeType.APPLICATION)
@Startup(depends= {PjeEurekaRegister.NAME})
@Install()
public class GerenciadorAutoridadesCertificadoras {
	
	public static final String NAME = "gerenciadorAutoridadesCertificadoras";
	
	@Logger
	private Log logger;
	
	private static final String ARQUIVO_ACs_ICPBR = "intermediarias.zip";
	private static final String ARQUIVO_CONFIAVEIS_ICPBR = "confiaveis.zip";

	private static BinarioACs intermediarias = new BinarioACs(ARQUIVO_ACs_ICPBR);
	private static BinarioACs confiaveis = new BinarioACs(ARQUIVO_CONFIAVEIS_ICPBR);

	@Create
	public void init() {
		buscarAtualizacaoAutoridadesCertificadoras();
		verificarAtualizacaoAutoridadesCertificadoras();
	}
	
	private void buscarAtualizacaoAutoridadesCertificadoras() {
		if(ConfiguracaoIntegracaoCloud.isUpdateAutoridadesCertificadorasOnStartup()) {
			try {
				MetaDadosConjuntoACs metaDados = ComponentUtil.getComponent(AutoridadesCertificadorasRestClient.class).recuperarUltimaAtualizacao();
				validarAtualizacaoAutoridadesCertificadoras(metaDados);
			}catch (Exception e) {
			}
		}
	}

	public void validarAtualizacaoAutoridadesCertificadoras(MetaDadosConjuntoACs metaDados) {
		if(metaDados != null) {
			String hashIntermediarias = metaDados.getChecksumIntermediarias();
			if(!hashIntermediarias.equals(intermediarias.getChecksum())) {
				Binario intermediariasDTO = ComponentUtil.getComponent(AutoridadesCertificadorasRestClient.class).recuperarBinarioIntermediarias(hashIntermediarias);
				intermediarias.alteraConteudo(hashIntermediarias, intermediariasDTO);
			}
			String hashConfiaveis = metaDados.getChecksumConfiaveis();
			if(!hashConfiaveis.equals(confiaveis.getChecksum())) {
				Binario confiaveisDTO = ComponentUtil.getComponent(AutoridadesCertificadorasRestClient.class).recuperarBinarioConfiaveis(hashConfiaveis);
				confiaveis.alteraConteudo(hashConfiaveis, confiaveisDTO);
			}
			verificarAtualizacaoAutoridadesCertificadoras();
		}
	}
	
	/**
	 * Verifica se houve atualização do arquivo de autoridades certificadoras e lança o evento correspondente
	 * 
	 * @param intermediarias
	 * @param confiaveis
	 */
	private void verificarAtualizacaoAutoridadesCertificadoras() {
		if(intermediarias.isConteudoAlterado() || confiaveis.isConteudoAlterado()) {
			Events.instance().raiseEvent(Eventos.EVENTO_AUTORIDADES_CERTIFICADORAS_ALTERADAS);
			intermediarias.setConteudoAlterado(false);
			confiaveis.setConteudoAlterado(false);
		}
	}

	public BinarioACs getIntermediarias() {
		return intermediarias;
	}

	public BinarioACs getConfiaveis() {
		return confiaveis;
	}
}