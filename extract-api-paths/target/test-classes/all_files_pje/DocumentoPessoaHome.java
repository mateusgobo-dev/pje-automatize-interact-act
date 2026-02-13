package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.util.Strings;

import br.com.infox.core.certificado.Certificado;
import br.com.infox.core.certificado.CertificadoException;
import br.com.infox.core.certificado.util.VerificaCertificadoPessoa;
import br.com.itx.component.FileHome;
import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.csjt.pje.view.action.CadastroJusPostulandiHome;
import br.jus.pje.nucleo.entidades.DocumentoPessoa;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFisicaEspecializada;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;

@Name("documentoPessoaHome")
@BypassInterceptors
public class DocumentoPessoaHome extends AbstractDocumentoPessoaHome<DocumentoPessoa> {

	private static final String DOCUMENTO_INSERIDO = "documentoInserido";

	private static final long serialVersionUID = 1L;

	private String certChain;
	private String signature;
	private Pessoa pessoaDoc;

	private boolean modelo = true;

	public DocumentoPessoaHome() {
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		boolean faltaDocumento = FileHome.instance().getData() == null
				&& Strings.isEmpty(getInstance().getDocumentoHtml());
		if (faltaDocumento) {
			FacesMessages.instance().add(Severity.ERROR, "Documento requerido.");
			return false;
		}
		return true;
	}

	@Override
	public String persist() {
		if (FileHome.instance() != null && !modelo) {
			instance.setDocumentoHtml(null);
			if (!FileHome.instance().getFileType().equals("pdf")) {
				FacesMessages.instance().add(Severity.ERROR, "O sistema só aceita documento do tipo pdf");
				return null;
			}
			instance.setDocumentoBin(FileHome.instance().getData());
			instance.setNomeArquivo(FileHome.instance().getFileName());
		}
		instance.setDataInclusao(new Date());
		instance.setAtivo(Boolean.TRUE);

		// Teste para saber se o cadastro do documento pessoa é feito no
		// cadastro do advogado.
		if (instance.getPessoa() == null) {
			instance.setPessoa(pessoaDoc);
		}
		if (instance.getUsuarioCadastro() == null) {
			Pessoa pessoaLogada = (Pessoa) Contexts.getSessionContext().get("pessoaLogada");
			pessoaLogada = getEntityManager().find(Pessoa.class, pessoaLogada.getIdUsuario());
			instance.setUsuarioCadastro(pessoaLogada);
		}

		String msg = super.persist();
		refreshGrid("documentoPessoaJusPostulandiGrid");
		refreshGrid("documentoPessoaGrid");
		if (msg != null) {
			Util.setToEventContext(DOCUMENTO_INSERIDO, true);
		}
		
		modelo = true;
		return msg;
	}

	public Boolean isDocumentoInserido() {
		Object object = Util.getFromEventContext(DOCUMENTO_INSERIDO);
		if (object != null) {
			return (Boolean) object;
		} else {
			return false;
		}
	}

