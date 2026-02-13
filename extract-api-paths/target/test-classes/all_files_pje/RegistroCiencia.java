/**
 * 
 */
package br.jus.cnj.pje.controleprazos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.web.RequestParameter;

import br.com.infox.cliente.home.ProcessoDocumentoBinHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.service.AtoComunicacaoService;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;

/**
 * @author cristof
 * 
 */
@Name(RegistroCiencia.REGISTRO_CIENCIA)
@Scope(ScopeType.CONVERSATION)
public class RegistroCiencia implements Serializable {

	public static final String REGISTRO_CIENCIA = "registroCiencia";
	private static final long serialVersionUID = 1L;

	private Map<Integer, Integer> mapaExpedientesPendentes = new HashMap<Integer, Integer>();

	@In
	protected EntityManager entityManager;

	@In
	protected Usuario usuarioLogado;

	@In(create = true)
	protected TipoParte tipoParteAdvogado;
	
	@In
	private transient AtoComunicacaoService atoComunicacaoService;

	@RequestParameter
	protected Integer documentoId;
	
	@RequestParameter
	protected Integer idAto;

	@In(required = false)
	@Out(required = false)
	@DataModel("expedientesPendentes")
	protected Set<ProcessoParteExpediente> expedientes;

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	private int localizaExpedientesPendentes() {
		if (documentoId != null) {
			return localizaExpedientesPendentes(documentoId);
		}
		return 0;
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public boolean expedienteProprioNaoVisto(ProcessoParteExpediente ppe) {
		if (ppe.getDtCienciaParte() == null) {
			List<ProcessoParteExpediente> analisados = new ArrayList<ProcessoParteExpediente>(0);
			Pessoa pessoaLogada = (Pessoa) usuarioLogado;
			String select = "SELECT DISTINCT ppe " + "	FROM ProcessoParteExpediente ppe" +
			// "		LEFT JOIN ppe.processoParte.processoParteRepresentanteList reps "
			// +
					"	WHERE " + "		ppe = :ppe";
			// "		AND (ppe.processoParte.pessoa = :pessoa " +
			// "			OR (reps.tipoRepresentante = :tipoParteAdvogado AND reps.representante = :pessoa))";
			Query q = entityManager.createQuery(select);
			// q.setParameter("pessoa", pessoaLogada);
			// q.setParameter("tipoParteAdvogado",
			// ParametroUtil.instance().getTipoParteAdvogado());
			q.setParameter("ppe", ppe);
			@SuppressWarnings("unchecked")
			List<ProcessoParteExpediente> resp = q.getResultList();

			for (ProcessoParteExpediente ppee : resp) {
				if (usuarioLogadoIsParte(ppee) || usuarioLogadoIsAdvogado(ppee)) {
					analisados.add(ppee);
				}
			}

			return analisados.size() > 0 ? true : false;
		}
		return false;
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public int localizaExpedientesPendentes(Integer id) {

		if (mapaExpedientesPendentes.containsKey(id)) {
			return mapaExpedientesPendentes.get(id);
		}

		List<ProcessoParteExpediente> analisados = new ArrayList<ProcessoParteExpediente>(0);
		// Pessoa pessoaLogada = (Pessoa) usuarioLogado;
		String consultaStatusLeitura = "SELECT DISTINCT p "
				+ "	FROM ProcessoParteExpediente AS p "
				+
				// "		LEFT JOIN p.processoParte.processoParteRepresentanteList reps "
				// +
				"		LEFT JOIN p.processoExpediente.processoDocumentoExpedienteList docs " + "	WHERE "
				+ "		docs.anexo = false" + "		AND p.dtCienciaParte IS NULL "
				+ "		AND docs.processoDocumento.idProcessoDocumento = :processoDocumento ";
		// "		AND (p.processoParte.pessoa = :pessoa " +
		// "			OR (reps.tipoRepresentante = :tipoParteAdvogado AND reps.representante = :pessoa))";
		// Query q =
		// EntityUtil.getEntityManager().createQuery(consultaStatusLeitura);
		Query q = entityManager.createQuery(consultaStatusLeitura);
		// q.setParameter("pessoa", pessoaLogada);
		// q.setParameter("tipoParteAdvogado",
		// ParametroUtil.instance().getTipoParteAdvogado());
		q.setParameter("processoDocumento", id);

		@SuppressWarnings("unchecked")
		List<ProcessoParteExpediente> localizados = q.getResultList();

		// ** Fix Integração 2º Grau - Validar Dr. Paulo
		// TODO CNJ VERIFICAR PESO -
		for (ProcessoParteExpediente ppe : localizados) {
			if (usuarioLogadoIsParte(ppe) || usuarioLogadoIsAdvogado(ppe)) {
				analisados.add(ppe);
			}
		}

		if (expedientes == null) {
			expedientes = new HashSet<ProcessoParteExpediente>(analisados.size());
		}
		expedientes.clear();
		expedientes.addAll(analisados);
		mapaExpedientesPendentes.put(id, expedientes.size());
		return expedientes.size();
	}

	private boolean usuarioLogadoIsAdvogado(ProcessoParteExpediente ppe) {
		Pessoa pessoaLogada = (Pessoa) usuarioLogado;
		TipoParte tipoAdvogado = ParametroUtil.instance().getTipoParteAdvogado();

		for (ProcessoParte pp : ppe.getProcessoExpediente().getProcessoTrf().getProcessoParteAtivoList()) {
			for (ProcessoParteRepresentante ppr : pp.getProcessoParteRepresentanteList()) {
				if (ppr.getInSituacao() == ProcessoParteSituacaoEnum.A) {
					if (ppr.getTipoRepresentante().equals(tipoAdvogado)) {
						if (ppr.getRepresentante().equals(pessoaLogada)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	private boolean usuarioLogadoIsParte(ProcessoParteExpediente ppe) {
		Pessoa pessoaLogada = (Pessoa) usuarioLogado;
		for (ProcessoParte pp : ppe.getProcessoExpediente().getProcessoTrf().getProcessoParteAtivoList()) {
			if (pp.getPessoa().equals(pessoaLogada)) {
				return true;
			}
		}
		return false;
	}

	@Begin(join = true, flushMode = FlushModeType.AUTO)
	public void registraCiencia() throws PJeDAOException {
		if(idAto != null){
			atoComunicacaoService.registraCienciaPessoal(idAto);
		}
//		this.localizaExpedientesPendentes();
//		for (ProcessoParteExpediente ppe : expedientes) {
//			ProcessoParteExpedienteHome.instance().cienciaIntimacao(ppe);
//		}
//		expedientes = null;
		return;
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void registraCienciaBinario() throws PJeDAOException {
		this.registraCiencia();
		ProcessoDocumentoBinHome pdbh = (ProcessoDocumentoBinHome) Component.getInstance("processoDocumentoBinHome");
		pdbh.setDownloadInstance();
	}

}
