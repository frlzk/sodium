/**
 * @author Liu Zhikun
 */
Ext.namespace("sodium.page");
sodium.sodium.pageRenderer="extjs3";
sodium.page._createBasePage=function(winCfg){
	var pageCfgParams=winCfg.pageConfig,
		pageDataParams=winCfg.pageParams,
		pageClassName=winCfg.pageClass,
		pageMenu=winCfg.windowMenu,
		windowParams=winCfg.windowConfig;
	var pageBox=new sodium.page.PageBox({region:"center",firstPageClassName:pageClassName,firstPageConfig:pageCfgParams,firstPageData:pageDataParams});
	pageBox.setBoxTitle=function(f){
		sodium["\137\137\144\157\143"]["title"]=f;
	};
	pageBox.closeBox=function(){
		//window.close();
	};
	sodium.rootPageBox=pageBox;
	var mainPanel={xtype:"panel",
			region:"center",
			layout:"border",
			frame:false,
			border:false,
			bodyBorder:false,
			items:[pageBox]
	};
	if(pageCfgParams.console&&pageCfgParams.console=="true"){
		var mainConsole=new Ext.form.TextArea({
			region:"south",
			height:100,
			border:true,
			xtype:"textarea"
		});
		sodium.page._consoleTextarea=mainConsole;
		mainPanel.items.push(mainConsole);
	}
	sodium.page.CreatePageWindow(pageMenu,new Ext.Panel(mainPanel),windowParams);
};
require(["xmlform/panel"],function(panel){
	var referenceCache={};
	panel.loadReference=function(param){
		if(param.length==0){
			return;
		}
		var reqArray=[];
		var res=[];
		for(var i=0;i<param.length;i++){
			if(referenceCache[param[i].key]){
				res.push(param[i]);
			}else{
				reqArray.push(param[i]);
				var innerFields=["@id"];
				var innerData={form:"F1",data:[["a"]]};
				param[i].data={
					version:"1.0",
					head:{
						forms:{F1:{fields:innerFields}},
						firstresult:0,
						maxresults:2147483647
					},
					body:innerData,
				};
				for(var key in param[i].fieldArgs){
					innerFields.push(key);
					innerData.data[0].push(param[i].fieldArgs[key]);
				};
				for(var key in param[i].valueArgs){
					innerFields.push(key);
					innerData.data[0].push(param[i].valueArgs[key]);
				};
			}
		};
		if(reqArray.length>0){
			Ext.Ajax.request({
				url: sodium.page.createActionPath("reference"),
				method:"POST",
				success: sodium.page.onLoadReferenced,
				failure: function(resp){Ext.Msg.alert('Error', resp.responseText);},
				params: Ext.encode(reqArray)
			});
		};
		for(var i=0;i<res.length;i++){
			var cmp=Ext.getCmp(res[i].id);
			if(cmp!=null){
				res[i].data=referenceCache[res[i].key];
				cmp[res[i].ondataload](res[i]);
			}
		};
	};
});
sodium.page.onLoadReferenced=function(resp){
	var array=Ext.decode(resp.responseText);
	for(var i=0;i<array.length;i++){
		if(array[i].data.head.faultcode=="ok"){
			if((array[i].cache=="first"&&(!referenceCache[array[i].key]))
					||array[i].cache=="all"){
				referenceCache[array[i].key]=array[i].data;
			}
			var cmp=Ext.getCmp(array[i].id);
			if(cmp!=null){
				cmp[array[i].ondataload](array[i]);
			}
		}else if(array[i].data.head.faultcode!="c.session"){
			Ext.Msg.alert('Error', array[i].data.head.faultcode+": "+array[i].data.head.faultstring); 
		}
	}
};

