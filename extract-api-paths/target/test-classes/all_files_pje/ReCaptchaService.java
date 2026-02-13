package br.jus.cnj.pje.nucleo.service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.Name;
import org.richfaces.json.JSONObject;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.ComponentUtil;

@Name(ReCaptchaService.NAME)
public class ReCaptchaService extends BaseService {

	public static final String NAME = "reCaptchaService";
	
    public static ReCaptchaService instance() {
    	return ComponentUtil.getComponent(ReCaptchaService.NAME);
    }

    public boolean validarResposta(String gRecaptchaResponse) {
    	boolean resultado = false;
        String url = "https://www.google.com/recaptcha/api/siteverify";
        HttpURLConnection con = null;

        if (StringUtils.isNotBlank(gRecaptchaResponse)) {
            try {
                URL obj = new URL(url);
                con = (HttpsURLConnection) obj.openConnection();
    			con.setRequestMethod("POST");
                con.setDoOutput(true);
                
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(String.format("secret=%s&response=%s", ParametroUtil.instance().obterReCaptchaSecretKey(), gRecaptchaResponse));
                wr.flush();
                wr.close();

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject json = new JSONObject(response.toString());

                resultado = (Boolean)json.get("success");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
    			if (con != null) {
    				con.disconnect();
    			}
    		}
        }
        return resultado;
    }
    
}
