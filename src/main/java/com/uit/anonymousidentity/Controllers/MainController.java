package com.uit.anonymousidentity.Controllers;


import com.uit.anonymousidentity.Models.Authenticator;
import com.uit.anonymousidentity.Models.Verifier;
import java.math.BigInteger;
import java.security.SecureRandom;
import org.springframework.context.NoSuchMessageException;
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
import com.uit.anonymousidentity.Repository.IssuerKeys.IssuerJDBCTemplate;
import com.uit.anonymousidentity.Repository.Nonces.Nonce;
import com.uit.anonymousidentity.Repository.Nonces.NonceJDBCTemplate;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
@MultipartConfig
@Controller
public class MainController {
    @Autowired
     ApplicationContext context;
	private BNCurve curve;
        //for testing, use 1 issuer only 
	private Issuer issuer;
	private final String TPM_ECC_BN_P256 = "TPM_ECC_BN_P256";
        private final String JSON = "JSON";
        private SecureRandom random;
        public final String SIG = "sig",
                KRD = "krd",
                APPID = "appId",
                JoinMessage1 = "JM1",
                SID = "sid",
                JSID = "jsid",
                SSID = "ssid",
                   STATUS = "status",OK ="ok", ERROR = "error", NONCE ="nonce", DATA ="data", MSG="msg";
        
        //revocation lIst for test . This must be in database
       private Set<BigInteger> revocationList = new HashSet<BigInteger>();
       //for testing
       private byte[] sig_data , krd_data;
       private String appId = "demoAppId";
	
