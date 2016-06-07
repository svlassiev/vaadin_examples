package com;

import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.util.sqlcontainer.connection.SimpleJDBCConnectionPool;
import com.vaadin.data.util.sqlcontainer.query.FreeformQuery;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.MouseEvents;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class MyVaadinApplication extends UI {

    private SimpleJDBCConnectionPool connectionPool = null;
    private SQLContainer sqlContainer = null;

    @Override
    public void init(VaadinRequest request) {
        VerticalLayout layout = new VerticalLayout();
        setContent(layout);

        initConnectionPool();
        initDatabase();
        initContainer();
        final Object id = sqlContainer.addItem();
        sqlContainer.getContainerProperty(id, "FIRSTNAME").setValue("Firstname");
        sqlContainer.getContainerProperty(id, "LASTNAME").setValue("Lastname");
        try {
            sqlContainer.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        sqlContainer.setAutoCommit(true);

        Table sqlTable = new Table("Table PEOPLE", this.sqlContainer);

        TextField modifyFirstname = new TextField("Modify FIRSTNAME");
        modifyFirstname.setEnabled(true);
        modifyFirstname.addTextChangeListener(new FieldEvents.TextChangeListener() {
            @Override
            public void textChange(FieldEvents.TextChangeEvent event) {
                sqlContainer.getContainerProperty(sqlContainer.getIdByIndex(0), "FIRSTNAME").setValue(event.getText());
            }
        });

        TextField modifyLastname = new TextField("Modify LASTNAME");
        modifyLastname.setPropertyDataSource(sqlContainer.getContainerProperty(sqlContainer.getIdByIndex(0), "LASTNAME"));
        modifyLastname.setEnabled(true);

        layout.addComponent(modifyFirstname);
        layout.addComponent(modifyLastname);
        layout.addComponent(sqlTable);

        this.addClickListener(new MouseEvents.ClickListener() {
            @Override
            public void click(MouseEvents.ClickEvent event) {
                    sqlContainer.refresh();
            }
        });
    }

    private void initConnectionPool() {
        try {
            connectionPool = new SimpleJDBCConnectionPool(
                    "org.hsqldb.jdbc.JDBCDriver",
                    "jdbc:hsqldb:mem:sqlcontainer", "SA", "", 2, 5);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initDatabase() {
        try {
            Connection conn = connectionPool.reserveConnection();
            Statement statement = conn.createStatement();
            try {
                statement.executeQuery("SELECT * FROM PEOPLE");
            } catch (SQLException e) {
                // Failed, which means that we should init the database
                statement
                        .execute("CREATE TABLE PEOPLE "
                                + "(ID INTEGER GENERATED ALWAYS AS IDENTITY, "
                                + "FIRSTNAME VARCHAR(32), LASTNAME VARCHAR(32), "
//                                + "COMPANY VARCHAR(32), MOBILE VARCHAR(20), WORKPHONE VARCHAR(20), "
//                                + "HOMEPHONE VARCHAR(20), WORKEMAIL VARCHAR(128), HOMEEMAIL VARCHAR(128), "
//                                + "STREET VARCHAR(32), ZIP VARCHAR(16), CITY VARCHAR(32), STATE VARCHAR(2), "
//                                + "COUNTRY VARCHAR(32),
                                  +  "PRIMARY KEY(ID))");
            }
            statement.close();
            conn.commit();
            connectionPool.releaseConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initContainer() {
        try {
            FreeformQuery query = new FreeformQuery("SELECT * FROM PEOPLE", connectionPool, "ID");
            query.setDelegate(new DemoFreeformQueryDelegate());
            sqlContainer = new SQLContainer(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
