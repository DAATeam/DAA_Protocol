package com.uit.anonymousidentity.Repository;
import com.uit.anonymousidentity.Models.User;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import javax.sql.DataSource;
import org.springframework.jdbc.core.*;
/**
 * Created by DK on 10/16/16.
 */
@Repository
public class JdbcCorporateEventDao implements CorporateEventDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public User findByUserId(int id) {
        return null;
    }

    @Override
    public User insertUser(User user) {
        this.jdbcTemplate.update("insert into Users (id, name) values (?, ?)", user.getId(), user.getName());
        return null;
    }
}