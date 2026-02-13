package br.jus.cnj.pje.nucleo.manager;

import java.lang.reflect.ParameterizedType;
import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.In;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.certificado.CertificadoException;
import br.com.infox.core.certificado.util.VerificaCertificadoPessoa;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.business.dao.ProcessoExpedienteCriminalDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoExpedienteCriminal;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.enums.SituacaoExpedienteCriminalEnum;
import br.jus.pje.nucleo.enums.TipoExpedienteCriminalEnum;

public abstract class ProcessoExpedienteCriminalManager<T extends ProcessoExpedienteCriminal, D extends ProcessoExpedienteCriminalDAO<T>> extends BaseManager<T>{
	
	public static final String MARCACAO_NUMERO_EXPEDIENTE_REPLACE = "||NUMERO||";
	public static final String MARCACAO_MAGISTRADO_REPLACE = "||MAGISTRADO||";

	@In
	private DocumentoJudicialService documentoJudicialService;

	@Override
	protected abstract D getDAO();
	
	protected abstract void setarSituacaoExpediente(T expediente);

	@SuppressWarnings("unchecked")
	public Class<T> getEntityClass(){
		Class<?> clazz = this.getClass();
		if (!ParameterizedType.class.isAssignableFrom(clazz.getGenericSuperclass().getClass())){
			clazz = clazz.getSuperclass();
		}
		return (Class<T>) ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[0];
	}

	public T getExpediente(){
		T pe = null;
		try{
			pe = getEntityClass().newInstance();
			pe.setDtCriacao(new Date());
			// pe.setCheckado(false);
			// pe.setUrgencia(false);
		} catch (InstantiationException e){
			e.printStackTrace();
		} catch (IllegalAccessException e){
			e.printStackTrace();
		}

		return pe;
	}

	public T getExpediente(ProcessoTrf processo){
		T pe = this.getExpediente();
		pe.setProcessoTrf(processo);
		return pe;
	}

	protected Integer gerarNumero(Integer idProcessoTrf){
		return getDAO().gerarNumeroExpediente(idProcessoTrf);
	}

	@Override
	public T persist(T entity) throws PJeBusinessException{
		// RN 169
		if (entity.getProcessoDocumento() == null){
			throw new PJeBusinessException("pje.processoExpedienteCriminalManager.error.documentoNaoInformado");
		}

		if (entity.getProcessoDocumento().getProcessoDocumentoBin() == null){
			throw new PJeBusinessException("pje.processoExpedienteCriminalManager.error.documentoBinarioNaoInformado");
		}

		if (entity.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento() == null
			|| entity.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento().trim().equals("")){
			throw new PJeBusinessException("pje.processoExpedienteCriminalManager.error.textoNaoInformado");
		}

		if (entity.getPessoa() == null){
			throw new PJeBusinessException("pje.processoExpedienteCriminalManager.error.pessoaNaoInformada");
		}
		
		// se inserindo, gera o numero do expediente
		if (entity.getId() == null){
			entity.setNumero(getDAO().gerarNumeroExpediente(entity.getProcessoTrf().getIdProcessoTrf()));			
		}
		
		//substitui a marcacao ||NUMERO|| do texto do expediente pelo numero gerado
		substituirMarcacaoNumeroExpediente(entity);
		
		if(entity.getNumero() == null){
			throw new PJeBusinessException("pje.processoExpedienteCriminalManager.error.numeroNaoInformado");
		}
		
		entity.getProcessoDocumento().setDocumentoSigiloso(entity.getInSigiloso());		

		return super.persist(entity);
	}

	public T persist(T pe, ProcessoTrf processo, Long jbpmTask) throws PJeBusinessException, CertificadoException{
		ProcessoDocumento pd = documentoJudicialService.persist(pe.getProcessoDocumento(), processo, true);

		pe.setTipoProcessoDocumento(pe.getProcessoDocumento().getTipoProcessoDocumento());
		pe.setProcessoDocumento(pd);
		pe.setProcessoTrf(processo);
		setarSituacaoExpediente(pe);
		return persist(pe);
	}

	public String documentosParaAssinatura(List<T> expedientes){
		StringBuilder sb = new StringBuilder();
		int i;
		for (i = 0; i < (expedientes.size() - 1); i++){
			sb.append(expedientes.get(i).getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento());
			sb.append("[[[[----SEP_DOC---]]]]");
		}

		if (!expedientes.isEmpty()){
			sb.append(expedientes.get(i).getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento());
		}
		return sb.toString();
	}

	public TipoProcessoDocumento getTipoProcessoDocumento(){
		return ParametroUtil.instance().getTipoProcessoDocumentoExpediente();
	}

	public List<T> recuperarExpedientesNaoAssinados(ProcessoTrf processoTrf){
		return getDAO().recuperarExpedientesNaoAssinados(processoTrf);
	}

