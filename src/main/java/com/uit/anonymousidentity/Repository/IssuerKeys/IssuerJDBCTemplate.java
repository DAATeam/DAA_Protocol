/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.uit.anonymousidentity.Repository.IssuerKeys;

import com.uit.anonymousidentity.Models.Issuer;
import com.uit.anonymousidentity.Models.crypto.BNCurve;

import static com.uit.anonymousidentity.Repository.Nonces.NonceJDBCTemplate.SID;
import static com.uit.anonymousidentity.Repository.Nonces.NonceJDBCTemplate.TABLE_NAME;
import static com.uit.anonymousidentity.Repository.Nonces.NonceJDBCTemplate.VALUE;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * @author root
 */
public class IssuerJDBCTemplate implements IssuerDAO{
    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
     public static final String SID = "sid",
            BNCurveName = "BNCurve",
            ID = "id",
            PK = "pk", SK = "sk",
           TPM_ECC_BN_P256 = "TPM_ECC_BN_P256", 
		TPM_ECC_BN_P638= "TPM_ECC_BN_P638",
		ECC_BN_DSD_P256= "ECC_BN_DSD_P256",
		ECC_BN_ISOP512 = "ECC_BN_ISOP512",
            TABLE_NAME = "issuers";
             
            
    
    @Autowired
    public void setDataSource(DataSource dataSource) throws SQLException {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        createTableIfNotExists();
    }

    @Override
    public void store(Issuer issuer) throws SQLException {
      String t_sql = "insert into %s (%s, %s, %s, %s) values ( ?, ?, ?, ? )";
      String sql =  String.format(t_sql, TABLE_NAME, SID, BNCurveName, PK, SK);
      PreparedStatement pst = dataSource.getConnection().prepareStatement(sql);
      pst.setString(1, issuer.getSid());
      pst.setString(2, issuer.getCurve().getName());
      pst.setString(3, issuer.pk.toJSON(issuer.getCurve()));
      pst.setString(4, issuer.getSk().toJson(issuer.getCurve()));
      pst.executeUpdate();
      pst.close();
      
    }
   
    @Override
    public Issuer getIssuerBySID(String sid) throws SQLException, NoSuchAlgorithmException {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        String sql = "select * from "+ TABLE_NAME+" where "+ SID +" = " + "'"+ sid +"'";
        PreparedStatement pst=  dataSource.getConnection().prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        if(rs.next()){
            BNCurve curve = BNCurve.createBNCurveFromName(rs.getString(BNCurveName));
            Issuer.IssuerPublicKey ipk = new Issuer.IssuerPublicKey(curve, rs.getString(PK));
            Issuer.IssuerSecretKey isk = new Issuer.IssuerSecretKey(curve, rs.getString(SK));
            return new Issuer(curve, isk, ipk);
        }
        else{
            return null;
        }
               
    }
  @Override
  public boolean isContainSid(String sid) throws SQLException{
      String sql= "select * from " + TABLE_NAME +
              " where " + SID + " = " +"'"+ sid+"'";
     PreparedStatement pst=  dataSource.getConnection().prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        if(rs.next()){
            return true;
        }
        else return false;
  }
  @Override 
    public Set<String> getAllSid() throws SQLException{
        String sql = "select "+ SID +" from " + TABLE_NAME;
         PreparedStatement pst=  dataSource.getConnection().prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        Set<String> ss = new HashSet<String>();
        if(rs.next()){
            do{
                ss.add(rs.getString(SID));
            }while(rs.next());
            return ss;
        }
        else{
            return null;
        }
    }
    
    @Override
    public void deleteIssuerBySID(String sid) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void createTableIfNotExists() throws SQLException {
         DatabaseMetaData metaData = dataSource.getConnection().getMetaData();
        ResultSet rs = metaData.getTables(null, null, TABLE_NAME,null);
        if(rs.next()){
            //Table exists
            return;
        }
        else{
            String tem_sql = "create table if not exists %s ( "
                + "%s int not null auto_increment,"
                + "%s text not null,"
                    + "%s text not null,"
               + "%s text not null,"
                    + "%s text not null,"
                + "primary key ( %s ) "
                + " ) ";
            String sql = String.format(tem_sql, TABLE_NAME, ID, SID, BNCurveName, PK, SK,ID);
            jdbcTemplate.execute(sql);
        
        }
    }
    
}
