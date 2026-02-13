package br.com.infox.cliente.home;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;

import br.com.infox.core.certificado.CertificadoException;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.service.AssinaturaDocumentoService;
import br.com.itx.component.grid.ProcessoDocumentoGridQuery;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.FacesUtil;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBinPessoaAssinatura;
import br.jus.pje.nucleo.util.StringUtil;

@Name("processoDocumentoBinPessoaAssinaturaHome")
public class ProcessoDocumentoBinPessoaAssinaturaHome extends AbstractProcessoDocumentoBinPessoaAssinaturaHome<ProcessoDocumentoBinPessoaAssinatura> {

	private static final long serialVersionUID = 1L;
	
	@In	protected AssinaturaDocumentoService assinaturaDocumentoService;
	
	private Map<String,Boolean> listaIdsAssinaturas = new HashMap<String,Boolean>();
	public static ProcessoDocumentoBinPessoaAssinaturaHome instance() {
		return ComponentUtil.getComponent("processoDocumentoBinPessoaAssinaturaHome");
	}
	
	@SuppressWarnings("unchecked")
	public Boolean listaAssinatura(String idProcessoDocumentoBin){
		if(!listaIdsAssinaturas.containsKey(idProcessoDocumentoBin)){
			List<Integer> lista = (List<Integer>)Contexts.getConversationContext().get(ProcessoDocumentoGridQuery.MAPA_PDB);
			Boolean contextual = lista != null && lista.size() >0;
			if(!idProcessoDocumentoBin.isEmpty()){
					EntityManager em = getEntityManager();
					StringBuilder sqlPes = new StringBuilder();
					sqlPes.append(" select o.processoDocumentoBin.idProcessoDocumentoBin from ");
					sqlPes.append(" ProcessoDocumentoBinPessoaAssinatura o");
					if(contextual){
						sqlPes.append(" where o.processoDocumentoBin.idProcessoDocumentoBin in (:id) and o.assinatura <> ''");
					}
					else{
						sqlPes.append(" where o.processoDocumentoBin.idProcessoDocumentoBin = :id and o.assinatura <> ''");
					}
					Query query = em.createQuery(sqlPes.toString());	
					query.setParameter("id", contextual ? lista : Integer.parseInt(idProcessoDocumentoBin));
					List<Integer>  list = (List<Integer>) query.getResultList();
					if(contextual){
						for(Integer id : lista){
							listaIdsAssinaturas.put(id.toString(),list.contains(id));
						}
					}
					else{
						for(Integer id : list){
							listaIdsAssinaturas.put(id.toString(),true);
						}
					}
				
			}
		}
		return listaIdsAssinaturas.get(idProcessoDocumentoBin); 
	}
	
	public Date dataPrimeiraAssinatura(String idProcessoDocumentoBin) {
		String query = "SELECT assinatura.dataAssinatura FROM ProcessoDocumentoBinPessoaAssinatura assinatura WHERE assinatura.processoDocumentoBin.idProcessoDocumentoBin = :id ORDER BY assinatura.dataAssinatura ASC";
		EntityManager em = getEntityManager();
		Query q = em.createQuery(query);
		q.setMaxResults(1); //como o "LIMIT 1" no hql nao estava funcionando, foi necessario ajustar aqui.
		q.setParameter("id", Integer.parseInt(idProcessoDocumentoBin));
		try{
			return (Date) q.getSingleResult();
		}catch (NoResultException e){
			return null;
		}
	}
	
	public void gravarAssinatura(ProcessoDocumento processoDocumento, String certChain, String signature) throws CertificadoException {
		assinaturaDocumentoService.assinarDocumento(processoDocumento.getProcessoDocumentoBin(), signature, certChain);
		processoDocumento.setPapel(Authenticator.getPapelAtual());
		EntityUtil.getEntityManager().merge(processoDocumento);
		EntityUtil.getEntityManager().flush();
	}

	public void limpar(){
		listaIdsAssinaturas = new HashMap<String,Boolean>();
	}

	public Map<String, Boolean> getListaIdsAssinaturas() {
		return listaIdsAssinaturas;
	}
	
	/**
	 * Verifica se é possível exibir os dados de juntada de um documento.
	 * Basicamente verifica se um documento foi assinado.
	 * 
	 * @param	processoDocumento  trata-se do documento vinculado ao processo
	 * @return	verdadeiro se o documento vinculado ao processo fizer parte dos documentos assinados digitalmente
	 */
	public Boolean podeExibirDadosDeJuntada(ProcessoDocumento processoDocumento){
		Boolean retorno = null;
		if (processoDocumento != null && processoDocumento.getProcessoDocumentoBin() != null) {
			retorno = listaAssinatura(String.valueOf(processoDocumento.getProcessoDocumentoBin().getIdProcessoDocumentoBin()));
		}
		return retorno == null ? false : retorno;
	}
	
	/**
	 * Recupera o nome para exibição da pessoa responsável pela juntada do documento no processo.
	 * 
	 * Caso existam, devem ser utilizados os campos NomeUsuarioJutada e LocalizacaoJuntada de ProcessoDocumento.
	 * Se não, a regra é recuperar o último usuário que fez a alteração no documento.
	 * 
	 * @link http://www.cnj.jus.br/jira/browse/PJEII-22393
	 * 
	 */
	public String recuperarNomeUsuarioJuntada(ProcessoDocumento processoDocumento) {
		String retorno = StringUtils.EMPTY;
		if (podeExibirDadosDeJuntada(processoDocumento)) {
			if(processoDocumento.getNomeUsuarioJuntada() != null){
				retorno = getNomeUsuarioJuntadaProcessoDocumento(processoDocumento);
			}else if (processoDocumento.getNomeUsuario() != null) {
				retorno = processoDocumento.getNomeUsuario().toUpperCase();
			} 
		} else {
			retorno = FacesUtil.getMessage("documentoProcesso.label.documento.nao.juntado");
		}
		return retorno;
	}

	/**
	 * Retorna a descrição do usuário que juntou o documento.
	 * Devem ser utilizados os campos NomeUsuarioJuntada e LocalizacaoJuntada de ProcessoDocumento.
	 * @param processoDocumento
	 * @return nomeUsuarioJuntadaProcessoDocumento
	 * @link http://www.cnj.jus.br/jira/browse/PJEII-22393
	 */
	private String getNomeUsuarioJuntadaProcessoDocumento(ProcessoDocumento processoDocumento) {
		StringBuilder descricao = new StringBuilder();
		if(processoDocumento.getNomeUsuarioJuntada() != null){
			descricao.append(processoDocumento.getNomeUsuarioJuntada());
		}
		if(processoDocumento.getLocalizacaoJuntada() != null){
			StringUtil.adicionarHifen(descricao);
			descricao.append(processoDocumento.getLocalizacaoJuntada());
		}
		return descricao.toString();
	}

	
}