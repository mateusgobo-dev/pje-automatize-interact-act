package br.jus.csjt.pje.view.action;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.com.infox.cliente.component.signfile.SignFile;
import br.com.infox.cliente.home.AudImportacaoHome;
import br.com.infox.cliente.home.ProcessoDocumentoHome;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.component.AbstractHome;
import br.com.itx.component.Util;
import br.com.itx.component.grid.GridQuery;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.csjt.pje.commons.model.dto.DocumentoAtaAudienciaDTO;
import br.jus.csjt.pje.commons.model.dto.ProcessoImportadoAudDTO;
import br.jus.pje.jt.entidades.AudImportacao;
import br.jus.pje.jt.entidades.ProcessoAudienciaJT;
import br.jus.pje.nucleo.entidades.ProcessoAudiencia;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.SituacaoProcesso;

@Name(ProcessoAudienciaJTHome.NAME)
@AutoCreate
public class ProcessoAudienciaJTHome extends AbstractHome<ProcessoAudienciaJT> {

	public static final String NAME = "processoAudienciaJTHome";
	private static final long serialVersionUID = 1L;
	private List<DocumentoAtaAudienciaDTO> listaProcessoAudienciaAtasParaAssinar;
	private boolean checkSelecionarTodos;

	public boolean isCheckSelecionarTodos() {
		return checkSelecionarTodos;
	}

	public void setCheckSelecionarTodos(boolean checkSelecionarTodos) {
		this.checkSelecionarTodos = checkSelecionarTodos;
	}

	public void popularListaProcessoAudienciaAtasParaAssinar() {
		String sql = "select pa, s "
				+ "from SituacaoProcesso s, "
				+ "     ProcessoAudiencia pa "
				+ "where pa.processoDocumento != null and pa.processoDocumento.ativo = true"
				+ " and s.idProcesso = pa.processoTrf.idProcessoTrf "
				+ " and (pa.pessoaConciliador.idUsuario = :idUsuarioLogado or pa.pessoaRealizador.idUsuario = :idUsuarioLogado)"
				+ " and not exists ( from ProcessoDocumentoBinPessoaAssinatura pdb "
				+ "                 where pdb.processoDocumentoBin = pa.processoDocumento.processoDocumentoBin.idProcessoDocumentoBin ) "
				+ "order by pa.dtInicio asc";
		Query query = EntityUtil.getEntityManager().createQuery(sql);
		query.setParameter("idUsuarioLogado", Authenticator.getUsuarioLogado().getIdUsuario());

		@SuppressWarnings("rawtypes")
		List atasParaAssinarList = query.getResultList();

		listaProcessoAudienciaAtasParaAssinar = new ArrayList<DocumentoAtaAudienciaDTO>();
		for (int i = 0; i < atasParaAssinarList.size(); i++) {
			Object[] retorno = (Object[]) atasParaAssinarList.get(i);
			DocumentoAtaAudienciaDTO documento = new DocumentoAtaAudienciaDTO();

			ProcessoAudiencia pa = (ProcessoAudiencia) retorno[0];
			SituacaoProcesso sp = (SituacaoProcesso) retorno[1];

			documento.setProcessoAudiencia(pa);
			documento.setIdTarefa(sp.getIdTarefa());
			documento.setNomeTarefa(sp.getNomeTarefa());
			listaProcessoAudienciaAtasParaAssinar.add(documento);
		}
	}

	public List<DocumentoAtaAudienciaDTO> getListaProcessoAudienciaAtasParaAssinar() {
		if (listaProcessoAudienciaAtasParaAssinar == null) {
			popularListaProcessoAudienciaAtasParaAssinar();
		}
		return listaProcessoAudienciaAtasParaAssinar;
	}

	public String getUrlDocsField() {
		StringBuilder sb = new StringBuilder();
		if (listaProcessoAudienciaAtasParaAssinar != null) {
			for (DocumentoAtaAudienciaDTO pd : listaProcessoAudienciaAtasParaAssinar) {
				if (pd.isAssina()) {
					if (sb.length() > 0) {
						sb.append(',');
					}
					sb.append(gerarLinkDonwload(pd.getProcessoAudiencia().getProcessoDocumento()));
				}
			}
		}
		return sb.toString();
	}

