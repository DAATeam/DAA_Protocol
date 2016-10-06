package com.uit.anonymousidentity.Controllers;


import com.uit.anonymousidentity.Models.Authenticator;
import java.math.BigInteger;
import java.security.SecureRandom;

import org.springframework.stereotype.Controller;
import javax.servlet.http.HttpServletRequest;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.uit.anonymousidentity.Models.crypto.BNCurve;
import com.uit.anonymousidentity.Models.crypto.BNCurve.BNCurveInstantiation;
import com.uit.anonymousidentity.Models.Issuer;
import com.uit.anonymousidentity.Models.Issuer.IssuerPublicKey;
import com.uit.anonymousidentity.Models.Issuer.IssuerSecretKey;
import com.uit.anonymousidentity.Models.Issuer.JoinMessage1;
import com.uit.anonymousidentity.Models.Issuer.JoinMessage2;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletResponse;
@Controller
public class MainController {
	private BNCurve curve;
        //for testing, use 1 issuer only 
	private Issuer issuer;
	private final String TPM_ECC_BN_P256 = "TPM_ECC_BN_P256";
        private final String JSON = "JSON";
        private SecureRandom random;
        public final String SIG = "sig",
                KRD = "krd",
                APPID = "appId",
                JoinMessage1 = "JoinMessage1",
                SID = "sid",
                JSID = "jsid",
                SSID = "ssid",
                   MSG = "msg",OK ="ok", ERROR = "error", NONCE ="nonce", DATA ="data";
	
	public MainController() throws NoSuchAlgorithmException{
		curve = new BNCurve(BNCurveInstantiation.valueOf(TPM_ECC_BN_P256));
                random = new SecureRandom();
		generateIssuerKeyPair();
	}
        public  void generateIssuerKeyPair() throws NoSuchAlgorithmException{
            IssuerSecretKey sk = Issuer.createIssuerKey(curve, random);
            IssuerPublicKey pk =  new IssuerPublicKey(curve, sk, random);
            issuer = new Issuer(curve,sk, pk);
            
        }
        //recieve sid from host and produce none to host to generate join messgae 1
	@RequestMapping(value = "/getNonce" , method = RequestMethod.POST)
	public void SendNonce(HttpServletRequest request ,  HttpServletResponse response) throws IOException{
                String sid = request.getParameter(SID);
                String jsid = request.getParameter(JSID);
                	
		BigInteger n = new BigInteger(130,random);
                //save to database
                //response to client 
                response.setStatus(200);
                String json ;
                

                if(sid != null && jsid != null ){
                    json = "{" + MSG + ":" + OK +"," + NONCE +":"+n.toString()+"}";
                }
                else {
                    json = "{" + MSG + ":" + ERROR + "}";
                }
                PrintWriter out = response.getWriter();
                out.println(json);
                
		
	}
        //recive join message 1 and produce join message 2 
        @RequestMapping(value = "/getJoinMessage2",method = RequestMethod.POST)
        public void sendJoinMessage2(HttpServletRequest request , HttpServletResponse response) throws NoSuchAlgorithmException, IOException{
            String json  = request.getParameter(DATA);
            JoinMessage1 jm1 = new JoinMessage1(curve, json);
            JoinMessage2 jm2 = issuer.EcDaaIssuerJoin(jm1);
            //response
            response.setStatus(200);
            String res;
            if(jm2 == null){
                res = "{" + MSG + ":" + "Invalid join message"+"}";
            }
            else{
                res = "{" + MSG + ":" +OK + ","+ 
                        DATA + ":" + jm2.toJson(curve) + "}";
                        
            }
            PrintWriter out = response.getWriter();
            out.println(res);
        }
        @RequestMapping(value = "/verify", method = RequestMethod.POST)
        public ModelAndView Verify(HttpServletRequest request ) throws NoSuchAlgorithmException {
            return new ModelAndView("json","json","not implemented");
        }
        // --- This is just for test ---//
        // Simulate a authenticate to test another functions
        @RequestMapping(value = "/getJoinMessage1", method = RequestMethod.GET)
        public ModelAndView getAJoinMessage1() throws NoSuchAlgorithmException{
            Authenticator auth = new Authenticator(curve, issuer.pk);
            JoinMessage1 jm1 = auth.EcDaaJoin1(issuer.GetNonce());
            String ret = jm1.toJson(curve);
            return new ModelAndView("json","json",ret);
        }
        

}