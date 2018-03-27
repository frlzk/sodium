/**
 * @author Liu Zhikun
 */
define("sodium/page/PageBox",["sodium","dojo/_base/declare","dojo/on","dojo/Evented","xmlform/dojo/TabContainer","dijit/registry","dojo/window","dijit/Dialog","dijit/_ConfirmDialogMixin","dijit/layout/BorderContainer","dijit/layout/StackContainer","dijit/layout/ContentPane","dojo/dom-style","dojo/_base/lang","dojo/dom-class","dijit/MenuItem","dijit/PopupMenuItem","xmlform/model","xmlform/panel"],
			function(sodium,declare,dojoOn,Evented,BaseContainer,Registry,DojoWindow,DojoDialog,_ConfirmDialogMixin,BorderContainer,StackContainer,ContentPane,DomStyle,lang,dojoClass,MenuItem,PopupMenuItem,formModel,xmlformPanel){
	var STRINGS={
			en:{maxBtn:"Maximization & Reduction",close:"Close",closeOthers:"Close Others",closeAll: "Close All"},
			zh:{maxBtn:"最大化与还原",close:"关闭",closeOthers:"关闭其他",closeAll:"全部关闭"}
		};
	function setDialogSizeByPage(dialog,page){
//		var con=dialog.get("content");
		var dim=page.getSizePolicy();
		var winBox=DojoWindow.getBox(),w=dim.bestwidth+20;
		var pad=BaseContainer.getTabControllerHeight()+dialog._bottomHeight;
		var pageWidth=dim.bestwidth+2;
		var pageHeight=dim.bestheight+2;
		var h=pageHeight+pad;
		if(pageWidth>winBox.w){
			w=winBox.w
		}
		if(pageHeight>winBox.h){
			h=winBox.h
		}
		
//		DomStyle.set(con.domNode,"width",w);
//		DomStyle.set(con.domNode,"height",h-pad);
		DomStyle.set(dialog.domNode,"width",w);
		DomStyle.set(dialog.domNode,"height",h);
		dialog.resize({w:w,h:h});
	}
	var PageDialog=declare([DojoDialog],{
		postCreate: function(){
			this._bottomHeight=0;
			this.inherited(arguments);
			DomStyle.set(this.containerNode,{padding:"0px"});
//			var me=this;
//			dojoOn(window,"resize",function(){
//				setDialogSizeByPage(me,Registry.byId(me._pageMainTabId).getChildren()[0]);
//			});
		},
		showMaximizeBtn:function(){
			if(this._maxbtnId){
				return;
			}
			var LSTRINGS=xmlformPanel.findLocale(xmlformPanel.locale,STRINGS);
			this._maxsizing=true;
			this._maxbtnId=sodium.page._nextId();
			var m=DomConstruct.toDom("<span id='"+this._maxbtnId+"' class='sodiumDlgMaxBtn sodiumDlgMaxBtnMax' title='"+LSTRINGS.maxBtn+"'></span>");
			DojoOn(m,"click",DojoLang.hitch(this,this._doMaxSize));
			DomConstruct.place(m,this.titleBar,"last");
		},
		_doMaxSize:function(){
			if(!this._oldDlgPos){
				this._oldDlgPos=DomGeometry.position(this.domNode);
			};
			var m=dojo.byId(this._maxbtnId);
			if(this._maxsizing){
				var box=DojoWindow.getBox();
				DomStyle.set(this.domNode,{left:"0px",top:"0px",width:box.w+ "px",height:box.h+ "px"});
				this._size();
				DomClass.remove(m,"sodiumDlgMaxBtnMax");
				DomClass.add(m,"sodiumDlgMaxBtnRes");
				this._maxsizing=false;
			}else{
				DomStyle.set(this.domNode,{left:this._oldDlgPos.x+"px",top:this._oldDlgPos.y+"px",width:this._oldDlgPos.w+ "px",height:this._oldDlgPos.h+ "px"});
				this._size();
				DomClass.remove(m,"sodiumDlgMaxBtnRes");
				DomClass.add(m,"sodiumDlgMaxBtnMax");
				this._maxsizing=true;
			}
		},
		_onPageSelectRecord:function(e){
			this._selectedRecord=null;
			if(this.actionBarNode&&this.okButton){
				this.okButton.set("disabled",!e.select);
				if(e.select){
					this._selectedRecord={page:e.page,form:e.form,record:e.record.getValue()};
				}
			}
		}
	});
	var ConfirmDialog=declare([PageDialog,_ConfirmDialogMixin],{
		postCreate: function(){
			this.inherited(arguments);
			this._bottomHeight=BaseContainer.getTabControllerHeight();
		},
		onExecute: function(){
			if(this._selectedRecord){
				this.emit("searchedrecord",this._selectedRecord);
			}
			this._selectedRecord=null;
		}
	});
	var PageBox=declare("sodium.page.PageBox",[BaseContainer,Evented],{
		constructor:function(cfg){
			this._ht="auto";
			this.tabPosition=sodium.config.pageBoxTabPosition;
			this.gutters=false;
			this.firstPageClass=cfg.firstPageClass;
			this.firstPageData=cfg.firstPageData;
			this.firstPageCfg=cfg.firstPageCfg;
			this.closeAllMenu=cfg.closeAllMenu;
			this.hasFirstPage=false;
		},
		postCreate:function(){
			this.inherited(arguments);
			if(this.firstPageClassName==null){
				return;
			}
//			if(this.hideBorder){
//				this.containerNode.style["border"]="none";
//			}
			var LSTRINGS=xmlformPanel.findLocale(xmlformPanel.locale,STRINGS);
			this._oldDisplay=Registry.byId(this.id+"_tablist").scrollNode.style["display"];
			this.firstPageConfig._isFirstPage=true;
			if(this.closeAllMenu){
				var menu=Registry.byId(this.id+"_tablist"+"_Menu");
				var child=menu.getChildren();
				child[0].set("label",LSTRINGS.close);
				var me=this;
				menu.addChild(new MenuItem({
					label: LSTRINGS.closeOthers,
					ownerDocument: this.ownerDocument,
					onClick: function(evt){
						var button = Registry.byNode(this.getParent().currentTarget);
						me.closeAllTabPage(button.page);
					}
				}));
				menu.addChild(new MenuItem({
					label: LSTRINGS.closeAll,
					ownerDocument: this.ownerDocument,
					onClick: function(evt){
						me.closeAllTabPage();
					}
				}));
			}
			this.openPageFrame(this.firstPageClassName,this.firstPageConfig,this.firstPageData);
		},
		closeTabPage:function(page){
			var ws=this.getChildren();
			if(ws.length>1){
				this.removeChild(page);
			}else{
				var par=this.getParent();
				if(par.hidePageTab){
					par.hidePageTab();
				}
			}
		},
		closeAllTabPage:function(view){
			var ws=this.getChildren();
			var chs=[];
			for(var i=0;i<ws.length;i++){
				chs.push(ws[i]);
			}
			for(var i=0;i<chs.length;i++){
				if(view&&chs[i]==view){
					continue;
				}
				this.removeChild(chs[i]);
			}
		},
		addChild: function(page){
			this.inherited(arguments);
			this._reShowHideTab();
		},
		removeChild: function(page){
			this.inherited(arguments);
			this._reShowHideTab();
		},
		_reShowHideTab:function(){
			if(this._ht!="auto"){
				return;
			}
			var dis="";
			if(this.getChildren().length>1){
				dis=this._oldDisplay;
			}else{
				dis="none";
			}
			var sn=Registry.byId(this.id+"_tablist").scrollNode;
			if(sn.style["display"]==dis){
				return;
			}
			sn.style["display"]=dis;
			this.layout();
		},
		setTabDisplay:function(h){
			this._ht=h;
			var sn=Registry.byId(this.id+"_tablist").scrollNode;
			if(h=="hide"||h=="none"){
				sn.style["display"]="none";
			}else if(h=="show"){
				sn.style["display"]=this._oldDisplay;
			}else{
				this._reShowHideTab();
			}
		},
		selectChild:function(page,animate){
			if(page.title){
				this.setBoxTitle(page.getPageTitle());
			}
			var res=this.inherited(arguments);
			//page.setFormFocus();
			return res;
		},
		////////////////////////////
		closePage:function(page){
			if(this._inDlg){
				if(page.id==this._firstPageFrameId){
					this.closeBox();
				}else{
					this.removeChild(page);
				}
			}else{
				this.removeChild(page);
				if(this.getChildren()==0){
					this.closeBox();
				}
			}
		},
		reopenPage:function(data){
			var ws=this.getChildren();
			for(var i=0;i<ws.length;i++){
				ws[i]._doBeforeReopenPage();
				this.selectChild(ws[i]);
				ws[i]._doOpenPage(data);
				return true;
			}
			return false;
		},
		setAutoConfirmMessage:function(t){
			sodium.config.autoConfirmMessage=t;
		},
		isAutoConfirmMessage:function(t){
			return sodium.config.autoConfirmMessage;
		},
		openPageDialog:function(pageClass,config,data){
			sodium.logger.debugOpenPage("openDialog",pageClass,config,data);
			var me=this;
			return sodium.page.createPromise(function(resolve, reject){
				var box=DojoWindow.getBox();
				var width=parseInt(box.w/3*2,10),height=parseInt(box.h/3*2,10);
				var widthOffset=20,heightOffset=54;
				var vtStyle="width:"+(width-widthOffset)+"px;height:"+(height-heightOffset)+"px;";
				var diaStyle="width:"+width+"px;height:"+height+"px";
				var diaCfg={width:width,height:height,doLayout:true,style:diaStyle,maxRatio:0.99999};
				var winClass=PageDialog;
				if(config.searchPage){
					winClass=ConfirmDialog;
				}
				var win=new winClass(diaCfg);
				require([pageClass.replace(/\./g,"/")],me._openPageDialogCb(pageClass,config,data,win,vtStyle,resolve));
			});
		},
		_openPageDialogCb:function(pageClass,config,data,win,borderStyle,resolve){
			return function(pc){
				var pageBox=new PageBox({firstPageClassName:pageClass,firstPageConfig:config,firstPageData:data});
				pageBox.setBoxTitle=function(title){win.set("title",sodium.page.createWindowTitle(title));};
				pageBox.closeBox=function(){
					if(("reusable" in config)&&config.reusable==false){
						win.destroy();
					}else{
						win.hide();
					}
				};
				pageBox._inDlg=true;
				dojoClass.add(pageBox.domNode,"pagebox-"+pageClass.toLowerCase().replace(/\./g,"-"));
				var con=new BorderContainer({gutters:false});
				pageBox.region="center";
				con.addChild(pageBox);
				win.set("content",con);
				win._pageMainTabId=pageBox.id;
				var showWin=function(evt){
//					setDialogSizeByPage(win,evt.page);
					var dim=evt.page.getSizePolicy();
					var winBox=DojoWindow.getBox(),w=dim.bestwidth+20;
					var pad=BaseContainer.getTabControllerHeight()+win._bottomHeight;
					var pageWidth=dim.bestwidth+2;
					var pageHeight=dim.bestheight+2;
					var h=pageHeight+pad;
					if(pageWidth>winBox.w){
						w=winBox.w
					}
					if(pageHeight>winBox.h){
						h=winBox.h
					}
					DomStyle.set(con.domNode,"width",w);
					DomStyle.set(con.domNode,"height",h-pad);
					DomStyle.set(win.domNode,"width",w);
					DomStyle.set(win.domNode,"height",h);
					win.resize({w:w,h:h});
					win.set("title",evt.page.getPageTitle());
					win.show();
					resolve({dialogId:win.id,page:evt.page,fresh:true});
				};
				if(pageBox.getChildren().length==1){
					showWin({page:pageBox.getChildren()[0]});
				}else{
					pageBox.on("createfirstpage",showWin);
					pageBox.on("selectrecord",lang.hitch(win,"_onPageSelectRecord"));
				}
			};
		},
		openPageFrame:function(pageClass,config,data){
			sodium.logger.debugOpenPage("openFrame",pageClass,config,data);
			if(config&&config.single){
				var dkey=pageClass;
				if(config.reusekey){
					dkey=config.reusekey;
				}
				var tc=this.getChildren();
				for(var i=0;i<tc.length;i++){
					var f=tc[i];
					if(f._reusekey==dkey){
						f._doOpenPage(data);
						this.selectChild(f);
						return sodium.page.createPromise(function(resolve, reject){
							resolve({pageId:f.id,page:f});
						});
					}
				}
			}
			var me=this;
			return sodium.page.createPromise(function(resolve, reject){
				require([pageClass.replace(/\./g,"/")],me._openPageFrameCb(pageClass,config||{},data||{},resolve));
			});
		},
		_openPageFrameCb:function(pageClass,config,data,resolve){
			var THIS=this;
			return function(pc){
				var newFrame=new pc(config);
				window.setTimeout(function(){
					var hasAttr=false;
					for(var k in data){hasAttr=true;break;};
					newFrame._doOpenPage(hasAttr==true?data:null);
					resolve({pageId:newFrame.id,page:newFrame});
				},1);
				newFrame.title=newFrame.getPageTitle();
				var dkey=pageClass;
				if(config.reusekey){
					dkey=config.reusekey;
				}
				newFrame._reusekey=dkey;
				if(formModel.Util.hasAttr(config,"closable")){
					newFrame.closable=config.closable;
				}else{
					if(sodium.rootPageBox==THIS||THIS.getChildren()>0){
						newFrame.closable=true;
					};
				}
				if(THIS.getChildren().length==0){
					THIS._firstPageFrameId=newFrame.id;
				};
				newFrame.pageBoxId=THIS.id;
				if(config.target){
					if(config.target=="self"&&THIS.getChildren().length>0){
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
				THIS.addChild(newFrame);
				if(formModel.Util.hasAttr(config,"autotactive")){
					if(config.autotactive==true){
						THIS.selectChild(newFrame);
					}
				}else{
					THIS.selectChild(newFrame);					
				}
				if(THIS.getChildren().length==1){
					THIS.emit("createfirstpage",{page:newFrame});
					newFrame.on("selectrecord",function(e){
						if(e.select){
							THIS.emit("selectrecord",e);							
						}
					});
				}
				//this.fireEvent("createpage",{type:"createPage",page:newFrame});
			};
		}
	});
	return PageBox;
});
define("sodium/page/BasePageImpl",["dojo/_base/declare","dojo/Evented","dijit/layout/BorderContainer","dijit/registry","dijit/Toolbar","dijit/form/Button","dijit/form/ComboButton","dijit/Menu","dijit/MenuItem","dijit/PopupMenuItem","xmlform/dojo/FormPanel","dojo/dom-geometry","dojo/cookie","dojo/window","xmlform/dojo/BorderContainer","xmlform/dojo/LayoutUtil","dojo/dom-style","dojo/json","dojo/dom","dojo/dom-construct","dijit/ConfirmDialog","sodium","sodium/page/ChartForm"],
			function(declare,Evented,BaseContainer,DijitRegistry,DojoToolbar,DojoButton,ComboButton,Menu,MenuItem,PopupMenuItem,FormPanel,domGeometry,dojoCookie,DojoWindow,FormBorder,LayoutUtil,domStyle,JSON,dojoDom,domConstruct,ConfirmDialog,sodium,ChartForm){
	var XFormPanel=declare([FormPanel],{
		postCreate:function(){
			this.lastRequestParams=null;
			this.inherited(arguments);
		}
	});
	var messageBoxDlg={};
	return declare("sodium.page.BasePageImpl",[BaseContainer],{
		constructor:function(cfg){
			cfg=cfg||{};
			this.gutters=false;
			this.superMethod=this.inherited;
			this._toolBarHeight=0;
			this._initVariable();
		},
		postCreate:function(){
			this.inherited(arguments);
			this.items=[];
			var ch=this._doCreatePage();
			if(ch!=null){
				ch.region="center";
				this.addChild(ch);
				this._centerWidgetId=ch.id;
			}
			var tbar=this.createAttachActions("page",this.getPageConfig().anchors);
			if(tbar&&tbar.length>0){
				var tb=new DojoToolbar({region:"top"});
				for(var i=0;i<tbar.length;i++){
					tb.addChild(tbar[i]);
				}
				this.addChild(tb);
				this._toolBarHeight=LayoutUtil.getToolbarHeight();
			}
			//dojo.addClass(this.domNode, "aaaaaaaa");
			this._resetActionStauts();
			this.on("show",this.setPageFocus,this);
			this._doTellDemiurge("createPage");
			this.emit("aftercreatepage",this);
		},
		destroy:function(){
			this._doTellDemiurge("destroyPage");
			for(var k in this._xmlformDialogs){
				var dlg=this.byId(this._xmlformDialogs[k]);
				if(dlg){
					dlg.destroy();
				}
			}
			this.superMethod(arguments);
		},
		
		_bindPanelListener:function(panel){
			panel.on("requestdata",dojo.hitch(this,"_onFormRequestData"));
			panel.on("rowdblclick",dojo.hitch(this,"onRecordDblclick"));
			panel.on("finishedit",dojo.hitch(this,"_onFormCompleteEdit"));
		},
		_createActionButton:function(cfg){
			var btnCfg={
					id:cfg.id,
					scope:this,
					label:cfg.label,
					iconCls:cfg.icon,	
					anchorId:cfg.anchorId
				};
			if(cfg.children){
				btnCfg.dropDown=this._createActionMenuButton(cfg.children);
				return new ComboButton(btnCfg);
			}else{
				btnCfg.onClick=cfg.handler;
			};
			return new DojoButton(btnCfg);
		},
		_createActionMenuButton:function(items){
			var menu=new Menu({id:this.nextPageId()});
			for(var p=0;p<items.length;p++){
				var item=items[p];
				if(item.children){
					var menus=this._createActionMenuButton(item.children);
					var bi=new PopupMenuItem({label:item.label,popup:menus});
					menu.addChild(bi);
				}else{
					menu.addChild(new MenuItem({label:item.label,onClick:item.handler}));					
				}
			}
			return menu;
		},
		_createChartPanel:function(cfg){
			return new ChartForm(cfg);
		},
		_createFormPanel:function(cfg){
			var panel=new XFormPanel(cfg);
			panel.ownerPageId=this.id;
			return panel;
		},
		_getActionButton:function(bid){
			return this.byId(bid);
		},
		_getCookieValue:function(k){
			return dojoCookie(k);
		},
		_setBtnEnable:function(btn,en){
			btn.set("disabled",!en);
		},
		_setBtnDisplay:function(btn,en){
			if(en==false){
				domStyle.set(btn.domNode,"display","none");
			}else{
				domStyle.set(btn.domNode,"display","");
			}
		},
		_showOldDialog:function(winId,pageClass,config,data){
			sodium.logger.debugOpenPage("openDialog",pageClass,config,data);
			var THIS=this;
			return this.createPromise(function(resolve, reject){
				var win=THIS.byId(winId);
				if(win){
					var tab=THIS.byId(win._pageMainTabId);
					if(tab.reopenPage(data)){
						win.show();
						resolve({dialogId:winId,page:tab,fresh:false});
						return;
					}
				}
				reject(pageClass);
			});
		},
		_showMessageBox:function(kind,msg,cfg){
			var me=this;
			return this.createPromise(function(resolve, reject){
				if(me._getPageBox().isAutoConfirmMessage()){
					console.debug("AutoConfirmMessage",kind,msg);
					resolve("yes");
					return;
				}
				var done=false;
				var callYes=function(){
					if(done==true){
						return;
					}
					done=true;
					okh.remove();
					canh.remove();
					dlg.hide();
					resolve("yes");
				};
				var dlg=me._getMessageBoxDlg(kind);
				var okh=dlg.okButton.on("click",callYes);
				var canh=dlg.cancelButton.on("click",function(){okh.remove();canh.remove();reject("no");});
				if(kind=="info"&&sodium.config.autoHideMessageBoxDelay&&cfg&&cfg.autoClose==true){
					window.setTimeout(callYes,sodium.config.autoHideMessageBoxDelay);
				}
				dlg.set("content","<div class='messageBoxContent'><span class='"+kind+"MessageBoxIcon'></span><span class='"+kind+"MessageBoxText'>"+msg+"</span></div>");
				dlg.show();
			});
		},
		_getMessageBoxDlg:function(kind){
			if(messageBoxDlg[kind]){
				return messageBoxDlg[kind];
			}
			var title=kind;
			if(kind=="info")
				title="infomation";
			var dlg=new ConfirmDialog({closable:false,title:title.charAt(0).toUpperCase()+title.substr(1)});
			if(kind!="confirm"){
				domStyle.set(dlg.cancelButton.domNode,"display","none");
			}
			messageBoxDlg[kind]=dlg;
			return dlg;
		},
		_callServerPrinter:function(params){
			if(params.service=="htmlprint"){
				if(!XFormPanel._printFrame){
					var cb=function(w){
						w.print();
					};
					var fid=this.nextPageId();
					var frameDiv=domConstruct.toDom("<iframe id='"+fid+"_frame' name='"+fid+"_frame' style='display:none;'></iframe>");
					if (frameDiv.attachEvent){
						frameDiv.attachEvent("onload", function(){
							cb(frameDiv.contentWindow);
						});
					} else {
						frameDiv.onload = function(){
							cb(frameDiv.contentWindow);
						};
					}
					var formDiv=domConstruct.toDom("<div id='"+fid+"_div' style='display:none'><form id='"+fid+"' name='"+fid+"' action='' method='POST' target='"+fid+"_frame'><input id='"+fid+"_param' name='data'/></div>");
					document.body.appendChild(frameDiv);
					document.body.appendChild(formDiv);
					XFormPanel._printFrame=dojoDom.byId(fid);
					XFormPanel._printFrameParam=dojoDom.byId(fid+"_param");
				}
				XFormPanel._printFrameParam.value=params.data;
				var form=XFormPanel._printFrame;
				form.action=params.url;
				form.submit();
				return;
			}
			if(!XFormPanel._printForm){
				var fid=this.nextPageId();
				var formDiv=domConstruct.toDom("<div id='"+fid+"_div' style='display:none'><form id='"+fid+"' name='"+fid+"' action='' method='POST' target='_blank'><input id='"+fid+"_param' name='data'/></div>");
				document.body.appendChild(formDiv);
				XFormPanel._printForm=dojoDom.byId(fid);
				XFormPanel._printFormParam=dojoDom.byId(fid+"_param");
			}
			XFormPanel._printFormParam.value=params.data;
			var form=XFormPanel._printForm;
			form.action=params.url;
			form.submit();
		},
		
		byId:function(id){
			return DijitRegistry.byId(id);
		},
		_doFireEvent:function(evtName,evtArgs){
			this.emit(evtName,evtArgs);
		},
		fromJson:function(str){
			return JSON.parse(str,true);
		},
		getWindowSize:function(){
			var box=DojoWindow.getBox();
			return {width:box.w,height:box.h};
		},
		getSizePolicy:function(){
			if(this.maximize){
				var dim=this.getWindowSize();
				return {bestwidth:dim.width,bestheight:dim.height};
			}
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
//		superMethod:function(a){
//			var c=a.callee;
//			return c._funprot[c._funname].apply(this,a);
//		},
		toJson:function(obj){
			return dojo.toJson(obj);
		}
	});
});

define("sodium/widget/SearchField",["dojo/_base/declare","sodium","dojo/Deferred","xmlform/dojo/_SearchField","sodium/page/PageBox","xmlform/dojo/FormWidgets","dijit/registry"],
      function(declare,sodium,Deferred,_SearchField,PageBox,FormWidgets,registry){
 	var SF=declare("sodium.widget.SearchField",[_SearchField],{
 		openSearchDialog:function(param){
 			var that=this;
 			var args=param.arguments||{},idx,argPre="arg.value.",argLen=argPre.length;
 			for(var k in param.params){
 				idx=k.indexOf(argPre);
 				if(idx==0){
 					args[k.substr(argLen)]=param.params[k];
 				}
 			}
 			if(that.searchDialogId){
 				var dlg=registry.byId(that.searchDialogId);
 				if(that.searchPage==param.params.page){
 					var tab=registry.byId(dlg._pageMainTabId);
 					tab.reopenPage(args);
 					dlg.show();
 					return;
 				}else{
 					dlg.destroy();
 				}
 			}
 			sodium.rootPageBox.openPageDialog(param.params.page,{searchPage:param.params.page,searchForm:param.params.form},args).then(function(result){
 				that.searchDialogId=result.dialogId;
 				registry.byId(result.dialogId).on("searchedrecord",function(e){that.setSearchResult(e.record)});
 			});
 			that.searchPage=param.params.page;
	    },
	    destroy: function(){
	    	var dlg=registry.byId(this.searchDialogId);
	    	if(dlg){
	    		dlg.destroy();
	    	}
	    	this.inherited(arguments);
	    }
 	});
 	FormWidgets.SearchFieldClass=SF;
 	return SF;
 });

define("sodium/widget/FileField",["dojo/_base/declare","sodium","xmlform/panel","xmlform/dojo/_FileField","xmlform/dojo/FormWidgets"],
 		function(declare,sodium,panel,_FileField,FormWidgets){
	panel.getFileUploadUrl=function(){
		return sodium.page.createUploadPath();
	};
	panel.getFileDownloadUrl=function(id){
		return sodium.page.createDownloadPath(id);
	};
 	var FF=declare("sodium.widget.FileField",[_FileField],{
 		postCreate:function(){
 			if(!this.uploadUrl){
 				this.uploadUrl=panel.getFileUploadUrl();
 			}
 			this.inherited(arguments);
 		},
 	    openSearchDialog:function(param){},
 		onUploadComplete: function(/*Object*/ json){
 			this.inherited(arguments);
 			if(json.faultcode!="ok"){
 				this.setXffValid(json.faultstring);
 				return;
 			}
 			this.setXffValid(null);
 			if(json.files){
 				if(json.files.length>0){
 					var file=json.files[0];
 					this.setFileValue(file.id,file.name); 					
 				}
 			}
 		},
 		onUploadError: function(/*Object or String*/ evtObject){
 			this.inherited(arguments);
 		}
 	});
 	FormWidgets.FileFieldClass=FF;
 	return FF;
 });

define("sodium/widget/PictureField",["dojo/_base/declare","xmlform/panel","xmlform/dojo/FormWidgets","dijit/layout/BorderContainer","dijit/layout/ContentPane","xmlform/dojo/WdtUtil","dojo/dom-style"],
 		function(declare,panel,FormWidgets,BorderContainer,ContentPane,WdtUtil,domStyle){
 	var FF=declare("sodium.widget.PictureField",[ContentPane],WdtUtil.commonAttribute({
 		postCreate:function(){
 			this.inherited(arguments);
 			if(this.xmlformWidget&&this.xmlformWidget.params&&this.xmlformWidget.params.style){
 				var style=this.xmlformWidget.params.style.split(";");
 				for(var i=0;i<style.length;i++){
 					var s=style[i].split(":");
 					if(s.length==2){
 						domStyle.set(this.domNode,s[0],s[1]); 					 						
 					}
 				}
 			}
 		},
 		setXffValue:function(v){
 			var id=v;
 			if(v instanceof Array){
 				id=v[0];
 			}
 			if(id){
 				this.set("content","<img src=\""+panel.getFileDownloadUrl(id)+"\"\"/>"); 				
 			}else{
 				this.set("content",""); 	
 			}
 		}
 	}));
 	FormWidgets["string-picture"]=FF;
 	return FF;
 });

define("sodium/windowConfig",["sodium","dojo/_base/declare","dijit/layout/BorderContainer","dijit/form/SimpleTextarea","dojo/_base/xhr","dijit/layout/TabContainer",
             		         "sodium/page/PageBox","dijit/registry","dojo/window","dojo/dom-construct","xmlform/panel","sodium/widget/SearchField","sodium/widget/FileField","sodium/widget/PictureField","xmlform/dojo/FormWidgets"],
     function(sodium,declare,BorderContainer,SimpleTextarea,xhr,TabContainer,PageBox,registry,DojoWindow,DomConstruct,xmlformPanel,SearchField,FileField,PictureField,FormWidgets){
	sodium.declare=declare;
	sodium.pageRenderer="dojo1_10";
	
//	var wbody=document.getElementsByTagName("body")[0];
//	wbody.appendChild(DomConstruct.create("applet",{
//		id:"sodiumAppletPrinter",code:"AppletPrinter.class",width:30,height:30,
//		style:"position:absolute;top:-500;left:-500"}));
	
	sodium.page._createBasePage=function(winCfg){
		var pageCfgParams=winCfg.pageConfig,
			pageDataParams=winCfg.pageParams,
			pageClassName=winCfg.pageClass,
			pageMenu=winCfg.windowMenu,
			windowParams=winCfg.windowConfig;
		sodium.page._createBasePage_env(xhr,registry,xmlformPanel);
		sodium.page.createPromise=function(fun){
			return FormWidgets.createPromise(fun);
		};
		sodium.page.createPromiseAll=function(fun){
			return FormWidgets.createPromiseAll(fun);
		};
		sodium.page.createPromiseRace=function(fun){
			return FormWidgets.createPromiseRace(fun);
		};
		var pageBox=new PageBox({region:"center",firstPageClassName:pageClassName,firstPageConfig:pageCfgParams,firstPageData:pageDataParams,closeAllMenu:true});
		pageBox.setBoxTitle=function(f){
			sodium["\137\137\144\157\143"]["title"]=sodium.page.createWindowTitle(f);
		};
		pageBox.closeBox=function(){
			//window.close();
		};
		sodium.rootPageBox=pageBox;
		var mainPanel=new BorderContainer({
			region:"center",
			design: "headline",
			gutters:false
		});
		mainPanel.addChild(pageBox);
		if(window.parent&&window.parent.onSodiumPageBoxLoaded){
			window.parent.onSodiumPageBoxLoaded(pageBox);
		}
		return {menu:pageMenu,panel:mainPanel,params:windowParams,openPage:sodium.page.MainMenuHandler};
	};
	sodium.page._createBasePage_env=function(xhr,registry,xmlformPanel){
		var MAX_REQ=60000;
		var requsting={},referenceCache={};
		function cleanRequest(){
			var ct=new Date().getTime();
			for(var k in requsting){
				var req=requsting[k];
				if(req.time<ct){
					delete requsting[k];
				}
			}
			var cache=referenceCache;
			for(var k in cache){
				var req=cache[k];
				if(req.time<ct){
					delete cache[k];
				}
			}
		};
		xmlformPanel.loadReference=function(param){
			if(param.length==0){
				return;
			}
			cleanRequest();
			var reqArray=[];
			var res=[];
			for(var i=0;i<param.length;i++){
				var noCache=false;
				if(param[i].cache&&param[i].cache=="no"){
					noCache=true;
				}
				if(referenceCache[param[i].key]&&noCache==false){
					res.push(param[i]);
				}else if(requsting[param[i].key]){
					requsting[param[i].key].push(param[i]);
				}else{
					requsting[param[i].key]=[param[i]];
					requsting[param[i].key].time=new Date().getTime()+MAX_REQ;
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
					for(var key in param[i].params){
						innerFields.push(key);
						innerData.data[0].push(param[i].params[key]);
					};
				}
			};
			if(reqArray.length>0){
				xhr.post({
					url: sodium.page.createActionPath("reference"),
					handleAs:"json",
					contentType:"application/json",
					load: sodium.page.onLoadReferenced,
		//			error: function(resp){Ext.Msg.alert('Error', resp.responseText);},
					postData: dojo.toJson(reqArray)
				});
			};
			xmlformPanel._loadCachedReference(res);
		};
		xmlformPanel._loadCachedReference=function(res){
			window.setTimeout(function(){
				for(var i=0;i<res.length;i++){
					var cmp=registry.byId(res[i].id);
					if(cmp!=null){
						res[i].data=referenceCache[res[i].key];
						cmp[res[i].ondataload](res[i]);
					}
				};
			},1);
		};
		sodium.page.onLoadReferenced=function(resp){
			var array=resp;
			for(var i=0;i<array.length;i++){
				if(array[i].data.head.faultcode=="ok"){
					if(!referenceCache[array[i].key]){
						var t=sodium.config.referenceCacheTimeOut;
						if(array[i].cachetime){
							t=new Numeric(array[i].cachetime);
						}
						referenceCache[array[i].key]=array[i].data;
						referenceCache[array[i].key].time=new Date().getTime()+t;
					}
					var reqs=requsting[array[i].key];
					if(!reqs){
						var cmp=registry.byId(array[i].id);
						if(cmp!=null){
							cmp[array[i].ondataload](array[i]);
						}
						continue;
					}
					for(var r=0;r<reqs.length;r++){
						var req=reqs[r];
						var cmp=registry.byId(req.id);
						if(cmp!=null){
							req.data=array[i].data;
							cmp[req.ondataload](req);
						}
					}
					delete requsting[array[i].key];
				}else if(array[i].data.head.faultcode!="c.session"&&!array[i].ignorefault){
					alert(/*'Error', */array[i].data.head.faultcode+": "+array[i].data.head.faultstring); 
				}
			}
		};
		sodium.page._sendDataToServer=function(params){
			xhr.post({
				url: params.url,
				handleAs:"json",
				contentType:"application/json",
				load: params.success,
				error: params.failure,
				postData: params.data
			});
		};
	};
	return {
		createWindow:sodium.page._createBasePage
	};
});
