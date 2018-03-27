package sodium.database.sequence;

import java.sql.SQLException;

import javax.sql.DataSource;

import sodium.database.Sequence;

/**
 * @author Liu Zhikun
 */

abstract public class AbstractSqlSequence  implements Sequence {
		private DataSource dataSource;
		private long cache=100;
		private long start=cache,end=cache;
		private String name;

		public DataSource getDataSource() {
			return dataSource;
		}

		public void setDataSource(DataSource dataSource) {
			this.dataSource = dataSource;
		}

		protected long getCache() {
			return cache;
		}

		protected void setCache(long cache) {
			this.cache = cache;
		}
		
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
		
		public void init(){
			SequenceHolder.addSequence(this);
		}

		synchronized public long getNextVal() {
			if(start==end){
				try {
					long p=generate();
					start=p*cache;
					end=(p+1)*cache-1;
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			return start++;
		}
		
		abstract protected long generate() throws SQLException;
}
