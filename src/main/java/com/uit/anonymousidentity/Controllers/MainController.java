package com.uit.anonymousidentity.Controllers;


import com.uit.anonymousidentity.Models.Authenticator;
import com.uit.anonymousidentity.Models.Verifier;
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
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
@MultipartConfig
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
                   STATUS = "status",OK ="ok", ERROR = "error", NONCE ="nonce", DATA ="data", MSG="msg";
        
        //revocation lIst for test . This must be in database
       private Set<BigInteger> revocationList = new HashSet<BigInteger>();
       //for testing
       private byte[] sig_data , krd_data;
       private String appId = "demoAppId";
	
	public MainController() throws NoSuchAlgorithmException{
		curve = new BNCurve(BNCurveInstantiation.valueOf(TPM_ECC_BN_P256));
                random = new SecureRandom();
		generateIssuerKeyPair();
                //for testing 
                generateExampleSign();
                
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
                    json = "{" + STATUS + ":" + OK +"," + NONCE +":"+n.toString()+"}";
                }
                else {
                    json = "{" + STATUS + ":" + ERROR + "}";
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
                res = "{" + STATUS + ":" + "Invalid join message"+"}";
            }
            else{
                res = "{" + STATUS + ":" +OK + ","+ 
                        DATA + ":" + jm2.toJson(curve) + "}";
                        
            }
            PrintWriter out = response.getWriter();
            out.println(res);
        }
        @RequestMapping(value = "/verify", method = RequestMethod.POST)
        public void Verify(HttpServletRequest request, HttpServletResponse response ) throws NoSuchAlgorithmException, IOException, ServletException {
            
            Part appId_part = request.getPart(APPID);
            Part krd_part = request.getPart(KRD);
            Part sig_part = request.getPart(SIG);
            
            Verifier ver = new Verifier(curve);
            byte krd[] = convertPartToByteArray(krd_part);
            byte sig[] = convertPartToByteArray(sig_part);
            byte appid_b[] = convertPartToByteArray(appId_part);
            String appId_s = new String(appid_b);
                    
            IssuerPublicKey pk = issuer.pk;
            Authenticator.EcDaaSignature signature = new Authenticator.EcDaaSignature(sig, krd, curve);
            boolean valid = ver.verify(signature, appId_s, pk, revocationList);
            //response
            response.setStatus(200);
            String res;
            if(valid){
                res = "{" + STATUS + ":" + OK +"," +
                        MSG + ":" + "Signature is valid" +"}";
            }
            else{
                 res = "{" + STATUS + ":" + ERROR +"," +
                        MSG + ":" + "Signature is invalid" +"}";
            }
            PrintWriter out = response.getWriter();
            out.println(res);
            
            
            
        }
        public byte[] convertPartToByteArray(Part part) throws IOException{
            InputStream in = part.getInputStream();
            int size =(int)part.getSize();
            int total = 0;
            int readbyte = 0;
            byte b[] = new byte[size];
           
            while(total < size){
                readbyte = in.read(b,total,size);
                total+= readbyte;
            }
            return b;
            
        }
        // --- This is just for test ---//
        // Simulate a authenticate to test another functions
        
        @RequestMapping(value = "/getExampleSign", method = RequestMethod.GET)
        public void getExampleSign(HttpServletResponse response) throws NoSuchAlgorithmException, IOException{
            response.getOutputStream().write(sig_data);
            
            
            
            
            
            
        }
        @RequestMapping(value = "/getExampleKrd", method = RequestMethod.GET)
        public void getExampleKrd(HttpServletResponse response) throws NoSuchAlgorithmException, IOException{
            response.getOutputStream().write(krd_data);
        }
        public void generateExampleSign() throws NoSuchAlgorithmException{
                Authenticator auth = new Authenticator(curve, issuer.pk);
            JoinMessage1 jm1 = auth.EcDaaJoin1(issuer.GetNonce());
            String jm1_s = jm1.toJson(curve);
            JoinMessage2 jm2 = issuer.EcDaaIssuerJoin(jm1);
            auth.EcDaaJoin2(jm2);
            Authenticator.EcDaaSignature sig = auth.EcDaaSign(appId);
            krd_data = sig.krd;
            sig_data = sig.encode(curve);
            
        }
                
        

}