sodium.page._sendDataToServer=function(params){
	Ext.Ajax.request({
		url: params.url,
		success: function(resp){params.success(Ext.decode(resp.responseText))},
		failure: params.failure,
		params: params.data
	});
};
sodium.declare=function(base,props){
	for(var k in props){
		if(typeof(props[k])=="function"){
			props[k]._funname=k;
			props[k]._funprot=base.prototype;
		}
	};
	return Ext.extend(base,props);
};
sodium.page.Logger=function(){
	this.info=function(msg){
		this._print("[info] "+msg);
	};
	this._print=function(msg){
		if(sodium.page._consoleTextarea==null){
			return;
		}
		var ta=sodium.page._consoleTextarea;
		ta.setValue(ta.getValue()+msg+"\n");
	}
};
sodium.page._consoleTextarea=null;
sodium.page._consoleLogger=new sodium.page.Logger();
sodium.page.PageBox=function(cfg){
	cfg=cfg||{};
	cfg.layout="fit";
	this._tabHeader=null;
	this._tabHeaderOldHeight=0;
	sodium.page.PageBox.superclass.constructor.call(this,cfg);
};
Ext.extend(sodium.page.PageBox,Ext.Panel,{
	initComponent:function(){
		sodium.page.PageBox.superclass.initComponent.call(this);
		var mainTab=new Ext.TabPanel({
			frame:false,
			border:false,
			bodyBorder:false,
			enableTabScroll:true
		});
		//mainTab.on("afterrender",this.onPageMainRender,this);
		mainTab.on("add",this.onPageMainTabChange,this);
		mainTab.on("remove",this.onPageMainTabChange,this);
		mainTab.on("tabchange",this.onPageMainTabChange,this);
		this._pageMainTab=mainTab;
		this.add(mainTab);
		this.onPageMainRender();
	},
	onPageMainRender:function(){
		this.openPageFrame(this.firstPageClassName,this.firstPageConfig,this.firstPageData);
	},
	onPageMainTabChange:function(){
		var atab=this._pageMainTab.getActiveTab();
		if(atab!=null){
			this.setBoxTitle(decodeURIComponent(atab.getPageTitle()).replace(/\+/g," "));
			atab.setPageFocus();
		}
			
		if(this._tabHeader==null&&this._pageMainTab.items.getCount()==1){
			var tab=Ext.get(this._pageMainTab.getTabEl(0));
			if(tab!=null){
				this._tabHeader=tab.parent(".x-tab-panel-header");
				this._tabHeaderOldHeight=this._tabHeader.getHeight();				
			}
		}
		if(this._tabHeader==null)
			return;
		if(this._pageMainTab.items.getCount()==1){
			this._tabHeader.hide();
			this._tabHeader.setHeight(0);
			this._pageMainTab.setHeight(this.getInnerHeight()-1);
		}else if(this._pageMainTab.items.getCount()==2){
			this._tabHeader.setHeight(this._tabHeaderOldHeight);
			this._tabHeader.show();
			this._pageMainTab.setHeight(this.getInnerHeight()-2);
		}
	},
	closePage:function(page){
		this._pageMainTab.remove(page);
		if(this._pageMainTab.items.getCount()==0){
			this.closeBox();
		}
	},
	openPageDialog:function(pageClass,config,data){
		var mwi=sodium.rootPageBox.getInnerWidth();
		var mhe=sodium.rootPageBox.getInnerHeight();
		var winCfg={
				title:"-",
				modal:true,
				layout:'fit',
				width:mwi/3*2,
				height:mhe/2*2,
				plain: true,
				maximizable:true,
				constrain:true,
				closeAction:"hide"
			};
		if(Ext.isDefined(config.reusable)){
			if(config.reusable==false){
				winCfg.closeAction="close";
			}
		}
		var win = new Ext.Window(winCfg);
		require([pageClass.replace(/\./g,"/")],this._openPageDialogCb(pageClass,config,data,win));
		return win.getId();
	},
	_openPageDialogCb:function(pageClass,config,data,win){
		return function(pc){
			var pageBox=new sodium.page.PageBox({firstPageClassName:pageClass,firstPageConfig:config,firstPageData:data});
			pageBox.setBoxTitle=function(title){win.setTitle(title);};
			pageBox.closeBox=function(){win.close();};
			//pageBox.addCls("pagebox-"+pageClass.toLowerCase().replace(/\./g,"-"));
			pageBox.on("createfirstpage",function(evt){
				var dim=evt.page.getSizePolicy();
				win.setWidth(sodium.config.dialogPadding+dim.width);
				win.setHeight(sodium.config.dialogPadding+dim.height);
				win.show();
			});
			win.add(pageBox);
		};
	},
	openPageFrame:function(pageClass,config,data){
		var info=["open: ",pageClass,", config: ",Ext.encode(config),", data: ",Ext.encode(data)];
		sodium.page._consoleLogger.info(info.join(""));
		require([pageClass.replace(/\./g,"/")],this._openPageFrameCb(pageClass,config,data));
	},
	_openPageFrameCb:function(pageClass,config,data){
		var THIS=this;
		return function(pc){
			var newFrame=new pc(config);
			newFrame.on("afterrender",function(){var hasAttr=false;for(var k in data){hasAttr=true;break;};newFrame._doOpenPage(hasAttr==true?data:null);});
			newFrame.title=newFrame.getPageTitle();
			var mainTab=THIS._pageMainTab;
			if(sodium.rootPageBox==mainTab||mainTab.items.getCount()>0){
				newFrame.closable=true;
			};
			if(mainTab.items.getCount()==0){
				mainTab._firstPageFrameId=newFrame.getId();
			};
			newFrame.pageBoxId=THIS.getId();
			if(config.target){
				if(config.target=="self"){
					var curTab=mainTab.getActiveTab();
					var idx=mainTab.items.indexOf(curTab);
					if(idx>0){
						mainTab.insert(idx,newFrame);
						mainTab.setActiveTab(newFrame);
						mainTab.remove(curTab,true);
						return;
					};
				}
			}
			mainTab.add(newFrame);
			mainTab.setActiveTab(newFrame);
			if(mainTab.items.getCount()==1){
				THIS.fireEvent("createfirstpage",{page:newFrame});		
			}
			//this.fireEvent("createPage",{type:"createPage",page:newFrame});
		};
	}
});

