package br.jus.cnj.pje.util;

import java.util.Collection;
import java.util.logging.Logger;

import org.apache.cxf.binding.soap.Soap12;
import org.apache.cxf.binding.soap.SoapBindingConstants;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.binding.soap.interceptor.EndpointSelectionInterceptor;
import org.apache.cxf.binding.soap.interceptor.ReadHeadersInterceptor;
import org.apache.cxf.binding.soap.interceptor.SoapActionInInterceptor;
import org.apache.cxf.binding.soap.model.SoapOperationInfo;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageUtils;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.ws.addressing.JAXWSAConstants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.util.ParametroUtil;

/*
* Detalhes de implementação: Infelizmente a classe SoapActionInInterceptor permite nenhum tipo de extensão ou reaproveitamento de código, 
* pois usa métodos e propriedades privados, sem nenhum ponto de extensão. Foi copiado o código da lib cxf-rt-bindings-soap-2.7.13.jar, 
* classe SoapActionInInterceptor.java.
*/

@Scope(ScopeType.APPLICATION)
@Name("org.apache.cxf.binding.soap.interceptor.SoapActionInInterceptor")
@BypassInterceptors
public class PJESoapActionInInterceptor extends AbstractSoapInterceptor {
    private static final Logger LOG = LogUtils.getL7dLogger(PJESoapActionInInterceptor.class);
    private static final String ALLOW_NON_MATCHING_TO_DEFAULT = "allowNonMatchingToDefaultSoapAction";
    private static final String CALCULATED_WSA_ACTION = PJESoapActionInInterceptor.class.getName() + ".ACTION";
    public static final String PARAMETRO_BYPASS_SOAP_ACTION_MISMATCH = "bypass_soap_action_mismatch";

    // bypass_soap_action_mismatch deverá ser informado na subida do jboss
    // -Dbypass_soap_action_mismatch=true irá evitar o lançamento de exceção throw new Fault("SOAP_ACTION_MISMATCH", LOG, null, action);
    // -Dbypass_soap_action_mismatch=false ou a inexistência do parâmetro fará com que o comportamento seja o padrão 
    public static boolean bypass_soap_action_mismatch=true;
   
    static {
        if (System.getProperty(PARAMETRO_BYPASS_SOAP_ACTION_MISMATCH) != null) {
        	bypass_soap_action_mismatch = Boolean.valueOf(System.getProperty(PARAMETRO_BYPASS_SOAP_ACTION_MISMATCH));
        } 
    }
    
	public PJESoapActionInInterceptor() {
        super(Phase.READ);
        addAfter(ReadHeadersInterceptor.class.getName());
        addAfter(EndpointSelectionInterceptor.class.getName());
	}

    public static String getSoapAction(Message m) {
        return SoapActionInInterceptor.getSoapAction(m);
    }
    
    public void handleMessage(SoapMessage message) throws Fault {
        if (isRequestor(message)) {
            return;
        }
        
        verificarRequisicaoMNI(message);

        String action = getSoapAction(message);
        if (!StringUtils.isEmpty(action)) {
            getAndSetOperation(message, action);
            message.put(SoapBindingConstants.SOAP_ACTION, action);
        }
    }
    
    public static void getAndSetOperation(SoapMessage message, String action) {
        getAndSetOperation(message, action, true);
    }
    
    public static void getAndSetOperation(SoapMessage message, String action, boolean strict) {
        if (StringUtils.isEmpty(action)) {
            return;
        }
        
        Exchange ex = message.getExchange();
        Endpoint ep = ex.get(Endpoint.class);
        if (ep == null) {
            return;
        }
        
        BindingOperationInfo bindingOp = null;
        
        Collection<BindingOperationInfo> bops = ep.getEndpointInfo()
            .getBinding().getOperations();
        if (bops != null) {
            for (BindingOperationInfo boi : bops) {
                if (isActionMatch(message, boi, action)) {
                    if (bindingOp != null) {
                        // more than one op with the same action, will need to parse normally
                        return;
                    }
                    bindingOp = boi;
                }
                if (matchWSAAction(boi, action)) {
                    if (bindingOp != null && bindingOp != boi) {
                        //more than one op with the same action, will need to parse normally
                        return;
                    }
                    bindingOp = boi;
                }
            }
        }
        
        if (bindingOp == null) {
            if (strict) {
                //we didn't match the an operation, we'll try again later to make
                //sure the incoming message did end up matching an operation.
                //This could occur in some cases like WS-RM and WS-SecConv that will
                //intercept the message with a new endpoint/operation
                message.getInterceptorChain().add(new SoapActionInAttemptTwoInterceptor(action));
            }
            return;
        }
        
        ex.put(BindingOperationInfo.class, bindingOp);
        ex.put(OperationInfo.class, bindingOp.getOperationInfo());
    }
    
