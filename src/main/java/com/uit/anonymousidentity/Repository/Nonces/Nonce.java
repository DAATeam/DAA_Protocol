/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.uit.anonymousidentity.Repository.Nonces;
import org.hsqldb.jdbcDriver;
import java.math.BigInteger;

/**
 *
 * @author root
 */
public class Nonce {
    Integer id;
    byte[] byteArray;
    String issuerSid;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
 

    public String getIssuerSid() {
        return issuerSid;
    }

    public void setIssuerSid(String issuerSid) {
        this.issuerSid = issuerSid;
    }
    public BigInteger getBigInt(){
        BigInteger bi = new BigInteger(byteArray);
        return bi;
    }
    public void setByteArray(byte[] b){
        this.byteArray = b;
    }
    public byte[] getByteArray(){
        return byteArray;
    }
    
}
