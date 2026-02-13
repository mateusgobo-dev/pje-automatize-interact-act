/**
 *  pje-web
 *  Copyright (C) 2013 Conselho Nacional de Justiça
 *
 *  A propriedade intelectual deste programa, tanto quanto a seu código-fonte
 *  quanto a derivação compilada é propriedade da União Federal, dependendo
 *  o uso parcial ou total de autorização expressa do Conselho Nacional de Justiça.
 * 
 */
package br.jus.cnj.pje.entidades.listeners;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.PaisManager;
import br.jus.cnj.pje.nucleo.manager.PessoaDocumentoIdentificacaoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaListenerComponente;
import br.jus.cnj.pje.nucleo.manager.TipoDocumentoIdentificacaoManager;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Pais;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.TipoDocumentoIdentificacao;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * Classe responsável por monitorar eventos JPA relevantes para a gravação
 * e recuperação de {@link Pessoa}.
 * 
 * Sua criação se dá principalmente em razão da necessidade de retirar da entidade
 * {@link Pessoa} a dependência a classes e métodos que não fazem parte da natureza
 * da entidade, em especial aqueles relativos à inclusão, atualização ou exclusão
 * de documentos de identificação ({@link PessoaDocumentoIdentificacao}).
 * 
 * A vinculação dessa classe com os eventos do ciclo de vida JPA deve ser feita por
 * meio do arquivo META-INF/orm.xml, que deverá conter a seguinte definição:
 * 
 * <pre>
 * {@code
 * 	<entity class="br.jus.pje.nucleo.entidades.Pessoa">
 * 		<entity-listeners>
 * 			<entity-listener class="br.jus.cnj.pje.entidades.listeners.PessoaListener">
 * 				<pre-persist method-name="prePersist"/>
 * 				<pre-update method-name="preUpdate"/>
 * 				<post-load method-name="postLoad"/>
 * 			</entity-listener>
 * 		</entity-listeners>
 * 	</entity>
 * }
 * </pre>
 * @author cristof
 * @since 1.6.0
 */
public class PessoaListener {
	
	private static final Logger logger = LoggerFactory.getLogger(PessoaListener.class);
	
	private static final String[][] camposPF = {{"numeroCPF", "CPF"}, {"numeroPassaporte","PAS"}, {"numeroTituloEleitor", "TIT"}};
	
	private static final String[][] camposPJ = {{"numeroCNPJ", "CPJ"},{"numeroRegistroJuntaComercial", "RJC"}};
		
	private static Pais brasil;
	
	
	/**
	 * Método de inicialização que permite recuperar os componentes de escopo de evento responsáveis
	 * pelas cargas preliminares.
	 */
	private void init(){
		if(brasil == null){
			PaisManager paisManager = (PaisManager) Component.getInstance("paisManager", ScopeType.EVENT);
			try {
				brasil = paisManager.recuperaBrasil();
			} catch (PJeBusinessException e) {
				logger.error("Houve um erro ao tentar recuperar o país Brasil. Por favor, verifique a carga de dados de países.");
			}
		}
	}
	
	/**
	 * Método responsável por criar os documentos principais de uma pessoa física ou jurídica
	 * a partir dos dados efêmeros existentes no objeto.
	 * 
	 * @param pessoa a pessoa que está a ponto de ser persistida.
	 */
	public void prePersist(Pessoa pessoa){
		init();
		if(pessoa.getNome() != null) {
			pessoa.setNome(StringUtil.limparCaracteresEntreStrings(pessoa.getNome()));
		}
		if(PessoaFisica.class.isAssignableFrom(pessoa.getClass()) && ((PessoaFisica)pessoa).getNomeSocial() != null) {
			((PessoaFisica)pessoa).setNomeSocial(StringUtil.limparCaracteresEntreStrings(((PessoaFisica)pessoa).getNomeSocial()));
		}
		atualizaDocumentos(pessoa, false);
	}
	
