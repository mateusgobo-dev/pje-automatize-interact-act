package br.com.infox.cliente.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.persistence.CacheRetrieveMode;
import javax.persistence.CacheStoreMode;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.Util;
import br.com.infox.exceptions.NegocioException;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.util.CarregarParametrosAplicacao;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.manager.AplicacaoClasseManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJusticaManager;
import br.jus.cnj.pje.nucleo.manager.PapelManager;
import br.jus.cnj.pje.nucleo.manager.TipoParteManager;
import br.jus.cnj.pje.nucleo.manager.TipoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.csjt.pje.commons.util.ParametroJtUtil;
import br.jus.pje.jt.entidades.TipoSituacaoPauta;
import br.jus.pje.jt.entidades.TipoVotoJT;
import br.jus.pje.jt.enums.ValorPesoEnum;
import br.jus.pje.nucleo.entidades.Agrupamento;
import br.jus.pje.nucleo.entidades.AplicacaoClasse;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.Caixa;
import br.jus.pje.nucleo.entidades.ComplementoQualificacao;
import br.jus.pje.nucleo.entidades.EnderecoWsdl;
import br.jus.pje.nucleo.entidades.Escolaridade;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.Fluxo;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.ModeloDocumentoLocal;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.Parametro;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.Profissao;
import br.jus.pje.nucleo.entidades.Qualificacao;
import br.jus.pje.nucleo.entidades.RpvStatus;
import br.jus.pje.nucleo.entidades.Tarefa;
import br.jus.pje.nucleo.entidades.TipoModeloDocumento;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.entidades.TipoPessoa;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumentoTrf;
import br.jus.pje.nucleo.entidades.TipoResultadoDiligencia;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.entidades.lancadormovimento.OrgaoJustica;
import br.jus.pje.nucleo.enums.TipoEditorEnum;
import br.jus.pje.nucleo.enums.TipoPrazoEnum;
import br.jus.pje.nucleo.util.ArrayUtil;
import br.jus.pje.nucleo.util.PropertiesUtil;
import br.jus.pje.nucleo.util.StringUtil;

@Name(ParametroUtil.NAME)
@Scope(ScopeType.APPLICATION)
@Install(dependencies =  {
    CarregarParametrosAplicacao.NAME}
)
@Startup(depends = CarregarParametrosAplicacao.NAME)
@BypassInterceptors
public class ParametroUtil {
    public static final String NAME = "parametroUtil";
    public static Log log = Logging.getLog(ParametroUtil.class);
    public static final String CAIXA_INTIMACOES_AUTO_PENDENTES = "Intimações Automáticas com Pendências";

	public static Set<String> parametrosNaoConfigurados = Collections.synchronizedSet(new HashSet<>());

    public String getChaveCriptograficaGenerica(){
    	return getParametro("chaveCriptograficaGenerica");
    }

    @Factory(scope = ScopeType.APPLICATION)
    public String getChaveCriptografiaSimetrica(){
    	return getParametro(Parametros.CHAVE_VOLATIL_CRIPTOGRAFIA);
    }
    
    public Tarefa getDarCienciaPartesSREEO() {
        return getEntity(Tarefa.class, "idTarefaDarCienciaPartesSREEO");
    }

    @Factory(scope = ScopeType.EVENT)
    public Tarefa getDarCienciaPartes() {
        return getEntity(Tarefa.class, "idTarefaDarCienciaPartes");
    }

    @Factory(scope = ScopeType.EVENT)
    public Tarefa getTarefaDarCienciaPartes() {
        return getEntity(Tarefa.class, "idTarefaDarCienciaPartes");
    }

    public Integer getIdTarefaDarCienciaPartes() {
        return Integer.parseInt(getParametro("idTarefaDarCienciaPartes"));
    }

    @Factory(scope = ScopeType.EVENT)
	public Tarefa getTarefaAnaliseLiquidacao() {
		return getEntity(Tarefa.class, "idTarefaAnaliseLiquidacao");
	}
	
	@Factory(scope = ScopeType.EVENT)
	public Tarefa getTarefaAnaliseExecucao() {
		return getEntity(Tarefa.class, "idTarefaAnaliseExecucao");
	}

