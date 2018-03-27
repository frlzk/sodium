{//BEGIN_DECLARE
	name:sys.BaseQueryPage,
	comment:查询基本页
}//END_DECLARE

define(["sodium","sys/BasePage","sys/Util"],function(sodium,BasePage,Util){
	return sodium.declare(BasePage,{
		onCreateConfig:null,
		onCreatePage:function(){
			var cfg=this.getQueryParam();
			if(!cfg||!cfg.formName){
				throw new Error("Must provider formName");
			}
			var paramForm=this.createPageForm(cfg.formName+"Param",{minrecords:1,maxrecords:1});
			paramForm.height=cfg.height;
			paramForm.region="north";
			var resultForm=this.createPageForm(cfg.formName,{readonly:true,pagesize:cfg.pageSize});
			resultForm.region="center";
			var mainPanel=Util.createBorderLayout([paramForm,resultForm],{});
			return mainPanel;
		},
		getQueryParam:function(){
			return {formName:null,pageSize:20,height:50};
		},
		onPreQueryAction:function(cfg){
			var formName=this.getQueryParam().formName+"Param";
			var paramRs=this.getRecordSet(formName);
			if(paramRs.isValid()==false){
				return null;
			}
			var fs=paramRs.getRecords()[0].getFields();
			for(var i=0;i<fs.length;i++){
				cfg.data[fs[i].getName()]=fs[i].getValue();
			}
			return cfg;
		}
	});
});