/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.uit.anonymousidentity.Repository.EndorsementKeys;

import com.uit.anonymousidentity.Models.Issuer;
import com.uit.anonymousidentity.Models.crypto.BNCurve;
import static com.uit.anonymousidentity.Repository.IssuerKeys.IssuerJDBCTemplate.BNCurveName;
import static com.uit.anonymousidentity.Repository.IssuerKeys.IssuerJDBCTemplate.ID;
import static com.uit.anonymousidentity.Repository.IssuerKeys.IssuerJDBCTemplate.PK;
import static com.uit.anonymousidentity.Repository.IssuerKeys.IssuerJDBCTemplate.SID;
import static com.uit.anonymousidentity.Repository.IssuerKeys.IssuerJDBCTemplate.SK;
import static com.uit.anonymousidentity.Repository.IssuerKeys.IssuerJDBCTemplate.TABLE_NAME;
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
public class EKJDBCTemplate implements EKDao{
    DataSource dataSource;
    JdbcTemplate jdbcTemplate;
    private final String TABLE_NAME = "endorsementKeys",
            COL_DEVICEID = "device_id",
            COL_ID = "id",
            COL_PK = "pk",
            COL_SK = "sk",
            COL_CURVE = "BNcurve";
    @Autowired
    public void setDataSource(DataSource d) throws SQLException{
        this.dataSource = d;
        jdbcTemplate = new JdbcTemplate(dataSource);
        createTableIfNotExists();
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
            String sql = String.format(tem_sql, TABLE_NAME, COL_ID, COL_DEVICEID, COL_PK
                    , COL_SK, COL_CURVE, COL_ID);
            jdbcTemplate.execute(sql);
        
        }
    }
    

    @Override
    public void store(EK ek) throws SQLException {
         String t_sql = "insert into %s (%s, %s, %s, %s) values ( ?, ?, ?, ? )";
      String sql =  String.format(t_sql, TABLE_NAME, COL_DEVICEID, COL_PK, COL_SK, COL_CURVE);
      PreparedStatement pst = dataSource.getConnection().prepareStatement(sql);
      pst.setString(1, ek.getDevice_ID());
      pst.setString(2, ek.getCurve().getName());
      pst.setString(3, ek.pk.toJSON(ek.getCurve()));
      pst.setString(4, ek.getSk().toJson(ek.getCurve()));
      pst.executeUpdate();
      pst.close();
    }

    @Override
    public EK getEKByDeviceID(String deviceID) throws SQLException {
          String sql = "select * from "+ TABLE_NAME+" where "+ COL_DEVICEID +" = " + "'"+ deviceID +"'";
        PreparedStatement pst=  dataSource.getConnection().prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        if(rs.next()){
            BNCurve curve = BNCurve.createBNCurveFromName(rs.getString(BNCurveName));
            Issuer.IssuerPublicKey ipk = new Issuer.IssuerPublicKey(curve, rs.getString(PK));
            Issuer.IssuerSecretKey isk = new Issuer.IssuerSecretKey(curve, rs.getString(SK));
            String did = rs.getString(COL_DEVICEID);
            EK ek = new EK(curve,did, ipk, isk );
            
            return ek;
        }
        else{
            return null;
        }
    }

    @Override
    public void removeByDeviceID(String deviceID) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
