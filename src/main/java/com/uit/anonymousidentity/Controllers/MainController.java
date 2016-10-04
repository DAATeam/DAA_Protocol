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
import java.security.NoSuchAlgorithmException;
@Controller
public class MainController {
	private BNCurve curve;
        //for testing, use 1 issuer only 
	private Issuer issuer;
	private final String TPM_ECC_BN_P256 = "TPM_ECC_BN_P256";
        private final String JSON = "JSON";
        private SecureRandom random;
	
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
	@RequestMapping(value = "/getNonce" , method = RequestMethod.GET)
	public ModelAndView SendNonce(){
		
		BigInteger n = new BigInteger(130,random);
		return new ModelAndView("json","json", n );
	}
        //recive join message 1 and produce join message 2 
        @RequestMapping(value = "/getJoinMessage2",method = RequestMethod.POST)
        public ModelAndView sendJoinMessage2(HttpServletRequest request ) throws NoSuchAlgorithmException{
            String json  = request.getParameter(JSON);
            JoinMessage1 jm1 = new JoinMessage1(curve, json);
            JoinMessage2 jm2 = issuer.EcDaaIssuerJoin(jm1);
            return new ModelAndView("json","json", jm2);
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