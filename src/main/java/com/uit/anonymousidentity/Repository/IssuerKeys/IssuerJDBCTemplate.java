/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.uit.anonymousidentity.Repository.IssuerKeys;

import com.uit.anonymousidentity.Models.Issuer;
import com.uit.anonymousidentity.Models.crypto.BNCurve;
import static com.uit.anonymousidentity.Repository.Nonces.NonceJDBCTemplate.ID;
import static com.uit.anonymousidentity.Repository.Nonces.NonceJDBCTemplate.SID;
import static com.uit.anonymousidentity.Repository.Nonces.NonceJDBCTemplate.TABLE_NAME;
import static com.uit.anonymousidentity.Repository.Nonces.NonceJDBCTemplate.VALUE;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
            BNCurve = "BNCurve",
            ID = "id",
            PK_X = "pk_x",PK_Y = "pk_y",
            SK_X = "sk_x", SK_Y = "sk_y",            
            TABLE_NAME = "issuers";
             
            
    
    @Autowired
    public void setDataSource(DataSource dataSource) throws SQLException {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        createTableIfNotExists();
    }

    @Override
    public void store(Issuer issuer) throws SQLException {
      String t_sql = "insert into %s (%s, %s, %s, %s, %s, %s) values (?,?,?,?,?,?)";
      String sql =  String.format(t_sql, TABLE_NAME, SID, BNCurve, PK_X, PK_Y, SK_X, SK_Y);
      PreparedStatement pst = dataSource.getConnection().prepareStatement(sql);
      pst.setString(0,issuer.getSid());
      //FIXME : find way to fit enum type in BNCurve
      pst.setString(1, "PM_ECC_BN_P256");
      //To be continue ...
      
    }

    @Override
    public void getIssuerBySID(String sid) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
                + "%s blob not null,"
                + "%s blob not null,"
                    + "%s blob not null,"
                    + "%s blob not null,"
                + "primary key ( %s ) "
                + " ) ";
            String sql = String.format(tem_sql, TABLE_NAME, ID, SID, BNCurve, PK_X, PK_Y, SK_X, SK_Y,ID);
            jdbcTemplate.execute(sql);
        
        }
    }
    
}
