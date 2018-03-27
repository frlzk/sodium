package sodium.database.sequence;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Liu Zhikun
 */

public class PostgreSqlSequence extends AbstractSqlSequence {
	private String databaseSequence="sodium_sequence";
	public PostgreSqlSequence() {
		super();
	}
	
	public String getDatabaseSequence() {
		return databaseSequence;
	}

	public void setDatabaseSequence(String databaseSequence) {
		this.databaseSequence = databaseSequence;
	}

	protected long generate() throws SQLException {
		Connection conn=this.getDataSource().getConnection();
		Statement pstmt=null;
		ResultSet rs=null;
		try{
			pstmt=conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,java.sql.ResultSet.CONCUR_UPDATABLE);
			rs=pstmt.executeQuery("SELECT nextval('"+databaseSequence+"')");
			rs.next();
			return rs.getLong(1);
		}finally{
			if(rs!=null){
				rs.close();
			}
			if(pstmt!=null){
				pstmt.close();
			}
			conn.close();
		}
	}

}
