package sodium.database.sequence;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Liu Zhikun
 */

/*
CREATE TABLE sodium_sequence(
   sid                  int not null auto_increment,
   scache               int,
   primary key (sid)
)
 */
public class MySqlSequence extends AbstractSqlSequence {
	private String table="sodium_sequence",idField="sid",cacheField="scache";
	public MySqlSequence() {
		super();
	}
	protected long generate() throws SQLException{
		Connection conn=this.getDataSource().getConnection();
		Statement pstmt=null;
		ResultSet rs=null;
		try{
			pstmt=conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,java.sql.ResultSet.CONCUR_UPDATABLE);
			pstmt.executeUpdate("INSERT INTO "+table+"("+cacheField+") VALUES("+getCache()+")");
			rs=pstmt.getGeneratedKeys();
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