    private static boolean matchWSAAction(BindingOperationInfo boi, String action) {
        Object o = getWSAAction(boi);
        if (o != null) {
            String oa = o.toString();
            if (action.equals(oa)
                || action.equals(oa + "Request")
                || oa.equals(action + "Request")) {
                return true;
            }
        }
        return false;
    }
    
    private static String getWSAAction(BindingOperationInfo boi) {
        Object o = boi.getOperationInfo().getInput().getProperty(CALCULATED_WSA_ACTION);
        if (o == null) {
            o = boi.getOperationInfo().getInput().getExtensionAttribute(JAXWSAConstants.WSAM_ACTION_QNAME);
            if (o == null) {
                o = boi.getOperationInfo().getInput().getExtensionAttribute(JAXWSAConstants.WSAW_ACTION_QNAME);
            }
            if (o == null) {
                String start = getActionBaseUri(boi.getOperationInfo());
                if (null == boi.getOperationInfo().getInputName()) {
                    o = addPath(start, boi.getOperationInfo().getName().getLocalPart());
                } else {
                    o = addPath(start, boi.getOperationInfo().getInputName());
                }
            }
            if (o != null) {
                boi.getOperationInfo().getInput().setProperty(CALCULATED_WSA_ACTION, o);
            }
        }
        return o == null ? null : o.toString();
    }

    private static String getActionBaseUri(final OperationInfo operation) {
        String interfaceName = operation.getInterface().getName().getLocalPart();
        return addPath(operation.getName().getNamespaceURI(), interfaceName);
    }
    
    private static String getDelimiter(String uri) {
        if (uri.startsWith("urn")) {
            return ":";
        }
        return "/";
    }

    private static String addPath(String uri, String path) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(uri);
        String delimiter = getDelimiter(uri);
        if (!uri.endsWith(delimiter) && !path.startsWith(delimiter)) {
            buffer.append(delimiter);
        }
        buffer.append(path);
        return buffer.toString();
    }

    
    public static class SoapActionInAttemptTwoInterceptor extends AbstractSoapInterceptor {
        final String action;
        public SoapActionInAttemptTwoInterceptor(String action) {
            super(action, Phase.PRE_LOGICAL);
            this.action = action;
        }
        public void handleMessage(SoapMessage message) throws Fault {
            BindingOperationInfo boi = message.getExchange().getBindingOperationInfo();
            if (boi == null) {
                return;
            }
            if (StringUtils.isEmpty(action)) {
                return;
            }
            if (isActionMatch(message, boi, action)) {
                return;
            }
            if (matchWSAAction(boi, action)) {
                return;
            }
       
        	if(!bypass_soap_action_mismatch) { 
                // Se o parâmetro bypass_soap_action_mismatch for true o código abaixo não será executado
	            boolean synthetic = Boolean.TRUE.equals(boi.getProperty("operation.is.synthetic"));
	            if (!synthetic) {
	            		throw new Fault("SOAP_ACTION_MISMATCH", LOG, null, action);
	            }
        	}
        }
    }

    private static boolean isActionMatch(SoapMessage message, BindingOperationInfo boi, String action) {
        SoapOperationInfo soi = boi.getExtensor(SoapOperationInfo.class);
        if (soi == null) {
            return false;
        }
        boolean allowNoMatchingToDefault = MessageUtils.getContextualBoolean(message,
                                                                    ALLOW_NON_MATCHING_TO_DEFAULT,
                                                                    false);
        return action.equals(soi.getAction())
               || (allowNoMatchingToDefault && StringUtils.isEmpty(soi.getAction())
               || (message.getVersion() instanceof Soap12) && StringUtils.isEmpty(soi.getAction()));
    }
    
    /**
     * Verifica se o MNI está habilitado nesta instância.
     * Para desabilitar é preciso adicionar o parâmetro de VM -Dmni.habilitado=false.
     * 
     * @param message SoapMessage
     */
    private void verificarRequisicaoMNI(SoapMessage message) {
    	if(ParametroUtil.isMNIHabilitado() == Boolean.FALSE){
        	throw new Fault("O MNI está desabilitado nesta instância.",LOG);
        }
    }
}
