/**
 * @author Liu Zhikun
 */
define("sodium/ready",["sodium","xmlform/expression","xmlform/format"],function(sodium,xmlformExp,xmlformFormat){
	xmlformExp.Functions["user"]=function(ctx,args){
		args=xmlformExp.FunHelper.factorsToValues(ctx,args);
		if(args.length==0)
			return xmlformExp.NULL_VALUE;
		var key=args[0].getValue();
		if(key in sodium.sessionAttributes){
			return new xmlformExp.StrValue(sodium.sessionAttributes[key]);
		}else{
			return xmlformExp.NULL_VALUE;
		}
	};
	xmlformFormat.XmlformFieldFormats["datetime"]=function(style,value){
		var datePart=value.split("T");
		if(datePart.length>1)
			return datePart[0]+" "+datePart[1];
		else
			return datePart[0];
	};
	return null;
});
define("sodium/_PageImpl",["sodium","sodium/page/BasePageImpl","xmlform/model","sodium/ready","xmlform/expression"],function(sodium,BasePageImpl,xmlformModel,ready,xmlformExp){
	var expFactorMap={};
	var ExpCtx=function(pageContext){
		this.tempValues={};
		this.currentItem=null;
		this.getNamedValue=function(valueName){
			var gv=pageContext.getNamedValue(valueName);
			return xmlformExp.createValue(gv==null?null:{name:valueName,type:gv.type,value:gv.value});
		};
		this.getAttributeValue=function(valueName){
			return this.tempValues[valueName]||null;
		};
		this.setAttributeValue=function(valueName,value){
			this.tempValues[valueName]=value;
		};
		this.getPageContext=function(){
			return pageContext;
		};
		this.executeFunction=function(funName,factors){
			if(sodium.page.Functions[funName]){
				return sodium.page.Functions[funName](this, factors);
			}
			return xmlformExp.Functions[funName](this, factors);
		};
		this.doExecute=function(expStr,exp){
			var factor;
			if(expFactorMap[expStr]){
				factor=expFactorMap[expStr];
			}else{
				factor=xmlformExp.createFactorFromExpression(exp);
				expFactorMap[expStr]=factor;
			}
			return factor.evaluate(this);
		};
	};
	
	return sodium.declare(BasePageImpl,{
		actionIcons:{"save":"sodium-icon-save",
			"query":"sodium-icon-query",
			"delete":"sodium-icon-delete",
			"custom":"sodium-icon-undefined",
			"print":"sodium-icon-print",
			"create":"sodium-icon-create",
			"view":"sodium-icon-view",
			"link":"sodium-icon-link"
		},
		_initVariable:function(){
			this._xmlformPanels={};
			this._xmlformDialogs={};
			this._firstFormId=null;
			this._reopenReset=true;
			this._pageCls="page-"+this.getPageConfig().name.toLowerCase().replace(/\./g,"-");
			if(sodium.page._islogin==false){
				this.onSessionFault();
			}
			for(var type in sodium.page.ActionHandlers){
				var hand=sodium.page.ActionHandlers[type];
				if(hand.init){
					hand.init(this);
				}
			}
			var anchors=this.getPageConfig().anchors;
			for(var i=0;i<anchors.length;i++){
				var at=anchors[i];
				if(xmlformModel.Util.hasAttr(at,"minoccurs")==false){
					at.minoccurs=0;
				}
				if(xmlformModel.Util.hasAttr(at,"maxoccurs")==false){
					at.maxoccurs=2147483647;
				}
				if(at.type&&at.type=="print"){
					at.printers=this.onCreatePrintAction(at);
				}
			};
			var me=this;
			this._xmlformContext={
					createActions:function(formName){
						return me.createAttachActions(formName);
					},
					createMarkWidget:function(param){
						return me.onCreateMarkWidget(param);
					},
					decorateTableCell:function(param){
						return me.onDecorateTableCell(param);
					}
			};
			if(this.getLayoutStore){
				this._xmlformContext.getLayoutStore=function(formName){
					return me.getLayoutStore();
				};
			}
		},
		setAnchorStauts:function(act,status){
			var u=xmlformModel.Util;
			if(act.buttonIds){
				var btn=null,actvieBtns=[];
				for(var b=0;b<act.buttonIds.length;b++){
					var btn=this._getActionButton(act.buttonIds[b]);
					if(btn==null){
						continue;
					}
					actvieBtns.push(act.buttonIds[b]);
					if(u.hasAttr(status,"enable")){
						this._setBtnEnable(btn,status.enable);						
					}
					if(u.hasAttr(status,"display")){				
						this._setBtnDisplay(btn,status.display);
					}
				}
				if(actvieBtns.length!=act.buttonIds.length){
					act.buttonIds=actvieBtns;						
				}
			}
		},
		_resetActionStauts:function(rset){
			for(var i=0;i<this.getPageConfig().anchors.length;i++){
				var act=this.getPageConfig().anchors[i];
				if(!act.label){
					continue;
				}
				var status=this._isAnchorEnable(act);
				this.setAnchorStauts(act,status);
			}
		},
		_isAnchorEnable:function(act){
			var status={display:true,enable:false},matchScope="single";
			var sourceforms=this._getAnchorSourceforms(act),sourceform,minocc=act.minoccurs,maxocc=act.maxoccurs;
			if(sourceforms.length>0){
				sourceform=sourceforms[0];
			}
			if(act.sourcescope){
				matchScope=act.sourcescope;
			}
			if(sourceform=="page"){
				status.enable=true;
			}else if(sodium.page.ActionHandlers[act.type]&&sodium.page.ActionHandlers[act.type].status){
				status=sodium.page.ActionHandlers[act.type].status(this,act,status);
			}else if(matchScope=="any"){
				status.enable=true;
			}else if(!act.source){
				status.enable=true;
			}else {
				if(sourceform){
					var rset=this._findRecordSet(sourceform);
					if(rset){
						var rs=rset.getRecords();
						var selRs=rset.getSelectedRecords();
						var min=0;
						if(matchScope=="all"){
							for(var s=0;s<rs.length;s++){
								if(rs[s].getStatus()!="t"){
									//status.enable=true;
									//break;
									min++;
								}
							}
							if(min>=minocc&&min<=maxocc){
								status.enable=true;
							}
						}else if(matchScope=="single"){
							if(selRs.length==1&&this._isActionRecordEnable(act,rset,selRs[0])){
								status.enable=true;
							}
						}else if(matchScope=="multiple"){
							var selCount=selRs.length;
							for(var s=0;s<selCount;s++){
								if(rs[s].getStatus()!="t"&&this._isActionRecordEnable(act,rset,rs[s])){
									//status.enable=true;
									//break;
									min++;
								}
							}
							if(min>=minocc&&min<=maxocc){
								status.enable=true;
							}
						}else if(matchScope=="empty"){
							status.enable=rs.length==0;
						}
					}else{
						sodium.logger.debug("Anchor [page "+act.page+",action "+act.action+"] not found form recordSet: "+sourceform+"");
					}
				}
			}
//			if(act.type=="print"){
//				var panel=this.getPageForm(act.result);
//				if(panel==null||!panel.lastRequestParams){
//					status.enable=false;
//				}
//			}
			if(act.vfollowe&&act.vfollowe=="1"){
				status.display=status.enable;
			}
			status=this.onAnchorStautsChange(act,status);
			return status;
		},
		_isActionRecordEnable:function(act,recordSet,record){
			if(act.enable){
				return this.evaluateExpression({recordSet:recordSet,record:record,getNamedValue:function(n){
					var f=record.getField(n);
					if(f){
						return {value:f.getValue(),type:f.getType()};
					}
					return null;
				}},act.enable).getValue()==1?true:false;
			}
			return true;
		},
		_getAnchorSourceforms:function(anchor){
			if(anchor.sourceforms){
				return anchor.sourceforms;
			}
			anchor.sourceforms=[];
			if(anchor.source){
				anchor.sourceforms.push(anchor.source[0]);
			}
			if(anchor.source2){
				if(anchor.source2.length>0){
					var s2=anchor.source2;
					if(!s2[0] instanceof Array){
						s2=[s2];
					}
					for(var m=0;m<s2.length;m++){
						anchor.sourceforms.push(s2[m][0]);
					}
				}
			}
			return anchor.sourceforms;
		},
		createAttachActions:function(formName){
			var acts=[],anchors=this.getPageConfig().anchors;
			for(var i=0;i<anchors.length;i++){
				var at=anchors[i];
				if(!at.label){
					continue;
				}
				if(at.attach==formName){
					acts.push(at);
				}
			};
			return this.createPageActions(formName,acts);
		},
		createPageActions:function(formName,anchors){
			var buttons=[];
			for (var i = 0; i < anchors.length; i++){
				var anchor = anchors[i];
				var btn =this.createPageAction(anchor);
				if(btn!=null)
					buttons.push(btn);
			}
			return buttons;
		},
		createPageAction:function(anchor){
			if(!anchor.type||!anchor.label||anchor.label.length==0){
				return null;
			};
			if(anchor.id){}else{
				anchor.id=this.nextPageId();
				anchor.buttonIds=[];
				anchor.doing=false;
			};
			var cls="action-"+anchor.type,btnId=this.nextPageId();
			anchor.buttonIds.push(btnId);
			var btnCfg={
				id:btnId,
				label:anchor.label,
				icon:this.getActionIconClas(anchor),	
				anchorId:anchor.id,
				actionCls:cls
			};
			if(anchor.type=="print"){
				if(!anchor.printers){
					anchor.printers=this.onCreatePrintAction(anchor);
				}
				var pracItems=anchor.printers;
				if(anchor.service){
					btnCfg.handler=this._createPrintHandler(anchor,anchor);
				}else if(pracItems.length==1){
					btnCfg.handler=this._createPrintHandler(anchor,pracItems[0]);
				}else if(pracItems.length>1){
					btnCfg.children=this._createPrintSelectMenu(anchor,pracItems);
				}else{
					return null;
				}
			}else{
				btnCfg.handler=this._createActionHandler(anchor);
			}
			var btn =this._createActionButton(btnCfg);
			return btn;
		},
		onCreatePrintAction:function(anchor){
			return sodium.pagePrinters;
		},
		getActionIconClas:function(action){
			if(action.icon){
				return action.icon
			}
			if(this.actionIcons[action.type]){
				return this.actionIcons[action.type];
			};
			return "";
		},
		_createActionHandler:function(anchor){
			var THIS=this;
			return function(){
				THIS._doPageAction(anchor);
			};
		},
		_createPrintHandler:function(anchor,printCfg){
			var THIS=this;
			return function(){
				THIS._doPagePrintAction(anchor,printCfg);
			};
		},
		_createPrintSelectMenu:function(anchor,items){
			var ch=[];
			for(var p=0;p<items.length;p++){
				var item=items[p];
				if(item.children){
					ch.push({label: item.label,children:this._createPrintSelectMenu(anchor,item.children)});
				}else{
					ch.push({label: item.label,handler:this._createPrintHandler(anchor,item)});					
				}
			}
			return ch;
		},
		_onRecordSetStatusChangeCb:function(form){
			var me=this;
			return function(evt){
				evt.rootFormName=form;
				me._onRecordSetStatusChange(evt);
			}
		},
		_onRecordSetStatusChange:function(evt){
			if(evt.type=="recordSetUpdate"){
				var sel=evt.recordSet.getSelectedRecords();
				var r=null;
				if(sel.length>0){
					r=sel[0];
				}
				this._onRecordSetRecordSelect(evt.path[0],r,r!=null);
			}else if(evt.type=="recordSelect"){
				this._onRecordSetRecordSelect(evt.path[0],evt.record,evt.select);
			}else if(evt.type=="recordRemove"){
				this._resetActionStauts(evt.path[0]);
			}
			this.onRecordSetChanage(evt);
		},
		_onRecordSetRecordSelect:function(rset,record,sel){
			var formName=(record==null?null:record.getConfig().name);
			var e={
					page:this,
					record:record,
					form:formName,
					select:sel
				};
			this._resetActionStauts(rset);
			if(sel&&formName){
				this._doTriggerAnchor("recordselect",{formName:formName});
			}
			this.firePageEvent("selectrecord",e);
		},
		_doTellDemiurge:function(evt){
			if(this.demiurge&&evt.bubble){
				var d=this.byId(this.demiurge.pageId);
				if(d!=null){
					var nevt={};
					for(var k in evt){
						nevt[k]=evt[k];
					}
					nevt.anchorId=this.demiurge.anchorId;
					nevt.page=this;
					d["_doChildPageChange"](nevt);
				}
			}
		},
		_doChildPageChange:function(evt){
			this.onChildPageChange(evt);
			evt.bubble--;
			this._doTellDemiurge(evt);
		},
		onChildPageChange:function(arg){
			var anchor=this.getAnchorById(arg.anchorId);
			if(anchor){
				if(anchor.refresh){
					var res=anchor.refresh;
					if(!(res instanceof Array)){
						res=[res];
					}
					for(var i=0;i<res.length;i++){
						this._doRefreshPageForm(res[i]);					
					}
				}
				if(anchor.cascade){
					var res=anchor.cascade;
					if(!(res instanceof Array)){
						res=[res];
					}
					for(var i=0;i<res.length;i++){
						this.triggerPageAnchor({action:res[i]});					
					}
				}
			}
		},
		onAnchorStautsChange:function(anchor,status){
			return status;
		},
		closePage:function(){
			this.firePageEvent("pageclose",{page:this});
			this._getPageBox().closePage(this);
		},
		createPromise:function(fun){
			return sodium.page.createPromise(fun);
		},
		createPromiseAll:function(fun){
			return sodium.page.createPromiseAll(fun);
		},
		createPromiseRace:function(fun){
			return sodium.page.createPromiseRace(fun);
		},
		rejectPromise:function(reason){
			return this.createPromise(function(resolve, reject){
				reject(reason);
			});
		},
		objToFormData:function(obj,headers){
			var data=xmlformModel.createFormData(obj);
			if(headers){
				if(headers.firstresult){
					data.head.firstresult=parseInt(headers.firstresult,10);
				}
				if(headers.maxresults){
					data.head.maxresults=parseInt(headers.maxresults,10);
				}
				if(headers.sortfields){
					data.head.sortfields=headers.sortfields;
				}
			}
			return data;
		},
		formDataToArray:function(data,cfg){
			return xmlformModel.createDataArray(data,cfg);
		},
		callServerAction:function(param){
			if(!param.action){
				throw new Error("Not set action");
			}
			var data;
			if(param.data){
				data=param.data;
			}else{
				data={};
			}
			if(!param.callctx){
				param.callctx={};
			}
//			if(!data.head||!data.version){
//				var first=param.firstresult;
//				var maxrow=param.maxresults;
//				if(typeof(first)=="undefined"){
//					first=0;
//				}
//				if(typeof(maxrow)=="undefined"){
//					maxrow=20;
//				}
//				var hds={firstresult:first,maxresults:maxrow};
//				if(param.sortfields){
//					hds.sortfields=param.sortfields;
//				}
//				data=this.objToFormData(data,hds);
//			}else{
//				var first=param.firstresult;
//				var maxrow=param.maxresults;
//				if(typeof(first)!="undefined"){
//					data.head.firstresult=first;
//				}
//				if(typeof(maxrow)!="undefined"){
//					data.head.maxresults=maxrow;
//				}
//				if(param.sortfields){
//					data.head.sortfields=param.sortfields;
//				}
//			}
			var callfun=function(resolve, reject){
				var params={
						url:sodium.page.createActionPath(THIS._addCategoryInfo(param.action)),
						data:THIS.toJson(data),
						success:function(json){resolve(json);},
						failure:function(ee){reject(ee);}
					};
					sodium.page._sendDataToServer(params);
			};
			var THIS=this;
			return this.createPromise(callfun)
				.then(function(json){
					if(json.head.faultcode=="c.session"){
						return THIS.onSessionFault();
					}
					return json;
				},function(err){
					return THIS.rejectPromise(err);
				})
				.then(function(json){
					return {
						callctx:param.callctx,
						anchor:THIS.getAnchorById(param.anchorId),
						response:json
					};
				},function(err){
					THIS._onSendDataException(err);
					return err;
				});
		},
		_doRefreshPageForm:function(form,first,maxrow){
			var panel=this.getPageForm(form);
			if(panel){
				return panel.reloadXmlformRecordset(first,maxrow);
			}
		},
		onSessionFault:function(){
			alert("Overide onSessionFault process c.session");
			return this.createPromise(function(){});
		},
		_onSendDataException:function(resp){
			if(resp){
				this.showErrorMsg(resp.responseText);				
			}
		},
		_getPageBox:function(){
			return this.byId(this.pageBoxId);
		},
		getPageTitle:function(){
			return this.getPageConfig().title;
		},
		getPageLogger:function(){
			return sodium.logger;
		},
		openPageDialog:function(pageClass,config,data){
			this._fillDemiurge(config);
			var dkey=pageClass;
			if(config.reusekey){
				dkey=config.reusekey;
			}
			var me=this;
			var fun=function(){
				return me._getPageBox().openPageDialog(pageClass,config,data)
					.then(function(result){
						var res=true;
						if(("reusable" in config)&&config.reusable==false){
							res=false;
						}
						if(res){
							me._xmlformDialogs[dkey]=result.dialogId;							
						}
						return result;
					});
				
			};
			if(this._xmlformDialogs[dkey]){
				return this._showOldDialog(this._xmlformDialogs[dkey],pageClass,config,data)
					.then(function(result){return result;},function(){return fun();});
			}
			return fun();
		},
		openPageFrame:function(pageClass,config,data){
			var box=this._getPageBox();
			if(box){
				this._fillDemiurge(config);
				return box.openPageFrame(pageClass,config,data);
			}else{
				return this.openPageDialog(pageClass,config,data);
			}
		},
		openPageWindow:function(pageClass,config,data){
			this._fillDemiurge(config);
			return sodium.page.openPageWindow(pageClass,config,data);
		},
		openPageByTarget:function(pageClass,config,data,defTarget){
			pageClass=this._addCategoryInfo(pageClass);
			var tar=null;
			if(config.target){
				tar=config.target;
			}else if(defTarget){
				tar=defTarget;
			}else{
				throw new Error("Not found parameter target");
			}
			if(tar=="self"||tar=="frame"){
				return this.openPageFrame(pageClass,config,data);
			}else if(tar=="dialog"){
				return this.openPageDialog(pageClass,config,data);
			}else if(tar="window"){
				return this.openPageWindow(pageClass,config,data)
			}else{
				return this.openPageFrame(pageClass,config,data);
			}
		},
		createPageForm:function(formName,cfg){
			cfg=cfg||{};
			var realFormName=formName;
			if(cfg.formName){
				realFormName=cfg.formName;
				delete cfg.formName;
			}
			if(!cfg.xmlformForm){
				cfg.xmlformForm=this.getPageConfig().forms[realFormName];				
			}
			if(!cfg.xmlformLayout){
				cfg.xmlformLayout=this.getPageConfig().layouts[realFormName];				
			}
			if(!cfg.xmlformForm){
				throw new Error("Not found xmlform definition: "+realFormName);
			}
			if(!cfg.xmlformLayout){
				throw new Error("Not found xmlform layout definition: "+realFormName);
			}
			var id;
			if(cfg.id){
				id=cfg.id;	
			}else{
				id=cfg.id=this.nextPageId();
			}
//			cfg.frame=false;
//			cfg.border=false;
			cfg._currentPageId=this.id;
//			if(cfg.actions){
//				cfg.actions=this._createFormActions(formName,cfg.actions);
//			}
			cfg.xmlformContext=this._getXmlformContext(formName,realFormName);
			cfg.xmlformRecordSet=this.getRecordSet(formName);
			cfg.xmlformRecordSetConfig={};
			if(typeof(cfg.minrecords)!="undefined"){
				cfg.xmlformRecordSetConfig.min=cfg.minrecords;
			}
			if(typeof(cfg.maxrecords)!="undefined"){
				cfg.xmlformRecordSetConfig.max=cfg.maxrecords;
			}
			var setReadOnly=false;
			if(typeof(cfg.readonly)!="undefined"){
				if(cfg.readonly==true){
					setReadOnly=true;
					delete cfg.readonly;
				}
			}
			if(setReadOnly){
				cfg.xmlformReadonly=true;
				cfg.xmlformRecordSetConfig["insertable"]=false;
				cfg.xmlformRecordSetConfig["modifiable"]=false;
				cfg.xmlformRecordSetConfig["removable"]=false;
			}
			if(typeof(cfg.insertable)!="undefined"){
				cfg.xmlformRecordSetConfig["insertable"]=cfg.insertable;
			}
			if(typeof(cfg.modifiable)!="undefined"){
				cfg.xmlformRecordSetConfig["modifiable"]=cfg.modifiable;
			}
			if(typeof(cfg.modifiable)!="undefined"){
				cfg.xmlformRecordSetConfig["modifiable"]=cfg.modifiable;
			}
			if(typeof(cfg.removable)!="undefined"){
				cfg.xmlformRecordSetConfig["removable"]=cfg.removable;
			}
			
			this._xmlformPanels[formName]=id;
			if(this._firstFormId==null){
				this._firstFormId=id;		
			}
			var panel=this._createFormPanel(cfg);
			panel.getXmlformRecordset().addListener(this,this._onRecordSetStatusChangeCb(formName));
			this._bindPanelListener(panel);
			return panel;
		},
		createChartForm:function(formName,cfg){
			var id;
			if(cfg.id){
				id=cfg.id;	
			}else{
				id=cfg.id=this.nextPageId();
			}
			var rs=this.getRecordSet(formName);
			if(rs==null){
				var setCfg={insertable:false,modifiable:false,removable:false};
				if(!cfg.xmlformForm){
					setCfg.xmlform=this.getPageConfig().forms[formName];				
				}else{
					setCfg.xmlform=cfg.xmlformForm;
				}
				rs=new xmlformModel.XmlformRecordSet(setCfg);
			}
			cfg.xmlformRecordSet=rs;
			this._xmlformPanels[formName]=id;
			var chart=this._createChartPanel(cfg);
			
			return chart;
		},
		onCreateMarkWidget:function(param){
			return param.widget;
		},
		onDecorateTableCell:function(param){
			return null;
		},
		getPageForm:function(nameOrId){
			var id=nameOrId;
			if(this._xmlformPanels[nameOrId]){
				id=this._xmlformPanels[nameOrId];
			};
			return this.byId(id);
		},
		firePageEvent:function(evtName,evtArgs){
			evtArgs.type=evtName;
			this._doFireEvent(evtName,evtArgs);
			if(evtArgs.bubble){
				this._doTellDemiurge(evtArgs);
			}
		},
//		getFieldValue:function(form,field){
//			if(form=="page"){
//				var v=this._getOnePageFormField(field);
//				if(v instanceof Array){
//					return v[0];
//				}
//				return v;
//			}
//			var field=this._getOneFormField(form,field);
//			if(!field){
//				return null;
//			}
//			var v=field.getValue();
//			if(v instanceof Array){
//				return v[0];
//			}
//			return v;
//		},
		_getFieldText:function(form,field){
			if(form=="page"){
				var v=this._getOnePageFormField(field);
				if(v instanceof Array){
					return v[1];
				}
				return v;
			}
			var field=this._getOneFormField(form,field);
			if(!field){
				return null;
			}
			return field.getText();
		},
		getRecordSet:function(nameOrId){
			return this._findRecordSet(nameOrId);
//			var id=nameOrId;
//			if(this._xmlformPanels[nameOrId]){
//				id=this._xmlformPanels[nameOrId];
//			};
//			var xf=this.byId(id);
//			if(xf==null)
//				return null;
//			return xf.getXmlformRecordset();
		},
		getAnchorById:function(anchorId){
			for(var i=0;i<this.getPageConfig().anchors.length;i++){
				var act=this.getPageConfig().anchors[i];
				if(act.id==anchorId){
					return act;
				}
			}
			return null;
		},
		isPromise:function(v){
			if(typeof(v)=="undefined"||v==null/*||!(v instanceof Function)*/){
				return false;
			}
			if(!v.then){
				return false;
			}
			return true;
		},
//		i18n:function(k){
//			var str=dojoConfig.getString(k);
//			if(arguments.length>1){
//				for(var i=1;i<arguments.length;i++){
//					str=str.replace("{"+(i-1)+"}",arguments[i]);
//				}
//			}
//			return str;
//		},
		setRecordSetData:function(nameOrId,data){
			var rs=this.getRecordSet(nameOrId);
			if(rs==null){
				return false;
			};
			rs.setData(data);
			return true;
		},
		triggerAnchorByGlobalIndex:function(anchorId,index){
			var anchor=this.getAnchorById(anchorId);
			var masterForm=this._getMasterForm(anchor);
			if(masterForm==null){
				return this.createPromise(function(resolve, reject){
					reject();
				});
			}
			var me=this;
			return this.selectRecordByGlobalIndex(masterForm,index)
			.then(function(r){
				if(r)
					return me._doPageAction(anchor);
				return null;
			},function(){
				return null;
			});
		},
		selectRecordByGlobalIndex:function(formOrId,index){
			var rs=this.getRecordSet(formOrId);
			if(rs==null||index<0||index>=rs.getTotalResults()){
				return this.createPromise(function(resolve, reject){
					reject();
				});
			}
			var sel=function(rs,index){
				var records=rs.getRecords();
				if(index>=rs.getFirstResult()&&index<rs.getFirstResult()+records.length){
					var selRs=rs.getSelectedRecords();
					for(var r=0;r<selRs.length;r++){
						selRs[r].setSelected(false);
					}
					var record=records[index-rs.getFirstResult()];
					record.setSelected(true);
					return record;
				}
				return null;
			};
			var selRec=sel(rs,index);
			if(selRec!=null){
				return this.createPromise(function(resolve, reject){
					resolve({record:selRec});
				});
			}
			var panel=this.getPageForm(formOrId);
			var cf=panel.lastRequestParams;
			if(cf==null){
				return this.createPromise(function(resolve, reject){
					reject();
				});
			}
			var me=this;
			var first=index-index%cf.maxresults;
			return this._onFormRequestData({formPanel:panel,firstresult:first})
			.then(function(){
				var selRec=sel(rs,index);
				if(selRec!=null){
					return {record:selRec};
				}else{
					return null;
				}
			});
		},
		showInfoMsg:function(msg,cfg){
			return this._showMessageBox("info",msg,cfg);
		},
		showErrorMsg:function(msg,cfg){
			return this._showMessageBox("error",msg,cfg);
		},
		showConfirmMsg:function(msg,cfg){
			return this._showMessageBox("confirm",msg,cfg);
		},
		resetAllPageForm:function(){
			for(var id in this._xmlformPanels){
				var rs=this.getRecordSet(id);
				if(rs!=null){
					rs.reset();
				}
			}
		},
		refreshPageForm:function(form,first,maxrow){
			return this._doRefreshPageForm(form,first,maxrow);
		},
		nextPageId:function(){
			return sodium.page._nextId();
		},
		setPageFocus:function(){
			if(this._firstFormId){
				this.getPageForm(this._firstFormId).setXmlformFocus();
			};
		},
		triggerPageAnchor:function(cfg){
			var ans=this.getPageConfig().anchors,done=[];
			for(var i=0;i<ans.length;i++){
				var an=ans[i];
				if(cfg.type&&cfg.type=="print"&&an.printers){
					var chk=true,box=[];
					for(var k in cfg){
						if(an[k]){
							if(an[k]==cfg[k]){
								chk=true;
							}else{
								chk=false;
								break;
							}
						}else{
							box.push(k);
						}
					}
					if(chk){
						var ptn=this._matchPrintCfg(box,cfg,an.printers);
						if(ptn!=null){
							var nptn={};
							for(var nk in ptn){
								nptn[nk]=ptn[nk];
							}
							done.push(this._doPagePrintAction(an,nptn));
							break;
						}
					}
				}else{
					var chk=true;
					for(var k in cfg){
						if(an[k]&&an[k]==cfg[k]){
							chk=true;
						}else{
							chk=false;
							break;
						}
					}
					if(chk){
						done.push(this._doPageAction(an));
					}
				}
			}
			return this.createPromiseAll(done);
		},
		toPath:function(path){
			return sodium.contextPath+path;
		},
		toUrl:function(path){
			return location.protocol+"//"+location.host+sodium.contextPath+path;
		},
		_matchPrintCfg:function(names,cfg,children){
			for(var n=0;n<children.length;n++){
				var ptn=children[n];
				if(ptn.children){
					var p=this._matchPrintCfg(names,cfg,ptn.children);
					if(p!=null)
						return p;
				}
				var ptnChk=true;
				for(var m=0;m<names.length;m++){
					var name=names[m];
					if(ptn[name]&&ptn[name]==cfg[name]){
						ptnChk=true;
					}else{
						ptnChk=false;
						break;
					}
				}
				if(ptnChk){
					return ptn;
				}
			}
			return null;
		},
		_getPageAction:function(id){
			var acts=this.getPageConfig().anchors;
			for(var i=0;i<acts.length;i++){
				if(acts[i].id==id){
					return acts[i];
				}
			}
			return null;
		},
		_doPageAction:function(anchor){
			if(anchor.doing){
				return this.rejectPromise("cancel");;
			}
			anchor.doing=true;
			this._startRunAction(anchor);
			this._setJoinDisable(anchor,true);
			
			var actionFun="onCall"+this._convertDotToCamel(anchor.action),
				markFun="onCall"+this._convertDotToCamel(anchor.mark)+"Mark",
				typeFun="onCall"+this._convertDotToCamel(anchor.type)+"Action";
			var T=this,result=null;
			if(T[actionFun]){
				this._logExecuteAction(actionFun,anchor);
				result=T[actionFun](anchor);
			}else if(T[markFun]){
				this._logExecuteAction(markFun,anchor);
				result=T[markFun](anchor);
			}else if(T[typeFun]){
				this._logExecuteAction(typeFun,anchor);
				result=T[typeFun](anchor);
			}else if(sodium.page.ActionHandlers[anchor.type]){
				this._logExecuteAction("ActionHandler: "+anchor.type,anchor);
				result=sodium.page.ActionHandlers[anchor.type].execute(this,anchor);
			}else if(anchor.from=="page"){
				result=this.onRequestOpenPage(anchor);
			}else if(anchor.from=="action"){
				this._logExecuteAction("callAction",anchor);
				result=this.onRequestExecAction(anchor);
			}else{
				throw new Error("Not know how to process from: "+anchor.from);
			}
			
			if(this.isPromise(result)){
				return result.then(function(v){
						T._stopRunAction(anchor);
						anchor.doing=false;
						T._setJoinDisable(anchor,false);
						return v;
					},function(v){
						T._stopRunAction(anchor);
						anchor.doing=false;
						T._setJoinDisable(anchor,false);
						return v;
					});
			}else{
				this._stopRunAction(anchor);
				anchor.doing=false;
				this._setJoinDisable(anchor,false);
				return result;
			}
		},
		_doPagePrintAction:function(anchor,printCfg){
			var actionFun="onCall"+this._convertDotToCamel(anchor.action)+"Print",
				markFun="onCall"+this._convertDotToCamel(anchor.mark)+"PrintMark";
			var T=this;
			if(T[actionFun]){
				this._logExecuteAction(actionFun,anchor);
				return T[actionFun](anchor,printCfg);
			}else if(T[markFun]){
				this._logExecuteAction(markFun,anchor);
				return T[markFun](anchor,printCfg);
			}else{
				return this.onCallPrintAction(anchor,printCfg);
			}
		},
		_setJoinDisable:function(anchor,dis){
			if(dis==true){
				if(sodium.config.autoJoinDisable){
					var anchors=this.getPageConfig().anchors;
					for(var i=0;i<anchors.length;i++){
						var at=anchors[i];
						if(typeof(at.joindisable)=="undefined"||at.joindisable){
							this.setAnchorStauts(at,{enable:!dis});
						}
					};
				}else{
					this.setAnchorStauts(anchor,{enable:!dis});
				}
			}else{
				this._resetActionStauts(null);
			}
			
		},
		_convertDotToCamel:function(str){
			if(str==null)
				return "";
			var s=str.split(".");
			var res="";
			for(var i=0;i<s.length;i++){
				res+=s[i].charAt(0).toUpperCase()+s[i].substr(1);
			}
			return res;
		},
		_getXmlformContext:function(formName,realFormName){
			if(formName==realFormName)
				return this._xmlformContext;
			var fc={};
			for(var k in this._xmlformContext){
				fc[k]=this._xmlformContext[k];
			}
			var me=this;
			fc.createMarkWidget=function(param){
				param.rootFormName=formName;
				return me._xmlformContext.createMarkWidget(param);
			};
			fc.createActions=function(name){
				if(name==realFormName)
					name=formName;
				return me._xmlformContext.createActions(name);
			};
			return fc;
		},
		
		onRequestOpenPage:function(anchor){
			var me=this;
			var anchorSource=me._buildAnchorSourceData(anchor);
			anchorSource.style.anchorId=anchor.id;
			if(anchorSource.valid==false){
				return me.rejectPromise("cancel");
			}
			
			var data=me.formDataToArray(anchorSource.sourcedata,{attribute:true});
			if(xmlformModel.Util.hasAttr(anchorSource,"localindex")){
				for(var i=0;i<data.length;i++){
					var row=data[i];
					row["@localindex"]=anchorSource.localindex;
					row["@globalindex"]=anchorSource.globalindex;
					row["@totalrecords"]=anchorSource.totalrecords;
				}
			}
			
			return me.onBeforeOpenPage(anchor,{page:anchor.page,style:anchorSource.style,mark:anchor.mark,data:data}).then(function(res){
				if(res){
					return me.showActionConfirm({anchor:anchor}).then(function(){
						return me.openPageByTarget(res.page,res.style,data,"dialog").then(function(r){
							return r;
						});	
					},function(){
						return me.rejectPromise("cancel");
					});
				}else{
					return me.rejectPromise("cancel");
				}
			});
		},
		onBeforeOpenPage:function(anchor,cfg){
			return this.createPromise(function(resolve, reject){
				resolve(cfg);
			});
		},
		onRequestExecAction:function(anchor){
			var me=this;
			var params=me._buildAnchorSourceData(anchor);
			if(params.valid==false){
				if(params.errortext){
					return me.showErrorMsg(params.errortext).then(function(){
						return me.rejectPromise("cancel");							
					});
				}else{
					return me.rejectPromise("cancel");
				}
			}
			
			return me.onBeforeExecAction(anchor,params).then(function(params){
				if(params.valid==false){
					if(params.errortext){
						return me.showErrorMsg(params.errortext).then(function(){
							return me.rejectPromise("cancel");							
						});
					}else{
						return me.rejectPromise("cancel");
					}
				}
				
				return me.showActionConfirm({anchor:anchor}).then(function(){
					return me.createPromise(function(resolve, reject){
						return me.callServerAction({action:params.action,anchorId:anchor.id,data:params.sourcedata}).then(function(result){
							if(result&&result.response){
								return {
									success:me.applyAnchorResultData(anchor,params,result.response),
									anchorId:anchor.id,
									anchor:anchor,
									response:result.response
								};
							}else{
								return {
									success:false,
									anchorId:anchor.id,
									anchor:anchor,
									response:null
								};
							}
						}).then(function(result){
							resolve(me._doOnExecActionComplete(result.anchor,result)||result);
						});
					});
				},function(){});
			});
			
		},
		onBeforeExecAction:function(anchor,cfg){
			return this.createPromise(function(resolve, reject){
				resolve(cfg);
			});
		},
		_doOnExecActionComplete:function(anchor,data){
			var T=this;
			var funName="onCall"+this._convertDotToCamel(anchor.action)+"Complete";
			if(T[funName]){
				return T[funName](data);
			}
			funName="onCall"+this._convertDotToCamel(anchor.mark)+"MarkComplete";
			if(T[funName]){
				return T[funName](data);
			}
			funName="onCall"+this._convertDotToCamel(anchor.type)+"ActionComplete";
			if(T[funName]){
				return T[funName](data);
			}
			return data;
		},
		onCallPrintAction:function(anchor,printCfg){
			var me=this;
			var params=this._buildAnchorSourceData(anchor);
			if(params.valid==false){
				return me.rejectPromise("cancel");
			}
			params.title=me.getPageTitle();
			return me.onBeforeCallPrint(anchor,params).then(function(res){
				if(res==null){
					return null;
				}
				me.callPrintAction({action:params.action,
					title:params.title,
					data:params.sourcedata,
					download:printCfg.download,
					format:printCfg.format,
					printer:printCfg.printer,
					service:printCfg.service
				});
				return null;
			});
		},
		onBeforeCallPrint:function(anchor,cfg){
			return this.createPromise(function(resolve, reject){
				resolve(cfg);
			});
		},
		applyAnchorResultData:function(anchor,params,result){
			var sus=(result.head.faultcode=="ok"),showmsg="all";
			if(anchor.showmessage){
				showmsg=anchor.showmessage;
			}
			var msgCfg={autoClose:true};
			if(anchor.closemessage&&anchor.closemessage=="no"){
				msgCfg.autoClose=false;
			}
			if(result.head.faultstring!=""&&showmsg!="no"){
				if(sus){
					this.showInfoMsg(result.head.faultstring,msgCfg);					
				}else{
					this.showErrorMsg(result.head.faultstring,msgCfg);
				};
			};
			if(sus==false&&this._getMasterForm(anchor)!=null){
				var rs=this.getRecordSet(this._getMasterForm(anchor));
				if(rs!=null){
					rs.setData(result);							
				}
			}
			if(sus==true&&params.result){
				var panel=this.getPageForm(params.result);
				if(anchor.style&&anchor.style.remember&&params.result&&panel){
					panel.lastRequestParams=params;
				}
				
				var rs=this.getRecordSet(params.result);
				if(rs!=null){
					rs.setMaxResults(params.maxresults);
					rs.setData(result);
				}
			}
			if(sus==true){
				if(anchor.refresh){
					var refs=anchor.refresh;
					if(!(refs instanceof Array)){
						refs=[refs];
					}
					for(var r=0;r<refs.length;r++){
						if(refs[r]=="parent"){
							this._doTellDemiurge({bubble:1});
						}else{
							this._doRefreshPageForm(refs[r]);
						}
					}
				}
				if(anchor.cascade){
					var refs=anchor.cascade;
					if(!(refs instanceof Array)){
						refs=[refs];
					}
					for(var r=0;r<refs.length;r++){
						if(refs[r]=="parent"){
							this._doTellDemiurge({bubble:1});
						}else if(refs[r]=="closepage"){
							this.closePage();
						}else{
							this.triggerPageAnchor({action:refs[r]});
						}
					}
				}
			}
			return sus;
		},
		onRecordDblclick:function(e){
			this._doTriggerAnchor("recorddblclick",{formName:e.formName,once:true});
		},
//		onTableCellRender:function(e){
//			
//		},
		onRecordSetChanage:function(evt){
			
		},
//		onCreateRichText:function(evt){
//			evt.config.language='zh-cn';
//			evt.config.filebrowserUploadUrl = '/uploader/upload.php';
//		},
		callPrintAction:function(cfg){
			var data=cfg.data;
			if(!data.head||!data.version){
				cfg.data=this.objToFormData(data);
			}
			cfg.data=this.toJson(cfg.data);
			this._doPrintAction1(cfg);
		},
		evaluateExpression:function(pageCtx,expStr){
			var exp=this.getPageConfig().expressions[expStr];
			if(!exp){
				throw new Error("Not found expression: "+exp);
			}
			pageCtx.page=this;
			var ctx=new ExpCtx(pageCtx);
			return ctx.doExecute(expStr,exp);
		},
		_doPrintAction1:function(param){
			var name=(param.title?param.title:param.action)+"."+param.format;
			if(param.download==false){
				var params={
						url:sodium.page.createPrintPath(name),
						data:this.toJson(param),
						success:function(){},
						failure:function(){}
					};
					sodium.page._sendDataToServer(params);
			}else{
				var cfg={};
				for(var k in param){
					cfg[k]=param[k];
				}
				cfg.url=sodium.page.createPrintPath(name);
				cfg.data=this.toJson(param);
				this._callServerPrinter(cfg);
			}
//			var that=this;
//			param.pid=encodeURIComponent(sodium["\137\137\144\157\143"]["\164\151\164\154\145"]);
//			var params={
//				url:sodium.page.createPrintPath(param.action),
//				data:this.toJson(param),
//				success:this._doPrintAction2(param),
//				failure:function(resp){that._onSendDataException(resp)}
//			};
//			sodium.page._sendDataToServer(params);
		},
//		_doPrintAction2:function(param){
//			var that=this;
//			return function(data){
//				if(data.head.faultcode=="c.session"){
//					return that.onSessionFault();
//				}
//				if(data.head.faultcode!="ok"){
//					that.showErrorMsg(data.head.faultstring);
//					return;
//				}
//				var obj=xmlformModel.createDataArray(data);
//				if(obj.length>0&&obj[0].pid){
//					window.open(sodium.page.createPrintPath(that._getCookieValue("SODIUMPRINTID")));	
//				}
//			};
//		},
		showActionConfirm:function(params){
			if(!params.anchor.confirm){
				return this.createPromise(function(resolve, reject){
					resolve();
				});
			}
			var str=params.anchor.confirm;
			var sb =[];
			var beginIdx=str.indexOf("${"),endIdx=str.indexOf("}", beginIdx),oldEnd=0;
			while(beginIdx>=0&&endIdx>=0){
				sb.push(str.substring(oldEnd, beginIdx));
				var key=str.substring(beginIdx+2, endIdx);
				var masterForm=this._getMasterForm(params.anchor);
				
				if(masterForm==null){
					continue;
				}
				if(key.indexOf("@")!=0){
					sb.push(this._getFieldText(masterForm,key));
				}else{
					if(key=="@title"){
						var rs=this._findRecordSet(masterForm);
						if(rs!=null){
							var cap=rs.getConfig().caption;
							if(cap){
								sb.push(cap);
							}
						}
					}
				}
				
				oldEnd=endIdx+1;
				beginIdx=str.indexOf("${",endIdx+1);
				endIdx=str.indexOf("}", beginIdx);
			}
			sb.push(str.substring(oldEnd));
			return this.showConfirmMsg(sb.join(""));
		},
		_parseActionStyle:function(cfg){
			var res={};
			if(cfg.style){
				for(var k in cfg.style){
					res[k]=cfg.style[k];
				}
			};
			if(!res.maxresults){
				res.maxresults=sodium.config.maxRequestResults;
			}
			return res;
		},
		_buildAnchorSourceData:function(anchor){
			var anchorSource=this._createEmptyAnchorSource(anchor);
			if(anchor.sourcetype&&anchor.sourcetype=="query"){
				anchorSource=this.buildAnchorQuerySourceData(anchor,anchorSource);
				anchorSource.sourcedata.head.bodytype="query";
			}else{
				anchorSource=this.buildAnchorFormSourceData(anchor,anchorSource);
			}
			if(anchorSource.valid){
				var res={
					sortfields:[],
					firstresult:0,
					maxresults:sodium.config.maxRequestResults
				};
				if(anchor.style&&typeof(anchor.style.maxresults)!="undefined"){
					res.maxresults=anchor.style.maxresults;
				}
				this.setAnchorSourceLimit(anchor,anchorSource,res);
				anchorSource.firstresult=res.firstresult;
				anchorSource.maxresults=res.maxresults;
			}
			anchorSource.anchorId=anchor.id;
			return anchorSource;
		},
		buildAnchorQuerySourceData:function(anchor,anchorSource){
			dataSet={condition:{operator:"and",conditions:[]}};
			data={
					version:"1.0",
					head:{
						firstresult:0,
						maxresults:0,
						bodytype:"query"
					},
					body:dataSet
				};
			anchorSource.sourcedata=data;
			return anchorSource;
		},
		buildAnchorFormSourceData:function(anchor,anchorSource){
			var dataSet=null,data=null;
			data=this._buildParamsFromSource(anchor,anchorSource);
			if(data==null){
				anchorSource.valid=false;
			}
			//dataSet=new xmlformModel.XmlformDataSet(data);
			anchorSource.sourcedata=data;
			return anchorSource;
		},
		setAnchorSourceLimit:function(anchor,anchorSource,limit){
			anchorSource.sourcedata.head.firstresult=limit.firstresult;
			anchorSource.sourcedata.head.maxresults=limit.maxresults;
			anchorSource.sourcedata.head.sortfields=limit.sortfields;
			return anchorSource;
		},
		getSourceFieldValues:function(anchorSource,sourceScope){
			var form=sourceScope.form,scope=sourceScope.scope,names=sourceScope.fields;
			if(form=="page"){
				var srcParams=this._pageParams;
				var rows=[];
				for(var i=0;i<srcParams.length;i++){
					var srcRow=srcParams[i];
					var row={},add=false;
					for(var j=0;j<names.length;j++){
						var k=names[j];
						if(srcRow[k]){
							row[k]=srcRow[k];
							add=true;
						}
					}
					if(add){
						rows.push(row);						
					}
				}
				if(rows.length==0){
					return null;
				}
				return this.objToFormData(rows);
			}
			var set=this._findRecordSet(form);
			if(set==null){
				var me=this;
				sodium.logger.debug(function(){
					var info=["getSourceFieldValues","Not found recordSet: ",form];
					return info.join("");
				});
				return null;
			}
			if(anchorSource.validate!="no"&&this._buildFormParamCheck(anchorSource,form)==false){
				return null;
			}
			var match=function(param){
				var st=param.record.getStatus();
				if(st=="t"){
					return false;
				}
				if(scope!="all"&&param.record.isSelected()==false){
					return false;
				}
				if(scope=="single"&&sourceScope.isMaster){
					var idx=param.record.getIndex();
					anchorSource.localindex=idx;
					anchorSource.globalindex=set.getFirstResult()+idx;
					anchorSource.totalrecords=set.getTotalResults();
				}
				return true;
			};
			var filter={match:match,fields:names};
			return set.getData(filter);
		},
		_createEmptyAnchorSource:function(anchor){
			var style=this._parseActionStyle(anchor);
			var req={action:anchor.action,style:style,mark:anchor.mark,valid:true,errortext:null,sourcedata:null,validate:"full"};
			if(anchor.result){
				req.result=anchor.result;
			}
			if(anchor.validate){
				req.validate=anchor.validate;
			}
			return req;
		},
		_buildParamsFromSource:function(anchor,anchorSource){
			var masterForm=this._getMasterForm(anchor),masterData=null;
			if(masterForm==null){
				masterData=this.objToFormData({});
				this._appendConstSource(anchor,masterData);
				return masterData;
			}
			var masterResult=this._buildOneParamFromSource(anchorSource,anchor.source,anchor.sourcescope,true);
			if(masterResult==null){
				masterData=this.objToFormData({});
				this._appendConstSource(anchor,masterData);
				return masterData;
			}
			masterData=masterResult.data;
			if(!anchor.source2){
				this._appendConstSource(anchor,masterData);
				return masterData;
			}
			var masterFieldIndex={},masterMaxIndex=0,masterFields;
			var masterFields=masterData.head.forms[masterData.body.form].fields;
			for(var i=0;i<masterFields.length;i++){
				masterFieldIndex[masterFields[i]]=i;
			}
			masterMaxIndex=masterFields.length;
			var s2=anchor.source2;
			if(!(s2 instanceof Array)){
				s2=[s2];
			}
			if(s2.length>0 && !(s2[0] instanceof Array)){
				s2=[s2];
			}
			for(var m=0;m<s2.length;m++){
				var result=this._buildOneParamFromSource(anchorSource,s2[m],null,false);
				if(result==null){
					return null;
				}
				if(result.data.body.data.length==0){
					return null;
				}
				var selRow=result.data.body.data[0],selFields=result.data.head.forms[result.data.body.form].fields;
				var rows=masterData.body.data;
				for(var d=0;d<rows.length;d++){
					var row=rows[d];
					var st=row[masterFieldIndex["@status"]];
					for(var c=0;c<selRow.length;c++){
						var k=selFields[c];
						if(k=="@id"||k=="@status"){
							continue;
						}
						if(result.cover.indexOf(st)<0){
							continue;
						}
						if(!masterFieldIndex[k]){
							masterFieldIndex[k]=masterMaxIndex++;
							masterFields.push(k);
						}
						row[masterFieldIndex[k]]=selRow[c];
					}
				}
			}
			this._appendConstSource(anchor,masterData);
			return masterData;
		},
		_appendConstSource:function(anchor,masterData){
			if(!anchor.constsource){
				return;
			}
			var mfs=masterData.head.forms[masterData.body.form].fields;
			var cs=anchor.constsource;
			for(var k in cs){
				var idx=-1;
				for(var m=0;m<mfs.length;m++){
					if(mfs[m]==k){
						idx=m;
						break;
					}
				}
				if(idx==-1){
					mfs.push(k);
					idx=mfs.length-1;
				}
				var data=masterData.body.data;
				for(var i=0;i<data.length;i++){
					var row=data[i];
					row[idx]=cs[k];
				}
			}
		},
		_buildOneParamFromSource:function(anchorSource,formAndFields,sScope,isMaster){
			if(!(formAndFields instanceof Array)){
				formAndFields=[formAndFields];
			}
			var formName=formAndFields[0];
			var isDel=false,matchScope="single";
			if(sScope){
				matchScope=sScope;
			};
			var fields=formAndFields.slice(1);
			if(formAndFields.length>1){
				var ind=formAndFields[1];
				var include=false;
				if(ind.indexOf("-")>=0){
					isDel=true;
					include=true;
				}
				if(include){
					fields=fields.slice(1);
				}
			}
			
			var match=function(param){
				var st=param.record.getStatus();
				if(st=="t"){
					return false;
				}
				if(matchScope!="all"&&param.record.isSelected()==false){
					return false;
				}
				return true;
			};
			var data=null;
			var filter={match:match};
			var newNames={};
			var fieldNames={};
			var cover="pnmru";
			var delFs={},inc=1;
			if(isDel){
				inc=2;
				var names=["-"];
				for(var d=0;d<fields.length;d++){
					delFs[fields[d]]=true;
					names.push(fields[d]);
				}
				filter.fields=names;
			}else if(fields.length==1&&fields[0].substr(0,1)=="+"){
				inc=1;
				var f=fields[0].split(":");
				if(f[0]=="+f"&&f.length>1){
					cover=f[1];
				}
			}else if(fields.length>0){
				inc=3;
				newNames={};
				var names=isMaster&&formName!="page"?["@id","@status"]:[];
				for(var i=0;i<fields.length;i++){
					var field=fields[i];
					if(field.substr(0,1)=="+"){
						var f=field.split(":");
						if(f[0]=="+f"&&f.length>1){
							cover=f[1];
						}
					}else{
						var f=field.split(">"),oldName,newName;
						if(f.length==1){
							oldName=newName=f[0];
						}else{
							oldName=f[0];
							var nnstr=f[1].split(",");
							if(nnstr.length==1){
								newName=nnstr[0];
							}else{
								newName=nnstr[0];
								grade=nnstr[1];
							}
						}
						newNames[oldName]=newName;
						names.push(oldName);
						fieldNames[oldName]=true;						
					}
				}
				filter.fields=names;
			}
			
			data=this.getSourceFieldValues(anchorSource,{form:formName,scope:matchScope,fields:filter.fields,isMaster:isMaster});
			if(data==null){
				return null;
//				if(formName=='page'){
//					var srcParams=this._pageParams;
//					var rows=[];
//					for(var i=0;i<srcParams.length;i++){
//						var srcRow=srcParams[i];
//						var row={},add=false;
//						for(var k in srcRow){
//							if((inc==1)||(inc==2&&!delFs[k])||(inc==3&&fieldNames[k])){
//								row[k]=srcRow[k];
//								add=true;
//							}
//						}
//						if(add){
//							rows.push(row);						
//						}
//					}
//					if(rows.length==0){
//						return null;
//					}
//					data=this.objToFormData(rows);
//				}else{
//					if(this._buildFormParamCheck(anchorSource,formName)==false){
//						return null;
//					}
//					var rs=this._findRecordSet(formName);
//					if(rs==null){
//						throw new Error("Not found Recordset:"+formName);
//					}
//					data=rs.getData(filter);
//				}
			}
			
			var fields=data.head.forms[data.body.form].fields;
			for(var i=0;i<fields.length;i++){
				if(newNames[fields[i]]){
					fields[i]=newNames[fields[i]];
				}
			}
			return {data:data,cover:cover};
		},
		_findRecordSet:function(formName){
			// "formName/subformName" or "formName/subformName/**" 
			var fn=formName.split("/"),last=null,rs=null;
			for(var i=0;i<fn.length;i++){
				if(i==0){
					rs=this._findRecordSet2(fn[i]);
					if(rs==null){
						return null;
					}
				}else if(fn[i]=="**"&&last!=null){
					while(true){
						var sels=rs.getSelectedRecords();
						if(sels.length==0){
							return rs;
						}
						var nrs=sels[0].getField(last);
						if(nrs==null){
							return rs;
						}
						rs=nrs;
					}
				}else{
					var sels=rs.getSelectedRecords();
					if(sels.length==0){
						return null;
					}
					rs=sels[0].getField(fn[i]);
					if(rs==null){
						return null;
					}
				}
				if(fn[i]!="**"){
					last=fn[i];
				}
			}
			return rs;
		},
		_findRecordSet2:function(nameOrId){
			var id=nameOrId;
			if(this._xmlformPanels[nameOrId]){
				id=this._xmlformPanels[nameOrId];
			};
			var xf=this.byId(id);
			if(xf==null)
				return null;
			return xf.getXmlformRecordset();
		},
		_fillDemiurge:function(config){
			if(config.demiurge){
				return;
			}
			config.demiurge={pageId:this.id};
			if(config.anchorId){
				config.demiurge.anchorId=config.anchorId;
				delete config.anchorId;
			}
		},
		_buildFormParamCheck:function(anchorSource,formName){
			if(formName=="page"){
				return true;
			}
			var form=this.getPageForm(formName);
			var rs=this._findRecordSet(formName);
			if(rs==null)
				throw new Error("Not found recordset: "+formName);
			if(!rs.isValid()){
				var text=this.getFormInvalidText(formName);
				if(text!=null){
					text=text.fieldLabel+": "+text.invalidText;		
				}else{
					var fields=rs.getInvalidFields();console.log(fields)
					if(fields.length>0){
						text=fields[0].getLabel()+": "+fields[0].getInvalidText();
					}else{
						text="Invalid Data";
					}
				}
				form.setXmlformInvalidFieldFocus();
				anchorSource.errortext=text;
				anchorSource.valid=false;
				return false;
			};
			return true;
		},
		getFormInvalidText:function(formName){
			var form=this.getPageForm(formName);
			var rs=this._findRecordSet(formName);
			if(rs==null)
				throw new Error("Not found recordset: "+formName);
			var text=form.getXmlformInvalidFieldText();
			if(text==null){
				return null;
			}else{
				return text;		
			}
		},
		_doTriggerAnchor:function(trigger,params){
			var ans=this.getPageConfig().anchors,last=[];
			for(var i=0;i<ans.length;i++){
				var trig=ans[i].trigger;
				if(trig&&trig==trigger&&this._isAnchorEnable(ans[i]).enable){
					if(params.formName==null){
						last.push(this._doPageAction(ans[i]));
					}else{
						var sf=this._getAnchorSourceforms(ans[i]);
						for(var f=0;f<sf.length;f++){
							if(params.formName==sf[f]){
								last.push(this._doPageAction(ans[i]));
								break;
							}
						}
					}
					if(last.length>0&&params.once){
						break;
					}
				}
			}
			return this.createPromiseAll(last);
		},
		_onFormCompleteEdit:function(evt){
			this._doTriggerAnchor("afterlastfield",{formName:evt.formName});
		},
		_onFormRequestData:function(evt){
			var cf=evt.formPanel.lastRequestParams;
			if(cf==null){
				return this.createPromise(function(resolve, reject){
					reject();
				});
			}
			var an=this.getAnchorById(cf.anchorId);
			if(an.doing){
				return this.createPromise(function(resolve, reject){
					reject();
				});
			}
			var res={
				sortfields:[],
				firstresult:cf.firstresult,
				maxresults:cf.maxresults
			};
			if(cf.sourcedata&&cf.sourcedata.head&&cf.sourcedata.head.sortfields){
				res.sortfields=cf.sourcedata.head.sortfields;
			}
			if(typeof(evt.firstresult)!="undefined"){
				res.firstresult=evt.firstresult;
			}
			if(typeof(evt.maxresults)!="undefined"){
				res.maxresults=evt.maxresults;
			}
			if(typeof(evt.sortfields)!="undefined"){
				res.sortfields=evt.sortfields;
			}
			this.setAnchorSourceLimit(an,cf,res);
			return this.callServerAction({action:cf.action,anchorId:cf.anchorId,data:cf.sourcedata})
				.then(function(result){
					var rs=evt.formPanel.getXmlformRecordset();
					rs.setData(result.response);
					if(result.response.body.data&&result.response.body.data.length>0){
						cf.firstresult=res.firstresult;
						cf.maxresults=res.maxresults;
					}
				});
		},
		_getFirstInvalidField:function(rs){
			rs=rs.getRecords();
			for(var i=0;i<rs.length;i++){
				if(rs[i].getStatus()!="t"){
					var fs=rs[i].getFields();
					for(var f=0;f<fs.length;f++){
						if(fs[f].isValid()==false){
							if(fs[f].getModelType()=="recordset"){
								if(fs[f].getInvalidText()!=null){
									return {record:rs[i],field:fs[f]};
								}
								return this._getFirstInvalidField(fs[f]);
							}else{
								return {record:rs[i],field:fs[f]};								
							}
						}
					}
				};
			};
			return null;
		},
		_getOneFormField:function(form,field){
			var rs=this.getRecordSet(form);
			if(rs==null){
				return null;
			}
			var sel=rs.getSelectedRecords();
			if(sel.length==0){
				return null;
			}
			return sel[0].getField(field);
		},
		_getOnePageFormField:function(field){
			if(this._pageParams.length==0){
				return null;
			}
			if(typeof(this._pageParams[0][field])=="undefined"){
				return null;
			}
			return this._pageParams[0][field];
		},
		_getMasterForm:function(an){
			if(!an.source){
				return null;
			}
			if(xmlformModel.Util.isArray(an.source)==false){
				return an.source;
			}
			if(an.source.length>0){
				return an.source[0];
			}
			return null;
		},
		_logExecuteAction:function(kind,anchor){
			var me=this;
			sodium.logger.debug(function(){
				var info=[me.getPageConfig().name," ",kind," action: ",anchor.action,", page: ",anchor.page,", source: "+me._getMasterForm(anchor),", result: ",anchor.result];
				return info.join("");
			});
		},
		_addCategoryInfo:function(name){
			if(this.getPageConfig().category){
				return name+"."+this.getPageConfig().category+".0";
			}
			return name;
		},
		_startRunAction:function(an){
		},
		_stopRunAction:function(an){
		},
		resetPage:function(cfg){
			if(!this._originalParams){
				this._originalParams=[];
				var rows=this._pageParams;
				for(var r=0;r<rows.length;r++){
					var row=rows[r];
					this._originalParams[r]={};
					for(var k in row){
						this._originalParams[r][k]=row[k];
					}
				}
			}
			this._pageParams=[];
			if(cfg&&cfg.rpn){
				var ns=cfg.rpn.split(","),params=this._originalParams;
				for(var p=0;p<params.length;p++){
					var row={},param=params[p];
					for(var n=0;n<ns.length;n++){
						if(param[ns[n]]){
							row=param[ns[n]];
						}
					}
					this._pageParams[p]=row;
				}
			}
			this._doBeforeReopenPage();
			this.onResetPage();
			this.onOpenPage(this._pageParams.length>0?this._pageParams[0]:{});
		},
		_doCreatePage:function(){
			return this.onCreatePage(this.getPageConfig());
		},
		_doBeforeReopenPage:function(){
			if(this._reopenReset){
				this.resetAllPageForm();				
			}
		},
		_doOpenPage:function(data){
			this._pageParams=[];
			if(data instanceof Array){
				this._pageParams=data;
			}else if(data){
				this._pageParams=[data];				
			}
			this.onOpenPage(this._pageParams.length>0?this._pageParams[0]:{});
			this._doTriggerAnchor("pageopen",{formName:null});
			this.firePageEvent("pageopen",{page:this});
		},
		getOpenPageData:function(){
			return this._pageParams;
		},
		onCreatePage:function(pageConfig){
			
		},
		onOpenPage:function(data){
			
		},
		onResetPage:function(){
			
		}
	});
});