    @Factory(scope = ScopeType.EVENT)
    public String getNomeTarefaDarCienciaPartes() {
        Tarefa t = getTarefaDarCienciaPartes();

        return (t != null) ? t.getTarefa() : null;
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    public Tarefa getTarefaControlePrazo() {
        return getEntity(Tarefa.class, "idTarefaControlePrazo");
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    public Tarefa getTarefaControlePrazoSREEO() {
        return getEntity(Tarefa.class, "idTarefaControlePrazoSreeo");
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    public Tarefa getTarefaDarCienciaPartesSREEO() {
        return getEntity(Tarefa.class, "idTarefaDarCienciaPartesSREEO");
    }

    public Integer getIdTarefaDarCienciaPartesSREEO() {
        return Integer.parseInt(getParametro("idTarefaDarCienciaPartesSREEO"));
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    public Tarefa getTarefaSecretariaProcessante() {
        return getEntity(Tarefa.class, "idTarefaSecretariaProcessante");
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    public Tarefa getTarefaSecretariaSREEO() {
        return getEntity(Tarefa.class, "idTarefaSecretariaSreeo");
    }

    @Factory(scope = ScopeType.EVENT)
    public Caixa getCaixaIntimacaoAutoPendSREEO() {
        return getEntity(Caixa.class,
            "idCaixaIntimacoesAutomaticasPendenciaSREEO");
    }

    public String getIdCaixaIntimacaoAutoPendSREEO() {
        return getParametro("idCaixaIntimacoesAutomaticasPendenciaSREEO");
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    public String getNomeTarfeDarCienciaPartesSREEO() {
        Tarefa t = getTarefaDarCienciaPartesSREEO();

        return (t != null) ? t.getTarefa() : null;
    }

    public String getTasksToVerify() {
        String tasksToVerify = getNomeTarefaDarCienciaPartes();
        tasksToVerify += (";" + getNomeTarfeDarCienciaPartesSREEO());

        return tasksToVerify;
    }

    @Factory(scope = ScopeType.EVENT)
    public String getSecao() {
        return getParametro("secao");
    }

    @Factory(scope = ScopeType.EVENT)
    public Evento getEventoArquivamento() {
        return getEntity(Evento.class, "idEventoArquivamento");
    }

    @Factory(scope = ScopeType.EVENT)
    public Evento getEventoArquivamentoDefinitivo() {
        return getEntity(Evento.class,
            "idEventoArquivamentoDefinitivo");
    }

    @Factory(scope = ScopeType.EVENT)
    public Evento getEventoArquivamentoDefinitivoProcessual() {
        return getEntity(Evento.class,
            "idEventoArquivamentoDefinitivo");
    }

    @Factory(scope = ScopeType.EVENT)
    public Evento getEventoArquivamentoProvisorio() {
        return getEntity(Evento.class,
            "idEventoArquivamentoProvisorio");
    }

    @Factory(scope = ScopeType.EVENT)
    public Evento getEventoDistribuicaoProcessual() {
        return getEntity(Evento.class, "idEventoDistribuicao");
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    public Evento getEventoExtincaoPunibilidade() {
        return getEntity(Evento.class, "idEventoExtincaoPunibilidade");
    }

    @Factory(scope = ScopeType.EVENT)
    public Evento getEventoDecisao() {
        return getEntity(Evento.class, "idEventoDecisao");
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    public Evento getEventoSemResolucaoMerito() {
        return getEntity(Evento.class, "idEventoSemResolucaoMerito");
    }

    @Factory(scope = ScopeType.EVENT)
    public Evento getEventoProcessualRedistribuicao() {
        return getEntity(Evento.class, "idEventoRedistribuicao");
    }

    @Factory(scope = ScopeType.EVENT)
    public Evento getEventoProcessualDistribuicao() {
        return getEntity(Evento.class, "idEventoDistribuicao");
    }

    @Factory(scope = ScopeType.EVENT)
    public Evento getEventoBaixaDefinitiva() {
        return getEntity(Evento.class, "idEventoBaixaDefinitiva");
    }

    @Factory(scope = ScopeType.EVENT)
    public Evento getEventoBaixaDefinitivaProcessual() {
        return getEntity(Evento.class, "idEventoBaixaDefinitiva");
    }

    @Factory(scope = ScopeType.EVENT)
    public Evento getEventoRecebimentoProcessual() {
        return getEntity(Evento.class, "idEventoRecebimento");
    }

    @Factory(scope = ScopeType.EVENT)
    public Evento getEventoReativacao() {
        return getEntity(Evento.class, "idEventoReativacao");
    }

    @Factory(scope = ScopeType.EVENT)
    public Evento getEventoReativacaoProcessual() {
        return getEntity(Evento.class, "idEventoReativacao");
    }

    @Factory(scope = ScopeType.EVENT)
    public Evento getEventoMudancaClasseProcessual() {
        return getEntity(Evento.class,
            "idEventoMudancaClasseProcessual");
    }

    @Factory(scope = ScopeType.EVENT)
    public Evento getEventoSuspensaoDecisao() {
        return getEntity(Evento.class, "idEventoSuspensaoDecisao");
    }

    @Factory(scope = ScopeType.EVENT)
    public Evento getEventoSuspensaoDecisaoProcessual() {
        return getEntity(Evento.class, "idEventoSuspensaoDecisao");
    }

    @Factory(scope = ScopeType.EVENT)
    public Evento getEventoSuspensaoDespacho() {
        return getEntity(Evento.class, "idEventoSuspensaoDespacho");
    }

    @Factory(scope = ScopeType.EVENT)
    public Evento getEventoSuspensaoDespachoProcessual() {
        return getEntity(Evento.class, "idEventoSuspensaoDespacho");
    }

    @Factory(scope = ScopeType.EVENT)
    public Evento getEventoDesarquivamento() {
        return getEntity(Evento.class, "idEventoDesarquivamento");
    }

    @Factory(scope = ScopeType.EVENT)
    public Evento getEventoDesarquivamentoProcessual() {
        return getEntity(Evento.class, "idEventoDesarquivamento");
    }

    @Factory(scope = ScopeType.EVENT)
    public Evento getEventoJulgamento() {
        return getEntity(Evento.class, "idEventoJulgamento");
    }

    @Factory(scope = ScopeType.EVENT)
    public Evento getEventoJulgamentoProcessual() {
        return getEntity(Evento.class, "idEventoJulgamento");
    }

    @Factory(scope = ScopeType.EVENT)
    public Evento getEventoJulgamentoEmDiligenciaProcessual() {
        return getEntity(Evento.class,
            "idEventoJulgamentoEmDiligencia");
    }

    @Factory(scope = ScopeType.EVENT)
    public Evento getEventoRemetidoTrf() {
        return getEntity(Evento.class, "idEventoRemetidoTrf");
    }

    @Factory(scope = ScopeType.EVENT)
    public Evento getEventoRemetidoTrfProcessual() {
        return getEntity(Evento.class, "idEventoRemetidoTrf");
    }

    @Factory(scope = ScopeType.EVENT)
    public Usuario getUsuarioSistema() {
        return getEntity(Usuario.class, Parametros.ID_USUARIO_SISTEMA);
    }
    
    public Pessoa getPessoaSistema() {
    	return getEntity(Pessoa.class, Parametros.ID_USUARIO_SISTEMA);
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{parametroUtil.isSegundoGrau() or parametroUtil.isTerceiroGrau()}")
    public Jurisdicao getJurisdicao() {
        return getEntity(Jurisdicao.class, "idJurisdicao");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoProcessoDocumento getTipoProcessoDocumentoCitacao() {
    	String ids = getParametro("idTipoProcessoDocumentoCitacao");
    	String id = ArrayUtil.get(ids, ",", 0);
    	
        return getEntityPelaPK(TipoProcessoDocumento.class, id);
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoProcessoDocumento getTipoProcessoDocumentoMandado() {
        return getEntity(TipoProcessoDocumento.class,
            "idTipoProcessoDocumentoMandado");
    }

    @Factory(scope = ScopeType.EVENT)
    public Papel getPapelAssistenteAdvogado() {
        return getEntity(Papel.class, "idPapelAssistenteAdvogado");
    }

    @Factory(scope = ScopeType.EVENT)
    public Papel getPapelAssistenteGestorAdvogado() {
        return getEntity(Papel.class, "idPapelAssistenteGestorAdvogado");
    }

    public TipoProcessoDocumento getTipoProcessoDocumentoApelacao() {
        return getEntity(TipoProcessoDocumento.class,
            "idTipoProcessoDocumentoApelacao");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoProcessoDocumento getTipoProcessoDocumentoVoto() {
        return getEntity(TipoProcessoDocumento.class,
            "idTipoProcessoDocumentoVoto");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoProcessoDocumento getTipoProcessoDocumentoEmenta() {
        return getEntity(TipoProcessoDocumento.class,
            "idTipoProcessoDocumentoEmenta");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoProcessoDocumentoTrf getTipoProcessoDocumentoAcordao() {
        return getEntity(TipoProcessoDocumentoTrf.class,
            "idTipoProcessoDocumentoAcordao");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoProcessoDocumentoTrf getTipoProcessoDocumentoAcordaoDEJT() {
        return getEntity(TipoProcessoDocumentoTrf.class,
            "idTipoProcessoDocumentoAcordaoDEJT");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoProcessoDocumento getTipoProcessoDocumentoInteiroTeor() {
        return getEntity(TipoProcessoDocumento.class,
            "idTipoProcessoDocumentoInteiroTeor");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoProcessoDocumento getTipoProcessoDocumentoRelatorio() {
        return getEntity(TipoProcessoDocumento.class,
            "idTipoProcessoDocumentoRelatorio");
    }

    @Factory(scope = ScopeType.EVENT)
    public Evento getEventoRetiradoPauta() {
        return getEntity(Evento.class, "idProcessoRetiradoPauta");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoProcessoDocumento getTipoProcessoDocumentoIntimacaoPauta() {
        return getEntity(TipoProcessoDocumento.class, Parametros.ID_TIPO_DOCUMENTO_INTIMACAO_PAUTA);
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoProcessoDocumento getTipoProcessoDocumentoIntimacao() {
    	String ids = getParametro("idTipoProcessoDocumentoIntimacao");
    	String id = ArrayUtil.get(ids, ",", 0);
    	
        return getEntityPelaPK(TipoProcessoDocumento.class, id);
    }
    
    @Factory(scope = ScopeType.EVENT)
    public List<Integer> getListaIdTipoProcessoDocumentoIntimacao() {
    	String parametro = getParametro("idTipoProcessoDocumentoIntimacao");
    	return StringUtil.converterParaListaInteiro(parametro, ",");
    }
    
    @Factory(scope = ScopeType.EVENT)
    public List<Integer> getListaIdTipoProcessoDocumentoCitacao() {
    	String parametro = getParametro("idTipoProcessoDocumentoCitacao");
    	return StringUtil.converterParaListaInteiro(parametro, ",");
    }
    
    @Factory(scope = ScopeType.EVENT)
    public List<Integer> getListaIdTipoProcessoDocumentoNotificacao() {
    	String parametro = getParametro("idTipoProcessoDocumentoNotificacao");
    	return StringUtil.converterParaListaInteiro(parametro, ",");
    }

    @Factory(scope = ScopeType.EVENT)
    public List<Integer> getListaIdTipoProcessoDocumentoVistaManifestacao() {
    	String parametro = getParametro("idTipoProcessoDocumentoVistaManifestacao");
    	return StringUtil.converterParaListaInteiro(parametro, ",");
    }

    @Factory(scope = ScopeType.EVENT)
    public List<Integer> getListaIdTipoProcessoDocumentoUrgente() {
    	String parametro = getParametro("idTipoProcessoDocumentoUrgente");
    	return StringUtil.converterParaListaInteiro(parametro, ",");
    }

    @Factory(scope = ScopeType.EVENT)
    public List<Integer> getListaIdTipoProcessoDocumentoPautaAudienciaOuJulgamento() {
    	String parametro = getParametro("idTipoProcessoDocumentoPautaAudienciaOuJulgamento");
    	return StringUtil.converterParaListaInteiro(parametro, ",");
    }
    
	@Factory(scope = ScopeType.EVENT)
    public TipoProcessoDocumento getTipoProcessoDocumentoAtoOrdinatorio() {
        return getEntity(TipoProcessoDocumento.class,
            "idTipoDocumentoAtoOrdinatorio");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoProcessoDocumento getTipoProcessoDocumentoJuntadaAR() {
        return getEntity(TipoProcessoDocumento.class, "idTipoDocumentoJuntadaAR");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoProcessoDocumento getTipoProcessoDocumentoCertidao() {
        return getEntity(TipoProcessoDocumento.class, "idTipoDocumentoCertidao");
    }
    
    @Factory(scope = ScopeType.EVENT)
    public TipoProcessoDocumento getTipoProcessoDocumentoCertidaoJulgamento() {
        return getEntity(TipoProcessoDocumento.class, Parametros.ID_TIPO_DOCUMENTO_CERTIDAO_JULGAMENTO);
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoProcessoDocumento getTipoProcessoDocumentoExpediente() {
        return getEntity(TipoProcessoDocumento.class, "idTipoProcessoDocumentoExpediente");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoProcessoDocumento getTipoProcessoDocumentoOficio() {
    	try {
			return ComponentUtil.getComponent(TipoProcessoDocumentoManager.class).findByCodigoDocumento(getParametro("cdTipoParametroOficio"), true);
		} catch (PJeBusinessException e) {
			return null;
		}
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    public Tarefa getTipoTarefaAtoMagistrado() {
        return getEntity(Tarefa.class, "idTarefaAtoMagistrado");
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    public Tarefa getTipoTarefaAtoMagistradoSreeo() {
        return getEntity(Tarefa.class, "idTarefaAtoMagistradoSreeo");
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    public Tarefa getTarefaAnaliseGabinete() {
        return getEntity(Tarefa.class, "idTarefaAnaliseGabinete");
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    public Tarefa getTarefaAnaliseGabineteSreeo() {
        return getEntity(Tarefa.class, "idTarefaAnaliseGabineteSreeo");
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    public Tarefa getTarefaMinutarSreeo() {
        return getEntity(Tarefa.class, "idTarefaMinutarSreeo");
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    public Tarefa getTarefaMinutar() {
        return getEntity(Tarefa.class, "idTarefaMinutar");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoProcessoDocumentoTrf getTipoProcessoDocumentoSentenca() {
        return getEntity(TipoProcessoDocumentoTrf.class,
            "idTipoProcessoDocumentoSentenca");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoProcessoDocumento getTipoProcessoDocumentoDespacho() {
        return getEntity(TipoProcessoDocumento.class,
            "idTipoProcessoDocumentoDespacho");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoProcessoDocumento getTipoProcessoDocumentoDecisao() {
        return getEntity(TipoProcessoDocumento.class,
            "idTipoProcessoDocumentoDecisao");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoProcessoDocumento getTipoProcessoDocumentoContestacao() {
        return getEntity(TipoProcessoDocumento.class,
            "idTipoProcessoDocumentoContestacao");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoProcessoDocumento getTipoProcessoDocumentoPeticaoTerceiro() {
        return getEntity(TipoProcessoDocumento.class,
            "idTipoProcessoDocumentoPeticaoTerceiro");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoParte getTipoParteSecaoJudiciaria() {
        return getEntity(TipoParte.class, "idTipoParteSecaoJudiciaria");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoParte getTipoParteAutoridadeCoatora() {
        return getEntity(TipoParte.class, "idTipoParteAutoridadeCoatora");
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    public Pessoa getSecaoJudiciaria() {
        return getEntity(Pessoa.class, "idSecaoJudiciaria");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoParte getTipoParteRepresentante() {
        return getEntity(TipoParte.class, "idTipoParteRepresentante");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoParte getTipoParteInterprete() {
        return getEntity(TipoParte.class, "idTipoParteInterprete");
    }
    
    @Factory(scope = ScopeType.EVENT)
    public TipoParte getTipoParteRepresentantePais() {
        return getEntity(TipoParte.class, "idTipoParteRepresentantePais");
    }    
    
    @Factory(scope = ScopeType.EVENT)
    public TipoParte getTipoParteInventariante() {
        return getEntity(TipoParte.class, "idTipoParteInventariante");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoParte getTipoParteTutor() {
        return getEntity(TipoParte.class, "idTipoParteTutor");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoParte getTipoParteCurador() {
        return getEntity(TipoParte.class, "idTipoParteCurador");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoParte getTipoParteProcurador() {
        return getEntity(TipoParte.class, "idTipoParteProcurador");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoParte getTipoParteHerdeiro() {
        return getEntity(TipoParte.class, "idTipoParteHerdeiro");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoParte getTipoParteCessionario() {
        return getEntity(TipoParte.class, "idTipoParteCessionario");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoParte getTipoPartePerito() {
        return getEntity(TipoParte.class, "idTipoPartePerito");
    }

	@Factory(scope = ScopeType.APPLICATION)
	public TipoParte getTipoParteFiscalLei() {
		TipoParte result = null;

		try {
			List<TipoParte> tiposParte = ComponentUtil.getComponent(TipoParteManager.class)
					.findByNomeParticipacao(getParametro(Parametros.VAR_TIPO_PARTE_FISCAL_LEI));

			if (!tiposParte.isEmpty()) {
				result = tiposParte.get(0);
			}
		} catch (PJeBusinessException e) {
			// Nothing to do.
		}

		return result;
	}

    @Factory(scope = ScopeType.EVENT)
    public Boolean getPermitirCadastrosBasicos() {
        if (Contexts.getApplicationContext().get("permitirCadastrosBasicos") == null) {
            return true;
        }

        return Contexts.getApplicationContext().get("permitirCadastrosBasicos")
                       .toString().equalsIgnoreCase("true");
    }
    
    @Factory(scope = ScopeType.EVENT)
    public Papel getPapelAdvogado() {
        return getEntity(Papel.class, Parametros.ID_PAPEL_ADVOGADO);
    }  


    /**
     * @author athos / rodrigo cartaxo
     * @since 1.2.0
     * @category PJE-JT
     * @return Papel jus postulandi
     */
    @Factory(scope = ScopeType.APPLICATION)
    public Papel getPapelJusPostulandi() {
        Papel papel = getEntity(Papel.class, Parametros.ID_PAPEL_JUSPOSTULANDI);

        /*
         * PJEII-3757 - Thiago Gutenberg C. da Costa - thiago.carvalho
         * Caso não encontre o papel para tal id, retornar uma nova instancia de Papel,
         * para que não haja quebra na clausula WHERE do hql do arquivo
         * confirmaCadastroPessoaJusPostulandiGrid.component.xml
         * na property ejbql, não carregando a tela.
         */
        return this.isPapelNull(papel);
    }

    /**
     * Verifica se o Papel esta nulo.
     *
     * @since 1.4.6 - 08/11/2012
     * @author Thiago Gutenberg C. da Costa
     * @param papel
     * @return uma nova instancia de Papel caso o parametro papel esteja nulo.
     */
    private Papel isPapelNull(Papel papel) {
        return (papel == null) ? new Papel() : papel;
    }

    @Factory(scope = ScopeType.EVENT)
    public Papel getPapelProcurador() {
        return getEntity(Papel.class, Parametros.ID_PAPEL_PROCURADOR);
    }
    
    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{parametroUtil.isSegundoGrau() or parametroUtil.isTerceiroGrau()}")
    public Papel getPapelProcuradorGestor() {
        return getEntity(Papel.class, "idPapelProcuradorGestor");
    }
    
    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{parametroUtil.isSegundoGrau() or parametroUtil.isTerceiroGrau()}")
    public Papel getPapelProcuradorMP() {
        return getEntity(Papel.class, "idPapelProcuradorMP");
    }
    
    @Factory(scope = ScopeType.EVENT)
    public Papel getPapelProcuradorMPGestor() {
        return getEntity(Papel.class, "idPapelProcuradorMPGestor");
    }

    @Factory(scope = ScopeType.EVENT)
    public Papel getPapelPerito() {
        return getEntity(Papel.class, Parametros.ID_PAPEL_PERITO);
    }

    @Factory(scope = ScopeType.EVENT)
    public Papel getPapelAssistenteProcuradoria() {
        return getEntity(Papel.class, "idPapelAssistenteProcuradoria");
    }

    @Factory(scope = ScopeType.EVENT)
    public Papel getPapelMagistrado() {
        return getEntity(Papel.class, "idPapelMagistrado");
    }

    @Factory(scope = ScopeType.EVENT)
    public Papel getPapelDiretorSecretaria() {
        return getEntity(Papel.class, "idPapelDiretorSecretaria");
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    public Tarefa getTarefaConhecimentoSecretaria() {
        return getEntity(Tarefa.class, "idTarefaConhecimentoSecretaria");
    }

    public Caixa getCaixaIntimacaoAutoPend() {
    	Caixa caixa = (Caixa)Contexts.getApplicationContext().get("caixaIntimacoesAutomaticasPendencia");
    	if(caixa == null) {
    		caixa = getEntity(Caixa.class, getIdCaixaIntimacaoAutoPend());
    		Contexts.getApplicationContext().set("caixaIntimacoesAutomaticasPendencia", caixa);
    	}
        return caixa;
    }

    public String getIdCaixaIntimacaoAutoPend() {
    	String id = (String)Contexts.getApplicationContext().get("idCaixaIntimacoesAutomaticasPendencia");
    	if(id == null) {
    		id = getParametro("idCaixaIntimacoesAutomaticasPendencia");
    		Contexts.getApplicationContext().set("idCaixaIntimacoesAutomaticasPendencia", id);
    	}
    	
        return id;
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    public Papel getPapelAdministradorConhecimento() {
        return getEntity(Papel.class, "idPapelAdministradorConhecimento");
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    public Papel getPapelServidorConhecimento() {
        return getEntity(Papel.class, "idPapelServidorConhecimento");
    }

    @Factory(scope = ScopeType.EVENT)
    public Papel getPapelOficialJustica() {
        return getEntity(Papel.class, Parametros.ID_PAPEL_OFICIAL_JUSTICA);
    }

    @Factory(scope = ScopeType.EVENT)
    public Boolean getRegistrarLogConsulta() {
        return Boolean.valueOf((String) Contexts.getApplicationContext()
                                                .get("registrarLogConsulta"));
    }

    @Factory(scope = ScopeType.EVENT)
    public ModeloDocumento getAvisoPermissaoCadastroAdvogado() {
        return getEntity(ModeloDocumento.class,
            "idAvisoPermissaoCadastroAdvogado");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoProcessoDocumento getTipoProcessoDocumentoTermoCompromisso() {
        return getEntity(TipoProcessoDocumento.class,
            Parametros.ID_TIPO_DOCUMENTO_CADASTRO_ADVOGADO);
    }

    /**
     * @author athos / rodrigo cartaxo
     * @since 1.2.0
     * @category PJE-JT
     * @return Papel jus postulandi
     */
    @Factory(scope = ScopeType.APPLICATION)
    public TipoProcessoDocumento getTipoProcessoDocumentoTermoCompromissoJusPostulandi() {
        return getEntity(TipoProcessoDocumento.class,
            Parametros.ID_TIPO_DOCUMENTO_CADASTRO_JUSPOSTULANDI);
    }

    @Factory(scope = ScopeType.EVENT)
    public RpvStatus getStatusRpvEmValidacao() {
        return getEntity(RpvStatus.class, "idStatusRpvEmValidacao");
    }

    @Factory(scope = ScopeType.EVENT)
    public RpvStatus getStatusRpvEmConferencia() {
        return getEntity(RpvStatus.class, "idStatusRpvEmConferencia");
    }

    @Factory(scope = ScopeType.EVENT)
    public RpvStatus getStatusRpvEmElaboracao() {
        return getEntity(RpvStatus.class, "idStatusRpvEmElaboracao");
    }

    @Factory(scope = ScopeType.EVENT)
    public RpvStatus getStatusRpvDevolvida() {
        return getEntity(RpvStatus.class, "idStatusRpvDevolvida");
    }

    @Factory(scope = ScopeType.EVENT)
    public RpvStatus getStatusRpvCancelada() {
        return getEntity(RpvStatus.class, "idStatusRpvCancelada");
    }

    @Factory(scope = ScopeType.EVENT)
    public RpvStatus getStatusRpvValidada() {
        return getEntity(RpvStatus.class, "idStatusRpvValidada");
    }

    @Factory(scope = ScopeType.EVENT)
    public ModeloDocumento getModeloDocumentoInconsistencia() {
        return getEntity(ModeloDocumento.class,
            "idModeloDocumentoInconsistencia");
    }

    /**
     * @author athos / rodrigo cartaxo
     * @since 1.2.0
     * @category PJE-JT
     * @return Tipo pessoa jus Postulandi
     */
    @Factory(scope = ScopeType.APPLICATION)
    public ModeloDocumento getModeloDocumentoInconsistenciaJusPostulandi() {
        return getEntity(ModeloDocumento.class,
            "idModeloDocumentoInconsistenciaJusPostulandi");
    }

    @Factory(scope = ScopeType.EVENT)
    public Profissao getProfissaoAdvogado() {
        return getEntity(Profissao.class, "idProfissaoAdvogado");
    }

    @Factory(scope = ScopeType.EVENT)
    public Escolaridade getEscolaridadeEnsinoSuperior() {
        return getEntity(Escolaridade.class, "idEscolaridadeEnsinoSuperior");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoProcessoDocumento getTipoProcessoDocumentoInconsistencia() {
        return getEntity(TipoProcessoDocumento.class,
            "idTipoDocumentoInconsistencia");
    }

    /**
     * @author athos / rodrigo cartaxo
     * @since 1.2.0
     * @category PJE-JT
     * @return Tipo pessoa jus Postulandi
     */
    @Factory(scope = ScopeType.APPLICATION)
    public TipoProcessoDocumento getTipoProcessoDocumentoInconsistenciaJusPostulandi() {
        return getEntity(TipoProcessoDocumento.class,
            "idTipoDocumentoInconsistenciaJusPostulandi");
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    //provavelmente lixo, não usado pela jt
    public Qualificacao getQualificacaoCpf() {
        return getEntity(Qualificacao.class, "idQualificacaoCpf");
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    //provavelmente lixo, não usado pela jt
    public Qualificacao getQualificacaoOab() {
        return getEntity(Qualificacao.class, "idQualificacaoOab");
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    //provavelmente lixo, não usado pela jt
    public ComplementoQualificacao getComplementoOabUf() {
        return getEntity(ComplementoQualificacao.class, "idComplementoOabUf");
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    //provavelmente lixo, não usado pela jt
    public ComplementoQualificacao getComplementoOabData() {
        return getEntity(ComplementoQualificacao.class, "idComplementoOabData");
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    //provavelmente lixo, não usado pela jt
    public Qualificacao getQualificacaoCnpj() {
        return getEntity(Qualificacao.class, "idQualificacaoCnpj");
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    //provavelmente lixo, não usado pela jt
    public Qualificacao getQualificacaoRg() {
        return getEntity(Qualificacao.class, "idQualificacaoRg");
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    //provavelmente lixo, não usado pela jt
    public ComplementoQualificacao getComplementoRgData() {
        return getEntity(ComplementoQualificacao.class, "idComplementoRgData");
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    //provavelmente lixo, não usado pela jt
    public ComplementoQualificacao getComplementoRgOrgao() {
        return getEntity(ComplementoQualificacao.class, "idComplementoRgOrgao");
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    //provavelmente lixo, não usado pela jt
    public Qualificacao getQualificacaoDataNascimento() {
        return getEntity(Qualificacao.class, "idQualificacaoDataNascimento");
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    //provavelmente lixo, não usado pela jt
    public Qualificacao getQualificacaoTelefoneCelular() {
        return getEntity(Qualificacao.class, "idQualificacaoTelefoneCelular");
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    //provavelmente lixo, não usado pela jt
    public Qualificacao getQualificacaoTelefoneComercial() {
        return getEntity(Qualificacao.class, "idQualificacaoTelefoneComercial");
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    //provavelmente lixo, não usado pela jt
    public Qualificacao getQualificacaoTelefoneResidencial() {
        return getEntity(Qualificacao.class, "idQualificacaoTelefoneResidencial");
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    //provavelmente lixo, não usado pela jt
    public Qualificacao getQualificacaoNaturalidade() {
        return getEntity(Qualificacao.class, "idQualificacaoNaturalidade");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoPessoa getTipoAdvogado() {
        return getEntity(TipoPessoa.class, "idTipoPessoaAdvogado");
    }

    public static String getLoginComCertificado() {
        String value = (String) Contexts.getApplicationContext().get("loginComCertificado");

        /*
         * A autenticação no sistema mediante informação de nome de usuário e senha será permitida
         * apenas se o parâmetro "loginComCertificado" não existir OU tiver valor diferente de "true".
         */
        if (value == null || !value.equals("true")) {
            return "false";
        }

        return value;
    }

    /**
     * @author athos / rodrigo cartaxo
     * @since 1.2.0
     * @category PJE-JT
     * @return Tipo pessoa jus Postulandi
     */
    @Factory(scope = ScopeType.APPLICATION)
    public TipoPessoa getTipoPessoaJusPostulandi() {
        return getEntity(TipoPessoa.class, "idTipoPessoaJusPostulandi");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoPessoa getTipoPessoaPerito() {
        return getEntity(TipoPessoa.class, "idTipoPessoaPerito");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoPessoa getTipoPessoaMagistrado() {
        return getEntity(TipoPessoa.class, "idTipoPessoaMagistrado");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoPessoa getTipoPessoaServidor() {
        return getEntity(TipoPessoa.class, "idTipoPessoaServidor");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoPessoa getTipoPessoaOficialJustica() {
        return getEntity(TipoPessoa.class, "idTipoPessoaOficialJustica");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoPessoa getTipoPessoaEscritorioAdvocacia() {
        return getEntity(TipoPessoa.class, "idTipoPessoaEscritorioAdvocacia");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoPessoa getTipoPessoaFisica() {
        return getEntity(TipoPessoa.class, "idTipoPessoaFisica");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoPessoa getTipoPessoaJuridica() {
        return getEntity(TipoPessoa.class, "idTipoPessoaJuridica");
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    public TipoPessoa getTipoPessoaEntidade() {
        return getEntity(TipoPessoa.class, "idTipoPessoaEntidade");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoParte getTipoParteAdvogado() {
        return getEntity(TipoParte.class, "idTipoParteAdvogado");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoParte getTipoParteReu() {
        return getEntity(TipoParte.class, "idTipoParteReu");
    }

    @Factory(scope = ScopeType.EVENT)
    public ModeloDocumento getModeloComprovanteCadastroAdvogado() {
        return getEntity(ModeloDocumento.class,
            Parametros.ID_MODELO_CADASTRO_ADVOGADO);
    }

    /**
     * @author athos / rodrigo cartaxo
     * @since 1.2.0
     * @category PJE-JT
     * @return Modelo de documento do termo de aceite do cadastro de jus
     *         Postulandi
     */
    @Factory(scope = ScopeType.APPLICATION)
    public ModeloDocumento getModeloComprovanteCadastroJusPostulandi() {
        return getEntity(ModeloDocumento.class,
            Parametros.ID_MODELO_CADASTRO_JUSPOSTULANDI);
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    //provavelmente lixo, não usado pela jt
    public Localizacao getLocalizacaoAdvogadoSemEscritorio() {
        return getEntity(Localizacao.class, "idLocalizacaoAdvogadoSemEscritorio");
    }

    @Factory(scope = ScopeType.EVENT)
    public ModeloDocumento getModeloPeticaoInicial() {
        return getEntity(ModeloDocumento.class, "idModeloPeticaoInicial");
    }
    
    @Factory(scope = ScopeType.EVENT)
    public TipoProcessoDocumento getTipoProcessoDocumentoPeticaoInicial() {
        return getEntity(TipoProcessoDocumento.class, Parametros.ID_TIPO_PROCESSO_DOCUMENTO_PETICAO_INICIAL);
    }

    @Factory(scope = ScopeType.EVENT)
    public ModeloDocumentoLocal getModeloLocalPeticaoInicial() {
        return getEntity(ModeloDocumentoLocal.class, "idModeloPeticaoInicial");
    }
    
    @Factory(scope = ScopeType.EVENT)
	public ModeloDocumentoLocal getModeloTermoAberturaLiquidacao() {
	 	return getEntity(ModeloDocumentoLocal.class, "idModeloDocTermoAberturaLiq");
	}
	
	@Factory(scope = ScopeType.EVENT)
	public ModeloDocumentoLocal getModeloTermoAberturaExecucao() {
	 	return getEntity(ModeloDocumentoLocal.class, "idModeloDocTermoAberturaExec");
	}

    @Factory(scope = ScopeType.EVENT)
    public ModeloDocumento getModeloPeticaoIncidental() {
        return getEntity(ModeloDocumento.class, "idModeloPeticaoIncidental");
    }

    @Factory(scope = ScopeType.EVENT)
    public ModeloDocumento getModeloIntimacaoPauta() {
        return getEntity(ModeloDocumento.class, Parametros.ID_MODELO_DOCUMENTO_INTIMACAO_PAUTA);
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    public ModeloDocumento getModeloCertidaoJulgamento() {
        return getEntity(ModeloDocumento.class,
            "idModeloDocumentoCertidaoJulgamento");
    }

    @Factory(scope = ScopeType.EVENT)
    public ModeloDocumento getModeloAtaJulgamento() {
        return getEntity(ModeloDocumento.class,
            "idModeloDocumentoAtaJulgamento");
    }

    @Factory(scope = ScopeType.EVENT)
    public ModeloDocumento getModeloJuntadaAR() {
        return getEntity(ModeloDocumento.class,
            "idModeloDocumentoJuntadaAR");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoProcessoDocumento getTipoProcessoDocumento() {
        return getEntity(TipoProcessoDocumento.class,
            "idProcessoDocumentoExpediente");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoProcessoDocumento getTipoProcessoDocumentoDiligencia() {
        return getEntity(TipoProcessoDocumento.class,
            "idProcessoDocumentoDiligencia");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoResultadoDiligencia getTipoResultadoDiligenciaRedistribuicao() {
        return getEntity(TipoResultadoDiligencia.class,
            "idTipoResultadoDiligenciaRedistribuicao");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoModeloDocumento getTipoModeloDocumentoIntimacaoPauta() {
        return getEntity(TipoModeloDocumento.class,
            "idTipoProcessoDocumentoIntimacaoPauta");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoResultadoDiligencia getTipoResultadoDiligenciaCumprido() {
        return getEntity(TipoResultadoDiligencia.class,
            "idTipoResultadoDiligenciaCumprido");
    }

    @Factory(scope = ScopeType.EVENT)
    public Evento getEventoTipoDistribuicao() {
        return getEventoDistribuicao();
    }

    @Factory(scope = ScopeType.EVENT)
    public Evento getEventoDistribuicao() {
        return getEntity(Evento.class, "idEventoDistribuicao");
    }

    @Factory(scope = ScopeType.EVENT)
    public Evento getEventoTipoRedistribuicao() {
        return getEntity(Evento.class, "idEventoRedistribuicao");
    }

    @Factory(scope = ScopeType.EVENT)
    public Evento getEventoConclusao() {
        return getEntity(Evento.class, "idEventoConclusao");
    }

    @Factory(scope = ScopeType.EVENT)
    public Evento getEventoRedistribuicaoSorteio() {
        return getEntity(Evento.class, "idEventoRedistribuicaoSorteio");
    }

    @Factory(scope = ScopeType.EVENT)
    public Evento getEventoRedistribuicaoPrevencao() {
        return getEntity(Evento.class,
            "idEventoRedistribuicaoPrevencao");
    }

    @Factory(scope = ScopeType.EVENT)
    public Evento getEventoRedistribuicaoDependencia() {
        return getEntity(Evento.class,
            "idEventoRedistribuicaoDependencia");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoProcessoDocumento getTipoProcessoDocumentoRpvValidada() {
        return EntityUtil.getEntityManager()
                         .find(TipoProcessoDocumento.class, 52);
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoProcessoDocumento getTipoProcessoDocumentoTransitoJulgado() {
        return getEntity(TipoProcessoDocumento.class,
            "idTipoProcessoDocumentoTransitoJulgado");
    }

    @Factory(scope = ScopeType.EVENT)
    public Fluxo getFluxoPadrao() {
        return getEntity(Fluxo.class, "idFluxoPadrao");
    }

    @Factory(scope = ScopeType.EVENT)
    public ModeloDocumento getModeloDocumentoEmailExpediente() {
        return getEntity(ModeloDocumento.class,
            "idModeloDocumentoEmailExpediente");
    }

    private <E> E getEntity(Class<E> clazz, String parametro) {
        String id = getParametro(parametro);
        E retorno = null;

        if (StringUtil.isNotEmpty(id) && NumberUtils.isNumber(id)) {
            retorno = getEntityPelaPK(clazz, id);
        }

        if (retorno == null) {
            log.warn(MessageFormat.format("getEntity({0}, {1}) não encontrou a entidade para o id={2}.", clazz.getName(), parametro, id));
        }

        return retorno;
    }
    
    private <E> E getEntityPelaPK(Class<E> clazz, String pk) {
        E retorno = null;

        if (pk != null && NumberUtils.isNumber(pk)) {
            EntityManager em = (EntityManager) Component.getInstance("entityManager",
                    ScopeType.CONVERSATION);
            retorno = em.find(clazz, Integer.parseInt(pk));
        }

        return retorno;
    }

    public static String getFromContext(String nomeParametro, boolean validar) {
    	String value = (String) Contexts.getApplicationContext().get(nomeParametro);

    	if (validar && (value == null)) {
        	String erroMsg = "Parâmetro não encontrado: " + nomeParametro;
        	log.error(erroMsg);
    	}

    	return value;
    }

    public static ParametroUtil instance() {
        return ComponentUtil.getComponent(NAME);
    }

    @Factory(scope = ScopeType.EVENT)
    public Papel getPapelConciliador() {
    	Papel papel = null;
		try {
			papel = ComponentUtil.getComponent(PapelManager.class).findByCodeName(Parametros.IDENTIFICADOR_PAPEL_CONCILIADOR);
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
		return papel;
    }

    @Factory(scope = ScopeType.EVENT)
    public Evento getEventoInclusaoPauta() {
        return getEntity(Evento.class, "idProcessoIncluidoPauta");
    }

    @Factory(scope = ScopeType.EVENT)
    public AplicacaoClasse getAplicacaoSistema() {
    	return ComponentUtil.getComponent(AplicacaoClasseManager.class).findByCodigo(this.getInstancia());
    }

    @Factory(scope = ScopeType.EVENT)
    public OrgaoJustica getOrgaoJustica() {
    	return ComponentUtil.getComponent(OrgaoJusticaManager.class).findByOrgaoJustica(this.getTipoJustica());
    }

    @Factory(scope = ScopeType.EVENT)
    public Boolean getDistribuicaoManual() {
        return Contexts.getApplicationContext().get("distribuicaoManual")
                       .toString().equalsIgnoreCase("true");
    }

    @Factory(scope = ScopeType.EVENT)
    public Fluxo getFluxoDistribuicao() {
        return getEntity(Fluxo.class, "fluxoDistribuicao");
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    //provavelmente lixo, não usado pela jt
    public Evento getEventoEnvio2Grau() {
        return getEntity(Evento.class, "idEventoEnvio2grau");
    }

    @Factory(scope = ScopeType.EVENT)
    public Evento getEventoEnvio1Grau() {
        return getEntity(Evento.class, "idEventoEnvio1grau");
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    public Evento getEventoRecebimento2Grau() {
        return getEntity(Evento.class, "idEventoRecebimento2grau");
    }

    @Factory(scope = ScopeType.EVENT)
    public Evento getEventoRecebimento1Grau() {
        return getEntity(Evento.class, "idEventoRecebimento1grau");
    }

    @Factory(scope = ScopeType.EVENT)
    public Fluxo getFluxoProcessante() {
        Query query = EntityUtil.createQuery(
                "select o from Fluxo o where o.codFluxo = 'proc_2grau'");
        Fluxo processante = EntityUtil.getSingleResult(query);

        return processante;
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    public Fluxo getFluxoSREEO() {
        Query query = EntityUtil.createQuery(
                "select o from Fluxo o where o.codFluxo = 'SREEO'");
        Fluxo processante = EntityUtil.getSingleResult(query);

        return processante;
    }

    public boolean isSegundoGrau() {
        return "2".endsWith(this.getInstancia());
    }
    
    public boolean isTerceiroGrau() {
        return "3".endsWith(this.getInstancia());
    }    

    public boolean isPrimeiroGrau() {
        return "1".endsWith(this.getInstancia());
    }
    
    public boolean habilitarCargoVinculacaoRegimental() {
		return "true".equals(getParametro(Parametros.PJE_HABILITAR_CARGO_VINCULACAO_REGIMENTAL));
    }

    public String getInstancia() {
        return getParametro(Parametros.APLICACAOSISTEMA);
    }

    /**
     * Retorna o codigo da instancia em um dos formatos: 
     * 1G, 2G, 3G, 4G (de acordo com a coluna codigo da aplicacao classe)
     * @return
     */
    public String getCodigoInstanciaAtual() {
    	String instancia = getInstancia();
    	if(instancia != null && instancia.trim().length() > 0) {
    		instancia = instancia.concat("G");
    	}
        return instancia;
    }

    @Factory(scope = ScopeType.EVENT)
    public Localizacao getLocalizacaoDirecaoDistribuicao() {
        return getEntity(Localizacao.class, "idLocalizacaoDirecaoDistribuicao");
    }

    @Factory(scope = ScopeType.EVENT)
    public Localizacao getLocalizacaoDirecaoSecretaria() {
        return getEntity(Localizacao.class, "idLocalizacaoDirecaoSecretaria");
    }

    @Factory(scope = ScopeType.EVENT)
    public Localizacao getLocalizacaoDirecaoSecretariaSRREO() {
        return getEntity(Localizacao.class,
            "idLocalizacaoDirecaoSecretariaSREEO");
    }

    @Factory(scope = ScopeType.EVENT)
    public Localizacao getLocalizacaoTribunal() {
        return getEntity(Localizacao.class, Parametros.PJE_LOCALIZACAO_TRIBUNAL);
    }
    
    @Factory(scope = ScopeType.EVENT)
    public Papel getPapelAdministrador() {
    	Papel papelAdministrador = null;
		try {
			papelAdministrador = ComponentUtil.getComponent(PapelManager.class).findByCodeName(Parametros.IDENTIFICADOR_PAPEL_ADMIN);
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
		return papelAdministrador;
    }    
    
    @Factory(scope = ScopeType.EVENT)
    public OrgaoJulgador getOrgaoJulgadorPlantao() {
        return getEntity(OrgaoJulgador.class, "idOjPlantao");
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    public OrgaoJulgador getOrgaoJulgadorSREEO() {
        return getEntity(OrgaoJulgador.class, "idOjSreeo");
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    public OrgaoJulgadorColegiado getOrgaoJulgadorColegiadoSRREO() {
        return getEntity(OrgaoJulgadorColegiado.class, "idOjcSreeo");
    }

    @Factory(scope = ScopeType.EVENT)
    public ModeloDocumento getModeloDocExpediente() {
        return getEntity(ModeloDocumento.class, "idModeloDocExpediente");
    }

    public String getParametroIdPapelEditarMinuta() {
        return getParametro("idPapelEditarMinuta");
    }

    @Factory(scope = ScopeType.EVENT)
    public List<Integer> getIdsPapeisEditarMinuta() {
        String idPapelEditarMinuta = getParametroIdPapelEditarMinuta();

        if (Strings.isEmpty(idPapelEditarMinuta)) {
            return Collections.emptyList();
        }

        List<Integer> list = new ArrayList<Integer>();
        String[] idsPapel = idPapelEditarMinuta.split(",");

        for (String id : idsPapel) {
            list.add(Integer.parseInt(id));
        }

        return list;
    }

    @Factory(scope = ScopeType.EVENT)
    public String getAgrupamentoConclusao() {
        return getParametro("agrupamentoConclusao");
    }

    @Factory(scope = ScopeType.EVENT)
    public Agrupamento getAgrupamentoExpedicaoDocumento() {
        return getEntity(Agrupamento.class, "idAgrupamentoExpedicaoDocumento");
    }

    @Factory(scope = ScopeType.EVENT)
    public Integer getPrazoIntimacoesAutomaticas() {
        return Integer.valueOf(getParametro("nrPrazoIntimacoesAutomaticas"));
    }

    @Factory(scope = ScopeType.EVENT)
    public String getPercLimiteValorCompsar() {
        return getParametro("percLimiteValorCompsar");
    }

    @Factory(scope = ScopeType.EVENT)
    public String getTextoFaleConosco() {
        return getParametro("textoFaleConosco");
    }

    @Factory(scope = ScopeType.APPLICATION)
    public ModeloDocumento getModeloDocumentoFaleConosco() {
    	ModeloDocumento modelo = (ModeloDocumento)Contexts.getApplicationContext().get("modeloDocumentoFaleConosco");
    	if(modelo == null) {
    		modelo = getEntity(ModeloDocumento.class, "idModeloDocumentoFaleConosco");
    		Contexts.getApplicationContext().set("modeloDocumentoFaleConosco", modelo);
    	}
        return modelo;
    }

    @Factory(scope = ScopeType.EVENT)
    public String getParametroOAB() {
        return getParametro("chaveOAB");
    }

    @Factory(scope = ScopeType.EVENT)
    public String getDsPendenciaIntimacoesAutomaticasPessoaAutoridadeCoatora() {
        return getParametro(
            "dsPendenciaIntimacoesAutomaticasPessoaAutoridadeCoatora");
    }

    @Factory(scope = ScopeType.EVENT)
    public String getDsPendenciaIntimacoesAutomaticasEntidade() {
        return getParametro("dsPendenciaIntimacoesAutomaticasEntidade");
    }

    @Factory(scope = ScopeType.EVENT)
    public String getDsPendenciaIntimacoesAutomaticasPessoaAdvogado() {
        return getParametro("dsPendenciaIntimacoesAutomaticasPessoaAdvogado");
    }

    @Factory(scope = ScopeType.EVENT)
    public String getDsPendenciaIntimacoesAutomaticasPessoaFisicaJuridica() {
        return getParametro(
            "dsPendenciaIntimacoesAutomaticasPessoaFisicaJuridica");
    }

    @Factory(scope = ScopeType.EVENT)
    public String getMensagemPlantao() {
        return getParametro("mensagemPlantao");
    }
    
	@Factory(scope = ScopeType.EVENT)
	public String getCodRamoJustica() {
		return getParametro("codRamoJustica");
    }

	@Factory(scope = ScopeType.EVENT)
	public String getNomeSessaoJudiciaria() {
		return getParametro("nomeSecaoJudiciaria");
    }

    @Factory(scope = ScopeType.EVENT)
    public String getIncidentalIdOrgaoJulgadorCompetencia() {
        String parametro = null;
        parametro = getParametro("tjrj:permitir:idOrgaoJulgadorCompetencia");
        if(parametro == null || parametro.isEmpty( )) {
            parametro = "0";
        }
        return parametro;
    }

	public static String getParametro(String nome) {
		return getParametro(nome, null);
	}

	public static String getParametro(String nome, String valorSeInexistente) {
		String parametro = null;

		if (!isParametroNaoConfigurado(nome)) {
			parametro = getParametroEnviroment(nome, null);

			if (Strings.isEmpty(parametro)) {
				parametro = getParametroDB(nome, null);
			}

			if (Strings.isEmpty(parametro)) {
				parametro = getParametroProperty(nome, null);
			}

			if (Strings.isEmpty(parametro)) {
				getParametrosNaoConfigurados().add(nome);
			}
		}

		if (Strings.isEmpty(parametro)) {
			log.warn("O parametro {0} não está configurado", nome);
			parametro = valorSeInexistente;
		}

		return parametro;
	}

	public static boolean getParametroBoolean(String parametro) {
		return getParametroBoolean(parametro, false);
	}

	public static boolean getParametroBoolean(String parametro, boolean valorSeInexistente) {
		String valorParametro = getParametro(parametro);
		if (valorParametro == null) {
			return valorSeInexistente;
		}
		return Boolean.parseBoolean(valorParametro);
	}

	public static TipoPrazoEnum getTipoPrazoParametro(String parametro, TipoPrazoEnum valorPadrao) {
		TipoPrazoEnum tipoPrazo = TipoPrazoEnum.obter(getParametro(parametro));
		return tipoPrazo != null ? tipoPrazo : valorPadrao;
	}

	public static Integer getPrazoParametro(TipoPrazoEnum tipoPrazo, String parametro, Integer valorPadrao) {
		if (tipoPrazo != null) {
			boolean ehSemPrazoOuDataCerta = tipoPrazo.isPrazoDataCerta() || tipoPrazo.isSemPrazo();
			if (ehSemPrazoOuDataCerta) {
				return null; // Não se aplica
			}

			String tempoPrazoString = getParametro(parametro);
			if (tempoPrazoString != null) {
				return Integer.valueOf(tempoPrazoString);
			}
		}
		return valorPadrao; // Não possui parâmetro do tipo do prazo ou não possui o parâmetro do prazo
	}

    private static String getParametroEnviroment(String nome, String valor) {
    	if(Strings.isEmpty(valor)) {
    		valor = System.getenv(nome);
    		if(Strings.isEmpty(valor)) {
    			valor = System.getProperty(nome);
    		}
    	}
    	return valor;
    }
    
    private static String getParametroProperty(String nome, String valor) {
    	if(Strings.isEmpty(valor)) {
    		valor = PropertiesUtil.getPJeProperty(nome);
    	}
    	return valor;
    }

//    private static String getParametroEurekaProperty(String nome, String valor) {
//    	if(Strings.isEmpty(valor)) {
//    		Properties p = PropertiesUtil.getProperties(ConfiguracaoIntegracaoCloud.EUREKA_CLIENT_PROPERTIES);
//    		if(p != null) {
//    			valor = p.getProperty(nome);
//    		}
//    	}
//    	return valor;
//    }

	private static String getParametroDB(String nome, String valor) {
		if (Strings.isEmpty(nome)) {
			return null;
		}

		if (!Strings.isEmpty(valor)) {
			return valor;
		}

		boolean contextoInicializado = false;
		try {
			if (!Contexts.isApplicationContextActive()) {
				Lifecycle.beginCall();
				contextoInicializado = true;
			}

			Context context = Contexts.getApplicationContext();

			if (context != null) {
				Object paramObj = context.get(nome);

				if (paramObj != null) {
					valor = (paramObj instanceof String) ? (String) paramObj : paramObj.toString();
				}
			}

			if (Strings.isEmpty(valor)) {
				EntityManager em = EntityUtil.getEntityManager();
				List<Parametro> resultList = em
						.createQuery("select p from Parametro p where nomeVariavel = :nome and ativo = true", Parametro.class)
						.setHint("javax.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS)
						.setHint("javax.persistence.cache.storeMode", CacheStoreMode.BYPASS)
						.setParameter("nome", nome)
						.getResultList();

				if (resultList != null && !resultList.isEmpty()) {
					valor = resultList.get(0).getValorVariavel();

					if (context != null) {
						context.set(nome, valor);
					}
				}
			}
		} finally {
			if (contextoInicializado) {
				Lifecycle.endCall();
			}
		}

		return valor;
	}

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    //provavelmente lixo, não usado pela jt
    public TipoProcessoDocumento getTipoProcessoDocumentoAvisoRecebimento() {
        return getEntity(TipoProcessoDocumento.class,
            "idTipoProcessoDocumentoAvisoRecebimento");
    }

    @Factory(scope = ScopeType.EVENT)
    public RpvStatus getStatusRpvRejeitada() {
        return getEntity(RpvStatus.class, "idStatusRpvRejeitada");
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    public RpvStatus getStatusRpvFinalizada() {
        return getEntity(RpvStatus.class, "idStatusRpvFinalizada");
    }

    @Factory(scope = ScopeType.EVENT)
    public AssuntoTrf getAssuntoMultaAstreintes() {
        return getEntity(AssuntoTrf.class, "idAssuntoMultaAstreintes");
    }

    @Factory(scope = ScopeType.EVENT)
    public Evento getEventoDespacho() {
        return getEntity(Evento.class, "idEventoDespacho");
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    public PessoaJuridica getPessoaSecaoJudiciaria() {
        return getEntity(PessoaJuridica.class, "idPessoaSecaoJudiciaria");
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    public TipoParte getTipoParteAutor() {
        return getEntity(TipoParte.class, "idTipoParteAutor");
    }

    @Factory(scope = ScopeType.EVENT)
    public RpvStatus getStatusRpvElaborado() {
        return getEntity(RpvStatus.class, "idStatusRpvElaborado");
    }
    
    public static Integer getTempoMinimoAudiencia() {
    	try {
    		return Integer.parseInt(ComponentUtil.getComponent(Parametros.TEMPO_MINIMO_AUDIENCIA));
    	} catch (NumberFormatException ex) {
    		return null;
    	}
    }

    @Factory(scope = ScopeType.EVENT)
    public String getTipoConexaoWebService() {
        return getParametro("tipoConexaoWebService");
    }

    @Factory(scope = ScopeType.EVENT)
    public Evento getEventoMagistrado() {
        return getEntity(Evento.class, "idEventoMagistrado");
    }
    
    @Factory(scope = ScopeType.APPLICATION)
    public Evento getProcessoRetiradoPauta() {
        return getEntity(Evento.class, "idProcessoRetiradoPauta");
    }

    @Factory(scope = ScopeType.APPLICATION)
    public static String getCodigoOrigem() {
        return getParametro("codigoOrigem");
    }

    public String executarFactorys() {
        for (Method metodo : this.getClass().getDeclaredMethods()) {
            try {
                metodo.invoke(this);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return "OK";
    }

    /**
     * Retorna o id do modelo de documento para a certidao de marcacao
     * automatica de audiencia nao marcada.
     *
     * @author Rafael Carvalho <rafael.carvalho@tst.jus.br>
     * @return id modelo de documento de marcacao automatica nao marcada.
     */
    @Factory(scope = ScopeType.APPLICATION, value = "idModeloDocumentoCMAANaoMarcada")
    public static Integer getIdModeloDocumentoCMAANaoMarcada() {
        return new Integer((String) EntityUtil.getEntityManager()
                                              .createQuery("select p.valorVariavel from Parametro p where p.nomeVariavel = 'idModeloDocumentoCMAANaoMarcada'")
                                              .getSingleResult());
    }

    /**
     * Retorna o id do modelo de documento para a certidao de marcacao de
     * audiencia marcada automaticamente.
     *
     * @author Rafael Carvalho <rafael.carvalho@tst.jus.br>
     *
     * @return id modelo de documento de marcacao automatica nao marcada.
     * @category PJE-JT
     * @since 1.2.0
     * @created 16/08/2011
     */
    @Factory(scope = ScopeType.APPLICATION, value = "idModeloDocumentoCMAAMarcada")
    public static Integer getIdModeloDocumentoCMAAMarcada() {
        return new Integer((String) EntityUtil.getEntityManager()
                                              .createQuery("select p.valorVariavel from Parametro p where p.nomeVariavel = 'idModeloDocumentoCMAAMarcada'")
                                              .getSingleResult());
    }

    /**
     * @author Rafael Carvalho <rafael.carvalho@tst.jus.br>
     *
     * @return
     * @category PJE-JT
     * @since 1.2.0
     * @created 16/08/2011
     */
    @Factory(scope = ScopeType.APPLICATION, value = "idTipoProcessoDocumentoCMAA")
    public static Integer getIdTipoProcessoDocumentoCMAA() {
        return new Integer((String) EntityUtil.getEntityManager()
                                              .createQuery("select p.valorVariavel from Parametro p where p.nomeVariavel = 'idTipoProcessoDocumentoCMAA'")
                                              .getSingleResult());
    }

    /**
     * @author Tiago Zanon/ Haroldo Arouca/ Rafael Carvalho [PJE-103 PJE-490]
     * @since 1.2.0
     * @category PJE-JT
     * @return boolean - Se o papel do usuário logado é jus postulandi.
     */
    @Factory(scope = ScopeType.EVENT, value = "isJusPostulandi")
    public boolean isJusPostulandi() {
    	return Authenticator.isJusPostulandi();
    }

    public Integer getIdTipoProcessoDocumentoRelatorio() {
        String idTipoProcessoDocumentoRelatorio = (String) Contexts.getApplicationContext().get("idTipoProcessoDocumentoRelatorio");

        if (idTipoProcessoDocumentoRelatorio != null) {
            return Integer.parseInt(idTipoProcessoDocumentoRelatorio);
        } else {
            log.warn("Parametro \"idTipoProcessoDocumentoRelatorio\" nao foi encontrado na tabela de parametros do banco de dados.");
        }

        return null;
    }

    public boolean isPermissaoAcessoVotoPreSessaoSecretarioTodos() {
        return "T".endsWith(ParametroUtil.getParametro("permissaoAcessoVotoPreSessaoSecretario"));
    }

    public boolean isPermissaoAcessoVotoPreSessaoSecretarioDecisorio() {
        return "D".endsWith(ParametroUtil.getParametro("permissaoAcessoVotoPreSessaoSecretario"));
    }

    public boolean isPermissaoAcessoVotoPreSessaoSecretarioSemAcesso() {
        return "N".endsWith(ParametroUtil.getParametro("permissaoAcessoVotoPreSessaoSecretario"));
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoProcessoDocumento getTipoProcessoDocumentoFundamentacao() {
        return getEntity(TipoProcessoDocumento.class,
            "idTipoProcessoDocumentoFundamentacao");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoProcessoDocumento getTipoProcessoDocumentoDispositivo() {
        return getEntity(TipoProcessoDocumento.class,
            "idTipoProcessoDocumentoDispositivo");
    }
    
	@Factory(scope = ScopeType.EVENT)
	public TipoProcessoDocumento getTipoProcessoDocumentoTermoAberturaLiquidacao() {
		return getEntity(TipoProcessoDocumento.class, "idTipoDocTermoAberturaLiq");
	}
	
	@Factory(scope = ScopeType.EVENT)
	public TipoProcessoDocumento getTipoProcessoDocumentoTermoAberturaExecucao() {
		return getEntity(TipoProcessoDocumento.class, "idTipoDocTermoAberturaExec");
    }

    @Factory(scope = ScopeType.EVENT)
    public Boolean getPermitirCadastroAdvogado() {
        return Contexts.getApplicationContext().get("permitirCadastroAdvogado")
                       .toString().equalsIgnoreCase("true");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoResultadoDiligencia getTipoResultadoDiligenciaConcluida() {
        return getEntity(TipoResultadoDiligencia.class,
            "idTipoResultadoDiligenciaConcluida");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoSituacaoPauta getTipoSituacaoPautaAguardandoSessaoJulgamento() {
        return getEntity(TipoSituacaoPauta.class,
            "tipoSituacaoPautaAguardandoSessaoJulgamento");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoSituacaoPauta getTipoSituacaoPautaApregoado() {
        return getEntity(TipoSituacaoPauta.class, "tipoSituacaoPautaApregoado");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoSituacaoPauta getTipoSituacaoPautaPendente() {
        return getEntity(TipoSituacaoPauta.class, "tipoSituacaoPautaPendente");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoSituacaoPauta getTipoSituacaoPautaJulgado() {
        return getEntity(TipoSituacaoPauta.class, "tipoSituacaoPautaJulgado");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoSituacaoPauta getTipoSituacaoPautaRetiradoPauta() {
        return getEntity(TipoSituacaoPauta.class,
            "tipoSituacaoPautaRetiradoPauta");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoVotoJT getTipoVotoAcompanhaRelator() {
        return getEntity(TipoVotoJT.class, "tipoVotoAcompanhaRelator");
    }

    @Factory(scope = ScopeType.EVENT)
    public Integer getAgrupamentoJulgamento() {
        return Integer.valueOf(getParametro("agrupamentoJulgamento"));
    }
    
    public Integer getIntAgrupamentoJulgamento() {
        String agrupamentoJulgamento = getParametro("agrupamentoJulgamento");
        if (agrupamentoJulgamento == null) {
            return getAgrupamentoJulgamento();
        }

        return Integer.valueOf(agrupamentoJulgamento);
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoVotoJT getTipoVotoDivergeEmParte() {
        return getEntity(TipoVotoJT.class, "tipoVotoDivergeEmParte");
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoVotoJT getTipoVotoDivergente() {
        return getEntity(TipoVotoJT.class, "tipoVotoDivergente");
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{justicaTrabalho and (parametroUtil.isSegundoGrau() or parametroUtil.isTerceiroGrau())}")
    public TipoVotoJT getTipoVotoNaoConhece() {
        return getEntity(TipoVotoJT.class, "tipoVotoNaoConhece");
    }

    @Factory(scope = ScopeType.EVENT)
    public String getTempoSustentacaoOral() {
        return getParametro("tempoSustentacaoOral");
    }

    @Factory(scope = ScopeType.EVENT)
    public int getTempoAtualizacaoProcessoApregoado() {
        String tempo = getParametro("tempoAtualizacaoProcessoApregoado");

        return Math.abs(Integer.parseInt(tempo) * 1000);
    }

    @Factory(scope = ScopeType.EVENT)
    @ValidarParametro(somenteQuando="#{!justicaTrabalho}")
    //provavelmente lixo, não usado pela jt
    public Evento getEventoTipoConclusao() {
        return getEntity(Evento.class, "idEventoTipoConclusao");
    }

    @Factory(scope = ScopeType.APPLICATION)
    public String getNomeTarefaRemessa2Grau() {
        return getParametro("nomeTarefaRemessa2Grau");
    }

    // PJEII-503 - Devolve o tipo de justica configurada na tabela de parametros do sistema
    @Factory(scope = ScopeType.EVENT)
    public String getTipoJustica() {
        return getParametro(Parametros.TIPOJUSTICA);
    }
    
    @Factory(scope = ScopeType.SESSION)
    public String getTipoEditor() {
        return getFromContext("tipoEditor", true);
    }

    /**
     * @author Rafael Barros
     * @category PJE-JT
     */
    @Factory(scope = ScopeType.APPLICATION)
    public ValorPesoEnum[] getValorPesoEnum() {
        return ValorPesoEnum.values();
    }

    @Factory(scope = ScopeType.APPLICATION)
    public Integer getIdTipoPessoaAutoridade() {
        Query query = EntityUtil.getEntityManager()
                                .createQuery("select o from TipoPessoa o where o.codTipoPessoa = :codTipoPessoa");
        query.setParameter("codTipoPessoa", "AUTORIDADE");

        TipoPessoa tipoPessoa = EntityUtil.getSingleResult(query);

        if (tipoPessoa == null) {
            return 0;
        } else {
            return tipoPessoa.getIdTipoPessoa();
        }
    }

    public int getIdTipoParteLitisconsorte() {
        Parametro p = (Parametro) EntityUtil.getEntityManager()
                                            .createQuery("select p from Parametro p where p.nomeVariavel = 'id_tipo_parte_litisconsorte' and p.ativo = true")
                                            .getSingleResult();

        return new Integer(p.getValorVariavel());
    }

    @Factory(scope = ScopeType.EVENT)
    public Localizacao getLocalizacaoPush() {
        return getEntity(Localizacao.class, "idLocalizacaoPush");
    }

    @Factory(scope = ScopeType.EVENT)
    public Papel getPapelUsuarioPush() {
        return getEntity(Papel.class, "idPapelPush");
    }

    public List<Integer> getTipoPrazosResultadoDiligenciaParaJtComComprimentoDePrazoFlexivel() {
        List<Integer> items = new ArrayList<Integer>();
        Parametro p = (Parametro) EntityUtil.getEntityManager()
                                            .createQuery("select p from Parametro p where p.nomeVariavel = 'tipoPrazosResultadoDiligenciaParaJtComComprimentoDePrazoFlexivel' and p.ativo = true")
                                            .getSingleResult();
        String[] values = p.getValorVariavel().split(":");

        for (String s : values) {
            items.add(new Integer(s));
        }

        return items;
    }
    
    @Factory(scope = ScopeType.EVENT)
	public Integer getPrazoED() {
		return Integer.valueOf(getParametro("prazoED"));
	}
	
	@Factory(scope = ScopeType.EVENT)
	public Integer getPrazoRecurso() {
		return Integer.valueOf(getParametro("prazoRecurso"));
	}
	
	@Factory(scope = ScopeType.EVENT)
	public Integer getIdTarefaAguardandoPrazoED() {
		return Integer.valueOf(getParametro("idTarefaAguardandoPrazoED"));
	}
	
	@Factory(scope = ScopeType.EVENT)
	public Integer getIdTarefaAguardandoPrazoRecurso() {
		return Integer.valueOf(getParametro("idTarefaAguardandoPrazoRecurso"));
	}
	/*
	 * [PJEII-2552] Rodrigo S. Menezes: Verifica casos em que o Magistrado se declara impedido ou suspeito
	 * sem que necessite proferir um voto. Esta é uma solução de contorno pois a funcionalidade
	 * não levou em conta a possibilidade de o Magistrado se declarar suspeito ou impedido e não necessitar
	 * proferir um voto
	 */
	@Factory(scope = ScopeType.EVENT)
	public Integer getIdTipoVotoSuspeicao()
	{
		return Integer.valueOf(getParametro("idTipoVotoSuspeicao"));
	}
	
	@Factory(scope = ScopeType.EVENT)
	public Integer getIdTarefaInclusaoPauta() {
		return Integer.valueOf(getParametro("idTarefaInclusaoPauta"));
	}
	
	@Factory(scope = ScopeType.EVENT)
	public Integer getIdMinutarDispositivoSessao() {
		return Integer.valueOf(getParametro("idTarefaMinutarDispositivoSessao"));
	}
	
	@Factory(scope = ScopeType.EVENT)	
	public Integer getIdTarefaAssinarAcordao() {
		return Integer.valueOf(getParametro("idTarefaAssinarAcordao"));
	}
	
	/**
	 * Recupera o código do agrupamento de classes e assuntos que deverá ser
	 * utilizado pela prevenção do art. 260 do Código Eleitoral.
	 * 
	 * @category PJEII-3651
	 * @created 18/02/2013
	 */
	@Deprecated
	@Factory(scope = ScopeType.EVENT)
	public String getCodAgrupamentoPrevencaoEleicaoOrigem() {
		// não validar parâmetro para evitar erros em outras justiças
		return getParametro("codAgrupamentoPrevencaoEleicaoOrigem");
	}
	
	/**
	 * [PJEII-7617] Parametro que indica se o quartz vai ignorar a falha de execução dos Jobs não executados; Rafael Carvalho (CSJT); 2013-05-29
	 * @return true para indicar se o quartz ignora as falhas de execução.
	 */
	public boolean isIgnoraFalhaDeExecucaoDoQuartz() {
		String value = getParametro("IGNORA_FALHA_DE_EXECUCAO_DO_QUARTZ");
		return "true".equalsIgnoreCase(value);
	}
	
	/**
	 * Recupera a lista de agrupamentos de classes e assuntos utilizados na
	 * aplicação das regras de prevenção conforme art. 260 do Código Eleitoral.
	 */
	@Factory(scope = ScopeType.EVENT)
	public String getListaAgrupamentoPrevencaoEleicaoOrigem() {
		return getParametro("listaAgrupamentosPrevencao260JE");
	}
	
	@Factory(scope = ScopeType.EVENT)
	public boolean isAssociarPorOrgaoJulgador() {
		boolean ret = false;
		try {
			String param = getParametro("pje:audiencia:modelo:associarPorOrgaoJulgador");
			if (param != null && "S".equals(param)) {
				ret = true;
			}
		} catch (Exception e) {
			//parametro nao cadastrado
		}
		return ret;
	}
    
	@Factory(scope = ScopeType.SESSION)
	public Integer getIdTipoResultadoDiligenciaNaoCumprida() {
		int ret = 0;
		try {
			String param = getParametro("pje:tipoResultadoDiligencia:id:naoCumprida");
			if (param != null) {
				ret = Integer.valueOf(param);
			}
		} catch (Exception e) {
			// Parametro não encontrado
		}
		return ret;
	}
	
	@Factory(scope = ScopeType.EVENT)
	public boolean isConsultarMateriasPeloReciboDePublicacaoDJE() {
		boolean ret = false;
		
		try {
			String param = getParametro("pje:publicacao:dje:materia:consultaPeloReciboDePublicacao");
			
			if (param != null && "S".equals(param)) {
				ret = true;
			}
		} catch (Exception e) {
			//parametro nao cadastrado
		}
		
		return ret;
	}

    @Factory(scope = ScopeType.EVENT)
    public boolean isRealizarAudienciaEmFluxo() {
        boolean ret = false;
        try {
            String param = getParametro("pje:audiencia:realizacaoEmFluxo");
            if (param != null && "true".equals(param)) {
                ret = true;
            }
        } catch (Exception e) {
            //parametro nao cadastrado
        }
        return ret;
    }	

	@Factory(scope = ScopeType.EVENT)
	public String getNaoExibirInformacaoJuntadoPorIds() {
		String param = null;
		try {
			param = getParametro("pje:processo:consulta:documentos:naoExibirInformacaoJuntadoPor:ids");
		} catch (Exception e) {
			// Parametro não encontrado
		}
		return param;
	}

	@Factory(scope = ScopeType.EVENT)
	public String getTipoOrdenacaoEndereco() {
    	return getParametro("pje:centralmandado:tipoordenacaoendereco");
	}

	@Factory(scope = ScopeType.EVENT)
	public String getTextoComplementarAudienciaComprovanteProtocolo() {
		String param = null;
		try {
			param = getParametro("pje:processo:comprovanteProtocolo:audiencia:textoComplementar");
		} catch (Exception e) {
			// Parametro não encontrado
		}
		return param;
	}
	@Factory(scope = ScopeType.APPLICATION)
	public boolean isGerarUmMandadoPorEndereco() {
		boolean ret = false;
		try {
			String param = getParametro("pje:centralMandado:gerarUmMandadoPorEndereco");
			if (param != null && "true".equals(param)) {
				ret = true;
			}
		} catch (Exception e) {
			//parametro nao cadastrado
		}
		return ret;
	}
	
	@Factory(value="qtdMinDiasAlertaAudiencia", scope = ScopeType.EVENT)
	public Integer getQtdMinDiasAlertaAudiencia() {
		int ret = 5;
		try {
			String param = getParametro("pje:centralmandado:qtdMinDiasAlertaAudiencia");
			if (param != null) {
				ret = Integer.valueOf(param);
			}
		} catch (Exception e) {
			// Nada a fazer.
		}
		return ret;
	}
	
    @Factory(scope = ScopeType.EVENT)
    public boolean isRegistrarMovimentacaoDistribuicaoMandado() {
        boolean ret = false;
        try {
            String param = getParametro("pje:centralmandado:registrarMovimentacaoDistribuicaoMandado");
            if (param != null && "true".equals(param)) {
                ret = true;
            }
        } catch (Exception e) {
            //parametro nao cadastrado
        }
        return ret;
    }
    
    @Factory(scope = ScopeType.EVENT)
    public boolean isRegistrarMovimentacaoDevolucaoMandado() {
        boolean ret = false;
        try {
            String param = getParametro("pje:centralmandado:registrarMovimentacaoDevolucaoMandado");
            if (param != null && "true".equals(param)) {
                ret = true;
            }
        } catch (Exception e) {
            //parametro nao cadastrado
        }
        return ret;
    }
    
    /**
    * Método responsável por recuperar o valor contido no parâmetro "pje:tipoDocumento:idTipoDocumentoProtocoloDistribuicao".
    * @return id do tipoDocumento que representa o Protocolo de Distribuição
    */
    public Integer getIdTipoDocumentoProtocoloDistribuicao() {
    	Integer ret = new Integer(0);
    	String param = getParametro(Parametros.PJE_TIPO_DOCUMENTO_ID_TIPO_DOCUMENTO_PROTOCOLO_DISTRIBUICAO);
    	if (param != null) {
    		ret = Integer.parseInt(param);
    	}
    	return ret;
    }
    
    public boolean isIniciarAbertaAbaMovimentacaoDetalhesProcesso() {
        return "true".equals(ParametroUtil.getParametro("pje:detalhesProcesso:IniciarAbertaAbaMovimentacao"));
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoProcessoDocumento getTipoProcessoDocumentoNotificacao() {
    	String ids = getParametro("idTipoProcessoDocumentoNotificacao");
    	String id = ArrayUtil.get(ids, ",", 0);
    	
        return getEntityPelaPK(TipoProcessoDocumento.class, id);
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoProcessoDocumento getTipoProcessoDocumentoVistaManifestacao() {
    	String ids = getParametro("idTipoProcessoDocumentoVistaManifestacao");
    	String id = ArrayUtil.get(ids, ",", 0);
    	
        return getEntityPelaPK(TipoProcessoDocumento.class, id);
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoProcessoDocumento getTipoProcessoDocumentoUrgente() {
    	String ids = getParametro("idTipoProcessoDocumentoUrgente");
    	String id = ArrayUtil.get(ids, ",", 0);
    	
        return getEntityPelaPK(TipoProcessoDocumento.class, id);
    }

    @Factory(scope = ScopeType.EVENT)
    public TipoProcessoDocumento getTipoProcessoDocumentoPautaAudienciaOuJulgamento() {
    	String ids = getParametro("idTipoProcessoDocumentoPautaAudienciaOuJulgamento");
    	String id = ArrayUtil.get(ids, ",", 0);
    	
        return getEntityPelaPK(TipoProcessoDocumento.class, id);
    }
    
    @Factory(scope = ScopeType.EVENT)
    public boolean isBaseBinariaUnificada() {
    	return "true".equalsIgnoreCase(ParametroUtil.getParametro(Parametros.UTILIZAR_BASE_BINARIA_UNIFICADA));
    }
    
	@Factory(scope = ScopeType.EVENT)
	public Integer getQtdDiasAlertaExpiracaoCertificado() {
		Integer qtdDiasAlertaExpiracaoCertificado = null;
		try {
			qtdDiasAlertaExpiracaoCertificado = new Integer(getParametro("qtdDiasAlertaExpiracaoCertificado"));
			return qtdDiasAlertaExpiracaoCertificado < 0 ? null
					: qtdDiasAlertaExpiracaoCertificado;
		} catch (Exception e) {
			return null;
		}
	}
	

	@Factory(scope = ScopeType.EVENT)
    public boolean isHabilitaVisualizacaoSigilososFluxoDeslocado() {
    	return "true".equalsIgnoreCase(ParametroUtil.getParametro(Parametros.HABILITA_VISUALIZACAO_SIGILOSO_FLUXO_DESLOCADO));
    }





	/**
	 * Método responsável por recuperar o valor contido no parâmetro "pje:lista:situacaoJulgamento".
	 *  
	 * @return Lista de situações de julgamento (separadas por vírgula) que podem gerar certidões.
	 * Essa lista <b>DEVE</b> estar cadastrada no formato <b>XX[,XX]*</b>. Caso não esteja, será retornado NULL.
	 */
	public String getListaSituacaoJulgamento() {
		String listSituacaoJulgamento = ParametroUtil.getParametro(Parametros.PJE_LISTA_SITUACAO_JULGAMENTO);
		
		if (StringUtils.isNotEmpty(listSituacaoJulgamento)) {
			listSituacaoJulgamento = listSituacaoJulgamento.trim();
			
			if (Pattern.compile("\\D{2}(,\\s*{1}\\D{2})*").matcher(listSituacaoJulgamento).matches()) {
				return listSituacaoJulgamento;
			}
		}
		
        return null;
    }

	/**
	 * Indica se a aplicação está configurada para funcionar em modo produção.
	 * Basicamente verifica se o parâmetro de producao está setado para true.
	 * 
	 * @return <code>true</code> caso a aplicação esteja configurada para rodar
	 *         em produção ou o parâmetro não esteja setado (ver pom.xml). Neste
	 *         caso considera-se que é uma versão de produção, pois em
	 *         desenvolvimento alguma opções de testes são liberadas
	 * 
	 *         <code>false</code> caso o parâmetro esteja setado como
	 *         false.
	 * 
	 * @see pom.xml
	 * 
	 */
    public boolean isAplicacaoModoProducao() {    	
    	Boolean producao = true;
        String parametroProducao = (String) Component.getInstance("producao", ScopeType.APPLICATION);
		if (parametroProducao != null && Boolean.FALSE.toString().equals(parametroProducao)) {
            producao = false;
        }
        return producao;
    }

	public TipoProcessoDocumento getTipoProcessoDocumentoNotasOrais() {
		 return getEntity(TipoProcessoDocumento.class,
				 Parametros.ID_TIPO_DOCUMENTO_NOTAS_ORAIS);
	}
	
	@Factory(scope=ScopeType.EVENT)
	public Integer[] getIdsTipoDocumentoVoto() {
		final Integer[] NO_IDS = new Integer[0];
		
		String idsTipoDocumentoVoto = ParametroUtil.getParametro(Parametros.IDS_TIPOS_VOTO);
		if (StringUtils.isBlank(idsTipoDocumentoVoto)) {
			try {
				idsTipoDocumentoVoto = getParametro(Parametros.IDS_TIPOS_VOTO);
			} catch (IllegalArgumentException ex) {
				log.error(ex.getMessage());
				return NO_IDS;
			}
		}
		return Util.converterStringIdsToIntegerArray(idsTipoDocumentoVoto);
	}
	
	@Factory(scope=ScopeType.SESSION)
	public Integer[] getIdsTipoDocumentoVotoVogalPainelMagistrado() {
		final Integer[] NO_IDS = new Integer[0];
		
		String idsTipoDocumentoVotoVogal = ParametroUtil.getFromContext(Parametros.IDS_TIPOS_VOTO_VOGAL_PAINEL_MAGISTRADO, true);
		if (StringUtils.isBlank(idsTipoDocumentoVotoVogal)) {
			try {
				idsTipoDocumentoVotoVogal = getParametro(Parametros.IDS_TIPOS_VOTO_VOGAL_PAINEL_MAGISTRADO);
			} catch (IllegalArgumentException ex) {
				log.error(ex.getMessage());
				return NO_IDS;
			}
		}
		return Util.converterStringIdsToIntegerArray(idsTipoDocumentoVotoVogal);
	}
		
	/**
	 * Parametro que define os ips permitidos para acessar os serviços de integração do pje1
	 * @return String os ips separados por ;
	 */
	@Factory(scope = ScopeType.APPLICATION)
	public String[] getIPsPermitidosPje2(){
		String ips = getParametro(Parametros.IPS_PERMITIDOS_PJE2);
		if (ips!=null)
			return ips.split(";");
		return null;
	}
	
	/**
	 * Parametro que define se o processo será ou não bloqueado após ser remetido
	 * @return
	 */
	@Factory(scope = ScopeType.EVENT)
	public boolean isBloquearProcessoRemetido(){
		return "true".equalsIgnoreCase(ParametroUtil.getParametro(Parametros.BLOQUEAR_PROCESSO_REMETIDO));
	}
	
	@Factory(scope = ScopeType.EVENT)
    public Pessoa getPessoaDestinacaoCienciaPublica() {
        return getEntity(Pessoa.class, Parametros.ID_DESTINACAO_PESSOA_CIENCIA_PUBLICA);
    }

	/**
	 * Recupera o tipo de documento utilizado na criação da certidão de publicação.
	 * 
	 * @return {@link TipoProcessoDocumento}.
	 */
	public TipoProcessoDocumento getTipoProcessoDocumentoCertidaoPublicacao() {
		return getEntity(TipoProcessoDocumento.class, "idTipoProcessoDocumentoCertidaoPublicacao");
	}
	
	/**
	 * Parametro que define se a certidão de julgamento irá mostrar o juiz substituto
	 * @return
	 */
	@Factory(scope = ScopeType.EVENT)
	public boolean isMostrarJuizSubstitutoNaCertidao(){
		return "true".equals(ParametroUtil.getParametro("pje:certidaoJulgamento:mostrarSubstituto"));
	}
	
	/**
	 * Parametro que define se os captchas estarão ativados ou não somente em ambientes diferente de produção.
	 * @return Defaut True ou False caso o parametro 'captchaHabilitado' esteja setado para false
	 */
	public boolean isCaptchaHabilitado(){
		Boolean captchaHabilitado = Boolean.TRUE;
		if(!isAplicacaoModoProducao()){
			try{
				String parametroCaptchaHabilitado = getParametro(Parametros.CAPTCHA_HABILITADO);
				return !(parametroCaptchaHabilitado != null && Boolean.FALSE.toString().equalsIgnoreCase(parametroCaptchaHabilitado));
			}catch (Exception e){
				log.warn(e.getMessage());
			}
		}
		return captchaHabilitado;
	}

    /**
	 * Tenta Recupera o parametro habilitarPJeOffice para definir se o aplicativo PJeOffice sera utilizado, se 
	 * o parametro nao existir retorna false
	 * @return
	 */
	public boolean isHabilitadoPJeOffice() {
		try {
			return Boolean.valueOf(getParametro("habilitarPJeOffice"));
		}
		catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Recupera a URL do servidor que hospeda a aplicacao PJeOffice
	 * @return
	 */
	public String getPjeOfficeUrl() {
		try {
			return getParametro("pjeOfficeUrl");
		}
		catch (Exception e) {
			return "http://www.cnj.jus.br/pjeOffice/";
		}
	}

	/**
	 * Parâmetro que define se os usuário que não são magistrados poderão
	 * visualizar o conteúdo dos votos de forma antecipada antes da
	 * finalalização do julgamento.
	 */
	public boolean isOcultarVotosAntecipadosNaoMagistrado() {
		return "true".equalsIgnoreCase(ParametroUtil.getParametro(Parametros.OCULTAR_VOTOS_ANTECIPADOS_NAO_MAGISTRADO));		
	}

	@Factory(scope = ScopeType.EVENT)
    public Papel getPapelProcuradorChefe() {
        return getEntity(Papel.class, Parametros.ID_PAPEL_PROCURADOR_CHEFE);
    }
	
	/**
	 * @return ID do registro EnderecoWSDL com o endpoint da instalação atual.
	 */
	@Factory(scope = ScopeType.EVENT)
    public EnderecoWsdl getEnderecoWsdlAplicacaoOrigem() {
		String parametro = "idEnderecoWsdlAplicacaoOrigem";
		EnderecoWsdl resultado = getEntity(EnderecoWsdl.class, parametro);
		
		validarEntidadeEnderecoWsdl(resultado, parametro);
		return resultado;
    }
	
	/**
	 * @return ID do registro EnderecoWSDL com o endpoint da instalação do PJE de integração.
	 */
	@Factory(scope = ScopeType.EVENT)
    public EnderecoWsdl getEnderecoWsdlIntegracao() {
		String parametro = "idEnderecoWsdlIntegracao";
		EnderecoWsdl resultado = getEntity(EnderecoWsdl.class, parametro);
		
		validarEntidadeEnderecoWsdl(resultado, parametro);
		return resultado;
    }
	
	/**
	 * Valida se o parâmetro configurado para EnderecoWsdl possui valores reais.
	 * 
	 * @param endereco EnderecoWsdl
	 * @param parametro Nome do parâmetro.
	 */
	private void validarEntidadeEnderecoWsdl(EnderecoWsdl endereco, String parametro) {
		String mensagem = "";
		if (endereco == null) {
			mensagem = "O parâmetro %s não foi definido, favor configurá-lo com o registro da funcionalidade Endereço WSDL.\n";
			mensagem = String.format(mensagem, parametro);
		} else {
			if (StringUtils.isBlank(endereco.getWsdlConsulta())) {
				mensagem = "O atributo wsdlConsulta não foi atribuído ao Endereço WSDL '%s'.\n";
				mensagem = String.format(mensagem, endereco.getDescricao());
			}
			
			if (StringUtils.isBlank(endereco.getWsdlIntercomunicacao())) {
				mensagem += "O atributo wsdlIntercomunicacao não foi atribuído ao Endereço WSDL '%s'.\n";
				mensagem = String.format(mensagem, endereco.getDescricao());
			}
		}
		
		if (StringUtils.isNotBlank(mensagem)) {
			throw new AplicationException(mensagem);
		}
	}
	
	/**
	 * @return Tipo de documento 'Comunicação entre instâncias'.
	 */
	public TipoProcessoDocumento getTipoProcessoDocumentoComunicacaoEntreInstancias() {
        return getEntity(TipoProcessoDocumento.class,
            "idTipoProcessoDocumentoComunicacaoEntreInstancias");
    }
	
	/**
	 * @return Sigla da instalação.
	 */
	public String getSiglaTribunal() {
        return getParametro(Parametros.SIGLA_TRIBUNAL);
    }
	
	/**
	 * @return Fluxo de comunicacação entre instâncias.
	 */
	@Factory(scope = ScopeType.EVENT)
    public Fluxo getFluxoComunicacaoEntreInstancias() {
        return getEntity(Fluxo.class, "pje:fluxo:comunicacaoEntreInstancias");
    }

	/**
	 * @return True se o PJe estiver configurado para disparar fluxo incidental.
	 */
	public Boolean isSempreDispararFluxoIncidental() {
		String parametro = getParametro(Parametros.SEMPRE_DISPARAR_FLUXO_INCIDENTAL);
		return BooleanUtils.toBoolean(parametro);
	}
	
	@Factory(scope = ScopeType.APPLICATION)
	public String getPrefixoUrlExterna() {
		return getParametro(Parametros.PJE_PREFIXO_URL_EXTERNA);
	}

	@Factory(scope = ScopeType.EVENT)
	public List<Pessoa> getFiscaisDaLei() {
		List<Pessoa> fiscaisDaLei = new ArrayList<>();
		String pjeFiscalDaLei = getParametro(Parametros.PJE_FISCAL_DA_LEI);
		if (StringUtils.isNotEmpty(pjeFiscalDaLei)) {
			String[] idsFiscaisDaLei = pjeFiscalDaLei.split(",");
			if (idsFiscaisDaLei != null && idsFiscaisDaLei.length > 0) {
				Pessoa fiscalDaLei = null;
				for (String idFiscalDaLei : idsFiscaisDaLei) {
					fiscalDaLei = getEntityPelaPK(Pessoa.class, idFiscalDaLei.trim());
					if (fiscalDaLei != null) {
						fiscaisDaLei.add(fiscalDaLei);
					}
				}
			}
		}
		return fiscaisDaLei;
	}
	
	@Factory(scope = ScopeType.EVENT)
	public Pessoa getFiscalDaLei() {
		Pessoa fiscalDaLei = null;
		List<Pessoa> fiscaisDaLei = this.getFiscaisDaLei();
		if (CollectionUtils.isNotEmpty(fiscaisDaLei) && fiscaisDaLei.size() == 1) {
			fiscalDaLei = fiscaisDaLei.get(0);
		}
		return fiscalDaLei;
	}

	/**
	 * Recupera o codigo de seguranca para utilizar o PJeOffice.
	 * @return
	 */
	public String getPjeOfficeCodigoSeguranca() {
		try {
			return getParametro("pjeOfficeCodigoSeguranca");
		}
		catch (Exception e) {
			return "";
		}
	}
	
	/**
	 * Recupera quantidade máxima de versões para um documento
	 * Caso não exista o parâmetro ou valor dele seja zero não haverá limite para a quantidade de versões que será guardada
	 * @return int
	 */
    public Integer getQuantidadeMaximaVersoesDocumento() {
    	try {
    		return Integer.parseInt(getParametro(Parametros.QUANTIDADE_MAXIMA_VERSOES_DOCUMENTO));
    	} catch(Exception e) {
    		return -1; // Quando não houver parâmetro não haverá limite
    	}
    }
    
    /**
     * Retorna true se a instalação for da justiça do trabalho.
     * 
     * @return boleano
     */
    public boolean isJusticaTrabalhista() {
    	return "jt".equalsIgnoreCase(ParametroUtil.getParametro(Parametros.TIPOJUSTICA));
    }
    
    /**
     * Retorna a presunção de entrega de correspondência.
     * 
     * @return string
     */
    public String getPresuncaoEntregaCorrespondencia() {
    	return getParametro(Parametros.PRESUNCAO_ENTREGA_CORRESPONDENCIA);
    }
    
	/**
	 * Recupera qual o editor utilizado no sistema (CKEditor ou TinyMCE ou Lool)
	 * @return
	 */
	public TipoEditorEnum getEditor() {
		TipoEditorEnum tipoEditorPadrao = TipoEditorEnum.T;
		String tipoEditorParametro = tipoEditorPadrao.toString();
		try {
			tipoEditorParametro = getParametro(Parametros.PJE_SISTEMA_EDITOR);
			if( tipoEditorParametro != null && !tipoEditorParametro.isEmpty()) {
				tipoEditorPadrao = TipoEditorEnum.valueOf(tipoEditorParametro);
			}
		}
		catch (Exception e) {
			
		}
		return tipoEditorPadrao; 
	}
	
	/**
	 * Parametro utilizado para chavear o uso do novo painel do magistrado na sessão.
	 * 
	 * @return true caso se deva usar a nova tela, false caso contrario.
	 */
	public boolean isUsarNovoPainelMagistradoSessao(){
		Boolean retorno = Boolean.FALSE;
		String valor = getParametro(Parametros.PJE_PAINEL_MAGISTRADO_SESSAO_NOVO);
		
		if(StringUtils.isNotBlank(valor) && (valor.equalsIgnoreCase("t") || "true".equalsIgnoreCase(valor))){
			retorno = Boolean.TRUE;
		}
		
		return retorno;
	}
	
	@Factory(scope = ScopeType.EVENT)
    public TipoModeloDocumento getTipoModeloDocumentoCKEditorRaiz() {
        return getEntity(TipoModeloDocumento.class, Parametros.PJE_SISTEMA_TIPO_MODELO_DOCUMENTO_CKEDITOR);
    }
    
    /**
     * Retorna a presunção de entrega de correspondência.
     * 
     * @return string
     */
    public Integer getPresuncaoEntregaCorrespondenciaInteger() {
    	Integer resultado = 0;
    	String string = getPresuncaoEntregaCorrespondencia();
    	
    	if (NumberUtils.isNumber(string)) {
    		resultado = NumberUtils.createInteger(string);
    	}
    	return resultado;
    }
            
    /**
     * Verifica se a eh Justica Eleitoral e se o parametro de agrupamento prevencao 260 esta ativo 
     * @return verdadeiro se JE e parametro ativo
     */
	public boolean isParametroPrevencaoAtivoNaJusticaEleitoral() {
		return ParametroJtUtil.instance().justicaEleitoral() && isParametroAgrupamentoPrevencaoEleicaoAtivo();
	}
	
	/**
	 * Metodo que verifica se o parametro LISTA_AGRUPAMENTO_PREVENCAO_260 esta ativo e com valor valido.
	 * @return True se ativo e com valor
	 */
	private boolean isParametroAgrupamentoPrevencaoEleicaoAtivo(){
		ParametroService parametroService = ComponentUtil.getComponent(ParametroService.NAME);
		String parametro = parametroService.valueOf(Parametros.LISTA_AGRUPAMENTO_PREVENCAO_260);
		return StringUtils.isNotBlank(parametro);
	}

	/**
	 * Metodo responsavel por verificar se os parametros para o CKEditor estao
	 * definidos
	 * 
	 * @return <code>True</code>, caso os parametros estejam definidos
	 */
	public boolean isParametrosCKEditorDefinidos() {
		return (this.getEditor() == TipoEditorEnum.C && this.getTipoModeloDocumentoCKEditorRaiz() != null);
	}
	
	@Factory(scope = ScopeType.APPLICATION)
	public String getNomeInstancia() {
		String nomeInstanciaParam = StringUtils.EMPTY;
		if (ParametroUtil.getParametro(Parametros.NUMERO_INSTANCIA) != null) {
			nomeInstanciaParam = String.format("[%s]", ParametroUtil.getParametro(Parametros.NUMERO_INSTANCIA));
		}
		return nomeInstanciaParam;
	}

	@Factory(scope = ScopeType.APPLICATION)
	public String getNomeSistema() {
		return ParametroUtil.getParametro(Parametros.NOME_SISTEMA);
	}
	
	@Factory(scope = ScopeType.APPLICATION)
	public String getProducao() {
	    String property = ParametroUtil.getParametro("pje.producao");
	    property = (property == null ? "true" : property);
	    return property;
	}
	
	/**
	 * @return Retorna true se o MNI estiver habilitado.
	 */
	public static Boolean isMNIHabilitado() {
		String property = ParametroUtil.getParametro("mni.habilitado");
		property = (property == null ? "true" : property);
	    return Boolean.valueOf(property);
	}
	
	/**
	 * Parâmetro que indica se o placar de votação mostrará o nome do magistrado ou o nome do órgão julgador (comportamento padrão).
	 */
	public boolean mostrarNomeMagistradoLabelPlacar() {
		return Boolean.parseBoolean((String)Component.getInstance(Parametros.MOSTRAR_NOME_MAGISTRADO_LABEL_PLACAR));
	}
	
	public TipoPrazoEnum recuperarTipoPrazoPublicacaoSessaoMural() {
		TipoPrazoEnum retorno = null;
		try {
			retorno = TipoPrazoEnum.obter(getParametro(Parametros.TIPO_PRAZO_PUBLICACAO_SESSAO_MURAL));
			if(retorno == null){
				retorno = TipoPrazoEnum.D;
			}
		} catch (Exception e) {
			retorno = TipoPrazoEnum.D;
		}
		return retorno;
	}
	
	public Integer recuperarPrazoPublicacaoSessaoMural() {
		Integer retorno = null;
		String parametro = getParametro(Parametros.PRAZO_PUBLICACAO_SESSAO_MURAL);
		if(parametro != null && !parametro.isEmpty()){
			retorno = Integer.parseInt(parametro);
		}
		return retorno;
	}
	
	public String recuperarNumeroOrgaoJustica() {
		String retorno = "";
		try {
			retorno = getParametro(Parametros.NUMERO_ORGAO_JUSTICA);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return retorno; 
	}
	
	public String recuperarUrlServicoMural() {
		String retorno = "";
		try {
			retorno = getParametro(Parametros.URL_SERVICO_MURAL);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return retorno; 
	}
	
	public String recuperarUrlPjeServico() {
		String retorno = "";
		try {
			retorno = getParametro(Parametros.URL_PJE_JE_SERVICO_WSDL);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return retorno; 
	}
	
	public boolean isReCaptchaAtivo() {
		return Boolean.parseBoolean((String)Component.getInstance(Parametros.RECAPTCHA_ATIVO));
	}
	
	public String obterReCaptchaSiteKey() {
		return (String)Component.getInstance(Parametros.RECAPTCHA_SITE_KEY);
	}
	
	@Factory(scope = ScopeType.EVENT)
    public ModeloDocumento getModeloEditorSessaoJulgamento() {
        return getEntity(ModeloDocumento.class, Parametros.PJE_MODELO_MINUTA_PREGAO);
    }
	
	public Integer getLimiteQuantidadeDocumentosBinariosConsultadosMNI() {
    	Integer retorno = null;
    	try {
    		retorno = Integer.valueOf(getParametro(Parametros.PJE_MNI_LIMITE_QTD_BINARIO_MNI));
    	} catch (Exception e) {
			log.error(e.getMessage());
		}
        return retorno;
    }

	public String obterReCaptchaSecretKey() {
		return (String)Component.getInstance(Parametros.RECAPTCHA_SECRET_KEY);
	}
	
	public String getIdsOrgaosJulgadoresVisualizacaoUltimaDistribuicao() {
		return getFromContext(Parametros.IDS_ORGAOS_JULGADORES_PERMITIDOS_VISUALIZAR_ULTIMA_DISTRIBUICAO, true);
	}
	
	public boolean isSessaoHabilitarAcoesEmVotacaoAntecipada() {
		return Boolean.parseBoolean((String)Component.getInstance(Parametros.PJE_SESSAO_HABILITAR_ACOES_EM_VOTACAO_ANTECIPADA));
	}

	@Factory(scope = ScopeType.APPLICATION)
	public String getMobileTokenSecret() {
		return getFromContext("mobileTokenSecret", true);
	}

	@Factory(scope = ScopeType.APPLICATION)
	public Long getMobileTokenTempoExpiracao() {
		String tempoExpira = getFromContext("mobileTokenTempoExpiracao", true);
		if(tempoExpira != null){
			return Long.valueOf(tempoExpira);
		}
		return null;
	}
	
	public boolean isJusticaEleitoralAndPrimeiroGrau() {
		return ParametroJtUtil.instance().justicaEleitoral() && ParametroUtil.instance().isPrimeiroGrau();
	}
	
	public boolean getIntegracaoConsumidorGovBr() {
		return BooleanUtils.toBoolean((String)Component.getInstance(Parametros.INTEGRACAO_CONSUMIDOR_GOV_BR));
	}

	public boolean isPermiteAtualizarDataJuntada() {
		boolean ret = false;
		try {
			String param = getParametro(Parametros.PJE_FLUXO_TRASLADO_DOCUMENTOS_PERMITE_ATUALIZAR_DATA_JUNTADA);
			ret = Boolean.parseBoolean(param);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return ret;
	}
	
	public String getIdPeticaoInicialTrasladada() {
		try {
			return getParametro(Parametros.PJE_DOCUMENTO_TRASLADO_PETICAO_INICIAL_TRASLADADA);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return null;
	}
	
	public String getIdMovimentoTraslado() {
		try {
			return getParametro(Parametros.PJE_MOVIMENTO_TRASLADO_DOCUMENTO);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return null;
	}

	public boolean isConteudoSigiloso() {
		return Boolean.parseBoolean((String)Component.getInstance(Parametros.PJE_CONTEUDO_SIGILOSO));
	}	
	public boolean isLiberaDocumentoSessaoAssinatura(){
		boolean retorno = true;
		String retornoString = "";
		try {
			retornoString = getParametro(Parametros.PJE_VISIBILIDADE_DOCUMENTO_SESSAO_ASSINATURA);
			if(retornoString != null){
				retorno = Boolean.parseBoolean(retornoString);
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return retorno;
	}
	
    public String cssCkEditor() {
		String retorno = "";
		try {
			retorno = getParametro(Parametros.PJE_SISTEMA_EDITOR_CSS_CKEDITOR);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return retorno; 
    }
    
    @Factory(scope = ScopeType.EVENT)
    public TipoProcessoDocumento getTipoProcessoDocumentoRPV() {
        return getEntity(TipoProcessoDocumento.class,"idTipoDocumentoRPV");
    }
    
    @Factory(scope = ScopeType.EVENT)
    public TipoProcessoDocumento getTipoProcessoDocumentoPrecatorio() {
        return getEntity(TipoProcessoDocumento.class,"idTipoDocumentoPrecatorio");
    }

    public String cssVisualizarDocumento() {
		String retorno = "";
		try {
			retorno = getParametro(Parametros.PJE_SISTEMA_EDITOR_CSS_VISUALIZAR_DOCUMENTO);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return retorno; 
    }

    public String cssPDFDocumento() {
		String retorno = "";
		try {
			retorno = getParametro(Parametros.PJE_SISTEMA_EDITOR_CSS_PDF_DOCUMENTO);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return retorno; 
   }   
    
    public Pessoa getPessoaINSS() {
    	return getEntity(Pessoa.class, Parametros.ID_PESSOA_INSS);
    }


	public Boolean isEditorLibreOfficeHabilitado() {
        return Boolean.valueOf(ParametroUtil.getParametro("editorLibreOfficeHabilitado"));
    }
	
	public ModeloDocumento getModeloDocumentoBaseLibreOffice() {
		return getEntity(ModeloDocumento.class, Parametros.PJE_DOCUMENTO_MODELO_BASE_LIBRE_OFFICE);
	}

	public ModeloDocumento getModeloDocumentoCertidaoCienciaDomicilio() {
		return getEntity(ModeloDocumento.class, Parametros.PJE_DOCUMENTO_MODELO_CERTIDAO_CIENCIA_DOMICILIO);
	}


	@Factory(scope = ScopeType.EVENT)
    public Papel getPapelServidor() {
        return getEntity(Papel.class, Parametros.ID_PAPEL_SERVIDOR);
    }

	@Factory(scope = ScopeType.EVENT)
    public Papel getPapelParaRemessaViaPessoaJuridica() {
        return getEntity(Papel.class, Parametros.ID_PAPEL_PARA_REMESSA_VIA_PJ);
	}
	
    public Integer getTamanhoLoteProcessosPush() {
        String tamanhoLoteProcessos = ParametroUtil.getParametro("pje:jobs:push:tamanhoLoteProcessos");

        if (tamanhoLoteProcessos != null) {
            return Integer.parseInt(tamanhoLoteProcessos);
        } else {
            log.warn("Parametro \"pje:jobs:push:tamanhoLoteProcessos\" nao foi encontrado na tabela de parametros do banco de dados.");
        }

        return null;

    }

	public static List<String> getInstanciasHabilitadasComSSO() {
		try {
			String nomeParametro = "sso:instancias:habilitadas";
			String valorParametro = (String) Contexts.getApplicationContext().get(nomeParametro);
			if (valorParametro == null) {
				return Collections.emptyList();
			} else {
				List<String> instanciasHabilitadasComSSO = StringUtil.stringToList(valorParametro, ",");
				return instanciasHabilitadasComSSO;
			}
		} catch (Exception e) {
			log.error("Erro ao recuperar parâmetro de instâncias habilitadas com SSO.", e);
			return Collections.emptyList();
		}
	}

	public String getUrlPDPJMarketplace() {
		try {
			return getParametro(Parametros.URL_PDPJ_MARKETPLACE);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return null;
	}

	public Boolean isDesativaSinalizacaoMovimentacaoAudiencia() {
		return Boolean.valueOf(ParametroUtil.getParametro(Parametros.PJE_DESATIVA_SINALIZACAO_MOVIMENTACAO_AUDIENCIA));
	}

	/*
	 * Os dominios validos serao recuperados na variavel Parametros.DOMINIO_ALGORITMOS_HASH_VALIDOS.
	 * Esses deverao ser definidos conforme exemplo:
	 *          [SHA1, SHA-1];[SHA256, SHA-256];[SHA256withRSA, SHA-256];
	 */
	@Factory(scope = ScopeType.EVENT)
	public Map<String, String> getDominioAlgoritmoHashValidos(){
		String dominioAlgoritmosHashValidos = getParametro(Parametros.DOMINIO_ALGORITMOS_HASH_VALIDOS);
	    if (Strings.isEmpty(dominioAlgoritmosHashValidos)) {
            return new HashMap<String, String>();
        }
	    Map<String, String> mapaAlgoritmo = new CaseInsensitiveMap();
        String[] dominios = dominioAlgoritmosHashValidos.split(";");
        for( String dominio : dominios) {
        	try {
        		dominio = dominio.replaceAll("\\[", "").replaceAll("\\]", "");
        		mapaAlgoritmo.put(dominio.split(",")[0].trim(), dominio.split(",")[1].trim());
        	}catch(PatternSyntaxException |  IndexOutOfBoundsException e) {
        		throw new NegocioException("Parmetro " + Parametros.DOMINIO_ALGORITMOS_HASH_VALIDOS + " mal formado. ");
        	}
        }
        return mapaAlgoritmo;
	}

	/*
	* Os dominios validos serao recuperados na variavel Parametros.DOMINIO_ALGORITMOS_HASH_RSA_VALIDOS.
	* Esses deverao ser definidos conforme exemplo:
	*          [SHA1, SHA-1];[SHA256, SHA-256];[SHA256withRSA, SHA256withRSA];
	*/
	@Factory(scope = ScopeType.EVENT)
	public Map<String, String> getDominioAlgoritmoHashRSAValidos(){
	    String dominioAlgoritmosHashRSAValidos = getParametro(Parametros.DOMINIO_ALGORITMOS_HASH_RSA_VALIDOS);
	    if (Strings.isEmpty(dominioAlgoritmosHashRSAValidos)) {
	        return new HashMap<String, String>();
	    }
	    Map<String, String> mapaAlgoritmo = new CaseInsensitiveMap();
	    String[] dominios = dominioAlgoritmosHashRSAValidos.split(";");
	    for( String dominio : dominios) {
	        try {
	            dominio = dominio.replaceAll("\\[", "").replaceAll("\\]", "");
	            mapaAlgoritmo.put(dominio.split(",")[0].trim(), dominio.split(",")[1].trim());
	        }catch(PatternSyntaxException |  IndexOutOfBoundsException e) {
	            throw new NegocioException("Parmetro " + Parametros.DOMINIO_ALGORITMOS_HASH_RSA_VALIDOS + " mal formado. ");
	        }
	    }
	    return mapaAlgoritmo;
	}

	@SuppressWarnings("unchecked")
    public static String getParametroPor(String likeNome,String valor) {
		String parametro=null;
	        EntityManager em = EntityUtil.getEntityManager();
	        List<Parametro> resultList = em.createQuery(
	                "select p from Parametro p where nomeVariavel like :nome and valorVariavel = :valor and ativo = true")
	                                       .setParameter("nome", "%"+likeNome+"%")
	                                       .setParameter("valor", valor)
	                                       .getResultList();

	        if (!resultList.isEmpty()) {
	            parametro = resultList.get(0).getNomeVariavel();
	        }
	        else {
	        	log.warn("Parametro {0} não cadastrado na base", likeNome);
	        }
        return parametro;
    }

	@Factory(scope = ScopeType.EVENT)
	public boolean isOrdenarTransicoesAlfabeticamente() {
		boolean ret = false;

		try {
			String param = getParametro("pje:fluxo:ordenarTransicoesAlfabeticamente");
			 if ("true".equals(param)) {
	                ret = true;
	            }
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return ret;
	}

	/**
	 * @return True se a integração com o domicílio estiver habilitada.
	 */
	public Boolean isDomicilioEletronicoHabilitado() {
		return BooleanUtils
				.toBoolean(ComponentUtil.getParametroDAO().valueOf(Parametros.PDPJ_INTEGRACAO_DOMICILIOELETRONICO));
	}

	/**
	 * @return Client-ID do Domicílio Eletrônico.
	 */
	public String getDomicilioEletronicoClientId() {
        return ComponentUtil.getParametroDAO()
				.valueOf(Parametros.PDPJ_INTEGRACAO_DOMICILIOELETRONICO_CLIENTID);
    }

	/**
	 * @return Secret do Domicílio Eletrônico.
	 */
	public String getDomicilioEletronicoSecret() {
        return ComponentUtil.getParametroDAO()
				.valueOf(Parametros.PDPJ_INTEGRACAO_DOMICILIOELETRONICO_SECRET);
    }

	/**
	 * @return ServiceName do Domicílio Eletrônico.
	 */
	public String getDomicilioEletronicoServiceName() {
        return ComponentUtil.getParametroDAO()
				.valueOf(Parametros.PDPJ_INTEGRACAO_DOMICILIOELETRONICO_SERVICENAME);
    }

	/**
	 * @return ServiceName da Comunicação Processual.
	 */
	public String getDomicilioEletronicoComunicacaoProcessualServiceName() {
        return ComponentUtil.getParametroDAO()
				.valueOf(Parametros.PDPJ_INTEGRACAO_DOMICILIOELETRONICO_COMUNICACAOPROCESSUAL_SERVICENAME);
    }

	/**
	 * @return Pos-fixo da URL verificacao de Online do servico do Domicilio Eletronico.
	 */
	public String getDomicilioEletronicoServiceOnlineCheck() {
		String valor = getParametro(Parametros.PDPJ_INTEGRACAO_DOMICILIOELETRONICO_SERVICE_ONLINE_CHECK);
		if (StringUtils.isNotEmpty(valor)) {
			return valor;
		}
        return "/actuator/info";
    }

	/**
	 * @return Pos-fixo da URL verificacao de Online do servico da Comunicacao Processual.
	 */
	public String getDomicilioEletronicoComunicacaoProcessualServiceOnlineCheck() {
		String valor = getParametro(Parametros.PDPJ_INTEGRACAO_DOMICILIOELETRONICO_COMUNICACAOPROCESSUAL_SERVICE_ONLINE_CHECK);
        if (StringUtils.isNotEmpty(valor)) {
			return valor;
		}
        return "/actuator/info";
    }

	/**
	 * @return Data e Hora da última execução do JOB do TCD.
	 */
	public Timestamp getDataHoraUltimaExecucaoJobTCD() {
		String dataHoraUltimaExecucaoJob = ComponentUtil.getParametroDAO()
				.valueOf(Parametros.PJE_DOMICILIO_ELETRONICO_DATA_HORA_ULTIMA_EXECUCAO_JOB_TCD);

		// SE A DATA E HORA NÃO ESTIVER DISPONÍVEL:
		// SE A DATA CADASTRADA NÃO ESTIVER NO FORTADO DESEJADO:
		// DEFINE A DATA E HORA PADRÃO NO INÍCIO DO DIA
		if (StringUtils.isBlank(dataHoraUltimaExecucaoJob)
				|| !isValidFormat(dataHoraUltimaExecucaoJob, "yyyy-MM-dd HH:mm:ss")) {
			dataHoraUltimaExecucaoJob = getDataHoraInicioDoDiaString();
		}

		// CONVERTE PARA LOCALDATETIME
		LocalDateTime dataHoraUltimaExecucaoJobTCILocalDateTime = LocalDateTime.parse(
				dataHoraUltimaExecucaoJob,
				DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
		);

		// SUBTRAI 10 MINUTOS PARA GARANTIR QUE OS EXPEDIENTES QUE ESTAVAM SENDO
		// PROCESSADOS ENTREM NA LISTA
		dataHoraUltimaExecucaoJobTCILocalDateTime.minusMinutes(10);

		// CONVERTE PARA TIMESTAMP
		return Timestamp.valueOf(dataHoraUltimaExecucaoJobTCILocalDateTime);
	}

	private String getDataHoraInicioDoDiaString() {
		return LocalDateTime.now()
                            .toLocalDate()
                            .atStartOfDay()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
	}

	/**
	 *  MÉTODO CRIADO PARA EVITAR EXECEÇÃO POR DATA CADASTRADA FORA DO PADRÃO
	 */
	public static boolean isValidFormat(String data, String formato) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formato);

		try {
			LocalDateTime.parse(data, formatter);
			// SE CHEGOU AQUI, A STRING ESTÁ NO FORMATO DESEJADO
			return true;
		} catch (DateTimeParseException e) {
			// A EXCEÇÃO SERÁ LANÇADA SE A STRING NÃO ESTIVER NO FORMATO DESEJADO
			return false;
		}
	}

	/**
	 * @return True se a busca por pessoa no domicílio estiver habilitada.
	 */
	public boolean isDomicilioEletronicoCacheConsultaOnlineHabilitada() {
		return BooleanUtils.toBoolean(getParametro(Parametros.PDPJ_INTEGRACAO_DOMICILIOELETRONICO_CACHE_CONSULTA_ONLINE_HABILITADA));
	}

	/**
	 * @return Tribunal de Origem
	 */
	public String getDomicilioEletronicoTribunalOrigem() {
		return ComponentUtil.getParametroDAO().valueOf(Parametros.PJE_DOMICILIO_ELETRONICO_TRIBUNAL_ORIGEM);
	}

	public static Set<String> getParametrosNaoConfigurados() {
		return parametrosNaoConfigurados;
	}

	public static boolean isParametroNaoConfigurado(String nome) {
		return getParametrosNaoConfigurados().contains(nome);
	}

    public boolean isAssociaCompetenciaSalaAudiencia() {
        return  BooleanUtils.toBoolean(ComponentUtil.getParametroDAO().valueOf(Parametros.PJE_ASSOCIA_COMPETENCIA_SALA_AUDIENCIA));
    }

	@Factory(scope = ScopeType.EVENT)
    public TipoProcessoDocumento getTipoProcessoDocumentoLiminar() {
        return getEntity(TipoProcessoDocumento.class,
            "idTipoProcessoDocumentoLiminar");
    }

	@Factory(scope = ScopeType.EVENT)
    public TipoProcessoDocumento getTipoProcessoDocumentoObrigacaoDeFazer() {
        return getEntity(TipoProcessoDocumento.class,
            "idTipoProcessoDocumentoObrigacaoDeFazer");
    }

    /**
     * @return Habilita registro de ciencia pessoal ao abrir link do inteiro teor.
     */
    public boolean getDomicilioEletronicoRegistraCienciaLinkInteiroTeor() {
        return BooleanUtils.toBoolean(getParametro(Parametros.PDPJ_INTEGRACAO_DOMICILIOELETRONICO_REGISTRACIENCIA_LINK_INTEIROTEOR));
    }

    public String getNumeroOrgaoJustica() {
		return getParametro("numeroOrgaoJustica");
	}

    @Factory(scope = ScopeType.EVENT)
    public Papel getPapel(String identificadorPapel) {
        return getEntity(Papel.class, identificadorPapel);
    }

    /**
	 * @return True se o log de acesso aos autos deve ser ativado.
	*/
	public Boolean isLogAcessoAutosAtivo() {
        String parametro = getParametro(Parametros.PJE_ATIVAR_LOG_ACESSO_AUTOS);
		if (!StringUtils.isEmpty(parametro)) return BooleanUtils.toBoolean(parametro);
		return false;
    }


    /**
	 * @return True se o log de download de documentos deve ser ativado.
	*/
	public Boolean isLogDownloadDocumentosAtivo() {
        String parametro = getParametro(Parametros.PJE_ATIVAR_LOG_DOWNLOAD_DOCUMENTOS);
		if (!StringUtils.isEmpty(parametro)) return BooleanUtils.toBoolean(parametro);
		return false;
    }


    /**
     * @return True se o log deve ser ativado no MNI.
     */
    public Boolean isLogAcessoMNIAtivo() {
        String parametro = getParametro(Parametros.PJE_ATIVAR_LOG_ACESSO_MNI);
        if (!StringUtils.isEmpty(parametro)) return BooleanUtils.toBoolean(parametro);
        return false;
    }

}