	public T adicionarAssinatura(T expediente, String assinatura, String encodedCertChain){

		if (expediente.getProcessoDocumento().getProcessoDocumentoBin().getCertChain() == null){
			expediente.getProcessoDocumento().getProcessoDocumentoBin().setCertChain(encodedCertChain);
			expediente.getProcessoDocumento().getProcessoDocumentoBin().setSignature(assinatura);
		}

		return expediente;
	}

	public T assinarExpedienteGravar(T expediente, String assinatura, String encodedCertChain, Long jbpmTask)
			throws PJeBusinessException{
		try{
			// verificando se o usuário logado é o mesmo do token
			VerificaCertificadoPessoa.verificaCertificadoPessoaLogada(encodedCertChain);

			if (!(Pessoa.instanceOf(Authenticator.getUsuarioLogado(), PessoaMagistrado.class))){
				throw new PJeBusinessException("Apenas magistrados podem assinar expedientes criminais");
			}

			// gravando novamente, pois o magistrado pode ter alterado o
			// expediente antes de assiná-lo
			expediente = persist(expediente, expediente.getProcessoTrf(), jbpmTask);

			T assinado = adicionarAssinatura(expediente, assinatura, encodedCertChain);
			assinado.setPessoaMagistrado(((PessoaFisica) Authenticator.getUsuarioLogado()).getPessoaMagistrado());
			documentoJudicialService.finalizaDocumento(assinado.getProcessoDocumento(), assinado.getProcessoTrf(), jbpmTask, true, true);
			return assinado;
		} catch (CertificadoException e){
			throw new PJeBusinessException(e);
		}
	}

	public void copiarExpediente(T origem, List<T> destinos) throws PJeBusinessException{
		if (origem != null && destinos != null){
			for (T aux : destinos){
				// se for diferente da origem e se estiver inserindo, copie.
				if (!aux.equals(origem) && aux.getId() == null){
					try{
						aux = EntityUtil.cloneEntity(origem, false);
					} catch (InstantiationException e){
						throw new PJeBusinessException("pje.processoExpedienteCriminalManager.error.copiarExpedienteCriminal");
					} catch (IllegalAccessException e){
						throw new PJeBusinessException("pje.processoExpedienteCriminalManager.error.copiarExpedienteCriminal");
					}
				}
			}
		}
	}

	public List<ProcessoExpedienteCriminal> pesquisarExpedientesCriminais(
			TipoExpedienteCriminalEnum tipoExpedienteCriminal, String numeroProcesso, String nomePessoa,
			String documentoPessoa, Date dtInicio, Date dtTermino, SituacaoExpedienteCriminalEnum situacaoExpediente){

		return getDAO().pesquisarExpedientesCriminais(tipoExpedienteCriminal, numeroProcesso, nomePessoa,
				documentoPessoa, dtInicio, dtTermino, situacaoExpediente);
	}
	
	public void substituirMarcacaoNumeroExpediente(T entity){
		if(entity != null && entity.getNumeroExpediente() != null){
			String textoNumero = entity.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento()
					.replace(MARCACAO_NUMERO_EXPEDIENTE_REPLACE, entity.getNumeroExpediente());
			entity.getProcessoDocumento().getProcessoDocumentoBin().setModeloDocumento(textoNumero);
		}
	}

	public void cancelarExpediente(T entity) throws PJeBusinessException {
		if(entity.getSituacaoExpedienteCriminal() == SituacaoExpedienteCriminalEnum.PC){
			throw new PJeBusinessException("pje.preparaExpedienteCriminalManager.error.exclusaoNaoPermitida",null,"Situação do expediente igual a '"+SituacaoExpedienteCriminalEnum.PC.getLabel()+"'");
		}
		
		if(entity.getSituacaoExpedienteCriminal() == SituacaoExpedienteCriminalEnum.CP){
			throw new PJeBusinessException("pje.preparaExpedienteCriminalManager.error.exclusaoNaoPermitida",null,"Situação do expediente igual a '"+SituacaoExpedienteCriminalEnum.CP.getLabel()+"'");
		}
		
		if(entity.getSituacaoExpedienteCriminal() == SituacaoExpedienteCriminalEnum.CA){
			throw new PJeBusinessException("pje.preparaExpedienteCriminalManager.error.exclusaoNaoPermitida",null,"Situação do expediente igual a '"+SituacaoExpedienteCriminalEnum.CA.getLabel()+"'");
		}
		
		if(entity.getSituacaoExpedienteCriminal() == SituacaoExpedienteCriminalEnum.RV){
			throw new PJeBusinessException("pje.preparaExpedienteCriminalManager.error.exclusaoNaoPermitida",null,"Situação do expediente igual a '"+SituacaoExpedienteCriminalEnum.RV.getLabel()+"'");
		}		
		
		entity.setSituacaoExpedienteCriminal(SituacaoExpedienteCriminalEnum.CA);
		super.persist(entity);
	}
}
