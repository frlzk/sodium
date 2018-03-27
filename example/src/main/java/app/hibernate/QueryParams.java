package app.hibernate;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.xmlform.data.SourceInfo;
import net.sf.xmlform.type.BooleanType;
import net.sf.xmlform.type.ByteType;
import net.sf.xmlform.type.DateTimeType;
import net.sf.xmlform.type.DateType;
import net.sf.xmlform.type.DecimalType;
import net.sf.xmlform.type.DoubleType;
import net.sf.xmlform.type.FloatType;
import net.sf.xmlform.type.IntType;
import net.sf.xmlform.type.IntegerType;
import net.sf.xmlform.type.LongType;
import net.sf.xmlform.type.ShortType;
import net.sf.xmlform.type.StringType;
import net.sf.xmlform.type.TimeType;
import net.sf.xmlform.type.YearMonthType;

import org.hibernate.Query;

public class QueryParams {
	final public static String ISNULL="isnull";
	final public static String NOTNULL="notnull";
	final public static String LIKE="like";
	final public static String LLIKE="llike";
	final public static String RLIKE="rlike";
	final public static String IN="in";
	final public static String NOTIN="notin";
	final public static String EQ="=";
	final public static String NE="<>";
	final public static String GT=">";
	final public static String GE=">=";
	final public static String LT="<";
	final public static String LE="<=";
	private static Set ops=new HashSet();
	static{
		ops.add("=");
		ops.add("<>");
		ops.add(">");
		ops.add(">=");
		ops.add("<");
		ops.add("<=");
		ops.add(ISNULL);
		ops.add(NOTNULL);
		ops.add(LIKE);
		ops.add(LLIKE);
		ops.add(RLIKE);
		ops.add(IN);
		ops.add(NOTIN);
	}
	private Map values=new HashMap();
	private int seq=0;
	public String append(String field,String op,Object value){
		StringBuilder sb=new StringBuilder();
		append(sb,field,op,value);
		return sb.toString();
	}
	public void append(StringBuilder sb,String field,String op,Object value){
		if(!ops.contains(op)){
			System.out.println("Not support op: "+op);
			return;
		}
		String key="_q_p_"+(seq++);
		if(value==null||ISNULL.equalsIgnoreCase(op)){
			sb.append(" ").append(field).append(" IS NULL ");
		}else if(NOTNULL.equalsIgnoreCase(op)){
			sb.append(" ").append(field).append(" IS NOT NULL ");
		}else if(NOTNULL.equalsIgnoreCase(op)){
			sb.append(" ").append(field).append(" IS NOT NULL ");
		}else if(LIKE.equalsIgnoreCase(op)){
			String v="%"+value.toString()+"%";
			values.put(key, v);
			sb.append(" ").append(field).append(" LIKE :").append(key);
		}else if(LLIKE.equalsIgnoreCase(op)){
			String v=value.toString()+"%";
			values.put(key, v);
			sb.append(" ").append(field).append(" LIKE :").append(key);
		}else if(RLIKE.equalsIgnoreCase(op)){
			String v="%"+value.toString();
			values.put(key, v);
			sb.append(" ").append(field).append(" LIKE :").append(key);
		}else if(IN.equalsIgnoreCase(op)){
			values.put(key, value);
			sb.append(" ").append(field).append(" IN (:").append(key).append(")");
		}else if(NOTIN.equalsIgnoreCase(op)){
			values.put(key, value);
			sb.append(" ").append(field).append(" NOT IN (:").append(key).append(")");
		}else{
			values.put(key, value);
			sb.append(" ").append(field).append(op).append(":").append(key);
		}
	}
	public Map getValues(){
		return values;
	}
	public void apply(Query query){
		Iterator it=values.keySet().iterator();
		while(it.hasNext()){
			String key=(String)it.next();
			setQueryParameter(query, key, values.get(key));
		}
	}
	public String toString() {
		StringBuilder sb=new StringBuilder();
		Iterator it=values.keySet().iterator();
		while(it.hasNext()){
			String key=(String)it.next();
			if(sb.length()>0)
				sb.append(",");
			sb.append(key).append(":").append(values.get(key));
		}
		return sb.toString();
	}
	static public void setQueryParameter(Query query,String name,Object value){
		Setter st=(Setter)classSetter.get(value.getClass());
		if(st==null&&(value instanceof List)){
			st=(Setter)classSetter.get(List.class);
		}
		st.setValue(query, name, value);
	}
	static public void setQueryParameter(Query query,String type,String name,Object value){
		Setter st=(Setter)valueSetter.get(type);
		st.setValue(query, name, value);
	}
	private static Map valueSetter=new HashMap();
	private static Map classSetter=new HashMap();
	static{
		valueSetter.put(StringType.NAME, new StringSetter());
		valueSetter.put(YearMonthType.NAME, new StringSetter());
		valueSetter.put(DateType.NAME, new DateSetter());
		valueSetter.put(DateTimeType.NAME, new DateTimeSetter());
		valueSetter.put(TimeType.NAME, new TimeSetter());
		valueSetter.put(BooleanType.NAME, new BooleanSetter());
		valueSetter.put(ByteType.NAME, new ByteSetter());
		valueSetter.put(DecimalType.NAME, new DecimalSetter());
		valueSetter.put(DoubleType.NAME, new DoubleSetter());
		valueSetter.put(FloatType.NAME, new FloatSetter());
		valueSetter.put(IntegerType.NAME, new IntegerSetter());
		valueSetter.put(IntType.NAME, new IntSetter());
		valueSetter.put(LongType.NAME, new LongSetter());
		valueSetter.put(ShortType.NAME, new ShortSetter());
		
		classSetter.put(String.class, new StringSetter());
		classSetter.put(Date.class, new DateSetter());
		classSetter.put(Timestamp.class, new DateTimeSetter());
		classSetter.put(Time.class, new TimeSetter());
		classSetter.put(Boolean.class, new BooleanSetter());
		classSetter.put(Byte.class, new ByteSetter());
		classSetter.put(BigDecimal.class, new DecimalSetter());
		classSetter.put(Double.class, new DoubleSetter());
		classSetter.put(Float.class, new FloatSetter());
		classSetter.put(BigInteger.class, new IntegerSetter());
		classSetter.put(Integer.class, new IntSetter());
		classSetter.put(Long.class, new LongSetter());
		classSetter.put(Short.class, new ShortSetter());
		classSetter.put(List.class, new InListSetter());
	}
	interface Setter{
		public void setValue(Query query,String name,Object value);
	}
	static  class StringSetter implements Setter{
		public void setValue(Query query,String name,Object value){
			query.setString(name, (String)value);
		}
	}
	static  class DateSetter implements Setter{
		public void setValue(Query query,String name,Object value){
			query.setDate(name, (Date)value);
		}
	}
	static  class DateTimeSetter implements Setter{
		public void setValue(Query query,String name,Object value){
			query.setTimestamp(name, (Timestamp)value);
		}
	}
	static  class TimeSetter implements Setter{
		public void setValue(Query query,String name,Object value){
			query.setTime(name, (Time)value);
		}
	}
	static  class BooleanSetter implements Setter{
		public void setValue(Query query,String name,Object value){
			query.setBoolean(name, (Boolean)value);
		}
	}
	static  class ByteSetter implements Setter{
		public void setValue(Query query,String name,Object value){
			query.setByte(name, (Byte)value);
		}
	}
	static  class DecimalSetter implements Setter{
		public void setValue(Query query,String name,Object value){
			query.setBigDecimal(name, (BigDecimal)value);
		}
	}
	static  class DoubleSetter implements Setter{
		public void setValue(Query query,String name,Object value){
			query.setDouble(name, (Double)value);
		}
	}
	static  class FloatSetter implements Setter{
		public void setValue(Query query,String name,Object value){
			query.setFloat(name, (Float)value);
		}
	}
	static  class IntegerSetter implements Setter{
		public void setValue(Query query,String name,Object value){
			query.setBigInteger(name, (BigInteger)value);
		}
	}
	static  class IntSetter implements Setter{
		public void setValue(Query query,String name,Object value){
			query.setInteger(name, (Integer)value);
		}
	}
	static  class LongSetter implements Setter{
		public void setValue(Query query,String name,Object value){
			query.setLong(name, (Long)value);
		}
	}
	static  class ShortSetter implements Setter{
		public void setValue(Query query,String name,Object value){
			query.setShort(name, (Short)value);
		}
	}
	static  class InListSetter implements Setter{
		public void setValue(Query query,String name,Object value){
			query.setParameterList(name, (List)value);
		}
	}
	static public void mergeMap(SourceInfo info,Map from,Map to){
		Set fs=info.getFieldNames();
		Iterator it=fs.iterator();
		while(it.hasNext()){
			String n=(String)it.next();
			to.put(n, from.get(n));
		}
	}
	public String add(String field,String op,Object value){
		return append(field, op, value);
	}
	public void add(StringBuilder sb,String field,String op,Object value){
		append(sb, field, op, value);
	}
}
