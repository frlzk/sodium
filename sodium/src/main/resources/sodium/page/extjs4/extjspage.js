/**
 * @author Liu Zhikun
 */
require(["xmlform/oldns","sodium"],function(oldns,sodium){
	window.sodium=sodium;	

define("sodium/windowConfig",["sodium"],function(sodium){
	sodium.sodium.pageRenderer="extjs4";
	sodium.page.createPromise=function(fun){return new Promise(fun);};
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
		return {menu:pageMenu,panel:new Ext.Panel(mainPanel),params:windowParams};
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
			panel._loadCachedReference(res);
		};
		panel._loadCachedReference=function(res){
			window.setTimeout(function(){
				for(var i=0;i<res.length;i++){
					var cmp=Ext.getCmp(res[i].id);
					if(cmp!=null){
						res[i].data=referenceCache[res[i].key];
						cmp[res[i].ondataload](res[i]);
					}
				};
			},1);
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
	
	return {
		createWindow:sodium.page._createBasePage
	};
});
Ext.define("sodium.page.PageBox",{
	extend:"Ext.panel.Panel",
	frame:false,
	border:false,
	bodyBorder:false,
	constructor:function(cfg){
		cfg=cfg||{};
		cfg.layout="fit";
		if(!cfg.id){
			cfg.id=sodium.page._nextId();
		}
		if(cfg.firstPageConfig.searchForm){
			cfg.buttonAlign="right";
			cfg.buttons=[{text:"确定",id:cfg.id+"_ok",handler:this._doSearchComplete(true)},
			    {text:"取消",id:cfg.id+"_cancel",handler:this._doSearchComplete(false)}];
		}
		this.callParent(arguments);
	},
	initComponent:function(){
		this.callParent(arguments);
		var mainTab=new Ext.tab.Panel({
			frame:false,
			border:false,
			bodyBorder:false,
			enableTabScroll:true
		});
		mainTab.on("afterrender",this.onPageMainRender,this);
		mainTab.on("add",this.onPageMainTabChange,this);
		mainTab.on("remove",this.onPageMainTabChange,this);
		mainTab.on("tabchange",this.onPageMainTabChange,this);
		this._pageMainTab=mainTab;
		this.add(mainTab);
		this.openPageFrame(this.firstPageClassName,this.firstPageConfig,this.firstPageData);
	},
	onPageMainRender:function(){
		var btn=Ext.getCmp(this.id+"_ok");
		if(btn){
			btn.disable();			
		}
	},
	onPageMainTabChange:function(){
		var atab=this._pageMainTab.getActiveTab();
		if(atab!=null){
			this.setBoxTitle(decodeURIComponent(atab.getPageTitle()).replace(/\+/g," "));
			atab.setPageFocus();
		}
		
		if(this._pageMainTab.items.getCount()==1){
			this._pageMainTab.items.getAt(0).tab.hide();
		}else if(this._pageMainTab.items.getCount()==2){
			var tab=this._pageMainTab;
			var tc=tab.items.getCount();
			for(var i=0;i<tc;i++){
				tab.items.getAt(i).tab.show();
			}
		}
	},
	closePage:function(page){
		this._pageMainTab.remove(page);
		if(this._pageMainTab.items.getCount()==0){
			this.closeBox();
		}
	},
	openPageDialog:function(pageClass,config,data){
		var mwi=sodium.rootPageBox.getWidth();
		var mhe=sodium.rootPageBox.getHeight();
		var winCfg={
				title:"-",
				modal:true,
				layout:'fit',
				width:mwi/3*2,
				height:mhe/2*2,
				plain: true,
				//maximizable:true,
				constrain:true,
				closeAction:"hide"
			};
		if(config.searchForm){
			winCfg.closable=false;
		}
		if(Ext.isDefined(config.reusable)){
			if(config.reusable==false){
				winCfg.closeAction="close";
			}
		}
		var me=this;
		return sodium.page.createPromise(function(resolve, reject){
			var win = new Ext.window.Window(winCfg);
			require([pageClass.replace(/\./g,"/")],me._openPageDialogCb(pageClass,config,data,win,resolve));
		});
	},
	_openPageDialogCb:function(pageClass,config,data,win,resolve){
		return function(pc){
			var pageBox=new sodium.page.PageBox({firstPageClassName:pageClass,firstPageConfig:config,firstPageData:data});
			pageBox.setBoxTitle=function(title){win.setTitle(title);};
			pageBox.closeBox=function(){win.close();};
			pageBox.addCls("pagebox-"+pageClass.toLowerCase().replace(/\./g,"-"));
			pageBox.on("createfirstpage",function(evt){
				var dim=evt.page.getSizePolicy();
				var bodyDim=Ext.getBody().getViewSize();
				var width=sodium.config.dialogPadding+dim.width,height=sodium.config.dialogPadding+dim.height;
				win.setWidth(width>bodyDim.width?bodyDim.width:width);
				win.setHeight(height>bodyDim.height?bodyDim.height:height);
				win.show();
				win.doLayout();
				resolve({dialogId:win.getId(),page:evt.page});
			});
			win.add(pageBox);
		};
	},
	openPageFrame:function(pageClass,config,data){
		var info=["open: ",pageClass,", config: ",Ext.encode(config),", data: ",Ext.encode(data)];
		sodium.page._consoleLogger.info(info.join(""));
		if(config.single){
			var tab=this._pageMainTab;
			var tc=tab.items.getCount();
			for(var i=0;i<tc;i++){
				var f=tab.items.getAt(i);
				if(f._pageClass==pageClass){
					f._doOpenPage(data);
					tab.setActiveTab(f);
					return;
				}
			}
		}
		var me=this;
		return sodium.page.createPromise(function(resolve, reject){
			require([pageClass.replace(/\./g,"/")],me._openPageFrameCb(pageClass,config,data,resolve));
		});
	},
	_openPageFrameCb:function(pageClass,config,data,resolve){
		var THIS=this;
		return function(pc){
			var newFrame=new pc(config);
			newFrame.on("afterrender",function(){
				var hasAttr=false;
				for(var k in data){hasAttr=true;break;};
				newFrame._doOpenPage(hasAttr==true?data:null);
				resolve({pageId:newFrame.getId(),page:newFrame});
			});
			newFrame.title=newFrame.getPageTitle();
			newFrame._pageClass=pageClass;
			var mainTab=THIS._pageMainTab;
			if(sodium.rootPageBox==mainTab||mainTab.items.getCount()>0){
				newFrame.closable=true;
			};
			if(mainTab.items.getCount()==0){
				mainTab._firstPageFrameId=newFrame.getId();
				if(THIS.firstPageConfig.searchForm){
					newFrame.on("selectrecord",THIS._onRecordSelect,THIS);
				}
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
			//this.fireEvent("createpage",{eventName:"createPage",page:newFrame});
		};
	},
	_onRecordSelect:function(evt){
		this._searchRecord=null;
		var btn=Ext.getCmp(this.id+"_ok");
		btn.disable();
		if(evt.select&&evt.formName==this.firstPageConfig.searchForm.formName){
			this._searchRecord=evt.record;
			btn.enable();
		}
	},
	_doSearchComplete:function(isOk){
		var me=this;
		return function(){
			Ext.getCmp(me.firstPageConfig.searchForm.searcherId).onSearchFormComplete(isOk,isOk?me._searchRecord:null);
			me.closeBox();
		}
	}
});

Ext.define("sodium.page.BasePageImpl",{
	extend:"Ext.panel.Panel",
	constructor:function(cfg){
		cfg=cfg||{};
		cfg.layout="fit";
		cfg.border=false;
		cfg.frame=false;
		cfg.bodyBorder=false;
		cfg.hideBorders=true;
		this._toolBarHeight=0;
		this._initVariable();
		this.callParent(arguments);
	},
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
		this.callParent(arguments);
		this._doTellDemiurge("createPage");
		this.addCls(this._pageCls);
	},
	afterRender:function(container){
		this.callParent(arguments);
		this._resetActionStauts();
	},
	destroy:function(){
		this._doTellDemiurge("destroyPage");
		for(var k in this._xmlformDialogs){
			this.byId(this._xmlformDialogs[k]).destroy();
		}
		this.superMethod(arguments);
	},
	
	_bindPanelListener:function(panel){
		panel.on("rowdblclick",this.onRecordDblclick,this);
		panel.on("finishedit",this._onFormCompleteEdit,this);
		panel.on("requestdata",this._onFormRequestData,this);
//		panel.on("richTextConfig",this.onCreateRichText,this);
//		panel.on("tablecellrender",this.onTableCellRender,this);
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
		btn.addCls(cfg.actionCls);
		return btn;
	},
	_createChartPanel:function(cfg){
		return new sodium.page.ChartForm(cfg);
	},
	_createFormPanel:function(cfg){
		return new sodium.page.FormPanel(cfg);
	},
	_getActionButton:function(bid){
		return Ext.getCmp(bid);
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
		var me=this;
		return this.createPromise(function(resolve, reject){
			var win=me.byId(winId);
			if(win.items&&win.items.getCount()>0){
				var pb=win.items.getAt(0);
				var mainTab=pb._pageMainTab;
				var fid=mainTab._firstPageFrameId;
				var firstFrame=Ext.getCmp(fid);
				if(mainTab.getActiveTab()!=firstFrame){
					mainTab.setActiveTab(firstFrame);
				}
				firstFrame.onResetPage();
				win.show();
				firstFrame._doOpenPage(data);
				resolve({pageId:firstFrame.getId(),page:firstFrame});
				return;
			}
			reject(pageClass);
		});
	},
	_showMessageBox:function(kind,msg){
		var me=this;
		return this.createPromise(function(resolve, reject){
			var cb2=function(v){
				if(v){
					resolve("yes");
				}else{
					reject("no");
				}
			};
			if(kind=="confirm"){
				Ext.MessageBox.confirm(me.getPageTitle(),msg,cb2);
				return;
			}
			var icon=("info"==kind?Ext.Msg.INFO:Ext.Msg.ERROR);
			var cfg = {
	                title : me.getPageTitle(),
	                msg : msg,
	                buttons: Ext.Msg.OK,
	                fn: cb2,
	                icon:icon,
	                scope : me
	            };
			Ext.Msg.alert(cfg);
		});
	},
	
	byId:function(id){
		return Ext.getCmp(id);
	},
	_doFireEvent:function(evtName,evtArgs){
		this.fireEvent(evtName,evtArgs);
	},
	fromJson:function(str){
		return Ext.decode(str);
	},
	getWindowSize:function(){
		return Ext.getBody().getViewSize();
		//return {width:sodium.rootPageBox.getWidth(),height:sodium.rootPageBox.getHeight()};
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
		if(!c._funname){
			return null;
		}
		return c._funprot[c._funname].apply(this,a);
	},
	toJson:function(obj){
		return Ext.encode(obj);
	}
});

define("sodium/page/BasePageImpl",function(){
	return sodium.page.BasePageImpl;
});

Ext.define("sodium.page.FormPanel",{
	extend:"net.sf.xmlform.extjs.FormPanel",
	initComponent:function(){
		this.lastRequestParams=null;
		if(this.actions&&this.actions.length>0){
			this.tbar=this.actions;
		}
		this.callParent(arguments);
	}
});

});