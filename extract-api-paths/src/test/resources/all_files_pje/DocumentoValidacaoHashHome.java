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
package br.com.infox.cliente.home;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Query;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.Redirect;

import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.cnj.pje.view.VisualizarExpedienteAction;
import br.jus.pje.nucleo.entidades.DocumentoValidacaoHash;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;

@Name("documentoValidacaoHashHome")
@BypassInterceptors
public class DocumentoValidacaoHashHome extends AbstractDocumentoValidacaoHashHome<DocumentoValidacaoHash> {

	private static final long serialVersionUID = 1L;
	private int idPD;
	private String numeroHash;
	private Boolean hashValidado;

	private DocumentoValidacaoHash trazerProcessoDocumentoHash(String hash) {
		Criteria criteria = HibernateUtil.getSession().createCriteria(DocumentoValidacaoHash.class);
		criteria.add(Restrictions.eq("validacaoHash", hash));
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		return (DocumentoValidacaoHash)criteria.uniqueResult();
	}

	public ProcessoDocumentoBinHome processoDocumentoBinHome() {
		return ComponentUtil.getComponent("processoDocumentoBinHome");
	}

	public String validarHash() throws IOException {
		DocumentoValidacaoHash dvHash = trazerProcessoDocumentoHash(getNumeroHash());
		if (dvHash == null) {
			setHashValidado(Boolean.FALSE);
			setNumeroHash("");
			Redirect.instance().setViewId("/Painel/painel_usuario/documentoHashHTMLError.seam");
		} else {
			setHashValidado(Boolean.TRUE);
			Redirect.instance().setViewId("/Painel/painel_usuario/documentoHTML.seam");
			Redirect.instance().setParameter("idBin", ProcessoDocumentoBinHome.instance().getId());
			Redirect.instance().setParameter("idProcessoDoc", ProcessoDocumentoHome.instance().getId());
		}
		Redirect.instance().execute();
		return "";
	}

	@Override
	public void newInstance() {
		super.newInstance();
	}

	private DocumentoValidacaoHash trazerDocumentoValidacaoHash(int idPD) {
		Criteria criteria = HibernateUtil.getSession().createCriteria(DocumentoValidacaoHash.class);
		criteria.add(Restrictions.eq("processoDocumento.idProcessoDocumento", idPD));
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		return (DocumentoValidacaoHash)criteria.uniqueResult();
	}

	private ProcessoDocumento trazerProcessoDocumento(Integer idDocumento) {
		String query = "select o from ProcessoDocumento o where " + "o.idProcessoDocumento = :idDoc";

		Query q = getEntityManager().createQuery(query);
		q.setParameter("idDoc", idDocumento);

		return (ProcessoDocumento) EntityUtil.getSingleResult(q);
	}

	private String convertToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int twoHalfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9))
					buf.append((char) ('0' + halfbyte));
				else
					buf.append((char) ('a' + (halfbyte - 10)));
				halfbyte = data[i] & 0x0F;
			} while (twoHalfs++ < 1);
		}
		return buf.toString();
	}

	public String gerarHash(Date data, ProcessoDocumento pd) throws NoSuchAlgorithmException,
			UnsupportedEncodingException {
		if (data == null) {
			throw new IllegalArgumentException(
					"O parâmetro 'data' não deve ser nulo.");
		}
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		String dataHora = format.format(data.getTime());

		if (pd == null) {
			throw new IllegalArgumentException(
					"O parâmetro 'pd' não deve ser nulo.");
		}
		if (pd.getProcesso() == null
				|| pd.getProcesso().getNumeroProcesso() == null) {
			throw new IllegalArgumentException(
					"Erro no parâmetro 'pd': O processo ou o número do processo são nulos.");
		}
		String text = pd.getProcesso().getNumeroProcesso().concat(String.valueOf(pd.getIdProcessoDocumento()))
				.concat(dataHora);

		MessageDigest md;
		md = MessageDigest.getInstance("SHA-1");
		md.update(text.getBytes("iso-8859-1"), 0, text.length());
		byte[] sha1hash = md.digest();
		setNumeroHash(convertToHex(sha1hash));
		return getNumeroHash();
	}

	public void gerarRegistro() throws NoSuchAlgorithmException, UnsupportedEncodingException {
		newInstance();
		DocumentoValidacaoHash dvhash = trazerDocumentoValidacaoHash(this.getIdPD());
		Date data = new Date();
		// verifica se o hash do documento já existe
		if (dvhash != null) {
			// se o hash já existe deve ser setado o hash do banco para não usar
			// um novo
			setNumeroHash(dvhash.getValidacaoHash());
			instance.setProcessoDocumento(dvhash.getProcessoDocumento());
			instance.setValidacaoHash(dvhash.getValidacaoHash());
			instance.setDtAtualizacao(dvhash.getDtAtualizacao());
		} else {
			// se o hash não foi gerado deverá ser gerado agora
			ProcessoDocumento processoDocumento = trazerProcessoDocumento(this.getIdPD());
			numeroHash = gerarHash(data, processoDocumento);
			instance.setProcessoDocumento(processoDocumento);
			instance.setValidacaoHash(numeroHash);
			instance.setDtAtualizacao(data);
			setNumeroHash(numeroHash);
			persist();
		}
	}

	@Override
	public String persist() {
		return super.persist();
	}

	@Override
	public String update() {
		return super.update();
	}

	protected ProcessoDocumentoHome getProcessoDocumentoHome() {
		return (ProcessoDocumentoHome) Component.getInstance("processoDocumentoHome");
	}

	public void IdPD(int idPD) {
		this.setIdPD(idPD);
	}

	public void setNumeroHash(String numeroHash) {
		this.numeroHash = numeroHash;
	}

	public String getNumeroHash() {
		return numeroHash;
	}

	public void setarNumeroHash(String hash) {
		setNumeroHash(hash);
	}

	public void setHashValidado(Boolean hashValidado) {
		this.hashValidado = hashValidado;
	}

	public Boolean getHashValidado() {
		return hashValidado;
	}

	public int getIdPD() {
		return idPD;
	}
	
	public void setIdPD(int idPD) {
		this.idPD = idPD;
	}
	
	/*
  	* PJE-JT: Thiago Oliveira : PJE-6924
  	* Método criado para que o botão imprimir só apareça quando o documento tem o número do processo.
  	*/
	public Boolean verificaSePodeImprimir() {
		ProcessoDocumento processoDocumento = trazerProcessoDocumento(this.getIdPD());
		if (processoDocumento == null || 
			processoDocumento.getProcesso() == null || 
			processoDocumento.getProcesso().getNumeroProcesso() == null) {
			return false;
		}

		return true;
	}
	
	public void imprimirPdf(){
		VisualizarExpedienteAction visualizarExpedienteAction = new VisualizarExpedienteAction();
		ProcessoDocumento processoDocumento = trazerProcessoDocumento(this.getIdPD());
		visualizarExpedienteAction.imprimirPdf(processoDocumento);
	}

}