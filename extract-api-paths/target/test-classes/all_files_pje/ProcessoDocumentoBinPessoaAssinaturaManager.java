/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import br.jus.cnj.pje.business.dao.ProcessoDocumentoBinPessoaAssinaturaDAO;
import br.jus.pje.jt.entidades.SessaoJT;
import br.jus.pje.jt.entidades.Voto;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBinPessoaAssinatura;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

/**
 * Componente de controle negocial da entidade {@link ProcessoDocumentoBinPessoaAssinatura}.
 * 
 * @author cristof
 *
 */
@Name("processoDocumentoBinPessoaAssinaturaManager")
public class ProcessoDocumentoBinPessoaAssinaturaManager extends BaseManager<ProcessoDocumentoBinPessoaAssinatura> {
	
	@In
	private ProcessoDocumentoBinPessoaAssinaturaDAO processoDocumentoBinPessoaAssinaturaDAO;

	@Override
	protected ProcessoDocumentoBinPessoaAssinaturaDAO getDAO() {
		return processoDocumentoBinPessoaAssinaturaDAO;
	}
	
	/**
	 * Recupera todas as assinaturas vinculadas ao conteúdo de documento.
	 * 
	 * @param conteudo conteúdo cujas assinaturas serão recuperadas
	 * @return lista de assinaturas do binário
	 * 
	 * @author Tassio Augusto
	 */
	public List<ProcessoDocumentoBinPessoaAssinatura> getAssinaturaDocumento(ProcessoDocumentoBin conteudo) {
		if (conteudo == null || conteudo.getIdProcessoDocumentoBin() == 0) {
			return new ArrayList<ProcessoDocumentoBinPessoaAssinatura>();
		}

	    Search s = new Search(ProcessoDocumentoBinPessoaAssinatura.class);
	    addCriteria(s, 
	    		Criteria.equals("processoDocumentoBin", conteudo));
		return list(s);
	}

	/**
	 * Recupera a assinatura mais recente do conteúdo de documento.
	 * 
	 * @param conteudo o conteúdo cuja assinatura se pretende recuperar
	 * @return a assinatura mais recente do conteúdo dado, ou null, se ele não tiver assinaturas vinculadas
	 */
	public ProcessoDocumentoBinPessoaAssinatura getUltimaAssinaturaDocumento(ProcessoDocumentoBin conteudo) {
	    Search s = new Search(ProcessoDocumentoBinPessoaAssinatura.class);
	    addCriteria(s, 
	    		Criteria.equals("processoDocumentoBin", conteudo));
	    s.setMax(1);
	    s.addOrder("o.dataAssinatura", Order.DESC);
	    List<ProcessoDocumentoBinPessoaAssinatura> list = list(s);
		return list.isEmpty() ? null : list.get(0);
	}

	/**
	 * Recupera a assinatura mais antiga do conteúdo de documento.
	 * 
	 * @param conteudo o conteúdo cuja assinatura se pretende recuperar
	 * @return a assinatura mais antiga do conteúdo dado, ou null, se ele não tiver assinaturas vinculadas
	 */
	public ProcessoDocumentoBinPessoaAssinatura getPrimeiraAssinaturaDocumento(ProcessoDocumentoBin conteudo) {
	    Search s = new Search(ProcessoDocumentoBinPessoaAssinatura.class);
	    addCriteria(s, 
	    		Criteria.equals("processoDocumentoBin", conteudo));
	    s.setMax(1);
	    s.addOrder("o.dataAssinatura", Order.ASC);
	    List<ProcessoDocumentoBinPessoaAssinatura> list = list(s);
		return list.isEmpty() ? null : list.get(0);
	}

	/**
	 * Recupera a data da assinatura mais antiga do conteúdo de documento.
	 * 
	 * @param conteudo o conteúdo cuja assinatura se pretende recuperar
	 * @return a data da assinatura mais antiga do conteúdo dado, ou null, se ele não tiver assinaturas vinculadas
	 */
	public Date getDataPrimeiraAssinaturaDocumento(ProcessoDocumentoBin conteudo) {
		ProcessoDocumentoBinPessoaAssinatura assinatura = getPrimeiraAssinaturaDocumento(conteudo);
		return assinatura != null ? assinatura.getDataAssinatura() : null;
	}

	public Integer countDocumentoAcordaoAssinado(ProcessoTrf processoTrf, SessaoJT sessao, Voto voto){
		if(sessao == null || processoTrf == null || voto == null){
			return null;
		}
		return processoDocumentoBinPessoaAssinaturaDAO.countDocumentoAcordaoAssinado(processoTrf, sessao, voto);
	}
	
	public String getNomeUsuarioUltimaAssinatura(int idProcessoDocumentoBin) {
		return processoDocumentoBinPessoaAssinaturaDAO.getNomeUsuarioUltimaAssinatura(idProcessoDocumentoBin);
	}		
	
	public List<Pessoa> listaPessoasAssinaramDocumento(ProcessoDocumentoBin processoDocumentoBin) {
		if(processoDocumentoBin == null){
			return null;
		}
	    Search s = new Search(ProcessoDocumentoBinPessoaAssinatura.class);
	    addCriteria(s, Criteria.equals("processoDocumentoBin", processoDocumentoBin));
		return list(s);
	}		

	public boolean temDocumentoNaoAssinadoPorTipo(ProcessoTrf processoTrf, TipoProcessoDocumento tipoProcessoDocumento){
		return processoDocumentoBinPessoaAssinaturaDAO.countDocumentoNaoAssinadoPorTipo(processoTrf, tipoProcessoDocumento) > 0;
	}

	/**
	 * Verifica se há assinatura para o documento provido
	 * @param processoDocumento 
	 * @return true se o documento está assinado, false se o documento não está assinado
	 */
	public boolean isDocumentoAssinado(ProcessoDocumento processoDocumento){
		return processoDocumentoBinPessoaAssinaturaDAO.verificaSeTemAssinatura(
				processoDocumento.getProcessoDocumentoBin());
	}
	
	/**
	 * Recebe um processoDocumento e um usuario e verifica se a pessoa informada já assinou o documento
	 * 
	 * @param processoDocumento
	 * @param usuario
	 * @return boolean
	 */
	public boolean verificaUsuarioAssinouDocumento(ProcessoDocumento processoDocumento, Usuario usuario) {
		boolean retorno = Boolean.FALSE;
		if(processoDocumento != null && usuario != null) {
			List<Pessoa> pessoasAssinaramDocumento = this.listaPessoasAssinaramDocumento(processoDocumento.getProcessoDocumentoBin());
			for (Pessoa pessoa : pessoasAssinaramDocumento) {
				if(pessoa.getIdPessoa() == usuario.getIdUsuario()) {
					retorno = Boolean.TRUE;
					break;
				}
			}
		}
		return retorno;
	}

}
