/**
 * @author Liu Zhikun
 */
define("sodium/BasePage",["sodium","sodium/_PageImpl"],function(sodium,PageImpl){
	return sodium.declare(PageImpl,{
		/*
		 * Get implement widget instance
		 */
		byId:function(id){
			return this.superMethod(arguments);
		},
		createAttachActions:function(formName){
			return this.superMethod(arguments);
		},
		/*
		 * create action button array
		 * [{
		 * target:	"page or action",
		 * label:	"a label",
		 * page:	"page name",
		 * source:	"form name",
		 * type:	"action type",
		 * result:	"parameters,a js object",
		 * style:	"js object,for example width height",
		 * icon:	"button icon,default by type",
		 * mark:	"a mark",
		 * action:	"action name"
		 * }
		 * ...]
		 */
		createPageActions:function(formName,actions){
			return this.superMethod(arguments);
		},
		/*
		 * create form by form name
		 * config can include: minrecords maxrecords readonly actions
		 * actions is array,if cfg incoude actions then form relative action auto create
		 */
		createPageForm:function(formName,cfg){
			return this.superMethod(arguments);
		},
		/*
		 * create chart by form name
		 * cfg:{type:,x:{field:fieldName},y:[{field:fieldName}]}
		 */
		createChartForm:function(formName,cfg){
			return this.superMethod(arguments);
		},
		/*
		 * call this clase current page
		 */
		closePage:function(){
			return this.superMethod(arguments);
		},
		/*
		 * get current page title
		 */
		getPageTitle:function(){
			return this.superMethod(arguments);
		},
		/*
		 * a logger,it has only method info
		 */
		getPageLogger:function(){
			return this.superMethod(arguments);
		},
		/*
		 * get form instance by form name or form id
		 */
		getPageForm:function(nameOrId){
			return this.superMethod(arguments);
		},
//		getFieldValue:function(nameOrId,fieldName){
//			return this.superMethod(arguments);
//		},
		/*
		 * get record set by form name or form id
		 */
		getRecordSet:function(nameOrId){
			return this.superMethod(arguments);
		},
		/*
		 * action is action name
		 * cbd is post to onLoadDataFinish
		 * param is simple js obj ,for example {name:"tom",age:20}
		 * 
		 */
//		loadData:function(action,cbd,param,firstresult,maxresults){
//			this.superMethod(arguments);
//		},
		/*
		 * generate unique id
		 */
		nextPageId:function(){
			return this.superMethod(arguments);
		},
		/*
		 * open a dialog
		 * pageClass is page name,
		 * config incude width height,it's value can be "max"
		 * data is page argument,it will by transfer to onResetPage and onOpenPage 
		 */
		openPageDialog:function(pageClass,config,data){
			return this.superMethod(arguments);
		},
		/*
		 * open a page tab
		 */
		openPageFrame:function(pageClass,config,data){
			return this.superMethod(arguments);
		},
		/*
		 * open page in a new window
		 */
		openPageWindow:function(pageClass,config,data){
			return this.superMethod(arguments);
		},
		/*
		 * default direct return status
		 * status is {display:true|false,enable:true|false}
		 */
		onActionStautsChange:function(action,status){
			return this.superMethod(arguments);
		},
		/*
		 * evt is js obj: {
		 * page:"a page instance",
		 * type:"event kind,include create saveData or destroy",
		 * form:"event source form"
		 * }
		 */
		onChildPageChange:function(evt){
			this.superMethod(arguments);
		},
		/*
		 * if a form layout include mark then this be call back
		 */
		onCreateMarkWidget:function(param){
			return this.superMethod(arguments);
		},
//		onLoadDataFinish:function(cbd,data){
//			this.superMethod(arguments);
//		},
		//action onCallXXX  onCallXXXComplete
		//mark onCallXXXMark onCallXXXMarkComplete
		//type onCallXXXAction onCallXXXActionComplete
		/*
		 * 
		 */
		onRequestOpenPage:function(anchor){
			return this.superMethod(arguments);
		},
		onBeforeOpenPage:function(anchor,param){
			return this.superMethod(arguments);
		},
		onRequestExecAction:function(anchor){
			return this.superMethod(arguments);
		},
		onBeforeExecAction:function(anchor,param){
			return this.superMethod(arguments);
		},
		onExecActionComplete:function(anchor){
			return this.superMethod(arguments);
		},
		
		////////////////////////////////////////
		/*
		onLinkAction:
		onPrintAction:
		*/
		/*
		 * called on first page create
		 */
		onCreatePage:function(){
			
		},
		/*
		 * everty page be show 
		 */
		onOpenPage:function(data){
			
		},
		/*
		 * everty page be show except first
		 */
		onResetPage:function(){
			this.superMethod(arguments);
		},
		/*
		 * on session timout or not login then call back
		 */
		onSessionFault:function(){
			return this.superMethod(arguments);
		},
		/*
		 * reset all form
		 */
		resetAllPageForm:function(){
			this.superMethod(arguments);
		},
		refreshPageForm:function(form){
			return this.superMethod(arguments);
		},
		/*
		 * direct set record set data
		 */
		setRecordSetData:function(nameOrId,data){
			return this.superMethod(arguments);
		},
		/*
		 * cb be call back when after display message
		 */
		showInfoMsg:function(msg,cb){
			return this.superMethod(arguments);
		},
		showErrorMsg:function(msg,cb){
			return this.superMethod(arguments);
		},
		showConfirmMsg:function(msb,cb){
			return this.superMethod(arguments);
		}
	});
});