sodium.page.BaseExtJsPage=function(cfg){
	cfg=cfg||{};
	cfg.layout="fit";
	cfg.border=false;
	cfg.frame=false;
	cfg.bodyBorder=false;
	cfg.hideBorders=true;
	this._toolBarHeight=0;
	this._initVariable();
	sodium.page.BaseExtJsPage.superclass.constructor.call(this,cfg);
};
Ext.extend(sodium.page.BaseExtJsPage,Ext.Panel,{
	initComponent:function(){
		this.items=[];
		var ch=this._doCreatePage();
		if(ch!=null){
			this.items.push(ch);
			this._centerWidgetId=ch.id;
		}
		var tbar=this.createAttachActions("page");
		if(tbar.length>0){
			this.tbar=tbar;
			this._toolBarHeight=30;
		}
//		this.on("show",this.setPageFocus,this);
		sodium.page.BaseExtJsPage.superclass.initComponent.call(this);
		this._doTellDemiurge({page:this,type:"create"});
	},
	afterRender:function(container){
		sodium.page.BaseExtJsPage.superclass.afterRender.call(this,container);
		this._resetActionStauts();
//		this.el.addCls(this._pageCls);
	},
	destroy:function(){
		this._doTellDemiurge({page:this,type:"destroy"});
		for(var k in this._xmlformDialogs){
			this.byId(this._xmlformDialogs[k]).destroy();
		}
		sodium.page.BaseExtJsPage.superclass.destroy.call(this);
	},
	
	_bindPanelListener:function(panel){
		panel.on("rowdblclick",this.onRecordDblclick,this);
		panel.on("finishedit",this._onFormCompleteEdit,this);
	},
	_createActionButton:function(cfg){
		var btnCfg={
				id:cfg.id,
				scope:this,
				text:cfg.label,
				iconCls:cfg.icon,	
				anchorId:cfg.anchorId
			};
		if(cfg.children){
			var menuFile = new Ext.menu.Menu();
			for(var p=0;p<cfg.children.length;p++){
				var ch=cfg.children[p];
				menuFile.add({text: ch.label,handler:ch.handler,anchorId:ch.anchorId});
			}
			btnCfg.menu=menuFile;
		}else{
			btnCfg.handler=cfg.handler;
		};
		var btn=new Ext.Button(btnCfg);
		//btn.el.addCls(cfg.actionCls);
		return btn;
	},
	_createChartPanel:function(cfg){
		throw new Error("Not implement _createChartPanel");
	},
	_createFormPanel:function(cfg){
		return new net.sf.xmlform.extjs.FormPanel(cfg);
	},
	_getActionButton:function(actionId){
		return Ext.getCmp(actionId);
	},
	_getCookieValue:function(k){
		return null;
	},
	_setBtnEnable:function(btn,en){
		btn.setDisabled(!en);
	},
	_setBtnDisplay:function(btn,en){
		if(en==false){
			btn.hide();
		}else{
			btn.show();
		}
	},
	_showOldDialog:function(winId,pageClass,config,data){
		var win=this.byId(winId);
		if(win.items&&win.items.getCount()>0){
			var pb=win.items.getAt(0);
			var mainTab=pb._pageMainTab;
			var fid=mainTab._firstPageFrameId;
			var firstFrame=Ext.getCmp(fid);
			if(mainTab.getActiveTab()!=firstFrame){
				mainTab.setActiveTab(firstFrame);
			}
			firstFrame.onResetPage(data);
			win.show();
			firstFrame._doOpenPage(data);
			return true;
		}
		return false;
	},
	_showMessageBox:function(kind,msg,cb){
		var cb2=function(v){if(cb)cb(v=="yes");};
		if(kind=="confirm"){
			Ext.MessageBox.confirm(this.getPageTitle(),msg,cb2);
			return;
		}
		var icon=("info"==kind?Ext.Msg.INFO:Ext.Msg.ERROR);
		var cfg = {
                title : this.getPageTitle(),
                msg : msg,
                buttons: Ext.Msg.OK,
                fn: cb2,
                icon:icon,
                scope : this
            };
		Ext.Msg.alert(cfg);
	},
	
	byId:function(id){
		return Ext.getCmp(id);
	},
	fromJson:function(str){
		return Ext.decode(str);
	},
	getWindowSize:function(){
		return {width:sodium.rootPageBox.getInnerWidth(),height:sodium.rootPageBox.getInnerHeight()};
	},
	getSizePolicy:function(){
		var center=this.byId(this._centerWidgetId);
		if(center&&center.getSizePolicy){
			var sp=center.getSizePolicy();
			return {minheight:sp.minheight+this._toolBarHeight,
				bestheight:sp.bestheight+this._toolBarHeight,
				minwidth:sp.minwidth,
				bestwidth:sp.bestwidth};
		}
		var dim=this.getWindowSize();
		return {bestwidth:dim.width*2/3,bestheight:dim.height*2/3};
	},
	superMethod:function(a){
		var c=a.callee;
		return c._funprot[c._funname].apply(this,a);
	},
	toJson:function(obj){
		return Ext.encode(obj);
	}
});

