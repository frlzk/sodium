package sodium.file.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;

import sodium.file.File;
import sodium.file.FileManager;

/**
 * @author Liu Zhikun
 */

/*
 * CREATE TABLE sodium_uploadfiles(
	id VARCHAR(35) NOT NULL,
	name VARCHAR(100) NOT NULL,
	mimeType VARCHAR(30) NOT NULL,
	claimed CHAR(1) NOT NULL,
	uploadtime TIMESTAMP NOT NULL,
	ownertable VARCHAR(40),
	ownerfield VARCHAR(40),
	ownerpk VARCHAR(50),
	data BLOB
)
 */
public class MySqlFileManager implements  FileManager {
	private DataSource dataSource;
	private int maxFileSize=200;//k
	private String fileDirectory=null;
	private String fileTable="sodium_uploadfiles";
	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public String getFileTable() {
		return fileTable;
	}

	public void setFileTable(String fileTable) {
		this.fileTable = fileTable;
	}
	
	public int getMaxFileSize() {
		return maxFileSize;
	}

	public void setMaxFileSize(int maxFileSize) {
		this.maxFileSize = maxFileSize;
	}

	public void save( File file) {
		try{
			dosave(file);
		}catch(Exception e){
			throw new IllegalArgumentException(e.getMessage(),e);
		}
	}
	private void dosave(File file)throws Exception {
		if(file.getData().length>maxFileSize*1024){
			throw new Exception("最大文件尺寸"+maxFileSize+"k");
		}
		String id=file.getId();
		if(id==null){
			id=UUID.randomUUID().toString().replace("-", "");
			file.setId(id);
		}
		Connection conn=dataSource.getConnection();
		PreparedStatement pstmt=null;
		try{
			doremove(conn,file.getId());
			String fid=file.getId();
			if(fileDirectory==null){
				ByteArrayInputStream is=new ByteArrayInputStream(file.getData());
				pstmt = conn.prepareStatement("INSERT INTO "+fileTable+"(id,name,mimeType,uploadtime,data,claimed) values(?,?,?,?,?,'0')");
				pstmt.setBinaryStream(5,is,is.available());
			}else{
				pstmt = conn.prepareStatement("INSERT INTO "+fileTable+"(id,name,mimeType,uploadtime,claimed) values(?,?,?,?,'0')");
				String subDir="00/00";
				if(fid.length()>4)
					subDir=fid.substring(0,2)+"/"+fid.substring(2,4);
				java.io.File dir=new java.io.File(fileDirectory,subDir);
				dir.mkdirs();
				java.io.File dataFile=new java.io.File(dir,fid);
				IOUtils.write(file.getData(), new FileOutputStream(dataFile));
			}
			pstmt.setString(1,file.getId());
			pstmt.setString(2,file.getName());
			pstmt.setString(3,file.getMimeType());
			pstmt.setTimestamp(4,new Timestamp(System.currentTimeMillis()));
			pstmt.executeUpdate();
		}finally{
			if(pstmt!=null){
				try {
					pstmt.close();
				} catch (SQLException e) {
				}
			}
			try {
				conn.close();
			} catch (SQLException e) {
			}
		}
	}

	public File load( String fid){
		try{
			return doload(fid);
		}catch(Exception e){
			throw new IllegalArgumentException(e.getMessage(),e);
		}
	}
	private File doload(String fid)throws Exception{
		Connection conn=dataSource.getConnection();
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		try{
			pstmt = conn.prepareStatement("SELECT id,name,mimeType,uploadtime,data FROM "+fileTable+" WHERE id=?");
			pstmt.setString(1,fid);
			rs=pstmt.executeQuery();
			if(!rs.next())
				return null;
			File fi=new File();
			if(fileDirectory==null){
				InputStream is=rs.getBinaryStream(5);
				ByteArrayOutputStream os=new ByteArrayOutputStream();
				int len;
				byte buffer[] = new byte[1024];
				len = is.read(buffer);
				while (len != -1) {
					os.write(buffer, 0, len);
					len = is.read(buffer);
				}
				fi.setData(os.toByteArray());
			}else{
				String subDir="00/00";
				if(fid.length()>4)
					subDir=fid.substring(0,2)+"/"+fid.substring(2,4);
				java.io.File dir=new java.io.File(fileDirectory,subDir);
				java.io.File dataFile=new java.io.File(dir,fid);
				if(dataFile.exists()==false){
					return null;
				}
				fi.setData(IOUtils.toByteArray(new FileInputStream(dataFile)));
			}
			
			fi.setId(rs.getString(1));
			fi.setName(rs.getString(2));
			fi.setMimeType(rs.getString(3));
			
			return fi;
		}finally{
			if(rs!=null){
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if(pstmt!=null){
				try {
					pstmt.close();
				} catch (SQLException e) {
				}
			}
			try {
				conn.close();
			} catch (SQLException e) {
			}
		}
	}
	public void remove( String fid){
		try{
			doremove(fid);
		}catch(Exception e){
			throw new IllegalArgumentException(e.getMessage(),e);
		}
	}
	private void doremove( String fid)  throws Exception{
		Connection conn=dataSource.getConnection();
		try{
			doremove(conn,fid);
		}finally{
			try {
				conn.close();
			} catch (SQLException e) {
			}
		}
	}
	
	public void claim( String fid,Map info){
		try{
			if(info==null)
				info=new HashMap();
			doclaim(fid,info);
		}catch(Exception e){
			throw new IllegalArgumentException(e.getMessage(),e);
		}
	}
	private void doclaim( String fid,Map info) throws Exception {
		Connection conn=dataSource.getConnection();
		PreparedStatement pstmt=null;
		try{
			StringBuilder sql=new StringBuilder("UPDATE "+fileTable+" SET claimed='1'");
			Iterator it=info.keySet().iterator();
			while(it.hasNext()){
				String k=it.next().toString();
				sql.append(",").append(k).append("=?");
			}
			sql.append("WHERE id=?");
			pstmt = conn.prepareStatement(sql.toString());
			it=info.keySet().iterator();
			int idx=1;
			while(it.hasNext()){
				Object k=it.next();
				pstmt.setObject(idx, info.get(k));
				idx++;
			}
			pstmt.setString(idx,fid);
			pstmt.executeUpdate();
		}finally{
			if(pstmt!=null){
				try {
					pstmt.close();
				} catch (SQLException e) {
				}
			}
			try {
				conn.close();
			} catch (SQLException e) {
			}
		}
	}
	private void doremove(Connection conn,String fid)throws Exception{
		PreparedStatement pstmt=null;
		try{
			String subDir="00/00";
			if(fid.length()>4)
				subDir=fid.substring(0,2)+"/"+fid.substring(2,4);
			java.io.File dir=new java.io.File(fileDirectory,subDir);
			java.io.File dataFile=new java.io.File(dir,fid);
			dataFile.delete();
			pstmt = conn.prepareStatement("DELETE FROM "+fileTable+" WHERE id=?");
			pstmt.setString(1,fid);
			pstmt.executeUpdate();
		}finally{
			if(pstmt!=null){
				try {
					pstmt.close();
				} catch (SQLException e) {
				}
			}
		}
	}
}
