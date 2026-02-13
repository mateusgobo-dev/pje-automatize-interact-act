//package br.com.infox.cliente.component;
//
//import java.io.IOException;
//
//import org.apache.commons.httpclient.Cookie;
//import org.apache.commons.httpclient.HttpClient;
//import org.apache.commons.httpclient.HttpException;
//import org.apache.commons.httpclient.HttpHost;
//import org.apache.commons.httpclient.HttpState;
//import org.apache.commons.httpclient.NameValuePair;
//import org.apache.commons.httpclient.cookie.CookiePolicy;
//import org.apache.commons.httpclient.methods.PostMethod;
//
//public class TesteHttpClient {
//
//	
//	public static void main(String[] args) throws HttpException, IOException {
//		
//        // Get target URL
//        String strURL = "http://localhost:8080/pje2_dev/signFile.seam";
//        // Get file to be posted
////        String strXMLFilename = args[1];
////        File input = new File(strXMLFilename);
//        // Prepare HTTP post
//        PostMethod post = new PostMethod(strURL);
//        // Request content will be retrieved directly
//        // from the input stream
////        RequestEntity entity = new FileRequestEntity(input, "text/xml; charset=ISO-8859-1");
//        
//        
//        NameValuePair[] parametersBody = { 
//        		new NameValuePair("hashSession", "00eD7ccmqafbSJJbITcIFgkXiI01MsR3I2pFhoyI"),
//        		new NameValuePair("sign", "data"),
//        		new NameValuePair("certChain", "O1mWQrDsL4N7bNq7xR8jMqUBtYsmMbo5CR979o1Z"),
//        		new NameValuePair("data", "2010-12-26 15:00:00"),
//        		new NameValuePair("id", "323"),
//        		new NameValuePair("codIni", "151349222"),
//        		new NameValuePair("md5", "563303c35334ba2ff958595c9980fe4e"),
//        		};
//		//        post.setRequestEntity(entity);
//        post.setRequestBody(parametersBody);
//        
//        
//        // Get HTTP client
//        HttpClient httpclient = new HttpClient();
//        // A different cookie management spec can be selected
//        // when desired
//        //httpclient.getParams().setCookiePolicy(CookiePolicy.NETSCAPE);
//        // Netscape Cookie Draft spec is provided for completeness
//        // You would hardly want to use this spec in real life situations
//        //httppclient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);        
//        
//        
//        //931C8D238D2F441385AA995F90706B58
//        
//        
//        
//        HttpHost host = new HttpHost(strURL);
////        httpclient.getState().
//        // Execute request
//        try {
//            int result = httpclient.executeMethod(post);
//            // Display status code
//            System.out.println("Response status code: " + result);
//            // Display response
//            System.out.println("Response body: ");
//            System.out.println(post.getResponseBodyAsString());
//        } finally {
//            // Release current connection to the connection pool once you are done
//            post.releaseConnection();
//        }		
//	}
//	
// }