	/**
	 * Método responsável por criar os documentos principais de uma pessoa física ou jurídica
	 * a partir dos dados efêmeros existentes no objeto.
	 * 
	 * @param pessoa a pessoa que está a ponto de ser atualizada.
	 */
	public void preUpdate(Pessoa pessoa){
		init();
		if(pessoa.getNome() != null) {
			pessoa.setNome(StringUtil.limparCaracteresEntreStrings(pessoa.getNome()));
		}
		if(PessoaFisica.class.isAssignableFrom(pessoa.getClass()) && ((PessoaFisica)pessoa).getNomeSocial() != null) {
			((PessoaFisica)pessoa).setNomeSocial(StringUtil.limparCaracteresEntreStrings(((PessoaFisica)pessoa).getNomeSocial()));
		}
		atualizaDocumentos(pessoa, true);
	}
	
	public void postUpdate(Pessoa pessoa) {
		PessoaDocumentoIdentificacaoManager pdim = (PessoaDocumentoIdentificacaoManager) Component.getInstance("pessoaDocumentoIdentificacaoManager");
		pdim.atualizarNomePessoaDocumento(pessoa);
	}
	
	/**
	 * Método responsável por carregar os documentos de uma pessoa física ou jurídica
	 * nos dados efêmeros existentes no objeto.
	 * 
	 * @param pessoa a pessoa que acabou de ser recuperada.
	 */
	public void postLoad(Pessoa pessoa){
		init();
		Class<?> clazz = pessoa.getClass();
		if(PessoaFisica.class.isAssignableFrom(clazz)){
			carregaDadosEfemeros((PessoaFisica) pessoa);
		}else if(PessoaJuridica.class.isAssignableFrom(clazz)){
			carregaDadosEfemeros((PessoaJuridica) pessoa);
		}
	}
	
	/**
	 * Atualiza dos documentos de uma dada pessoa a partir dos dados efêmeros
	 * que estão presentes no objeto.
	 * 
	 * @param pessoa a pessoa cujos documentos devem ser atualizados
	 * @param gerenciada marca indicativa de que a entidade já está gerenciada pelo JPA
	 */
	private void atualizaDocumentos(Pessoa pessoa, boolean gerenciada){
		Class<?> clazz = pessoa.getClass();
		if(PessoaFisica.class.isAssignableFrom(clazz)){
			atualizaDocumentos((PessoaFisica) pessoa, gerenciada);
		}else if(PessoaJuridica.class.isAssignableFrom(clazz)){
			atualizaDocumentos((PessoaJuridica) pessoa, gerenciada);
		}
	}

	/**
	 * Atualiza dos documentos de uma dada pessoa a partir dos dados efêmeros
	 * que estão presentes no objeto.
	 * 
	 * @param pessoa a pessoa cujos documentos devem ser atualizados
	 * @param gerenciada marca indicativa de que a entidade já está gerenciada pelo JPA
	 */
	private void atualizaDocumentos(PessoaFisica pessoa, boolean gerenciada){
		processaDocumentos(pessoa, camposPF, gerenciada);
	}
	
	/**
	 * Atualiza dos documentos de uma dada pessoa a partir dos dados efêmeros
	 * que estão presentes no objeto.
	 * 
	 * @param pessoa a pessoa cujos documentos devem ser atualizados
	 * @param gerenciada marca indicativa de que a entidade já está gerenciada pelo JPA
	 */
	private void atualizaDocumentos(PessoaJuridica pessoa, boolean gerenciada){
		processaDocumentos(pessoa, camposPJ, gerenciada);
	}
	
	/**
	 * Carrega, na pessoa dada, os dados efêmeros a ela pertencentes a partir de seus
	 * documentos de identificação.
	 * 
	 * @param pessoa a pessoa cujos dados devem ser carregados
	 */
	private void carregaDadosEfemeros(PessoaFisica pessoa){
		preencheDocumentos(pessoa, camposPF);
	}
	
	/**
	 * Carrega, na pessoa dada, os dados efêmeros a ela pertencentes a partir de seus
	 * documentos de identificação.
	 * 
	 * @param pessoa a pessoa cujos dados devem ser carregados
	 */
	private void carregaDadosEfemeros(PessoaJuridica pessoa){
		preencheDocumentos(pessoa, camposPJ);
	}
	
