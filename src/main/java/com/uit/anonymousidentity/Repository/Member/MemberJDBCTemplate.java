package com.uit.anonymousidentity.Repository.Member;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import javax.sql.DataSource;
import org.springframework.jdbc.core.*;
/**
 * Created by DK on 10/16/16.
 */
@Repository
public class MemberJDBCTemplate implements MemberDAO {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        System.out.println("data connect: " + dataSource);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

}