	public static DocumentoPessoaHome instance() {
		return ComponentUtil.getComponent("documentoPessoaHome");
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getSignature() {
		return signature;
	}

	public void setCertChain(String certChain) {
		this.certChain = certChain;
	}

	public String getCertChain() {
		return certChain;
	}

	public void assinarDocumento(boolean verificaCertificadoPessoaLogada) {
		try {
			if (verificaCertificadoPessoaLogada) {
				VerificaCertificadoPessoa.verificaCertificadoPessoaLogada(certChain);
			}
			DocumentoPessoa documentoPessoa = getInstance();
			documentoPessoa.setCertChain(certChain);
			documentoPessoa.setSignature(signature);
			documentoPessoa.setAtivo(true);
			getEntityManager().merge(documentoPessoa);
			getEntityManager().flush();
			FacesMessages.instance().add(Severity.INFO, "Documento Assinado com Sucesso");
		} catch (CertificadoException e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao assinar o documento: " + e.getMessage(), e);
		}
	}

	public void assinarDocumento() {
		assinarDocumento(true);
	}

	public void atualizaAssinatura(boolean verificaCertificadoPessoaLogada) {
		try {
			if (verificaCertificadoPessoaLogada)
				VerificaCertificadoPessoa.verificaCertificadoPessoaLogada(certChain);

			DocumentoPessoa documentoPessoa = getInstance();
			documentoPessoa.setCertChain(certChain);
			documentoPessoa.setSignature(signature);
			getEntityManager().merge(documentoPessoa);
			getEntityManager().flush();
			FacesMessages.instance().add(Severity.INFO, "Documento Assinado com Sucesso");
		} catch (CertificadoException e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao Assinar com Sucesso: " + e.getMessage(), e);
		}
	}
	
	/*

     * PJE-JT: Ricardo Scholz e David Vieira : PJE-1158 - 2012-01-18 Alterações feitas pela JT.

     * Inclusão de método para recuperar, a partir da cadeia de certificados, o nome do usuário que

     * assinou o documento.

     */

    /**

     * Recupera o nome do usuário que assinou o documento em {@link DocumentoPessoaHome#getInstance()},

     * a partir da cadeia de certificados em {@link DocumentoPessoa#getCertChain()}.

     * 

     * @return o nome do usuário que assinou o documento; caso não seja encontrado, retorna <code>null</code>.

     */

    public String getNomeAssinatura() {
 
            try {

                    Certificado c = new Certificado(getInstance().getCertChain());

                    return c.getNome();     

            } catch (CertificadoException e) {}

            return null;

    }

    /*

     * PJE-JT: Fim.

     */
    
	public void atualizaAssinatura() {
		atualizaAssinatura(true);
	}

	public void assinaTermo(String destino) {
		assinarDocumento(false);
		PessoaAdvogadoHome instance2 = PessoaAdvogadoHome.instance();
		instance2.setId(getInstance().getPessoa().getIdUsuario());
		instance2.getInstance().setValidado(true);
		instance2.getInstance().setAtivo(true);
		getEntityManager().merge(instance2.getInstance());
		getEntityManager().flush();		
		if (!Strings.isEmpty(destino)) {
			if (instance2.getInstance().getValidado()) {
				FacesMessages.instance().add(Severity.INFO, "Acesso liberado ao sistema.");
			}
			Redirect.instance().setViewId(destino);
			Redirect.instance().execute();
		}
	}

	/**
	 * @author reiser / rodrigo
	 * @category PJE-JT
	 * @since versao 1.4.2
	 * @param destino
	 */
	public void assinaTermoJusPostulandi(String destino) {
		atualizaAssinatura(false);
		CadastroJusPostulandiHome instance2 = CadastroJusPostulandiHome.instance();
		instance2.setId(getInstance().getPessoa().getIdUsuario());
		if (!Strings.isEmpty(destino)) {
			if (instance2.getInstance().getValidado()) {
				FacesMessages.instance().add(Severity.INFO, "Acesso liberado ao sistema.");
			}
			Redirect.instance().setViewId(destino);
			Redirect.instance().execute();
		}
	}

	public void setModelo(boolean modelo) {
		this.modelo = modelo;
	}

	public boolean isModelo() {
		return modelo;
	}

	public void executeDownload(int idDocumentoPessoa) {
		DocumentoPessoa documentoPessoa = EntityUtil.getEntityManager().find(DocumentoPessoa.class, idDocumentoPessoa);
		FileHome fileHome = new FileHome();
		fileHome.setData(documentoPessoa.getDocumentoBin());
		fileHome.setFileName(documentoPessoa.getNomeArquivo());
		Contexts.getConversationContext().set("fileHome", fileHome);
	}

	public Pessoa getPessoaDoc() {
		return pessoaDoc;
	}

	public void setPessoaDoc(Pessoa pessoaDoc) {
		this.pessoaDoc = pessoaDoc;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoaDoc(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída
	 */
	public void setPessoaDoc(PessoaFisicaEspecializada pessoa){
		setPessoaDoc(pessoa != null ? pessoa.getPessoa() : (Pessoa) null);
	}

	@SuppressWarnings("unchecked")
	public List<TipoProcessoDocumento> tipoProcessoDocumentoItemsList() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from TipoProcessoDocumento o ");
		sb.append("where o.ativo = true ");
		sb.append("and (o.inTipoDocumento = #{processoDocumentoHome.modelo ? 'P' : 'D'} OR o.inTipoDocumento = 'T') ");
		// sb.append("and o.tipoProcessoDocumento != #{processoDocumentoHome.isPeticaoInicial() ? '' : 'Petição Inicial'} ");
		sb.append("and o.tipoProcessoDocumento != 'Petição Inicial' ");
		sb.append("and o in (select d.tipoProcessoDocumento " + "		 	 from TipoProcessoDocumentoPapel d "
				+ "		 	 where d.papel.identificador like #{identificadorPapelAtual}) ");
		sb.append("and o in (select tpd.tipoProcessoDocumento from AplicacaoClasseTipoProcessoDocumento tpd "
				+ "where tpd.aplicacaoClasse.idAplicacaoClasse = #{parametroUtil.aplicacaoSistema.idAplicacaoClasse}) ");
		sb.append("and o.idTipoProcessoDocumento != #{parametroUtil.tipoProcessoDocumentoDespacho.idTipoProcessoDocumento} ");
		sb.append("and o.idTipoProcessoDocumento != #{parametroUtil.tipoProcessoDocumentoSentenca.idTipoProcessoDocumento} ");
		sb.append("order by o.tipoProcessoDocumento ");
		Query q = getEntityManager().createQuery(sb.toString());
		// q.setParameter("parametro", arg1);
		return q.getResultList();
	}

	@Override
	public void newInstance() {
		setModelo(true);
		super.newInstance();
	}

	@SuppressWarnings("unchecked")
	public ArrayList<TipoProcessoDocumento> getTipoDocumentoItems() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from TipoProcessoDocumento o ");
		sb.append("where o.ativo = true ");
		sb.append("and o in (select d.tipoProcessoDocumento ");
		sb.append("from TipoProcessoDocumentoPapel d ");
		sb.append("where d.papel.identificador like #{identificadorPapelAtual}) ");
		sb.append("and o in (select tpd.tipoProcessoDocumento ");
		sb.append("from  AplicacaoClasseTipoProcessoDocumento tpd ");
		sb.append("where tpd.aplicacaoClasse.idAplicacaoClasse = #{parametroUtil.aplicacaoSistema.idAplicacaoClasse}) ");
		sb.append("and o.idTipoProcessoDocumento != #{parametroUtil.tipoProcessoDocumentoDespacho.idTipoProcessoDocumento} ");
		sb.append("and o.idTipoProcessoDocumento != #{parametroUtil.tipoProcessoDocumentoSentenca.idTipoProcessoDocumento} ");
		if(modelo){
			sb.append("and (o.inTipoDocumento = 'P' or o.inTipoDocumento = 'T') ");
		}else{
			sb.append("and (o.inTipoDocumento = 'D' or o.inTipoDocumento = 'T') ");
		}
		Query q = getEntityManager().createQuery(sb.toString());
		return (ArrayList<TipoProcessoDocumento>) q.getResultList();
		

	}	
	
	@Override
	public String update() {
		refreshGrid("documentoPessoaGrid");
		String ret = null;
		ret = super.update();
		newInstance();
		return ret;
	}
	
}