	/**
	 * Processa as informações de documentos no objeto dado por meio de reflexão dos campos
	 * das entidades.
	 * 
	 * @param pessoa a pessoa que deverá ter seus dados efêmeros avaliados para armazenamento dos documentos
	 * @param campos par de nome de campo efêmero da entidade e de sigla de tipo de documento que deverá ser 
	 * a ele associado. 
	 * @param gerenciada marca indicativa de que a entidade já está gerenciada pelo JPA
	 */
	private void processaDocumentos(final Pessoa pessoa, final String[][] campos, final boolean gerenciada){
		TipoDocumentoIdentificacao tipo = null;
		PessoaDocumentoIdentificacao doc = null;
		
		PessoaDocumentoIdentificacaoManager pdim = (PessoaDocumentoIdentificacaoManager) Component.getInstance("pessoaDocumentoIdentificacaoManager");
		TipoDocumentoIdentificacaoManager tdim = (TipoDocumentoIdentificacaoManager) Component.getInstance("tipoDocumentoIdentificacaoManager");
		
		for(String[] f: campos){
			try{
				String value = BeanUtils.getSimpleProperty(pessoa, f[0]);
				boolean cpf = f[1].equalsIgnoreCase("CPF");
				boolean tel = f[1].equalsIgnoreCase("TIT");
				boolean cnpj = f[1].equalsIgnoreCase("CPJ");
				boolean rjc = f[1].equalsIgnoreCase("RJC");
				if(value != null && !value.isEmpty()){
					tipo = tdim.findById(f[1]);
					doc = pdim.recuperaDocumento(value, tipo);
					if(doc == null && tipo != null){
						if(cpf){
							trataDocumento(pessoa, tipo, value, "Secretaria da Receita Federal do Brasil", null, null, true, true, gerenciada, pdim);
						}else if(cnpj){
							trataDocumento(pessoa, tipo, value, "Secretaria da Receita Federal do Brasil", null, null, true, false, gerenciada, pdim);
						}else if(tel){
							trataDocumento(pessoa, tipo, value, "Justiça Eleitoral", null, null, false, true, gerenciada, pdim);
						}else if(rjc){
							Estado estado = ((PessoaJuridica) pessoa).getUfJuntaComercial();
							String expedidor = "Órgão do Registro de Comércio de(a) ";
							
							expedidor += ((expedidor+estado.getEstado()).length() < 50 ? estado.getEstado() : estado.getCodEstado() );
							
							trataDocumento(pessoa, tipo, value, expedidor, null, estado, false, false, gerenciada, pdim);
						}else{
							trataDocumento(pessoa, tipo, value, null, null, null, false, false, gerenciada, pdim);
						}
					}
				}
			}catch (PJeBusinessException e){
				logger.error("Erro negocial ao tentar manter os documentos identificadores de [" + pessoa.getNome() + "]: " + e.getLocalizedMessage());
			}catch (Throwable e){
				logger.error("Erro ao tentar manter os documentos identificadores de [" + pessoa.getNome() + "]: " + e.getLocalizedMessage());
			}
		}
	}
	
	/**
	 * Cria um documento de identificação vinculado a uma dada pessoa a partir dos dados informados.
	 * 
	 * @param pessoa a pessoa a quem será vinculado o documento
	 * @param tipo o tipo de documento
	 * @param numeroDocumento o número do documento
	 * @param expedidor o nome do ente que expediu o documento
	 * @param dataExpedicao a data de expedição do documento
	 * @param estadoOrigem o {@link Estado} que emitiu o documento
	 * @param principal marca indicativa de que se trata de documento principal
	 * @param desativarDemais marca indicativa de que os demais documentos do mesmo tipo devem ser inativados
	 * @param gerenciada marca indicativa de que a entidade já está gerenciada pelo JPA
	 * @throws PJeBusinessException
	 */
	private void trataDocumento(Pessoa pessoa, TipoDocumentoIdentificacao tipo, 
			String numeroDocumento, String expedidor, Date dataExpedicao, Estado estadoOrigem, 
			boolean principal, boolean desativarDemais, boolean gerenciada, PessoaDocumentoIdentificacaoManager pdim) throws PJeBusinessException{
		if(desativarDemais && gerenciada){
			List<PessoaDocumentoIdentificacao> docs = pdim.recuperaDocumentos(pessoa, tipo, false);
			if(!docs.isEmpty()){
				for(PessoaDocumentoIdentificacao doc: docs){
					doc.setAtivo(false);
				}
			}
		}
		PessoaDocumentoIdentificacao doc = pdim.preparaDocumentoIdentificador(pessoa, numeroDocumento, tipo, principal, dataExpedicao, expedidor, brasil);
		if(estadoOrigem != null){
			doc.setEstado(estadoOrigem);
		}
		doc.setPessoa(pessoa);
		doc.setNomeUsuarioLogin(pessoa.getNome());
		if(gerenciada){
			/** Verifica se o documento já existe */
			List<PessoaDocumentoIdentificacao> documentos = pdim.recuperaDocumentos(numeroDocumento, tipo, false);
			if( documentos == null || documentos.isEmpty() ) {
				pdim.persist(doc);
			}
		}else{
			pessoa.getPessoaDocumentoIdentificacaoList().add(doc);
		}
	}
	
