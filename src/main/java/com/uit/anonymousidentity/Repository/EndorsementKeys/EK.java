/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.uit.anonymousidentity.Repository.EndorsementKeys;

import com.uit.anonymousidentity.Models.Issuer;
import com.uit.anonymousidentity.Models.crypto.BNCurve;
import iaik.security.random.SecRandom;
import java.security.SecureRandom;

/**
 *
 * @author root
 */
public class EK {
    private String device_ID;
    public Issuer.IssuerPublicKey pk;
    //for testing, we also store sk
    private Issuer.IssuerSecretKey sk;
    private BNCurve curve;
    private SecureRandom random;
    public EK(String curve , String id){
        this.device_ID = id;
        random = new SecureRandom(id.getBytes());
        this.curve = BNCurve.createBNCurveFromName(curve);
        sk = Issuer.createIssuerKey(this.curve, random);
        pk = new Issuer.IssuerPublicKey(this.curve,sk.toJson(this.curve));
        
    }
    public EK(String curve, String id, String pkjson, String skjson){
        this.device_ID = id;
        this.curve = BNCurve.createBNCurveFromName(curve);
        this.pk = new Issuer.IssuerPublicKey(this.curve, pkjson);
        this.sk = new Issuer.IssuerSecretKey(this.curve, skjson);
        this.random = new SecureRandom(this.device_ID.getBytes());
    }
    public EK(BNCurve curve, String id, Issuer.IssuerPublicKey pk, Issuer.IssuerSecretKey sk){
        this.curve = curve;
        this.pk = pk;
        this.sk = sk;
        this.device_ID = id;
        
    }

    public String getDevice_ID() {
        return device_ID;
    }

    public void setDevice_ID(String device_ID) {
        this.device_ID = device_ID;
    }

    public Issuer.IssuerPublicKey getPk() {
        return pk;
    }

    public void setPk(Issuer.IssuerPublicKey pk) {
        this.pk = pk;
    }

    public Issuer.IssuerSecretKey getSk() {
        return sk;
    }

    public void setSk(Issuer.IssuerSecretKey sk) {
        this.sk = sk;
    }

    public BNCurve getCurve() {
        return curve;
    }

    public void setCurve(BNCurve curve) {
        this.curve = curve;
    }
    
    
}
