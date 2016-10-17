/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.uit.anonymousidentity.Repository.Nonces;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author root
 */
public class NonceMapper implements RowMapper<Nonce>{

    @Override
    public Nonce mapRow(ResultSet rs, int i) throws SQLException {
       Nonce nonce = new Nonce();
       nonce.setId(rs.getInt(NonceJDBCTemplate.ID));
       nonce.setIssuerSid(rs.getString(NonceJDBCTemplate.SID));
       return nonce;        
    }
    
    
}
