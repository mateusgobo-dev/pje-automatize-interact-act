package br.jus.cnj.pje.intercomunicacao.v222.servico;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;

import br.jus.cnj.intercomunicacao.v222.beans.ManifestacaoProcessual;
import br.jus.cnj.pje.intercomunicacao.v222.util.MNIUtil;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(IntercomunicacaoObserver.NAME)
@Scope(ScopeType.APPLICATION)
public class IntercomunicacaoObserver {
	public static final String NAME = "v222.intercomunicacaoObserver";
	public static final String UNIDADE_CNJ = "200";
	
	public enum ServiceAdapterEnum {
		SERVICO_MIGRACAO_ECNJ("ECNJ"),
		SERVICO_MIGRACAO_PROJUDI("PROJUDI");

		private final String text;

		private ServiceAdapterEnum(final String text) {
			this.text = text;
		}

		public static boolean constains(String observerEnum) {
			if (observerEnum == null)
				return false;

			boolean contains = false;
			for (ServiceAdapterEnum enumeration : ServiceAdapterEnum.values()) {
				if (observerEnum.contains(enumeration.toString())) {
					contains = true;
					break;
				}
			}
			return contains;
		}

		@Override
		public String toString() {
			return text;
		}
	}
	
	@Observer({ IntercomunicacaoAbstract.BEFORE_EVENT_NAME })
	public void handleBeforeEvent(ManifestacaoProcessual manifestacaoProcessual) {
		String login = MNIUtil.obterLogin(manifestacaoProcessual);
		ManifestacaoProcessualHandler handler = buscaHandler(login);
		if (handler != null) {
			handler.onBeforeEntregarManifestacaoProcessual(manifestacaoProcessual);
		}
	}

	@Observer({IntercomunicacaoService.AFTER_EVENT_NAME})
	public void handleAfterEvent(ManifestacaoProcessual manifestacaoProcessual, ProcessoTrf processoTrf, ProcessoDocumento documentoPrincipal) {
		String login = MNIUtil.obterLogin(manifestacaoProcessual);
		ManifestacaoProcessualHandler handler = buscaHandler(login);
		if (handler != null) {
			handler.onAfterEntregarManifestacaoProcessual(manifestacaoProcessual, processoTrf, documentoPrincipal);
		}
	}

	private ManifestacaoProcessualHandler buscaHandler(String idManifestante) {
		ManifestacaoProcessualHandler handler = null;
		if (StringUtils.contains(idManifestante, "_")) {
			String codUnidade = idManifestante.substring(0,
					idManifestante.indexOf("_"));
			String codServico = idManifestante.substring(idManifestante
					.indexOf("_") + 1);

			if (codUnidade.equals(UNIDADE_CNJ)) {
				if (codServico.equals(ServiceAdapterEnum.SERVICO_MIGRACAO_ECNJ.toString())) {
					handler = (ManifestacaoProcessualHandler) Component.getInstance(
							IntercomunicacaoECNJAdapter.NAME);
				}
				if (codServico.equals(ServiceAdapterEnum.SERVICO_MIGRACAO_PROJUDI.toString())) {
					handler = (ManifestacaoProcessualHandler) Component.getInstance(
							IntercomunicacaoProjudiAdapter.NAME);
				}
				// TODO tratar outros serviços aqui
			}
			// TODO tratar outros orgaos aqui
		}
		
		if (handler == null) {
			handler = (ManifestacaoProcessualHandler) Component.getInstance(IntercomunicacaoPJEAdapter.NAME);
		}

		return handler;
	}
}
