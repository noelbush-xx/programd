/*
 * FindFile.java
 */

package org.alicebot.server.sql.util;
import java.sql.*;
import java.io.*;

class FindFile {

  // The entry point of this class
  public static void main(String arg[]) {

    // Exceptions may occur
    try {

      // Load the Hypersonic SQL JDBC driver
      Class.forName("org.alicebot.server.sql.jdbcDriver");

      // Connect to the database
      // It will be create automatically if it does not yet exist
      // 'testfiles' in the URL is the name of the database
      // "sa" is the user name and "" is the (empty) password
      Connection conn=DriverManager.getConnection(
        "jdbc:alicebot:testfiles","dany",""
      );

      // Check the command line parameters
      if(arg.length==1) {

        // One parameter:
        // Find and print the list of files that are like this
        listFiles(conn,arg[0]);

      } else if(arg.length==2 && arg[0].equals("-init")) {

        // Command line parameters: -init pathname
        // Init the database and fill all file names in
        fillFileNames(conn,arg[1]);

      } else {

        // Display the usage info
        System.out.println("Usage:");
        System.out.println("java FindFile -init .");
        System.out.println("  Re-create database from directory '.'");
        System.out.println("java FindFile name");
        System.out.println("  Find files like 'name'");
      }

      // Finally, close the connection
      conn.close();

    } catch(Exception e) {

      // Print out the error message
      System.out.println(e);
      e.printStackTrace();
    }

  }

  // Search in the database and list out files like this
  static void listFiles(Connection conn,String name) 
  throws SQLException {

    System.out.println("Files like '"+name+"'");

    // Convert to upper case, so the search is case-insensitive
    name=name.toUpperCase();

    // Create a statement object
    Statement stat=conn.createStatement();

    // Now execute the search query
    // UCASE: This is a case insensitive search
    // ESCAPE ':' is used so it can be easily searched for '\'
    ResultSet result=stat.executeQuery(
      "SELECT Path FROM Files WHERE "+
      "UCASE(Path) LIKE '%"+name+"%' ESCAPE ':'");

    // Moves to the next record until no more records
    while(result.next()) {

      // Print the first column of the result 
      // could use also getString("Path")
      System.out.println(result.getString(1));

    }

    // Close the ResultSet - not really necessary, but recommended
    result.close();
  }

  // Re-create the database and fill the file names in
  static void fillFileNames(Connection conn,String root) 
  throws SQLException {

    System.out.println("Re-creating the database...");

    // Create a statement object
    Statement stat=conn.createStatement();

    // Try to drop the table
    try {
      stat.executeUpdate("DROP TABLE Files");
    } catch(SQLException e) {
      // Ignore Exception, because the table may not yet exist
    }

    // For compatibility to other database, use varchar(255)
    // In Hypersonic SQL, length is unlimited, like Java Strings
    stat.execute("CREATE TABLE Files"+
      "(Path varchar(255),Name varchar(255))");

    // Close the Statement object, it is no longer used
    stat.close();

    // Use a PreparedStatement because Path and Name could contain '
    PreparedStatement prep=conn.prepareCall(
      "INSERT INTO Files (Path,Name) VALUES (?,?)"
    );

    // Start with the 'root' directory and recurse all subdirectories
    fillPath(root,"",prep);

    // Close the PreparedStatement
    prep.close();

    System.out.println("Finished");
  }

  // Fill the file names, using the PreparedStatement
  static void fillPath(String path,String name,
  PreparedStatement prep) throws SQLException {

    File f=new File(path);
    if(f.isFile()) {
    
      // Clear all Parameters of the PreparedStatement
      prep.clearParameters();

      // Fill the first parameter: Path
      prep.setString(1,path); 

      // Fill the second parameter: Name
      prep.setString(2,name); 

      // Its a file: add it to the table
      prep.execute();

    } else if(f.isDirectory()) {

      if(!path.endsWith(File.separator)) {
        path+=File.separator;
      }
      String list[]=f.list();

      // Process all files recursivly
      for(int i=0;list!=null && i<list.length;i++) {
        fillPath(path+list[i],list[i],prep);
      }
    }
  }
}

