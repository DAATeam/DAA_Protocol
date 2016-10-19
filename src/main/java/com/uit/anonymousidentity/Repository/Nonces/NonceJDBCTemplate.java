/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.uit.anonymousidentity.Repository.Nonces;

import java.math.BigInteger;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.*;

/**
 *
 * @author root
 */
public class NonceJDBCTemplate implements NonceDAO{
    private DataSource dataSource;
    JdbcTemplate jdbcTemplate;
    public static final String SID = "sid",
            VALUE = "value",
            ID = "id",
            TABLE_NAME = "nonces";
    
    @Autowired
    public void setDataSource(DataSource dataSource) throws SQLException {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        createTableIfNotExist();
    }

    @Override
    public void store(Nonce nonce)throws SQLException {
        String t_sql = "insert into %s (%s, %s) values (?, ?)";
        String sql = String.format(t_sql, TABLE_NAME, VALUE, SID);
        PreparedStatement pst = dataSource.getConnection().prepareStatement(sql);
        
        pst.setBytes(1, nonce.getByteArray());
        pst.setString(2, nonce.getIssuerSid());
        pst.executeUpdate();
        pst.close();
            
                
    }
    @Override
    public boolean isFresh(Nonce n) throws SQLException{
        String sql = "select * from " + TABLE_NAME + " where " + SID +" = ? and " + VALUE + " = ?";
        PreparedStatement pst = dataSource.getConnection().prepareStatement(sql);
        pst.setString(1, n.getIssuerSid());
        pst.setBytes(2, n.getByteArray());
         
        ResultSet rs =  pst.executeQuery();
        if(rs.next()){
            return false;
        }
        else return true;
        
    }
    @Override
    public void delete(Nonce nonce)throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public Nonce find(BigInteger i) throws SQLException{
        String sql = "select * from " + TABLE_NAME + " where " + VALUE + " = ?" ;
        PreparedStatement pst = dataSource.getConnection().prepareStatement(sql);
        Nonce n = new Nonce();
        pst.setBytes(1, i.toByteArray());
        ResultSet rs = pst.executeQuery();
        if(rs.next()){
            n.setIssuerSid(rs.getString(SID));
            n.setByteArray(rs.getBytes(VALUE));
            n.setId(rs.getInt(ID));
            return n;
        }
        else return null;
    }

    @Override
    public void deleteById(Integer id) throws SQLException{
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public void createTableIfNotExist() throws SQLException{
        DatabaseMetaData metaData = dataSource.getConnection().getMetaData();
        ResultSet rs = metaData.getTables(null, null, TABLE_NAME,null);
        if(rs.next()){
            //Table exists
            return;
        }
        else{
            String tem_sql = "create table if not exists %s ( "
                + "%s int not null auto_increment,"
                + "%s blob not null,"
                + "%s text,"
                + "primary key ( %s ) "
                + " ) ";
            String sql = String.format(tem_sql, TABLE_NAME, ID, VALUE, SID, ID);
            jdbcTemplate.execute(sql);
        
        }
        
                
    }
    public Nonce getNonceById(Integer id) throws SQLException{
        String sql = "select * from "+ TABLE_NAME + 
                " where " + ID + " = " + id;
       PreparedStatement pst=  dataSource.getConnection().prepareStatement(sql);
       ResultSet rs = pst.executeQuery(sql);
       if(rs.next()){
           //data valid
           Integer nonceID = rs.getInt(ID);
           String nonceSID = rs.getString(SID);
           byte[] nonceBytes = rs.getBytes(VALUE);
            Nonce nonce = new Nonce();
             nonce.setId(nonceID);
             nonce.setIssuerSid(nonceSID);
             nonce.setByteArray(nonceBytes);
             return nonce;
       }
       else{
           return null;
       }
      
        
    }
    public Set<Nonce> getNoncesBySID(String sid) throws SQLException{
        String sql = "select * from "+ TABLE_NAME + 
                " where " + SID + " = " + "'"+sid+"'";
       PreparedStatement pst=  dataSource.getConnection().prepareStatement(sql);
       ResultSet rs = pst.executeQuery(sql);
       if(rs.next()){
           //data valid
           Integer nonceID;
           String nonceSID;
           byte[] nonceBytes;
           Set<Nonce> set = new HashSet<>();
           do{
            nonceID = rs.getInt(ID);
            nonceSID = rs.getString(SID);
            nonceBytes = rs.getBytes(VALUE);
            Nonce nonce = new Nonce();
             nonce.setId(nonceID);
             nonce.setIssuerSid(nonceSID);
             nonce.setByteArray(nonceBytes);
            set.add(nonce);
           }while(rs.next());
           return set;             
       }
       else{
           return null;
       }
    }
    
}
