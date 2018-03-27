/**
 * @author Liu Zhikun
 */
define("sodium",["xmlform/model","xmlform/validation","xmlform/panel"],function(formModel,formVal,formPanel){
	function simpleToJson(obj){
		if(formVal.isArray(obj)&&obj.length>0){
			obj=obj[0];
		}
		var a=["{"];
		for(var k in obj){
			if(a.length>1){
				a.push(",");
			}
			a.push(k);
			a.push("=");
			a.push(""+obj[k]);
		}
		a.push("}");
		return a.join("");
	};
	function pageNavStatus(page,act,status,offset){
		var pd=page.getOpenPageData(),dis=true;
		if(!pd||pd.length==0){
			dis=false;
		}
		if(pd&&pd[0]["@prevnextnav"]!="pn"){
			dis=false;
		}
		if(dis==false){
			status.display=false;
			status.enable=false;
			return status;
		}else{
			status.display=true;
		}
		var pd=page.getOpenPageData(),index=0,total=0;
		if(formModel.Util.hasAttr(pd[0],"@globalindex")){
			index=pd[0]["@globalindex"];
		}
		if(formModel.Util.hasAttr(pd[0],"@totalrecords")){
			total=pd[0]["@totalrecords"];
		}
		var newIdx=index+offset;
		status.enable=(newIdx>=0&&newIdx<total);
		return status;
	};
	function pageNavExecute(page,anchor,offset){
		var d=page.byId(page.demiurge.pageId);
		if(d==null){
			return;
		}
		var pd=page.getOpenPageData(),index=0;
		if(formModel.Util.hasAttr(pd[0],"@globalindex")){
			index=pd[0]["@globalindex"];
		}
		var newIdx=index+offset;
		page._reopenReset=false;
		return d.triggerAnchorByGlobalIndex(page.demiurge.anchorId,newIdx).
		then(function(){
			page._reopenReset=true;
		});
	};
	var tempWinCfg=null;
	var sodium={
		page:{
		},
		logger:{
			info:function(msg){},
			warn:function(w){},
			debug:function(d){}
		},
		pagePrinters:[],
//		rendererName:"UNKNOW",
		config:{},
		sessionAttributes:{}
		//declare page implement must provide
	};
	sodium.page.MainMenuHandler=function(page,config){
		return sodium.page.MenuHandler({page:page},config);
	};
	sodium.page.MenuHandler=function(page,config){
		return sodium.rootPageBox.openPageFrame(page.page,config||{},{});
	};
	sodium.page.ActionHandlers={
		"resetform":{
			status:function(page,act,status){
				status.enable=true;
				return status;
			},
			execute:function(page,anchor){
				if(anchor.forms){
					var refs=anchor.forms;
					if(!(refs instanceof Array)){
						refs=[refs];
					}
					for(var r=0;r<refs.length;r++){
						var rs=page.getRecordSet(refs[r]);
						if(rs){
							rs.reset();
						}
					}
				}else{
					page.resetAllPageForm();
				}
			}
		},
		"resetpage":{
			status:function(page,act,status){
				status.enable=true;
				return status;
			},
			execute:function(page,anchor){
				var cfg={rpn:"@prevnextnav"};
				if(anchor.rpn){
					cfg.rpn+=","+anchor.rpn;
				}
				page.resetPage(cfg);
			}
		},
		"closepage":{
			status:function(page,act,status){
				status.enable=true;
				return status;
			},
			execute:function(page,anchor){
				page.closePage();
			}
		},
		"prevpage":{
			status:function(page,act,status){
				return pageNavStatus(page,act,status,-1);
			},
			execute:function(page,anchor){
				return pageNavExecute(page,anchor,-1);
			}
		},
		"nextpage":{
			status:function(page,act,status){
				return pageNavStatus(page,act,status,1);
			},
			execute:function(page,anchor){
				return pageNavExecute(page,anchor,1);
			}
		}
	};
	sodium.page.Styles={};
	sodium.page.Functions={};
	sodium.page.createActionPath=function(actionName){
		return sodium.baseServletPath+"/action/"+actionName.replace(/\./g,"/")+"?_t="+(new Date().getTime());
	};
	sodium.page.createDownloadPath=function(fid){
		return sodium.baseServletPath+"/file/"+fid;
	};
	sodium.page.createUploadPath=function(){
		return sodium.baseServletPath+"/upload";
	};
	sodium.page.createPrintPath=function(pid){
		return sodium.baseServletPath+"/print/"+pid;
			
	};
	sodium.page.createFormPath=function(form){
		return sodium.baseServletPath+"/form/"+form;	
	};
	sodium.page.createWindowTitle=function(title){
		if(title){
			if(sodium.systemName){
				return title+" - "+sodium.systemName+" - "+sodium.companyName;				
			}
			return title;
		}
		return sodium.systemName+" - "+sodium.companyName;
	};
	//sodium.page.createPromise=function(fun){return new Promise(fun);};
	sodium.page._nextId=function(){
		sodium.page._pageIdSeek++;
		return "_11_"+sodium.page._pageIdSeek;
	};
	sodium.config.dialogPadding=40;
	sodium.config.pageBoxTabPosition="bottom";
	sodium.config.autoConfirmMessage=false;
	sodium.config.maxRequestResults=20;
	sodium.config.autoJoinDisable=true;
	sodium.config.cssStyles=["overflow","overflow-wrap","overflow-x","overflow-y"];
	sodium.config.referenceCacheTimeOut=5000;

	sodium.page._pageCache={};
	sodium.page._pageIdSeek=1;
	sodium.page._islogin=true;

	sodium.__doc=window.document;
	sodium.page._loadPageFrame=function(pageClass,cb,scope){
		require([pageClass.replace(/\./g,"/")],function(pc){
			cb.call(scope,pc);
		});
	};
	sodium.logger.debugOpenPage=function(kind,pageClass,config,data){
		sodium.logger.debug(function(){
			var info=[kind,": ",pageClass,", data: ",simpleToJson(data)];
			return info.join("");
		});
	}
	sodium.page.openPageWindow=function(pageClass,config,data){
		sodium.logger.debugOpenPage("openWindow",pageClass,config,data);
		var params="?",target=null;
		if(config){
			for(var k in config){
				if(k=="target"){
					target=config[k];
				}else if(k.indexOf("w-")==0){
					params+=k+"="+config[k]+"&";	
				}else{
					params+="p-"+k+"="+config[k]+"&";					
				}
			}
		}
		if(data){
			for(var k in data){
				params+="a-"+k+"="+config[k]+"&";
			}
		}
		var hr=sodium.baseServletPath+"/window/"+tempWinCfg.window+"/"+pageClass.replace(/\./g,"/")+params;
		if(target=="self"||target==null){
			window.location.href=hr;
			return;
		}
		return sodium.page.createPromise(function(r,j){
			r({win:window.open(hr)});
		});
	};
	sodium.page.getLocale=function(){return formPanel.locale};
	sodium.init=function(cfg){
		var sas=cfg.sessionAttributes;
		for(var f in sas){
			sodium.sessionAttributes[f]=sas[f];
		};
		var txs=cfg.typeTextes;
		for(var t in txs){
			formVal.TypeTextes[t]=txs[t];
		};
		
		if(cfg.windowConfig.logger){
			var l={
					debug:function(d){
						if(formVal.isFunction(d)){
							d=d();
						}
						console.debug(d);
					},
					info:function(d){
						if(formVal.isFunction(d)){
							d=d();
						}
						console.log(d);
					},
					warn:function(d){
						if(formVal.isFunction(d)){
							d=d();
						}
						console.warn(d);
					}
			};
			var ls=cfg.windowConfig.logger.split(",");
			for(var ll=0;ll<ls.length;ll++){
				sodium.logger[ls[ll]]=l[ls[ll]];
			}
		};
		
		tempWinCfg=cfg.windowConfig;
		//sodium.windowConfig=cfg.windowConfig;
		sodium.contextPath=cfg.contextPath;
		sodium.baseServletPath=cfg.baseServletPath;
		sodium.companyName=cfg.companyName;
		sodium.systemName=cfg.systemName;
		sodium.systemVersion=cfg.systemVersion;
		sodium.buildVersion=cfg.buildVersion;
		sodium.pagePrinters=cfg.pagePrinters;
		formPanel.locale=cfg.locale;
		var csss={};
		for(var ci=0;ci<sodium.config.cssStyles.length;ci++){
			csss[sodium.config.cssStyles[ci]]=true;
		}
		formPanel.isCssStyle=function(n){
			return csss[n]?true:false;
		};
	};
	sodium.createWindowBuilder=function(cfg,winCreator){
		var instance=null;
		return {
			getInstance:function(){
				if (!instance) {
	                instance = winCreator.createWindow(cfg);
	            }
	            return instance;
			}
		};
	};
	return sodium;
});