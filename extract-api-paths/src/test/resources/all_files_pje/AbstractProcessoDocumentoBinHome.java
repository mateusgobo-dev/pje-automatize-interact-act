/*
 IBPM - Ferramenta de produtividade Java
 Copyright (c) 1986-2009 Infox Tecnologia da Informação Ltda.

 Este programa é software livre; você pode redistribuí-lo e/ou modificá-lo 
 sob os termos da GNU GENERAL PUBLIC LICENSE (GPL) conforme publicada pela 
 Free Software Foundation; versão 2 da Licença.
 Este programa é distribuído na expectativa de que seja útil, porém, SEM 
 NENHUMA GARANTIA; nem mesmo a garantia implícita de COMERCIABILIDADE OU 
 ADEQUAÇÃO A UMA FINALIDADE ESPECÍFICA.

 Consulte a GNU GPL para mais detalhes.
 Você deve ter recebido uma cópia da GNU GPL junto com este programa; se não, 
 veja em http://www.gnu.org/licenses/   
 */
package br.com.infox.ibpm.home;

import java.io.IOException;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.util.Strings;

import com.lowagie.text.pdf.PdfReader;

import br.com.infox.cliente.component.ValidacaoAssinaturaProcessoDocumento;
import br.com.infox.cliente.home.ProcessoDocumentoHome;
import br.com.itx.component.AbstractHome;
import br.com.itx.component.FileHome;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.PdfUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.DocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumentoTrf;
import br.jus.pje.nucleo.enums.ExigibilidadeAssinaturaEnum;
import br.jus.pje.nucleo.util.Crypto;

public abstract class AbstractProcessoDocumentoBinHome<T> extends AbstractHome<ProcessoDocumentoBin> {

	private static final long serialVersionUID = 1L;
	private ProcessoDocumento processoDocumento;
	private boolean isModelo;
	private boolean ignoraConteudoDocumento = Boolean.FALSE;

	public void setProcessoDocumento(ProcessoDocumento processoDocumento) {
		this.processoDocumento = processoDocumento;
	}

	public ProcessoDocumento getProcessoDocumento() {
		return processoDocumento;
	}

	public boolean isModelo() {
		return isModelo;
	}

	public void isModelo(boolean isModelo) {
		this.isModelo = isModelo;
	}

	@Override
	public void newInstance() {
		FileHome.instance().clear();
		super.newInstance();
	}