	/**
	 * Carrega, nos campos efêmeros listados, os dados do primeiro documento ativo e 
	 * não usado falsamente do tipo dado vinculado à pessoa.
	 * 
	 * @param pessoa a pessoa cujos campos serão preenchidos
	 * @param campos par de campo e sigla do tipo de documento
	 */
	private void preencheDocumentos(final Pessoa pessoa, final String[][] campos){
		Collection<PessoaDocumentoIdentificacao> documentosPessoa = null;
		if (isAcessivelPelaEntidade(pessoa)){
			documentosPessoa = pessoa.getPessoaDocumentoIdentificacaoList();
			logger.debug("A própria entidade da Pessoa foi utilizada para obter seus documentos: " + pessoa);
		}else{
			PessoaListenerComponente pessoaLstnrManager = (PessoaListenerComponente) Component.getInstance(PessoaListenerComponente.NAME, ScopeType.EVENT);
			documentosPessoa = pessoaLstnrManager.recuperarDocumentosPessoa(pessoa);
			logger.debug("Documentos da pessoa {0} obtidos da base de dados: " + pessoa);
		}
		preencherLocalmente(pessoa, campos, documentosPessoa);
	}

	
	private boolean isAcessivelPelaEntidade(Pessoa pessoa){
		boolean retorno= false;
		try {
			if (pessoa.getPessoaDocumentoIdentificacaoList().size()>0){
				retorno = true;
			}
		} catch (Exception e) {
			logger.debug("Lazy capturado?"+ e.getMessage());
		}
		return retorno;
	}
	
	private void preencherLocalmente(final Pessoa pessoa, final String[][] campos, final Collection<PessoaDocumentoIdentificacao> docs){
		for (PessoaDocumentoIdentificacao pessoaDocumentoIdentificacao : docs) {
			if (!pessoaDocumentoIdentificacao.getAtivo() || pessoaDocumentoIdentificacao.getUsadoFalsamente()){
				continue;
			}
			TipoDocumentoIdentificacao tipo = pessoaDocumentoIdentificacao.getTipoDocumento();
			assert (tipo!=null) : "O tipo do documento nunca deveria ser nulo...";
			
			if("RJC".equals(tipo.getCodTipo())){
				((PessoaJuridica)pessoa).setUfJuntaComercial(pessoaDocumentoIdentificacao.getEstado());
				continue;
			}
			
			for(String[] campo: campos){
				if(campo[1].equals(tipo.getCodTipo())){
					try {
						if(BeanUtils.getProperty(pessoa, campo[0]) == null){ //TODO Chamar BeanUtils.getProperty/setProperty aqui é muito penoso para a performance... ao invés, deveria usar diretamente os métodos ou cachear os métodos de interesse.
							PessoaDocumentoIdentificacao doc = pessoaDocumentoIdentificacao;
							BeanUtils.setProperty(pessoa, campo[0], doc.getNumeroDocumento());
							
						}
					} catch (Exception e) {
						logger.error("N\u00e3o foi poss\u00edvel acessar a propriedade {} da pessoa {}. Erro: {}", new Object[]{campo[0], pessoa.getLogin(), e});
					} 
					break;
				}
			
			}
		}
	}
	

}
