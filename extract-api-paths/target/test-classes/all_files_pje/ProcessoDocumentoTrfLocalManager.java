package br.com.infox.pje.manager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.manager.GenericManager;
import br.com.infox.pje.dao.ProcessoDocumentoTrfLocalDAO;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoTrfLocal;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.enums.LiberacaoConsultaPublicaEnum;
import br.jus.pje.jt.entidades.DocumentoVoto;

/**
 * Classe manager para a entidade de ProcessoDocumentoTrfLocal
 * 
 * @author Daniel
 * 
 */
@Name(ProcessoDocumentoTrfLocalManager.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class ProcessoDocumentoTrfLocalManager extends GenericManager {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "processoDocumentoTrfLocalManager";

	@In
	private ProcessoDocumentoTrfLocalDAO processoDocumentoTrfLocalDAO;
	@In
	private ProcessoDocumentoManager processoDocumentoManager;

	public ProcessoDocumentoTrfLocal getProcessoDocumentoTrfByProcessoDocumento(ProcessoDocumento pd) {
		return find(ProcessoDocumentoTrfLocal.class, pd.getIdProcessoDocumento());
	}

	/**
	 * Verifica se o documento informado pode ser visualizado na consulta
	 * pública, validando pelo Tipo do ProcessoDocumento.
	 * 
	 * @param pdTrfLocal
	 * @return true se pode ser visualizado na consulta pública.
	 */
	public boolean canSeeConsultaPublica(ProcessoDocumentoTrfLocal pdTrfLocal) {
		ParametroUtil parametroUtil = ParametroUtil.instance();
		TipoProcessoDocumento tipoPd = pdTrfLocal.getProcessoDocumento().getTipoProcessoDocumento();
		TipoProcessoDocumento relatorio = parametroUtil.getTipoProcessoDocumentoRelatorio();
		TipoProcessoDocumento voto = parametroUtil.getTipoProcessoDocumentoVoto();
		TipoProcessoDocumento ementa = parametroUtil.getTipoProcessoDocumentoEmenta();
		if (tipoPd.equals(relatorio) || tipoPd.equals(voto) || tipoPd.equals(ementa)) {
			return false;
		}

		return !processoDocumentoManager.isDocumentoAto(pdTrfLocal.getProcessoDocumento())
				|| pdTrfLocal.getLiberadoConsultaPublica();
	}

	public void criarDocumentoLiberadoConsultaPublica(ProcessoDocumento pd, LiberacaoConsultaPublicaEnum liberacao) {
		if (pd == null) {
			return;
		}

		ProcessoDocumentoTrfLocal pdTrfLocal = getProcessoDocumentoTrfByProcessoDocumento(pd);
		if (pdTrfLocal == null) {
			pdTrfLocal = new ProcessoDocumentoTrfLocal();
			pdTrfLocal.setIdProcessoDocumentoTrf(pd.getIdProcessoDocumento());
			pdTrfLocal.setProcessoDocumento(pd);
			pdTrfLocal.setLiberadoConsultaPublica(true);
			pdTrfLocal.setLiberacaoConsultaPublicaEnum(liberacao);
			persist(pdTrfLocal);
		} else {
			pdTrfLocal.setLiberadoConsultaPublica(true);
			pdTrfLocal.setLiberacaoConsultaPublicaEnum(liberacao);
			update(pdTrfLocal);
		}
	}

	/**
	 * Verifica se o documento ja existe, e de acordo com o que o usuario
	 * definiu no campo liberarCOnsultaPublica ele realiza a liberação ou o
	 * bloqueio do documento, se o documento não existir ele cria, se existir
	 * ele atualiza para o que foi definido.
	 * 
	 * @param pd
	 *            - documento a ser criado ou atualizado
	 * @param liberar
	 *            true se for para liberar para consulta publica
	 */
	public void criarDocumentoPublico(ProcessoDocumento pd, boolean liberar) {
		if (pd == null) {
			return;
		}

		ProcessoDocumentoTrfLocal pdTrfLocal = find(ProcessoDocumentoTrfLocal.class, pd.getIdProcessoDocumento());
		if (pdTrfLocal == null) {
			pdTrfLocal = new ProcessoDocumentoTrfLocal();
			pdTrfLocal.setIdProcessoDocumentoTrf(pd.getIdProcessoDocumento());
			pdTrfLocal.setProcessoDocumento(pd);
			definirConsultaPublica(liberar, pdTrfLocal);
			persist(pdTrfLocal);
		} else {
			definirConsultaPublica(liberar, pdTrfLocal);
			update(pdTrfLocal);
		}
	}

	/**
	 * Define a liberação da consulta publica para o documentoTrf inforamdo.
	 * 
	 * @param liberar
	 *            true se for para liberar para consulta publica
	 * @param pdTrfLocal
	 *            - documento que se deseja liberar.
	 */
	private void definirConsultaPublica(boolean liberar, ProcessoDocumentoTrfLocal pdTrfLocal) {
		if (liberar) {
			pdTrfLocal.setLiberadoConsultaPublica(true);
			pdTrfLocal.setLiberacaoConsultaPublicaEnum(LiberacaoConsultaPublicaEnum.A);
		} else {
			pdTrfLocal.setLiberadoConsultaPublica(false);
			pdTrfLocal.setLiberacaoConsultaPublicaEnum(LiberacaoConsultaPublicaEnum.D);
		}
	}
	
	public void removerDaTabelaProcessoDocumentoTrf(DocumentoVoto documentoVoto) {
		processoDocumentoTrfLocalDAO.removerDaTabelaProcessoDocumentoTrf(documentoVoto);
	}

}