	public boolean isModeloVazio() {
		boolean modeloVazio = isModeloVazio(getInstance());
		if (modeloVazio) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "O modelo está vazio.");
		}
		return modeloVazio;
	}

	public static boolean isModeloVazio(ProcessoDocumentoBin bin) {
		return isModeloVazio(bin.getModeloDocumento());
	}

	public static boolean isModeloVazio(String modelo) {
		return modelo == null || Strings.isEmpty(modelo)
				|| Strings.isEmpty(removeTags(modelo));
	}

	private static String removeTags(String modelo) {
		return modelo.replaceAll("\\<.*?\\>", "").replaceAll("\n", "").replaceAll("\r", "").replaceAll("&nbsp;", "");
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		boolean ret = true;
		if (isModelo) {
			if (!ignoraConteudoDocumento && isModeloVazio()) {
				ret = false;
			}
			if (ret) {
				getInstance().setUsuario(Authenticator.getUsuarioLogado());
				getInstance().setMd5Documento(Crypto.encodeMD5(getInstance().getModeloDocumento()));
			}
		}
		return ret;
	}

	@Override
	public String persist() {

		setValido();
		String ret = null;
		try {
			if (isModelo) {
				ret = super.persist();
			} else {
				FileHome file = FileHome.instance();
				if (isDocumentoBinValido(file)) {
					getInstance().setUsuario(Authenticator.getUsuarioLogado());
					getInstance().setExtensao(file.getFileType());
					getInstance().setMd5Documento(file.getMD5());
					getInstance().setNomeArquivo(file.getFileName());
					getInstance().setSize(file.getSize());
					getInstance().setModeloDocumento(null);
					getInstance().setNumeroDocumentoStorage(DocumentoBinManager.instance().persist(file.getData(),file.getContentType()));
					ret = super.persist();
				}
			}
			if (ret == null) {
				FacesMessages.instance().add(Severity.ERROR, "Erro ao gravar o documento.");
			} else {
				FacesMessages.instance().clear();
			}
		} catch (PJeBusinessException e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao gravar o documento.");
		}
		return ret;
	}

	public String persistSemLista() {
		return super.persist();
	}

	private boolean isDocumentoBinValido(FileHome file) {
		if (file == null) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Nenhum documento selecionado.");
			return false;
		}
		if (!file.getFileType().equalsIgnoreCase("PDF")) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "O documento deve ser do tipo PDF.");
			return false;
		}
		if (file.getSize() != null && file.getSize() > 5242880) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR,
					"O documento deve ter o tamanho máximo de 5MB!");
			return false;
		}
		//--Verificação do tamanho máximo da página de um pdf
		ProcessoDocumento pdPdf = ProcessoDocumentoHome.instance().getInstance();
		if (pdPdf != null && pdPdf.getTipoProcessoDocumento() != null) {
			TipoProcessoDocumentoTrf tpd = EntityUtil.find(TipoProcessoDocumentoTrf.class, pdPdf.getTipoProcessoDocumento().getIdTipoProcessoDocumento());
			if (tpd != null) {
				try {
					PdfReader pdf = new PdfReader(file.getData());
					if (!PdfUtil.verificarTamanhoValidoPagina(pdf, tpd)) {
						FacesMessages.instance().add(StatusMessage.Severity.ERROR,
								"O tamanho médio da página para o Tipo " + tpd.getTipoProcessoDocumento() + " é de " + tpd.getTamanhoMaximoPagina() + " KB.");
						return false;
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}

	public void setProcessoDocumentoBinIdProcessoDocumentoBin(Integer id) {
		setId(id);
	}

	public Integer getProcessoDocumentoBinIdProcessoDocumentoBin() {
		return (Integer) getId();
	}

	@Override
	protected ProcessoDocumentoBin createInstance() {
		ProcessoDocumentoBin processoDocumentoBin = new ProcessoDocumentoBin();
		UsuarioHome usuarioHome = (UsuarioHome) Component.getInstance("usuarioHome", false);
		if (usuarioHome != null) {
			processoDocumentoBin.setUsuario(usuarioHome.getDefinedInstance());
		}
		return processoDocumentoBin;
	}

	@Override
	public String remove(ProcessoDocumentoBin obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("processoDocumentoBinGrid");
		return ret;
	}

	public List<ProcessoDocumento> getProcessoDocumentoList() {
		return getInstance() == null ? null : getInstance().getProcessoDocumentoList();
	}

	public String setDownloadInstance() {
		exportData();
		return "/download.xhtml";
	}

	public void exportData() {
		String numeroDocumento = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("numeroDocumento");
		String nomeArquivo = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("nomeArqProcDocBin");
		String idProcessoDocumento = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("idProcessoDocumento");
		ProcessoDocumentoManager pdm = ComponentUtil.getComponent("processoDocumentoManager");
		ProcessoDocumento pd = null;
		try {
			pd = pdm.findById(Integer.parseInt(idProcessoDocumento));
		} catch (PJeBusinessException e1) {
			e1.printStackTrace();
		}
		if( numeroDocumento != null && numeroDocumento.trim().length() > 0 ){
			FileHome file = FileHome.instance();
			String fileName = "ProcessoDocumentoBin";
			if (nomeArquivo != null) {
				fileName = nomeArquivo;
			}
			file.setFileName(fileName);
			try {
				byte[] data = DocumentoBinManager.instance().getData(numeroDocumento);
				data = ValidacaoAssinaturaProcessoDocumento.instance().inserirInfoAssinaturasPDF(getInstance(), data, pd);
				file.setData(data);

				// PJEII-4570 - Abrir documento/anexo à direita do paginador
				if (getInstance().getExtensao() == null) {
					String extensao = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("extensao");
					
					if (extensao != null && !"".equals(extensao)) {
						getInstance().setExtensao(extensao);
					}					
				}
				// PJEII-4570
				
				file.setContentType(getInstance().getExtensao());
			} catch (Exception e) {
				FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Erro ao descarregar o documento.");
				e.printStackTrace();
			}
			Contexts.getConversationContext().set("fileHome", file);
		}
	}

	public void setIgnoraConteudoDocumento(boolean ignoraConteudoDocumento) {
		this.ignoraConteudoDocumento = ignoraConteudoDocumento;
	}

	public boolean isIgnoraConteudoDocumento() {
		return ignoraConteudoDocumento;
	}

	public void setValido() {
		if (getInstance() != null && !getInstance().getValido()) {
			ProcessoDocumentoHome pdH = ProcessoDocumentoHome.instance();
			getInstance().setValido(pdH.estaValido(getInstance()));
			//getInstance().setValido(estaValidado(getInstance()));
		}
	}

	public Boolean estaValidado(ProcessoDocumentoBin documentoBin) {
		// Agora é validado se pelo menos um dos papeis obrigatórios assinou

		EntityManager em = getEntityManager();

		if (!em.contains(documentoBin)) {
			return false;
		}

		StringBuilder sqlPes = new StringBuilder();

		sqlPes.append(" select count(tipopapel.papel) from ");
		sqlPes.append(" TipoProcessoDocumentoPapel tipopapel, ");
		sqlPes.append(" ProcessoDocumento doc, ");
		sqlPes.append(" UsuarioLocalizacao loc, ");
		sqlPes.append(" ProcessoDocumentoBinPessoaAssinatura ass ");
		sqlPes.append(" where (tipopapel.tipoProcessoDocumento = doc.tipoProcessoDocumento");
		sqlPes.append(" and tipopapel.exigibilidade = :exibilidade ");
		sqlPes.append(" and loc.papel = tipopapel.papel ");
		sqlPes.append(" and ass.pessoa.idUsuario = loc.usuario.idUsuario ");
		sqlPes.append(" and ass.processoDocumentoBin = doc.processoDocumentoBin ");
		sqlPes.append(" and doc.processoDocumentoBin = :documento)");

		Query query = em.createQuery(sqlPes.toString());
		query.setParameter("documento", documentoBin);
		query.setParameter("exibilidade", ExigibilidadeAssinaturaEnum.S);
		query.setMaxResults(1);
		// Se houver pelo menos uma assinatura das suficiente, está validado
		Long retorno = 0L;
		try {
			retorno = (Long) query.getSingleResult();
		} catch (NoResultException no) {
		}
		if (retorno > 0) {
			return true;
		} else {
			sqlPes = new StringBuilder();
			sqlPes.append("select count(tipopapel) from TipoProcessoDocumentoPapel tipopapel ");
			sqlPes.append("inner join tipopapel.tipoProcessoDocumento.processoDocumentoList doclist ");
			sqlPes.append("where doclist.processoDocumentoBin = :documento ");
			sqlPes.append("and doclist.processoDocumentoBin not in ");
			sqlPes.append("(select doclist2.processoDocumentoBin from TipoProcessoDocumentoPapel tipopapel2 ");
			sqlPes.append("inner join tipopapel2.tipoProcessoDocumento.processoDocumentoList doclist2 ");
			sqlPes.append("where (tipopapel2.exigibilidade = 'S' or tipopapel2.exigibilidade = 'O') ");
			sqlPes.append("and doclist2.processoDocumentoBin = :documento) ");
			query = em.createQuery(sqlPes.toString());
			query.setParameter("documento", documentoBin);
			query.setMaxResults(1);
			// Se não existir assinatura obrigatória ou suficiente, está
			// validado
			try {
				retorno = (Long) query.getSingleResult();
			} catch (NoResultException no) {
			}
			if (retorno > 0) {
				return true;
			} else {
				// Se tiver todas as assinaturas obrigatórias, está validado
				sqlPes = new StringBuilder();

				sqlPes.append(" select count(tipopapel2.papel) from ");
				sqlPes.append(" TipoProcessoDocumentoPapel tipopapel2 ");
				sqlPes.append(" inner join tipopapel2.tipoProcessoDocumento.processoDocumentoList doc2 ");
				sqlPes.append(" where tipopapel2.exigibilidade = 'O' ");
				sqlPes.append(" and doc2.processoDocumentoBin = :documento ");
				sqlPes.append(" and tipopapel2.papel not in ");
				sqlPes.append(" (select tipopapel.papel from ");
				sqlPes.append(" TipoProcessoDocumentoPapel tipopapel, ");
				sqlPes.append(" ProcessoDocumento doc, ");
				sqlPes.append(" UsuarioLocalizacao loc, ");
				sqlPes.append(" ProcessoDocumentoBinPessoaAssinatura ass ");
				sqlPes.append(" where (tipopapel.tipoProcessoDocumento = doc.tipoProcessoDocumento");
				sqlPes.append(" and loc.papel = tipopapel.papel ");
				sqlPes.append(" and ass.pessoa.idUsuario = loc.usuario.idUsuario ");
				sqlPes.append(" and ass.processoDocumentoBin = doc.processoDocumentoBin ");
				sqlPes.append(" and doc.processoDocumentoBin = :documento))");

				query = em.createQuery(sqlPes.toString());
				query.setParameter("documento", documentoBin);
				query.setMaxResults(1);
				try {
					retorno = (Long) query.getSingleResult();
				} catch (NoResultException no) {
					return Boolean.TRUE;
				}
				// Se todos os papeis de assinatura obrigatória forem
				// encontrados no processo esta consulta retorna vazia
				if (retorno == 0) {
					return true;
				} else {
					return false;
				}
			}
		}
	}

}