	public MainController() throws NoSuchAlgorithmException, SQLException{
            
		curve = new BNCurve(BNCurveInstantiation.valueOf(TPM_ECC_BN_P256));
                random = new SecureRandom();
		
                
                //for testing 
                
                
	}
        @RequestMapping(value = "/createNewIssuer" , method = RequestMethod.GET)
        public void storeKeyPair(@RequestParam("sid") String sid, HttpServletResponse response) throws NoSuchAlgorithmException, SQLException, IOException{
            String res = "";
            IssuerJDBCTemplate template = (IssuerJDBCTemplate) context.getBean("issuerJDBCTemplate");
            if(sid != null && !sid.equals("") && !template.isContainSid(sid)){
            //check exists sid here
            Issuer i = generateIssuerKeyPair();
            i.setSid(sid);
            
            template.store(i);
            res = "{" + STATUS + ":" + OK + "}";
            }
            else{
                res = "{" + STATUS + ":" + ERROR + "," + MSG + ":" + "Dupliacate issuerSid" + "}";
            }
            PrintWriter w = response.getWriter();
            w.println(res);
            
            
        }
        public  Issuer generateIssuerKeyPair() throws NoSuchAlgorithmException{
            IssuerSecretKey sk = Issuer.createIssuerKey(curve, random);
            IssuerPublicKey pk =  new IssuerPublicKey(curve, sk, random);
             Issuer i= new Issuer(curve,sk, pk);
             return i;
            
        }
        @RequestMapping(value = "/getPubicKey",method = RequestMethod.GET)
        public void getPublicKey(@RequestParam("sid")String sid, HttpServletResponse response) throws SQLException, NoSuchAlgorithmException, IOException{
            
            IssuerJDBCTemplate template = (IssuerJDBCTemplate) context.getBean("issuerJDBCTemplate");
            Issuer i = template.getIssuerBySID(sid);
            String res = "";
            if(i != null){
                res = "{" + STATUS + ":" + OK + ","+ DATA +":";
                res += i.pk.toJSON(curve);
                res += "}";
                
            }
            else{
                res = "{" + STATUS + ":" + ERROR + "," + MSG + ":" + "Invalid issuerSid" + "}";
            }
            PrintWriter w = response.getWriter();
            w.println(res);
            
        }
        //recieve sid from host and produce none to host to generate join messgae 1
	@RequestMapping(value = "/getNonce" , method = RequestMethod.POST)
	public void SendNonce(HttpServletRequest request ,  HttpServletResponse response) throws IOException, SQLException{
                String sid = request.getParameter(SID);
                String jsid = request.getParameter(JSID);
                String json ;
                IssuerJDBCTemplate template = (IssuerJDBCTemplate) context.getBean("issuerJDBCTemplate");
                NonceJDBCTemplate ntemplate = (NonceJDBCTemplate) context.getBean("nonceJDBCTemplate");  
                boolean success = true;
                response.setStatus(200);
                success &= (sid != null && !sid.equals(""));
                success &= (jsid != null && !jsid.equals(""));
                success &= template.isContainSid(sid);
                if(success){
		BigInteger n = new BigInteger(130,random);
                Nonce nonce = new Nonce();
                nonce.setIssuerSid(sid);
                nonce.setByteArray(n.toByteArray());
                //save to database
                //response to client 
                while(!ntemplate.isFresh(nonce)){
                    n = new BigInteger(130, random);
                    nonce.setByteArray(n.toByteArray());
                    
                }
                      ntemplate.store(nonce);
                  json = "{" + STATUS + ":" + OK +"," + NONCE +":"+n.toString()+"}";
                
                }
                else{
                      json = "{" + STATUS + ":" + ERROR + "}";
                }
                
                PrintWriter out = response.getWriter();
                out.println(json);
                
		
	}
        //recive join message 1 and produce join message 2 
        @RequestMapping(value = "/getJoinMessage2",method = RequestMethod.POST)
        public void sendJoinMessage2(
                HttpServletRequest request,
                HttpServletResponse response) throws NoSuchAlgorithmException, IOException, SQLException{
            boolean success = true;
            String json  = request.getParameter(JoinMessage1);
            
            String sid = request.getParameter(SID);
            JoinMessage1 jm1 = new JoinMessage1(curve, json);
            Nonce nonce ;
            NonceJDBCTemplate ntemplate = (NonceJDBCTemplate) context.getBean("nonceJDBCTemplate");
            IssuerJDBCTemplate template = (IssuerJDBCTemplate) context.getBean("issuerJDBCTemplate");
            
            nonce = ntemplate.find(jm1.nonce);
            JoinMessage2 jm2 = null;
            success &= (json != null);
            success &= (jm1 != null);
            success &= (nonce != null);
            if(success && sid.equals(nonce.getIssuerSid())){
            Issuer i = template.getIssuerBySID(sid);
            
            if(nonce != null){
             jm2 = i.EcDaaIssuerJoin(jm1);
            }
            }
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
        public void Verify(HttpServletRequest request, HttpServletResponse response ) throws NoSuchAlgorithmException, IOException, ServletException, SQLException {
            boolean success = true;
            Part appId_part = request.getPart(APPID);
            Part krd_part = request.getPart(KRD);
            Part sig_part = request.getPart(SIG);
            Part sid_part = request.getPart(SID);
            String res;
            //response
            response.setStatus(200);
            
            success &= (appId_part != null);
            success &= (krd_part != null);
            success &= (sig_part != null);
            success &= (sid_part != null);
            if(success){
            Verifier ver = new Verifier(curve);
            byte krd[] = convertPartToByteArray(krd_part);
            byte sig[] = convertPartToByteArray(sig_part);
            byte appid_b[] = convertPartToByteArray(appId_part);
            byte sid_b[] = convertPartToByteArray(sid_part);
            String appId_s = new String(appid_b);
            String sid_s = new String(sid_b);
                    
            IssuerJDBCTemplate template = (IssuerJDBCTemplate) context.getBean("issuerJDBCTemplate");
           
            Issuer i = template.getIssuerBySID(sid_s);
            IssuerPublicKey pk = i.pk;
            Authenticator.EcDaaSignature signature = new Authenticator.EcDaaSignature(sig, krd, curve);
            boolean valid = ver.verify(signature, appId_s, pk, revocationList);
                       
            if(valid){
                res = "{" + STATUS + ":" + OK +"," +
                        MSG + ":" + "Signature is valid" +"}";
            }
            else{
                 res = "{" + STATUS + ":" + ERROR +"," +
                        MSG + ":" + "Signature is invalid" +"}";
            }
            }
            else{
                res = "{" + STATUS + ":" + ERROR + "," +
                        MSG + ":" + "Invalid Parameter" + "}";
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
        @RequestMapping(value = "/createExampleData", method = RequestMethod.GET)
        public void generateExampleSign(HttpServletResponse response) throws NoSuchAlgorithmException, SQLException, IOException{
            IssuerJDBCTemplate template = (IssuerJDBCTemplate) context.getBean("issuerJDBCTemplate");
            NonceJDBCTemplate ntemplate = (NonceJDBCTemplate) context.getBean("nonceJDBCTemplate");
            Issuer i = template.getIssuerBySID("NDY");
            BigInteger n =curve.getRandomModOrder(random);
            Nonce nonce = new Nonce();
            nonce.setIssuerSid("NDY");
            nonce.setByteArray(n.toByteArray());
            while(!ntemplate.isFresh(nonce)){
                n =curve.getRandomModOrder(random);
                nonce.setByteArray(n.toByteArray());
            }
             Authenticator auth = new Authenticator(i.getCurve(),i.pk);
            JoinMessage1 jm1 = auth.EcDaaJoin1(nonce.getBigInt());
            String jm1_s = jm1.toJson(curve);
            JoinMessage2 jm2 = i.EcDaaIssuerJoin(jm1);
            auth.EcDaaJoin2(jm2);
            Authenticator.EcDaaSignature sig = auth.EcDaaSign(appId);
            krd_data = sig.krd;
            sig_data = sig.encode(curve);
            response.getWriter().println("done");
        }
        @RequestMapping(value = "/insertExampleNonce", method = RequestMethod.GET)
        public void insertNonce(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException{
            //create and store
            BigInteger n = new BigInteger(130, random);
            Nonce nonce = new Nonce();
            nonce.setIssuerSid("DamnIssuerSID");
            nonce.setByteArray(n.toByteArray());
            NonceJDBCTemplate nonceJDBCTemplate =(NonceJDBCTemplate) context.getBean("nonceJDBCTemplate");
            nonceJDBCTemplate.store(nonce);
            //get back and compare
            Set<Nonce> setNonces = nonceJDBCTemplate.getNoncesBySID("DamnIssuerSID");
                                    
            response.setStatus(200);
            String res = "";
            res+= " store : " + new String(n.toByteArray()) + "\n";
            for(Nonce i : setNonces){
                res += " get : " + new String(i.getByteArray());
                if ((new BigInteger(i.getByteArray())).compareTo(n) == 0  ){
                    res += "correct \n";
                }
                else{
                    res += "\n";
                }
            }
            
            PrintWriter out = response.getWriter();
           out.println(res);
            
                                    
        }


}