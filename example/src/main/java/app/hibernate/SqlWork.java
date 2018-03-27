package app.hibernate;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.jdbc.Work;


public abstract class SqlWork implements Work {
	private Object result=null;
	Connection connection=null;
	public void execute(Connection conn)throws SQLException{
		try{
			this.connection=conn;
			result=doSqlWork();
		}finally{
			this.connection=null;
		}
	}
	protected Connection getConnection(){
		return connection;
	}
	protected void executeUpdate(String sql) throws SQLException{
		executeUpdate(sql,new Object[]{});
	}
	protected void executeUpdate(String sql,Object param[]) throws SQLException{
		PreparedStatement st=getConnection().prepareStatement(sql);
		try{
			setParameters(st,param);
			st.executeUpdate();
		}finally{
			try{
				st.close();
			}catch(Exception e){
				
			}
		}
	}
	protected List query(String sql) throws SQLException{
		return query(sql,new Object[]{});
	}
	protected List query(String sql,Object param[]) throws SQLException{
		PreparedStatement st=getConnection().prepareStatement(sql);
		try{
			setParameters(st,param);
			return resultSetToList(st.executeQuery());
		}finally{
			try{
				st.close();
			}catch(Exception e){
				
			}
		}
	}
	protected List resultSetToList(ResultSet rs) throws SQLException{
		return resultSetToList(rs,0,Integer.MAX_VALUE);
	}
	protected List resultSetToList(ResultSet rs,long offset, long limit) throws SQLException{
		List list=new ArrayList();
		long idx=0;
		String fns[]=null;
		Set nullTypes=new HashSet();
		if(rs!=null){
			fns=new String[rs.getMetaData().getColumnCount()];
			int dt[]=new int[rs.getMetaData().getColumnCount()];
            for(int i=0;i<fns.length;i++){
            	dt[i]=0;
                fns[i]=rs.getMetaData().getColumnName(i+1);
                fns[i]=fns[i].toLowerCase();
                nullTypes.add(fns[i]);
                if(rs.getMetaData().getColumnType(i+1)==Types.DATE){
                	dt[i]=1;
                }
            }
            boolean first=true;
			while(rs.next()){
				if(idx<offset){
					idx++;
					continue;
				}
				if(idx>=offset+limit){
					idx++;
					continue;
				}
				idx++;
				if(first==true){
					for(int i=0;i<fns.length;i++){
						if(dt[i]==1){
							try{
								rs.getTimestamp(i+1);
								dt[i]=2;
							}catch(Exception e){
								
							}
						}
					}
					first=false;
				}
				Map map=new HashMap();
	            for(int i=0;i<fns.length;i++){
	                Object value=null;
	                if(dt[i]==2)
	                	value=rs.getTimestamp(i+1);
	                else
	                	value=rs.getObject(i+1);
	                map.put(fns[i], value);
	                if(value!=null&&nullTypes!=null){
	                	if(nullTypes.contains(fns[i])){
	                		nullTypes.remove(fns[i]);
	                		if(nullTypes.size()==0)
	                			nullTypes=null;
	                	}
	                }
	                if(value instanceof Blob){
	                	Blob blob=(Blob)value;
						try {
							InputStream is=blob.getBinaryStream();
							ByteArrayOutputStream bos=new ByteArrayOutputStream();
							byte[] ch=new byte[1024];
							int len;
							while((len=is.read(ch))!=-1){
								bos.write(ch, 0, len);
							};
							is.close();
							map.put(fns[i],bos.toByteArray());
						} catch (Exception e) {
							e.printStackTrace();
						}
	                }else if(value instanceof Clob){
	                	Clob c=(Clob)value;
	                	try{
		                	Reader rd = c.getCharacterStream();
		                	int ch = 0;
		                	StringBuffer sb = new StringBuffer();
		                	while((ch = rd.read())!=-1 ) {
		                		sb.append((char)ch);
		                	}
		                	rd.close();
		                	map.put(fns[i],sb.toString());
	                	}catch(Exception e){
	                		e.printStackTrace();
	                	}
	                }
	            }
	            list.add(map);
	        }
		}
		return list;
	}
	protected void setParameters(PreparedStatement ps,Object values[]) throws SQLException{
		for(int i=0;i<values.length;i++){
			Object value=values[i];
			if(value instanceof String){
				ps.setString(i+1, (String)value);
			}else if(value instanceof Long){
				ps.setLong(i+1, (Long)value);
			}else if(value instanceof Timestamp){
				ps.setTimestamp(i+1, (Timestamp)value);
			}else if(value instanceof Integer){
				ps.setInt(i+1, (Integer)value);
			}else if(value instanceof Date){
				ps.setDate(i+1, (Date)value);
			}else if(value instanceof Time){
				ps.setTime(i+1, (Time)value);
			}else if(value instanceof Double){
				ps.setDouble(i+1, (Double)value);
			}else if(value instanceof Float){
				ps.setFloat(i+1, (Float)value);
			}else{
				ps.setObject(i+1, value);
			}
		}
	}
	protected PreparedStatement createStatement(String sql) throws SQLException{
		return getConnection().prepareStatement(sql);
	}
	public Object getResult(){
		return result;
	}
	abstract protected Object doSqlWork()throws SQLException;
}