sodium.page.FormPanel=function(cfg){
	this.maxresults=0;
	cfg.border=false;
//	cfg.frame=false;
	sodium.page.FormPanel.superclass.constructor.call(this,cfg);
};
Ext.extend(sodium.page.FormPanel,net.sf.xmlform.extjs.FormPanel,{
	initComponent:function(){
		this._adpterStore=new Ext.data.Store({_firstResult:0,_totalResult:0,_maxresults:this.maxresults,getCount : function(){return this._maxresults;},
			getTotalCount : function(){return this._totalResult ;}});
		var tbar=this.createFormToolBar();
		if(tbar!=null&&tbar.length>0){
			this.tbar=tbar;
		}
		var bbar=this.createFormBottomBar();
		if(bbar!=null){
			this.bbar=bbar;
		}
		sodium.page.FormPanel.superclass.initComponent.call(this);
	},
	setPageInfo:function(firstresult,totalresult){
		this._adpterStore._firstResult=firstresult;
		this._adpterStore._totalResult=totalresult;
		this._adpterStore.fireEvent('load', this._adpterStore, [], {});
	},
	onCreateMarkWidget:function(param){
		var p=Ext.getCmp(this._currentPageId);
		return p.onCreateMarkWidget(param);
	},
	createFormToolBar:function(){
		if(this.actions)
			return this.actions;
		else
			return null;
	},
	createFormBottomBar:function(){
		if(this.maxresults>0){
			return new sodium.page.SimpleQueryPagingToolbar({ownerFormId:this.id,pageSize:this.maxresults,store:this._adpterStore});
		}else{
			return null;
		}
	},
	onQueryNextPage:function(cfg){
		Ext.getCmp(this._currentPageId)._onQueryNextPage({form:this.xmlformForm.name,firstresult:cfg});
	}
});
sodium.__doc=window.document;
sodium.page.SimpleQueryPagingToolbar=function(cfg){
	this.pageSize=cfg.pageSize;
	this.displayInfo=true;
	this.firstText=null;
	this.prevText=null;
	this.nextText=null;
	this.lastText=null;
	this.refreshText=null;
	sodium.page.SimpleQueryPagingToolbar.superclass.constructor.call(this,cfg);
};
Ext.extend(sodium.page.SimpleQueryPagingToolbar,Ext.PagingToolbar,{
	doLoad:function(d){
		Ext.getCmp(this.ownerFormId).onQueryNextPage(d);
	},
	onLoad:function(store,r,o){
		o={params:{start:store._firstResult}};
		sodium.page.SimpleQueryPagingToolbar.superclass.onLoad.call(this,store,r,o);
	}
});