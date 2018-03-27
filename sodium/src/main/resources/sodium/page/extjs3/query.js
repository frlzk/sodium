/**
 * @author Liu Zhikun
 */
Ext.namespace("sodium.page");
sodium.page.SimpleQueryList=function(cfg){
	this.hideConditionField=false;
	this.printable=true;
	this._queryParam={firstResult:0,maxResults:20,sys:{group:"and",items:[]},user:{group:"and",items:[]}};
	sodium.page.SimpleQueryList.superclass.constructor.call(this,cfg);
};
Ext.extend(sodium.page.SimpleQueryList,sodium.page.FormPanel,{
	getQueryParam:function(){
		return this._queryParam;
	},
	setQueryParam:function(param){
		this._queryParam=param;
	},
	reloadData:function(){
		this._queryParam.firstResult=0;
		this._queryParam.maxResults=20;
		this.loadData();
	},
	loadData:function(){
		var data=this.createLoadParam();
		this._lastParameters=data;
		this.xmlformPage._doExecAction({scope:this,action:this.action,data:data,success:this.onLoadData});
	},
	createLoadParam:function(){
		return {
			data:{fields:["@id","sys","user"],data:[["query_1",Ext.encode(this._queryParam.sys),Ext.encode(this._queryParam.user)]]},
			firstresult:this._queryParam.firstResult,
			maxresults:this._queryParam.maxResults,
			version:"1.0"
		};
	},
	onLoadData:function(data){
		this._queryParam.firstResult=data.firstresult;
		this._queryParam.maxResults=data.maxresults;
		this.getXmlformRecordset().setData(data);
		this.fireEvent("dataloaded",{recordSet:this.getXmlformRecordset()});
	},
	initComponent:function(){
		this.bbar=this.createQueryPagingToolbar();
		sodium.page.SimpleQueryList.superclass.initComponent.call(this);
		var grid=this.xmlformBlock.rootXmlformBlock;
		this.getBottomToolbar().bind(this.xmlformBlock._blockStore);
		this._lastParameters=this.createLoadParam();
	},
	createFormToolBar:function(){
		if(this.hideConditionField==true){
			if(this.actions){
				return this.actions;
			}else{
				return [];
			}
		}
		var fieldWidgets=[];
		this.fieldWidgetMaps={};
		this.parseWidgets(fieldWidgets,this.fieldWidgetMaps,this.xmlformLayout);
		var xmlform=this.xmlformParamForm.forms[this.xmlformParamForm.root];
		var items=xmlform.fields;
		var fields=[];
		for (var i=0;i<fieldWidgets.length;i++){
			var field=net.sf.xmlform.model.Util.getXmlformField(xmlform,fieldWidgets[i]["field"]);
			if(field!=null){
				fields.push({value:field.name,text:field.label});
			}
		}
		if(fields.length==0){
			return fields;
		}
		this.fieldNamesStore=new Ext.data.JsonStore({fields : ['value', 'text'],data : fields});
		this.fieldComponents={};
		var field=new Ext.form.ComboBox({
				store:this.fieldNamesStore,
				valueField:"value",
				displayField:"text",
				mode:"local",
				triggerAction:"all",
				emptyText:"选择查询字段",
				selectOnFocus:true,
				width:100
			});
		field.on("select",this.onQueryFieldChange,this);
		var submitBtn=new Ext.Toolbar.SplitButton({
				text:"",
				iconCls:"profiles-query",
				menu:new Ext.menu.Menu({items:{text:"高级查询",iconCls:"profiles-advance-query",handler:this.onQueryAdvancedQuery,scope:this}})
			});
		submitBtn.on("click", this.onQuerySubmitQuery, this);
		this.fieldContainer=this.createConditionField(this.fieldComponents,this);
		var items=[field, "-",this.fieldContainer, "-", submitBtn];
		if(this.printable==true){
			items.push(new Ext.Button({iconCls:"profiles-print",text:"打印",handler:this.doQueryListPrint,	scope:this}));
		};
		if(this.actions){
			items=items.concat(this.actions);
		}
		return this.xmlformPage.onCreatePageQueryBar({},items);
	},
	getFieldNamesStore:function(){
		return this.fieldNamesStore;
	},
	createConditionField:function(fieldComponents,update){
		var fieldWidgets=this.fieldWidgetMaps;
		var xmlform=this.xmlformParamForm.forms[this.xmlformParamForm.root];
		var items=xmlform.fields;
		var up={setValue:function(f,v){update._queryFieldValue=v;}};
		
		var comps=[];
		for (var i=0;i<items.length;i++){
			if(!fieldWidgets[items[i].name]){
				continue;
			}
			var param={
					form:xmlform,
					field:items[i],
					widget:fieldWidgets[items[i].name].widget?fieldWidgets[items[i].name].widget:null,
					updateProxy:up
			};
			var widget=net.sf.xmlform.web.js.extjs.createFormFieldWidget(param);
			if(widget==null){
				continue;
			}
			widget.nipWidgetType=fieldWidgets[items[i].name].widget.type;
			widget.nipDataType=items[i].type;
			fieldComponents[items[i].name]=widget.id;
			widget.on("afterrender",this.reloadSelectData,this);
			comps.push(widget);
		}
		comps.push(new Ext.form.TextField({width:150,selectOnFocus:true,name:"page",emptyText:"查询条件"}));
		var fieldContainer=new Ext.Container({
		   layout:"card",
		   activeItem: comps.length-1,
		   defaults: {
		       border:false,frame:false
		   },
		   items: comps
		});
		return fieldContainer;
	},
	reloadSelectData:function(obj){
		obj.setReadonlyFromXmlform(false);
		if(obj.reload){
			obj.reload();
		}
	},
	parseWidgets:function(fieldWidgets,fieldWidgetMaps,obj){
		if(obj["field"]&&obj["type"]&&obj["type"]=="field"){
			fieldWidgets.push(obj);
			fieldWidgetMaps[obj["field"]]=obj;
		}
		for(var k in obj){
			if(typeof(obj[k])=="object"){
				this.parseWidgets(fieldWidgets,fieldWidgetMaps,obj[k]);
			}
		}
	},
	onQueryFieldChange:function(field,record,evt){
		this._queryFieldName=field.getName();
		this.fieldContainer.getLayout().setActiveItem(this.fieldComponents[field.getName()]);
		this._queryValueField=Ext.getCmp(this.fieldComponents[field.getName()]);
	},
	onQuerySubmitQuery:function(btn){
		this._queryParam.user.items=[];
		if(this._queryFieldName&&this._queryValueField&&this._queryFieldValue){
			var value=this._queryFieldValue;
			if(this._queryFieldName.length>0&&value.length>0){
				var op="=";
				if(this._queryValueField.nipWidgetType=="input"&&this._queryValueField.nipDataType=="string"){
					op="like";
				}
				this._queryParam.user.items=[{field:this._queryFieldName,op:op,value:value}];
			}
		}
		this.loadData();
	},
	onQueryAdvancedQuery:function(){
		var adv=new sodium.page.AdvQuery({simpleQueryList:this});
		var winCfg={
			title:"高级查询",
			modal:true,
			layout:'fit',
			width:600,
			height:450,
			plain: true,
			maximizable:true,
			items:adv,
			constrain:true
		};
		var win = new Ext.Window(winCfg);
		adv.win=win;
		win.show();
	},
	createQueryPagingToolbar:function(){
		return new sodium.page.SimpleQueryPagingToolbar({queryForm:this});
	},
	onLoadNextPage:function(d){
		this._queryParam.firstResult=d;
		this.loadData();
	},
	onRowDblClick:function(grid,index,evt){
		this._doRowClick(grid,index,"rowdblclick");
	},
	onRowClick:function(grid,index,evt){
		this._doRowClick(grid,index,"rowclick");
	},
	_doRowClick:function(grid,index,evt){
		var record=grid.store.getAt(index);
		if(record==null)
			return;
		var r=this.getXmlformRecordset().getRecord(record.data["@id"]);
		this.fireEvent(evt,{record:r});
	},
	doQueryListPrint:function(){
		this.xmlformPage.printPage({action:this.action,title:this.xmlformPage.pageTitle,params:this._lastParameters,type:"list"},{showPageRadio:true});
	}
});

