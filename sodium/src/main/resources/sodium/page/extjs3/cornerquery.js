/**
 * @author Liu Zhikun
 */
define("sodium/CornerQueryPage",["sodium"],function(sodium){
	sodium.CornerQueryPage=function(cfg){
		sodium.CornerQueryPage.superclass.constructor.call(this,cfg);
	};
	Ext.extend(sodium.CornerQueryPage,sodium.BasePage,{
		createPageQuery:function(formName,cfg){
			cfg=cfg||{};
			cfg.xmlformForm=this.getPageConfig().forms[formName];
			cfg.xmlformLayout=this.getPageConfig().layouts[formName];
			var id;
			if(cfg.id){
				id=cfg.id;	
			}else{
				id=cfg.id=this.nextPageId();
			}
			this._xmlformPanels[formName]=id;
			cfg.xmlformParamForm=this.getPageConfig().forms[formName+"Param"];
			cfg.xmlformRecordSetConfig={"insert":false,"update":false,"delete":false};
			cfg.xmlformPage=this;
			cfg._currentPageId=this.id;
			if(cfg.actions){
				cfg.actions=this._createFormActions(formName,cfg.actions);
			}
			if(typeof(cfg.xmlformReadonly)=="undefined"){
				cfg.xmlformReadonly=true;
			}
			var list=new sodium.page.SimpleQueryList(cfg);
			list.getXmlformRecordset().addListener(this,this._recordSetStatusChange);
			return list;
		},
		onCreatePageQueryBar:function(param,bars){
			return bars;
		}
	});
	return sodium.CornerQueryPage;
});