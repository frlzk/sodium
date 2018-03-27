package sodium.print.impl;

import java.util.ArrayList;
import java.util.List;

import net.sf.xmlform.form.Field;

public class ColumnModel {
	private String name;
	private String text;
	private int wdith = 80;
	private int height = 20;
	private int boxheight = 20;
	private byte VERTICAL_ALIGN;
	private byte HORIZONTAL_ALIGN;
	private boolean isStretchWithOverflow;
	private boolean hide = false;
	private Field field=null;
	private List children=new ArrayList();
	static final public int LABEL_NUL=16;
	public boolean isHide() {
		return hide;
	}
	public void setHide(boolean hide) {
		this.hide = hide;
	}
	private Class valueClass;
	public Class getValueClass() {
		return valueClass;
	}
	public void setValueClass(Class valueClass) {
		this.valueClass = valueClass;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getWdith() {
		return wdith;
	}

	public void setWdith(int wdith) {
		this.wdith = wdith;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public byte getVERTICAL_ALIGN() {
		return VERTICAL_ALIGN;
	}

	public void setVERTICAL_ALIGN(byte vertical_align) {
		VERTICAL_ALIGN = vertical_align;
	}

	public byte getHORIZONTAL_ALIGN() {
		return HORIZONTAL_ALIGN;
	}

	public void setHORIZONTAL_ALIGN(byte horizontal_align) {
		HORIZONTAL_ALIGN = horizontal_align;
	}

	public boolean isStretchWithOverflow() {
		return isStretchWithOverflow;
	}

	public void setStretchWithOverflow(boolean isStretchWithOverflow) {
		this.isStretchWithOverflow = isStretchWithOverflow;
	}
	public Field getField() {
		return field;
	}
	public void setField(Field field) {
		this.field = field;
	}
	public List getChildren(){
		return this.children;
	}
	public int getBoxWidth(){
		if(children.size()==0)
			return this.getWdith();
		int w=0;
		for(int i=0;i<children.size();i++){
			ColumnModel cm=(ColumnModel)children.get(i);
			w+=cm.getBoxWidth();
		}
		return w;
	}
	public int getBoxHeight(){
		return boxheight;
	}
	public void setBoxHeight(int h){
		boolean bottom=false;
		if(bottom){
			if(children.size()==0){
				boxheight=h*20;
				return;
			}
			for(int i=0;i<children.size();i++){
				ColumnModel cm=(ColumnModel)children.get(i);
				cm.setBoxHeight(h*20-this.height);
			}
		}else{
			if(children.size()==0){
				boxheight=20*h;
				return;
			}
			int c=childs();
			boxheight=(h-c)*20;
			for(int i=0;i<children.size();i++){
				ColumnModel cm=(ColumnModel)children.get(i);
				cm.setBoxHeight(c==0?1:c);
			}
		}
		
	}
	private int childs(){
		if(children.size()==0){
			return 0;
		}
		int m=0;
		for(int i=0;i<children.size();i++){
			ColumnModel cm=(ColumnModel)children.get(i);
			int c=cm.childs();
			if(c>m)
				m=c;
		}
		return m+1;
	}
	public int getColSpan(){
		if(children.size()==0)
			return 1;
		int w=0;
		for(int i=0;i<children.size();i++){
			ColumnModel cm=(ColumnModel)children.get(i);
			w+=cm.getColSpan();
		}
		return w;
	}
	public int getRowSpan(){
		return getBoxHeight()/20;
	}
}
