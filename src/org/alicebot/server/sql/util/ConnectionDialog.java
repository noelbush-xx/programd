/*
 * ConnectionDialog.java
 */

package org.alicebot.server.sql.util;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.applet.*;
import java.sql.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class ConnectionDialog extends Dialog implements ActionListener,
ItemListener {
  final static String sJDBCTypes[][]={
		{"Alicebot In-Memory","org.alicebot.server.sql.jdbcDriver","jdbc:alicebot:DATABASE"},
		{"Alicebot Standalone","org.alicebot.server.sql.jdbcDriver","jdbc:alicebot:DATABASE"},
  };
  Connection mConnection;
  TextField mDriver,mURL,mUser,mPassword;
  Label mError;

  public static Connection createConnection(String driver,String url,
  String user,String password) throws Exception {
    Class.forName(driver).newInstance();
    return DriverManager.getConnection(url,user,password);
  }
  ConnectionDialog(Frame owner,String title) {
    super(owner,title,true);
  }
  void create() {
    setLayout(new BorderLayout());
    Panel p=new Panel(new GridLayout(6,2,10,10));
    p.setBackground(SystemColor.control);
    p.add(createLabel("Type:"));
    Choice types=new Choice();
    types.addItemListener(this);
    for(int i=0;i<sJDBCTypes.length;i++) {
      types.add(sJDBCTypes[i][0]);
    }
    p.add(types);
    p.add(createLabel("Driver:"));
    mDriver=new TextField("org.alicebot.server.sql.jdbcDriver");
    p.add(mDriver);
    p.add(createLabel("URL:"));
    mURL=new TextField("jdbc:alicebot:DATABASE");
    p.add(mURL);
    p.add(createLabel("User:"));
    mUser=new TextField("alicebot");
    p.add(mUser);
    p.add(createLabel("Password:"));
    mPassword=new TextField("");
    mPassword.setEchoChar('*');
    p.add(mPassword);
    Button b;
    b=new Button("Ok");
    b.setActionCommand("ConnectOk");
    b.addActionListener(this);
    p.add(b);
    b=new Button("Cancel");
    b.setActionCommand("ConnectCancel");
    b.addActionListener(this);
    p.add(b);
    setLayout(new BorderLayout());
    add("East",createLabel(""));
    add("West",createLabel(""));
    mError=new Label("");
    Panel pMessage=createBorderPanel(mError);
    add("South",pMessage);
    add("North",createLabel(""));
    add("Center",p);
    doLayout();
    pack();
    Dimension d=Toolkit.getDefaultToolkit().getScreenSize();
    Dimension size=getSize();
    setLocation((d.width-size.width)/2,(d.height-size.height)/2);
    show();
  }
  void trace(String s) {
    mError.setText(s);
    System.out.println(s);
  }
  public static Connection createConnection(Frame owner,String title) {
    ConnectionDialog dialog=new ConnectionDialog(owner,title);
    dialog.create();
    return dialog.mConnection;
  }
  public static Label createLabel(String s) {
    Label l=new Label(s);
    l.setBackground(SystemColor.control);
    return l;
  }
  public static Panel createBorderPanel(Component center) {
    Panel p=new Panel();
    p.setBackground(SystemColor.control);
    p.setLayout(new BorderLayout());
    p.add("Center",center);
    p.add("North",createLabel(""));
    p.add("South",createLabel(""));
    p.add("East",createLabel(""));
    p.add("West",createLabel(""));
    p.setBackground(SystemColor.control);
    return p;
  }
  public void actionPerformed(ActionEvent ev) {
    String s=ev.getActionCommand();
    if(s.equals("ConnectOk")) {
      try {
        mConnection=createConnection(
          mDriver.getText(),
          mURL.getText(),
          mUser.getText(),
          mPassword.getText()
        );
        dispose();
      } catch(Exception e) {
        e.printStackTrace();
        mError.setText(e.toString());
      }
    } else if(s.equals("ConnectCancel")) {
      dispose();
    }
  }
  public void itemStateChanged(ItemEvent e) {
    String s=(String)e.getItem();
    for(int i=0;i<sJDBCTypes.length;i++) {
      if(s.equals(sJDBCTypes[i][0])) {
        mDriver.setText(sJDBCTypes[i][1]);
        mURL.setText(sJDBCTypes[i][2]);
      }
    }
  }
}