	private String gerarLinkDonwload(ProcessoDocumento pd) {
		StringBuilder sb = new StringBuilder();
		sb.append(getUrlProjeto());
		sb.append("/downloadProcessoDocumento.seam?id=");
		sb.append(String.valueOf(pd.getIdProcessoDocumento()));
		sb.append("&codIni=");
		sb.append(ProcessoDocumentoHome.instance().getCodData(pd));
		sb.append("&md5=");
		sb.append(pd.getProcessoDocumentoBin().getMd5Documento());
		sb.append("&isBin=");
		sb.append(pd.getProcessoDocumentoBin().getExtensao() != null);
		return sb.toString();
	}

	private String getUrlProjeto() {
		Util util = new Util();
		return util.getUrlProject();
	}

	public void selecionarTodos() {
		int idTarefaParaAssinarAtasAudiencia = Integer.valueOf(ComponentUtil.getComponent(
				"idTarefaParaAssinarAtasAudiencia").toString());
		for (DocumentoAtaAudienciaDTO daa : listaProcessoAudienciaAtasParaAssinar) {
			if (idTarefaParaAssinarAtasAudiencia == 0 || daa.getIdTarefa() == idTarefaParaAssinarAtasAudiencia) {
				if (checkSelecionarTodos)
					daa.setAssina(true);
				else
					daa.setAssina(false);
			}
		}
	}

	public void importarDadosAud(String id, String codIni, String md5, String sign, String certChain,
			String hashSession, String data) throws Exception {
		SignFile signFile = ComponentUtil.getComponent("signFile");
		signFile.setId(id);
		signFile.setCodIni(codIni);
		signFile.setMd5(md5);
		signFile.setSign(sign);
		signFile.setCertChain(certChain);
		signFile.setHashSession(hashSession);
		signFile.setData(data);
		signFile.sign();
		String sql = "select a from AudImportacao a, ProcessoAudiencia pa "
				+ "where  pa.processoDocumento.idProcessoDocumento = :idProcessoDocumento "
				+ " and   a.idProcessoAudiencia = pa.idProcessoAudiencia ";
		Query query = EntityUtil.getEntityManager().createQuery(sql);
		query.setParameter("idProcessoDocumento", Integer.valueOf(id));
		try {
			AudImportacao audImportacao = (AudImportacao) query.getSingleResult();
			AudImportacaoHome audImportacaoHome = (AudImportacaoHome) ComponentUtil.getComponent("audImportacaoHome");
			audImportacaoHome.setInstance(audImportacao);
			audImportacaoHome.importarDados(audImportacao);
		} catch (javax.persistence.NoResultException e) {
			String mensagemErro = "Dados do AUD não foram importados corretamente. Não encontrado nenhum registro em AudImportacao "
					+ "(tabela temporária com dados sendo importados do AUD) para a audiência com id do ProcessoDocumentoBin="
					+ signFile.getId();
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, mensagemErro);
			LogProvider log = Logging.getLogProvider(ProcessoAudienciaJTHome.class);
			log.error(mensagemErro);
			throw new RuntimeException(mensagemErro);
		}

	}

	public void marcarAudienciaComoVerificada(ProcessoAudienciaJT processoAudienciaJT) {
		processoAudienciaJT.setVerificada(true);
		EntityManager entityManager = this.getEntityManager();
		entityManager.persist(processoAudienciaJT);
		entityManager.flush();
		entityManager.refresh(processoAudienciaJT);
	}
	
	public void marcarAudienciaComoVerificada(ProcessoImportadoAudDTO processoImportadoAudDTO) {
		ProcessoAudienciaJT processoAudienciaJT = processoImportadoAudDTO.getPajt();
		processoAudienciaJT.setVerificada(true);
		EntityManager entityManager = this.getEntityManager();
		entityManager.persist(processoAudienciaJT);
		entityManager.flush();
		entityManager.refresh(processoAudienciaJT);
		((GridQuery)ComponentUtil.getComponent("processoImportadoAudGrid")).refresh();
	}

}